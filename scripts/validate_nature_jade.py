#!/usr/bin/env python3
import json, sys, zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]
herbs=json.loads((ROOT/'core/src/main/resources/data/herbcraft/herbs.json').read_text(encoding='utf-8'))['herbs']
nature_path=ROOT/'core/src/main/resources/data/herbcraft/herb_natures.json'
if not nature_path.exists():
    errors.append('missing herb_natures.json'); natures={}
else:
    natures=json.loads(nature_path.read_text(encoding='utf-8'))
temps={'cold','cool','neutral','warm','hot'}; flavors={'sweet','bitter','pungent','sour','astringent','salty','bland'}
traits={'clearing','toxic','nourishing','stirring','sedating','aquatic','fiery','withered','nether','floral','resinous','fungal','lunar','solar','leafy','icy','slimy','honeyed','earthy','addictive','curio'}
for h in herbs:
    if h['item'] not in natures:
        errors.append(f'missing nature for {h["item"]}')
for item,n in natures.items():
    if n.get('temperature') not in temps: errors.append(f'{item} bad temperature {n.get("temperature")}')
    for f in n.get('flavors',[]):
        if f not in flavors: errors.append(f'{item} bad flavor {f}')
    for t in n.get('traits',[]):
        if t not in traits: errors.append(f'{item} bad trait {t}')
for rel, needles in {
 'core/src/main/java/com/herbcraft/nature/HerbNatureRegistry.java':['loadFromBundledJson','getOrFallback','NatureProfile.DEFAULT'],
 'core/src/main/java/com/herbcraft/api/HerbNatureAPI.java':['natureLine','traitHint'],
 'core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java':['HerbNatureAPI.natureLine','HerbNatureAPI.traitHint'],
 'core/src/main/java/com/herbcraft/compat/jade/HerbcraftJadePlugin.java':['IWailaPlugin','registerBlockComponent','registerBlockDataProvider','registerEntityComponent','registerEntityDataProvider'],
 'core/src/main/java/com/herbcraft/compat/jade/HerbNatureProvider.java':['IBlockComponentProvider','appendTooltip'],
 'core/src/main/java/com/herbcraft/compat/jade/HerbNatureServerDataProvider.java':['IServerDataProvider<BlockAccessor>','appendServerData','shouldRequestData'],
 'core/src/main/java/com/herbcraft/compat/jade/HerbNatureEntityProvider.java':['IEntityComponentProvider','ItemEntity','appendTooltip'],
 'core/src/main/java/com/herbcraft/compat/jade/HerbNatureEntityServerDataProvider.java':['IServerDataProvider<EntityAccessor>','ItemEntity','appendServerData','shouldRequestData'],
 'core/src/main/java/com/herbcraft/compat/jade/HerbJadeData.java':['jade.herbcraft.unknown','HerbNatureAPI.natureLine','KnowledgeAPI.displayState','KnowledgeAPI.displayState','herbcraft:knowledge_state'],
 'cuisine/src/main/java/com/herbcraft/cuisine/client/CuisineTooltips.java':['tooltip.herbcraft_cuisine.water_bowl.nature']}.items():
    p=ROOT/rel
    if not p.exists():
        errors.append(f'missing {rel}'); continue
    text=p.read_text(encoding='utf-8')
    if rel == 'core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java':
        text += '\n' + '\n'.join(path.read_text(encoding='utf-8') for path in (ROOT/'core/src/main/java/com/herbcraft/knowledge').glob('HerbalChapter*.java'))
    for needle in needles:
        if needle not in text: errors.append(f'{rel} missing {needle}')
core_mod=json.loads((ROOT/'core/src/main/resources/fabric.mod.json').read_text(encoding='utf-8'))
if 'jade' not in core_mod.get('entrypoints',{}): errors.append('fabric.mod.json missing jade entrypoint')
if 'jade' not in core_mod.get('suggests',{}): errors.append('fabric.mod.json missing jade suggests')
for lp in ['core/src/main/resources/assets/herbcraft/lang/zh_cn.json','core/src/main/resources/assets/herbcraft/lang/en_us.json']:
    data=json.loads((ROOT/lp).read_text(encoding='utf-8'))
    for k in ['nature.herbcraft.line','jade.herbcraft.unknown','jade.herbcraft.heard','jade.herbcraft.mastered','config.jade.plugin_herbcraft.herb_nature','config.jade.plugin_herbcraft.herb_nature_item_entity']:
        if k not in data: errors.append(f'{lp} missing {k}')
version=core_mod.get('version', 'UNKNOWN')
jar=ROOT/f'core/build/libs/herbcraft-{version}.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z: names=set(z.namelist())
    for n in ['data/herbcraft/herb_natures.json','assets/herbcraft/textures/gui/advancements/backgrounds/herbcraft.png','com/herbcraft/compat/jade/HerbcraftJadePlugin.class','com/herbcraft/compat/jade/HerbNatureProvider.class','com/herbcraft/compat/jade/HerbNatureServerDataProvider.class','com/herbcraft/compat/jade/HerbNatureEntityProvider.class','com/herbcraft/compat/jade/HerbNatureEntityServerDataProvider.class','com/herbcraft/compat/jade/HerbJadeData.class','com/herbcraft/nature/HerbNatureRegistry.class']:
        if n not in names: errors.append(f'core jar missing {n}')
else:
    errors.append('built core jar missing')
if errors:
    print('FAIL')
    for e in errors: print(' -', e)
    sys.exit(1)
print('PASS - nature data/API/codex/Jade lightweight compatibility validation passed.')
