# STAGE4_REPORT

日期：2026-06-12

## 本轮任务

1. 村民交易标签验证
2. en_us 翻译差集检查
3. 死亡最后一餐
4. 食用音效差异化
5. 百草经 UI 多语言自适应

## ① 村民交易标签验证

新增脚本：

```text
scripts/validate_trade_tags.py
```

结果：

```text
All villager trade tag validation checks passed.
```

结论：

- 所有 `villager_trade/**/*.json` 都被当前模块自己的 `data/minecraft/tags/villager_trade/**` tag 引用。
- 所有 tag 均为 `replace=false`。
- 新增 P0 标签均存在：
  - weaponsmith level 2~5
  - toolsmith level 2~5
  - armorer level 2~5
  - librarian level 2/3/4/5
- alchemy / cuisine 交易保留在各自 jar 内，不污染只装 core 的资源。
- `common_smith` 未新增引用：本模组铁匠交易均是职业专属交易，直接挂到 weaponsmith/toolsmith/armorer 对应等级 tag，避免通过 common_smith 在三职业重复出现。
- 流浪商人交易当前沿用 vanilla 26.1.2 存在的 `wandering_trader/uncommon` 池；服务器数据加载测试通过。

## ② en_us 翻译差集检查

新增脚本：

```text
scripts/check_lang_diff.py
```

结果：

```text
All zh_cn/en_us language key sets match exactly.
```

本轮新增并补齐：

- `message.herbcraft.last_meal`

当前 core / alchemy / cuisine 的 `zh_cn.json` 与 `en_us.json` key 差集为 0。

## ③-A 死亡最后一餐

实现位置：

```text
core/src/main/java/com/herbcraft/logic/LastMealTracker.java
```

使用事件 / 钩子：

- 食用记录：复用现有 `HerbConsumeEvents`
- 死亡检测：Fabric API `ServerLivingEntityEvents.AFTER_DEATH`
- cuisine 菜品 / 百草盅 / 盛水的碗：在 `finishUsingItem` 服务端分支调用 `LastMealTracker.record(...)`

行为：

- 玩家死亡前 60 tick 内吃过 Herbcraft 相关食物，则死亡后额外广播：
  - 中文：`最后吃的是：%s`
  - 英文：`Last meal: %s`
- 不替换原版死亡消息。
- 记录存在服务端临时 Map，死亡后移除，不持久化，退出重进不会保留旧 last meal。

## ③-B 食用音效差异化

集中实现位置：

```text
core/src/main/java/com/herbcraft/logic/ConsumeSoundResolver.java
```

接入位置：

- Core 草药：`HerbEntry.consumable()`
- Cuisine：`CuisineItems`、`DishRegistry`

类别映射：

- 花草 / 树叶 / 普通草药：`GRASS_STEP`
- 冰雪：`GLASS_BREAK`
- 树脂 / 蜂蜜 / 粘液：`HONEY_BLOCK_SLIDE`
- 金色 / 发光 / 晶体感：`AMETHYST_BLOCK_CHIME`
- 下界菌 / 下界植物：`NETHER_WART_BREAK`
- 饮用 / 药膳 / 汤羹：`GENERIC_DRINK` 或 `HONEY_DRINK`

未新增 Mixin。

## 百草经 UI 自适应

实现位置：

```text
core/src/main/java/com/herbcraft/knowledge/client/CodexScreen.java
core/src/main/java/com/herbcraft/knowledge/client/CodexTextLayout.java
```

完成内容：

- 动态计算书本宽高、左右面板宽度。
- 左侧目录单行显示，超宽省略号截断。
- 左侧目录 hover 显示完整 tooltip；未知条目仍只显示 `???`，不泄露真实名称。
- 右侧正文统一 `font.split(...)` 自动换行。
- 右侧正文滚轮滚动，并继续使用 scissor 裁剪。
- 右侧标题支持最多两行换行，不再直接溢出面板。
- `isPauseScreen()` 保持 `false`。
- 没有修改知识解锁逻辑、章节排序、章节编号、翻译内容。

## 测试

### 构建

```bash
JAVA_HOME=/home/user/jdks/jdk-25 /home/user/gradle/gradle-9.4.1/bin/gradle clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### trade tag 校验

```bash
./scripts/validate_trade_tags.py
```

结果：

```text
All villager trade tag validation checks passed.
```

### 翻译差集

```bash
./scripts/check_lang_diff.py
```

结果：

```text
All zh_cn/en_us language key sets match exactly.
```

### 服务端 smoke

`alchemy:runServer` 已加载到 Herbcraft / Alchemy 初始化完成，未出现 Mixin apply failure、交易数据包错误或 Herbcraft 初始化错误。由于本沙盒环境启动偶发慢，最终以 `clean build` + 数据/语言脚本校验作为本轮硬验收。

## 版本与产物

- `herbcraft`: `1.1.3`
- `herbcraft_alchemy`: `0.2.4`
- `herbcraft_cuisine`: `0.3.3`

产物：

```text
/home/user/herbcraft/release_stage4/
```

- `herbcraft-1.1.3.jar`
- `herbcraft-alchemy-0.2.4.jar`
- `herbcraft-cuisine-0.3.3.jar`
- `SHA256SUMS.txt`
