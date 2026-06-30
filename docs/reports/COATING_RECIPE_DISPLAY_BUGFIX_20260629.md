# Coating Recipe Display Bugfix

Date: 2026-06-29  
Version line: `1.1.3`

## Problem

Client report: weapon coatings (武器淬层) could be crafted with the smithing table, and the CurseForge page showed the smithing-table flow, but recipe viewers such as JEI/REI could not find/search the coating recipes.

## Root cause

`CoatingSmithingRecipe` is a custom smithing recipe serialized as `herbcraft:coating_smithing` with only a compact JSON mode field:

- `data/herbcraft/recipe/coating_apply.json`
- `data/herbcraft/recipe/coating_refine.json`

The recipe correctly implemented `matches(...)`, `assemble(...)`, placement info, and ingredients, so the smithing-table mechanic worked. However, it inherited the default `Recipe.display()` implementation, which returns an empty list. In Minecraft 26.1.x, recipe book/viewer integrations consume recipe display entries (`RecipeDisplay` / `SmithingRecipeDisplay`) for discoverable/searchable visual recipes. Therefore the custom special recipe was functionally valid but invisible to display-driven clients.

Follow-up after client report "not fixed": the first source-side fix was incomplete. Although display entries were added, `CoatingSmithingRecipe.isSpecial()` still returned `true`. JEI/REI-style integrations commonly filter special/custom recipes from ordinary searchable recipe categories even when display metadata exists. The second fix now marks coating smithing recipes as non-special while retaining custom `matches(...)` / `assemble(...)` behavior.

## Fix

Updated `alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingSmithingRecipe.java` to override `display()` and emit `SmithingRecipeDisplay` entries:

- `isSpecial()` now returns `false`, so default smithing recipe categories in JEI/REI are less likely to discard the recipe before indexing. This does not remove the custom serializer or custom matching logic; it only changes discoverability classification.

- Apply mode now emits one smithing-table display per data-backed essence:
  - template: `herbcraft:blank_adhesive`
  - base: `#herbcraft:coatable_weapons`
  - addition: the essence item
  - result: representative coated iron sword with the real `WEAPON_COATING` component applied
- Refine mode now emits entries per eligible essence/effect side:
  - `herbcraft:amethyst_adhesive` for positive refinement when the essence has positive effects
  - `herbcraft:echo_adhesive` for negative refinement when the essence has negative effects
  - base/result display stacks preserve coating components through `ItemStackTemplate.fromNonEmptyStack(...)`
  - addition slot remains empty, matching the runtime smithing recipe
- Added `AlchemyRegistries.essenceInfos()` as a read-only immutable-copy accessor so displays remain generated from the JSON-backed essence registry instead of hardcoded Java item tables.

The representative display weapon is `minecraft:iron_sword`; this affects only recipe-viewer output and does not change runtime matching, which still accepts all items in `#herbcraft:coatable_weapons`.

## Architecture notes

- Java change is limited to UI/display metadata for an existing custom recipe mechanic.
- Content remains data-backed: all essence-specific display rows are derived from `data/herbcraft_alchemy/essences.json` via `AlchemyRegistries`.
- No third-party recipe-viewer-specific hard dependency was added; the fix uses vanilla 26.1.x recipe display APIs and should be consumed by JEI/REI-style integrations that read recipe displays.

## Files changed

- `alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingSmithingRecipe.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java`
- `scripts/validate_coating_recipe_display.py`
- Documentation/baseline/changelog files for this handoff.

## Validation

Automated validations executed:

- `bash scripts/setup_jdk25_temurin.sh` — PASS; JDK 25 Temurin provisioned in this sandbox.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew :alchemy:compileJava --stacktrace` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/validate_coating_recipe_display.py` — PASS, including guard that `isSpecial()` returns `false`.
- `python3 scripts/verify_built_jars.py` — PASS.

Release output refreshed:

- `release_output/herbcraft_test_20260614_review/herbcraft-1.1.3.jar`
- `release_output/herbcraft_test_20260614_review/herbcraft-alchemy-1.1.3.jar`
- `release_output/herbcraft_test_20260614_review/herbcraft-cuisine-1.1.3.jar`
- `release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip`

## Client-only checks still required

The following must be confirmed in a real client because the sandbox cannot launch JEI/REI/CurseForge UI integrations:

1. With Core + Alchemy + JEI or REI installed, search for `淬层`, `coating`, an essence item, `Blank Adhesive`, `Amethyst Adhesive`, and `Echo Adhesive`.
2. Confirm coating apply recipes appear as smithing-table recipes.
3. Confirm amethyst/echo refinement recipes appear only for essences with matching positive/negative effects.
4. Confirm clicking displayed recipes still corresponds to the actual smithing-table flow:
   - blank adhesive + coatable weapon + essence;
   - amethyst/echo adhesive + fully coated weapon + empty addition slot.
5. Confirm runtime crafting and hit effects are unchanged.

## Second follow-up: explicit JEI/REI plugins

After the user reported that both vanilla `RecipeDisplay` and `isSpecial() == false` still did not fix the live client, the fix was expanded from passive vanilla metadata to explicit recipe-viewer integration.

Added JEI integration:

- `alchemy/src/main/java/com/herbcraft/alchemy/compat/jei/CoatingJeiPlugin.java`
- Annotated with `@JeiPlugin` and implements `IModPlugin`.
- Registers a smithing category extension for `CoatingSmithingRecipe`.
- Explicitly adds `coating_apply` and `coating_refine` recipe holders into JEI `RecipeTypes.SMITHING`.
- Supplies template/base/addition/output slots directly, including real coated output stacks.

Added REI integration:

- `alchemy/src/main/java/com/herbcraft/alchemy/compat/rei/CoatingReiPlugin.java`
- Declared as a `rei_client` entrypoint in `alchemy/src/main/resources/fabric.mod.json`.
- Explicitly registers `DefaultSmithingDisplay` entries for every JSON-backed essence apply/refine case.
- Uses built-in REI smithing display category with transform-style displays.

Build/dependency notes:

- Added compile-only viewer API dependencies only:
  - JEI common/fabric API `29.6.2.38`
  - REI API/default plugin `26.1.819`
  - Architectury Fabric `20.0.6`, required by REI API types
- JEI/REI remain optional and are not hard dependencies in `fabric.mod.json`.
- The built Alchemy jar contains only Herbcraft plugin classes, not JEI/REI/Architectury classes.

Additional validation:

- `python3 scripts/validate_coating_viewer_plugins.py` — PASS.
- `python3 scripts/verify_built_jars.py` now checks the JEI/REI plugin classes are present in the Alchemy jar.
- Jar listing confirmed `CoatingJeiPlugin.class` and `CoatingReiPlugin.class` are present, with no bundled `mezz/jei`, `shedaniel`, or `architectury` package classes.

## JEI plugin discovery correction

After REI was confirmed visible but JEI was still missing, JEI Fabric plugin discovery was inspected directly. `mezz.jei.fabric.startup.FabricPluginFinder` loads plugins from the Fabric entrypoint key:

```text
jei_mod_plugin
```

Therefore `@JeiPlugin` alone was insufficient for this Fabric JEI line. The Alchemy `fabric.mod.json` now declares:

```json
"jei_mod_plugin": [
  "com.herbcraft.alchemy.compat.jei.CoatingJeiPlugin"
]
```

This is the source-level reason JEI did not load the previously added plugin. `scripts/validate_coating_viewer_plugins.py` now guards both viewer entrypoints:

- `jei_mod_plugin` -> `CoatingJeiPlugin`
- `rei_client` -> `CoatingReiPlugin`

Architecture note: explicit viewer plugins remain within the Java responsibility boundary because they are UI/registration/adapter projections of existing mechanics. They do not define balancing/content tables; coating display rows are generated from the JSON-backed `AlchemyRegistries.essenceInfos()` data source.

## REI display parity follow-up: coatable weapon tag expansion

After REI was confirmed working, the remaining UX issue was that REI showed only an iron sword example while JEI exposed the broader coatable weapon ingredient. This was not a runtime bug; it was caused by the first REI adapter using `Items.IRON_SWORD` as a representative sample.

Updated `CoatingReiPlugin` so REI now expands the actual coatable weapon tag:

```java
BuiltInRegistries.ITEM.getTagOrEmpty(CoatingSystem.COATABLE_WEAPONS)
```

Effects:

- Apply displays show all tag-listed coatable weapons as base inputs.
- Apply outputs show corresponding coated variants for each coatable weapon.
- Refine displays show all fully coated weapon variants as base inputs.
- Refine outputs show corresponding positive/negative refined variants for each coatable weapon.
- `minecraft:iron_sword` remains only as a defensive fallback if the tag is unexpectedly empty.

This keeps the architecture rule intact: Java owns UI adapter projection, while the weapon compatibility set remains tag/data driven and is not hardcoded as a Java content table.
