# Herbcraft External Extension Specification Draft

Status: **DRAFT / partially implemented; P4.1 JSON spec formalized**
Target baseline: Herbcraft Core/Alchemy `1.1.4`, Cuisine `1.1.3`, Minecraft `26.3` (port in validation), Fabric Loader `0.19.3+`, Java `25+
Purpose: define a future-safe path for third-party mod/data-pack compatibility without turning Herbcraft into an unstable pile of hardcoded cross-mod patches.

> P4.1 formal JSON contract: `docs/JSON_EXTENSION_SPEC_1_1_3.md`. This draft remains the roadmap/history document; the JSON spec is now the authoritative schema reference for public data extension paths.

> This file is a planning/specification draft plus implementation tracker. As of 2026-06-17, the first low-risk Core external data merge layer is implemented for selected reload-safe paths, server-authoritative nutrition/nature/ingredient metadata syncs to clients, startup-only loaded-mod/addon herb edible extension is implemented, optional `food_rolls` are implemented for bundled/startup herb food data, and P4 read-only/event Java APIs are implemented. Java write/registration APIs remain deferred.

## 1. Why this exists

Herbcraft is moving from "Java tables hidden in source" toward a layered integration model:

1. **Java owns mechanics**: registration, events, mixins, data components, sync packets, UI, validation, and compatibility hooks.
2. **JSON owns content**: herb food values, natures, nutrition values, overuse counters, Codex rumors, loot injections, Homeostatic data, and balance tables.
3. **Addons/compat packs own optional integration**: compatibility for other mods should be mostly data, with Java only where the target mod exposes no data/API surface.

This matters because a good ecosystem-friendly mod should allow other content to say:

- this item has Herbcraft nature/flavor/traits;
- this food contributes to Herbcraft nutrition;
- this plant/food has a Codex rumor;
- this item has an overuse counter;
- this drink/food interacts with Homeostatic thirst/temperature;
- this already-registered item can become a Herbcraft edible under controlled startup rules.

But it must avoid unsafe promises such as:

- JSON magically registers brand-new Minecraft items;
- arbitrary packs can hot-reload item default food components safely at any time;
- UI/network/component/registry IDs are editable balance data;
- two conflicting compat packs can define the same item with no deterministic rule.

## 2. Current `1.1.4` Core/Alchemy reality

In the current Herbcraft Core/Alchemy `1.1.4` line, many systems are already data-backed internally, but most of them are loaded as **bundled module resources**, not as a fully public datapack merge API.

Current internal files include:

```text
core/src/main/resources/data/herbcraft/herbs.json
core/src/main/resources/data/herbcraft/herb_natures.json
core/src/main/resources/data/herbcraft/nutrition_profiles.json
core/src/main/resources/data/herbcraft/nutrition_rules.json
core/src/main/resources/data/herbcraft/nutrition_effects.json
core/src/main/resources/data/herbcraft/special_counters.json
core/src/main/resources/data/herbcraft/knowledge_rumors.json
core/src/main/resources/data/herbcraft/codex_loot_injections.json
core/src/main/resources/data/herbcraft/codex_guides.json

core/src/main/resources/data/herbcraft/ingredients.json

cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json
cuisine/src/main/resources/data/herbcraft_cuisine/hodgepodge_rules.json
cuisine/src/main/resources/data/herbcraft_cuisine/codex_loot_injections.json
cuisine/src/main/resources/data/herbcraft_cuisine/codex_guides.json

alchemy/src/main/resources/data/herbcraft_alchemy/essences.json
alchemy/src/main/resources/data/herbcraft_alchemy/potion_bonus_pools.json
alchemy/src/main/resources/data/herbcraft_alchemy/special_potions.json
alchemy/src/main/resources/data/herbcraft_alchemy/coating_rules.json
alchemy/src/main/resources/data/herbcraft_alchemy/witch_loot.json
alchemy/src/main/resources/data/herbcraft_alchemy/codex_loot_injections.json
alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json
```

Current implementation status:

```text
Implemented in this source state:
- data/<namespace>/herbcraft/herb_natures/*.json
- data/<namespace>/herbcraft/nutrition_profiles/*.json
- data/<namespace>/herbcraft/ingredients/*.json
- data/<namespace>/herbcraft/special_counters/*.json
- data/<namespace>/herbcraft/knowledge_rumors/*.json
- data/<namespace>/herbcraft/codex_loot_injections/*.json
- data/<namespace>/herbcraft/herbs/*.json startup-only loaded-mod/addon edible extension

Not implemented yet:
- world datapack `/reload` support for herb edible component changes
- stable public Java extension events/API
- full external Codex chapter/entry creation
- Homeostatic temperature gameplay effects
```

Important limitations:

```text
The P2 merge layer reloads server data and keeps the server authoritative. In current `1.1.3` source, nutrition profiles, herb nature profiles, and external ingredient entry IDs are synced to clients for tooltip/Codex-facing display. Full external Codex chapter/entry creation is still not implemented and should not be advertised as a stable API.

The P3 herb edible extension is startup-only and scans loaded Fabric mod/addon resources. It does not make world datapack /reload capable of adding or changing FOOD/CONSUMABLE default components.
```

Therefore this draft remains the roadmap for the next extension phases instead of pretending all future paths are already a complete public API.

## 3. Terminology

| Term | Meaning |
|---|---|
| Herbcraft core data | Built-in JSON shipped inside Herbcraft modules. |
| Extension data | Future data loaded from other namespaces and merged into Herbcraft systems. |
| Compat pack | A data pack or lightweight addon jar that adds Herbcraft metadata for another mod's existing items. |
| Code addon | A Fabric mod that depends on Herbcraft and may call Java APIs or register its own items. |
| Existing item | An item already registered by Minecraft, Herbcraft, or another mod. |
| New item | An item that does not exist until a code addon registers it. Herbcraft JSON alone must not create it. |
| Reload-safe data | Data that can be reloaded with `/reload` without changing item registration/default components. |
| Startup-only data | Data that must be collected before item default components or registries are effectively frozen. |

## 4. Design principles

1. **Do not break `1.1.3` content.** Built-in data remains the baseline.
2. **Do not use same-path override as the extension mechanism.** External packs should not be told to replace `data/herbcraft/herbs.json`.
3. **Prefer additive per-file resources.** Use `data/<namespace>/herbcraft/<system>/*.json` so multiple packs can coexist.
4. **Separate reload-safe data from startup-only data.** Nature/nutrition/counters are easier than changing default edible components.
5. **Warn loudly on conflicts.** Silent override makes compatibility impossible to debug.
6. **Use soft dependencies for cross-mod integration.** Herbcraft should not require Homeostatic or any food mod unless a separate addon explicitly does.
7. **Keep Java IDs stable.** Component IDs, packet IDs, recipe serializer IDs, mixin targets, item IDs, and effect IDs are not balance JSON.
8. **Validate extension data.** Every public JSON surface should get a validator before being advertised as stable.
9. **Keep guide ownership modular.** Core guide pages may teach Core systems; addon-specific guide pages must be registered or loaded by their own addon modules, or be gated by explicit optional-module data.

## 5. Proposed extension paths

### 5.1 Low-risk, reload-safe extension paths

These are the recommended first implementation targets.

```text
data/<namespace>/herbcraft/herb_natures/*.json
data/<namespace>/herbcraft/nutrition_profiles/*.json
data/<namespace>/herbcraft/ingredients/*.json
data/<namespace>/herbcraft/special_counters/*.json
data/<namespace>/herbcraft/knowledge_rumors/*.json
data/<namespace>/herbcraft/codex_loot_injections/*.json
```

These should affect existing items without necessarily making them newly edible.

Example layout:

```text
data/farmers_compat/herbcraft/herb_natures/cabbage.json
data/farmers_compat/herbcraft/nutrition_profiles/cabbage.json
data/farmers_compat/herbcraft/ingredients/cabbage.json
data/farmers_compat/herbcraft/special_counters/hot_pepper.json
data/farmers_compat/herbcraft/knowledge_rumors/cabbage.json
```

### 5.2 Startup-only loaded-mod/addon herb food extension

Implemented as a startup-only loaded-mod/addon resource scan:

```text
data/<namespace>/herbcraft/herbs/*.json
```

Purpose: allow an existing item to become a Herbcraft edible item or override food values in a controlled startup phase.

Constraint:

```text
This is scanned from loaded Fabric mod containers during Herbcraft initialization before default item components are applied. It is not a world datapack `/reload` feature. Do not promise hot-reloadable FOOD/CONSUMABLE component changes.
```

### 5.3 Optional Homeostatic compatibility data

Homeostatic already reads its own drinkable data path:

```text
data/<namespace>/environment/drinkable/*.json
```

Herbcraft currently ships optional Homeostatic drinkable entries under:

```text
data/herbcraft/environment/drinkable/*.json
data/herbcraft_cuisine/environment/drinkable/*.json
```

For future temperature integration, do **not** invent a format until Homeostatic temperature source/API has been re-read. The correct priority is:

1. Homeostatic-supported data format, if any.
2. Homeostatic public API, if any.
3. Soft-dependency integration wrapper.
4. Mixin/reflection only if unavoidable and validated.

## 6. Proposed JSON shapes

These are draft shapes. Field names should remain close to current internal files where possible.

### 6.1 Herb nature extension

Path:

```text
data/<namespace>/herbcraft/herb_natures/<id>.json
```

Single item form:

```json
{
  "schema_version": 1,
  "item": "examplemod:lavender",
  "temperature": "cool",
  "flavors": ["bitter", "pungent"],
  "taste_flavors": ["bitter", "astringent"],
  "traits": ["floral", "sedating"]
}
```

Multi-entry form:

```json
{
  "schema_version": 1,
  "entries": [
    {
      "item": "examplemod:lavender",
      "temperature": "cool",
      "flavors": ["bitter"],
      "traits": ["floral", "sedating"]
    },
    {
      "item": "examplemod:mint",
      "temperature": "cool",
      "flavors": ["pungent"],
      "traits": ["leafy", "clearing"]
    }
  ]
}
```

Initial allowed `temperature` values should match current Herbcraft:

```text
cold, cool, neutral, warm, hot
```

Initial allowed `flavors` should match current Herbcraft:

```text
sweet, bitter, pungent, sour, astringent, salty, bland
```

Initial allowed `traits` should match current Herbcraft until trait registration becomes dynamic:

```text
clearing, toxic, nourishing, stirring, sedating, aquatic, fiery, withered,
nether, floral, resinous, fungal, lunar, solar, leafy, icy, slimy,
honeyed, earthy, addictive, curio
```

Draft rule:

```text
Unknown temperature/flavor/trait should fail validation and warn or fail load depending on strictness.
```

### 6.2 Nutrition profile extension

Path:

```text
data/<namespace>/herbcraft/nutrition_profiles/<id>.json
```

Single item form:

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

Multi-entry form:

```json
{
  "schema_version": 1,
  "entries": [
    {
      "item": "examplemod:cabbage_roll",
      "values": { "grain": 12, "vege": 18, "oil": 4 }
    },
    {
      "item": "examplemod:venison_stew",
      "values": { "flesh": 28, "oil": 8, "vege": 8 }
    }
  ]
}
```

Current internal dimensions:

```text
grain, flesh, oil, vege, fruit
```

Important compatibility notes:

```text
The internal key `fruit` is currently displayed as `津液` / `Fluids`.
Do not rename the internal key in extension data until the nutrition dimension system is redesigned.

All built-in nutrition profiles are scaled at 0.6x relative to early design values for balanced pacing.
External extension profiles should use similar magnitude. Reference: cooked_beef is flesh=41, oil=16.
A single-eat value of 30-50 is typical for a primary dimension; 5-20 for secondary dimensions.
```

Future hard-core nutrition addons may require dynamic dimensions, but that is **not** part of this draft's first implementation.

### 6.3 Ingredient Codex extension

Path:

```text
data/<namespace>/herbcraft/ingredients/<id>.json
```

Single item form:

```json
{
  "schema_version": 1,
  "item": "farmersdelight:cabbage",
  "section": "fruit"
}
```

Multi-entry form:

```json
{
  "schema_version": 1,
  "entries": [
    { "item": "farmersdelight:cabbage", "section": "fruit" },
    { "item": "farmersdelight:cooked_rice", "section": "grain" }
  ]
}
```

Supported section keys currently match the built-in 食材录 sections:

```text
meat, seafood, grain, fruit, soup, special
```

External ingredient entries are server-authoritative and their item-to-entry ID map is synced to clients. Non-vanilla entries use namespace-aware internal Codex IDs such as `farmersdelight__cabbage` to avoid collisions with other mods that may use the same item path.

### 6.4 Special counter extension

Path:

```text
data/<namespace>/herbcraft/special_counters/<id>.json
```

Example:

```json
{
  "schema_version": 1,
  "id": "examplemod_hot_pepper",
  "items": ["examplemod:hot_pepper"],
  "window_ticks": 1200,
  "threshold_effects": [
    {
      "min_count": 3,
      "chance": 0.75,
      "effect": "minecraft:nausea",
      "duration_ticks": 160,
      "amplifier": 0
    }
  ]
}
```

This should map to current `SpecialCounters` behavior:

- `always_effects` apply whenever the item is consumed.
- `threshold_effects` apply when the count in `window_ticks` reaches `min_count`.
- `chance` should default to `1.0` if omitted.

### 6.5 Herb edible extension

Path:

```text
data/<namespace>/herbcraft/herbs/<id>.json
```

Example:

```json
{
  "schema_version": 1,
  "item": "examplemod:lavender",
  "nutrition": 1,
  "saturation_modifier": 0.1,
  "food_rolls": {
    "nutrition": { "amount": 1, "chance": 0.35 },
    "saturation": { "amount": 0.3, "chance": 0.45 }
  },
  "consume_seconds": 1.6,
  "medicinal": true,
  "stack_group": false,
  "effects": [
    {
      "effect": "minecraft:regeneration",
      "chance": 0.2,
      "amplifier": 0,
      "duration_seconds": 5,
      "positive": true
    }
  ],
  "counter": "examplemod_lavender"
}
```

Supported fields should initially match current `herbs.json`:

```text
item
nutrition
saturation_modifier
food_rolls
consume_seconds
medicinal
stack_group
remove_effects
effects
damage_events
heal_events
flags
counter
```

`food_rolls` is optional and is implemented for bundled herb data plus the startup-only loaded-mod/addon herb food path. It does not replace the stable vanilla base fields:

```text
nutrition                         stable visible hunger / food level
saturation_modifier               stable vanilla saturation modifier, converted by vanilla as nutrition * modifier * 2
food_rolls.nutrition.amount       direct visible hunger points, integer; usually +1 for raw forage
food_rolls.nutrition.chance       0.0..1.0 probability
food_rolls.saturation.amount      direct hidden saturation points, not a vanilla modifier
food_rolls.saturation.chance      0.0..1.0 probability
```

Common raw forage should usually set `nutrition = 0` and `saturation_modifier = 0.0`, then use small `food_rolls` values. Foodlike exceptions may keep stable visible hunger: raw kelp stays low-saturation, while post-client tuning lets sugar cane keep only +1 visible hunger but restore about +2 hidden saturation because it is juicy/fibrous. Hidden-only rolls must clamp to the current visible food level.

Current known flags:

```text
clearPositiveEffects
extinguish
bucketRemainder
drink
shortUseProjectile
```

Startup-only warning:

```text
This path is implemented for loaded Fabric mod/addon resources scanned during Herbcraft initialization. If this entry makes a non-food item edible, it must be present in a loaded mod container before Herbcraft registers default item components. World datapack `/reload` is not supported for making non-food items edible.
```

### 6.6 Knowledge rumor extension

Path:

```text
data/<namespace>/herbcraft/knowledge_rumors/<id>.json
```

Example:

```json
{
  "schema_version": 1,
  "entry": "herbal/lavender",
  "claims": [
    {
      "id": "calming_scent",
      "type": "note",
      "text_key": "knowledge.examplemod.lavender.claim.calming_scent",
      "truth": true,
      "source_tier": "common",
      "suspicious": false
    }
  ]
}
```

This should merge with current `KnowledgeRumorRegistry` behavior.

Required external resources:

```text
assets/<namespace>/lang/en_us.json
assets/<namespace>/lang/zh_cn.json
```

or equivalent language files for the referenced `text_key` values.

### 6.7 Codex loot injection extension

Path:

```text
data/<namespace>/herbcraft/codex_loot_injections/<id>.json
```

Example:

```json
{
  "schema_version": 1,
  "loot_table": "minecraft:chests/village/village_temple",
  "chance": 0.18,
  "chapter": "herbal",
  "entry": "lavender",
  "page_kind": "common"
}
```

For module-specific content, later implementation may allow:

```text
data/<namespace>/herbcraft/alchemy/codex_loot_injections/*.json
data/<namespace>/herbcraft/cuisine/codex_loot_injections/*.json
```

or keep one shared path with a `chapter` field. The implementation should choose one approach and document it before release.

## 7. Conflict and merge rules

A public extension API must define what happens if multiple sources define the same item/counter/entry.

### 7.1 Recommended baseline priority

From lowest to highest:

1. Herbcraft built-in data.
2. Herbcraft module built-in compatibility data.
3. External datapacks/addon resources in normal Minecraft resource-pack/datapack order.
4. Explicit Java API calls, if a future API allows them and runs after JSON load.

### 7.2 Item profile conflicts

For item-keyed systems such as nature and nutrition:

```text
Default rule: last loaded definition for the same item replaces the previous one.
Required behavior: log a WARN with item ID, old source, and new source.
```

Possible future optional fields:

```json
{
  "priority": 100,
  "replace": true
}
```

Do not add `priority` until there is a real need. Minecraft datapack order may be enough for the first public version.

### 7.3 Counter conflicts

For special counters:

- `id` must be unique after namespace resolution.
- Multiple counters may include the same item, but this should warn because it can multiply effects.
- If same `id` appears twice, last loaded definition replaces prior definition and logs WARN.

### 7.4 Knowledge conflicts

For `knowledge_rumors`:

- Claims with different `id` values under the same entry should merge.
- Claims with the same `id` should replace or warn; recommended first behavior: replace with WARN.

For full future knowledge entries/chapters:

- Chapter IDs and entry IDs must be stable.
- UI/sync compatibility must be validated before external chapter registration is advertised.

## 8. Reload policy

### 8.1 Reload-safe candidates

The following should be designed as `/reload` capable if implementation cost is reasonable:

```text
herb_natures
nutrition_profiles
ingredients
special_counters
knowledge_rumors
codex_loot_injections
Homeostatic-style data resources, if the target mod supports reload
```

### 8.2 Startup-only candidates

The following should be treated as startup-only unless proven otherwise:

```text
herbs entries that add or modify FOOD/CONSUMABLE default components
new essence item registration
new potion/effect registration
new data component registration
new recipe serializer registration
new network payload registration
full Codex chapter/entry structure registration if client sync cannot reload safely
```

Reason:

Minecraft/Fabric registry and default component timing is not the same as ordinary datapack balance reload. In 26.1.2, item component and registry behavior must be tested directly before promising hot reload.

## 9. Java API surface proposal

Data-only integration should be enough for many compat packs, but code addons need a stable API.

Future APIs may include:

```java
HerbcraftApi.registerNature(ItemLike item, NatureProfile profile);
HerbcraftApi.registerNutrition(ItemLike item, NutritionProfile profile);
HerbcraftApi.registerSpecialCounter(SpecialCounterDefinition definition);
HerbcraftApi.registerHerbFood(HerbFoodDefinition definition);
HerbcraftApi.registerKnowledgeRumor(String entryKey, KnowledgeClaim claim);
```

However, the current `1.1.3` public API is limited and should not be over-promised. The API must specify:

- when it may be called;
- whether calls are client, server, or common;
- whether calls are reload-aware;
- what happens if the item does not exist;
- what happens on duplicate definitions.

Recommended event phases for future implementation:

```text
HerbcraftExtensionEvents.REGISTER_RELOAD_SAFE_DATA
HerbcraftExtensionEvents.REGISTER_STARTUP_HERB_FOODS
HerbcraftExtensionEvents.REGISTER_KNOWLEDGE_CONTENT
```

Names are placeholders, not final API.

## 10. Knowledge, Codex, Jade, and tooltip gate policy

Current Herbcraft knowledge display is Java-owned:

```text
Unknown / Encountered / Heard / Tasted / Mastered
```

A herb entry created from Herbcraft food data can be shown through existing Codex/Jade/tooltip gates, but the deeper systems remain Java:

```text
Codex UI
client/server sync payloads
chapter/section registration
entry sorting
Jade providers
tooltip callbacks
practice route registration
knowledge state storage
```

### 10.1 First safe goal

Do not start by allowing arbitrary new Codex chapters from JSON.

First safe goal:

```text
Allow external data to attach nature, nutrition, rumors, and maybe section hints to existing Herbcraft herbal entries.
```

Possible field:

```json
{
  "item": "examplemod:lavender",
  "section": "flowers"
}
```

or a separate path:

```text
data/<namespace>/herbcraft/knowledge_sections/*.json
```

### 10.2 Later goal

After basic extension data is stable, allow full external knowledge entries:

```text
data/<namespace>/herbcraft/knowledge_chapters/*.json
data/<namespace>/herbcraft/knowledge_entries/*.json
data/<namespace>/herbcraft/practice_routes/*.json
```

This is high-risk because it touches UI and sync. It should be planned for a larger compatibility/API version, not slipped into a patch without testing.

## 11. Homeostatic compatibility roadmap

Current Herbcraft Core/Alchemy `1.1.4` has optional Homeostatic thirst/drinkable data.

Important learned rule:

```text
Do not use negative Homeostatic drinkable amount values.
Homeostatic can store water below zero internally, which can cause dehydration persistence after partial recovery.
Risk foods should use amount 0 plus thirst-effect chance/duration/potency instead.
```

### 11.1 Temperature compatibility

Herbcraft nature has a natural bridge to Homeostatic temperature:

```text
cold/cool -> mild cooling
warm/hot -> mild warming
icy trait -> cold risk
fiery/nether trait -> heat risk
soups/stews -> mild warmth and hydration
```

But temperature support must not be implemented by guessing. Required research first:

```text
Homeostatic temperature classes and storage
public APIs or events
supported data JSON formats, if any
network sync requirements
effect-based alternatives
safe soft-dependency patterns
```

Implementation priority:

1. Use Homeostatic's own data format if available.
2. Use Homeostatic public API if available.
3. Add a soft-dependency wrapper in Herbcraft if safe.
4. Avoid mixins unless no safer approach exists.

Balance policy:

```text
short duration
small magnitude
no permanent body-temperature manipulation
no negative-residue behavior
no bypass of Homeostatic's own protection systems
```

## 12. Proposed implementation phases

### Phase 0: Preserve `1.1.3` release candidate

Do not add large untested extension systems to `1.1.3`.

Allowed:

```text
critical bug fixes
documentation
output correction
validators that do not change gameplay
```

### Phase 1: Homeostatic temperature research

Status: **completed for this round; no gameplay integration yet.**

Homeostatic `26.1` source was cached under `reference_cache/Homeostatic_26_1` and inspected. Notes were written to:

```text
docs/HOMEOSTATIC_TEMPERATURE_COMPAT_NOTES.md
```

Decision:

```text
Do not directly manipulate Homeostatic core/skin temperature from Herbcraft Core yet.
No drinkable-like item temperature JSON surface was found in this round.
Future implementation should be a conservative optional compat addon or soft-dependency adapter after more design/testing.
```

### Phase 2: Low-risk external JSON merge layer

Status: **implemented for Core server data paths in this source state.**

Implemented ResourceManager/datapack merging for:

```text
herb_natures
nutrition_profiles
ingredients
special_counters
knowledge_rumors
codex_loot_injections
```

Runtime implementation files:

```text
core/src/main/java/com/herbcraft/data/ExternalDataResources.java
core/src/main/java/com/herbcraft/data/HerbcraftExternalDataReloadListener.java
core/src/main/java/com/herbcraft/nature/HerbNatureRegistry.java
core/src/main/java/com/herbcraft/nutrition/NutritionRegistry.java
core/src/main/java/com/herbcraft/knowledge/IngredientsChapter.java
core/src/main/java/com/herbcraft/data/HerbcraftDataSyncPayload.java
core/src/main/java/com/herbcraft/data/HerbcraftDataSyncManager.java
core/src/main/java/com/herbcraft/logic/SpecialCounters.java
core/src/main/java/com/herbcraft/knowledge/KnowledgeRumorRegistry.java
core/src/main/java/com/herbcraft/knowledge/CodexLootRules.java
core/src/main/java/com/herbcraft/knowledge/CodexLoot.java
```

Validation/example files:

```text
scripts/validate_external_extension_spec.py
docs/examples/external_extension_compat_pack/
```

Current client sync status:

```text
As of Core/Alchemy 1.1.4, server-authoritative nutrition profiles, nature profiles, and 食材录 ingredient entry IDs are synced to clients. This lets multiplayer/datapack-provided nutrition/nature/ingredient data participate in client tooltip and Codex-facing display. Codex snapshots themselves are still generated server-side.
```

### Phase 3: Startup-only herb food extension

Status: **implemented for loaded Fabric mod/addon resources; not reloadable world datapacks.**

Implemented path:

```text
data/<namespace>/herbcraft/herbs/*.json
```

Runtime behavior:

```text
HerbFoodRegistry.loadFromBundledJson()
HerbFoodRegistry.loadExternalStartupResources()
DefaultItemComponentEvents.MODIFY.register(HerbFoodRegistry::registerDefaultComponents)
```

Implementation files:

```text
core/src/main/java/com/herbcraft/registry/HerbFoodRegistry.java
core/src/main/java/com/herbcraft/Herbcraft.java
```

Example, smoke evidence, and client-test artifact:

```text
docs/examples/startup_herb_food_extension_mod/
docs/reports/P3_STARTUP_HERB_FOOD_SMOKE_20260615.md
docs/reports/P3_CLIENT_TEST_CHECKLIST_20260615.md
release_output/herbcraft_test_20260614_review/herbcraft-p3-startup-food-test-addon-1.0.0.jar
```

The test addon is not a release jar; it exists only to validate the P3 startup resource path in a real client.

Boundary:

```text
This is startup-only. It scans loaded Fabric mod containers before default components are applied. It does not make world datapack `/reload` able to add FOOD/CONSUMABLE components to arbitrary items.
```

### Phase 4: Public Java API

Status: **P4.1-P4.5 completed for read/event extension boundary; write APIs still deferred.**

P4 is now split into:

```text
P4.1 JSON spec formalization — completed in docs/JSON_EXTENSION_SPEC_1_1_3.md
P4.2 read-only Java API — implemented as com.herbcraft.api.HerbcraftDataAPI
P4.3 event API — implemented as com.herbcraft.api.HerbcraftEvents
P4.4 example addon/datapack — implemented under docs/examples/
P4.5 validators and docs final sweep — completed in docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md
```

Reason for splitting:

```text
A public Java API is a compatibility contract. JSON already owns most content registration. Java should expose stable reads and events before any write/registration API is considered.
```

P4.5 deliverables completed:

```text
final validator coverage review
final documentation sweep
release-readiness summary
```

Remaining before public 1.1.3 release is release work rather than P4 design work:

```text
version bump decision
final full validator run
focused client sanity pass
release copy/update notes
```

### Phase 5: Knowledge/Codex external content

Add data-driven chapter/entry/section/practice-route extension only after sync/UI risks are handled.

Deliverables:

```text
knowledge entry schema
client/server sync validation
Codex UI test
Jade/tooltip test
language-key requirements
```

### Phase 6: Specific compatibility packs

After the extension surface is stable, create or document compat packs for concrete mods.

Current concrete food-mod batch status (2026-06-17): Farmer's Delight Refabricated + More Delight compatibility is prepared as data-first CSV/JSON, not Java mod-ID hardcoding. Rounds 1-5 are complete for this decision: author client testing passed for Farmer's Delight + More Delight, and Reg's More Foods was removed because its datapack-driven item identity can collide with vanilla/Herbcraft-known items. The current generated release-candidate draft pack is `docs/compat_drafts/food_mods_26_1_2/generated_pack/`, sourced from `compat_review_round5_farmer_moredelight.csv`, and contains 108 nutrition profiles, 108 ingredient entries, 5 herb nature entries, and 0 knowledge rumors. The same Farmer+More data is now bundled into Herbcraft Core under `data/farmers_moredelight_herbcraft/herbcraft/`. A generic food-nature pharmacology bridge lets explicitly profiled ordinary foods participate in nature pharmacology without using the startup-only `herbs` path or overriding target mod food components, and 百草志 now gets `药食志` entries for those nature-bearing foods after tasting.

Examples:

```text
Homeostatic temperature/thirst
large food/crop mods
tea/drink mods
season/temperature mods
adventure/dungeon loot mods
```

## 13. Validator plan

Every public extension surface should have a validator before it is advertised.

Current and suggested scripts:

```text
scripts/validate_external_extension_spec.py        # implemented: validates the example P2 compat pack schema
scripts/validate_external_herb_natures.py         # possible future split validator
scripts/validate_external_nutrition_profiles.py   # possible future split validator
scripts/validate_external_ingredients.py          # possible future split validator
scripts/validate_external_special_counters.py     # possible future split validator
scripts/validate_external_knowledge_rumors.py     # possible future split validator
scripts/validate_external_herb_foods.py           # possible future split validator; P3 example is currently covered by validate_external_extension_spec.py
```

Minimum checks:

- valid JSON;
- `schema_version` supported;
- item IDs are parseable;
- known item IDs when running against a modded registry or declared test fixture;
- known effect IDs where possible;
- nutrition dimensions are known;
- nature values are allowed;
- no accidental negative Homeostatic drinkable amount in Herbcraft-provided compat data;
- duplicate entries produce deterministic warnings;
- language keys exist for bundled examples.

## 14. What this draft does not promise

This draft does **not** promise:

- data-only registration of brand-new Minecraft items;
- hot-reloadable default FOOD/CONSUMABLE changes;
- arbitrary custom nutrition dimensions;
- arbitrary custom nature traits beyond current validated values;
- full Codex chapter creation by JSON in the first phase;
- stable Java extension events before they are implemented and documented;
- compatibility with every external mod's internal mechanics;
- Homeostatic temperature manipulation before its API/source is researched.

## 15. Recommended immediate priority

Current status after the 2026-06-15 consistency review and later P3 client report:

| Priority | Work | Status | Reason |
|---|---|---|---|
| P0 | Keep `1.1.3` stable | Ongoing | Current jars/source zip are release-candidate baseline. |
| P1 | Research Homeostatic temperature | Done for this round | No safe item-temperature data surface found; no gameplay integration yet. |
| P2 | Implement low-risk external JSON merge layer | Implemented + server-smoked | Foundation for broad mod compatibility. |
| P3 | Implement startup-only herb food extension | Implemented + server-smoked + author client-tested PASS | Allows loaded addon/mod resources to make existing items Herbcraft edible. |
| P3.5 | Vanilla food "食材录" knowledge chapter | Implemented | 39 vanilla foods in 6 sections (meat/seafood/grain/fruit/soup/special). Eating-gated: must eat to see nutrition. No pharmacology. |
| P3.6 | `1.1.3` new-player guidance / Codex activation | Phase 2.1 + pharmacology guide microcopy implemented; client combination test pending | Guide registration is data-backed by module-owned `codex_guides.json`; Cuisine/Alchemy guide pages are addon-owned; 五味/七情 principles are persisted in player pharmacology state and can reveal guide lines with latest item examples. Core Codex guide pages now plainly say 性味 is pharmacology rather than nutrition, and 七情 is recent-herb interaction rather than a fixed recipe list. See `docs/NEW_PLAYER_GUIDANCE_1_1_2_DESIGN.md` and Phase 2.1 reports. |
| P3.7 | `1.1.3` one-time guide hints | Implemented; client test pending | Data-backed module-owned `guide_hints.json` files plus `GuideHintManager` with persisted seen-hint IDs. |
| P3.8 | Pharmacology qi/relation data tuning | Implemented; client feel test pending | `pharmacology_rules.json` now owns temperature values and ordered `seven_emotion_rules`; exact hot/cold tuned to ±15 and qi regression interval tuned to 600 ticks. |
| P3.9 | Cuisine/Alchemy in-Codex route guidance | Implemented; client test pending | Cuisine and Alchemy keep short 总纲 gateway pages and now add module-owned overview sections/sub-guide entries inside `膳房录` and `炼金要术`, driven by their `codex_guides.json` files and existing progress/knowledge state. |
| P3.10 | Raw herb hunger/saturation rebalance | Implemented in source; post-client seed/sugar-cane tuning applied; retest pending | `food_rolls` is parsed from bundled/startup herb food JSON and settled after vanilla consumption. Common scenery/flowers/leaves/saplings still avoid stable early food. After client feedback, seeds now use stronger hidden-saturation rolls (`S+2` ordinary seeds, `S+3` melon/pumpkin seeds), sugar cane remains visible +1 but now gives about +2 hidden saturation, and Homeostatic sugar cane hydration is anchored at amount 3. `minecraft:blue_orchid` no longer keeps a guaranteed `minecraft:saturation` effect. Guarded by `scripts/validate_raw_herb_food_balance.py` and `scripts/validate_homeostatic_compat.py`. |
| P4 | Extension API | P4.1-P4.5 complete for read/event boundary | `docs/JSON_EXTENSION_SPEC_1_1_3.md` freezes supported JSON paths/schemas/boundaries. `HerbcraftDataAPI` exposes read-only Java queries. `HerbcraftEvents` exposes observational events. Examples show JSON content plus Java reads/events. Write APIs remain deferred. |
| P5 | Externalize deeper Knowledge/Codex content | Future | Powerful but UI/sync-heavy. |
| P6 | Build concrete compat packs | Farmer+More built-in optional compat implemented | Farmer's Delight Refabricated + More Delight passed author client testing and now has both a generated data pack artifact and bundled optional Core data. A generic food-nature pharmacology bridge handles explicit nature+nutrition foods without overriding target food components. RMF was removed due datapack-driven item identity risk. |

P3 client result reported by the author:

```text
Full pass. The test item was edible, functional, recorded in the Codex/残页 flow, and appeared normally in 奇材志 with expected description/lines.
```

Recommended next directions:

```text
1. Client-test implemented Phase 2.1/P3.7 guide behavior and one-time hints across module combinations and pharmacology discoveries. Use `docs/reports/PHARMACOLOGY_SEVEN_EMOTION_TEST_MATRIX_20260616.md` for concrete 七情/五味 trigger pairs, and confirm the new Codex microcopy teaches the system without needing extra advancements.
2. Client-test pharmacology qi/relation tuning in the packaged `1.1.3` jars; `temperature_values` and `seven_emotion_rules` are now data-backed.
3. Client-test implemented P3.9 module guide pages in `膳房录` and `炼金要术`; confirm staged guidance is useful and does not reveal hidden recipes too early.
4. Client-test the implemented P3.10 raw herb food-roll balance in the packaged `1.1.3` jars. Confirm common scenery/flower/leaf/sapling spam no longer replaces real early food, retest stronger seeds so they feel meaningful without becoming bread, and confirm sugar cane remains visible +1 while providing about +2 hidden saturation plus Homeostatic hydration amount 3. Keep P3.10 data-driven and validator-backed.
5. Keep P3's boundary explicit: loaded-mod/addon startup resources only, not world datapack /reload edible-component changes.
6. Client-test any guidance/food-balance implementation with a fresh player/world; it must reduce confusion and avoid making common forage replace real food.
7. `1.1.4` Core/Alchemy version bump/release packaging is now done in the current source line; after final client smoke, resume P4 Java API design rather than adding more patch-scope systems.

Already validated in current source/release line:
- Latest nutrition correction matrix: 津液/Fluids is decoupled from the ordinary correction matrix.
- High-津液 surplus helper requires at least two non-fruit excess dimensions that share a correction-matrix relationship (weight > 0) before activating. A single excess dimension alone, or two unrelated excess dimensions, will not trigger fluid surplus decay.
```

Related reports/specs:

```text
docs/reports/EXTENSION_CONSISTENCY_REVIEW_20260615.md
docs/reports/NUTRITION_FLUIDS_DESIGN_REVIEW_20260615.md
docs/reports/NUTRITION_IMBALANCE_REDESIGN_OPTIONS_20260615.md
docs/reports/NUTRITION_CORRECTION_NEXT_IMPLEMENTATION_SPEC_20260615.md
```

Implemented nutrition correction direction:

```text
B + E hybrid implemented in Core: tracks recent meals, computes corrective score by actual nutrition amount through the non-fruit correction matrix, temporarily relieves dietary imbalance when recent meals are structurally corrective, accelerates decay of excessive dimensions, and makes high 津液/Fluids non-punitive in the base mod. Matrix correction now activates at 750+ (was 850), with graduated decay: 750-849 uses half decay rate, 850+ uses full rate. Punitive debuffs still require 850+. 津液/Fluids does not participate in the ordinary correction matrix; it only acts through its low-deficiency behavior and high-fluids surplus helper. The high-津液 surplus helper still requires 850+ and at least two non-fruit excess dimensions that share a correction-matrix relationship before activating the -10/200t decay toward 700.
```

Implementation, tuning, and issue reports:

```text
docs/reports/NUTRITION_CORRECTION_IMPLEMENTATION_REPORT_20260615.md
docs/reports/NUTRITION_CORRECTION_TUNING_MATRIX_PROPOSAL_20260615.md
docs/reports/NUTRITION_CORRECTION_STACKING_REVIEW_20260615.md
docs/reports/NUTRITION_FLUIDS_CORRECTION_COUPLING_REVIEW_20260615.md
```

## 16. Summary

The safest long-term direction is:

```text
Do not hardcode every compatibility mod in Java.
Do not pretend current bundled JSON files are already a complete public datapack API.
Keep the external merge layer and startup-only edible registration clearly separated.
Only then harden client behavior and design deeper Codex/API integration.
```

This lets Herbcraft become a genuinely integration-friendly mod while keeping its core mechanics, save data, UI, and networking stable.
