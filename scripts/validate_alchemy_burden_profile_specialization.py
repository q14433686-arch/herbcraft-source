#!/usr/bin/env python3
import json, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def require(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

rules=json.loads((ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json').read_text(encoding='utf-8'))
profiles=rules.get('burden_consequence_profiles',{})
for profile in ['steady','warming','chilling','overdriven','muddy','scattering','stagnating']:
    require(profile in profiles, f'missing burden consequence profile {profile}')
    if profile in profiles:
        p=profiles[profile]
        require('effect' in p, f'{profile} missing effect')
        require('message_key' in p, f'{profile} missing message_key')
        require('strain_duration_multiplier' in p and 'overload_duration_multiplier' in p, f'{profile} missing duration multipliers')
for profile, effect in {
    'warming':'herbcraft_alchemy:potion_heat_stir',
    'chilling':'herbcraft_alchemy:potion_cold_stagnation',
    'overdriven':'herbcraft_alchemy:potion_overdrive_rebound',
    'muddy':'herbcraft_alchemy:potion_turbid_disturbance',
    'scattering':'herbcraft_alchemy:potion_qi_scattering',
    'stagnating':'herbcraft_alchemy:potion_qi_stagnation',
}.items():
    require(profiles.get(profile,{}).get('effect')==effect, f'{profile} should route to {effect}')

processing=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java')
for token in ['burden_consequence_profiles','record BurdenConsequenceProfile','burdenConsequenceProfile','exhaustionPerTick','strainDurationMultiplier','overloadDurationMultiplier']:
    require(token in processing, f'PotionProcessingRules missing {token}')
state=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenState.java')
for token in ['dominantProfile','rememberLastPotionContext','PotionBodyStateProjection','lastThermalTendency','lastQiTendency','tailDecayBonus','consequenceDurationMultiplier']:
    require(token in state, f'PlayerPotionBurdenState missing {token}')
runtime=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenConsequenceRuntime.java')
for token in ['burdenConsequenceProfile(state.dominantProfile())','applyProfileExhaustion','player.causeFoodExhaustion','profile.effect()','profile.messageKey()','state.consequenceDurationMultiplier()']:
    require(token in runtime, f'PotionBurdenConsequenceRuntime missing {token}')
effects=read('alchemy/src/main/java/com/herbcraft/alchemy/effect/AlchemyMobEffects.java')
for token in ['POTION_HEAT_STIR','POTION_COLD_STAGNATION','POTION_OVERDRIVE_REBOUND','POTION_TURBID_DISTURBANCE','POTION_QI_SCATTERING','POTION_QI_STAGNATION']:
    require(token in effects, f'AlchemyMobEffects missing {token}')

armor=json.loads((ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/armor_support_rules.json').read_text(encoding='utf-8'))
supports={s.get('id'):s for s in armor.get('supports',[])}
expected={
    'clear_bearing':('purified',12,'muddy'),
    'steady_bearing':('sustained',8,'stagnating'),
    'braced_bearing':('intense',10,'overdriven'),
}
for sid,(orientation,buf,profile) in expected.items():
    s=supports.get(sid)
    require(s is not None, f'missing support {sid}')
    if s:
        require(s.get('orientation')==orientation, f'{sid} orientation mismatch')
        require(s.get('buffer_amount')==buf, f'{sid} buffer should be retuned to {buf}')
        require(profile in s.get('matching_medicinal_profiles',[]), f'{sid} should specialize for {profile}')
        require('active_buffer_amount' in s and 'tail_buffer_amount' in s, f'{sid} missing split active/tail buffers')
        require('match_active_bonus' in s and 'match_tail_bonus' in s, f'{sid} missing match bonuses')
        require('consequence_duration_multiplier' in s, f'{sid} missing consequence mitigation')
armor_rules=read('alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportRules.java')
for token in ['activeBufferAmount','tailBufferAmount','matchingStyles','matchingBurdenProfiles','matchingMedicinalProfiles','matchActiveBonus','matchTailBonus','consequenceDurationMultiplier','tailDecayBonus']:
    require(token in armor_rules, f'ArmorSupportRules missing {token}')
armor_runtime=read('alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportRuntime.java')
for token in ['PotionBurdenBufferResult','activeBuffer','tailBuffer','matches(support','matchingStyles','matchingBurdenProfiles','matchingMedicinalProfiles','modeScale','consequenceMultiplier','tailDecayBonus']:
    require(token in armor_runtime, f'ArmorSupportRuntime missing {token}')
events=read('alchemy/src/main/java/com/herbcraft/alchemy/AlchemyExpansionEvents.java')
for token in ['POTION_BURDEN_BUFFER','POTION_BURDEN_BUFFER_DETAILED','record PotionBurdenBufferResult','activeBuffer','tailBuffer','consequenceDurationMultiplier','tailDecayBonus','plus']:
    require(token in events, f'AlchemyExpansionEvents missing {token}')

for lang in ['en_us','zh_cn']:
    data=json.loads((ROOT/f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json').read_text(encoding='utf-8'))
    for effect in ['potion_heat_stir','potion_cold_stagnation','potion_overdrive_rebound','potion_turbid_disturbance','potion_qi_scattering','potion_qi_stagnation']:
        require(f'effect.herbcraft_alchemy.{effect}' in data, f'missing {lang} effect key {effect}')
    for profile in ['steady','warming','chilling','overdriven','muddy','scattering','stagnating']:
        require(f'message.herbcraft_alchemy.potion_burden.profile.{profile}' in data, f'missing {lang} profile message {profile}')

if errors:
    print('FAIL')
    for e in errors: print(' -',e)
    sys.exit(1)
print('PASS - alchemy burden consequences are profile-specialized and armor support has clear/steady/braced matching semantics.')
