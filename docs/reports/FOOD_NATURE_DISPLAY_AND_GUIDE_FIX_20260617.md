# Food-Nature Display and Guide Fix — 2026-06-17

Status: implemented; built jars refreshed; author client retest required  
Baseline: Herbcraft `1.1.3`

## Problem reported

After the Farmer+More food-nature bridge was connected, the author confirmed that food-nature items could appear in 药食志 and participate in basic nature behavior, but found several integration gaps:

```text
- Hovering Farmer's Delight tomato did not show Herbcraft nature / knowledge state.
- Dropping tomato on the ground and looking with Jade did not show nature / knowledge state.
- Food-nature items could show in 药食志, but guide pages such as 五味与性味 / 七情与配伍 did not reliably reveal flavor and relation principles from those items.
- Some temperature discoveries such as 寒热相济 appeared, but other flavor/relation discoveries did not.
```

## Root causes

### 1. Tooltip and Jade still only recognized full Herbcraft herbs

The following code paths were still gated by full `HerbFoodRegistry` / `HerbcraftAPI.getDefinition(...)` herb entries:

```text
core/src/main/java/com/herbcraft/knowledge/client/HerbNatureItemTooltips.java
core/src/main/java/com/herbcraft/compat/jade/HerbJadeData.java
```

Food-nature entries such as Farmer's Delight tomato/onion/cabbage are ordinary foods with `nutrition + ingredients + herb_natures`, not startup `herbs` entries. Therefore they were invisible to those display paths.

### 2. Guide gating counted only full Herbcraft herbs

In `HerbalChapter`, guide progress helpers only counted items from `HerbcraftAPI.getHerbRegistry()`:

```text
herbStats(...)
firstTastedHerb(...)
discoveredFlavorCount(...)
```

So a player could taste food-nature entries and even trigger pharmacology, but the guide pages could still think the player had not tasted enough relevant herbal/nature items.

### 3. Food-nature bridge used inert entries, so flavor discoveries with effect-gated conditions did not record

`PharmacologyResolver.resolve(...)` records some flavor principles only when effects/healing/negative effects are present. Food-nature bridge entries intentionally use an inert `HerbEntry` so they do not apply or override target-mod food effects.

That meant flavors such as sweet, pungent, sour, astringent, and bland might affect the computation but not be recorded as discovered principles.

## Fixes implemented

### Tooltip support

Updated:

```text
core/src/main/java/com/herbcraft/knowledge/client/HerbNatureItemTooltips.java
```

Now item tooltip display recognizes:

```text
full Herbcraft herbs
OR ordinary food-nature items with nature + nutrition + 食材录 data
```

Food-nature items use the namespace-aware 药食志 entry ID:

```text
HerbalChapter.foodNatureEntryId(itemId)
```

### Jade support

Updated:

```text
core/src/main/java/com/herbcraft/compat/jade/HerbJadeData.java
```

Now Jade recognizes food-nature items as Herbcraft-displayable items and uses their 药食志 entry ID for knowledge state.

A new server-data flag distinguishes food-nature items from full herbs:

```text
herbcraft:is_food_nature
```

Food-nature Jade lines show nature state and point nutrition back to 食材录 rather than claiming full herb usage.

### Guide support

Updated:

```text
core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java
```

The guide helpers now include food-nature entries when counting tasted nature knowledge:

```text
herbStats(...)
firstTastedHerb(...)
discoveredFlavorCount(...)
```

This lets 五味与性味 and 七情与配伍 guide pages use Farmer+More food-nature entries as valid nature-learning experiences.

### Flavor discovery support

Updated:

```text
core/src/main/java/com/herbcraft/pharmacology/PharmacologyResolver.java
```

The food-nature bridge now explicitly records flavor discoveries for inert food-nature entries where the normal herb-effect path would otherwise skip discovery due to having no direct effects.

Currently recorded through this bridge:

```text
sweet
pungent
sour
astringent
bland
```

Bitter/salty remain handled by the normal resolver path.

### Safer item names

`HerbalChapter.itemName(...)` now checks registry presence before resolving item IDs, avoiding fallback display problems for missing optional items.

## Expected client behavior after fix

With Farmer's Delight + More Delight installed and the refreshed `1.1.3` Core jar:

```text
- Hovering Farmer's Delight tomato/onion/cabbage after relevant knowledge sync should show Herbcraft nature state.
- Dropped tomato/onion/cabbage should display Herbcraft nature state through Jade after server data arrives.
- 药食志 remains the Codex home for nature/taste/body-feeling lines.
- 食材录 remains the Codex home for nutrition.
- 五味与性味 should reveal food-nature flavor principles after tasting relevant food-nature entries.
- 七情与配伍 should reveal relation principles after valid recent food-nature/herb pairings.
```

## Suggested retest cases

```text
1. Eat farmersdelight:cabbage, then hover it in inventory after sync.
2. Drop farmersdelight:tomato and inspect it with Jade.
3. Eat farmersdelight:onion and verify pungent/sweet flavor discovery appears in 五味与性味.
4. Eat farmersdelight:cabbage twice close together and check for 相须 discovery in 七情与配伍.
5. Eat farmersdelight:cabbage then farmersdelight:tomato close together and check for 相使 discovery.
6. Confirm nutrition still appears in 食材录, not as full herb nutrition in 百草志.
7. Confirm target food behavior is not overwritten.
```

## Validation

Executed:

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

## Remaining risk

This was a source-level fix driven by a client bug report. The author should rerun the specific client cases above before public upload.
