# Alchemy armor-side support D foundation — 2026-06-29

## Scope

Implemented D board: first armor-side protective/supportive Alchemy foundation.

Design identity followed:

```text
Armor support is inward/protective/body-supportive, not retaliation-first.
```

The first implemented role is potion burden buffering using the P8 `POTION_BURDEN_BUFFER` hook.

## What changed

### Data-backed armor support rules

Added:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/armor_support_rules.json
```

Current support rows:

```text
clear_bearing      orientation purified   buffer 10  implemented
steady_bearing     orientation sustained  buffer 6   implemented
braced_bearing     orientation intense    buffer 4   implemented
harmonized_bearing orientation harmonized buffer 14  reserved/deferred
```

All active supports use the custom `herbcraft_alchemy:clear_bearing` effect as the support feedback effect.

### Armor support component

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportComponent.java
alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportComponents.java
```

Registered component:

```text
herbcraft_alchemy:armor_support
```

Component fields:

```text
support_id
source_essence
orientation
buffer_amount
```

### Armor support recipe

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportRecipe.java
alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportRecipes.java
alchemy/src/main/resources/data/herbcraft/recipe/armor_support.json
```

Crafting rule:

```text
one armor piece + one implemented distilled essence -> same armor piece with armor_support component
```

Armor detection uses the vanilla `DataComponents.EQUIPPABLE` component and requires an armor equipment slot, avoiding hardcoded item tables.

### Runtime burden buffer

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportRuntime.java
```

Runtime behavior:

- registers a listener on `AlchemyExpansionEvents.POTION_BURDEN_BUFFER`;
- scans equipped armor slots: head, chest, legs, feet;
- sums `buffer_amount` from equipped armor support components;
- applies the best support feedback effect duration from equipped supports;
- returns total buffer to the potion burden application path.

This means armor support reduces potion burden when processed potions are consumed, instead of acting as a retaliatory or damage-dealing armor mechanic.

### Tooltip projection

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/client/ArmorSupportTooltips.java
```

Tooltips show:

```text
support type
source essence
potion burden buffer amount
```

Language keys were added in both Alchemy lang files.

## What did not change

This D pass does not yet add:

- armor-specific textures/models;
- armor-side JEI/REI display integration;
- environment adaptation;
- status-response support;
- slot synergy bonuses;
- damage retaliation or thorns-like behavior;
- full harmonized support runtime.

Those remain future expansion candidates.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

New validator:

```bash
python3 scripts/validate_armor_support_foundation.py
```

Result: PASS.

`verify_built_jars.py` also passes and checks armor support classes/resources.

## Client-only checks

A real client should confirm:

1. crafting any normal armor piece + purified/sustained/intense distilled essence yields the same armor with support tooltip;
2. unsupported harmonized distilled essence does not craft support yet;
3. support tooltip shows source essence and buffer value;
4. wearing supported armor reduces burden gained from processed potions;
5. `clear_bearing` feedback appears when support buffers burden;
6. armor support does not damage attackers or otherwise behave like thorns;
7. ordinary armor without support is unchanged.

## Next recommended board

Recommended next board:

```text
E — processed weapon coating expansion
```

This should reuse the processing/distillation foundation without invalidating current base coating.
