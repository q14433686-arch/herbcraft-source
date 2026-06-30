# P1 Release Report — Codex Overview / Advancement Tree

日期：2026-06-12

## 结论

P1 已补完并输出可测试 jar。P0 的炼金催化 Mixin 修复也已保留，没有回退。

本轮没有继续长时间跑 `runServer`。原因：`runServer` 启动成功后不会自动退出，之前因 timeout 留下过残余 Java 进程并占用 `session.lock`。本轮吸取教训，采用：

1. 构建前先检查残余进程；
2. 不再把长跑 `runServer` 作为本轮必要验收；
3. 使用 `build + 静态验证 + jar 内容验证`；
4. 打包后执行 Gradle daemon 停止与进程复查。

## 本轮修复内容

### 1. 百草经总纲

已在 core 注册固定条目：

```text
core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java
```

新增：

- section：`overview` / 总纲
- entry：`overview`
- title：`book.herbcraft.entry.overview.title`
- lines：`book.herbcraft.entry.overview.line1-4`

该条目默认可见，玩家打开百草经即可看到。

### 2. 单根进度树

31 个 advancement 已重建为单根结构：

```text
herbcraft:root
```

alchemy / cuisine 进度均通过 parent 链接回 `herbcraft:root`，不再是多个碎片 root。

翻译键已从旧的：

```text
advancement.*
```

改为：

```text
advancements.*
```

### 3. 程序化授予入口

新增：

```text
core/src/main/java/com/herbcraft/advancement/HerbcraftAdvancements.java
core/src/main/java/com/herbcraft/advancement/HerbcraftAdvancementHooks.java
```

所有进度 JSON 仍使用统一 criterion：

```text
minecraft:impossible / trigger
```

由 Java 在对应事件发生时授予。

### 4. 已接入的基础触发

Core：

- 获得百草经 → root
- 打开百草经 → open_codex
- 首次食用 Herbcraft 草药 → first_bite
- 食用草/蕨类 → eat_grass
- 食用负面药性草药 → bitter_medicine
- 清除/反制类草药 → counter_poison
- herb_stack 3/5/8 → herb_stack_3/5/8
- 药性过载 → herb_overload
- 使用残页 → first_page
- 知识掌握 → master_first_entry / master_25_entries / master_all_core

Cuisine：

- 水碗 → water_bowl
- 任意固定菜 → first_dish
- 药膳清除/免疫菜 → immunity_meal
- 百草盅 → first_hodgepodge
- 百花宴 → hundred_flower_feast
- 剧毒蛊 → deadly_hodgepodge

Alchemy：

- 精华 → first_essence
- Herbcraft 药水 → first_potion
- 澄清 → first_clarified
- 浊化 / T3 毒系 → first_murky
- 极品药水组件 → epic_potion
- 淬层武器 → first_coating
- 亡灵毒液 → undead_toxin
- 女巫安全药水物品 → witch_knowledge

### 5. P0 防回退

已保留：

```text
alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java
scripts/validate_catalyst_mixin.py
```

## 构建

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew build --rerun-tasks --console=plain
```

结果：

```text
BUILD SUCCESSFUL
```

## 验证

已通过：

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

关键输出：

```text
All zh_cn/en_us language key sets match exactly.
PASS - catalyst mixin source/config/helper/built-jar checks passed.
PASS - P1 codex overview and unified advancement tree validation passed.
VERIFY OK: all built jars contain expected metadata/resources/classes and Java 25 bytecode.
```

## Release artifacts

输出目录：

```text
/home/user/herbcraft_p1_release/
```

包含：

```text
herbcraft-1.1.5.jar
herbcraft-alchemy-0.2.4.jar
herbcraft-cuisine-0.3.5.jar
herbcraft-1.1.5-0.2.4-0.3.5-p1-codex-advancements.zip
herbcraft-source-p1-codex-advancements.zip
SHA256SUMS.txt
```

SHA256 以 `/home/user/herbcraft_p1_release/SHA256SUMS.txt` 为准。

## 已知仍可细化项

这些不是阻塞 P1 jar 测试的内容，但后续可继续精修：

1. `feed_cow_grass` 目前进度 JSON 存在，但动物喂食精确事件尚未接入。
2. `villager_trade_herbcraft` 目前进度 JSON 存在，但 26.1 村民交易完成事件 / Mixin 点尚需单独确认。
3. `survive_witch_brew` 目前进度 JSON 存在，但“被女巫安全浊毒命中并存活”的精确触发尚未接入。
4. 客户端视觉测试仍需用户本地完成：百草经总纲显示位置、进度页是否为单页树、toast 是否符合预期。
