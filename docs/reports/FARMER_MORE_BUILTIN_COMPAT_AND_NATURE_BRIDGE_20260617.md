# Farmer's Delight + More Delight Built-in Compat and Food-Nature Pharmacology Bridge — 2026-06-17

Status: implemented in current source; Air-placeholder bug fixed; built jars refreshed; author client retest still recommended after this code/resource change  
Baseline: Herbcraft `1.1.3`

## Why this pass happened

The Round 5 datapack proved that Farmer's Delight Refabricated + More Delight can safely participate in Herbcraft's 食材录 / nutrition flow, while RMF should be excluded.

A remaining caveat was then identified:

```text
External herb_natures data loads/syncs, but by itself does not make ordinary third-party food participate in Herbcraft's pharmacology flow.
```

The author requested the compat to be completed rather than left as a metadata-only datapack, and selected the direction:

```text
bundle the Farmer+More data into Herbcraft Core
add a generic data-driven food-nature pharmacology bridge
```

## Final decision

Farmer's Delight + More Delight compatibility is now integrated into Herbcraft Core as optional bundled data.

After author testing, two completion bugs were fixed:

1. Nature-bearing foods such as cabbage/onion/tomato now get 百草志 `药食志` entries after tasting, so the system that teaches 性味 also records them.
2. Non-edible seed rows (`farmersdelight:cabbage_seeds`, `farmersdelight:tomato_seeds`) were removed instead of being shown as impossible-to-taste 食材录/百草志 entries.

A registry-presence bug was also fixed: unknown optional compat item IDs now use `BuiltInRegistries.ITEM.containsKey(id)` before `getValue(id)`, preventing missing Farmer's Delight items from becoming `minecraft:air` placeholder entries when the target mod is absent. `药食志` is not registered as a permanent empty section; it is created only when valid nature-bearing food entries exist.

It is **not** Java hardcoded by mod ID. The bundled resources use the existing external-data namespace/path mechanism:

```text
core/src/main/resources/data/farmers_moredelight_herbcraft/herbcraft/nutrition_profiles/generated.json
core/src/main/resources/data/farmers_moredelight_herbcraft/herbcraft/ingredients/generated.json
core/src/main/resources/data/farmers_moredelight_herbcraft/herbcraft/herb_natures/generated.json
```

These are loaded through the same `ExternalDataResources` / `HerbcraftExternalDataReloadListener` path as third-party datapacks.

If Farmer's Delight / More Delight are absent:

- Ingredient Codex entries for missing item IDs are skipped by registry lookup.
- Nutrition/nature ID profiles are harmless inert metadata until a matching item exists.
- No hard dependency is introduced.

## Counts

```text
nutrition_profiles: 108
ingredients: 108
herb_natures: 5
knowledge_rumors: 0
RMF: 0
```

Source CSV:

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round5_farmer_moredelight.csv
```

Final Chinese nature table:

```text
docs/compat_drafts/food_mods_26_1_2/ROUND5_FINAL_CHINESE_NATURE_TABLE.md
```

## Food-nature pharmacology bridge

Implemented in:

```text
core/src/main/java/com/herbcraft/pharmacology/PharmacologyResolver.java
core/src/main/java/com/herbcraft/mixin/ItemMixin.java
```

New behavior:

- When a non-Herbcraft-herb food is consumed server-side,
- and it has an explicit `herb_natures` profile,
- and it has a Herbcraft nutrition profile,
- it participates in Herbcraft's nature pharmacology state through a nature-only bridge.

Important boundaries:

- It does **not** use the startup-only `herbs` path for Farmer's Delight foods.
- It does **not** override Farmer's Delight / More Delight `FOOD` or `CONSUMABLE` components.
- It does **not** register target-mod items in Java.
- It does **not** add direct Herbcraft herbal effects to every third-party food.
- It lets explicit data drive qi/flavor/seven-emotion pharmacology for ordinary foods that have both nutrition and nature data.
- It registers matching `药食志` entries in 百草志 so nature-bearing foods are visible where the pharmacology system is taught.

Why not use `data/<namespace>/herbcraft/herbs/*.json` here?

```text
The current startup-only herbs path modifies FOOD/CONSUMABLE default components. That is appropriate for making a non-food item edible, but risky for existing Farmer's Delight foods because it could overwrite the target mod's own food behavior.
```

The bridge avoids that by leaving the target food components alone.

## Generated standalone pack

The generated datapack remains in the repository as reference/example material:

```text
docs/compat_drafts/food_mods_26_1_2/generated_pack/
```

After this pass, users with the refreshed Herbcraft Core jar do not need a separate datapack for Farmer+More compatibility. We should not publish it as an old-jar compatibility route because older jars lack the food-nature bridge. It remains useful as a regression fixture for comparing generated JSON.

## Removed non-edible seed rows

The final Farmer+More data no longer includes:

```text
farmersdelight:cabbage_seeds
farmersdelight:tomato_seeds
```

Reason: these are not edible in the target mod's runtime behavior. Keeping them in 食材录/百草志 would create entries players can never truly taste or master. We chose not to force FOOD/CONSUMABLE components onto them in this pass.

## Coffee bean note

Coffee beans are not included because RMF was removed.

Future design note retained:

```text
If a stable real mod provides coffee beans, treat them as processed cocoa beans and review them separately for lower side-effect risk, longer positive buff, Haste-like direction, or bitter/settling behavior.
```

No coffee-bean mechanic was implemented in this pass.

## Validation

Executed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/check_lang_diff.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```

Results:

```text
Gradle build: PASS
Language parity: PASS
Externalized rules: PASS
External extension spec + bundled Farmer+More compat guard: PASS
Nutrition correction phase: PASS
P1 Codex/advancement validation: PASS
Built jar verification: PASS
```

## Output jars refreshed

```text
release_output/herbcraft_test_20260614_review/herbcraft-1.1.3.jar
release_output/herbcraft_test_20260614_review/herbcraft-alchemy-1.1.3.jar
release_output/herbcraft_test_20260614_review/herbcraft-cuisine-1.1.3.jar
```

Only Core behavior/resources changed; Alchemy/Cuisine jars were rebuilt together to keep the release_output set coherent.

## Remaining recommended client test

Because this pass changes Core jar behavior, one final local client sanity test is recommended:

```text
Herbcraft 1.1.3 Core jar + Farmer's Delight + More Delight, without any standalone compat datapack.
```

Check:

- Farmer/More foods still record 食材录 and nutrition.
- Farmer's Delight nature rows participate in nature pharmacology without overriding target food behavior.
- Cabbage/onion/tomato-style nature foods appear in 百草志 under `药食志` after tasting, while nutrition remains in 食材录.
- `farmersdelight:cabbage_seeds` and `farmersdelight:tomato_seeds` remain absent because they are not edible in the target mod.
- If the reference datapack is installed accidentally alongside 1.1.3 Core, duplicate external data should override/warn but not crash; it is not needed for normal play.
- RMF remains absent.
