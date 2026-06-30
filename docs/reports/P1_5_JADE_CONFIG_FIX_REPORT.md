# P1.5：Jade 显示性味修复与基线流程报告

日期：2026-06-12

## 基线执行

本轮严格执行工作基线规则：

1. 先列出 release 目录：

```text
/home/user/herbcraft_p1_3_release
/home/user/herbcraft_p1_4_release
```

2. 使用最新正确版本作为唯一基线：

```text
/home/user/herbcraft_p1_4_release/herbcraft-source-p1.4-rigorous-recheck.zip
```

3. 解压覆盖到：

```text
/home/user/herbcraft_work/herbcraft_source
```

4. 本轮所有修改、构建、验证、smoke test、打包均在该目录完成。

没有读取或使用旧 handoff 源码目录。

## 用户反馈

本轮用户确认：

- 百草经内可见性味；
- 没有严重 bug；
- 主要问题是 Jade / 玉不显示草药性味；
- 需要按当前环境联网核查 Jade，并严格按实际 API 写法修复；
- 版本保留策略改为：只保留本轮与上一轮 release。

## 联网核查结论

Jade 文档确认：

- Fabric 需要在 `fabric.mod.json` 添加 `jade` entrypoint；
- 插件实现 `IWailaPlugin`；
- 客户端通过 `registerBlockComponent` 注册展示；
- 需要服务端数据时，用 `IServerDataProvider` + `registerBlockDataProvider`；
- 1.21+ Jade 支持给无方块实体的普通 Block 注册 server data provider；
- `shouldRequestData` 用于客户端决定是否请求服务端数据。

本轮保留 Jade 26.1 线：

```text
maven.modrinth:jade:26.1.0+fabric
```

## 修复内容

### 1. Jade provider 明确启用配置项

修改：

```text
core/src/main/java/com/herbcraft/compat/jade/HerbcraftJadePlugin.java
```

新增：

```java
registration.addConfig(HERB_NATURE, true);
```

原因：Jade 文档说明注册 component provider 后会创建可开关配置项；本轮显式默认开启，避免 provider 被默认隐藏或配置缺失导致不显示。

新增配置翻译：

```text
config.jade.plugin_herbcraft.herb_nature
```

中文：

```text
百草识性
```

英文：

```text
Herb Nature
```

### 2. Jade server data 请求路径更明确

修改：

```text
core/src/main/java/com/herbcraft/compat/jade/HerbNatureProvider.java
```

保留：

```text
IBlockComponentProvider + IServerDataProvider<BlockAccessor>
```

新增/强化：

```java
@Override
public boolean shouldRequestData(BlockAccessor accessor) {
   Item item = accessor.getBlockState().getBlock().asItem();
   return item != null && HerbcraftAPI.getHerbRegistry().containsKey(item);
}
```

作用：只对 Herbcraft 草药方块请求服务端数据，确保玩家个人知识状态能同步给 Jade 客户端。

### 3. Jade tooltip fallback

`appendTooltip` 现在即使服务端数据尚未到达，也会用客户端本地 block item 判断是否为 Herbcraft 草药；这样至少能显示 Herbcraft 标识与未知提示。若 server data 到达，则根据：

```text
herbcraft:knowledge_state
```

显示：

- 未知：草木有性，尚未识得；
- 听闻：百草经：听闻 + 性味；
- 谙熟：百草经：谙熟 + 性味 + 可食，可入盅。

## 构建

命令：

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew build --rerun-tasks --console=plain
```

结果：

```text
BUILD SUCCESSFUL
```

## 静态验证

已通过：

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
PASS - explicit Jade config + server-data route checks passed
```

确认 jar 内存在：

```text
com/herbcraft/compat/jade/HerbNatureProvider.class
com/herbcraft/compat/jade/HerbcraftJadePlugin.class
assets/herbcraft/textures/gui/advancements/backgrounds/herbcraft.png
data/herbcraft/herb_natures.json
com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.class
```

## 实地 smoke test

### alchemy runServer

命令：

```bash
timeout 120 ./gradlew :alchemy:runServer --console=plain
```

结果：

```text
Done (9.942s)! For help, type "help"
```

并确认 Jade 加载 Herbcraft 插件：

```text
Start loading plugin from Herbcraft | 百草可食: com.herbcraft.compat.jade.HerbcraftJadePlugin
com.herbcraft.compat.jade.HerbcraftJadePlugin loaded
```

无 Mixin prepare / ClassNotFound 错误。

### alchemy runClient

命令：

```bash
timeout 240 ./gradlew :alchemy:runClient --console=plain
```

结果加载到：

```text
herbcraft 1.1.5
herbcraft_alchemy 0.2.4
jade 26.1.0+fabric
```

随后因沙盒无图形环境出现预期错误：

```text
Failed to initialize GLFW, X11: The DISPLAY environment variable is missing
```

这说明已越过 Fabric / Mixin / Jade 初始化阶段。该错误不是模组错误。

## 输出产物

输出目录：

```text
/home/user/herbcraft_p1_5_release/
```

包含：

```text
herbcraft-1.1.5.jar
herbcraft-alchemy-0.2.4.jar
herbcraft-cuisine-0.3.5.jar
herbcraft-1.1.5-0.2.4-0.3.5-p1.5-jade-config-fix.zip
herbcraft-source-p1.5-jade-config-fix.zip
SHA256SUMS.txt
```

## 版本保留策略执行

本轮输出 P1.5 后，只保留：

```text
/home/user/herbcraft_p1_4_release
/home/user/herbcraft_p1_5_release
```

删除：

```text
/home/user/herbcraft_p1_3_release
```

## 进程清理

runServer / runClient 后均清理残余 Java/Gradle/Minecraft 进程。最终无：

```text
GradleDaemon
KnotServer
KnotClient
net.fabricmc.devlaunchinjector
minecraft
```
