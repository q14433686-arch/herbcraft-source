# Alchemy distillation-to-potion routing C — 2026-06-29

## Scope

Implemented C board after the B3 distillation foundation:

```text
C — route componentized distilled essences into existing potion processing paths.
```

This keeps current stable potion IDs and does not remove the older redstone/glowstone/honeycomb routes. Distillation becomes an additional Herbcraft-flavored route into the same processing families.

## What changed

### Distilled essence brewing route

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistilledEssenceBrewing.java
```

A distilled essence can now be placed in the brewing ingredient slot when it carries the `herbcraft_alchemy:distilled_essence` component.

Brewing rule:

```text
matching base Herbcraft potion + matching distilled essence -> corresponding processed potion path
```

Current routes:

```text
base source potion + purified distilled essence  -> source_clarified
base source potion + sustained distilled essence -> source_long
base source potion + intense distilled essence   -> source_strong
wither_rose base potion + intense distilled essence -> wither_venom_iii
```

Important matching rule:

```text
The distilled essence source must match the base potion path.
```

Example:

```text
herbcraft:dandelion potion + distilled_essence[source=dandelion, orientation=purified]
-> herbcraft:dandelion_clarified
```

But:

```text
herbcraft:poppy potion + distilled_essence[source=dandelion, orientation=purified]
-> no brew
```

### Brewing stand mixin integration

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java
```

The mixin now supports both existing custom brewing routes:

```text
HiddenCuisineCatalystBrewing
DistilledEssenceBrewing
```

The hidden Cuisine catalyst route remains intact.

### Component cleanup on output

When brewing through distilled essence routing, the output stack keeps the same potion item type but receives new `POTION_CONTENTS`. Old Alchemy initialization components are removed:

```text
POTION_QUALITY
POTION_BONUS
POTION_PROCESSING_STYLE
POTION_BURDEN
ADVANCED_POTION_INITIALIZED
```

This prevents stale quality/burden/style metadata from leaking from an input stack to the routed output. The normal inventory/update path can then initialize the output according to its new potion path.

## What did not change

This C pass did not:

- delete or replace existing redstone/glowstone/honeycomb brewing routes;
- create new potion IDs;
- add JEI/REI displays for distillation brewing;
- implement armor support;
- implement processed weapon coating;
- consume mismatched distilled essences.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

Validators:

```bash
python3 scripts/validate_distillation_foundation.py
python3 scripts/validate_distillation_brewing_routing.py
python3 scripts/verify_built_jars.py
```

Result: PASS.

## Client-only checks

A real client should confirm:

1. Distilled essence can be placed in the brewing ingredient slot.
2. Matching base potion + purified distilled essence brews into `_clarified`.
3. Matching base potion + sustained distilled essence brews into `_long`.
4. Matching base potion + intense distilled essence brews into `_strong`, except wither rose routes to `wither_venom_iii`.
5. Mismatched base potion/source essence does not brew and does not consume the distilled essence.
6. Existing redstone/glowstone/honeycomb routes still work.
7. Hidden Cuisine catalyst route still works.
8. Output stack does not retain stale quality/bonus/burden from an input stack.

## Next recommended board

Recommended next board:

```text
D — armor-side burden support / protective alchemy
```

Rationale: the potion processing + burden + distillation route now has enough structure for armor support to use `POTION_BURDEN_BUFFER` and `clear_bearing` meaningfully.
