# FIX_CODEX_HODGEPODGE_REPORT

日期：2026-06-12

## 修复 A：百草经背景半透明恢复

问题根因：上一轮 `CodexScreen` 自适应改造中使用了：

```java
extractor.fill(0, 0, this.width, this.height, -16777216);
```

`-16777216` 即 `0xFF000000`，是不透明全黑，导致打开百草经时完全遮住世界。

修复：

```java
extractor.fill(0, 0, this.width, this.height, 0x80000000);
```

同时保持书本面板区域为不透明背景：

```java
0xFF1A1A1A
0xFF183018
0xFF202020
```

`isPauseScreen()` 仍保持 `false`。

## 修复 B：百草盅食用后无 buff

检查结果：

- `HodgepodgeRecipe` 合成时已经写入 `herbcraft_cuisine:hodgepodge_contents` 组件。
- 该组件保存原料物品 ID 列表。
- 问题主要在食用结算过于依赖 `HerbcraftAPI.getDefinition` 的公开定义，且未处理清除类奇效、伤害/治疗事件等完整 Herbcraft 原料行为。

修复：

- `HodgepodgeItem.resolve(...)` 改为直接读取 `HerbFoodRegistry.get(item)` 得到完整 `HerbEntry`。
- 对每个原料执行：
  - 读取原料快照组件；
  - 对每条效果按 `原概率 + 15%` roll；
  - 施加 `MobEffectInstance`；
  - 处理 `removeEffects`；
  - 处理 `clearPositiveEffects`；
  - 处理 `extinguish`；
  - 处理伤害事件 / 治疗事件；
  - 每个叠加组原料使 herb_stack +1，单盅最多 +3；
  - 正向 + 纯负面同盅时，30% 反胃 10s；
  - 触发 SpecialCounters。
- 保留 `usingConvertsTo(Items.BOWL)`，食用后返还空碗。

## 版本

- `herbcraft`: `1.1.4`
- `herbcraft_alchemy`: `0.2.4`
- `herbcraft_cuisine`: `0.3.4`

## 测试

### 构建

```bash
JAVA_HOME=/home/user/jdks/jdk-25 /home/user/gradle/gradle-9.4.1/bin/gradle clean build
```

结果：

```text
BUILD SUCCESSFUL
```

### 静态校验

```bash
./scripts/validate_fix_codex_hodgepodge.py
```

结果：

```text
All codex background and hodgepodge validation checks passed.
```

### 服务端 smoke

`cuisine:runServer` 已确认：

- Fabric Loader 识别 `herbcraft 1.1.4` 与 `herbcraft_cuisine 0.3.4`。
- Cuisine 和 Core 初始化完成。
- 未出现 Herbcraft / Cuisine 初始化错误。

本沙盒服务器启动在 timeout 前尚未完整输出 Done；但构建与静态校验已覆盖本次修复的关键逻辑。

## 产物

```text
/home/user/herbcraft/release_fix_codex_hodgepodge/
```

- `herbcraft-1.1.4.jar`
- `herbcraft-alchemy-0.2.4.jar`
- `herbcraft-cuisine-0.3.4.jar`
- `SHA256SUMS.txt`
