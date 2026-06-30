# Herbcraft Documentation and Source Consistency Audit — 2026-06-16

Status: audit + cleanup pass implemented  
Baseline: Herbcraft `1.1.1`, planned `1.1.2` polish work in source

## 1. Why this pass happened

The author requested a full source-zip/source-tree sweep while local client testing continues:

1. Check documentation for stale or misleading instructions, old implementation status, wrong naming, old release/output guidance, and old design notes that could mislead the next maintainer or AI.
2. Check source code for unnecessary Java config, redundant checks, and mismatches between JSON balance/config files and Java fallback behavior.

This pass does not rewrite every historical report. Instead, it separates current authority from historical context and fixes the current handoff/docs that future agents are expected to read first.

## 2. Documentation cleanup performed

### Added current documentation map

```text
docs/DOCUMENTATION_INDEX.md
```

This file now tells future maintainers which files are authoritative current-state anchors, which are implemented reports, which are examples, and which are historical context.

### Updated current handoff / working docs

```text
README.md
AI_ONBOARDING.md
PROJECT_STATE.md
NEXT_TASK.md
BUGS_TODO.md
CHANGE_IMPACT.md
docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md
docs/HOMEOSTATIC_COMPAT.md
docs/NEW_PLAYER_GUIDANCE_1_1_2_DESIGN.md
docs/HERBCRAFT_FUTURE_IDEAS_AND_1_1_2_ROADMAP.md
```

Key corrections:

- P3.10 `food_rolls` and raw herb rebalance are now documented as implemented.
- Seed/sugar-cane post-client tuning is documented as current source state.
- Sugar cane Homeostatic hydration is documented and validator-anchored at `amount 3 / saturation 0.5`.
- 五味/七情 Codex microcopy is documented as implemented.
- `NEXT_TASK.md` no longer presents Codex guide implementation as the recommended next task; it now points to client testing of guidance, pharmacology, and raw-herb food-roll polish.
- `AI_ONBOARDING.md`, `PROJECT_STATE.md`, `BUGS_TODO.md`, and `CHANGE_IMPACT.md` now include the current validators and the new raw herb/Codex guidance facts.

### Historical docs marked to avoid confusion

Added top warnings to these files:

```text
docs/PROJECT_SUMMARY.md
docs/HOMEOSTATIC_COMPAT_DESIGN.md
docs/HOMEOSTATIC_COMPAT_NOTES.md
docs/Herbcraft_1.1.0_Combined_Mod_Page.md
docs/Herbcraft_1.1.1_Combined_Mod_Page.md
docs/RELEASE_PAGE_COPY_1.1.0.md
docs/RELEASE_PAGE_COPY_1.1.1.md
```

These files are still useful history or public release-copy snapshots, but they no longer present themselves as current source-state truth.

## 3. Source audit performed

### 3.1 26.1/Fabric forbidden API scan

Current Java/source scan found no active source usage of:

```text
TradeOfferHelper
appendHoverText
ItemGroupEvents
HudRenderCallback
modImplementation
remapJar
```

Fabric loot hooks still use `LootTableEvents.MODIFY` with v3 imports and `source.isBuiltin()` guarding in module Codex loot hooks, which matches the current 26.1 porting notes.

### 3.2 JSON-backed/fallback consistency check

Reviewed the major JSON-backed rule/config systems:

```text
NutritionRules / nutrition_rules.json / nutrition_correction_rules.json
PharmacologyRules / pharmacology_rules.json
HodgepodgeRules / hodgepodge_rules.json
HerbFoodRegistry / herbs.json food_rolls
Homeostatic drinkable data / validate_homeostatic_compat.py
CodexGuideRegistry / codex_guides.json
GuideHintManager / guide_hints.json
```

Existing validators already guard most of these. This pass tightened two areas:

1. `validate_p1_codex_advancements.py` now checks the new 五味/七情 explanatory `line2` keys and verifies `HerbalChapter` uses them.
2. `validate_nutrition_correction_phase.py` now guards that nutrition band thresholds and punitive imbalance spread use `NutritionRules`, not stale hardcoded values.

### 3.3 Code cleanup performed

Fixed one actual source inconsistency discovered during the audit:

```text
core/src/main/java/com/herbcraft/nutrition/NutritionManager.java
core/src/main/java/com/herbcraft/nutrition/PlayerNutritionState.java
scripts/validate_nutrition_correction_phase.py
```

Changes:

- `NutritionManager.applyDimensionEffects(...)` now uses data-backed thresholds:
  - `rules.balancedMin`
  - `rules.balancedMax`
  - `rules.imbalanceHigh`
- Warning gradient now uses `rules.balancedMin - rules.malnutritionLow` instead of an implicit 200-point hardcoded span.
- `PlayerNutritionState.hasPunitiveImbalance(...)` now uses `rules.imbalanceSpread` instead of hardcoded `500`.
- Removed the old unused private `applyFluidSurplusDecay(...)` helper from `PlayerNutritionState`; the live stacked decay path is `tickCorrectionDecay(...)`.
- Validator coverage now prevents these regressions.

This is behavior-preserving for current JSON values except that future JSON tuning of `balanced_min`, `balanced_max`, or `imbalance_spread` will now actually affect the Java logic consistently.

### 3.4 Not changed / intentionally retained

- Historical reports under `docs/reports/` remain historical snapshots unless they are current implementation reports.
- `SOURCE_REVIEW_2026-06-14.md` was already marked as superseded and retained for old baseline context.
- Public release-copy files are retained with warnings instead of being rewritten to current development state.
- `HerbDefinition` still exposes stable base `nutrition` / `saturation`, not `food_rolls`. This is acceptable for the current limited internal/public API because `food_rolls` are raw post-consumption settlement, not stable default food values; a future public Java API can extend this intentionally if needed.
- Hodgepodge nutrition still uses stable `HerbDefinition` values and does not inherit raw `food_rolls`. This is intentional: raw eating rolls should not become cooked/combined dish nutrition automatically.

## 4. Validation completed

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

## 5. Remaining non-blockers

- Real-client readability testing for the new 五味/七情 microcopy is still useful.
- Real-client survival feel retesting for stronger seeds/sugar cane is still useful.
- Historical docs are now marked, but not every old report was rewritten line-by-line. When in doubt, use `docs/DOCUMENTATION_INDEX.md` and current source.
- P4 stable public Java API is still deferred.
- Homeostatic temperature gameplay integration remains unimplemented.
