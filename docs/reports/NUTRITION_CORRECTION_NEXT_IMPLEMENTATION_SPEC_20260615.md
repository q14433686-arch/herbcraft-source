# Nutrition Correction Next-Implementation Specification

Date: 2026-06-15  
Scope: specification/numeric plan for the next implementation turn.  
Status: **no gameplay code changed in this turn**

## Author decision recorded

The author agrees with the previous analysis:

```text
- Use the B + E hybrid direction.
- Add a correction/recovery mechanism instead of relying only on slow raw-value decay.
- Make high 津液 / Fluids non-punitive or least-punitive in the base mod.
- Long term, tracking the last dozen to few dozen meals is the most elegant model.
- Corrective food should be weighted by amount: low-content foods require several meals, medium-content foods require fewer, high-content/hard-to-make dishes can correct quickly.
```

## Source facts from this review

### Current storage and persistence

Current nutrition state is stored in:

```text
core/src/main/java/com/herbcraft/nutrition/PlayerNutritionState.java
```

It currently contains only:

```text
values: Map<String, Integer>
lastDecayTick
lastCheckTick
lastWarningTick
badStateStreak
```

Persistence currently happens in:

```text
core/src/main/java/com/herbcraft/mixin/PlayerMixin.java
```

Current saved nutrition data is only the five raw values under:

```text
HerbcraftNutrition/<dimension>
```

Therefore a recent-meal system needs new state and persistence.

### Current consumption hook

All foods are already routed through:

```text
core/src/main/java/com/herbcraft/mixin/ItemMixin.java
```

which calls:

```java
NutritionManager.onFoodConsumed(serverPlayer, stack);
```

This is the correct hook for recording recent meals because it catches Herbcraft, Cuisine, vanilla, and other foods with nutrition profiles.

`HerbConsumeEvents` only covers Herbcraft herb consumption after `EffectResolver`, so it is not enough for recent meals.

### Current client sync

Current sync payload:

```text
core/src/main/java/com/herbcraft/nutrition/NutritionSyncPayload.java
```

sends:

```text
Map<String, Integer> values
int statusOrdinal
```

Current client status codes:

```text
0 starved
1 malnourished
2 imbalanced
3 normal
4 abundant
5 balanced
```

If correction status should be visible in the panel, add a new code such as:

```text
6 correcting / 饮食回正中
```

and update:

```text
NutritionClientCache
NutritionPanelScreen status coloring
lang en_us/zh_cn
```

## Nutrition value tiers from current data

Source checked:

```text
core/src/main/resources/data/herbcraft/nutrition_profiles.json
```

Representative values:

| Item | Profile |
|---|---|
| `minecraft:carrot` | `vege 29 / fruit 7` |
| `minecraft:bread` | `grain 60` |
| `minecraft:cooked_beef` | `flesh 69 / oil 27` |
| `minecraft:melon` | `fruit 77 / vege 19` |
| `herbcraft_cuisine:dandelion_salad` | `vege 80 / fruit 30` |
| `herbcraft_cuisine:pumpkin_porridge` | `grain 100 / vege 40 / fruit 20` |
| `herbcraft_cuisine:crimson_war_hotpot` | `flesh 120 / oil 40 / vege 20` |
| `herbcraft_cuisine:golden_dandelion_stew` | `vege 120 / fruit 80 / oil 30` |
| `herbcraft_cuisine:hundred_flower_feast` | `vege 140 / fruit 120 / oil 60 / grain 50` |
| `herbcraft_cuisine:deadly_hodgepodge` | `vege 20 / fruit 10` |
| `herbcraft_cuisine:hodgepodge` | `vege 40 / fruit 30 / grain 20 / oil 20 / flesh 20` |

Top current Cuisine values by dimension include:

```text
grain: pumpkin_porridge 100, omen_calming_porridge 80
flesh: crimson_war_hotpot 120, twin_fungus_hotpot 100
oil: resin_guard_stew 60, hundred_flower_feast 60, twin_fungus_hotpot/honeycomb_soothing_stew 50
vege: hundred_flower_feast 140, golden_dandelion_stew 120, many dishes 80-100
fruit: hundred_flower_feast 120, honeyed/cherry/honeycomb/golden dishes 80
```

This supports a correction model where:

```text
small raw herbs / simple foods = low score, need multiple meals
ordinary foods / medium dishes = medium score, need one or two
curated or hidden Cuisine dishes = high score, often one enough
```

## Recommended design: Recent meals + corrective-score window

### Core idea

Do not immediately replace the five stored nutrition values. Keep them as slow-moving body stores.

Add a recent-meal window and use it to decide whether the player's **current diet structure** is correcting the old imbalance.

```text
stored values = body stores / long-term consequence
recent meals = current dietary direction
correction = recent meals are structurally correcting high dimensions
```

## New state to add

### PlayerNutritionState additions

Add a recent meal ring buffer:

```java
private final Deque<RecentMeal> recentMeals = new ArrayDeque<>();
```

Suggested record:

```java
public record RecentMeal(long gameTime, String itemId, Map<String, Integer> values) {}
```

Suggested constants / rules-backed values:

```text
recent_meal_limit = 24
recent_meal_window_ticks = 7200      # 6 minutes; enough for several meals, not old history
correction_recent_window_ticks = 3600 # 3 minutes; active current correction window
```

Why 24?

```text
- Fits the author's "十几餐甚至几十餐" preference.
- Small enough to serialize easily.
- Large enough for repeated small foods to matter.
```

### Persistence

Add persistence in `PlayerMixin`.

To avoid depending on complex list APIs, first implementation can encode recent meals as a compact string, similar to existing knowledge/effect-immunity strings:

```text
HerbcraftNutritionRecentMeals = gameTime,itemId,grain,flesh,oil,vege,fruit;...
```

This is not beautiful, but it matches current project style and is safe for a 1.1.x pass.

A later cleanup can replace it with structured `ValueOutput` children if desired.

## New rules to add to nutrition_rules.json

Recommended fields:

```json
{
  "recent_meal_limit": 24,
  "recent_meal_window_ticks": 7200,
  "correction_recent_window_ticks": 3600,
  "correction_high_threshold": 850,
  "correction_target_value": 650,
  "correction_score_threshold": 60,
  "correction_extra_decay_interval_ticks": 200,
  "correction_extra_decay_min": 4,
  "correction_extra_decay_max": 12,
  "fruit_high_non_punitive": true
}
```

### Numeric rationale

| Field | Suggested | Reason |
|---|---:|---|
| `recent_meal_limit` | `24` | Tracks dozens-ish without heavy state. |
| `recent_meal_window_ticks` | `7200` | 6 minutes of broad recent diet. |
| `correction_recent_window_ticks` | `3600` | 3 minutes for active correction. |
| `correction_high_threshold` | `850` | Matches current excess threshold. |
| `correction_target_value` | `650` | Current normal/abundant boundary; not too strict. |
| `correction_score_threshold` | `60` | Small foods need multiples; medium/high dishes can trigger correction. |
| `correction_extra_decay_interval_ticks` | `200` | Every 10 seconds. |
| `correction_extra_decay_min` | `4` | Minimum meaningful correction. |
| `correction_extra_decay_max` | `12` | Prevents feast correction from deleting values instantly. |
| `fruit_high_non_punitive` | `true` | Base mod should not punish hydration/Fluids excess. |

## Corrective score formula

For each excessive dimension `H`, compute a recent corrective score from meals in the correction window.

### Step 1: identify excessive dimensions

```text
High if value >= correction_high_threshold.
Do not treat fruit/津液 as a high punitive dimension if fruit_high_non_punitive = true.
```

### Step 2: identify corrective dimensions

For a high dimension `H`, a meal's corrective contribution is the sum of dimensions that are:

```text
- not H;
- currently below correction_target_value or below the high dimension by a large spread;
- present in the meal profile.
```

Implemented first-pass rule:

```text
corrective(H, meal) = sum(profile[d] for d != H and currentValue[d] < correction_target_value)
highContribution(H, meal) = profile[H]
score(H, meal) = corrective(H, meal) only if corrective(H, meal) > highContribution(H, meal), otherwise 0
```

This prevents a meal dominated by the already-excessive dimension from correcting itself merely because it has trace secondary nutrition. For high `fruit`, if fruit high is non-punitive, do not create a correction target for it.

### Step 3: sum recent meals

```text
recentScore(H) = sum score(H, meal) for meals in last correction_recent_window_ticks
```

Correction is active for `H` if:

```text
recentScore(H) >= correction_score_threshold
```

## Example scoring

Assume high flesh:

| Corrective food | Relevant profile | Approx score | Expected result |
|---|---|---:|---|
| `minecraft:carrot` | `vege 29 + fruit 7` | `36` | Not enough alone; 2 carrots or another small food helps. |
| `minecraft:bread` | `grain 60` | `60` | One bread can start mild correction if grain is low. |
| `herbcraft_cuisine:dandelion_salad` | `vege 80 + fruit 30` | `110` | One should start correction. |
| `herbcraft_cuisine:pumpkin_porridge` | `grain 100 + vege 40 + fruit 20` | `160` | Strong correction. |
| `herbcraft_cuisine:hundred_flower_feast` | `vege 140 + fruit 120 + oil 60 + grain 50` | high/capped | One should strongly correct most non-oil/flesh high states. |

Assume high oil:

| Corrective food | Relevant profile | Approx score | Expected result |
|---|---|---:|---|
| `minecraft:carrot` | `vege 29 + fruit 7` | `36` | Multiple needed. |
| `herbcraft_cuisine:resin_guard_stew` | `vege 60 + grain 30` plus oil itself ignored | `90` | Can correct if vege/grain are low, despite also containing oil. |
| `herbcraft_cuisine:honeycomb_soothing_stew` | `fruit 80 + vege 30` plus oil itself ignored | `110` | Good correction if fruit is not already excessive. |

## Accelerated decay formula

When correction is active for a high dimension `H`, apply extra decay only to that high dimension.

Suggested formula every `correction_extra_decay_interval_ticks`:

```text
extraDecay(H) = clamp(correction_extra_decay_min, correction_extra_decay_max, correction_extra_decay_min + recentScore(H) / 40)
```

Examples:

| recentScore | extraDecay every 10s |
|---:|---:|
| 60 | 5 |
| 110 | 6 |
| 160 | 8 |
| 300+ | 11-12 cap |

Stop extra decay when:

```text
value(H) <= correction_target_value
```

This means a dimension at 900 can fall toward 650 over minutes, not hours, but large dishes still do not instantly erase the history.

## Dietary imbalance debuff policy

### During correction

If any high punitive dimension is currently being corrected:

```text
- suppress sustained DIETARY_IMBALANCE reapplication;
- optionally show a positive/neutral message: nutrition.herbcraft.state.correcting;
- keep raw values visible in the panel.
```

### Fruit/津液 high value

Base-mod policy:

```text
- fruit high does not trigger EXCESS dietary_imbalance roll;
- fruit high does not count as the high punitive max side for persistent imbalance;
- fruit low still causes fruit_deficiency;
- fruit outside 400..600 can still block well_nourished.
```

This preserves Fluids as a deficiency dimension while preventing Homeostatic hydration from becoming a trap.

## Recent-diet status for client/server sync

Add a new client status code:

```text
6 = correcting / 饮食回正中
```

Required files:

```text
core/src/main/java/com/herbcraft/nutrition/NutritionSyncPayload.java
core/src/main/java/com/herbcraft/nutrition/client/NutritionClientCache.java
core/src/main/java/com/herbcraft/nutrition/client/NutritionPanelScreen.java
core/src/main/resources/assets/herbcraft/lang/en_us.json
core/src/main/resources/assets/herbcraft/lang/zh_cn.json
```

Suggested language:

```json
"nutrition.herbcraft.state.correcting": "Diet is correcting itself."
"nutrition.herbcraft.state.correcting": "饮食渐趋调和。"
```

Status ordering proposal:

```text
starved > malnourished > correcting > imbalanced > balanced > abundant > normal
```

Rationale:

```text
If the player is critically low, deficiency should remain visible.
If the player is correcting an imbalance, show that instead of treating them as actively punished.
```

## Files to modify next implementation turn

### Required code

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

### Required resources

```text
core/src/main/resources/data/herbcraft/nutrition_rules.json
core/src/main/resources/assets/herbcraft/lang/en_us.json
core/src/main/resources/assets/herbcraft/lang/zh_cn.json
```

### Validators/reports

Add or update:

```text
scripts/validate_nutrition_correction_phase.py
scripts/validate_custom_nutrition_effects_phase8.py
```

Add a test report after implementation:

```text
docs/reports/NUTRITION_CORRECTION_IMPLEMENTATION_REPORT_YYYYMMDD.md
```

## Debug command additions

Add presets or commands to test correction:

```text
/herbcraft nutrition preset <targets> correcting_flesh
/herbcraft nutrition preset <targets> correcting_oil
/herbcraft nutrition preset <targets> correcting_grain
/herbcraft nutrition preset <targets> excess_fruit_nonpunitive
```

Optional diagnostics:

```text
/herbcraft nutrition meals get [target]
/herbcraft nutrition correction get [target]
```

If adding commands is too much for the first pass, at least add presets and log/report behavior.

## Acceptance tests for next implementation

### Server/debug tests

1. Set `flesh=900`, others around `400`.
2. Confirm dietary imbalance can appear without correction.
3. Eat low-score corrective foods; one carrot should not fully correct, multiple should.
4. Eat `dandelion_salad`; correction should start quickly.
5. During correction, `flesh` should decay faster than ordinary -2/minute.
6. Once `flesh <= 650`, correction should stop.
7. Set `fruit=900`, others around `400`; high fruit should not apply negative dietary imbalance in base mod.
8. Set `fruit=0`; fruit deficiency should still apply.

### Client tests

1. Nutrition panel shows raw values.
2. Correcting status appears when correction is active.
3. No stale imbalanced status remains while correction is active.
4. Existing deficiency and well_nourished behavior remains intact.

## Risks

- Recent meal persistence can be error-prone if encoded strings are malformed.
- Client status code changes require language and cache updates.
- If correction is too strong, imbalance becomes meaningless.
- If correction score threshold is too high, small foods feel useless.
- If fruit is completely non-punitive, high Fluids becomes safer than other dimensions; this is intentional for base mod but should be documented.

## Recommendation for next turn

Implement the following minimum viable version:

```text
1. Recent meal ring buffer: 24 meals, 6-minute retention.
2. Correction score from recent meals in the last 3 minutes.
3. Correction threshold: 60 raw nutrition points.
4. Extra decay every 10 seconds, 4..12 based on score.
5. Correction target: 650.
6. High fruit/津液 non-punitive in base mod.
7. Add client status `correcting`.
8. Add validators and a focused report.
```

Do not implement P4 Java API in the same turn unless this nutrition correction pass is already stable.
