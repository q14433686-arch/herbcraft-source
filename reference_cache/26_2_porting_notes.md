# 26.2 / Fabric 0.19.3 迁移调查

调查日期：2026-07-19  
当前仓库基线：Minecraft `26.1.2` / Fabric Loader `0.19.3` / Fabric API `0.151.0+26.1.2` / Java `25`  

> 这里的“Fabric 0.193”应理解为 Fabric Loader `0.19.3`，不是 Fabric API 版本。Fabric API 需要另外指定 `0.x+26.2` 版本。

## 结论先行

**不是只改版本声明，但也不是一次大规模重写。**

当前源码已经完成了 26.1 最大的一轮迁移（Mojang official mappings、组件、`GuiGraphicsExtractor`、`RenderPipelines`、loot v3、数据驱动交易等），对 26.2 的最小功能迁移预计是：

1. 更新构建工具和依赖声明；
2. 把 Minecraft 26.2 的 `Minecraft` 屏幕管理调用迁移到新的 `Minecraft.gui`；
3. 用 26.2 依赖编译，检查 JEI/REI 适配器和 Mixin 目标；
4. 启动客户端/服务端做一次 Mixin 和数据加载验证。

**静态审计暂未发现需要改配方、交易、loot、组件、营养、炼金或渲染底层实现的明确变化。** 但在实际编译前，不能把 Mixin/第三方 viewer 兼容性承诺为零改动。

## 已查证的 26.2 工具链

Fabric 官方 26.2 迁移说明：

- <https://fabricmc.net/2026/06/15/262.html>

官方说明给出的要求/建议：

| 项目 | 当前 26.1.2 | 26.2 目标 | 判断 |
|---|---|---|---|
| Minecraft | `26.1.2` | `26.2` | 必改 |
| Fabric Loader | `0.19.3` | `0.19.3` | 保持，不是 `0.193` |
| Fabric Loom | `1.16-SNAPSHOT`（实际解析为 1.16.3） | `1.17`（仓库应使用 `1.17-SNAPSHOT`） | 必改 |
| Gradle Wrapper | `9.4.1` | `9.5.1` | 必改 |
| Java | `25` | `25` | 保持 |
| Fabric API | `0.151.0+26.1.2` | 调查日期可用的 `0.155.2+26.2` | 必改 |

Fabric 官方还说明了三类 26.2 变化：

- Loader `0.19.0` / Loom `1.17` 的 enum extension 能力；本项目没有使用；
- Fabric API 的 tag removal、Fluid Interaction API、attended client command；本项目没有使用；
- vanilla 的注册/数据生成 ID 拆分和 GUI/HUD 重组。

截至调查日，Fabric API 的 26.2 发布页可见 `0.155.2+26.2`：

- <https://modrinth.com/mod/fabric-api/version/0.155.2+26.2>
- <https://www.curseforge.com/minecraft/mc-mods/fabric-api>

Modrinth 页面在不同镜像/时间可能显示较旧版本；正式更新前以 Maven 能否解析和构建结果为准。

## 与当前源码的逐项对照

### 1. GUI/HUD 重组：确定有源码改动

Fabric 官方给出的例子是：

```java
// 26.1
Minecraft.getInstance().setScreen(screen);

// 26.2
Minecraft.getInstance().gui.setScreen(screen);
```

当前源码静态命中如下：

| 文件 | 当前用法 | 迁移判断 |
|---|---|---|
| `core/src/main/java/com/herbcraft/knowledge/client/HerbcraftCoreClient.java` | `context.client().screen`、`context.client().setScreen(...)`、`client.screen`、`client.setScreen(...)` | 确定要改到 26.2 `Gui` 屏幕访问/设置 API；这是唯一已经能从官方说明直接确认的功能性改动 |
| `core/src/main/java/com/herbcraft/knowledge/client/HerbNatureItemTooltips.java` | `Minecraft.getInstance().screen` | 需要改为 26.2 的 `Gui` 当前屏幕访问方式 |
| `core/src/main/java/com/herbcraft/nutrition/client/NutritionItemTooltips.java` | `Minecraft.getInstance().screen` | 需要改为 26.2 的 `Gui` 当前屏幕访问方式 |

精确 getter 名称应以 26.2 Loom mapped jar 编译错误/源码为准；不能仅凭 setter 示例猜测。当前使用 `Minecraft.getInstance().getResourceManager()`、`.level` 等非屏幕字段不属于官方列出的这次迁移。

### 2. 注册和数据生成：当前没有命中风险点

26.2 移除了 vanilla/Fabric 数据生成中的 `valueLookupBuilder`，并建议把 block ID 与 block 实体、item ID 与 item 实体分开。

本项目检查结果：

- 没有 datagen source set；
- 没有 `valueLookupBuilder`；
- 没有 `BlockIds`/`BlockItemIds`/`ItemIds` 相关代码；
- 内容是直接 `Registry.register(...)` + 已打包 JSON；
- 当前没有自定义 Block 注册表需要按新 ID 结构拆分。

因此本项目这一项预计**无需改**，不是因为 26.2 没变化，而是因为仓库没有使用受影响的 datagen API。

### 3. Vulkan/渲染：当前没有 raw OpenGL 风险

26.2 增加实验性 Vulkan 后端，使用 raw OpenGL 的模组必须迁移到 Blaze3D API。

当前代码审计结果：

- 没有 `RenderSystem`、`GL11`、`GlStateManager`、`BufferBuilder`、`VertexFormat` 等 raw OpenGL/低层顶点调用；
- Codex/Nutrition UI 使用的是 26.1 已迁移的 `GuiGraphicsExtractor` 和 `RenderPipelines.GUI_TEXTURED`；
- 没有 block/entity 自定义渲染器。

所以渲染层预计**不需要改**。仍需在 26.2 客户端以 OpenGL 默认后端和 Vulkan 实验后端各启动一次，至少确认 UI 没有运行时问题。

### 4. Fabric API 新增能力：当前没有使用

以下 26.2 相关能力在当前源码中没有调用，因此不需要为了升级主动新增适配：

- enum extensions；
- tag removal (`fabric:remove`)；
- Fluid Interaction API；
- `FabricClientCommandSource.attended()`。

已有的 `fabric-loot-api-v3`、`CreativeModeTabEvents`、payload networking、tooltip callback、resource reload listener 等用法没有被官方 26.2 文章列为移除项，但仍要通过编译验证最新 API。

### 5. Mixin：没有明确必改项，但必须做启动验证

当前 Mixin 目标包括 `Item.finishUsingItem`、`Item.releaseUsing`、`Player` 存档方法、`BrewingStandBlockEntity`、`Arrow`、`AreaEffectCloud`、`Witch`、`ItemCost` 等。官方 26.2 摘要没有点名这些方法发生变化。

因此静态判断是“暂不改”，但 Mixin 目标属于 Minecraft 内部实现，最终必须：

- 先以 26.2 mapped jar 编译；
- 再启动 `:core:runServer` / `:cuisine:runServer` 或客户端；
- 检查 mixin apply、`@Shadow` 字段/方法和注入 descriptor。

## 第三方 viewer 依赖：不能只沿用旧版本

当前 `build.gradle` 还声明了 26.1 线的可选开发依赖：

- Jade `26.1.0+fabric`；
- JEI `29.6.2.38`；
- REI `26.1.819`；
- Architectury `20.0.6`。

查到的 26.2 线候选版本如下（正式改动时仍以对应 Maven artifact 能解析为准）：

- Jade：`26.2.1+fabric`（Modrinth：<https://modrinth.com/mod/jade/version/26.2.1+fabric>）；
- JEI：`30.12.0.72` for Fabric 26.2（当前项目需要确认 `mezz.jei` 的 common/fabric API artifact 名称）；
- REI：`26.2.820`（<https://www.curseforge.com/minecraft/mc-mods/roughly-enough-items/files/all>）；
- Architectury：`21.0.4`（<https://www.curseforge.com/minecraft/mc-mods/architectury-api>）。

这意味着配置层至少要更新这些第三方版本；而 Alchemy 的三个 viewer adapter：

- `alchemy/src/main/java/com/herbcraft/alchemy/compat/jei/CoatingJeiPlugin.java`；
- `alchemy/src/main/java/com/herbcraft/alchemy/compat/rei/CoatingReiPlugin.java`；
- `alchemy/src/main/java/com/herbcraft/alchemy/compat/rei/CoatingReiCommonPlugin.java`；

必须随新 API 编译。若 JEI/REI 26.2 API 保持兼容，这三处可以不改源码；若接口签名变了，编译器会给出局部修复点。它们不能在“只改 Minecraft 版本声明”的方案里被忽略。

## 预计改动范围

### 最小必改集合

- `gradle.properties`：Minecraft、Loom、Fabric API、Gradle 相关版本；Loader 继续 `0.19.3`；可选 viewer 版本；
- `gradle/wrapper/gradle-wrapper.properties`：`9.4.1` -> `9.5.1`；
- 三个 `fabric.mod.json`：Minecraft/Fabric API/Jade 版本约束；
- 上面列出的 3 个 Core client Java 文件：迁移屏幕访问/设置；
- 构建后同步 release/docs/scripts 中仍写死 `26.1.2` 的版本声明。

### 编译/启动后可能追加

- 0–3 个 Alchemy JEI/REI adapter 文件；
- 个别 Mixin 的方法 descriptor 或 `@Shadow`；
- 不预期修改玩法规则、组件 schema、交易 JSON、配方 JSON、loot JSON、advancement 或贴图模型。

## 建议实施顺序

1. 先只更新构建工具和依赖，运行 `compileJava`，记录第一批错误；
2. 先修 26.2 GUI API，再修 JEI/REI 编译错误；
3. 运行完整 `build` 和现有 validators；
4. 运行服务端/客户端 smoke test，验证 mixin、资源和可选 viewer；
5. 最后批量同步文档与脚本版本字符串，并生成 26.2 迁移报告。

## 实施状态

作者已确认开始迁移。当前分支已经应用了构建配置、依赖和 GUI 屏幕管理调用的第一批 26.2 修改；完整 Java 编译和客户端/服务端启动验证仍待本地 Java 25 环境可用后执行。
