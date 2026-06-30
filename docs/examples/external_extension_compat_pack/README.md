# Herbcraft JSON Extension Example Datapack

This documentation example follows the P4.1 formal JSON spec:

```text
docs/JSON_EXTENSION_SPEC_1_1_3.md
```

It is intentionally tiny and uses vanilla item IDs so the source-tree validator can check it without depending on another mod.

Demonstrated reload-safe paths:

```text
data/example_compat/herbcraft/herb_natures/allium.json
data/example_compat/herbcraft/nutrition_profiles/honey_bottle.json
data/example_compat/herbcraft/ingredients/honey_bottle.json
data/example_compat/herbcraft/knowledge_rumors/allium.json
data/example_compat/herbcraft/codex_loot_injections/village_temple.json
data/example_compat/herbcraft/special_counters/sweet_overuse.json
```

What it demonstrates:

- `minecraft:allium` receives Herbcraft nature metadata.
- `minecraft:honey_bottle` receives a tiny nutrition profile and 食材录 entry.
- A rumor line is attached to the existing `herbal/allium` Codex entry.
- A village temple loot table can receive a Herbcraft Codex page.
- Repeated honey-bottle use can trigger a small overuse counter.

Boundary reminders:

- This datapack does not register new Minecraft items.
- This datapack does not make a non-food item edible.
- Use `data/<namespace>/herbcraft/herbs/*.json` only from a loaded addon/mod at startup when you need to alter FOOD/CONSUMABLE components.
