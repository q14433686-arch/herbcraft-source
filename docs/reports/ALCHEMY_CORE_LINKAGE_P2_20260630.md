# Alchemy/Core Linkage P2 2026-06-30

## Scope

This turn completed **Round 2 / P2** on the preferred four-round Alchemy/Core linkage track from `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md`.

P2 target:

- preserve source essence identity after processing;
- expose source pharmacology in a runtime-readable/queryable way;
- support, at minimum, source temperature, flavors/taste flavors, and traits;
- ensure processed outputs do not collapse into style-only behavior.

## Design decision

P2 was implemented as a **stack-level persisted pharmacology projection layer** for processed potions.

Why this route:

- it follows the architecture rule that Java owns runtime state, sync, persistence, and tooltip/query projection;
- it keeps actual nature data Core-owned through `HerbNatureAPI` / `NatureProfile`;
- it avoids hardcoding per-source tables inside Alchemy;
- it creates a stable bridge for later P3/P4 work without prematurely forcing new gameplay penalties or full body-state integration.

## Implemented this turn

### 1. New persisted projection component

Added:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionPharmacologyProjection.java`

This component stores:

- `source_essence`
- `temperature`
- `flavors`
- `taste_flavors`
- `traits`

It is both persistent and network-synced, so the projection is available for runtime logic, tooltips, and future integration.

### 2. New component registration

Updated:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java`

Added a new registered data component:

- `herbcraft_alchemy:potion_pharmacology`

### 3. Runtime projection builder

Added:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionPharmacologyProjectionRuntime.java`

Behavior:

- reads `POTION_SOURCE` from the processed potion stack;
- resolves the source herb nature through Core `HerbNatureAPI.getNatureOrFallback(...)`;
- converts the Core `NatureProfile` into a persisted `PotionPharmacologyProjection`;
- stores it on the item stack;
- provides a read helper so later systems can safely query it.

### 4. Automatic attachment during potion initialization

Updated:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java`

Now, when processed potion metadata is initialized, the source pharmacology projection is also attached automatically.

This means different source essences under the same style now preserve distinct, queryable identity on the potion stack.

### 5. Read-only API exposure

Updated:

- `alchemy/src/main/java/com/herbcraft/alchemy/AlchemyProcessingAPI.java`

Added read-only query:

- `potionPharmacology(ItemStack stack)`

This gives future runtime systems and external read-only consumers a narrow API surface for source pharmacology projection.

### 6. Tooltip projection

Updated:

- `alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java`

Processed potion tooltips now also show source-side pharmacology projection:

- source herb ID
- source temperature
- source taste flavors
- source trait hints

This satisfies the P2 requirement that different source essences under the same style remain visibly different, not only internally stored.

### 7. Language keys

Added zh/en keys:

- `tooltip.herbcraft.potion.source_essence`
- `tooltip.herbcraft.potion.source_temperature`
- `tooltip.herbcraft.potion.source_taste`
- `tooltip.herbcraft.potion.source_traits`

## Validation additions

Added validator:

- `scripts/validate_potion_source_projection.py`

It checks:

- component registration;
- projection record structure;
- runtime metadata attachment;
- API exposure;
- tooltip projection;
- language keys;
- jar presence of the new classes.

Also updated:

- `scripts/verify_built_jars.py`

## Validation executed

```bash
chmod +x gradlew
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/validate_potion_processing_rules.py
python3 scripts/validate_potion_source_projection.py
python3 scripts/check_lang_diff.py
python3 scripts/verify_built_jars.py
```

Results:

- Gradle build — PASS (`BUILD SUCCESSFUL`)
- `validate_potion_processing_rules.py` — PASS
- `validate_potion_source_projection.py` — PASS
- `check_lang_diff.py` — PASS
- `verify_built_jars.py` — PASS

## Release output refresh

Refreshed local output under:

```text
release_output/herbcraft_test_20260614_review/
```

Refreshed files:

- `herbcraft-1.1.3.jar`
- `herbcraft-alchemy-1.1.3.jar`
- `herbcraft-cuisine-1.1.3.jar`
- `herbcraft-source-current-20260614.zip`
- `herbcraft-p4-runtime-test-datapack-20260617.zip`

## Boundaries kept this turn

This turn did **not** yet make processed potions enter Core body-state or recent-intake pharmacology runtime.

That remains for later stages:

- P3 — body-state / medicinal accumulation integration
- P4 — seven-relation integration

So P2 is still conservative: it preserves and exposes source identity first, without yet changing deeper gameplay integration.

## Next step

The next planned stage is **P3: processed potion body-state / medicinal accumulation integration layer**.
