> Release-copy snapshot: this page may describe the published `1.1.1` public copy, but the working tree also contains planned `1.1.2` polish such as `food_rolls`, Codex microcopy, and guide hints. For current source status use `CURRENT_BASELINE.md` and `docs/DOCUMENTATION_INDEX.md`.

# Herbcraft / 百草可食 1.1.1

> 三模块 Fabric 模组套件合并介绍页。  
> 包含：`Herbcraft` 核心、`Herbcraft: Alchemy` 炼金分册、`Herbcraft: Cuisine` 膳房分册。  
> 环境：Minecraft Java Edition `26.1.2` / Fabric Loader `0.19.3+` / Fabric API `0.151.0+26.1.2+` / Java `25+`

---

## 短简介

**百草可食 Herbcraft** 是一组三模块 Fabric 生存玩法模组。它让 Minecraft 中大量原版自然物与奇物可以食用，并围绕“生吃风险、药性蓄积、七情配伍、营养状态、百草经认知、炼金与药膳”构建一套慢热的草药探索系统。

它不是一个简单的“吃东西加 Buff”的模组。你需要见过、听闻、尝过、记录、辨伪、做菜、炼药，最后才真正识得其性。

---

## 一句话版本

让花草、树叶、冰雪、菌类与奇物皆可入口，并在百草经、药理学、营养、炼金与药膳中慢慢认识它们。

---

# 一、模块组成

| 模块 | 文件 | 是否必需 | 简介 |
|---|---|---|---|
| Core | `herbcraft-1.1.1.jar` | 必需 | 可食物、生吃结算、药理学、药性蓄积、营养系统、百草经、知识系统、Jade/tooltip 信息门禁、动物食物扩展 |
| Alchemy | `herbcraft-alchemy-1.1.1.jar` | 可选 | 草药精华、药水、澄清/浊化路线、品质系统、武器淬层、女巫与炼金经济 |
| Cuisine | `herbcraft-cuisine-1.1.1.jar` | 可选 | 水碗、固定药膳、百草盅、清除与免疫、百花宴、剧毒蛊 |

Alchemy 和 Cuisine 都依赖 Core，但二者彼此独立。你可以只装 Core，也可以装 Core + Alchemy，或者 Core + Cuisine，也可以三者全装。

当 Alchemy 与 Cuisine 同时安装时，隐藏料理可以作为高阶炼药催化剂参与部分药水品质升级。

---

# 二、Core：百草可食

## 更多东西可以吃

Herbcraft 让许多原版自然物与奇物可以入口，例如：

- 花、草、蕨、树叶、树苗、种子；
- 海草、海带、海泡菜；
- 雪球、冰、蓝冰、细雪桶；
- 树脂、蜜脾、蜂蜜、黏液球；
- 下界疣、下界菌、烈焰粉、岩浆膏等炼药材料；
- 南瓜、西瓜、鸡蛋，以及更多自然材料。

有些东西温和，有些危险，有些只是口感古怪，有些则具有真正的药理意义。

---

## 生吃是赌博

直接吃草药是最快的探索方式，但也是最不稳定的方式。

你可能获得恢复、速度、抗性、夜视、清除负面状态等帮助；也可能中毒、反胃、饥饿、虚弱、受伤，甚至因为药性冲突遭到反噬。

Herbcraft 尽量用原版效果组合出新的药理语义；但从 `1.1.1` 起，营养系统使用 Herbcraft 自定义营养效果，以免和药膳清除/免疫逻辑互相误伤。

---

## 药性蓄积

连续食用草药会让药性在体内蓄积。

随着层数上升，你可能跨过不同阶段，药效会逐渐增强；但如果吃得太多、太杂、太急，最终可能触发药性过载。

药性蓄积不是单纯奖励，而是身体对药性的承受极限。

当前版本中：

- 七情配伍等即时药理反应显示在 ActionBar；
- 药性蓄积阶段提示显示为只给自己的聊天 / system message；
- 两者不会互相覆盖。

---

## 药理学：寒热、五味、七情

Herbcraft 加入了一套简化的药理互动：

- **寒热**：寒、凉、平、温、热会影响体内平衡；
- **五味**：甘、苦、辛、酸、涩、咸、淡参与不同药理机制；
- **七情配伍**：短时间内连续食用不同草药，可能相须、相使、相畏、相杀、相恶、相反。

这些不是单纯的提示语。它们会影响效果概率、持续时间、强度、负面效果，甚至让本次药效完全失效。

当前版本已经区分：

- `flavors`：药理五味，参与机制计算；
- `taste_flavors`：口感味道，只用于玩家尝过后的感性描述。

所以“吃起来苦”不再必然等于“药理上有苦味效果”。

---

# 三、营养系统：温和提醒，不是硬核惩罚

`1.1.1` 对营养与兼容系统进行了大幅重构。

Herbcraft 现在追踪五类营养：

- 谷粮 / Grain
- 血肉 / Flesh
- 膏脂 / Oil
- 菜蔬 / Vegetables
- 津液 / Fluids

主模组里的营养系统不是为了硬核惩罚玩家，而是温和提醒玩家注意饮食结构。真正严格的“硬核营养学”更适合作为未来独立附属。

## 自定义营养状态

当前有 7 个 Herbcraft 营养效果：

| 效果 | 机制 |
|---|---|
| 谷气亏虚 | 挖掘速度下降 |
| 血肉亏虚 | 攻击伤害下降 |
| 津脂不足 | 移动速度下降 |
| 蔬素不足 | 缓慢增加饥饿消耗 |
| 津液不足 | 轻微移动速度下降 |
| 饮食失衡 | 轻微移动速度下降并缓慢增加饥饿消耗 |
| 五养调和 | 提供少量护甲与 12% 击退抗性 |

严重缺乏时，负面状态会持续到下一次营养检查，而不是随机闪一下。轻度不足仍然保持短暂、概率性的提醒。

水碗有 15% 概率临时缓解饮食失衡 60 秒，但不会真正修复营养值。

---

# 四、可选 Homeostatic / 稳态兼容

Herbcraft 1.1.1 内置可选 Homeostatic 数据兼容。未安装 Homeostatic 时，这些数据不会影响游戏。

如果安装 Homeostatic：

- 膳房分册中的水碗、汤、羹、粥、煲、冷萃和部分料理会影响水分值；
- 水碗获取容易，因此补水同时有较高概率带来 Homeostatic 的口渴消耗效果，避免成为廉价纯净水；
- 核心中部分被 Herbcraft 改造成可食的原版物品也有水分或口渴风险数据；
- 冰、雪球、细雪桶等水分明确的物品会补水；
- 糖、蜂蜜块、树脂、烈焰粉、毒性植物等不会补水，而是通过 Homeostatic 的口渴效果表达干燥/刺激/毒性风险；
- 兼容数据不会给 `minecraft:potion` 统一补水，避免影响所有药水。

---

# 五、百草经：知识不是免费给你的

Herbcraft 的核心之一是“认识”。

百草经不会一开始告诉你所有物品的完整数据。每个条目都有认知状态：

```text
未知 → 见过 → 听闻 / 尝过 → 谙熟
```

- **未知**：你还不了解它；
- **见过**：你曾见过此物，但尚未识得其性；
- **听闻**：你通过残页或传闻知道了一些信息；
- **尝过**：你亲自吃过它，知道口感、体感和自己实际触发过的效果；
- **谙熟**：你经过足够实践，真正掌握了它。

Jade、背包 tooltip 和百草经都会遵守信息门禁。刚见过一个物品时，不会直接显示它的真实性味、特质和用途。

---

## 残页、疑载与辨伪

世界中可以找到残页。残页会揭示某些条目的传闻。

传闻可能是：

- **据载**：较可信的记载；
- **疑载**：似是而非的信息。

你可以在百草经中给每条传闻做自己的判断：

- 我认为它是真的；
- 我认为它是假的；
- 我还不确定。

之后通过实践、勘误和谙熟，你会知道自己当初有没有看走眼。

这不是额外装饰，而是 Herbcraft 的核心节奏：信息需要被挣来。

---

# 六、动物也认识这些草木

Herbcraft 不只扩展玩家能吃什么，也补了一部分原版动物的食性与引诱逻辑。

这些改动走原版 tag 系统，不硬改 AI，不替换原版行为。

当前包括：

- 牛、羊、猪、兔可以接受部分草蕨类；
- 山羊可以吃更多花、草和树叶；
- 马可以被草蕨类引诱，但不是简单变成繁殖捷径；
- 骆驼可以吃枯灌木；
- 嗅探兽可以吃瓶子草荚果。

危险或毒性的植物不会被随便塞进动物食物表。

---

# 七、原版交互优先

Herbcraft 尽量不抢原版交互。

例如：

- 种子喂鸡优先；
- 竹子喂熊猫优先；
- 金胡萝卜喂马优先；
- 蜜脾涂蜡优先；
- 方块类物品对方块使用时优先放置 / 交互；
- 鸡蛋、雪球短按仍可投掷，长按才会吃。

也就是说，它是在原版行为上追加，而不是粗暴覆盖。

---

# 八、Alchemy：炼金分册

如果说 Core 里的生吃是赌博，那么炼金就是契约。

生吃草药很快，但不稳定。炼金则把草药转化为更明确、更可控的形式：精华、药水、品质、余韵、杂毒与淬层。

## 草药精华

许多草药可以萃取成精华。

精华是炼金系统的基础。它们连接草药、药水、武器淬层、村民交易和知识系统。

通常你需要：

```text
同种原料 ×8 + 玻璃瓶 → 草药精华
```

---

## 药水路线

通过精华，你可以酿造草药药水。

药水路线包括：

- 基础草药药水；
- 澄清药水；
- 浊化药水；
- 长效和强化变体；
- 更高阶的毒系复合药水。

澄清路线偏向正向效果，浊化路线偏向危险或武器化负面效果。

---

## 品质、附加药性与淬层

高阶药水可能拥有品质：

- 普通；
- 优质；
- 极品。

部分药水还可能带有附加药性，例如澄清药水的余韵、浊化药水的杂毒。

炼金也不只服务于药水。你可以将精华用于武器淬层，让近战武器在一定次数或时间内携带临时药性。

淬层不是附魔，也不是永久强化。它更像一种消耗型战斗准备。

---

## 女巫、村民与隐藏催化

Alchemy 扩展了女巫和交易生态。

女巫可能掉落炼金相关残页、精华或安全版草药药水；村民交易可能涉及精华、药水、药箭、树脂、淬层等经济线。

如果同时安装 Cuisine，某些隐藏料理还可以作为高阶药水催化剂，让对应路线药水达到更高品质。

---

# 九、Cuisine：膳房分册

如果说生吃是赌博，炼金是契约，那么做菜就是手艺。

**Herbcraft: Cuisine** 让草药不再只是在手里直接啃掉。你可以把它们做成汤、羹、茶、冷盘、药膳和百草盅，用更稳定的方式处理风险。

---

## 水碗与固定药膳

Cuisine 引入了水碗作为基础材料。

许多固定药膳不是直接用空碗合成，而是需要：

```text
水碗 + 指定原料 → 生坯 → 烹饪 → 成品
```

固定药膳通常比生吃更稳定，常用于：

- 清除特定负面效果；
- 提供短暂免疫；
- 提供恢复、抗性、急迫、速度等辅助；
- 应对凋零、挖掘疲劳、反胃、燃烧、冻结等具体风险；
- 通过营养 profile 补充五类营养。

药膳不是牛奶的替代品。牛奶是事后无差别清除，药膳更像是提前理解某类风险，并用合适的食物处理它。

---

## 百草盅

百草盅是 Cuisine 中更不稳定、更有实验感的一部分。

你可以把多种草药放进空碗中做成百草盅。它会记录原料，并在食用时动态结算效果。

百草盅可能让多种草药效果一起发挥，也可能因为正负药性混杂而相冲。

它适合喜欢实验的玩家，但不适合完全追求稳定收益的玩家。

---

## 隐藏料理

Cuisine 中包含一些隐藏料理，例如：

- 百花宴；
- 剧毒蛊。

它们不仅是料理，也可能与更高阶系统产生联系。某些隐藏料理可以作为炼金催化剂，让对应路线药水达到更高品质。

这些内容不会一开始直接告诉你，需要通过探索、残页或实践逐步发现。

---

# 十、适合谁？

适合：

- 喜欢探索和试错的玩家；
- 喜欢图鉴、草药、药性、炼金和烹饪系统的玩家；
- 喜欢慢慢解锁信息，而不是一开始看完所有数据的玩家；
- 喜欢有一点中医文化气质和模拟感的玩法。

不太适合：

- 只想快速获得强力 Buff 的玩家；
- 不喜欢概率、风险和信息隐藏的玩家；
- 不想阅读百草经或研究系统的玩家。

---

# 十一、环境要求

- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`

文件：

```text
herbcraft-1.1.1.jar
herbcraft-alchemy-1.1.1.jar
herbcraft-cuisine-1.1.1.jar
```

至少需要安装：

```text
herbcraft-1.1.1.jar
```

Alchemy 和 Cuisine 是可选附属模块，但都需要 Core。

---

# 十二、已知局限

Herbcraft 1.1.1 已经可以作为测试版本游玩，但仍有一些未来可改进方向：

- 百草经 UI 仍可继续打磨；
- 残页和传闻内容未来可继续扩充；
- 营养系统当前偏温和，硬核营养玩法更适合未来独立附属；
- 多人服务器建议先小范围测试；
- Jade / tooltip 显示可能受客户端配置影响，建议实际确认；
- 隐藏料理炼药催化建议在真实客户端炼药台中测试。

---

# English Summary

**Herbcraft** is a three-module Fabric mod suite that makes many vanilla natural items edible and turns them into a slow herbal knowledge system involving taste, rumors, pharmacology, nutrition, animal interactions, cuisine, and alchemy.

It is not a quick buff mod. You learn by seeing, hearing rumors, tasting, marking claims, cooking, brewing, and practicing. Seen items do not reveal their true nature immediately; knowledge is earned over time.

## Modules

- **Herbcraft**: core edible herbs, pharmacology, herb accumulation, custom nutrition effects, Codex, knowledge system, Jade/tooltips, animal food and temptation tags.
- **Herbcraft: Alchemy**: essences, potions, clarified/murky routes, potion quality, bonus components, weapon coatings, witch and trade integration.
- **Herbcraft: Cuisine**: water bowls, medicinal dishes, cleansing and immunity, Herbal Hodgepodge, hidden dishes, and cooking-based preparation.

## Requirements

- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`

Core is required. Alchemy and Cuisine are optional addons.
