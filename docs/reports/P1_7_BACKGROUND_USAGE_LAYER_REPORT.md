# P1.7：进度背景格式修复 + 用途层（usage hints）一期报告

日期：2026-06-12

## 基线

本轮基线 = GitHub 仓库根目录的 `herbcraft-source-p1.6-jade-rewrite..zip`（作者确认根目录三个 jar 即 p1.6 最新产物），解压到：

```text
/home/user/herbcraft_work/herbcraft_source
```

按工作基线强制规则，已将 p1.6 三 jar + 源码 zip 存档为 `/home/user/herbcraft_p1_6_release/`，本轮产物输出到全新 `/home/user/herbcraft_p1_7_release/`。

## 修复 1：进度页背景紫黑缺失贴图（P0）

根因（联网查证，minecraft.wiki Java Edition 1.21.5 更新日志）：

> The field `background` no longer contains `textures/` prefix and `.png` suffix.

p1.6 的 root.json 使用旧格式 `herbcraft:textures/gui/advancements/backgrounds/herbcraft.png`，在 26.1.2 下被二次拼接成不存在的路径，导致紫黑棋盘格。贴图文件本身一直存在。

修改：

```text
core/src/main/resources/data/herbcraft/advancement/root.json
  "background": "herbcraft:gui/advancements/backgrounds/herbcraft"
```

防复发：`scripts/validate_p1_codex_advancements.py` 新增两条断言：
1. root background 必须等于新格式字符串；
2. 任何 advancement 的 background 含 `textures/` 前缀或 `.png` 后缀即 FAIL。

## 新增 2：用途层（识性系统二期 · usage hints）

解决作者反馈"百草册/Jade 只能看到属性（buff），看不到用途（能做草药/吃的/精华）"。

架构（严格遵守 core 不依赖附属铁律）：

```text
core:    com/herbcraft/api/HerbUsageAPI.java    —— 注册表 + usageKeys(Item) + usageLine()
         core 自注册：96 个 herbs.json 条目 → "可食"
alchemy: knowledge/AlchemyUsageHints.java       —— 39 种精华原料 → "可萃精华"
         （tulip 四色 / fern+large_fern 变体共享精华，已按配方实际来源展开）
cuisine: knowledge/CuisineUsageHints.java       —— #herbcraft:herb → "可入盅"
                                                   dishes.json 全部 ingredients → "可入药膳"
```

信息分层纪律：只披露类别，不披露具体菜名/精华名；隐藏菜（百花宴/剧毒蛊）不会被剧透——它们的原料只显示"可入药膳"，与普通菜原料无区别。

展示位置：

1. **百草经谙熟态**：性味行 + 诗意提示行（顺手修复：p1.6 谙熟态丢失了 traitHint，本轮找回）+ 金色"用途：可食，可入盅，可萃精华…"行 + 原有数值；听闻态不变（不剧透用途）。
2. **Jade 谙熟态**：server data 新增 `herbcraft:usages` 键，客户端拼接为同一格式的用途行；无 usage 数据时回退旧文案"可食，可入盅"。未知/听闻态 Jade 不显示用途。

新增 lang（zh/en 成对，check_lang_diff PASS）：

```text
core:    usage.herbcraft.line / usage.herbcraft.separator / usage.herbcraft.edible
alchemy: usage.herbcraft.essence
cuisine: usage.herbcraft.hodgepodge / usage.herbcraft.cuisine
```

注意：usage 注册发生在各模块 onInitialize（客户端与服务端均执行），数据两侧一致，无需网络同步。百草经的行文本经 CodexSnapshotPayload 由服务端下发，附属未安装时对应用途自然缺席，模块正交性保持。

## 问题 3 结论：动物吸引间隔 = 原版机制，非 bug

本模组动物喂养为纯标签驱动零代码。原版 TemptGoal 在引诱中断后有 100 tick（5 秒）calm-down 冷却；山羊/骆驼等另有 temptation_cooldown_ticks 记忆（默认亦 100 tick）。维持原版行为，不修。

## 构建与验证（本轮全部实际执行）

```bash
JAVA_HOME=jdk-25 ./gradlew build --rerun-tasks   → BUILD SUCCESSFUL
```

验证脚本 13/13 PASS：

```text
check_lang_diff / validate_catalyst_mixin / validate_p1_codex_advancements
validate_nature_jade / verify_built_jars / validate_recipe_isolation
validate_trade_tags / validate_item_textures / validate_task_abcd
validate_extended_features / validate_bugfix_advanced_potion_trade
validate_panel_hodge_recipe / validate_animal_food_tags
（animal_food_tags 需要原版 client jar 缓存，已从 loom 缓存软链接到
 /home/user/cache/minecraft/client-26.1.2.jar 后 PASS）
```

Smoke test：

```text
:alchemy:runServer → Done (10.163s)!；herbcraft 1.1.5 / herbcraft_alchemy 0.2.4 / jade 26.1.0+fabric 全部加载；
                     HerbcraftJadePlugin loaded: 1.574 ms；无 Mixin/ClassNotFound 错误
:cuisine:runServer → Done (10.361s)!；32 道菜注册；HerbcraftJadePlugin loaded；无错误
```

Jar 内容复核：三 jar 均含本轮新增类；core jar 内 root.json background 已是新格式；HerbUsageAPI.class / AlchemyUsageHints.class / CuisineUsageHints.class 均在；BrewingStandCatalystMixin.class 仍在。

进程清理：runServer 后已 kill 全部 KnotServer / GradleDaemon。

## 输出

```text
/home/user/herbcraft_p1_7_release/
  herbcraft-1.1.5.jar
  herbcraft-alchemy-0.2.4.jar
  herbcraft-cuisine-0.3.5.jar
  herbcraft-source-p1.7-background-usage-layer.zip
  SHA256SUMS.txt
```

## 版本保留策略执行

保留：herbcraft_p1_6_release（上一轮）、herbcraft_p1_7_release（本轮）。
更早版本目录：本环境中不存在，无需删除。

## 待作者游戏内确认

1. 进度页背景是否正常显示（及 64×64 平铺观感是否满意，如不满意下轮换 16×16）；
2. 百草经谙熟条目"用途："行是否符合预期粒度；
3. Jade 谙熟态用途行；以及掉落物 Jade 性味是否正常出现（若仍只显示"尚未识得"，按 P1.6 报告排查清单继续查 server data 同步）。
