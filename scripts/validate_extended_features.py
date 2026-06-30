#!/usr/bin/env python3
import json, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errs=[]
def ok(c,m):
 print(('PASS - ' if c else 'FAIL - ')+m)
 if not c: errs.append(m)
# glistering
h={e['item']:e for e in json.loads((ROOT/'core/src/main/resources/data/herbcraft/herbs.json').read_text())['herbs']}
g=h['minecraft:glistering_melon_slice']
ok(g['nutrition']==4 and abs(g['saturation_modifier']-9.6)<1e-6, 'glistering melon slice is 4 nutrition / 9.6 saturation')
ok(any(e['effect']=='minecraft:instant_health' for e in g['effects']) and any(e['effect']=='minecraft:absorption' for e in g['effects']), 'glistering melon effects preserved')
# stack feedback
pm=(ROOT/'core/src/main/java/com/herbcraft/mixin/PlayerMixin.java').read_text(); hm=(ROOT/'core/src/main/java/com/herbcraft/logic/HerbStackManager.java').read_text()
ok('message.herbcraft.herb_stack.stage_' in pm and 'sendSystemMessage' in pm, 'herb stack threshold private messages implemented')
ok('message.herbcraft.herb_stack.fade' in pm and 'sendSystemMessage' in pm, 'herb stack fade private message implemented')
ok('message.herbcraft.herb_stack.overload' in hm, 'herb stack overload overlay implemented')
# hidden dishes
jd=json.loads((ROOT/'cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json').read_text())
d={x['id']:x for x in jd['dishes']+jd['medicinal_dishes']}
for item in ['hundred_flower_feast','deadly_hodgepodge']:
 ok(item in d, f'{item} registered in dishes.json')
 ok((ROOT/f'cuisine/src/main/resources/data/herbcraft_cuisine/recipe/raw_{item}.json').exists(), f'raw_{item} water-bowl recipe exists')
 ok((ROOT/f'cuisine/src/main/resources/assets/herbcraft_cuisine/items/{item}.json').exists(), f'{item} item definition exists')
 ok((ROOT/f'cuisine/src/main/resources/assets/herbcraft_cuisine/models/item/{item}.json').exists(), f'{item} model exists')
 ok((ROOT/f'cuisine/src/main/resources/assets/herbcraft_cuisine/textures/item/{item}.png').exists(), f'{item} texture exists')
ok(d['hundred_flower_feast']['can_always_eat'] is True and d['hundred_flower_feast']['nutrition']==10, 'hundred flower feast food values set')
ok(d['deadly_hodgepodge'].get('clear_all_effects') is True and d['deadly_hodgepodge']['can_always_eat'] is True, 'deadly hodgepodge clear-all and always-edible set')
# advancements
counts={
 'core/src/main/resources/data/herbcraft/advancement':16,
 'cuisine/src/main/resources/data/herbcraft_cuisine/advancement':6,
 'alchemy/src/main/resources/data/herbcraft_alchemy/advancement':13,
}
for rel,n in counts.items():
 files=list((ROOT/rel).glob('*.json'))
 ok(len(files)==n, f'{rel} contains {n} advancement files')
 for f in files: json.loads(f.read_text())
# witch integration
ar=(ROOT/'alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java').read_text(); wh=(ROOT/'alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyCodexHooks.java').read_text()
ok('WITCH_TULIP_MURKY' in ar and 'WITCH_CLOSED_EYEBLOSSOM_MURKY' in ar, 'safe witch murky potion variants registered')
wl=json.loads((ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/witch_loot.json').read_text())
ok('witch_loot.json' in wh and wl.get('page',{}).get('chance') == 0.15 and wl.get('essence_pool',{}).get('chance') == 0.10 and wl.get('potion_pool',{}).get('chance') == 0.03, 'witch loot integration includes data-backed page/essence/potion pools')
ok('WITHER_VENOM_III' not in wh and 'UNDEAD_VENOM' not in wh and 'CARDIAC_TOXIN' not in wh, 'witch loot excludes dangerous T3 potions')
mm=json.loads((ROOT/'alchemy/src/main/resources/herbcraft_alchemy.mixins.json').read_text())
ok('WitchHerbcraftPotionMixin' in mm.get('mixins', []), 'witch safe potion throw mixin registered')
wm=(ROOT/'alchemy/src/main/java/com/herbcraft/alchemy/mixin/WitchHerbcraftPotionMixin.java').read_text()
sp=json.loads((ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/special_potions.json').read_text())
ok('witchHerbcraftPotionChance' in wm and 'pickWitchThrowPotion' in wm and 'ci.cancel()' in wm and len(sp.get('witch_throw_pool', [])) == 5, 'witch safe potion throw replacement is configurable, data-backed, and cancellable')
if errs: sys.exit(1)
print('All extended feature validation checks passed.')
