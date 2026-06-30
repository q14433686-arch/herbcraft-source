#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(cond, msg):
    if not cond:
        errors.append(msg)

def read(path):
    return (ROOT / path).read_text(encoding='utf-8')

state = read('core/src/main/java/com/herbcraft/nutrition/PlayerNutritionState.java')
manager = '\n'.join([
    read('core/src/main/java/com/herbcraft/nutrition/NutritionManager.java'),
    read('core/src/main/java/com/herbcraft/nutrition/NutritionRuntimeService.java'),
    read('core/src/main/java/com/herbcraft/nutrition/NutritionEffectApplicator.java'),
    read('core/src/main/java/com/herbcraft/nutrition/NutritionDebugService.java'),
])
rules_java = read('core/src/main/java/com/herbcraft/nutrition/NutritionRules.java')
sync = read('core/src/main/java/com/herbcraft/nutrition/NutritionSyncPayload.java')
cache = read('core/src/main/java/com/herbcraft/nutrition/client/NutritionClientCache.java')
panel = read('core/src/main/java/com/herbcraft/nutrition/client/NutritionPanelScreen.java')
player_mixin = read('core/src/main/java/com/herbcraft/mixin/PlayerMixin.java')
commands = read('core/src/main/java/com/herbcraft/nutrition/NutritionDebugCommands.java')

rules = json.load(open(ROOT / 'core/src/main/resources/data/herbcraft/nutrition_rules.json', encoding='utf-8'))
for key, expected in {
    'recent_meal_limit': 24,
    'recent_meal_window_ticks': 7200,
    'correction_recent_window_ticks': 3600,
    'correction_high_threshold': 750,
    'correction_target_value': 650,
    'correction_score_threshold': 60,
    'correction_extra_decay_interval_ticks': 200,
    'correction_extra_decay_min': 15,
    'correction_extra_decay_max': 45,
    'fruit_high_non_punitive': True,
    'fluid_surplus_threshold': 850,
    'fluid_surplus_target_value': 700,
    'fluid_surplus_decay_interval_ticks': 200,
    'fluid_surplus_decay_amount': 10,
}.items():
    require(rules.get(key) == expected, f'nutrition_rules.json {key} expected {expected!r}, got {rules.get(key)!r}')


require('i(r,"correction_high_threshold",750)' in rules_java, 'NutritionRules JSON-load fallback for correction_high_threshold must be 750')
require('i(r,"correction_high_threshold",850)' not in rules_java, 'NutritionRules still has stale correction_high_threshold fallback 850')
# The Java fallback matrix is used if nutrition_correction_rules.json is missing or malformed.
# It must not silently re-couple 津液/Fluids to the ordinary correction matrix.
for stale in ['"fruit", 0.35F', '"fruit", 0.80F', '"fruit", 0.40F']:
    require(stale not in rules_java, f'NutritionRules defaultCorrectionWeights still contains stale fruit fallback weight {stale}')
for expected_token in [
    '"grain", Map.of("vege", 1.35F, "flesh", 1.15F, "oil", 0.45F, "fruit", 0.0F)',
    '"flesh", Map.of("vege", 1.60F, "fruit", 0.0F, "grain", 0.0F, "oil", 0.0F)',
    '"oil", Map.of("vege", 1.45F, "fruit", 0.0F, "grain", 0.25F, "flesh", 0.0F)',
    '"vege", Map.of("grain", 1.20F, "flesh", 1.20F, "oil", 0.50F, "fruit", 0.0F)',
]:
    require(expected_token in rules_java, f'NutritionRules defaultCorrectionWeights should mirror zero-fruit correction fallback: {expected_token}')

for token in [
    'recentMealLimit', 'recentMealWindowTicks', 'correctionRecentWindowTicks',
    'correctionHighThreshold', 'correctionTargetValue', 'correctionScoreThreshold',
    'correctionExtraDecayIntervalTicks', 'correctionExtraDecayMin', 'correctionExtraDecayMax',
    'fruitHighNonPunitive', 'fluidSurplusThreshold', 'fluidSurplusTargetValue',
    'fluidSurplusDecayIntervalTicks', 'fluidSurplusDecayAmount', 'selfPenaltyMultiplier',
    'correctionWeights', 'correctionWeight(', 'nutrition_correction_rules.json'
]:
    require(token in rules_java, f'NutritionRules missing field/load token {token}')

for token in [
    'Deque<PlayerNutritionState.RecentMeal>', 'recordMeal(', 'tickCorrectionDecay(', 'correctionScore(',
    'correctionDecayAmount(', 'isCorrectionActive(', 'hasPunitiveImbalance(', 'encodeRecentMeals(',
    'decodeRecentMeals(', 'record RecentMeal'
]:
    require(token in state, f'PlayerNutritionState missing correction/recent meal token: {token}')

require('state.recordMeal(player.level().getGameTime()' in manager, 'NutritionManager should record recent meals on food consumed')
require('state.tickCorrectionDecay(gameTime)' in manager, 'NutritionManager should tick accelerated correction decay')
require('state.isCorrectionActive(rules, gameTime)' in manager, 'NutritionManager should detect active correction')
require('state.hasPunitiveImbalance(rules)' in manager, 'NutritionManager should use punitive imbalance helper')
require('nutrition.herbcraft.state.correcting' in manager, 'NutritionManager should message correcting state')
require('!rules.fruitHighNonPunitive' in manager, 'NutritionManager should gate high fruit excess punishment')
for token in ['value < rules.balancedMin', 'value < rules.balancedMax', 'value < rules.imbalanceHigh']:
    require(token in manager, f'NutritionManager dimension bands should use data-backed NutritionRules threshold: {token}')
for stale in ['value < 350', 'value < 650', 'value < 850', 'high - min > 500']:
    require(stale not in manager and stale not in state, f'Nutrition threshold/spread should not be hardcoded: {stale}')
require('case "correcting_flesh"' in manager and 'case "correcting_oil"' in manager and 'case "correcting_grain"' in manager, 'NutritionManager missing correcting debug presets')
require('excess_fruit_nonpunitive' in manager and 'excess_fruit_nonpunitive' in commands, 'missing excess fruit nonpunitive debug preset')
require('correcting_flesh' in commands and 'correcting_oil' in commands and 'correcting_grain' in commands, 'NutritionDebugCommands missing correction presets')
require('rules.correctionWeight(highDim, dim)' in state, 'Correction score should use data-backed dimension relationship matrix')
require('high - min > rules.imbalanceSpread' in state, 'Punitive imbalance spread should use NutritionRules.imbalanceSpread, not a hardcoded value')
require('applyFluidSurplusDecay' not in state, 'PlayerNutritionState should not keep the old unused fluid surplus decay helper; tickCorrectionDecay owns stacked decay')
require('get(dim) < rules.correctionTargetValue' not in state, 'Correction score must not require corrective dimensions to be below target; matrix should decide relationships')
require('corrective > meal.values().getOrDefault(highDim, 0) * rules.selfPenaltyMultiplier' in state, 'Correction score should require weighted corrective nutrition to exceed same-high-dimension contribution')
require('score / 12' in state and 'correctionExtraDecayMin + score / 12' in state, 'Correction decay should use tuned 15..45 matrix-score formula')
require('originalValues' in state and 'decayByDimension' in state and 'targetByDimension' in state and 'addDecay(decayByDimension, targetByDimension' in state, 'Matrix and high-fluids decay should stack from original tick values before applying')
require('isFluidSurplusActive' in state and 'fluidSurplusTargetValue' in state, 'High fluids surplus helper should normalize other excesses toward target')

require('RecentMeals' in player_mixin and 'encodeRecentMeals()' in player_mixin and 'decodeRecentMeals' in player_mixin, 'PlayerMixin should persist recent meals')
require('statusCode = 3; // 0=starved,1=malnourished,2=imbalanced,3=normal,4=abundant,5=balanced,6=correcting' in cache, 'NutritionClientCache comment/status should include correcting')
require('nutrition.herbcraft.state.correcting' in cache, 'NutritionClientCache missing correcting translation key')
require('return 6; // correcting' in sync, 'NutritionSyncPayload should emit status 6 for correction')
require('isCorrecting()' in cache and 'NutritionClientCache.isCorrecting()' in panel, 'client panel should color correcting status')
for token in ['CodexTextLayout.trimWithEllipsis', 'drawCenteredTrimmed', 'drawCenteredWrappedLimited', 'labelMaxWidth', 'enableScissor(panelX, panelY', 'disableScissor()']:
    require(token in panel, f'NutritionPanelScreen should defensively trim/wrap/scissor translated panel text: {token}')

matrix_path = ROOT / 'core/src/main/resources/data/herbcraft/nutrition_correction_rules.json'
require(matrix_path.exists(), 'missing nutrition_correction_rules.json')
if matrix_path.exists():
    matrix_data = json.load(open(matrix_path, encoding='utf-8'))
    matrix = matrix_data.get('matrix', {})
    expected = {
        'grain': {'vege': 1.35, 'flesh': 1.15, 'oil': 0.45, 'fruit': 0.0},
        'flesh': {'vege': 1.6, 'fruit': 0.0, 'grain': 0.0, 'oil': 0.0},
        'oil': {'vege': 1.45, 'fruit': 0.0, 'grain': 0.25, 'flesh': 0.0},
        'vege': {'grain': 1.2, 'flesh': 1.2, 'oil': 0.5, 'fruit': 0.0},
        'fruit': {},
    }
    require(matrix_data.get('self_penalty_multiplier') == 1.0, 'self_penalty_multiplier should be 1.0')
    for high, row in expected.items():
        require(high in matrix, f'matrix missing row {high}')
        for dim, value in row.items():
            require(abs(matrix.get(high, {}).get(dim, -999) - value) < 0.0001, f'matrix {high}.{dim} expected {value}')

for lang in ['en_us','zh_cn']:
    data = json.load(open(ROOT / f'core/src/main/resources/assets/herbcraft/lang/{lang}.json', encoding='utf-8'))
    require('nutrition.herbcraft.state.correcting' in data, f'missing {lang} correcting lang key')

if errors:
    print('FAIL')
    for err in errors:
        print(' -', err)
    raise SystemExit(1)
print('PASS - nutrition correction recent-meal tracking, weighted correction decay, non-punitive high fluids, client correcting status, debug presets, and persistence are wired.')
