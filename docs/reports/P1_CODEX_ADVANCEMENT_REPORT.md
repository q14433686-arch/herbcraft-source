# P1：百草经总纲与进度树修复报告

日期：2026-06-12

## 先说明：关于 runServer 超时

本轮后半段我已经暂停继续跑长时间 `runServer`。

观察到的情况不是“构建失败”，而是：

1. `./gradlew :alchemy:runServer` / `:cuisine:runServer` 属于启动一个真实 Minecraft 服务端。
2. 服务端正常启动到 `Done (...)! For help, type "help"` 后不会自动退出。
3. 因此如果用 `timeout` 包住，它最终会被 `timeout` 杀掉，工具层面会显示超时或退出码 `124`。
4. 有一次 120 秒超时发生在服务端启动/世界加载期间，随后残留的 Java 进程继续占用 `alchemy/run/world/session.lock`，导致下一次启动提示：

```text
session.lock: already locked (possibly by other Minecraft instance?)
```

我已经停止残留的 Gradle/Minecraft Java 进程，避免继续占用世界锁。本轮后续没有再继续做长跑服务端测试。

## 本轮范围

按用户要求，P0 成果由用户本地自行测试；我继续处理 P1：

1. 修复/重建进度树结构。
2. 修复进度翻译键。
3. 给进度增加基础程序化授予入口。
4. 确认并实现 `百草经总纲`。
5. 保留上一轮 P0 的炼金催化 Mixin 修复，防止回退。

未处理：真实客户端 UI 点击验证、进度全量精确触发器细化、GameTest。原因：本沙盒无图形环境；且用户要求若长跑服务端反复超时则暂停。

## 修改摘要

### 1. 百草经总纲已注册

修改：

```text
core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java
```

新增：

- `overview` section：总纲
- 固定 entry：`overview`
- 标题：`book.herbcraft.entry.overview.title`
- 文本：`overviewLines()`

新增语言键：

```text
book.herbcraft.section.herbal.overview
book.herbcraft.entry.overview.title
book.herbcraft.entry.overview.line1
book.herbcraft.entry.overview.line2
book.herbcraft.entry.overview.line3
book.herbcraft.entry.overview.line4
```

总纲默认 `mastered/heard` 条件均为 `player -> true`，也就是说玩家打开百草经时能看到，不需要先吃草或残页解锁。

### 2. 进度树从“散落占位”改为单根树

所有 advancement 文件仍位于 26.1 正确路径：

```text
data/<namespace>/advancement/*.json
```

本轮重写 31 个 advancement：

- Core：16 个
- Alchemy：9 个
- Cuisine：6 个

关键变化：

1. 只有一个 root：

```text
herbcraft:root
```

2. alchemy / cuisine 进度都通过 parent 链接回 `herbcraft:root`，不再形成碎片化 root。
3. 所有进度翻译键从旧的单数：

```text
advancement.xxx
```

改为标准复数：

```text
advancements.xxx
```

4. 仍使用单一 `minecraft:impossible` criterion，统一由 Java 代码在事件发生时授予。这避免写一堆不可靠或不可达的原版 JSON 条件。

### 3. 新增进度授予工具与基础 Hook

新增：

```text
core/src/main/java/com/herbcraft/advancement/HerbcraftAdvancements.java
core/src/main/java/com/herbcraft/advancement/HerbcraftAdvancementHooks.java
```

`HerbcraftAdvancements.grant(...)` 会：

1. 自动先授予 `herbcraft:root`；
2. 再授予目标进度；
3. 统一使用 criterion 名：`trigger`。

`HerbcraftAdvancementHooks` 当前接入：

- 背包扫描发现 `herbcraft:herbal_codex` → grant `root`
- 食用任意 Herbcraft 草药 → grant `first_bite`
- 食用草/蕨类 → grant `eat_grass`
- 食用带负面药性草药 → grant `bitter_medicine`
- 食用具有清除/反制机制的草药 → grant `counter_poison`

### 4. Core 进度 Hook

修改：

```text
core/src/main/java/com/herbcraft/Herbcraft.java
core/src/main/java/com/herbcraft/knowledge/CodexBookItem.java
core/src/main/java/com/herbcraft/logic/HerbStackManager.java
core/src/main/java/com/herbcraft/api/KnowledgeManager.java
core/src/main/java/com/herbcraft/knowledge/TatteredPageItem.java
```

当前基础授予：

- 获得百草经：`root`
- 打开百草经：`open_codex`
- 草药食用：`first_bite` / `eat_grass` / `bitter_medicine` / `counter_poison`
- 药性蓄积到 3/5/8：`herb_stack_3` / `herb_stack_5` / `herb_stack_8`
- 药性过载：`herb_overload`
- 使用残页：`first_page`
- 知识 MASTERED：`master_first_entry`
- MASTERED 数量 >= 25：`master_25_entries`
- herbal 核心条目全掌握：`master_all_core`

说明：`feed_cow_grass`、`villager_trade_herbcraft` 仍是占位式 programmatic advancement，尚未接入具体动物喂食/村民交易事件。这属于下一轮精细触发器工作。

### 5. Cuisine 进度 Hook

修改：

```text
cuisine/src/main/java/com/herbcraft/cuisine/item/WaterBowlItem.java
cuisine/src/main/java/com/herbcraft/cuisine/item/DishItem.java
cuisine/src/main/java/com/herbcraft/cuisine/item/HodgepodgeItem.java
```

当前基础授予：

- 饮用水碗：`herbcraft_cuisine:water_bowl`
- 吃任意固定菜：`herbcraft_cuisine:first_dish`
- 吃有清除/免疫属性的菜：`herbcraft_cuisine:immunity_meal`
- 吃百草盅：`herbcraft_cuisine:first_hodgepodge`
- 吃百花宴：`herbcraft_cuisine:hundred_flower_feast`
- 吃剧毒蛊：`herbcraft_cuisine:deadly_hodgepodge`

### 6. Alchemy 进度 Hook

修改：

```text
alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java
alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionInventoryHooks.java
alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingHitHandler.java
```

当前基础授予：

- 获得任意精华：`herbcraft_alchemy:first_essence`
- 获得任意 Herbcraft 药水：`herbcraft_alchemy:first_potion`
- 获得 `_clarified` 药水：`herbcraft_alchemy:first_clarified`
- 获得 `_murky` / T3 毒系药水：`herbcraft_alchemy:first_murky`
- 获得 `undead_venom`：`herbcraft_alchemy:undead_toxin`
- 获得或使用带淬层武器：`herbcraft_alchemy:first_coating`
- 获得极品药水组件：`herbcraft_alchemy:epic_potion`
- 获得 `witch_*` 药水：`herbcraft_alchemy:witch_knowledge`

说明：`survive_witch_brew` 尚未接入“玩家受到女巫安全浊毒且存活”的精确事件。下一轮可补。

### 7. P0 Mixin 修复已重新保留

本轮继续保留/恢复：

```text
alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java
scripts/validate_catalyst_mixin.py
```

避免此前“源码里缺 Mixin class，但 mixin json 引用它”的回退。

## 新增验证脚本

新增：

```text
scripts/validate_p1_codex_advancements.py
```

它检查：

- advancement 数量是否为 31；
- 是否只有 `herbcraft:root` 一个 root；
- 所有 parent 链是否最终回到 `herbcraft:root`；
- 是否仍有旧 `advancement.*` 单数翻译键；
- 所有 advancement title/description 翻译键是否存在；
- 百草经总纲是否注册；
- 基础 Java grant hook 是否存在；
- P0 catalyst mixin 是否仍存在于源码/构建 jar。

## 已完成的构建与静态验证

已成功运行：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew build --rerun-tasks --console=plain
```

结果：

```text
BUILD SUCCESSFUL
```

已成功运行：

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/validate_panel_hodge_recipe.py
python3 scripts/validate_catalyst_mixin.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```

关键结果：

```text
All zh_cn/en_us language key sets match exactly.
PASS - catalyst mixin source/config/helper/built-jar checks passed.
PASS - P1 codex overview and unified advancement tree validation passed.
VERIFY OK: all built jars contain expected metadata/resources/classes and Java 25 bytecode.
```

## 服务端运行状态

- P1 后曾尝试 `:alchemy:runServer`。
- 之前有一次长跑超时后残留进程占用了 `alchemy/run/world/session.lock`。
- 我已清理残留 Java/Gradle 进程。
- 随后不再继续长跑，以避免反复超时和干扰用户测试。

结论：本轮以 `build + 静态验证 + 防回退脚本` 为准，不把服务端长跑作为通过条件。

## 当前仍需下一轮处理

1. `feed_cow_grass` 精确触发：需要接动物喂食事件或 Mixin/API 替代方案。
2. `villager_trade_herbcraft` 精确触发：需要确认 26.1 是否有交易完成事件或合适 Mixin 点。
3. `survive_witch_brew` 精确触发：需要记录女巫安全药水命中来源并在存活后授予。
4. 客户端实际检查：百草经总纲是否显示位置合理、进度页面是否视觉上为一棵树。
5. 进度图标和排布可进一步美化，目前优先是结构正确与不碎根。
