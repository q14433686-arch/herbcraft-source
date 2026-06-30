# Edge Review P1/P2 Fixes — 2026-06-17

Status: implemented in source; built jars refreshed; full validator suite passed  
Baseline: Herbcraft `1.1.3`

## Context

After the edge/regression review, the author completed a small runtime/performance sanity pass with two local Minecraft clients. Result reported by the author:

```text
Two-player local test: no visible performance change / no obvious MSPT-feeling issue.
```

The author then requested the P1/P2 follow-up fixes from the edge review.

## P1 fixed: dynamic special counter storage

### Problem

`special_counters` looked data-driven and externally extensible:

```text
data/<namespace>/herbcraft/special_counters/*.json
```

But the player-side counter state in `PlayerMixin` was hardcoded to four counters:

```text
poppy
torchflower
sugar
slime
```

Any external counter ID could load as a rule, but `threshold_effects` could not accumulate because unknown IDs returned count `0`.

### Fix

Updated:

```text
core/src/main/java/com/herbcraft/mixin/PlayerMixin.java
```

The player now stores counters in a map:

```text
Map<String, CounterState>
```

Saved as:

```text
HerbcraftWindowCounters
```

Encoding shape:

```text
counter_id|count|expiresAt;counter_id|count|expiresAt;...
```

Legacy migration is included for old built-in fields:

```text
HerbcraftPoppyCount / HerbcraftPoppyExpiresAt
HerbcraftTorchflowerCount / HerbcraftTorchflowerExpiresAt
HerbcraftSugarCount / HerbcraftSugarExpiresAt
HerbcraftSlimeCount / HerbcraftSlimeExpiresAt
```

These are migrated into the new map when present.

### Result

External special counter IDs can now accumulate threshold counts as the JSON spec implies.

## P2 fixed: optional compat missing item warning aggregation

### Problem

Bundled optional Farmer+More compatibility data is loaded through the same external-data layer. When Farmer's Delight / More Delight are absent, missing item IDs should be skipped cleanly.

The Air placeholder issue had already been fixed, but `IngredientsChapter` could still log one warning per missing optional item, creating noisy logs.

### Fix

Updated:

```text
core/src/main/java/com/herbcraft/knowledge/IngredientsChapter.java
```

For external/optional ingredient rows:

- missing item IDs are counted and skipped;
- a single aggregate info line is logged with up to five examples;
- valid rows still register normally;
- invalid item ID syntax still warns.

### Result

Absent Farmer+More target mods should no longer generate one warning per optional ingredient row. Missing optional entries are summarized.

## Additional guard fixed while touching this area

Updated:

```text
core/src/main/java/com/herbcraft/logic/SpecialCounters.java
```

Counter item resolution now checks:

```text
BuiltInRegistries.ITEM.containsKey(itemId)
```

before resolving an item. This avoids missing optional item IDs falling back to a default item.

## Similar hardcoding sweep

Checked current obvious registry/extension hotspots after the P1 finding:

```text
IngredientsChapter
HerbalChapter food-nature entries
HerbFoodRegistry startup resources
SpecialCounters
AlchemyUsageHints
AlchemyCodexHooks
```

Findings:

- Ingredient and herb-food optional item resolution now use registry presence checks.
- Food-nature entries already use registry presence checks.
- Alchemy usage hints already used `containsKey`.
- Alchemy witch loot item pool is bundled alchemy data, not optional third-party compat; no immediate external-hardcoding issue found there.
- Hodgepodge content IDs are produced by Herbcraft recipe/component logic rather than arbitrary external compat item profiles; no P1-level issue found, but it could receive a defensive `containsKey` guard in a future cleanup if desired.

No other issue as severe as the hardcoded special-counter storage was found in this quick sweep.

## Traits note

The author's note about traits is correct: many traits exist as a fixed vocabulary, but only a subset currently has direct rule effects.

Current state:

```text
All allowed traits are defined/validated.
Only toxic, clearing, nourishing, stirring, sedating are strongly active in seven-emotion rules.
Other traits mostly serve as Codex hints, shared-trait relation material, flavor identity, and future extension space.
```

This is documented in:

```text
docs/HERBCRAFT_FULL_SYSTEM_WIKI_1_1_3.md
```

No trait expansion was implemented in this pass.

## Validation

Executed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_raw_herb_food_balance.py
python3 scripts/validate_essence_resources.py
python3 scripts/validate_homeostatic_compat.py
python3 scripts/validate_nutrition_cuisine_immunity_isolation.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_catalyst_mixin.py
python3 scripts/validate_nature_jade.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```

Result: all passed.

## Runtime retest suggestions

Minimal runtime check for P1:

```text
Create a tiny datapack with a new special counter id, e.g. example_compat:test_counter.
Attach threshold_effects at min_count 2 to a food/herb.
Eat twice inside the window.
Confirm threshold effect triggers.
Save/reload world and confirm persisted counters do not crash old saves.
```

Minimal runtime check for P2:

```text
Launch Core without Farmer's Delight / More Delight.
Confirm no Air entries.
Confirm logs summarize missing optional Farmer+More ingredient entries instead of printing one warning per item.
```
