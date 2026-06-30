#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CUISINE_DIR = ROOT / "cuisine/src/main/resources/data/herbcraft_cuisine/environment/drinkable"
CORE_DIR = ROOT / "core/src/main/resources/data/herbcraft/environment/drinkable"
DISHES = ROOT / "cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json"
HERBS = ROOT / "core/src/main/resources/data/herbcraft/herbs.json"
errors = []

REQUIRED = {"type", "amount", "saturation", "effect_potency", "effect_duration", "effect_chance"}
HOMEOSTATIC_VANILLA_ALREADY_DEFINED = {
    "minecraft:apple",
    "minecraft:baked_potato",
    "minecraft:beetroot_soup",
    "minecraft:cake",
    "minecraft:enchanted_golden_apple",
    "minecraft:glistering_melon_slice",
    "minecraft:glow_berries",
    "minecraft:golden_apple",
    "minecraft:golden_carrot",
    "minecraft:honey_bottle",
    "minecraft:melon_slice",
    "minecraft:milk_bucket",
    "minecraft:mushroom_stew",
    "minecraft:poisonous_potato",
    "minecraft:rabbit_stew",
    "minecraft:suspicious_stew",
    "minecraft:sweet_berries",
}
CUISINE_EXCLUDED = {"herbcraft_cuisine:hodgepodge"}
CUISINE_RISKY = {
    "herbcraft_cuisine:deadly_hodgepodge": 0.75,
    "herbcraft_cuisine:crimson_war_hotpot": 0.50,
    "herbcraft_cuisine:bitter_remedy": 0.35,
    "herbcraft_cuisine:analgesic_stew": 0.35,
    "herbcraft_cuisine:resin_guard_stew": 0.20,
}
CORE_POSITIVE_REQUIRED = {
    "minecraft:snowball",
    "minecraft:ice",
    "minecraft:packed_ice",
    "minecraft:blue_ice",
    "minecraft:powder_snow_bucket",
    "minecraft:kelp",
    "minecraft:seagrass",
    "minecraft:sea_pickle",
    "minecraft:sugar_cane",
    "minecraft:cactus_flower",
    "minecraft:melon",
}
CORE_RISKY_REQUIRED = {
    "minecraft:sugar",
    "minecraft:honey_block",
    "minecraft:honeycomb",
    "minecraft:honeycomb_block",
    "minecraft:slime_ball",
    "minecraft:resin_clump",
    "minecraft:resin_brick",
    "minecraft:dried_kelp_block",
    "minecraft:blaze_powder",
    "minecraft:magma_cream",
    "minecraft:glowstone_dust",
    "minecraft:nether_wart",
    "minecraft:crimson_fungus",
    "minecraft:warped_fungus",
    "minecraft:fermented_spider_eye",
    "minecraft:wither_rose",
    "minecraft:lily_of_the_valley",
    "minecraft:azalea",
    "minecraft:flowering_azalea",
    "minecraft:azalea_leaves",
    "minecraft:flowering_azalea_leaves",
    "minecraft:phantom_membrane",
    "minecraft:pitcher_plant",
    "minecraft:bamboo",
    "minecraft:fern",
    "minecraft:large_fern",
    "minecraft:jungle_leaves",
}
ALWAYS_EDIBLE_DRINKLIKE_CUISINE = {
    "blue_ice_brew",
    "seafarer_stew",
    "pine_needle_soup",
    "pine_needle_broth",
    "eye_opening_soup",
    "warped_swift_soup",
    "pale_night_soup",
}


def err(msg):
    errors.append(msg)


def load(path):
    try:
        return json.load(open(path, encoding="utf-8"))
    except Exception as exc:
        err(f"invalid json {path}: {exc}")
        return {}


def validate_common(path: Path, data: dict):
    missing = REQUIRED - data.keys()
    if missing:
        err(f"{path.relative_to(ROOT)} missing fields: {sorted(missing)}")
        return False
    item = data["type"]
    if data["amount"] < 0:
        err(f"{item} has negative amount; Homeostatic stores water below zero and can desync display/dehydration recovery")
    if data["amount"] > 9:
        err(f"{item} amount exceeds Homeostatic milk bucket reference amount 9")
    if data["saturation"] < 0:
        err(f"{item} has negative saturation")
    if not (0.0 <= data["effect_chance"] <= 1.0):
        err(f"{item} effect_chance must be between 0 and 1")
    if data["effect_chance"] > 0 and (data["effect_duration"] <= 0 or data["effect_potency"] <= 0):
        err(f"{item} has effect chance but no positive duration/potency")
    return True


def validate_cuisine():
    if not CUISINE_DIR.exists():
        err(f"missing Homeostatic Cuisine drinkable directory: {CUISINE_DIR.relative_to(ROOT)}")
        return set()
    files = sorted(CUISINE_DIR.glob("*.json"))
    seen = set()
    for path in files:
        data = load(path)
        if not validate_common(path, data):
            continue
        item = data["type"]
        seen.add(item)
        if not item.startswith("herbcraft_cuisine:"):
            err(f"round-1 Cuisine Homeostatic compat must only target herbcraft_cuisine items, got {item}")
        if item in CUISINE_EXCLUDED:
            err(f"excluded cuisine item should not have drinkable data: {item}")
        if item.startswith("herbcraft_cuisine:raw_"):
            err(f"raw cuisine item should not have drinkable data: {item}")
    for item, expected_chance in CUISINE_RISKY.items():
        if item not in seen:
            err(f"missing risky cuisine Homeostatic entry: {item}")
        else:
            data = load(CUISINE_DIR / (item.split(":", 1)[1] + ".json"))
            if abs(data.get("effect_chance", -1) - expected_chance) > 1e-6:
                err(f"{item} should have effect_chance {expected_chance}")
            if data.get("effect_duration", 0) <= 0 or data.get("effect_potency") != 45:
                err(f"{item} should use positive duration and potency 45 for thirst risk")
    water = CUISINE_DIR / "water_bowl.json"
    if water.exists():
        data = load(water)
        if data.get("type") != "herbcraft_cuisine:water_bowl" or data.get("amount") != 6 or abs(data.get("saturation", 0) - 1.2) > 1e-6 or abs(data.get("effect_chance", 0) - 0.85) > 1e-6 or data.get("effect_duration") != 200 or data.get("effect_potency") != 45:
            err("water_bowl Homeostatic values should be amount 6 / saturation 1.2 / 85% thirst risk for 200t potency 45")
    if DISHES.exists():
        dish_data = load(DISHES)
        valid = {"herbcraft_cuisine:water_bowl"}
        for group in ["dishes", "medicinal_dishes"]:
            for dish in dish_data.get(group, []):
                valid.add("herbcraft_cuisine:" + dish["id"])
        for item in seen:
            if item not in valid:
                err(f"cuisine drinkable item is not a known edible cuisine output/water bowl: {item}")
    return seen


def validate_core():
    if not CORE_DIR.exists():
        err(f"missing Homeostatic Core drinkable directory: {CORE_DIR.relative_to(ROOT)}")
        return set()
    herb_items = {entry["item"] for entry in load(HERBS).get("herbs", [])}
    files = sorted(CORE_DIR.glob("*.json"))
    seen = set()
    for path in files:
        data = load(path)
        if not validate_common(path, data):
            continue
        item = data["type"]
        seen.add(item)
        if not item.startswith("minecraft:"):
            err(f"round-2 Core Homeostatic compat must only target minecraft items, got {item}")
        if item not in herb_items:
            err(f"core drinkable item is not a Herbcraft edible definition: {item}")
        if item in HOMEOSTATIC_VANILLA_ALREADY_DEFINED:
            err(f"core drinkable item duplicates Homeostatic generated vanilla data and should be avoided: {item}")
        if item == "minecraft:potion":
            err("minecraft:potion must not be made drinkable because it would affect all potions")
    for item in CORE_POSITIVE_REQUIRED:
        if item not in seen:
            err(f"missing positive Core Homeostatic hydration entry: {item}")
        else:
            data = load(CORE_DIR / (item.split(":", 1)[1] + ".json"))
            if data.get("amount", 0) <= 0 or data.get("effect_chance", 0) > 0.15:
                err(f"{item} should be a positive hydration entry, not a primary risk entry")
    for item in CORE_RISKY_REQUIRED:
        if item not in seen:
            err(f"missing risky Core Homeostatic entry: {item}")
        else:
            data = load(CORE_DIR / (item.split(":", 1)[1] + ".json"))
            if data.get("amount") != 0:
                err(f"{item} should use amount 0 and thirst effect risk, not negative hydration")
            if data.get("effect_chance", 0) <= 0 or data.get("effect_duration") <= 0 or data.get("effect_potency") != 45:
                err(f"{item} should have Homeostatic thirst-risk effect with potency 45")
    return seen

cuisine_seen = validate_cuisine()
core_seen = validate_core()
if "herbcraft_cuisine:hodgepodge" in cuisine_seen:
    err("hodgepodge must remain excluded because Homeostatic cannot inspect hodgepodge contents")
if "minecraft:potion" in core_seen or "minecraft:potion" in cuisine_seen:
    err("minecraft:potion must remain excluded")

# Explicit current balance anchors: no negative hydration; high-risk items use strong thirst effect chances.
sugar_cane_path = CORE_DIR / "sugar_cane.json"
if sugar_cane_path.exists():
    sugar_cane = load(sugar_cane_path)
    if sugar_cane.get("amount") != 3 or abs(sugar_cane.get("saturation", 0) - 0.5) > 1e-6 or sugar_cane.get("effect_chance") != 0.0:
        err("minecraft:sugar_cane should hydrate as a juicy cane: amount 3 / saturation 0.5 / no thirst-risk effect")
else:
    err("missing expected positive hydration entry minecraft:sugar_cane")

for item, (chance, duration) in {
    "minecraft:honey_block": (1.0, 400),
    "minecraft:blaze_powder": (1.0, 400),
    "minecraft:magma_cream": (0.9, 300),
    "minecraft:sugar": (0.7, 240),
    "minecraft:slime_ball": (0.8, 240),
}.items():
    path = CORE_DIR / (item.split(":", 1)[1] + ".json")
    if exists := path.exists():
        data = load(path)
        if data.get("amount") != 0 or abs(data.get("effect_chance", -1) - chance) > 1e-6 or data.get("effect_duration") != duration:
            err(f"{item} should have amount 0, effect_chance {chance}, effect_duration {duration}")
    else:
        err(f"missing expected strong thirst-risk entry {item}")

# Drink-like Cuisine dishes should be can_always_eat so they can hydrate when the food bar is full.
if DISHES.exists():
    dish_data = load(DISHES)
    by_id = {dish["id"]: dish for group in ["dishes", "medicinal_dishes"] for dish in dish_data.get(group, [])}
    for dish_id in ALWAYS_EDIBLE_DRINKLIKE_CUISINE:
        if dish_id not in by_id:
            err(f"missing drink-like cuisine dish in dishes.json: {dish_id}")
        elif not by_id[dish_id].get("can_always_eat"):
            err(f"drink-like cuisine dish should be can_always_eat for Homeostatic hydration: {dish_id}")


if errors:
    print("FAIL")
    for e in errors:
        print(" -", e)
    raise SystemExit(1)
print(f"PASS - Homeostatic compat has {len(cuisine_seen)} Cuisine entries and {len(core_seen)} Core round-2 entries, with vanilla duplicates avoided.")
