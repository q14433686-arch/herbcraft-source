# P4 Extension Readiness Report for 1.1.3 — 2026-06-17

Status: **P4.1-P4.5 complete; source bumped to 1.1.3; final client sanity pending**

## 1. Scope

This report closes the five planned P4 extension rounds:

```text
P4.1 JSON spec formalization
P4.2 read-only Java API
P4.3 observational event API
P4.4 example addon/datapack
P4.5 validators and documentation final sweep
```

P4's goal is not to add a large Java write-registration surface. Its goal is to make Herbcraft's extension boundary stable:

```text
JSON provides content and balance.
Java provides stable queries, observations, mechanics, sync, UI, and validation.
```

## 2. P4.1 — JSON spec formalization

Status: **complete**

Formal spec:

```text
docs/JSON_EXTENSION_SPEC_1_1_3.md
```

The spec defines:

```text
reload-safe JSON paths
startup-only herbs path
schema_version convention
single-entry and entries-array forms
allowed temperature/flavor/trait/nutrition values
Ingredient Codex section keys
special counter schema
knowledge rumor schema
Codex loot injection schema
merge/override policy
client sync policy
bundled optional compat policy
explicit non-goals
```

Important fixed boundary:

```text
data/<namespace>/herbcraft/herbs/*.json is startup-only.
It can modify FOOD/CONSUMABLE components only from loaded mod/addon resources during startup.
It is not a world datapack /reload path.
```

Reload-safe paths remain:

```text
herb_natures
nutrition_profiles
ingredients
special_counters
knowledge_rumors
codex_loot_injections
```

## 3. P4.2 — Read-only Java API

Status: **complete**

Implemented API:

```text
core/src/main/java/com/herbcraft/api/HerbcraftDataAPI.java
```

Main query areas:

```text
registered item presence
nature profiles
nutrition profiles
Ingredient Codex entries
Herbcraft herb definitions
food-nature / 药食志 entries
current loaded profile snapshots
```

Important boundary:

```text
Identifier-based nature/nutrition queries may return loaded metadata for optional compat IDs even when the target item is absent.
If an addon needs a live item, it should call isRegisteredItem(...) or query by Item / ItemStack.
```

No write/registration API was added.

## 4. P4.3 — Observational event API

Status: **complete**

Implemented API:

```text
core/src/main/java/com/herbcraft/api/HerbcraftEvents.java
```

Events:

```text
HERB_CONSUMED
NUTRITION_APPLIED
INGREDIENT_DISCOVERED
FOOD_NATURE_RESOLVED
EXTERNAL_DATA_RELOADED
```

Legacy compatibility:

```text
HerbConsumeEvents remains, but delegates to HerbcraftEvents.HERB_CONSUMED.
```

Important boundary:

```text
Events are observational only.
They do not cancel or replace nutrition, pharmacology, knowledge, reload, or component results.
```

## 5. P4.4 — Examples

Status: **complete**

JSON datapack example:

```text
docs/examples/external_extension_compat_pack/
```

Demonstrates:

```text
herb_natures
nutrition_profiles
ingredients
knowledge_rumors
codex_loot_injections
special_counters
```

Java read/event addon example:

```text
docs/examples/herbcraft_readonly_event_addon/
```

Demonstrates:

```text
HerbcraftDataAPI.nature(...)
HerbcraftDataAPI.ingredientEntryId(...)
HerbcraftEvents.NUTRITION_APPLIED
HerbcraftEvents.INGREDIENT_DISCOVERED
HerbcraftEvents.FOOD_NATURE_RESOLVED
HerbcraftEvents.EXTERNAL_DATA_RELOADED
```

The Java example is currently documentation-only, not wired into the root Gradle build.

Decision for 1.1.3:

```text
A fully compilable standalone example addon is not required before 1.1.3 if the documented code sample and validators remain in sync.
```

Reason:

- A standalone Gradle example increases maintenance burden.
- The current source validates API names and example presence.
- P4's first release should prioritize stable docs/API shape over maintaining an additional build fixture.

Optional future improvement:

```text
Add a separate compilable sample addon after 1.1.3 if external addon authors request it.
```

## 6. P4.5 — Validator and documentation sweep

Status: **complete**

Validator coverage now includes:

```text
formal JSON spec presence and required tokens
JSON datapack example files and README tokens
startup herb food example schema
read-only Java API source tokens
observational event API source tokens and integration points
bundled Farmer+More optional compat counts/item sets/no-RMF/no-non-edible-seeds
built jar API/event classes through verify_built_jars.py
```

Updated validators:

```text
scripts/validate_external_extension_spec.py
scripts/verify_built_jars.py
```

Documentation status was synchronized across:

```text
docs/JSON_EXTENSION_SPEC_1_1_3.md
docs/PUBLIC_EXTENSION_API_DESIGN.md
docs/EXTENDING_HERBCRAFT.md
docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md
docs/DOCUMENTATION_INDEX.md
CURRENT_BASELINE.md
NEXT_TASK.md
CHANGELOG.md
```

## 7. Built jar coverage

`verify_built_jars.py` checks the Core jar for:

```text
com/herbcraft/api/HerbcraftDataAPI.class
com/herbcraft/api/HerbcraftEvents.class
com/herbcraft/api/HerbcraftEvents$NutritionApplied.class
com/herbcraft/api/HerbcraftEvents$FoodNatureResolved.class
com/herbcraft/api/HerbcraftEvents$ExternalDataReloaded.class
```

This confirms the public API/event classes are present in built outputs.

## 8. What P4 still intentionally does not expose

The following remain deferred after P4.5:

```text
Java write/registration APIs
registerNature(...)
registerNutrition(...)
registerIngredient(...)
registerHerbFood(...)
dynamic trait registration
dynamic nutrition dimensions
full external Codex chapter creation
hot-reloadable FOOD/CONSUMABLE component changes
JSON registration of brand-new Minecraft items
```

This is intentional. P4.1-P4.5 establish the stable read/event extension boundary first.

## 9. Required validation for release readiness

Executed in this P4.5 pass:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```

Recommended before tagging/uploading 1.1.3:

```bash
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

## 10. Remaining client tests before public 1.1.3 release

Minimum recommended client sanity pass:

```text
Core only
Core + Cuisine
Core + Alchemy
Core + Cuisine + Alchemy
```

Farmer+More built-in compat sanity:

```text
Refreshed Core jar + Farmer's Delight + More Delight, without standalone compat datapack.
Confirm 食材录/nutrition for Farmer/More foods.
Confirm cabbage/onion/tomato/rice nature rows appear in 药食志 after tasting.
Confirm onion/tomato can still participate in food-nature pharmacology.
Confirm cabbage_seeds, tomato_seeds, and raw rice do not appear as compat food entries.
Confirm target mod food behavior is not overwritten.
```

P4 API/event sanity, if a quick dev-client addon test is possible:

```text
Register listeners for NUTRITION_APPLIED, INGREDIENT_DISCOVERED, FOOD_NATURE_RESOLVED, and EXTERNAL_DATA_RELOADED.
Confirm logs fire when expected.
Confirm listeners cannot mutate Herbcraft results.
```

If no addon runtime test is done before 1.1.3, mark the Java example as documentation-only in release notes.

## 11. 1.1.3 release-readiness conclusion

P4.1-P4.5 are source/docs complete.

Herbcraft is now bumped to `1.1.3` and can be treated as an extension/API release candidate after:

```text
final full validator run
focused client sanity pass
release copy review/update
```

Suggested release theme:

```text
Herbcraft 1.1.3 — Extension API & Data Compatibility
```

Suggested short wording:

```text
This update formalizes Herbcraft's JSON extension spec, adds read-only Java APIs and observational events for addons, includes examples for datapack and code integrations, and bundles optional Farmer's Delight + More Delight compatibility data without hard dependencies.
```
