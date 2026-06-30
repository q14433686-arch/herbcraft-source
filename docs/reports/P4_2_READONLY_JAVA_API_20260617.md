# P4.2 Read-only Java API — 2026-06-17

Status: implemented in source; built jars refreshed  
Baseline: Herbcraft `1.1.2` source line, `1.1.3` extension-planning milestone

## Goal

P4.2 implements the first stable Java-facing extension API without opening Java write/registration methods.

The rule remains:

```text
JSON owns content.
Java API first exposes safe queries.
Events come later in P4.3.
Write/registration APIs remain deferred.
```

## Implemented class

```text
core/src/main/java/com/herbcraft/api/HerbcraftDataAPI.java
```

This class is a read-only facade for code addons.

## Query surface

Supported query groups:

```text
item registry presence
nature profiles
nutrition profiles
Ingredient Codex / 食材录 entries
Herbcraft herb definitions
food-nature / 药食志 entries
current profile snapshots
```

Representative methods:

```java
boolean isRegisteredItem(Identifier itemId);
Optional<Identifier> itemId(Item item);

Optional<NatureProfile> nature(Identifier itemId);
Optional<NatureProfile> nature(Item item);
Optional<NatureProfile> nature(ItemStack stack);
boolean hasNature(...);

Optional<NutritionProfile> nutrition(Identifier itemId);
Optional<NutritionProfile> nutrition(Item item);
Optional<NutritionProfile> nutrition(ItemStack stack);
boolean hasNutrition(...);

boolean hasIngredientEntry(...);
Optional<String> ingredientEntryId(...);

boolean isHerb(...);
Optional<HerbDefinition> herb(...);

boolean hasFoodNatureEntry(...);
Optional<String> foodNatureEntryId(...);
boolean hasFoodNatureData(...);

Map<Identifier, NatureProfile> natureProfiles();
Map<Identifier, NutritionProfile> nutritionProfiles();
Map<Identifier, String> ingredientEntryIds();
```

## Important boundary

Identifier-based nature/nutrition queries read Herbcraft's loaded metadata tables. Optional bundled compat metadata can exist even when the target mod item is absent.

Therefore code addons that need a live runtime item should use either:

```java
HerbcraftDataAPI.isRegisteredItem(itemId)
```

or query by:

```java
Item
ItemStack
```

This is intentional. It lets tools inspect data by ID while still giving addons a safe way to avoid the old Air-placeholder mistake.

## What P4.2 does not do

P4.2 does not expose:

```text
registerNature(...)
registerNutrition(...)
registerIngredient(...)
registerHerbFood(...)
custom trait registration
custom nutrition dimensions
Codex chapter creation
FOOD/CONSUMABLE component mutation
```

Those are write/registration APIs and remain deferred.

## Documentation updated

Updated:

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

## Validator coverage

Updated:

```text
scripts/validate_external_extension_spec.py
scripts/verify_built_jars.py
```

`validate_external_extension_spec.py` now checks the `HerbcraftDataAPI` source for the read-only API tokens and rejects obvious write-registration method names.

`verify_built_jars.py` now checks that the Core jar contains:

```text
com/herbcraft/api/HerbcraftDataAPI.class
```

## Validation

Executed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/check_lang_diff.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```

Result: all passed.

## Next phase

P4.3 should implement public event APIs. Recommended first event surface:

```text
nutrition applied
food-nature resolved
external Herbcraft data reloaded
Herbcraft herb consumed, if the existing HerbConsumeEvents should be formalized
Ingredient Codex entry discovered, if needed
```

Keep events observational first. Do not let P4.3 mutate results unless explicitly designed and tested.
