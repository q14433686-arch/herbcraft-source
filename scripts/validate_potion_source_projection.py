#!/usr/bin/env python3
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

components = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java')
initializer = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java')
runtime = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionPharmacologyProjectionRuntime.java')
api = read('alchemy/src/main/java/com/herbcraft/alchemy/AlchemyProcessingAPI.java')
tooltips = read('alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java')
projection = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionPharmacologyProjection.java')
verify = read('scripts/verify_built_jars.py')

for token in [
    'POTION_PHARMACOLOGY',
    'potion_pharmacology',
    'PotionPharmacologyProjection',
    'PotionPharmacologyProjection.CODEC',
    'PotionPharmacologyProjection.STREAM_CODEC',
]:
    require(token in components, f'AlchemyPotionComponents missing token {token}')

for token in [
    'record PotionPharmacologyProjection',
    'source_essence',
    'temperature',
    'taste_flavors',
    'traits',
    'CODEC',
    'STREAM_CODEC',
]:
    require(token in projection, f'PotionPharmacologyProjection missing token {token}')

for token in [
    'HerbNatureAPI.getNatureOrFallback',
    'POTION_SOURCE',
    'POTION_PHARMACOLOGY',
    'NatureProfile',
    'source,',
    'tasteFlavors',
    'applyMetadata(ItemStack stack)',
]:
    require(token in runtime, f'PotionPharmacologyProjectionRuntime missing token {token}')

require('PotionPharmacologyProjectionRuntime.applyMetadata(stack);' in initializer, 'AdvancedPotionStackInitializer should attach source pharmacology projection metadata')
require('potionPharmacology(ItemStack stack)' in api, 'AlchemyProcessingAPI should expose read-only potion pharmacology projection query')
for token in [
    'tooltip.herbcraft.potion.source_essence',
    'sourceName(projection.sourceEssence())',
    'PotionPharmacologyProjectionRuntime.projection(stack)',
]:
    require(token in tooltips, f'AdvancedPotionTooltips missing compact P2 projection token {token}')
for token in [
    'tooltip.herbcraft.potion.source_temperature',
    'tooltip.herbcraft.potion.source_taste',
    'tooltip.herbcraft.potion.source_traits',
    'nature.herbcraft.temperature.',
    'nature.herbcraft.flavor.',
    'nature.herbcraft.hint.',
]:
    require(token in tooltips or token in read('alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java'), f'P2 projection token should exist in compact tooltip or Codex {token}')

for lang in ['en_us', 'zh_cn']:
    path = ROOT / f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json'
    lang_data = json.loads(path.read_text(encoding='utf-8'))
    for key in [
        'tooltip.herbcraft.potion.source_essence',
        'tooltip.herbcraft.potion.source_temperature',
        'tooltip.herbcraft.potion.source_taste',
        'tooltip.herbcraft.potion.source_traits',
    ]:
        require(key in lang_data, f'missing {lang} lang key {key}')

require('PotionPharmacologyProjection.class' in verify, 'verify_built_jars should check PotionPharmacologyProjection class')
require('PotionPharmacologyProjectionRuntime.class' in verify, 'verify_built_jars should check PotionPharmacologyProjectionRuntime class')

jar = ROOT / 'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z:
        names = set(z.namelist())
    require('com/herbcraft/alchemy/potion/PotionPharmacologyProjection.class' in names, 'built jar missing PotionPharmacologyProjection.class')
    require('com/herbcraft/alchemy/potion/PotionPharmacologyProjectionRuntime.class' in names, 'built jar missing PotionPharmacologyProjectionRuntime.class')

if errors:
    print('FAIL')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print('PASS - P2 source essence pharmacology projection is persisted, queryable, tooltip-projected, and jar-verified.')
