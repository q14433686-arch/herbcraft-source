#!/usr/bin/env python3
import json
import sys
import zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
errors=[]
def require(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

rules=json.loads((ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json').read_text(encoding='utf-8'))
stages=rules.get('burden_stages',[])
ids=[s.get('id') for s in stages]
require(ids==['steady','warming','strain','overload'], f'burden stage order should be steady/warming/strain/overload, got {ids}')
expected={'steady':(0,0.0,''),'warming':(120,0.0,'herbcraft_alchemy:potion_warming'),'strain':(300,-0.02,'herbcraft_alchemy:potion_strain'),'overload':(600,-0.05,'herbcraft_alchemy:potion_overload')}
for s in stages:
    min_total, speed, effect = expected.get(s.get('id'), (-1, None, None))
    require(s.get('min_total')==min_total, f'{s.get("id")} min_total expected {min_total}')
    require(abs(float(s.get('speed_modifier',999))-speed)<0.0001, f'{s.get("id")} speed_modifier expected {speed}')
    require(s.get('effect','')==effect, f'{s.get("id")} effect expected {effect}')
    require(s.get('message_key'), f'{s.get("id")} missing message_key')

rules_java=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java')
consequence=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenConsequenceRuntime.java')
state=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenState.java')
runtime=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenRuntime.java')
player_mixin=read('alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyPlayerMixin.java')
verify=read('scripts/verify_built_jars.py')
for token in ['record BurdenStage','burdenStages()','burdenStageFor','burden_stages','speed_modifier','message_key','effectDurationTicks']:
    require(token in rules_java, f'PotionProcessingRules missing stage token {token}')
for token in ['PotionBurdenConsequenceRuntime','AlchemyMobEffects::isBurdenStageEffect','MobEffectInstance','POTION_BURDEN_STAGE_CHANGED','sendSystemMessage','clear(ServerPlayer player)']:
    require(token in consequence, f'PotionBurdenConsequenceRuntime missing token {token}')
require('Attributes.MOVEMENT_SPEED' not in consequence, 'P7 consequence should use custom Alchemy effects, not direct speed modifiers')
for token in ['lastStageId','setLastStageId','LastStage']:
    require(token in state + '\n' + player_mixin, f'player burden state/persistence missing stage token {token}')
require('PotionBurdenConsequenceRuntime.apply(player, state)' in runtime, 'PlayerPotionBurdenRuntime should apply conservative consequences during tick')
require('PotionBurdenConsequenceRuntime.clear(serverPlayer)' in player_mixin, 'death reset should clear burden stage effects')
for lang in ['en_us','zh_cn']:
    data=json.loads((ROOT/f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json').read_text(encoding='utf-8'))
    for k in ['message.herbcraft_alchemy.potion_burden.stage.steady','message.herbcraft_alchemy.potion_burden.stage.warming','message.herbcraft_alchemy.potion_burden.stage.strain','message.herbcraft_alchemy.potion_burden.stage.overload']:
        require(k in data, f'missing {lang} burden stage message {k}')
require('PotionBurdenConsequenceRuntime.class' in verify, 'verify_built_jars should check consequence class')
jar=ROOT/'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z: names=set(z.namelist())
    require('com/herbcraft/alchemy/potion/PotionBurdenConsequenceRuntime.class' in names, 'built jar missing consequence runtime class')
    require('com/herbcraft/alchemy/potion/PotionProcessingRules$BurdenStage.class' in names, 'built jar missing BurdenStage class')
if errors:
    print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - P7 conservative potion burden stages are data-backed and apply Alchemy custom burden effects.')
