# TEST_REPORT

日期：2026-06-12

## 环境

- Minecraft `26.1.2`
- Fabric Loader `0.19.3`
- Fabric API `0.151.0+26.1.2`
- Java `25.0.3`
- Gradle `9.4.1`
- Fabric Loom `1.16.3`

## 执行的测试

### 1. 完整构建测试

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 ./gradlew clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### 2. 静态规则校验

命令：

```bash
./scripts/validate_alchemy_changes.py
```

结果：

```text
PASS - normal potions use stabilized formula
PASS - clarified no longer uses half-duration formula
PASS - murky no longer uses old x1.5 formula
PASS - normal positive minimum is 15 seconds
PASS - normal negative minimum is 10 seconds
PASS - advanced minimum is 30 seconds
PASS - duration cap is 480 seconds
PASS - single negative murky can be boosted from I to II
PASS - quality component uses herbcraft_alchemy:potion_quality
PASS - bonus component uses herbcraft_alchemy:potion_bonus
PASS - initializer is idempotent
PASS - quality weights 70/25/5 are present
PASS - bonus excludes forbidden omen/economic effects
PASS - Chinese tooltip keys exist
PASS - English display uses Turbid instead of Murky
All alchemy static validation checks passed.
```

### 3. Jar 内容检查

确认 `herbcraft-alchemy-0.2.1.jar` 内含：

- `AdvancedPotionStackInitializer`
- `AdvancedPotionInventoryHooks`
- `AlchemyPotionComponents`
- `PotionBonusComponent`
- `AdvancedPotionTooltips`

确认 `fabric.mod.json` 内部版本：

```json
"version": "0.2.1"
```

### 4. 服务端 smoke test

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 timeout 90 ./gradlew :alchemy:runServer --stacktrace
```

结果：

- Fabric Loader 成功加载。
- `herbcraft 1.1.0` 成功加载。
- `herbcraft_alchemy 0.2.1` 成功加载。
- 服务器停在 EULA 提示，属于未同意 EULA 的预期停止点。
- 日志未出现 herbcraft / herbcraft_alchemy 初始化崩溃。

关键日志：

```text
Herbcraft: Alchemy loaded. Registered essences, brewing mixes and the v3.4 weapon coating system.
SMOKE_OK: alchemy mod loaded until expected EULA stop
```

## 产物路径

```text
/home/user/herbcraft/release/herbcraft-1.1.0.jar
/home/user/herbcraft/release/herbcraft-alchemy-0.2.1.jar
/home/user/herbcraft/release/herbcraft-cuisine-0.3.0.jar
/home/user/herbcraft/release/SHA256SUMS.txt
```

## 续做阶段测试：残页战利品注入补全

### 完整构建

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 ./gradlew clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### 静态校验

命令：

```bash
./scripts/validate_stage2_loot_trades.py
```

结果：

```text
All stage2 loot/trade static validation checks passed.
```

校验覆盖：

- Core / Alchemy / Cuisine 均使用 `LootTableEvents.MODIFY`。
- 均带 `source.isBuiltin()` 守卫。
- Core 覆盖村庄、沉船、埋藏宝藏、丛林神庙、下界堡垒/堡垒遗迹、林地府邸等目标表。
- Alchemy / Cuisine 残页注入由各自模块负责。
- 特定 entry 残页 tooltip 不剧透。
- 模块版本与依赖版本正确。

### 服务端 smoke test

- Core + Cuisine：已启动到 EULA 预期停止点，`herbcraft_cuisine 0.3.1` 与 `herbcraft 1.1.1` 正常加载。
- Core + Alchemy：启动日志已确认 Fabric Loader 识别 `herbcraft_alchemy 0.2.2`、`herbcraft 1.1.1` 与目标环境；该次 runServer 被超时器截断在 Mixin 初始化后，未观察到 herbcraft/alchemy 报错。由于本阶段对 Alchemy 只改战利品事件映射，主要以 `clean build` 与静态校验作为通过标准。

### 新产物路径

```text
/home/user/herbcraft/release_stage2/herbcraft-1.1.1.jar
/home/user/herbcraft/release_stage2/herbcraft-alchemy-0.2.2.jar
/home/user/herbcraft/release_stage2/herbcraft-cuisine-0.3.1.jar
/home/user/herbcraft/release_stage2/SHA256SUMS.txt
```

## 续做阶段测试：创造栏残页 + 村民联动 P0

### 完整构建

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 /home/user/gradle/gradle-9.4.1/bin/gradle clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### 静态校验

命令：

```bash
./scripts/validate_stage3_creative_trades.py
```

结果：

```text
All stage3 creative/trade validation checks passed.
```

校验覆盖：

- 所有资源 JSON 可解析。
- 残页创造栏注册使用 `CreativeModeTabEvents`。
- 残页变体来自 `KnowledgeAPI.chapters()` / `chapter.entries()`。
- 残页变体全部带 `page_entry`，没有裸 `tattered_page`。
- 武器匠 / 工具匠 / 盔甲匠 / 图书管理员交易文件与 tag 均存在。
- 淬层武器交易写入 `herbcraft:weapon_coating` 组件。
- 药水收购交易带 `minecraft:potion_contents` 精确组件断言。
- cuisine/alchemy 残页交易位于各自模块，未放进 core。
- 版本与依赖正确。

### 服务端数据加载 smoke test

Alchemy：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 timeout 120 /home/user/gradle/gradle-9.4.1/bin/gradle :alchemy:runServer --stacktrace
```

结果：

```text
SMOKE_OK_ALCHEMY
Done (...s)! For help, type "help"
```

Cuisine：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 timeout 90 /home/user/gradle/gradle-9.4.1/bin/gradle :cuisine:runServer --stacktrace
```

结果：

```text
SMOKE_OK_CUISINE
Done (...s)! For help, type "help"
```

说明：两次服务端均已同意 EULA 跑到世界加载完成，数据包/交易 JSON 没有触发启动失败。`server.properties` 首次缺失警告为原版首次启动生成文件行为，不影响结果。

### 新产物路径

```text
/home/user/herbcraft/release_stage3/herbcraft-1.1.2.jar
/home/user/herbcraft/release_stage3/herbcraft-alchemy-0.2.3.jar
/home/user/herbcraft/release_stage3/herbcraft-cuisine-0.3.2.jar
/home/user/herbcraft/release_stage3/SHA256SUMS.txt
```

## 修复测试：高阶药水村民收购

### 完整构建

```bash
JAVA_HOME=/home/user/jdks/jdk-25 /home/user/gradle/gradle-9.4.1/bin/gradle clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### 静态修复校验

```bash
./scripts/validate_bugfix_advanced_potion_trade.py
```

结果：

```text
All advanced potion villager trade bugfix validation checks passed.
```

校验覆盖：

- alchemy 版本为 `0.2.4`。
- alchemy 声明 `herbcraft_alchemy.mixins.json`。
- Mixin 目标为 `ItemCost.test`。
- Mixin 仍保留 item/container 类型检查。
- Mixin 只对 Herbcraft 高阶药水成本启用，并按药水路径比较。
- 所有高阶药水收购交易 JSON 的输入只声明 `minecraft:potion_contents.potion`，没有要求品质、附加药性、初始化组件。

### 服务端 smoke

`alchemy:runServer` 已确认：

- Fabric Loader 识别 `herbcraft_alchemy 0.2.4`。
- Mixin 子系统启动，无 `Mixin apply failed` / `InjectionError` / `Critical injection failure`。
- Herbcraft / Herbcraft Alchemy 初始化无错误。

本环境下该次 runServer 因启动速度被 timeout 截断，未作为世界完全加载验收；核心验收以 `clean build`、Mixin 包装检查、静态规则校验为准。

### 产物路径

```text
/home/user/herbcraft/release_bugfix_potion_trades/herbcraft-1.1.2.jar
/home/user/herbcraft/release_bugfix_potion_trades/herbcraft-alchemy-0.2.4.jar
/home/user/herbcraft/release_bugfix_potion_trades/herbcraft-cuisine-0.3.2.jar
/home/user/herbcraft/release_bugfix_potion_trades/SHA256SUMS.txt
```

## 续做阶段测试：发布前修缮 + UI 自适应

### 完整构建

```bash
JAVA_HOME=/home/user/jdks/jdk-25 /home/user/gradle/gradle-9.4.1/bin/gradle clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### 村民交易标签校验

```bash
./scripts/validate_trade_tags.py
```

结果：

```text
All villager trade tag validation checks passed.
```

### 翻译差集校验

```bash
./scripts/check_lang_diff.py
```

结果：

```text
All zh_cn/en_us language key sets match exactly.
```

### 服务端 smoke

`alchemy:runServer` 已确认：

- Fabric Loader 识别 `herbcraft 1.1.3` 与 `herbcraft_alchemy 0.2.4`。
- Herbcraft / Alchemy 初始化完成。
- 未出现 Mixin apply failure / InjectionError / 数据包加载失败 / Herbcraft 初始化错误。

本轮 UI 自适应为客户端界面逻辑，服务端只验证通用数据包与注册项加载。

### 新产物路径

```text
/home/user/herbcraft/release_stage4/herbcraft-1.1.3.jar
/home/user/herbcraft/release_stage4/herbcraft-alchemy-0.2.4.jar
/home/user/herbcraft/release_stage4/herbcraft-cuisine-0.3.3.jar
/home/user/herbcraft/release_stage4/SHA256SUMS.txt
```

## 修复测试：百草经背景 + 百草盅 buff

### 完整构建

```bash
JAVA_HOME=/home/user/jdks/jdk-25 /home/user/gradle/gradle-9.4.1/bin/gradle clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### 静态校验

```bash
./scripts/validate_fix_codex_hodgepodge.py
```

结果：

```text
All codex background and hodgepodge validation checks passed.
```

### 服务端 smoke

`cuisine:runServer` 已确认 Fabric Loader 识别 `herbcraft 1.1.4` 和 `herbcraft_cuisine 0.3.4`，且 Core / Cuisine 初始化完成，未出现 Herbcraft 初始化错误。

### 新产物路径

```text
/home/user/herbcraft/release_fix_codex_hodgepodge/herbcraft-1.1.4.jar
/home/user/herbcraft/release_fix_codex_hodgepodge/herbcraft-alchemy-0.2.4.jar
/home/user/herbcraft/release_fix_codex_hodgepodge/herbcraft-cuisine-0.3.4.jar
/home/user/herbcraft/release_fix_codex_hodgepodge/SHA256SUMS.txt
```

## 续修测试：百草经面板半透明 + 百草盅生存合成

### 静态校验

```bash
./scripts/validate_panel_hodge_recipe.py
```

结果：

```text
All panel translucency and hodgepodge recipe checks passed.
```

### 完整构建

```bash
JAVA_HOME=/home/user/jdks/jdk-25 /home/user/gradle/gradle-9.4.1/bin/gradle clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### Jar 内容检查

已确认 `herbcraft-cuisine-0.3.5.jar` 包含：

```text
data/herbcraft_cuisine/recipe/hodgepodge.json
```

已确认 `herbcraft-1.1.5.jar` 包含：

```text
data/herbcraft/tags/item/herb.json
```

### 产物路径

```text
/home/user/herbcraft/release_panel_hodge_recipe/herbcraft-1.1.5.jar
/home/user/herbcraft/release_panel_hodge_recipe/herbcraft-alchemy-0.2.4.jar
/home/user/herbcraft/release_panel_hodge_recipe/herbcraft-cuisine-0.3.5.jar
/home/user/herbcraft/release_panel_hodge_recipe/SHA256SUMS.txt
```
