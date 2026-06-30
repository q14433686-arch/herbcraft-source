# PROJECT_STATE

This file records the current repository/source state. It has been updated for the 2026-06-30 Core/Alchemy 1.1.4 version-only bump; detailed current truth remains in CURRENT_BASELINE.md. Treat older reports as historical unless verified against `CURRENT_BASELINE.md`, `docs/DOCUMENTATION_INDEX.md`, and current source.

## Versions and baseline

| Module | Mod ID | Current built jar name |
|---|---|---|
| core | `herbcraft` | `herbcraft-1.1.4.jar` |
| alchemy | `herbcraft_alchemy` | `herbcraft-alchemy-1.1.4.jar` |
| cuisine | `herbcraft_cuisine` | `herbcraft-cuisine-1.1.3.jar` |

The current packaged line intentionally bumps Core and Alchemy to `1.1.4` while keeping Cuisine at `1.1.3`. The current output baseline is:

```text
release_output/herbcraft_test_20260614_review/
```

with:

- `herbcraft-1.1.4.jar`
- `herbcraft-alchemy-1.1.4.jar`
- `herbcraft-cuisine-1.1.3.jar`
- `herbcraft-source-current-20260614.zip`

`release_output/` is local/publishing-only and git-ignored. Use it for local jar/source-zip staging, then upload public jars via GitHub Releases rather than committing them.

## Locked environment

- Minecraft `26.1.2`
- Fabric Loader `0.19.3`
- Fabric API `0.151.0+26.1.2`
- Java `25`
- Gradle Wrapper `9.4.1`
- Fabric Loom `1.16-SNAPSHOT` / resolves as `1.16.3`
- Mojang official mappings

JDK helper/cache:

- `scripts/setup_jdk25_temurin.sh`
- `reference_cache/26_1_porting_notes.md`

## Current source state by module

### Core

- `data/herbcraft/herbs.json` drives 96 edible herb/raw-material definitions. It supports stable `nutrition` / `saturation_modifier` plus optional `food_rolls` for probabilistic direct visible hunger and direct hidden saturation settlement.
- P3.10 raw herb food rebalance is implemented: common scenery/flowers/leaves/saplings are no longer stable early food; seeds were strengthened after client feedback (`S+2` ordinary seeds, `S+3` melon/pumpkin seeds); sugar cane remains +1 visible hunger but now gives about +2 hidden saturation and Homeostatic hydration amount 3.
- `data/herbcraft/herb_natures.json` covers 96 nature profiles with `temperature`, `flavors`, `traits`, and `taste_flavors`.
- Raw-herb settlement runs after vanilla `finishUsingItem` and only for items present in `HerbFoodRegistry`.
- P3 startup-only external herb food definitions are supported for loaded Fabric mod/addon resources under `data/<namespace>/herbcraft/herbs/*.json`; these are scanned before default item components are registered and are not world datapack `/reload` edible-component changes.
- Herb stack thresholds/fade use private system messages; overload uses ActionBar plus private chat.
- Pharmacology/seven-emotions are wired into `EffectResolver`.
- Effect immunity exists and is persisted on player data.
- Last-meal death flavor message exists.
- Consume sound resolver exists.
- Herb Codex exists with multi-flag knowledge states (`encountered`, `heard`, `tasted`, `practiced`, `eatCount`), rumor claims, claim marks, errata verification, sync payloads, and client UI.
- `百草经总纲` is registered in `HerbalChapter` as the `overview` entry and is always mastered.
- Jade/tooltip information gating follows the five-state display model.
- Nutrition system is now a first-class custom-effect system, detailed below.

### Core nutrition system current state

Data/config:

- `minecraft:milk_bucket` provides gentle flesh/oil nutrition.
- `herbcraft_cuisine:water_bowl` now provides only trace `Fluids`/`津液` nutrition and keeps its 15% temporary dietary-imbalance relief behavior.

- `data/herbcraft/nutrition_profiles.json` includes vanilla foods, Herbcraft edibles, cuisine dishes, water bowl, and hodgepodge nutrition profiles.
- `data/herbcraft/nutrition_rules.json` defaults include:
  - `max_value = 1000`
  - `initial_value = 500`
  - `decay_interval_ticks = 1200`
  - `decay_amount = 2`
  - `check_interval_ticks = 1200`
  - `malnutrition_low = 150`
  - `imbalance_high = 850`
  - `full_balance_min = 400`
  - `full_balance_max = 600`

Custom effects:

| Effect ID | CN | Current mechanic |
|---|---|---|
| `herbcraft:grain_deficiency` | 谷气亏虚 | `BLOCK_BREAK_SPEED -15%` |
| `herbcraft:flesh_deficiency` | 血肉亏虚 | `ATTACK_DAMAGE -1` |
| `herbcraft:oil_deficiency` | 津脂不足 | `MOVEMENT_SPEED -8%` |
| `herbcraft:vege_deficiency` | 蔬素不足 | Every 4s: exhaustion `+0.08 * (amplifier+1)` |
| `herbcraft:fruit_deficiency` | 津液不足 | `MOVEMENT_SPEED -5%` |
| `herbcraft:dietary_imbalance` | 饮食失衡 | `MOVEMENT_SPEED -3%`; every 6s: exhaustion `+0.04 * (amplifier+1)` |
| `herbcraft:well_nourished` | 五养调和 | `ARMOR +3`, `KNOCKBACK_RESISTANCE +0.12` |

Behavior:

- Warning range (`150..349`) remains light/probabilistic and short.
- Danger range (`<150`) is deterministic and sustained for `rules.checkIntervalTicks + 200` ticks, minimum 400 ticks.
- Starved state (all five dimensions `<150`) deterministically applies all five deficiency effects for the sustained duration.
- Confirmed dietary imbalance remains grace-gated by `badStateStreak`, but the nutrition correction pass can now suppress/recover it when recent meals are structurally corrective.
- Nutrition correction/recent-meal pass is implemented: 24 recent meals, 3-minute correction scoring, score threshold 60, extra decay every 200 ticks by 15..45, target 650, and client status `correcting` / `饮食回正中`.
- High `fruit` / 津液 / Fluids is non-punitive in the base mod: it does not trigger high-side dietary imbalance punishment, but low 津液 deficiency remains meaningful and high 津液 can still block perfect `well_nourished` balance.
- Excess flesh/oil remains light/probabilistic/short to keep the main mod non-hardcore.
- Every nutrition evaluation clears stale Herbcraft nutrition effects first, then reapplies current-state effects. Debug commands, water relief, or eating corrective foods can remove/suppress old effects immediately.
- Water bowl keeps a 15% success chance; on success it clears vanilla nausea and temporarily relieves dietary imbalance for 60s without changing nutrition values.
- Cuisine/medicine clearing paths skip Herbcraft nutrition effects via `HerbcraftMobEffects.isNutritionEffect(...)`.
- Icons are generated from vanilla item textures by `scripts/generate_nutrition_effect_icons.py` and are intentionally more vanilla-recognizable than previous placeholders.
- Debug command exists: `/herbcraft nutrition get|set|preset ...`, requiring gamemaster permission. Presets include `correcting_flesh/oil/grain`, `fluid_surplus_pair/single/unrelated`, `excess_fruit_nonpunitive`, and all `_low`/`imbalanced_`/`starved`/`balanced` variants.
- Nutrition profiles are scaled at 0.6x for balanced pacing: `cooked_beef` gives `flesh=41` (was 69), requiring ~7 eats from baseline to reach correction threshold and ~9 to reach excess.
- `correction_high_threshold` is 750 (was 850). Matrix correction now activates at 75% instead of 85%, with graduated decay: 750-849 uses half-speed, 850+ uses full-speed. Punitive debuffs and fluid surplus still require 850+.
- Java fallback defaults in `NutritionRules` should match this threshold; validators now check the bundled-JSON fallback remains 750 rather than the old 850.
- Nutrition dimension bands now use `NutritionRules` fields (`balancedMin`, `balancedMax`, `imbalanceHigh`) rather than hardcoded 350/650/850 cutoffs; punitive imbalance spread uses `rules.imbalanceSpread` rather than a hardcoded 500.
- 食材录 (Ingredient Codex) chapter is registered in Core with sortPriority 50 (between herbal:0 and cuisine:100). 39 vanilla food entries in 6 sections (meat/seafood/grain/fruit/soup/special), data-driven from `ingredients.json`. Knowledge is gated by eating: pick up → ENCOUNTERED, eat → MASTERED. No pharmacology integration.
- `1.1.2` Codex guidance Phase 2.1/P3.7/P3.9 is implemented: guide entries under 百草经总纲 are data-backed by module-owned `codex_guides.json` files; Core owns only Core guide pages, Cuisine/Alchemy own their addon guide pages. 五味/七情 guide lines reveal principle details from stored player pharmacology discoveries rather than printing exact item nature in the guide. The Core guide pages now plainly explain that 性味 is pharmacology rather than nutrition, and 七情 is recent-herb interaction rather than a fixed recipe list. One-time onboarding hints are data-backed by module-owned `guide_hints.json` files and persist seen IDs on player data. Cuisine and Alchemy now also have their own module overview guide sections inside `膳房录` and `炼金要术`.
- Pharmacology qi/relation tuning is now more data-driven: `pharmacology_rules.json` owns `temperature_values` (`hot/cold` tuned to ±15, `warm/cool` ±8), faster qi regression (`600` ticks for 5), and ordered `seven_emotion_rules`. The previous exact hot/cold same-qi trigger on the 3rd bite is softened to roughly the 5th exact-hot/cold bite.

### Cuisine

- `DishRegistry` loads 10 fixed dishes and 22 medicinal/hidden dishes from `data/herbcraft_cuisine/dishes.json`.
- Medicinal dishes register raw items and use raw -> cooking routes.
- Water bowl exists and is acquired from water source or water cauldron.
- Water bowl can clear vanilla nausea and temporarily relieve dietary imbalance on a 15% chance.
- `DishItem.clear_all_effects` skips Herbcraft nutrition effects.
- `HodgepodgeRecipe` uses empty bowl + herbs, rejects water bowl, rejects vanilla red/brown mushrooms, and isolates fixed/hidden recipes.
- `HodgepodgeItem.clearPositiveEffects` skips Herbcraft nutrition effects.
- Hidden dishes exist in source/resources and built jar:
  - `hundred_flower_feast`
  - `raw_hundred_flower_feast`
  - `deadly_hodgepodge`
  - `raw_deadly_hodgepodge`
- Hidden dishes use water-bowl raw recipes and smelting/smoking/campfire cooking routes.

### Alchemy

- `AlchemyRegistries` registers 40 essence items and potion lines.
- Essence/brewing lines include base, clarified, murky, long, strong, and T3/special potions.
- High-tier potion components exist:
  - `potion_quality`
  - `potion_bonus`
  - `potion_source`
  - `advanced_potion_initialized`
- `AdvancedPotionInventoryHooks` initializes advanced potions through inventory scanning.
- `ItemCostAdvancedPotionMixin` allows randomized high-tier potion trades to match by potion path rather than exact random components.
- Coating system exists with `weapon_coating` component, smithing recipe, hit handler, and adhesive items.
- Witch safe Herbcraft potion throw mixin exists.
- Hidden cuisine catalyst brewing exists:
  - `BrewingStandIngredientSlotMixin` permits hidden cuisine catalysts in the ingredient slot.
  - `BrewingStandCatalystMixin` hooks `canPlaceItem`, `isBrewable`, and `doBrew`.
  - `HiddenCuisineCatalystBrewing` copies input stacks and sets only `POTION_QUALITY = EXCEPTIONAL` for clarified/murky target lines.


### Externalized rule/data systems in the 1.1.x baseline

The following formerly Java-hardcoded rule tables are now JSON-backed:

- P2 external server-data merge layer:
  - `data/<namespace>/herbcraft/herb_natures/*.json`
  - `data/<namespace>/herbcraft/nutrition_profiles/*.json`
  - `data/<namespace>/herbcraft/special_counters/*.json`
  - `data/<namespace>/herbcraft/knowledge_rumors/*.json`
  - `data/<namespace>/herbcraft/codex_loot_injections/*.json`
- P3 startup-only loaded-mod/addon herb food layer:
  - `data/<namespace>/herbcraft/herbs/*.json` (startup-only edible definitions; optional `food_rolls` supported)
- `data/herbcraft/special_counters.json` for poppy, torchflower, sugar/honey, and slime overuse counters.
- `data/herbcraft_cuisine/hodgepodge_rules.json` for hodgepodge ingredient counts, nutrition cap, effect chance bonus, stack gain, and clash formula values.
- `data/herbcraft_alchemy/essences.json` for the 40 current alchemy essence definitions, source items, potion names, and base effect specs; extra JSON essence definitions are registered dynamically but still need external recipes/assets/lang.
- `data/herbcraft_alchemy/potion_bonus_pools.json` for advanced potion bonus/aftertaste/impurity pools.
- `data/herbcraft_alchemy/special_potions.json` for T3/special potion definitions and the safe witch throw pool.
- `data/herbcraft_alchemy/witch_loot.json` for witch page/essence/potion loot pools.
- `data/herbcraft_alchemy/coating_rules.json` for weapon coating duration, use count, signature effects, and random pools.
- `data/herbcraft/nutrition_effects.json` for custom nutrition effect mechanics and timing.
- `data/herbcraft/nutrition_correction_rules.json` for dimension-specific correction relationship weights and self-penalty multiplier.
- `data/herbcraft/pharmacology_rules.json` for five-flavor multipliers, qi regression timing, temperature values, ordered seven-emotion relation rules/priority, and pharmacology balance constants.
- `data/herbcraft/ingredients.json` for the 食材录 (Ingredient Codex) chapter: 39 vanilla food entries with section assignments. Loaded by `IngredientsChapter` at startup.
- `data/herbcraft/codex_guides.json`, `data/herbcraft_cuisine/codex_guides.json`, and `data/herbcraft_alchemy/codex_guides.json` for module-owned Codex guide entries under 百草经总纲. Loaded by `CodexGuideRegistry`.
- `data/herbcraft/guide_hints.json`, `data/herbcraft_cuisine/guide_hints.json`, and `data/herbcraft_alchemy/guide_hints.json` for module-owned once-per-player onboarding hints. Loaded by `GuideHintManager`.
- `data/herbcraft/codex_loot_injections.json`, `data/herbcraft_alchemy/codex_loot_injections.json`, and `data/herbcraft_cuisine/codex_loot_injections.json` for Codex page loot injections.

`SpecialCounters`, `HodgepodgeRules`, `AlchemyRegistries`, and `AdvancedPotionStackInitializer` load these files at startup. `scripts/validate_externalized_rules.py` checks that these rule tables remain externalized and loaded.

## Current validation scripts

Important scripts now include:

- `scripts/check_lang_diff.py`
- `scripts/validate_custom_nutrition_effects_phase8.py`
- `scripts/validate_nutrition_cuisine_immunity_isolation.py`
- `scripts/validate_recipe_isolation.py`
- `scripts/validate_item_textures.py`
- `scripts/validate_task_abcd.py`
- `scripts/validate_extended_features.py`
- `scripts/validate_trade_tags.py`
- `scripts/verify_built_jars.py`
- `scripts/validate_external_extension_spec.py`
- `scripts/generate_nutrition_effect_icons.py`

`validate_custom_nutrition_effects_phase1.py` through `phase7.py` are compatibility wrappers to the current phase 8 validator.

`validate_animal_food_tags.py` now searches `/home/user/cache/minecraft/client-26.1.2.jar`, Loom's downloaded client/merged jars, or `HERBCRAFT_VANILLA_CLIENT_JAR`. Run a Gradle build first if no vanilla jar cache exists.

## Validation status from latest pass

Latest successful checks included:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks
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

A core server smoke reached `Done` after the nutrition/icon updates.

## Extension/generator docs

- `docs/EXTENSION_GENERATOR_PLAN.md` specifies the recommended generator/API rollout.
- `docs/EXTENDING_HERBCRAFT.md` documents the current data extension surface.
- `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md` tracks P1/P2/P3/P4 external extension status.
- `docs/examples/external_extension_compat_pack/` provides a P2 external datapack example.
- `docs/examples/startup_herb_food_extension_mod/` provides a P3 startup-only addon resource example.
- `docs/reports/EXTENSION_CONSISTENCY_REVIEW_20260615.md` records the latest consistency review and next-direction recommendation.
- `docs/examples/essence_extension_example/` provides a sample essence extension layout.

## Homeostatic compatibility

- Client testing showed negative Homeostatic drinkable `amount` can push water below zero internally and keep dehydration active after partial recovery, so Herbcraft compat now avoids negative amounts and uses strong thirst-effect risk for drying foods.
- Selected drink-like Cuisine dishes are marked `can_always_eat` so they can hydrate when hunger is full.
- Round-1 Homeostatic compatibility is implemented as optional Cuisine data under `data/herbcraft_cuisine/environment/drinkable/`.
- It covers 33 Herbcraft Cuisine custom edible outputs.
- Round-2 Homeostatic compatibility is implemented as optional Core data under `data/herbcraft/environment/drinkable/`.
- It covers selected Core/vanilla Herbcraft edible items, including ice/snow/water-adjacent positives, sugar cane (`amount 3 / saturation 0.5`), and sweet/sticky/hot/toxic thirst-risk entries.
- Homeostatic known generated vanilla drinkables are intentionally avoided to reduce duplicate/override risk.
- It intentionally excludes raw dishes, hodgepodge, and `minecraft:potion`.
- `scripts/validate_homeostatic_compat.py` validates this optional data.

## Known current risks / not fully trustworthy areas

- Real client UX still needs testing after recent polish:
  - stronger seed and sugar cane raw-food feel,
  - 五味/七情 Codex microcopy readability,
  - nutrition effect icons in the HUD/inventory,
  - `/herbcraft nutrition` command behavior in an actual client,
  - Codex UI and claim clicking,
  - Jade item/block overlays.
- Alchemy hidden cuisine catalyst brewing is source/build/server-smoke validated but still needs real client interaction verification in the brewing stand.
- Advancement JSON loads, but a dedicated reachability/pass criteria audit is still useful.
- `PlayerNutritionState.evaluate()` / `NutritionStatus` has been removed; `NutritionManager.evaluateAndApply()` is the authoritative runtime nutrition evaluation path.
- Nutrition effect icon art is improved but still generated. The author may request further visual tuning.

## Do not regress

- Do not reintroduce vanilla negative effects as NutritionManager's core malnutrition states.
- Do not allow cuisine `clear_all_effects` or raw/herb/hodgepodge `clearPositiveEffects` to remove Herbcraft nutrition effects.
- Do not turn main-mod nutrition into hard-core survival punishment; hard-core nutrition is planned as a future addon concept.
- Do not rename stable item/effect/component IDs without explicit author approval.
