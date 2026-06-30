# P1.1：进度背景贴图、识性系统一期、Jade 轻提示适配报告

日期：2026-06-12

## 结论

已修复用户本地发现的 Herbcraft 专属进度页背景缺失贴图问题，并在 core 中完成“识性 / 性味”一期系统；同时按当前环境联网核查 Jade 版本后，加入 Jade 轻提示兼容入口。

本轮没有长时间运行 `runServer`，避免再次出现服务端进程被 timeout 杀掉后残留并占用 `session.lock` 的问题。验证方式为：完整构建、静态脚本、jar 内容检查、最后停止 Gradle daemon 并复查残余进程。

## 1. 进度背景贴图修复

新增贴图：

```text
core/src/main/resources/assets/herbcraft/textures/gui/advancements/backgrounds/herbcraft.png
```

修改 root advancement：

```text
core/src/main/resources/data/herbcraft/advancement/root.json
```

现在 root 使用：

```json
"background": "herbcraft:textures/gui/advancements/backgrounds/herbcraft.png"
```

这应修复游戏内 Herbcraft 专属进度页背景紫黑缺失贴图的问题。

## 2. 关于“女巫的手稿”

这不是崩溃型 bug，而是上一轮 P1 中预留的女巫/炼金联动进度：

```text
herbcraft_alchemy:witch_knowledge
中文：女巫的手稿
```

当前含义是：“玩家发现女巫与草药炼金有关”。现阶段基础触发是获得 `witch_*` 安全女巫药水物品时授予。

如果作者觉得这个名字太像一个具体物品，后续可以改名为：

- `女巫的药谱`
- `巫酿笔记`
- `女巫也识草`

本轮未改名，以免影响你正在测试的进度文本。

## 3. 识性 / 性味系统一期

新增数据：

```text
core/src/main/resources/data/herbcraft/herb_natures.json
```

当前覆盖 96 个 `herbs.json` 条目。每个条目包含：

```json
{
  "temperature": "neutral|cool|cold|warm|hot",
  "flavors": ["sweet", "bitter", "..."],
  "traits": ["clearing", "floral", "..."]
}
```

新增 core 子系统：

```text
core/src/main/java/com/herbcraft/nature/NatureProfile.java
core/src/main/java/com/herbcraft/nature/HerbNatureRegistry.java
core/src/main/java/com/herbcraft/api/HerbNatureAPI.java
```

设计边界：

- 不新增第四 jar；
- 不让 core 依赖 alchemy/cuisine；
- 性味目前只作为信息层，不参与数值计算；
- 缺 nature 时有 fallback：`neutral + bland`，不会崩溃。

## 4. 百草经性味展示

修改：

```text
core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java
```

现在百草经草药条目：

- 听闻：显示性味 + 模糊诗意提示；
- 谙熟：显示性味 + 原有营养/效果数值；
- 未知：仍不显示具体性味，避免 Jade/百草经提前剧透。

新增语言键包括：

```text
nature.herbcraft.line
nature.herbcraft.temperature.*
nature.herbcraft.flavor.*
nature.herbcraft.hint.*
```

## 5. 水碗 Tooltip 提示

修改：

```text
cuisine/src/main/java/com/herbcraft/cuisine/client/CuisineTooltips.java
```

新增 water_bowl 提示：

```text
盛水以调性，宜入药膳，不宜乱炖。
```

英文：

```text
Water tempers the herbs — suited for cuisine, not hodgepodge.
```

## 6. Jade 版本核查与兼容入口

联网核查结果：Modrinth 上 Jade 的 Fabric 26.1 线存在 `26.0.4+fabric`，页面标注支持 Fabric / Quilt 与 26.1，发布日期为 2026-03-24。Jade 官方文档说明 Fabric 下需要在 `fabric.mod.json` 添加 `jade` entrypoint，并实现 `IWailaPlugin`，在 `registerClient` 注册 component provider。

本轮加入可选依赖：

```gradle
compileOnly "maven.modrinth:jade:26.0.4+fabric"
```

新增：

```text
core/src/main/java/com/herbcraft/compat/jade/HerbcraftJadePlugin.java
core/src/main/java/com/herbcraft/compat/jade/HerbNatureProvider.java
```

修改：

```text
core/src/main/resources/fabric.mod.json
```

加入：

```json
"entrypoints": {
  "jade": ["com.herbcraft.compat.jade.HerbcraftJadePlugin"]
},
"suggests": {
  "jade": ">=26.0.4"
}
```

当前 Jade 显示策略：

- 只显示轻提示；
- 不显示完整药效数值；
- 未知时显示“草木有性，尚未识得”；
- 可访问玩家知识状态时，听闻/谙熟显示性味；
- 谙熟时追加“可食，可入盅”。

注意：Jade 的玩家个人知识状态更理想的实现方式是 server data provider 精确同步。本轮先完成轻提示入口与编译适配；后续如你实测发现 Jade 里永远只显示未知，我们再补 Jade server data provider。

## 7. P0 / P1 防回退

本轮确认保留：

```text
alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java
scripts/validate_catalyst_mixin.py
scripts/validate_p1_codex_advancements.py
```

`BrewingStandCatalystMixin.class` 已存在于新构建 alchemy jar。

## 8. 构建与验证

构建命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew build --rerun-tasks --console=plain
```

结果：

```text
BUILD SUCCESSFUL
```

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

## 9. 输出产物

输出目录：

```text
/home/user/herbcraft_p1_1_release/
```

包含：

```text
herbcraft-1.1.5.jar
herbcraft-alchemy-0.2.4.jar
herbcraft-cuisine-0.3.5.jar
herbcraft-1.1.5-0.2.4-0.3.5-p1.1-nature-jade-background.zip
herbcraft-source-p1.1-nature-jade-background.zip
SHA256SUMS.txt
```

## 10. 本轮保留的已知后续项

1. Jade server data provider：如果实测玩家知识状态不能在 Jade 客户端正确识别，需要补充。
2. `女巫的手稿` 是否改名：等待作者确认语感。
3. `feed_cow_grass` / `villager_trade_herbcraft` / `survive_witch_brew` 的精确触发仍可继续细化。
