# P1.3：从 P1.1 良好基线重做修复报告

日期：2026-06-12

## 重要说明

本轮明确不再使用 GitHub 初始 handoff 源码，也不使用被回退污染的 `/home/user/herbcraft/herbcraft_source` 作为基线。

本轮基线为上一轮已经包含 P0/P1/P1.1 工作的源码包：

```text
/home/user/herbcraft_p1_1_release/herbcraft-source-p1.1-nature-jade-background.zip
```

新工作目录：

```text
/home/user/herbcraft_work/herbcraft_source
```

原因：用户指出之前出现 `BrewingStandCatalystMixin` 丢失，根因正是误基于旧源码/回退源码打包。本轮已纠正。

## 修复的问题

### 1. P0 Mixin 修复保留

确认源码与 jar 均包含：

```text
alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java
com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.class
```

用户提供的崩溃：

```text
ClassNotFoundException: com.herbcraft.alchemy.mixin.BrewingStandCatalystMixin
```

本轮已通过 jar 检查与实际 runServer smoke test 重新验证不会再发生。

### 2. 百草经总纲 raw key 修复

补齐：

```text
book.herbcraft.section.herbal.overview
book.herbcraft.entry.overview.title
book.herbcraft.entry.overview.line1
book.herbcraft.entry.overview.line2
book.herbcraft.entry.overview.line3
book.herbcraft.entry.overview.line4
```

并同时补齐 `en_us`，通过 `check_lang_diff.py`。

### 3. 草药食用 ActionBar raw key 修复

补齐：

```text
message.herbcraft.herb_stack.stage_3
message.herbcraft.herb_stack.stage_5
message.herbcraft.herb_stack.stage_8
message.herbcraft.herb_stack.fade
message.herbcraft.herb_stack.overload
```

这对应用户反馈“吃矮草丛后底下提示缺 key”的问题。

### 4. Herbcraft 进度页背景贴图

确认 root advancement 使用：

```json
"background": "herbcraft:textures/gui/advancements/backgrounds/herbcraft.png"
```

确认 jar 内存在：

```text
assets/herbcraft/textures/gui/advancements/backgrounds/herbcraft.png
```

### 5. Jade / 玉 一直显示未知的问题

上一版问题原因：Jade component provider 在客户端直接判断 `accessor.getPlayer() instanceof ServerPlayer`，客户端玩家不是 `ServerPlayer`，所以永远 fallback 成 unknown。

本轮改为 Jade 正确的服务端数据路径：

```text
IBlockComponentProvider + IServerDataProvider<BlockAccessor>
```

服务端写入：

```text
herbcraft:is_herb
herbcraft:knowledge_state
herbcraft:nature_temperature
herbcraft:nature_flavors
```

客户端只读取 `accessor.getServerData()`。

本轮同时将 Jade 开发运行时版本提升到当前 26.1 线：

```gradle
compileOnly "maven.modrinth:jade:26.1.0+fabric"
runtimeOnly "maven.modrinth:jade:26.1.0+fabric"
```

`runtimeOnly` 只用于 dev run 验证，不会把 Jade 打包进 Herbcraft jar。`fabric.mod.json` 仍是 optional：

```json
"suggests": {
  "jade": ">=26.1.0"
}
```

## 构建验证

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew build --rerun-tasks --console=plain
```

结果：

```text
BUILD SUCCESSFUL
```

## 静态验证

已通过：

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_catalyst_mixin.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/validate_nature_jade.py
python3 scripts/verify_built_jars.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/validate_panel_hodge_recipe.py
```

额外检查：

```text
PASS - user-reported raw translation keys present
```

## 实地 smoke test

### alchemy runServer

命令：

```bash
timeout 120 ./gradlew :alchemy:runServer --console=plain
```

结果：服务端达到：

```text
Done (0.488s)! For help, type "help"
```

并且 Jade 也在 dev runtime 中加载到 Herbcraft 插件：

```text
Start loading plugin from Herbcraft | 百草可食: com.herbcraft.compat.jade.HerbcraftJadePlugin
com.herbcraft.compat.jade.HerbcraftJadePlugin loaded
```

无 Mixin prepare 错误。

### alchemy runClient

命令：

```bash
timeout 120 ./gradlew :alchemy:runClient --console=plain
```

结果：客户端加载到：

```text
herbcraft 1.1.5
herbcraft_alchemy 0.2.4
jade 26.1.0+fabric
```

随后因沙盒无图形环境出现预期错误：

```text
Failed to initialize GLFW, X11: The DISPLAY environment variable is missing
```

这是 headless 沙盒限制；关键是已越过 Fabric/Mixin/Jade 插件加载阶段，没有 `BrewingStandCatalystMixin` 缺失或 Jade 插件类错误。

## 输出产物

输出目录：

```text
/home/user/herbcraft_p1_3_release/
```

包含：

```text
herbcraft-1.1.5.jar
herbcraft-alchemy-0.2.4.jar
herbcraft-cuisine-0.3.5.jar
herbcraft-1.1.5-0.2.4-0.3.5-p1.3-good-base-fixes.zip
herbcraft-source-p1.3-good-base-fixes.zip
SHA256SUMS.txt
```

## 进程清理

本轮 runServer / runClient 后均立即清理残余 Java/Gradle 进程。最终检查无：

```text
GradleDaemon
KnotServer
KnotClient
net.fabricmc.devlaunchinjector
minecraft
```
