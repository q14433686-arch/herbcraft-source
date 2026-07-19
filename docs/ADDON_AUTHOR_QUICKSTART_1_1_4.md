# Herbcraft 1.1.4 Addon Author Quickstart

Status: public-facing quickstart for datapack, modpack, and code-addon authors
Target: Herbcraft Core/Alchemy `1.1.4`, Cuisine `1.1.3`. The data/API surface is inherited from the 1.1.3 quickstart; this file is the current named copy for the 1.1.4 Core/Alchemy line.
Minecraft: `26.2` (port in validation)
Java: `25+`

This is the practical “what should I use?” guide. The formal schema reference is:

```text
docs/JSON_EXTENSION_SPEC_1_1_3.md
```

The short rule is:

```text
Use JSON for content.
Use Java API for reading and observing.
Do not hardcode compatibility tables in Java.
```

---

## 1. Which extension path do I need?

| Goal | Use | Reload? | Notes |
|---|---|---:|---|
| Give an existing food Herbcraft nutrition | `nutrition_profiles` JSON | yes | Also add `ingredients` if it should appear in 食材录. |
| Add a 食材录 entry | `ingredients` JSON | yes | Eating gates the entry. Non-vanilla internal entry IDs are namespace-aware. |
| Give an existing item 性味 / nature | `herb_natures` JSON | yes | If the item is ordinary food and also has nutrition + ingredients, it can enter 药食志 / food-nature bridge. |
| Add overuse / threshold side effects | `special_counters` JSON | yes | External counter IDs are dynamically persisted in `1.1.3`. |
| Add Codex rumor lines | `knowledge_rumors` JSON + lang | yes | Requires language keys. Keep rumors useful and not spammy. |
| Add tattered pages to loot | `codex_loot_injections` JSON | yes | Loot-table modification only; no full table replacement. |
| Make a non-food existing item edible | `herbs` JSON in a loaded addon/mod | **startup only** | Not a world datapack `/reload` feature. Changes FOOD/CONSUMABLE components. |
| Query Herbcraft data from Java | `HerbcraftDataAPI` | n/a | Read-only facade. |
| Observe Herbcraft actions from Java | `HerbcraftEvents` | n/a | Observational events only. |
| Register new Minecraft items | Your own Fabric mod | n/a | Herbcraft JSON does not create new items. |

---

## 2. The three common addon patterns

### Pattern A — Existing edible food gets nutrition only

Use when your food should affect Herbcraft nutrition but should not have herb pharmacology.

Files:

```text
data/my_compat/herbcraft/nutrition_profiles/my_food.json
data/my_compat/herbcraft/ingredients/my_food.json
```

Example:

```json
{
  "schema_version": 1,
  "item": "examplemod:rice_bowl",
  "values": {
    "grain": 24,
    "vege": 4
  }
}
```

```json
{
  "schema_version": 1,
  "item": "examplemod:rice_bowl",
  "section": "grain"
}
```

Expected result:

```text
Player eats food -> nutrition is applied -> 食材录 entry unlocks after eating.
```

### Pattern B — Existing edible food is “药食同源”

Use when the food is already edible and should participate in 性味 / 五味 / 七情 / 气机.

Files:

```text
data/my_compat/herbcraft/nutrition_profiles/my_food.json
data/my_compat/herbcraft/ingredients/my_food.json
data/my_compat/herbcraft/herb_natures/my_food.json
```

Example:

```json
{
  "schema_version": 1,
  "item": "examplemod:onion",
  "temperature": "warm",
  "flavors": ["pungent", "sweet"],
  "taste_flavors": ["pungent", "sweet"],
  "traits": ["stirring", "earthy"]
}
```

Expected result:

```text
Player eats food -> nutrition applies -> 食材录 unlocks -> 药食志 unlocks -> food-nature pharmacology bridge runs.
```

Important boundary:

```text
This does not override the target mod's FOOD/CONSUMABLE behavior.
```

### Pattern C — Existing non-food item becomes a Herbcraft edible

Use only from a loaded addon/mod resource, not an ordinary world datapack.

Path:

```text
data/my_addon/herbcraft/herbs/my_item.json
```

Example:

```json
{
  "schema_version": 1,
  "item": "examplemod:lavender",
  "nutrition": 1,
  "saturation_modifier": 0.1,
  "consume_seconds": 1.6,
  "medicinal": true,
  "stack_group": false,
  "effects": [
    { "effect": "minecraft:regeneration", "chance": 0.2, "amplifier": 0, "duration_seconds": 5, "positive": true }
  ]
}
```

Expected result:

```text
Loaded addon jar present at startup -> Herbcraft scans resource -> item default FOOD/CONSUMABLE components modified.
```

Do **not** expect this to work from `/reload`.

---

## 3. JSON paths at a glance

```text
data/<namespace>/herbcraft/herb_natures/*.json
data/<namespace>/herbcraft/nutrition_profiles/*.json
data/<namespace>/herbcraft/ingredients/*.json
data/<namespace>/herbcraft/special_counters/*.json
data/<namespace>/herbcraft/knowledge_rumors/*.json
data/<namespace>/herbcraft/codex_loot_injections/*.json
data/<namespace>/herbcraft/herbs/*.json              # startup-only loaded addon/mod resources
```

Full schemas are in:

```text
docs/JSON_EXTENSION_SPEC_1_1_3.md
```

---

## 4. Java read-only API

Class:

```java
com.herbcraft.api.HerbcraftDataAPI
```

Use it when your addon needs to ask what Herbcraft currently knows.

Common calls:

```java
HerbcraftDataAPI.isRegisteredItem(itemId);
HerbcraftDataAPI.nature(itemId);
HerbcraftDataAPI.nutrition(itemId);
HerbcraftDataAPI.hasIngredientEntry(itemId);
HerbcraftDataAPI.ingredientEntryId(itemId);
HerbcraftDataAPI.isHerb(item);
HerbcraftDataAPI.herb(item);
HerbcraftDataAPI.hasFoodNatureEntry(itemId);
HerbcraftDataAPI.foodNatureEntryId(itemId);
HerbcraftDataAPI.hasFoodNatureData(itemId);
```

Important caveat:

```text
Identifier-based nature/nutrition queries can return loaded metadata for optional target mods even when their item is absent.
If you need a real live item, check isRegisteredItem(...) or query by Item / ItemStack.
```

---

## 5. Java event API

Class:

```java
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

Events are observational:

```text
They do not cancel, replace, or mutate Herbcraft results.
```

If you need to add content, use JSON.

---

## 6. Examples

JSON datapack example:

```text
docs/examples/external_extension_compat_pack/
```

Java read/event example:

```text
docs/examples/herbcraft_readonly_event_addon/
```

Startup-only edible-item example:

```text
docs/examples/startup_herb_food_extension_mod/
```

P4 runtime test datapack:

```text
docs/compat_drafts/p4_runtime_datapack_test/
release_output/herbcraft_test_20260614_review/herbcraft-p4-runtime-test-datapack-20260617.zip
```

---

## 7. Common mistakes

### Mistake: putting non-edible items in 食材录

Do not add nutrition/ingredient rows for items the player cannot actually eat.

Bad example:

```text
farmer_mod:raw_seed_that_is_not_food
```

Reason:

```text
The player can never truly taste or master it.
```

Either remove it or deliberately design a startup-only `herbs` edible override.

### Mistake: hardcoding third-party content in Java

Do not write:

```java
if (id.equals("farmersdelight:tomato")) { ... }
```

Use JSON rows:

```text
nutrition_profiles
ingredients
herb_natures
```

### Mistake: expecting `/reload` to add food components

`/reload` can change Herbcraft metadata and rules. It cannot safely add FOOD/CONSUMABLE components to arbitrary items.

### Mistake: assuming datapack-driven pseudo-items are stable

If a datapack-style food pack rewrites vanilla item identity rather than registering stable new item IDs, Herbcraft compatibility may be unreliable. Reg's More Foods was excluded from the main compat for this reason.

---

## 8. Testing checklist

For a nutrition + ingredient entry:

```text
- /reload succeeds.
- Item can actually be eaten.
- Nutrition applies.
- 食材录 entry appears after eating.
- Tooltip respects knowledge gating.
```

For a food-nature entry:

```text
- /reload succeeds.
- Item can actually be eaten.
- Nutrition applies.
- 食材录 appears after eating.
- 药食志 appears after eating.
- Tooltip/Jade show nature after sync.
- Food-nature event fires if observed by addon.
- 五味/七情/气机 discoveries can be recorded.
```

For a special counter:

```text
- Counter ID is unique.
- Items are real registered item IDs.
- Eat enough times inside window.
- threshold_effects trigger.
- Save/reload does not crash.
```

For a startup-only herb food:

```text
- Resource is inside a loaded addon/mod jar.
- Item exists at startup.
- FOOD/CONSUMABLE appears after restart.
- World datapack /reload alone is not used for this.
```
