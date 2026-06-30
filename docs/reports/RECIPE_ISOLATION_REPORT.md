# RECIPE_ISOLATION_REPORT

## Scope

Applied `配方冲突回避规则` as a patch on top of the previous Hundred-Flower Feast / Deadly Hodgepodge work.

## Key decisions based on actual source

- `HodgepodgeRecipe` already required `minecraft:bowl`; this was preserved and explicitly validated.
- Existing raw medicinal recipes already used `herbcraft_cuisine:water_bowl`; validated all raw recipes and fixed the hidden dishes to follow the same route.
- Hidden dishes were changed from direct final shapeless recipes to:
  - `water_bowl + specific ingredients -> raw_*`
  - `raw_* -> cooked final dish` by smelting/smoking/campfire.
- Direct final crafting recipes for `hundred_flower_feast` and `deadly_hodgepodge` were removed.
- `#herbcraft:herb` was cleaned so mushrooms are not accepted by hodgepodge.

## Files changed

```text
cuisine/src/main/java/com/herbcraft/cuisine/recipe/HodgepodgeRecipe.java
core/src/main/resources/data/herbcraft/tags/item/herb.json
cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/raw_hundred_flower_feast.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/raw_deadly_hodgepodge.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/hundred_flower_feast_from_smelting.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/hundred_flower_feast_from_smoking.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/hundred_flower_feast_from_campfire.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/deadly_hodgepodge_from_smelting.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/deadly_hodgepodge_from_smoking.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/deadly_hodgepodge_from_campfire.json
```

## Hodgepodge isolation behavior

`HodgepodgeRecipe` now:

- accepts only `minecraft:bowl` as the container;
- rejects `water_bowl` because any non-empty-bowl item that is not a herb fails analysis;
- rejects red/brown mushrooms to preserve vanilla suspicious stew behavior;
- accepts normal hodgepodge as empty bowl + 3~4 herbs;
- accepts the two hidden ingredient sets with empty bowl as hodgepodge, not hidden dishes;
- lets water-bowl fixed recipes own hidden dishes and medicinal raw dishes.

## Validation

Added:

```text
scripts/validate_recipe_isolation.py
```

Result:

```text
All recipe isolation checks passed.
```

Full build:

```text
BUILD SUCCESSFUL
```

Full validation run passed:

```bash
python3 scripts/validate_recipe_isolation.py
python3 scripts/check_lang_diff.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_panel_hodge_recipe.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_extended_features.py
python3 scripts/verify_built_jars.py
```

## Matrix covered

- `minecraft:bowl + dandelion x3 -> hodgepodge`.
- `water_bowl + random herbs -> no hodgepodge`.
- `minecraft:bowl + hidden feast/deadly ingredients -> hodgepodge path`.
- `water_bowl + hidden feast/deadly ingredients -> raw hidden dish`.
- `minecraft:bowl + red mushroom + brown mushroom + flower -> vanilla suspicious stew path, not hodgepodge`.
