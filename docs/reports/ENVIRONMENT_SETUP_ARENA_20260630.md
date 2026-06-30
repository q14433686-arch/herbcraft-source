# Arena environment setup and validator sync 2026-06-30

## Summary

A fresh Arena.ai Agent Mode workspace was set up for the current Herbcraft `1.1.3` source line from repository `q14433686-arch/herbcraft-source`.

This pass completed the requested report/handoff review, build environment provisioning, full Gradle build, validator execution, validator maintenance for stale source expectations, and local `release_output/` artifact recreation.

No gameplay Java logic, JSON balance/content, recipes, assets, or language behavior were intentionally changed. The only source-side edits were validator-script updates so the current validator suite matches the already-current source architecture and compact tooltip/Codex migration decisions.

## Required onboarding completed

1. Read root `AGENT_BRIEF.md`.
2. Regenerated the current handoff brief:

```bash
python3 scripts/generate_agent_brief.py --write
```

3. Read `docs/reports/CURRENT_AGENT_BRIEF.md`.
4. Read the requested anchor documents in order:

```text
CURRENT_BASELINE.md
docs/DOCUMENTATION_INDEX.md
NEXT_TASK.md
docs/JSON_EXTENSION_SPEC_1_1_3.md
docs/EXTENDING_HERBCRAFT.md
CHANGELOG.md
```

## Environment setup

- Restored Gradle wrapper execute permission:

```bash
chmod +x gradlew
```

- Provisioned JDK 25 with the repository helper because `/tmp/herbcraft-jdk25/jdk-25` was absent in this fresh sandbox:

```bash
bash scripts/setup_jdk25_temurin.sh
```

- Confirmed Temurin JDK 25.0.3 was installed under `/tmp/herbcraft-jdk25/jdk-25`.

## Build verification

Command:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result:

- PASS (`BUILD SUCCESSFUL`)

Notes:

- Gradle wrapper downloaded Gradle `9.4.1` in the fresh workspace.
- Fabric Loom resolved as `1.16.3`.
- Build warnings were limited to ordinary deprecation/unchecked notes.

## Validator maintenance in this pass

Three validators had stale expectations relative to the current source/docs state and were refreshed:

1. `scripts/validate_externalized_rules.py`
   - Updated the expected Alchemy guide ID set to include the current `guide_alchemy_armor_support` entry.
   - This matches the already-current `alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json` and the knowledge-gating/Codex work recorded earlier.

2. `scripts/validate_potion_burden_runtime.py`
   - Updated the tooltip assertion after verbose burden/semantic lines were intentionally moved out of compact item tooltips and into mastered Codex pages.
   - The validator now checks that burden metadata/runtime wiring remains present while the compact tooltip stays free of `tooltip.herbcraft.potion.burden_weights` / direct `POTION_BURDEN` projection.

3. `scripts/validate_panel_hodge_recipe.py`
   - Updated Codex translucency checks to inspect the data-driven `codex_theme.json` values rather than expecting old hardcoded color literals inside `CodexScreen.java`.
   - Updated the hodgepodge count check to accept the current JSON-backed `HodgepodgeRules` path (`minHerbs` / `normalMaxHerbs`) instead of fixed Java literals.

These are validator-only changes. They do not change jar-facing gameplay behavior.

## Validation executed

After the build and validator maintenance, the following validators passed:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_raw_herb_food_balance.py
python3 scripts/validate_essence_resources.py
python3 scripts/validate_potion_processing_rules.py
python3 scripts/validate_potion_processing_runtime.py
python3 scripts/validate_potion_burden_runtime.py
python3 scripts/validate_potion_burden_consequences.py
python3 scripts/validate_alchemy_burden_effects.py
python3 scripts/validate_alchemy_expansion_boundaries.py
python3 scripts/validate_distillation_foundation.py
python3 scripts/validate_distillation_brewing_routing.py
python3 scripts/validate_distillation_brewing_slot_acceptance.py
python3 scripts/validate_armor_support_foundation.py
python3 scripts/validate_processed_coating_expansion.py
python3 scripts/validate_homeostatic_compat.py
python3 scripts/validate_nutrition_cuisine_immunity_isolation.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_catalyst_mixin.py
python3 scripts/validate_nature_jade.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/validate_coating_recipe_display.py
python3 scripts/validate_coating_viewer_plugins.py
python3 scripts/validate_bootstrap_split_phase1.py
python3 scripts/validate_nutrition_split_phase2.py
python3 scripts/validate_coupling_hotspots.py
python3 scripts/validate_codex_payload_separation.py
python3 scripts/validate_codex_respawn_ui_state.py
python3 scripts/validate_player_state_respawn_copy.py
python3 scripts/validate_potion_source_projection.py
python3 scripts/validate_potion_body_state_projection.py
python3 scripts/validate_potion_relation_and_catalyst_semantics.py
python3 scripts/validate_alchemy_knowledge_gating_and_codex.py
python3 scripts/validate_codex_hitboxes.py
python3 scripts/validate_herbalchapter_split_phase3.py
python3 scripts/validate_panel_hodge_recipe.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/verify_built_jars.py
```

Result:

- PASS for all listed checks.

## Output artifacts refreshed

Because validator/report/docs changed and the fresh workspace had no `release_output/`, the local publishing/test output directory was recreated:

```text
release_output/herbcraft_test_20260614_review/
```

Current files recreated/refreshed:

```text
herbcraft-1.1.3.jar
herbcraft-alchemy-1.1.3.jar
herbcraft-cuisine-1.1.3.jar
herbcraft-source-current-20260614.zip
herbcraft-p4-runtime-test-datapack-20260617.zip
```

Jars were copied from the successful Gradle build. The source zip was regenerated from the current repository state, excluding build/cache/output directories.

## Client-only checks not covered

The sandbox cannot confirm live-client behavior. The following remain client-only:

- JEI installed without REI: optional entrypoint loading and Brewing/Smithing display visibility.
- REI installed without JEI: optional entrypoint loading and Brewing/Smithing display visibility.
- Search/click behavior for componentized distilled essences, processed potion bottle variants, armor support stacks, and processed coating variants.
- Processed potion tooltip readability across knowledge states.
- Splash/lingering potion exposure runtime behavior in an actual client world.
- Codex mastered-page scrolling/readability and guide discoverability in the real UI.
