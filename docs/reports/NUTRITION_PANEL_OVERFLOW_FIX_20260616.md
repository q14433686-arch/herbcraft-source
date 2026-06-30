# Nutrition Panel Text Overflow Fix — 2026-06-16

Status: implemented in `1.1.2` source line

## Problem

Client testing found that the Nutrition panel opened by the nutrition keybind did not defensively handle translated text width. In English, the balanced status line could draw past both sides of the panel.

The Codex already had text wrapping/trimming and scissor protection; the Nutrition panel did not.

## Fix

Updated:

```text
core/src/main/java/com/herbcraft/nutrition/client/NutritionPanelScreen.java
scripts/validate_nutrition_correction_phase.py
```

Implemented:

- Title and bottom hint use `CodexTextLayout.trimWithEllipsis(...)` before drawing.
- Dimension labels are trimmed to the available space before the bar, preventing overlap.
- Status text uses `CodexTextLayout.wrap(...)` and is drawn centered with up to two lines.
- A panel-level `enableScissor(...)` / `disableScissor()` guard now prevents any translated text from drawing outside the panel even if a future language string is unexpectedly long.
- Validator coverage now checks that the Nutrition panel uses trim/wrap/scissor safeguards.

## Validation

Completed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/check_lang_diff.py
python3 scripts/verify_built_jars.py
```

Also reran the general validator suite after the change; all checks passed.

## Client retest target

Open the Nutrition panel in Chinese and English, especially while the status is:

```text
Diet is well-balanced. Body is in good shape.
```

Expected result: the text should wrap or trim inside the panel and never protrude beyond the panel edges.
