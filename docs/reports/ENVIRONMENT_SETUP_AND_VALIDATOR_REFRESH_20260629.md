# Environment setup and validator refresh — 2026-06-29

## Scope

This pass followed the repository onboarding sequence in `AGENT_BRIEF.md`, regenerated `docs/reports/CURRENT_AGENT_BRIEF.md`, read the current handoff anchors, and completed the sandbox environment setup for the current `1.1.3` source line.

No gameplay Java logic, JSON content, resources, recipes, language files, or jar-facing mod behavior was intentionally changed.

## Environment setup

- Cloned `q14433686-arch/herbcraft-source` into the Arena workspace.
- Ran:

```bash
python3 scripts/generate_agent_brief.py --write
```

- Read the current authoritative anchors:

```text
docs/reports/CURRENT_AGENT_BRIEF.md
CURRENT_BASELINE.md
docs/DOCUMENTATION_INDEX.md
NEXT_TASK.md
docs/JSON_EXTENSION_SPEC_1_1_3.md
docs/EXTENDING_HERBCRAFT.md
CHANGELOG.md
```

- Provisioned JDK 25 via:

```bash
bash scripts/setup_jdk25_temurin.sh
```

- Restored Gradle wrapper execute permission for local use.
- Built the full multi-module project with Java 25:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks
```

Result: `BUILD SUCCESSFUL`.

## Validator maintenance performed

Several source validators were still text-anchored to pre-split class locations even though the current source had already moved responsibilities into focused classes during earlier structural work. The validators were updated to check the current architecture rather than stale file locations:

- `scripts/validate_custom_nutrition_effects_phase8.py`
  - now checks `NutritionRuntimeService` / `NutritionEffectApplicator` and `HerbcraftDataBootstrap` in addition to the thin `NutritionManager` facade.
- `scripts/validate_nutrition_correction_phase.py`
  - now checks `NutritionRuntimeService`, `NutritionEffectApplicator`, and `NutritionDebugService` for correction/runtime/debug tokens.
- `scripts/validate_externalized_rules.py`
  - now checks bootstrap classes, `NutritionDiscoveryHooks`, and split HerbalChapter registration files.
- `scripts/validate_nutrition_cuisine_immunity_isolation.py`
  - now checks `NutritionEffectApplicator` for the scoped immunity-bypassing helper while preserving the immediate post-food re-evaluation guard.
- `scripts/validate_external_extension_spec.py`
  - now accounts for event firing in `NutritionDiscoveryHooks` and food-nature projection tokens in split `HerbalChapter*` files.
- `scripts/validate_nature_jade.py`
  - now accounts for nature line / trait hint usage in split `HerbalChapter*` files.
- `scripts/validate_p1_codex_advancements.py`
  - now accounts for overview/guide/provider logic in split `HerbalChapter*` files.

These are validator-only maintenance changes. They do not alter runtime mechanics.

## Validation executed

### Build

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks
```

Result: PASS.

### Full source validator set

All listed validators passed after the location-aware validator refresh:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_raw_herb_food_balance.py
python3 scripts/validate_essence_resources.py
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
python3 scripts/verify_built_jars.py
```

## Output refresh

The local publishing/test output directory was recreated because `release_output/` is git-ignored and was absent in the fresh clone. It now contains rebuilt jars copied from the successful Gradle build and a refreshed source zip.

Expected local files:

```text
release_output/herbcraft_test_20260614_review/herbcraft-1.1.3.jar
release_output/herbcraft_test_20260614_review/herbcraft-alchemy-1.1.3.jar
release_output/herbcraft_test_20260614_review/herbcraft-cuisine-1.1.3.jar
release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip
release_output/herbcraft_test_20260614_review/herbcraft-p4-runtime-test-datapack-20260617.zip
```

The historical P4 runtime test datapack zip was also recreated from source-side files under `docs/compat_drafts/p4_runtime_datapack_test/`.

## Client-only checks still required

The current sandbox cannot run a real Minecraft client. The following remain client-only:

- JEI/REI live plugin discovery and search/display confirmation with the user's exact installed viewer versions.
- Runtime smithing-table coating behavior and hit-effect behavior in a client world.
- Codex UI interaction/theme/scroll behavior.
- Farmer's Delight + More Delight live optional compat behavior.
- Raw herb early-survival feel tuning.
