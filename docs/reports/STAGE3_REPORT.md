# STAGE3_REPORT

日期：2026-06-12

## 范围

本阶段完成用户新增的两个任务：

1. 残页 `tattered_page` 加入创造模式物品栏，并枚举带 `page_entry` 组件的变体。
2. 补全村民联动：武器匠、工具匠、盔甲匠、图书管理员，以及 cuisine 侧工具匠菜品交易。

## 残页创造栏

实现位置：

```text
core/src/main/java/com/herbcraft/knowledge/CreativeKnowledgeItems.java
```

入口：

```text
core/src/main/java/com/herbcraft/Herbcraft.java
```

实现方式：

- 使用 26.1 Fabric API：`CreativeModeTabEvents.modifyOutputEvent(...)`
- 目标创造页：`minecraft:tools_and_utilities`
- 不添加裸 `tattered_page`
- 遍历 `KnowledgeAPI.chapters()` 和 `chapter.entries()`
- 为每个条目生成带组件的残页：

```text
herbcraft:page_entry = { chapter: <chapter_id>, entry: <entry_id> }
```

模块边界：由于变体在创造页输出回调中动态遍历 `KnowledgeAPI`，所以只装 core 时只有百草志残页；装 cuisine/alchemy 后对应章节自动出现。

## 村民交易补全

### Core

新增图书管理员：

- 等级 2：绿宝石 ×2 → 百草志残页，限购 8
- 等级 5：绿宝石 ×12 → 随机已加载章节稀有残页，限购 2

保留原有 level 1 残页交易，没有改旧 ID。

### Alchemy

新增职业：

- Weaponsmith / 武器匠
- Toolsmith / 工具匠
- Armorer / 盔甲匠
- Librarian / 图书管理员炼金残页

关键点：

- 武器匠出售的淬层武器写入真实 `herbcraft:weapon_coating` 组件。
- 药水收购交易使用 `minecraft:potion_contents` 精确断言，普通水瓶不能冒充。
- 使用 26.1 数据驱动交易 JSON，不使用 `TradeOfferHelper`，不新增 Mixin。

### Cuisine

新增：

- 图书管理员等级 3：绿宝石 ×4 → 膳房录残页
- 工具匠等级 3：绿宝石 ×8 → 双菌下界煲
- 工具匠等级 4：双菌下界煲 ×1 → 绿宝石 ×5

说明：任务文档写“强筋健骨汤”，当前 cuisine 数据中没有同名菜品；检索发现 `twin_fungus_hotpot / 双菌下界煲` 是含力量效果的现有菜品，因此用它作为本轮工具匠菜品联动目标。

## 未做 / TODO

P1 轻量职业 Fisherman / Shepherd / Leatherworker / Mason 本轮未做，已记录在 `BUGS_TODO.md`。

## 新版本

- `herbcraft`：`1.1.2`
- `herbcraft_alchemy`：`0.2.3`
- `herbcraft_cuisine`：`0.3.2`

## 产物

```text
/home/user/herbcraft/release_stage3/herbcraft-1.1.2.jar
/home/user/herbcraft/release_stage3/herbcraft-alchemy-0.2.3.jar
/home/user/herbcraft/release_stage3/herbcraft-cuisine-0.3.2.jar
/home/user/herbcraft/release_stage3/SHA256SUMS.txt
```
