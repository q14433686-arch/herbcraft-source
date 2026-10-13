# docs/ci/ —— workflow 暂存区

> **分支 `arena/dcbd25af-herbcraft-source` 说明（2026-10-14 更新）**：本分支的 `.github/workflows/` 已经存在，
> 四个 workflow（`build`、`compile-check`、`consistency`、`runtime-smoke`）由维护者上线（提交 `32feb5d`、`aedee0a`）。
> 早先 GitHub 拒绝推送含 workflow 改动的提交（沙箱凭据没有 `workflows` 权限），所以本分支历史里去掉了 `eb0ade3`、`eb9f72a`、`cd31557`。
> 本目录保留这四个文件的副本；以 `.github/workflows/` 为准。

沙箱 Agent 的 GitHub 凭据没有 `workflows` 权限，推不动 `.github/workflows/`
（实测报错：`refusing to allow a GitHub App to create or update workflow
.github/workflows/build.yml without workflows permission`）。
所以约定（与姊妹仓 `TaCZ_Refabricated_Unofficial` 的 `docs/ci/` 完全相同）：

**Agent 把 workflow 改动写到本目录，维护者在 GitHub 网页端复制到
`.github/workflows/` 同名文件**（新建文件或编辑现有文件，粘贴、提交即可）。

两边不一致时，**以 `.github/workflows/` 的正式件为准**；上线后本目录的
副本就是正式件的镜像，下次改动继续在这里迭代。

## 当前状态（2026-10-14）

四件**已全部上线**：维护者已在 GitHub 网页端建好 `.github/workflows/` 下同名四个
文件，内容与本目录副本一致（仅 `build.yml` / `compile-check.yml` 少了后来补的
`chmod +x gradlew` 一行，见「已知注意点」）。

| 文件 | 状态 | 内容 |
|---|---|---|
| `compile-check.yml` | ✅ 已上线并跑绿 | `compileJava` 全三模块；arena/** 分支把编译日志经 Contents API 回推本分支 `build-reports/compile-java.log`（沙箱到不了 `maven.fabricmc.net`，这是本地看编译错误的唯一通道）；主分支 push + PR 也编译，但只有 arena/** 回推日志 |
| `consistency.yml` | ✅ 已上线，**尚未触发过** | 版本声明守门：`gradle.properties` ↔ `build.gradle` ↔ 三个 `fabric.mod.json` ↔ `README.md`；`paths` 只覆盖版本声明文件，所以「只改 README」也守得住。因为历次提交都没碰这些路径，它一次都没跑过——改 `gradle.properties` 即可触发 |
| `runtime-smoke.yml` | ✅ 已上线并跑绿（`81b5e07`） | 用 Loom `:core` / `:alchemy` / `:cuisine` `runServer` 拉起无头专用服务端，只有日志到达 `Done (` 且无 mixin apply 失败、无崩溃才算通过；日志回推 `build-reports/runtime-smoke.log`。前两轮（`aedee0a`、`5e497c7`）红在 26.3 mixin 签名上，见 `docs/reports/PORTING_26_3_FABRIC_20261013.md` |
| `build.yml` | ✅ 已上线并跑绿 | 全量 `./gradlew build` + 三模块 jar artifact（14 天）+ 六个静态校验（语言键齐平 / mixin 配置完整性 / 版本一致性 / 酿造配方与生成器一致 / 村民交易 26.3 schema / built-jar 校验） |

### 运行记录（`arena/99a30c9a-herbcraft-source`）

| commit | 结果 | 原因 |
|---|---|---|
| `eb0ade3` / `eb9f72a` | ❌ | `gradlew` 以 mode `100644` 提交 → `./gradlew: Permission denied`，**不是代码问题** |
| `e2ba41c` | ❌ | 可执行位修好（`git update-index --chmod=+x gradlew`）后暴露真问题：`:core`/`:cuisine` 编译通过，`:alchemy` 3 个 loot API 错误 |
| `e64f1bf` | ✅ **全绿** | `AlchemyCodexHooks` 迁到 26.3 number provider API：`ConstantValue` → `providers.number.ints.ContextIntProviders.exactly(int)`（返回 `Holder<ContextIntProvider>`），`LootPoolSingletonContainer.Builder<?>` → `var`。三个 `compileJava` + 全量 `build` + 六个静态校验全过 |

## 上线步骤（维护者，约 2 分钟）

> 三个文件都已上线，本节留作**下次改动**的流程：把本目录改好的文件全文粘贴到
> `.github/workflows/` 同名文件，提交即可。注意本目录副本可能比线上多
> `chmod +x gradlew`（线上是早期上传的版本），粘的时候别把已有内容覆盖丢了。

1. 打开 <https://github.com/q14433686-arch/herbcraft-source/new/main?filename=.github/workflows/build.yml>
   （`compile-check.yml`、`consistency.yml` 同理，改 `filename` 即可）。
2. 把本目录对应文件全文粘贴进去，Commit new file。
3. 三个文件都上线后，`arena/**` 分支的每次 push 都会自动：
   - 跑 `compile-check`，把编译日志写回分支里的 `build-reports/compile-java.log`；
   - 跑 `build`，产出可下载的 jar artifact；
   - 跑 `consistency`，版本声明不同步就让 PR 变红。

> 或者：在 Arena 的 GitHub 连接里给本仓授予 `Workflows` 权限后重连，
> Agent 就能自己推 `.github/workflows/`，不再需要手动复制。

## 已知注意点

- artifact 名只用 `github.sha`：`github.ref_name` 在 arena 分支带 `/`，
  是 artifact 名的非法字符。
- `compile-check.yml` 的 `paths-ignore: build-reports/**` 是防死循环的：
  日志回推本身是一次 commit，不能再触发自己。
- `build.yml` 的 `paths-ignore` 里有 `*.md` 和 `docs/**`：文档改动不该触发
  全量构建；「只改文档但改了版本号」由 `consistency.yml` 独立守门。
- 三个模块的 mixin 配置完整性检查同时覆盖 `herbcraft.mixins.json`（core）
  与 `herbcraft_alchemy.mixins.json`（alchemy）；cuisine 没有 mixin。
- **`gradlew` 必须带可执行位**：本仓历史上以 mode `100644` 提交过 `gradlew`，
  GitHub 网页上传/部分检出也会丢 mode，表现为
  `./gradlew: Permission denied`（编译步骤直接失败，且**不是**代码问题）。
  已在 git 里改为 `100755`，并且 workflow 里都显式 `chmod +x gradlew` 兜底。
  如果哪天编译又报 Permission denied，先查 `git ls-files -s gradlew`。
- `build.yml` 新增的两个数据校验（`validate_brewing_recipes.py` /
  `migrate_villager_trades_26_3.py`）是 26.3 迁移的一部分：酿造配方与村民交易
  在 26.3 都改了 schema，且**旧写法不会报错、只会被静默忽略**，所以必须由 CI 守门，
  不能只靠人记得。两者都是纯 Python、秒级完成。
