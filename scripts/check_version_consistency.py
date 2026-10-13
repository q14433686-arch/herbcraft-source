#!/usr/bin/env python3
"""Herbcraft 版本一致性自检（参照 TaCZ_Refabricated_Unofficial 的 CI 守门思路改写）。

守门对象（三模块 Gradle 工程）：

    gradle.properties                唯一的版本事实来源
    build.gradle                     三个模块的 archivesName / version
    {core,alchemy,cuisine}/fabric.mod.json   三个 fabric.mod.json 的 version 与 depends 约束
    README.md                        「Locked environment」小节与模块表
    gradle/wrapper/gradle-wrapper.properties  Gradle Wrapper 版本

用法：
    python3 scripts/check_version_consistency.py            # 检查工作区，不一致即退出码 1
    python3 scripts/check_version_consistency.py --quiet    # 只输出失败项

退出码：0 = 全部一致；1 = 存在不一致；2 = 参数或文件错误。
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODULES = ("core", "alchemy", "cuisine")
# build.gradle 里 cuisine 单独一个版本、其余模块共用一个版本。
DEFAULT_VERSION_MODULES = ("core", "alchemy")

failures: list[str] = []
passes: list[str] = []


def fail(msg: str) -> None:
    failures.append(msg)


def ok(msg: str) -> None:
    passes.append(msg)


def read(rel: str) -> str:
    p = ROOT / rel
    if not p.exists():
        fail(f"缺少文件: {rel}")
        return ""
    return p.read_text(encoding="utf-8")


def gradle_props() -> dict[str, str]:
    props: dict[str, str] = {}
    for line in read("gradle.properties").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        props[k.strip()] = v.strip()
    return props


def main() -> int:
    quiet = "--quiet" in sys.argv[1:]
    unknown = [a for a in sys.argv[1:] if a.startswith("-") and a != "--quiet"]
    if unknown:
        print(f"未知参数：{unknown}（可用 --quiet）", file=sys.stderr)
        return 2

    props = gradle_props()
    for key in (
        "minecraft_version",
        "loader_version",
        "loom_version",
        "fabric_api_version",
        "jade_version",
        "jei_version",
        "rei_version",
        "architectury_version",
    ):
        if not props.get(key):
            fail(f"gradle.properties 缺少 {key}")

    mc = props.get("minecraft_version", "")
    loader = props.get("loader_version", "")
    loom = props.get("loom_version", "")
    api = props.get("fabric_api_version", "")
    jade = props.get("jade_version", "")
    # '26.2.1+fabric' -> '26.2.1'，用于和 fabric.mod.json 的 suggests.jade 约束比较
    jade_semver = jade.split("+", 1)[0]

    # ---------- build.gradle 模块版本 ----------
    build_gradle = read("build.gradle")
    m = re.search(r"version\s*=\s*project\.name\s*==\s*'cuisine'\s*\?\s*'([^']+)'\s*:\s*'([^']+)'", build_gradle)
    if m:
        cuisine_version, default_version = m.group(1), m.group(2)
        ok(f"build.gradle: cuisine={cuisine_version}, 其余模块={default_version}")
    else:
        cuisine_version = default_version = ""
        fail("build.gradle 中找不到 version = project.name == 'cuisine' ? '...' : '...' 声明")

    def module_version(module: str) -> str:
        return cuisine_version if module == "cuisine" else default_version

    # ---------- 三个 fabric.mod.json ----------
    import json

    for module in MODULES:
        rel = f"{module}/src/main/resources/fabric.mod.json"
        raw = read(rel)
        if not raw:
            continue
        try:
            mod = json.loads(raw)
        except json.JSONDecodeError as e:
            fail(f"{rel}: JSON 解析失败: {e}")
            continue
        name = mod.get("id", module)
        # 1) version 与 build.gradle 一致
        want = module_version(module)
        if mod.get("version") != want:
            fail(f"{rel}: version 是 '{mod.get('version')}'，build.gradle 是 '{want}'")
        else:
            ok(f"{name}: version {want}")
        # 2) depends 约束
        depends = mod.get("depends", {})
        if depends.get("minecraft") != f"~{mc}":
            fail(f"{rel}: depends.minecraft 是 '{depends.get('minecraft')}'，gradle.properties minecraft_version={mc}")
        else:
            ok(f"{name}: depends.minecraft ~{mc}")
        if depends.get("fabric-api") != f">={api}":
            fail(f"{rel}: depends.fabric-api 是 '{depends.get('fabric-api')}'，gradle.properties fabric_api_version={api}")
        else:
            ok(f"{name}: depends.fabric-api >={api}")
        if depends.get("fabricloader") != f">={loader}":
            fail(f"{rel}: depends.fabricloader 是 '{depends.get('fabricloader')}'，gradle.properties loader_version={loader}")
        else:
            ok(f"{name}: depends.fabricloader >={loader}")
        # 3) core 的 jade suggests
        if module == "core":
            suggests = mod.get("suggests", {})
            if suggests.get("jade") != f">={jade_semver}":
                fail(f"{rel}: suggests.jade 是 '{suggests.get('jade')}'，gradle.properties jade_version={jade}")
            else:
                ok(f"{name}: suggests.jade >={jade_semver}")

    # ---------- README ----------
    readme = read("README.md")

    def readme_env(label: str) -> str | None:
        m = re.search(rf"^- {re.escape(label)} `([^`]+)`", readme, re.M)
        return m.group(1) if m else None

    for label, value, desc in (
        ("Minecraft", mc, "minecraft_version"),
        ("Fabric Loader", loader, "loader_version"),
        ("Fabric API", api, "fabric_api_version"),
        ("Fabric Loom", loom, "loom_version"),
    ):
        got = readme_env(label)
        if got is None:
            fail(f"README.md 「Locked environment」缺少 '- {label} `...`' 一行")
        elif got != value:
            fail(f"README.md 「Locked environment」{label} 写 '{got}'，gradle.properties {desc}={value}")
        else:
            ok(f"README: {label} {value}")

    # Java 版本：gradle.properties 没有 java 声明，以 README 与 build.gradle 的 release 为准
    readme_java = readme_env("Java")
    gradle_java = re.search(r"options\.release\s*=\s*(\d+)", build_gradle)
    if readme_java and gradle_java and readme_java != gradle_java.group(1):
        fail(f"README.md Java 写 '{readme_java}'，build.gradle options.release={gradle_java.group(1)}")
    elif readme_java and gradle_java:
        ok(f"README/build.gradle Java {readme_java}")

    # Gradle Wrapper：README 与 gradle-wrapper.properties 一致
    readme_gradle = readme_env("Gradle Wrapper")
    wrapper = read("gradle/wrapper/gradle-wrapper.properties")
    wm = re.search(r"gradle-(\d+(?:\.\d+)*)-bin\.zip", wrapper)
    if readme_gradle and wm:
        if readme_gradle != wm.group(1):
            fail(f"README.md Gradle Wrapper 写 '{readme_gradle}'，gradle-wrapper.properties 是 '{wm.group(1)}'")
        else:
            ok(f"README/wrapper Gradle {readme_gradle}")

    # 模块表版本：| `core` | `herbcraft` | `1.1.4` | ...
    for module, mod_id in (("core", "herbcraft"), ("alchemy", "herbcraft_alchemy"), ("cuisine", "herbcraft_cuisine")):
        want = module_version(module)
        row = re.search(rf"^\|\s*`{module}`\s*\|\s*`{mod_id}`\s*\|\s*`([^`]+)`\s*\|", readme, re.M)
        if not row:
            fail(f"README.md 模块表找不到 {module} (`{mod_id}`) 行")
        elif row.group(1) != want:
            fail(f"README.md 模块表 {module} 版本写 '{row.group(1)}'，build.gradle 是 '{want}'")
        else:
            ok(f"README: 模块表 {module} {want}")

    # ---------- 汇总 ----------
    if not quiet:
        for p in passes:
            print(f"  ok:   {p}")
        for f in failures:
            print(f"  FAIL: {f}")
        print(f"\n通过 {len(passes)} · 失败 {len(failures)}")
    else:
        for f in failures:
            print(f"FAIL: {f}")

    if failures:
        print("版本声明不一致。修改 gradle.properties 后必须同步：build.gradle（模块版本）、"
              "三个 fabric.mod.json（version / depends / suggests）与 README.md"
              "（Locked environment、模块表、Gradle Wrapper）。", file=sys.stderr)
        return 1
    if not quiet:
        print("版本一致性全部通过。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
