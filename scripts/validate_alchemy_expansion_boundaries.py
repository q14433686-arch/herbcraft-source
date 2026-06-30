#!/usr/bin/env python3
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def require(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

events=read('alchemy/src/main/java/com/herbcraft/alchemy/AlchemyExpansionEvents.java')
api=read('alchemy/src/main/java/com/herbcraft/alchemy/AlchemyProcessingAPI.java')
burden=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenRuntime.java')
processing=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRuntime.java')
verify=read('scripts/verify_built_jars.py')
for token in ['PROCESSING_STYLE_APPLIED','POTION_BURDEN_APPLIED','POTION_BURDEN_STAGE_CHANGED','POTION_BURDEN_BUFFER','future distillation, armor support, and advanced\n * coating integration']:
    require(token in events, f'AlchemyExpansionEvents missing hook token {token}')
for token in ['processingStyleId(ItemStack stack)','processingStyle(ItemStack stack)','potionBurden(ItemStack stack)','playerBurden(ServerPlayer player)','playerBurdenStage(ServerPlayer player)']:
    require(token in api, f'AlchemyProcessingAPI missing read-only query {token}')
require('POTION_BURDEN_BUFFER' in burden and 'bufferPotionBurden' in burden, 'PotionBurdenRuntime should expose armor/support buffer hook')
require('POTION_BURDEN_APPLIED' in burden, 'PotionBurdenRuntime should fire burden applied event')
require('PROCESSING_STYLE_APPLIED' in processing, 'PotionProcessingRuntime should fire style-applied event')
for forbidden in ['registerStyle(', 'registerPotion(', 'registerBurdenProfile(', 'registerDistillation']:
    require(forbidden not in api, f'AlchemyProcessingAPI should not expose write registration method {forbidden}')
require('AlchemyExpansionEvents.class' in verify and 'AlchemyProcessingAPI.class' in verify, 'verify_built_jars should check P8 hook/API classes')
jar=ROOT/'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z: names=set(z.namelist())
    for cls in ['com/herbcraft/alchemy/AlchemyExpansionEvents.class','com/herbcraft/alchemy/AlchemyProcessingAPI.class']:
        require(cls in names, f'built jar missing {cls}')
if errors:
    print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - P8 minimal Alchemy expansion hooks/API are present without content registration/write APIs.')
