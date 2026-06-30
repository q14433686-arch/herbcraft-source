# Alchemy potion processing foundation P1-P3 — 2026-06-29

## Scope

Implemented the first round of the Alchemy expansion plan, covering P1-P3 only:

```text
P1 — data-backed potion processing style rules
P2 — Java loader/query/facade mapping current potion paths to style IDs
P3 — tooltip and acquisition routing projection
```

This round intentionally does **not** implement P4 runtime effect rewriting and does **not** implement P5-P7 potion burden runtime. It is a mechanism-foundation pass that preserves current stable potion IDs and current potion behavior.

## What changed

### Added data-backed processing style rules

Added:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json
```

The file defines current first-batch planned styles:

```text
_long      -> sustained
_strong    -> intense
_clarified -> purified
```

It also preserves deferred-but-confirmed future branches:

```text
_murky and current risky special potions -> murky_edge
harmonized -> reserved/deferred style with no current potion path
```

All styles currently have `implemented: false` because P4 runtime behavior differentiation has not been implemented yet. This is deliberate and prevents the source from claiming runtime behavior that does not exist.

### Added Java loader/query layer

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java
```

Responsibilities:

- load bundled `potion_processing_rules.json`;
- register processing style metadata;
- resolve aliases;
- map exact potion paths and suffixes to style IDs;
- expose read-only helpers:
  - `style(...)`
  - `styles()`
  - `processingStyleId(...)`
  - `isImplemented(...)`
  - `isDeferred(...)`

Alchemy initialization now loads these rules during startup.

### Added facade on current potion path logic

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java
```

New helpers:

```java
potionPath(ItemStack)
potionPath(PotionContents)
processingStyleId(ItemStack)
processingStyleId(PotionContents)
processingStyleId(String)
```

Important boundary:

```text
_long and _strong are mapped to style IDs but are not converted into initialized advanced potions in P1-P3.
```

This keeps first-round behavior stable and avoids adding random quality/bonus processing to long/strong potions before the runtime design is implemented.

### Added tooltip projection

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java
alchemy/src/main/resources/assets/herbcraft/lang/en_us.json
alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json
```

Tooltips can now show:

- processing style name;
- burden tendency hint from style metadata.

This is projection only. There is no potion burden component or player burden runtime yet.

### Added style-aware acquisition routing

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java
```

Clarified/murky advancement routing now consults style IDs:

```text
purified   -> first_clarified
murky_edge -> first_murky
```

The effective behavior is intended to remain equivalent for current potions, while removing raw suffix-only dependency from this route.

### Added validator and jar verification

Added:

```text
scripts/validate_potion_processing_rules.py
```

Updated:

```text
scripts/verify_built_jars.py
```

The validator guards:

- style JSON presence and schema;
- first-batch and deferred style rows;
- suffix/exact-path mappings;
- `implemented: false` boundary for P1-P3;
- loader/facade/tooltips/acquisition tokens;
- no-runtime-change boundary for `_long` / `_strong` advanced initialization;
- language keys;
- built jar resource/class inclusion when a jar exists.

## What did not change

This round intentionally did not add:

- `PotionProcessingRuntime`;
- actual `sustained` / `intense` / `purified` effect rewriting;
- `PotionBurdenComponent`;
- player-level potion burden state;
- burden decay;
- burden threshold consequences;
- distillation coupling;
- armor-side support;
- advanced weapon-coating branches.

Current potion registry IDs and brewing paths remain unchanged.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

Validators run this round: the full validator suite listed in `CURRENT_BASELINE.md`, now including `python3 scripts/validate_potion_processing_rules.py`.

Result: all PASS.

## Next round

Recommended next round is P4 only:

```text
Implement PotionProcessingRuntime for sustained / intense / purified, while avoiding double-scaling because _long and _strong already have registry-time transformations.
```

The P4 validator should specifically guard:

- no stable potion ID renames;
- no unbounded duration/amplifier double-scaling;
- first-batch styles become `implemented: true` only when runtime behavior exists;
- deferred `murky_edge` / `harmonized` remain visible but not active as first-batch runtime styles.

## Client-only checks

A real client should later confirm:

- style tooltip lines display for current mapped potions;
- long/strong potions have style projection but no unexpected quality/bonus behavior from this P1-P3 pass;
- clarified/murky advancement behavior is unchanged;
- JEI/REI weapon coating visibility remains unchanged.
