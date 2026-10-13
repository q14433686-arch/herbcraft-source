# 26.3 / Fabric 0.162.0 迁移调查与实施记录

调查日期：2026-10-13 ~ 2026-10-14  
迁移前基线：Minecraft `26.2` / Fabric Loader `0.19.3` / Fabric API `0.155.2+26.2` / Java `25` / Gradle Wrapper `9.5.1`  
迁移后基线：Minecraft `26.3` / Fabric Loader `0.19.3` / Fabric API `0.162.0+26.3` / Java `25` / Gradle Wrapper `9.6.0`

> 本文只收录**已被官方文档 / 上游源码 / 真实移植 PR 验证过**的结论。凡是「博客这么说」但没验证的，
> 一律标注证据强度。上游一手材料见文末「信息来源」。

## 结论先行

26.3 对 Herbcraft **不是版本号迁移，而是两次数据 schema 迁移 + 一次输入层迁移**：

1. **酿造改为数据驱动**（最大的一项）：vanilla `PotionBrewing` 注册表与 Fabric 的
   `FabricPotionBrewingBuilder` 被移除，所有 mix 必须写成 `minecraft:brewing` 配方 JSON；
   并且 26.3 **不再为已注册药水自动生成喷溅/滞留配方**，每个药水的容器转换也必须显式写。
   → 520 个配方 JSON（170 个 mix + 350 个容器转换，覆盖 175 个已注册药水），
   由 `scripts/generate_brewing_recipes.py` 生成。
2. **村民交易 / 物品修饰器 schema 改名**：`given_item_modifiers` → `given_item_modifier`、
   内联修饰器 `function` → `type`、`wants/gives/additional_wants` 的 `count` 与
   `max_uses`/`xp`/`reputation_discount` 变必填。旧写法**解析不报错、直接被忽略**。
   → 80 个交易文件由 `scripts/migrate_villager_trades_26_3.py` 迁移。
3. **LWJGL 切到 SDL3**：`org.lwjgl.glfw` 不在 classpath 上，键位常量统一走
   `InputConstants`，`InputConstants.Type.KEYSYM` → `KEYBOARD`。
   → 3 个 Core client 文件。

渲染底层（blaze3d → renderpearl）虽然是大改，但 Herbcraft 只用到 `InputConstants` 与
`RenderPipelines.GUI_TEXTURED` 常量，未命中破坏面（见下）。

## 一、已查证的 26.3 工具链

| 项目 | 26.2 | 26.3 | 证据 |
|---|---|---|---|
| Minecraft | `26.2` | `26.3`（data pack format `121.0`，协议 777） | minecraft.wiki `w/Java_Edition_26.3` |
| Fabric API | `0.155.2+26.2` | `0.162.0+26.3` | fabricmc.net 26.3 发布文 + `FabricMC/fabric` 分支 `26.3` 的 gradle.properties |
| Fabric Loader | `0.19.3` | `0.19.3`（玩家侧 0.19.5） | 同上 |
| Fabric Loom | `1.17-SNAPSHOT` | `1.17-SNAPSHOT`（上游用 `1.17.20`） | `FabricMC/fabric@26.3` |
| Gradle Wrapper | `9.5.1` | `9.6.0` | `FabricMC/fabric@26.3` wrapper |
| Java | `25` | `25` | 上游一致 |

可选 viewer 依赖已逐个核对 Maven 元数据 / 上游分支（不是沿用 26.2 号）：

| 依赖 | 26.2 | 26.3（本仓采用） | 核查方式 |
|---|---|---|---|
| Jade | `26.2.1+fabric` | `26.3.5+fabric` | `Snownee/Jade` 分支 `26.3-fabric` 的 `gradle.properties`：`mod_version=26.3.5` |
| JEI | `30.12.0.72` | `31.12.0.65`（26.3 通道最新） | `maven.blamejared.com/mezz/jei/jei-26.3-fabric-api/maven-metadata.xml` 最新版本即 `31.12.0.65` |
| REI | `26.2.820` | `26.3.824`（26.3 通道最新） | `maven.shedaniel.me/.../RoughlyEnoughItems-api-fabric/maven-metadata.xml` |
| Architectury | `21.0.4` | `22.0.5`（最新） | `maven.architectury.dev/.../architectury-fabric/maven-metadata.xml` |

> TaCZ_Refabricated_Unofficial 的 26.3 迁移报告记录 JEI `31.8.0.48` / REI `26.3.823` /
> Architectury `22.0.3`（核查日 2026-09-30，当时 JEI/PAL 还是 beta 通道）。本仓取的是
> 2026-10-12/13 复核后的更新小版本。

## 二、26.3 破坏性变更逐项对照

### 1. 酿造数据驱动（已实施，520 个配方）

证据链：

- NeoForged 26.3 primer：酿造改为数据驱动；「if you do not know a potion or container
  exists, the recipe will no longer work」；`MinecraftServer.potionBrewing` 被移除。
- fabricmc.net 26.3 发布文：`FabricPotionBrewingBuilder` 被移除（改为 JSON）。
- spawnguard 26.3 发布说明：「Minecraft 26.3 removed the runtime PotionBrewing registry …
  both loaders now ship `data/spawnguard/recipe/brewing/*.json`」。
- Taverns 1.3.0+26.3-neoforge：酿造配方从 config `brewing.json` 搬到数据包 recipe JSON。
- `FabricMC/fabric@26.3` 自带 `FabricBrewingProvider.java`（模组生成酿造配方的官方入口），
  其中 `addContainerTransformations()` 写死两条链：
  `POTION + gunpowder -> SPLASH_POTION`、`SPLASH_POTION + dragon_breath -> LINGERING_POTION`；
  其测试模组生成的 `recipe/brewing/{potion_haste_gunpowder,splash_potion_haste_dragon_breath,
  lingering_potion_water_simple_block,potion_water_simple_block}.json` 与本仓生成的 JSON
  **字段名与嵌套完全一致**（`type` / `input{item,potion_contents.potions}` /
  `reagent{item}` / `output{id,components."minecraft:potion_contents".potion}`），
  并且证实滞留转换的输入是**喷溅药水**而不是基础药水。

**关键判断（26.3 不再自动生成容器转换）**：minecraft.wiki《Brewing》History 记载
「26.3 snap3: Potions of Luck and splash potions of Luck cannot be brewed into splash and
lingering potions anymore」。Luck 是 vanilla 自己的药水——连它都失去自动转换，说明
**每个药水的喷溅/滞留配方都是一条独立配方**。因此本仓为全部 175 个自定义药水
（40 个精华各自的 基/澄清/浊化/长效/强化 变体共 168 个 + 8 个特殊药水）
显式生成 gunpowder / dragon_breath 容器转换，而不是依赖任何「泛型转换」。

配方 schema（minecraft.wiki `w/Recipe_(Java_Edition)` + `w/Data_component_predicate`）：

```json
{
  "type": "minecraft:brewing",
  "input":     { "item": "minecraft:potion", "potion_contents": { "potions": "minecraft:awkward" } },
  "reagent":   { "item": "herbcraft:essence_dandelion" },
  "output":    { "id": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "herbcraft:dandelion" } } }
}
```

注意谓词侧是**复数 `potions`**，输出组件侧是**单数 `potion`**——写反了不报错但不匹配。

实施：

- `alchemy/src/main/resources/data/herbcraft/recipe/brewing/` 520 个文件：
  `base_<id>`(40) / `clarified_<id>`(31) / `murky_<id>`(17) / `long_<id>`(40) /
  `strong_<id>`(40，wither_rose 的特例输出是 `herbcraft:wither_venom_iii`，
  文件名 `strong_wither_rose.json`) + `undead_venom.json` / `cardiac_toxin.json` 两个特殊 mix
  （共 170 个 mix）+ 每个药水的 `splash_<potion>.json`(175) / `lingering_<potion>.json`(175)。
- `AlchemyRegistries` 删除 mix 注册，只保留配方引用的药水 ID 与说明注释。
- `scripts/generate_brewing_recipes.py`（check / `--write`）与
  `scripts/validate_brewing_recipes.py`（逐字比对 + Java 侧已移除 API 守门 +
  `BrewingStandCatalystMixin` 仍在 mixin 配置中）。
- 自定义酿造 `HiddenCuisineCatalystBrewing` / `DistilledEssenceBrewing` 走
  `BrewingStandBlockEntity#doBrew` HEAD 注入，与数据驱动配方**互不影响**：
  `FabricMC/fabric@26.3` 自己的 `BrewingStandBlockEntityMixin` 仍然 `@Redirect` 到
  `method = "doBrew"`，说明该方法名/签名在 26.3 未变；同时全仓无 `PotionBrewing` 引用。
  同一个 Fabric mixin 还证明 26.3 的 `Item#getCraftingRemainder()` 返回
  `ItemStackTemplate`（与本仓 `SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate...)` 用法一致）。

### 2. 村民交易 / 物品修饰器 schema（已实施，80 个文件）

证据：minecraft.wiki《Villager trade definition》History（26.3 snap4 改字段名、
pre1 改必填）、《Item modifier》Format 与 26.3 snap4 条目、ColossalChests PR #212
（同一次改动的 loot 表侧，明确说旧格式「parses without any error and is then ignored」）。

| 变更 | 旧（26.2） | 新（26.3） | 不改的后果 |
|---|---|---|---|
| 交易修饰器字段 | `given_item_modifiers` | `given_item_modifier` | 静默丢失：村民卖空药水 |
| 内联修饰器类型键 | `"function": "minecraft:set_potion"` | `"type": "minecraft:set_potion"` | 静默丢失（同上） |
| `wants/gives/additional_wants.count` | 可选 | 必填（inline `context_int_provider`） | 交易加载失败 |
| `max_uses` / `xp` | 可选 | 必填 | 交易加载失败 |
| `reputation_discount` | 可选 | 必填（inline `context_float_provider`） | 交易加载失败 |

纯数字（`16.0`、`0.05`）仍是合法的 inline 常量提供器，因此只补缺失的 `count`，不改动已有数值。

`minecraft:set_potion`（字段 `id`）与 `minecraft:set_components`（字段 `components`）
在 26.3 均未改名，仅外层 `function` → `type`。

实施：`scripts/migrate_villager_trades_26_3.py`（check / `--write`，可重复运行），
并同步修正 `scripts/generate_essence_resources.py`（ Wanderer 交易补 `count`）与
`scripts/validate_alchemy_trade_loot_arrow_adaptation.py`（改读新字段名）。

### 3. SDL / 输入常量（已实施）

证据：fabricmc.net 26.3 发布文（GLFW → SDL，`InputConstants`）、NeoForged primer
（`KeyEvent.key` = SDL scancode，`keycode()` 由 `scancode()` 改名）、ChestTracker PR #53
（真实 26.3 移植：`GLFW.GLFW_MOUSE_BUTTON_2` → `InputConstants.MOUSE_BUTTON_RIGHT`）、
ImprovedMaps #17 / BetterHitreg #30 / ParkourCalculatorMod #505 / SoundTweaks #15。

本仓改动（3 处，`org.lwjgl.glfw` 全部出 classpath）：

| 文件 | 改动 |
|---|---|
| `core/.../client/HerbcraftCoreClient.java` | `InputConstants.Type.KEYSYM` → `KEYBOARD`；`GLFW.GLFW_KEY_N` → `InputConstants.KEY_N` |
| `core/.../client/CodexScreen.java` | `keyPressed` 比较 `InputConstants.KEY_T` |
| `core/.../nutrition/client/NutritionPanelScreen.java` | `keyPressed` 比较 `InputConstants.KEY_N` |

`CodexScreen.mouseClicked` 用的是 `event.button() == 0`（左键）与 `MouseButtonEvent`，
26.3 未变，保持原样（不引入未经证实的 `InputConstants.MOUSE_BUTTON_LEFT` 常量）。

### 4. 渲染 / GUI：审计过，未命中

26.3 渲染底层重构（`com.mojang.blaze3d.*` → `com.mojang.renderpearl.*`、
`RenderPipeline` 搬到 `com.mojang.renderpearl.api.pipeline`、`PoseStack#mulPose` 只收矩阵、
`RenderTypes.textBackground()` 删除、shader 改 shaderc/SPIR-V）。

本仓审计结果：

- 没有 `RenderSystem` / `GL11` / `BufferBuilder` / 自定义 `RenderType` / shader；
- GUI 走 26.1 已迁移的 `GuiGraphicsExtractor` + `RenderPipelines.GUI_TEXTURED`，
  26.3 两者均在（ChestTracker 26.3 移植仍在用 `GuiGraphicsExtractor`；
  `GuiGraphicsExtractor#tooltip()` 只是多了一个 `extraSpaceAfterFirstLine` 参数，本仓用的是
  `setTooltipForNextFrame`，签名未变）；
- 没有 block/entity 自定义渲染器。

### 5. 已审计且未命中的其它 26.3 变更

| 变更 | 本仓情况 |
|---|---|
| `FriendlyByteBuf#readMap/writeMap` 移除 | 无调用 |
| `Util.getPlatform().openUri()` → `Blaze3D.openPath()` | 无调用 |
| `AbstractPackResources`/`CompositePackResources` 改名 | 无调用 |
| 方块 `code()`/`codec()` 移除 | 无调用 |
| `PushReaction` 变更 | 无调用 |
| `RegistryLayer#WORLDGEN` → `WORLD` | 无调用 |
| `BundleContents#itemCopyStream` → `itemCopies`、`SlotDisplay.TagSlotDisplay(HolderSet<Item>)` | 无调用 |
| loot 表 schema（`functions`→`modifier`、`condition` 单数、`name`→`items`） | 全仓无 loot 表 JSON；但**代码里有 loot 注入**，见下方第 7 节 |
| advancement schema（entity predicate `type` 必填、`nbt` → `minecraft:nbt`、`enter_block.recipe`→`recipes`） | 35 个 advancement 全部只用 `minecraft:impossible` |
| 物品标签 `#dowses_campfires` → `#douses_campfires` | 未引用（只引用 `#minecraft:leaves/swords/spears/shovels/pickaxes/hoes/axes`，均未改名） |
| Explorer Map 物品改名 / item sprite 改名 | 未引用 |
| `#minecraft:spears` | 25w41a 起存在，26.3 仍在 |
| 26.3 起 lang 文件严格解析（一处语法错误丢整组语言） | 6 个 lang 文件已由 `check_lang_diff.py` + 严格 JSON 解析覆盖 |

### 6. Mixin

`herbcraft.mixins.json`（7 个）与 `herbcraft_alchemy.mixins.json`（10 个）的目标在
26.3 官方摘要里没有被点名改动；`BrewingStandCatalystMixin` 的 `doBrew` HEAD 注入仍需要
（自定义催化剂/蒸馏酿造不走数据驱动配方）。**启动期 apply 仍待实机验证。**

### 7. loot 注入代码（CI 实测发现，已修）

第 5 节说「全仓无 loot 表」只对**数据**成立；`core/CodexLoot` 与
`alchemy/AlchemyCodexHooks` 在**代码**里用 `LootTableEvents.MODIFY` 往原版表注入
pool，走的是 Java builder API，26.3 在这里改了三处（`:alchemy:compileJava` 三个错误，
`:core`/`:cuisine` 一次通过）：

| 26.2 写法 | 26.3 现状 | 处理 |
|---|---|---|
| `import ...loot.providers.number.ConstantValue` | number provider 拆成 `providers/number/ints`（`ContextIntProvider`）与 `providers/number/floats`（`ContextFloatProvider`）；`ConstantValue` 只剩 floats 版 | 计数改用 `providers.number.ints.ContextIntProviders.exactly(int)`，返回 `Holder<ContextIntProvider>` |
| `entries.LootPoolSingletonContainer.Builder<?>`（`LootItem.lootTableItem(item)` 的声明类型） | 该 builder 类型已不在 `entries` 包 | 局部变量改用 `var` 推断；`LootItem.lootTableItem(Item)` 本身仍在，builder 的 `apply(LootItemFunction)` 也仍在（`:core` 的 `CodexLoot.addTatteredPagePool` 链式 `.apply(...)` 一次编译通过即为证据） |
| `SetItemCountFunction.setCount(ConstantValue.exactly(n))` | `set_count` 是 int 语义字段 | 改传 `ContextIntProviders.exactly(n)`（`Holder<ContextIntProvider>`） |

判型依据（为什么 count 用 int provider 而不是 floats 的 `ConstantValue`）：
26.3 的 loot 数值字段里 `rolls`/`bonusRolls` 已改为 `Holder<ContextIntProvider>`
（`TheMarstonConnell/randomloot` #145/#147/#148/#151/#153/#154/#155/#162 均实测），
其余 loot function 的数值字段（`levels`/`limit`/`amount`/`damage`/`numberOfDyes`/`duration`）
也全是 int 语义；NeoForged 26.3 primer「Splitting Numbers into Floats and Ints」确认
`BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE` 拆成 `CONTEXT_FLOAT_PROVIDER_TYPE` /
`CONTEXT_INT_PROVIDER_TYPE`，provider 变成可注册（因此 API 侧是 `Holder`）。
`:core` 用的面（`LootPool.lootPool()`、`when(...)`、`add(LootPoolEntryContainer)`、
`tableBuilder.pool(...)`、`LootItemRandomChanceCondition.randomChance`）全部未变。

## 三、借鉴的其它仓库移植手册

| 仓库 / PR | 借鉴点 |
|---|---|
| `q14433686-arch/TaCZ_Refabricated_Unofficial` `docs/PORTING_NOTES.md` | 「同一个 API 语义变了，旧版能编译、行为静默改变」是移植期最难发现的 bug 类型；据此把本仓所有**旧 schema 静默失效**的面（交易修饰器、酿造配方）做成生成器 + 校验器，而不是手改 |
| 同上 `docs/investigations/SCOPE_26_3_VS_26_2_DELTA_ANALYSIS_2026_09_20.md` | 26.2→26.3 渲染底层重构的逐项差异（blaze3d 三分、`GlRenderPipeline#info()` 删除、shader 方言） |
| 同上 `docs/CHANGELOG_26_3_R1.md` | 真实 26.3 移植清单：`InputConstants`、`Blaze3D.openUri`、`AbstractPackMetadataResources`、`Pack.ResourcesSupplier`、方块 `codec()` 移除、loot 表 schema 迁移、26.3 起 lang 文件严格解析 |
| `CyclopsMC/CommonCapabilities#50`、`IntegratedCrafting#243` | `PotionBrewing` 消失后的替代面：`BrewingRecipe`/`BrewingInput`/`RecipeType.BREWING`、`Level#potionBrewing()` 移除、`RecipePropertySet.BREWING_REAGENTS` |
| `ZsoltMolnarrr/Taverns` 1.3.0+26.3 | 独立佐证：酿造从 config 搬数据包 |
| `ColossalChests#212`、`Castle#53` | loot/物品修饰器旧格式「解析不报错、被忽略」的第一手描述 |
| `ponuing/ChestTracker#53` | `GuiGraphicsExtractor` 在 26.3 仍存在、`tooltip()` 多一个参数、鼠标常量走 `InputConstants` |
| `MCreator#6657` | 26.3 配方侧的另一个真实移植（brewing 进 datapack 生成器） |
| `Snownee/Jade@26.3-fabric`、`shedaniel/RoughlyEnoughItems@26.3`、`mezz/JustEnoughItems@26.3` | 逐个接口核对可选兼容层的 26.3 API（见下） |

## 四、可选兼容层 API 核对（已逐接口比对上游 26.3 分支源码）

- **Jade**（`26.3-fabric`）：`IWailaPlugin.register/registerClient`、
  `IWailaCommonRegistration.registerBlockDataProvider/registerEntityDataProvider`、
  `IWailaClientRegistration.registerBlockComponent/registerEntityComponent`、
  `IServerDataProvider.appendServerData/shouldRequestData/getUid`、
  `ITooltip.add(Component)`、`Identifier`（26.x 的 Mojang 名，非 `ResourceLocation`）
  全部与 `com.herbcraft.compat.jade` 的用法一致。
- **REI**（`26.3`）：`DefaultBrewingDisplay(EntryIngredient, EntryIngredient, EntryIngredient)`、
  `DefaultSmithingDisplay(List, List, Optional<SmithingRecipeType>, Optional<Identifier>)`、
  `EntryStacks.of(ItemStack/ItemLike)`、`EntryIngredient.of/empty`、
  `DisplayConsumer.add(Display)`、`ItemComparatorRegistry.register(EntryComparator, Item...)`、
  `REICommonPlugin.registerItemComparators` 全部存在且签名一致。
- **JEI**（`26.3`）：`IJeiBrewingRecipe`（`getPotionInputs/getIngredients/getPotionOutput/
  getBrewingSteps/getUid`）、`ISimpleRecipeManagerPlugin<T>`、
  `ISmithingCategoryExtension<R>`、`IIngredientAcceptor<THIS>` 与
  `alchemy/.../compat/jei/CoatingJeiPlugin.java` 的实现一一对应。
  （`IVanillaCategoryExtensionRegistration#getSmithingCategory`、
  `IAdvancedRegistration#addSimpleRecipeManagerPlugin`、`IRecipeRegistration#addRecipes`、
  `ISubtypeRegistration#registerSubtypeInterpreter` 待编译期最终确认。）

## 五、实施状态（2026-10-14）

已完成：

- 工具链与 5 个 `fabric.mod.json`、README「Locked environment」；`check_version_consistency.py` 23/23 通过；
- 520 个酿造配方 + 生成器/校验器；
- 80 个村民交易迁移 + 迁移脚本/生成器/旧校验器同步；
- 3 处 SDL 输入常量；
- 顺手修了 `validate_codex_payload_separation.py` 里两条 26.1 时代的失效字符串
  （`Minecraft.screen` → `gui.screen()`，该校验器在 26.2 线上就已经是假红）；
- 沙箱内可跑的 60+ 个静态校验器全绿（PIL 依赖已装；需要 built jar / vanilla client jar 的
  4+2 个仍依赖 Gradle 构建，本地到不了 `maven.fabricmc.net`）。

待办：

- **编译验证**：`docs/ci/` 三件套（build / compile-check / consistency）已就绪但尚未上线
  （沙箱 GitHub 凭据没有 `workflows` 权限）。上线后 `arena/**` 分支的编译日志会回推到
  `build-reports/compile-java.log`。
- 实机 smoke test：mixin apply、酿造台 UI/JEI/REI 三类显示、村民交易、中英语言。

## 信息来源

- <https://fabricmc.net/2026/09/15/263.html>（26.3 移除项：`FabricPotionBrewingBuilder`、`CompostingChanceRegistry`、`FuelRegistry`、`Strippable/Tillable/FlattenableBlockRegistry`；GLFW→SDL；Loom 1.17 / Gradle 9.6.0）
- <https://docs.neoforged.net/primer/docs/26.3/>（官方 26.2→26.3 primer，Mojang mappings）
- <https://minecraft.wiki/w/Java_Edition_26.3>、`w/Villager_trade_definition`、`w/Item_modifier`、`w/Recipe_(Java_Edition)`、`w/Data_component_predicate`、`w/Brewing`
- <https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3> 与 snapshot 3/4/10 说明
- `FabricMC/fabric@26.3`、`Snownee/Jade@26.3-fabric`、`shedaniel/RoughlyEnoughItems@26.3`、`mezz/JustEnoughItems@26.3`（`gh api` 直读）
- Maven 元数据：`maven.blamejared.com`（JEI）、`maven.shedaniel.me`（REI）、`maven.architectury.dev`（Architectury）
- 移植 PR：`CyclopsMC/CommonCapabilities#50`、`CyclopsMC/IntegratedCrafting#243`、`CyclopsMC/ColossalChests#212`、`Avuorina/Castle#53`、`ponuing/ChestTracker#53`、`MCreator/MCreator#6657`
- `q14433686-arch/TaCZ_Refabricated_Unofficial`（CI 三件套 + `docs/PORTING_NOTES.md` + 26.3 调查报告/变更日志）
- `ZsoltMolnarrr/Taverns` 1.3.0+26.3-neoforge、spawnguard 26.3-1.4.0
