# BUGFIX_ADVANCED_POTION_TRADES

日期：2026-06-12

## 问题

高阶药水进入玩家背包后会被初始化品质与附加药性：

- `herbcraft_alchemy:potion_quality`
- `herbcraft_alchemy:potion_bonus`
- `herbcraft_alchemy:advanced_potion_initialized`
- 有附加药性时还会向 `minecraft:potion_contents.custom_effects` 写入效果

26.1 的村民交易 `wants.components` 使用 `DataComponentExactPredicate`。它会按组件值做相等比较。额外的自定义组件本身不会导致失败，但 `minecraft:potion_contents` 内部一旦多了 `custom_effects` 或被品质封顶逻辑改写，就不再等于交易 JSON 中只写 `potion` 的基础组件，因此高阶药水无法卖给村民。

## 修复策略

保留现有交易 JSON 的正确写法：

```json
"components": {
  "minecraft:potion_contents": {
    "potion": "herbcraft:xxx_clarified"
  }
}
```

不在 JSON 中要求：

- `herbcraft_alchemy:potion_quality`
- `herbcraft_alchemy:potion_bonus`
- `herbcraft_alchemy:advanced_potion_initialized`

由于 26.1 的 `ItemCost` 只支持 `DataComponentExactPredicate`，不能仅通过 JSON 对 `potion_contents.potion` 做局部匹配，本次加入一个最小 Mixin：

```text
alchemy/src/main/java/com/herbcraft/alchemy/mixin/ItemCostAdvancedPotionMixin.java
alchemy/src/main/resources/herbcraft_alchemy.mixins.json
```

Mixin 逻辑：

1. 仍先检查物品类型一致，因此普通药水 / 喷溅药水 / 滞留药水 / 药箭容器不会互相冒充。
2. 只对交易期望物品为 Herbcraft 高阶药水时生效。
3. 从交易期望 stack 读取 `minecraft:potion_contents.potion`。
4. 从玩家实际 stack 读取 Herbcraft 高阶药水路径；若因品质封顶被改写为 custom effects，则读取 `customName` 中保存的原药水路径。
5. 两者药水路径相同则允许匹配，忽略品质、附加药性与初始化标记。

## 覆盖范围

会修复所有现有高阶药水收购交易，包括：

- 牧师：`_clarified`、`undead_venom`、`cardiac_toxin`、`wither_venom_iii`
- 制箭师：高级滞留药水收购
- 武器匠：`_murky` 系列收购

## 仍然拒绝

- 普通水瓶
- 错误药种
- 错误容器类型
- 非 Herbcraft 高阶药水

## 版本

- `herbcraft_alchemy`：`0.2.4`
- core 仍为 `1.1.2`
- cuisine 仍为 `0.3.2`

## 测试

- `gradle clean build`：通过
- `scripts/validate_bugfix_advanced_potion_trade.py`：通过
- `alchemy:runServer`：Mixin 加载、模组加载均未出现错误；受本环境启动速度影响，smoke test 在超时前已经确认无 Mixin apply failure / injection failure / Herbcraft 初始化错误。
