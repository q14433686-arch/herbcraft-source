#!/usr/bin/env python3
"""Migrate Herbcraft villager_trade JSON to the MC 26.3 format.

26.3 改了村民交易的数据包 schema（见 minecraft.wiki《Villager trade definition》History
与《Item modifier》Format / 26.3 snap4 条目）：

1. `given_item_modifiers` -> `given_item_modifier`（snap4 改名，且该字段不支持
   命名空间 ID / 标签 ID，只能是内联 item modifier 列表）；
2. 内联 item modifier 的 `function` 字段 -> `type`（snap4，全部 loot function 统一）。
   旧写法**不会报错，只会被静默忽略** —— 药水交易的 `set_potion` / `set_components`
   会全部失效，村民开始卖空药水；
3. `wants` / `gives` / `additional_wants` 的 `count` 与 `max_uses` / `xp` /
   `reputation_discount` 在 26.3-pre1 起为必填（inline `context_int_provider` /
   `context_float_provider`；纯数字即常量提供器，仓库里现有的 `16.0` 形式合法）。

用法：
  python3 scripts/migrate_villager_trades_26_3.py           # 只检查，报告需要迁移的文件
  python3 scripts/migrate_villager_trades_26_3.py --write   # 就地迁移
退出码：0 = 已全部是 26.3 格式；1 = 检查模式下仍有待迁移文件（--write 后恒为 0）。
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TRADE_GLOBS = [
    "*/src/main/resources/data/**/villager_trade/**/*.json",
    "docs/examples/**/villager_trade/**/*.json",
]
ITEM_STACK_FIELDS = ("wants", "gives", "additional_wants")
MODIFIER_KEYS = ("given_item_modifiers", "given_item_modifier")
KNOWN_MODIFIER_TYPES = {
    "minecraft:set_potion",
    "minecraft:set_components",
    "minecraft:set_count",
    "minecraft:enchant_randomly",
    "minecraft:enchant_with_levels",
    "minecraft:discard",
    "minecraft:filtered",
}


def trade_files() -> list[Path]:
    found: list[Path] = []
    for pattern in TRADE_GLOBS:
        found.extend(ROOT.glob(pattern))
    return sorted(set(found))


def needs_migration(data: dict) -> list[str]:
    """返回该交易在 26.3 下的问题列表（空列表 = 已是 26.3 格式）。"""
    problems: list[str] = []
    for key in ITEM_STACK_FIELDS:
        stack = data.get(key)
        if isinstance(stack, dict) and "count" not in stack:
            problems.append(f"{key} 缺少必填 count")
    if "given_item_modifiers" in data:
        problems.append("given_item_modifiers 已改名为 given_item_modifier")
    for key in MODIFIER_KEYS:
        for index, modifier in enumerate(data.get(key, []) or []):
            if not isinstance(modifier, dict):
                problems.append(f"{key}[{index}] 不是对象")
                continue
            if "function" in modifier:
                problems.append(f"{key}[{index}] 的 function 字段已改名为 type")
            kind = modifier.get("type") or modifier.get("function")
            if kind is not None and kind not in KNOWN_MODIFIER_TYPES:
                problems.append(f"{key}[{index}] 使用未知 modifier 类型 {kind}")
    return problems


def migrate(data: dict) -> tuple[dict, list[str]]:
    """就地迁移（返回新 dict 与已执行的动作列表）。动作列表为空 = 无需改动。"""
    actions: list[str] = []
    out: dict = {}
    for key, value in data.items():
        if key == "given_item_modifiers":
            actions.append("given_item_modifiers -> given_item_modifier")
            out["given_item_modifier"] = value
            continue
        if key in ITEM_STACK_FIELDS and isinstance(value, dict) and "count" not in value:
            value = {**value, "count": 1}
            actions.append(f"{key}.count = 1")
        out[key] = value
    for key in MODIFIER_KEYS:
        modifiers = out.get(key)
        if not isinstance(modifiers, list):
            continue
        new_modifiers = []
        for modifier in modifiers:
            if isinstance(modifier, dict) and "function" in modifier:
                modifier = {"type" if k == "function" else k: v for k, v in modifier.items()}
                actions.append(f"{key}[]: function -> type")
            new_modifiers.append(modifier)
        out[key] = new_modifiers
    return out, actions


def dump(data: dict) -> str:
    return json.dumps(data, ensure_ascii=False, indent=2) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--write", action="store_true", help="就地写入迁移结果（默认只检查）")
    args = parser.parse_args()

    files = trade_files()
    if not files:
        print("FAIL: 没有找到任何 villager_trade JSON")
        return 1

    pending: list[tuple[Path, list[str]]] = []
    changed = 0
    for path in files:
        data = json.loads(path.read_text(encoding="utf-8"))
        problems = needs_migration(data)
        if not problems:
            continue
        if not args.write:
            pending.append((path, problems))
            continue
        migrated, actions = migrate(data)
        if not needs_migration(migrated):
            path.write_text(dump(migrated), encoding="utf-8")
            changed += 1
        else:
            pending.append((path, needs_migration(migrated)))

    if pending:
        for path, problems in pending:
            rel = path.relative_to(ROOT)
            print(f"FAIL: {rel}: {'; '.join(problems)}")
        print(f"\n共 {len(pending)} 个交易文件需要迁移到 26.3 格式。"
              f"修复：python3 scripts/migrate_villager_trades_26_3.py --write")
        return 1

    if args.write:
        print(f"村民交易迁移完成：{changed} 个文件已改写为 26.3 格式"
              f"（given_item_modifier + type + 必填 count）。")
    else:
        print(f"村民交易 26.3 格式校验通过：{len(files)} 个交易文件全部使用 "
              f"given_item_modifier / type / 必填 count。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
