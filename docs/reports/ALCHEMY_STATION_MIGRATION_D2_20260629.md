# Alchemy station migration D2 — 2026-06-29

## Scope

Implemented the user's station-correctness reminder before moving to processed weapon coating E.

Corrected workflow identity:

```text
Distillation: brewing stand, not crafting table.
Armor support: smithing table with adhesives, not crafting table.
```

## D2.1 Distillation moved to brewing stand

### Removed bundled crafting route

Removed old crafting JSON recipes:

```text
data/herbcraft/recipe/distilled_essence_purified.json
data/herbcraft/recipe/distilled_essence_sustained.json
data/herbcraft/recipe/distilled_essence_intense.json
```

### Brewing stand route

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistilledEssenceBrewing.java
alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java
```

New behavior:

```text
bottom slots 0-2: Herbcraft essence items are accepted as distillation inputs
ingredient slot 3: implemented distillation catalysts are accepted
```

Current distillation catalysts remain data-backed in `distillation_rules.json`:

```text
honeycomb       -> purified distilled essence
redstone        -> sustained distilled essence
glowstone_dust  -> intense distilled essence
```

Example:

```text
slot 0: herbcraft:essence_dandelion
slot 3: minecraft:honeycomb
-> slot 0: herbcraft:distilled_essence[source_essence=dandelion, orientation=purified]
```

The existing distilled-essence-to-potion route remains intact:

```text
base Herbcraft potion + matching distilled essence -> _clarified / _long / _strong / special wither output
```

The hidden Cuisine catalyst route remains intact.

## D2.2 Armor support moved to smithing table

### Removed bundled crafting route

Removed old crafting JSON:

```text
data/herbcraft/recipe/armor_support.json
```

The old crafting serializer remains registered only as a legacy/local compatibility stub, but no bundled recipe uses it.

### Added smithing route

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportSmithingRecipe.java
```

Added recipes:

```text
data/herbcraft/recipe/armor_support_apply.json
data/herbcraft/recipe/armor_support_refine.json
```

Serializer:

```text
herbcraft:armor_support_smithing
```

### Adhesive linkage

Armor support now mirrors the weapon coating station pattern, but with support semantics:

```text
blank_adhesive + armor + distilled_essence -> ATTACHED armor support
amethyst_adhesive + supported armor + empty -> CLEAR refinement
echo_adhesive + supported armor + empty -> DEEP refinement
```

New component mode:

```text
ATTACHED
CLEAR
DEEP
```

Implemented in:

```text
alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportMode.java
alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportComponent.java
```

Current support behavior:

```text
ATTACHED: base buffer
CLEAR:    stronger active burden buffer (+50%)
DEEP:     deeper bearing buffer (+25% current total-buffer representation)
```

This gives all three adhesives a meaningful armor-side role without turning armor support into a retaliation system.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

Updated validators:

```bash
python3 scripts/validate_distillation_foundation.py
python3 scripts/validate_distillation_brewing_routing.py
python3 scripts/validate_armor_support_foundation.py
python3 scripts/verify_built_jars.py
```

Result: PASS.

Full validator suite listed in `CURRENT_BASELINE.md` was run and passed.

## Client-only checks

### Distillation station check

1. Try the old crafting-table recipes for distilled essence.
   - Expected: no crafting result.
2. Put an essence in brewing stand bottom slot and honeycomb/redstone/glowstone in ingredient slot.
   - Expected: distilled essence output with correct source/orientation.
3. Verify distilled essence + matching base potion still routes to processed potion.
4. Verify hidden Cuisine catalyst brewing still works.

### Armor support station check

1. Try old crafting-table armor + distilled essence.
   - Expected: no crafting result.
2. Smithing table:
   - blank adhesive + armor + distilled essence -> supported armor `ATTACHED`.
3. Smithing table:
   - amethyst adhesive + supported armor + empty addition -> support mode `CLEAR`.
4. Smithing table:
   - echo adhesive + supported armor + empty addition -> support mode `DEEP`.
5. Confirm tooltip shows support mode and buffer.
6. Confirm worn armor buffers potion burden and gives clear-bearing feedback.

## Next board

Now E can proceed on a consistent station model:

```text
Distillation: brewing stand
Potion processing: brewing stand
Weapon coating: smithing table + adhesives
Armor support: smithing table + adhesives
```

Recommended next board remains:

```text
E — processed weapon coating expansion
```
