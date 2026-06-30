# Datapack Runtime Compatibility Verification — 2026-06-17

Status: source-level verification complete; P4 runtime test datapack prepared; built jars refreshed; full validators passed  
Baseline: Herbcraft `1.1.3`

## Goal

The author asked for a final source-level check that items/data added through datapacks can truly integrate with Herbcraft's large system surface:

```text
recognition / knowledge
Codex UI
Jade / tooltip display
nutrition
nature / five flavors / traits
qi
seven-emotion relations
food-nature bridge
special counters
reload-safe data sync
```

## Source-level conclusion

The reload-safe datapack path can integrate ordinary already-edible items into Herbcraft when the datapack supplies the correct data surfaces.

For an ordinary food item, the complete integration path is:

```text
nutrition_profiles + ingredients + herb_natures
```

If all three exist and the item is actually registered/edible at runtime, the item can:

```text
- apply Herbcraft nutrition on consumption;
- enter 食材录 / Ingredient Codex after tasting;
- enter 百草志 / 药食志 after tasting;
- participate in food-nature pharmacology;
- participate in qi, flavor, trait, and seven-emotion discovery logic;
- show knowledge-gated nature in tooltip/Jade after sync;
- be observed by HerbcraftDataAPI / HerbcraftEvents.
```

This does not make a non-food item edible. FOOD/CONSUMABLE component changes remain startup-only through:

```text
data/<namespace>/herbcraft/herbs/*.json
```

## P4 runtime test datapack prepared

Folder:

```text
docs/compat_drafts/p4_runtime_datapack_test/herbcraft_p4_runtime_test_datapack/
```

Zip for direct world datapacks folder:

```text
release_output/herbcraft_test_20260614_review/herbcraft-p4-runtime-test-datapack-20260617.zip
```

Datapack contents:

```text
pack.mcmeta
data/p4_runtime_test/herbcraft/nutrition_profiles/generated.json
data/p4_runtime_test/herbcraft/ingredients/generated.json
data/p4_runtime_test/herbcraft/herb_natures/generated.json
data/p4_runtime_test/herbcraft/special_counters/honey_bottle_counter.json
```

It uses only vanilla items:

```text
minecraft:honey_bottle
minecraft:bread
minecraft:baked_potato
```

Purpose:

- `honey_bottle` tests nutrition + 食材录 + 药食志 + custom external special counter.
- `bread` and `baked_potato` test food-nature bridge and a likely 相须 relation because both share `nourishing + earthy`.
- All items are already edible, so no startup-only component mutation is involved.

## Runtime test plan for the author

1. Put this zip into a test world's `datapacks/` folder:

```text
release_output/herbcraft_test_20260614_review/herbcraft-p4-runtime-test-datapack-20260617.zip
```

2. Enter world and run:

```mcfunction
/datapack list
/reload
/datapack list
```

3. Drink/eat:

```text
minecraft:honey_bottle
minecraft:bread
minecraft:baked_potato
```

4. Expected checks:

```text
- honey_bottle enters 食材录 after tasting;
- honey_bottle enters 药食志 after tasting;
- tooltip/Jade can show food-nature knowledge after sync;
- two honey bottles within 60 seconds trigger the external special counter effect: minecraft:glowing for 100 ticks;
- bread + baked_potato close together can discover 相须 because both share nourishing + earthy;
- /reload does not erase existing player nutrition/knowledge/qi data;
- no non-food item is made edible by the datapack.
```

## Fix made during verification: ingredient reload reset

While checking the full datapack path, one robustness issue was found.

Problem:

```text
IngredientsChapter.applyExternalData(...) cleared only external entries. If an external datapack overrode a built-in vanilla ingredient entry and was later removed/reloaded, the built-in entry could be removed and not restored until restart.
```

Fix:

```text
IngredientsChapter.applyExternalData(...) now resets all item entries back to bundled baseline first, then applies external entries on top.
```

This makes `/reload` behavior consistent with other registries such as nutrition and nature, which already reload bundled data before external merges.

## Fix made during verification: dynamic special counters

The earlier edge-review P1 fix is part of this final verification.

External special counter IDs now persist/count through dynamic player storage:

```text
HerbcraftWindowCounters
```

This is required for the runtime test datapack's custom counter:

```text
p4_runtime_test:honey_bottle_counter
```

## Fix made during verification: optional missing item aggregation

Optional missing ingredient entries now aggregate into a compact info log rather than warning once per missing target-mod item.

This matters for built-in optional Farmer+More data when target mods are absent.

## What source-level verification cannot prove

The following still require runtime/client/server observation:

```text
- exact tooltip/Jade timing after sync in a real client;
- multiplayer knowledge isolation in all UI contexts;
- large-server MSPT/TPS impact beyond the author's two-client sanity run;
- third-party right-click mod conflicts;
- external special counter effect timing in a real world;
- log volume when Farmer's Delight / More Delight are absent.
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

## Final answer

At the code-architecture level, reload-safe datapack-added Herbcraft metadata can now integrate into the relevant systems when the data surfaces are complete.

For ordinary already-edible foods, the required set is:

```text
nutrition_profiles + ingredients + herb_natures
```

For threshold overuse counters, external IDs now work through dynamic counter storage.

For non-edible items, datapack metadata alone is not enough; making them edible still requires startup-only `herbs` resources from a loaded addon/mod.
