# Alchemy potion processing runtime P4 — 2026-06-29

## Scope

Implemented Round 2 / P4 of the four-round Alchemy expansion task.

P4 goal:

```text
Add the first runtime behavior layer for sustained / intense / purified potion-processing styles.
```

This round builds on the P1-P3 style foundation and intentionally does not implement P5-P7 potion burden metadata/player burden/threshold consequences.

## What changed

### Runtime profiles added to JSON

Updated:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json
```

The first-batch styles are now marked `implemented: true` and carry runtime profiles:

```text
sustained:
  duration_multiplier = 1.25
  amplifier_delta = -1
  max_amplifier = 1

intense:
  duration_multiplier = 0.75
  amplifier_delta = +1
  max_amplifier = 2

purified:
  duration_multiplier = 1.10
  amplifier_delta = 0
  max_amplifier = 1
  force_non_ambient = true
  force_visible = true
  force_icon = true
```

Deferred branches remain deferred and not runtime-implemented:

```text
murky_edge: implemented=false, deferred=true
harmonized: implemented=false, deferred=true
```

### Runtime loader support

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java
```

Added `RuntimeProfile` parsing/query support for JSON-owned runtime values.

### Runtime application layer

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRuntime.java
```

Responsibilities:

- only applies styles that are `implemented=true` and `deferred=false`;
- transforms effect duration/amplifier/visibility from data-backed runtime profile;
- caps non-instant effect duration at 9600 ticks;
- writes rewritten effects into `PotionContents` custom effects while preserving the original potion path as custom name;
- resets `POTION_DURATION_SCALE` to `1.0F` after rewriting;
- marks the stack with the applied processing style.

### Processing-style stack component

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java
```

Added persistent/network-synced component:

```text
herbcraft_alchemy:potion_processing_style
```

This prevents repeated runtime rewriting and allows already-initialized advanced potion stacks to receive the new style runtime once if they were created before this component existed.

### Initializer wiring

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java
```

Behavior after P4:

- existing advanced potion paths (`_clarified`, `_murky`, risky special potions) still use the existing quality/bonus initialization path;
- implemented processing styles apply `PotionProcessingRuntime` once per stack/style;
- `_long` and `_strong` now receive sustained/intense runtime processing through the style path;
- `_long` and `_strong` still do **not** enter the legacy advanced quality/bonus classification, avoiding unexpected random bonus/quality behavior;
- current stable potion registry IDs are unchanged.

## Double-scaling boundary

The current registry already creates `_long` and `_strong` potion paths with basic duration/amplifier transformations. P4 does not erase that existing baseline. Instead, it applies a bounded second-stage style adjustment:

- sustained adds only a controlled duration lift and reduces peak where possible;
- intense adds a bounded amplifier increase and shortens duration;
- purified adds a light stable duration lift and clean visible projection.

Validator coverage guards that `_long` / `_strong` do not enter the legacy quality/bonus advanced classification.

## What did not change

This round did not add:

- `PotionBurdenComponent`;
- item-level burden metadata;
- player-level potion burden storage;
- burden decay;
- burden threshold consequences;
- distillation routing;
- armor support;
- advanced weapon coating branches.

Those belong to later rounds.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

New/updated validators:

```bash
python3 scripts/validate_potion_processing_rules.py
python3 scripts/validate_potion_processing_runtime.py
python3 scripts/verify_built_jars.py
```

Result: PASS.

The full project validator suite was also run after this change and passed.

## Client-only checks

A real client should confirm:

1. `_long` potions show sustained style and receive the intended bounded runtime profile;
2. `_strong` potions show intense style and receive the intended bounded runtime profile;
3. `_clarified` potions show purified style and receive the clean/stable runtime profile;
4. `_long` / `_strong` do not unexpectedly roll quality or bonus effects;
5. existing `_murky` / risky special potion behavior remains unchanged except style metadata projection;
6. existing brewing and potion IDs remain stable.

## Next round

Recommended Round 3 / P5-P6:

```text
Add item-level potion burden metadata and player-level potion burden runtime/decay, while keeping threshold consequences conservative/deferred to P7.
```
