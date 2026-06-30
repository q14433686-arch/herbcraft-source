#!/usr/bin/env python3
import json, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errs=[]
def ok(c,m):
 print(('PASS - ' if c else 'FAIL - ')+m)
 if not c: errs.append(m)
# Hodgepodge code checks
hp=(ROOT/'cuisine/src/main/java/com/herbcraft/cuisine/recipe/HodgepodgeRecipe.java').read_text()
ok('item == Items.BOWL' in hp, 'hodgepodge only accepts vanilla empty bowl')
ok('CuisineItems.WATER_BOWL' not in hp, 'hodgepodge does not accept water bowl')
ok('Items.RED_MUSHROOM' in hp and 'Items.BROWN_MUSHROOM' in hp, 'hodgepodge rejects vanilla suspicious-stew mushrooms')
ok('isHiddenSpecialCombo' in hp, 'hodgepodge explicitly handles hidden-combo empty-bowl isolation cases')
# Toxic/mushrooms not in herb tag
herb=json.loads((ROOT/'core/src/main/resources/data/herbcraft/tags/item/herb.json').read_text())['values']
for forbidden in ['minecraft:red_mushroom','minecraft:brown_mushroom','minecraft:red_mushroom_block','minecraft:brown_mushroom_block','minecraft:mushroom_stem']:
 ok(forbidden not in herb, f'{forbidden} is not in #herbcraft:herb')
# Raw medicinal recipes use water bowl; no raw recipe uses empty bowl
rdir=ROOT/'cuisine/src/main/resources/data/herbcraft_cuisine/recipe'
raws=list(rdir.glob('raw_*.json'))
ok(len(raws)>=22, 'raw dish recipes include existing medicinal dishes plus hidden dishes')
for p in raws:
 d=json.loads(p.read_text())
 vals=d.get('ingredients',[])
 ok('herbcraft_cuisine:water_bowl' in vals, f'{p.name} uses water bowl')
 ok('minecraft:bowl' not in vals, f'{p.name} does not use empty bowl')
# Hidden final direct recipes must not exist; hidden raw/cooking routes must exist
for direct in ['hundred_flower_feast.json','deadly_hodgepodge.json']:
 ok(not (rdir/direct).exists(), f'{direct} direct final crafting recipe removed')
for raw in ['raw_hundred_flower_feast.json','raw_deadly_hodgepodge.json']:
 ok((rdir/raw).exists(), f'{raw} exists')
 d=json.loads((rdir/raw).read_text())
 ok(d['ingredients'][0]=='herbcraft_cuisine:water_bowl', f'{raw} begins with water bowl')
for final in ['hundred_flower_feast','deadly_hodgepodge']:
 for suffix in ['from_smelting','from_smoking','from_campfire']:
  ok((rdir/f'{final}_{suffix}.json').exists(), f'{final}_{suffix}.json cooking route exists')
# Hidden raw assets should exist
for item in ['raw_hundred_flower_feast','raw_deadly_hodgepodge']:
 for rel in [f'cuisine/src/main/resources/assets/herbcraft_cuisine/items/{item}.json', f'cuisine/src/main/resources/assets/herbcraft_cuisine/models/item/{item}.json', f'cuisine/src/main/resources/assets/herbcraft_cuisine/textures/item/{item}.png']:
  ok((ROOT/rel).exists(), f'{item} asset exists: {Path(rel).name}')
if errs: sys.exit(1)
print('All recipe isolation checks passed.')
