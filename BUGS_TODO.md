# BUGS_TODO

Current for Herbcraft `1.1.2` test baseline.

## Current known risks

| Priority | Area | Item |
|---:|---|---|
| P0 | Release sanity | Before public upload, run a clean build and all validators listed in `CURRENT_BASELINE.md`. |
| P0 | Client compatibility | Homeostatic compatibility has passed local client testing after the negative-amount fix, but should be rechecked if Homeostatic updates its data format or water storage behavior. |
| P0 | Brewing interaction | Hidden cuisine catalyst brewing passed local client testing once, but should be rechecked in any release candidate with all three jars installed. |
| P1 | Dynamic essence extension | Extra JSON essences can register, but polished addon entries still require assets, lang, recipes, and optional trade/Codex resources. Use `scripts/generate_essence_resources.py`. |
| P1 | Advancement reachability | Advancement JSON loads and count validators pass, but a full reachability/criteria audit remains useful. |
| P1 | Public extension docs | `docs/EXTENDING_HERBCRAFT.md` exists, but the extension surface is still a 1.1.x test-line contract, not a long-term stable public API promise. |
| P1 | P3.10 survival feel retest | Retest stronger seeds (`S+2` ordinary seeds, `S+3` melon/pumpkin seeds), sugar cane +1 visible/about +2 hidden saturation with Homeostatic amount 3, raw kelp short relief, and common forage/leaves spam. |
| P1 | Codex pharmacology guide readability | Retest the new 五味/七情 microcopy in the actual Codex UI; it should clarify mechanics without turning advancements into the tutorial. |
| P2 | Animal food validator | `validate_animal_food_tags.py` now searches the explicit cache, Loom client/merged jars, or `HERBCRAFT_VANILLA_CLIENT_JAR`. |
| P2 | REI | REI compatibility is not implemented. |
| P2 | Ingredients chapter client test | 食材录 chapter needs real-client verification: Codex entries, tooltip gating after eating, section display. |

## Recently addressed

### Source/jar consistency

- Built jars are now `1.1.2`.
- Alchemy and Cuisine depend on `herbcraft >=1.1.2`.
- `BrewingStandCatalystMixin.java` exists and builds.
- Hidden Cuisine dishes exist in source/resources/built jars.
- `百草经总纲` is registered in source.

### Nutrition and Homeostatic

- Nutrition uses custom Herbcraft effects, not vanilla negative effects.
- Nutrition effect mechanics and timing are data-backed by `data/herbcraft/nutrition_effects.json`.
- Stale nutrition effects are cleared on evaluation before current-state effects are applied.
- Severe malnutrition is sustained; warning states remain light/probabilistic.
- Cuisine clear effects skip Herbcraft nutrition effects.
- Milk bucket has gentle `flesh/oil` nutrition (`flesh:14, oil:10` after 0.6x scaling).
- All 162 nutrition profiles scaled to 0.6x for balanced pacing (2026-06-16).
- `correction_high_threshold` lowered from 850 to 750 with graduated decay (2026-06-16).
- Fluid surplus helper now requires correction-matrix relationship between excess dimensions (2026-06-16).
- 食材录 (Ingredient Codex) chapter added: 39 vanilla foods, data-driven from `ingredients.json`, eating-gated knowledge (2026-06-16).
- Fixed `NutritionRules` JSON-load fallback for `correction_high_threshold` from stale 850 to current 750 and added validator coverage (2026-06-16).
- Validator coverage expanded for `ingredients.json`, `pharmacology_rules.json`, built-jar resource checks, and stale `1.0.0` jar path checks (2026-06-16).
- Displayed `fruit` nutrition dimension is now `津液` / `Fluids`.
- Optional Homeostatic compatibility exists for Cuisine custom edible outputs and selected Core/vanilla Herbcraft edible items.
- Homeostatic negative `amount` values were removed after client testing showed below-zero internal water could keep dehydration active after partial recovery.
- Selected drink-like Cuisine dishes are `can_always_eat` so they can hydrate at full hunger.

### Data-driven architecture

The following major tables are JSON-backed:

- herbs
- herb natures
- nutrition profiles
- nutrition effects
- special counters
- Codex loot injections
- Cuisine dishes
- hodgepodge rules
- alchemy essences
- potion bonus pools
- special/T3/witch potions
- witch loot
- coating rules
- Homeostatic drinkable compatibility
- nutrition correction rules (matrix weights)
- pharmacology rules
- ingredients chapter entries

### Codex guidance and raw herb food-roll balance

- P3.10 raw herb `food_rolls` are implemented and data-backed; common scenery/flowers/leaves/saplings no longer provide stable early food.
- Client-feedback tuning strengthened seeds (`S+2` ordinary seeds, `S+3` melon/pumpkin seeds) and sugar cane (`nutrition 1`, `saturation_modifier 1.0`, Homeostatic amount 3).
- Core Codex 五味/七情 pages now include plain beginner-facing microcopy explaining that 性味 is pharmacology rather than nutrition, and 七情 is recent-herb interaction rather than a fixed recipe list.
- `scripts/validate_raw_herb_food_balance.py`, `scripts/validate_homeostatic_compat.py`, and `scripts/validate_p1_codex_advancements.py` guard these current expectations.

### Documentation and source consistency audit

- Added `docs/DOCUMENTATION_INDEX.md` to distinguish current authoritative docs from historical reports/release copy.
- Marked old project summary, old Homeostatic design notes, and release-copy snapshots as historical/non-authoritative where appropriate.
- Fixed nutrition threshold/spread consistency so `NutritionManager`/`PlayerNutritionState` use `NutritionRules` values instead of hardcoded threshold/spread literals.
- Removed the old unused `PlayerNutritionState.applyFluidSurplusDecay(...)` helper; stacked correction/fluid surplus decay is owned by `tickCorrectionDecay(...)`.

## TODO / future ideas

| Priority | Idea |
|---:|---|
| P1 | Add better documentation/examples for third-party Herbcraft addon authors after 1.1.2 settles. |
| P1 | Consider a dedicated Hardcore Nutrition addon. Do not harden the main mod further by default. |
| P1 | Consider Homeostatic temperature compatibility for ice/snow/hot foods, separate from thirst. |
| P2 | Improve generated or placeholder textures if the author dislikes in-game presentation. |
| P2 | Add more generators for cuisine/trades/Codex resources if extension work increases. |

## Required validation before release claim

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_raw_herb_food_balance.py
python3 scripts/validate_essence_resources.py
python3 scripts/validate_homeostatic_compat.py
python3 scripts/validate_nutrition_cuisine_immunity_isolation.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_catalyst_mixin.py
python3 scripts/validate_nature_jade.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```
