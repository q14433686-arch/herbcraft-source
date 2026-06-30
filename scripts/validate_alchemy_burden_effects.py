#!/usr/bin/env python3
import json
import sys
import zipfile
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def require(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

rules_path=ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/potion_burden_effects.json'
require(rules_path.exists(), 'missing potion_burden_effects.json')
if rules_path.exists():
    data=json.loads(rules_path.read_text(encoding='utf-8'))
    require(data.get('schema_version')==1, 'potion_burden_effects schema_version must be 1')
    effects=data.get('effects',{})
    for effect in ['potion_warming','potion_strain','potion_overload','potion_heat_stir','potion_cold_stagnation','potion_overdrive_rebound','potion_turbid_disturbance','potion_qi_scattering','potion_qi_stagnation','clear_bearing']:
        require(effect in effects, f'missing alchemy burden effect {effect}')
        require(effects[effect].get('category') in ['beneficial','neutral','harmful'], f'{effect} invalid category')
        require(effects[effect].get('color'), f'{effect} missing color')
    strain_attrs=effects.get('potion_strain',{}).get('attributes',[])
    overload_attrs=effects.get('potion_overload',{}).get('attributes',[])
    require(any(a.get('attribute')=='minecraft:movement_speed' and abs(a.get('amount',0)+0.02)<0.0001 for a in strain_attrs), 'potion_strain should carry -2% movement speed custom effect rule')
    require(any(a.get('attribute')=='minecraft:movement_speed' and abs(a.get('amount',0)+0.05)<0.0001 for a in overload_attrs), 'potion_overload should carry -5% movement speed custom effect rule')

effects_java=read('alchemy/src/main/java/com/herbcraft/alchemy/effect/AlchemyMobEffects.java')
rules_java=read('alchemy/src/main/java/com/herbcraft/alchemy/effect/AlchemyBurdenEffectRules.java')
alchemy_init=read('alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java')
consequence=read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenConsequenceRuntime.java')
verify=read('scripts/verify_built_jars.py')
for token in ['POTION_WARMING','POTION_STRAIN','POTION_OVERLOAD','POTION_HEAT_STIR','POTION_COLD_STAGNATION','POTION_OVERDRIVE_REBOUND','POTION_TURBID_DISTURBANCE','POTION_QI_SCATTERING','POTION_QI_STAGNATION','CLEAR_BEARING','isBurdenStageEffect','Registry.registerForHolder']:
    require(token in effects_java, f'AlchemyMobEffects missing {token}')
for token in ['potion_burden_effects.json','record EffectRule','record AttributeRule','add_multiplied_base','MobEffectCategory']:
    require(token in rules_java, f'AlchemyBurdenEffectRules missing {token}')
require('AlchemyMobEffects.initialize();' in alchemy_init, 'HerbcraftAlchemy should initialize custom Alchemy mob effects')
require('AlchemyMobEffects::isBurdenStageEffect' in consequence and 'MobEffectInstance' in consequence, 'burden consequence runtime should apply custom effects')
for lang in ['en_us','zh_cn']:
    data=json.loads((ROOT/f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json').read_text(encoding='utf-8'))
    for effect in ['potion_warming','potion_strain','potion_overload','potion_heat_stir','potion_cold_stagnation','potion_overdrive_rebound','potion_turbid_disturbance','potion_qi_scattering','potion_qi_stagnation','clear_bearing']:
        require(f'effect.herbcraft_alchemy.{effect}' in data, f'missing {lang} lang key for {effect}')
for effect in ['potion_warming','potion_strain','potion_overload','potion_heat_stir','potion_cold_stagnation','potion_overdrive_rebound','potion_turbid_disturbance','potion_qi_scattering','potion_qi_stagnation','clear_bearing']:
    p=ROOT/f'alchemy/src/main/resources/assets/herbcraft_alchemy/textures/mob_effect/{effect}.png'
    require(p.exists(), f'missing icon {effect}.png')
    if p.exists():
        img=Image.open(p)
        require(img.size==(18,18), f'{effect}.png should be 18x18')
for token in ['AlchemyMobEffects.class','AlchemyBurdenEffectRules.class','potion_burden_effects.json','textures/mob_effect/potion_strain.png']:
    require(token in verify, f'verify_built_jars should check {token}')
jar=ROOT/'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z: names=set(z.namelist())
    for cls in ['com/herbcraft/alchemy/effect/AlchemyMobEffects.class','com/herbcraft/alchemy/effect/AlchemyBurdenEffectRules.class']:
        require(cls in names, f'built jar missing {cls}')
    require('data/herbcraft_alchemy/potion_burden_effects.json' in names, 'built jar missing potion_burden_effects.json')
if errors:
    print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - Alchemy burden custom effects are JSON-backed, registered, localized, iconized, and used by burden stages.')
