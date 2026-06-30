# P1.6：Jade 适配重写与不生效原因复查报告

日期：2026-06-12

## 基线

本轮严格使用最新正确版本作为基线：

```text
/home/user/herbcraft_p1_5_release/herbcraft-source-p1.5-jade-config-fix.zip
```

解压覆盖到：

```text
/home/user/herbcraft_work/herbcraft_source
```

没有使用旧 handoff 源码。

## 为什么 P1.5 Jade 可能仍不生效

根据 Jade 文档与 1.21+ 变更复查，P1.5 虽然已经注册了：

```text
IBlockComponentProvider + IServerDataProvider<BlockAccessor>
```

但仍有两个风险点：

1. **只处理 Block 目标**
   - 如果玩家实际看的是掉落物 ItemEntity，或把“物品”理解为地上的草药物品，Block provider 不会触发。
   - 这会让玩家感觉“玉不显示食物的性味”。

2. **知识状态读取过于依赖 Entry 查找路径**
   - P1.5 使用 `KnowledgeAPI.chapter("herbal").entry(entryId)` 后再调用 `KnowledgeAPI.state(...)`。
   - 若 entryId 与实际目标 item path 有任何差异，state 会回到 0。
   - 百草经内部能显示性味，不代表 Jade 的 provider 一定取到了同一个 entry。

因此本轮不是继续小修，而是把 Jade 适配拆成共享数据工具，并同时支持 Block 与 ItemEntity。

## 本轮重写内容

### 1. 新增共享 Jade 数据工具

新增：

```text
core/src/main/java/com/herbcraft/compat/jade/HerbJadeData.java
```

统一负责：

- 判断 item 是否 Herbcraft 草药；
- 写入 server data；
- 拼 Jade tooltip；
- 计算玩家知识状态。

知识状态现在更稳健：

1. 若玩家 `Stats.ITEM_USED` 记录该 item 已使用 → MASTERED；
2. 否则直接查玩家知识 map：

```text
herbal/<entryId>
```

3. 最后才 fallback 到 `KnowledgeAPI.state(...)`。

这样比只靠 chapter entry 查找更可靠。

### 2. Block Jade provider 简化并稳定

修改：

```text
core/src/main/java/com/herbcraft/compat/jade/HerbNatureProvider.java
```

现在它只负责 BlockAccessor：

- `appendTooltip` 调用 `HerbJadeData.appendTooltip(...)`；
- `appendServerData` 调用 `HerbJadeData.write(...)`；
- `shouldRequestData` 只在目标 block 的 item 是 Herbcraft 草药时请求数据。

### 3. 新增掉落物 / 物品实体 Jade provider

新增：

```text
core/src/main/java/com/herbcraft/compat/jade/HerbNatureEntityProvider.java
```

支持：

```text
ItemEntity
```

也就是说，如果玩家看的是地上的草药掉落物，Jade 也可以显示性味状态，而不是只支持方块。

### 4. Jade plugin 同时注册 Block 与 ItemEntity

修改：

```text
core/src/main/java/com/herbcraft/compat/jade/HerbcraftJadePlugin.java
```

现在注册：

```java
registration.registerBlockDataProvider(HerbNatureProvider.INSTANCE, Block.class);
registration.registerEntityDataProvider(HerbNatureEntityProvider.INSTANCE, ItemEntity.class);
registration.addConfig(HERB_NATURE, true);
registration.addConfig(HERB_NATURE_ITEM_ENTITY, true);
registration.registerBlockComponent(HerbNatureProvider.INSTANCE, Block.class);
registration.registerEntityComponent(HerbNatureEntityProvider.INSTANCE, ItemEntity.class);
```

### 5. 新增 Jade 配置翻译

新增：

```text
config.jade.plugin_herbcraft.herb_nature
config.jade.plugin_herbcraft.herb_nature_item_entity
```

中文：

```text
百草识性
百草识性（掉落物）
```

英文：

```text
Herb Nature
Herb Nature (Item Entity)
```

## 当前 Jade 显示策略

未知：

```text
百草可食
草木有性，尚未识得
```

听闻：

```text
百草可食
百草经：听闻
性味：...
```

谙熟：

```text
百草可食
百草经：谙熟
性味：...
可食，可入盅
```

仍不显示完整数值，完整数值留给百草经。

## 构建

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew build --rerun-tasks --console=plain
```

结果：

```text
BUILD SUCCESSFUL
```

## 验证

通过：

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

额外检查：

```text
PASS - extra Jade block+item entity provider checks passed
```

Jar 确认包含：

```text
com/herbcraft/compat/jade/HerbJadeData.class
com/herbcraft/compat/jade/HerbNatureProvider.class
com/herbcraft/compat/jade/HerbNatureEntityProvider.class
com/herbcraft/compat/jade/HerbcraftJadePlugin.class
```

## 实地 smoke test

### runServer

```bash
timeout 120 ./gradlew :alchemy:runServer --console=plain
```

结果：

```text
Done (9.767s)! For help, type "help"
```

Jade 插件加载：

```text
Start loading plugin from Herbcraft | 百草可食: com.herbcraft.compat.jade.HerbcraftJadePlugin
com.herbcraft.compat.jade.HerbcraftJadePlugin loaded
```

无 Mixin / ClassNotFound 错误。

### runClient

```bash
timeout 180 ./gradlew :alchemy:runClient --console=plain
```

加载到：

```text
herbcraft 1.1.5
herbcraft_alchemy 0.2.4
jade 26.1.0+fabric
```

随后因沙盒无 DISPLAY，出现预期 GLFW 错误。这说明已越过 Fabric/Mixin/Jade 初始化阶段。

## 输出目录

```text
/home/user/herbcraft_p1_6_release/
```

## 版本保留

本轮完成后只保留：

```text
/home/user/herbcraft_p1_5_release
/home/user/herbcraft_p1_6_release
```

删除：

```text
/home/user/herbcraft_p1_4_release
```
