#!/usr/bin/env python3
import json, sys, zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def require(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

rules_path=ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/processed_coating_rules.json'
require(rules_path.exists(),'missing processed_coating_rules.json')
if rules_path.exists():
    data=json.loads(rules_path.read_text())
    require(data.get('schema_version')==1,'processed coating schema_version must be 1')
    styles={s.get('id'):s for s in data.get('styles',[])}
    for sid in ['base','sustained','intense','purified','murky_edge']:
        require(sid in styles, f'missing coating style {sid}')
    for sid in ['sustained','intense','purified']:
        require(styles.get(sid,{}).get('implemented') is True, f'{sid} should be implemented')
    require(styles.get('murky_edge',{}).get('implemented') is False, 'murky_edge should remain reserved')

weapon=read('alchemy/src/main/java/com/herbcraft/alchemy/coating/WeaponCoating.java')
rules_java=read('alchemy/src/main/java/com/herbcraft/alchemy/coating/ProcessedCoatingRules.java')
system=read('alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingSystem.java')
recipe=read('alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingSmithingRecipe.java')
hit=read('alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingHitHandler.java')
tooltip=read('alchemy/src/main/java/com/herbcraft/alchemy/client/CoatingTooltips.java')
init=read('alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java')
verify=read('scripts/verify_built_jars.py')
for token in ['processing_style','processingStyle','optionalFieldOf("processing_style", "base")']:
    require(token in weapon, f'WeaponCoating missing {token}')
for token in ['processed_coating_rules.json','record Style','usesMultiplier','hitDurationMultiplier','amplifierDelta','styleOrBase']:
    require(token in rules_java, f'ProcessedCoatingRules missing {token}')
for token in ['effectsFor(AlchemyRegistries.EssenceInfo info, WeaponCoating coating)','transform(List<AlchemyRegistries.EssenceEffect> effects, String processingStyle)','usesFor(AlchemyRegistries.EssenceInfo info, WeaponCoating coating)','applyCoating(ItemStack weapon, AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode, String processingStyle)']:
    require(token in system, f'CoatingSystem missing processed coating token {token}')
for token in ['DistillationComponents.DISTILLED_ESSENCE','DistillationItems.stack','distilledEssence','ProcessedCoatingRules.styles()','CoatingSystem.applyCoating(result, info.get(), WeaponCoating.Mode.FULL, processingStyle)','coating.processingStyle()']:
    require(token in recipe, f'CoatingSmithingRecipe missing token {token}')
require('CoatingSystem.effectsFor(info.get(), coating)' in hit and 'coating.processingStyle()' in hit, 'CoatingHitHandler should preserve/use processed style')
require('tooltip.herbcraft.coating.processing' in tooltip and 'ProcessedCoatingRules.styleOrBase' in tooltip, 'Coating tooltip should show processed coating style')
require('ProcessedCoatingRules.loadFromBundledJson();' in init, 'Alchemy init should load processed coating rules')
for lang in ['en_us','zh_cn']:
    data=json.loads((ROOT/f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json').read_text())
    for key in ['tooltip.herbcraft.coating.processing','tooltip.herbcraft.coating.processing.sustained','tooltip.herbcraft.coating.processing.intense','tooltip.herbcraft.coating.processing.purified']:
        require(key in data, f'missing {lang} key {key}')
require('ProcessedCoatingRules.class' in verify and 'processed_coating_rules.json' in verify, 'verify_built_jars should check processed coating rule artifacts')
jar=ROOT/'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z: names=set(z.namelist())
    require('com/herbcraft/alchemy/coating/ProcessedCoatingRules.class' in names, 'built jar missing ProcessedCoatingRules.class')
    require('data/herbcraft_alchemy/processed_coating_rules.json' in names, 'built jar missing processed_coating_rules.json')
if errors:
    print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - processed weapon coating expansion uses distilled essences, data-backed style rules, preserved refinement, and tooltip/runtime projection.')
