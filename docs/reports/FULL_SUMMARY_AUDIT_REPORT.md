# FULL_SUMMARY_AUDIT_REPORT

## Scope

The project was audited and reconstructed against `docs/PROJECT_SUMMARY.md` after a stale source/build rollback caused hidden cuisine content and other cumulative changes to disappear from the produced jars.

## Confirmed restored / present

### Core

- `herbs.json` current values, including:
  - `minecraft:glistering_melon_slice`: nutrition `4`, saturation `9.6`, guaranteed instant health + absorption.
  - `minecraft:golden_carrot`: vanilla food values + 10s night vision.
- Effect immunity storage/API/mixin.
- Herb stack threshold/fade/overload overlay messages.
- Animal food/tempt tag additions.
- `#herbcraft:herb` excludes red/brown mushrooms and mushroom blocks/stem.
- Herbcraft overview/codex hint translations remain present.
- Core advancement files: 16.

### Cuisine

- Hundred-Flower Feast / Deadly Hodgepodge full item stack restored:
  - final items;
  - raw items;
  - item definitions;
  - item models;
  - textures;
  - lang keys;
  - raw water-bowl crafting recipes;
  - smelting/smoking/campfire routes.
- Recipe isolation restored:
  - hodgepodge uses empty bowl only;
  - medicinal and hidden raw dishes use water bowl;
  - direct final hidden dish shapeless recipes removed;
  - empty bowl + hidden ingredients resolves through hodgepodge route, not hidden-dish route.
- Cuisine advancement files: 6.

### Alchemy

- Advanced potion quality/bonus system present.
- Witch loot integration restored.
- Safe witch-only murky variants restored.
- Witch potion replacement mixin restored.
- Hidden cuisine catalyst brewing restored:
  - Hundred-Flower Feast upgrades `_clarified` potions to `EXCEPTIONAL` quality.
  - Deadly Hodgepodge upgrades `_murky` potions to `EXCEPTIONAL` quality.
  - Input `ItemStack` is copied and only `herbcraft_alchemy:potion_quality` is overwritten, preserving existing potion components.
- Alchemy advancement files: 9.

## Validation commands passed

```bash
./gradlew build --rerun-tasks
python3 scripts/validate_recipe_isolation.py
python3 scripts/check_lang_diff.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_extended_features.py
python3 scripts/verify_built_jars.py
```

## Jar spot checks

- `herbcraft-cuisine.jar` contains all `hundred_flower_feast`, `raw_hundred_flower_feast`, `deadly_hodgepodge`, and `raw_deadly_hodgepodge` assets/recipes.
- `herbcraft-alchemy.jar` contains `WitchHerbcraftPotionMixin`, `BrewingStandCatalystMixin`, and `HiddenCuisineCatalystBrewing`.
- `herbcraft.jar` contains 8 animal food/tempt tag additions.

## Remaining caveat

Advancement files are present and syntactically build-valid. Some specialized progression hooks remain lightweight/manual and should be refined in a future dedicated advancement pass if exact trigger semantics are required for every single listed advancement.
