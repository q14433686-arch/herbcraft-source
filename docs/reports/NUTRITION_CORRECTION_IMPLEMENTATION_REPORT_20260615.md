# Nutrition Correction Implementation Report

Date: 2026-06-15  
Scope: B + E hybrid nutrition correction / recent-meal redesign.  
Status: implemented and build/validator passed in sandbox; real-client validation still recommended.

## Implemented features

### 1. Recent 24 meal tracking

Implemented in:

```text
core/src/main/java/com/herbcraft/nutrition/PlayerNutritionState.java
```

New recent-meal state:

```text
Deque<RecentMeal>
RecentMeal(long gameTime, String itemId, Map<String, Integer> values)
```

Meals are recorded from the existing all-food hook:

```text
core/src/main/java/com/herbcraft/mixin/ItemMixin.java
  -> NutritionManager.onFoodConsumed(serverPlayer, stack)
```

`NutritionManager.onFoodConsumed(...)` now records the consumed item ID and its nutrition profile before applying/evaluating nutrition.

### 2. Recent-meal retention and correction scoring

New rule fields in:

```text
core/src/main/resources/data/herbcraft/nutrition_rules.json
```

```json
"recent_meal_limit": 24,
"recent_meal_window_ticks": 7200,
"correction_recent_window_ticks": 3600,
"correction_high_threshold": 850,
"correction_target_value": 650,
"correction_score_threshold": 60,
"correction_extra_decay_interval_ticks": 200,
"correction_extra_decay_min": 15,
"correction_extra_decay_max": 45,
"fruit_high_non_punitive": true,
"fluid_surplus_threshold": 850,
"fluid_surplus_target_value": 700,
"fluid_surplus_decay_interval_ticks": 200,
"fluid_surplus_decay_amount": 10
```

Correction relationship matrix data lives in:

```text
core/src/main/resources/data/herbcraft/nutrition_correction_rules.json
```

Rules are loaded by:

```text
core/src/main/java/com/herbcraft/nutrition/NutritionRules.java
```

Current behavior:

```text
- Keep up to 24 recent meals.
- Keep broad recent meal memory for 7200 ticks / 6 minutes.
- Use last 3600 ticks / 3 minutes for active correction scoring.
- Correction threshold is 60 raw nutrition points.
```

### 3. Amount-weighted corrective meals with relationship matrix

Correction is not a simple tag/bool. It is weighted by actual nutrition amount and by the relationship matrix in `nutrition_correction_rules.json`.

Current matrix:

```text
grain excess: vege 1.35, flesh 1.15, oil 0.45, fruit 0.0
flesh excess: vege 1.60, fruit 0.0, grain 0.0, oil 0.0
oil excess: vege 1.45, fruit 0.0, grain 0.25, flesh 0.0
vege excess: grain 1.20, flesh 1.20, oil 0.50, fruit 0.0
fruit excess: no punitive correction target
```

津液/Fluids is intentionally decoupled from ordinary matrix correction. It only acts through low-fluids deficiency and the high-fluids surplus helper. This prevents widespread small/medium fruit values on ordinary Core/Cuisine foods from suppressing DIETARY_IMBALANCE too broadly.

For each high punitive dimension `H`, a meal first computes:

```text
corrective = sum(profile[d] * matrix[H][d] where d != H)
highContribution = profile[H] * self_penalty_multiplier
```

A meal only contributes if:

```text
corrective > highContribution
```

Then it adds the weighted `corrective` value to the recent score. This prevents foods dominated by the already-excessive dimension from counting as corrective merely because they contain trace secondary nutrition.

This means:

```text
small genuinely corrective foods may require several meals;
medium foods may require one or two;
high-value Cuisine/hidden dishes can correct quickly;
more of the same excessive food will not usually correct itself;
grain does not correct flesh, and flesh does not correct oil, according to the matrix.
```

### 4. Accelerated high-dimension decay

Implemented in:

```text
PlayerNutritionState.tickCorrectionDecay(...)
NutritionManager.tick(...)
```

When correction is active:

```text
- every 200 ticks / 10 seconds,
- excessive dimensions receive extra decay,
- decay amount is 15..45 based on weighted recent correction score,
- decay stops at correction_target_value = 650.
```

Formula:

```text
extraDecay = clamp(15, 45, 15 + correctionScore / 12)
```

Approximate 1000 -> 650 timing:

```text
weak correction around 3.8 minutes;
medium correction around 2.3 minutes;
strong correction around 1.6 minutes;
very strong correction around 1.3 minutes.
```

### 5. Correcting state suppresses active dietary imbalance punishment

Implemented in:

```text
core/src/main/java/com/herbcraft/nutrition/NutritionManager.java
```

If stored values are still imbalanced but recent meals are structurally corrective:

```text
- bad-state streak is cleared;
- sustained DIETARY_IMBALANCE reapplication is suppressed;
- existing DIETARY_IMBALANCE is removed;
- player can receive nutrition.herbcraft.state.correcting message.
```

### 6. High 津液 / Fluids is non-punitive and beneficial in base mod

Implemented through:

```text
fruit_high_non_punitive = true
```

Base-mod policy now:

```text
- high fruit/津液/Fluids does not count as a punitive high dimension for persistent imbalance;
- high fruit/津液/Fluids does not roll the short EXCESS dietary_imbalance effect;
- low fruit/津液/Fluids still causes FRUIT_DEFICIENCY;
- fruit outside 400..600 can still block WELL_NOURISHED / 五养调和.
```

Additional high-fluids helper:

```text
If fruit >= 850, every 200 ticks / 10 seconds each non-fruit dimension above 850 is reduced by 10 toward 700.
```

Matrix decay and this high-fluids helper now stack from original tick values. If both mechanisms apply to the same dimension, their deltas are summed and then applied once with the stricter applicable target.

This preserves the deficiency role of 津液 while avoiding punishment for normal hydration/Homeostatic play and gives surplus 津液 a slow normalizing benefit.

### 7. Client status: correcting / 饮食回正中

New status code:

```text
6 = correcting
```

Updated files:

```text
core/src/main/java/com/herbcraft/nutrition/NutritionSyncPayload.java
core/src/main/java/com/herbcraft/nutrition/client/NutritionClientCache.java
core/src/main/java/com/herbcraft/nutrition/client/NutritionPanelScreen.java
core/src/main/resources/assets/herbcraft/lang/en_us.json
core/src/main/resources/assets/herbcraft/lang/zh_cn.json
```

Language keys:

```text
nutrition.herbcraft.state.correcting = Diet is correcting itself.
nutrition.herbcraft.state.correcting = 饮食渐趋调和。
```

Panel color for correcting is a light green/aqua tint.

### 8. Persistence

Implemented in:

```text
core/src/main/java/com/herbcraft/mixin/PlayerMixin.java
```

Recent meals are stored under:

```text
HerbcraftNutrition/RecentMeals
```

Encoding format for this first pass:

```text
gameTime,itemId,grain,flesh,oil,vege,fruit;...
```

This follows the project's existing compact-string persistence style. A future cleanup may move to structured child entries if needed.

### 9. Debug presets

Added presets in:

```text
core/src/main/java/com/herbcraft/nutrition/NutritionDebugCommands.java
core/src/main/java/com/herbcraft/nutrition/NutritionManager.java
```

New presets:

```text
correcting_flesh
correcting_oil
correcting_grain
excess_fruit_nonpunitive
```

Usage examples:

```mcfunction
/herbcraft nutrition preset @p correcting_flesh
/herbcraft nutrition preset @p correcting_oil
/herbcraft nutrition preset @p correcting_grain
/herbcraft nutrition preset @p excess_fruit_nonpunitive
```

## Files changed

Code:

```text
core/src/main/java/com/herbcraft/nutrition/PlayerNutritionState.java
core/src/main/java/com/herbcraft/nutrition/NutritionManager.java
core/src/main/java/com/herbcraft/nutrition/NutritionRules.java
core/src/main/java/com/herbcraft/nutrition/NutritionDebugCommands.java
core/src/main/java/com/herbcraft/nutrition/NutritionSyncPayload.java
core/src/main/java/com/herbcraft/nutrition/client/NutritionClientCache.java
core/src/main/java/com/herbcraft/nutrition/client/NutritionPanelScreen.java
core/src/main/java/com/herbcraft/mixin/PlayerMixin.java
```

Resources:

```text
core/src/main/resources/data/herbcraft/nutrition_rules.json
core/src/main/resources/data/herbcraft/nutrition_correction_rules.json
core/src/main/resources/assets/herbcraft/lang/en_us.json
core/src/main/resources/assets/herbcraft/lang/zh_cn.json
```

Validators:

```text
scripts/validate_nutrition_correction_phase.py
scripts/validate_custom_nutrition_effects_phase8.py
```

## Validation run

Build passed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks
```

Validators passed:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_essence_resources.py
python3 scripts/validate_homeostatic_compat.py
python3 scripts/validate_nutrition_cuisine_immunity_isolation.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/verify_built_jars.py
```

## Server smoke

Core server smoke command:

```bash
timeout 70s env JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon :core:runServer --args nogui
```

Result:

```text
exit_code=124  # expected timeout after startup evidence
[main/INFO] (herbcraft) Loaded 7 Herbcraft nutrition effect rules.
[main/INFO] (herbcraft) Loaded Herbcraft nutrition correction matrix.
[main/INFO] (herbcraft) Reloaded Herbcraft external data merge layer
[Server thread/INFO] (Minecraft) Done (0.681s)! For help, type "help"
```

No crash was observed before timeout terminated the idle server.

## Recommended client tests

1. Use `/herbcraft nutrition preset @p correcting_flesh`.
2. Open nutrition panel and confirm status can show `饮食渐趋调和。` / correcting.
3. Wait 10-30 seconds and confirm high flesh value decays faster than normal.
4. Use `/herbcraft nutrition preset @p correcting_oil` and repeat.
5. Use `/herbcraft nutrition preset @p excess_fruit_nonpunitive` and confirm high 津液 does not apply dietary imbalance.
6. Use `/herbcraft nutrition preset @p fruit_low` and confirm 津液不足 still applies.
7. Eat small corrective foods vs high-value Cuisine dishes in survival and compare correction speed.

## Known risks / follow-up

- Recent meal persistence uses compact strings; malformed data is ignored defensively but should be monitored.
- The correction score formula is generic, not a hand-authored pair table. If balance feels too broad or too narrow, later externalize correction rules.
- If correction is too strong, lower `correction_extra_decay_max` or raise `correction_score_threshold`.
- If small foods feel too weak, lower `correction_score_threshold` or increase recent window length.
- Real-client validation is still needed for the new `correcting` panel status and player message timing.
