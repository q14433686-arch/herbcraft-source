# Herbcraft Raw Herb Hunger/Saturation Rebalance Review — 2026-06-16

Status: source audit / design proposal / copywriting notes  
Baseline: Herbcraft `1.1.1`, planning target `1.1.2` or later patch  
Primary source checked: `core/src/main/resources/data/herbcraft/herbs.json`

## 1. User concern restated

The current Herbcraft raw edible system makes many common natural items edible through vanilla `FOOD` / `CONSUMABLE` components. Some very common items, such as wildflowers, petals, ferns, leaves, saplings, seeds, and other ground clutter, can provide stable visible hunger and/or saturation. This may let early players survive on ambient scenery too easily.

The requested design direction is not “make everything useless.” The goal is more nuanced:

- Very common forage should rarely or weakly restore the visible hunger bar.
- Some fibrous/leafy items may give hidden satiety/fullness without visible calories.
- Some rare, risky, processed, bulky, or intuitively nourishing items may remain stable food.
- Some medicinal items should mainly provide effects/knowledge, not reliable food.
- The rules should be data-driven, not scattered Java hardcoding.

## 2. Terminology

Minecraft has two relevant food values:

| Player term in discussion | Minecraft term | Current Herbcraft JSON field |
|---|---|---|
| “看得见的那条” | visible hunger / food level | `nutrition` |
| “看不见的那条” | saturation | `saturation_modifier` currently, but this is a vanilla modifier, not direct saturation points |

Important nuance:

- `nutrition` is visible food points.
- `saturation_modifier` is a multiplier used by vanilla food calculation, not a direct hidden saturation amount.
- If we want “hidden saturation only” or “probabilistic hunger,” vanilla `FoodProperties` alone is not enough. We need a post-consumption Herbcraft food-roll settlement.

## 3. Current implementation facts

Relevant source:

```text
core/src/main/java/com/herbcraft/registry/HerbEntry.java
core/src/main/java/com/herbcraft/registry/HerbFoodRegistry.java
core/src/main/java/com/herbcraft/logic/EffectResolver.java
core/src/main/java/com/herbcraft/mixin/ItemMixin.java
core/src/main/resources/data/herbcraft/herbs.json
```

Current behavior:

- `HerbFoodRegistry.registerDefaultComponents()` attaches `DataComponents.FOOD` and `DataComponents.CONSUMABLE` to every entry in `herbs.json`.
- `HerbEntry.foodProperties()` uses fixed `nutrition` and fixed `saturation_modifier`.
- Vanilla applies these food values in `finishUsingItem` before Herbcraft's post-consumption `EffectResolver.applyAfterConsume(...)` runs.
- Herbcraft then applies pharmacology, effects, counters, herb stack, knowledge, etc.
- There is currently no data field for probabilistic hunger/saturation restoration.

Therefore, any probabilistic or “hidden saturation only” behavior requires new data fields and a small custom settlement after vanilla consumption.

## 4. Current distribution in `herbs.json`

Current count: 96 edible definitions.

Current visible hunger distribution:

```text
nutrition 0: 20 items
nutrition 1: 49 items
nutrition 2: 17 items
nutrition 3: 3 items
nutrition 4: 1 item
nutrition 6: 3 items
nutrition 8: 2 items
nutrition 9: 1 item
```

Current saturation modifier distribution:

```text
0.0: 18 items
0.1: 13 items
0.2: 27 items
0.3: 21 items
0.4: 9 items
0.6: 4 items
0.8: 1 item
1.2: 1 item
9.6: 1 item
14.4: 1 item
```

Key source of concern:

- 49 items currently provide stable `nutrition = 1`.
- Many are common ambient plants or blocks.
- Some common items provide `nutrition = 2` or more.

## 5. Proposed data model

### 5.1 Keep existing fields as stable vanilla base

Do not break old data immediately. Keep:

```json
"nutrition": 1,
"saturation_modifier": 0.2
```

Meaning:

- stable vanilla visible hunger;
- stable vanilla saturation behavior.

### 5.2 Add optional food roll fields

Add optional field, suggested name:

```json
"food_rolls": {
  "nutrition": {
    "amount": 1,
    "chance": 0.35
  },
  "saturation": {
    "amount": 0.4,
    "chance": 0.5
  }
}
```

Meaning:

- `food_rolls.nutrition.amount`: direct visible hunger points to add after consumption if roll succeeds.
- `food_rolls.nutrition.chance`: chance to add those points.
- `food_rolls.saturation.amount`: direct hidden saturation points to add after consumption if roll succeeds.
- `food_rolls.saturation.chance`: chance to add those saturation points.

Use direct saturation points rather than vanilla saturation modifier, because the goal is explicitly “the hidden bar only.”

### 5.3 Example: common wildflower

```json
{
  "item": "minecraft:wildflowers",
  "nutrition": 0,
  "saturation_modifier": 0.0,
  "food_rolls": {
    "nutrition": { "amount": 1, "chance": 0.25 }
  }
}
```

Interpretation:

- Sometimes you get one visible hunger point.
- Usually it is only a taste / herb effect / knowledge action.
- It is not reliable early food.

### 5.4 Example: leafy/fibrous item

```json
{
  "item": "minecraft:oak_leaves",
  "nutrition": 0,
  "saturation_modifier": 0.0,
  "food_rolls": {
    "saturation": { "amount": 0.4, "chance": 0.5 }
  }
}
```

Interpretation:

- Chewing leaves can feel filling sometimes.
- It does not reliably feed the visible hunger bar.

### 5.5 Example: processed or bulky item remains stable

```json
{
  "item": "minecraft:dried_kelp_block",
  "nutrition": 9,
  "saturation_modifier": 0.3
}
```

Interpretation:

- Processed/bulk food block can remain stable.

## 6. Implementation approach

### 6.1 Data classes

Extend `HerbEntry` with optional rolls:

```java
public FoodRoll nutritionRoll;
public FoodRoll saturationRoll;

public record FoodRoll(int amount, float chance) {}
public record SaturationRoll(float amount, float chance) {}
```

Or one generic record:

```java
public record FoodRoll(float amount, float chance) {}
```

For visible hunger, `amount` should be whole-number in JSON and cast to int.

### 6.2 Loader

Extend `HerbFoodRegistry.loadEntries(...)` to parse:

```json
"food_rolls": {
  "nutrition": { "amount": 1, "chance": 0.35 },
  "saturation": { "amount": 0.4, "chance": 0.5 }
}
```

Validation rules:

- chance must be `0.0..1.0`.
- nutrition amount should be `0..20` integer.
- saturation amount should be `0.0..20.0` float.
- if field missing: no custom roll.

### 6.3 Settlement

Apply rolls in `EffectResolver.applyAfterConsume(...)`, after vanilla consumption.

Need to verify exact 26.1 method names in local mappings before coding. Likely approaches:

- visible hunger: use `player.getFoodData().eat(...)` with zero saturation modifier if available, or direct food level setter if exposed.
- saturation only: use direct saturation setter/getter if exposed.

Important: do not use old or guessed mappings. If unsure, inspect the Loom/Minecraft jar with `javap` or local source. This follows `reference_cache/26_1_porting_notes.md`: use current Mojang official mappings and do not trust old tutorials.

### 6.4 Interaction with vanilla base food

For common items that should not stable-feed:

- set `nutrition = 0`;
- set `saturation_modifier = 0.0`;
- add `food_rolls` as needed.

For items that should stable-feed:

- keep existing `nutrition` / `saturation_modifier`.

For items that should stable visible but no hidden:

- use `nutrition > 0`, `saturation_modifier = 0.0`.

For items that should hidden-only:

- use `nutrition = 0`, `saturation_modifier = 0.0`, and `food_rolls.saturation`.

## 7. Audit categories and recommendations

## 7.1 Category A — ambient ground clutter: should not be reliable food

These are very common, intuitive low-calorie forage. They should not be stable visible hunger.

| Item | Current | Recommended |
|---|---:|---|
| `minecraft:short_grass` | 0 / 0.1 | Keep visible 0. Optional tiny saturation roll only. |
| `minecraft:tall_grass` | 0 / 0.1 | Keep visible 0. Optional tiny saturation roll only. |
| `minecraft:wildflowers` | 1 / 0.2 | Change to visible 0 + 25% chance for +1 visible hunger. No stable saturation. |
| `minecraft:pink_petals` | 1 / 0.2 | Change to visible 0 + 25% chance for +1 visible hunger. No stable saturation. |
| `minecraft:fern` | 2 / 0.2 | Too high. Change to visible 0 + 35% chance for +1 visible, optional 0.2 saturation roll. |
| `minecraft:large_fern` | 2 / 0.2 | Too high. Same as fern. |
| `minecraft:seagrass` | 1 / 0.2 | Change to visible 0 + 30% chance for +1 visible or hidden-only aquatic satiety. |
| `minecraft:lily_pad` | 1 / 0.2 | Change to visible 0 + 25% chance for +1 visible or saturation-only. |

Rationale:

- These are easy to find.
- They should remain useful for knowledge/pharmacology/nutrition profiles, but not as dependable food.

## 7.2 Category B — common small flowers: weak probabilistic visible hunger

Common flowers are plausible to nibble but should not replace food.

| Item | Current | Recommended |
|---|---:|---|
| `minecraft:dandelion` | 1 / 0.3 | visible 0 + 40% chance +1 visible. |
| `minecraft:blue_orchid` | 1 / 0.4 | visible 0 + 40% chance +1 visible. |
| `minecraft:oxeye_daisy` | 1 / 0.3 | visible 0 + 40% chance +1 visible. |
| `minecraft:cornflower` | 1 / 0.3 | visible 0 + 40% chance +1 visible. |
| tulips | 1 / 0.2 | visible 0 + 35% chance +1 visible. |
| `minecraft:allium` | 2 / 0.6 | Too high for a flower. visible 1 stable OR visible 0 + 60% chance +1. Recommend stable 1, sat 0.1 due stronger route value. |
| `minecraft:lilac` | 1 / 0.3 | Keep or probabilistic. Large flower but not foodlike; recommend visible 0 + 50% chance +1. |
| `minecraft:rose_bush` | 2 / 0.3 | Too high. Recommend visible 1 stable, sat 0.1. |
| `minecraft:peony` | 2 / 0.4 | Too high. Recommend visible 1 stable, sat 0.1. |

Rationale:

- One flower occasionally helps, but gathering flowers should not be a reliable breakfast.
- Large flowers can be slightly better but not major food.

## 7.3 Category C — rare/special flowers and medicinal flowers

These may stay stronger if rare, risky, or medicinal.

| Item | Current | Recommended |
|---|---:|---|
| `minecraft:golden_dandelion` | 2 / 1.2 | Keep mostly stable; rare and special. Maybe reduce saturation to 0.6 if too strong. |
| `minecraft:poppy` | 2 / 0.1 | Medicinal/risky. Keep visible 1 or 2? Recommend visible 1, sat 0.0/0.1. |
| `minecraft:azure_bluet` | 0 / 0 | Keep. Medicinal/knowledge, not food. |
| `minecraft:lily_of_the_valley` | 0 / 0 | Keep. Toxic medicinal. |
| `minecraft:wither_rose` | 0 / 0 | Keep. Toxic. |
| `minecraft:torchflower` | 2 / 0.6 | Rare-ish / special. Keep visible 2 or reduce sat to 0.3. |
| `minecraft:closed_eyeblossom` | 0 / 0 | Keep. Medicinal. |
| `minecraft:open_eyeblossom` | 0 / 0 | Keep. Medicinal. |
| `minecraft:cactus_flower` | 2 / 0.6 | Somewhat special. Keep visible 1 or 2; recommend visible 1 + saturation 0.2. |
| `minecraft:sunflower` | 3 / 0.6 | Large plant, more seed/foodlike. Keep visible 2 or 3; recommend 2 / 0.3. |
| `minecraft:pitcher_plant` | 3 / 0.8 | Rare/special. Keep 3 / 0.8 acceptable. |
| `minecraft:spore_blossom` | 1 / 0.1 | Rare but not foodlike. Keep 1 or 0 + special effects. |

## 7.4 Category D — leaves and saplings: mostly hidden satiety/fiber, not visible food

Leaves are extremely abundant with shears. Saplings are common enough that stable food is risky.

| Item group | Current | Recommended |
|---|---:|---|
| ordinary leaves | mostly 1 / 0.1-0.2 | visible 0; saturation roll 0.2-0.4 at 35-50%. |
| `minecraft:cherry_leaves` | 2 / 0.4 | Too high. visible 0 + 50% saturation roll; maybe 25% visible +1 if desired. |
| `minecraft:pale_oak_leaves` | 0 / 0 | Keep. |
| `minecraft:azalea_leaves` | 0 / 0 | Keep. Toxic/medicinal. |
| `minecraft:flowering_azalea_leaves` | 0 / 0 | Keep. Toxic/medicinal. |
| ordinary saplings | 1 / 0.2 | visible 0 + 25% chance +1 or saturation-only. |
| `minecraft:cherry_sapling` | 2 / 0.4 | Too high. visible 0 + 35% chance +1, optional saturation roll. |
| `minecraft:mangrove_propagule` | 1 / 0.2 | visible 0 + 25% chance +1, or keep 1 if desired due fruitlike propagule. |
| `minecraft:azalea` / `flowering_azalea` | 0 / 0 | Keep. |

Rationale:

- “Chewing leaves” should not fill the visible food bar reliably.
- Hidden satiety is intuitive as rough fiber/fullness.

## 7.5 Category E — seeds and small crop parts

Seeds are common but more foodlike than petals/leaves.

| Item | Current | Recommended |
|---|---:|---|
| wheat seeds | 1 / 0.3 | visible 0 + 50% chance +1 or stable 1 with sat 0.0. Recommend probability. |
| beetroot seeds | 1 / 0.3 | same. |
| melon seeds | 1 / 0.3 | same; maybe slight oil saturation roll. |
| pumpkin seeds | 1 / 0.3 | same; maybe hidden saturation/oil roll. |
| torchflower seeds | 1 / 0.3 | same or stable 1 if author wants special seeds. |
| pitcher pod | 1 / 0.3 | rare-ish; stable 1 acceptable. |

Rationale:

- Seeds can be eaten, but should not outcompete bread/crops.

## 7.6 Category F — aquatic plants

| Item | Current | Recommended |
|---|---:|---|
| kelp | 1 / 0.3 | Keep visible 1 but low sat, or probability +1. Since vanilla dried kelp exists, raw kelp should be weaker. Recommend visible 0 + 50% chance +1. |
| dried kelp block | 9 / 0.3 | Keep stable. It is processed bulk food. |
| sea pickle | 1 / 0.3 | Medicinal/odd. visible 0 + 35% chance +1 or keep 1 if its effects justify risk. |

## 7.7 Category G — ice/snow and mineral-ish items

These already mostly do not feed visible hunger.

| Item | Current | Recommended |
|---|---:|---|
| snowball | 0 / 0 | Keep. |
| ice | 0 / 0 | Keep. |
| packed ice | 0 / 0 | Keep. |
| blue ice | 0 / 0 | Keep. |
| powder snow bucket | 0 / 0 | Keep; drink/relief/knowledge not food. |
| glowstone dust | 0 / 0 | Keep. |
| blaze powder | 0 / 0 | Keep. |
| ghast tear | 0 / 0 | Keep. |

## 7.8 Category H — resin, slime, nether materials, alchemy oddities

These should be about risk/effects/material identity, not reliable hunger.

| Item | Current | Recommended |
|---|---:|---|
| resin clump | 1 / 0.1 | visible 0, possible hidden saturation roll 0.2 due sticky fullness. |
| resin brick | 1 / 0.2 | visible 0, possible hidden saturation roll 0.2. |
| slime ball | 1 / 0.1 | visible 0 + hidden saturation roll; risk/effects matter. |
| nether wart | 1 / 0.2 | Keep 1 or probability; not common early. |
| fermented spider eye | 1 / 0.1 | Keep 1 or 0; risky crafted item. |
| magma cream | 1 / 0.2 | Keep 1; rare/risky. |
| rabbit foot | 1 / 0.2 | Keep 1; rare. |
| phantom membrane | 1 / 0.1 | Keep 1 or 0; rare/medicinal. |

## 7.9 Category I — eggs, honey, sugar, crop blocks

These are more foodlike and may stay stable, but some current values are high.

| Item | Current | Recommended |
|---|---:|---|
| egg / blue egg / brown egg | 2 / 0.3 | Keep. Foodlike and not trivially farmed instantly. |
| sugar | 1 / 0.1 | Keep or probability; sugar is calories but risky. |
| sugar cane | 2 / 0.3 | Reduce to 1 stable or 50% +1; very farmable. |
| cocoa beans | 1 / 0.4 | Keep 1; more limited. |
| honeycomb | 3 / 0.4 | Possibly high. Keep due honeyed identity or reduce to 2. |
| honeycomb block | 6 / 0.3 | High but block-sized. Keep or reduce if exploit appears. |
| honey block | 8 / 0.2 | High but block-sized and risky; monitor. |
| pumpkin | 6 / 0.3 | Whole pumpkin is bulky. Stable okay, but could reduce if early pumpkin fields trivialize food. |
| melon | 8 / 0.3 | Whole melon block is bulky/crafted; stable okay. |

## 7.10 Category J — vanilla-already-food / special food exceptions

Do not accidentally nerf items whose vanilla identity is already food or special food.

| Item | Current | Recommended |
|---|---:|---|
| golden carrot | 6 / 14.4 | Keep vanilla values. |
| glistering melon slice | 4 / 9.6 | Keep current fixed special values. |

## 8. Recommended first implementation batch

Do not rebalance all 96 manually in one blind pass. Start with the high-risk common forage classes.

### Batch 1 — obvious common forage nerf

Change these first:

```text
wildflowers
pink_petals
fern
large_fern
ordinary leaves
ordinary saplings
seeds
kelp/seagrass/lily_pad/sea_pickle
resin/slime
```

Target:

- remove stable visible hunger from most of them;
- add probability rolls where appropriate;
- keep Herbcraft nutrition profiles and pharmacology identities unchanged.

### Batch 2 — flower tuning

Tune common flowers:

```text
dandelion
blue_orchid
oxeye_daisy
cornflower
tulips
allium
lilac
rose_bush
peony
```

Target:

- common small flowers should be probability +1, not stable food;
- larger/rarer flowers may keep stable 1;
- special/rare flowers may keep stronger values.

### Batch 3 — block/crop/material review

Review:

```text
honey blocks
pumpkin
melon
dried kelp block
sugar cane
honeycomb
```

Target:

- keep if intuitive and not exploitative;
- reduce if real survival tests show they replace normal food too much.

## 9. Suggested initial class presets

These can become comments in a design doc or validator expectations.

```text
COMMON_FORAGE:
  base nutrition 0
  base saturation 0.0
  25%-40% chance +1 visible hunger
  no hidden saturation

FIBER_FORAGE:
  base nutrition 0
  base saturation 0.0
  0% visible hunger
  35%-50% chance +0.2 to +0.4 hidden saturation

SEED_NIBBLE:
  base nutrition 0
  base saturation 0.0
  50% chance +1 visible hunger
  optional +0.2 hidden saturation for oily seeds

RARE_HERB:
  stable 1-2 visible hunger allowed
  low/moderate saturation

MEDICINAL_ONLY:
  stable visible 0
  stable saturation 0
  effects/knowledge/pharmacology are the reward

BULK_OR_PROCESSED:
  stable food allowed
  examples: dried kelp block, pumpkin, melon, honey block, honeycomb block
```

## 10. Validator recommendations

Add a new validator or extend existing one:

```text
scripts/validate_raw_herb_food_balance.py
```

Checks:

- Very common forage blacklist cannot have stable `nutrition > 0` unless explicitly allowlisted.
- `food_rolls` chance in `0..1`.
- `food_rolls.nutrition.amount` integer and small for common forage.
- `food_rolls.saturation.amount` reasonable.
- special allowlist for vanilla/special foods:
  - `minecraft:golden_carrot`
  - `minecraft:glistering_melon_slice`
  - block foods if approved.

Potential common-forage blacklist:

```text
short_grass
tall_grass
wildflowers
pink_petals
fern
large_fern
ordinary leaves
ordinary saplings
ordinary seeds
```

## 11. User-facing copy notes

If tooltips or guide pages mention this later, use plain wording:

```text
这些草木能入口，却不顶饭；偶尔能垫一口，更多时候只是识其性。
```

English:

```text
These herbs can be eaten, but they are not meals. Sometimes they take the edge off hunger; more often, they teach their nature.
```

For leaves/fiber:

```text
叶草多纤，能稍解口腹空感，却少有真正饱食。
```

English:

```text
Leaves and grasses are fibrous. They may dull emptiness, but rarely feed you in the visible way.
```

## 12. Future Hardcore Nutrition note

The author raised a larger future idea: split `津液` / Fluids into multiple hardcore dimensions such as:

```text
water
vitamins
minerals
fiber
trace elements
```

Recommendation:

- Do not do this in base `1.1.2`.
- Put it in a future Hardcore Nutrition addon or major compatibility module.
- Current base mod should keep `fruit`/津液 as a simple compatibility dimension and avoid making it carry too much punitive meaning.

## 13. Recommended next decision

Before coding, decide these policy points:

1. Should common flowers become probability-visible-hunger, or should some stay stable 1?
2. Should leaves/saplings give hidden saturation at all, or only Herbcraft nutrition/pharmacology?
3. Should sugar cane stay stable 1/2 because it is calorie-like, or become probabilistic due easy farming?
4. Should block foods such as pumpkin/melon/honey blocks remain stable high-value food?
5. Is direct saturation-only settlement acceptable, or should we avoid manipulating hidden saturation because it is harder to explain?

My recommended answers:

1. Common flowers: probability +1, no stable visible hunger.
2. Leaves/saplings: hidden saturation roll only, no stable visible hunger.
3. Sugar cane: reduce to stable 1 or 50% +1.
4. Block foods: keep stable for now; monitor survival exploit.
5. Saturation-only is acceptable if data-driven and described as fullness/fiber, but implementation should verify 26.1 `FoodData` method names before coding.
