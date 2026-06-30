#!/usr/bin/env python3
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
fail=[]
def check(name, cond):
    print(('PASS' if cond else 'FAIL')+' - '+name)
    if not cond: fail.append(name)
mod=json.loads((root/'alchemy/src/main/resources/fabric.mod.json').read_text())
check('alchemy declares mixin config', 'herbcraft_alchemy.mixins.json' in mod.get('mixins',[]))
check('mixin config exists', (root/'alchemy/src/main/resources/herbcraft_alchemy.mixins.json').exists())
mixin=(root/'alchemy/src/main/java/com/herbcraft/alchemy/mixin/ItemCostAdvancedPotionMixin.java').read_text()
check('mixin targets ItemCost', '@Mixin(ItemCost.class)' in mixin)
check('mixin keeps item type check', 'stack.is(self.item())' in mixin)
check('mixin compares potion path', 'expectedPath' in mixin and 'actualPath' in mixin)
for p in (root/'alchemy/src/main/resources/data/herbcraft/villager_trade').rglob('*.json'):
    data=json.loads(p.read_text())
    pc=data.get('wants',{}).get('components',{}).get('minecraft:potion_contents')
    if pc and 'potion' in pc:
        path=pc['potion'].split(':')[-1]
        if path.endswith('_clarified') or path.endswith('_murky') or path in {'undead_venom','cardiac_toxin','wither_venom_iii'}:
            comps=data['wants'].get('components',{})
            check(f'{p.relative_to(root)} only potion_contents input', set(comps)=={'minecraft:potion_contents'} and set(pc)=={'potion'})
if fail: raise SystemExit('failed checks: '+', '.join(fail))
print('All advanced potion villager trade bugfix validation checks passed.')
