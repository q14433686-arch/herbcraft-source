# Environment Setup and Verification Report

**Date:** 2026-06-18  
**Project Version:** `1.1.3`  
**Minecraft:** `26.1.2`  
**Java:** `25+` (`Temurin-25.0.3+9`)  

## 1. Executive Summary

In accordance with the user's instructions for the current turn (【阅读并遵循文档文件，搭建环境，等下一步确认】), we have performed a clean environment setup in the sandbox, compiled and built the entire `1.1.3` source line, and executed the comprehensive project validation suite.

All build tasks and validators passed successfully with zero errors or regressions. The working repository is fully stabilized and awaiting the user's next confirmed action.

## 2. Environment Setup Log

The following setup steps were executed:
1. Cloned the authoritative root repository (`https://github.com/q14433686-arch/herbcraft-source`).
2. Executed `python3 scripts/generate_agent_brief.py --write` and reviewed all primary authoritative anchors (`AGENT_BRIEF.md`, `CURRENT_BASELINE.md`, `NEXT_TASK.md`, `JSON_EXTENSION_SPEC_1_1_3.md`, `EXTENDING_HERBCRAFT.md`, `CHANGELOG.md`).
3. Provisioned JDK 25 in the sandbox via `bash scripts/setup_jdk25_temurin.sh`, resolving `Temurin-25.0.3+9` to `/tmp/herbcraft-jdk25/jdk-25`.
4. Granted execute permissions to Gradle wrapper (`chmod +x gradlew`) and executed the complete build:
   ```bash
   JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build
   ```
   **Result:** `BUILD SUCCESSFUL` (all 15 actionable subproject tasks executed and passed).

## 3. Automated Validation Suite (Fully Executed)

The entire validator suite mandated in `CURRENT_BASELINE.md` was run. Every script returned a completely passing status:

1. `python3 scripts/check_lang_diff.py` — **PASS:** All `zh_cn` and `en_us` language key sets match exactly across `core`, `alchemy`, and `cuisine`; `en_us` formatting/quality checks passed.
2. `python3 scripts/validate_custom_nutrition_effects_phase8.py` — **PASS:** Custom nutrition effect mechanics are JSON-backed; icons, stale effect refresh, sustained serious malnutrition, debug commands, water relief, and clear isolation are verified intact.
3. `python3 scripts/validate_nutrition_correction_phase.py` — **PASS:** Nutrition correction recent-meal tracking, weighted correction decay, non-punitive high fluids, client `correcting` status, debug presets, and persistence match specifications.
4. `python3 scripts/validate_externalized_rules.py` — **PASS:** All 18 rule tables (`special_counters`, `hodgepodge_rules`, `ingredients`, `pharmacology_rules`, `codex_guides`, `guide_hints`, `codex_loot_injections`, `essences`, `special_potions`, `witch_loot`, `potion_bonus_pools`, etc.) are correctly externalized to JSON and loaded by Java runtime code.
5. `python3 scripts/validate_raw_herb_food_balance.py` — **PASS:** Raw herb `food_rolls` balance is data-backed, common forage lacks stable visible food, blue orchid no longer bypasses rolls, and special bulk foods remain allowlisted.
6. `python3 scripts/validate_essence_resources.py` — **PASS:** All 40 essence definitions have complete resource coverage (items, models, textures, recipes, and language keys).
7. `python3 scripts/validate_homeostatic_compat.py` — **PASS:** Homeostatic thirst/drinkable compatibility covers 33 Cuisine entries and 40 Core round-2 entries, with vanilla duplicates avoided.
8. `python3 scripts/validate_nutrition_cuisine_immunity_isolation.py` — **PASS:** Nutrition effects successfully bypass dietary immunities, and food consumption re-evaluates nutrition immediately.
9. `python3 scripts/validate_recipe_isolation.py` — **PASS:** Hodgepodge correctly accepts vanilla empty bowl, rejects water bowl and suspicious-stew mushrooms, and handles hidden-combo empty-bowl isolation cases; raw dish recipes include medicinal and hidden dishes.
10. `python3 scripts/validate_item_textures.py` — **PASS:** Custom item texture references are complete and preserve vanilla bowl/bottle pixels.
11. `python3 scripts/validate_task_abcd.py` — **PASS:** Core `EffectImmunityManager` mechanics, saving/loading, death clearing, and specific dish/melon/carrot immunities and buffs are intact.
12. `python3 scripts/validate_extended_features.py` — **PASS:** Glistering melon slice values (4 nutrition / 9.6 saturation), herb stack threshold/fade private messages and overload overlays, hidden dishes (`hundred_flower_feast`, `deadly_hodgepodge`), unified advancement trees, and witch safe-potion murky variants are fully verified.
13. `python3 scripts/validate_trade_tags.py` — **PASS:** All villager trades and wandering trader data-driven entries are referenced by trade tags with `replace=false`.
14. `python3 scripts/validate_external_extension_spec.py` — **PASS:** Validates P4.1-P4.5 readiness documentation tokens, JSON/Java examples, read-only API tokens, event API classes, bundled Farmer+More compat data counts, and no-RMF / non-edible seed rules.
15. `python3 scripts/validate_animal_food_tags.py` — **PASS:** Animal food/tempt tags append to `minecraft` namespace with `replace=false`, valid items, no forbidden toxic items, and no vanilla duplicates.
16. `python3 scripts/validate_catalyst_mixin.py` — **PASS:** Catalyst mixin source, configuration, helpers, and built-jar class checks passed.
17. `python3 scripts/validate_p1_codex_advancements.py` — **PASS:** P1 Codex overview, data-driven guide registration, principle discovery hooks, and unified advancement tree validation passed.
18. `python3 scripts/validate_nature_jade.py` — **PASS:** Nature data, read API, Codex, and Jade lightweight compatibility validation passed.
19. `python3 scripts/verify_built_jars.py` — **PASS:** Confirmed all built jars in `release_output/herbcraft_test_20260614_review/` (`herbcraft-1.1.3.jar`, `herbcraft-alchemy-1.1.3.jar`, `herbcraft-cuisine-1.1.3.jar`) contain expected metadata, resources, and Java 25 bytecode.

## 4. Client-Only Verification Checklist

Because the sandbox executes headless and does not possess an interactive Minecraft client, the following recent UI features and experiential balances must be confirmed by the author in a real client before final publishing:

1. **Codex UI Theme (Layer 1 & Layer 2):**
   - Confirm that pressing **T** while the Codex is open instantly toggles between Light (宣纸) and Dark themes.
   - Confirm that Light theme text renders properly without dark-on-dark ghosting shadows (`shadow: false`).
   - If enabled in `codex_theme.json` (`show_entry_item_icon: true`), verify that 16×16 item textures render correctly next to entry titles across all chapters.
   - If enabled in `codex_theme.json` (`nature_tags.enabled: true`), verify that inline colored flavor/temperature tags display with correct visual aesthetics.
   - Verify that click-and-drag scrollbars function smoothly on both the left directory panel and right detail panel.
2. **Nutrition Panel UI Theme:**
   - Verify that the Nutrition panel auto-scales row heights gracefully for N dimensions and displays threshold marks on bars.
   - Confirm that `飲食回正中` / `correcting` appears and active faster decay triggers when a correction window is scored.
3. **Farmer's Delight + More Delight Built-in Compat:**
   - Confirm Farmer+More items appear correctly in 食材录 (for nutrition) and 药食志 (for nature/pharmacology) without requiring a standalone compat datapack.
   - Confirm that target mod food consumption timing and hunger/saturation values are not overwritten.
4. **Survival Feel & Pacing:**
   - Verify that P3.10 raw herb probability rolls function as expected in early survival (common forage does not spam stable visible food, while seeds `S+2`/`S+3` and sugar cane hidden saturation feel rewarding).

## 5. Artifact & Archive Status

The local publishing directory `release_output/herbcraft_test_20260614_review/` is fully populated and verified with the following artifacts:
- `herbcraft-1.1.3.jar`
- `herbcraft-alchemy-1.1.3.jar`
- `herbcraft-cuisine-1.1.3.jar`
- `herbcraft-source-current-20260614.zip` (Refreshed source archive maintaining the precise naming convention requested).
- `herbcraft-p4-runtime-test-datapack-20260617.zip` (Local runtime test artifact).
