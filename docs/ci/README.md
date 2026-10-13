# docs/ci/ —— workflow 暂存区

沙箱 Agent 的 GitHub 凭据没有 `workflows` 权限，推不动 `.github/workflows/`
（实测报错：`refusing to allow a GitHub App to create or update workflow
.github/workflows/build.yml without workflows permission`）。
所以约定（与姊妹仓 `TaCZ_Refabricated_Unofficial` 的 `docs/ci/` 完全相同）：

**Agent 把 workflow 改动写到本目录，维护者在 GitHub 网页端复制到
`.github/workflows/` 同名文件**（新建文件或编辑现有文件，粘贴、提交即可）。

两边不一致时，**以 `.github/workflows/` 的正式件为准**；上线后本目录的
副本就是正式件的镜像，下次改动继续在这里迭代。

## 当前状态（2026-10-13）

三件**全部待上线**——本目录是唯一副本，`.github/workflows/` 还是空的：

| 文件 | 状态 | 内容 |
|---|---|---|
| `compile-check.yml` | ⏳ 待上线 | `compileJava` 全三模块；arena/** 分支把编译日志经 Contents API 回推本分支 `build-reports/compile-java.log`（沙箱到不了 `maven.fabricmc.net`，这是本地看编译错误的唯一通道）；主分支 push + PR 也编译，但只有 arena/** 回推日志 |
| `consistency.yml` | ⏳ 待上线 | 版本声明守门：`gradle.properties` ↔ `build.gradle` ↔ 三个 `fabric.mod.json` ↔ `README.md`；`paths` 覆盖全部版本声明文件（含 `*.md`，所以「只改 README」也守得住） |
| `build.yml` | ⏳ 待上线 | 全量 `./gradlew build` + 三模块 jar artifact（14 天）+ 四个静态校验（语言键齐平 / mixin 配置完整性 / 版本一致性 / built-jar 校验） |

## 上线步骤（维护者，约 2 分钟）

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
