#!/usr/bin/env python3
"""Generate Herbcraft Alchemy brewing recipe JSON for MC 26.3.

MC 26.3 起酿造改为数据驱动：`PotionBrewing` / `FabricPotionBrewingBuilder` 被移除，
所有 mix（基料 + 修正 + 特殊）与容器转换（药水→喷溅/滞留）都必须以
`minecraft:brewing` 配方 JSON 形式随数据包发布。

本脚本是这些 JSON 的唯一生成入口，逻辑与
`alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java`
中的 `registerModifiers()` / `initialize()`（26.2 及以前的 addMix 版本）保持一致：

- 每个精华：awkward + essence_<id>            -> <potion_name>
- 含正面效果：  <potion_name> + honeycomb       -> <id>_clarified
- 含负面效果：  <potion_name> + fermented_spider_eye -> <id>_murky
- 全部：        <potion_name> + redstone        -> <id>_long
- wither_rose： <potion_name> + glowstone_dust  -> wither_venom_iii（特殊）
- 其余：        <potion_name> + glowstone_dust  -> <id>_strong
- 特殊混合：    flowering_azalea_murky + essence_wither_rose      -> undead_venom
               lily_of_the_valley_murky + essence_closed_eyeblossom -> cardiac_toxin
- 每个已注册药水（含 special_potions.json）都要有容器转换配方：
  potion(P) + gunpowder -> splash_potion(P)；splash_potion(P) + dragon_breath -> lingering_potion(P)
  （26.3 起容器转换不再是全局能力，连 vanilla 的 Potion of Luck 都失去了转化配方，
    见 minecraft.wiki《Brewing》History。不生成这些配方 = 相对 26.2 的功能回退。）

用法：
    python3 scripts/generate_brewing_recipes.py            # 校验模式（默认）：只比对，不写文件
    python3 scripts/generate_brewing_recipes.py --write    # 写入缺失/不一致的文件
    python3 scripts/generate_brewing_recipes.py --prune    # 连同 --write：删除多余文件

退出码：0 = 一致（或写入完成）；1 = 校验发现不一致 / 写入失败。
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ALCHEMY_RES = ROOT / "alchemy/src/main/resources"
ESSENCES_JSON = ALCHEMY_RES / "data/herbcraft_alchemy/essences.json"
SPECIAL_POTIONS_JSON = ALCHEMY_RES / "data/herbcraft_alchemy/special_potions.json"
BREWING_DIR = ALCHEMY_RES / "data/herbcraft/recipe/brewing"

NS = "herbcraft"
AWKWARD = "minecraft:awkward"
POTION = "minecraft:potion"
SPLASH_POTION = "minecraft:splash_potion"
LINGERING_POTION = "minecraft:lingering_potion"

# 与 registerModifiers() 中硬编码的两个特殊混合保持一致。
SPECIAL_MIXES = [
    # (input potion, reagent item, output potion, file stem)
    (f"{NS}:flowering_azalea_murky", f"{NS}:essence_wither_rose", f"{NS}:undead_venom", "undead_venom"),
    (f"{NS}:lily_of_the_valley_murky", f"{NS}:essence_closed_eyeblossom", f"{NS}:cardiac_toxin", "cardiac_toxin"),
]

CONTAINER_TRANSFORMS = [
    # (input item, reagent, output item)
    (POTION, "minecraft:gunpowder", SPLASH_POTION),
    (SPLASH_POTION, "minecraft:dragon_breath", LINGERING_POTION),
]


def load_json(path: Path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def mix_recipe(input_potion: str, reagent_item: str, output_potion: str) -> dict:
    return {
        "type": "minecraft:brewing",
        "input": {"item": POTION, "potion_contents": {"potions": input_potion}},
        "reagent": {"item": reagent_item},
        "output": {
            "id": POTION,
            "components": {"minecraft:potion_contents": {"potion": output_potion}},
        },
    }


def container_recipe(input_item: str, reagent_item: str, output_item: str, potion_id: str) -> dict:
    return {
        "type": "minecraft:brewing",
        "input": {"item": input_item, "potion_contents": {"potions": potion_id}},
        "reagent": {"item": reagent_item},
        "output": {
            "id": output_item,
            "components": {"minecraft:potion_contents": {"potion": potion_id}},
        },
    }


def build_expected() -> dict[str, dict]:
    """返回 {文件名: 配方 dict}，与 AlchemyRegistries 的注册逻辑一一对应。"""
    essences = load_json(ESSENCES_JSON)["essences"]
    specials = [p["id"] for p in load_json(SPECIAL_POTIONS_JSON)["potions"]]

    expected: dict[str, dict] = {}
    potions: list[str] = []

    for e in essences:
        eid = e["id"]
        potion = e.get("potion_name", eid)
        effects = e["effects"]
        has_positive = any(x["positive"] for x in effects)
        has_negative = any(not x["positive"] for x in effects)

        # 基料混合
        expected[f"base_{eid}.json"] = mix_recipe(AWKWARD, f"{NS}:essence_{eid}", f"{NS}:{potion}")
        potions.append(f"{NS}:{potion}")

        if has_positive:
            expected[f"clarified_{eid}.json"] = mix_recipe(f"{NS}:{potion}", "minecraft:honeycomb", f"{NS}:{eid}_clarified")
            potions.append(f"{NS}:{eid}_clarified")

        if has_negative:
            expected[f"murky_{eid}.json"] = mix_recipe(f"{NS}:{potion}", "minecraft:fermented_spider_eye", f"{NS}:{eid}_murky")
            potions.append(f"{NS}:{eid}_murky")

        expected[f"long_{eid}.json"] = mix_recipe(f"{NS}:{potion}", "minecraft:redstone", f"{NS}:{eid}_long")
        potions.append(f"{NS}:{eid}_long")

        if eid == "wither_rose":
            expected["strong_wither_rose.json"] = mix_recipe(f"{NS}:{potion}", "minecraft:glowstone_dust", f"{NS}:wither_venom_iii")
            potions.append(f"{NS}:wither_venom_iii")
        else:
            expected[f"strong_{eid}.json"] = mix_recipe(f"{NS}:{potion}", "minecraft:glowstone_dust", f"{NS}:{eid}_strong")
            potions.append(f"{NS}:{eid}_strong")

    # 特殊混合（原 initialize() 里硬编码的两个 addMix）
    for input_potion, reagent, output_potion, stem in SPECIAL_MIXES:
        expected[f"{stem}.json"] = mix_recipe(input_potion, reagent, output_potion)

    # 特殊药水（女巫投掷池等）也注册为药水，同样给容器转换
    for sid in specials:
        potions.append(f"{NS}:{sid}")

    # 容器转换：26.3 起每个药水都要显式配方才能做喷溅/滞留
    for potion_id in potions:
        for input_item, reagent, output_item in CONTAINER_TRANSFORMS:
            if input_item == POTION:
                stem = f"splash_{potion_id.split(':', 1)[1]}.json"
            else:
                stem = f"lingering_{potion_id.split(':', 1)[1]}.json"
            expected[stem] = container_recipe(input_item, reagent, output_item, potion_id)

    # 自检：特殊混合引用的输入药水必须真实存在
    for input_potion, _reagent, output_potion, _stem in SPECIAL_MIXES:
        if input_potion not in potions:
            raise SystemExit(f"特殊混合输入药水 {input_potion} 不存在于精华/特殊药水集合，请检查 essences.json")
        if output_potion not in potions:
            raise SystemExit(f"特殊混合输出药水 {output_potion} 不存在于特殊药水集合，请检查 special_potions.json")

    return expected


def dump(data: dict) -> str:
    return json.dumps(data, ensure_ascii=False, indent=2) + "\n"


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--write", action="store_true", help="写入缺失/不一致的文件")
    ap.add_argument("--prune", action="store_true", help="连同 --write：删除目录下的多余文件")
    args = ap.parse_args()

    expected = build_expected()
    BREWING_DIR.mkdir(parents=True, exist_ok=True)

    existing = {p.name for p in BREWING_DIR.glob("*.json")}
    problems: list[str] = []
    written = 0

    for name, data in sorted(expected.items()):
        path = BREWING_DIR / name
        want = dump(data)
        if not path.exists():
            if args.write:
                path.write_text(want, encoding="utf-8")
                written += 1
            else:
                problems.append(f"缺失: {name}")
        elif path.read_text(encoding="utf-8") != want:
            if args.write:
                path.write_text(want, encoding="utf-8")
                written += 1
            else:
                problems.append(f"内容不一致: {name}")

    extra = sorted(existing - set(expected))
    if extra:
        if args.write and args.prune:
            for name in extra:
                (BREWING_DIR / name).unlink()
                written += 1
        else:
            problems.append(f"多余文件（未登记于生成逻辑）: {', '.join(extra)}")

    if problems:
        for p in problems:
            print(f"FAIL: {p}")
        print(f"\n共 {len(expected)} 个期望配方，{len(problems)} 个问题。")
        if not args.write:
            print("修复：python3 scripts/generate_brewing_recipes.py --write")
        return 1

    verb = "写入" if args.write else "校验"
    print(f"{verb}通过：{len(expected)} 个酿造配方与 AlchemyRegistries 注册逻辑一致"
          + (f"（本次写入 {written} 个文件）" if args.write else "。"))
    return 0


if __name__ == "__main__":
    sys.exit(main())
