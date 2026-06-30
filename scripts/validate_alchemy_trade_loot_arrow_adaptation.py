#!/usr/bin/env python3
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    p = ROOT / rel
    require(p.exists(), f"missing {rel}")
    return p.read_text(encoding="utf-8") if p.exists() else ""

def load(rel):
    p = ROOT / rel
    require(p.exists(), f"missing {rel}")
    return json.loads(p.read_text(encoding="utf-8")) if p.exists() else {}

new_trades = [
    "herbcraft:cleric/4/emerald_dandelion_long",
    "herbcraft:cleric/4/emerald_poppy_strong",
    "herbcraft:cleric/5/emerald_wither_venom_iii",
    "herbcraft:toolsmith/4/emerald_cocoa_beans_long",
    "herbcraft:toolsmith/5/emerald_crimson_fungus_strong",
    "herbcraft:fletcher/4/emerald_arrow_dandelion_long_arrow",
    "herbcraft:fletcher/4/emerald_arrow_poppy_strong_arrow",
    "herbcraft:fletcher/5/emerald_arrow_wither_venom_iii_arrow",
    "herbcraft:wanderer/emerald_distilled_essence_dandelion_sustained",
    "herbcraft:wanderer/emerald_distilled_essence_poppy_intense",
    "herbcraft:armorer/4/emerald_iron_chestplate_steady_bearing",
    "herbcraft:armorer/5/emerald_diamond_chestplate_braced_bearing",
]
for trade in new_trades:
    rel = trade.replace("herbcraft:", "alchemy/src/main/resources/data/herbcraft/villager_trade/") + ".json"
    data = load(rel)
    require("wants" in data and "gives" in data, f"{trade} should define wants/gives")
    require(data.get("max_uses", 0) > 0 and data.get("xp", 0) > 0, f"{trade} should keep vanilla trade metadata")

expected_tag_refs = {
    "data/minecraft/tags/villager_trade/cleric/level_4.json": ["herbcraft:cleric/4/emerald_dandelion_long", "herbcraft:cleric/4/emerald_poppy_strong"],
    "data/minecraft/tags/villager_trade/cleric/level_5.json": ["herbcraft:cleric/5/emerald_wither_venom_iii"],
    "data/minecraft/tags/villager_trade/fletcher/level_4.json": ["herbcraft:fletcher/4/emerald_arrow_dandelion_long_arrow", "herbcraft:fletcher/4/emerald_arrow_poppy_strong_arrow"],
    "data/minecraft/tags/villager_trade/fletcher/level_5.json": ["herbcraft:fletcher/5/emerald_arrow_wither_venom_iii_arrow"],
    "data/minecraft/tags/villager_trade/armorer/level_4.json": ["herbcraft:armorer/4/emerald_iron_chestplate_steady_bearing"],
    "data/minecraft/tags/villager_trade/armorer/level_5.json": ["herbcraft:armorer/5/emerald_diamond_chestplate_braced_bearing"],
    "data/minecraft/tags/villager_trade/toolsmith/level_4.json": ["herbcraft:toolsmith/4/emerald_cocoa_beans_long"],
    "data/minecraft/tags/villager_trade/toolsmith/level_5.json": ["herbcraft:toolsmith/5/emerald_crimson_fungus_strong"],
    "data/minecraft/tags/villager_trade/wandering_trader/uncommon.json": ["herbcraft:wanderer/emerald_distilled_essence_dandelion_sustained", "herbcraft:wanderer/emerald_distilled_essence_poppy_intense"],
}
for tag_rel, values in expected_tag_refs.items():
    data = load("alchemy/src/main/resources/" + tag_rel)
    require(data.get("replace") is False, f"{tag_rel} must append, not replace")
    for value in values:
        require(value in data.get("values", []), f"{tag_rel} missing {value}")

for rel, potion in {
    "alchemy/src/main/resources/data/herbcraft/villager_trade/cleric/4/emerald_dandelion_long.json": "herbcraft:dandelion_long",
    "alchemy/src/main/resources/data/herbcraft/villager_trade/cleric/4/emerald_poppy_strong.json": "herbcraft:poppy_strong",
    "alchemy/src/main/resources/data/herbcraft/villager_trade/cleric/5/emerald_wither_venom_iii.json": "herbcraft:wither_venom_iii",
    "alchemy/src/main/resources/data/herbcraft/villager_trade/fletcher/5/emerald_arrow_wither_venom_iii_arrow.json": "herbcraft:wither_venom_iii",
}.items():
    data = load(rel)
    require(any(m.get("function") == "minecraft:set_potion" and m.get("id") == potion for m in data.get("given_item_modifiers", [])), f"{rel} should reuse set_potion {potion}")

for rel, comp in {
    "alchemy/src/main/resources/data/herbcraft/villager_trade/wanderer/emerald_distilled_essence_dandelion_sustained.json": "herbcraft_alchemy:distilled_essence",
    "alchemy/src/main/resources/data/herbcraft/villager_trade/armorer/4/emerald_iron_chestplate_steady_bearing.json": "herbcraft_alchemy:armor_support",
}.items():
    data = load(rel)
    require(any(m.get("function") == "minecraft:set_components" and comp in m.get("components", {}) for m in data.get("given_item_modifiers", [])), f"{rel} should set {comp} component")

recipe = load("alchemy/src/main/resources/data/herbcraft/recipe/advanced_tipped_arrow.json")
require(recipe.get("type") == "minecraft:crafting_imbue", "advanced_tipped_arrow should reuse vanilla crafting_imbue")
require(recipe.get("source") == "minecraft:lingering_potion" and recipe.get("material") == "minecraft:arrow", "advanced_tipped_arrow should use lingering potion + arrows")
require(recipe.get("result", {}).get("id") == "minecraft:tipped_arrow", "advanced_tipped_arrow result should be tipped_arrow")

loot = load("alchemy/src/main/resources/data/herbcraft_alchemy/alchemy_loot_injections.json")
entries = loot.get("item_injections", [])
require(loot.get("schema_version") == 1, "alchemy loot injection schema should be versioned")
require(len(entries) >= 8, "alchemy loot should cover several world loot contexts")
require(any(e.get("item") == "minecraft:tipped_arrow" and e.get("potion") == "herbcraft:undead_venom" for e in entries), "alchemy loot should include potion arrows")
require(any(e.get("item") == "herbcraft:distilled_essence" and "herbcraft_alchemy:distilled_essence" in e.get("components", {}) for e in entries), "alchemy loot should include componentized distilled essence")
require(any(e.get("item") == "minecraft:iron_chestplate" and "herbcraft_alchemy:armor_support" in e.get("components", {}) for e in entries), "alchemy loot should include armor support")

hooks = read("alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyCodexHooks.java")
for token in ["alchemy_loot_injections.json", "SetPotionFunction.setPotion", "SetComponentsFunction.setComponent", "SetItemCountFunction.setCount", "ItemLootInjection"]:
    require(token in hooks, f"AlchemyCodexHooks missing {token}")

mixin = read("alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyPotionArrowMixin.java")
for token in ["doPostHurtEffects", "markPotionTasted", "PotionBurdenRuntime.onPotionConsumed", "getPickupItemStackOrigin"]:
    require(token in mixin, f"AlchemyPotionArrowMixin missing {token}")
config = read("alchemy/src/main/resources/herbcraft_alchemy.mixins.json")
require("AlchemyPotionArrowMixin" in config, "mixin config should register AlchemyPotionArrowMixin")
knowledge = read("alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyKnowledgeSupport.java")
require("Items.TIPPED_ARROW" in knowledge, "knowledge support should treat tipped arrows as potion containers")

if errors:
    print("FAIL")
    for e in errors:
        print(" -", e)
    sys.exit(1)
print("PASS - Alchemy trades, world loot, and potion arrows reuse existing potion/component rules and are wired into validation.")
