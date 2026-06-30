#!/usr/bin/env python3
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errs=[]

def require(cond,msg):
    if cond: print('PASS - '+msg)
    else:
        print('FAIL - '+msg); errs.append(msg)

# Core immunity code presence
require((ROOT/'core/src/main/java/com/herbcraft/logic/EffectImmunityManager.java').exists(), 'core has EffectImmunityManager')
api=(ROOT/'core/src/main/java/com/herbcraft/api/HerbcraftAPI.java').read_text()
require('grantEffectImmunity' in api and 'isImmuneToEffect' in api, 'HerbcraftAPI exposes immunity grant/query')
player=(ROOT/'core/src/main/java/com/herbcraft/mixin/PlayerMixin.java').read_text()
require('HerbcraftEffectImmunities' in player, 'effect immunities are saved/loaded')
require('herbcraft$clearEffectImmunities' in player and 'herbcraft$clearOnDeath' in player, 'effect immunities clear on death')
mixin=json.loads((ROOT/'core/src/main/resources/herbcraft.mixins.json').read_text())
require('LivingEntityMixin' in mixin.get('mixins',[]), 'LivingEntityMixin registered')
lem=(ROOT/'core/src/main/java/com/herbcraft/mixin/LivingEntityMixin.java').read_text()
require('addEffect' in lem and 'isImmuneToEffect' in lem and 'setReturnValue(false)' in lem, 'LivingEntity.addEffect is cancellable by immunity')

# Cuisine immunity and custom effect data
dishes=json.loads((ROOT/'cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json').read_text())
all_dishes=dishes['dishes']+dishes['medicinal_dishes']
clearing=[d for d in all_dishes if d.get('remove_effects')]
require(clearing and all(d.get('effect_immunities') for d in clearing), 'all dishes that remove effects also grant immunities')
require(all(i['duration_seconds']>=30 for d in clearing for i in d.get('effect_immunities',[])), 'all dish immunity durations are at least 30 seconds')
by_id={d['id']:d for d in all_dishes}
require(by_id['soul_cleansing_stew']['effect_immunities'][0]['effect']=='minecraft:wither' or any(i['effect']=='minecraft:wither' for i in by_id['soul_cleansing_stew']['effect_immunities']), 'blood-purifying mapped dish grants wither immunity')
require(any(i['effect']=='minecraft:mining_fatigue' for i in by_id['fatigue_breaking_stew']['effect_immunities']), 'guardian-target dish grants mining fatigue immunity')
require(any(e['effect']=='minecraft:haste' and e['amplifier']>=0 for e in by_id['fatigue_breaking_stew']['effects']), 'guardian-target dish has haste mining buff')
require(by_id['golden_dandelion_stew'].get('clear_all_effects') is True, 'emergency dish clears all effects')
require(any(e['effect']=='minecraft:instant_health' for e in by_id['golden_dandelion_stew']['effects']), 'emergency dish gives instant health')
require(by_id['blue_ice_brew'].get('clear_frozen') is True, 'cold-dispelling dish clears frozen ticks')
require(by_id['crimson_war_hotpot'].get('ignite_seconds')==8, 'spicy combat dish ignites player for 8 seconds')

# Core herbs changes
herbs=json.loads((ROOT/'core/src/main/resources/data/herbcraft/herbs.json').read_text())['herbs']
h={e['item']:e for e in herbs}
g=h.get('minecraft:glistering_melon_slice')
require(g is not None, 'glistering melon exists')
require(g['nutrition']==4 and abs(g['saturation_modifier']-9.6)<1e-6 and g['medicinal'] is True, 'glistering melon nutrition/saturation/always edible updated to 4/9.6')
require(any(e['effect']=='minecraft:instant_health' and e['chance']==1.0 for e in g['effects']), 'glistering melon guarantees instant health')
require(any(e['effect']=='minecraft:absorption' and e['chance']==1.0 and e.get('duration_seconds')==30 for e in g['effects']), 'glistering melon guarantees absorption 30s')
require('heal_events' not in g, 'glistering melon no longer relies on separate heal event')
gc=h.get('minecraft:golden_carrot')
require(gc is not None, 'golden carrot added to herbs.json')
require(gc['nutrition']==6 and abs(gc['saturation_modifier']-14.4)<1e-6 and gc['medicinal'] is False, 'golden carrot keeps vanilla food values and is not always edible')
require(any(e['effect']=='minecraft:night_vision' and e['chance']==1.0 and e.get('duration_seconds')==10 for e in gc['effects']), 'golden carrot grants 10s night vision')
tag=json.loads((ROOT/'core/src/main/resources/data/herbcraft/tags/item/herb.json').read_text())
require('minecraft:golden_carrot' in tag['values'], 'golden carrot is included in herb tag')

if errs:
    raise SystemExit(1)
print('All task A/B/C/D validation checks passed.')
