# Alchemy custom burden effects A-round — 2026-06-29

## Scope

Implemented the A-round follow-up after the four-round potion-processing foundation:

```text
A — replace direct burden-stage movement modifier logic with Alchemy custom MobEffects and fix misleading long/strong tooltip quality text.
```

This round addresses the client report that `_long` / `_strong` potions displayed the line "quality will be rolled when this stack is updated" even though they intentionally do not enter the legacy advanced random quality/bonus path. Clarified/murky potions still use the legacy quality/bonus path and may show quality.

## Client issue fixed

### Problem

`AdvancedPotionTooltips` displayed the uninitialized quality line for all style-projected potions. That included `_long` and `_strong` potions, even though the P4 boundary deliberately kept those paths out of legacy quality/bonus randomization.

### Fix

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java
```

The uninitialized quality text now appears only when the potion is a legacy advanced path (`_clarified`, `_murky`, or risky special advanced path). `_long` and `_strong` still display processing style and burden metadata, but no longer promise random quality.

Expected after fix:

```text
_long / _strong:
  show processing style + burden info
  do not show "quality will be rolled..."
  do not roll random quality/bonus

_clarified / _murky:
  still show advanced type / quality or uninitialized quality as before
  still can roll random quality/bonus where existing logic allows
```

## Custom Alchemy burden effects

### Added JSON rules

Added:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/potion_burden_effects.json
```

Defines custom Alchemy effects:

```text
herbcraft_alchemy:potion_warming
herbcraft_alchemy:potion_strain
herbcraft_alchemy:potion_overload
herbcraft_alchemy:clear_bearing
```

`potion_strain` and `potion_overload` carry the movement-speed attribute rules that were previously expressed as a direct consequence modifier:

```text
potion_strain:   movement_speed -0.02
potion_overload: movement_speed -0.05
```

`clear_bearing` is registered as a future supportive/buffering effect for armor or harmonized routes, but it is not actively applied by burden stages yet.

### Added Java rules/registration

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/effect/AlchemyBurdenEffectRules.java
alchemy/src/main/java/com/herbcraft/alchemy/effect/AlchemyMobEffects.java
```

Alchemy initialization now registers these effects.

### Added icons/lang

Added effect icons:

```text
alchemy/src/main/resources/assets/herbcraft_alchemy/textures/mob_effect/potion_warming.png
alchemy/src/main/resources/assets/herbcraft_alchemy/textures/mob_effect/potion_strain.png
alchemy/src/main/resources/assets/herbcraft_alchemy/textures/mob_effect/potion_overload.png
alchemy/src/main/resources/assets/herbcraft_alchemy/textures/mob_effect/clear_bearing.png
```

Added language keys in Alchemy lang files for all four effects.

## Burden stage integration

Updated:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenConsequenceRuntime.java
```

Burden stages now include custom effect IDs:

```text
warming  -> herbcraft_alchemy:potion_warming
strain   -> herbcraft_alchemy:potion_strain
overload -> herbcraft_alchemy:potion_overload
```

`PotionBurdenConsequenceRuntime` now applies/removes Alchemy custom effects instead of directly adding/removing a raw movement-speed attribute modifier. This makes the consequence layer more visible, more extensible, and more consistent with the Core custom nutrition-effect architecture.

## Validators

Added:

```text
scripts/validate_alchemy_burden_effects.py
```

Updated:

```text
scripts/validate_potion_processing_rules.py
scripts/validate_potion_burden_consequences.py
scripts/verify_built_jars.py
```

Validation now guards:

- no misleading uninitialized quality line for style-only `_long` / `_strong` paths;
- `potion_burden_effects.json` schema and effect rules;
- custom Alchemy effect registration;
- language keys;
- 18x18 effect icons;
- burden stages using custom effects;
- built jar class/resource inclusion.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

Full validator suite listed in `CURRENT_BASELINE.md` was run and passed, including:

```bash
python3 scripts/validate_alchemy_burden_effects.py
python3 scripts/validate_potion_processing_rules.py
python3 scripts/validate_potion_burden_consequences.py
python3 scripts/verify_built_jars.py
```

## Client-only checks

A real client should confirm:

1. `_long` and `_strong` no longer show the uninitialized quality line.
2. `_long` and `_strong` still show processing style and burden info.
3. `_long` and `_strong` still do not roll quality/bonus.
4. `_clarified` and `_murky` still show/roll quality as before.
5. burden stages apply visible custom effects:
   - 药势内温 / Medicinal Warming;
   - 药负牵滞 / Medicinal Strain;
   - 药势过载 / Medicinal Overload.
6. strain/overload movement changes still feel mild.
7. death and burden decay clear the stage effects.

## Next major-board planning

The next larger board should be selected from:

```text
B — distillation / re-distillation foundation
C — distillation routing into potion processing
D — armor-side support / burden buffering
E — processed weapon coating expansion
```

Recommended order remains B -> C -> D -> E, unless client testing shows the new burden system needs tuning first.
