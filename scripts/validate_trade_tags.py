#!/usr/bin/env python3
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
fail=[]
def check(name, cond):
    print(('PASS' if cond else 'FAIL')+' - '+name)
    if not cond: fail.append(name)
for module in ['core','alchemy','cuisine']:
    res=root/module/'src/main/resources'
    trades=[]
    for nsdir in (res/'data').glob('*') if (res/'data').exists() else []:
        vt=nsdir/'villager_trade'
        if vt.exists():
            for p in vt.rglob('*.json'):
                trades.append(f'{nsdir.name}:{p.relative_to(vt).with_suffix("").as_posix()}')
    refs=set()
    tag_root=res/'data/minecraft/tags/villager_trade'
    for tag in tag_root.rglob('*.json') if tag_root.exists() else []:
        data=json.loads(tag.read_text(encoding='utf-8'))
        check(f'{module} {tag.relative_to(res)} replace=false', data.get('replace') is False)
        refs.update(v for v in data.get('values',[]) if isinstance(v,str) and not v.startswith('#'))
    for trade in trades:
        check(f'{module} trade referenced by tag: {trade}', trade in refs)
if fail: raise SystemExit('failed checks: '+', '.join(fail))
print('All villager trade tag validation checks passed.')
