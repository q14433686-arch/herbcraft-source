#!/usr/bin/env python3
"""Validate Herbcraft raw herb food-roll balance.

This validator guards the P3.10 balance direction: common forage should not
reliably restore the visible hunger bar, hidden/fullness restoration should match item identity (small for scenery, stronger for seeds/oily seeds), and vanilla Saturation effects should not bypass the
new raw-forage food-roll model.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HERBS = ROOT / "core/src/main/resources/data/herbcraft/herbs.json"
HERB_ENTRY = ROOT / "core/src/main/java/com/herbcraft/registry/HerbEntry.java"
HERB_FOOD_REGISTRY = ROOT / "core/src/main/java/com/herbcraft/registry/HerbFoodRegistry.java"
EFFECT_RESOLVER = ROOT / "core/src/main/java/com/herbcraft/logic/EffectResolver.java"

errors: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


def close(a: float, b: float) -> bool:
    return abs(float(a) - float(b)) < 1.0e-6


def load_herbs() -> dict[str, dict]:
    root = json.loads(HERBS.read_text(encoding="utf-8"))
    herbs = root.get("herbs", [])
    require(len(herbs) == 101, f"herbs.json should still define 101 edible herb entries, got {len(herbs)}")
    by_item: dict[str, dict] = {}
    for entry in herbs:
        item = entry.get("item")
        require(isinstance(item, str) and ":" in item, f"herb entry has malformed item id: {entry!r}")
        require(item not in by_item, f"duplicate herb food entry: {item}")
        by_item[item] = entry
    return by_item


# Exact values chosen from the approved 2026-06-16 probability review ranges.
EXPECTED: dict[str, dict] = {
    "minecraft:short_grass": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.2, 0.30)}},
    "minecraft:tall_grass": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.2, 0.30)}},
    "minecraft:wildflowers": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.10), "saturation": (0.2, 0.35)}},
    "minecraft:pink_petals": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.15), "saturation": (0.2, 0.40)}},
    "minecraft:fern": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.15), "saturation": (0.3, 0.45)}},
    "minecraft:large_fern": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.15), "saturation": (0.3, 0.45)}},
    "minecraft:dandelion": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.40), "saturation": (0.2, 0.50)}},
    "minecraft:blue_orchid": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.35), "saturation": (0.3, 0.55)}},
    "minecraft:oxeye_daisy": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.40), "saturation": (0.2, 0.50)}},
    "minecraft:cornflower": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.35), "saturation": (0.2, 0.45)}},
    "minecraft:red_tulip": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.30), "saturation": (0.2, 0.40)}},
    "minecraft:orange_tulip": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.30), "saturation": (0.2, 0.40)}},
    "minecraft:pink_tulip": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.30), "saturation": (0.2, 0.40)}},
    "minecraft:white_tulip": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.30), "saturation": (0.2, 0.40)}},
    "minecraft:allium": {"nutrition": 1, "sat": 0.0, "rolls": {"saturation": (0.2, 0.50)}},
    "minecraft:lilac": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.55), "saturation": (0.2, 0.55)}},
    "minecraft:rose_bush": {"nutrition": 1, "sat": 0.0, "rolls": {"saturation": (0.2, 0.45)}},
    "minecraft:peony": {"nutrition": 1, "sat": 0.0, "rolls": {"saturation": (0.2, 0.50)}},
    "minecraft:poppy": {"nutrition": 1, "sat": 0.0, "rolls": {}},
    "minecraft:torchflower": {"nutrition": 2, "sat": 0.3, "rolls": {}},
    "minecraft:cactus_flower": {"nutrition": 1, "sat": 0.0, "rolls": {"nutrition": (1, 0.40), "saturation": (0.2, 0.50)}},
    "minecraft:sunflower": {"nutrition": 2, "sat": 0.2, "rolls": {"saturation": (0.2, 0.45)}},
    "minecraft:pitcher_plant": {"nutrition": 3, "sat": 0.5, "rolls": {}},
    "minecraft:spore_blossom": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.35)}},
    "minecraft:oak_leaves": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.3, 0.40)}},
    "minecraft:spruce_leaves": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.3, 0.45)}},
    "minecraft:birch_leaves": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.3, 0.45)}},
    "minecraft:jungle_leaves": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.3, 0.40)}},
    "minecraft:acacia_leaves": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.3, 0.40)}},
    "minecraft:dark_oak_leaves": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.3, 0.40)}},
    "minecraft:mangrove_leaves": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.3, 0.45)}},
    "minecraft:cherry_leaves": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.10), "saturation": (0.4, 0.55)}},
    "minecraft:oak_sapling": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.25), "saturation": (0.2, 0.35)}},
    "minecraft:birch_sapling": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.25), "saturation": (0.2, 0.35)}},
    "minecraft:spruce_sapling": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.25), "saturation": (0.2, 0.35)}},
    "minecraft:jungle_sapling": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.25), "saturation": (0.2, 0.35)}},
    "minecraft:acacia_sapling": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.25), "saturation": (0.2, 0.35)}},
    "minecraft:dark_oak_sapling": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.25), "saturation": (0.2, 0.35)}},
    "minecraft:pale_oak_sapling": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.15), "saturation": (0.2, 0.30)}},
    "minecraft:mangrove_propagule": {"nutrition": 1, "sat": 0.0, "rolls": {"saturation": (0.2, 0.35)}},
    "minecraft:cherry_sapling": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.35), "saturation": (0.3, 0.50)}},
    "minecraft:wheat_seeds": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.60), "saturation": (2.0, 0.30)}},
    "minecraft:beetroot_seeds": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.60), "saturation": (2.0, 0.30)}},
    "minecraft:melon_seeds": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.65), "saturation": (3.0, 0.50)}},
    "minecraft:pumpkin_seeds": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.65), "saturation": (3.0, 0.50)}},
    "minecraft:torchflower_seeds": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.60), "saturation": (2.0, 0.35)}},
    "minecraft:pitcher_pod": {"nutrition": 1, "sat": 1.0, "rolls": {}},
    "minecraft:kelp": {"nutrition": 1, "sat": 0.05, "rolls": {"saturation": (0.2, 0.35)}},
    "minecraft:seagrass": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.10), "saturation": (0.2, 0.40)}},
    "minecraft:lily_pad": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.10), "saturation": (0.3, 0.40)}},
    "minecraft:sea_pickle": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.30), "saturation": (0.2, 0.40)}},
    "minecraft:sugar_cane": {"nutrition": 1, "sat": 1.0, "rolls": {}},
    "minecraft:sugar": {"nutrition": 1, "sat": 0.05, "rolls": {}},
    "minecraft:bamboo": {"nutrition": 0, "sat": 0.0, "rolls": {"nutrition": (1, 0.10), "saturation": (0.2, 0.40)}},
    "minecraft:cocoa_beans": {"nutrition": 1, "sat": 0.2, "rolls": {}},
    "minecraft:brown_mushroom": {"nutrition": 1, "sat": 0.2, "rolls": {}},
    "minecraft:resin_clump": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.2, 0.40)}},
    "minecraft:resin_brick": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.2, 0.45)}},
    "minecraft:slime_ball": {"nutrition": 0, "sat": 0.0, "rolls": {"saturation": (0.3, 0.45)}},
    "minecraft:nether_wart": {"nutrition": 1, "sat": 0.1, "rolls": {}},
    "minecraft:fermented_spider_eye": {"nutrition": 1, "sat": 0.0, "rolls": {}},
    "minecraft:magma_cream": {"nutrition": 1, "sat": 0.1, "rolls": {}},
    "minecraft:rabbit_foot": {"nutrition": 1, "sat": 0.1, "rolls": {}},
    "minecraft:phantom_membrane": {"nutrition": 1, "sat": 0.0, "rolls": {}},
    "minecraft:egg": {"nutrition": 2, "sat": 0.2, "rolls": {}},
    "minecraft:blue_egg": {"nutrition": 2, "sat": 0.2, "rolls": {}},
    "minecraft:brown_egg": {"nutrition": 2, "sat": 0.2, "rolls": {}},
    "minecraft:honeycomb": {"nutrition": 2, "sat": 0.2, "rolls": {}},
    "minecraft:pumpkin": {"nutrition": 6, "sat": 0.2, "rolls": {}},
    "minecraft:melon": {"nutrition": 8, "sat": 0.2, "rolls": {}},
}


def validate_food_roll_shape(item: str, entry: dict) -> None:
    rolls = entry.get("food_rolls", {})
    require(isinstance(rolls, dict), f"{item}: food_rolls must be an object")
    for kind, whole_amount in (("nutrition", True), ("saturation", False)):
        if kind not in rolls:
            continue
        roll = rolls[kind]
        require(isinstance(roll, dict), f"{item}: food_rolls.{kind} must be an object")
        amount = roll.get("amount")
        chance = roll.get("chance")
        require(isinstance(amount, (int, float)) and amount > 0, f"{item}: food_rolls.{kind}.amount must be positive")
        require(isinstance(chance, (int, float)) and 0.0 <= float(chance) <= 1.0, f"{item}: food_rolls.{kind}.chance must be 0..1")
        if whole_amount:
            require(isinstance(amount, int), f"{item}: visible hunger roll amount must be an integer")
            require(amount <= 1, f"{item}: raw forage visible hunger roll should be at most +1")
        else:
            require(float(amount) <= 3.0, f"{item}: raw hidden saturation roll should stay within approved direct amount cap (<=3.0)")


def validate_expected_values(by_item: dict[str, dict]) -> None:
    for item, expected in EXPECTED.items():
        require(item in by_item, f"missing expected balanced item {item}")
        if item not in by_item:
            continue
        entry = by_item[item]
        require(entry.get("nutrition") == expected["nutrition"], f"{item}: expected nutrition {expected['nutrition']}, got {entry.get('nutrition')}")
        require(close(entry.get("saturation_modifier"), expected["sat"]), f"{item}: expected saturation_modifier {expected['sat']}, got {entry.get('saturation_modifier')}")
        rolls = entry.get("food_rolls", {})
        if expected["rolls"]:
            require(isinstance(rolls, dict), f"{item}: expected food_rolls")
        else:
            require("food_rolls" not in entry, f"{item}: should not have food_rolls")
        for kind, (amount, chance) in expected["rolls"].items():
            require(kind in rolls, f"{item}: missing food_rolls.{kind}")
            if kind in rolls:
                require(close(rolls[kind].get("amount"), amount), f"{item}: expected {kind} roll amount {amount}, got {rolls[kind].get('amount')}")
                require(close(rolls[kind].get("chance"), chance), f"{item}: expected {kind} roll chance {chance}, got {rolls[kind].get('chance')}")


by_item = load_herbs()
for item, entry in by_item.items():
    require(isinstance(entry.get("nutrition"), int) and entry.get("nutrition") >= 0, f"{item}: nutrition must be non-negative int")
    require(isinstance(entry.get("saturation_modifier"), (int, float)) and entry.get("saturation_modifier") >= 0, f"{item}: saturation_modifier must be non-negative number")
    validate_food_roll_shape(item, entry)

validate_expected_values(by_item)

# Common forage must not keep stable visible hunger unless explicitly approved as a low-saturation exception.
no_stable_visible = {
    "minecraft:short_grass", "minecraft:tall_grass", "minecraft:wildflowers", "minecraft:pink_petals",
    "minecraft:fern", "minecraft:large_fern", "minecraft:seagrass", "minecraft:lily_pad",
    "minecraft:bamboo", "minecraft:resin_clump", "minecraft:resin_brick", "minecraft:slime_ball",
    "minecraft:dandelion", "minecraft:blue_orchid", "minecraft:oxeye_daisy", "minecraft:cornflower",
    "minecraft:red_tulip", "minecraft:orange_tulip", "minecraft:pink_tulip", "minecraft:white_tulip",
    "minecraft:lilac", "minecraft:spore_blossom", "minecraft:sea_pickle",
    "minecraft:oak_leaves", "minecraft:spruce_leaves", "minecraft:birch_leaves", "minecraft:jungle_leaves",
    "minecraft:acacia_leaves", "minecraft:dark_oak_leaves", "minecraft:mangrove_leaves", "minecraft:cherry_leaves",
    "minecraft:oak_sapling", "minecraft:birch_sapling", "minecraft:spruce_sapling", "minecraft:jungle_sapling",
    "minecraft:acacia_sapling", "minecraft:dark_oak_sapling", "minecraft:pale_oak_sapling", "minecraft:cherry_sapling",
    "minecraft:wheat_seeds", "minecraft:beetroot_seeds", "minecraft:melon_seeds", "minecraft:pumpkin_seeds", "minecraft:torchflower_seeds",
}
for item in no_stable_visible:
    require(by_item[item].get("nutrition") == 0, f"{item}: common forage should not have stable visible hunger")
    require(close(by_item[item].get("saturation_modifier"), 0.0), f"{item}: common forage should not have stable vanilla saturation")

# Blue orchid was the only common flower with a guaranteed vanilla Saturation effect; it must not bypass food_rolls.
blue_orchid_effects = [effect.get("effect") for effect in by_item["minecraft:blue_orchid"].get("effects", [])]
require("minecraft:saturation" not in blue_orchid_effects, "blue_orchid should not keep a guaranteed minecraft:saturation effect after food-roll rebalance")

# Special exceptions intentionally remain strong and should not be swept into common forage rules.
require(by_item["minecraft:dried_kelp_block"].get("nutrition") == 9, "dried kelp block should remain a stable bulk/processed food exception")
require(by_item["minecraft:sugar_cane"].get("nutrition") == 1 and close(by_item["minecraft:sugar_cane"].get("saturation_modifier"), 1.0), "sugar cane should remain +1 visible hunger but restore about 2 hidden saturation points")
require(by_item["minecraft:golden_carrot"].get("nutrition") == 6, "golden carrot should remain a special food exception")
require(by_item["minecraft:glistering_melon_slice"].get("nutrition") == 4, "glistering melon slice should remain a special food exception")

# Runtime support should be data-driven and direct; no item-by-item food balance in Java.
entry_java = HERB_ENTRY.read_text(encoding="utf-8")
registry_java = HERB_FOOD_REGISTRY.read_text(encoding="utf-8")
resolver_java = EFFECT_RESOLVER.read_text(encoding="utf-8")
require("FoodRoll" in entry_java and "nutritionRoll" in entry_java and "saturationRoll" in entry_java, "HerbEntry should expose data-backed food rolls")
require('"food_rolls"' in registry_java and "readFoodRoll" in registry_java, "HerbFoodRegistry should parse food_rolls from herbs.json/startup resources")
require("applyFoodRolls" in resolver_java and "addDirectSaturation" in resolver_java, "EffectResolver should settle food_rolls after vanilla consumption")
require("getFoodData().eat(amount, 0.0F)" in resolver_java, "visible food roll should add hunger without vanilla saturation")
require("setSaturation" in resolver_java and "getSaturationLevel" in resolver_java and "getFoodLevel" in resolver_java, "saturation roll should set direct hidden saturation with clamping")

if errors:
    print("FAIL - raw herb food-roll balance validation failed")
    for error in errors:
        print(" -", error)
    raise SystemExit(1)

print("PASS - raw herb food-roll balance is data-backed, common forage lacks stable visible food, blue orchid no longer bypasses rolls, and special bulk foods remain allowlisted.")
