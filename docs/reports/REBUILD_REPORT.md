# Herbcraft 反编译与源码重建报告

日期：2026-06-12

## 已完成

1. 已阅读用户提供文档：
   - `/home/user/uploads/Herbcraft 当前阶段补.txt`
   - `/home/user/uploads/Herbcraft Alche.txt`
2. 已联网核对 Fabric Wiki / Fabric Docs / Fabric 26.1 说明。
3. 已克隆仓库：`https://github.com/q14433686-arch/herbcraft/tree/main`
4. 已反编译仓库内三个 jar：
   - `herbcraft-1.1.0 (2).jar`
   - `herbcraft-alchemy-0.2.0 (2).jar`
   - `herbcraft-cuisine-0.3.0 (3).jar`
5. 已基于反编译结果重建多模块源码工程：`/home/user/herbcraft/rebuilt`
6. 已配置 26.1.2 环境：
   - Minecraft `26.1.2`
   - Fabric Loader `0.19.3`
   - Fabric API `0.151.0+26.1.2`
   - Java `25`
   - Gradle Wrapper `9.4.1`
   - Fabric Loom `1.16-SNAPSHOT`（解析为 1.16.3）
7. 已完成一次完整构建验证：`JAVA_HOME=/home/user/jdks/jdk-25 ./gradlew build`，结果 `BUILD SUCCESSFUL`。

## 源码模块

- Core：`rebuilt/core/src/main/java` + `rebuilt/core/src/main/resources`
- Alchemy：`rebuilt/alchemy/src/main/java` + `rebuilt/alchemy/src/main/resources`
- Cuisine：`rebuilt/cuisine/src/main/java` + `rebuilt/cuisine/src/main/resources`

## 编译修复说明

Vineflower 反编译结果整体可编译，仅修复了少量 DataComponentType 泛型 builder 推断问题：

- `CodexComponents.PAGE_ENTRY` 使用 `DataComponentType.<PageEntry>builder()`
- `CoatingComponents.WEAPON_COATING` 使用 `DataComponentType.<WeaponCoating>builder()`
- `CuisineComponents.HODGEPODGE_CONTENTS` 使用 `DataComponentType.<List<String>>builder()`

这些是反编译源码重建所需的类型修复，不改变模组业务逻辑。

## Alchemy Step 1 小报告

1. 当前普通药水效果计算位置：`alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java`。各精华通过 `essence(...)` 声明 `EffectSpec`，再由 `registerPotion(...)` 写入 `MobEffectInstance`。
2. 澄清药水生成位置：同文件 `registerModifiers(...)`，旧逻辑为正面效果 `halfDuration(..., false)` 并注册 `<id>_clarified`，配方为基础草药药水 + 蜜脾。
3. 浊毒/浊化药水生成位置：同文件 `registerModifiers(...)`，现有稳定 ID 为 `<id>_murky`，配方为基础草药药水 + 发酵蛛眼，旧逻辑为负面效果 `multiplyDuration(..., 1.5f)`。
4. 当前 potion stack factory：没有统一 Java factory。药水本体是注册表中的 `Holder<Potion>`；酿造通过 `FabricPotionBrewingBuilder.BUILD.addMix(...)`；交易 JSON 使用 `minecraft:set_potion` 或 `minecraft:potion_contents` 组件断言。
5. 当前 Data Component 注册类：Core 有 `CodexComponents`，Alchemy 只有 `CoatingComponents`；尚无 `potion_quality`、`potion_bonus`、`advanced_potion_initialized`。
6. 当前 tooltip：Alchemy 客户端入口 `HerbcraftAlchemyClient` 注册 `CoatingTooltips`，通过 `ItemTooltipCallback.EVENT` 显示精华和武器淬层；药水品质/附加药性 tooltip 尚未实现。
7. 当前村民交易：26.1 数据驱动 JSON，路径 `data/herbcraft/villager_trade/...`，不是 TradeOfferHelper Java 注册；售卖药水使用 `given_item_modifiers` 的 `minecraft:set_potion`，收购药水使用 `minecraft:potion_contents` 精确组件断言。

## 注意：文档冲突

用户补充文档要求 `TradeOfferHelper`，但 Fabric 26.1 官方说明与当前 jar 资源均显示村民交易已改为数据驱动：`data/<namespace>/villager_trade` + `data/<namespace>/trade_set` / tags。后续交易相关修改应优先遵守 26.1 官方环境与现有仓库数据驱动结构，避免倒退到过时 API。

## 下一阶段

下一阶段开始实现 `Herbcraft Alche.txt`：

1. 重做普通 / 澄清 / murky 浊毒药水公式；保留现有 ID，不改成 purified/corrupted/turbid 等新 ID。
2. 新增 `herbcraft_alchemy:potion_quality` 与 `herbcraft_alchemy:advanced_potion_initialized` 组件。
3. 实现 `AdvancedPotionStackInitializer.initializeIfNeeded(...)`，并接入 20 tick 背包兜底扫描。
4. 实现品质 tooltip。
5. 视时间实现随机附加药性组件与 tooltip。
6. 出 jar 前执行构建与基础验证，不把测试完全推给用户。
