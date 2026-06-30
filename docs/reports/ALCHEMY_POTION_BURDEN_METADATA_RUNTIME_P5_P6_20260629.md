# Alchemy potion burden metadata/runtime P5-P6 — 2026-06-29

## Scope

Implemented Round 3 / P5-P6 of the four-round Alchemy expansion task.

```text
P5 — item-stack potion burden metadata
P6 — player-level potion burden runtime, persistence, death/reset policy, and timed decay
```

This round intentionally does **not** implement P7 threshold consequences such as movement penalties, backlash damage, overload status effects, or broader body-state integration.

## What changed

### JSON burden metadata and decay rules

Updated:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json
```

Each style now has item-stack burden metadata:

```text
burden_weight
tail_weight
burden_profile
```

Current first-pass weights:

```text
sustained:   burden 18 / tail 14
intense:     burden 34 / tail 8
purified:    burden 12 / tail 6
murky_edge:  burden 42 / tail 18
harmonized:  burden 10 / tail 10   # reserved/deferred style metadata only
```

Added player burden decay rules:

```json
"burden_runtime": {
  "decay_interval_ticks": 200,
  "active_decay_amount": 5,
  "tail_decay_amount": 2,
  "max_burden": 1000
}
```

### Item-stack burden component

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenComponent.java
```

Registered in:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java
```

Component id:

```text
herbcraft_alchemy:potion_burden
```

The component is persistent and network-synced, containing:

```text
profile
burden_weight
tail_weight
```

### Burden metadata application

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenRuntime.java
```

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java
```

Processed potion stacks now receive burden metadata from their processing style. Existing initialized stacks can receive missing burden metadata without reapplying runtime effect rewriting.

### Player-level burden state

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenState.java
alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPlayerData.java
alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyPlayerMixin.java
```

Player state stores:

```text
burden
tail_burden
last_decay_game_time
```

Persistence key:

```text
HerbcraftAlchemyPotionBurden
```

Death/reset policy:

```text
Potion burden clears on death.
```

Respawn/copy policy:

```text
COPY_FROM copies potion burden only when alive=true, such as non-death player replacement cases.
Death respawns keep death-reset semantics.
```

### Consumption and ticking hooks

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyItemMixin.java
alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenRuntime.java
```

Updated:

```text
alchemy/src/main/resources/herbcraft_alchemy.mixins.json
alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java
```

Runtime behavior:

- processed potions are initialized at `finishUsingItem` HEAD so style metadata is present before/when consumed;
- after use, potion burden metadata is added into the player burden state;
- server tick decay runs every configured interval;
- active burden decays faster than tail burden;
- no P7 threshold consequences are applied yet.

### Tooltip projection

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java
alchemy/src/main/resources/assets/herbcraft/lang/en_us.json
alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json
```

Tooltips now include burden weights:

```text
Burden weight: active / tail
承负量：即时 / 尾负
```

## What did not change

This round did not add:

- burden stages;
- movement penalties;
- backlash damage;
- overload effects;
- HUD/client sync for player burden;
- Core pharmacology/body-state integration;
- distillation routing;
- armor support;
- advanced weapon coating branches.

Those belong to later rounds, especially P7 and P8.

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
python3 scripts/validate_potion_burden_runtime.py
python3 scripts/verify_built_jars.py
```

Result: PASS.

The full validator suite listed in `CURRENT_BASELINE.md` was also run and passed.

## Client-only checks

A real client should confirm:

1. processed potion tooltips show burden weights;
2. drinking processed potions increases player burden state without visible P7 consequences yet;
3. burden decays over time;
4. burden clears on death;
5. non-death player replacement/dimension-style copy cases do not unexpectedly wipe burden;
6. ordinary vanilla potions and non-potion food/items do not receive burden;
7. `_long` / `_strong` still do not roll random quality/bonus.

## Next round

Recommended Round 4 / P7-P8:

```text
P7 — add conservative burden stages/consequences.
P8 — add minimal cross-system extension hooks for future distillation, armor support, and advanced coating integration.
```

Keep P7 conservative: start with stage computation and small consequences only. Do not turn main Herbcraft Alchemy into a hardcore punishment system.
