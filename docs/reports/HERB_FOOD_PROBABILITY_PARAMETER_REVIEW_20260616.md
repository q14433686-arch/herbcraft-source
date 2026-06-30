# Herbcraft Raw Herb Probability Food Balance Parameters — 2026-06-16

Status: source-query / balance evaluation / parameter proposal; **no code implementation**  
Baseline: Herbcraft `1.1.1`, planned `1.1.2` polish candidate  
Primary source checked this turn:

```text
core/src/main/resources/data/herbcraft/herbs.json
core/src/main/resources/data/herbcraft/nutrition_profiles.json
core/src/main/resources/data/herbcraft/herb_natures.json
core/src/main/java/com/herbcraft/registry/HerbEntry.java
core/src/main/java/com/herbcraft/registry/HerbFoodRegistry.java
core/src/main/java/com/herbcraft/logic/EffectResolver.java
core/src/main/java/com/herbcraft/mixin/ItemMixin.java
reference_cache/26_1_porting_notes.md
```

A machine-readable current-value audit was also exported for review:

```text
docs/reports/raw_herb_food_current_values_20260616.csv
```

## 1. Verdict on the proposed direction

The proposed direction is reasonable and better than a single uniform probability rule.

Different raw Herbcraft edibles should be separated by two axes:

1. **Availability / exploit risk** — how easy the item is to gather repeatedly in early survival.
2. **Food intuition** — whether the item feels caloric, fibrous/filling, medicinal, toxic, processed, or bulky.

Therefore the food model should support independent outcomes:

```text
stable visible hunger            e.g. sugar cane, eggs, bulk foods, some special plants
probability visible hunger       e.g. flowers, petals, seeds, ferns
stable low hidden saturation     only for items that are genuinely foodlike and not trivial
probability hidden saturation    e.g. leaves/fiber/sticky materials
default no food restoration      toxic/medicinal/mineral/ice oddities
```

The user-proposed ranges are directionally sound:

```text
Very easy forage:
  visible hunger chance: 5%-15%
  hidden saturation chance: 25%-40%

Common flowers:
  visible hunger chance: 20%-55%
  hidden saturation chance: 30%-65%

Special caloric plants such as sugar cane / raw kelp:
  stable visible hunger can be allowed,
  but hidden saturation should stay low.
```

The important adjustment is that **the chance alone is not the full balance value**. Amount matters too. For common forage, keep the roll amount small:

```text
visible hunger roll amount: usually +1 only
hidden saturation roll amount: usually +0.2 to +0.4 direct saturation points only
```

This keeps raw scenery edible without letting it become breakfast.

## 2. Current code facts that matter for balance

### 2.1 Current system has only stable food values

Current `HerbEntry.foodProperties()` builds vanilla food components from:

```text
nutrition
saturation_modifier
```

`HerbFoodRegistry.registerDefaultComponents()` attaches `FOOD` and `CONSUMABLE` components during default component modification. `ItemMixin.finishUsingItem` then calls `EffectResolver.applyAfterConsume(...)` after vanilla food settlement.

There is currently no `food_rolls` field and no probabilistic hunger/saturation settlement.

### 2.2 Visible and hidden bars in source terms

| User term | Minecraft/source term | Current bundled JSON field |
|---|---|---|
| 看得见的那条 | food level / visible hunger | `nutrition` |
| 看不见的那条 | saturation / hidden fullness | produced from `saturation_modifier` through vanilla food calculation |

In 26.1, `FoodProperties.Builder.saturationModifier(x)` stores a modifier, and the builder converts it into direct saturation points with:

```text
direct_saturation = nutrition * saturation_modifier * 2
```

Then vanilla clamps saturation to the current visible food level.

Consequences:

- `nutrition = 0` plus `saturation_modifier > 0` gives **no stable hidden saturation**, because `0 * modifier * 2 = 0`.
- Hidden-only restoration cannot be represented by the current JSON fields alone.
- A future `food_rolls.saturation.amount` should mean **direct saturation points**, not the vanilla modifier.

### 2.3 26.1 API check for a future implementation

Using the local Loom merged jar, `FoodData` exposes:

```text
getFoodLevel()
setFoodLevel(int)
getSaturationLevel()
setSaturation(float)
```

So a future implementation can avoid extra mixins by clamping and setting food/saturation through public methods, but this report does not implement that.

### 2.4 Do not forget `minecraft:saturation` effects

`herbs.json` currently contains direct `minecraft:saturation` effects on:

```text
minecraft:blue_orchid       chance 1.0, duration_ticks 10
minecraft:dried_kelp_block  chance 1.0, duration_seconds 2
```

In 26.1, `SaturationMobEffect.applyEffectTick(...)` calls:

```text
player.getFoodData().eat(amplifier + 1, 1.0F)
```

That restores visible food through the effect, not merely hidden fullness.

Balance implication:

- If `blue_orchid` remains a common flower, its guaranteed saturation effect must be reduced, removed, or converted into the future hidden-only `food_rolls.saturation`; otherwise nerfing its base `nutrition` would not solve the food-exploit problem.
- `dried_kelp_block` is processed bulk food, so it can remain stronger, but its saturation effect should be consciously reviewed as a processed-food exception, not forgotten.

## 3. Current high-risk groups from source

Current `herbs.json` has 96 edible definitions. Distribution:

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

The risk is not that one flower gives `nutrition = 1`; the risk is that many very common items do so **reliably**.

High-risk source examples:

```text
wildflowers        1 / 0.2, stack_group true, 3 positive effects, nutrition_profile vege 7
pink_petals        1 / 0.2, stack_group true
fern               2 / 0.2, vege 14
large_fern         2 / 0.2, vege 14
ordinary leaves    mostly 1 / 0.1-0.2, many medicinal + stack_group true
cherry_leaves      2 / 0.4
ordinary saplings  1 / 0.2
cherry_sapling     2 / 0.4
seeds              1 / 0.3
kelp               1 / 0.3
sugar_cane         2 / 0.3
resin/slime        1 / 0.1-0.2
```

These should not all keep stable visible food.

## 4. Recommended probability model

Future data shape remains:

```json
"food_rolls": {
  "nutrition": { "amount": 1, "chance": 0.35 },
  "saturation": { "amount": 0.3, "chance": 0.45 }
}
```

Suggested interpretation:

```text
nutrition.amount: visible food points, integer, usually 1 for raw forage
nutrition.chance: probability for visible bar restoration
saturation.amount: direct hidden saturation points, float, usually 0.2-0.4 for raw forage
saturation.chance: probability for hidden fullness restoration
```

Stable base fields still mean:

```text
nutrition: stable visible food points applied by vanilla
saturation_modifier: stable vanilla saturation modifier, converted to nutrition * modifier * 2 direct saturation
```

For common forage, set stable values to:

```text
nutrition = 0
saturation_modifier = 0.0
```

Then add only the intended rolls.

## 5. Class presets

These are recommended tuning classes, not hardcoded Java categories.

| Class | Use case | Stable visible | Stable hidden | Visible roll | Hidden roll | Notes |
|---|---|---:|---:|---:|---:|---|
| `SCENERY_NIBBLE` | petals, wildflower carpet, grass-like clutter | 0 | 0 | +1 @ 5%-15% | +0.2 @ 25%-40% | Mostly knowledge/taste; barely food. |
| `FIBER_FORAGE` | leaves, ferns, bamboo-like rough fiber | 0 | 0 | +1 @ 0%-15% | +0.3/+0.4 @ 35%-55% | Hidden fullness, not real visible food. |
| `COMMON_FLOWER` | dandelion, oxeye, cornflower, tulips | 0 | 0 | +1 @ 20%-45% | +0.2/+0.3 @ 30%-55% | Better than grass, worse than food. |
| `STRONG_FLOWER` | allium, rose bush, peony, sunflower edge cases | 0 or 1 | very low | +1 @ 45%-60% if no stable food | +0.2/+0.3 @ 35%-55% | Large/special flowers may keep stable 1. |
| `SEED_NIBBLE` | common seeds | 0 | 0 | +1 @ 50%-65% | +0.2 @ 25%-50% for oily seeds | More foodlike but competes with planting. |
| `AQUATIC_RAW` | kelp, seagrass, lily pad, sea pickle | 0 or 1 | very low | +1 @ 5%-50% | +0.2/+0.3 @ 25%-45% | Raw kelp may be stable 1; seagrass should not. |
| `QUICK_SUGAR` | sugar, sugar cane | 1 | 0 or tiny | optional +1 @ 0%-25% | +0.1/+0.2 @ 0%-25% | Short hunger relief, poor lasting fullness. |
| `STICKY_FULLNESS` | resin clump/brick, slime ball | 0 | 0 | none | +0.2/+0.3 @ 30%-45% | Feels full/sticky, not nourishing. |
| `MEDICINAL_ONLY` | toxic/ice/mineral/odd medicine | 0 | 0 | none | none | Effects/knowledge are the reward. |
| `BULK_PROCESSED` | dried kelp block, pumpkin, melon, honey blocks | stable allowed | low/moderate | usually none | usually none | Bulk/crafted food may be reliable. |

## 6. Per-item parameter recommendations

Format:

```text
current nutrition / saturation_modifier -> recommended stable base + rolls
```

`S+0.3 @ 45%` means direct hidden saturation amount, not vanilla modifier.

### 6.1 Extremely easy scenery / ambient forage

| Item | Current | Recommended | Reason |
|---|---:|---|---|
| `minecraft:short_grass` | 0 / 0.1 | 0 / 0.0; S+0.2 @ 25%-30%; no visible roll | Current modifier does not actually add hidden saturation because nutrition is 0. Keep as fiber taste only. |
| `minecraft:tall_grass` | 0 / 0.1 | 0 / 0.0; S+0.2 @ 25%-30%; no visible roll | Same as short grass. |
| `minecraft:wildflowers` | 1 / 0.2 | 0 / 0.0; H+1 @ 8%-12%; S+0.2 @ 30%-35% | Very easy scenery item; should not be stable food despite pleasant effects. |
| `minecraft:pink_petals` | 1 / 0.2 | 0 / 0.0; H+1 @ 10%-15%; S+0.2 @ 30%-40% | Slightly more plausible than wildflower carpet, still not a meal. |
| `minecraft:fern` | 2 / 0.2 | 0 / 0.0; H+1 @ 10%-15%; S+0.3 @ 40%-45% | Current stable 2 is too strong for common rough forage. |
| `minecraft:large_fern` | 2 / 0.2 | 0 / 0.0; H+1 @ 10%-15%; S+0.3 @ 40%-45% | Same as fern; size should improve fiber fullness, not visible calories. |

### 6.2 Common flowers

| Item | Current | Recommended | Reason |
|---|---:|---|---|
| `minecraft:dandelion` | 1 / 0.3 | 0 / 0.0; H+1 @ 35%-45%; S+0.2 @ 40%-50% | Common but edible-ish; should average below stable food. |
| `minecraft:blue_orchid` | 1 / 0.4 + guaranteed saturation effect | 0 / 0.0; H+1 @ 30%-40%; S+0.3 @ 45%-55%; reduce/replace `minecraft:saturation` effect | Its current guaranteed saturation effect is the major issue. |
| `minecraft:oxeye_daisy` | 1 / 0.3 | 0 / 0.0; H+1 @ 35%-45%; S+0.2 @ 40%-50% | Similar to dandelion, with medicinal route value. |
| `minecraft:cornflower` | 1 / 0.3 | 0 / 0.0; H+1 @ 30%-40%; S+0.2 @ 35%-45% | Keep weaker than direct food. |
| `minecraft:red_tulip` | 1 / 0.2 | 0 / 0.0; H+1 @ 25%-35%; S+0.2 @ 30%-40% | Weakness risk already discourages spamming; still should not be stable. |
| `minecraft:orange_tulip` | 1 / 0.2 | 0 / 0.0; H+1 @ 25%-35%; S+0.2 @ 30%-40% | Same as tulip group. |
| `minecraft:pink_tulip` | 1 / 0.2 | 0 / 0.0; H+1 @ 25%-35%; S+0.2 @ 30%-40% | Same as tulip group. |
| `minecraft:white_tulip` | 1 / 0.2 | 0 / 0.0; H+1 @ 25%-35%; S+0.2 @ 30%-40% | Same as tulip group. |
| `minecraft:allium` | 2 / 0.6 | either 1 / 0.0 + S+0.2 @ 40%-50%, or 0 / 0.0 + H+1 @ 55%-60% + S+0.2 @ 45% | Current stable 2 and high modifier are too strong for a flower. Stable 1 is acceptable if it is meant to feel like a stronger herb. |
| `minecraft:lilac` | 1 / 0.3 | 0 / 0.0; H+1 @ 45%-55%; S+0.2 @ 45%-55% | Large flower, less ubiquitous than petals. |
| `minecraft:rose_bush` | 2 / 0.3 | 1 / 0.0; S+0.2 @ 35%-45%; keep thorn damage | Large plant can feed a little, but 2 stable is high. |
| `minecraft:peony` | 2 / 0.4 | 1 / 0.0; S+0.2 @ 40%-50% | Large/soft flower; stable 1 is enough. |

### 6.3 Special flowers / rare plants / medicinal plants

| Item | Current | Recommended | Reason |
|---|---:|---|---|
| `minecraft:golden_dandelion` | 2 / 1.2 | keep 2 / 0.6-1.2 | Rare/special; stable food is acceptable. |
| `minecraft:poppy` | 2 / 0.1 | 1 / 0.0-0.1, or H+1 @ 45%-55% | Common and risky; 2 stable is too generous even with nausea/slowness. |
| `minecraft:azure_bluet` | 0 / 0.0 | keep 0 / 0.0 | Medicinal/knowledge only. |
| `minecraft:lily_of_the_valley` | 0 / 0.0 | keep 0 / 0.0 | Toxic; no food value. |
| `minecraft:wither_rose` | 0 / 0.0 | keep 0 / 0.0 | Toxic; no food value. |
| `minecraft:torchflower` | 2 / 0.6 | keep 2 / 0.2-0.3, or 1 / 0.0 + H+1 @ 50% | Special/rarer; stable visible can remain, hidden should be lower. |
| `minecraft:closed_eyeblossom` | 0 / 0.0 | keep 0 / 0.0 | Medicinal/odd. |
| `minecraft:open_eyeblossom` | 0 / 0.0 | keep 0 / 0.0 | Medicinal/odd. |
| `minecraft:cactus_flower` | 2 / 0.6 | 1 / 0.0; H+1 @ 35%-45%; S+0.2 @ 40%-50% | More special than petals, but not a full meal. |
| `minecraft:sunflower` | 3 / 0.6 | 2 / 0.1-0.2; optional S+0.2 @ 35%-45% | Large/oily/seedlike identity supports stable visible food, but 3 + high sat is too much. |
| `minecraft:pitcher_plant` | 3 / 0.8 | keep 2-3 / 0.3-0.5 | Rare/special; can remain a stronger raw plant. |
| `minecraft:spore_blossom` | 1 / 0.1 | 0 / 0.0 + H+1 @ 30%-40%, or keep 1 / 0.0 | Rare but not foodlike; either is acceptable. |

### 6.4 Leaves and saplings

| Item group | Current | Recommended | Reason |
|---|---:|---|---|
| ordinary leaves | mostly 1 / 0.1-0.2 | 0 / 0.0; no visible roll; S+0.3 @ 35%-50% | Abundant with shears; fiber/fullness only. |
| `minecraft:cherry_leaves` | 2 / 0.4 | 0 / 0.0; H+1 @ 5%-10%; S+0.4 @ 45%-55% | Current stable 2 is too strong; cherry flavor can justify a tiny visible chance. |
| `minecraft:pale_oak_leaves` | 0 / 0.0 | keep 0 / 0.0; optional S+0.2 @ 20%-30% | Withered/lunar identity; not food. |
| `minecraft:azalea_leaves` | 0 / 0.0 | keep 0 / 0.0 | Toxic. |
| `minecraft:flowering_azalea_leaves` | 0 / 0.0 | keep 0 / 0.0 | Toxic. |
| ordinary saplings | 1 / 0.2 | 0 / 0.0; H+1 @ 15%-25%; S+0.2 @ 25%-35% | Nibbleable but not reliable. |
| `minecraft:pale_oak_sapling` | 1 / 0.2 | 0 / 0.0; H+1 @ 10%-15%; S+0.2 @ 20%-30% | Stranger/less foodlike than ordinary saplings. |
| `minecraft:mangrove_propagule` | 1 / 0.2 | either keep 1 / 0.0, or 0 / 0.0 + H+1 @ 35%-45% + S+0.2 @ 35% | More propagule/fruitlike; stable 1 is defensible. |
| `minecraft:cherry_sapling` | 2 / 0.4 | 0 / 0.0; H+1 @ 25%-35%; S+0.3 @ 40%-50% | Current stable 2 is too high. |
| `minecraft:azalea` / `flowering_azalea` | 0 / 0.0 | keep 0 / 0.0 | Toxic/medicinal, not food. |

### 6.5 Seeds and pods

| Item | Current | Recommended | Reason |
|---|---:|---|---|
| `minecraft:wheat_seeds` | 1 / 0.3 | 0 / 0.0; H+1 @ 55%-60%; S+0.1 @ 20%-30% | Foodlike but small; eating competes with planting. |
| `minecraft:beetroot_seeds` | 1 / 0.3 | 0 / 0.0; H+1 @ 55%-60%; S+0.1 @ 20%-30% | Same. |
| `minecraft:melon_seeds` | 1 / 0.3 | 0 / 0.0; H+1 @ 60%-65%; S+0.2 @ 40%-50% | Oily seed; slightly better hidden fullness. |
| `minecraft:pumpkin_seeds` | 1 / 0.3 | 0 / 0.0; H+1 @ 60%-65%; S+0.2 @ 45%-50% | Oily seed; slightly better hidden fullness. |
| `minecraft:torchflower_seeds` | 1 / 0.3 | 0 / 0.0; H+1 @ 55%-60%; S+0.1 @ 25%-35% | Special, but still a seed. |
| `minecraft:pitcher_pod` | 1 / 0.3 | keep 1 / 0.0-0.1, or H+1 @ 70% | Rarer pod; stable 1 is acceptable. |

### 6.6 Aquatic plants

| Item | Current | Recommended | Reason |
|---|---:|---|---|
| `minecraft:kelp` | 1 / 0.3 | keep 1 / 0.0-0.05; optional S+0.2 @ 25%-35% | Raw kelp may reliably ease hunger, but should not provide lasting fullness. |
| `minecraft:seagrass` | 1 / 0.2 | 0 / 0.0; H+1 @ 5%-10%; S+0.2 @ 30%-40% | Too common/grasslike for stable food. |
| `minecraft:lily_pad` | 1 / 0.2 | 0 / 0.0; H+1 @ 5%-10%; S+0.3 @ 30%-40% | More fiber/water plant than food. |
| `minecraft:sea_pickle` | 1 / 0.3 | 0 / 0.0; H+1 @ 20%-30%; S+0.2 @ 30%-40%, or keep 1 / 0.0 if treated as special | Medicinal/aquatic oddity; do not give stable hidden fullness. |
| `minecraft:dried_kelp_block` | 9 / 0.3 + saturation effect | keep stable bulk food, but review saturation effect | Processed block can be reliable; effect may overfeed. |

### 6.7 Sugar, fibrous crops, mushrooms

| Item | Current | Recommended | Reason |
|---|---:|---|---|
| `minecraft:sugar_cane` | 2 / 0.3 | 1 / 0.0-0.05; optional H+1 @ 15%-25%; hidden none or S+0.1 @ 20% | Caloric/juicy enough for stable short relief, but farmable and not filling. |
| `minecraft:sugar` | 1 / 0.1 | keep 1 / 0.0-0.05 | Quick calories, poor saturation. |
| `minecraft:bamboo` | 1 / 0.2 | 0 / 0.0; H+1 @ 5%-10%; S+0.2 @ 30%-40% | More rough fiber than food. |
| `minecraft:cocoa_beans` | 1 / 0.4 | keep 1 / 0.1-0.2, or 0 / 0.0 + H+1 @ 60% + S+0.2 @ 45% | More limited/foodlike; stable 1 is acceptable, but current hidden value is high. |
| `minecraft:brown_mushroom` | 1 / 0.4 | keep 1 / 0.1-0.2 | Mushroom is foodlike but raw; saturation should be modest. |

### 6.8 Sticky/material oddities

| Item | Current | Recommended | Reason |
|---|---:|---|---|
| `minecraft:resin_clump` | 1 / 0.1 | 0 / 0.0; S+0.2 @ 30%-40%; no visible roll | Sticky fullness, not food. |
| `minecraft:resin_brick` | 1 / 0.2 | 0 / 0.0; S+0.2 @ 35%-45%; no visible roll | More processed but still not food. |
| `minecraft:slime_ball` | 1 / 0.1 | 0 / 0.0; S+0.3 @ 35%-45%; no visible roll | Nauseating/sticky, not nourishment. |
| `minecraft:nether_wart` | 1 / 0.2 | keep 1 / 0.0-0.1, or H+1 @ 45%-55% | Not early-common; risk/effects already matter. |
| `minecraft:fermented_spider_eye` | 1 / 0.1 | keep 1 / 0.0, or 0 / 0.0 + H+1 @ 50% | Crafted/risky, not early exploit. |
| `minecraft:magma_cream` | 1 / 0.2 | keep 1 / 0.0-0.1 | Rare/risky. |
| `minecraft:rabbit_foot` | 1 / 0.2 | keep 1 / 0.0-0.1 | Rare animal drop. |
| `minecraft:phantom_membrane` | 1 / 0.1 | 0 or 1 / 0.0 | Rare/medicinal; either is fine. |

### 6.9 Eggs, honey, crop blocks, special foods

| Item | Current | Recommended | Reason |
|---|---:|---|---|
| `minecraft:egg`, `minecraft:blue_egg`, `minecraft:brown_egg` | 2 / 0.3 | keep 2 / 0.1-0.2 | Foodlike; stable is fine. |
| `minecraft:honeycomb` | 3 / 0.4 | 2 / 0.1-0.2, or keep if honey route should be strong | Current value may be high for a single comb. |
| `minecraft:honeycomb_block` | 6 / 0.3 | keep stable; monitor | Block-sized/crafted. |
| `minecraft:honey_block` | 8 / 0.2 | keep stable or reduce to 6; monitor | Block-sized/crafted but farmable later. |
| `minecraft:pumpkin` | 6 / 0.3 | keep stable 4-6 / 0.1-0.2 | Bulky crop; can remain reliable. |
| `minecraft:melon` | 8 / 0.3 | keep stable 6-8 / 0.1-0.2 | Whole melon block is bulky. |
| `minecraft:golden_carrot` | 6 / 14.4 in current JSON | do not touch in common-forage pass; separately decide whether to normalize to vanilla modifier 1.2 | Current JSON value is not vanilla modifier; it behaves as an extreme special exception. |
| `minecraft:glistering_melon_slice` | 4 / 9.6 in current JSON | do not touch in common-forage pass; separately review | Same special-value issue as golden carrot. |

## 7. Why stable sugar cane / kelp can be balanced

Stable visible food is not automatically bad. It becomes bad when it also gives strong lasting saturation or is too trivial to mass-gather.

Sugar cane and raw kelp can reasonably be:

```text
stable visible hunger: yes, usually +1
hidden saturation: no or very low
```

This means:

- they can stop immediate hunger loss;
- they do not keep sprinting/regeneration sustained for long;
- they feel like chewing cane/kelp for quick relief, not eating a meal;
- farming them still has opportunity cost and time, unlike casually sweeping petals/leaves.

Recommended wording:

```text
甘蔗、海带能稍解饥意，却不耐久；入口有味，腹中无实。
```

English:

```text
Sugar cane and kelp can take the edge off hunger, but they do not last. There is taste in the mouth, little substance in the belly.
```

## 8. Why hidden-only leaves are safe

Hidden saturation does not raise the visible hunger bar. Since saturation is clamped to the current food level, leaves/fiber cannot rescue a starving player into sprinting range.

That makes hidden-only foliage a good middle ground:

```text
It rewards tasting and gives a small feeling of fullness,
but the player still needs real food to restore the visible bar.
```

Recommended wording:

```text
叶草多纤，能压一压空腹，却少有真正饭力。
```

English:

```text
Leaves and grasses are fibrous. They may press down emptiness, but they rarely have the strength of a meal.
```

## 9. Batch recommendation

If implemented later, do not touch all 96 entries at once. Use three batches.

### Batch 1 — exploit risk removal

```text
wildflowers
pink_petals
fern
large_fern
ordinary leaves
cherry_leaves
ordinary saplings
cherry_sapling
seagrass
lily_pad
bamboo
resin_clump
resin_brick
slime_ball
blue_orchid saturation effect review
```

Goal: remove stable visible food from scenery/fiber/material items.

### Batch 2 — common flower and seed identity

```text
dandelion
oxeye_daisy
cornflower
tulips
allium
lilac
rose_bush
peony
wheat/beetroot/melon/pumpkin/torchflower seeds
pitcher pod
```

Goal: flowers become probabilistic; seeds become semi-reliable but still weaker than actual crops/bread.

### Batch 3 — special plants, bulk foods, and exceptions

```text
sugar_cane
kelp
sea_pickle
sunflower
cactus_flower
poppy
honeycomb/honey blocks
pumpkin/melon
dried_kelp_block saturation effect
golden_carrot/glistering_melon_slice saturation_modifier anomaly
```

Goal: stable food exceptions remain intentional, not accidental.

## 10. Acceptance criteria for later implementation

A future code/data implementation should be considered balanced only if:

1. Wildflower/petal/grass/leaf spam cannot be a dependable early food source.
2. Common flowers average below stable vanilla low-tier food.
3. Leaves and sticky materials can give fullness but do not visibly feed the player.
4. Sugar cane and raw kelp give short relief but low lasting saturation.
5. `minecraft:saturation` effects are audited so they do not bypass the new food-roll balance.
6. The model is data-driven in `herbs.json` and validated by a script, not hardcoded per item in Java.
7. The Codex/tooltip copy says the principle plainly without revealing exact hidden numbers too early.

Short player-facing copy:

```text
这些草木能入口，却不顶饭；偶尔垫一口，更多时候只是识其性。
```

English:

```text
These herbs can be eaten, but they are not meals. Sometimes they take the edge off hunger; more often, they teach their nature.
```
