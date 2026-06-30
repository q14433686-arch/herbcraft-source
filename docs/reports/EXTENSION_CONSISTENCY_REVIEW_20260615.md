# Extension Consistency Review and Next Direction

Date: 2026-06-15  
Scope: P1/P2/P3/P4 external extension roadmap after P2/P3 implementation and smoke tests.

## Reviewed sources

Runtime source reviewed:

```text
core/src/main/java/com/herbcraft/Herbcraft.java
core/src/main/java/com/herbcraft/registry/HerbFoodRegistry.java
core/src/main/java/com/herbcraft/data/ExternalDataResources.java
core/src/main/java/com/herbcraft/data/HerbcraftExternalDataReloadListener.java
core/src/main/java/com/herbcraft/nature/HerbNatureRegistry.java
core/src/main/java/com/herbcraft/nutrition/NutritionRegistry.java
core/src/main/java/com/herbcraft/logic/SpecialCounters.java
core/src/main/java/com/herbcraft/knowledge/KnowledgeRumorRegistry.java
core/src/main/java/com/herbcraft/knowledge/CodexLootRules.java
core/src/main/java/com/herbcraft/knowledge/CodexLoot.java
```

Docs/reports reviewed:

```text
CURRENT_BASELINE.md
docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md
docs/EXTENDING_HERBCRAFT.md
docs/DATA_DRIVEN_ARCHITECTURE.md
docs/HOMEOSTATIC_TEMPERATURE_COMPAT_NOTES.md
docs/reports/P2_EXTERNAL_DATAPACK_SMOKE_20260615.md
docs/reports/P3_STARTUP_HERB_FOOD_SMOKE_20260615.md
```

## Consistency result

Result: **PASS with documented boundaries**

The current source behavior matches the intended split:

```text
P2 = reloadable/server-data external merge layer for low-risk metadata.
P3 = startup-only loaded-mod/addon resource scan for edible herb food definitions.
P4 = not implemented yet.
```

## Confirmed behavior

### P2 confirmed

P2 paths are loaded through `HerbcraftExternalDataReloadListener` and `ExternalDataResources`:

```text
data/<namespace>/herbcraft/herb_natures/*.json
data/<namespace>/herbcraft/nutrition_profiles/*.json
data/<namespace>/herbcraft/special_counters/*.json
data/<namespace>/herbcraft/knowledge_rumors/*.json
data/<namespace>/herbcraft/codex_loot_injections/*.json
```

The smoke report confirms a temporary world datapack merged these paths and the dedicated server reached `Done`.

### P3 confirmed

P3 path is loaded by `HerbFoodRegistry.loadExternalStartupResources()` from loaded Fabric mod containers:

```text
data/<namespace>/herbcraft/herbs/*.json
```

The load order is correct for default item component registration:

```text
HerbFoodRegistry.loadFromBundledJson()
HerbFoodRegistry.loadExternalStartupResources()
DefaultItemComponentEvents.MODIFY.register(HerbFoodRegistry::registerDefaultComponents)
```

The smoke report confirms a temporary external mod added one new external edible definition, increasing edible definitions from 96 to 97, and the dedicated server reached `Done`.

### P4 confirmed not implemented

There is no stable public Java event/API contract for external extension registration yet. Existing limited public API remains separate and should not be presented as the P4 extension API.

## Boundaries that must stay explicit

1. P3 is **not** a world datapack `/reload` feature.
2. P3 does not register brand-new Minecraft items; the item must already exist in the registry.
3. P3 adds Herbcraft food/default component behavior, but external addons should also provide their own assets/lang/recipes/tags as needed.
4. If an external P3 item should be accepted by Herbcraft tag-based systems such as hodgepodge ingredients, the addon must also provide appropriate item tags. P3 does not automatically edit tags.
5. P2 server-data nature values are authoritative for server-side Codex/Jade flows; fully synchronized client-only local tooltip display for server datapack-provided values remains a later UI/sync task.
6. Homeostatic temperature compatibility remains research-only; no temperature gameplay integration has been added.

## Stale documentation found and updated this turn

The previous `EXTERNAL_EXTENSION_SPEC_DRAFT.md` header still said startup-only herb edible extension was not implemented. It has been corrected.

`HOMEOSTATIC_TEMPERATURE_COMPAT_NOTES.md` still said P3 should be deferred. It has been updated to state P3 is now implemented as startup-only loaded-mod/addon resources.

`EXTENDING_HERBCRAFT.md`, `DATA_DRIVEN_ARCHITECTURE.md`, `PROJECT_STATE.md`, `NEXT_TASK.md`, and `README.md` were updated so the extension surface is consistent across maintainer-facing docs.

## Recommended next direction

### Next round option A: P3 hardening and client sanity pass

Recommended as the next implementation/validation round.

Tasks:

```text
1. Run a real client with a small external P3 addon jar.
2. Confirm the external item is actually edible in-game.
3. Confirm the external item receives FOOD/CONSUMABLE components.
4. Confirm Codex entry creation/knowledge acquisition for the external herb.
5. Confirm Jade/tooltip behavior for external P3 + P2 nature data.
6. Confirm no accidental support claim for world datapack /reload edible registration.
7. Add a validator or report specifically for external herb food examples if needed.
```

### Next round option B: P4 API design only, not immediate coding

Before implementing P4 Java API, decide:

```text
- exact event names;
- call timing;
- duplicate/override rules;
- client/server side expectations;
- whether API data is reload-aware or startup-only;
- how P4 relates to P2/P3 JSON.
```

Recommended first P4 deliverable:

```text
docs/PUBLIC_EXTENSION_API_DESIGN.md
```

### Next round option C: P2/P3 example polish

Useful if the goal is publishing addon guidance:

```text
- make a minimal external addon template zip/jar example;
- split validators by system;
- add a quick-start guide for compat-pack authors;
- add tags guidance for hodgepodge/cuisine interaction.
```

## Recommendation

Do **P3 hardening + real client sanity** before P4 Java API coding.

Reason:

```text
P4 is a public compatibility contract. It should be based on observed P2/P3 behavior in a real client, not only server smoke logs.
```
