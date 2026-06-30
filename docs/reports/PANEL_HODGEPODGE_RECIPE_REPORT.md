# PANEL_HODGEPODGE_RECIPE_REPORT

日期：2026-06-12

## 联网/官方环境核对

- Fabric 26.1 文档确认本环境使用 Java 25、Gradle 9.4+、`net.fabricmc.fabric-loom`、不再使用旧 mappings/remapJar。
- Fabric 26.1 发布说明确认 26.1 recipe serializer 已简化为 `MapCodec + StreamCodec`。当前 `HodgepodgeRecipe` 的 serializer 正是 `new RecipeSerializer(MapCodec.unit(HodgepodgeRecipe::new), StreamCodec.unit(new HodgepodgeRecipe()))`，因此实际 recipe JSON 只需要声明：

```json
{
  "type": "herbcraft_cuisine:hodgepodge"
}
```

## 任务 A：百草经面板半透明化

修改文件：

```text
core/src/main/java/com/herbcraft/knowledge/client/CodexScreen.java
```

已改为：

- 全屏遮罩：`0x80000000`
- 主面板：`0x90181818`
- 标题栏：`0xA0183018`
- 左侧目录：`0xA0101010`
- 分隔线：`0xD0D0D0D0`
- hover：`0xB0303030`
- selected：`0xB0456045`

保留：

- `isPauseScreen() == false`
- 左侧折叠 / 展开
- 左侧省略号和 tooltip
- 右侧自动换行
- 右侧滚动
- scissor 裁剪

## 百草盅核查报告

1. 当前百草盅物品 ID：

```text
herbcraft_cuisine:hodgepodge
```

2. 当前是否存在合成配方：

此前 jar 中存在 `HodgepodgeRecipe` 自定义 recipe 类和 serializer，但没有实际 recipe JSON。也就是说，自定义配方能力存在，但没有一条数据驱动 recipe 实例被加载。

3. 当前配方类型：

```text
自定义 recipe type / serializer: herbcraft_cuisine:hodgepodge
```

4. 当前 recipe 是否能写入原料快照：

能。`assemble(...)` 会写入：

```text
herbcraft_cuisine:hodgepodge_contents = [item id list]
```

5. 当前生存模式下是否真能合成：

修复前大概率不能，因为缺少实际 recipe JSON。修复后已新增：

```text
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/hodgepodge.json
```

内容：

```json
{
  "type": "herbcraft_cuisine:hodgepodge"
}
```

6. 根因：

只注册了自定义 recipe serializer，但缺少实际 recipe JSON。

7. 修复方式：

- 新增 `hodgepodge.json`，让 recipe manager 加载自定义配方。
- 新增 `core/src/main/resources/data/herbcraft/tags/item/herb.json`。
- `HodgepodgeRecipe.matches(...)` 现在同时要求：
  - 1 个碗；
  - 3~4 个原料；
  - 原料在 `#herbcraft:herb` 标签内；
  - 原料也必须能被 `HerbcraftAPI.getDefinition(item)` 识别。

这样既满足文档的 tag 前提，也避免 tag 与 herbs.json 未来不同步时错误接受没有 Herbcraft 定义的物品。

## 测试

### 静态校验

```bash
./scripts/validate_panel_hodge_recipe.py
```

结果：

```text
All panel translucency and hodgepodge recipe checks passed.
```

### 构建

```bash
JAVA_HOME=/home/user/jdks/jdk-25 /home/user/gradle/gradle-9.4.1/bin/gradle clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### 服务端 smoke

尝试运行 `:cuisine:runServer`，Fabric Loader 能识别 `herbcraft 1.1.5` 与 `herbcraft_cuisine 0.3.5`，但本沙盒环境启动极慢，300 秒超时前尚未走到 recipe manager 输出。因此本轮硬验收采用构建 + 静态校验 + jar 内容检查。

## 产物

```text
/home/user/herbcraft/release_panel_hodge_recipe/
```

- `herbcraft-1.1.5.jar`
- `herbcraft-alchemy-0.2.4.jar`
- `herbcraft-cuisine-0.3.5.jar`
- `SHA256SUMS.txt`
