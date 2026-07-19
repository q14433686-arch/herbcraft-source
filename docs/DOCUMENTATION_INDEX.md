# Herbcraft Documentation Index

Status: current documentation map for the Core/Alchemy `1.1.4` source line; Cuisine remains `1.1.3`; the Minecraft `26.2` port is currently in validation.
Last updated: 2026-07-19

Use this file to avoid treating historical design notes as the current source of truth.

For future AI/helper sessions, the shortest safe handoff is:

```bash
python3 scripts/generate_agent_brief.py --write
```

then read `docs/reports/CURRENT_AGENT_BRIEF.md`.

## 1. Authoritative current-state anchors

Read these first when taking over the project:

```text
AGENT_BRIEF.md
CURRENT_BASELINE.md
README.md
AI_ONBOARDING.md
PROJECT_STATE.md
NEXT_TASK.md
CHANGELOG.md
docs/DATA_DRIVEN_ARCHITECTURE.md
docs/EXTENDING_HERBCRAFT.md
docs/AI_COLLABORATION_GUIDE.md
docs/ADDON_AUTHOR_QUICKSTART_1_1_4.md
docs/HERBCRAFT_FULL_SYSTEM_WIKI_1_1_3.md
docs/wiki/README.md
docs/JSON_EXTENSION_SPEC_1_1_4.md
docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md
docs/PUBLIC_EXTENSION_API_DESIGN.md
reference_cache/26_1_porting_notes.md
```

Important rule:

```text
Current user request > current source > current documentation > older memory or old reports.
```

## 2. Current release/upload copy

Use these for current Core/Alchemy `1.1.4` platform descriptions and version changelogs:

```text
docs/RELEASE_PAGE_COPY_1.1.4.md        # Modrinth / CurseForge, English first
docs/MCMOD_RELEASE_COPY_1.1.4.md       # MC百科 / MCMOD, Chinese community copy
```

## 3. Current implementation reports / design references

These reports describe implemented or recently tuned systems that are part of the current working tree:

```text
docs/reports/ALCHEMY_TRADE_LOOT_ARROW_ADAPTATION_20260630.md
docs/reports/ALCHEMY_ADVANCEMENT_AND_SYNC_AUDIT_20260630.md
docs/reports/ALCHEMY_VIEWER_COMPONENT_MATCHING_FIXES_20260630.md
docs/reports/ALCHEMY_BURDEN_PROFILE_AND_ARMOR_SPECIALIZATION_20260630.md
docs/reports/ENVIRONMENT_SETUP_ARENA_20260630.md
docs/reports/ENVIRONMENT_SETUP_20260630.md
docs/reports/ALCHEMY_VIEWER_ADAPTERS_20260630.md
docs/reports/ALCHEMY_KNOWLEDGE_GATING_CODEX_FIXES_20260630.md
docs/reports/ALCHEMY_PROCESSED_WEAPON_COATING_E_20260629.md
docs/reports/ALCHEMY_STATION_MIGRATION_D2_20260629.md
docs/reports/ALCHEMY_ARMOR_SUPPORT_D_FOUNDATION_20260629.md
docs/reports/ALCHEMY_DISTILLATION_TO_POTION_ROUTING_C_20260629.md
docs/reports/ALCHEMY_DISTILLATION_B3_FOUNDATION_20260629.md
docs/reports/ALCHEMY_CUSTOM_BURDEN_EFFECTS_A_ROUND_20260629.md
docs/reports/ALCHEMY_BURDEN_CONSEQUENCES_AND_EXTENSION_HOOKS_P7_P8_20260629.md
docs/reports/ALCHEMY_POTION_BURDEN_METADATA_RUNTIME_P5_P6_20260629.md
docs/reports/ALCHEMY_POTION_PROCESSING_RUNTIME_P4_20260629.md
docs/reports/ALCHEMY_POTION_PROCESSING_FOUNDATION_P1_P3_20260629.md
docs/reports/ALCHEMY_EXPANSION_FEASIBILITY_REVIEW_20260629.md
docs/reports/ENVIRONMENT_SETUP_AND_VALIDATOR_REFRESH_20260629.md
docs/reports/COATING_RECIPE_DISPLAY_BUGFIX_20260629.md
docs/reports/NUTRITION_CORRECTION_IMPLEMENTATION_REPORT_20260615.md
docs/reports/PHARMACOLOGY_BALANCE_AND_RELATION_REVIEW_20260616.md
docs/reports/PHARMACOLOGY_SEVEN_EMOTION_TEST_MATRIX_20260616.md
docs/reports/HERB_FOOD_ROLLS_IMPLEMENTATION_REPORT_20260616.md
docs/reports/HERB_FOOD_SEED_SUGARCANE_TUNING_20260616.md
docs/reports/CODEX_GUIDANCE_PHASE2_1_IMPLEMENTATION_SPEC_20260616.md
docs/reports/CUISINE_ALCHEMY_GUIDE_COPY_PHASE3_9_SPEC_20260616.md
docs/reports/EXTERNAL_INGREDIENTS_CLIENT_SYNC_GENERATOR_API_20260616.md
docs/reports/FARMER_MORE_BUILTIN_COMPAT_AND_NATURE_BRIDGE_20260617.md
docs/reports/FOOD_NATURE_DISPLAY_AND_GUIDE_FIX_20260617.md
docs/reports/P4_2_READONLY_JAVA_API_20260617.md
docs/reports/P4_3_EVENT_API_20260617.md
docs/reports/P4_4_EXAMPLES_20260617.md
docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md
docs/reports/EDGE_REGRESSION_REVIEW_20260617.md
docs/reports/EDGE_P1_P2_FIXES_20260617.md
docs/reports/DATAPACK_RUNTIME_COMPAT_VERIFICATION_20260617.md
docs/reports/UI_REVIEW_AND_HARDCORE_NUTRITION_ADDON_DISCUSSION_20260617.md
docs/reports/UI_DATADRIVEN_AND_HARDCORE_MOD_DEEP_DISCUSSION_20260617.md
```

Current key facts from these reports:

- Codex guide entries and one-time guide hints are data-backed and module-owned.
- Pharmacology temperature values and ordered seven-emotion rules are data-backed in `pharmacology_rules.json`.
- Raw herb `food_rolls` are implemented for bundled/startup herb food data.
- Common forage no longer acts as reliable early food.
- Seeds were strengthened after client testing: ordinary seeds roll `S+2`, melon/pumpkin seeds roll `S+3`.
- Sugar cane remains +1 visible hunger, now gives about +2 hidden saturation, and Homeostatic hydration is anchored at amount 3.
- 五味/七情 Codex guide pages now include plain beginner-facing microcopy.
- Food-mod compatibility Rounds 1-5 are tracked under `docs/compat_drafts/food_mods_26_1_2/`; author client testing passed for Farmer's Delight + More Delight, RMF was removed, current generated pack is `generated_pack/`, source table is `compat_review_round5_farmer_moredelight.csv`, final Chinese nature table is `ROUND5_FINAL_CHINESE_NATURE_TABLE.md`, and final report is `ROUND5_CLIENT_TEST_REPORT.md`.
- Farmer's Delight + More Delight data is also bundled into Core as optional data under `data/farmers_moredelight_herbcraft/herbcraft/`, and a generic food-nature pharmacology bridge lets explicit nature+nutrition ordinary foods participate in nature pharmacology without overriding target food components. Tasted nature-bearing foods appear in 百草志 `药食志`; nutrition remains in 食材录.
- Alchemy processed weapon coating E is implemented in `docs/reports/ALCHEMY_PROCESSED_WEAPON_COATING_E_20260629.md`: distilled essences can now create processed weapon coatings through the existing smithing-table adhesive path, processing style is stored on weapon coatings, runtime effects/uses are data-backed, and amethyst/echo refinement preserves style.
- Alchemy station migration D2 is implemented in `docs/reports/ALCHEMY_STATION_MIGRATION_D2_20260629.md`: distillation moved from crafting table to brewing stand, and armor support moved from crafting table to smithing table with blank/amethyst/echo adhesive linkage.
- Alchemy armor-side support D foundation is implemented in `docs/reports/ALCHEMY_ARMOR_SUPPORT_D_FOUNDATION_20260629.md`: armor pieces can receive componentized support from distilled essences, equipped support buffers processed-potion burden through the P8 hook, and the design remains protective/supportive rather than retaliation-first.
- Alchemy distillation-to-potion routing C is implemented in `docs/reports/ALCHEMY_DISTILLATION_TO_POTION_ROUTING_C_20260629.md`: matching base Herbcraft potion + componentized distilled essence in the brewing stand now routes to `_clarified`, `_long`, `_strong`, or wither special output while preserving stable potion IDs and old routes.
- Alchemy distillation B3 foundation is implemented in `docs/reports/ALCHEMY_DISTILLATION_B3_FOUNDATION_20260629.md`: `herbcraft:distilled_essence` is a single styled container item with persistent `source_essence`/`orientation` component, data-backed orientation rules, generic distillation recipes, tooltips, and read-only API. It intentionally avoids per-source item explosion.
- Alchemy custom burden effects A-round is implemented in `docs/reports/ALCHEMY_CUSTOM_BURDEN_EFFECTS_A_ROUND_20260629.md`: `_long`/`_strong` no longer show misleading uninitialized quality text, and burden stages now use custom Alchemy MobEffects from `potion_burden_effects.json` instead of direct raw movement modifiers.
- Alchemy expansion Round 4 / P7-P8 is implemented in `docs/reports/ALCHEMY_BURDEN_CONSEQUENCES_AND_EXTENSION_HOOKS_P7_P8_20260629.md`: potion burden now has data-backed stages with mild movement consequences, and minimal events/read-only API hooks exist for future distillation, armor support, and advanced coating integration.
- Alchemy expansion Round 3 / P5-P6 is implemented in `docs/reports/ALCHEMY_POTION_BURDEN_METADATA_RUNTIME_P5_P6_20260629.md`: processed potions now receive persistent/synced burden metadata, player potion burden state persists/decays and clears on death.
- Alchemy expansion Round 2 / P4 is implemented in `docs/reports/ALCHEMY_POTION_PROCESSING_RUNTIME_P4_20260629.md`: `PotionProcessingRuntime` now applies data-backed first-batch runtime profiles for `sustained`, `intense`, and `purified`, while `murky_edge` / `harmonized` remain deferred and `_long` / `_strong` still avoid the legacy random quality/bonus path.
- Alchemy expansion Round 1 / P1-P3 is implemented in `docs/reports/ALCHEMY_POTION_PROCESSING_FOUNDATION_P1_P3_20260629.md`: `potion_processing_rules.json`, `PotionProcessingRules`, style-id facades, tooltip projection, and style-aware acquisition routing now exist.
- Alchemy expansion planning feasibility review is recorded in `docs/reports/ALCHEMY_EXPANSION_FEASIBILITY_REVIEW_20260629.md`; source verification found that the attached master-status document is ahead of the current repo for potion-processing/burden claims, while current source has only the existing advanced-potion quality/bonus and clarified/murky/long/strong groundwork.
- Current sandbox environment setup and validator-location refresh are recorded in `docs/reports/ENVIRONMENT_SETUP_AND_VALIDATOR_REFRESH_20260629.md`; the full build and validator suite pass after updating validators to the split bootstrap/nutrition/HerbalChapter source layout.
- Weapon coating JEI/REI visibility is tracked in `docs/reports/COATING_RECIPE_DISPLAY_BUGFIX_20260629.md`; Alchemy now includes explicit optional JEI and REI plugins for coating smithing displays, guarded by `scripts/validate_coating_viewer_plugins.py`.
- P4.1-P4.5 are complete for the read/event extension boundary: JSON spec, read-only Java API, observational event API, examples, validators, and readiness report. Write/registration APIs remain deferred.
- Full system Wiki draft `docs/HERBCRAFT_FULL_SYSTEM_WIKI_1_1_3.md` remains mechanism-valid for the version-only Core/Alchemy `1.1.4` bump; it explains Codex, 食材录, 药食志, raw herb eating, 药性回旋, 性味/五味/特质/气机/七情, nutrition, Cuisine, Alchemy, compatibility, and extension boundaries.
- AI/developer handoff prompt guide is `docs/AI_COLLABORATION_GUIDE.md`; addon author quickstart is `docs/ADDON_AUTHOR_QUICKSTART_1_1_4.md`; canonical future Wiki source lives under `docs/wiki/`.
- P4 datapack runtime verification report is `docs/reports/DATAPACK_RUNTIME_COMPAT_VERIFICATION_20260617.md`; test datapack zip is `release_output/herbcraft_test_20260614_review/herbcraft-p4-runtime-test-datapack-20260617.zip`.
- UI review and Hardcore Nutrition addon direction discussion is `docs/reports/UI_REVIEW_AND_HARDCORE_NUTRITION_ADDON_DISCUSSION_20260617.md`; covers Codex, Nutrition Panel, tooltips, Jade stability (no P0 blockers), and a two-phase addon architecture recommendation (Tier A data-only with 1.1.3, Tier B dynamic dimensions with Core 1.2.0).
- Deep UI data-driven architecture and hardcore mod positioning discussion is `docs/reports/UI_DATADRIVEN_AND_HARDCORE_MOD_DEEP_DISCUSSION_20260617.md`; covers three-layer data-driven UI plan, pixel-level Codex beautification spec with texture requirements, hardcore mod Mode B (independent core + bridge) recommendation, health slider concept, seasonal herb interaction, and 6 open design questions.

## 4. Current extension/addon examples

```text
docs/examples/external_extension_compat_pack/
docs/examples/herbcraft_readonly_event_addon/
docs/examples/startup_herb_food_extension_mod/
docs/examples/essence_extension_example/
```

Validators:

```text
scripts/validate_external_extension_spec.py
scripts/validate_essence_resources.py
scripts/generate_essence_resources.py --check
scripts/generate_compat_pack.py
```

## 5. Homeostatic documents

Current practical reference:

```text
docs/HOMEOSTATIC_COMPAT.md
```

Historical/research references:

```text
docs/HOMEOSTATIC_COMPAT_DESIGN.md
docs/HOMEOSTATIC_COMPAT_NOTES.md
docs/HOMEOSTATIC_TEMPERATURE_COMPAT_NOTES.md
```

Notes:

- Thirst/drinkable data compatibility is implemented.
- Do not use negative Homeostatic `amount` values.
- Sugar cane Homeostatic hydration is currently `amount 3 / saturation 0.5`.
- Temperature gameplay integration is not implemented.

## 6. Historical documents retained for context

The following files are useful history but must not be treated as current authoritative state without source verification:

```text
SOURCE_REVIEW_2026-06-14.md
docs/PROJECT_SUMMARY.md
docs/Herbcraft_1.1.0_Combined_Mod_Page.md
docs/Herbcraft_1.1.0_Combined_Mod_Page.md
docs/Herbcraft_1.1.1_Combined_Mod_Page.md
docs/RELEASE_PAGE_COPY_1.1.0.md
docs/RELEASE_PAGE_COPY_1.1.1.md
docs/reports/P1_*.md
docs/reports/STAGE*.md
docs/reports/RUN_REPORT_2026-06-12.md
```

Why:

- Some describe old jar names or pre-current polish state.
- Some predate data-backed Codex guides, `food_rolls`, guide hints, pharmacology rule externalization, or current nutrition correction tuning.
- Some are public release copy snapshots and should remain as historical copy, not source-state documentation.

## 7. Future idea / planning documents

These are idea banks, not implementation truth:

```text
docs/HERBCRAFT_FUTURE_IDEAS_AND_1_1_2_ROADMAP.md
docs/HARDCORE_NUTRITION_ADDON_PLAN.md
docs/EXTENSION_GENERATOR_PLAN.md
```

When implementing from them:

1. Re-read current source first.
2. Check `CURRENT_BASELINE.md` and `NEXT_TASK.md`.
3. Write or update a validator if the feature affects data or runtime behavior.
4. Refresh the source zip after documentation/source changes.

## 8. Release output convention

Current local baseline path remains:

```text
release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip
```

Do not rename this source zip or output folder unless the author explicitly asks.

`release_output/` is local/publishing-only and git-ignored. Public jars should be uploaded through GitHub Releases/platforms, not committed to source.
