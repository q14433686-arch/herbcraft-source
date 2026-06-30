#!/usr/bin/env python3
import json, sys, zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def require(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

rules_path=ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/armor_support_rules.json'
require(rules_path.exists(),'missing armor_support_rules.json')
if rules_path.exists():
    data=json.loads(rules_path.read_text())
    require(data.get('schema_version')==1,'armor_support_rules schema_version must be 1')
    supports={s.get('id'):s for s in data.get('supports',[])}
    for sid, orient, buf in [('clear_bearing','purified',12),('steady_bearing','sustained',8),('braced_bearing','intense',10),('harmonized_bearing','harmonized',14)]:
        s=supports.get(sid); require(s is not None,f'missing support {sid}')
        if s:
            require(s.get('orientation')==orient,f'{sid} orientation expected {orient}')
            require(s.get('buffer_amount')==buf,f'{sid} buffer expected {buf}')
            require(s.get('effect')=='herbcraft_alchemy:clear_bearing',f'{sid} should use clear_bearing effect')
    require(supports.get('harmonized_bearing',{}).get('implemented') is False,'harmonized_bearing should remain reserved')

files={
'ArmorSupportComponent.java':['support_id','source_essence','orientation','buffer_amount','STREAM_CODEC'],
'ArmorSupportComponents.java':['herbcraft_alchemy", "armor_support"','persistent','networkSynchronized'],
'ArmorSupportRules.java':['armor_support_rules.json','record Support','supportForOrientation','bufferAmount','activeBufferAmount','tailBufferAmount','matchingMedicinalProfiles'],
'ArmorSupportSmithingRecipe.java':['SimpleSmithingRecipe','RecipeMode','CoatingItems.BLANK_ADHESIVE','CoatingItems.AMETHYST_ADHESIVE','CoatingItems.ECHO_ADHESIVE','DistillationComponents.DISTILLED_ESSENCE','ArmorSupportMode.ATTACHED','ArmorSupportMode.CLEAR','ArmorSupportMode.DEEP','isSpecial()'],
'ArmorSupportRecipes.java':['armor_support_smithing','RecipeSerializer'],
'ArmorSupportRuntime.java':['POTION_BURDEN_BUFFER_DETAILED.register','EquipmentSlot.HEAD','EquipmentSlot.CHEST','EquipmentSlot.LEGS','EquipmentSlot.FEET','getItemBySlot','bufferPotionBurden','support.effect()','PotionBurdenBufferResult','activeBuffer','tailBuffer','matches(support'],
}
for fn,tokens in files.items():
    t=read(f'alchemy/src/main/java/com/herbcraft/alchemy/armor/{fn}')
    for token in tokens: require(token in t, f'{fn} missing {token}')
init=read('alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java')
for token in ['ArmorSupportComponents.initialize','ArmorSupportRules.loadFromBundledJson','ArmorSupportRecipes.initialize','ArmorSupportRuntime.register']:
    require(token in init, f'HerbcraftAlchemy missing {token}')
client=read('alchemy/src/main/java/com/herbcraft/alchemy/client/HerbcraftAlchemyClient.java')
require('ArmorSupportTooltips.register()' in client, 'client should register armor support tooltips')
tooltip=read('alchemy/src/main/java/com/herbcraft/alchemy/client/ArmorSupportTooltips.java')
for token in ['ArmorSupportComponents.ARMOR_SUPPORT','tooltip.herbcraft.armor_support','tooltip.herbcraft.armor_support.buffer','sourceEssence']:
    require(token in tooltip, f'ArmorSupportTooltips missing {token}')
require(not (ROOT/'alchemy/src/main/resources/data/herbcraft/recipe/armor_support.json').exists(), 'legacy armor_support crafting recipe should be removed after D2')
for recipe_name, mode in [('armor_support_apply','apply'),('armor_support_refine','refine')]:
    recipe=ROOT/f'alchemy/src/main/resources/data/herbcraft/recipe/{recipe_name}.json'
    require(recipe.exists(), f'missing {recipe_name} smithing recipe json')
    if recipe.exists():
        data=json.loads(recipe.read_text())
        require(data.get('type')=='herbcraft:armor_support_smithing', f'{recipe_name} recipe type mismatch')
        require(data.get('mode')==mode, f'{recipe_name} mode should be {mode}')
for lang in ['en_us','zh_cn']:
    data=json.loads((ROOT/f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json').read_text())
    for key in ['tooltip.herbcraft.armor_support','tooltip.herbcraft.armor_support.source','tooltip.herbcraft.armor_support.buffer','tooltip.herbcraft.armor_support.clear_bearing','tooltip.herbcraft.armor_support.steady_bearing','tooltip.herbcraft.armor_support.braced_bearing','tooltip.herbcraft.armor_support.mode','tooltip.herbcraft.armor_support.mode.attached','tooltip.herbcraft.armor_support.mode.clear','tooltip.herbcraft.armor_support.mode.deep']:
        require(key in data, f'missing {lang} key {key}')
verify=read('scripts/verify_built_jars.py')
for token in ['ArmorSupportRules.class','ArmorSupportSmithingRecipe.class','armor_support_rules.json','armor_support_apply.json','armor_support_refine.json']:
    require(token in verify, f'verify_built_jars should check {token}')
jar=ROOT/'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z: names=set(z.namelist())
    for n in ['com/herbcraft/alchemy/armor/ArmorSupportRules.class','com/herbcraft/alchemy/armor/ArmorSupportSmithingRecipe.class','data/herbcraft_alchemy/armor_support_rules.json','data/herbcraft/recipe/armor_support_apply.json','data/herbcraft/recipe/armor_support_refine.json']:
        require(n in names, f'built jar missing {n}')
if errors:
    print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - armor-side support foundation applies distilled-essence orientations to armor and buffers potion burden through the P8 hook.')
