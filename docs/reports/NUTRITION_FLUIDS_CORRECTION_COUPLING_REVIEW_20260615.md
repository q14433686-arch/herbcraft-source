# Nutrition Fluids Correction Coupling Review

Date: 2026-06-15  
Scope: discussion/source review after author reported that widespread `fruit` / 津液 values may over-suppress dietary imbalance.  
Status: **implemented after review; report documents diagnosis and applied fix**

## Author concern

The author observed that many Core/Cuisine foods have some `fruit` / 津液 / Fluids value. Since the current correction system allows `fruit` to contribute to correction scores for other high dimensions, the system may treat many ordinary meals as "corrective" and suppress `DIETARY_IMBALANCE` too easily.

In other words:

```text
A high dimension should be able to cause dietary imbalance.
But if almost every non-meat food has some 津液, and 津液 can correct other high dimensions, then dietary imbalance may become too easy to suppress.
```

## Source facts checked

### Current correction-score logic

Current file:

```text
core/src/main/java/com/herbcraft/nutrition/PlayerNutritionState.java
```

Current scoring logic after the stacking fix:

```java
for (String dim : NutritionProfile.DIMENSIONS) {
   if (!dim.equals(highDim)) {
      corrective += meal.values().getOrDefault(dim, 0) * rules.correctionWeight(highDim, dim);
   }
}

if (corrective > meal.values().getOrDefault(highDim, 0) * rules.selfPenaltyMultiplier) {
   score += corrective;
}
```

This means:

```text
If the matrix gives fruit a positive correction weight, any recent meal with enough fruit can contribute to correcting a high punitive dimension.
```

### Current matrix

Current file:

```text
core/src/main/resources/data/herbcraft/nutrition_correction_rules.json
```

Current fruit weights:

```json
"grain": { "fruit": 0.35 }
"flesh": { "fruit": 0.8 }
"oil":   { "fruit": 0.8 }
"vege":  { "fruit": 0.4 }
```

So `fruit` currently helps correct all non-fruit excesses, especially `flesh` and `oil`.

### Current correction gate

Current `NutritionManager.evaluateAndApply(...)` suppresses sustained imbalance if correction is active:

```java
if (state.hasPunitiveImbalance(rules)) {
   if (correcting) {
      state.clearBadStreak();
      player.removeEffect(HerbcraftMobEffects.DIETARY_IMBALANCE);
      ...
   } else {
      state.incrementBadStreak();
      ... add DIETARY_IMBALANCE after grace
   }
}
```

Therefore the author's concern is valid:

```text
If fruit-heavy recent meals make correction active too easily, dietary_imbalance is suppressed too easily.
```

## Data prevalence checked

`nutrition_profiles.json` currently has:

```text
162 total nutrition profiles
63 profiles with fruit > 0
34 Herbcraft Cuisine profiles
29 Cuisine profiles with fruit > 0
34 minecraft/Core herb/vanilla profiles with fruit > 0
```

Fruit value distribution:

```text
fruit >= 10: 45 profiles
fruit >= 20: 39 profiles
fruit >= 30: 31 profiles
fruit >= 40: 23 profiles
fruit >= 50: 13 profiles
fruit >= 60: 8 profiles
fruit >= 80: 5 profiles
fruit >= 100: 1 profile
```

Cuisine examples:

```text
herbcraft_cuisine:dandelion_salad       fruit 30, vege 80
herbcraft_cuisine:blue_ice_brew         fruit 50, vege 30
herbcraft_cuisine:honey_leaf_soup       fruit 70, oil 40, vege 40
herbcraft_cuisine:cherry_nectar_porridge fruit 80, grain 50, vege 30
herbcraft_cuisine:honeycomb_soothing_stew fruit 80, oil 50, vege 30
herbcraft_cuisine:hundred_flower_feast  fruit 120, vege 140, oil 60, grain 50
herbcraft_cuisine:hodgepodge            fruit 30, vege 40, grain 20, oil 20, flesh 20
```

Conclusion:

```text
Fruit/津液 is common enough that using it as a normal matrix corrector can make correction too broad.
```

## Diagnosis

The current implementation joins two concepts that should be separated:

### Concept A: 津液 as a nutrition/storage dimension

```text
low fruit/津液 should cause deficiency;
high fruit/津液 should not be punished in base mod;
high fruit/津液 may provide a special normalizing helper.
```

### Concept B: fruit as a normal matrix corrector

```text
fruit contributes to correctionScore for high grain/flesh/oil/vege.
```

The bug/design issue is that Concept B makes fruit part of the ordinary correction loop, while the author now wants fruit to be special and separate:

```text
津液 should not participate in the ordinary correction loop.
津液 should only run its special surplus helper: if high, slowly reduce other excesses toward 700.
```

## Options

### Option 1: Remove fruit from the ordinary correction matrix

Set all fruit weights in `nutrition_correction_rules.json` to `0.0`:

```json
"grain": { "fruit": 0.0 }
"flesh": { "fruit": 0.0 }
"oil":   { "fruit": 0.0 }
"vege":  { "fruit": 0.0 }
```

Keep high-fruit surplus helper:

```text
if fruit >= 850, normalize other non-fruit excesses toward 700 by fluid_surplus_decay_amount.
```

Pros:

```text
Cleanly separates 津液 from ordinary correction loop.
Prevents common fruit-containing foods from suppressing dietary_imbalance too broadly.
Keeps the author's special 津液 idea intact.
```

Cons:

```text
High flesh/oil cannot be corrected by normal fruit values unless fruit is actually high enough for surplus helper.
Some previous matrix examples involving fruit become obsolete.
```

### Option 2: Keep fruit matrix weights but require fruit >= 850 to count

Fruit contributes to matrix correction only if current fruit is high.

Pros:

```text
Fruit still helps as a special high-fluid state.
Less drastic than removing fruit from matrix entirely.
```

Cons:

```text
Duplicates the high-fruit surplus helper concept.
More confusing: fruit both matrix-corrects and surplus-corrects.
```

### Option 3: Keep small fruit matrix weights only

Reduce all fruit weights to very low values, e.g. `0.05` or `0.10`.

Pros:

```text
Keeps fruit as a tiny helper.
Less disruptive.
```

Cons:

```text
Still allows widespread fruit to participate in the correction loop.
Does not fully solve conceptual coupling.
```

## Recommendation

Recommended next fix:

```text
Use Option 1. Remove fruit/津液 from the ordinary correction matrix entirely.
Keep fruit/津液 only as:
  - deficiency dimension when low;
  - non-punitive storage when high;
  - special high-fluid surplus helper when fruit >= 850.
```

Proposed matrix after decoupling:

```json
{
  "grain": { "flesh": 1.15, "oil": 0.45, "vege": 1.35, "fruit": 0.0 },
  "flesh": { "grain": 0.0, "oil": 0.0, "vege": 1.6, "fruit": 0.0 },
  "oil":   { "grain": 0.25, "flesh": 0.0, "vege": 1.45, "fruit": 0.0 },
  "vege":  { "grain": 1.2, "flesh": 1.2, "oil": 0.5, "fruit": 0.0 },
  "fruit": {}
}
```

This means:

```text
- High grain: corrected by vege/flesh, weakly by oil.
- High flesh: corrected by vege only.
- High oil: corrected by vege, weakly by grain.
- High vege: corrected by grain/flesh, weakly by oil.
- High fruit: no punitive target.
- High fruit surplus: separate helper slowly pulls other high dimensions toward 700.
```

## Expected behavior after recommended fix

### If flesh = 1000, vege = 500, fruit = 500

```text
Eating fruit-heavy food alone should not suppress flesh imbalance unless it also contains matrix-valid vege.
```

### If flesh = 1000, vege = 500, fruit = 900

```text
High fruit surplus helper can slowly pull flesh toward 700 by its special rule.
This is separate from ordinary matrix correction.
```

### If flesh = 1000, vege = 850, fruit = 995

```text
If recent meals include sufficient vege and pass anti-self guard, matrix correction can apply.
High fruit surplus helper can also apply.
But fruit itself is not counted as ordinary matrix score.
```

### If many foods contain trace fruit

```text
Trace fruit no longer accidentally suppresses dietary imbalance.
```

## Implemented fix

The follow-up implementation changed:

```text
core/src/main/resources/data/herbcraft/nutrition_correction_rules.json
scripts/validate_nutrition_correction_phase.py
docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md
CURRENT_BASELINE.md
```

All ordinary correction-matrix `fruit` weights are now `0.0`:

```json
{
  "grain": { "flesh": 1.15, "oil": 0.45, "vege": 1.35, "fruit": 0.0 },
  "flesh": { "grain": 0.0, "oil": 0.0, "vege": 1.6, "fruit": 0.0 },
  "oil":   { "grain": 0.25, "flesh": 0.0, "vege": 1.45, "fruit": 0.0 },
  "vege":  { "grain": 1.2, "flesh": 1.2, "oil": 0.5, "fruit": 0.0 },
  "fruit": {}
}
```

Current policy after fix:

```text
- 津液/Fluids no longer participates in the ordinary correction matrix.
- Low 津液 still causes deficiency.
- High 津液 remains non-punitive.
- High 津液 still runs the special surplus helper that slowly pulls other excess dimensions toward 700.
- Ordinary fruit-bearing meals no longer suppress DIETARY_IMBALANCE merely because they contain fruit.
```

`validate_nutrition_correction_phase.py` now expects these zero fruit matrix weights.

Validation after implementation:

```text
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon build --rerun-tasks
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

Core server smoke reached `Done (0.485s)` before intentional timeout and logged:

```text
Loaded Herbcraft nutrition correction matrix.
```

