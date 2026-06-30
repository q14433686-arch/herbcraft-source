# Herbcraft 1.1.3 全系统 Wiki / 百草可食系统总览

状态：`1.1.3` 当前系统说明草案，面向作者、维护者、Wiki 编写与高级玩家。  
适用版本：Minecraft `26.1.2` · Fabric Loader `0.19.3+` · Fabric API `0.151.0+26.1.2+` · Java `25+`  
最后更新：2026-06-17

> 这是一份“全系统说明”，不是全物品答案表。它讲机制、权重、叠加顺序与边界；不主动列出所有隐藏配方、所有物品精确性味、所有最优组合。百草经仍应是游戏内发现的主渠道。

---

## 0. 阅读路线

如果你只想理解玩家体验，读：

```text
1. 总览
2. 知识系统：百草经 / 食材录 / 药食志
3. 生食草木与药性回旋
4. 性味、五味、气机、七情
5. 营养学
```

如果你要维护 / 扩展 / 写兼容，读：

```text
10. 数据驱动与扩展边界
11. Farmer+More 内置兼容
12. P4 API 与事件
13. Validator 与发布检查
```

如果你要做玩家 Wiki，建议先拆成：

```text
基础玩法页
百草经页
营养系统页
性味/气机/七情页
Cuisine 页
Alchemy 页
兼容与扩展页
FAQ 页
```

---

## 1. 一句话总览

Herbcraft 的核心不是“吃花拿 buff”，而是：

```text
吃草是赌博。
喝药是契约。
做菜是手艺。
知识要亲尝、实践、残页与系统引导慢慢得来。
```

三条主线：

| 线 | 核心动词 | 主要模块 | 设计含义 |
|---|---|---|---|
| 生食 / 百草 | 吃 | Core | 风险、药性、五味、七情、气机、药性回旋。 |
| 营养 / 食材 | 养 | Core | 普通食物进入长期五养，不走完整草药药效。 |
| 烹饪 / 药膳 | 做 | Cuisine | 用配方和火候换确定性、免疫、清除与营养。 |
| 炼金 | 炼 | Alchemy | 把草药知识变成精华、药水、品质、淬层。 |

1.1.3 的新定位：

```text
Extension API & Data Compatibility
```

- P4.1 JSON 扩展规范完成。
- P4.2 只读 Java API 完成。
- P4.3 观察型事件 API 完成。
- P4.4 示例 datapack/addon 完成。
- P4.5 validators + docs 收口完成。
- Farmer's Delight Refabricated + More Delight 兼容随 Core 内置。

---

## 2. 信息系统：百草经、食材录、药食志

### 2.1 三本“书”的职责

| 系统 | 章节 / 入口 | 负责什么 | 不负责什么 |
|---|---|---|---|
| 百草经 / Herb Codex | `herbal` | 草木性味、药效、五味、气机、七情、传闻、谙熟。 | 普通食物营养详情。 |
| 食材录 / Ingredient Codex | `ingredients` | 普通食物和兼容食物的营养倾向。 | 药理、五味、七情。 |
| 药食志 | 百草志下的 `foods` section | “本是食物，但有性味”的药食同源条目。 | 食物营养数值；营养仍归食材录。 |

一句话边界：

```text
营养归食材录。
性味归百草志。
普通食物若有性味，进药食志。
```

### 2.2 知识状态

百草经与食材录都使用知识门控。核心状态大致是：

| 状态 | 含义 | 玩家看到什么 |
|---|---|---|
| UNKNOWN | 未知 | 目录可能不显示或显示未知，详情隐藏。 |
| ENCOUNTERED | 见过 / 遇到 | 名称或基础提示，具体性味/效果隐藏。 |
| HEARD | 听闻 | 残页/传闻给出模糊信息，可能不完整或可疑。 |
| TASTED | 尝过 | 玩家亲自食用，性味、口感、体感、粗略信息出现。 |
| MASTERED | 谙熟 | 更完整的效果/用途/实践信息。 |

设计规则：

```text
进度指路，百草经讲理，玩家亲尝见真。
```

### 2.3 药食志为什么存在

Farmer's Delight 的番茄、洋葱、卷心菜这类食物可以有性味并参与药理，但它们不应变成完整 Herbcraft 生草药，也不应覆盖目标模组的 FOOD/CONSUMABLE 组件。

因此 1.1.3 使用“药食志”：

```text
食材录：记录它吃下后的营养。
药食志：记录它作为药食同源物的性味、口感、体感与药理经验。
```

当前 Farmer+More 内置兼容中，药食志条目为 5 个：

```text
farmersdelight:cabbage
farmersdelight:cabbage_leaf
farmersdelight:onion
farmersdelight:pumpkin_slice
farmersdelight:tomato
```

特别排除：

```text
farmersdelight:rice            # 生稻米，目标模组不可食
farmersdelight:cabbage_seeds   # 不可食种子
farmersdelight:tomato_seeds    # 不可食种子
```

熟米饭仍在食材录 / 营养中：

```text
farmersdelight:cooked_rice
```

---

## 3. 生食草木：完整 Herbcraft Herb 流程

完整 Herbcraft herb 指 `data/herbcraft/herbs.json` 或 startup-only `data/<namespace>/herbcraft/herbs/*.json` 注册为 Herbcraft edible 的物品。

### 3.1 食用结算总顺序

完整 herb 食用后，当前 Core 结算顺序可以概括为：

```text
1. Vanilla finishUsingItem 完成。
2. 如果是 Herbcraft herb：进入 EffectResolver.applyAfterConsume。
3. 更新 herb_stack / 药性回旋。
4. 移除 entry.remove_effects。
5. 若 clearPositiveEffects：清除正面效果，但跳过 Herbcraft 营养效果。
6. 若 extinguish：灭火。
7. 计算 pharmacology：气机、五味、七情、特质关系。
8. 结算 food_rolls：概率可见饱食 / 概率隐藏饱和。
9. 若未 cancelAll：给 remove_effects 对应免疫。
10. 若未 cancelAll：按药理修正后施加 effects。
11. 若未 cancelAll：结算 damage_events / heal_events。
12. 处理 bucketRemainder。
13. 处理 special counters。
14. 检查 herb_stack overload。
15. 标记百草经 tasted。
16. 触发 HERB_CONSUMED 事件。
17. 之后 ItemMixin 对所有食物记录 nutrition。
```

### 3.2 HerbEntry 主要字段

| 字段 | 含义 |
|---|---|
| `nutrition` | 稳定可见饱食度。 |
| `saturation_modifier` | Vanilla 饱和度系数。 |
| `food_rolls.nutrition` | 食后概率直接加可见饱食点。 |
| `food_rolls.saturation` | 食后概率直接加隐藏饱和点。 |
| `consume_seconds` | 食用时间。 |
| `medicinal` | 是否 always edible。 |
| `stack_group` | 是否增加药性回旋层数。 |
| `effects` | 可能施加的药效。 |
| `remove_effects` | 食用时先移除的效果，并可授予短免疫。 |
| `damage_events` | 概率伤害事件。 |
| `heal_events` | 概率治疗事件。 |
| `flags` | `drink`, `bucketRemainder`, `extinguish`, `clearPositiveEffects`, `shortUseProjectile` 等。 |
| `counter` | 绑定 special counter。 |

### 3.3 food_rolls 公式

`food_rolls` 是 1.1.2 以后用于修正“常见草木不该稳定当饭吃”的机制。

```json
"food_rolls": {
  "nutrition": { "amount": 1, "chance": 0.35 },
  "saturation": { "amount": 0.3, "chance": 0.45 }
}
```

含义：

| 字段 | 含义 |
|---|---|
| `nutrition.amount` | 直接可见饱食点，必须整数。 |
| `nutrition.chance` | 触发概率。 |
| `saturation.amount` | 直接隐藏饱和点，不是 vanilla modifier。 |
| `saturation.chance` | 触发概率。 |

隐藏饱和点会被钳制：

```text
0 <= saturation <= 当前可见饱食度
```

---

## 4. 药性回旋 / Herb Stack

药性回旋是短时间连续食用部分草药形成的层数。

源码：

```text
HerbStackManager
PlayerMixin herbcraft$herbStack
```

### 4.1 基础规则

| 项 | 当前值 |
|---|---:|
| 持续窗口 | 1200 tick = 60 秒 |
| 叠层来源 | `stack_group = true` 的 herb |
| 过期 | 超过窗口未续吃则清零 |
| 死亡 | 清零 |
| 过载阈值 | `>= 10` |

每次吃 `stack_group` herb：

```text
herbStack += 1
herbExpiresAt = 当前时间 + 1200 tick
```

### 4.2 对正面效果的加成

在 `EffectResolver.applyEffect` 中，只有：

```text
effect.positive == true && entry.stackGroup == true
```

才吃 herb_stack 加成。

计算：

```text
概率 = min( pharmacology.chance(baseChance) + 0.1 × max(0, herbStack - 1), 0.9 )
```

等级：

```text
若 herbStack >= 5：amplifier 最多 +1 到 I/II 档附近
再叠加 pharmacology.positiveAmplifierBonus
```

时长：

```text
若 herbStack >= 8：正面效果时长 × 1.5
```

### 4.3 过载

若：

```text
herbStack >= 10
```

则：

```text
发送提示
授予 herb_overload 进度
施加 nausea 200t + hunger 200t
清空 herbStack
```

### 4.4 与苦味的关系

五味中的 `bitter` 会调用：

```text
HerbStackManager.decrement(...)
```

即苦味会削减一层药性回旋。设计上是“苦能下行、能收蓄”。

---

## 5. 性味系统总览

性味由三部分组成：

```text
temperature  寒热偏性
flavors      药理味
traits       特质标签
```

数据来源：

```text
data/herbcraft/herb_natures.json
data/<namespace>/herbcraft/herb_natures/*.json
```

### 5.1 性 / temperature

允许值：

| key | 中文 | 气机值 |
|---|---|---:|
| `cold` | 寒 | -15 |
| `cool` | 微寒 | -8 |
| `neutral` | 平 | 0 |
| `warm` | 微温 | +8 |
| `hot` | 热 / 辛热 | +15 |

气机值在 `pharmacology_rules.json` 中配置。

### 5.2 味 / flavors

允许值：

```text
sweet, bitter, pungent, sour, astringent, salty, bland
```

中文：

| key | 中文 |
|---|---|
| `sweet` | 甘 |
| `bitter` | 苦 |
| `pungent` | 辛 |
| `sour` | 酸 |
| `astringent` | 涩 |
| `salty` | 咸 |
| `bland` | 淡 |

注意：玩家口感显示可用 `taste_flavors`，可以与 `flavors` 不完全一致。

### 5.3 特质 / traits

当前特质是固定词表，不是动态注册表。

允许值：

```text
clearing, toxic, nourishing, stirring, sedating, aquatic, fiery, withered,
nether, floral, resinous, fungal, lunar, solar, leafy, icy, slimy,
honeyed, earthy, addictive, curio
```

特质定位：

```text
小型药理标签 + 百草经提示 + 七情规则条件 + 相须相使暗线。
```

它不是主数值系统，不建议做成玩家必须背的词条表。

---

## 6. 气机：隐藏寒热轴

气机是玩家体内短期寒热偏向。

源码：

```text
PlayerPharmacologyState.qiBalance
PharmacologyResolver
pharmacology_rules.json
```

范围：

```text
-100 .. +100
```

含义：

```text
负数 = 寒偏
0 = 平
正数 = 热偏
```

### 6.1 气机变化

每次吃下有性味的东西：

```text
qiBalance = clamp(qiBefore + temperatureValue, -100, 100)
```

当前 `temperatureValue`：

```text
cold    -15
cool     -8
neutral   0
warm     +8
hot     +15
```

### 6.2 气机回落

```text
每 600 tick，向 0 回落 5 点
```

也就是约每 30 秒回落 5。

### 6.3 寒热相济

条件：

```text
qiBefore != 0
本次 temperatureValue != 0
两者正负号相反
```

效果：

```text
negative probability multiplier *= 0.7
negative duration multiplier *= 0.75
记录 temperature_harmonize
提示：寒热相济，药性调和。
```

设计含义：用相反寒热调和当前偏性。

### 6.4 同气偏盛

条件：

```text
qiBefore != 0
本次 temperatureValue != 0
两者正负号相同
abs(qiBefore) >= 50
```

效果：

```text
negative probability multiplier *= 1.3
negative duration multiplier *= 1.25
```

正向偏热时记录：

```text
temperature_same_hot
提示：热性渐盛，反噬将至。
```

负向偏寒时记录：

```text
temperature_same_cold
提示：寒意积聚，药力渐沉。
```

---

## 7. 五味：概率、时长、治疗、免疫的轻量修正

五味在 `PharmacologyResolver` 中参与结算。

### 7.1 基础公式

药理结果中的概率计算：

```text
finalChance = clamp(baseChance × probabilityMultiplier + probabilityBonus, 0, 1)
```

时长计算：

```text
positive finalDuration = baseTicks × positiveDurationMultiplier
negative finalDuration = baseTicks × negativeDurationMultiplier
```

治疗/伤害：

```text
finalHeal = baseHeal × healMultiplier
finalDamage = baseDamage × damageMultiplier
```

### 7.2 各味效果

| 味 | 当前效果 |
|---|---|
| 甘 `sweet` | `healMultiplier *= 1.15`。有治疗类效果时记录发现。食物药性桥会额外记录甘味发现。 |
| 苦 `bitter` | `immunityMultiplier *= 1.2`，并削减一层 herb_stack。 |
| 辛 `pungent` | `positiveDurationMultiplier *= 1.15`；8% 反胃 60t；可记录辛味发现。 |
| 酸 `sour` | `positiveDurationMultiplier *= 1.1`，`negativeDurationMultiplier *= 0.9`。 |
| 涩 `astringent` | 同酸的时长方向；若玩家血量低于 8 且冷却可用，额外 `healMultiplier += 0.35`。冷却 600t。 |
| 咸 `salty` | `immunityMultiplier *= 1.15`；近水时正负时长都 `*= 1.1` 并记录近水发现。 |
| 淡 `bland` | `probabilityMultiplier *= 0.85`，降低副作用概率。 |

### 7.3 食物药性桥的五味发现

普通药食没有直接 Herbcraft 药效，因此部分“有药效才记录”的五味不会自然被记录。1.1.3 已补：

```text
sweet
pungent
sour
astringent
bland
```

会在 food-nature bridge 中记录发现。苦/咸仍由正常 resolver 路径记录。

---

## 8. 七情与配伍

七情规则在：

```text
data/herbcraft/pharmacology_rules.json
```

按顺序判断，先匹配者生效。

当前顺序：

```text
xiang_fan
xiang_sha
xiang_wei
xiang_e
xiang_xu
xiang_shi
single
```

### 8.1 相反 / xiang_fan

条件：

```text
冷热冲突
200 tick 内
当前或前一个有 toxic 特质
```

效果：

```text
cancelAll = true
qiBalance = 0
若配置开启，施加 nausea 200t amplifier 1 + hunger 300t
```

### 8.2 相杀 / xiang_sha

条件：

```text
当前 clearing
前一个 toxic
```

效果：

```text
negativeDurationMultiplier *= 0.6
toxicProtectionExpiresAt = 当前时间 + 400t
清除最近 toxic 负面效果
```

### 8.3 相畏 / xiang_wei

条件：

```text
当前 toxic
前一个 clearing
```

效果：

```text
negativeDurationMultiplier *= 0.5
```

### 8.4 相恶 / xiang_e

条件：

```text
stirring 与 sedating 冲突
```

效果：

```text
probabilityMultiplier *= 0.75
healMultiplier *= 0.75
damageMultiplier *= 0.75
```

### 8.5 相须 / xiang_xu

条件：

```text
当前与前一个共享特质数量 >= 2
```

效果：

```text
positiveAmplifierBonus += 1
healMultiplier *= 1.2
```

示例：

```text
卷心菜 + 卷心菜叶
```

它们都具有：

```text
leafy + nourishing
```

因此容易触发相须。

### 8.6 相使 / xiang_shi

条件：

```text
当前含 nourishing / clearing / sedating 之一
前一个含 stirring / clearing / nourishing 之一
且不是完全相同 profile
```

效果：

```text
positiveDurationMultiplier *= 1.15
```

### 8.7 单行 / single

条件：

```text
当前与历史窗口内所有 recent use 都没有共享特质
```

效果：

```text
probabilityBonus += 0.05
```

### 8.8 七情窗口

基础 recent-use 窗口：

```text
600 tick
```

相反额外要求：

```text
within_ticks = 200
```

---

## 9. 特质：低存在感但很关键的暗线

特质主要不是直接给 buff，而是：

```text
1. 百草经提示语。
2. 七情条件。
3. 相须共享特质数量。
4. 单行判断。
5. 少量毒性保护。
```

真正直接参与当前七情规则的特质：

```text
toxic
clearing
stirring
sedating
nourishing
```

其他特质多作为风味标签、共享特质、提示与未来扩展空间：

```text
floral, leafy, aquatic, fiery, icy, nether, fungal, resinous,
lunar, solar, honeyed, earthy, slimy, withered, addictive, curio
```

建议玩家向 Wiki 只轻讲，不做大表背诵。

---

## 10. 普通食物：食材录、营养与药食桥

普通食物默认不走 Herbcraft herb 流程。

但如果它有：

```text
nutrition profile
ingredient entry
herb nature profile
```

则可以走 food-nature bridge：

```text
吃下后仍保持目标模组原本食物行为；
Herbcraft 额外记录营养；
Herbcraft 额外计算性味、气机、五味、七情；
百草志 / 药食志记录性味；
食材录记录营养。
```

这用于 Farmer's Delight 番茄、洋葱、卷心菜等。

不会做的事：

```text
不会把不可食用种子变成食物。
不会覆盖 Farmer's Delight FOOD/CONSUMABLE。
不会给旧 jar 发布独立 datapack 兼容路线。
```

---

## 11. 特殊计数器

数据：

```text
data/herbcraft/special_counters.json
```

当前内置 counter：

| id | 物品 | 窗口 | 规则 |
|---|---|---:|---|
| `poppy` | `minecraft:poppy` | 1200t | 总是 resistance 240t；第 3 次起 darkness 200t。 |
| `torchflower` | `minecraft:torchflower` | 1200t | 第 3 次起 fire_resistance 600t。 |
| `sugar` | `minecraft:sugar` | 1200t | 第 4 次起 50% nausea 160t。 |
| `sugar` | `minecraft:honey_block` | 1200t | 第 2 次起 50% nausea 160t。 |
| `slime` | `minecraft:slime_ball` | 1200t | 第 3 次起 slowness 100t。 |

说明：

```text
同 id 计数器会共享玩家窗口计数。
外部 special counter ID 也会按 ID 动态持久化；旧版内置计数字段会迁移到动态计数表。
```

---

## 12. 营养学 / 五养系统

营养维度：

| key | 中文 | 英文 |
|---|---|---|
| `grain` | 谷粮 | Grain |
| `flesh` | 血肉 | Flesh |
| `oil` | 膏脂 | Oil |
| `vege` | 菜蔬 | Greens |
| `fruit` | 津液 | Fluids |

### 12.1 基础数值

当前规则：

| 字段 | 值 |
|---|---:|
| max_value | 1000 |
| initial_value | 500 |
| decay_interval_ticks | 1200 |
| decay_amount | 2 |
| check_interval_ticks | 1200 |
| balanced_min | 350 |
| balanced_max | 650 |
| malnutrition_low | 150 |
| imbalance_high | 850 |
| imbalance_spread | 550 |
| full_balance_min | 400 |
| full_balance_max | 600 |

每次吃有 nutrition profile 的食物：

```text
各维度 += profile 值，最高不超过 1000
记录最近餐食
立即 evaluateAndApply
```

自然衰减：

```text
每 1200 tick，各维度 -2，最低 0
```

### 12.2 营养状态区间

| 区间 | 状态 | 效果 |
|---:|---|---|
| `<150` | 严重缺乏 | 持续缺乏效果，直到下次评估。 |
| `150..349` | 警告缺乏 | 概率短 debuff。 |
| `350..649` | 一般均衡区 | 无缺乏。 |
| `650..849` | 丰盛/偏高 | 部分维度有轻正向。 |
| `>=850` | 过盛 | 可能触发饮食失衡等。 |

### 12.3 缺乏效果

| 维度 | 效果 |
|---|---|
| 谷粮 | block_break_speed -15% |
| 血肉 | attack_damage -1 |
| 膏脂 | movement_speed -8% |
| 菜蔬 | periodic exhaustion 80t / 0.08 |
| 津液 | movement_speed -5% |

### 12.4 丰盛效果

当前丰盛直接加成只有部分维度：

| 维度 | 效果 |
|---|---|
| 血肉 | attack_damage +1 |
| 津液 | movement_speed +5% base multiplier |

### 12.5 五养调和

条件：

```text
所有维度在 400..600
```

效果：

```text
well_nourished 1800t
armor +3
knockback_resistance +0.12
```

### 12.6 饮食失衡

高维度判断：

```text
某非豁免高维 >= correction_high_threshold 750
```

惩罚失衡判断：

```text
high > imbalance_high 850
且 high - min(other dimensions) > imbalanceSpread 550
```

高津液特殊规则：

```text
fruit_high_non_punitive = true
```

所以高津液不作为普通惩罚维度，但仍可能挡住五养调和。

### 12.7 饮食回正 / correction

最近餐食：

```text
最多 24 餐
普通记录窗口 7200t
回正评分窗口 3600t
```

矩阵：

| 过盛维度 | 有效回正维度与权重 |
|---|---|
| grain | flesh 1.15, oil 0.45, vege 1.35 |
| flesh | vege 1.6 |
| oil | grain 0.25, vege 1.45 |
| vege | grain 1.2, flesh 1.2, oil 0.5 |
| fruit | 无普通矩阵回正 |

评分公式概括：

```text
score(highDim) = Σ recentMeal[dim] × correctionWeight(highDim, dim)
```

但若该餐食中过盛维度自身太多：

```text
corrective <= meal[highDim] × selfPenaltyMultiplier
```

则该餐不算回正。

当前：

```text
selfPenaltyMultiplier = 1.0
correctionScoreThreshold = 60
correctionHighThreshold = 750
correctionTargetValue = 650
```

回正衰减：

```text
每 200t
基础衰减 = clamp(15 + score / 12, 15, 45)
750..849 区间半速
850+ 全速
目标不低于 650
```

### 12.8 高津液 surplus helper

条件：

```text
fruit >= 850
且至少两个非 fruit 维度 > 850
且这两个非 fruit 维度之间存在 correction matrix 正权重关系
```

效果：

```text
合格非 fruit 维度每 200t -10，目标不低于 700
```

---

## 13. Cuisine：水碗、药膳、百草盅

### 13.1 水碗

水碗是 Cuisine 的基础容器。

定位：

```text
不是纯净水系统。
不是炼药水瓶。
是药膳/生坯配方容器。
```

可饮用：

```text
清燃烧
15% 清反胃
成功时暂时缓解 dietary_imbalance 60 秒
```

Homeostatic 安装时，水碗有独立 hydration 数据。

### 13.2 百草盅 / Hodgepodge

规则数据：

```text
data/herbcraft_cuisine/hodgepodge_rules.json
```

当前参数：

| 字段 | 值 |
|---|---:|
| min_herbs | 3 |
| max_herbs | 7 |
| normal_max_herbs | 4 |
| nutrition_cap | 10 |
| chance_bonus | 0.15 |
| max_stack_gain | 3 |
| floral_bonus_min_count | 2 |

冲突规则：

| 字段 | 值 |
|---|---:|
| base_chance | 0.30 |
| toxic_bonus | 0.10 |
| clearing_reduction | 0.15 |
| bland_reduction | 0.10 |
| min/max | 0.0 / 0.80 |
| nausea_seconds | 10 |
| toxic_without_clearing_duration_multiplier | 1.5 |

设计：

```text
空碗 + 3~4 草药 = 百草盅常规路线。
水碗 + 特定组合 = 固定生坯路线。
蘑菇炖菜保留原版。
```

### 13.3 固定药膳

Cuisine 当前 `dishes.json` 定义 10 个主要菜肴。它们强调：

```text
确定性
免疫
清除
营养
配方成本
```

而不是生食赌博。

---

## 14. Alchemy：精华、药水、品质、事件

### 14.1 精华

当前 40 种 essence 定义在：

```text
data/herbcraft_alchemy/essences.json
```

原则：

```text
8 个来源原料 + 玻璃瓶 → 精华
精华作为炼金稳定输入
```

### 14.2 药水品质与余韵 / 杂毒

核心思路：

```text
普通药水 = 契约基础
澄清 = 去负面、强化正面
浊化 = 去正面、强化负面
品质 = 普通 / 优质 / 极品
```

高阶药水组件包括：

```text
potion_quality
refinement_bonus
advanced_potion_initialized
```

bonus pool 数据：

```text
data/herbcraft_alchemy/potion_bonus_pools.json
```

### 14.3 特殊药水与女巫安全池

特殊药水数据：

```text
data/herbcraft_alchemy/special_potions.json
```

女巫投掷池只使用安全短时负面，不投极端 T3 毒物。

---

## 15. 扩展系统 / API

### 15.1 JSON 扩展

正式规范：

```text
docs/JSON_EXTENSION_SPEC_1_1_3.md
```

reload-safe 路径：

```text
herb_natures
nutrition_profiles
ingredients
special_counters
knowledge_rumors
codex_loot_injections
```

startup-only：

```text
herbs
```

### 15.2 Java 只读 API

```text
com.herbcraft.api.HerbcraftDataAPI
```

查询：

```text
nature
nutrition
ingredient entry
herb definition
food-nature entry
snapshots
```

### 15.3 观察型事件 API

```text
com.herbcraft.api.HerbcraftEvents
```

事件：

```text
HERB_CONSUMED
NUTRITION_APPLIED
INGREDIENT_DISCOVERED
FOOD_NATURE_RESOLVED
EXTERNAL_DATA_RELOADED
```

不开放：

```text
registerNature
registerNutrition
registerIngredient
registerHerbFood
```

---

## 16. 当前 Farmer+More 兼容

随 Core 1.1.3 内置。

范围：

```text
Farmer's Delight Refabricated
More Delight
```

不包括：

```text
Reg's More Foods
```

原因：RMF 数据包驱动物品身份不稳定，可能与原版/Herbcraft 已知物品顶号。

最终数量：

```text
nutrition_profiles: 108
ingredients: 108
herb_natures: 5
knowledge_rumors: 0
```

药食志条目：

```text
cabbage
cabbage_leaf
onion
pumpkin_slice
tomato
```

排除：

```text
raw rice
cabbage seeds
tomato seeds
RMF all entries
```

---

## 17. 叠加顺序总表

最重要的总顺序：

```text
Vanilla consumption
→ Herbcraft full herb effect pipeline, if full herb
→ Nutrition pipeline for all profiled foods
→ Food-nature bridge for ordinary nature foods
→ Knowledge updates
→ Client sync / tooltip / Codex visibility
```

完整 herb 内部：

```text
herb_stack update
→ remove effects / clear flags
→ pharmacology result
→ food_rolls
→ immunities
→ effects / damage / heal
→ special counters
→ overload
→ knowledge tasted
→ event
```

药理内部：

```text
qi regression / cleanup
→ temperature qi check
→ flavor multipliers
→ astringent low-health bonus
→ toxic protection
→ seven-emotion relation
→ qi update
→ recent-use record
→ overlay message
→ result returned to effect application
```

营养内部：

```text
record meal
→ add profile values
→ mark 食材录 tasted
→ fire events
→ evaluate deficiencies/imbalance/correction/well nourished
→ sync nutrition panel
```

---

## 18. Wiki 拆分建议

这份文档可以拆成以下公开 Wiki 页面：

```text
1. Overview / 百草可食是什么
2. Herb Codex / 百草经
3. Ingredient Codex / 食材录
4. Food-Herbs / 药食志
5. Raw Herb Eating / 生食草木
6. Herb Stack / 药性回旋
7. Nature, Flavors, Traits / 性味五味特质
8. Qi / 气机
9. Seven Relations / 七情配伍
10. Nutrition / 五养营养
11. Cuisine / 膳房分册
12. Alchemy / 炼金分册
13. Compatibility / 兼容
14. Addon Author Guide / 扩展作者指南
15. FAQ
```

玩家向页面应少剧透物品表；开发者页面可以给完整 schema 和 API。
