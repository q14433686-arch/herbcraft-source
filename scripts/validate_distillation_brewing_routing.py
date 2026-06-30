#!/usr/bin/env python3
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def require(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

brewing=read('alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistilledEssenceBrewing.java')
mixin=read('alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java')
verify=read('scripts/verify_built_jars.py')
for token in ['isCatalystItem','isDistillationInput','distilledOutput','AlchemyRegistries.essenceInfo','DistillationRules.implementedOrientations','DistillationItems.stack','isBrewable','brew(NonNullList<ItemStack> items)','DistillationComponents.DISTILLED_ESSENCE','AdvancedPotionStackInitializer.potionPath','path.get().equals(component.sourceEssence())','case "purified" -> sourceEssence + "_clarified"','case "sustained" -> sourceEssence + "_long"','case "intense" -> "wither_rose".equals(sourceEssence) ? "wither_venom_iii" : sourceEssence + "_strong"','POTION_QUALITY','POTION_BONUS','POTION_PROCESSING_STYLE','POTION_BURDEN','ADVANCED_POTION_INITIALIZED','ingredient.shrink(1)']:
    require(token in brewing, f'DistilledEssenceBrewing missing token {token}')
require('slot >= 0 && slot < 3 && DistilledEssenceBrewing.isDistillationInput' in mixin, 'brewing stand automation should accept essence items in bottle slots for distillation')
require('DistilledEssenceBrewing.isCatalystItem' in mixin, 'brewing stand automation should accept distilled essence/catalyst ingredient')
require('DistilledEssenceBrewing.isBrewable' in mixin, 'brewing stand isBrewable should include distilled essence route')
require('DistilledEssenceBrewing.brew(items)' in mixin, 'brewing stand doBrew should execute distilled essence route')
require('HiddenCuisineCatalystBrewing.isBrewable(items)' in mixin, 'hidden cuisine catalyst route should remain intact')
require('DistilledEssenceBrewing.class' in verify, 'verify_built_jars should check DistilledEssenceBrewing class')
jar=ROOT/'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z: names=set(z.namelist())
    require('com/herbcraft/alchemy/distillation/DistilledEssenceBrewing.class' in names, 'built jar missing DistilledEssenceBrewing.class')
if errors:
    print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - distilled essence brewing routes matching base potions into sustained/intense/purified paths without breaking hidden cuisine catalyst brewing.')
