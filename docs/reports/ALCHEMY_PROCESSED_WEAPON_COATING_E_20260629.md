# Alchemy processed weapon coating E — 2026-06-29

## Scope

Implemented E board: processed/distilled essence integration for weapon coating while preserving the current base coating system.

Design boundary:

```text
Base essence coating remains valid.
Distilled essence coating is an advanced specialization, not a replacement.
```

## What changed

### Data-backed processed coating styles

Added:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/processed_coating_rules.json
```

Current styles:

```text
base       implemented, neutral baseline
sustained  implemented, more uses / longer softer application
intense    implemented, fewer uses / sharper stronger application
purified   implemented, stable cleaner application
murky_edge reserved/deferred
```

Fields:

```text
uses_multiplier
hit_duration_multiplier
amplifier_delta
lang_key
```

### Weapon coating component extension

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/coating/WeaponCoating.java
```

Added optional/persistent/networked field:

```text
processing_style
```

Default:

```text
base
```

Existing saved/base coatings remain compatible through the optional default.

### Runtime/style application

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/coating/ProcessedCoatingRules.java
```

Updated:

```text
CoatingSystem.java
CoatingHitHandler.java
CoatingTooltips.java
CoatingSmithingRecipe.java
HerbcraftAlchemy.java
```

Behavior:

- `blank_adhesive + weapon + base essence` still creates normal base coating.
- `blank_adhesive + weapon + distilled_essence[source, orientation]` now creates processed coating using the distilled orientation as processing style.
- `amethyst_adhesive` / `echo_adhesive` refinement preserves the coating processing style.
- Tooltips show processed coating style when not `base`.
- Hit/runtime effect profile uses data-backed processed coating style modifiers.
- Use count uses data-backed style multiplier.

Current style identities:

```text
sustained: more uses and longer/softer hit application
intense:   fewer uses and sharper hit application
purified:  stable moderate boost
```

### Smithing display

`CoatingSmithingRecipe.display()` now also includes representative processed coating rows using a dandelion distilled essence sample for implemented non-base styles.

This is a vanilla display projection. Dedicated JEI/REI plugin parity can be expanded in a later viewer-specific pass if needed.

## What did not change

This E pass did not:

- remove base coating recipes;
- add new weapon coating item IDs;
- implement murky_edge runtime;
- add separate processed coating JEI/REI plugin rows beyond vanilla display projection;
- add armor interaction with weapon coating;
- add new combat status effects beyond the current coating effect pipeline.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

New validator:

```bash
python3 scripts/validate_processed_coating_expansion.py
```

Result: PASS.

`verify_built_jars.py` also passes and checks processed coating rule/class artifacts.

## Client-only checks

A real client should confirm:

1. Existing base coating still works:
   - blank adhesive + weapon + base essence.
2. Processed coating works:
   - blank adhesive + weapon + distilled essence.
3. Processed coating tooltip shows:
   - base essence;
   - coating mode;
   - processed style.
4. Amethyst/echo refinement preserves processed style.
5. Sustained coating has more uses / softer longer profile.
6. Intense coating has fewer uses / sharper profile.
7. Purified coating feels stable and not weaker than base.
8. JEI/REI still show existing coating recipes; processed rows may need a later viewer-specific parity pass if not visible.

## Current major-board status

```text
A — custom Alchemy burden effects: complete
B — distillation B3 foundation: complete
C — distillation-to-potion routing: complete
D — armor support + D2 station migration: complete
E — processed weapon coating expansion: complete
```

Recommended next work:

```text
Real-client testing and tuning pass across the whole Alchemy expansion chain.
```
