# P1.4 严格基线重检与重新输出报告

日期：2026-06-12

## 重要：本轮基线

本轮严格遵守用户提供的《工作基线强制规则》。没有读取、没有构建、没有基于旧 handoff 源码：

```text
/home/user/herbcraft/herbcraft_source
```

本轮唯一源码基线是最新正确版本：

```text
/home/user/herbcraft_p1_3_release/herbcraft-source-p1.3-good-base-fixes.zip
```

已解压覆盖到：

```text
/home/user/herbcraft_work/herbcraft_source
```

后续所有检查、构建、smoke test、打包都在该目录完成。

## 用户重提的问题与本轮确认

### 1. Herbcraft 专属进度页背景缺贴图

已确认 root advancement：

```text
core/src/main/resources/data/herbcraft/advancement/root.json
```

使用：

```json
"background": "herbcraft:textures/gui/advancements/backgrounds/herbcraft.png"
```

已确认源码和 jar 内都有：

```text
assets/herbcraft/textures/gui/advancements/backgrounds/herbcraft.png
```

### 2. “女巫的手稿”是什么

该进度不是崩溃 bug。它是：

```text
herbcraft_alchemy:witch_knowledge
中文：女巫的手稿
```

含义：玩家发现女巫与草药炼金有联系。目前触发基础是获得 `witch_*` 安全女巫药水物品。

若作者觉得名称太像具体物品，后续可改为：

```text
巫酿笔记 / 女巫的药谱 / 女巫也识草
```

本轮未改名，避免进一步扰动测试文本。

### 3. Jade / 玉 已认识仍显示未知

本轮确认当前源码不是旧的客户端猜测版，而是服务端数据版：

```text
core/src/main/java/com/herbcraft/compat/jade/HerbNatureProvider.java
```

实现：

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

客户端从：

```text
accessor.getServerData()
```

读取，不再直接判断客户端玩家是否为 `ServerPlayer`。

同时 runServer smoke test 已确认 Jade 加载到 Herbcraft 插件：

```text
Start loading plugin from Herbcraft | 百草可食: com.herbcraft.compat.jade.HerbcraftJadePlugin
com.herbcraft.compat.jade.HerbcraftJadePlugin loaded
```

### 4. 百草经总纲与草药食用提示 raw key

已确认中英语言键存在：

```text
book.herbcraft.entry.overview.title
book.herbcraft.entry.overview.line1
book.herbcraft.entry.overview.line2
book.herbcraft.entry.overview.line3
book.herbcraft.entry.overview.line4
book.herbcraft.section.herbal.overview
```

已确认草药食用 ActionBar 键存在：

```text
message.herbcraft.herb_stack.stage_3
message.herbcraft.herb_stack.stage_5
message.herbcraft.herb_stack.stage_8
message.herbcraft.herb_stack.fade
message.herbcraft.herb_stack.overload
```

额外脚本检查输出：

```text
PASS - reported raw-key translations present and no old advancement.* keys
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

关键输出：

```text
PASS - catalyst mixin source/config/helper/built-jar checks passed.
PASS - P1 codex overview and unified advancement tree validation passed.
PASS - nature data/API/codex/Jade lightweight compatibility validation passed.
VERIFY OK: all built jars contain expected metadata/resources/classes and Java 25 bytecode.
```

Jar 内容确认：

```text
core jar:
  com/herbcraft/compat/jade/HerbNatureProvider.class
  assets/herbcraft/textures/gui/advancements/backgrounds/herbcraft.png
  data/herbcraft/herb_natures.json

alchemy jar:
  com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.class
```

## 实地 smoke test

### alchemy runServer

命令：

```bash
timeout 120 ./gradlew :alchemy:runServer --console=plain
```

结果达到：

```text
Done (8.652s)! For help, type "help"
```

并且 Jade / Herbcraft 插件加载成功。没有 Mixin prepare / ClassNotFound 错误。

### alchemy runClient

命令：

```bash
timeout 120 ./gradlew :alchemy:runClient --console=plain
```

结果加载到：

```text
herbcraft 1.1.5
herbcraft_alchemy 0.2.4
jade 26.1.0+fabric
```

随后因当前沙盒无图形环境，出现预期错误：

```text
Failed to initialize GLFW, X11: The DISPLAY environment variable is missing
```

这说明已越过 Fabric/Mixin/Jade 初始化阶段。该 headless 错误不是模组错误。

## 输出产物

输出目录：

```text
/home/user/herbcraft_p1_4_release/
```

包含：

```text
herbcraft-1.1.5.jar
herbcraft-alchemy-0.2.4.jar
herbcraft-cuisine-0.3.5.jar
herbcraft-1.1.5-0.2.4-0.3.5-p1.4-rigorous-recheck.zip
herbcraft-source-p1.4-rigorous-recheck.zip
SHA256SUMS.txt
```

## 进程清理

runServer / runClient 后均立即清理可能残留的 Java/Gradle/Minecraft 进程。最终无：

```text
GradleDaemon
KnotServer
KnotClient
net.fabricmc.devlaunchinjector
minecraft
```
