# Herbcraft Public Extension API Design Draft

Status: P4 design draft; **P4.1-P4.5 completed for read/event extension boundary; write APIs not implemented yet**
Target: Core/Alchemy `1.1.4` extension/API line; Cuisine remains `1.1.3`
Minecraft: `26.2` (port in validation)
Java: `25+`

P4.1 output: `docs/JSON_EXTENSION_SPEC_1_1_3.md`.
P4.2 output: `com.herbcraft.api.HerbcraftDataAPI`.
P4.3 output: `com.herbcraft.api.HerbcraftEvents`.
P4.4 outputs: `docs/examples/external_extension_compat_pack/` and `docs/examples/herbcraft_readonly_event_addon/`.
P4.5 output: `docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md`.

## 1. Purpose

Herbcraft already supports several data-driven extension paths. Before exposing a stable Java API, the project should preserve the current boundary:

```text
Java owns mechanics, registration timing, network sync, UI, and validation.
JSON owns content, balance values, compatibility tables, guide/hint entries, and most rules.
```

The public API should exist for code addons that cannot reasonably express their integration with JSON alone. It should not become an excuse to hardcode compatibility tables in Java.

## 2. Current data extension surfaces

Implemented today:

```text
data/<namespace>/herbcraft/herb_natures/*.json
data/<namespace>/herbcraft/nutrition_profiles/*.json
data/<namespace>/herbcraft/ingredients/*.json
data/<namespace>/herbcraft/special_counters/*.json
data/<namespace>/herbcraft/knowledge_rumors/*.json
data/<namespace>/herbcraft/codex_loot_injections/*.json
```

Startup-only loaded-mod/addon path:

```text
data/<namespace>/herbcraft/herbs/*.json
```

The `herbs` path can add or override default FOOD/CONSUMABLE components only during mod startup. It is not a world datapack `/reload` feature.

## 3. API principles

1. **Data first.** If JSON can express the integration, use JSON.
2. **No new item registration through Herbcraft JSON/API.** Other mods register their own items.
3. **Separate reload-safe data from startup-only edible components.**
4. **Log duplicate/override behavior.** No silent conflict resolution.
5. **Expose read APIs before write APIs.** Compatibility mods often only need to query Herbcraft state.
6. **Keep client/server authority clear.** Server owns gameplay data; client receives synced display data.
7. **Do not promise hot-reloadable item default component changes.**

## 4. P4.2 read-only API

Implemented class:

```text
com.herbcraft.api.HerbcraftDataAPI
```

Current read-only query surface:

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

Boundary:

- read-only only;
- no Java registration/write methods;
- Identifier-based nature/nutrition queries may return loaded metadata for optional absent mods, so addons that need live items should also check `isRegisteredItem(...)` or query by `Item` / `ItemStack`;
- content should still be provided through JSON where possible.

Read-only APIs are stable sooner because they do not modify registration timing.

## 5. P4.3 observational event API

Implemented class:

```text
com.herbcraft.api.HerbcraftEvents
```

Current events:

```java
HerbcraftEvents.HERB_CONSUMED
HerbcraftEvents.NUTRITION_APPLIED
HerbcraftEvents.INGREDIENT_DISCOVERED
HerbcraftEvents.FOOD_NATURE_RESOLVED
HerbcraftEvents.EXTERNAL_DATA_RELOADED
```

Boundary:

- observational only;
- listeners cannot mutate Herbcraft's computed nutrition/pharmacology/reload results;
- server gameplay remains authoritative;
- use `HerbcraftDataAPI` for read queries inside listeners;
- provide content through JSON rather than event-time writes.

Legacy note: `HerbConsumeEvents` remains as a compatibility shim and delegates to `HerbcraftEvents.HERB_CONSUMED`.

## 6. Proposed future write registration APIs

Only add after source/client tests prove the timing is safe.

### Reload-safe registrations

Possible event:

```java
HerbcraftExtensionEvents.REGISTER_RELOAD_SAFE_DATA
```

Possible methods available during that event:

```java
registerNature(ItemLike item, NatureProfile profile);
registerNutrition(ItemLike item, NutritionProfile profile);
registerIngredientEntry(ItemLike item, String section);
registerSpecialCounter(SpecialCounterDefinition counter);
registerKnowledgeRumor(String entryKey, KnowledgeClaim claim);
```

These should mirror the external JSON paths and follow the same conflict rules.

### Startup-only herb food registration

Possible event:

```java
HerbcraftExtensionEvents.REGISTER_STARTUP_HERB_FOODS
```

Possible method:

```java
registerHerbFood(ItemLike item, HerbFoodDefinition definition);
```

Restrictions:

- Must run before Herbcraft applies default item components.
- Should not run during `/reload`.
- If missed, use JSON in a loaded addon jar next restart.

## 6. Suggested Java definitions

Draft shapes only:

```java
record HerbFoodDefinition(
    int nutrition,
    float saturationModifier,
    Optional<FoodRoll> nutritionRoll,
    Optional<FoodRoll> saturationRoll,
    float consumeSeconds,
    boolean medicinal,
    boolean stackGroup,
    List<HerbEffectDefinition> effects,
    List<Identifier> removeEffects,
    Set<String> flags,
    Optional<String> counter
) {}

record FoodRoll(float amount, float chance) {}

record IngredientEntryDefinition(ItemLike item, String section) {}
```

Important: these should be views/definitions, not direct mutable access to internal registries.

## 7. Conflict policy

For item-keyed data:

```text
Built-in bundled data < external data pack/addon data < explicit Java API event registration
```

On duplicate item IDs:

- replacement is allowed only at defined merge phases;
- log a warning with old source and new source;
- avoid nondeterministic ordering.

## 8. Client sync policy

As of Core/Alchemy `1.1.4`, Herbcraft syncs server-authoritative nutrition profiles, nature profiles, and ingredient entry IDs to the client for tooltip/Codex display. Future API registrations must update the same sync surface.

Client display should never assume that local bundled resources are the complete data set on a multiplayer server.

## 9. What should stay JSON-only for now

Do not rush Java APIs for:

```text
full Codex chapter creation
arbitrary guide page creation outside module ownership
recipe serializer registration
new item/effect/component registration
UI layout constants
network payload IDs
```

Those are either high-risk or already better handled by normal Minecraft/Fabric data/registry systems.

## 10. Example addon strategy

For Farmer's Delight compatibility, prefer a data pack/addon resource pack first:

```text
data/farmersdelight_herbcraft/herbcraft/nutrition_profiles/generated.json
data/farmersdelight_herbcraft/herbcraft/ingredients/generated.json
data/farmersdelight_herbcraft/herbcraft/herb_natures/herbs.json
```

Only write Java if the target mod exposes dynamic behavior that cannot be represented by static JSON.

## 11. Release requirement before implementation

P4.1 is complete: the formal JSON spec is now `docs/JSON_EXTENSION_SPEC_1_1_3.md`.
P4.2 is complete: the read-only Java facade is `com.herbcraft.api.HerbcraftDataAPI`.

P4.3 is complete: observational events are available through `com.herbcraft.api.HerbcraftEvents`.
P4.4 is complete: examples are available under `docs/examples/external_extension_compat_pack/` and `docs/examples/herbcraft_readonly_event_addon/`.
P4.5 is complete: extension readiness is summarized in `docs/reports/P4_EXTENSION_READINESS_1_1_3_20260617.md`.

Before public 1.1.4 Core/Alchemy release:

1. Keep Java write/registration APIs out of scope for now.
2. Run the final full validator suite.
3. Test Core only, Core+Cuisine, Core+Alchemy, and all three modules.
4. Sanity-test bundled Farmer+More compat with refreshed Core jar and no standalone compat datapack.
5. Keep `docs/JSON_EXTENSION_SPEC_1_1_3.md`, `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md`, and `docs/EXTENDING_HERBCRAFT.md` in sync if any release copy changes.
