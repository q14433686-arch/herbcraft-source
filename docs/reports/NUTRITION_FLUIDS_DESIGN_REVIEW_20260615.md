# Nutrition Fluids / 津液 Design Review

Date: 2026-06-15  
Scope: discussion-only review after real-client P3 addon validation and survival testing feedback.  
Status: **no gameplay code changed in this report**

## Trigger

The author reported that the P3 startup-food addon test passed in a real client:

```text
全线通过；功能全部正常；残页和百草经记载正常；条目进入奇材志。
```

During survival testing, the author also found a design issue:

```text
Many items that restore Homeostatic thirst also add Herbcraft `fruit` / 津液 / Fluids nutrition.
Repeated hydration can push 津液 too high, and because current nutrition imbalance logic treats high 津液 like other high dimensions, it can contribute to dietary imbalance.
```

## Source/code facts checked

### Nutrition dimensions

`NutritionProfile.DIMENSIONS` is still fixed:

```java
{"grain", "flesh", "oil", "vege", "fruit"}
```

The internal key remains `fruit`, but UI language presents it as:

```text
津液 / Fluids
```

### Nutrition intake

`PlayerNutritionState.addFromProfile(...)` adds all positive values directly and clamps only at `max_value`:

```java
for (String dim : NutritionProfile.DIMENSIONS) {
   int val = profile.get(dim);
   if (val > 0) add(dim, val);
}
```

`add(...)` clamps to:

```text
0..NutritionRules.maxValue
```

Current `max_value` is:

```json
1000
```

### Imbalance/excess thresholds

Current `nutrition_rules.json` includes:

```json
"imbalance_high": 850,
"bad_state_grace_checks": 3,
"full_balance_min": 400,
"full_balance_max": 600
```

Current persistent imbalance logic in `NutritionManager.evaluateAndApply(...)` checks all five dimensions, including `fruit`:

```java
if (max - min > 500 && max > rules.imbalanceHigh) {
   state.incrementBadStreak();
   if (!isDietaryImbalanceRelieved(player) && state.shouldApplyDebuff()) {
      addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.DIETARY_IMBALANCE, sustainedTicks, 0));
   }
}
```

Current per-dimension excess handling also includes `fruit`:

```java
case "fruit" -> {
   if (roll(player, 0.10F)) addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.DIETARY_IMBALANCE, ...));
}
```

Therefore the author's observation is source-confirmed:

```text
A very high 津液/fruit value can currently produce both short excess dietary_imbalance rolls and persistent dietary_imbalance if it creates a large enough spread from other dimensions.
```

## Data overlap checked

Many Homeostatic-positive Cuisine foods also have `fruit` nutrition. Examples:

```text
herbcraft_cuisine:water_bowl        Homeostatic amount 6  / fruit 8
herbcraft_cuisine:blue_ice_brew     Homeostatic amount 8  / fruit 50
herbcraft_cuisine:seafarer_stew     Homeostatic amount 8  / fruit 40
herbcraft_cuisine:hundred_flower_feast amount 8 / fruit 120
minecraft:melon                     Homeostatic amount 6  / fruit 77
minecraft:sugar_cane                Homeostatic amount 3  / fruit 18
minecraft:sea_pickle                Homeostatic amount 2  / fruit 5
```

Not all thirst-compatible items add `fruit`; ice/snow examples often hydrate Homeostatic but do not add `fruit` nutrition:

```text
minecraft:ice
minecraft:packed_ice
minecraft:blue_ice
minecraft:snowball
minecraft:powder_snow_bucket
```

But enough common hydrating/drink-like foods overlap with `fruit` that the survival concern is legitimate.

## Candidate options

### Option 1 — Make 津液/Fluids excess non-punitive

Change logic so high `fruit` does not create negative `DIETARY_IMBALANCE` by itself.

Possible code direction:

```text
- Keep fruit deficiency when too low.
- Keep fruit/fluids nutrition values on hydrating foods.
- Keep or tune fruit abundant positive modifier.
- Remove fruit from per-dimension EXCESS dietary_imbalance roll.
- Exclude fruit from persistent max-min dietary_imbalance calculation, or treat it as non-punitive for max-side spread.
```

Gameplay meaning:

```text
Too little 津液 is still a problem.
High 津液 is not directly punished in the main mod.
Very high 津液 may still block 五养调和 because it is outside 400..600, but it does not apply a negative debuff.
```

Pros:

- Matches the main mod's non-hardcore design.
- Avoids punishing players for drinking/eating soup in Homeostatic survival.
- Does not require retuning many JSON profiles immediately.
- Keeps `fruit` / 津液 meaningful as a deficiency dimension.

Cons:

- High `fruit` becomes safer than other dimensions.
- Needs code change in `NutritionManager`, not just data.

### Option 2 — Keep current logic and accept the penalty

Do not change code.

Pros:

- No implementation risk.
- Keeps all five dimensions symmetrical.
- Makes overconsumption/one-sided diet meaningful.

Cons:

- Survival testing already shows this can feel wrong because hydration and nutrition overlap.
- Players may be punished for normal thirst management.
- Homeostatic compatibility makes this more visible.

### Option 3 — Remove or reduce `fruit` from common hydrating foods

Data-only rebalance:

```text
- Keep current imbalance logic.
- Reduce `fruit` values on common drinks/soups/water-adjacent foods.
- Reserve `fruit`/津液 for specific nourishing foods instead of hydration itself.
```

Pros:

- JSON-only for many items.
- Keeps nutrition logic symmetrical.

Cons:

- Many profile edits.
- Does not fully solve repeated consumption if any common food still has `fruit`.
- Weakens the semantic link between 津液/Fluids and hydration.

### Option 4 — Diminishing returns / soft cap for fruit intake

Code change:

```text
When current fruit is high, incoming fruit values are reduced or ignored.
Example: full value below 650, half value 650..849, no value above 850.
```

Pros:

- Keeps high fluids from exploding to 1000.
- Still allows hydrating foods to contribute when low.

Cons:

- More complex and less transparent.
- Needs UI/tooltip explanation.
- Does not address whether high `fruit` should be punitive if it still reaches high values.

## Recommendation

Recommended direction if/when code changes are allowed:

```text
Adopt Option 1 as the main-mod behavior:
High 津液/Fluids should not directly apply negative dietary imbalance in the base mod.
```

Concrete future implementation proposal:

```text
1. In NutritionManager.applyDimensionEffects(...), remove the `fruit` case from EXCESS dietary_imbalance rolls.
2. In persistent dietary imbalance calculation, compute the high-side imbalance over dietary solids only: grain/flesh/oil/vege.
3. Keep fruit deficiency behavior unchanged.
4. Keep fruit profile values for now; do not do a large JSON rebalance in the same turn unless testing shows it is still needed.
5. Update debug preset expectations for `excess_fruit`: it should demonstrate non-punitive high Fluids rather than a debuff state.
```

Rationale:

```text
Homeostatic thirst and Herbcraft 津液 overlap by design, but the base Herbcraft nutrition system is supposed to be advisory/light. Punishing high fluids makes normal thirst management feel like a trap. Hardcore Nutrition can later reintroduce stricter fluid-overload mechanics if desired.
```

## No-change status

This report is analysis only. No source gameplay code or JSON balance values were changed in this turn.
