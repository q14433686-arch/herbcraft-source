# Minecraft 26.2 Fabric port — initial implementation

Date: 2026-07-19  
Branch: `arena/019f7953-herbcraft-source`  
Baseline: Minecraft `26.1.2` / Fabric Loader `0.19.3` / Fabric API `0.151.0+26.1.2`  
Target: Minecraft `26.2` / Fabric Loader `0.19.3` / Fabric API `0.155.2+26.2`

## Scope

This is the first implementation pass after the 26.2 investigation in `reference_cache/26_2_porting_notes.md`.

Applied changes:

- Minecraft target: `26.1.2` -> `26.2`;
- Fabric Loader remains `0.19.3`;
- Fabric API: `0.151.0+26.1.2` -> `0.155.2+26.2`;
- Fabric Loom: `1.16-SNAPSHOT` -> `1.17-SNAPSHOT`;
- Gradle Wrapper: `9.4.1` -> `9.5.1`;
- optional development dependencies moved to the 26.2 lines:
  - Jade `26.2.1+fabric`;
  - JEI `30.12.0.72`;
  - REI `26.2.820`;
  - Architectury `21.0.4`;
- Core, Alchemy, and Cuisine `fabric.mod.json` Minecraft/Fabric API constraints updated;
- the Core Jade dependency is now read from `jade_version` instead of being hardcoded in `build.gradle`;
- migrated the 26.2 GUI screen manager calls:
  - `Minecraft#setScreen` -> `Minecraft#gui#setScreen`;
  - direct `Minecraft#screen` access -> `Minecraft#gui#screen()`;
- updated the small set of active docs, examples, and vanilla-texture validator/generator references to target 26.2.

## Source files changed for the GUI migration

- `core/src/main/java/com/herbcraft/knowledge/client/HerbcraftCoreClient.java`;
- `core/src/main/java/com/herbcraft/knowledge/client/HerbNatureItemTooltips.java`;
- `core/src/main/java/com/herbcraft/nutrition/client/NutritionItemTooltips.java`.

The exact 26.2 `Gui#screen()` accessor was checked against current 26.2-targeted open-source code, including JEI's 26.2 GUI integration source. The official Fabric migration note explicitly documents the setter move to `Minecraft.getInstance().gui.setScreen(...)`.

## What was intentionally not changed

Static source audit found no current use of:

- `valueLookupBuilder` / custom data-generation registries;
- raw OpenGL calls;
- the new enum extension, tag removal, fluid interaction, or attended client-command APIs;
- recipe/trade/loot/component schemas that are specifically called out as changed by the 26.2 Fabric migration note.

No gameplay balance, item IDs, recipe IDs, trade IDs, component IDs, or serialized data formats were intentionally changed.

## Windows compile feedback and fix

The author ran `set JAVA_HOME=D:\JDK25` followed by `gradlew clean build` on Windows with Gradle 9.5.1/Loom 1.17.16. Core and Cuisine compiled; Alchemy failed at eight call sites because 26.2 official mappings expose the correctly spelled `MobEffect.isInstantaneous()` while the 26.1 source used the old misspelled `isInstantenous()` name.

Fixed in:

- `alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java`;
- `alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java`;
- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRuntime.java`;
- `alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java`.

The author should rerun `gradlew clean build` to reveal the next 26.2 compatibility layer, if any.

## Validation

Passed without requiring Minecraft/Gradle:

- `python3 scripts/check_lang_diff.py`;
- `python3 scripts/validate_trade_tags.py`;
- `python3 scripts/validate_recipe_isolation.py`;
- `python3 scripts/validate_coating_viewer_plugins.py`;
- `git diff --check` for tracked changes.

Not yet run:

- `:core:compileJava`, `:alchemy:compileJava`, `:cuisine:compileJava`;
- full Gradle `build`;
- client/server smoke tests;
- actual JEI/REI/Jade runtime checks.

The sandbox currently has no Java 25 executable, and the existing Temurin setup helper could not retrieve its Adoptium metadata endpoint. The next validation pass must run with a working Java 25 installation and should prioritize dependency resolution, the three viewer adapters, Mixin application, and the client GUI open/close paths.

References:

- Fabric 26.2 porting note: <https://fabricmc.net/2026/06/15/262.html>
- Fabric API 26.2 versions: <https://modrinth.com/mod/fabric-api/versions?g=26.2>
- Minecraft 26.2 version metadata: <https://piston-meta.mojang.com/v1/packages/9e272020887b72a23b1a525c5d8e74d2d7aa8222/26.2.json>
