#!/usr/bin/env python3
"""Validate Herbcraft Alchemy brewing recipes against the Java registration logic.

MC 26.3 起酿造是数据驱动的；本校验保证：

1. `data/herbcraft/recipe/brewing/*.json` 与
   `scripts/generate_brewing_recipes.py`（= AlchemyRegistries.registerModifiers() 的
   逻辑镜像）逐字一致，没有缺失、没有漂移、没有多余文件；
1b. 容器转换链与 vanilla `BrewingProvider#addContainerTransformations` 一致：
   `potion + gunpowder -> splash_potion`、`splash_potion + dragon_breath -> lingering_potion`；
2. Java 侧不再引用已被 26.3 移除的 API（FabricPotionBrewingBuilder / addMix / PotionBrewing），
   防止有人重新把 mix 注册写回代码而与 JSON 配方脱节；
3. 每个 mix 配方引用的药水都来自 essences.json / special_potions.json 推导出的药水集合；
4. `BrewingStandCatalystMixin` 仍在 mixin 配置中（隐藏厨艺催化剂 / 蒸馏精华酿造依赖它）。

用法：python3 scripts/validate_brewing_recipes.py
退出码：0 = 全部通过；1 = 发现问题。
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

import generate_brewing_recipes as gen  # noqa: E402

ALCHEMY_JAVA = ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java"
ALCHEMY_MIXINS_JSON = ROOT / "alchemy/src/main/resources/herbcraft_alchemy.mixins.json"

FORBIDDEN_JAVA_PATTERNS = [
    (r"FabricPotionBrewingBuilder", "Fabric API 26.3 已移除 FabricPotionBrewingBuilder"),
    (r"\.addMix\(", "26.3 起 mix 必须是 data/herbcraft/recipe/brewing/*.json"),
    (r"import net\.minecraft\.world\.item\.alchemy\.PotionBrewing", "26.3 已移除 PotionBrewing 类"),
]


def main() -> int:
    failures: list[str] = []
    expected = gen.build_expected()

    # 1) 磁盘文件与生成逻辑逐字一致
    brewing_dir = gen.BREWING_DIR
    on_disk = {p.name: p.read_text(encoding="utf-8") for p in brewing_dir.glob("*.json")} if brewing_dir.exists() else {}
    for name, data in sorted(expected.items()):
        want = gen.dump(data)
        if name not in on_disk:
            failures.append(f"缺失配方文件: recipe/brewing/{name}")
        elif on_disk[name] != want:
            failures.append(f"配方内容与生成逻辑不一致: recipe/brewing/{name}（跑 scripts/generate_brewing_recipes.py --write）")
    for name in sorted(set(on_disk) - set(expected)):
        failures.append(f"目录中存在未登记的多余配方: recipe/brewing/{name}（确认后删除或更新生成逻辑）")

    # 2) JSON 可解析 + 引用完整性
    essences = gen.load_json(gen.ESSENCES_JSON)["essences"]
    specials = [p["id"] for p in gen.load_json(gen.SPECIAL_POTIONS_JSON)["potions"]]
    known_potions = {f"{gen.NS}:{e.get('potion_name', e['id'])}" for e in essences}
    for e in essences:
        eid = e["id"]
        if any(x["positive"] for x in e["effects"]):
            known_potions.add(f"{gen.NS}:{eid}_clarified")
        if any(not x["positive"] for x in e["effects"]):
            known_potions.add(f"{gen.NS}:{eid}_murky")
        known_potions.add(f"{gen.NS}:{eid}_long")
        known_potions.add(f"{gen.NS}:{eid}_strong" if eid != "wither_rose" else f"{gen.NS}:wither_venom_iii")
    known_potions.update(f"{gen.NS}:{s}" for s in specials)

    for name, data in sorted(expected.items()):
        try:
            json.loads(gen.dump(data))
        except json.JSONDecodeError as e:
            failures.append(f"配方不是合法 JSON: {name}: {e}")
            continue
        for field in ("input", "output"):
            potion_ref = data[field].get("components", {}).get("minecraft:potion_contents", {}).get("potion")
            if potion_ref is None:
                potion_ref = data[field].get("potion_contents", {}).get("potions")
            if potion_ref and potion_ref not in known_potions and potion_ref != gen.AWKWARD:
                failures.append(f"{name}: {field} 引用未知药水 {potion_ref}")

    # 2b) 容器转换链必须与 vanilla `BrewingProvider#addContainerTransformations` 一致：
    #     potion + gunpowder -> splash_potion；splash_potion + dragon_breath -> lingering_potion
    for name, data in sorted(expected.items()):
        if name.startswith("splash_"):
            if data["input"]["item"] != gen.POTION or data["reagent"]["item"] != "minecraft:gunpowder" \
                    or data["output"]["id"] != gen.SPLASH_POTION:
                failures.append(f"{name}: 喷溅转换必须是 potion + gunpowder -> splash_potion")
        elif name.startswith("lingering_"):
            if data["input"]["item"] != gen.SPLASH_POTION or data["reagent"]["item"] != "minecraft:dragon_breath" \
                    or data["output"]["id"] != gen.LINGERING_POTION:
                failures.append(f"{name}: 滞留转换必须是 splash_potion + dragon_breath -> lingering_potion")

    # 3) Java 回归守门（先剥掉注释，避免文档性说明误报）
    if ALCHEMY_JAVA.exists():
        raw = ALCHEMY_JAVA.read_text(encoding="utf-8")
        java = re.sub(r"/\*.*?\*/", "", raw, flags=re.S)
        java = re.sub(r"//[^\n]*", "", java)
        for pattern, why in FORBIDDEN_JAVA_PATTERNS:
            if re.search(pattern, java):
                failures.append(f"AlchemyRegistries.java 仍包含已移除 API（{why}）: /{pattern}/")
    else:
        failures.append(f"找不到 {ALCHEMY_JAVA}")

    # 4) mixin 仍在配置中
    if ALCHEMY_MIXINS_JSON.exists():
        mixins = json.loads(ALCHEMY_MIXINS_JSON.read_text(encoding="utf-8"))
        if "BrewingStandCatalystMixin" not in mixins.get("mixins", []):
            failures.append("herbcraft_alchemy.mixins.json 中缺少 BrewingStandCatalystMixin")
    else:
        failures.append(f"找不到 {ALCHEMY_MIXINS_JSON}")

    if failures:
        for f in failures:
            print(f"FAIL: {f}")
        print(f"\n酿造配方校验未通过（{len(failures)} 个问题）。")
        return 1

    print(f"酿造配方校验通过：{len(expected)} 个配方与 AlchemyRegistries 注册逻辑一致，"
          f"Java 侧无已移除 API 残留，BrewingStandCatalystMixin 已注册。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
