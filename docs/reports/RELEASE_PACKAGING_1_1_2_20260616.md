# Herbcraft 1.1.2 Release Packaging Report — 2026-06-16

Status: packaged in local release output; final author/client smoke pending  
Minecraft: `26.1.2`  
Fabric Loader: `0.19.3+`  
Java: `25+`

## 1. Scope

This packaging pass turns the current `1.1.2` polish source line into local release/test jars while preserving the required local source archive naming convention:

```text
release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip
```

## 2. Last copy fix before packaging

Cuisine guide page:

```text
膳房录 / 膳房总述 / 隐秘菜肴与炼金催化
```

was updated so it clearly states that alchemy catalyst linkage requires the Alchemy chapter/module:

```text
少数菜肴似乎不止能入口。若另有《炼金要术》（安装炼金分册），极端之膳也可能成为炼金催化。
```

English:

```text
A few dishes seem to be more than food. If the Alchemy chapter is installed, extreme meals may even become alchemical catalysts.
```

The hint and discovered deadly-hodgepodge line were also adjusted to mention that alchemy linkage requires the Alchemy chapter/module.

Validator coverage was added in:

```text
scripts/validate_p1_codex_advancements.py
```

so the Chinese text keeps `炼金分册` and English keeps `Alchemy chapter`.

## 3. Version bump

Changed:

```text
build.gradle                         version = 1.1.2
core/src/main/resources/fabric.mod.json       1.1.2
alchemy/src/main/resources/fabric.mod.json    1.1.2, herbcraft >=1.1.2
cuisine/src/main/resources/fabric.mod.json    1.1.2, herbcraft >=1.1.2
scripts/verify_built_jars.py                 VERSION = 1.1.2
```

Current local output jars:

```text
herbcraft-1.1.2.jar
herbcraft-alchemy-1.1.2.jar
herbcraft-cuisine-1.1.2.jar
```

## 4. Documentation updated

Updated current docs/handoff files to describe the source line as packaged `1.1.2`:

```text
README.md
AI_ONBOARDING.md
PROJECT_STATE.md
NEXT_TASK.md
BUGS_TODO.md
CURRENT_BASELINE.md
docs/DOCUMENTATION_INDEX.md
docs/EXTENDING_HERBCRAFT.md
docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md
docs/examples/startup_herb_food_extension_mod/fabric.mod.json
CHANGELOG.md
```

Historical release-copy snapshots remain historical and are listed as such in `docs/DOCUMENTATION_INDEX.md`.

## 5. Validation completed

```bash
bash scripts/setup_jdk25_temurin.sh
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
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
python3 scripts/verify_built_jars.py
```

Result: all passed.

## 6. Remaining release sanity

Before public upload, do a final local client smoke with the packaged `1.1.2` jars:

1. Core only.
2. Core + Cuisine.
3. Core + Alchemy.
4. All three modules.

Focus:

- module-owned Codex guide pages only appear with their modules;
- Cuisine hidden catalyst wording is clear;
- Alchemy catalyst interactions still work when Alchemy is installed;
- raw herb food-roll balance remains acceptable;
- seed/sugar-cane tuning feels right;
- 五味/七情 microcopy fits in the Codex UI.
