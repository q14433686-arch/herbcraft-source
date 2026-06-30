# Historical source review warning — 2026-06-16

This 2026-06-14 source review is retained for historical context. It is superseded by `CURRENT_BASELINE.md`, `PROJECT_STATE.md`, and the actual current source/resources. In particular, the old `1.0.0` jar names and early nutrition descriptions below are stale. Current source is `1.1.1`, nutrition uses custom Herbcraft effects, ingredients are data-driven from `data/herbcraft/ingredients.json`, pharmacology constants are in `data/herbcraft/pharmacology_rules.json`, and correction relationships are in `data/herbcraft/nutrition_correction_rules.json`.

---

# Herbcraft 测试版源码阅读总结

阅读日期：2026-06-14  
阅读对象：`/home/user/herbcraft/source` 实际源码与资源，不以任务文档为准。

## 0. 构建与验证状态

已用临时 JDK 25 构建：

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks
```

结果：`BUILD SUCCESSFUL`。

已启动 `:alchemy:runServer`，服务端到达 `Done (...)!`，未出现 Mixin 类缺失或 Mixin prepare 崩溃。

已生成可下载/保留产物：

- `release_output/herbcraft_test_20260614_review/herbcraft-1.0.0.jar`
- `release_output/herbcraft_test_20260614_review/herbcraft-alchemy-1.0.0.jar`
- `release_output/herbcraft_test_20260614_review/herbcraft-cuisine-1.0.0.jar`
- `release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip`

另：平台快照会排除名为 `output` 的目录，所以我使用 `release_output/` 来满足“保留输出产物”的目的。

---

## 1. 总体架构

这是一个三模块 Fabric 26.1.2 模组套件：

1. `core` / `herbcraft`：核心。负责让大量原版物品变为可食，提供草药效果、药性蓄积、药性/五味/七情逻辑、营养系统、百草经知识系统、Jade/Tooltip 信息分层、进度触发基础。
2. `alchemy` / `herbcraft_alchemy`：炼金分册。依赖 core，提供 40 种精华、草药药水、澄清/浊化/长效/强化/T3 药水、高阶药水品质与余韵/杂毒组件、武器淬层、女巫安全药水、村民交易。
3. `cuisine` / `herbcraft_cuisine`：膳房分册。依赖 core，提供盛水的碗、固定菜、药膳、生坯烹饪、百草盅、百花宴、剧毒蛊、菜肴知识章节与村民交易。

模块关系从源码看基本保持：core 不依赖两个附属；alchemy/cuisine 只通过 core API 与事件、知识系统、HerbcraftAPI 等交互。

---

## 2. Core：可食草药系统

### 2.1 数据驱动食用表

核心食用数据在：

- `core/src/main/resources/data/herbcraft/herbs.json`

实际加载逻辑在 `HerbFoodRegistry.loadFromBundledJson()`。

当前数据：

- 96 个可食定义。
- 33 个 `medicinal`。
- 24 个 `stack_group`。
- 80 个带效果。
- 26 个带 `remove_effects`。
- 特殊 flags：
  - `shortUseProjectile`：4 个。
  - `clearPositiveEffects`：1 个。
  - `extinguish`：1 个。
  - `bucketRemainder`：1 个。
  - `drink`：1 个。

效果类型共 24 种，源码统计中较常见的有：

- 反胃、生命恢复、速度、中毒、缓慢、抗性、虚弱、饥饿、夜视、跳跃提升、火焰抗性、发光、水下呼吸、伤害吸收等。

`HerbFoodRegistry.registerDefaultComponents()` 会给这些原版物品追加食物组件和消耗组件，不是注册一套新的草药物品。

### 2.2 食用结算

入口是 `ItemMixin.finishUsingItem`：

- 只有 `HerbFoodRegistry.get(stack.getItem()) != null` 的物品才触发 Herbcraft 自定义结算。
- 结算发生在 vanilla `finishUsingItem` 返回后，因此不是右键开始就解锁知识。

主要逻辑在 `EffectResolver.applyAfterConsume()`：

1. 读取 `HerbEntry`。
2. 对玩家执行药理系统 `PharmacologyResolver.resolve()`。
3. 根据 herb stack 计算概率、等级、时长。
4. 先处理清除正面效果、清除指定效果、灭火等特殊行为。
5. 施加效果、伤害事件、治疗事件。
6. 处理细雪桶返还桶等容器逻辑。
7. 触发特殊计数器。
8. 处理药性过载。
9. 标记知识为 tasted。
10. 触发 `HerbConsumeEvents`，给菜谱解锁、最后一餐等系统用。

### 2.3 药性蓄积 herb_stack

实现位置：

- `HerbStackManager`
- `PlayerMixin` 中保存字段。

规则：

- `stack_group` 草药食用时 herb stack +1。
- 持续窗口是 1200 tick，即约 60 秒。
- 到 3 / 5 / 8 层时给玩家私聊系统提示，并发对应进度。
- 到 10 层触发过载：
  - ActionBar + 私聊提示。
  - 反胃 10 秒。
  - 饥饿 10 秒。
  - 清空 herb stack。
  - 授予 `herb_overload` 进度。

效果加成：

- 正面效果概率随 stack 增加，每层 +10%，上限 90%。
- stack ≥5：正面效果等级最多提升到 II。
- stack ≥8：正面效果时长 ×1.5。

### 2.4 特殊计数器

实现位置：`SpecialCounters`。

当前有四类：

- 罂粟：60 秒窗口内累计；每次给抗性；第 3 次起追加黑暗。
- 火把花：第 3 次起追加火焰抗性。
- 糖/蜂蜜块：糖第 4 次起、蜂蜜块第 2 次起，有 50% 反胃。
- 粘液球：第 3 次起缓慢。

这些计数器也会被菜肴触发，因为 `DishItem` 和 `HodgepodgeItem` 都会调用 `SpecialCounters.apply()`。

### 2.5 短按投掷 / 长按食用

实现位置：

- `EggItemMixin`
- `SnowballItemMixin`
- `ProjectileUseHandler`
- `ItemMixin.releaseUsing`

机制：

- 对标记 `shortUseProjectile` 的物品，右键开始进入使用状态。
- 释放时如果使用时间小于 6 tick，就走投掷逻辑。
- 长按则走食用逻辑。
- 雪球、鸡蛋类因此兼容投掷与食用。

### 2.6 细雪桶饮用

实现位置：`BucketItemMixin`。

当手持 `POWDER_SNOW_BUCKET` 且准星没有命中可交互对象时，会开始使用物品，从而允许“喝细雪桶”；桶返还由 `EffectResolver` 中的 `bucketRemainder` 处理。

### 2.7 效果免疫

实现位置：

- `EffectImmunityManager`
- `HerbcraftAPI.grantEffectImmunity()` / `isImmuneToEffect()`
- `LivingEntityMixin`
- `PlayerMixin` 持久化

机制：

- 免疫以 effect id -> 到期 tick 存在玩家数据里。
- `LivingEntity.addEffect` 被 Mixin 拦截。
- 如果玩家对某效果还在免疫期，`addEffect` 直接返回 false。
- 死亡时清除免疫。

这个系统主要被 cuisine 的药膳使用。

### 2.8 最后一餐

实现位置：`LastMealTracker`。

- 食用 Herbcraft 草药或菜肴时记录玩家最后食物与时间。
- 玩家死亡时，如果死亡前 60 tick 内有记录，则广播“最后吃的是……”风味文本。

### 2.9 食用音效

实现位置：`ConsumeSoundResolver`。

大致按物品路径判断：

- 饮品：喝水声。
- 冰雪：玻璃碎声。
- 树脂/粘液/蜂蜜：蜂蜜块滑动声。
- 金色/发光/水晶：紫水晶声。
- 下界菌/下界疣：下界疣破坏声。
- 其他草木：草声。

---

## 3. Core：药性、五味、七情系统

### 3.1 药性数据

数据文件：

- `core/src/main/resources/data/herbcraft/herb_natures.json`

当前覆盖 96 个条目。

每个条目大致包含：

- `temperature`：寒/凉/平/温/热，代码中是 cold/cool/neutral/warm/hot。
- `flavors`：用于机制的药理五味。
- `taste_flavors`：用于口感/描述，不等同于机制五味。
- `traits`：如 floral、leafy、toxic、clearing、nourishing、stirring、sedating 等。

统计：

- 温性分布：neutral 63、cold 12、cool 9、warm 6、hot 6。
- 机制味：astringent 46、bland 46、bitter 16、sweet 14、pungent 12、sour 6、salty 5。
- traits 常见：floral 28、leafy 24、curio 19、nourishing 8、clearing 8、toxic 7 等。

### 3.2 PharmacologyResolver

`PharmacologyResolver.resolve()` 在每次草药结算前运行，根据当前食物药性与玩家近期食用状态调整后续效果。

它会影响：

- 效果概率。
- 正面/负面时长。
- 正面等级 bonus。
- 治疗倍率。
- 伤害倍率。
- 免疫倍率。
- 是否完全取消本次效果。

五味机制示例：

- sweet：提高治疗倍率。
- bitter：提高免疫倍率，并减少 herb stack。
- pungent：延长正面时长，同时有小概率反胃。
- sour/astringent：延长正面、缩短负面。
- salty：提高免疫；靠近水时进一步影响时长。
- bland：降低副作用概率。
- astringent 在低血量且冷却通过时给额外治疗倍率。

### 3.3 七情 / 药性相互作用

源码中的七情枚举：

- `XIANG_FAN`
- `XIANG_SHA`
- `XIANG_WEI`
- `XIANG_XU`
- `XIANG_SHI`
- `XIANG_E`
- `SINGLE`
- `NONE`

判定基于当前草药与最近一次/近期食用草药的 temperature 与 traits。

实际效果：

- 相反：冷热冲突且有 toxic 时，取消本次效果，清 qi，可能给反胃/饥饿。
- 相杀：clearing 对 toxic，削弱负面并给短期毒性保护。
- 相畏：toxic 遇 clearing，负面时长减半。
- 相须：共享 traits ≥2，正面等级 +1，治疗倍率提升。
- 相使：部分 trait 组合延长正面时长。
- 相恶：stirring/sedating 冲突，降低概率、治疗和伤害倍率。
- 单行：近期历史都不相似，给概率 bonus。

这些结果通过 ActionBar 文本提示玩家。

---

## 4. Core：营养系统

营养系统不是文档里最显眼的部分，但源码里已经有完整实现。

涉及文件：

- `NutritionRegistry`
- `NutritionProfile`
- `NutritionRules`
- `NutritionManager`
- `PlayerNutritionState`
- `NutritionSyncPayload`
- `NutritionPanelScreen`
- `NutritionItemTooltips`

入口：`ItemMixin.finishUsingItem` 对所有食物都调用 `NutritionManager.onFoodConsumed()`，不只是 Herbcraft 草药。

营养维度：

- grain
- flesh
- oil
- vege
- fruit

机制：

- 食物有营养 profile。
- 玩家营养状态存在 `PlayerNutritionState`。
- 每 tick 由 `ServerTickEvents.END_LEVEL_TICK` 定期检查。
- 营养值会衰减。
- 极低时触发饥荒状态：虚弱、缓慢、挖掘疲劳等。
- 单维度不足会给对应轻/重 debuff。
- flesh 充足会给攻击力 transient modifier。
- fruit 充足会给移动速度 modifier。
- 五维均衡会给隐藏抗性 I。
- 过度不均衡会警告并可能反胃。
- 客户端有营养同步 payload 与面板 UI。

这说明当前测试版已经不只是“草药可食”，而是带了一个泛食物营养生态系统。

---

## 5. Core：百草经 / 知识系统

### 5.1 知识状态

当前源码中不是简单三态，而是 flags：

- encountered
- heard
- tasted
- practiced
- eatCount

展示层是五态：

- Unknown
- Encountered
- Heard
- Tasted
- Mastered

`KnowledgeAPI.displayState()` 决定显示状态。

特别点：

- `overview` 总纲永远显示 MASTERED。
- mastery 可以来自 flags + practice routes，也有 core fallback。
- 旧版 `HerbcraftKnowledge` 整数 map 仍会镜像写入，兼容老存档。

### 5.2 数据持久化

`PlayerMixin` 保存：

- legacy knowledge map。
- knowledge flags。
- experienced effects。
- claim marks。
- verified true / false claims。
- effect immunities。
- pharmacology state。
- nutrition state。

### 5.3 百草经物品与 UI

物品：

- `herbcraft:herb_codex`
- `herbcraft:tattered_page`

核心类：

- `CodexBookItem`
- `TatteredPageItem`
- `CodexScreen`
- `CodexSnapshotPayload`
- `ClaimMarkPayload`

百草经 UI：

- 打开后请求服务端快照。
- 左侧为章节/小节/条目树。
- 右侧为详情页。
- 可滚动。
- `isPauseScreen() = false`，打开书不暂停世界。
- 支持 claim 标记点击。
- claim hitbox 按实际显示行定位。

### 5.4 总纲

源码里 `HerbalChapter.register()` 已注册：

- section：`overview`
- entry：`overview`
- 标题：`book.herbcraft.entry.overview.title`

中文为：`百草经总纲`。

所以总纲不是只存在语言文件，它确实在章节注册里。

### 5.5 传闻 / 谣言 / 校勘

数据：

- `knowledge_rumors.json`

当前 96 个 raw herb 条目都有 claims。

Codex 在 Heard 状态显示 claims。玩家可以对 claim 做个人标记；errata 页面可以验证 claim 真/假。

### 5.6 信息分层

从 tooltip/Jade/百草经代码看，信息门控是认真的：

- 未知：只提示未知。
- Encountered：知道见过，但不暴露药性。
- Heard：显示传闻，但不等同真相。
- Tasted：显示亲尝验证的自然属性，不显示全部精确数值。
- Mastered：显示完整药性、用途、数值。

### 5.7 物品 tooltip 与 Jade

客户端 tooltip：`HerbNatureItemTooltips`。

- 只在有 screen 时追加，避免世界 overlay 重复注入。
- 按知识状态决定显示内容。

Jade：

- `HerbcraftJadePlugin`
- `HerbNatureProvider`
- `HerbNatureEntityProvider`
- server data provider 与 client display provider 分离。

这部分是为了适应 Jade 26.1 的限制，并防止信息泄漏。

---

## 6. Core：战利品、村民、动物标签、进度

### 6.1 残页战利品

`CodexLoot` 使用 Fabric Loot API v3 修改原版战利品表，并用 `source.isBuiltin()` 守卫。

残页会投放到：

- 村庄相关箱子。
- 沉船。
- 丛林神庙。
- 下界堡垒。
- 林地府邸。

不同 loot table 有不同 page kind、章节、稀有度设计。

### 6.2 交易

core 有 librarian / wandering trader 相关 trade JSON。alchemy 与 cuisine 有大量交易 JSON。

源码没有使用旧 `TradeOfferHelper`。

实际资源统计：

- core trade：9 个。
- alchemy trade：94 个。
- cuisine trade：19 个。

并且 `validate_trade_tags.py` 已通过，说明这些 trade JSON 都被对应 tag 引用，且 tag `replace=false`。

### 6.3 进度

当前 advancement 数量：

- core：16。
- alchemy：9。
- cuisine：6。

合计 31。

服务端启动时 advancement 加载成功。

---

## 7. Alchemy：精华与药水

### 7.1 精华

核心注册在 `AlchemyRegistries`。

当前源码注册 40 种精华：

- dandelion
- golden_dandelion
- poppy
- blue_orchid
- azure_bluet
- tulip
- oxeye_daisy
- cornflower
- lily_of_the_valley
- wither_rose
- torchflower
- closed_eyeblossom
- open_eyeblossom
- cactus_flower
- pink_petals
- sunflower
- lilac
- rose_bush
- peony
- pitcher_plant
- fern
- spore_blossom
- cocoa_beans
- oak_leaves
- spruce_leaves
- birch_leaves
- acacia_leaves
- mangrove_leaves
- cherry_leaves
- pale_oak_leaves
- azalea_leaves
- flowering_azalea_leaves
- cherry_sapling
- azalea
- flowering_azalea
- kelp
- sea_pickle
- crimson_fungus
- warped_fungus
- brown_mushroom

每个 essence 同时注册：

- 精华物品。
- 基础 potion。
- 可用 `AWKWARD + essence` 酿出基础药水。
- 自动生成修饰药水线。

### 7.2 药水线

源码逻辑：

- 基础药水：对原效果做稳定化，正面最低 15 秒、负面最低 10 秒，时长最大 480 秒，普通正面 amplifier 最高 1。
- 澄清：从正面效果生成，`basePotion + honeycomb`，时长 ×3，最低 30 秒。
- 浊化：从负面效果生成，`basePotion + fermented_spider_eye`，时长 ×3，最低 30 秒；单负面且等级 0 时会强化到 I。
- 长效：`basePotion + redstone`，时长 ×2。
- 强化：`basePotion + glowstone_dust`，正面等级 +1 且时长减半。
- 凋零玫瑰特殊：glowstone 生成 `wither_venom_iii`，而不是普通 strong。

T3 特殊药水：

- `undead_venom`：flowering_azalea_murky + wither_rose essence。
- `cardiac_toxin`：lily_of_the_valley_murky + closed_eyeblossom essence。
- `wither_venom_iii`：wither rose base + glowstone。

### 7.3 高阶药水品质系统

组件：

- `herbcraft_alchemy:potion_quality`
- `herbcraft_alchemy:potion_bonus`
- `herbcraft_alchemy:potion_source`
- `herbcraft_alchemy:advanced_potion_initialized`

品质：

- STANDARD：时长 ×1.0。
- FINE：时长 ×1.5。
- EXCEPTIONAL：时长 ×2.0。

初始化：

- `AdvancedPotionInventoryHooks` 挂到 `KnowledgeInventoryScanner`。
- 玩家背包每秒扫描一次。
- 遇到高级药水，若未初始化则 roll 品质与 bonus。
- `advanced_potion_initialized` 防止重复 roll。

Bonus：

- 50% 概率获得一个额外效果。
- 澄清类从正面 bonus pool 取。
- 浊化/T3 从负面 bonus pool 取。
- bonus 会实际写入 potion contents，并写入 `PotionBonusComponent` 用于 tooltip。

时长处理：

- 如果 vanilla `POTION_DURATION_SCALE` 不会超 9600 tick，则使用 vanilla duration scale。
- 如果会超上限，则重写 PotionContents 的每个效果时长，保留 custom name。

### 7.4 高阶药水 tooltip

`AdvancedPotionTooltips` 显示：

- 药水类型：澄清、浊化、T3 等。
- 品质与倍率。
- 余韵/杂毒 bonus 的效果名、等级、持续时间。

### 7.5 村民收购高级药水兼容

`ItemCostAdvancedPotionMixin` 改写 `ItemCost.test`：

- 如果交易要求的是某种 Herbcraft 高级药水，只匹配 potion path。
- 不要求随机品质/bonus 完全一致。

这解决“同一种药水因随机组件不同而不能卖”的问题。

### 7.6 隐藏菜催化

相关类：

- `HiddenCuisineCatalystBrewing`
- `BrewingStandIngredientSlotMixin`
- `BrewingStandCatalystMixin`

逻辑：

- `hundred_flower_feast` 是催化剂，只作用于 `_clarified` 药水。
- `deadly_hodgepodge` 是催化剂，只作用于 `_murky` 药水。
- 放入炼药台 ingredient slot 由 `BrewingStandIngredientSlotMixin` 允许。
- 自动化/方块实体 slot 由 `BrewingStandCatalystMixin.canPlaceItem` 允许。
- `isBrewable` 判断三个药水槽是否有可升级对象。
- `doBrew` 时复制原 stack，只设置 `POTION_QUALITY = EXCEPTIONAL`，然后消耗 1 个催化菜。

从源码看，这条线不会重建药水，所以会保留已有组件。

### 7.7 女巫安全药水

`WitchHerbcraftPotionMixin` 注入女巫远程攻击：

- 受 `HerbcraftAlchemyConfig` 控制。
- 默认概率读取 config，目前源码默认 10%。
- 不对 Raider 目标使用。
- 替换为安全短时 Herbcraft splash potion：
  - tulip murky：虚弱。
  - azure bluet murky：失明 + 反胃。
  - pitcher plant murky：饥饿。
  - azalea murky：中毒 + 反胃。
  - closed eyeblossom murky：黑暗。

不会扔 T3、凋零 III、亡灵毒液等危险药水。

---

## 8. Alchemy：武器淬层

### 8.1 组件与物品

组件：`herbcraft:weapon_coating`。  
数据结构：`WeaponCoating`。

淬层物品：

- blank_adhesive
- amethyst_adhesive
- echo_adhesive

### 8.2 锻造台配方

`CoatingSmithingRecipe` 是自定义 smithing recipe。

有两种用途：

1. 给可淬武器上涂层。
2. 精炼已有涂层。

`coatable_weapons` tag 决定哪些武器可淬。

### 8.3 命中触发

`CoatingHitHandler` 监听 `ServerLivingEntityEvents.AFTER_DAMAGE`。

条件：

- 没有被格挡。
- 服务器端。
- 伤害源实体是攻击者本人，直接实体也是攻击者，意味着主要是近战。
- 主手武器有 `weapon_coating` 组件。

效果：

- FULL 模式：将精华本身的效果施加给目标。
- POSITIVE 模式：给攻击者正面效果，并额外随机一个正面效果。
- NEGATIVE 模式：给目标负面效果，并额外随机一个负面效果。
- 每次有效命中减少 remainingUses。
- 到期或次数耗尽会移除涂层。

使用次数：

- 基于精华效果强度评分。
- FULL 一般 12~24 次。
- 精炼模式次数翻倍。

持续时间：

- FULL 约 10 分钟。
- 精炼模式约 12 分钟。

---

## 9. Cuisine：盛水的碗

物品：`herbcraft_cuisine:water_bowl`。

实现：

- `CuisineItems.registerWaterBowl()`
- `WaterBowlItem`
- `WaterBowlInteraction`

获取方式：

- 空碗右键水源。
- 空碗右键水炼药锅，炼药锅水位 -1。

食用效果：

- 0 营养。
- 1 秒饮用。
- 清除燃烧。
- 15% 概率清除反胃。
- 使用后转为空碗。
- 授予 `water_bowl` 进度。

---

## 10. Cuisine：固定菜、药膳、生坯烹饪

数据文件：

- `cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json`

当前：

- 10 道普通固定菜。
- 22 道药膳/隐藏菜。
- 合计 32 道 DishRegistry dish。

普通固定菜：

- 蒲公英沙拉
- 三花拼盘
- 凉拌海杂
- 嫩芽什锦
- 松针热汤
- 镇痛羹
- 双菌下界煲
- 蜜渍花瓣
- 雪顶冰盅
- 苦口良菜

药膳/特殊菜包括：

- 净魂雪羹
- 饱腹南瓜粥
- 清毒蜜叶汤
- 醒胃冰盅
- 复力牡丹汤
- 破倦镇痛羹
- 行身铃兰羹
- 明目开眼汤
- 坠星压浮汤
- 安兆闭眼粥
- 绯红战煲
- 诡异疾汤
- 樱花甘露粥
- 松针热汤（药膳）
- 海行杂炖
- 蜜脾润喉羹
- 树脂护胃炖
- 金蒲公英羹
- 夜游苍叶汤
- 蓝冰冷萃汤
- 百花宴
- 剧毒蛊

### 10.1 DishItem 行为

`DishItem.finishUsingItem()`：

- 可清火。
- 可清冰冻。
- 可点燃玩家若 `ignite_seconds > 0`。
- 可清全部效果或指定效果。
- 可通过 HerbcraftAPI 授予效果免疫。
- 可施加 100% 或概率效果。
- 可造成 damage events。
- 可触发特殊计数器。
- 可记录最后一餐。
- 可授予 cuisine 进度。

### 10.2 生坯路线

`DishRegistry.register()` 中如果 dish 属于 medicinal，会额外注册 `raw_<dish_id>` item。

资源中有大量：

- raw dish crafting recipe。
- smelting recipe。
- smoking recipe。
- campfire cooking recipe。

原始药膳配方使用 `water_bowl`，成品经烹饪获得。

---

## 11. Cuisine：百草盅

物品：`herbcraft_cuisine:hodgepodge`。

配方：自定义 `HodgepodgeRecipe`。

规则：

- 必须是 1 个原版空碗。
- 3~4 个 `#herbcraft:herb` 草药，或隐藏特殊组合。
- 红/棕蘑菇显式拒绝，避免冲突原版迷之炖菜。
- 水碗不参与百草盅配方。
- 如果组合正好是固定菜，则让位给固定菜，避免抢配方。
- 隐藏特殊组合例外，可允许 7 花或 4 毒组合进入百草盅/隐藏菜隔离逻辑。

合成结果：

- 写入 `HODGEPODGE_CONTENTS` 组件，保存原料 item id。
- 营养值为原料营养总和，上限 10。
- 饱和取原料中的最大值。

食用结算：`HodgepodgeItem.resolve()`。

- 对每个原料复用 Herbcraft 草药效果。
- 每个效果概率 +15%，上限 100%。
- 处理原料的 remove effects / clear positive / extinguish / damage / heal。
- stack_group 原料最多让 herb stack +3。
- floralCount ≥2 会额外加一次 herb stack。
- 若同时有正面与纯负面药性，有概率“药性相冲”反胃。
- 相冲概率受 toxic / clearing / bland 数量调节，最高 80%。

---

## 12. Cuisine：百花宴与剧毒蛊

源码与资源中都存在。

### 12.1 百花宴

物品：

- `raw_hundred_flower_feast`
- `hundred_flower_feast`

配方：

- water_bowl
- dandelion
- oxeye_daisy
- cornflower
- blue_orchid
- allium
- pink_petals
- torchflower

生坯经熔炉/烟熏炉/营火变成成品。

源码层功能：

- 是可食药膳。
- 是炼金催化剂：升级 `_clarified` 药水到 EXCEPTIONAL。

### 12.2 剧毒蛊

物品：

- `raw_deadly_hodgepodge`
- `deadly_hodgepodge`

配方：

- water_bowl
- wither_rose
- lily_of_the_valley
- flowering_azalea_leaves
- closed_eyeblossom

生坯经熔炉/烟熏炉/营火变成成品。

源码层功能：

- 是可食药膳。
- 有 clear_all_effects 等强烈效果设计。
- 是炼金催化剂：升级 `_murky` 药水到 EXCEPTIONAL。

---

## 13. Cuisine / Alchemy 的知识联动

### 13.1 CuisineChapter

章节 id：`cuisine`，排序 100。

根据 DishRegistry 自动注册每道菜条目。

分节：

- cold
- soups
- medicinal

知识显示：

- 未掌握时，没吃过的原料显示未知。
- 掌握后显示完整原料与效果。
- 吃过任意相关原料会让对应菜谱在配方书中解锁。

### 13.2 CuisineAcquisitionHooks

背包扫描：

- 拿到成品菜，如果是自己 craft 过，标记 cuisine practiced。
- 同时对其原料标记 herbal practiced。
- 如果不是自己 craft，标记 heard。
- 拿到 raw dish，标记 heard。

### 13.3 AlchemyChapter

章节 id：`alchemy`，排序 200。

自动注册：

- 每种 essence 条目。
- 每种 potion 条目。
- 每种 coating 条目。

分节：

- essences
- potions
- coatings

### 13.4 AlchemyAcquisitionHooks

背包扫描：

- 拿到精华：授予 first_essence；craft 过则 practiced，否则 heard。
- 拿到 Herbcraft potion：标记对应 potion/heard，授予药水进度。
- 拿到带 weapon_coating 的武器：授予 first_coating，解锁对应 coating 条目。

---

## 14. 资源与内容规模

实际资源统计：

### core

- Java：约 75 文件。
- assets 文件：10。
- data 文件：45。
- recipes：1。
- advancements：16。
- trades：9。

### alchemy

- Java：约 26 文件。
- assets 文件：132。
- data 文件：153。
- recipes：49。
- advancements：9。
- trades：94。

### cuisine

- Java：约 16 文件。
- assets 文件：171。
- data 文件：125。
- recipes：99。
- advancements：6。
- trades：19。

---

## 15. 这个测试版实际实现了什么样的模组体验

从源码看，它已经不是一个单点功能测试，而是一个较完整的生存系统扩展：

1. 玩家可以吃大量原版植物、树叶、花、冰雪、树脂、蜂蜜、蛋、种子、下界菌、炼药材料等。
2. 生吃不是稳定收益，而是概率性、叠层性、受药性关系影响的风险行为。
3. 反复吃草会积累 herb stack，短期内变强，但过量会反噬。
4. 草药之间有寒热、五味、traits 与七情关系，不同食用顺序会改变效果。
5. 玩家有长期知识进度：见过、听闻、尝过、实践、谙熟。
6. 信息显示被严格分层，tooltip/Jade/百草经不会一开始剧透全部数值。
7. 残页、传闻、claim 标记、errata 校勘让知识系统有“考据”味道。
8. 营养系统把所有食物纳入五维营养长期管理。
9. 厨房提供稳定路线：水碗、固定菜、药膳、生坯烹饪、百草盅。
10. 药膳不是单纯 buff 食物，而是“清除 + 免疫 + 饱腹/战斗效果”的准备型系统。
11. 炼金把草药转化成精华、药水、澄清/浊化路线与高阶随机品质。
12. 武器淬层把精华效果转移到近战武器上。
13. 女巫、村民、战利品、进度、配方书、创造栏都已经接入。
14. 百花宴与剧毒蛊同时是菜肴和炼金催化剂，形成 cuisine 与 alchemy 的交叉高阶玩法。

一句话概括当前源码状态：

> 这是一个围绕“可食原版自然物”扩展出的三路线系统：生食是短期赌博，厨房是可控准备，炼金是稳定提纯；百草经负责把玩家的认知进度变成游戏机制的一部分。

---

## 16. 我认为当前测试版的主要未验证点

不是说它没实现，而是源码读完后我认为还需要实际游戏测试的地方：

1. 百草经 UI 的实际手感：滚动、点击 claim、不同语言长文本布局。
2. Jade 对掉落物和物品 tooltip 的组合行为，因为 Jade 配置可能影响显示。
3. 隐藏菜炼药台催化的真实客户端交互：源码和服务端启动都过了，但最好实际放进炼药台试一次。
4. 所有 advancement 是否都能自然达成，还是有些只是占位触发。
5. 营养系统长期游玩下是否过重，因为它已经影响所有食物。
6. 村民交易经济是否平衡；脚本只验证 tag 引用，不验证价格体验。
7. 药性七情系统提示是否会和其他 ActionBar/聊天提示互相抢存在感。
8. `validate_animal_food_tags.py` 需要原版 client jar 缓存，目前未完成该项校验。

---

## 17. 外部 26.1 参考缓存

已写入：

- `reference_cache/26_1_porting_notes.md`

其中记录了 Fabric 26.1 官方页面与 Minecraft 官方 Snapshot 交易数据驱动说明，并列出本项目与 26.1 API 的对应点。
