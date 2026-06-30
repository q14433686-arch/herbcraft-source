#!/usr/bin/env python3
import json, sys, zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
errors=[]
mx=ROOT/'alchemy/src/main/resources/herbcraft_alchemy.mixins.json'
src=ROOT/'alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java'
helper=ROOT/'alchemy/src/main/java/com/herbcraft/alchemy/potion/HiddenCuisineCatalystBrewing.java'
alchemy_mod=json.loads((ROOT/'alchemy/src/main/resources/fabric.mod.json').read_text(encoding='utf-8'))
version=alchemy_mod.get('version', 'UNKNOWN')
jar=ROOT/f'alchemy/build/libs/herbcraft-alchemy-{version}.jar'
if not mx.exists(): errors.append('missing mixin json')
elif 'BrewingStandCatalystMixin' not in json.loads(mx.read_text(encoding='utf-8')).get('mixins',[]): errors.append('mixin json missing BrewingStandCatalystMixin')
if not src.exists(): errors.append('missing BrewingStandCatalystMixin.java')
else:
 t=src.read_text(encoding='utf-8')
 for n in ['@Mixin(BrewingStandBlockEntity.class)','method = "canPlaceItem"','method = "isBrewable"','method = "doBrew"','HiddenCuisineCatalystBrewing.brew(items)','ci.cancel()']:
  if n not in t: errors.append(f'catalyst mixin missing {n}')
if not helper.exists(): errors.append('missing HiddenCuisineCatalystBrewing.java')
else:
 t=helper.read_text(encoding='utf-8')
 for n in ['hundred_flower_feast','deadly_hodgepodge','POTION_QUALITY','PotionQuality.EXCEPTIONAL','stack.copy()']:
  if n not in t: errors.append(f'helper missing {n}')
if jar.exists():
 with zipfile.ZipFile(jar) as z: names=set(z.namelist())
 if 'com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.class' not in names: errors.append('jar missing BrewingStandCatalystMixin.class')
else: errors.append('built jar missing')
if errors:
 print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - catalyst mixin source/config/helper/built-jar checks passed.')
