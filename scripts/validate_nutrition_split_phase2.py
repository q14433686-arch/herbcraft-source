#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errs = []

def require(cond, msg):
    if cond:
        print('PASS - ' + msg)
    else:
        print('FAIL - ' + msg)
        errs.append(msg)

manager = (ROOT / 'core/src/main/java/com/herbcraft/nutrition/NutritionManager.java').read_text()
runtime = (ROOT / 'core/src/main/java/com/herbcraft/nutrition/NutritionRuntimeService.java').read_text()
effects = (ROOT / 'core/src/main/java/com/herbcraft/nutrition/NutritionEffectApplicator.java').read_text()
discovery = (ROOT / 'core/src/main/java/com/herbcraft/nutrition/NutritionDiscoveryHooks.java').read_text()
debug = (ROOT / 'core/src/main/java/com/herbcraft/nutrition/NutritionDebugService.java').read_text()

require('NutritionRuntimeService.tick(player, gameTime);' in manager, 'manager delegates ticking to runtime service')
require('NutritionRuntimeService.onLogin(player);' in manager, 'manager delegates login to runtime service')
require('NutritionRuntimeService.onDeath(player);' in manager, 'manager delegates death handling to runtime service')
require('NutritionDiscoveryHooks.onFoodConsumed(player, stack, profile);' in manager, 'manager delegates knowledge/discovery side effects')
require('NutritionDebugService.setNutritionForDebug(player, dimension, value);' in manager, 'manager delegates debug set operation')
require('NutritionDebugService.applyNutritionPresetForDebug(player, preset);' in manager, 'manager delegates debug preset operation')
require('class NutritionRuntimeService' in runtime and 'evaluateAndApply' in runtime, 'runtime evaluation service exists')
require('class NutritionEffectApplicator' in effects and 'applyDimensionEffects' in effects and 'removeAllModifiers' in effects, 'effect applicator exists')
require('class NutritionDiscoveryHooks' in discovery and 'INGREDIENT_DISCOVERED' in discovery, 'discovery hooks exist')
require('class NutritionDebugService' in debug and 'applyNutritionPresetForDebug' in debug, 'debug service exists')

if errs:
    raise SystemExit(1)
print('All nutrition split phase 2 validation checks passed.')
