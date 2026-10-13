# Extending Herbcraft 1.1.4

This document describes the current supported data-extension surface for Herbcraft Core/Alchemy `1.1.4` and Cuisine `1.1.3` on Minecraft `26.3` (the port is currently in validation).

> P4.1 note: the formal JSON extension contract for the `1.1.4` Core/Alchemy line is now `docs/JSON_EXTENSION_SPEC_1_1_4.md`. This file remains the practical how-to guide; the formal spec is authoritative for supported paths, schema shapes, reload boundaries, merge rules, and client sync expectations.
>
> For a shorter addon-author decision tree, read `docs/ADDON_AUTHOR_QUICKSTART_1_1_3.md`.

Herbcraft is increasingly data-driven, but not every internal ID is meant to be configurable. Stable registry IDs, component IDs, packet IDs, and recipe serializer IDs should be treated as code-level API, not pack-level configuration.

## Golden rules

1. Keep IDs stable once published.
2. Add gameplay data in JSON where a loader exists.
3. Provide matching assets, language keys, and recipes for any newly registered item.
4. Run validators before testing in-game.
5. Do not expect UI constants, packet IDs, component IDs, or serializer IDs to be data-driven.

## Current data-driven systems

Herbcraft now has two distinct extension layers:

1. **Bundled baseline files** inside Herbcraft modules, used by the main mod content.
2. **External extension paths** for addon/datapack compatibility.

Do not confuse these layers. Editing the bundled files is appropriate for maintaining Herbcraft itself. Third-party compatibility should use the external paths described below.

### Core

- `data/herbcraft/herbs.json` — edible raw item definitions, including stable food values and optional probabilistic `food_rolls`.
- `data/herbcraft/herb_natures.json` — temperature, pharmacological flavors, taste flavors, and traits.
- `data/herbcraft/nutrition_profiles.json` — five-dimension nutrition profiles.
- `data/herbcraft/nutrition_rules.json` — nutrition decay/check thresholds.
- `data/herbcraft/nutrition_effects.json` — custom nutrition effect mechanics and timing.
- `data/herbcraft/nutrition_correction_rules.json` — dimension-specific correction relationship weights and self-penalty multiplier.
- `data/herbcraft/pharmacology_rules.json` — five-flavor multipliers, qi regression timing, data-backed temperature values, seven-emotion relation rules/priority, and pharmacology balance constants.
- `data/herbcraft/special_counters.json` — poppy/sugar/slime/etc. overuse counters.
- `data/herbcraft/ingredients.json` — vanilla food entries for the 食材录 knowledge chapter (items and section assignments).
- `data/herbcraft/codex_guides.json` — Core-owned guide entries under 百草经总纲.
- `data/herbcraft/guide_hints.json` — Core-owned once-per-player onboarding hints.
- `data/herbcraft/knowledge_rumors.json` — rumor/claim data for Codex entries.
- `data/herbcraft/codex_loot_injections.json` — core Codex page loot injection rules.

### Alchemy

- `data/herbcraft_alchemy/essences.json` — essence definitions, source items, potion names, and base effects.
- `data/herbcraft_alchemy/potion_bonus_pools.json` — advanced potion bonus pools.
- `data/herbcraft_alchemy/potion_processing_rules.json` — mechanism-layer processing style metadata, first-batch runtime profiles, potion burden weights/decay settings, and conservative burden stage thresholds. Current P4 runtime support applies data-backed effect-profile adjustments for `sustained`, `intense`, and `purified`; current P5-P6 support adds item-stack burden metadata and player burden persistence/decay; current P7 support adds mild data-backed movement consequences at higher burden stages. `murky_edge` and `harmonized` remain deferred for runtime style behavior, though their burden metadata is reserved.
- `data/herbcraft_alchemy/potion_burden_effects.json` — custom Alchemy burden effect definitions for burden stages and future support effects.
- `data/herbcraft_alchemy/distillation_rules.json` — B3 mixed-route distillation orientation rules. Current foundation uses one visible `herbcraft:distilled_essence` container item with a persistent component carrying `source_essence` and `orientation`, avoiding per-source item explosion. Distillation is a brewing-stand process: essence items in bottom slots plus orientation catalysts in the ingredient slot produce distilled essences.
- `data/herbcraft_alchemy/armor_support_rules.json` — armor-side protective/support rules. Current D/D2 foundation maps distilled-essence orientations to armor support components that buffer processed-potion burden when worn. Armor support is a smithing-table process using Alchemy adhesives: blank applies support, amethyst refines to CLEAR, and echo refines to DEEP.
- `data/herbcraft_alchemy/special_potions.json` — T3/special/witch potion definitions and witch throw pool.
- `data/herbcraft_alchemy/coating_rules.json` — base weapon coating rules.
- `data/herbcraft_alchemy/processed_coating_rules.json` — processed/distilled weapon coating style rules. Distilled essences can create processed coatings while base essence coating remains valid.
- `data/herbcraft_alchemy/witch_loot.json` — witch alchemy page/essence/potion drops.
- `data/herbcraft_alchemy/codex_guides.json` — Alchemy-owned guide entry under 百草经总纲.
- `data/herbcraft_alchemy/guide_hints.json` — Alchemy-owned once-per-player onboarding hints.
- `data/herbcraft_alchemy/codex_loot_injections.json` — alchemy Codex page loot injection rules.

### Cuisine

- `data/herbcraft_cuisine/dishes.json` — fixed and medicinal dish definitions.
- `data/herbcraft_cuisine/hodgepodge_rules.json` — hodgepodge balancing rules.
- `data/herbcraft_cuisine/codex_guides.json` — Cuisine-owned guide entry under 百草经总纲.
- `data/herbcraft_cuisine/guide_hints.json` — Cuisine-owned once-per-player onboarding hints.
- `data/herbcraft_cuisine/codex_loot_injections.json` — cuisine Codex page loot injection rules.

## External extension paths

### P2: reloadable/server-data metadata extensions

The following paths are implemented for external datapacks or addon data resources:

```text
data/<namespace>/herbcraft/herb_natures/*.json
data/<namespace>/herbcraft/nutrition_profiles/*.json
data/<namespace>/herbcraft/ingredients/*.json
data/<namespace>/herbcraft/special_counters/*.json
data/<namespace>/herbcraft/knowledge_rumors/*.json
data/<namespace>/herbcraft/codex_loot_injections/*.json
```

These are loaded by the server resource reload layer. They are intended for low-risk metadata and rules: nature, nutrition, 食材录 ingredient entries, overuse counters, Codex rumors, and Codex loot injections. Nutrition profiles, nature profiles, and ingredient entry IDs are synced to clients for tooltip/Codex display.

Example pack:

```text
docs/examples/external_extension_compat_pack/
```

Smoke report:

```text
docs/reports/P2_EXTERNAL_DATAPACK_SMOKE_20260615.md
```

### P3: startup-only external herb food definitions

The following path is implemented for loaded Fabric mod/addon resources:

```text
data/<namespace>/herbcraft/herbs/*.json
```

This path can make an existing registered item become a Herbcraft edible item by adding Herbcraft `FOOD` / `CONSUMABLE` default components at startup. Because this touches default item components, it is **startup-only** and is scanned from loaded Fabric mod containers during Herbcraft initialization.

It is **not** a world datapack `/reload` feature. A world datapack cannot safely make an arbitrary non-food item edible after item default components have already been applied.

Example addon layout:

```text
docs/examples/startup_herb_food_extension_mod/
```

Smoke report:

```text
docs/reports/P3_STARTUP_HERB_FOOD_SMOKE_20260615.md
```

Important P3 notes:

- Herbcraft JSON does not register brand-new Minecraft items. The item must already exist.
- If the external item should be accepted by tag-based systems such as hodgepodge ingredients, the addon should also provide the relevant item tags. P3 does not automatically edit tags.
- If the item should have Herbcraft nature/nutrition/rumor data, provide the P2 files as well.
- Optional `food_rolls` are supported at this startup-only path for probabilistic visible hunger or direct hidden saturation settlement.

## Adding an alchemy essence

Add an entry to:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/essences.json
```

Example:

```json
{
  "id": "lavender",
  "source_items": ["examplemod:lavender"],
  "potion_name": "lavender",
  "effects": [
    { "effect": "minecraft:regeneration", "amplifier": 0, "duration_seconds": 20, "positive": true }
  ]
}
```

The Java registry can register extra essence definitions from JSON, but a polished entry still needs resources:

- `assets/herbcraft/items/essence_lavender.json`
- `assets/herbcraft/models/item/essence_lavender.json`
- `assets/herbcraft/textures/item/essence_lavender.png`
- `data/herbcraft/recipe/essence_lavender.json`
- language keys in `assets/herbcraft/lang/en_us.json` and `zh_cn.json`

Use:

```bash
python3 scripts/generate_essence_resources.py --check
python3 scripts/generate_essence_resources.py --write-missing
```

`--write-missing` creates skeleton files only for missing resources. Existing files are not overwritten unless `--overwrite-generated` is provided.

To check the current built-in essence resource coverage:

```bash
python3 scripts/generate_essence_resources.py --check
```

To check a standalone extension scaffold/resource directory:

```bash
python3 scripts/generate_essence_resources.py --check-extension /path/to/extension
```

To create a standalone example scaffold for one essence, including optional wandering trader trade and Codex rumor skeletons:

```bash
python3 scripts/generate_essence_resources.py \
  --scaffold-extension /tmp/herbcraft_lavender_example \
  --essence lavender \
  --source examplemod:lavender \
  --en-name "Lavender Essence" \
  --zh-name "薰衣草精华"
```

For multiple source items:

```bash
python3 scripts/generate_essence_resources.py \
  --scaffold-extension /tmp/herbcraft_tulip_example \
  --essence tulip_example \
  --sources examplemod:red_tulip,examplemod:white_tulip
```

To generate only the mandatory essence/item/model/texture/recipe/lang skeleton and skip optional trade/Codex skeletons:

```bash
python3 scripts/generate_essence_resources.py \
  --scaffold-extension /tmp/herbcraft_minimal_example \
  --essence mint \
  --source examplemod:mint \
  --no-trade \
  --no-codex
```

Review generated trades, Codex claim text keys, lang, and placeholder texture before release.

See:

```text
docs/examples/essence_extension_example/
```

## Adding nutrition profiles

For Herbcraft's built-in content, edit:

```text
data/herbcraft/nutrition_profiles.json
```

For external compatibility packs/addons, prefer:

```text
data/<namespace>/herbcraft/nutrition_profiles/*.json
```

Supported dimensions are still internally named:

- `grain`
- `flesh`
- `oil`
- `vege`
- `fruit`

The displayed name for `fruit` is now `津液` / `Fluids`, but the internal key remains `fruit` for compatibility.

Built-in file example:

```json
"examplemod:lavender_tea": {
  "fruit": 12,
  "vege": 6
}
```

External file example:

```json
{
  "schema_version": 1,
  "item": "examplemod:lavender_tea",
  "values": {
    "fruit": 12,
    "vege": 6
  }
}
```

## Adding Ingredient Codex entries

For Herbcraft's built-in content, edit:

```text
data/herbcraft/ingredients.json
```

For external compatibility packs/addons, add files under:

```text
data/<namespace>/herbcraft/ingredients/*.json
```

Single external entry example:

```json
{
  "schema_version": 1,
  "item": "farmersdelight:cabbage",
  "section": "fruit"
}
```

Multi-entry example:

```json
{
  "schema_version": 1,
  "entries": [
    { "item": "farmersdelight:cabbage", "section": "fruit" },
    { "item": "farmersdelight:cooked_rice", "section": "grain" }
  ]
}
```

Supported section keys currently remain:

```text
meat, seafood, grain, fruit, soup, special
```

External non-vanilla ingredient entries use namespace-aware Codex IDs internally to avoid path collisions. For example, `farmersdelight:cabbage` becomes an `ingredients/farmersdelight__cabbage` knowledge key. Add matching nutrition profiles under `nutrition_profiles/*.json`; otherwise the entry can exist but will show no notable nutrition.

## Adding raw edible herb data

For Herbcraft's built-in content, add entries to:

```text
data/herbcraft/herbs.json
```

For a loaded Fabric addon/mod that wants to make an existing item Herbcraft-edible at startup, add files under:

```text
data/<namespace>/herbcraft/herbs/*.json
```

You must provide:

- item ID
- stable `nutrition` / `saturation_modifier` / consume timing
- medicinal flag
- stack group flag
- effects/removals/flags as needed

You may also provide optional `food_rolls`:

```json
"food_rolls": {
  "nutrition": { "amount": 1, "chance": 0.35 },
  "saturation": { "amount": 0.3, "chance": 0.45 }
}
```

`food_rolls.nutrition.amount` adds direct visible hunger points and should be an integer. `food_rolls.saturation.amount` adds direct hidden saturation points, not a vanilla modifier. This is useful for raw forage that should sometimes help without becoming stable early food.

If the item should show nature information, also add either built-in or external nature data:

```text
data/herbcraft/herb_natures.json
data/<namespace>/herbcraft/herb_natures/*.json
```

If it should participate in rumors/Codex claims, add either built-in or external rumor data:

```text
data/herbcraft/knowledge_rumors.json
data/<namespace>/herbcraft/knowledge_rumors/*.json
```

If it should participate in tag-based systems, add the appropriate item tags separately. P3 startup herb food JSON does not automatically add item tags.

## Adding cuisine dishes

Add dish definitions to:

```text
data/herbcraft_cuisine/dishes.json
```

Cuisine raw/cooked assets and recipes are not automatically generated yet. Follow existing dish resource patterns.

## Adding Codex loot injections

Use the module-level files:

```text
data/herbcraft/codex_loot_injections.json
data/herbcraft_alchemy/codex_loot_injections.json
data/herbcraft_cuisine/codex_loot_injections.json
```

Each injection should include:

```json
{
  "loot_table": "minecraft:chests/village/village_temple",
  "chance": 0.18,
  "chapter": "alchemy",
  "entry": "",
  "page_kind": "common"
}
```

## P4.2 read-only Java API

For code addons that need to query Herbcraft state, use:

```text
com.herbcraft.api.HerbcraftDataAPI
```

This is a read-only facade. It does not register content and should not be used instead of JSON content files.

Useful queries include:

```java
HerbcraftDataAPI.nature(itemId);
HerbcraftDataAPI.nutrition(itemId);
HerbcraftDataAPI.hasIngredientEntry(itemId);
HerbcraftDataAPI.ingredientEntryId(itemId);
HerbcraftDataAPI.isHerb(item);
HerbcraftDataAPI.herb(item);
HerbcraftDataAPI.hasFoodNatureEntry(itemId);
HerbcraftDataAPI.foodNatureEntryId(itemId);
HerbcraftDataAPI.isRegisteredItem(itemId);
```

Important caveat:

```text
Identifier-based nature/nutrition queries can return loaded metadata for optional compat items even when the target mod is absent. If you need a live item, check isRegisteredItem(...) or query by Item / ItemStack.
```

## P4.4 examples

Current examples:

```text
docs/examples/external_extension_compat_pack/
docs/examples/herbcraft_readonly_event_addon/
docs/examples/startup_herb_food_extension_mod/
```

Use them this way:

- `external_extension_compat_pack/` shows JSON content extension: nature, nutrition, 食材录, rumors, loot injection, and special counter.
- `herbcraft_readonly_event_addon/` shows Java read/event integration with `HerbcraftDataAPI` and `HerbcraftEvents`.
- `startup_herb_food_extension_mod/` shows the startup-only `herbs` path for FOOD/CONSUMABLE component changes.

## P4.3 observational event API

For code addons that need to observe Herbcraft behavior, use:

```text
com.herbcraft.api.HerbcraftEvents
```

Current events:

```java
HerbcraftEvents.HERB_CONSUMED.register((player, item) -> { ... });
HerbcraftEvents.NUTRITION_APPLIED.register((player, stack, itemId, profile) -> { ... });
HerbcraftEvents.INGREDIENT_DISCOVERED.register((player, itemId, entryId) -> { ... });
HerbcraftEvents.FOOD_NATURE_RESOLVED.register((player, stack, itemId, nature, result) -> { ... });
HerbcraftEvents.EXTERNAL_DATA_RELOADED.register((natureCount, nutritionCount, ingredientCount) -> { ... });
```

These events are observational. Do not expect listeners to cancel, replace, or mutate Herbcraft's computed results.

Legacy note:

```text
HerbConsumeEvents still exists but delegates to HerbcraftEvents.HERB_CONSUMED. New addons should use HerbcraftEvents.
```

## Alchemy processing hooks and read-only API

For code that needs to observe or query the current Alchemy processing foundation, use:

```text
com.herbcraft.alchemy.AlchemyProcessingAPI
com.herbcraft.alchemy.AlchemyExpansionEvents
```

Current read-only queries include processing style, potion burden metadata, player burden state, and player burden stage. Current events expose style application, burden application, burden stage changes, and a narrow burden-buffer query intended for future armor-support integration. These hooks do not register new content and should not be used instead of JSON content/rule files.

## What is not public data API yet

The following are not recommended extension points yet:

- network payload IDs
- data component IDs
- recipe serializer IDs
- item registry IDs for built-in items
- UI layout constants
- automatic villager trade generation for new dynamic essences
- automatic Codex prose generation for new dynamic essences

## Validators

Run at minimum:

```bash
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_essence_resources.py
python3 scripts/check_lang_diff.py
```

For full release checks, use the validator list in `CURRENT_BASELINE.md`.
