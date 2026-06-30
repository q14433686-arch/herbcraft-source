# Pharmacology / Seven-Emotion Test Matrix — 2026-06-16

Status: discussion / source-audit report  
Purpose: help client-test whether the Codex 七情 guide lines are implemented and triggered correctly.

## 1. Source-audit result

The current relation decision order is in:

```text
core/src/main/java/com/herbcraft/pharmacology/PharmacologyResolver.java
```

Current order in `judgeSevenEmotion(...)`:

1. `XIANG_FAN` / 相反
2. `XIANG_SHA` / 相杀
3. `XIANG_WEI` / 相畏
4. `XIANG_E` / 相恶
5. `XIANG_XU` / 相须
6. `XIANG_SHI` / 相使
7. `SINGLE` / 单行
8. `NONE`

Important consequences:

- Earlier checks win. For example, a hot/cold toxic conflict becomes `XIANG_FAN` even if the current item is also `clearing` and the previous item is `toxic`.
- `SINGLE` exists as a mechanic, but current discovery tracking intentionally does **not** record/display it in the Codex. It has no overlay message in `SevenEmotionType` either.
- Current Codex discovery tracking records these six relation keys:

```text
seven_xiang_xu
seven_xiang_shi
seven_xiang_sha
seven_xiang_wei
seven_xiang_e
seven_xiang_fan
```

Therefore, if the author only saw `相须` and `相反`, the likely reason is that only those two were actually triggered. The other five visible relations are implemented in source and should appear after triggering. `单行` will not appear unless we decide to add it later.

## 2. General test rules

- Eat the **previous** item first, then the **current** item.
- Keep the two bites close together. Most relations use the recent-use window (`600` ticks / 30 seconds). `相反` additionally requires the hot/cold conflict within `xiang_fan_window_ticks = 200` ticks / 10 seconds.
- For clean testing, wait at least 35 seconds between separate pair tests, or die/respawn to clear pharmacology state.
- Many suggested items are `medicinal` / always edible, so full hunger should not block eating. If a non-medicinal alternative is used, lower hunger first.
- Some test items have unpleasant effects. Use a controlled test world, creative/survival commands, or be prepared for nausea/hunger/poison-like effects.

## 3. Recommended 七情 trigger table

| Target relation | Eat first | Eat second | Why it should trigger | Expected Codex line after trigger |
|---|---|---|---|---|
| 相须 / `XIANG_XU` | `minecraft:golden_dandelion` / 金蒲公英 | `minecraft:azure_bluet` / 蓝花美耳草 | They share at least two traits (`floral`, `clearing`). | `你曾见过“相须”：金蒲公英 与 蓝花美耳草 性近相助，正向药力更盛。` |
| 相反 / `XIANG_FAN` | `minecraft:wither_rose` / 凋零玫瑰 | `minecraft:blue_ice` / 蓝冰 | Previous is `hot` + `toxic`; current is `cold`; hot/cold toxic conflict wins first. Must be within 10 seconds. | `你曾见过“相反”：凋零玫瑰 与 蓝冰 寒热毒性相激，药效会反噬。` |
| 相杀 / `XIANG_SHA` | `minecraft:lily_of_the_valley` / 铃兰 | `minecraft:azure_bluet` / 蓝花美耳草 | Previous has `toxic`; current has `clearing`. | `你曾见过“相杀”：蓝花美耳草 的清解压过 铃兰 的毒性。` |
| 相畏 / `XIANG_WEI` | `minecraft:azure_bluet` / 蓝花美耳草 | `minecraft:lily_of_the_valley` / 铃兰 | Previous has `clearing`; current has `toxic`. | `你曾见过“相畏”：铃兰 遇 蓝花美耳草 制约，毒性反而收敛。` |
| 相恶 / `XIANG_E` | `minecraft:poppy` / 罂粟 | `minecraft:blaze_powder` / 烈焰粉 | Previous has `sedating`; current has `stirring`. | `你曾见过“相恶”：罂粟 与 烈焰粉 相忤，整体力道转弱。` |
| 相使 / `XIANG_SHI` | `minecraft:blaze_powder` / 烈焰粉 | `minecraft:golden_dandelion` / 金蒲公英 | Previous has `stirring`; current has `clearing`/`nourishing`; they are not the same profile and do not hit earlier relation checks. | `你曾见过“相使”：烈焰粉 引动 金蒲公英，药势行得更远。` |
| 单行 / `SINGLE` | `minecraft:golden_dandelion` / 金蒲公英 | `minecraft:blaze_powder` / 烈焰粉 | No shared traits and no earlier relation. Mechanically gives a small probability bonus. | Current source does **not** record/display 单行. Add `seven_single` later if desired. |

## 4. Alternate easier/non-rare pairs

If a listed item is hard to obtain, these alternatives should also work.

| Relation | Alternate pair | Notes |
|---|---|---|
| 相须 | `minecraft:dandelion` → `minecraft:blue_orchid` | Dandelion is not always edible; lower hunger if needed. |
| 相须 | `minecraft:poppy` → `minecraft:lily_of_the_valley` | Both are medicinal; shared `floral` + `sedating`. |
| 相反 | `minecraft:wither_rose` → `minecraft:snowball` | Both are medicinal; must be within 10 seconds. |
| 相反 | `minecraft:azalea_leaves` → `minecraft:torchflower` | Current is hot and previous is cold+toxic; torchflower may not be always edible. |
| 相杀 | `minecraft:wither_rose` → `minecraft:dandelion` | Dandelion not always edible; relation should still be `XIANG_SHA`. |
| 相畏 | `minecraft:dandelion` → `minecraft:wither_rose` | Dandelion not always edible; relation should be `XIANG_WEI`, unless hot/cold toxic conflict applies. Neutral→hot does not trigger `XIANG_FAN`. |
| 相恶 | `minecraft:closed_eyeblossom` → `minecraft:blaze_powder` | Sedating → stirring. |
| 相使 | `minecraft:dandelion` → `minecraft:poppy` | Current sedating, previous clearing/nourishing; dandelion not always edible. |

## 5. 五味 / 温性 guide test examples

The `五味与性味` guide should no longer print exact first tasted item nature. It should show broader principles only after they are discovered.

| Principle | Suggested item/action | Why |
|---|---|---|
| 甘 / `flavor_sweet` | Eat `minecraft:golden_dandelion` / 金蒲公英 | Has `sweet` and healing-like positive effects. |
| 苦 / `flavor_bitter` | Eat `minecraft:golden_dandelion` / 金蒲公英 | Has `bitter`; bitter rule applies immunity/herb-stack behavior. |
| 辛 / `flavor_pungent` | Eat `minecraft:blaze_powder` / 烈焰粉 | Has `pungent`, positive strength, and guaranteed nausea. |
| 酸 / `flavor_sour` | Eat `minecraft:glistering_melon_slice` / 闪烁的西瓜片 | Has `sour` and timed effects. |
| 涩 / `flavor_astringent` | Eat `minecraft:poppy` / 罂粟 | Has `astringent` and timed negative effects. |
| 咸 / `flavor_salty` | Eat `minecraft:kelp` / 海带 | Has `salty`. |
| 咸近水 / `flavor_salty_water` | Eat `minecraft:kelp` near water | `salty` + `nearWater(player)`. |
| 淡 / `flavor_bland` | Eat `minecraft:blue_ice` / 蓝冰 | Has `bland` and a negative side effect. |
| 寒热相济 / `temperature_harmonize` | Build positive qi with a hot item, then eat a cold item | Example: eat `blaze_powder`, then a cold item such as `blue_ice` before qi regresses. |
| 热性渐盛 / `temperature_same_hot` | Eat hot items until qi is at least +50, then eat another hot item | Example: repeated `blaze_powder` / `magma_cream`. |
| 寒意积聚 / `temperature_same_cold` | Eat cold items until qi is at most -50, then eat another cold item | Example: repeated `blue_ice` / `snowball`. |

## 6. If something does not appear

If a relation line does not appear after testing:

1. Confirm the pair order is correct.
2. Confirm no other herb was eaten immediately before the pair.
3. Confirm the pair was close enough in time.
4. For `相反`, confirm the two items are exact `hot`/`cold`, not just `warm`/`cool`, and that at least one has `toxic`.
5. Reopen the Codex after triggering; the snapshot is sent when the book opens.
6. If still absent, it may be a bug in discovery recording or guide line display.

## 7. Design note

Showing item pairs here does not violate knowledge gates because:

- the player has already eaten both items;
- the guide only records the relation the player personally triggered;
- exact item effects and exact hidden recipe knowledge remain in their own gated entries.

This is the intended role of the Codex as a field notebook.
