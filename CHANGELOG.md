# CHANGELOG

## 2026-07-19 Minecraft 26.2 Fabric port — initial pass

### Changed

- Updated the development target from Minecraft `26.1.2` to `26.2`.
- Kept Fabric Loader at `0.19.3`; updated Fabric API to `0.155.2+26.2`.
- Updated Fabric Loom to `1.17-SNAPSHOT` and the Gradle Wrapper to `9.5.1`.
- Updated optional Jade, JEI, REI, and Architectury development dependencies to their 26.2 lines.
- Migrated Core client screen access from `Minecraft.screen` / `Minecraft.setScreen` to `Minecraft.gui.screen()` / `Minecraft.gui.setScreen(...)`.
- Updated active docs, example manifests, and vanilla-template validation/generation paths for 26.2.

### Validation

- `scripts/check_lang_diff.py` — PASS.
- `scripts/validate_trade_tags.py` — PASS.
- `scripts/validate_recipe_isolation.py` — PASS.
- `scripts/validate_coating_viewer_plugins.py` — PASS.
- Java 25 Gradle compilation and client/server smoke tests — pending; the sandbox currently has no working Java 25 executable.

### Reports

- Investigation: `reference_cache/26_2_porting_notes.md`.
- Implementation: `docs/reports/PORTING_26_2_FABRIC_20260719.md`.

## 2026-06-30 Core and Alchemy 1.1.4 version bump

### Changed

- Bumped Core (`herbcraft`) from `1.1.3` to `1.1.4`.
- Bumped Alchemy (`herbcraft_alchemy`) from `1.1.3` to `1.1.4`.
- Updated Alchemy's Core dependency from `>=1.1.3` to `>=1.1.4`.
- Kept Cuisine (`herbcraft_cuisine`) at `1.1.3` as requested. Cuisine still depends on Core `>=1.1.3` and remains compatible with Core `1.1.4`.
- Updated `scripts/verify_built_jars.py` for per-module version expectations.
- Updated current release-facing docs and added `docs/RELEASE_PAGE_COPY_1.1.4.md`, `docs/MCMOD_RELEASE_COPY_1.1.4.md`, `docs/JSON_EXTENSION_SPEC_1_1_4.md`, `docs/ADDON_AUTHOR_QUICKSTART_1_1_4.md`, and report `docs/reports/VERSION_BUMP_CORE_ALCHEMY_1_1_4_20260630.md`.

### Boundary

- Version/documentation/package pass only. No intentional gameplay mechanics, JSON balance/rules, recipe, trade, loot, advancement, lang, texture, or model changes were made.

### Validation

- Full Gradle build with Java 25 — PASS.
- `scripts/check_lang_diff.py` — PASS.
- `scripts/verify_built_jars.py` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.4.jar`, `herbcraft-alchemy-1.1.4.jar`, retained/rebuilt `herbcraft-cuisine-1.1.3.jar`, and regenerated `herbcraft-source-current-20260614.zip`.
- Removed stale staged Core/Alchemy `1.1.3` jars from the current release-output directory to avoid accidental upload confusion.
- Kept `herbcraft-p4-runtime-test-datapack-20260617.zip` unchanged.

## 2026-06-30 Alchemy trade, loot, and potion-arrow adaptation

### Added

- Added trade coverage for newer Alchemy outputs: sustained/intense/T3 potions, processed potion arrows, distilled essence samples, and armor-support armor samples.
- Added `data/herbcraft_alchemy/alchemy_loot_injections.json` for bundled Alchemy item injections into selected vanilla loot tables.
- Added `data/herbcraft/recipe/advanced_tipped_arrow.json`, reusing vanilla `minecraft:crafting_imbue` for lingering-potion + arrows -> tipped arrows.
- Added `AlchemyPotionArrowMixin` so Herbcraft potion arrows initialize processed potion metadata before hit effects and then apply taste/progression/burden handling on actual player hit exposure.
- Added validator `scripts/validate_alchemy_trade_loot_arrow_adaptation.py`.
- Added report `docs/reports/ALCHEMY_TRADE_LOOT_ARROW_ADAPTATION_20260630.md`.

### Changed

- `AlchemyCodexHooks` now also loads bundled Alchemy item-loot injection rules and injects potions, tipped arrows, distilled essences, and armor-support stacks through the existing loot v3 modify path.
- `AlchemyKnowledgeSupport.isPotionContainer(...)` now includes `minecraft:tipped_arrow`, so arrow exposure uses the same taste/progression path as drinking, splash, and lingering exposure.
- Updated villager trade tags with `replace=false` references for all new trade JSON.

### Validation

- Full Gradle build with Java 25 — PASS.
- `scripts/validate_alchemy_trade_loot_arrow_adaptation.py` — PASS.
- `scripts/validate_trade_tags.py` — PASS.
- `scripts/validate_bugfix_advanced_potion_trade.py` — PASS.
- `scripts/validate_alchemy_advancement_and_sync.py` — PASS.
- `scripts/validate_potion_processing_rules.py` — PASS.
- `scripts/validate_potion_burden_runtime.py` — PASS.
- `scripts/validate_extended_features.py` — PASS.
- `scripts/check_lang_diff.py` — PASS.
- `scripts/verify_built_jars.py` — PASS.

### Client-only

- Verify new villager trades, chest injections, and potion-arrow hit behavior in a live client/world.

## 2026-06-30 Alchemy advancement progression and sync audit

### Added

- Added four Alchemy advancements for newer progression states: `first_distilled_essence`, `first_sustained`, `first_intense`, and `first_armor_support`.
- Added localized advancement text and an armor-support guide completion line in English and Simplified Chinese.
- Added `scripts/validate_alchemy_advancement_and_sync.py` to guard narrowed progression hooks and Alchemy component sync assumptions.
- Added report `docs/reports/ALCHEMY_ADVANCEMENT_AND_SYNC_AUDIT_20260630.md`.

### Changed

- Narrowed Alchemy potion progression so inventory scanning no longer grants potion tasting/use milestones. Inventory scan now only grants concrete possession milestones and seen/heard knowledge.
- Moved `first_potion`, processing-style milestones, witch/undead milestones, and exceptional-quality potion milestone to the actual taste/exposure path used by drinking, splash, and lingering cloud hooks.
- Normalized special potion source paths (`undead_venom`, `cardiac_toxin`, `wither_venom_iii`, `witch_*`) for knowledge/progression lookup.
- Updated Alchemy guide gating to account for distilled essence, sustained/intense processing, and armor support.
- Updated the advancement/lang rebuild helper so future regeneration preserves the expanded 13-file Alchemy advancement tree and does not recreate broad witch inventory progression.
- Updated validator expectations for 35 total advancements and 13 Alchemy advancements.

### Data-sync audit

- Confirmed Alchemy potion, distilled-essence, and armor-support item stack components are persistent and network synchronized.
- Confirmed Alchemy processing/support/coating/distillation rule tables are currently bundled/startup mod-jar rules, not hot-reload/server-synced datapack rules.
- Documented current requirement: client and server should run the same Alchemy jar for tooltip/viewer rule-text consistency. A dedicated Alchemy rule sync payload remains future work if these rule tables become server-overridable.

### Validation

- Full Gradle build with Java 25 — PASS.
- Full `scripts/validate_*.py` suite — PASS.
- `scripts/check_lang_diff.py` — PASS.
- `scripts/verify_built_jars.py` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, regenerated `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.

### Client-only

- Verify in a live client that holding processed potions no longer grants taste/use advancements, while drinking/splash/lingering exposure still grants the correct milestones.

## 2026-06-30 JEI distilled-essence exact lookup follow-up

### Changed

- Replaced JEI vanilla brewing-factory registration for Herbcraft distilled-essence routes with a component-aware advanced brewing recipe manager plugin.
- JEI brewing lookup now filters distilled essence by exact `sourceEssence|orientation`, preventing one distilled essence from matching all source essences.
- Updated viewer validator and jar verification expectations for the custom JEI brewing recipe/plugin classes.

### Validation

- Full Gradle build with Java 25 — PASS.
- `scripts/validate_coating_viewer_plugins.py` — PASS.
- `scripts/verify_built_jars.py` — PASS.

### Client-only

- Re-test JEI: looking up from one distilled essence should now show only recipes using that exact source/orientation.

## 2026-06-30 Alchemy JEI/REI viewer component matching fixes

### Added

- Added REI common plugin entrypoint `CoatingReiCommonPlugin` for component-aware item comparators.
- Added JEI subtype interpreters for distilled essence and processed potion bottles.
- Added report `docs/reports/ALCHEMY_VIEWER_COMPONENT_MATCHING_FIXES_20260630.md`.

### Changed

- Fixed JEI brewing display argument order so catalysts/distilled essence are shown in the top brewing ingredient slot and essence/potion inputs are shown in the bottom slots.
- Initialized processed-potion viewer outputs to match runtime processed stack metadata for recipe lookup.
- Split/correlated JEI and REI smithing display samples for distilled-essence coating and armor-support attachment so output samples follow input samples.
- Updated viewer validators and jar verification expectations for the new component-aware integration.

### Validation

- `:alchemy:compileJava` — PASS.
- Full Gradle build with Java 25 — PASS.
- `scripts/validate_coating_viewer_plugins.py` — PASS.

### Client-only

- Still verify actual JEI/REI UI navigation and cycling behavior in a live client with the relevant viewer installed.

## 2026-06-30 Alchemy burden profile consequences and armor support specialization

### Added

- Added JSON-backed `burden_consequence_profiles` to `potion_processing_rules.json` for profile-specialized overburden outcomes: `steady`, `warming`, `chilling`, `overdriven`, `muddy`, `scattering`, and `stagnating`.
- Added new Alchemy burden effects: `potion_heat_stir`, `potion_cold_stagnation`, `potion_overdrive_rebound`, `potion_turbid_disturbance`, `potion_qi_scattering`, and `potion_qi_stagnation`.
- Added lang keys and 18x18 icons for the new effects.
- Added `POTION_BURDEN_BUFFER_DETAILED` and `PotionBurdenBufferResult` for split active/tail buffering, consequence mitigation, and tail-decay support while keeping the existing simple `POTION_BURDEN_BUFFER` event.
- Added validator `scripts/validate_alchemy_burden_profile_specialization.py`.
- Added report `docs/reports/ALCHEMY_BURDEN_PROFILE_AND_ARMOR_SPECIALIZATION_20260630.md`.

### Changed

- `PotionBurdenConsequenceRuntime` now uses both total burden stage and the player's remembered dominant medicinal profile to choose consequence effects/messages.
- `PlayerPotionBurdenState` now stores the last processed-potion semantic burden context: dominant profile, burden profile, thermal tendency, qi tendency, consequence duration multiplier, and tail-decay bonus.
- `PotionBurdenRuntime` combines legacy simple buffer listeners and detailed active/tail buffer listeners before applying active and tail burden.
- `armor_support_rules.json` now gives `clear_bearing`, `steady_bearing`, and `braced_bearing` distinct support roles through split active/tail buffers, matching style/profile bonuses, consequence mitigation, and tail-decay support.
- `ArmorSupportRuntime` now uses the detailed buffer event and checks potion style, burden profile, and medicinal profile for matching support.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- Current validator suite listed in `docs/reports/ALCHEMY_BURDEN_PROFILE_AND_ARMOR_SPECIALIZATION_20260630.md` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, regenerated `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.

### Client-only

- Confirm live-client feel for profile-specific consequences and clear/steady/braced armor support specialization.


## 2026-06-30 Arena environment setup and validator sync

### Changed

- Completed a fresh Arena.ai workspace environment setup for the current `1.1.3` line.
- Read `AGENT_BRIEF.md`, regenerated `docs/reports/CURRENT_AGENT_BRIEF.md`, and re-read the required handoff anchors.
- Restored `gradlew` execute permission and provisioned Temurin JDK 25 at `/tmp/herbcraft-jdk25/jdk-25`.
- Added report `docs/reports/ENVIRONMENT_SETUP_ARENA_20260630.md`.
- Refreshed validator expectations that had become stale relative to current source/docs:
  - `scripts/validate_externalized_rules.py` now includes current `guide_alchemy_armor_support` in the Alchemy guide set.
  - `scripts/validate_potion_burden_runtime.py` now reflects the compact tooltip / mastered-Codex migration for verbose burden semantics.
  - `scripts/validate_panel_hodge_recipe.py` now checks data-driven Codex theme translucency and JSON-backed hodgepodge herb-count rules instead of old Java literals.

### Validation

- `python3 scripts/generate_agent_brief.py --write` — PASS.
- `chmod +x gradlew` — PASS.
- `bash scripts/setup_jdk25_temurin.sh` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- Current validator suite listed in `docs/reports/ENVIRONMENT_SETUP_ARENA_20260630.md` — PASS.

### Output

- Recreated/refreshed `release_output/herbcraft_test_20260614_review/` with `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, `herbcraft-source-current-20260614.zip`, and `herbcraft-p4-runtime-test-datapack-20260617.zip`.

### Client-only

- Live-client JEI/REI checks, processed-potion tooltip/Codex readability, splash/lingering exposure behavior, and UI interaction checks remain client-only.


## 2026-06-30 Alchemy JEI/REI viewer adapters for new recipes

### Added

- Expanded the existing optional JEI plugin `CoatingJeiPlugin` into an Alchemy viewer adapter while keeping the existing `jei_mod_plugin` entrypoint path.
- Added JEI brewing displays for essence + orientation catalyst -> componentized distilled essence.
- Added JEI brewing displays for base Herbcraft potion + matching distilled essence -> `_clarified`, `_long`, `_strong`, and special `wither_venom_iii` outputs, including normal, splash, and lingering potion containers.
- Added JEI smithing displays for armor support apply/refine routes.
- Expanded processed coating JEI samples so componentized distilled-essence coating variants are visible.
- Expanded the existing optional REI plugin `CoatingReiPlugin` with built-in brewing and smithing displays for distillation, processed potions, armor support, and processed coating variants.
- Added report `docs/reports/ALCHEMY_VIEWER_ADAPTERS_20260630.md`.

### Changed

- `scripts/validate_coating_viewer_plugins.py` now validates the broader Alchemy JEI/REI adapters, including Brewing Stand routes, armor support, splash/lingering processed-potion displays, processed coatings, optional entrypoints, and compile-only viewer dependencies.
- Confirmed current Fabric 26.1.2 viewer dependency shape: JEI/REI remain compile-only, REI default-plugin API and Architectury remain compile-only support dependencies, and `fabric.mod.json` still does not hard-depend on either viewer.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' :alchemy:compileJava --stacktrace` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_coating_viewer_plugins.py` — PASS.
- `python3 scripts/validate_distillation_brewing_routing.py` — PASS.
- `python3 scripts/validate_armor_support_foundation.py` — PASS.
- `python3 scripts/validate_processed_coating_expansion.py` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/validate_potion_body_state_projection.py` — PASS.
- `python3 scripts/validate_potion_relation_and_catalyst_semantics.py` — PASS.
- `python3 scripts/validate_alchemy_knowledge_gating_and_codex.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, regenerated `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.

### Client-only

- Confirm with JEI installed alone and REI installed alone that optional entrypoint loading succeeds and the Brewing/Smithing categories show the new Alchemy routes.

## 2026-06-30 Alchemy knowledge-gating and Codex design fixes

### Added

- Added `AlchemyKnowledgeSupport` and `AlchemyKnowledgeClientSupport` for shared potion/essence/support knowledge-state lookup.
- Added splash-potion and lingering-cloud mixins so actual exposure can mark Alchemy potion Codex entries as `TASTED`.
- Added `scripts/validate_alchemy_knowledge_gating_and_codex.py`.
- Added report `docs/reports/ALCHEMY_KNOWLEDGE_GATING_CODEX_FIXES_20260630.md`.

### Changed

- Processed-potion `性味偏向` and `特质亲和` tooltip labels now use Core nature lang prefixes instead of stale tooltip prefixes.
- Processed-potion tooltip source/style/body-state semantics are now knowledge-gated: tasted reveals source clue; mastered reveals full thermal/flavor/trait/qi/clarity/relation/body-state semantics.
- `_long`, `_strong`, `_clarified`, and `_murky` potions now map to their base Alchemy potion Codex entry for encountered/heard/tasted state.
- Potion Codex lines now use exact `KnowledgeDisplayState` to separate seen/heard wording from tasted wording.
- Alchemy overview guide text now mentions the current linkage update and potion taste gate.
- Armor support now has an Alchemy guide entry and associated zh/en language text.
- Distilled essence and armor support tooltips no longer expose explanatory detail without mastered source knowledge.
- `verify_built_jars.py` now checks the new Alchemy knowledge helper and splash/lingering mixin classes.
- `AreaEffectCloudKnowledgeMixin` no longer shadows inherited `Entity.tickCount`, fixing the client bootstrap crash reported in `RawOutput.log`.
- Moved verbose processed-potion semantic lines out of item tooltips and into mastered Alchemy Codex potion entries.
- Localized `来源药材` display through source item / essence item names instead of raw IDs.
- Splash and lingering potion exposure now routes through `PotionBurdenRuntime`, so those forms participate in burden, body-state, relation, armor-support buffering, and extension events instead of only knowledge marking.
- Added separators between implemented processing-style blocks on mastered Alchemy potion Codex pages.
- Restored Core `五味与性味` / `七情与配伍` guide examples by reading current `flavor_*`, `temperature_*`, and `seven_*` discovery keys.
- Added missing Core Chinese guide keys for `食材录与五养` and `残页与校勘` current Java paths.

### Validation

- `chmod +x gradlew` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/validate_potion_body_state_projection.py` — PASS.
- `python3 scripts/validate_potion_relation_and_catalyst_semantics.py` — PASS.
- `python3 scripts/validate_alchemy_knowledge_gating_and_codex.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, regenerated `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.


## 2026-06-30 Alchemy/Core linkage P4 seven-relation integration and P5 catalyst semantics upgrade

### Added

- Added `com/herbcraft/alchemy/potion/PotionRelationRuntime` as a conservative processed-potion bridge into Core recent-intake / seven-relation context.
- Added validator `scripts/validate_potion_relation_and_catalyst_semantics.py`.
- Added report `docs/reports/ALCHEMY_CORE_LINKAGE_P4_P5_20260630.md`.

### Changed

- `PotionBurdenRuntime` now routes processed potion consumption through `PotionRelationRuntime` before P3 body-state and burden application.
- Alchemy guide projection now explicitly teaches processed-potion relation participation and catalyst semantics through:
  - `book.herbcraft_alchemy.guide.potions.relation_integration`
  - `book.herbcraft_alchemy.guide.quality_bonus.catalyst_semantics`
- Distilled-essence orientation tooltip hints now describe catalysts in Herbcraft process language:
  - honeycomb = clarifying / taming murk
  - redstone = carrying the tail / extending momentum
  - glowstone = sharpening / intensifying
  - amethyst = steadying the bearing / harmonizing
- `verify_built_jars.py` now checks `PotionRelationRuntime.class` in the Alchemy jar.

### Validation

- `chmod +x gradlew` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew :alchemy:compileJava --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' --stacktrace` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/validate_potion_body_state_projection.py` — PASS.
- `python3 scripts/validate_potion_relation_and_catalyst_semantics.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, regenerated `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.

## 2026-06-30 Alchemy/Core linkage P3 body-state integration

### Added

- Added `com/herbcraft/alchemy/potion/PotionBodyStateProjection` as a persisted/network-synced processed-potion body-state projection component carrying `medicinal_profile`, `medicinal_weight`, `thermal_tendency`, `thermal_value`, `qi_tendency`, `qi_value`, and `body_traits`.
- Added `com/herbcraft/alchemy/potion/PotionBodyStateProjectionRuntime` to derive conservative Core-facing body-state projection from processing style plus P2 source pharmacology metadata.
- Added `com/herbcraft/alchemy/potion/PotionBodyStateConsumptionRuntime` to apply processed-potion body-state projection into Core `PlayerPharmacologyState` on consumption.
- Added read-only `AlchemyProcessingAPI.potionBodyState(ItemStack)`.
- Added tooltip/body-state lang keys in both languages for medicinal accumulation, thermal tendency, qi tendency, and typed medicinal profiles.
- Added validator `scripts/validate_potion_body_state_projection.py`.
- Added report `docs/reports/ALCHEMY_CORE_LINKAGE_P3_20260630.md`.

### Changed

- `AlchemyPotionComponents` now registers `herbcraft_alchemy:potion_body_state`.
- `AdvancedPotionStackInitializer` now applies P3 body-state projection metadata during processed-potion initialization.
- `PotionBurdenRuntime` now also routes processed potion consumption into Core-side body-state / pharmacology state before applying burden storage.
- `AdvancedPotionTooltips` now project medicinal profile, medicinal accumulation, body thermal tendency, and body qi tendency.
- `verify_built_jars.py` now checks `PotionBodyStateProjection`, `PotionBodyStateProjectionRuntime`, and `PotionBodyStateConsumptionRuntime` in the Alchemy jar.

### Validation

- `chmod +x gradlew` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/validate_potion_body_state_projection.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, regenerated `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.

## 2026-06-30 Alchemy/Core linkage plan file establishment

### Added

- Added `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md` as the authoritative multi-turn plan file for the next Alchemy/Core pharmacology linkage line.
- Froze P1-P5 definitions in that file and documented both the preferred four-round track and the allowed two-round compressed mapping.

### Changed

- `NEXT_TASK.md` now requires future turns in this line to read and update `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md` instead of relying on chat memory or ad-hoc summaries.
- `CURRENT_BASELINE.md` now records the plan file as the active next-work anchor after the distillation slot fix.

## 2026-06-30 distillation brewing slot acceptance fix

### Fixed

- Fixed the brewing-stand distillation interaction bug reported by the user where essence/catalyst placement was blocked in normal client use, preventing distillation from functioning even though the runtime brew path existed.
- Root cause was a two-part menu acceptance mismatch: the ingredient slot did not accept `DistilledEssenceBrewing.isCatalystItem(stack)`, and the bottom brewing slots still used vanilla `BrewingStandMenu$PotionSlot` placement rules that reject non-potion essence inputs unless explicitly patched.

### Changed

- Updated `BrewingStandIngredientSlotMixin` so brewing ingredient-slot `mayPlace(...)` now accepts both hidden Cuisine catalysts and distillation catalysts.
- Audited adjacent brewing-path code and confirmed the issue was the menu-slot acceptance layer rather than the custom distillation brew execution itself.

### Added

- Added validator `scripts/validate_distillation_brewing_slot_acceptance.py`.
- Added report `docs/reports/DISTILLATION_BREWING_SLOT_FIX_20260630.md`.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks` — PASS.
- `python3 scripts/validate_distillation_brewing_slot_acceptance.py` — PASS.
- `python3 scripts/validate_distillation_foundation.py` — PASS.
- `python3 scripts/validate_distillation_brewing_routing.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

### Client-only

- Confirm live-client brewing-stand slot placement, distillation output, and distilled-essence follow-up routing into processed potions.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, and regenerated `herbcraft-source-current-20260614.zip`.

## 2026-06-30 Alchemy/Core linkage P2 source pharmacology projection

### Added

- Added `com/herbcraft/alchemy/potion/PotionPharmacologyProjection`, a persisted/network-synced processed-potion source pharmacology projection component carrying `source_essence`, `temperature`, `flavors`, `taste_flavors`, and `traits`.
- Added registered data component `herbcraft_alchemy:potion_pharmacology` in `AlchemyPotionComponents`.
- Added `PotionPharmacologyProjectionRuntime` to derive source pharmacology projection from Core `HerbNatureAPI` / `NatureProfile` and attach it to processed potion stacks.
- Added read-only `AlchemyProcessingAPI.potionPharmacology(ItemStack)` query.
- Added tooltip/source-projection lang keys in both languages:
  - `tooltip.herbcraft.potion.source_essence`
  - `tooltip.herbcraft.potion.source_temperature`
  - `tooltip.herbcraft.potion.source_taste`
  - `tooltip.herbcraft.potion.source_traits`
- Added validator `scripts/validate_potion_source_projection.py`.
- Added report `docs/reports/ALCHEMY_CORE_LINKAGE_P2_20260630.md`.

### Changed

- `AdvancedPotionStackInitializer` now attaches source pharmacology projection metadata during processed-potion initialization.
- `AdvancedPotionTooltips` now project source herb/temperature/taste/trait identity so different source essences under the same style remain visibly distinct.
- `verify_built_jars.py` now checks `PotionPharmacologyProjection.class` and `PotionPharmacologyProjectionRuntime.class` in the Alchemy jar.

### Validation

- `chmod +x gradlew` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, regenerated `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.

### Client-only

- Confirm the new source-pharmacology tooltip projection reads well in a live client and decide later whether raw source IDs should be upgraded to localized display names.

## 2026-06-30 Alchemy/Core linkage P1 guide-semantics completion

### Changed

- Began the preferred four-round Alchemy/Core linkage track from `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md` and completed Round 1 / P1.
- Confirmed the P1 semantic schema was already present in `data/herbcraft_alchemy/potion_processing_rules.json` and `PotionProcessingRules`, including `thermal_shift`, `flavor_bias`, `trait_affinity`, `burden_profile`, `relation_modifier`, `qi_bias`, and `clarity_level`, with tooltip projection already wired in `AdvancedPotionTooltips`.
- Extended `AlchemyChapter` so the Alchemy Codex now explicitly teaches processing styles as pharmacological handling semantics rather than only stronger/longer/stable variants.

### Added

- Added new guide-facing language keys in both `zh_cn` and `en_us`:
  - `book.herbcraft_alchemy.guide.potions.processing_semantics`
  - `book.herbcraft_alchemy.guide.quality_bonus.processing_meaning`
- Added report `docs/reports/ALCHEMY_CORE_LINKAGE_P1_20260630.md`.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

### Output

- Refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, regenerated `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.

### Client-only

- Confirm the new Alchemy guide wording reads naturally and clearly in the in-game Codex.

## 2026-06-30 environment setup and handoff sync

### Changed

- Completed a fresh-clone environment setup and current-state handoff review for the `1.1.3` line.
- Cloned the repository, read `AGENT_BRIEF.md`, regenerated `docs/reports/CURRENT_AGENT_BRIEF.md`, and re-read the authoritative anchor documents required by the repository workflow.
- Restored `gradlew` execute permission and provisioned JDK 25 with `scripts/setup_jdk25_temurin.sh`.
- Rebuilt the full multi-module project successfully and re-ran the current validator suite.
- Added `docs/reports/ENVIRONMENT_SETUP_20260630.md`.

### Validation

- `python3 scripts/generate_agent_brief.py --write` — PASS.
- `bash scripts/setup_jdk25_temurin.sh` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks` — PASS.
- Full validator suite listed in `CURRENT_BASELINE.md` — PASS.

### Client-only

- Live-client JEI/REI verification and in-game behavior checks remain pending.


## 2026-06-29 Alchemy processed weapon coating expansion E

### Added

- Added `data/herbcraft_alchemy/processed_coating_rules.json` for data-backed processed weapon coating styles.
- Added `ProcessedCoatingRules` and extended `WeaponCoating` with optional `processing_style` defaulting to `base` for saved-data compatibility.
- Added processed coating support to the existing smithing route:
  - `blank_adhesive + weapon + base essence` still creates base coating;
  - `blank_adhesive + weapon + distilled_essence` creates processed coating using the distilled orientation/style.
- Added tooltip line for processed coating style.
- Added validator `scripts/validate_processed_coating_expansion.py`.
- Added report `docs/reports/ALCHEMY_PROCESSED_WEAPON_COATING_E_20260629.md`.

### Changed

- Coating runtime now applies style multipliers to effect profile and use count for `sustained`, `intense`, and `purified`.
- Amethyst/echo refinement preserves the coating processing style.
- Coating display generation includes representative processed coating rows using distilled essence samples.

### Boundaries

- Base coating remains valid and unchanged as the default route.
- `murky_edge` is reserved/deferred.
- Dedicated JEI/REI explicit plugin rows for processed coatings may need a later viewer parity pass if vanilla display projection is insufficient.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_processed_coating_expansion.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

## 2026-06-29 Alchemy station migration D2

### Changed

- Moved distillation from crafting-table recipes to the brewing stand.
  - Removed bundled crafting recipes for distilled essence outputs.
  - Essence items can now be placed in brewing stand bottom slots for distillation.
  - Honeycomb/redstone/glowstone in the ingredient slot produce purified/sustained/intense distilled essences.
- Moved armor support from crafting-table recipes to the smithing table.
  - Removed bundled `armor_support.json` crafting route.
  - Added `herbcraft:armor_support_smithing` custom smithing serializer and apply/refine recipe JSON.
- Added adhesive linkage for armor support:
  - `blank_adhesive + armor + distilled_essence` -> ATTACHED support;
  - `amethyst_adhesive + supported armor` -> CLEAR support;
  - `echo_adhesive + supported armor` -> DEEP support.
- Added `ArmorSupportMode` and tooltip mode display.

### Architecture

- Station identity is now aligned:
  - distillation and potion processing use brewing stand;
  - weapon coating and armor support use smithing table + adhesives.
- Old hidden Cuisine catalyst brewing and old potion modifier routes remain intact.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- Full validator suite listed in `CURRENT_BASELINE.md` — PASS.
- Release jars and source zip refreshed.

### Client-only

- Confirm no old crafting-table outputs remain.
- Confirm brewing stand distillation works.
- Confirm smithing-table armor support apply/refine works with blank/amethyst/echo adhesives.

## 2026-06-29 Alchemy armor-side support D foundation

### Added

- Added `data/herbcraft_alchemy/armor_support_rules.json` with first armor-side support rows:
  - `clear_bearing` from purified distilled essence;
  - `steady_bearing` from sustained distilled essence;
  - `braced_bearing` from intense distilled essence;
  - reserved `harmonized_bearing`.
- Added persistent/network-synced armor component `herbcraft_alchemy:armor_support`.
- Added generic custom recipe `herbcraft:armor_support`: one armor piece + one implemented distilled essence -> same armor with support component.
- Added `ArmorSupportRuntime`, which buffers potion burden through `POTION_BURDEN_BUFFER` while supported armor is worn.
- Added armor support tooltip projection and validator `scripts/validate_armor_support_foundation.py`.
- Added report `docs/reports/ALCHEMY_ARMOR_SUPPORT_D_FOUNDATION_20260629.md`.

### Architecture / boundaries

- Armor detection uses the vanilla `EQUIPPABLE` component and armor equipment slots, not hardcoded item tables.
- This is body-support/protective armor alchemy only. No thorns/retaliation behavior was added.
- Harmonized support remains reserved until harmonized distillation/processing is implemented.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_armor_support_foundation.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

### Client-only

- Confirm armor + distilled essence crafting, support tooltip, burden buffering, clear_bearing feedback, and no retaliation behavior.

## 2026-06-29 Alchemy distillation-to-potion routing C

### Added

- Added `DistilledEssenceBrewing`, a custom brewing route for the B3 distilled essence item.
- Matching base Herbcraft potion + matching componentized distilled essence now routes to existing stable processed potion IDs:
  - `purified` -> `_clarified`;
  - `sustained` -> `_long`;
  - `intense` -> `_strong`;
  - `wither_rose` + `intense` -> `wither_venom_iii`.
- Added validator `scripts/validate_distillation_brewing_routing.py`.
- Added report `docs/reports/ALCHEMY_DISTILLATION_TO_POTION_ROUTING_C_20260629.md`.

### Changed

- `BrewingStandCatalystMixin` now accepts distilled essence as a custom brewing ingredient while preserving the existing hidden Cuisine catalyst route.
- Distilled brewing output clears stale quality/bonus/style/burden/initialization components before the new potion path initializes normally.

### Architecture / boundaries

- Existing redstone/glowstone/honeycomb modifier routes remain available.
- No new potion IDs were added.
- Distilled essence source must match the base potion path; mismatches do not brew or consume the ingredient.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_distillation_foundation.py` — PASS.
- `python3 scripts/validate_distillation_brewing_routing.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

### Client-only

- Confirm matching distilled essence brewing works, mismatches are rejected, old routes still work, and hidden Cuisine catalyst brewing still works.

## 2026-06-29 Alchemy distillation B3 foundation

### Added

- Added `data/herbcraft_alchemy/distillation_rules.json` with first-batch distillation orientations:
  - `purified` via honeycomb;
  - `sustained` via redstone;
  - `intense` via glowstone dust;
  - reserved `harmonized` via amethyst shard.
- Added B3 mixed-route distilled essence foundation:
  - one visible item `herbcraft:distilled_essence`;
  - persistent/network-synced component `herbcraft_alchemy:distilled_essence` carrying `source_essence` and `orientation`;
  - generic `herbcraft:distillation` custom recipe serializer;
  - recipes for purified/sustained/intense distillation from any current essence + orientation catalyst;
  - read-only `AlchemyDistillationAPI`;
  - client tooltip projection for source essence and orientation.
- Added item/model/texture/lang for `distilled_essence`. The texture keeps the existing essence-bottle silhouette and palette style with pale distilled-liquid/sparkle treatment.
- Added validator `scripts/validate_distillation_foundation.py`.
- Added report `docs/reports/ALCHEMY_DISTILLATION_B3_FOUNDATION_20260629.md`.

### Architecture / boundaries

- This deliberately avoids per-source distilled essence item explosion.
- This foundation does not yet route distilled essences into potion processing, armor support, or advanced weapon coating.
- Current essence, potion, and coating recipes remain unchanged.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_distillation_foundation.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

### Client-only

- Confirm any essence + honeycomb/redstone/glowstone crafts the correct componentized distilled essence.
- Confirm tooltip shows source and orientation.
- Confirm texture style matches the existing essence family in-game.

## 2026-06-29 Alchemy expansion A-round: custom burden effects and long/strong tooltip fix

### Fixed

- Fixed a client-facing tooltip issue: `_long` and `_strong` potions showed `Quality will be rolled when this stack is updated` even though they intentionally do not enter the legacy advanced random quality/bonus path.
- The uninitialized quality line now appears only for legacy advanced paths such as `_clarified`, `_murky`, and risky special advanced paths.

### Added

- Added data-backed Alchemy custom burden effects in `data/herbcraft_alchemy/potion_burden_effects.json`:
  - `herbcraft_alchemy:potion_warming` / 药势内温;
  - `herbcraft_alchemy:potion_strain` / 药负牵滞;
  - `herbcraft_alchemy:potion_overload` / 药势过载;
  - `herbcraft_alchemy:clear_bearing` / 澄承, reserved for future armor/support buffering.
- Added `AlchemyBurdenEffectRules` and `AlchemyMobEffects`.
- Added 18x18 effect icons and lang keys for all four effects.
- Added validator `scripts/validate_alchemy_burden_effects.py`.
- Added report `docs/reports/ALCHEMY_CUSTOM_BURDEN_EFFECTS_A_ROUND_20260629.md`.

### Changed

- Potion burden stages now apply/remove custom Alchemy effects instead of direct raw movement modifiers.
- The strain/overload movement modifiers now live in JSON-backed custom effect rules.
- `verify_built_jars.py` now checks custom Alchemy effect classes/resources/icons.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- Full validator suite listed in `CURRENT_BASELINE.md` — PASS, including new `validate_alchemy_burden_effects.py`.
- Release jars and source zip refreshed.

### Client-only

- Confirm `_long` / `_strong` no longer show uninitialized quality text.
- Confirm custom burden effects display with names/icons and clear after decay/death.

## 2026-06-29 Alchemy expansion Round 4/P7-P8: burden consequences and extension hooks

### Added

- Added data-backed potion burden stages to `potion_processing_rules.json`: `steady`, `warming`, `strain`, and `overload`.
- Added `PotionBurdenConsequenceRuntime` with conservative movement-speed consequences:
  - `strain`: -2% movement speed;
  - `overload`: -5% movement speed.
- Added localized burden stage messages and stage persistence to avoid repeated tick spam.
- Added `AlchemyExpansionEvents` with minimal hooks for future expansion:
  - `PROCESSING_STYLE_APPLIED`;
  - `POTION_BURDEN_APPLIED`;
  - `POTION_BURDEN_STAGE_CHANGED`;
  - `POTION_BURDEN_BUFFER` for future armor-support style buffering.
- Added read-only `AlchemyProcessingAPI` for processing style, potion burden, player burden, and player burden stage queries.
- Added validators `scripts/validate_potion_burden_consequences.py` and `scripts/validate_alchemy_expansion_boundaries.py`.
- Added report `docs/reports/ALCHEMY_BURDEN_CONSEQUENCES_AND_EXTENSION_HOOKS_P7_P8_20260629.md`.

### Changed

- Player burden tick now applies the conservative consequence layer.
- Death reset now also clears the transient potion-burden movement modifier.
- `PotionProcessingRuntime` and `PotionBurdenRuntime` now fire expansion events.
- `verify_built_jars.py` now checks consequence/runtime hook/API classes.

### Architecture / boundaries

- This completes the four-round source-level foundation for potion style, burden metadata, player burden, conservative consequences, and future integration hooks.
- Distillation, armor alchemy, and advanced processed-essence coating are still future systems; P8 only added narrow hooks/query API.
- `AlchemyProcessingAPI` intentionally exposes read-only queries and no content registration/write APIs.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- Full validator suite listed in `CURRENT_BASELINE.md` — PASS, including new `validate_potion_burden_consequences.py` and `validate_alchemy_expansion_boundaries.py`.
- Release jars and source zip refreshed.

### Client-only

- Confirm stage messages, mild movement penalties, decay, death cleanup, and no unintended penalties outside processed potion burden.

## 2026-06-29 Alchemy expansion Round 3/P5-P6: potion burden metadata and player runtime

### Added

- Added `PotionBurdenComponent` and registered persistent/network-synced stack component `herbcraft_alchemy:potion_burden`.
- Added JSON-owned burden metadata to `potion_processing_rules.json`: `burden_weight`, `tail_weight`, and `burden_runtime` decay settings.
- Added `PotionBurdenRuntime` to attach burden metadata to processed potion stacks and apply burden when consumed.
- Added Alchemy-owned player burden state:
  - `PlayerPotionBurdenState`;
  - `AlchemyPlayerData`;
  - `AlchemyPlayerMixin` save/load and death reset;
  - `PlayerPotionBurdenRuntime` tick decay and alive-copy policy.
- Added `AlchemyItemMixin` hooks to initialize processed potions before use and apply burden after use.
- Added tooltip display for active/tail burden weights.
- Added validator `scripts/validate_potion_burden_runtime.py`.
- Added report `docs/reports/ALCHEMY_POTION_BURDEN_METADATA_RUNTIME_P5_P6_20260629.md`.

### Changed

- `AdvancedPotionStackInitializer` now attaches missing burden metadata without reapplying style runtime.
- Alchemy initialization now registers player burden lifecycle/tick hooks.
- `herbcraft_alchemy.mixins.json` now includes Alchemy item/player mixins.
- `verify_built_jars.py` now checks burden component/runtime/player-state classes and mixins.

### Architecture / boundaries

- Potion burden is currently Alchemy-owned and clears on death.
- Alive player-copy cases copy burden state; death respawn keeps reset semantics.
- No P7 threshold consequences are active yet: burden stores, persists, increases, and decays, but does not yet impose movement penalties/backlash/overload effects.
- `murky_edge` and `harmonized` remain deferred for runtime style behavior, although their burden metadata is reserved in JSON.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- Full validator suite listed in `CURRENT_BASELINE.md` — PASS, including new `python3 scripts/validate_potion_burden_runtime.py`.
- Release jars and source zip refreshed.

### Client-only

- Confirm burden metadata appears in processed potion tooltips.
- Confirm drinking processed potions increases burden, burden decays, and death clears it.
- Confirm no visible P7-style penalties occur before the next round.

## 2026-06-29 Alchemy expansion Round 2/P4: potion processing runtime

### Added

- Added `PotionProcessingRuntime`, the first runtime behavior layer for implemented potion-processing styles.
- Added persistent/network-synced stack component `herbcraft_alchemy:potion_processing_style` to mark which style runtime has been applied and prevent repeated rewrites.
- Added validator `scripts/validate_potion_processing_runtime.py`.
- Added report `docs/reports/ALCHEMY_POTION_PROCESSING_RUNTIME_P4_20260629.md`.

### Changed

- `potion_processing_rules.json` now marks first-batch styles as implemented and supplies JSON-owned runtime profiles:
  - `sustained`: bounded duration lift with peak reduction where possible;
  - `intense`: bounded amplifier increase with duration reduction;
  - `purified`: clean/stable projection with light duration lift and visible/non-ambient effect display.
- `PotionProcessingRules` now parses `RuntimeProfile` metadata.
- `AdvancedPotionStackInitializer` now applies implemented processing runtime once per stack/style.
- `_long` and `_strong` now receive sustained/intense processing through style runtime while still avoiding the legacy random quality/bonus advanced-potion path.
- `murky_edge` and `harmonized` remain deferred and not runtime-implemented.
- `verify_built_jars.py` now checks `PotionProcessingRuntime`, `RuntimeProfile`, and `AlchemyPotionComponents` classes.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_processing_runtime.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

### Client-only

- Confirm actual `_long`, `_strong`, and `_clarified` effect profiles in a real client.
- Confirm `_long` / `_strong` do not roll quality or bonus effects unexpectedly.

## 2026-06-29 Alchemy expansion Round 1/P1-P3: potion processing style foundation

### Added

- Added `data/herbcraft_alchemy/potion_processing_rules.json` as the mechanism-layer style metadata foundation for the four-round Alchemy expansion task.
- Added `PotionProcessingRules`, a data-backed loader/query layer for potion processing styles, aliases, exact path mappings, and suffix mappings.
- Added style-id facades to `AdvancedPotionStackInitializer` so current potion paths can resolve processing styles without renaming stable potion IDs.
- Added tooltip projection for processing style and burden tendency metadata.
- Added validator `scripts/validate_potion_processing_rules.py`.

### Changed

- Alchemy initialization now loads potion processing rules.
- Clarified/murky acquisition routing now uses style IDs (`purified`, `murky_edge`) instead of only raw suffix checks, while preserving current advancement behavior.
- `verify_built_jars.py` now checks the new rules JSON and `PotionProcessingRules.class` in the Alchemy jar.
- `docs/EXTENDING_HERBCRAFT.md`, `docs/DOCUMENTATION_INDEX.md`, `CURRENT_BASELINE.md`, and `NEXT_TASK.md` now record the new foundation and the next P4 boundary.

### Architecture / boundaries

- This is P1-P3 only. Current potion runtime behavior is intentionally unchanged.
- `_long` and `_strong` map to `sustained` / `intense` for projection, but are not converted into initialized advanced potions with quality/bonus in this round.
- `sustained`, `intense`, and `purified` remain `implemented: false` in JSON until P4 runtime effect differentiation is actually implemented.
- Deferred branches `murky_edge` and `harmonized` remain visible but not first-batch runtime styles.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- Full validator suite listed in `CURRENT_BASELINE.md` — PASS, including new `python3 scripts/validate_potion_processing_rules.py`.
- Release jars and source zip refreshed.

## 2026-06-29 Alchemy expansion feasibility/source verification

### Added

- Added `docs/reports/ALCHEMY_EXPANSION_FEASIBILITY_REVIEW_20260629.md`, reviewing the attached `ALCHEMY_EXPANSION_MASTER_STATUS.md` against current source.

### Findings

- Confirmed the overall expansion direction is feasible, especially starting with potion-processing style metadata layered over existing `_long` / `_strong` / `_clarified` / `_murky` potion paths.
- Verified that the attached document is ahead of the current repository for potion-processing implementation claims: current source does not yet contain `potion_processing_rules.json`, `PotionProcessingRules`, `processingStyleId(...)`, `PotionProcessingRuntime`, potion burden components/runtime, player-level potion burden storage, or `murky_edge` / `harmonized` source rows.
- Recorded a staged implementation plan: first reconcile docs/status, then add style JSON/loader, style projection, tooltip/acquisition routing, runtime effect differentiation, burden metadata, player burden runtime, and finally distillation/armor/coating integration.

### Validation

- Source search confirmed the absence of the claimed new potion-processing/burden classes/resources and identified the current relevant Alchemy files.
- `check_lang_diff.py`, `validate_externalized_rules.py`, `validate_external_extension_spec.py`, `validate_coating_viewer_plugins.py`, and `verify_built_jars.py` — PASS.
- A daemon Gradle run disappeared unexpectedly in the sandbox; retry with `--no-daemon -Xmx1g` passed. Documentation-only pass; no gameplay Java/JSON/resources changed and no jar-facing behavior change was intended.

## 2026-06-29 environment setup and validator-location refresh

### Changed

- Completed the current sandbox environment setup from a fresh clone: regenerated `CURRENT_AGENT_BRIEF`, provisioned JDK 25, restored Gradle wrapper execute permission, and rebuilt the full `1.1.3` multi-module project.
- Refreshed validators that were still anchored to pre-split source locations so they now validate the current split bootstrap, nutrition, and `HerbalChapter` architecture:
  - nutrition effect/correction validators now inspect `NutritionRuntimeService`, `NutritionEffectApplicator`, `NutritionDebugService`, and `HerbcraftDataBootstrap` where appropriate;
  - externalized-rule and extension validators now inspect `NutritionDiscoveryHooks` and split `HerbalChapter*` projection/registration files;
  - Jade/Codex advancement validators now account for split `HerbalChapter*` classes.
- Added `docs/reports/ENVIRONMENT_SETUP_AND_VALIDATOR_REFRESH_20260629.md` documenting the setup, validator refresh, validation results, output refresh, and client-only checks.

### Output

- Recreated local `release_output/herbcraft_test_20260614_review/` in the fresh clone with rebuilt `herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`, refreshed `herbcraft-source-current-20260614.zip`, and recreated `herbcraft-p4-runtime-test-datapack-20260617.zip`.

### Validation

- `bash scripts/setup_jdk25_temurin.sh` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks` — PASS.
- Full validator suite listed in `CURRENT_BASELINE.md` plus coating/bootstrap/nutrition/Codex split validators — PASS.

### Client-only

- Real-client JEI/REI coating display verification remains required with the user's exact installed viewer versions.
- Runtime UI/compat/survival-feel checks remain client-only.

## 2026-06-29 REI coating display parity: expand coatable weapon tag

### Changed

- Improved REI weapon coating displays after client feedback: REI was visible, but only used an iron sword as the representative coating example while JEI showed the broader coatable weapon set.
- `CoatingReiPlugin` now expands `#herbcraft:coatable_weapons` through `BuiltInRegistries.ITEM.getTagOrEmpty(CoatingSystem.COATABLE_WEAPONS)`.
- REI apply displays now show all coatable weapons as base inputs and all corresponding coated weapon outputs for each essence.
- REI refine displays now show all fully coated weapon variants as base inputs and all corresponding refined weapon outputs.
- The iron sword remains only as a defensive fallback if the tag is unexpectedly empty; ordinary display is tag-driven.

### Architecture

- This remains a UI adapter/projection change only. Runtime smithing mechanics and JSON content are unchanged.
- The REI plugin still derives content from existing registries/tags and JSON-backed essence data; it does not hardcode a weapon compatibility table.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew :alchemy:compileJava --stacktrace` — PASS.
- `python3 scripts/validate_coating_viewer_plugins.py` — PASS, now guarding REI coatable-tag expansion.

## 2026-06-29 JEI plugin discovery fix for weapon coatings

### Fixed

- Fixed the remaining JEI-only visibility issue after REI was confirmed working.
- Root cause: JEI Fabric `FabricPluginFinder` loads plugins from Fabric entrypoint key `jei_mod_plugin`; the previous JEI class had `@JeiPlugin` but was not declared as a Fabric JEI plugin entrypoint, so JEI did not load it.
- Added `jei_mod_plugin` entrypoint in Alchemy `fabric.mod.json` pointing to `com.herbcraft.alchemy.compat.jei.CoatingJeiPlugin`.

### Validation

- `python3 scripts/validate_coating_viewer_plugins.py` — PASS, now checking both `jei_mod_plugin` and `rei_client` entrypoints.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

### Architecture

- Confirmed explicit viewer plugins are acceptable under project rules: Java owns UI/registration/adapter logic; content rows remain generated from JSON-backed essence data and are not hardcoded content tables.

## 2026-06-29 weapon coating JEI/REI explicit plugin integration

### Fixed

- Follow-up after the user confirmed the previous passive vanilla-display fixes still did not make weapon coatings searchable in the live client.
- Added explicit JEI and REI recipe-viewer integrations for weapon coating smithing recipes instead of relying on automatic indexing of the custom recipe serializer.

### Added

- Added `com.herbcraft.alchemy.compat.jei.CoatingJeiPlugin`:
  - JEI `@JeiPlugin` / `IModPlugin` integration;
  - registers a smithing category extension for `CoatingSmithingRecipe`;
  - explicitly adds coating apply/refine recipe holders to JEI `RecipeTypes.SMITHING`;
  - supplies template/base/addition/output slots using real data-backed essence/coating stacks.
- Added `com.herbcraft.alchemy.compat.rei.CoatingReiPlugin`:
  - REI `REIClientPlugin` integration;
  - registered through `fabric.mod.json` `rei_client` entrypoint;
  - explicitly registers `DefaultSmithingDisplay` entries for every data-backed essence apply/refine case.
- Added compile-only optional viewer API dependencies for JEI, REI, and Architectury.
- Added validator `scripts/validate_coating_viewer_plugins.py`.

### Validation

- `python3 scripts/validate_coating_recipe_display.py` — PASS.
- `python3 scripts/validate_coating_viewer_plugins.py` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS, including JEI/REI plugin classes in Alchemy jar.
- Jar listing confirmed Herbcraft plugin classes are present and JEI/REI/Architectury API packages are not bundled.
- Release jars and source zip refreshed.
- Client-only confirmation still required with the user's actual JEI/REI versions.

## 2026-06-29 weapon coating JEI/REI follow-up: remove special-recipe filtering

### Fixed

- Follow-up to the weapon coating recipe display bug: the first fix added `SmithingRecipeDisplay` entries but left `CoatingSmithingRecipe.isSpecial()` returning `true`, which can still cause JEI/REI-style viewers to filter the recipe out of normal searchable smithing categories.
- `CoatingSmithingRecipe.isSpecial()` now returns `false` so the recipe remains a normal searchable smithing recipe while preserving the existing custom serializer, `matches(...)`, and `assemble(...)` behavior.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/validate_coating_recipe_display.py` — PASS, now guarding non-special classification.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed again after the follow-up.
- Client-only confirmation still required with JEI/REI installed.

## 2026-06-29 weapon coating recipe display bugfix

### Fixed

- Fixed weapon coating (武器淬层) recipes being craftable in the smithing table but not discoverable/searchable in JEI/REI-style recipe viewers.
- Root cause: the custom `herbcraft:coating_smithing` recipe implemented matching/assembly but inherited the default empty `Recipe.display()` list, so display-driven clients had no smithing recipe entries to index.

### Changed

- `CoatingSmithingRecipe.display()` now emits vanilla `SmithingRecipeDisplay` entries for both apply and refine modes.
- Apply displays are generated per JSON-backed essence: blank adhesive + coatable weapon + essence -> representative coated iron sword.
- Refine displays are generated per eligible essence/effect side: amethyst adhesive for positive refinement and echo adhesive for negative refinement, preserving coating components in display stacks.
- Added `AlchemyRegistries.essenceInfos()` as an immutable-copy accessor so recipe displays remain derived from the data-backed essence registry instead of hardcoded Java item lists.

### Added

- Added validator `scripts/validate_coating_recipe_display.py`.
- Added report `docs/reports/COATING_RECIPE_DISPLAY_BUGFIX_20260629.md`.

### Validation

- `bash scripts/setup_jdk25_temurin.sh` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew :alchemy:compileJava --stacktrace` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/validate_coating_recipe_display.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.
- Client-only confirmation still required for JEI/REI search/indexing and visual smithing recipe display.

## 2026-06-28 structural decomposition phase 2: split `NutritionManager`

### Changed

- Began structural decomposition of the multi-responsibility nutrition subsystem while preserving behavior.
- Refactored `core/src/main/java/com/herbcraft/nutrition/NutritionManager.java` into a thin facade delegating to focused services.
- Added focused nutrition service classes:
  - `NutritionRuntimeService` — runtime ticking, evaluation, relief timing, login/death orchestration, client sync coordination
  - `NutritionEffectApplicator` — effect application/removal and abundant modifier handling
  - `NutritionDiscoveryHooks` — ingredient discovery / knowledge / guide-hint side effects on food consume
  - `NutritionDebugService` — debug mutation and preset helpers

### Architecture

- Nutrition runtime logic, effect logic, knowledge/discovery side effects, and debug helpers now have clearer ownership boundaries.
- Public call sites continue using `NutritionManager`, so this phase preserves external API stability.

### Added

- Added validator `scripts/validate_nutrition_split_phase2.py`.
- Added report `docs/reports/NUTRITION_SPLIT_PHASE2_20260628.md`.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/validate_nutrition_split_phase2.py` — PASS.
- `python3 scripts/validate_bootstrap_split_phase1.py` — PASS.
- `python3 scripts/validate_coupling_hotspots.py` — PASS.
- `python3 scripts/validate_codex_payload_separation.py` — PASS.
- `python3 scripts/validate_codex_respawn_ui_state.py` — PASS.
- `python3 scripts/validate_player_state_respawn_copy.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

## 2026-06-28 bootstrap split phase 1: decomposed `Herbcraft.java`

### Changed

- Began structural decomposition of the oversized core bootstrap/orchestrator.
- Split `Herbcraft.java` Phase 1 responsibilities into focused bootstrap classes:
  - `core/src/main/java/com/herbcraft/bootstrap/HerbcraftDataBootstrap.java`
  - `core/src/main/java/com/herbcraft/bootstrap/HerbcraftKnowledgeBootstrap.java`
  - `core/src/main/java/com/herbcraft/bootstrap/HerbcraftNetworkingBootstrap.java`
  - `core/src/main/java/com/herbcraft/bootstrap/HerbcraftLifecycleBootstrap.java`
- `Herbcraft.java` now delegates initialization instead of directly owning bundled data loading, reload listener registration, Codex/knowledge bootstrap wiring, payload registration, and join/tick lifecycle wiring.

### Architecture

- Data/bootstrap, knowledge bootstrap, networking registration, and lifecycle registration now have clearer ownership boundaries.
- Behavior was intentionally kept functionally equivalent in this phase; this is a structural decomposition pass, not a gameplay change.

### Added

- Added validator `scripts/validate_bootstrap_split_phase1.py`.
- Added report `docs/reports/BOOTSTRAP_SPLIT_PHASE1_20260628.md`.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/validate_bootstrap_split_phase1.py` — PASS.
- `python3 scripts/validate_coupling_hotspots.py` — PASS.
- `python3 scripts/validate_codex_payload_separation.py` — PASS.
- `python3 scripts/validate_codex_respawn_ui_state.py` — PASS.
- `python3 scripts/validate_player_state_respawn_copy.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

## 2026-06-28 broader high-coupling audit and hotspot guardrails

### Added

- Added architecture audit report `docs/reports/HIGH_COUPLING_AUDIT_20260628.md` covering broader non-UI coupling risk after the death/respawn incidents.
- Added validator `scripts/validate_coupling_hotspots.py` to guard the highest-risk fixed boundary and ensure the hotspot audit remains present.

### Findings

- Confirmed the recent bugs were symptoms of a broader coupling pattern, not just isolated UI mistakes.
- Identified the main current high-risk hotspots as:
  - oversized central bootstrap/orchestrator in `Herbcraft.java`;
  - multi-responsibility `NutritionManager`;
  - multi-responsibility `HerbalChapter`;
  - broad snapshot refresh path for narrow Codex claim-mark updates;
  - static/global client caches relying on implicit lifecycle assumptions.
- No additional same-severity live packet/UI auto-open coupling was found beyond the already fixed Codex path.

### Validation

- `python3 scripts/validate_coupling_hotspots.py` — PASS.
- `python3 scripts/validate_codex_payload_separation.py` — PASS.
- `python3 scripts/validate_codex_respawn_ui_state.py` — PASS.
- `python3 scripts/validate_player_state_respawn_copy.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

## 2026-06-28 Codex payload/UI responsibility separation refactor

### Changed

- Refactored Codex networking so `CodexSnapshotPayload` is now treated as pure data instead of a combined "data snapshot + open UI" packet.
- Added explicit clientbound payload `OpenCodexPayload` to serve as the dedicated "open Codex UI now" signal.
- `CodexBookItem` now sends two packets in order:
  - `CodexSnapshotPayload` for the current Codex data snapshot;
  - `OpenCodexPayload` to explicitly open the Codex screen.
- Client `CodexSnapshotPayload` handling no longer calls `setScreen(...)`. It now updates `CodexClientState` and refreshes the current `CodexScreen` in-place if the screen is already open.
- Added `CodexClientState` as a small client-side cache for the latest Codex snapshot.
- `CodexScreen` now supports live snapshot replacement via `replaceSnapshot(...)`.
- Client disconnect cleanup now clears both Codex remembered UI session state and cached snapshot state.

### Architecture

- Codex UI opening and Codex data synchronization are now explicitly separated responsibilities.
- This prevents future lifecycle sync paths (respawn/join/reconnect/etc.) from accidentally opening the Codex UI simply by reusing the snapshot payload.

### Added

- Added validator `scripts/validate_codex_payload_separation.py`.
- Added report `docs/reports/CODEX_PAYLOAD_UI_RESPONSIBILITY_SEPARATION_20260628.md`.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/validate_codex_payload_separation.py` — PASS.
- `python3 scripts/validate_codex_respawn_ui_state.py` — PASS.
- `python3 scripts/validate_player_state_respawn_copy.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

## 2026-06-28 death/respawn Codex stale-page reopen bugfix

### Fixed

- Fixed a new post-respawn client/UI bug where the Codex (`百草经`) could reopen on the previously viewed page after death/respawn.
- Root cause 1: `CodexScreen` kept static client-side session state (`lastViewedKey` and `expandedNodes`), so the previous page selection survived as long as the client JVM stayed alive.
- Root cause 2: the earlier respawn persistence fix resent `CodexSnapshotPayload` during `AFTER_RESPAWN`, but this payload is also the client-side "open the Codex screen now" signal, not a passive cache-only sync packet.
- Removed respawn-time `CodexSnapshotPayload` sending from `PlayerStateSync` so death/respawn no longer force-opens the Codex UI.
- Added `CodexScreen.clearClientSessionState()` and wired client disconnect cleanup so remembered Codex page/expanded-node UI state does not leak across client play sessions.

### Added

- Added validator `scripts/validate_codex_respawn_ui_state.py`.
- Added report `docs/reports/DEATH_RESPAWN_CODEX_STALE_PAGE_BUGFIX_20260628.md`.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/validate_codex_respawn_ui_state.py` — PASS.
- `python3 scripts/validate_player_state_respawn_copy.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.

## 2026-06-28 death/respawn knowledge persistence bugfix

### Fixed

- Fixed a critical player-data sync bug where death/respawn recreated the `ServerPlayer` entity without copying Herbcraft custom player fields, causing tasted records / Codex knowledge progress to appear wiped after death.
- Added `core/src/main/java/com/herbcraft/PlayerStateSync.java` and registered Fabric `ServerPlayerEvents.COPY_FROM` to copy death-persistent Herbcraft player state from the old player entity to the new respawned player.
- Added `ServerPlayerEvents.AFTER_RESPAWN` handling to immediately refresh nutrition sync, knowledge sync, and Codex snapshot after respawn.

### Persistence semantics reviewed

- Preserved across death/respawn: knowledge map, knowledge flags, experienced effects, claim marks, verified true/false claims, and seen guide hints.
- Intentionally NOT preserved across death/respawn: herb stack, effect immunities, pharmacology runtime state, and nutrition runtime state/recent meals, because these already have explicit death-reset semantics in source.
- Window counters were reviewed and intentionally left non-copied as transient runtime state; this decision is now explicit in code.

### Added

- Added validator `scripts/validate_player_state_respawn_copy.py` to guard against future regression in player respawn copy wiring.
- Added report `docs/reports/DEATH_RESPAWN_KNOWLEDGE_PERSISTENCE_BUGFIX_20260628.md`.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- `python3 scripts/validate_player_state_respawn_copy.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
- Release jars and source zip refreshed.


## 2026-06-28 environment setup / rebuild / verification handoff

### Added

- Added a fresh current-turn environment verification report: `docs/reports/ENVIRONMENT_SETUP_AND_VERIFICATION_20260628.md`.

### Changed

- Re-ran `python3 scripts/generate_agent_brief.py --write` and re-read the current authoritative onboarding anchors for the sandbox turn.
- Provisioned JDK 25 (`Temurin-25.0.3+9`) again in the current sandbox via `scripts/setup_jdk25_temurin.sh`.
- Restored Gradle wrapper execute permission and rebuilt the full `1.1.3` multi-module line successfully in the current workspace.
- Refreshed local `release_output/herbcraft_test_20260614_review/` jars:
  - `herbcraft-1.1.3.jar`
  - `herbcraft-alchemy-1.1.3.jar`
  - `herbcraft-cuisine-1.1.3.jar`
- Regenerated `release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip` from the current repository state.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS.
- Passed validators: `check_lang_diff.py`, `validate_custom_nutrition_effects_phase8.py`, `validate_nutrition_correction_phase.py`, `validate_externalized_rules.py`, `validate_raw_herb_food_balance.py`, `validate_essence_resources.py`, `validate_homeostatic_compat.py`, `validate_nutrition_cuisine_immunity_isolation.py`, `validate_recipe_isolation.py`, `validate_item_textures.py`, `validate_task_abcd.py`, `validate_extended_features.py`, `validate_trade_tags.py`, `validate_external_extension_spec.py`, `validate_animal_food_tags.py`, `validate_catalyst_mixin.py`, `validate_p1_codex_advancements.py`, `validate_nature_jade.py`, and `verify_built_jars.py`.
- Client-only confirmation still required for Codex UI interaction/theme behavior, optional item-icon/nature-tag rendering, draggable scrollbars, Farmer+More live compat behavior, and raw-herb early-survival feel.


## 2026-06-18 Repository setup, automated validation pass, and Codex UI claim hitbox bugfix

### Fixed — Codex UI Claim Hitboxes
- Fixed a major UI logic bug in `CodexScreen.java` where clicking on errata (勘误) options / claim marks produced no reaction in-game.
- Root cause analysis revealed that `maxRightScroll(...)` invoked `contentHeight(...)` to determine the scrollbar thumb size, but `contentHeight(...)` contained an erroneous `this.claimHitboxes.clear()` call. This completely wiped out all interactive claim hitboxes at the end of every rendering frame (`extractRenderState`) and during mouse click evaluations (`mouseClicked`).
- Implemented robust fixes in `CodexScreen.java`:
  - Removed `this.claimHitboxes.clear()` from `contentHeight(...)` so mathematical height queries no longer destroy interactive UI state.
  - Implemented multi-line wrapped text height computation in `renderRightPanel` so hitboxes correctly span entire multi-line wrapped claims.
  - Added strict screen-area scissor bounds checks in `mouseClicked` so clicks outside the right panel area will never accidentally trigger scrolled-off hitboxes.
- Added automated validator `scripts/validate_codex_hitboxes.py` to AST-guard these hitboxes and screen bounds against regression.
- Authored `docs/reports/CODEX_CLAIM_HITBOX_BUGFIX_20260618.md`.

### Verified
- Professional Role Alignment frozen per author instruction (`GAME_PLANNER_AND_DEVELOPER_COLLABORATION_SPEC_20260618.md`): Author is Lead Game Planner & System Architect (游戏策划); Agent is Lead Professional Developer executing 100% of codebase, AST verifications, and Gradle operations.
- Resolved vital Planner technical errata regarding the item registry matching logic (`resolveHerbalItemId`): confirmed that traversal operates entirely in RAM (`Local Computer Memory`) within the JVM, executing zero network requests and causing zero lag or TPS regressions (`0.01ms` on-demand execution).
- Fully set up the development environment in Arena.ai's Agent Mode sandbox: provisioned JDK 25 (`Temurin-25.0.3+9`), verified build configurations, and compiled the complete `1.1.3` source line.
- Executed the complete automated project validator suite (20+ verification scripts), confirming 100% passing status across language parity, custom nutrition effects, nutrition correction math, data externalization, raw herb food rolls, essence resource coverage, Homeostatic compatibility, recipe/immunity isolation, villager trade tags, and P4 extension contracts.
- Transitioned to Yansheng (`严生`) Mode B hardcore survival kickoff, detailing baseline readiness and soliciting Planner acceptance criteria for visual UI concepts, pathological wound/temp interactions, and explicit requests for immediate Fabric subproject provisioning.
- Authored `docs/reports/ENVIRONMENT_SETUP_AND_VERIFICATION_20260618.md`.

### Output

- No source code or jar content was changed in this verification pass. Jars remain stable at `1.1.3`.
- Refreshed `release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip` to incorporate the new verification report and documentation updates.

## 2026-06-17 UI data-driven Layer 1+2 implementation

### Added — Layer 1 (Theme Config)

- Added `assets/herbcraft/ui/codex_theme.json` — data-driven theme for the Herb Codex UI. All hardcoded colors, sizes, paddings, row heights, arrow characters, and layout constants are now JSON-configurable. Resource packs can override this file to restyle the Codex.
- Added `assets/herbcraft/ui/nutrition_theme.json` — data-driven theme for the Nutrition Panel UI. All panel dimensions, bar colors, dimension-specific colors/icons, status colors, and auto-scaling rules are now JSON-configurable. The dimension list is extensible: addon mods can add entries to support extra nutrition dimensions.
- Added `CodexTheme.java` — theme loader for the Codex UI with 130+ configurable fields and full hardcoded fallbacks. Includes nature tag color maps for flavor/temperature.
- Added `NutritionTheme.java` — theme loader for the Nutrition Panel UI with 80+ configurable fields. Supports auto-scaling row height based on dimension count (20px for ≤5, 18px for 6, 16px for 7, 15px for 8+), dynamic panel height computation, and extensible dimension entries.
- Added UI theme reload listener in `HerbcraftCoreClient` so themes reload when resource packs change.
- Added `docs/reports/DECISIONS_UI_AND_HARDCORE_MOD_20260617.md` recording confirmed author decisions.

### Fixed — Text Readability on Light Paper Background

- Added `text.shadow` boolean to `codex_theme.json`. Light theme now renders text **without shadow** (shadow=false) to eliminate the dark-on-dark "ghosting" effect that made text hard to read on the warm xuan paper background.
- Adapted all text colors for the light theme: title is now dark forest green (`0xFF2D5A1E`), left panel text is very dark brown (`0xFF3B2A1A`), completion badge is warm brown (`0xFF6B4E2A`), separator is golden brown. These contrast heme: title is now dark forest green (`0xFF2D5A1E`), left panel text is very dark brown (`0xFF3B2A1A`), completion badge is warm brown (`0xFF6B4E2A`), separator is golden brown. These contrast properly against the pale cream paper.
- Dark theme retains shadow=true with bright text on dark background (the original look).

### Added — Light/Dark Theme Toggle

- Added `codex_theme_dark.json` as the dark alternate theme. It uses solid dark colors, no textures, and bright text with shadows — identical to the original pre-texture appearance.
- Press **T** while the Codex is open to instantly toggle between light (宣纸) and dark themes. The toggle is persistent within a session.
- `CodexTheme.toggleDarkMode()` switches the active theme file and reloads. Falls back to light theme if the dark file is missing.
- Added lang keys `book.herbcraft.codex.theme_toggle_hint` in both languages.

### Added — Pixel Art Textures and Texture Rendering

- Added `scripts/generate_codex_textures.py` that generates all Codex/Nutrition panel pixel art textures programmatically using Pillow.
- Generated 11 Minecraft-style pixel art textures:
  - `codex_background.png` (16×16) — aged xuan paper, warm cream with fiber and age spots
  - `codex_left_panel.png` (16×16) — slightly darker paper for the directory panel
  - `codex_header.png` (32×16) — dark red silk ribbon with diagonal weave sheen
  - `codex_divider.png` (4×32) — bamboo stalk divider with node rings
  - `codex_corner_tl/tr/bl/br.png` (8×8) — brass L-shaped corner fittings
  - `codex_scrollbar_track.png` (4×16) — semi-transparent warm track
  - `codex_scrollbar_thumb.png` (4×8) — bamboo-colored thumb
  - `nutrition_panel_bg.png` (16×16) — dark parchment for nutrition panel
  - `codex_back_button.png` (16×16) — bamboo-colored left arrow
- Added `blitTiled()` and `blitExact()` rendering helpers to `CodexScreen` for tiling textures across panel areas.
- `CodexScreen.extractRenderState()` now renders tiled textures for book background, header, left panel, divider, and corner decorations when enabled in `codex_theme.json`.
- Extended `CodexTheme` with `useHeaderTexture`, `headerTexture`, `leftUseBackgroundTexture`, `leftBackgroundTexture`, `dividerUseTexture`, `dividerTexture`, `scrollbarUseTexture`, `scrollbarTrackTexture`, `scrollbarThumbTexture`, and all four corner texture path fields.
- All texture features are enabled by default in the shipped `codex_theme.json`.
- Divider width updated to 4px to match the bamboo texture width.

### Added — Interactive Scrollbar

- Added scrollbar drag-to-scroll functionality to the Codex. Left-click and drag the scrollbar thumb on either the left directory panel or the right detail panel to scroll. The scrollbar track has a 2px click tolerance on each side for easy grab. Release to stop dragging.
- Implemented `mouseDragged()` and `mouseReleased()` overrides on `CodexScreen` to track drag state and compute scroll position proportionally from mouse movement. The thumb position maps linearly to the scroll range.

### Added — Layer 2 (Entry Component Rendering)

- Extended `CodexSnapshotPayload.EntrySnap` with `itemId`, `temperature`, and `flavors` fields. The server now resolves the Minecraft item ID and nature data for each Codex entry and sends them to the client.
- Added item icon rendering in the Codex right panel entry header: when `show_entry_item_icon: true` is set in `codex_theme.json`, the entry's actual 16×16 item texture renders to the left of the entry title (e.g., a dandelion texture next to "蒲公英").
- Added nature flavor tag rendering in the Codex right panel: when `nature_tags.enabled: true` is set, tasted/mastered entries display inline colored tags for temperature (寒/微寒/微温/辛热) and flavors (甘/苦/辛/酸/咸/涩). Colors are data-driven from `codex_theme.json`.
- Added `resolveEntryItemId()` to `CodexSnapshotPayload` that maps entry IDs back to Minecraft item IDs for herbs, food-nature entries, and ingredient entries. Guide/overview entries return empty (no icon).

### Changed

- Refactored `CodexScreen.java` to read all layout/color constants from `CodexTheme`. Added visible scrollbar rendering, completion badge support, full-width claim hitboxes, item icon header rendering, and nature tag rendering. All disabled by default to preserve current visual behavior.
- Refactored `NutritionPanelScreen.java` to read all layout/color/dimension constants from `NutritionTheme`. Supports dynamic dimension count with auto-scaling, threshold marks on bars, and data-driven dimension colors/icons.
- Extended `CodexSnapshotPayload` serialization to include the 3 new Layer 2 fields per entry while maintaining wire-compatible ordering.

### Architecture

- UI Layer 1 (theme config JSON) is implemented. All visual constants are data-driven.
- UI Layer 2 (entry component rendering) is implemented. Item icons, nature tags, and structured claim hitboxes are supported and controlled by theme JSON.
- UI Layer 3 (declarative layout engine) is deferred to a future version.
- Default theme values exactly match previous hardcoded constants. Layer 2 features are disabled by default (`show_entry_item_icon: false`, `nature_tags.enabled: false`). To activate them, set the flags to `true` in `codex_theme.json`.

### Validation

- Gradle build passed with Java 25.
- Full validator suite passed: `check_lang_diff.py`, `validate_externalized_rules.py`, `validate_external_extension_spec.py`, `validate_nutrition_correction_phase.py`, `validate_p1_codex_advancements.py`, `validate_raw_herb_food_balance.py`, `validate_custom_nutrition_effects_phase8.py`, `validate_nature_jade.py`, `validate_catalyst_mixin.py`, `verify_built_jars.py`.
- Release output jars and source zip were refreshed.

## 2026-06-17 UI data-driven architecture and Hardcore mod deep discussion

### Added

- Added `docs/reports/UI_DATADRIVEN_AND_HARDCORE_MOD_DEEP_DISCUSSION_20260617.md`, a deep-dive discussion covering:
  - Three-layer data-driven UI architecture (theme JSON → component registry → declarative layout).
  - Pixel-level Codex beautification spec with 10+ texture requirements, entry detail page component layout, breadcrumb/back button/scrollbar/search bar design.
  - Hardcore mod positioning analysis: Mode A (pure addon), Mode B (independent hardcore mod + compat bridge, recommended), Mode C (lib split, deferred).
  - Health slider / magnetic bar concept: 0-100 health index with 5 anchor zones, computed from nutrition balance + temperature stability + hydration + poison/debuff history + medicine history.
  - Season/climate herb interaction design: summer=bitter/cold is good, winter=pungent/hot is good ("因时制宜").
  - cocoa_fat confirmed as placeholder only, not a planned feature.
  - Codex completion statistics design.
  - 6 open design questions for author decision.
- Added `docs/reports/UI_REVIEW_AND_HARDCORE_NUTRITION_ADDON_DISCUSSION_20260617.md` (previous turn, retained).

### Findings

- UI data-driven conversion is recommended and feasible; split into theme config (1.2.0), component registry (1.2.0), and declarative layout (future).
- Hardcore mod should be an independent core mod (`严生` / Harsh Survival) with a Herbcraft compat bridge, not a pure Herbcraft addon. This lets it own temperature, hydration, medical systems, and seasonal herb mechanics independently.
- Codex beautification requires specific textures (background paper, header, divider, corners, scrollbar), entry detail item icons, nature flavor tags, and breadcrumb navigation — all driven by `codex_theme.json`.
- Nutrition panel auto-scaling for N dimensions is achievable with `nutrition_theme.json` + `auto_scale_rows`.

### Output

- Documentation/report-only pass. No source, jars, or release output changed.

## 2026-06-17 final AI/addon/wiki handoff docs

### Added

- Added `docs/AI_COLLABORATION_GUIDE.md` with copy/paste prompts and workflow rules for future AI/helper sessions.
- Added `docs/ADDON_AUTHOR_QUICKSTART_1_1_3.md`, a practical decision-tree guide for datapack authors, modpack authors, and code-addon authors.
- Added `docs/wiki/README.md` describing where to keep canonical Wiki source and how to split the full-system Wiki later.

### Changed

- Updated `docs/DOCUMENTATION_INDEX.md` and `docs/EXTENDING_HERBCRAFT.md` to point to the new handoff/addon/wiki guides.
- Clarified recommended Wiki hosting: keep canonical Markdown in the repository under `docs/wiki/`, then optionally mirror to GitHub Wiki or platform pages. No separate website is needed now.

### Output

- Documentation-only pass; source zip refreshed.

## 2026-06-17 final datapack integration verification and runtime test pack

### Fixed

- Fixed `IngredientsChapter.applyExternalData(...)` reload behavior so item entries reset to the bundled baseline before applying external datapack entries. This prevents a datapack override/removal from deleting built-in vanilla ingredient entries until restart.

### Added

- Added `docs/reports/DATAPACK_RUNTIME_COMPAT_VERIFICATION_20260617.md`, documenting the final source-level verification of reload-safe datapack integration across recognition, Codex UI, tooltip/Jade display, nutrition, food-nature pharmacology, traits/seven relations, and special counters.
- Added a runtime test datapack under `docs/compat_drafts/p4_runtime_datapack_test/` and zipped it as `release_output/herbcraft_test_20260614_review/herbcraft-p4-runtime-test-datapack-20260617.zip`.

### Test datapack

- The test datapack uses vanilla items `minecraft:honey_bottle`, `minecraft:bread`, and `minecraft:baked_potato`.
- It provides `nutrition_profiles`, `ingredients`, `herb_natures`, and a custom external `special_counter` ID to verify datapack-added metadata and dynamic counter storage in a real client.

### Validation

- Gradle build passed with Java 25.
- Full validator suite from `CURRENT_BASELINE.md` passed.
- Release output jars, P4 runtime test datapack zip, and source zip were refreshed.

## 2026-06-17 edge P1/P2 fixes

### Fixed

- Replaced hardcoded player special-counter fields with dynamic counter storage keyed by counter ID. External `special_counters` threshold effects can now count arbitrary IDs instead of only `poppy`, `torchflower`, `sugar`, and `slime`.
- Added migration from legacy built-in counter NBT fields into the new dynamic `HerbcraftWindowCounters` storage.
- Aggregated missing optional external ingredient rows into a compact info log instead of warning once per missing target-mod item.
- Hardened `SpecialCounters` item resolution with registry presence checks before resolving item IDs.

### Added

- Added `docs/reports/EDGE_P1_P2_FIXES_20260617.md`.

### Validation

- Gradle build passed with Java 25.
- Full validator suite from `CURRENT_BASELINE.md` passed.
- Release output jars and source zip were refreshed.

## 2026-06-17 edge/regression source review

### Added

- Added `docs/reports/EDGE_REGRESSION_REVIEW_20260617.md`, a source-level review of the uploaded edge/regression checklist covering milk/effect clearing, cancelAll behavior, performance risks, death/offline/dimension boundaries, numeric clamps, information gating, reload safety, compatibility, and interaction edge cases.

### Findings

- Confirmed by source: milk-triggered nutrition re-evaluation should restore nutrition effects, milk does not clear qi/herbStack/nutrition data, Cuisine clear-all skips nutrition effects, and `xiang_fan` cancelAll does not swallow later tasted/event/nutrition paths.
- Initial review found that external `special_counters` could load arbitrary IDs while player counter persistence was hardcoded to built-in counters; this was fixed in the later edge P1/P2 fixes entry.
- Initial review noted possible warning spam if bundled optional Farmer+More compat data is loaded without target mods installed; this was also reduced in the later edge P1/P2 fixes entry.

### Output

- Documentation/report-only pass; jars unchanged.
- Source zip refreshed.

## 2026-06-17 full system Wiki draft

### Added

- Added `docs/HERBCRAFT_FULL_SYSTEM_WIKI_1_1_3.md`, a large mechanism-level Wiki draft for Herbcraft `1.1.3`.
- The document covers Codex knowledge, 食材录, 药食志, raw herb eating, `food_rolls`, 药性回旋, 性味/五味/特质/气机/七情, nutrition and correction math, Cuisine, Alchemy, Farmer+More compatibility, P4 extension APIs, and suggested Wiki page splits.

### Notes

- This is documentation-only and intentionally not a complete item-spoiler encyclopedia. It is meant to be the source document for later player-facing and developer-facing Wiki pages.

### Output

- Source zip was refreshed. Jars unchanged.

## 2026-06-17 final client sanity pass recorded

### Notes

- Author reported the current `1.1.3` release-candidate jars pass the final client sanity pass.
- Farmer+More built-in compatibility now works without the standalone datapack, including 食材录/nutrition, 药食志, item tooltip/Jade food-nature display, and guide discovery behavior.
- Raw `farmersdelight:rice`, `farmersdelight:cabbage_seeds`, and `farmersdelight:tomato_seeds` remain absent from final compat entries.

### Output

- No jar rebuild was needed in this note-only pass.
- Source zip was refreshed to include the updated handoff docs and generated current agent brief.

## 2026-06-17 Farmer+More raw rice removal and agent handoff automation

### Fixed

- Removed non-edible `farmersdelight:rice` from the final Farmer+More compat data. `farmersdelight:cooked_rice` remains as the correct edible rice entry for 食材录/nutrition.
- Regenerated the reference compat datapack and bundled Core compat data; final Farmer+More counts are now 108 nutrition profiles, 108 ingredient entries, 5 herb nature entries, and 0 rumors.

### Added

- Added `scripts/generate_agent_brief.py`, which generates `docs/reports/CURRENT_AGENT_BRIEF.md` from current baseline, next task, changelog, docs index, and release output files.
- Added root `AGENT_BRIEF.md` as a one-file entry point for future AI/helper sessions.
- Updated `AI_ONBOARDING.md` to require regenerating and reading the current agent brief before coding.

### Changed

- Updated final Farmer+More docs/tables/reports for raw rice removal and clarified that the generated datapack is reference/example material only.
- Updated docs/reports/current handoff so future agents are directed to current source/resources and JSON specs instead of stale chat memory.

### Validation

- Gradle build passed with Java 25.
- Full validator suite from `CURRENT_BASELINE.md` passed.
- Release output jars and source zip were refreshed.

## 2026-06-17 food-nature tooltip/Jade/guide bug fix

### Fixed

- Fixed Farmer+More food-nature items not appearing in Herbcraft inventory tooltips or Jade despite having 药食志 data. Tooltips and Jade now recognize ordinary foods with explicit nature + nutrition + 食材录 data and use their namespace-aware 药食志 entry IDs.
- Fixed food-nature entries not counting toward 五味与性味 / 七情与配伍 guide gating and discovered flavor counts.
- Fixed food-nature flavor discoveries being skipped when ordinary foods had no direct Herbcraft herb effects; the bridge now records relevant flavor discoveries for inert food-nature entries.
- Hardened item-name display against missing optional item IDs by checking registry presence before resolving display names.

### Changed

- Updated `HerbNatureItemTooltips`, `HerbJadeData`, `HerbalChapter`, and `PharmacologyResolver` for food-nature display and guide integration.
- Added `docs/reports/FOOD_NATURE_DISPLAY_AND_GUIDE_FIX_20260617.md`.
- Strengthened `scripts/validate_external_extension_spec.py` checks for food-nature tooltip/Jade/guide bridge tokens.

### Validation

- Gradle build passed with Java 25.
- Full validator suite from `CURRENT_BASELINE.md` passed, including `validate_nature_jade.py`, `validate_external_extension_spec.py`, and `verify_built_jars.py`.
- Release output jars and source zip were refreshed.

## 2026-06-17 1.1.3 version bump and release-candidate packaging

### Changed

- Bumped all modules from `1.1.2` to `1.1.3` in Gradle and `fabric.mod.json`.
- Alchemy and Cuisine now depend on Herbcraft Core `>=1.1.3`.
- Updated `scripts/verify_built_jars.py` for `1.1.3` jar names and metadata.
- Updated README, current baseline, nes reference/example material only because older jars lack the food-nature bridge.

### Output

- Refreshed local jars in `release_output/herbcraft_test_20260614_review/`:
  - `herbcraft-1.1.3.jar`
  - `herbcraft-alchemy-1.1.3.jar`
  - `herbcraft-cuisine-1.1.3.jar`
- Removed old `1.1.2` jars and the standalone Farmer+More compat zip from `release_output/` to avoid accidental upload.
- Refreshed `release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip` while keeping the required source zip name.

### Validation

- Gradle build passed with Java 25.
- Full validator suite from `CURRENT_BASELINE.md` passed, including built-jar verification for `1.1.3`.

## 2026-06-17 P4.5 validators/docs final sweep

### Added

- Added `docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md`, summarizing P4.1-P4.5 completion, validator coverage, example status, release-readiness, and remaining client tests.

### Changed

- Synchronized P4 status across `docs/JSON_EXTENSION_SPEC_1_1_3.md`, `docs/PUBLIC_EXTENSION_API_DESIGN.md`, `docs/EXTENDING_HERBCRAFT.md`, `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md`, `docs/DOCUMENTATION_INDEX.md`, `CURRENT_BASELINE.md`, and `NEXT_TASK.md`.
- Updated `scripts/validate_external_extension_spec.py` to validate P4 readiness documentation tokens in addition to JSON examples, read-only API, event API, bundled Farmer+More compat, and the formal JSON spec.
- Confirmed the Java addon example remains documentation-only for 1.1.3; a fully compilable standalone sample is optional future work, not a release blocker.

### Validation

- Gradle build passed with Java 25.
- `scripts/check_lang_diff.py`, `scripts/validate_externalized_rules.py`, `scripts/validate_external_extension_spec.py`, `scripts/validate_nutrition_correction_phase.py`, `scripts/validate_p1_codex_advancements.py`, and `scripts/verify_built_jars.py` passed.
- Release output jars and source zip were refreshed.

## 2026-06-17 P4.4 example addon/datapack

### Added

- Refreshed `docs/examples/external_extension_compat_pack/` as a P4.4 JSON datapack example following `docs/JSON_EXTENSION_SPEC_1_1_3.md`.
- Added `docs/examples/herbcraft_readonly_event_addon/`, a documentation-only Fabric addon skeleton demonstrating `HerbcraftDataAPI` and `HerbcraftEvents`.
- Added `docs/reports/P4_4_EXAMPLES_20260617.md`.

### Changed

- The JSON example now uses vanilla IDs and demonstrates `herb_natures`, `nutrition_profiles`, `ingredients`, `knowledge_rumors`, `codex_loot_injections`, and `special_counters`.
- The Java example demonstrates read-only data queries and observational events without content registration/write APIs.
- Updated P4 docs and `NEXT_TASK.md` to mark P4.4 complete and P4.5 final validator/docs sweep as next.
- Updated `scripts/validate_external_extension_spec.py` to validate the P4.4 JSON and Java examples.

### Validation

- `scripts/validate_external_extension_spec.py` passed.
- No Java runtime code or jars changed in this P4.4 pass; source zip was refreshed.

## 2026-06-17 P4.3 observational event API

### Added

- Added `core/src/main/java/com/herbcraft/api/HerbcraftEvents.java`, a public observational event API for code addons.
- Added events: `HERB_CONSUMED`, `NUTRITION_APPLIED`, `INGREDIENT_DISCOVERED`, `FOOD_NATURE_RESOLVED`, and `EXTERNAL_DATA_RELOADED`.
- Added `docs/reports/P4_3_EVENT_API_20260617.md`.

### Changed

- `HerbConsumeEvents` now delegates to `HerbcraftEvents.HERB_CONSUMED` and remains as a compatibility shim.
- `NutritionManager` fires nutrition-applied and first ingredient-discovered events.
- `PharmacologyResolver` fires food-nature-resolved events after the nature bridge computes pharmacology.
- `HerbcraftExternalDataReloadListener` fires external-data-reloaded events after the reload-safe merge completes and sync is marked dirty.
- Updated P4 docs to mark P4.3 complete and P4.4 examples as next.

### Validation

- Gradle build passed with Java 25.
- `scripts/check_lang_diff.py`, `scripts/validate_externalized_rules.py`, `scripts/validate_external_extension_spec.py`, `scripts/validate_nutrition_correction_phase.py`, `scripts/validate_p1_codex_advancements.py`, and `scripts/verify_built_jars.py` passed.
- Validators now check event API source tokens and built Core jar event classes.

## 2026-06-17 P4.2 read-only Java API

### Added

- Added `core/src/main/java/com/herbcraft/api/HerbcraftDataAPI.java`, a read-only facade for code addons to query Herbcraft data without mutating registries.
- Added `docs/reports/P4_2_READONLY_JAVA_API_20260617.md`.

### API surface

- The new API exposes read-only queries for item registry presence, nature profiles, nutrition profiles, 食材录 entry IDs, Herbcraft herb definitions, food-nature/药食志 entries, and current profile snapshots.
- No Java write/registration API was added. Content registration remains JSON-first.

### Changed

- Updated `docs/PUBLIC_EXTENSION_API_DESIGN.md`, `docs/EXTENDING_HERBCRAFT.md`, `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md`, `docs/JSON_EXTENSION_SPEC_1_1_3.md`, and `docs/DOCUMENTATION_INDEX.md` to mark P4.2 complete and P4.3 events as next.
- Updated `scripts/validate_external_extension_spec.py` to check the read-only API source tokens and reject obvious write-registration methods.
- Updated `scripts/verify_built_jars.py` to verify `com/herbcraft/api/HerbcraftDataAPI.class` is included in the Core jar.

### Validation

- Gradle build passed with Java 25.
- `scripts/check_lang_diff.py`, `scripts/validate_externalized_rules.py`, `scripts/validate_external_extension_spec.py`, `scripts/validate_nutrition_correction_phase.py`, `scripts/validate_p1_codex_advancements.py`, and `scripts/verify_built_jars.py` passed.
- Release output jars and source zip were refreshed.

## 2026-06-17 P4.1 JSON extension spec formalization

### Added

- Added `docs/JSON_EXTENSION_SPEC_1_1_3.md`, the formal P4.1 JSON extension contract for Herbcraft's public data extension surface.
- The spec covers reload-safe paths, the startup-only `herbs` path, schema shapes, allowed nature/flavor/trait/nutrition values, merge/override rules, client sync expectations, bundled Farmer+More optional compat, and explicit non-goals.

### Changed

- Updated `docs/EXTENDING_HERBCRAFT.md`, `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md`, `docs/PUBLIC_EXTENSION_API_DESIGN.md`, and `docs/DOCUMENTATION_INDEX.md` to point to the formal JSON spec.
- Updated `NEXT_TASK.md` to make P4.2 read-only Java API the next extension milestone.
- Updated `scripts/validate_external_extension_spec.py` so the validator now checks for the formal JSON spec and required schema/boundary tokens.

### Validation

- `scripts/validate_external_extension_spec.py` passed.
- No Java code or jars changed in this P4.1 pass; source zip was refreshed.

## 2026-06-17 food-nature Codex completion and missing-item guard

### Fixed

- Fixed the Farmer+More food-nature bridge feeling incomplete: nature-bearing ordinary foods now register in 百草志 under `药食志` after tasting, while their nutrition remains in 食材录.
- Fixed missing optional target-mod items appearing as `minecraft:air` placeholder entries by checking `BuiltInRegistries.ITEM.containsKey(id)` before resolving item IDs for external ingredient/herb-food and food-nature Codex entries.
- Removed non-edible Farmer's Delight seed rows (`farmersdelight:cabbage_seeds`, `farmersdelight:tomato_seeds`) from the final Farmer+More compat data instead of showing entries players can never truly taste/master.

### Changed

- Final Farmer+More compat counts are now 108 nutrition profiles, 108 ingredient entries, 5 herb nature entries, and 0 rumors.
- `药食志` is registered only when actual nature-bearing food entries exist; unloading Farmer's Delight should no longer leave Air-like placeholder entries.
- `validate_external_extension_spec.py` derives final Farmer+More counts from the CSV, forbids RMF and non-edible seed rows, and checks the bridge/Codex source tokens.

### Validation

- Gradle build passed with Java 25.
- `scripts/check_lang_diff.py`, `scripts/validate_externalized_rules.py`, `scripts/validate_external_extension_spec.py`, `scripts/validate_nutrition_correction_phase.py`, `scripts/validate_p1_codex_advancements.py`, and `scripts/verify_built_jars.py` passed.
- Release output jars, standalone Farmer+More compat zip, and source zip were refreshed.

## 2026-06-17 built-in Farmer+More compat and food-nature pharmacology bridge

### Added

- Bundled Farmer's Delight Refabricated + More Delight optional compat data into Core under `data/farmers_moredelight_herbcraft/herbcraft/`: 108 nutrition profiles, 108 ingredient entries, and 5 herb nature entries.
- Added a generic food-nature pharmacology bridge: ordinary consumed foods with explicit `herb_natures` and nutrition profiles can now participate in qi/flavor/seven-emotion pharmacology without becoming startup `herbs` entries.
- Added 百草志 `药食志` entries for explicit food-nature items so foods that trigger nature pharmacology are also visible in the Herbal Codex after tasting.
- Added `docs/reports/FARMER_MORE_BUILTIN_COMPAT_AND_NATURE_BRIDGE_20260617.md`.

### Changed

- `ItemMixin` now invokes the nature-only pharmacology bridge for non-Herbcraft-herb foods after nutrition recording.
- `PharmacologyResolver` now has `resolveFoodNatureOnly(...)`, which uses an inert herb entry to run nature pharmacology while leaving target-mod FOOD/CONSUMABLE components untouched.
- Removed non-edible Farmer's Delight seed rows (`cabbage_seeds`, `tomato_seeds`, `rice`) from the final compat data instead of pretending they can be tasted.
- `validate_external_extension_spec.py` now guards the bundled Farmer+More data counts, no-RMF item set, and food-nature bridge source tokens.

### Validation

- Gradle build passed with Java 25.
- `scripts/check_lang_diff.py`, `scripts/validate_externalized_rules.py`, `scripts/validate_external_extension_spec.py`, `scripts/validate_nutrition_correction_phase.py`, `scripts/validate_p1_codex_advancements.py`, and `scripts/verify_built_jars.py` passed.
- Release output jars and source zip were refreshed.

## 2026-06-17 food-mod compat Round 5 client pass and RMF removal

### Added

- Added `docs/compat_drafts/food_mods_26_1_2/compat_review_round5_farmer_moredelight.csv`, filtering the final compat source table down to Farmer's Delight Refabricated + More Delight.
- Added `docs/compat_drafts/food_mods_26_1_2/ROUND5_FINAL_CHINESE_NATURE_TABLE.md` with the final 6 Chinese herb nature rows after RMF removal.
- Created final local compat zip `release_output/herbcraft_test_20260614_review/herbcraft-farmers-moredelight-compat-client-pass-20260617.zip` and removed the older three-mod Round 5 draft zip to avoid accidental RMF use.

### Changed

- Regenerated `docs/compat_drafts/food_mods_26_1_2/generated_pack/` from the Round 5 Farmer+More CSV using namespace `farmers_moredelight_herbcraft`.
- Removed all `rmf:*` entries from the generated compat pack after author client testing showed RMF's datapack-driven item identity can collide with vanilla/Herbcraft-known items and is not worth supporting in the main compat pack.
- Updated Round 5 report, active tracker, README, documentation index, external extension spec, baseline, and next-task handoff to mark Farmer's Delight + More Delight as the client-tested release-candidate scope.
- Coffee-bean design remains deferred because coffee beans were only present through RMF in this batch.

### Validation

- Final generated pack validation passed: 108 nutrition profiles, 108 ingredient entries, 5 herb nature entries, and 0 knowledge rumors.
- Confirmed no `rmf:*` IDs remain in the final CSV, generated pack, or final zip.
- Final zip root/path/count check passed.
- `scripts/validate_external_extension_spec.py` passed.
- No Java code or release jars changed; source zip was refreshed.

## 2026-06-17 food-mod compat Round 5 preflight/package

### Added

- Added `docs/compat_drafts/food_mods_26_1_2/ROUND5_CLIENT_TEST_CHECKLIST.md` with the required local real-client test matrix for Farmer's Delight Refabricated, More Delight, Reg's More Foods, and the generated compat datapack.
- Added `docs/compat_drafts/food_mods_26_1_2/ROUND5_CLIENT_TEST_REPORT.md` recording Round 5 preflight status, the hard boundary that a real Minecraft client cannot be run in this sandbox, and the current publish decision: HOLD.
- Created the zipped client-test datapack `release_output/herbcraft_test_20260614_review/herbcraft-food-mods-compat-draft-round5-20260617.zip`.

### Changed

- Updated the active compatibility tracker, draft README, documentation index, external extension spec, baseline, and next-task handoff to record Round 5 preflight as prepared but real-client execution still pending.
- Recorded the author's coffee-bean design note for a later pass: coffee beans can be treated as processed cocoa beans, possibly with lower side-effect risk, longer positive buff, a Haste-like direction, or bitter/settling behavior. No coffee-bean data or Java mechanic was changed in Round 5.
- Documented an important current-source caveat: external `herb_natures` rows load/sync as metadata, but ordinary third-party foods do not automatically become full Herbcraft herbal entries unless also registered through the startup-only `herbs` path or used by a system that explicitly queries nature by item ID.

### Validation

- Checked the Round 5 zipped datapack root contains `pack.mcmeta` and the generated nutrition, ingredient, and herb nature JSON paths.
- Confirmed generated pack counts remain 317 nutrition profiles, 317 ingredient entries, 14 herb nature entries, and 0 knowledge rumors.
- `scripts/validate_external_extension_spec.py` passed.
- No Java code or release jars changed in this round; source zip was refreshed to include the Round 5 reports.

## 2026-06-17 food-mod compat Round 4 regeneration/validation

### Added

- Added `docs/compat_drafts/food_mods_26_1_2/ROUND4_GENERATION_VALIDATION_NOTES.md` documenting Round 4 regeneration commands, generated files, counts, and validation checks.
- Added `docs/compat_drafts/food_mods_26_1_2/ROUND4_CHINESE_NATURE_TABLE.md`, a Chinese-readable table for all 14 generated herb nature rows, including Chinese names, 食材录分类, nutrition draft, 性、性味、入口味、特质, notes, and a Chinese property glossary.

### Changed

- Regenerated `docs/compat_drafts/food_mods_26_1_2/generated_pack/` from `compat_review_round3.csv` under namespace `food_mods_herbcraft`.
- Updated the active compatibility tracker, draft README, documentation index, external extension spec, baseline, and next-task handoff to mark Round 4 complete and Round 5 real-client testing as the next active round.

### Validation

- Round 4 generated pack validation passed: 317 nutrition profiles, 317 ingredient entries, 14 herb nature entries, and 0 knowledge rumors.
- JSON/CSV consistency checks confirmed schema version, valid dimensions/sections/nature values, no duplicate item IDs, and generated item sets matching `compat_review_round3.csv`.
- `scripts/validate_external_extension_spec.py` passed.
- No Java code or release jars changed in this round; source zip was refreshed to include generated pack and documentation updates.

## 2026-06-17 food-mod compat Round 3 nature/rumor review

### Added

- Added `docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv`, preserving 317 Round 2 draft entries while revising the optional herb nature and rumor columns.
- Added `docs/compat_drafts/food_mods_26_1_2/ROUND3_NATURE_RUMOR_NOTES.md` with kept/removed/deferred nature decisions, RMF caveats, and the Round 4 regeneration command.

### Changed

- Reduced draft herb nature rows from 18 to 14: kept conservative crop/seed/grain/bulb profiles, removed generic RMF fruit/berry natures, and added `rmf:coffee_beans` as a clearly botanical stimulant candidate.
- Left all `rumor_key` fields blank for the first compat draft to avoid noisy hearsay without dedicated language resources.
- Updated the active compatibility tracker, draft README, documentation index, external extension spec, baseline, and next-task handoff to mark Round 3 complete and Round 4 regeneration/validation as the next active round.

### Validation

- Smoke-tested `scripts/generate_compat_pack.py` against `compat_review_round3.csv` into `/tmp/herbcraft_round3_pack`: 317 nutrition profiles, 317 ingredient entries, 14 herb nature entries, and 0 knowledge rumors.
- `scripts/validate_external_extension_spec.py` passed, and a CSV count check confirmed 317 rows by source (`farmersdelight` 82, `moredelight` 29, `rmf` 206).
- No Java code or release jars changed in this round; source zip was refreshed to include the documentation/CSV updates.

## 2026-06-16 food-mod compat Round 2 review

### Added

- Added `docs/compat_drafts/food_mods_26_1_2/compat_review_round2.csv`, a cleaned and retuned draft for Farmer's Delight Refabricated, More Delight, and Reg's More Foods compatibility.
- Added `docs/compat_drafts/food_mods_26_1_2/round2_removed_false_positives.csv` recording removed non-food/tool/model/helper entries.
- Added `docs/compat_drafts/food_mods_26_1_2/ROUND2_REVIEW_NOTES.md` with Round 2 counts, removal rules, nutrition approach, and RMF caveats.

### Changed

- Updated the active compatibility tracker report to mark Round 2 complete and Round 3 herb nature/rumor selection as the next active round.
- Round 2 keeps 317 entries, removes 79 false positives, and previews as 317 nutrition profiles, 317 ingredient entries, and 18 herb nature entries.

### Notes

- Round 2 is still a draft. Values require author review and client testing before any compatibility pack publication.

## 2026-06-16 external ingredients, client sync, compat generator, P4 design

### Added

- Added external 食材录 / Ingredient Codex extension path: `data/<namespace>/herbcraft/ingredients/*.json`.
- Added server-to-client data sync for nutrition profiles, herb nature profiles, and ingredient item-to-entry IDs so external datapack/addon data can appear correctly in client tooltips/Codex-facing display.
- Added `scripts/generate_compat_pack.py`, a CSV-to-datapack draft generator for nutrition profiles, ingredient entries, optional herb natures, and rumor skeletons.
- Added `docs/PUBLIC_EXTENSION_API_DESIGN.md` as the P4 Java API design draft.
- Added `docs/reports/EXTERNAL_INGREDIENTS_CLIENT_SYNC_GENERATOR_API_20260616.md`.
- Added an external ingredient example under `docs/examples/external_extension_compat_pack/data/example_compat/herbcraft/ingredients/`.

### Changed

- Ingredient Codex external entries now use namespace-aware internal entry IDs such as `farmersdelight__cabbage`, avoiding collisions with other mods that share item paths.
- Nutrition tooltip gating now uses the synced namespace-aware Ingredient Codex entry IDs.
- Updated data-driven architecture, extension, external spec, documentation index, and validators to describe the new external ingredient/client sync surfaces.

### Validation

- Gradle build passed with Java 25. Full validator suite passed. The new compat generator was smoke-tested with its generated example CSV and output datapack.

## 2026-06-16 English localization sweep

### Changed

- Changed the Herb Codex English chapter prefix from `Ch.%s · %s` to `Chapter %s · %s` so the directory no longer looks partially untranslated.
- Standardized current Alchemy English visible wording from `Turbid` to `Murky`, matching the `_murky` route and guide wording.
- Aligned English nutrition wording from `Vege` / `Vegetable Deficiency` to `Greens` / `Greens Deficiency`, and changed the Ingredient Codex produce section from `Fruits & Vegetables` to `Produce`.
- Updated current `1.1.2` release page copy to use `Greens` in English nutrition descriptions.

### Validation

- `scripts/check_lang_diff.py` now also fails on CJK text in `en_us`, ambiguous `Ch.`, or old `Turbid` wording in current English language files.
- Pending full build/validator rerun in this entry until this modifying turn completes.

## 2026-06-16 MC百科 release copy

### Added

- Added `docs/MCMOD_RELEASE_COPY_1.1.2.md` with MC百科-oriented Chinese module descriptions, metadata templates, relationship/dependency suggestions, per-module `1.1.2` update-log templates, and a generic future资料 page template.
- The document records MC百科 editing cautions such as objective wording, avoiding casual/subjective language, filling original names/relationships clearly, and only using the `BCKS` abbreviation if the author treats it as a real public search abbreviation.

## 2026-06-16 nutrition panel text overflow fix

### Fixed

- Fixed Nutrition panel text overflowing outside the panel in English and other long translations.
- The panel now uses Codex-style safeguards: title/hint trimming, dimension-label trimming, wrapped centered status text, and a panel-level scissor guard.
- Added validator coverage in `validate_nutrition_correction_phase.py` so the Nutrition panel keeps trim/wrap/scissor protection.
- Added `docs/reports/NUTRITION_PANEL_OVERFLOW_FIX_20260616.md`.

### Validation

- Gradle build passed with Java 25. Nutrition correction validator, language parity, built-jar verification, and the general validator suite passed.

## 2026-06-16 1.1.2 release page copy

### Added

- Added `docs/RELEASE_PAGE_COPY_1.1.2.md` with platform notes, metadata/dependency matrix, English-first Modrinth/CurseForge-ready descriptions for Core, Alchemy, and Cuisine, per-module `1.1.2` changelogs, and a shared upload changelog.
- The copy includes Chinese descriptions after English and mentions the BCKS / Baicao Keshi search abbreviation for Core.

### Notes

- The copy was written around CurseForge and Modrinth description/summary/dependency expectations: clear functional descriptions, English-first multilingual presentation, no external file download links, and explicit version dependencies.

## 2026-06-16 1.1.2 packaging and Cuisine catalyst wording

### Changed

- Bumped all three modules from `1.1.1` to `1.1.2` in Gradle and `fabric.mod.json`; Alchemy/Cuisine now depend on `herbcraft >=1.1.2`.
- Updated `scripts/verify_built_jars.py` for the `1.1.2` jar names and metadata.
- Updated current handoff/docs to describe the source line as packaged `1.1.2` while preserving the required source zip name `herbcraft-source-current-20260614.zip`.
- Clarified the Cuisine Codex page `隐秘菜肴与炼金催化` / `Hidden Meals and Catalysts`: alchemy catalyst linkage now explicitly says the Alchemy chapter/module must be present (`炼金分册` / `Alchemy chapter`).
- Strengthened `validate_p1_codex_advancements.py` so the Cuisine hidden catalyst guide keeps that Alchemy-module requirement visible in both Chinese and English.

### Validation

- Gradle build passed with Java 25. Full validator suite passed, including updated Cuisine hidden-catalyst wording validation and built-jar verification for `1.1.2` metadata/resources/classes.

## 2026-06-16 documentation/source consistency audit

### Added

- Added `docs/DOCUMENTATION_INDEX.md` to separate current authoritative docs from historical reports/release-copy snapshots.
- Added `docs/reports/DOCS_AND_SOURCE_CONSISTENCY_AUDIT_20260616.md` recording the documentation and source audit pass.

### Changed

- Updated current handoff and planning docs (`README.md`, `AI_ONBOARDING.md`, `PROJECT_STATE.md`, `NEXT_TASK.md`, `BUGS_TODO.md`, `CHANGE_IMPACT.md`, `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md`, `docs/HOMEOSTATIC_COMPAT.md`, `docs/NEW_PLAYER_GUIDANCE_1_1_2_DESIGN.md`, and roadmap docs) so they no longer describe implemented P3.10/Codex guidance work as pending.
- Marked historical documents and release-copy snapshots with clear warnings so future maintainers do not treat old design notes as current source truth.
- Tightened nutrition rule consistency: `NutritionManager` now uses `NutritionRules.balancedMin`, `balancedMax`, and `imbalanceHigh` for dimension bands; `PlayerNutritionState` now uses `NutritionRules.imbalanceSpread` instead of a hardcoded `500`. Removed the old unused `applyFluidSurplusDecay` helper.
- Strengthened validators so new Codex guide line2 text and data-backed nutrition thresholds/spread are guarded.

### Validation

- Gradle build passed with Java 25. Full validator suite passed, including updated nutrition correction consistency validation, Codex guide validation, raw herb balance validation, Homeostatic compat validation, language parity, and built-jar verification.

## 2026-06-16 Codex pharmacology guide microcopy

### Changed

- Added plain beginner-facing explanatory lines to the Core Codex guide pages `五味与性味` / `Nature and Flavors` and `七情与配伍` / `Relations Between Herbs`.
- The new wording clarifies that 性味 is not nutrition: heat/cold can accumulate or harmonize, while flavors gently adjust effect chance, duration, strength, and side effects.
- The 七情 wording now says directly that relations are short-window interactions between recently eaten herbs, not a fixed recipe list, and that triggered relations leave recent examples in the Codex.
- Kept this guidance inside the Codex rather than adding new advancements, preserving advancements as route signs and the Codex as the teaching surface.

### Validation

- Gradle build passed with Java 25. Full validator suite passed, including language parity, P1 Codex guide validation, and built-jar verification.

## 2026-06-16 seed and sugar cane food-roll feel tuning

### Changed

- Increased seed hidden-saturation roll amounts after client testing showed the first P3.10 values were too weak: wheat/beetroot/torchflower seeds now roll `S+2.0`, while melon/pumpkin seeds roll `S+3.0` to reflect their oilier identity.
- Increased `minecraft:pitcher_pod` stable saturation modifier to `1.0`, making its stable +1 visible hunger feel like a rarer pod rather than a barely lasting nibble.
- Increased `minecraft:sugar_cane` saturation modifier to `1.0`: it still restores only +1 visible hunger, but now gives about +2 hidden saturation points.
- Confirmed current Homeostatic sugar cane hydration data is already `amount 3`; updated `validate_homeostatic_compat.py` to anchor this value so it cannot regress to 1.
- Added `docs/reports/HERB_FOOD_SEED_SUGARCANE_TUNING_20260616.md`.

### Validation

- Gradle build passed with Java 25. Full validator suite passed, including updated raw herb food balance validation and Homeostatic sugar cane hydration anchor.

## 2026-06-16 raw herb food-roll rebalance implementation

### Added

- Added data-backed `food_rolls` support to Herbcraft herb food definitions. `food_rolls.nutrition` adds probabilistic visible hunger after vanilla consumption, while `food_rolls.saturation` adds direct hidden saturation with vanilla-style clamping to the current food level.
- Added `scripts/validate_raw_herb_food_balance.py` to guard the P3.10 raw forage balance and verify Java support, JSON roll shape, exact tuned values, common-forage no-stable-visible rules, and the blue orchid saturation-effect fix.
- Added `docs/reports/HERB_FOOD_ROLLS_IMPLEMENTATION_REPORT_20260616.md` documenting the implementation and exact balance classes.

### Changed

- Rebalanced 70 raw Herbcraft edible entries in `data/herbcraft/herbs.json` according to the approved probability direction: wildflowers, petals, ferns, common flowers, leaves, saplings, seeds, aquatic plants, sugar/fiber plants, sticky materials, and selected foodlike exceptions.
- Very common scenery/forage now mostly has `nutrition = 0`, `saturation_modifier = 0.0`, and small probability rolls instead of stable visible hunger.
- Leaves and sticky materials now lean toward hidden-saturation rolls rather than visible hunger.
- Sugar cane and raw kelp remain stable visible-hunger exceptions, but with very low stable saturation.
- Removed the guaranteed `minecraft:saturation` effect from `minecraft:blue_orchid` so it no longer bypasses the food-roll balance model.

### Validation

- Gradle build passed with Java 25. Full validator suite passed, including `scripts/validate_raw_herb_food_balance.py`, language parity, nutrition/correction/externalized rules, Homeostatic compat, recipe/item checks, trade tags, P1 Codex validation, and built-jar verification.

## 2026-06-16 raw herb probability parameter review

### Documented

- Added `docs/reports/HERB_FOOD_PROBABILITY_PARAMETER_REVIEW_20260616.md` and `docs/reports/raw_herb_food_current_values_20260616.csv` after re-checking current raw herb food source, nutrition profiles, herb natures, and 26.1 `FoodData` / `SaturationMobEffect` behavior.
- Refined the raw Herbcraft hunger/saturation rebalance from broad classes into per-item probability ranges, distinguishing scenery nibble, fiber forage, common flowers, seeds, aquatic raw plants, quick sugar, sticky fullness, medicinal-only, and bulk/processed exceptions.
- Recorded that `minecraft:saturation` effects on `minecraft:blue_orchid` and `minecraft:dried_kelp_block` must be considered in any later food-roll implementation, because they restore visible food outside base `nutrition` / `saturation_modifier`.
- No code or gameplay data implementation in this turn.

## 2026-06-16 raw herb hunger/saturation balance review

### Documented

- Added `docs/reports/HERB_FOOD_HUNGER_SATURATION_REBALANCE_REVIEW_20260616.md` after source-auditing `herbs.json`, `HerbFoodRegistry`, `HerbEntry`, and the post-consumption settlement flow.
- Identified common forage classes whose stable visible hunger may undermine early survival food progression: wildflowers, petals, ferns, leaves, saplings, seeds, and some aquatic plants.
- Proposed a data-backed `food_rolls` model for probabilistic visible hunger and/or hidden saturation restoration while keeping existing `nutrition` / `saturation_modifier` as stable vanilla base values.
- Recommended first implementation batches and validator coverage, while deferring hard-core multi-dimension fluid/vitamin/mineral/fiber split to a future addon.

## 2026-06-16 Cuisine/Alchemy module guide implementation

### Added

- Added module-owned overview guide entries inside `膳房录` and `炼金要术` using the existing data-backed `codex_guides.json` system.
- Cuisine now has `overview` guide entries for: 膳房总述, 水碗与药膳, 固定菜与准备, 百草盅, 清除与免疫, 隐秘菜肴与炼金催化.
- Alchemy now has `overview` guide entries for: 炼金总述, 精华, 草药药水, 澄清与浊化, 品质与余韵, 武器淬层, 女巫与浊毒.

### Changed

- `膳房与药膳` and `炼金与精华` remain short gateway pages under `百草经总纲`; the deeper route teaching now lives in each module's own chapter.
- Module guide pages use existing advancement completion and representative entry knowledge state for staged clarity without adding new save data or revealing hidden recipes too early.

### Validation

- Gradle build and full validator suite passed.

## 2026-06-16 Cuisine/Alchemy guide copy spec

### Documented

- Added `docs/reports/CUISINE_ALCHEMY_GUIDE_COPY_PHASE3_9_SPEC_20260616.md` after deeper source/recipe review of Cuisine and Alchemy.
- The spec keeps `膳房与药膳` and `炼金与精华` as short gateway pages under 百草经总纲, and proposes module-owned overview sections/sub-guide entries inside `膳房录` and `炼金要术`.
- Proposed staged, non-spoiling copy for Cuisine topics: 膳房总述, 水碗与药膳, 固定菜与准备, 百草盅, 清除与免疫, 隐秘菜肴与炼金催化.
- Proposed staged, non-spoiling copy for Alchemy topics: 炼金总述, 精华, 草药药水, 澄清与浊化, 品质与余韵, 武器淬层, 女巫与浊毒.

## 2026-06-16 Cuisine/Alchemy guidance direction discussion

### Documented

- Added `docs/reports/CUISINE_ALCHEMY_GUIDANCE_DIRECTION_20260616.md` after checking current Cuisine/Alchemy guide, hint, advancement, and chapter source.
- Recommended keeping `膳房与药膳` / `炼金与精华` as short gateway pages under 百草经总纲, while adding module-owned overview sections and guide subentries inside `膳房录` and `炼金要术` if more guidance is desired.
- Updated `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md` to track this as P3.9 discussion/spec candidate.

## 2026-06-16 pharmacology temperature/relation data pass

### Added

- Added data-backed `temperature_values` to `data/herbcraft/pharmacology_rules.json`. Current values: `cold -15`, `cool -8`, `neutral 0`, `warm 8`, `hot 15`.
- Added data-backed ordered `seven_emotion_rules` to `pharmacology_rules.json`, preserving the current relation priority while allowing future tuning without Java edits.

### Changed

- `PharmacologyResolver` now reads temperature values and 七情 relation conditions/priority from `PharmacologyRules` instead of hardcoding them.
- Tuned qi regression interval from `1200` ticks to `600` ticks. Exact hot/cold now builds toward same-qi more slowly and recovers less stickily.
- Updated validators so temperature values and seven-emotion rules are checked as externalized data.

### Validation

- Gradle build and full validator suite passed.

## 2026-06-16 guide hints and pharmacology review

### Added

- Added data-backed one-time guide hints via module-owned JSON files:
  - `data/herbcraft/guide_hints.json`
  - `data/herbcraft_cuisine/guide_hints.json`
  - `data/herbcraft_alchemy/guide_hints.json`
- Added `GuideHintManager` and persisted seen-hint IDs on player data.
- Added first-hint triggers for first herb bite, first Codex open, first page read, first ingredient tasted, first correction, first water bowl, first dish, and first essence.
- Added `seven_single` / 单行 discovery and guide display.
- Added `docs/reports/PHARMACOLOGY_BALANCE_AND_RELATION_REVIEW_20260616.md` analyzing qi accumulation speed and relation coverage.

### Changed

- Guide hints remain module-owned: Core hints in Core, Cuisine hints in Cuisine, Alchemy hints in Alchemy.
- Validators now cover guide hints, `GuideHintManager`, persisted seen-hint storage, and built-jar guide hint resources.

### Validation

- Gradle build and full validator suite passed.

## 2026-06-16 pharmacology test matrix

### Documented

- Added `docs/reports/PHARMACOLOGY_SEVEN_EMOTION_TEST_MATRIX_20260616.md` after source-auditing `PharmacologyResolver`.
- The report confirms `XIANG_XU`, `XIANG_SHI`, `XIANG_SHA`, `XIANG_WEI`, `XIANG_E`, and `XIANG_FAN` are recorded for Codex discovery. `SINGLE` exists mechanically but is not currently recorded/displayed.
- Added concrete item-pair tables for client testing all 七情 relations and common 五味/温性 principle lines.

## 2026-06-16 next-project direction after Phase 2.1

### Planned

- Added planning report for the next `1.1.2` steps after Phase 2.1: client-test module combinations, then implement a small data-backed one-time hint system if Phase 2.1 passes.
- Proposed module-owned `guide_hints.json` files plus a `GuideHintManager` with persisted seen-hint IDs for first herb bite, first Codex open, first page read, first ingredient tasted, first correction, Cuisine water bowl/dish hints, and Alchemy first essence hint.
- Reaffirmed that P4 API, 药引子, Homeostatic temperature gameplay, and Hardcore Nutrition should wait until `1.1.2` guidance polish is stable.

### Docs

- Added `docs/reports/NEXT_PROJECT_DIRECTION_1_1_2_AFTER_PHASE2_1_20260616.md`.
- Updated `NEXT_TASK.md` and `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md` to track P3.7 one-time guide hints as the next candidate after Phase 2.1 client verification.

## 2026-06-16 Codex guidance Phase 2.1 implementation

### Added

- Added data-backed Codex guide registration via module-owned JSON files:
  - `data/herbcraft/codex_guides.json`
  - `data/herbcraft_cuisine/codex_guides.json`
  - `data/herbcraft_alchemy/codex_guides.json`
- Added `CodexGuideRegistry` to load guide entries from JSON and bind them to Java line providers.
- Added pharmacology principle discovery persistence to `PlayerPharmacologyState`, storing latest item pair + count for flavor, temperature, and seven-emotion discoveries.
- Added progressive guide lines for flavor, temperature, and seven-emotion principles.

### Changed

- Core no longer owns or registers `膳房与药膳` / `炼金与精华`. Cuisine and Alchemy register their own guide entries only when their modules are installed.
- `五味与性味` no longer prints exact first tasted item/nature in the system guide. Exact item nature remains in the item entry; the guide reveals broader principles from player discoveries.
- Strengthened validators and built-jar checks for data-driven guide files, module ownership, and pharmacology discovery hooks.

### Validation

- Gradle build and full validator suite passed.

## 2026-06-16 Codex guidance Phase 2.1 implementation spec

### Planned

- Drafted the Phase 2.1 plan to make Codex guide registration data-driven via module-owned JSON files instead of Core hardcoding the guide list.
- Proposed `data/herbcraft/codex_guides.json`, `data/herbcraft_cuisine/codex_guides.json`, and `data/herbcraft_alchemy/codex_guides.json`.
- Specified that Cuisine/Alchemy guide entries should only appear when their addon module is loaded.
- Specified player principle discovery tracking for 七情 / flavor / temperature learning, storing latest item pair + count so the Codex can remind the player which eaten items triggered a relation.
- Wrote proposed Chinese and English guide copy for progressive 五味/性味 and 七情 reveal lines.

### Docs

- Added `docs/reports/CODEX_GUIDANCE_PHASE2_1_IMPLEMENTATION_SPEC_20260616.md`.
- Updated `docs/NEW_PLAYER_GUIDANCE_1_1_2_DESIGN.md`, `NEXT_TASK.md`, and `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md` to point to the new implementation spec.

## 2026-06-16 Codex guidance Phase 2 review

### Reviewed

- Author client-tested the new Codex guide pages: all 9 guide entries appear under `百草经总纲`, and text renders normally.
- Source/client review identified two issues before Phase 2 should be treated as release-ready:
  - `膳房与药膳` and `炼金与精华` are currently registered unconditionally by Core and still appear when their addon modules are absent.
  - `五味与性味` currently displays exact first tasted item/nature in the guide page; this should be softened so exact item data remains in the item entry and principle meanings unlock through experience.

### Documented

- Added `docs/reports/CODEX_GUIDANCE_PHASE2_CLIENT_REVIEW_20260616.md` with source audit, design analysis, and recommended fixes.
- Updated `docs/NEW_PLAYER_GUIDANCE_1_1_2_DESIGN.md`, `NEXT_TASK.md`, and `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md` to mark Phase 2.1 fixes as the next priority before one-time hints.

## 2026-06-16 1.1.2 onboarding phase 2 Codex guides

### Added

- Added always-visible guide subentries under `百草经总纲` / Herbal Codex overview:
  - 认识草木 / Knowing Wild Herbs
  - 听闻、亲尝与谙熟 / Rumor, Tasting, Mastery
  - 五味与性味 / Nature and Flavors
  - 七情与配伍 / Relations Between Herbs
  - 药性蓄积 / Medicinal Momentum
  - 膳房与药膳 / Cuisine and Medicinal Meals
  - 炼金与精华 / Alchemy and Essences
  - 食材录与五养 / Ingredient Codex and Nutrition
  - 残页与校勘 / Pages and Errata
- Added a 食材录 overview entry explaining that ordinary ingredients do not use herb pharmacology but still affect long-term nutrition.

### Changed

- Guide entries start vague and become clearer using existing player progress: tasted herb count, first tasted herb nature, discovered flavor count, herb-stack/cuisine/alchemy advancements, tasted ingredient count, heard claims, and verified claims.
- `KnowledgeAPI` now treats entries whose IDs start with `guide_` as always visible, similar to `overview`, so guide pages cannot be consumed by random tattered-page targeting and do not require knowledge flags.
- Updated `docs/NEW_PLAYER_GUIDANCE_1_1_2_DESIGN.md`, `NEXT_TASK.md`, and `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md` to mark Phase 2 implemented and one-time hints as the next design candidate.

### Validation

- Gradle build and the full validator set passed, including strengthened P1 Codex/advancement validation for guide entries and 食材录 overview.

## 2026-06-16 1.1.2 onboarding phase 1 text polish

### Changed

- Rewrote the four-line `百草经总纲` / Herbal Codex overview to explain risk, rumor, tasting/practice, the three paths, 食材录, and the nutrition panel more clearly.
- Updated Core advancement descriptions so the dedicated advancement page reads more like route signs instead of only trophies. This fixes the root advancement wording so it no longer implies the player already owns a Codex.
- Updated Cuisine and Alchemy advancement descriptions with light route guidance while preserving hidden recipe and knowledge gates.
- Polished 食材录 wording to clarify that ordinary foods do not carry herb nature but still nourish long-term.
- Recorded Phase-1 implementation status in `docs/NEW_PLAYER_GUIDANCE_1_1_2_DESIGN.md`, updated `NEXT_TASK.md`, and updated `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md` P3.6 tracking.

### Validation

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks`, `python3 scripts/check_lang_diff.py`, and `python3 scripts/verify_built_jars.py` passed.

## 2026-06-16 P0 consistency pass

### Changed

- Added `release_output/` to `.gitignore` so local release/test artifacts are not accidentally committed.
- Marked `docs/PROJECT_SUMMARY.md` and `SOURCE_REVIEW_2026-06-14.md` as historical/superseded context.
- Updated `PROJECT_STATE.md`, `AI_ONBOARDING.md`, and `BUGS_TODO.md` to reflect current correction threshold/fallback and validator behavior.

### Fixed

- Fixed `NutritionRules.loadFromBundledJson()` fallback for `correction_high_threshold` from stale `850` to current `750`, matching `nutrition_rules.json` and `NutritionRules.defaults()`. This only affects missing-field fallback behavior.
- Updated validators that still looked for `1.0.0` built jars to derive the current jar version from `fabric.mod.json`.
- Updated `validate_animal_food_tags.py` to use Loom's downloaded Minecraft client/merged jar or `HERBCRAFT_VANILLA_CLIENT_JAR`, not only `/home/user/cache/minecraft/client-26.1.2.jar`.

### Validation

- `validate_nutrition_correction_phase.py` now checks the 750 fallback and zero-`fruit` correction fallback matrix.
- `validate_externalized_rules.py` now covers `ingredients.json` and `pharmacology_rules.json`.
- `verify_built_jars.py` now checks `ingredients.json`, `pharmacology_rules.json`, `IngredientsChapter.class`, and `PharmacologyRules.class` in the built core jar.

## 2026-06-16 nutrition tuning, ingredients chapter, data-driven cleanup

### Added

- Added 食材录 (Ingredient Codex) chapter: 39 vanilla foods in 6 sections (meat/seafood/grain/fruit/soup/special), data-driven from `data/herbcraft/ingredients.json`. Knowledge gated by eating: pick up → ENCOUNTERED, eat → MASTERED with fuzzy nutrition display.
- Added three fluid surplus debug presets: `fluid_surplus_pair`, `fluid_surplus_single`, `fluid_surplus_unrelated`.
- Added `data/herbcraft/ingredients.json` as the 18th data-driven JSON file.
- Added `IngredientsChapter.hasEntry()` for knowledge flag hooks.
- Added knowledge flag hooks in `NutritionManager.onFoodConsumed()` and `KnowledgeInventoryScanner` for vanilla food items.
- Added `data/herbcraft/nutrition_correction_rules.json` and `data/herbcraft/pharmacology_rules.json` to documentation (`DATA_DRIVEN_ARCHITECTURE.md`, `PROJECT_STATE.md`, `EXTENDING_HERBCRAFT.md`).

### Changed

- Scaled all 162 nutrition profiles to 0.6x for balanced pacing. `cooked_beef` flesh 69→41, `bread` grain 60→36. Players now need ~7 eats to reach correction threshold and ~9 to reach excess debuffs.
- Lowered `correction_high_threshold` from 850 to 750. Matrix correction now activates at 75% instead of 85%, eliminating the "dead zone" where 750-849 had only passive -2/min decay.
- Added graduated decay in `correctionDecayAmount()`: 750-849 uses half-speed, 850+ uses full-speed.
- Tightened fluid surplus helper: now requires at least two non-fruit excess dimensions (>850) that share a correction-matrix relationship. Single excess or unrelated excess no longer triggers surplus decay.
- Fixed `correcting_grain`/`correcting_flesh`/`correcting_oil` debug presets: non-target dimensions raised from 400-450 to 500-550.
- Synced `NutritionRules.defaultCorrectionWeights()` Java fallback defaults with `nutrition_correction_rules.json` (all fruit weights 0.0).
- Vanilla food tooltip nutrition now gated behind eating (ingredients chapter state).
- Expanded `reference_cache/26_1_porting_notes.md` with comprehensive 26.1 API details.

### Fixed

- Deleted redundant `core/data/herbcraft/tags/items/` (plural, old MC path); only `tags/item/` (singular, 26.1 path) remains.
- Fixed IngredientsChapter knowledge flags never being set (root cause: `displayState()` reads `KnowledgeFlags`, not Entry predicates).

### Removed

- Removed hardcoded `Items.*` entries from `IngredientsChapter.java`; replaced with JSON-driven `ingredients.json` loader.

## 2026-06-14 test baseline review / nutrition integration pass

### Added

- Added optional round-1 Homeostatic drinkable compatibility data for 33 Herbcraft Cuisine custom edible outputs and round-2 Homeostatic drinkable compatibility data for selected Core/vanilla Herbcraft edible items; added/updated `scripts/validate_homeostatic_compat.py`.
- Added `docs/HOMEOSTATIC_COMPAT_NOTES.md` and `docs/HARDCORE_NUTRITION_ADDON_PLAN.md` for future compatibility/addon planning.
- Added `scripts/generate_essence_resources.py` and `scripts/validate_essence_resources.py` for project-local essence resource coverage checks, missing skeleton generation, standalone `--scaffold-extension` example pack creation, and `--check-extension` scaffold validation.
- Added `docs/examples/essence_extension_example/` and `docs/EXTENDING_HERBCRAFT.md` to document the current extension workflow.
- Externalized several Java rule tables into JSON for easier maintenance: special counters, hodgepodge rules, alchemy essence definitions, advanced potion bonus pools, Codex loot injections, special/T3 potion definitions, the safe witch throw pool, witch loot, weapon coating rules, and nutrition effect mechanics/timing.
- Added `scripts/validate_externalized_rules.py` to guard those externalized rule tables.
- Added dynamic extra essence registration support for JSON entries beyond the current compatibility static fields.
- Added `SOURCE_REVIEW_2026-06-14.md`, a source-based feature summary of the current test baseline.
- Added `reference_cache/26_1_porting_notes.md` with Fabric/Minecraft 26.1 development notes.
- Added `scripts/setup_jdk25_temurin.sh` for temporary JDK 25 setup in sandboxes.
- Added release baseline output under `release_output/herbcraft_test_20260614_review/`.
- Added seven Herbcraft custom nutrition effects:
  - `herbcraft:grain_deficiency` / 谷气亏虚
  - `herbcraft:flesh_deficiency` / 血肉亏虚
  - `herbcraft:oil_deficiency` / 津脂不足
  - `herbcraft:vege_deficiency` / 蔬素不足
  - `herbcraft:fruit_deficiency` / 津液不足
  - `herbcraft:dietary_imbalance` / 饮食失衡
  - `herbcraft:well_nourished` / 五养调和
- Added mechanics for nutrition effects:
  - grain deficiency reduces block break speed;
  - flesh deficiency reduces attack damage;
  - oil deficiency reduces movement speed;
  - vege deficiency periodically increases exhaustion;
  - fruit deficiency lightly reduces movement speed;
  - dietary imbalance lightly reduces movement speed and periodically increases exhaustion;
  - well nourished gives armor and knockback resistance.
- Added `/herbcraft nutrition get|set|preset` debug commands gated behind gamemaster permission.
- Added `scripts/generate_nutrition_effect_icons.py` to generate nutrition effect icons from vanilla item textures.
- Added `scripts/validate_custom_nutrition_effects_phase8.py`; older phase validators now wrap to the current validator.

### Changed

- Updated Homeostatic compatibility to avoid negative Homeostatic `amount` values after client testing showed below-zero internal water can keep dehydration active after partial recovery; thirst-risk foods now use stronger thirst-effect chances/durations instead.
- Marked selected drink-like Cuisine dishes as `can_always_eat` so they can hydrate when the hunger bar is full.
- Rebalanced Homeostatic compatibility values against its 20-point water bar: stronger cuisine hydration, water bowl 85% thirst-effect risk, and meaningful negative hydration for sweet/sticky/hot/toxic Core edibles.
- Added gentle milk bucket nutrition (`flesh`/`oil`), reduced water bowl nutrition to trace fluids, and renamed the displayed fruit nutrition dimension to `津液` / `Fluids`.
- NutritionManager now uses Herbcraft custom effects instead of vanilla `WEAKNESS`, `SLOWNESS`, `MINING_FATIGUE`, `HUNGER`, `NAUSEA`, and hidden vanilla `RESISTANCE` for core nutrition states.
- Serious malnutrition and starved states are now sustained through the next nutrition evaluation instead of random short flashes.
- Nutrition evaluation now clears stale Herbcraft nutrition effects before applying the current nutrition state, so commands/food can remove old nutrition effects immediately.
- Confirmed dietary imbalance remains grace-gated, but once confirmed is sustained through the next nutrition evaluation.
- Warning states remain short/probabilistic to keep main-mod nutrition non-hardcore.
- Water bowl still has a 15% success chance, but on success now also temporarily relieves dietary imbalance for 60 seconds without repairing nutrition values.
- Cuisine/medicine clearing paths now skip Herbcraft nutrition effects.
- Nutrition effect icons were replaced with vanilla-derived item icons plus deficiency/down-arrow or positive/up-arrow overlays.
- Updated handoff docs: `AI_ONBOARDING.md`, `PROJECT_STATE.md`, `NEXT_TASK.md`, and `BUGS_TODO.md`.

### Fixed

- Removed stale PlayerNutritionState.evaluate / NutritionStatus logic so NutritionManager.evaluateAndApply is the sole authoritative runtime nutrition evaluation path.
- Fixed the conflict where medicinal cuisine/effect immunities could suppress NutritionManager's vanilla-effect-based debuffs.
- Fixed stale nutrition effects remaining after debug commands or corrective food changed nutrition values.
- Fixed severe malnutrition feeling like a random, short cosmetic flicker.
- Fixed the migrated grain danger path that previously applied `grain_deficiency` twice due to direct vanilla-effect ID replacement.

### Verified

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks` passes.
- Language key parity passes.
- Custom nutrition phase 8 validator passes.
- Recipe isolation, item texture, task A/B/C/D, extended feature, and built-jar validators pass.
- Core server smoke reaches `Done` after the nutrition and icon updates.

## 2026-06-12

### Added

- Added responsive Herb Codex text layout with wrapping, scrolling and left-panel ellipsis trimming.
- Added creative tab variants for tattered pages in earlier stage, preserved in this release line.
- Added last meal death flavor message.
- Added centralized consume sound resolver for herbs and cuisine items.
- Added validation scripts for villager trade tags and language key diffs.

### Fixed

- Fixed long Codex text overflowing in English and other long-text languages.
- Fixed language key parity between `zh_cn` and `en_us`.
- Verified all data-driven villager trades are referenced by trade tags with `replace=false`.

### Builds

- Historical listed builds may not match the current test-baseline jar names. Current local test outputs are staged under `release_output/herbcraft_test_20260614_review/`, which is git-ignored/local-only.
 listed builds may not match the current test-baseline jar names. Current local test outputs are staged under `release_output/herbcraft_test_20260614_review/`, which is git-ignored/local-only.
