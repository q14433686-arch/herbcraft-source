# Alchemy burden consequences and extension hooks P7-P8 — 2026-06-29

## Scope

Implemented Round 4 / P7-P8 of the four-round Alchemy expansion task.

```text
P7 — conservative potion burden stages and consequences
P8 — minimal extension hooks/API for future distillation, armor support, and advanced coating integration
```

This completes the planned four-round source-level foundation while deliberately avoiding a full distillation system, armor alchemy system, or advanced coating branch in this round.

## P7 — burden stages and conservative consequences

### JSON stage rules

Updated:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json
```

Added data-backed burden stages:

```text
steady    min_total 0    speed_modifier  0.00
warming   min_total 120  speed_modifier  0.00
strain    min_total 300  speed_modifier -0.02
overload  min_total 600  speed_modifier -0.05
```

Each stage has a localized message key.

### Runtime consequence layer

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenConsequenceRuntime.java
```

Responsibilities:

- compute the current burden stage from total burden;
- apply a single transient movement-speed modifier for stage consequence;
- send a localized message when stage changes;
- fire a stage-changed event;
- clear the movement modifier on death/reset.

The current consequences are intentionally mild:

```text
steady/warming: no penalty
strain:         -2% movement speed
overload:       -5% movement speed
```

No backlash damage or status-effect punishment was added in this round.

### Player state stage persistence

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenState.java
alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyPlayerMixin.java
```

The last known burden stage is saved under `LastStage` in `HerbcraftAlchemyPotionBurden` so stage messages do not spam every tick after save/load.

Death reset clears burden state and removes the speed modifier.

## P8 — minimal extension hooks/API

### Events

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/AlchemyExpansionEvents.java
```

Events:

```java
PROCESSING_STYLE_APPLIED
POTION_BURDEN_APPLIED
POTION_BURDEN_STAGE_CHANGED
POTION_BURDEN_BUFFER
```

Intended future use:

- distillation can observe/apportion processing styles;
- armor support can buffer potion burden through `POTION_BURDEN_BUFFER`;
- advanced coating can observe style application or query style metadata;
- external code can observe burden application/stage changes without patching Alchemy internals.

### Read-only API

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/AlchemyProcessingAPI.java
```

Queries:

```java
processingStyleId(ItemStack)
processingStyle(ItemStack)
potionBurden(ItemStack)
playerBurden(ServerPlayer)
playerBurdenStage(ServerPlayer)
```

This API deliberately does **not** expose content registration/write methods.

### Hook wiring

Updated:

```text
PotionProcessingRuntime.java
PotionBurdenRuntime.java
PotionBurdenConsequenceRuntime.java
```

- style application fires `PROCESSING_STYLE_APPLIED`;
- burden application allows future buffer contributions and fires `POTION_BURDEN_APPLIED`;
- stage changes fire `POTION_BURDEN_STAGE_CHANGED`.

## Validators

Added:

```text
scripts/validate_potion_burden_consequences.py
scripts/validate_alchemy_expansion_boundaries.py
```

Updated:

```text
scripts/verify_built_jars.py
```

Validation covers:

- data-backed burden stages;
- mild movement-speed consequence values;
- localized stage messages;
- stage state persistence;
- speed modifier cleanup;
- P8 event/API classes;
- absence of write-registration methods in `AlchemyProcessingAPI`;
- built jar class/resource inclusion.

## What did not change

This round did not implement:

- distillation/re-distillation recipes or outputs;
- armor coating/protection runtime;
- advanced processed-essence weapon coating branches;
- HUD sync for burden;
- hard damage/backlash consequence;
- custom new potion output generation beyond existing stable path family.

The new P8 hooks make those future systems easier to attach without forcing them into the current implementation.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

Full validator suite listed in `CURRENT_BASELINE.md` was run and passed, including:

```bash
python3 scripts/validate_potion_processing_rules.py
python3 scripts/validate_potion_processing_runtime.py
python3 scripts/validate_potion_burden_runtime.py
python3 scripts/validate_potion_burden_consequences.py
python3 scripts/validate_alchemy_expansion_boundaries.py
python3 scripts/verify_built_jars.py
```

## Client-only checks

A real client should confirm:

1. burden stage messages appear only when crossing stages;
2. strain/overload movement penalties are noticeable but mild;
3. movement modifier clears after burden decays below stage and after death;
4. no backlash/damage occurs yet;
5. potion tooltip/style/burden metadata still display correctly;
6. vanilla potions and unrelated items remain unaffected.

## Four-round task status

```text
Round 1 / P1-P3 — complete
Round 2 / P4    — complete
Round 3 / P5-P6 — complete
Round 4 / P7-P8 — complete
```

The current result is a complete foundation for potion processing style, burden metadata, player burden, conservative consequences, and future integration hooks.
