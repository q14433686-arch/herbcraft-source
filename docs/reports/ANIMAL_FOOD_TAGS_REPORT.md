# ANIMAL_FOOD_TAGS_REPORT

## Scope

Implemented `新功能：草类/植物类物品可用于喂养原版动物` for core only.

## Source verification notes

The task document said to use:

```text
data/herbcraft/tags/items/*.json
```

I checked the Minecraft `26.1.2` vanilla client resources directly. The actual vanilla animal food tags are under:

```text
data/minecraft/tags/item/*.json
```

Examples verified from vanilla `client-26.1.2.jar`:

```text
data/minecraft/tags/item/cow_food.json
data/minecraft/tags/item/sheep_food.json
data/minecraft/tags/item/goat_food.json
data/minecraft/tags/item/horse_tempt_items.json
```

Therefore the implementation appends to the `minecraft` namespace using `tags/item` singular. This is necessary for the vanilla `minecraft:*_food` and `minecraft:*_tempt_items` tags to be extended.

## Added files

```text
core/src/main/resources/data/minecraft/tags/item/cow_food.json
core/src/main/resources/data/minecraft/tags/item/sheep_food.json
core/src/main/resources/data/minecraft/tags/item/goat_food.json
core/src/main/resources/data/minecraft/tags/item/pig_food.json
core/src/main/resources/data/minecraft/tags/item/rabbit_food.json
core/src/main/resources/data/minecraft/tags/item/horse_tempt_items.json
core/src/main/resources/data/minecraft/tags/item/camel_food.json
core/src/main/resources/data/minecraft/tags/item/sniffer_food.json
```

All use:

```json
"replace": false
```

## Deliberate non-changes

No Java code was added or edited for this feature.
No Mixin was added.
No cuisine or alchemy files were needed.

The following tags were intentionally not touched:

```text
chicken_food, wolf_food, cat_food, fox_food, ocelot_food, frog_food,
hoglin_food, piglin_food, bee_food, parrot_poisonous_food, armadillo_food,
horse_food, llama_food, llama_tempt_items, happy_ghast_food,
happy_ghast_tempt_items
```

## Safety checks

Added:

```text
scripts/validate_animal_food_tags.py
```

It verifies:

- files are in the correct `data/minecraft/tags/item` path;
- no mistaken animal tag files exist in `tags/items` for this task;
- all target vanilla tags exist in Minecraft `26.1.2`;
- every file uses `replace=false`;
- all referenced item IDs exist in vanilla `26.1.2`;
- no forbidden toxic plants are included;
- no values duplicate vanilla tag contents;
- intentionally untouched animal tags were not created.

Result:

```text
PASS - animal food/tempt tags append to minecraft namespace with replace=false, valid items, no forbidden toxic items, no wrong tags/items path, and no vanilla duplicates.
```

## Build and validation

Build:

```text
BUILD SUCCESSFUL
```

Validation commands passed:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_panel_hodge_recipe.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/verify_built_jars.py
```

Core jar contains 8 new animal tag files:

```text
data/minecraft/tags/item/camel_food.json
data/minecraft/tags/item/cow_food.json
data/minecraft/tags/item/goat_food.json
data/minecraft/tags/item/horse_tempt_items.json
data/minecraft/tags/item/pig_food.json
data/minecraft/tags/item/rabbit_food.json
data/minecraft/tags/item/sheep_food.json
data/minecraft/tags/item/sniffer_food.json
```
