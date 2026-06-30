# Herbcraft JSON Extension Spec 1.1.3

Status: **P4.1 formal extension specification**  
Baseline: Herbcraft `1.1.3` source/release-candidate line  
Minecraft: `26.1.2`  
Java: `25+`

This document freezes the supported JSON-facing extension surface for Herbcraft's first public extension contract.

It does **not** make Java registration APIs public. It records the stable data paths, schema shapes, reload boundaries, merge rules, and client sync expectations that P4.2/P4.3 Java APIs must respect later.

## 1. Design contract

Herbcraft's extension model is:

```text
Java owns mechanics, registries, load timing, sync, UI, validation, and events.
JSON owns content, balance values, compatibility tables, guide/hint rows, nutrition/nature data, and most rules.
```

If a compatibility concern can be expressed in JSON, it should be expressed in JSON. Java APIs are for querying and listening first, not for hardcoding third-party content tables.

## 2. Supported extension paths

### 2.1 Reload-safe server data paths

These paths are supported for datapacks and addon resources and are reloaded by the server resource reload layer:

```text
data/<namespace>/herbcraft/herb_natures/*.json
data/<namespace>/herbcraft/nutrition_profiles/*.json
data/<namespace>/herbcraft/ingredients/*.json
data/<namespace>/herbcraft/special_counters/*.json
data/<namespace>/herbcraft/knowledge_rumors/*.json
data/<namespace>/herbcraft/codex_loot_injections/*.json
```

Runtime owner:

```text
core/src/main/java/com/herbcraft/data/HerbcraftExternalDataReloadListener.java
core/src/main/java/com/herbcraft/data/ExternalDataResources.java
```

Reload-safe means:

- the data can be discovered through `/reload`;
- the server remains authoritative;
- compatible client-facing data is synced from server to client where implemented;
- the path does not change item registration or default FOOD/CONSUMABLE components.

### 2.2 Startup-only herb food path

This path is supported only from loaded Fabric mod/addon resources scanned during mod startup:

```text
data/<namespace>/herbcraft/herbs/*.json
```

It may add or override default FOOD/CONSUMABLE components for existing registered items. It is **not** world-datapack reloadable.

Boundary:

```text
World datapacks cannot use /reload to make arbitrary items edible through Herbcraft.
```

## 3. Common file conventions

### 3.1 Namespace

Use your own namespace:

```text
data/my_compat/herbcraft/nutrition_profiles/example.json
```

Do not replace Herbcraft's bundled files such as:

```text
data/herbcraft/nutrition_profiles.json
```

### 3.2 Schema version

All public extension JSON should include:

```json
{ "schema_version": 1 }
```

Current loaders tolerate some legacy/bundled shapes, but public packs should use `schema_version: 1`.

### 3.3 Single entry and multi-entry forms

Most item-keyed paths support either one entry per file:

```json
{
  "schema_version": 1,
  "item": "examplemod:lavender",
  "temperature": "cool",
  "flavors": ["bitter"],
  "traits": ["floral"]
}
```

or a multi-entry wrapper:

```json
{
  "schema_version": 1,
  "entries": [
    { "item": "examplemod:lavender", "temperature": "cool", "flavors": ["bitter"], "traits": ["floral"] },
    { "item": "examplemod:mint", "temperature": "cool", "flavors": ["pungent"], "traits": ["leafy", "clearing"] }
  ]
}
```

Use the multi-entry form for generated compatibility tables.

### 3.4 Missing item IDs

An extension may mention an item from an optional mod. If that mod is absent, Herbcraft must skip the runtime entry rather than creating an Air placeholder.

Current guard requirement:

```text
Check registry presence before resolving optional external item IDs.
```

## 4. Herb natures

Path:

```text
data/<namespace>/herbcraft/herb_natures/*.json
```

Purpose:

```text
Attach Herbcraft 性味 / nature metadata to an existing item.
```

Single-entry shape:

```json
{
  "schema_version": 1,
  "item": "examplemod:mint_leaf",
  "temperature": "cool",
  "flavors": ["pungent", "sweet"],
  "taste_flavors": ["pungent"],
  "traits": ["leafy", "clearing"]
}
```

Fields:

| Field | Required | Type | Notes |
|---|---:|---|---|
| `schema_version` | recommended | integer | Use `1`. |
| `item` | yes | item id | Existing registered item ID. |
| `temperature` | no | string | Defaults to `neutral` if omitted. |
| `flavors` | no | string array | Defaults to `['bland']` if omitted/empty. |
| `taste_flavors` | no | string array | Defaults to `flavors` if omitted/empty. |
| `traits` | no | string array | Defaults to empty list. |

Allowed `temperature` values:

```text
cold, cool, neutral, warm, hot
```

Allowed `flavors` and `taste_flavors` values:

```text
sweet, bitter, pungent, sour, astringent, salty, bland
```

Allowed `traits` values in 1.1.3:

```text
clearing, toxic, nourishing, stirring, sedating, aquatic, fiery, withered,
nether, floral, resinous, fungal, lunar, solar, leafy, icy, slimy,
honeyed, earthy, addictive, curio
```

Traits are **not** dynamically registered in 1.1.3. They are a fixed vocabulary used by Codex hints and pharmacology relation rules.

### 4.1 Food-nature bridge

If an ordinary non-Herbcraft-herb food has all three:

```text
nutrition profile
ingredient entry
explicit herb nature profile
```

then it may participate in the generic food-nature pharmacology bridge. This lets food-like items such as Farmer's Delight onion/tomato affect 五味、七情、气机 without overriding target-mod FOOD/CONSUMABLE components.

Codex split:

```text
食材录: nutrition and food category.
百草志 / 药食志: nature, taste, body feeling, and pharmacology hints.
```

The bridge is data-driven: Java defines the rule, JSON provides the item rows.

## 5. Nutrition profiles

Path:

```text
data/<namespace>/herbcraft/nutrition_profiles/*.json
```

Purpose:

```text
Attach Herbcraft nutrition dimensions to an existing food item.
```

Shape:

```json
{
  "schema_version": 1,
  "entries": [
    {
      "item": "farmersdelight:cabbage",
      "values": {
        "vege": 14,
        "fruit": 4
      }
    }
  ]
}
```

Supported dimensions:

```text
grain, flesh, oil, vege, fruit
```

Current display names:

```text
grain = 谷粮 / Grain
flesh = 血肉 / Flesh
oil = 膏脂 / Oil
vege = 菜蔬 / Greens
fruit = 津液 / Fluids
```

Rules:

- values should be positive integers;
- zero/negative values are ignored by current loader behavior;
- public data should omit dimensions with no contribution;
- a typical primary dimension is around `20..50`; secondary dimensions are often `2..20`.

Nutrition profiles apply to all consumed foods whose runtime item ID matches the profile.

## 6. Ingredient Codex entries

Path:

```text
data/<namespace>/herbcraft/ingredients/*.json
```

Purpose:

```text
Add 食材录 entries for existing foods.
```

Shape:

```json
{
  "schema_version": 1,
  "entries": [
    { "item": "farmersdelight:cabbage", "section": "fruit" },
    { "item": "farmersdelight:cooked_rice", "section": "grain" }
  ]
}
```

Supported sections:

```text
meat, seafood, grain, fruit, soup, special
```

Notes:

- The internal section key `fruit` currently displays as `蔬果` / `Produce` in 食材录 contexts.
- External non-vanilla ingredient entry IDs are namespace-aware to avoid collisions.
- Example: `farmersdelight:cabbage` becomes internal entry ID `farmersdelight__cabbage`.
- Missing optional target-mod item IDs are skipped.
- Eating the item records 食材录 knowledge when a matching entry exists.

## 7. Special counters

Path:

```text
data/<namespace>/herbcraft/special_counters/*.json
```

Purpose:

```text
Add windowed overuse / threshold side effects for existing items.
```

Shape:

```json
{
  "schema_version": 1,
  "id": "examplemod_hot_pepper",
  "items": ["examplemod:hot_pepper"],
  "window_ticks": 1200,
  "always_effects": [
    { "effect": "minecraft:fire_resistance", "duration_ticks": 100, "amplifier": 0, "chance": 1.0 }
  ],
  "threshold_effects": [
    { "min_count": 3, "effect": "minecraft:nausea", "duration_ticks": 160, "amplifier": 0, "chance": 0.5 }
  ]
}
```

Supported wrapper forms:

```text
single object
{ "entries": [...] }
{ "counters": [...] }
```

Fields:

| Field | Required | Notes |
|---|---:|---|
| `id` | recommended | If omitted externally, derived from resource ID. |
| `items` | yes | Item IDs affected by the counter. |
| `window_ticks` | no | Defaults to `1200`. |
| `always_effects` | no | Applied whenever matching item is consumed. |
| `threshold_effects` | no | Applied once count in window reaches `min_count`. |

Effect fields:

```text
effect              required effect id
duration_ticks      preferred duration field
duration_seconds    accepted fallback
amplifier           defaults to 0
chance              defaults to 1.0
min_count           threshold_effects only; defaults to 1
```

Duplicate counter IDs currently warn but both rules may run. Avoid duplicates.

External counter IDs are supported and persisted by ID. Older built-in counter save fields are migrated into the dynamic counter map when present.

## 8. Knowledge rumors

Path:

```text
data/<namespace>/herbcraft/knowledge_rumors/*.json
```

Purpose:

```text
Attach rumor/claim lines to existing Codex entries.
```

Shape:

```json
{
  "schema_version": 1,
  "entry": "herbal/lavender",
  "claims": [
    {
      "id": "calming_scent",
      "type": "note",
      "text_key": "knowledge.examplemod.lavender.calming_scent",
      "truth": true,
      "source_tier": "common",
      "suspicious": false
    }
  ]
}
```

Fields:

| Field | Required | Notes |
|---|---:|---|
| `entry` | yes | Use `chapter/entry`, e.g. `herbal/dandelion`. If no slash is provided, current loader normalizes to `herbal/<path>`. Public packs should use explicit `chapter/entry`. |
| `claims` | no | List of claim objects. |
| `claims[].id` | yes | Unique within the entry. |
| `claims[].type` | no | Defaults to `note`. |
| `claims[].text_key` | yes | Must be supplied in lang files. |
| `claims[].truth` | no | Defaults false unless true. |
| `claims[].source_tier` | no | Defaults `common`. |
| `claims[].suspicious` | no | Defaults to `!truth`. |

Supported `source_tier` / page-kind-like values:

```text
common, trade, rare, errata
```

External claims with the same claim ID replace prior claims for that entry and log a warning.

## 9. Codex loot injections

Path:

```text
data/<namespace>/herbcraft/codex_loot_injections/*.json
```

Purpose:

```text
Inject Herbcraft tattered pages into existing loot tables.
```

Shape:

```json
{
  "schema_version": 1,
  "entries": [
    {
      "loot_table": "minecraft:chests/village/village_temple",
      "chance": 0.18,
      "chapter": "herbal",
      "entry": "dandelion",
      "page_kind": "common"
    }
  ]
}
```

Supported wrapper forms:

```text
single object with loot_table
{ "entries": [...] }
{ "page_injections": [...] }
```

Fields:

| Field | Required | Notes |
|---|---:|---|
| `loot_table` | yes | Loot table ID. |
| `chance` | yes | Float `0.0..1.0`. |
| `chapter` | no | Defaults to `herbal`. |
| `entry` | no | Defaults empty; random/unspecified page behavior depends on Codex page item handling. |
| `page_kind` | no | Defaults `common`. |

Supported `page_kind` values:

```text
common, trade, rare, errata
```

Loot injection only modifies builtin loot tables in the current implementation.

## 10. Startup-only herb food definitions

Path:

```text
data/<namespace>/herbcraft/herbs/*.json
```

Purpose:

```text
Make an existing registered item a Herbcraft edible item during startup, or override Herbcraft edible behavior at startup.
```

This path is for loaded mod/addon resources, not ordinary world datapack reload.

Shape:

```json
{
  "schema_version": 1,
  "item": "examplemod:lavender",
  "nutrition": 1,
  "saturation_modifier": 0.1,
  "consume_seconds": 1.6,
  "medicinal": true,
  "stack_group": false,
  "food_rolls": {
    "nutrition": { "amount": 1, "chance": 0.35 },
    "saturation": { "amount": 0.3, "chance": 0.45 }
  },
  "effects": [
    { "effect": "minecraft:regeneration", "chance": 0.2, "amplifier": 0, "duration_seconds": 5, "positive": true }
  ],
  "remove_effects": ["minecraft:poison"],
  "damage_events": [
    { "chance": 0.1, "amount": 1.0 }
  ],
  "heal_events": [
    { "chance": 0.1, "amount": 1.0 }
  ],
  "flags": ["drink"],
  "counter": "examplemod_lavender"
}
```

Required fields in public examples:

```text
item
nutrition
saturation_modifier
consume_seconds
medicinal
stack_group
```

Supported `flags`:

```text
clearPositiveEffects, extinguish, bucketRemainder, drink, shortUseProjectile
```

`food_rolls` semantics:

```text
food_rolls.nutrition.amount     direct visible hunger points; integer
food_rolls.nutrition.chance     0.0..1.0
food_rolls.saturation.amount    direct hidden saturation points, not vanilla modifier
food_rolls.saturation.chance    0.0..1.0
```

Important warning:

```text
Do not use this path for ordinary target-mod foods if all you need is nutrition/nature metadata.
It changes FOOD/CONSUMABLE default components and can override target mod behavior.
```

For ordinary already-edible foods, prefer:

```text
nutrition_profiles + ingredients + optional herb_natures
```

## 11. Merge, override, and conflict policy

Current effective priority:

```text
Herbcraft bundled baseline
< bundled optional Herbcraft compat data
< external datapack/addon resources in resource manager order
```

Item-keyed profile behavior:

- later definitions for the same item may override earlier profiles;
- loaders should log warnings when overriding known profiles where implemented;
- public packs should avoid defining the same item as another active compat pack unless intentionally replacing it.

Special counters:

- duplicate counter IDs warn;
- multiple counter rules can run if active;
- avoid duplicate IDs unless you deliberately want layered behavior.

Knowledge rumors:

- claims merge by entry;
- same claim ID replaces prior claim and logs warning.

## 12. Client sync policy

The server owns gameplay data.

Currently synced to clients:

```text
nutrition profiles
herb nature profiles
ingredient item -> Codex entry ID map
```

This supports client tooltip/Codex-facing display for external data in multiplayer.

Not currently a public JSON API:

```text
full external Codex chapter creation
arbitrary external guide page ownership
custom UI layout
new network payload IDs
```

## 13. Bundled optional compat data

Herbcraft Core may ship optional compat data using the same extension paths as external packs.

Current bundled optional compat namespace:

```text
data/farmers_moredelight_herbcraft/herbcraft/
```

Current scope:

```text
Farmer's Delight Refabricated + More Delight
Reg's More Foods intentionally excluded
```

Current counts:

```text
nutrition_profiles: 108
ingredients: 108
herb_natures: 5
knowledge_rumors: 0
```

If the target mods are absent, missing item IDs must be skipped. They must not become `minecraft:air` entries.

## 14. What this spec does not promise

P4.1 does **not** promise:

- Java write/registration APIs;
- custom nutrition dimensions;
- custom dynamically registered trait types;
- hot-reloadable FOOD/CONSUMABLE component changes;
- JSON registration of brand-new Minecraft items;
- arbitrary Codex chapter creation;
- custom UI layout or packet registration;
- compatibility with datapack-driven item identity systems that do not expose stable item IDs.

## 15. P4 follow-up phases

This spec is P4.1.

Current follow-up phase status:

```text
P4.2 read-only Java API — implemented as com.herbcraft.api.HerbcraftDataAPI
P4.3 event API — implemented as com.herbcraft.api.HerbcraftEvents
P4.4 example addon/datapack — implemented under docs/examples/
P4.5 validators and docs final sweep — completed in docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md
```

Any later Java write/registration API must follow this JSON spec's merge, reload, conflict, and sync rules.
