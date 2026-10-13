#!/usr/bin/env python3
"""Generate/check resource skeletons for Herbcraft Alchemy essences.

This is a project-local helper, not a full public datagen API. It reads
alchemy/src/main/resources/data/herbcraft_alchemy/essences.json and checks or
creates the resource files needed for each essence item to be polished in-game.

Default mode is --check. Generation is intentionally conservative:
- --write-missing creates missing item definitions, item models, recipes, and
  placeholder textures.
- Existing files are never overwritten unless --overwrite-generated is passed.
- Lang placeholders are only added when --write-missing is used.
"""
from __future__ import annotations

import argparse
import json
import re
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ESSENCES_JSON = ROOT / "alchemy/src/main/resources/data/herbcraft_alchemy/essences.json"
ITEM_DEF_DIR = ROOT / "alchemy/src/main/resources/assets/herbcraft/items"
MODEL_DIR = ROOT / "alchemy/src/main/resources/assets/herbcraft/models/item"
TEXTURE_DIR = ROOT / "alchemy/src/main/resources/assets/herbcraft/textures/item"
RECIPE_DIR = ROOT / "alchemy/src/main/resources/data/herbcraft/recipe"
LANG_EN = ROOT / "alchemy/src/main/resources/assets/herbcraft/lang/en_us.json"
LANG_ZH = ROOT / "alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json"

GENERATED_MARKER = "generated_by_herbcraft_essence_resource_helper"


def load_json(path: Path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def dump_json(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def title_case(s: str) -> str:
    return " ".join(part.capitalize() for part in s.split("_"))


def item_path(item_id: str) -> str:
    return item_id.split(":", 1)[1] if ":" in item_id else item_id


def recipe_name(essence_id: str, source_items: list[str], source: str) -> str:
    if len(source_items) == 1:
        return f"essence_{essence_id}.json"
    return f"essence_{essence_id}_from_{item_path(source)}.json"


def item_definition(essence_id: str) -> dict:
    return {
        "model": {
            "type": "minecraft:model",
            "model": f"herbcraft:item/essence_{essence_id}",
        }
    }


def item_model(essence_id: str) -> dict:
    return {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"herbcraft:item/essence_{essence_id}",
        },
    }


def essence_recipe(essence_id: str, source: str) -> dict:
    return {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [source] * 8 + ["minecraft:glass_bottle"],
        "result": {
            "id": f"herbcraft:essence_{essence_id}",
            "count": 1,
        },
    }


def write_png(path: Path, pixels: list[list[tuple[int, int, int, int]]]) -> None:
    h = len(pixels)
    w = len(pixels[0])
    raw = b"".join(b"\x00" + bytes([c for px in row for c in px]) for row in pixels)

    def chunk(t: bytes, d: bytes) -> bytes:
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)

    data = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def placeholder_texture(essence_id: str) -> list[list[tuple[int, int, int, int]]]:
    """Create a simple 16x16 generated bottle-like placeholder.

    It is intentionally not as polished as hand-made textures; it exists so
    dynamic/dev essences have a visible placeholder instead of missing texture.
    """
    seed = sum(ord(c) for c in essence_id)
    r = 80 + seed % 120
    g = 80 + (seed * 3) % 120
    b = 100 + (seed * 7) % 120
    glass = (210, 230, 245, 180)
    liquid = (r, g, b, 255)
    outline = (45, 55, 65, 255)
    mark = (255, 255, 255, 180)
    pix = [[(0, 0, 0, 0) for _ in range(16)] for __ in range(16)]
    # bottle outline
    for y in range(3, 15):
        for x in range(4, 12):
            if x in (4, 11) or y in (3, 14):
                pix[y][x] = outline
            else:
                pix[y][x] = glass
    for y in range(1, 4):
        for x in range(6, 10):
            pix[y][x] = outline if x in (6, 9) or y == 1 else glass
    # liquid
    for y in range(9, 14):
        for x in range(5, 11):
            pix[y][x] = liquid
    pix[5][6] = mark
    pix[6][5] = mark
    pix[15][15] = (seed & 255, (seed >> 1) & 255, (seed >> 2) & 255, 80)
    return pix


def is_generated_texture(path: Path) -> bool:
    if not path.exists():
        return False
    # PNG has no text chunk in our minimal writer; use a deliberately simple
    # heuristic: allow overwrite only if caller explicitly opts in. Existing
    # hand-authored files are therefore safe by default.
    return True


def ensure_file(path: Path, data, write_missing: bool, overwrite: bool, errors: list[str]) -> None:
    if path.exists() and not overwrite:
        return
    if not write_missing:
        errors.append(f"missing {path.relative_to(ROOT)}")
        return
    dump_json(path, data)


def ensure_texture(path: Path, essence_id: str, write_missing: bool, overwrite: bool, errors: list[str]) -> None:
    if path.exists() and not overwrite:
        return
    if not write_missing:
        errors.append(f"missing {path.relative_to(ROOT)}")
        return
    write_png(path, placeholder_texture(essence_id))


def update_lang(path: Path, missing: dict[str, str], write_missing: bool, errors: list[str]) -> None:
    data = load_json(path)
    changed = False
    for key, value in missing.items():
        if key not in data:
            if write_missing:
                data[key] = value
                changed = True
            else:
                errors.append(f"missing lang key {key} in {path.relative_to(ROOT)}")
    if changed:
        dump_json(path, data)


def check_recipe_shape(path: Path, essence_id: str, source: str, errors: list[str]) -> None:
    if not path.exists():
        return
    try:
        data = load_json(path)
    except Exception as exc:
        errors.append(f"invalid recipe json {path.relative_to(ROOT)}: {exc}")
        return
    if data.get("result", {}).get("id") != f"herbcraft:essence_{essence_id}":
        errors.append(f"recipe {path.relative_to(ROOT)} result should be herbcraft:essence_{essence_id}")
    ingredients = data.get("ingredients", [])
    if ingredients.count(source) != 8 or "minecraft:glass_bottle" not in ingredients:
        errors.append(f"recipe {path.relative_to(ROOT)} should use 8x {source} plus glass bottle")




def check_extension(args) -> int:
    base = Path(args.check_extension)
    errors: list[str] = []
    essences_file = base / "data/herbcraft_alchemy/essences.json"
    if not essences_file.exists():
        print(f"EXTENSION CHECK FAILED\n- missing {essences_file}")
        return 1
    try:
        essences = load_json(essences_file).get("essences", [])
    except Exception as exc:
        print(f"EXTENSION CHECK FAILED\n- invalid {essences_file}: {exc}")
        return 1
    en = load_json(base / "assets/herbcraft/lang/en_us.json") if (base / "assets/herbcraft/lang/en_us.json").exists() else {}
    zh = load_json(base / "assets/herbcraft/lang/zh_cn.json") if (base / "assets/herbcraft/lang/zh_cn.json").exists() else {}
    for entry in essences:
        essence_id = entry.get("id")
        source_items = entry.get("source_items") or [f"minecraft:{essence_id}"]
        if not essence_id:
            errors.append("essence entry missing id")
            continue
        if not entry.get("effects"):
            errors.append(f"essence {essence_id} missing effects")
        check_required_file(base / f"assets/herbcraft/items/essence_{essence_id}.json", errors)
        check_required_file(base / f"assets/herbcraft/models/item/essence_{essence_id}.json", errors)
        check_required_file(base / f"assets/herbcraft/textures/item/essence_{essence_id}.png", errors, binary=True)
        for source in source_items:
            check_required_file(base / f"data/herbcraft/recipe/{recipe_name(essence_id, source_items, source)}", errors)
        key = f"item.herbcraft.essence_{essence_id}"
        if key not in en:
            errors.append(f"missing en_us lang key {key}")
        if key not in zh:
            errors.append(f"missing zh_cn lang key {key}")
    # Optional trade tag integrity.
    trade_tag = base / "data/minecraft/tags/villager_trade/wandering_trader/uncommon.json"
    if trade_tag.exists():
        try:
            tag = load_json(trade_tag)
            for value in tag.get("values", []):
                if not value.startswith("herbcraft:"):
                    continue
                rel = value.split(":", 1)[1]
                trade_file = base / f"data/herbcraft/villager_trade/{rel}.json"
                check_required_file(trade_file, errors)
        except Exception as exc:
            errors.append(f"invalid trade tag {trade_tag}: {exc}")
    # Optional Codex rumor skeleton syntax.
    rumor = base / "data/herbcraft/knowledge_rumors.json"
    if rumor.exists():
        try:
            data = load_json(rumor)
            for item_id, body in data.items():
                if not body.get("claims"):
                    errors.append(f"Codex rumor entry {item_id} has no claims")
        except Exception as exc:
            errors.append(f"invalid {rumor}: {exc}")
    if errors:
        print("EXTENSION CHECK FAILED")
        for error in errors:
            print("-", error)
        return 1
    print(f"EXTENSION CHECK OK: {len(essences)} essence definition(s) in {base}")
    return 0


def check_required_file(path: Path, errors: list[str], binary: bool = False) -> None:
    if not path.exists():
        errors.append(f"missing {path}")
        return
    if not binary and path.suffix == ".json":
        try:
            load_json(path)
        except Exception as exc:
            errors.append(f"invalid json {path}: {exc}")
    if binary and path.suffix == ".png" and not path.read_bytes().startswith(b"\x89PNG\r\n\x1a\n"):
        errors.append(f"not a PNG file: {path}")


def scaffold_extension(args) -> int:
    if not args.essence:
        print("--scaffold-extension requires --essence")
        return 1
    source_items = scaffold_sources(args)
    if not source_items:
        print("--scaffold-extension requires --source or --sources")
        return 1
    base = Path(args.scaffold_extension)
    essence_id = args.essence
    en_name = args.en_name or f"{title_case(essence_id)} Essence"
    zh_name = args.zh_name or en_name

    essence_entry = {
        "id": essence_id,
        "source_items": source_items,
        "potion_name": essence_id,
        "effects": [
            {"effect": args.effect, "amplifier": args.amplifier, "duration_seconds": args.duration_seconds, "positive": args.positive}
        ],
    }
    files = {
        base / "data/herbcraft_alchemy/essences.json": {
            "schema_version": 1,
            "essences": [essence_entry],
        },
        base / f"assets/herbcraft/items/essence_{essence_id}.json": item_definition(essence_id),
        base / f"assets/herbcraft/models/item/essence_{essence_id}.json": item_model(essence_id),
        base / "assets/herbcraft/lang/en_us.json": {f"item.herbcraft.essence_{essence_id}": en_name},
        base / "assets/herbcraft/lang/zh_cn.json": {f"item.herbcraft.essence_{essence_id}": zh_name},
        base / "README.md": scaffold_readme(essence_id, source_items, en_name, zh_name, include_trade=not args.no_trade, include_codex=not args.no_codex),
    }
    for source in source_items:
        files[base / f"data/herbcraft/recipe/{recipe_name(essence_id, source_items, source)}"] = essence_recipe(essence_id, source)

    if not args.no_codex:
        files[base / "data/herbcraft/knowledge_rumors.json"] = {
            source: {
                "claims": [
                    {
                        "id": "nature",
                        "type": "nature",
                        "text_key": f"knowledge.herbcraft.claim.{item_path(source)}.nature",
                        "truth": True,
                        "source_tier": "common",
                        "suspicious": False,
                    },
                    {
                        "id": "trait",
                        "type": "trait",
                        "text_key": f"knowledge.herbcraft.claim.{item_path(source)}.trait",
                        "truth": True,
                        "source_tier": "common",
                        "suspicious": False,
                    },
                ]
            }
            for source in source_items
        }

    if not args.no_trade:
        files[base / f"data/herbcraft/villager_trade/wanderer/emerald_essence_{essence_id}.json"] = {
            "wants": {"id": "minecraft:emerald", "count": args.trade_emerald_cost},
            # 26.3: wants/gives/additional_wants 的 count 为必填（context_int_provider）。
            "gives": {"id": f"herbcraft:essence_{essence_id}", "count": 1},
            "max_uses": 1.0,
            "reputation_discount": 0.05,
            "xp": 12.0,
        }
        files[base / "data/minecraft/tags/villager_trade/wandering_trader/uncommon.json"] = {
            "replace": False,
            "values": [f"herbcraft:wanderer/emerald_essence_{essence_id}"],
        }

    for path, data in files.items():
        if path.exists() and not args.overwrite_generated:
            continue
        if isinstance(data, str):
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(data, encoding="utf-8")
        else:
            dump_json(path, data)
    tex = base / f"assets/herbcraft/textures/item/essence_{essence_id}.png"
    if not tex.exists() or args.overwrite_generated:
        write_png(tex, placeholder_texture(essence_id))
    print(f"Scaffolded Herbcraft essence extension at {base}")
    print("Review generated files before release. Trades and Codex skeletons are optional and conservative.")
    return 0


def scaffold_sources(args) -> list[str]:
    sources: list[str] = []
    if args.source:
        sources.append(args.source)
    if args.sources:
        for raw in args.sources.split(','):
            raw = raw.strip()
            if raw:
                sources.append(raw)
    # Preserve order, remove duplicates.
    seen = set()
    unique = []
    for source in sources:
        if source not in seen:
            seen.add(source)
            unique.append(source)
    return unique


def scaffold_readme(essence_id: str, source_items: list[str], en_name: str, zh_name: str, include_trade: bool, include_codex: bool) -> str:
    source_list = "\n".join(f"- `{source}`" for source in source_items)
    optional = []
    if include_trade:
        optional.append("- optional wandering trader uncommon trade skeleton")
    if include_codex:
        optional.append("- Codex rumor skeleton with text keys")
    optional_text = "\n".join(optional) if optional else "- no optional trade/Codex skeletons were requested"
    return f"""# Herbcraft Essence Extension Scaffold: {essence_id}

This generated scaffold demonstrates the files needed for a polished Herbcraft Alchemy essence extension.

- Essence ID: `herbcraft:essence_{essence_id}`
- Source item(s):
{source_list}
- English name: `{en_name}`
- Chinese name: `{zh_name}`

## Included

- `data/herbcraft_alchemy/essences.json`
- item definition/model/texture placeholder
- extraction recipe(s)
- English/Chinese lang skeleton
{optional_text}

## Important review points

1. Replace the generated placeholder texture with proper art.
2. Review generated trades if enabled; generated trade prices are only a starting point.
3. Add actual claim lang text for Codex rumor keys if Codex skeletons are enabled.
4. If this is a separate mod, ensure it depends on Herbcraft Core and Herbcraft Alchemy.
5. Run Herbcraft validators after copying into a development workspace.
"""

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true", help="Check resources only (default).")
    parser.add_argument("--write-missing", action="store_true", help="Create missing resource skeletons and placeholders.")
    parser.add_argument("--overwrite-generated", action="store_true", help="Overwrite generated skeleton files/placeholders. Existing files are otherwise preserved.")
    parser.add_argument("--scaffold-extension", help="Create a standalone extension scaffold directory for one essence.")
    parser.add_argument("--check-extension", help="Check a standalone extension scaffold/resource directory.")
    parser.add_argument("--essence", help="Essence id for --scaffold-extension, e.g. lavender.")
    parser.add_argument("--source", help="Single source item id for --scaffold-extension, e.g. examplemod:lavender. Can be combined with --sources.")
    parser.add_argument("--sources", help="Comma-separated source item ids for --scaffold-extension, e.g. red,white variants.")
    parser.add_argument("--en-name", help="English display name for scaffold lang.")
    parser.add_argument("--zh-name", help="Chinese display name for scaffold lang.")
    parser.add_argument("--effect", default="minecraft:regeneration", help="Primary effect id for scaffold essence.")
    parser.add_argument("--amplifier", type=int, default=0, help="Primary effect amplifier for scaffold essence.")
    parser.add_argument("--duration-seconds", type=int, default=20, help="Primary effect duration for scaffold essence.")
    parser.add_argument("--positive", action=argparse.BooleanOptionalAction, default=True, help="Whether scaffold primary effect is positive.")
    parser.add_argument("--trade-emerald-cost", type=int, default=4, help="Wandering trader emerald cost in scaffold.")
    parser.add_argument("--no-trade", action="store_true", help="Do not generate optional wandering trader trade/tag skeletons in scaffold mode.")
    parser.add_argument("--no-codex", action="store_true", help="Do not generate optional Codex rumor skeletons in scaffold mode.")
    parser.add_argument("--extension-namespace", default="herbcraft", help="Reserved metadata namespace for scaffold README; registry ids remain Herbcraft-compatible.")
    args = parser.parse_args()
    if args.check_extension:
        return check_extension(args)
    if args.scaffold_extension:
        return scaffold_extension(args)
    write_missing = args.write_missing
    overwrite = args.overwrite_generated

    essences = load_json(ESSENCES_JSON).get("essences", [])
    errors: list[str] = []
    en_missing: dict[str, str] = {}
    zh_missing: dict[str, str] = {}

    for entry in essences:
        essence_id = entry["id"]
        source_items = entry.get("source_items") or [f"minecraft:{essence_id}"]
        item_def = ITEM_DEF_DIR / f"essence_{essence_id}.json"
        model = MODEL_DIR / f"essence_{essence_id}.json"
        texture = TEXTURE_DIR / f"essence_{essence_id}.png"
        ensure_file(item_def, item_definition(essence_id), write_missing, overwrite, errors)
        ensure_file(model, item_model(essence_id), write_missing, overwrite, errors)
        ensure_texture(texture, essence_id, write_missing, overwrite, errors)
        for source in source_items:
            recipe = RECIPE_DIR / recipe_name(essence_id, source_items, source)
            ensure_file(recipe, essence_recipe(essence_id, source), write_missing, overwrite, errors)
            check_recipe_shape(recipe, essence_id, source, errors)
        lang_key = f"item.herbcraft.essence_{essence_id}"
        en_missing[lang_key] = f"{title_case(essence_id)} Essence"
        zh_missing[lang_key] = f"TODO {title_case(essence_id)} Essence"

    update_lang(LANG_EN, en_missing, write_missing, errors)
    update_lang(LANG_ZH, zh_missing, write_missing, errors)

    if errors:
        print("ESSENCE RESOURCE CHECK FAILED")
        for error in errors:
            print("-", error)
        return 1
    print(f"ESSENCE RESOURCE CHECK OK: {len(essences)} essence definitions have required resource coverage.")
    if write_missing:
        print("Missing resources were generated conservatively; review generated placeholders before release.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
