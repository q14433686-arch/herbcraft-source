#!/usr/bin/env python3
from pathlib import Path
import json
root = Path(__file__).resolve().parents[1]
failures=[]
for module in ['core','alchemy','cuisine']:
    assets=root/module/'src/main/resources/assets'
    for lang_dir in assets.glob('*/lang') if assets.exists() else []:
        zh=lang_dir/'zh_cn.json'; en=lang_dir/'en_us.json'
        if not (zh.exists() and en.exists()):
            continue
        zk=set(json.loads(zh.read_text(encoding='utf-8')).keys())
        ek=set(json.loads(en.read_text(encoding='utf-8')).keys())
        missing=sorted(zk-ek); extra=sorted(ek-zk)
        print(f'[{module}:{lang_dir.parent.name}]')
        print(f'Missing in en_us: {len(missing)}')
        for k in missing: print('-', k)
        print(f'Extra in en_us: {len(extra)}')
        for k in extra: print('-', k)
        en_data=json.loads(en.read_text(encoding='utf-8'))
        quality=[]
        for key,value in en_data.items():
            text=str(value)
            if any('\u4e00' <= ch <= '\u9fff' for ch in text):
                quality.append(f'{key}: contains CJK text in en_us')
            if 'Ch.' in text:
                quality.append(f'{key}: uses ambiguous chapter abbreviation Ch.; use Chapter instead')
            if 'Turbid' in text or 'turbid' in text:
                quality.append(f'{key}: uses Turbid; current English alchemy wording should use Murky')
        print(f'English quality issues: {len(quality)}')
        for q in quality: print('-', q)
        if missing or extra or quality: failures.append(f'{module}:{lang_dir.parent.name}')
if failures:
    raise SystemExit('language key/quality differences found: '+', '.join(failures))
print('All zh_cn/en_us language key sets match exactly, and en_us quality checks passed.')
