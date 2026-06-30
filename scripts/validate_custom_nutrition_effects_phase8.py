#!/usr/bin/env python3
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
EFFECTS = [
    "grain_deficiency", "flesh_deficiency", "oil_deficiency", "vege_deficiency",
    "fruit_deficiency", "dietary_imbalance", "well_nourished",
]
DEFICIENCIES = ["grain_deficiency", "flesh_deficiency", "oil_deficiency", "vege_deficiency", "fruit_deficiency"]
VANILLA_FORBIDDEN_IN_NUTRITION = [
    "MobEffects.WEAKNESS", "MobEffects.SLOWNESS", "MobEffects.MINING_FATIGUE",
    "MobEffects.HUNGER", "MobEffects.NAUSEA", "MobEffects.RESISTANCE",
]
errors = []

def require(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

registry = read("core/src/main/java/com/herbcraft/effect/HerbcraftMobEffects.java")
rules_java = read("core/src/main/java/com/herbcraft/effect/NutritionEffectRules.java")
nutrition_effect = read("core/src/main/java/com/herbcraft/effect/NutritionMobEffect.java")
nutrition_manager = read("core/src/main/java/com/herbcraft/nutrition/NutritionManager.java")
nutrition_runtime = read("core/src/main/java/com/herbcraft/nutrition/NutritionRuntimeService.java")
nutrition_applicator = read("core/src/main/java/com/herbcraft/nutrition/NutritionEffectApplicator.java")
nutrition = nutrition_manager + "\n" + nutrition_runtime + "\n" + nutrition_applicator
commands = read("core/src/main/java/com/herbcraft/nutrition/NutritionDebugCommands.java")
state = read("core/src/main/java/com/herbcraft/nutrition/PlayerNutritionState.java")
effect_resolver = read("core/src/main/java/com/herbcraft/logic/EffectResolver.java")
dish_item = read("cuisine/src/main/java/com/herbcraft/cuisine/item/DishItem.java")
hodgepodge = read("cuisine/src/main/java/com/herbcraft/cuisine/item/HodgepodgeItem.java")
water_bowl = read("cuisine/src/main/java/com/herbcraft/cuisine/item/WaterBowlItem.java")
herbcraft = read("core/src/main/java/com/herbcraft/Herbcraft.java") + "\n" + read("core/src/main/java/com/herbcraft/bootstrap/HerbcraftDataBootstrap.java")
icon_script = read("scripts/generate_nutrition_effect_icons.py")

# Registration/resources.
require("NutritionEffectRules.current().effect" in registry, "HerbcraftMobEffects should register effects from nutrition_effects.json rules")
require("isNutritionEffect" in registry, "missing nutrition-effect identification helper")
require("data/herbcraft/nutrition_effects.json" in rules_java, "NutritionEffectRules should load nutrition_effects.json")
for effect in EFFECTS:
    require(f'"{effect}"' in registry or effect in rules_java, f"effect not declared/loaded: {effect}")
for lang in ["zh_cn", "en_us"]:
    data = json.load(open(ROOT / f"core/src/main/resources/assets/herbcraft/lang/{lang}.json", encoding="utf-8"))
    for effect in EFFECTS:
        require(data.get(f"effect.herbcraft.{effect}"), f"missing {lang} lang key for {effect}")
require("HerbcraftMobEffects.initialize();" in herbcraft, "Herbcraft initializer does not load custom mob effects")

# Icon checks: generated from vanilla item textures, 18x18, deficiency down arrows and well-nourished up arrow.
require("minecraft-client.jar" in icon_script and "assets/minecraft/textures/item/" in icon_script, "nutrition icon generator should derive icons from vanilla item textures")
for token in ["wheat.png", "cooked_beef.png", "honeycomb.png", "kelp.png", "melon_slice.png", "suspicious_stew.png", "golden_apple.png"]:
    require(token in icon_script, f"nutrition icon generator missing vanilla base texture {token}")
for effect in EFFECTS:
    icon = ROOT / f"core/src/main/resources/assets/herbcraft/textures/mob_effect/{effect}.png"
    require(icon.exists(), f"missing mob effect icon: {effect}.png")
    if icon.exists():
        require(icon.read_bytes().startswith(b"\x89PNG\r\n\x1a\n"), f"mob effect icon is not PNG: {effect}.png")
        img = Image.open(icon).convert("RGBA")
        require(img.size == (18, 18), f"mob effect icon should be 18x18: {effect}.png")
        pixels = list(img.get_flattened_data() if hasattr(img, "get_flattened_data") else img.getdata())
        require(sum(1 for p in pixels if p[3] > 0) > 40, f"mob effect icon appears mostly empty: {effect}.png")
        if effect in DEFICIENCIES:
            red_pixels = sum(1 for r, g, b, a in pixels if a > 0 and r > 150 and g < 80 and b < 80)
            require(red_pixels >= 12, f"deficiency icon lacks complete red down-arrow pixels: {effect}.png")
        if effect == "well_nourished":
            green_pixels = sum(1 for r, g, b, a in pixels if a > 0 and g > 150 and r < 130 and b < 130)
            require(green_pixels >= 12, "well_nourished icon lacks complete green up-arrow/accent pixels")

# Effect mechanics now come from nutrition_effects.json.
profiles = json.load(open(ROOT / "core/src/main/resources/data/herbcraft/nutrition_profiles.json", encoding="utf-8"))
require(profiles.get("minecraft:milk_bucket") == {"flesh": 14, "oil": 10}, "milk bucket should provide gentle flesh/oil nutrition")
require(profiles.get("herbcraft_cuisine:water_bowl") == {"fruit": 5}, "water bowl should only provide trace fluids nutrition")
zh = json.load(open(ROOT / "core/src/main/resources/assets/herbcraft/lang/zh_cn.json", encoding="utf-8"))
en = json.load(open(ROOT / "core/src/main/resources/assets/herbcraft/lang/en_us.json", encoding="utf-8"))
require(zh.get("nutrition.herbcraft.dimension.fruit") == "津液", "Chinese fruit/fluid nutrition dimension should display as 津液")
require(en.get("nutrition.herbcraft.dimension.fruit") == "Fluids", "English fruit/fluid nutrition dimension should display as Fluids")

rules = json.load(open(ROOT / "core/src/main/resources/data/herbcraft/nutrition_effects.json", encoding="utf-8"))
effects = rules.get("effects", {})
require(effects["grain_deficiency"]["attributes"][0]["attribute"] == "minecraft:block_break_speed" and effects["grain_deficiency"]["attributes"][0]["amount"] == -0.15, "grain_deficiency rule mismatch")
require(effects["flesh_deficiency"]["attributes"][0]["attribute"] == "minecraft:attack_damage" and effects["flesh_deficiency"]["attributes"][0]["amount"] == -1.0, "flesh_deficiency rule mismatch")
require(effects["oil_deficiency"]["attributes"][0]["amount"] == -0.08, "oil_deficiency rule mismatch")
require(effects["fruit_deficiency"]["attributes"][0]["amount"] == -0.05, "fruit_deficiency rule mismatch")
require(effects["dietary_imbalance"]["attributes"][0]["amount"] == -0.03, "dietary_imbalance movement rule mismatch")
require(effects["vege_deficiency"]["periodic_exhaustion"]["interval_ticks"] == 80 and effects["vege_deficiency"]["periodic_exhaustion"]["amount"] == 0.08, "vege_deficiency exhaustion mismatch")
require(effects["dietary_imbalance"]["periodic_exhaustion"]["interval_ticks"] == 120 and effects["dietary_imbalance"]["periodic_exhaustion"]["amount"] == 0.04, "dietary_imbalance exhaustion mismatch")
require(any(a["attribute"] == "minecraft:armor" and a["amount"] == 3.0 for a in effects["well_nourished"]["attributes"]), "well_nourished armor mismatch")
require(any(a["attribute"] == "minecraft:knockback_resistance" and a["amount"] == 0.12 for a in effects["well_nourished"]["attributes"]), "well_nourished knockback mismatch")
require("causeFoodExhaustion" in nutrition_effect and "scaleExhaustionWithAmplifier" in nutrition_effect, "NutritionMobEffect exhaustion behavior missing")

# NutritionManager migration, refresh and sustained serious states.
for token in VANILLA_FORBIDDEN_IN_NUTRITION:
    require(token not in nutrition, f"NutritionManager still references old vanilla nutrition/balanced effect: {token}")
for constant in ["GRAIN_DEFICIENCY", "FLESH_DEFICIENCY", "OIL_DEFICIENCY", "VEGE_DEFICIENCY", "FRUIT_DEFICIENCY", "DIETARY_IMBALANCE", "WELL_NOURISHED"]:
    require(f"HerbcraftMobEffects.{constant}" in nutrition or f"{constant}" in registry, f"missing use/registration of {constant}")
require("int sustainedTicks = sustainedEffectTicks(rules);" in nutrition, "NutritionManager does not compute sustained effect duration at evaluation start")
require("clearNutritionEffects(player);" in nutrition, "NutritionManager does not clear stale nutrition effects before re-evaluation")
require("filter(HerbcraftMobEffects::isNutritionEffect)" in nutrition, "clearNutritionEffects does not remove only Herbcraft nutrition effects")
require("timing.sustainedMinTicks()" in nutrition and "timing.sustainedExtraTicks()" in nutrition, "sustained effect duration should come from nutrition_effects timing")
require("if (player.getRandom().nextFloat() < 0.80F)" not in nutrition, "starved state is still random")
require("DANGER_DEBUFF_TICKS" not in nutrition and "STARVED_DEBUFF_TICKS" not in nutrition and "IMBALANCED_DEBUFF_TICKS" not in nutrition, "old short serious-state constants remain")
require("case \"grain\" -> addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.GRAIN_DEFICIENCY, sustainedTicks, 0));" in nutrition, "grain danger is not sustained/deterministic")
require(nutrition.count("HerbcraftMobEffects.GRAIN_DEFICIENCY, sustainedTicks") == 2, "grain_deficiency sustained use should be starved + danger only; duplicate grain danger may remain")
require("state.shouldApplyDebuff())" in nutrition and "HerbcraftMobEffects.DIETARY_IMBALANCE, sustainedTicks" in nutrition, "confirmed imbalance is not sustained after grace")
require("new MobEffectInstance(HerbcraftMobEffects.WELL_NOURISHED, NutritionEffectRules.current().timing.wellNourishedTicks()" in nutrition, "balanced state does not apply data-timed well_nourished")
require("NutritionStatus" not in state and "evaluate(" not in state, "stale PlayerNutritionState evaluate/NutritionStatus path should be removed")

# Debug commands.
require("CommandRegistrationCallback.EVENT.register" in commands, "nutrition debug command is not registered")
for token in ["get", "set", "preset", "grain_low", "flesh_low", "oil_low", "vege_low", "fruit_low", "starved", "balanced"]:
    require(f'"{token}"' in commands, f"nutrition debug command missing token/preset: {token}")
require("setNutritionForDebug" in nutrition and "applyNutritionPresetForDebug" in nutrition, "NutritionManager lacks debug mutation APIs")
require("Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)" in commands, "nutrition debug command should require gamemaster permission")

# Water bowl relief and clear isolation.
require(rules["timing"]["dietary_imbalance_relief_ticks"] == 1200, "dietary imbalance water-bowl relief should be 1200 ticks / 60s")
require("relieveDietaryImbalance" in nutrition, "NutritionManager lacks water-bowl dietary imbalance relief API")
require("!isDietaryImbalanceRelieved(player) && state.shouldApplyDebuff()" in nutrition, "temporary relief does not suppress dietary imbalance reapplication")
require("filter(effect -> !HerbcraftMobEffects.isNutritionEffect(effect))" in effect_resolver, "EffectResolver.clearPositiveEffects does not skip nutrition effects")
require("filter(effect -> !HerbcraftMobEffects.isNutritionEffect(effect))" in dish_item, "DishItem.clear_all_effects does not skip nutrition effects")
require("filter(effect -> !HerbcraftMobEffects.isNutritionEffect(effect))" in hodgepodge, "HodgepodgeItem.clearPositiveEffects does not skip nutrition effects")
require("CLEAR_NAUSEA_CHANCE = 0.15F" in water_bowl, "water bowl relief chance must remain 15%")
require("removeEffect(MobEffects.NAUSEA)" in water_bowl, "WaterBowlItem should still clear vanilla nausea on success")
require("NutritionManager.relieveDietaryImbalance" in water_bowl, "WaterBowlItem does not trigger temporary dietary imbalance relief")

if errors:
    print("FAIL")
    for err in errors:
        print(" -", err)
    raise SystemExit(1)
print("PASS - nutrition effect mechanics are JSON-backed; icons, stale effect refresh, sustained serious malnutrition, debug commands, water relief, and clear isolation are intact.")
