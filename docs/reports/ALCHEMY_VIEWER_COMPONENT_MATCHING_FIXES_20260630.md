# Alchemy JEI/REI viewer component matching and correlated display fixes — 2026-06-30

## Summary

This pass fixes the user-reported JEI/REI recipe-viewer issues around distilled essence brewing, processed-potion lookup, and smithing-table attachment examples.

## Fixed

- JEI brewing displays now use the correct brewing-factory argument order:
  - top ingredient slot: catalyst or distilled essence;
  - bottom brewing slots: raw essence item or potion bottle input;
  - output slot: distilled essence or processed potion.
- JEI distilled-essence creation recipes now use the list/list brewing overload so non-potion bottom inputs are represented explicitly instead of being treated like a vanilla potion input.
- JEI registers subtype interpreters for:
  - distilled essence by its `DISTILLED_ESSENCE` component (`sourceEssence|orientation`);
  - potion bottles by normalized potion path plus processing style/source metadata.
- REI adds a `rei_common` entrypoint (`CoatingReiCommonPlugin`) with component-aware comparators for:
  - distilled essence source/orientation;
  - potion path plus processing style/source metadata.
- JEI and REI processed-potion recipe outputs now initialize the display stack through `AdvancedPotionStackInitializer.initializeIfNeeded(..., RandomSource.create(0L))`, matching the runtime rewritten processed-potion stack shape used by in-game lookup targets.
- JEI smithing displays for distilled-essence coating/armor attachment now use display-only recipe holders with fixed template/base/addition/output samples, avoiding independently rotating input/output lists.
- REI smithing displays for processed coating apply and armor support apply are split per base item, so the output stack corresponds to the displayed input base and distilled essence.

## Files changed

- `alchemy/src/main/java/com/herbcraft/alchemy/compat/jei/CoatingJeiPlugin.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/compat/rei/CoatingReiPlugin.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/compat/rei/CoatingReiCommonPlugin.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingSmithingRecipe.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportSmithingRecipe.java`
- `alchemy/src/main/resources/fabric.mod.json`
- `scripts/validate_coating_viewer_plugins.py`
- `scripts/verify_built_jars.py`

## Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' :alchemy:compileJava --stacktrace` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_coating_viewer_plugins.py` — PASS.

## Client-only validation still required

These changes compile and are guarded by source validators, but JEI/REI UI behavior must still be checked in a real client with JEI and/or REI installed:

1. JEI distilled essence recipe page should no longer show `?` as the step and should show raw essence in the bottom brewing slots and honeycomb/redstone/glowstone catalyst in the top ingredient slot.
2. JEI processed-potion brewing pages should show potion bottle input at the bottom and distilled essence in the top ingredient slot.
3. JEI and REI smithing-table distilled-essence attachment examples should keep base and output samples correlated while cycling.
4. Looking up recipes/usages from an initialized processed potion should jump to the matching processed-potion brewing recipe instead of doing nothing or opening an uncraftable potion page.
5. Looking up from one distilled essence should match only recipes for that source/orientation instead of all distilled essences.

## Follow-up: JEI distilled-essence overmatching fix

After client testing showed REI was fixed but JEI still matched every source essence when looking up from one distilled essence, the JEI brewing path was changed again:

- Removed JEI brewing registration through the vanilla recipe factory / vanilla brewing recipe table for Herbcraft distilled-essence routes. That path still allowed item-fuzzy matching for the shared `distilled_essence` item.
- Added a `ComponentAwareBrewingRecipeManagerPlugin` through `registerAdvanced(...)` for `RecipeTypes.BREWING`.
- Added a Herbcraft `IJeiBrewingRecipe` implementation with copied stack inputs/outputs and `getBrewingSteps() == 1`.
- The advanced plugin now filters brewing recipes with Herbcraft's component-aware equality:
  - distilled essence: exact `sourceEssence + orientation`;
  - potion bottles: normalized potion path + style/source;
  - other stacks: normal item + component equality.

This should make JEI recipe lookup from one distilled essence return only recipes using that same source/orientation, while keeping the vanilla brewing UI category and the corrected slot order.
