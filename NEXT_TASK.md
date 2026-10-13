# NEXT_TASK

## Current handoff note: Minecraft 26.3 Fabric port (CI first)

On 2026-10-13/14 the 26.3 port was applied. The detailed report is
`docs/reports/PORTING_26_3_FABRIC_20261013.md`; the investigation notes are
`reference_cache/26_3_porting_notes.md`. Per the author's instruction the CI trio was
built **first** and is staged in `docs/ci/` (mirroring `TaCZ_Refabricated_Unofficial`).

Completed in this pass:

- Minecraft `26.2` -> `26.3`; Fabric API `0.155.2+26.2` -> `0.162.0+26.3`; Gradle Wrapper `9.5.1` -> `9.6.0`; Loader `0.19.3` and Java `25` kept;
- optional deps to their verified 26.3 lines: Jade `26.3.5+fabric`, JEI `31.12.0.65`, REI `26.3.824`, Architectury `22.0.5`;
- **brewing moved to data-driven JSON**: 520 `minecraft:brewing` recipes (`alchemy/src/main/resources/data/herbcraft/recipe/brewing/`) covering every mix and every splash/lingering container transformation for all 175 registered potions — `scripts/generate_brewing_recipes.py` + `scripts/validate_brewing_recipes.py`;
- **villager trades migrated to the 26.3 schema** (80 files): `given_item_modifier`, inline modifier `type`, required `count` — `scripts/migrate_villager_trades_26_3.py`;
- **SDL input** in three Core client files (`InputConstants.Type.KEYBOARD`, `InputConstants.KEY_*`);
- five `fabric.mod.json`, README, and the vanilla-tooling scripts bumped to 26.3;
- `scripts/check_version_consistency.py` 23/23 green; full sandbox-runnable validator suite green.

### Required validation (26.3)

1. **Activate CI**: copy `docs/ci/{build,compile-check,consistency}.yml` to `.github/workflows/` (or grant the Arena GitHub app `Workflows` permission). Until then nothing compiles in automation — the sandbox cannot reach `maven.fabricmc.net`.
2. Read `build-reports/compile-java.log` after the first `arena/**` push and iterate. Most likely remaining errors: the two custom smithing recipe classes (`CoatingSmithingRecipe`, `ArmorSupportSmithingRecipe`) and JEI registration calls.
3. Start a dedicated server and client; check all Mixin targets, resource/data loading, the brewing stand UI, villager trades, and Jade/JEI/REI with the exact 26.3 versions.
4. Refresh release output only after the 26.3 build and smoke test pass.

## Previous handoff note: Minecraft 26.2 Fabric port validation


On 2026-07-19 the initial 26.2 Fabric port was applied. The detailed report is `docs/reports/PORTING_26_2_FABRIC_20260719.md`; the investigation notes are `reference_cache/26_2_porting_notes.md`.

Completed in this pass:

- Minecraft `26.1.2` -> `26.2`;
- Fabric API `0.151.0+26.1.2` -> `0.155.2+26.2`;
- Loom `1.16-SNAPSHOT` -> `1.17-SNAPSHOT`;
- Gradle Wrapper `9.4.1` -> `9.5.1`;
- kept Fabric Loader `0.19.3` and Java `25`;
- updated 26.2 Jade/JEI/REI/Architectury development dependencies;
- migrated Core GUI screen management to `Minecraft.gui`;
- updated active docs, examples, and texture validator/generator references.

Static checks passed: language parity, trade tags, recipe isolation, and viewer source validation.

Windows feedback fixed the first 26.2 compiler error: the eight Alchemy calls to the old `MobEffect.isInstantenous()` name now use `isInstantaneous()`.

### Required validation

1. Provide a working Java 25 executable and run `:core:compileJava`, `:alchemy:compileJava`, and `:cuisine:compileJava`.
2. Run the full Gradle build and inspect any JEI/REI API errors.
3. Start a dedicated server and client; check all Mixin targets and resource/data loading.
4. Open/close the Codex and Nutrition panel, then test Jade/JEI/REI with the exact 26.2 versions.
5. Refresh release output only after the 26.2 build and smoke test pass.

(26.2 validation is superseded by the 26.3 note above.)

## Current handoff note: Core/Alchemy 1.1.4 version bump

On 2026-06-30 the Core and Alchemy modules were bumped to `1.1.4` while Cuisine intentionally remains `1.1.3`. The detailed report is `docs/reports/VERSION_BUMP_CORE_ALCHEMY_1_1_4_20260630.md`.

Completed in this pass:

- Core `herbcraft`: `1.1.3` -> `1.1.4`;
- Alchemy `herbcraft_alchemy`: `1.1.3` -> `1.1.4`;
- Alchemy dependency on Core: `>=1.1.4`;
- Cuisine `herbcraft_cuisine`: remains `1.1.3`;
- built-jar verifier now supports per-module version expectations;
- current release-facing docs and source archive were refreshed.

This was a version/documentation/package pass only. No intentional gameplay, JSON balance, recipe, trade, loot, advancement, lang, texture, or model behavior changed.

### Client-only follow-up

1. Boot with the refreshed jars and confirm the mod list shows Core `1.1.4`, Alchemy `1.1.4`, and Cuisine `1.1.3`.
2. Confirm Alchemy accepts Core `1.1.4` and Cuisine still works against Core `1.1.4`.
3. Continue the already pending gameplay/client checks from the previous Alchemy trade/loot/potion-arrow and JEI/REI work.

## Current handoff note: Alchemy trade/loot/potion-arrow adaptation implemented

On 2026-06-30 the newer Alchemy mechanics were adapted to trades, world loot, and potion arrows without adding a new rule system. The detailed report is `docs/reports/ALCHEMY_TRADE_LOOT_ARROW_ADAPTATION_20260630.md`.

Completed in this pass:

- added villager trades for sustained/intense/T3 potions, processed potion arrows, distilled essence samples, and armor-support armor samples;
- referenced all new trades from data-driven villager trade tags with `replace=false`;
- added bundled `alchemy_loot_injections.json` and loader support in `AlchemyCodexHooks` for potions, tipped arrows, distilled essence components, and armor-support components;
- added `advanced_tipped_arrow.json`, reusing vanilla `minecraft:crafting_imbue`;
- added `AlchemyPotionArrowMixin` so arrow hits initialize processed potion metadata before vanilla hit effects and then apply taste/progression/burden handling after player hit exposure;
- added `scripts/validate_alchemy_trade_loot_arrow_adaptation.py`.

Validation passed: full Java 25 Gradle build, targeted validators for trade/loot/arrows, trade tags, advanced potion trade matching, progression/sync, potion processing/burden, extended features, language parity, and built-jar verification. Release outputs should be refreshed after any further source/resource changes.

### Client-only follow-up

1. Check new cleric/toolsmith/fletcher/armorer/wandering-trader trades appear at the expected levels and produce correct componentized stacks.
2. Check loot-table injections in fresh generated loot: village temple/fletcher/armorer, bastion bridge, ancient city, stronghold library, jungle temple, and trial reward rare.
3. Craft tipped arrows from Herbcraft lingering potions and confirm hit effects reflect processed/special potion metadata.
4. Confirm potion-arrow hits grant potion taste/progression/burden only on actual hit exposure, not when merely held in inventory.
5. Tune chance/prices/counts if early-game villages feel overloaded.


## Current handoff note: Alchemy advancement progression and sync audit complete

On 2026-06-30 the Alchemy progression system was updated after the JEI distilled-essence lookup fix was confirmed. The detailed report is `docs/reports/ALCHEMY_ADVANCEMENT_AND_SYNC_AUDIT_20260630.md`.

Completed in this pass:

- added `first_distilled_essence`, `first_sustained`, `first_intense`, and `first_armor_support`;
- moved potion-use progression out of broad inventory scanning and into actual drink/splash/lingering taste/exposure hooks;
- kept inventory scanning for concrete possession milestones and seen/heard Codex knowledge only;
- normalized special potion path mapping for `undead_venom`, `cardiac_toxin`, `wither_venom_iii`, and `witch_*` paths;
- updated Alchemy guide gating for distilled essence, sustained/intense processing, and armor support;
- updated `scripts/rebuild_advancements_and_lang.py` so regenerated advancements preserve the expanded tree;
- added `scripts/validate_alchemy_advancement_and_sync.py`;
- audited sync: Alchemy item stack components are network synchronized; Alchemy processing/support/coating/distillation/burden-effect rule tables remain bundled/startup rules and require client/server jar parity for tooltip/viewer text consistency.

Validation passed: full Java 25 Gradle build, full `scripts/validate_*.py` suite, language parity, and built-jar verification. Release outputs were refreshed.

### Client-only follow-up

1. Pick up/receive processed potions without consuming them: should not grant `first_potion`, `first_clarified`, `first_sustained`, `first_intense`, `first_murky`, `witch_knowledge`, `undead_toxin`, or `epic_potion`.
2. Drink or get hit by splash/lingering processed potions: should grant the relevant milestones.
3. Obtain distilled essence and supported armor: should grant `first_distilled_essence` and `first_armor_support`.
4. Check the Alchemy guide pages unlock the new hints at the expected stages.
5. Use matching client/server Alchemy jars when testing tooltip/viewer rule text; Alchemy rule-table sync is not a hot-reload datapack feature yet.


## Current handoff note: JEI distilled-essence exact lookup follow-up implemented

After user client testing confirmed REI was correct but JEI still matched all source essences when tracing from one distilled essence, JEI brewing registration was moved off the vanilla brewing factory path and onto a component-aware `ComponentAwareBrewingRecipeManagerPlugin` for `RecipeTypes.BREWING`.

Validation passed: full Gradle build, `validate_coating_viewer_plugins.py`, and `verify_built_jars.py`.

Client-only follow-up: re-test JEI lookup from a single distilled essence. It should now return only brewing recipes with that exact `sourceEssence|orientation`, while preserving corrected brewing slot order.


## Current handoff note: Alchemy JEI/REI viewer bug fixes implemented

On 2026-06-30 the reported Alchemy viewer integration bugs were addressed in source. The detailed report is `docs/reports/ALCHEMY_VIEWER_COMPONENT_MATCHING_FIXES_20260630.md`.

Completed in this pass:

- corrected JEI brewing display slot order for distilled essence creation and processed-potion brewing;
- added component-aware JEI subtype interpreters for distilled essence and processed potion bottles;
- added REI common comparator plugin and `rei_common` entrypoint for distilled essence/potion component matching;
- initialized processed-potion viewer output stacks so lookup can match runtime processed potion metadata;
- split/correlated JEI and REI distilled-essence smithing display samples so outputs correspond to the displayed inputs;
- updated validator coverage for brewing order, subtype/comparator registration, and correlated smithing displays.

Validation passed so far: `:alchemy:compileJava`, full Gradle build, and `scripts/validate_coating_viewer_plugins.py`.

### Client-only follow-up

Verify in a live client with JEI and/or REI installed:

1. JEI distilled essence creation no longer shows `?` and has essence bottom / honeycomb-redstone-glowstone top;
2. JEI processed-potion brewing has potion bottom / distilled essence top;
3. JEI and REI smithing attachment examples keep output samples synchronized with input samples;
4. recipe lookup from an initialized processed potion jumps to the matching brewing route;
5. recipe lookup from a distilled essence no longer overmatches all source essences.


## Current handoff note: Alchemy burden profile and armor-support specialization complete

On 2026-06-30 the Alchemy burden consequence layer was upgraded from mostly total-burden-only output into a two-layer system: total burden stage plus dominant medicinal profile. The detailed report is `docs/reports/ALCHEMY_BURDEN_PROFILE_AND_ARMOR_SPECIALIZATION_20260630.md`.

Completed in this pass:

- added JSON-backed `burden_consequence_profiles` for `steady`, `warming`, `chilling`, `overdriven`, `muddy`, `scattering`, and `stagnating`;
- added profile-specific Alchemy burden effects and icons for heat stirring, cold stagnation, overdrive rebound, murky disturbance, qi scattering, and qi stagnation;
- persisted the last processed-potion burden context on `PlayerPotionBurdenState` so consequences can use `medicinal_profile`, burden profile, thermal tendency, qi tendency, armor mitigation, and tail-decay bonuses;
- kept total burden thresholds while routing non-steady consequences through the dominant profile;
- specialized armor support so `clear_bearing`, `steady_bearing`, and `braced_bearing` now use active/tail split buffers, profile/style matching bonuses, consequence mitigation, and tail-decay support rather than only generic buffer values;
- preserved the existing simple `POTION_BURDEN_BUFFER` hook for compatibility and added `POTION_BURDEN_BUFFER_DETAILED` for split active/tail support.

Validation passed: full Gradle build and the current validator suite, including new `validate_alchemy_burden_profile_specialization.py`.

### Client-only follow-up

Verify in a live client:

1. sustained/long potions feel tail-heavy and `steady_bearing` helps tail buildup/decay;
2. intense/strong potions feel peak-heavy and `braced_bearing` helps prevent sharp overdrive spikes;
3. muddy/toxic/murky paths produce murky disturbance and `clear_bearing` softens them;
4. new effect names/icons and messages read correctly and are not spammy;
5. old simple burden-buffer listeners, if any, still behave as active-buffer plus half-tail-buffer.


## Current handoff note: Arena environment setup and validator sync complete

On 2026-06-30 a fresh Arena.ai workspace setup pass was completed for the current `1.1.3` source line. The detailed report is `docs/reports/ENVIRONMENT_SETUP_ARENA_20260630.md`.

Completed in this pass:

- cloned `q14433686-arch/herbcraft-source` into the workspace;
- read `AGENT_BRIEF.md`, regenerated `docs/reports/CURRENT_AGENT_BRIEF.md`, and re-read the required authoritative anchors;
- restored `gradlew` execute permission;
- provisioned Temurin JDK 25 under `/tmp/herbcraft-jdk25/jdk-25`;
- ran the full Gradle build successfully with Java 25;
- refreshed stale validator expectations for current Alchemy armor-support guide coverage, compact processed-potion tooltip behavior after Codex migration, data-driven Codex theme checks, and data-backed hodgepodge herb-count rules;
- ran the current validator suite successfully;
- recreated `release_output/herbcraft_test_20260614_review/` with rebuilt jars, refreshed source zip, and P4 runtime test datapack zip.

This was environment/documentation/validator synchronization only. No gameplay Java mechanics, JSON balance/content, recipes, assets, or language behavior were intentionally changed.

### Client-only follow-up

The sandbox cannot replace live-client confirmation. Continue to verify JEI/REI optional viewer behavior, processed potion tooltip/Codex readability, splash/lingering potion runtime exposure, and general in-game UI/interaction checks in a real client.


## Current handoff note: Alchemy JEI/REI adapters for new recipes complete

On 2026-06-30 the optional JEI and REI compatibility classes were expanded for the newer Alchemy recipe routes. The detailed report is `docs/reports/ALCHEMY_VIEWER_ADAPTERS_20260630.md`.

Completed in this pass:

- confirmed the Fabric 26.1.2 dependency shape for JEI/REI: normal `compileOnly` API dependencies, REI default-plugin and Architectury compile-only support, no hard viewer dependency in `fabric.mod.json`;
- kept existing entrypoint class paths `com.herbcraft.alchemy.compat.jei.CoatingJeiPlugin` and `com.herbcraft.alchemy.compat.rei.CoatingReiPlugin`;
- added JEI/REI Brewing Stand displays for essence + catalyst -> componentized distilled essence;
- added JEI/REI Brewing Stand displays for base potion + distilled essence -> processed potion, including normal, splash, and lingering containers;
- added JEI/REI smithing displays for armor support apply/refine;
- expanded coating viewer displays to include processed distilled-essence coating variants;
- refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt jars, source zip, and test datapack zip.

Validation passed: Alchemy compile, full Gradle build, viewer validator, distillation/armor/coating/potion validators, language parity, and built-jar verification.

### Client-only follow-up

Verify in a live client:

1. JEI installed without REI: optional entrypoint loads and Brewing/Smithing displays appear;
2. REI installed without JEI: optional entrypoint loads and Brewing/Smithing displays appear;
3. distilled essence component variants are searchable/clickable and show the correct distillation, processed-potion, armor-support, and processed-coating routes;
4. normal/splash/lingering processed potion displays show matching input/output bottle forms.

## Current handoff note: Alchemy knowledge-gating / Codex fixes complete

On 2026-06-30 the author-reported Alchemy display and 百草经 design issues were fixed at source level and output was refreshed. After the author provided `RawOutput.log`, a bootstrap crash in `AreaEffectCloudKnowledgeMixin` was also hotfixed by removing the invalid inherited-field `tickCount` shadow. Further client-review passes then shortened processed-potion tooltips, localized `来源药材`, expanded and separated mastered Codex potion details, routed splash/lingering exposure through the new runtime features, restored Core `五味与性味` / `七情与配伍` latest-example rendering, and added missing Chinese guide keys for `食材录与五养` / `残页与校勘`. The detailed report is `docs/reports/ALCHEMY_KNOWLEDGE_GATING_CODEX_FIXES_20260630.md`.

Completed in this pass:

- fixed `性味偏向` / `特质亲和` tooltip key leakage by using Core lang prefixes;
- added Alchemy potion tooltip knowledge gates aligned with Core-style knowledge states;
- mapped `_long`, `_strong`, `_clarified`, and `_murky` to base Alchemy potion Codex entries;
- made normal, splash, and lingering potion containers participate in potion knowledge lookup;
- marked potion entries as tasted when drinking, when affected by splash potions, and when standing in lingering clouds;
- corrected potion Codex entry rendering to distinguish exact `TASTED` from collapsed `HEARD` state via `KnowledgeAPI.displayState(...)`;
- added “炼金总述” linkage/taste-gate text and a new armor-support guide entry;
- gated distilled-essence and armor-support explanatory tooltip details;
- refreshed `release_output/herbcraft_test_20260614_review/` with rebuilt jars and source zip.

Validation passed: full Gradle build, potion processing/source/body-state/relation validators, `validate_alchemy_knowledge_gating_and_codex.py`, language parity, and built-jar verification.

### Client-only follow-up

Verify in a live client:

1. processed potion tooltips stay compact at unknown / encountered / heard / tasted / mastered states;
2. no raw untranslated `tooltip.herbcraft.flavor.*`, `tooltip.herbcraft.trait.*`, or raw `minecraft:*` source-herb strings remain;
3. splash and lingering potion exposure updates 百草经 potion entries to “亲尝” and applies burden/body-state/relation runtime behavior;
4. potion Codex entries switch from seen/heard wording to tasted/mastered wording and the mastered details scroll/read correctly;
5. “炼金总述” and “护甲护持” guide content appears and reads correctly.


## Current handoff note: environment setup is complete

A fresh-clone setup pass was completed again on 2026-06-30 for the current `1.1.3` source line: the repository was cloned, the agent brief was regenerated, the authoritative anchor docs were re-read, `gradlew` execute permission was restored, JDK 25 was provisioned, the full Gradle build passed, and the current validator suite passed. This is recorded in `docs/reports/ENVIRONMENT_SETUP_20260630.md`.

That pass was environment/documentation synchronization only and did not intentionally change gameplay logic, JSON balance/content, recipes, resources, or language behavior.

## Current handoff note: environment setup is complete

The current sandbox has been set up from a fresh clone for the `1.1.3` line: `CURRENT_AGENT_BRIEF` was regenerated, JDK 25 was provisioned, the full Gradle build passed, stale source-location validators were refreshed for the split bootstrap/nutrition/HerbalChapter architecture, the full validator suite passed, and local `release_output/herbcraft_test_20260614_review/` was recreated with rebuilt jars plus a refreshed source zip. Details are recorded in `docs/reports/ENVIRONMENT_SETUP_AND_VALIDATOR_REFRESH_20260629.md`.

That setup pass did not change gameplay mechanics, JSON balance/content, resources, recipes, language files, or jar-facing mod behavior. The user's current four-round Alchemy expansion task now takes priority over the older JEI/REI client-verification handoff, while JEI/REI confirmation remains a client-only pending check.

## Alchemy expansion A-round complete: client verification / next board selection

Round 1 / P1-P3 is complete and recorded in `docs/reports/ALCHEMY_POTION_PROCESSING_FOUNDATION_P1_P3_20260629.md`.
Round 2 / P4 is complete and recorded in `docs/reports/ALCHEMY_POTION_PROCESSING_RUNTIME_P4_20260629.md`.
Round 3 / P5-P6 is complete and recorded in `docs/reports/ALCHEMY_POTION_BURDEN_METADATA_RUNTIME_P5_P6_20260629.md`.

Implemented so far:

- `data/herbcraft_alchemy/potion_processing_rules.json`;
- `PotionProcessingRules` loader/query/runtime-profile/burden-runtime layer;
- `AdvancedPotionStackInitializer.processingStyleId(...)` facades;
- tooltip projection for processing style and burden tendency/weights;
- style-aware clarified/murky acquisition routing;
- `PotionProcessingRuntime` for first-batch style behavior;
- persistent/synced `POTION_PROCESSING_STYLE` stack marker;
- persistent/synced `POTION_BURDEN` stack metadata;
- Alchemy-owned `PlayerPotionBurdenState` with save/load, death reset, alive-copy policy, and tick decay;
- validators `validate_potion_processing_rules.py`, `validate_potion_processing_runtime.py`, and `validate_potion_burden_runtime.py`.

Current behavior:

- `_long` maps to implemented `sustained`, receives bounded runtime style processing, and gets burden metadata.
- `_strong` maps to implemented `intense`, receives bounded runtime style processing, and gets burden metadata.
- `_clarified` maps to implemented `purified`, receives clean/stable runtime style processing, and gets burden metadata.
- `_long` / `_strong` still do not enter the legacy advanced random quality/bonus path.
- Player burden increases when processed potions are consumed, decays over time, persists to save, and clears on death.
- No P7 threshold consequences are active yet.
- `murky_edge` and `harmonized` remain deferred/reserved for runtime style behavior.

Round 4 / P7-P8 is complete: conservative burden stages/consequences and minimal cross-system extension hooks/read-only API were added. A follow-up A-round is also complete: `_long` / `_strong` no longer show misleading uninitialized quality text, and burden stages now use custom Alchemy effects (`potion_warming`, `potion_strain`, `potion_overload`) from `potion_burden_effects.json`; `clear_bearing` is registered as a future support/buffering effect. B3 distillation foundation is implemented in `docs/reports/ALCHEMY_DISTILLATION_B3_FOUNDATION_20260629.md`: one `herbcraft:distilled_essence` container item carries `source_essence` and `orientation` components, data-backed orientation/catalyst rules exist, and generic distillation recipes produce purified/sustained/intense distilled essences from any current essence. C distillation-to-potion routing is implemented in `docs/reports/ALCHEMY_DISTILLATION_TO_POTION_ROUTING_C_20260629.md`: matching base potion + matching distilled essence in the brewing stand routes into existing stable processed potion IDs. D armor-side burden support / protective alchemy is implemented in `docs/reports/ALCHEMY_ARMOR_SUPPORT_D_FOUNDATION_20260629.md`. D2 station migration is implemented in `docs/reports/ALCHEMY_STATION_MIGRATION_D2_20260629.md`: distillation now happens in the brewing stand, armor support now happens in the smithing table, and armor support uses blank/amethyst/echo adhesive semantics matching the weapon coating station identity. E processed weapon coating expansion is implemented in `docs/reports/ALCHEMY_PROCESSED_WEAPON_COATING_E_20260629.md`: distilled essences can now create processed weapon coatings through the existing smithing adhesive path while base coating remains valid. The distillation brewing-slot path has now been confirmed fixed by the user. The Alchemy/Core linkage line is continuing from `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md` on the preferred four-round track. Round 1 / P1 is complete: processing-style pharmacology semantics are JSON-backed, tooltip-projected, and explicitly taught in the Alchemy Codex guide layer. Round 2 / P2 is complete: processed potion stacks persist and sync source-side pharmacology projection metadata (`source_essence`, `temperature`, `flavors`, `taste_flavors`, `traits`), this projection is queryable through `AlchemyProcessingAPI`, and tooltips now show source pharmacology identity instead of collapsing fully into style-only behavior. Round 3 / P3 is complete: processed potion stacks persist and sync conservative body-state projection metadata (`medicinal_profile`, `medicinal_weight`, `thermal_tendency`, `thermal_value`, `qi_tendency`, `qi_value`, `body_traits`), tooltips project medicinal accumulation and body-state tendencies, and processed potion consumption now enters Core `PlayerPharmacologyState` recent-use + qi state rather than staying purely Alchemy-local. Round 4 / P4 + P5 is now also complete: processed potion consumption now conservatively participates in Core recent-intake / seven-relation context through `PotionRelationRuntime`, and catalyst semantics have been upgraded in tooltips/guide wording so honeycomb/redstone/glowstone/amethyst read as Herbcraft processing language rather than only vanilla modifier materials. The preferred four-round Alchemy/Core linkage track is therefore complete through P1-P5. The next user-facing work should return to the deferred issues the user explicitly parked earlier: display bugs, knowledge-gating problems, and 百草经-related design errors. Client-only verification for broader A-E behavior and JEI/REI visibility still remains relevant, but the current implementation-planning source for the finished linkage line remains `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md`.

## Pending client-only task: real-client JEI/REI verification for weapon coatings

This turn completed a stronger source-level fix for weapon coatings (武器淬层) being craftable in the smithing table but not searchable in JEI/REI-style viewers. Passive vanilla display metadata was not sufficient in the user's live client, so Alchemy now includes explicit optional JEI and REI plugins: JEI gets a smithing category extension plus explicit `RecipeTypes.SMITHING` recipe holders through the required `jei_mod_plugin` entrypoint, and REI gets explicit `DefaultSmithingDisplay` entries through `rei_client`, now expanded from `#herbcraft:coatable_weapons` rather than an iron-sword-only sample.

Next verification should be done in a real client with Core + Alchemy + JEI or REI:

1. search `淬层`, `coating`, `Blank Adhesive`, `Amethyst Adhesive`, `Echo Adhesive`, and several essence names;
2. confirm apply recipes show as smithing-table recipes: blank adhesive + coatable weapon + essence;
3. confirm refine recipes show amethyst/echo adhesive flows for essences with matching positive/negative effects;
4. confirm runtime smithing and hit effects are unchanged.

If client verification passes, resume the prior structural recommendation: split `HerbalChapter` into registration/projection/guide/text-provider layers.

## Recommended next task: structural decomposition phase 3 — split `HerbalChapter`

Structural decomposition Phase 2 is now complete: the nutrition subsystem has been separated into runtime, effect, discovery, and debug layers behind the stable `NutritionManager` facade.

The next highest-value structural reduction is now:

1. split `HerbalChapter` into registration/projection/guide/text-provider layers;
2. optionally introduce narrower Codex delta packets for claim-mark refreshes;
3. optionally standardize disconnect-clearing for all client caches.

Immediate runtime verification still recommended before or alongside Phase 3:

- nutrition gameplay remains unchanged;
- ingredient discovery / guide hints still fire correctly on food consume;
- Codex book still opens correctly;
- claim-mark updates refresh open Codex without reopening;
- death/respawn preserves knowledge but does not auto-open Codex.

Until Phase 3 and runtime verification are complete, treat the architecture as improved but not yet fully decomposed.

## Previous recommended next task: optional P4 runtime datapack test, then 1.1.3 release upload / Wiki split

The current source is packaged as the `1.1.3` release/test line with nutrition correction, 0.6x profile scaling, ingredients chapter, fluid surplus refinements, raw herb food-roll rebalance, and onboarding/Codex guidance polish.

Current status:

- Professional Role Alignment frozen per author instruction (`GAME_PLANNER_AND_DEVELOPER_COLLABORATION_SPEC_20260618.md`): Author is Lead Game Planner & System Architect (游戏策划); Agent is Lead Professional Developer executing 100% of codebase, AST verifications, and Gradle operations. Confirmed zero network lag / TPS impact of localized Item RAM traversal. Kicking off Yansheng (`严生`) Mode B design discussion and requesting Planner's acceptance criteria for immediate subproject provisioning.
- Systematic source audit of all recently implemented features in `1.1.3` completed as of 2026-06-18. Flawless algorithmic health confirmed, and architectural cleanups executed to decouple hardcoded item-path and counter ID assumptions. Zipped source updated with `docs/reports/SYSTEMATIC_SOURCE_AUDIT_AND_DECOUPLING_20260618.md`.
- Codex UI claim hitbox clearing bug is fully fixed (`contentHeight` no longer wipes interactive hitboxes, multi-line hitboxes are accurate, strict click bounds enforced). AST/Textual validator `scripts/validate_codex_hitboxes.py` added.
- Environment setup in the sandbox is fully verified (JDK 25 Temurin provisioned, automated validation suite 100% passed).
- Final source-level datapack integration verification is recorded in `docs/reports/DATAPACK_RUNTIME_COMPAT_VERIFICATION_20260617.md`. A runtime test datapack is available at `release_output/herbcraft_test_20260614_review/herbcraft-p4-runtime-test-datapack-20260617.zip`.
- Edge/regression source review is recorded in `docs/reports/EDGE_REGRESSION_REVIEW_20260617.md`; P1/P2 fixes are implemented in `docs/reports/EDGE_P1_P2_FIXES_20260617.md`. Remaining edge work is runtime-only/perf/cross-dimension/multiplayer testing plus optional custom-counter runtime verification.
- P1 Homeostatic temperature research is documented; no temperature gameplay integration exists yet.
- P2 low-risk external server-data merge layer is implemented and server-smoked.
- P3 startup-only loaded-mod/addon herb food extension is implemented, server-smoked, and author client-tested PASS.
- Nutrition correction/recent-meal redesign is implemented in source and built jars:
  - recent 24 meal tracking;
  - 3-minute correction scoring window;
  - correction score threshold 60;
  - data-backed correction relationship matrix in `data/herbcraft/nutrition_correction_rules.json`;
  - excessive dimensions extra-decay every 200 ticks by 15..45;
  - correction target 650;
  - high `fruit` / 津液 / Fluids non-punitive in the base mod;
  - high 津液 surplus helper: when fruit >= 850 and at least two non-fruit dimensions above 850 share a correction-matrix relationship, normalizes those related excesses toward 700 by 10 per 200 ticks;
  - client status `correcting` / `饮食回正中`;
  - debug presets and validator.
- 食材录 (Ingredient Codex) chapter implemented: 39 vanilla foods, data-driven from `ingredients.json`, eating-gated knowledge.
- All nutrition profiles scaled to 0.6x for balanced pacing.
- `correction_high_threshold` lowered to 750 with graduated decay (half-speed 750-849, full-speed 850+).
- Fluid surplus helper now requires correction-matrix relationship between excess dimensions.
- P4.1-P4.5 are complete for the read/event extension boundary: JSON spec, read-only Java API, observational event API, examples, validators/docs sweep, and `docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md`. Java write/registration APIs remain deferred.
- Full system Wiki draft exists at `docs/HERBCRAFT_FULL_SYSTEM_WIKI_1_1_3.md`; optional next docs work is splitting it into player-facing and developer-facing Wiki pages.
- `1.1.3` new-player guidance Phase 1 is implemented: route-sign advancement descriptions, rewritten four-line 百草经总纲, and polished 食材录 wording.
- `1.1.3` new-player guidance Phase 2.1 is implemented: guide entries are data-backed by module-owned `codex_guides.json`; Cuisine/Alchemy guide pages are addon-owned; 五味/七情 principle discovery is persisted and can reveal guide lines with latest item examples. A small Codex microcopy pass now explains plainly that 性味 is pharmacology, not nutrition, and 七情 is recent-herb interaction, not a fixed recipe list.
- `1.1.3` one-time guide hints are implemented as data-backed module-owned `guide_hints.json` files plus `GuideHintManager` with persisted seen-hint IDs.
- Pharmacology qi/relation data tuning is implemented: `temperature_values` and ordered `seven_emotion_rules` are in `pharmacology_rules.json`; exact hot/cold same-qi now builds more slowly.
- P3.9 Cuisine/Alchemy module overview guides are implemented: `膳房录` and `炼金要术` now have their own overview sections and staged guide subentries.
- P3.10 raw herb hunger/saturation rebalance is implemented in source: `food_rolls` support exists, 70 raw herb entries were tuned, and `scripts/validate_raw_herb_food_balance.py` guards the new balance. Client survival feel testing found seeds/sugar cane too weak, so a follow-up tuning pass increased seed hidden-saturation roll amounts and sugar cane hidden saturation while keeping sugar cane visible hunger at +1; retest pending.
- Remaining immediate work after `1.1.3` packaging: real-client smoke test the final jars in module combinations, especially guide visibility, one-time hints, pharmacology tuning, Cuisine/Alchemy hidden-catalyst wording, P3.10 raw herb food-roll balance, and the new external ingredient/client data sync path with a small datapack.
- Food-mod compatibility work completed its five tracked rounds for the current decision. Author client testing passed for Farmer's Delight Refabricated + More Delight. RMF was removed due datapack-driven item identity / vanilla-item collision risk. Reference generated datapack remains under `docs/compat_drafts/food_mods_26_1_2/generated_pack/`, but it is not a public old-jar route. The Farmer+More compatibility is bundled into Core, and ordinary foods with explicit nature+nutrition profiles participate in nature pharmacology through a generic bridge without overriding target food components.

## Why this is next

P4.1 through P4.5 are now complete for the read/event extension boundary: `docs/JSON_EXTENSION_SPEC_1_1_3.md` freezes the supported JSON paths, `com.herbcraft.api.HerbcraftDataAPI` exposes safe read-only Java queries, `com.herbcraft.api.HerbcraftEvents` exposes observational events, examples show both JSON content and Java read/event usage, and `docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md` summarizes release readiness. The source is now bumped to 1.1.3; food-nature tooltip/Jade/guide integration was fixed after client report; author final client sanity pass is now reported PASS, so the next step is release copy/platform final check. Separately, because Core jar behavior changed after the Farmer+More client test, one focused local sanity test with the refreshed Core jar and no standalone compat datapack is still recommended. Confirm Farmer+More entries still appear in 食材录/nutrition and that the 5 Farmer's Delight nature rows participate in pharmacology without breaking target food behavior.

Implementation report:

```text
docs/reports/NUTRITION_CORRECTION_IMPLEMENTATION_REPORT_20260615.md
```

Future ideas and a conservative `1.1.3` polish roadmap are collected in:

```text
docs/HERBCRAFT_FUTURE_IDEAS_AND_1_1_2_ROADMAP.md
```

Raw herb hunger/saturation rebalance source audit and refined parameter review are recorded in:

```text
docs/reports/HERB_FOOD_HUNGER_SATURATION_REBALANCE_REVIEW_20260616.md
docs/reports/HERB_FOOD_PROBABILITY_PARAMETER_REVIEW_20260616.md
docs/reports/HERB_FOOD_ROLLS_IMPLEMENTATION_REPORT_20260616.md
docs/reports/HERB_FOOD_SEED_SUGARCANE_TUNING_20260616.md
```

## Required next work

1.1.3 extension release track:

- Optional but recommended: install `herbcraft-p4-runtime-test-datapack-20260617.zip` in a test world, run `/reload`, drink two honey bottles to test a custom external special counter, and eat bread + baked potato to test datapack-added food-nature/七情 integration.
- Optional runtime check: create a tiny datapack with a new `special_counters` ID and verify threshold effects now count/persist.
- Current source is bumped to `1.1.3`; prepare final release sanity and copy.
- Run the full validator suite listed in `CURRENT_BASELINE.md` before tagging/uploading.
- Use `docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md` as the release-readiness checklist.
- Keep Java write/registration APIs deferred. P4.1-P4.5 delivered JSON spec + read API + event API + examples + validators/docs, not mutable registration APIs.
- If writing release copy, describe this as an extension/API and data-compatibility release.

Compatibility sanity track:

- Client sanity for refreshed `release_output/herbcraft_test_20260614_review/herbcraft-1.1.3.jar` + Farmer's Delight + More Delight without standalone compat datapack is reported PASS by the author. If changing code again, repeat this sanity set.
- Final checked set included Farmer+More built-in compat behavior, food-nature tooltip/Jade/guide behavior, and removal of raw rice/seeds from compat entries. Next action is release text/platform readiness, not more compat-table editing unless a new client bug appears.
- Confirm target food behavior is not overwritten: hunger/saturation/consumption still feel like Farmer's Delight / More Delight, because the bridge must not use startup `herbs` component override.
- Final public route: refreshed Core jar with built-in optional compat. The generated datapack is reference/example material only, because older jars lack the food-nature bridge.
- Do not re-add RMF unless explicitly creating a separate experimental pack.

General `1.1.3` stabilization track:

1. Client-test Phase 2.1/P3.7 guidance in four module combinations: Core only, Core + Cuisine, Core + Alchemy, and all three modules. Confirm addon guide pages and module-owned hints only appear with their addon, and confirm the new 五味/七情 Codex microcopy is clear enough without adding new advancements.
2. In a real client, trigger several pharmacology principles and verify 五味/七情 guide lines become clearer and show useful recent item examples without spoiling unearned item entries. Use `docs/reports/PHARMACOLOGY_SEVEN_EMOTION_TEST_MATRIX_20260616.md`.
3. Client-test pharmacology tuning: exact hot/cold should no longer trigger same-qi on the 3rd bite; with current values it should be closer to the 5th exact-hot/cold bite unless qi is already biased.
4. Client-test that data-backed seven-emotion relation rules still match the table in `docs/reports/PHARMACOLOGY_SEVEN_EMOTION_TEST_MATRIX_20260616.md`.
5. Client-test the implemented P3.10 raw herb hunger/saturation rebalance in the packaged `1.1.3` jars. In a fresh survival world, verify wildflowers/petals/ferns/leaves/saplings no longer act as reliable early food; retest seeds after the stronger hidden-saturation pass (`S+2` ordinary seeds, `S+3` melon/pumpkin seeds); sugar cane should remain visible +1 but now feel like juicy short fullness and Homeostatic hydration amount 3; blue orchid should no longer bypass the model with guaranteed `minecraft:saturation`.
6. Test debug presets:

```mcfunction
/herbcraft nutrition preset @p correcting_flesh
/herbcraft nutrition preset @p correcting_oil
/herbcraft nutrition preset @p correcting_grain
/herbcraft nutrition preset @p excess_fruit_nonpunitive
/herbcraft nutrition preset @p fluid_surplus_pair
/herbcraft nutrition preset @p fluid_surplus_single
/herbcraft nutrition preset @p fluid_surplus_unrelated
/herbcraft nutrition preset @p fruit_low
```

8. Check:
   - `饮食回正中` / correcting appears in the nutrition panel when correction is active;
   - high dimensions decay faster than the old `-2/minute` during correction;
   - high `fruit` / 津液 / Fluids does not apply negative dietary imbalance;
   - `fluid_surplus_pair` (flesh+vege+fruit=900) should show correcting status and both flesh/vege should decay;
   - `fluid_surplus_single` (flesh+fruit=900) should NOT trigger surplus decay;
   - `fluid_surplus_unrelated` (flesh+oil+fruit=900) should NOT trigger surplus decay (flesh↔oil have 0.0 weight);
   - low `fruit` / 津液 still applies deficiency;
   - normal deficiency, well_nourished, water bowl relief, Cuisine clearing isolation, and custom nutrition effects still behave correctly.
9. If behavior feels too weak/strong, tune:

```text
correction_high_threshold (now 750, was 850)
correction_score_threshold
correction_extra_decay_min
correction_extra_decay_max
correction_target_value
correction_recent_window_ticks
nutrition_correction_rules.json matrix weights
fluid_surplus_decay_amount
fluid_surplus_target_value
Note: 750-849 range uses half decay rate; 850+ uses full rate
```

10. Record client results in a new report under `docs/reports/`.
11. Only after `1.1.3` guidance and correction system are stable, draft P4 API design before coding:

```text
docs/PUBLIC_EXTENSION_API_DESIGN.md
```

UI data-driven and beautification roadmap:

- **Layer 1 DONE**: `codex_theme.json` and `nutrition_theme.json` implemented. All UI constants are data-driven. Resource packs can override themes. Nutrition panel supports dynamic dimension count with auto-scaling.
- **Layer 2 DONE**: `CodexSnapshotPayload.EntrySnap` now carries `itemId`, `temperature`, `flavors`. Item icon rendering and nature flavor tag rendering are implemented in `CodexScreen` right panel. All disabled by default; enable by setting `show_entry_item_icon: true` and `nature_tags.enabled: true` in `codex_theme.json`.
- **Beautification NEXT**: Multi-round texture art direction discussion → AI-generate textures (background paper, header, divider, corners, scrollbar) → author review → activate breadcrumb, back button, visible scrollbar, search bar, and completion badge in theme JSON. Then enable item icons and nature tags.
- Layer 3 (declarative layout engine): deferred to future version.

Hardcore mod direction (confirmed by author):

- Mode B confirmed: independent hardcore survival mod (`严生` or `难存`) + Herbcraft compat bridge.
- Implementation after UI beautification is stable.
- Decisions recorded in `docs/reports/DECISIONS_UI_AND_HARDCORE_MOD_20260617.md`.
- Full discussion in `docs/reports/UI_DATADRIVEN_AND_HARDCORE_MOD_DEEP_DISCUSSION_20260617.md`.

## Do not do unless explicitly asked

- Do not claim P3 supports hot-reloadable world datapack FOOD/CONSUMABLE changes.
- Do not add a hard dependency on Homeostatic.
- Do not implement P4 Java API before nutrition correction client validation/tuning.
- Do not change stable IDs.
- Do not make main-mod nutrition more hardcore; reserve stricter mechanics for a future addon.

## Acceptance criteria

- Build passes.
- Validators in `CURRENT_BASELINE.md` pass, including `python3 scripts/validate_raw_herb_food_balance.py`.
- Client sanity pass has no visible blocker.
- Any new report is included in `herbcraft-source-current-20260614.zip`.
- `release_output/herbcraft_test_20260614_review/` contains current Core/Alchemy `1.1.4` jars, Cuisine `1.1.3` jar, and source zip, with the source zip still named `herbcraft-source-current-20260614.zip`.
- `CURRENT_BASELINE.md` is updated after any further modifying turn.
