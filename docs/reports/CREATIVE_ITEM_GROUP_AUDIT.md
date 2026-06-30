# CREATIVE_ITEM_GROUP_AUDIT

日期：2026-06-12

## 本次检查结论

### Core / herbcraft

| 物品 | 原状态 | 本次处理 |
|---|---|---|
| `herbcraft:herbal_codex` 百草经本体 | 已注册，但没有挂创造物品组 | 已加入原版 `minecraft:tools_and_utilities` 创造页 |
| `herbcraft:tattered_page` 残页 | 已注册，但没有挂创造物品组；裸物品无意义 | 不加入裸物品；改为枚举所有知识条目的带组件变体 |

残页创造栏变体生成逻辑：

- 位置：`core/src/main/java/com/herbcraft/knowledge/CreativeKnowledgeItems.java`
- 事件：`CreativeModeTabEvents.modifyOutputEvent(...)`
- 目标页：`minecraft:tools_and_utilities`
- 枚举源：`KnowledgeAPI.chapters()` → `chapter.entries()`
- 每个 `ItemStack` 都写入：

```text
herbcraft:page_entry = { chapter: <chapter_id>, entry: <entry_id> }
```

因此：

- 只装 core：只出现百草志条目的残页变体。
- 装 cuisine：自动额外出现膳房录条目的残页变体。
- 装 alchemy：自动额外出现炼金要术条目的残页变体。
- 空白无组件残页不会出现在创造栏。

### Alchemy / herbcraft_alchemy

| 物品 | 原状态 | 本次处理 |
|---|---|---|
| 各类 `herbcraft:essence_*` 精华 | 已在 `herbcraft:alchemy` 自定义创造页展示 | 无需补 |
| `herbcraft:blank_adhesive` 空白附着剂 | 已在 `herbcraft:alchemy` 自定义创造页展示 | 无需补 |
| `herbcraft:amethyst_adhesive` 紫晶附着剂 | 已在 `herbcraft:alchemy` 自定义创造页展示 | 无需补 |
| `herbcraft:echo_adhesive` 回响附着剂 | 已在 `herbcraft:alchemy` 自定义创造页展示 | 无需补 |
| 淬层武器 | 不是注册物品，是带 `herbcraft:weapon_coating` 组件的原版武器实例 | 不作为普通创造物品补入 |

### Cuisine / herbcraft_cuisine

| 物品 | 原状态 | 本次处理 |
|---|---|---|
| `herbcraft_cuisine:water_bowl` 盛水的碗 | 已在 `herbcraft_cuisine:cuisine` 自定义创造页展示 | 无需补 |
| 所有生坯菜品 | 已在 `herbcraft_cuisine:cuisine` 自定义创造页展示 | 无需补 |
| 所有成品菜品 | 已在 `herbcraft_cuisine:cuisine` 自定义创造页展示 | 无需补 |
| `herbcraft_cuisine:hodgepodge` 百草盅 | 已在 `herbcraft_cuisine:cuisine` 自定义创造页展示 | 无需补 |

## 验收对应

- 创造模式可找到残页变体：已通过 `KnowledgeAPI` 枚举实现。
- 每张残页带正确 `page_entry`：已通过静态校验脚本确认。
- 空白残页不出现在创造栏：实现中没有 `output.accept(CodexItems.TATTERED_PAGE)` 裸添加。
- 搜索“残页”：所有变体都是 `herbcraft:tattered_page` 物品栈，会进入搜索页。
- 模块边界：枚举发生在创造页输出回调中，按当前已注册的 `KnowledgeAPI` 章节与条目生成。
