# Environment Setup 2026-06-30

## Summary

A fresh-clone environment setup pass was completed for the current Herbcraft `1.1.3` source line.

This pass was limited to environment preparation, authoritative-document review, build verification, and validator execution. No gameplay Java logic, JSON gameplay content, recipes, resources, language files, or runtime behavior were intentionally modified.

## Work completed

1. Cloned repository `q14433686-arch/herbcraft-source`.
2. Read root `AGENT_BRIEF.md`.
3. Regenerated the current handoff brief with:

```bash
python3 scripts/generate_agent_brief.py --write
```

4. Read the required authoritative anchors:

- `docs/reports/CURRENT_AGENT_BRIEF.md`
- `CURRENT_BASELINE.md`
- `docs/DOCUMENTATION_INDEX.md`
- `NEXT_TASK.md`
- `docs/JSON_EXTENSION_SPEC_1_1_3.md`
- `docs/EXTENDING_HERBCRAFT.md`
- `CHANGELOG.md`

5. Restored Gradle wrapper execute permission:

```bash
chmod +x gradlew
```

6. Provisioned JDK 25 using the repository helper:

```bash
bash scripts/setup_jdk25_temurin.sh
```

7. Built the full multi-module project successfully.
8. Ran the current validator suite successfully.

## Build verification

Command:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
```

Result:

- PASS (`BUILD SUCCESSFUL`)

Notes:

- Gradle wrapper downloaded `gradle-9.4.1` successfully in the fresh environment.
- Loom resolved as `1.16.3`, matching the documented baseline.
- Build output showed only ordinary deprecation/unchecked notes; no build failure occurred.

## Validator verification

The following validation set was executed successfully:

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
python3 scripts/verify_built_jars.py
```

Result:

- PASS (all listed validators completed successfully)

## Current environment facts confirmed

- Version line: `1.1.3`
- Minecraft target: `26.1.2`
- Java requirement: `25+`
- Gradle wrapper: `9.4.1`
- Local JDK path used for validation: `/tmp/herbcraft-jdk25/jdk-25`

## Client-only items not covered by this pass

The following remain client-only verification items and were not confirmable in the sandbox:

- live-client JEI/REI visibility behavior;
- in-game brewing/smithing interaction feel;
- broader real-client Alchemy A-E smoke verification;
- UI interaction and visual sanity beyond source-level/build-level validation.

## Output / artifact notes

This setup pass did not intentionally modify source gameplay/runtime content.

`release_output/` was not present in this fresh clone at the time of setup, so no local release artifact refresh was performed in this pass. A future modifying/publishing pass should recreate or refresh local output artifacts as required by the baseline workflow.
