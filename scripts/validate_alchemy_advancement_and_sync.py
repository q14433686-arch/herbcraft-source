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

adv_dir = ROOT / "alchemy/src/main/resources/data/herbcraft_alchemy/advancement"
expected_new = {
    "first_distilled_essence": ("herbcraft_alchemy:first_essence", "herbcraft:distilled_essence"),
    "first_sustained": ("herbcraft_alchemy:first_potion", "minecraft:redstone"),
    "first_intense": ("herbcraft_alchemy:first_potion", "minecraft:glowstone_dust"),
    "first_armor_support": ("herbcraft_alchemy:first_distilled_essence", "minecraft:iron_chestplate"),
}
for name, (parent, icon) in expected_new.items():
    data = load(f"alchemy/src/main/resources/data/herbcraft_alchemy/advancement/{name}.json")
    require(data.get("parent") == parent, f"{name} parent should be {parent}")
    require(data.get("display", {}).get("icon", {}).get("id") == icon, f"{name} icon should be {icon}")
    crit = data.get("criteria", {}).get("trigger", {})
    require(crit.get("trigger") == "minecraft:impossible", f"{name} must be code-granted, not broad inventory-granted")
    require(data.get("requirements") == [["trigger"]], f"{name} should use single trigger requirement")
require(len(list(adv_dir.glob("*.json"))) == 13, "alchemy advancement tree should contain 13 files")

for lang in ["en_us", "zh_cn"]:
    data = load(f"alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json")
    for name in expected_new:
        require(f"advancements.herbcraft_alchemy.{name}.title" in data, f"missing {lang} advancement title {name}")
        require(f"advancements.herbcraft_alchemy.{name}.description" in data, f"missing {lang} advancement desc {name}")
    require("book.herbcraft_alchemy.guide.armor_support.first" in data, f"missing {lang} armor support first guide key")

acq = read("alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java")
knowledge = read("alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyKnowledgeSupport.java")
chapter = read("alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java")
require('"first_distilled_essence"' in acq, "inventory acquisition should grant first_distilled_essence for distilled essence components")
require('"first_armor_support"' in acq, "inventory acquisition should grant first_armor_support for armor support components")
require("awardPotionAdvancements" not in acq, "inventory acquisition must not award potion taste/progression advancements")
require("markPotionTasted(player" in knowledge and "awardPotionAdvancements" in knowledge, "potion taste path must award potion advancements")
for name in ["first_potion", "first_clarified", "first_sustained", "first_intense", "first_murky", "undead_toxin", "witch_knowledge", "epic_potion"]:
    require(f'"{name}"' in knowledge, f"taste-path awards should mention {name}")
for source in ["undead_venom", "cardiac_toxin", "wither_venom_iii", "witch_"]:
    require(source in knowledge and source in acq, f"special potion source mapping should handle {source}")
for name in ["first_sustained", "first_intense", "first_distilled_essence", "first_armor_support"]:
    require(f'"{name}"' in chapter, f"Alchemy guide should account for {name}")

rebuilder = read("scripts/rebuild_advancements_and_lang.py")
for name in expected_new:
    require(name in rebuilder, f"rebuild script missing {name}")
require("('first_essence',)" in rebuilder and "'witch_knowledge')" not in rebuilder, "rebuild script should not recreate broad witch inventory trigger")

component_files = [
    "alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java",
    "alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportComponents.java",
    "alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistillationComponents.java",
]
for rel in component_files:
    text = read(rel)
    declarations = [line for line in text.splitlines() if "public static final DataComponentType<" in line]
    sync_count = text.count(".networkSynchronized(")
    require(len(declarations) == sync_count, f"all data components in {rel} should be network synchronized ({sync_count}/{len(declarations)})")
    require(".persistent(" in text, f"components in {rel} should be persistent")

for rel in [
    "alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java",
    "alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportRules.java",
    "alchemy/src/main/java/com/herbcraft/alchemy/coating/ProcessedCoatingRules.java",
    "alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistillationRules.java",
]:
    text = read(rel)
    require("loadFromBundledJson" in text, f"{rel} should explicitly load bundled/startup alchemy rules")

burden_rules = read("alchemy/src/main/java/com/herbcraft/alchemy/effect/AlchemyBurdenEffectRules.java")
require("private static final AlchemyBurdenEffectRules INSTANCE = load();" in burden_rules and "potion_burden_effects.json" in burden_rules, "burden effects should be bundled registration-time static rules")

if errors:
    print("FAIL")
    for e in errors:
        print(" -", e)
    sys.exit(1)
print("PASS - alchemy advancement progression is narrowed to concrete acquisition/taste paths, and component sync/startup rule assumptions are validated.")
