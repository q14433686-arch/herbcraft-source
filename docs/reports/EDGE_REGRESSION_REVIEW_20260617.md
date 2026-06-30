# Edge / Regression Review — 2026-06-17

Status: source-level review completed; no runtime code changed in this pass  
Baseline inspected: Herbcraft `1.1.3`

Input checklist:

```text
/home/user/uploads/Herbcraft 边缘.txt
```

This report answers each checklist item from current source. Items that require a real client, multiplayer server, profiler, or third-party modpack test are explicitly marked.

## Executive summary

High-confidence source conclusions:

- Milk can clear active nutrition effects, but drinking milk itself has a nutrition profile and `ItemMixin` re-evaluates nutrition at `finishUsingItem RETURN`, so the nutrition effects should be restored immediately according to current nutrition values.
- Milk does not clear Herbcraft data state such as `qiBalance`, `herbStack`, nutrition values, or recent meals.
- Cuisine `clearAllEffects` deliberately skips Herbcraft nutrition effects.
- `xiang_fan` / `cancelAll` does **not** stop later knowledge, HerbConsume event, bucket remainder, counters, herb-stack overload check, or the separate nutrition recording path.
- Death clears herb stack, pharmacology state / qi, effect immunities, and resets nutrition to initial values.
- Offline nutrition does not catch up by elapsed real time; it resumes while online.
- Food/nutrition/reload APIs are mostly bounded and data-driven, but external `special_counters` currently have an important limitation: arbitrary external counter IDs do not persist/count because player counter storage is hardcoded to four built-in IDs.

Post-review P1/P2 update:

- Dynamic special counter storage is now implemented; arbitrary external counter IDs can accumulate threshold counts. See `docs/reports/EDGE_P1_P2_FIXES_20260617.md`.
- Optional missing ingredient entries are now aggregated for external/optional compat rows instead of warning one line per missing item.

Main things still needing runtime confirmation:

- Real MSPT/TPS impact under larger 20+ player simultaneous eating loads. The author reported a two-client local runtime test with no visible performance change.
- Tick spike behavior from synchronized periodic checks.
- Cross-dimension timing for herb stack / pharmacology windows.
- Multiplayer knowledge isolation in actual shared-container/Jade situations.
- Third-party right-click mod conflicts.
- FD/Mores absent or version mismatch log volume after aggregation.

## Risk highlights / possible follow-up tasks

### A. External special counter IDs are now fixed after this review

Original finding:

```text
SpecialCounters loaded arbitrary JSON rule IDs, but PlayerMixin only persisted poppy/torchflower/sugar/slime counters.
```

Resolution:

```text
core/src/main/java/com/herbcraft/mixin/PlayerMixin.java now stores window counters in a dynamic Map<String, CounterState> and persists it as HerbcraftWindowCounters.
```

Legacy built-in counter fields are migrated on read. See:

```text
docs/reports/EDGE_P1_P2_FIXES_20260617.md
```

### B. Optional Farmer+More missing item warnings are now aggregated after this review

Air placeholder bug was already fixed by registry `containsKey` checks. This review also led to aggregation for external/optional ingredient rows:

```text
IngredientsChapter now skips missing optional item IDs and logs one aggregate info line with examples.
```

If Farmer's Delight / More Delight are absent, expected behavior is now:

```text
No Air entries.
No one-warning-per-item spam.
A compact optional-missing summary may appear.
```

### C. Periodic player checks are not staggered

Nutrition tick checks are bounded, but not deliberately staggered by UUID/player index. On a large server, many players can evaluate on the same interval tick.

Recommended follow-up only if profiler shows spikes:

```text
Stagger nutrition check offsets per player UUID or player join tick.
```

## Item-by-item review

## 一、状态清除类交互

### 1. Milk vs nutrition effects

Verdict: **source indicates safe / immediate re-evaluation**.

Relevant source:

```text
core/src/main/java/com/herbcraft/mixin/ItemMixin.java
core/src/main/java/com/herbcraft/nutrition/NutritionManager.java
core/src/main/resources/data/herbcraft/nutrition_profiles.json
```

Facts:

- `minecraft:milk_bucket` has a nutrition profile:

```text
flesh: 14
oil: 10
```

- `ItemMixin` injects at `finishUsingItem RETURN` for all foods:

```text
NutritionManager.onFoodConsumed(serverPlayer, stack)
```

- `onFoodConsumed(...)` records nutrition and immediately calls:

```text
evaluateAndApply(player, state)
```

Therefore, if vanilla milk clears active potion/effect instances first, Herbcraft runs after `finishUsingItem` and re-applies nutrition effects according to the current nutrition state.

Potential client test:

```text
Set a deficiency, drink milk, confirm deficiency/well_nourished state is restored immediately after the drink tick rather than waiting 1200t.
```

### 2. Milk vs qi / herb_stack / nutrition data

Verdict: **source confirms milk does not clear those data fields**.

Milk removes active status effects. It does not call:

```text
herbcraft$clearHerbStack()
herbcraft$clearPharmacologyState()
PlayerNutritionState.resetOnDeath()
```

Those are only cleared by specific Herbcraft logic or death handling.

Expected:

```text
qiBalance remains.
herbStack remains until expiry/overload/decrement/death.
nutrition values remain and milk adds its flesh/oil nutrition.
```

### 3. Cuisine clearing vs Herbcraft custom nutrition effects

Verdict: **clearAll safe; explicit remove list safe in current data, but relies on data not listing nutrition effects**.

Relevant source:

```text
cuisine/src/main/java/com/herbcraft/cuisine/item/DishItem.java
cuisine/src/main/java/com/herbcraft/cuisine/item/HodgepodgeItem.java
```

`DishItem.clearAllEffects` filters out Herbcraft nutrition effects:

```java
.filter(effect -> !HerbcraftMobEffects.isNutritionEffect(effect))
```

`HodgepodgeItem.clearPositiveEffects` also skips nutrition effects.

However, `DishItem.clears.forEach(player::removeEffect)` removes exactly whatever effects are listed in dish data. Current dish data uses vanilla effects for clearing; if a future JSON row listed a Herbcraft nutrition effect, it would remove it.

Recommended guard if desired:

```text
Validator: ensure Cuisine remove_effects never references Herbcraft nutrition effects unless explicitly intended.
```

### 4. 七情 cancelAll cutoff

Verdict: **source confirms later knowledge/event/nutrition are not swallowed**.

Relevant source:

```text
core/src/main/java/com/herbcraft/logic/EffectResolver.java
core/src/main/java/com/herbcraft/mixin/ItemMixin.java
```

`cancelAll` skips:

```text
immunity granting
effects
damage_events
heal_events
```

But after that block, the method still performs:

```text
bucket remainder
SpecialCounters.apply(...)
HerbStackManager.overloadIfNeeded(...)
KnowledgeManager.markTasted(...)
HerbConsumeEvents.fire(...)
```

Nutrition is outside `EffectResolver`, in `ItemMixin` after `applyAfterConsume(...)`, so it also still runs.

Conclusion:

```text
明明吃了却没记录: not expected from cancelAll.
HERB_CONSUMED: still fires.
Nutrition: still records through ItemMixin.
```

## 二、性能 / 服务器负担

### 5. 20+ players eating at once

Verdict: **cannot confirm without profiler/MSPT measurement**.

Source suggests the work per eating event is bounded:

- Herb pipeline iterates over the eaten item's effect lists.
- Pharmacology checks current profile, recent uses, and ordered seven-emotion rules.
- Nutrition checks five dimensions and recent meals capped at 24.
- Special counters iterate over current counter rules.

But no source-only review can prove MSPT impact for 20+ simultaneous eaters.

Recommended test:

```text
Dedicated server, Spark or built-in MSPT monitor, 20+ players/bots eating herbs repeatedly.
```

### 6. Periodic tick task alignment

Verdict: **bounded but not staggered**.

Relevant source:

```text
Herbcraft.java END_LEVEL_TICK -> NutritionManager.tick(player, gameTime)
KnowledgeSyncManager every 20 ticks
HerbcraftDataSyncManager dirty sync on server tick
```

Nutrition decay/evaluation checks interval timestamps per player, but initial timings may align. There is no explicit per-player stagger offset.

Potential issue:

```text
Many players may evaluate nutrition on the same tick after long synchronized play.
```

Recommended only if profiler shows spikes:

```text
Add per-player offset or join-time stagger to check intervals.
```

### 7. Recent meals memory / serialization

Verdict: **bounded**.

Relevant source:

```text
PlayerNutritionState.recentMeals
recentMealLimit = 24
```

Each player keeps at most 24 recent meals, each with a dimension map of up to five ints. The NBT serialization is a compact string.

Risk level:

```text
Low.
```

### 8. special_counters accumulation

Verdict: **no unbounded growth, but external arbitrary IDs currently do not count correctly**.

Current player storage is four hardcoded counters:

```text
poppy
torchflower
sugar
slime
```

This prevents infinite growth, but means new external special counter IDs do not get persistent count windows.

See risk highlight A.

## 三、维度切换 / 死亡 / 重生 / 离线

### 9. Cross-dimension behavior

Verdict: **data persists; timing needs runtime confirmation**.

Persisted on player:

```text
herbStack / herbExpiresAt
pharmacology state / qi
nutrition state
knowledge
```

Herb stack expiry uses `gameTime` from the current level. If dimension game times are not effectively aligned in the target environment, expiry windows may drift.

Recommended test:

```text
Build herbStack in overworld, immediately change dimension, continue eating / wait expiry window.
Build qi in one dimension, change dimension, eat opposite/same nature.
```

### 10. Death clearing boundary

Verdict: **source confirms clearing/reset behavior**.

Relevant source:

```text
PlayerMixin.die TAIL
PlayerNutritionState.resetOnDeath()
```

On death:

```text
herbStack cleared
pharmacology state cleared, including qiBalance
Effect immunities cleared
Nutrition reset to initial_value 500
Recent meals cleared
Knowledge persists
```

So current intended behavior is:

```text
死亡清药性/气机/营养状态，不清知识。
```

### 11. Offline decay

Verdict: **no nutrition catch-up while offline**.

Nutrition ticks only for online players in server tick. There is no real-time catch-up on login.

On login:

```text
NutritionManager.onLogin -> evaluateAndApply + sync
```

Pharmacology qi regression is called when resolving pharmacology, not as a background tick. After long offline time, the next relevant consumption can regress qi based on gameTime delta.

Expected:

```text
No instant nutrition crash after three offline days.
No nutrition decay while offline.
HerbStack may expire on next check if world gameTime advanced beyond herbExpiresAt.
Qi may regress on next pharmacology resolve.
```

### 12. /kill and first post-respawn food

Verdict: **source indicates safe; client sanity recommended**.

Death resets relevant player data. Nutrition constructor/defaults and `resetOnDeath` use current rules. First post-respawn food should use initialized state.

No obvious null path found.

## 四、数值边界 / 极端组合

### 13. herbStack threshold and bitter decrement

Verdict: **stack_group bitter cannot trivially avoid overload at 9; non-stack bitter can intentionally reduce stack**.

Order in `EffectResolver`:

```text
herbStack = updateAndGet(...)
PharmacologyResolver.resolve(...) may decrement for bitter
HerbStackManager.overloadIfNeeded(player, herbStack) uses the pre-decrement local value
```

So if stack is 9 and you eat a `stack_group` bitter herb:

```text
updateAndGet -> local herbStack = 10
bitter may decrement stored stack
but overloadIfNeeded sees local 10 and overloads
```

If you eat a bitter non-stack item, it can reduce herb stack without incrementing first. That appears to be intended management, not a bypass bug.

### 14. Clamp / NaN / multiplier safety

Verdict: **core clamps are present; malformed JSON multipliers still rely on validators/review**.

Confirmed clamps:

```text
qiBalance clamp -100..100
Nutrition values clamp 0..maxValue
PharmacologyResult.chance clamps 0..1
PharmacologyResult.duration min 1 tick
Hodgepodge clash chance min/max clamp
Food_roll saturation clamps 0..current food level
```

Multipliers are ordinary floats from JSON. Current shipped values are sane. A malicious datapack could still put strange numbers in some fields if validators do not catch it.

### 15. 7-herb hodgepodge extreme clash

Verdict: **source bounded; runtime recipe test recommended**.

Relevant source:

```text
HodgepodgeItem
HodgepodgeRules
```

Bounds:

```text
max_herbs = 7
clash chance clamped 0.0..0.80
effect chance bonus capped at 1.0
stack gain capped by max_stack_gain plus floral bonus path
```

No obvious explosion to unbounded probabilities.

### 16. food_rolls saturation at food level 0

Verdict: **source confirms saturation becomes 0 if visible food is 0**.

`addDirectSaturation` clamps target to:

```java
min(foodData.getFoodLevel(), foodData.getSaturationLevel() + amount)
```

If food level is 0, hidden saturation cannot be above 0. This matches vanilla saturation semantics.

## 五、信息门禁 / 显示一致性

### 17. Jade / tooltip / Codex consistency

Verdict: **source aims for consistent gating; client retest needed for exact sync timing**.

Current after recent fixes:

- Inventory tooltip recognizes full herbs and food-nature items.
- Jade recognizes full herbs and food-nature items.
- Codex uses server-side snapshot and knowledge flags.
- Food-nature uses namespace-aware 药食志 entry IDs.

Potential timing difference:

```text
Knowledge sync runs periodically; tooltip/Jade/Codex can differ briefly right after eating until sync arrives.
```

Recommended client check:

```text
Eat tomato/onion/cabbage, wait ~1 second, compare tooltip/Jade/Codex.
```

### 18. Jade absent / client config

Verdict: **source suggests optional Jade is safe; runtime modpack launch still best**.

Jade is only suggested in metadata. Core has a `jade` entrypoint for Jade to discover when Jade is present. Lang keys exist and validators check them.

Cannot fully confirm all launcher/Jade-absent behavior from source alone, but this pattern is typical.

### 19. Multiplayer knowledge isolation

Verdict: **source indicates per-player state; multiplayer test recommended**.

Facts:

- Knowledge flags live on each `ServerPlayer`.
- Tooltip uses each client's own `KnowledgeClientCache`.
- Jade server data uses `accessor.getPlayer()` to build server data for the viewing player.
- Codex snapshots are built per player.

Expected:

```text
Player B should not inherit Player A's knowledge.
```

Still worth testing in a two-player client/server session.

## 六、兼容 / 数据重载

### 20. /reload safety

Verdict: **reload-safe metadata reloads; player state should persist; startup herbs changes still require restart and have no in-game warning**.

Reload path:

```text
HerbcraftExternalDataReloadListener
```

Reloads:

```text
herb_natures
nutrition_profiles
ingredients
food-nature Codex entries
special_counters
knowledge_rumors
codex_loot_injections
```

Then marks data sync dirty and fires `EXTERNAL_DATA_RELOADED`.

Player data is not cleared by reload:

```text
nutrition values remain
qi/recent uses remain
knowledge flags remain
herbStack remains
```

Startup-only `herbs` path is not part of `/reload`. If a world datapack edits `data/<namespace>/herbcraft/herbs/*.json`, it will not make items newly edible at runtime. There is currently no in-game warning telling the user to restart.

### 21. Farmer's Delight absent / version mismatch

Verdict: **no Air placeholders expected; possible warning spam from missing ingredient entries**.

Recent fixes use registry presence checks before resolving optional item IDs.

Expected without Farmer's Delight:

```text
No air placeholder Codex entries.
No crash from missing Farmer's Delight classes, because no hard dependency exists.
Nutrition/nature ID metadata may be loaded but inert without runtime items.
```

Potential issue:

```text
IngredientsChapter may log one warning per missing optional bundled compat item.
```

Recommended test:

```text
Launch Core without Farmer's Delight / More Delight and inspect log volume.
```

### 22. Core without Alchemy/Cuisine

Verdict: **source indicates safe**.

Core does not import Cuisine or Alchemy classes for hidden catalyst logic. It only contains some ID strings / lang keys / nutrition profile IDs. Missing addon items are inert if the addon is absent.

Expected:

```text
Core only should run.
```

### 23. Right-click interaction mod conflicts

Verdict: **cannot guarantee from source alone**.

Herbcraft uses an Item mixin on:

```text
finishUsingItem RETURN
releaseUsing HEAD cancellable for shortUseProjectile items
```

Egg/snowball short-use behavior may conflict with other mods that also redirect `releaseUsing` or item use. This needs modpack conflict testing.

## 七、小角落

### 24. Creative / spectator mode

Verdict: **creative can likely trigger effects when consuming; spectator likely blocked by vanilla, needs client test**.

Source does not skip Herbcraft effects for creative players. Creative players can avoid item consumption, but if they use/eat the item, Herbcraft logic can run.

No explicit spectator guard in Herbcraft item-consume logic; vanilla usually prevents normal item use in spectator.

### 25. Feeding animals vs player eating

Verdict: **source indicates player nutrition/pharmacology is tied to player item consumption, not animal feeding**.

Herbcraft's player nutrition/pharmacology hooks are in `Item.finishUsingItem` / `DishItem.finishUsingItem`. Animal feeding interactions do not call player `finishUsingItem` as an eaten item.

Expected:

```text
Feeding animals should not increment player nutrition/herbStack/qi.
```

### 26. Crafting/container consumption

Verdict: **source indicates safe**.

Automated consumption by hopper/crafting does not call `finishUsingItem` on a player. Herbcraft's player state changes require active player consumption or explicit dish/hodgepodge finish-use logic.

Expected:

```text
No player nutrition/pharmacology from crafting/hoppers.
```

## Suggested test priority

Highest value tests from the checklist:

```text
1. Milk vs nutrition effects, to confirm no visible blank window.
4. xiang_fan cancelAll, to confirm player-facing knowledge/event behavior.
5-6. Server profiler test if targeting large multiplayer.
9. Cross-dimension herbStack/qi timing.
11. Offline then login behavior.
14. Extreme multiplier sanity if custom datapacks alter JSON.
20. /reload with active nutrition/qi/knowledge and with attempted herbs changes.
21. Launch without Farmer's Delight/More Delight to check log noise.
```

## Recommended follow-up bugfix backlog

1. **Dynamic special counters**: replace hardcoded `poppy/torchflower/sugar/slime` player fields with a saved map so external special counter IDs actually work.
2. **Optional compat missing-item logging**: avoid or aggregate warning spam when bundled optional compat target mods are absent.
3. **Tick staggering**: only if profiler shows synchronized nutrition checks causing spikes.
4. **Optional runtime warning for startup-only herbs**: document or warn when a datapack tries to use `herbs` in a reload-only context, if feasible.
