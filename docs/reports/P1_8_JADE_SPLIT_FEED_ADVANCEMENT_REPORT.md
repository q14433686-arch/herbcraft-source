# P1.8：Jade 不显示根因修复（拆分 Provider）+ 喂食成就真实触发 + 改名

日期：2026-06-12

## 基线

p1.7（herbcraft_p1_7_release）→ 工作目录 /home/user/herbcraft_work/herbcraft_source。
本轮产出 /home/user/herbcraft_p1_8_release/。按保留两版规则，p1.6 目录将被删除。

## 一、Jade 不显示的根因（本轮最重要发现）

本轮没有再"猜"，而是把 Jade 26.1.0+fabric 的官方 sources jar 从 Modrinth 拉下来逐文件读了。
在 `snownee/jade/impl/WailaCommonRegistration.java` 里找到：

```java
private static void checkDataProvider(IServerDataProvider<?> dataProvider) {
    if (CommonProxy.isPhysicallyClient() && dataProvider instanceof IComponentProvider) {
        throw new IllegalArgumentException(
            "Data providers cannot implement IComponentProvider since Minecraft 1.21.6.
             Use a separate client provider instead.");
    }
}
```

而 p1.6/p1.7 的写法恰好是被禁止的组合：

```java
public enum HerbNatureProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
public enum HerbNatureEntityProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor>
```

后果链：在**物理客户端**上，`plugin.register()` 调用 `registerBlockDataProvider` 时
`checkDataProvider` 直接抛 IllegalArgumentException → Jade 把 HerbcraftJadePlugin 整个标记为
erroneous 并跳过 → **block/entity component、server data、配置项全都没注册** → 游戏里看草药
没有任何 Herbcraft 行。这就是"玉始终不显示"的全部原因。

为什么以前没发现：专用服务器上 `isPhysicallyClient()` 为 false，检查不触发，所以
runServer smoke test 一直显示 "HerbcraftJadePlugin loaded"——服务端日志根本测不出这个 bug。
这正是"必须按用户实际环境（客户端）验证"的教训。

Jade 官方对照样本：原版 VanillaPlugin 全部采用"Data provider 与 Client provider 分离"
写法（如 `BrewingStandProvider` + `BrewingStandProvider.Client`）。

### 修复（完全按 Jade 26.1 官方模式重写）

拆成四个类，职责单一：

```text
HerbNatureProvider                  IBlockComponentProvider           （仅显示）
HerbNatureServerDataProvider        IServerDataProvider<BlockAccessor>（仅数据）
HerbNatureEntityProvider            IEntityComponentProvider          （仅显示）
HerbNatureEntityServerDataProvider  IServerDataProvider<EntityAccessor>（仅数据）
```

插件注册改为：

```java
register():       registerBlockDataProvider(HerbNatureServerDataProvider, Block.class)
                  registerEntityDataProvider(HerbNatureEntityServerDataProvider, ItemEntity.class)
registerClient(): registerBlockComponent(HerbNatureProvider, Block.class)
                  registerEntityComponent(HerbNatureEntityProvider, ItemEntity.class)
```

同时删除了手动 `addConfig(...)` 两行：读源码确认 `registerBlockComponent` 内部的
`tryAddConfig()` 会按 UID 自动建配置项，且 `PluginConfig.addConfig` 对重复 key 直接
`Preconditions.checkArgument` 抛错——p1.6 的手动 addConfig + 自动 tryAddConfig 用同一个
UID，在客户端本身就是第二颗雷。display/server 两半共用同一 UID（官方写法如此，
tryAddConfig 有 hasConfig 防重）。

数据键、文案、知识三态逻辑（HerbJadeData）不变。

### 客户端实测证据

沙盒装了 Xvfb 跑 `:alchemy:runClient`（物理客户端环境，正是触发 checkDataProvider 的环境）：

```text
[Render thread/INFO] (Jade) Start loading plugin from Herbcraft | 百草可食: com.herbcraft.compat.jade.HerbcraftJadePlugin
[Render thread/INFO] (Jade) com.herbcraft.compat.jade.HerbcraftJadePlugin loaded: 2.888 ms
```

无 IllegalArgumentException、无 "Failed to load plugin"、无 "Trying to load plugins again"。
p1.6/p1.7 的代码在这一步必然炸（这就是用户端从未显示的原因）；现在通过。
客户端随后加载资源至 blocks atlas 阶段因沙盒 2GB 内存被 OOM SIGKILL——这是环境限制，
发生在 Jade 插件验证点之后，不影响结论。

## 二、"喂牛吃草"成就无法达成（真 bug）+ 改名

根因：`feed_cow_grass` 和其余 30 个进度一样是 `minecraft:impossible` 触发 + 代码授予，
但全项目搜索证实**没有任何代码调用过 grant("feed_cow_grass")**——动物喂养是纯标签驱动
零 Java，当初立项时根本没有挂触发钩子。它是 31 个进度里唯一一个"无人认领"的。

修复：在 `HerbcraftAdvancementHooks` 注册 Fabric `UseEntityCallback`（docs.fabricmc.net
官方事件，永远 return PASS 不干扰原版交互）：

```text
玩家手持物品对 Animal 使用 && animal.isFood(stack) && 该物品属于扩展食谱
→ grant feed_cow_grass
```

"扩展食谱"判定 = 物品在 herbs.json 注册表中（我们加给动物的草/蕨/花/树叶全是草药）
或为枯灌木（骆驼特例，不在 herbs.json）。原版小麦/种子等不触发，因为它们不是草药。
判定走 `animal.isFood()`（即 `*_food` 标签），所以"任意对应动物 × 任意对应新食物"
都能解锁，与你的预期一致。

改名（按你的原话）：

```text
zh_cn: 它们本来就应该吃这些 / 不是吗？
en_us: They Should Eat These Anyway / Shouldn't they?
```

进度 ID `feed_cow_grass` 不改——ID 神圣不可变，只改显示文案。

## 三、构建与验证（全部实跑）

```text
./gradlew build --rerun-tasks            → BUILD SUCCESSFUL (2m 2s)
13/13 validate 脚本                       → PASS（validate_nature_jade 已更新为强制
                                            检查"拆分式"四类结构，旧合体写法会 FAIL，防回退）
:alchemy:runServer                        → Done (12.104s)，插件加载，无错误
:alchemy:runClient (Xvfb)                 → Jade 插件在物理客户端通过校验并加载（见上）
jar 内容物核对                            → 四个新 Jade 类 + HerbUsageAPI + 新钩子类 + 新文案全部在 jar
```

## 四、输出

```text
/home/user/herbcraft_p1_8_release/
  herbcraft-1.1.5.jar
  herbcraft-alchemy-0.2.4.jar
  herbcraft-cuisine-0.3.5.jar
  herbcraft-source-p1.8-jade-split-feed-advancement.zip
  SHA256SUMS.txt
```

版本保留：p1_7 + p1_8；删除 p1_6。

## 五、给作者的验证清单

1. 装 p1.8 三件套 + Jade 26.1.0+fabric，进世界对着任意花/草：应出现"百草可食"行
   （未知态显示"草木有性，尚未识得"；吃过后变谙熟并显示性味+用途）；
2. 看地上的草药掉落物也应有同样显示；
3. 拿一朵花喂山羊（或草喂牛/枯灌木喂骆驼）：应弹出"它们本来就应该吃这些"；
4. 若 Jade 仍无显示，请发 logs/latest.log——现在插件已确认能在客户端加载，
   剩余可能性只剩配置被关（Jade 设置 → 插件 → Herbcraft）或版本不符。
