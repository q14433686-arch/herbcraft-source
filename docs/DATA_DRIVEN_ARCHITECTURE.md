# Herbcraft Data-Driven Architecture

This document is the current guide for future maintainers and AI agents. It explains which parts of Herbcraft are data-driven, which Java classes load them, and which parts must remain stable Java/API code.

## Core idea

Herbcraft should use this division of responsibility:

- **Java** owns mechanics, registration, synchronization, UI, event hooks, and validation logic.
- **JSON/data resources** own content, balance values, loot/trade/recipe data, and compatibility tables.
- **Scripts** check resource coverage and prevent regressions.

Do not move stable registry IDs, network payload IDs, data component IDs, or recipe serializer IDs into editable balance JSON files.

## Authoritative data files

### Core module

| System | Data file | Java loader / user | Notes |
|---|---|---|---|
| Raw edible items | `data/herbcraft/herbs.json`; startup addon path `data/<namespace>/herbcraft/herbs/*.json` | `HerbFoodRegistry`, `EffectResolver` | 96 built-in edible definitions; loaded Fabric addons can add startup-only edible definitions before default components are applied. Supports stable `nutrition` / `saturation_modifier` plus optional probabilistic `food_rolls` for direct visible hunger and hidden saturation. Not `/reload` hot-swappable. |
| Herb nature | `data/herbcraft/herb_natures.json`; external path `data/<namespace>/herbcraft/herb_natures/*.json` | `HerbNatureRegistry`, `HerbcraftExternalDataReloadListener` | Temperature, pharmacological flavors, taste flavors, traits. External path is server-data reload layer. |
| Knowledge rumors | `data/herbcraft/knowledge_rumors.json`; external path `data/<namespace>/herbcraft/knowledge_rumors/*.json` | `KnowledgeRumorRegistry`, `HerbcraftExternalDataReloadListener` | Codex claims, true/false/suspicious markers. |
| Nutrition profiles | `data/herbcraft/nutrition_profiles.json`; external path `data/<namespace>/herbcraft/nutrition_profiles/*.json` | `NutritionRegistry`, `HerbcraftExternalDataReloadListener` | Includes vanilla, Herbcraft, and Cuisine food profiles. Internal key `fruit` displays as `Fluids` / `津液`. |
| Nutrition rules | `data/herbcraft/nutrition_rules.json` | `NutritionRules` | Decay, thresholds, warning timing, correction parameters, fluid surplus parameters. |
| Nutrition correction matrix | `data/herbcraft/nutrition_correction_rules.json` | `NutritionRules` | Dimension-specific correction relationship weights and self-penalty multiplier. Used by the correction/recent-meal pass to determine how eating corrective foods accelerates decay of excessive dimensions. All `fruit` weights are `0.0` after 津液 decoupling. |
| Nutrition effects | `data/herbcraft/nutrition_effects.json` | `NutritionEffectRules`, `HerbcraftMobEffects`, `NutritionManager` | Custom effect attributes/timing. |
| Pharmacology rules | `data/herbcraft/pharmacology_rules.json` | `PharmacologyRules`, `PharmacologyResolver` | Five-flavor multipliers, qi regression timing, data-backed temperature values, and data-backed seven-emotion relation rules/priority. Loaded at startup with Java defaults as fallback. |
| Special counters | `data/herbcraft/special_counters.json`; external path `data/<namespace>/herbcraft/special_counters/*.json` | `SpecialCounters`, `HerbcraftExternalDataReloadListener` | Poppy, sugar/honey, slime, torchflower overuse rules plus external overuse counters. |
| Ingredients chapter | `data/herbcraft/ingredients.json`; external path `data/<namespace>/herbcraft/ingredients/*.json` | `IngredientsChapter`, `HerbcraftExternalDataReloadListener` | Vanilla/non-herb food entries for the 食材录 knowledge chapter. Defines items and section assignments. External entries use namespace-aware Codex IDs to avoid item-path collisions and are synced to clients for tooltip gating. |
| Codex guide entries | `data/herbcraft/codex_guides.json` | `CodexGuideRegistry`, `HerbalChapter` | Core-owned always-visible guide entries under 百草经总纲. Addon guide entries live in addon module files. |
| Guide hints | `data/herbcraft/guide_hints.json` | `GuideHintManager` | Core-owned once-per-player onboarding hints. Seen hint IDs persist on player data. |
| Codex loot injection | `data/herbcraft/codex_loot_injections.json`; external path `data/<namespace>/herbcraft/codex_loot_injections/*.json` | `CodexLootRules`, `CodexLoot`, `HerbcraftExternalDataReloadListener` | Core tattered page loot injections plus external additions. |
| Homeostatic compatibility | `data/herbcraft/environment/drinkable/*.json` | Homeostatic, validated by Herbcraft scripts | Optional data; Homeostatic reads it if installed. |

### Cuisine module

| System | Data file | Java loader / user | Notes |
|---|---|---|---|
| Dishes | `data/herbcraft_cuisine/dishes.json` | `DishRegistry` | Fixed, medicinal, and hidden dishes. Supports `can_always_eat`. |
| Hodgepodge rules | `data/herbcraft_cuisine/hodgepodge_rules.json` | `HodgepodgeRules`, `HodgepodgeItem`, `HodgepodgeRecipe` | Ingredient count, nutrition cap, clash formula, chance bonus. |
| Codex guide entries | `data/herbcraft_cuisine/codex_guides.json` | `CodexGuideRegistry`, `CuisineChapter` | Cuisine-owned guide entry under Core 百草经总纲; present only when Cuisine is installed. |
| Guide hints | `data/herbcraft_cuisine/guide_hints.json` | `GuideHintManager`, `WaterBowlItem`, `DishItem` | Cuisine-owned once-per-player onboarding hints. |
| Codex loot injection | `data/herbcraft_cuisine/codex_loot_injections.json` | `CodexLootRules`, `CuisineCodexHooks` | Cuisine page loot injections. |
| Homeostatic compatibility | `data/herbcraft_cuisine/environment/drinkable/*.json` | Homeostatic, validated by Herbcraft scripts | Optional drinkable data for Cuisine edible outputs. |

### Alchemy module

| System | Data file | Java loader / user | Notes |
|---|---|---|---|
| Essence definitions | `data/herbcraft_alchemy/essences.json` | `AlchemyRegistries` | Current 40 definitions; extra entries can register dynamically but need resources. |
| Potion bonus pools | `data/herbcraft_alchemy/potion_bonus_pools.json` | `AdvancedPotionStackInitializer` | High-tier potion aftertaste/impurity bonus pools. |
| Special/T3/witch potions | `data/herbcraft_alchemy/special_potions.json` | `AlchemyRegistries`, `WitchHerbcraftPotionMixin` | T3 potions, safe witch potions, throw weights. |
| Coating rules | `data/herbcraft_alchemy/coating_rules.json` | `CoatingRules`, `CoatingSystem` | Duration, use counts, random pools, signatures. |
| Witch loot | `data/herbcraft_alchemy/witch_loot.json` | `AlchemyCodexHooks` | Witch page/essence/potion loot. |
| Codex guide entries | `data/herbcraft_alchemy/codex_guides.json` | `CodexGuideRegistry`, `AlchemyChapter` | Alchemy-owned guide entry under Core 百草经总纲; present only when Alchemy is installed. |
| Guide hints | `data/herbcraft_alchemy/guide_hints.json` | `GuideHintManager`, `AlchemyAcquisitionHooks` | Alchemy-owned once-per-player onboarding hints. |
| Codex loot injection | `data/herbcraft_alchemy/codex_loot_injections.json` | `CodexLootRules`, `AlchemyCodexHooks` | Alchemy page loot injections. |

## Stable Java/API areas that should not become arbitrary JSON

Do not data-drive these unless there is a very specific migration plan:

| Area | Why |
|---|---|
| Data component IDs | Stored in item/player data and must remain stable. |
| Network payload IDs | Client/server protocol stability. |
| Recipe serializer IDs | Recipe JSON and registry stability. |
| Mixin target signatures | Version-specific Java integration, not balance data. |
| Built-in item registry IDs | Commands, recipes, trades, saves, and addons may depend on them. |
| UI pixel constants | Strongly coupled to rendering, scrolling, mouse hitboxes. |

Examples of stable code-level IDs:

- `herbcraft_alchemy:potion_quality`
- `herbcraft_alchemy:potion_bonus`
- `herbcraft:weapon_coating`
- `herbcraft_cuisine:hodgepodge_contents`
- `herbcraft:codex_snapshot`
- `herbcraft:knowledge_sync`

## Current extension/generator tools

| Script | Purpose |
|---|---|
| `scripts/generate_essence_resources.py --check` | Check built-in essence resources against `essences.json`. |
| `scripts/generate_essence_resources.py --write-missing` | Create missing built-in essence skeleton files without overwriting existing files. |
| `scripts/generate_essence_resources.py --scaffold-extension ...` | Generate a one-essence extension scaffold. |
| `scripts/generate_essence_resources.py --check-extension <path>` | Validate a standalone essence extension scaffold/resource directory. |
| `scripts/validate_essence_resources.py` | Wrapper around essence resource check. |
| `scripts/validate_externalized_rules.py` | Ensures externalized rule tables are present and actually used. |
| `scripts/validate_homeostatic_compat.py` | Validates optional Homeostatic drinkable compatibility data. |
| `scripts/validate_external_extension_spec.py` | Validates the current P2/P3 external extension example schemas. |
| `scripts/validate_raw_herb_food_balance.py` | Guards raw Herbcraft edible food-roll balance so common forage does not regain stable visible hunger. |
| `scripts/generate_compat_pack.py` | Generates draft Herbcraft data-compat packs from CSV tables for nutrition profiles, ingredient entries, optional natures, and rumor skeletons. |
| `scripts/generate_nutrition_effect_icons.py` | Regenerates vanilla-derived nutrition effect icons. |

## Data-driven does not mean behavior-free

A JSON entry does not magically implement a new mechanic. For example:

- `essences.json` can register a new essence item/potion, but resources, recipes, lang, and optional trade/Codex data still need to exist.
- `environment/drinkable/*.json` is only used if Homeostatic is installed.
- `nutrition_effects.json` configures attributes/exhaustion, but Java still owns MobEffect registration and tick behavior.
- Server-authoritative nutrition profiles, nature profiles, and 食材录 ingredient entry IDs are synced to the client so multiplayer/datapack-provided data can appear in client tooltips and Codex-related display.
- `data/<namespace>/herbcraft/herbs/*.json` can make an existing item Herbcraft-edible only when packaged in a loaded Fabric mod/addon and scanned at startup. It can define optional `food_rolls`, but it is not a world datapack hot-reload mechanism for default item components.

## Adding new data safely

1. Add or edit the relevant JSON.
2. Add required assets/lang/recipes if the JSON creates visible items.
3. Run the specific validator for that system.
4. Run the general release validators listed in `CURRENT_BASELINE.md`.
5. Update `CURRENT_BASELINE.md` if the change is committed.

## Known limitation

The project supports dynamic extra alchemy essence registration from JSON, but it does not yet automatically generate a complete polished content pack for those entries. Use the essence generator/scaffold tools and review generated output manually.
