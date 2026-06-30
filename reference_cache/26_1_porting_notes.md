# 26.1 / Fabric 开发参考缓存

缓存时间：2026-06-16（更新自 2026-06-14 初版）

> 这是给 Herbcraft 测试版维护用的快速参考，不替代实时查 API。遇到 26.1 细节、Fabric API 命名、Mixin 目标签名、数据驱动交易、配方序列化等不确定点时，应重新联网查证或直接检查本地反编译/Gradle 缓存。

## 已确认的外部参考

1. Fabric 官方《Fabric for Minecraft 26.1》：https://fabricmc.net/2026/03/14/261.html
2. Minecraft 官方 26.1 Snapshot 1：https://www.minecraft.net/en-us/article/minecraft-26-1-snapshot-1
3. Minecraft Wiki — Java Edition 26.1：https://minecraft.wiki/w/Java_Edition_26.1
4. Minecraft Wiki — Java Edition 26.1 Snapshot 1：https://minecraft.wiki/w/Java_Edition_26.1_Snapshot_1
5. 在线反编译源码浏览：https://mcsrc.dev/

## 环境要求

- **Java 25** 最低要求（Gradle JVM 和运行时均需）
- Fabric Loom `1.15+`（本项目锁定 `1.16-SNAPSHOT`，解析为 `1.16.3`）
- Gradle `9.4.0+`（本项目锁定 `9.4.1`）
- IntelliJ IDEA `2025.3+`（Mixin 需要）
- 26.1 是**首个不混淆的 Minecraft 版本**，Yarn 不再被 Fabric 官方支持，应使用 Mojang official mappings

## Fabric API 重要变化

### 废弃/移除

- `fabric` mod ID 不再提供，必须依赖 `fabric-api`
- `fabric-convention-tags-v1` 已移除
- `fabric-loot-api-v2` 已移除 → 使用 **v3**
- `HudRenderCallback` 已移除 → 使用 `HudElementRegistry`
- `ColorProviderRegistry` 简化为 `BlockColorRegistry`
- `TradeOfferHelper` 旧 API 已废弃 → 使用数据驱动 JSON

### API 重命名

- `ItemGroupEvents` → `CreativeModeTabEvents`（以及其他 Yarn→Official 重命名）
- 完整迁移映射见 Fabric Docs：https://docs.fabricmc.net/develop/porting/26.1/fabric-api

### Loom 变化

- 不再使用 `modImplementation`/`modCompileOnly`，改用标准 `implementation`/`compileOnly`
- 不再使用 `remapJar`，改用 `jar`
- 不再需要 remap 操作

### 新增 API

- `DimensionEvents.MODIFY_ATTRIBUTES` — 维度级事件
- `BlockEvents#USE_ITEM_ON` / `USE_WITHOUT_ITEM` — 精确方块交互事件
- `ItemEvents#USE_ON` / `USE` — 精确物品交互事件

## Minecraft 核心变化

### ItemStackTemplate

- `ItemStack` 不能在世界加载前创建
- 新增 `ItemStackTemplate`（不可变，表示 Item + count + DataComponentPatch）
- 世界加载前不可访问 `ItemStackTemplate` 上的组件（会抛异常）

### 配方序列化器

- 不再需要 inner class 实现 `RecipeSerializer`
- 简化为 `MapCodec` + `StreamCodec`：

```java
Registry.register(BuiltInRegistries.RECIPE_SERIALIZE, "mod_recipe",
    new RecipeSerializer<>(ModRecipe.CODEC, ModRecipe.STREAM_CODEC));
```

### 村民交易（数据驱动）

26.1 把村民和流浪商人交易完全数据驱动化。三层结构：

**层 1：交易定义** — `data/<namespace>/villager_trade/<profession>/<level>/<trade_name>.json`

```json
{
  "wants": {
    "id": "minecraft:emerald",
    "count": 1
  },
  "gives": {
    "id": "minecraft:diamond_pickaxe",
    "count": 1
  },
  "max_uses": 12,
  "xp": 10,
  "reputation_discount": 0.05
}
```

交易定义字段：
- `wants` — 所需物品（`id`, `count`（number provider，默认 1）, `components`（可选组件映射））
- `additional_wants` — 可选第二所需物品（同 `wants` 格式）
- `gives` — 给予物品（标准 ItemStack 格式）
- `given_item_modifiers` — 可选物品修改器列表（附魔/药水等动态化）
- `max_uses` — 最大交易次数（number provider，默认 4，最低 1）
- `reputation_discount` — 声望折扣影响（number provider，默认 0.0，最低 0.0）
- `xp` — 商人获得经验（number provider，默认 1，最低 0）
- `merchant_predicate` — 商人条件谓词（可选，用于限制特定生物群系等）
- `double_trade_price_enchantments` — 若附魔存在则翻倍成本（可选）

**层 2：交易集** — `data/<namespace>/trade_set/<profession>/level_<N>.json`

```json
{
  "amount": 2.0,
  "trades": "#minecraft:armorer/level_1",
  "random_sequence": "minecraft:trade_set/armorer/level_1",
  "allow_duplicates": true
}
```

交易集字段：
- `trades` — 交易 ID / ID 列表 / 交易标签
- `amount` — 从池中随机选取的交易数量（number provider）
- `allow_duplicates` — 是否允许重复选取（默认 false）
- `random_sequence` — 确定性随机序列名（可选）

**层 3：交易标签** — `data/minecraft/tags/villager_trade/<profession>/level_<N>.json`

```json
{
  "replace": false,
  "values": [
    "#minecraft:common_smith/level_1",
    "minecraft:armorer/1/emerald_iron_leggings",
    "minecraft:armorer/1/emerald_iron_boots"
  ]
}
```

职业标签格式：`#<profession>/level_<1-5>`
铁匠共享标签：`#common_smith/level_<1-5>`
流浪商人标签：`#wandering_trader/buying`、`#wandering_trader/special`、`#wandering_trader/common`

**关键：** 交易 JSON 必须被标签引用才会生效。硬编码上限仍为 5 级。

### tooltip

- `appendHoverText` 已弃用 → 使用 `TooltipProvider` 或 `ItemTooltipCallback`

### GUI/渲染

- 1.21.6+ 颜色全部 ARGB，传 RGB 会变透明
- `Screen.isPauseScreen()` 默认 `true`，单人模式会暂停世界+触发存档
- `ChunkSectionLayer` 替代旧 `RenderType`/`RenderLayer`
- 方块渲染层现在根据纹理属性自动设置（半透明/裁切/实心）

### 动物食物标签

26.1 中动物食物完全标签驱动。可用标签：
- `cow_food`, `sheep_food`, `pig_food`, `chicken_food`, `rabbit_food`
- `goat_food`, `camel_food`, `sniffer_food`
- `horse_food`, `horse_tempt_items`（引诱不繁殖）
- `cat_food`, `wolf_food`, `parrot_food`
- `bee_food`, `panda_food`, `turtle_food`
- `axolotl_food`, `strider_food`, `frog_food`
- `armadillo_food`

注意：1.21.5 起羊已原版吃蕨，避免重复添加。

### 战利品表

- `fabric-loot-api-v2` 已移除 → 使用 `loot.v3`
- 只使用 `MODIFY` 追加 pool
- `source.isBuiltin()` 守卫避免修改非原版表

### 药箭

26.1 药箭配方也数据驱动化（source/material 可配）。

### 创造物品栏

- 注册 ≠ 出现，必须使用 `CreativeModeTabEvents`（旧 `ItemGroupEvents`）
- 组件型物品需枚举变体

### 其他

- 26.2 预计支持 Vulkan 渲染后端
- mcsrc.dev 提供在线反编译源码浏览和 Mixin/AccessWidener 辅助工具

## 本项目当前采用方式

- Gradle：`net.fabricmc.fabric-loom`，Java 25，Mojang official names
- 交易：源码中使用 `data/*/villager_trade/**.json` + `data/minecraft/tags/villager_trade/**.json`，没有使用旧 `TradeOfferHelper`
- 配方：`HodgepodgeRecipe`、`CoatingSmithingRecipe` 均按 26.1 风格注册 `RecipeSerializer(MapCodec, StreamCodec)`
- 组件：药水品质、药水 bonus、武器淬层、百草盅内容均使用 DataComponentType
- Loot：core 使用 Fabric loot v3 事件路径
- Tooltip：使用 `ItemTooltipCallback`，不使用已弃用的 `appendHoverText`
- 动物食物：纯标签驱动，8 个 `minecraft/tags/item/` 标签文件
- GUI：所有颜色使用 ARGB；CodexScreen `isPauseScreen()` 返回 false

## 本地缓存资产

- MC merged jar 已缓存至 `/home/user/cache/minecraft/client-26.1.2.jar`（34M，来自 Loom 缓存）
- Loom 内部缓存位于 `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/26.1.2/`
- JDK 25 缓存位于 `/tmp/herbcraft-jdk25/jdk-25`（需通过 `scripts/setup_jdk25_temurin.sh` 安装）

## 后续规则

- 若涉及 26.1 API 细节：先查此文件，再查 mcsrc.dev 或官方页面
- 若 Mixin 目标签名不确定：用 Gradle 缓存中的 merged jar 或 mcsrc.dev 核验后再改
- 不把搜索结果当成源码事实；最终仍以本项目源码、构建、启动验证为准
