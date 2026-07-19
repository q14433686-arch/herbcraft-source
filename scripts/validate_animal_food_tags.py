#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def minecraft_version() -> str:
    props = ROOT / 'gradle.properties'
    if props.exists():
        for line in props.read_text(encoding='utf-8').splitlines():
            if line.startswith('minecraft_version='):
                return line.split('=', 1)[1].strip()
    return '26.2'

def find_vanilla_client() -> tuple[Path | None, list[Path]]:
    import os
    version = minecraft_version()
    candidates: list[Path] = []
    env = os.environ.get('HERBCRAFT_VANILLA_CLIENT_JAR')
    if env:
        candidates.append(Path(env))
    candidates.extend([
        Path(f'/home/user/cache/minecraft/client-{version}.jar'),
        Path.home() / '.gradle' / 'caches' / 'fabric-loom' / version / 'minecraft-client.jar',
        Path.home() / '.gradle' / 'caches' / 'fabric-loom' / version / 'minecraft-merged.jar',
        ROOT / '.gradle' / 'loom-cache' / version / 'minecraft-client.jar',
    ])
    for candidate in candidates:
        if candidate.exists():
            return candidate, candidates
    return None, candidates

VANILLA_CLIENT, VANILLA_CLIENT_CANDIDATES = find_vanilla_client()
TAG_DIR = ROOT / 'core/src/main/resources/data/minecraft/tags/item'
WRONG_TAG_DIRS = [
    ROOT / 'core/src/main/resources/data/herbcraft/tags/items',
    ROOT / 'core/src/main/resources/data/minecraft/tags/items',
]

EXPECTED = {
    'cow_food': ['minecraft:short_grass', 'minecraft:tall_grass', 'minecraft:fern', 'minecraft:large_fern'],
    'sheep_food': ['minecraft:short_grass', 'minecraft:tall_grass', 'minecraft:large_fern'],
    'goat_food': ['minecraft:short_grass', 'minecraft:tall_grass', 'minecraft:fern', 'minecraft:large_fern', 'minecraft:dandelion', 'minecraft:poppy', 'minecraft:blue_orchid', 'minecraft:allium', 'minecraft:azure_bluet', 'minecraft:oxeye_daisy', 'minecraft:cornflower', 'minecraft:torchflower', 'minecraft:oak_leaves', 'minecraft:birch_leaves', 'minecraft:spruce_leaves', 'minecraft:jungle_leaves', 'minecraft:acacia_leaves', 'minecraft:dark_oak_leaves', 'minecraft:mangrove_leaves', 'minecraft:cherry_leaves', 'minecraft:pale_oak_leaves'],
    'pig_food': ['minecraft:short_grass', 'minecraft:tall_grass', 'minecraft:fern', 'minecraft:large_fern', 'minecraft:beetroot_seeds'],
    'rabbit_food': ['minecraft:short_grass', 'minecraft:tall_grass', 'minecraft:fern', 'minecraft:oxeye_daisy', 'minecraft:azure_bluet'],
    'horse_tempt_items': ['minecraft:short_grass', 'minecraft:tall_grass', 'minecraft:fern', 'minecraft:large_fern'],
    'camel_food': ['minecraft:dead_bush'],
    'sniffer_food': ['minecraft:pitcher_pod'],
}

FORBIDDEN = {
    'minecraft:wither_rose',
    'minecraft:lily_of_the_valley',
    'minecraft:azalea_leaves',
    'minecraft:flowering_azalea_leaves',
    'minecraft:closed_eyeblossom',
    'minecraft:open_eyeblossom',
    'minecraft:crimson_fungus',
    'minecraft:warped_fungus',
}

DO_NOT_TOUCH = {
    'chicken_food', 'wolf_food', 'cat_food', 'fox_food', 'ocelot_food', 'frog_food', 'hoglin_food',
    'piglin_food', 'bee_food', 'parrot_poisonous_food', 'armadillo_food', 'horse_food', 'llama_food',
    'llama_tempt_items', 'happy_ghast_food', 'happy_ghast_tempt_items'
}

errors: list[str] = []

def err(msg: str) -> None:
    errors.append(msg)

for wrong in WRONG_TAG_DIRS:
    if wrong.exists():
        for tag in EXPECTED:
            bad = wrong / f'{tag}.json'
            if bad.exists():
                err(f'wrong animal tag path exists: {bad}')

if VANILLA_CLIENT is None:
    searched = ', '.join(str(path) for path in VANILLA_CLIENT_CANDIDATES)
    err(f'missing vanilla client jar; searched: {searched}. Run a Gradle build first or set HERBCRAFT_VANILLA_CLIENT_JAR.')
else:
    with zipfile.ZipFile(VANILLA_CLIENT) as zf:
        names = set(zf.namelist())
        item_defs = {p.removeprefix('assets/minecraft/items/').removesuffix('.json') for p in names if p.startswith('assets/minecraft/items/') and p.endswith('.json')}
        for tag in EXPECTED:
            if f'data/minecraft/tags/item/{tag}.json' not in names:
                err(f'vanilla tag does not exist in 26.2: minecraft:{tag}')
        for tag, expected_values in EXPECTED.items():
            p = TAG_DIR / f'{tag}.json'
            if not p.exists():
                err(f'missing tag file {p}')
                continue
            data = json.loads(p.read_text())
            values = data.get('values')
            if data.get('replace') is not False:
                err(f'{p} must use replace=false')
            if values != expected_values:
                err(f'{p} values mismatch: {values!r}')
            if len(values) != len(set(values)):
                err(f'{p} has duplicate local values')
            forbidden = FORBIDDEN.intersection(values)
            if forbidden:
                err(f'{p} contains forbidden toxic items: {sorted(forbidden)}')
            for value in values:
                if value.startswith('#'):
                    continue
                ns, item = value.split(':', 1)
                if ns != 'minecraft' or item not in item_defs:
                    err(f'{p} references missing vanilla item {value}')
            vanilla_path = f'data/minecraft/tags/item/{tag}.json'
            vanilla_values = json.loads(zf.read(vanilla_path).decode()).get('values', [])
            dupes = set(values).intersection(vanilla_values)
            if dupes:
                err(f'{p} duplicates vanilla values: {sorted(dupes)}')

# Ensure no intentionally untouched food tags were added by this task.
if TAG_DIR.exists():
    for p in TAG_DIR.glob('*.json'):
        if p.stem in DO_NOT_TOUCH:
            err(f'task should not modify tag {p.stem}')
        if p.stem not in EXPECTED and p.stem.endswith(('food', 'tempt_items')):
            err(f'unexpected animal food/tempt tag added: {p.name}')

if errors:
    print('FAIL')
    for e in errors:
        print(' -', e)
    sys.exit(1)

print('PASS - animal food/tempt tags append to minecraft namespace with replace=false, valid items, no forbidden toxic items, no wrong tags/items path, and no vanilla duplicates.')
