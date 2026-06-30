# TASK_ABCD_REPORT

## Summary

Implemented the requested combined pass:

- Task A: core effect immunity system.
- Task B: cuisine dish effects reworked around clear + immunity, stronger buffs and risk dishes.
- Task C: glistering melon slice fixed.
- Task D: golden carrot added to `herbs.json`.

Existing stable item IDs were preserved. The task sheet's new conceptual dish names were mapped onto the current stable cuisine IDs instead of renaming items.

## Task A - Effect Immunity

Added:

```text
core/src/main/java/com/herbcraft/logic/EffectImmunityManager.java
core/src/main/java/com/herbcraft/mixin/LivingEntityMixin.java
```

Updated:

```text
core/src/main/java/com/herbcraft/HerbcraftPlayerData.java
core/src/main/java/com/herbcraft/mixin/PlayerMixin.java
core/src/main/java/com/herbcraft/api/HerbcraftAPI.java
core/src/main/resources/herbcraft.mixins.json
```

Behavior:

- Per-player effect immunity map: `effect id -> expiry game tick`.
- Saved/loaded with player data, like herb stack counters.
- Cleared on death.
- Query removes stale expired entries lazily.
- `LivingEntity.addEffect(MobEffectInstance, Entity)` is intercepted for `ServerPlayer` and cancelled if the player currently has immunity to that effect.
- Public API:
  - `HerbcraftAPI.grantEffectImmunity(ServerPlayer, Identifier, int durationTicks)`
  - `HerbcraftAPI.isImmuneToEffect(ServerPlayer, Identifier)`

## Task B - Cuisine Rework

Updated:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json
cuisine/src/main/java/com/herbcraft/cuisine/registry/DishRegistry.java
cuisine/src/main/java/com/herbcraft/cuisine/item/DishItem.java
```

New supported dish JSON fields:

```json
"effect_immunities": [
  {"effect": "minecraft:wither", "duration_seconds": 60}
],
"clear_all_effects": true,
"clear_frozen": true,
"ignite_seconds": 8,
"damage_events": [
  {"chance": 0.15, "amount": 1.0}
]
```

Dish settlement order in `DishItem`:

1. Fire/frozen/ignite special entity-state handling.
2. Counter tracking.
3. Optional clear-all.
4. `remove_effects`.
5. `effect_immunities` via `HerbcraftAPI`.
6. Normal status effects.
7. Damage events.
8. Last-meal tracking.

All dishes that remove effects now also grant matching immunity, with durations >= 30s.

Stable-ID mapping examples:

| Concept in task | Current stable ID used |
|---|---|
| blood-purifying broth | `soul_cleansing_stew` |
| appetite/digestive soup | `pumpkin_porridge` |
| vision-clearing gruel | `eye_opening_soup` |
| muscle/bone tonic | `fatigue_breaking_stew` |
| cold-dispelling tea | `blue_ice_brew` |
| emergency poison-fighting soup | `golden_dandelion_stew` |

## Task C - Glistering Melon Slice

Updated `core/src/main/resources/data/herbcraft/herbs.json`:

- `minecraft:glistering_melon_slice`
  - nutrition `2`
  - saturation `0.4`
  - always edible through `medicinal: true`
  - guaranteed `minecraft:instant_health` I
  - guaranteed `minecraft:absorption` I 30s
  - no longer relies on `heal_events`

## Task D - Golden Carrot

Updated:

```text
core/src/main/resources/data/herbcraft/herbs.json
core/src/main/resources/data/herbcraft/tags/item/herb.json
```

Added:

- `minecraft:golden_carrot`
  - nutrition `6`
  - saturation `14.4`
  - not always edible
  - guaranteed night vision 10s

No core code was needed for golden carrot because the item is now data-driven through `herbs.json`.

## Validation

Full build:

```text
BUILD SUCCESSFUL
```

Validation commands passed:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_panel_hodge_recipe.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/verify_built_jars.py
```

Additional note:

- `:core:compileJava` and `:cuisine:compileJava` passed after the new immunity code.
- `:cuisine:runServer` reached Fabric/Mixin initialization and previously confirmed Herbcraft/Cuisine loading with the new mixin registered; in this sandbox later server smoke attempts timed out during slow MC 26.1 startup without a crash or mixin failure.

## Release artifacts

Simple final release directory:

```text
release/
  herbcraft.jar
  herbcraft-alchemy.jar
  herbcraft-cuisine.jar
  herbcraft-pack.zip
  VERSION.txt
  SHA256SUMS.txt
  REPORT.md
```
