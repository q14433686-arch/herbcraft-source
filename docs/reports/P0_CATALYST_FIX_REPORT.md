# P0 Catalyst / Source-Jar Sync Fix Report

日期：2026-06-12

## 范围

本轮只处理已确认的 P0：

1. `herbcraft_alchemy.mixins.json` 引用 `BrewingStandCatalystMixin`，但源码中缺失该类，导致从源码构建后的 alchemy jar 启动时 Mixin prepare 崩溃。
2. `HiddenCuisineCatalystBrewing` 已存在但没有接入炼药台实际 brew 流程，百花宴 / 剧毒蛊催化无法真正生效。
3. 构建并验证三模块 jar。

未处理：进度树、百草经总纲、菜品数值再平衡等 P1/P2 内容。

## 环境

- Minecraft: `26.1.2`
- Fabric Loader: `0.19.3`
- Fabric API: `0.151.0+26.1.2`
- Java: Temurin OpenJDK `25.0.3`
- Gradle Wrapper: `9.4.1`
- Loom: `1.16.3`

JDK 已放置在：

```text
/home/user/jdks/jdk-25
```

为满足动物标签校验脚本，已保留/建立客户端 jar 缓存入口：

```text
/home/user/cache/minecraft/client-26.1.2.jar -> /home/user/.gradle/caches/fabric-loom/26.1.2/minecraft-client.jar
```

## 修改文件

新增：

```text
alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java
scripts/validate_catalyst_mixin.py
```

更新：

```text
BUGS_TODO.md
PROJECT_STATE.md
```

## 修改内容

`BrewingStandCatalystMixin` 现在接入 `BrewingStandBlockEntity` 三处逻辑：

1. `canPlaceItem(...)`
   - 允许自动化/容器逻辑把 `hundred_flower_feast` / `deadly_hodgepodge` 放入材料槽。
2. 私有静态 `isBrewable(...)`
   - 当 `HiddenCuisineCatalystBrewing.isBrewable(items)` 为真时，使炼药台认为该组合可酿造。
3. 私有静态 `doBrew(...)`
   - 当材料是隐藏菜催化剂且底部药水可升级时，取消原版 `PotionBrewing.mix(...)` 路径，改走 `HiddenCuisineCatalystBrewing.brew(items)`。
   - 该路径复制输入药水栈，只覆写 `herbcraft_alchemy:potion_quality = EXCEPTIONAL`，因此会保留已有组件，如 `refinement_bonus` / `advanced_potion_initialized`。
   - 同时播放原版炼药完成事件 `levelEvent(1035, pos, 0)`。

## 构建验证

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew build --rerun-tasks --console=plain
```

结果：

```text
BUILD SUCCESSFUL
```

## 服务端 smoke test

### alchemy

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH timeout 180 ./gradlew :alchemy:runServer --console=plain
```

结果：

- 修复前复现：Mixin prepare 崩溃，提示 `BrewingStandCatalystMixin` not found。
- 修复后：服务端加载 `herbcraft 1.1.5` 与 `herbcraft_alchemy 0.2.4`，并达到：

```text
Done (8.984s)! For help, type "help"
```

### cuisine

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH timeout 180 ./gradlew :cuisine:runServer --console=plain
```

结果：

- 服务端加载 `herbcraft 1.1.5` 与 `herbcraft_cuisine 0.3.5`。
- `Cuisine` 初始化显示注册 `32` 个菜品和百草盅。
- 达到：

```text
Done (8.132s)! For help, type "help"
```

## 静态验证

已运行并通过：

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
python3 scripts/verify_built_jars.py
```

`verify_built_jars.py` 输出：

```text
VERIFY OK: all built jars contain expected metadata/resources/classes and Java 25 bytecode.
```

手动 jar 检查：

```text
alchemy/build/libs/herbcraft-alchemy-0.2.4.jar
  com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.class
  herbcraft_alchemy.mixins.json
```

## Release artifacts

已复制到：

```text
/home/user/herbcraft_p0_fix_release/
```

包含：

```text
herbcraft-1.1.5.jar
herbcraft-alchemy-0.2.4.jar
herbcraft-cuisine-0.3.5.jar
herbcraft-1.1.5-0.2.4-0.3.5-p0-catalyst-fix.zip
herbcraft-source-p0-catalyst-fix.zip
SHA256SUMS.txt
```

SHA256：

```text
d3314516ca0c447d17f31beb6813b4025012ccc68d7cde7748c2e1138804e84c  herbcraft-1.1.5.jar
9a3243a0a3db2ec84eb621e5f9b7a076aec15d376dba5763a11f662b25766ddb  herbcraft-alchemy-0.2.4.jar
2824485008dc0d8185e5694f95e7ffbec36f88d7df7e335c75e53b1fa9c9d73c  herbcraft-cuisine-0.3.5.jar
# source zip hash is listed in /home/user/herbcraft_p0_fix_release/SHA256SUMS.txt
```

## 尚未处理 / 下一轮建议

1. 进度树仍是 P1：当前 advancement 文件多数仍为 `minecraft:impossible` 且无 parent，虽然数量校验通过，但实际树结构不可信。
2. `百草经总纲` 仍需单独确认/实现：当前 core 的 `HerbalChapter` 未看到固定 overview entry 注册。
3. 可追加一个更严格的 catalyst 静态/运行时验证脚本，专门断言：
   - mixin json 引用类全部存在；
   - `BrewingStandCatalystMixin` 注入 `isBrewable` / `doBrew`；
   - `HiddenCuisineCatalystBrewing.brew` 只覆写品质组件，不重置其他组件。
