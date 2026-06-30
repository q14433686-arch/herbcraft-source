#!/usr/bin/env python3
import json
import sys
import zipfile
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def require(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

rules_path=ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/distillation_rules.json'
require(rules_path.exists(), 'missing distillation_rules.json')
if rules_path.exists():
    data=json.loads(rules_path.read_text(encoding='utf-8'))
    require(data.get('schema_version')==1, 'distillation_rules schema_version must be 1')
    orientations={o.get('id'):o for o in data.get('orientations',[])}
    for oid, catalyst, style in [('purified','minecraft:honeycomb','purified'),('sustained','minecraft:redstone','sustained'),('intense','minecraft:glowstone_dust','intense'),('harmonized','minecraft:amethyst_shard','harmonized')]:
        o=orientations.get(oid)
        require(o is not None, f'missing orientation {oid}')
        if o:
            require(o.get('catalyst')==catalyst, f'{oid} catalyst expected {catalyst}')
            require(o.get('processing_style_hint')==style, f'{oid} processing style hint expected {style}')
            require(o.get('lang_key'), f'{oid} missing lang_key')
            require(o.get('description_key'), f'{oid} missing description_key')
    require(orientations.get('harmonized',{}).get('implemented') is False, 'harmonized distillation should remain reserved')
    for oid in ['purified','sustained','intense']:
        require(orientations.get(oid,{}).get('implemented') is True, f'{oid} should be implemented first batch')

files={
 'DistillationRules.java':['distillation_rules.json','record Orientation','implementedOrientations','processingStyleHint'],
 'DistillationItems.java':['DISTILLED_ESSENCE','stack(String sourceEssence, String orientation)','DistilledEssenceComponent'],
 'DistillationComponents.java':['herbcraft_alchemy", "distilled_essence"','persistent','networkSynchronized'],
 'DistilledEssenceComponent.java':['source_essence','orientation','STREAM_CODEC'],
 'DistillationRecipe.java':['CustomRecipe','AlchemyRegistries.essenceInfo','DistillationRules.orientation','DistillationItems.stack','filled == 2'],
 'DistillationRecipes.java':['herbcraft", "distillation"','RecipeSerializer'],
}
for fn,tokens in files.items():
    text=read(f'alchemy/src/main/java/com/herbcraft/alchemy/distillation/{fn}')
    for token in tokens: require(token in text, f'{fn} missing {token}')
api=read('alchemy/src/main/java/com/herbcraft/alchemy/AlchemyDistillationAPI.java')
for token in ['distilledEssence(ItemStack stack)','orientation(String id)','orientation(ItemStack stack)']:
    require(token in api, f'AlchemyDistillationAPI missing {token}')
init=read('alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java')
for token in ['DistillationComponents.initialize','DistillationRules.loadFromBundledJson','DistillationItems.initialize','DistillationRecipes.initialize']:
    require(token in init, f'HerbcraftAlchemy missing distillation init token {token}')
registries=read('alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java')
require('DistillationItems.DISTILLED_ESSENCE' in registries, 'distilled essence should appear in Alchemy creative tab')
client=read('alchemy/src/main/java/com/herbcraft/alchemy/client/HerbcraftAlchemyClient.java')
require('DistilledEssenceTooltips.register()' in client, 'client should register distilled essence tooltips')
for rel in ['assets/herbcraft/items/distilled_essence.json','assets/herbcraft/models/item/distilled_essence.json','assets/herbcraft/textures/item/distilled_essence.png']:
    require((ROOT/'alchemy/src/main/resources'/rel).exists(), f'missing {rel}')
tex=ROOT/'alchemy/src/main/resources/assets/herbcraft/textures/item/distilled_essence.png'
if tex.exists():
    require(Image.open(tex).size==(16,16), 'distilled essence texture should be 16x16 and match item style')
for removed in ['distilled_essence_purified','distilled_essence_sustained','distilled_essence_intense']:
    require(not (ROOT/f'alchemy/src/main/resources/data/herbcraft/recipe/{removed}.json').exists(), f'{removed} crafting recipe should be removed after D2; distillation happens in brewing stand')
for lang in ['en_us','zh_cn']:
    data=json.loads((ROOT/f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json').read_text(encoding='utf-8'))
    for key in ['item.herbcraft.distilled_essence','tooltip.herbcraft.distilled_essence.orientation','tooltip.herbcraft.distilled_essence.source','tooltip.herbcraft.distilled_essence.orientation.purified','tooltip.herbcraft.distilled_essence.orientation.sustained','tooltip.herbcraft.distilled_essence.orientation.intense']:
        require(key in data, f'missing {lang} key {key}')
verify=read('scripts/verify_built_jars.py')
for token in ['DistillationRules.class','DistillationRecipe.class','distillation_rules.json','distilled_essence.png']:
    require(token in verify, f'verify_built_jars should check {token}')
jar=ROOT/'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z: names=set(z.namelist())
    for n in ['com/herbcraft/alchemy/distillation/DistillationRules.class','com/herbcraft/alchemy/distillation/DistillationRecipe.class','data/herbcraft_alchemy/distillation_rules.json','assets/herbcraft/textures/item/distilled_essence.png']:
        require(n in names, f'built jar missing {n}')
if errors:
    print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - B3 distillation foundation uses one styled container item plus componentized source/orientation, JSON rules, brewing-station migration, tooltips, and read-only API.')
