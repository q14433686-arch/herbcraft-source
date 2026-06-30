# Herbcraft Raw Herb Food-Roll Rebalance Implementation — 2026-06-16

Status: implemented in source; client survival feel test pending  
Baseline: Herbcraft `1.1.1`, planned `1.1.2` polish candidate

## 1. Scope

This turn implements the approved P3.10 direction: common raw Herbcraft edibles should not all restore visible hunger and hidden saturation reliably.

Implemented files:

```text
core/src/main/java/com/herbcraft/registry/HerbEntry.java
core/src/main/java/com/herbcraft/registry/HerbFoodRegistry.java
core/src/main/java/com/herbcraft/logic/EffectResolver.java
core/src/main/resources/data/herbcraft/herbs.json
scripts/validate_raw_herb_food_balance.py
```

Documentation/tracking updated:

```text
docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md
NEXT_TASK.md
CHANGELOG.md
CURRENT_BASELINE.md
```

## 2. Data model

`herbs.json` entries now support optional `food_rolls`:

```json
"food_rolls": {
  "nutrition": { "amount": 1, "chance": 0.35 },
  "saturation": { "amount": 0.3, "chance": 0.45 }
}
```

Meaning:

```text
food_rolls.nutrition.amount   direct visible hunger points; integer; raw forage normally uses +1
food_rolls.nutrition.chance   probability 0.0..1.0
food_rolls.saturation.amount  direct hidden saturation points, not a vanilla saturation_modifier
food_rolls.saturation.chance  probability 0.0..1.0
```

Existing stable fields remain:

```text
nutrition              stable vanilla visible hunger
saturation_modifier    stable vanilla modifier; vanilla converts it as nutrition * modifier * 2
```

For common scenery/forage, stable values are now generally:

```json
"nutrition": 0,
"saturation_modifier": 0.0
```

Then the entry uses small `food_rolls` if it should sometimes help.

## 3. Runtime behavior

`HerbFoodRegistry` parses `food_rolls` from bundled `herbs.json` and from the existing startup-only loaded-mod/addon herb food extension path:

```text
data/<namespace>/herbcraft/herbs/*.json
```

`EffectResolver` settles rolls after vanilla consumption:

- visible hunger roll calls `player.getFoodData().eat(amount, 0.0F)`, adding no extra vanilla saturation;
- hidden saturation roll directly adds/clamps saturation with `getSaturationLevel()`, `getFoodLevel()`, and `setSaturation(...)`.

This uses 26.1 public `FoodData` APIs and does not add a new mixin.

Food rolls are treated as food settlement rather than pharmacology effects. They therefore follow vanilla food settlement: vanilla base values are applied first, then Herbcraft rolls, while medicinal effects/damage/healing still go through existing pharmacology logic.

## 4. Balance classes implemented

## 4.1 Very easy scenery / ambient forage

Stable visible hunger removed. Small visible chance and/or hidden fullness only.

```text
short_grass       S+0.2 @ 30%
tall_grass        S+0.2 @ 30%
wildflowers       H+1 @ 10%, S+0.2 @ 35%
pink_petals       H+1 @ 15%, S+0.2 @ 40%
fern              H+1 @ 15%, S+0.3 @ 45%
large_fern        H+1 @ 15%, S+0.3 @ 45%
```

## 4.2 Common flowers

Common flowers now average below stable low-tier foods.

```text
dandelion         H+1 @ 40%, S+0.2 @ 50%
blue_orchid       H+1 @ 35%, S+0.3 @ 55%; guaranteed minecraft:saturation removed
oxeye_daisy       H+1 @ 40%, S+0.2 @ 50%
cornflower        H+1 @ 35%, S+0.2 @ 45%
tulips            H+1 @ 30%, S+0.2 @ 40%
allium            stable H+1, S+0.2 @ 50%
lilac             H+1 @ 55%, S+0.2 @ 55%
rose_bush         stable H+1, S+0.2 @ 45%
peony             stable H+1, S+0.2 @ 50%
```

## 4.3 Special plants lightly tuned

Special/large/rare plants may keep stable visible hunger, but overly high hidden values were reduced.

```text
poppy             stable H+1, no stable hidden saturation
torchflower       stable H+2, saturation_modifier 0.3
cactus_flower     stable H+1, plus H+1 @ 40%, S+0.2 @ 50%
sunflower         stable H+2, saturation_modifier 0.2, S+0.2 @ 45%
pitcher_plant     stable H+3, saturation_modifier 0.5
spore_blossom     H+1 @ 35%
```

## 4.4 Leaves and saplings

Leaves now mainly give hidden fiber/fullness rather than visible food. Saplings are weak nibble items.

```text
ordinary leaves       visible 0, S+0.3 @ 40%-45%
cherry_leaves         H+1 @ 10%, S+0.4 @ 55%
ordinary saplings     H+1 @ 25%, S+0.2 @ 35%
pale_oak_sapling      H+1 @ 15%, S+0.2 @ 30%
mangrove_propagule    stable H+1, S+0.2 @ 35%
cherry_sapling        H+1 @ 35%, S+0.3 @ 50%
```

Toxic azalea leaves/plants and pale/azalea special leaves remain non-food or medicinal exceptions unless separately tuned later.

## 4.5 Seeds and pods

Seeds are more foodlike than petals but weaker than true crops/bread.

```text
wheat_seeds           H+1 @ 60%, S+0.1 @ 30%
beetroot_seeds        H+1 @ 60%, S+0.1 @ 30%
melon_seeds           H+1 @ 65%, S+0.2 @ 50%
pumpkin_seeds         H+1 @ 65%, S+0.2 @ 50%
torchflower_seeds     H+1 @ 60%, S+0.1 @ 35%
pitcher_pod           stable H+1, saturation_modifier 0.1
```

## 4.6 Aquatic, sugar, fibrous, sticky, foodlike exceptions

```text
kelp                  stable H+1, saturation_modifier 0.05, S+0.2 @ 35%
seagrass              H+1 @ 10%, S+0.2 @ 40%
lily_pad              H+1 @ 10%, S+0.3 @ 40%
sea_pickle            H+1 @ 30%, S+0.2 @ 40%
sugar_cane            stable H+1, saturation_modifier 1.0 after follow-up tuning
sugar                 stable H+1, saturation_modifier 0.05
bamboo                H+1 @ 10%, S+0.2 @ 40%
resin_clump           S+0.2 @ 40%
resin_brick           S+0.2 @ 45%
slime_ball            S+0.3 @ 45%
egg variants          stable H+2, saturation_modifier 0.2
honeycomb             stable H+2, saturation_modifier 0.2
pumpkin               stable H+6, saturation_modifier 0.2
melon                 stable H+8, saturation_modifier 0.2
```

Sugar cane and kelp are intentionally not treated like wildflowers: they can reliably ease hunger, but they have poor lasting saturation.

## 5. Validator

New validator:

```bash
python3 scripts/validate_raw_herb_food_balance.py
```

It checks:

- `food_rolls` schema and chance ranges;
- raw visible-hunger roll amount is integer and at most +1 for forage;
- direct hidden saturation rolls stay small;
- common forage does not keep stable visible hunger;
- exact approved values for the tuned set;
- `minecraft:blue_orchid` no longer has a guaranteed `minecraft:saturation` effect;
- special bulk/processed exceptions remain allowlisted;
- Java parses and settles `food_rolls` through `HerbEntry`, `HerbFoodRegistry`, and `EffectResolver`.

## 6. Player-facing wording

Suggested future guide/tooltip copy, if needed:

```text
这些草木能入口，却不顶饭；偶尔垫一口，更多时候只是识其性。
```

```text
叶草多纤，能压一压空腹，却少有真正饭力。
```

## 7. Validation completed this turn

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_raw_herb_food_balance.py
python3 scripts/validate_essence_resources.py
python3 scripts/validate_homeostatic_compat.py
python3 scripts/validate_nutrition_cuisine_immunity_isolation.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_catalyst_mixin.py
python3 scripts/validate_nature_jade.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```

Result: all passed.

## 8. Post-client seed/sugar-cane tuning addendum

Client feel testing found seed hidden-saturation rolls too weak and sugar cane too thin. A follow-up tuning pass is recorded in:

```text
docs/reports/HERB_FOOD_SEED_SUGARCANE_TUNING_20260616.md
```

Current follow-up values:

```text
wheat_seeds           H+1 @ 60%, S+2.0 @ 30%
beetroot_seeds        H+1 @ 60%, S+2.0 @ 30%
torchflower_seeds     H+1 @ 60%, S+2.0 @ 35%
melon_seeds           H+1 @ 65%, S+3.0 @ 50%
pumpkin_seeds         H+1 @ 65%, S+3.0 @ 50%
pitcher_pod           stable H+1, saturation_modifier 1.0
sugar_cane            stable H+1, saturation_modifier 1.0; Homeostatic amount remains 3
```

## 9. Remaining testing

Client survival feel test is still required:

1. Fresh world, low hunger, try wildflowers/petals/ferns/leaves/seeds in early survival.
2. Confirm they do not replace bread/meat/crops as reliable food.
3. Confirm sugar cane and raw kelp feel like short relief, not durable meals.
4. Confirm blue orchid no longer produces guaranteed visible food through `minecraft:saturation`.
5. Watch leaves at full hunger: hidden saturation rolls should not feel like an exploit with shears.
