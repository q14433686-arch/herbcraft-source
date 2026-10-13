#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(cond, msg):
    if not cond:
        errors.append(msg)

def load(rel):
    p = ROOT / rel
    require(p.exists(), f"missing {rel}")
    return json.load(open(p, encoding="utf-8")) if p.exists() else {}

special = load("core/src/main/resources/data/herbcraft/special_counters.json")
rules = { (entry.get("id"), tuple(entry.get("items", []))): entry for entry in special.get("counters", []) }
require(len(special.get("counters", [])) >= 5, "special counter rules should include poppy, torchflower, sugar, honey block, and slime")
require(("poppy", ("minecraft:poppy",)) in rules, "missing poppy special counter rule")
require(("torchflower", ("minecraft:torchflower",)) in rules, "missing torchflower special counter rule")
require(("sugar", ("minecraft:sugar",)) in rules, "missing sugar special counter rule")
require(("sugar", ("minecraft:honey_block",)) in rules, "missing honey block shared sugar counter rule")
require(("slime", ("minecraft:slime_ball",)) in rules, "missing slime special counter rule")
require(any(e.get("effect") == "minecraft:darkness" and e.get("min_count") == 3 for e in rules[("poppy", ("minecraft:poppy",))].get("threshold_effects", [])), "poppy rule should apply darkness at count 3")
require(any(e.get("effect") == "minecraft:fire_resistance" and e.get("min_count") == 3 for e in rules[("torchflower", ("minecraft:torchflower",))].get("threshold_effects", [])), "torchflower rule should apply fire resistance at count 3")
require(any(e.get("effect") == "minecraft:nausea" and e.get("min_count") == 4 and abs(e.get("chance", 0)-0.5)<1e-6 for e in rules[("sugar", ("minecraft:sugar",))].get("threshold_effects", [])), "sugar rule should apply 50% nausea at count 4")
require(any(e.get("effect") == "minecraft:nausea" and e.get("min_count") == 2 and abs(e.get("chance", 0)-0.5)<1e-6 for e in rules[("sugar", ("minecraft:honey_block",))].get("threshold_effects", [])), "honey block rule should apply 50% nausea at count 2")

special_java = (ROOT / "core/src/main/java/com/herbcraft/logic/SpecialCounters.java").read_text(encoding="utf-8")
herbcraft_java = "\n".join([
    (ROOT / "core/src/main/java/com/herbcraft/Herbcraft.java").read_text(encoding="utf-8"),
    (ROOT / "core/src/main/java/com/herbcraft/bootstrap/HerbcraftDataBootstrap.java").read_text(encoding="utf-8"),
    (ROOT / "core/src/main/java/com/herbcraft/bootstrap/HerbcraftKnowledgeBootstrap.java").read_text(encoding="utf-8"),
])
require("special_counters.json" in special_java, "SpecialCounters should load special_counters.json")
require("MobEffects." not in special_java, "SpecialCounters should not hardcode MobEffects after externalization")
require("Items.POPPY" not in special_java and "Items.SUGAR" not in special_java and "Items.SLIME_BALL" not in special_java, "SpecialCounters should not hardcode item-specific rules")
require("SpecialCounters.loadFromBundledJson();" in herbcraft_java, "Herbcraft initializer must load special counter JSON")


# Ingredient Codex entries should be content data, not a Java item table.
ingredients = load("core/src/main/resources/data/herbcraft/ingredients.json")
nutrition_profiles = load("core/src/main/resources/data/herbcraft/nutrition_profiles.json")
allowed_sections = {"meat", "seafood", "grain", "fruit", "soup", "special"}
require(ingredients.get("schema_version") == 1, "ingredients.json should declare schema_version 1")
require(ingredients.get("sections") == ["meat", "seafood", "grain", "fruit", "soup", "special"], "ingredients.json sections should preserve current 食材录 order")
entries = ingredients.get("entries", [])
require(len(entries) == 39, "ingredients.json should contain 39 current vanilla food entries")
seen_ingredient_items = set()
for entry in entries:
    item = entry.get("item")
    section = entry.get("section")
    require(isinstance(item, str) and ":" in item, f"ingredients entry has malformed item id: {entry!r}")
    require(section in allowed_sections, f"ingredients entry {item} has invalid section {section!r}")
    require(item not in seen_ingredient_items, f"duplicate ingredient entry {item}")
    seen_ingredient_items.add(item)
    require(item in nutrition_profiles, f"ingredients entry {item} missing nutrition profile")
for required in ["minecraft:cooked_beef", "minecraft:bread", "minecraft:apple", "minecraft:mushroom_stew", "minecraft:milk_bucket"]:
    require(required in seen_ingredient_items, f"ingredients.json missing expected entry {required}")
ingredients_java = (ROOT / "core/src/main/java/com/herbcraft/knowledge/IngredientsChapter.java").read_text(encoding="utf-8")
nutrition_manager_java = "\n".join([
    (ROOT / "core/src/main/java/com/herbcraft/nutrition/NutritionManager.java").read_text(encoding="utf-8"),
    (ROOT / "core/src/main/java/com/herbcraft/nutrition/NutritionDiscoveryHooks.java").read_text(encoding="utf-8"),
])
external_reload_java = (ROOT / "core/src/main/java/com/herbcraft/data/HerbcraftExternalDataReloadListener.java").read_text(encoding="utf-8")
data_sync_payload_java = (ROOT / "core/src/main/java/com/herbcraft/data/HerbcraftDataSyncPayload.java").read_text(encoding="utf-8")
data_sync_manager_java = (ROOT / "core/src/main/java/com/herbcraft/data/HerbcraftDataSyncManager.java").read_text(encoding="utf-8")
client_init_java = (ROOT / "core/src/main/java/com/herbcraft/knowledge/client/HerbcraftCoreClient.java").read_text(encoding="utf-8")
require("data/herbcraft/ingredients.json" in ingredients_java, "IngredientsChapter should load ingredients.json")
require("getResourceAsStream(resourcePath)" in ingredients_java, "IngredientsChapter should read bundled JSON resources")
for token in ["applyExternalData", "herbcraft/ingredients", "itemEntryIds", "replaceClientIngredientEntries", "defaultEntryId"]:
    require(token in ingredients_java, f"IngredientsChapter missing external/sync ingredient token {token}")
require("Items." not in ingredients_java, "IngredientsChapter must not hardcode the vanilla food item table")
require("IngredientsChapter.register();" in herbcraft_java, "Herbcraft initializer must register IngredientsChapter")
require("IngredientsChapter.hasEntry" in nutrition_manager_java and "markTasted" in nutrition_manager_java and "markPracticed" in nutrition_manager_java and "IngredientsChapter.entryId" in nutrition_manager_java, "NutritionManager should mark namespace-aware 食材录 knowledge on food consumption")
for token in ["IngredientsChapter.applyExternalData(manager)", "HerbcraftDataSyncManager.markDirty()"]:
    require(token in external_reload_java, f"external reload listener missing ingredient/data sync token {token}")
for token in ["nutritionProfiles", "natureProfiles", "ingredientEntryIds", "NutritionRegistry.profiles()", "HerbNatureRegistry.profiles()", "IngredientsChapter.itemEntryIds()"]:
    require(token in data_sync_payload_java, f"HerbcraftDataSyncPayload missing sync token {token}")
for token in ["ServerPlayConnectionEvents.JOIN", "markDirty", "replaceWithSyncedData", "replaceClientIngredientEntries"]:
    require(token in data_sync_manager_java, f"HerbcraftDataSyncManager missing sync token {token}")
require("HerbcraftDataSyncPayload.TYPE" in client_init_java and "HerbcraftDataSyncManager.applyClientPayload" in client_init_java, "client initializer should receive Herbcraft data sync payload")

# Pharmacology constants should be loaded from pharmacology_rules.json, with Java defaults only as fallback.
pharmacology = load("core/src/main/resources/data/herbcraft/pharmacology_rules.json")
expected_pharmacology = {
    "window_ticks": 600,
    "qi_regression_interval_ticks": 600,
    "qi_regression_amount": 5,
    "same_qi_threshold": 50,
    "harmonize_negative_probability_multiplier": 0.7,
    "harmonize_negative_duration_multiplier": 0.75,
    "same_qi_negative_probability_multiplier": 1.3,
    "same_qi_negative_duration_multiplier": 1.25,
    "sweet_heal_multiplier": 1.15,
    "bitter_immunity_multiplier": 1.2,
    "base_immunity_ticks": 600,
    "pungent_positive_duration_multiplier": 1.15,
    "pungent_nausea_chance": 0.08,
    "pungent_nausea_ticks": 60,
    "sour_positive_duration_multiplier": 1.1,
    "sour_negative_duration_multiplier": 0.9,
    "astringent_cooldown_ticks": 600,
    "salty_immunity_multiplier": 1.15,
    "salty_water_duration_multiplier": 1.1,
    "bland_side_effect_probability_multiplier": 0.85,
    "xiang_fan_window_ticks": 200,
    "xiang_fan_applies_effects": True,
}
for key, expected in expected_pharmacology.items():
    require(pharmacology.get(key) == expected, f"pharmacology_rules.json {key} expected {expected!r}, got {pharmacology.get(key)!r}")
require(pharmacology.get("temperature_values") == {"cold": -15, "cool": -8, "neutral": 0, "warm": 8, "hot": 15}, "pharmacology_rules.json temperature_values should be data-backed tuned values")
seven_rules = pharmacology.get("seven_emotion_rules", [])
require([rule.get("type") for rule in seven_rules] == ["xiang_fan", "xiang_sha", "xiang_wei", "xiang_e", "xiang_xu", "xiang_shi", "single"], "seven_emotion_rules should preserve current relation priority order")
require(seven_rules[0].get("hot_cold_conflict") is True and seven_rules[0].get("either_traits") == ["toxic"], "xiang_fan rule should be data-backed hot/cold toxic conflict")
require(seven_rules[-1].get("single_no_shared_with_history") is True, "single rule should be data-backed")
pharmacology_rules_java = (ROOT / "core/src/main/java/com/herbcraft/pharmacology/PharmacologyRules.java").read_text(encoding="utf-8")
pharmacology_resolver_java = (ROOT / "core/src/main/java/com/herbcraft/pharmacology/PharmacologyResolver.java").read_text(encoding="utf-8")
require("data/herbcraft/pharmacology_rules.json" in pharmacology_rules_java, "PharmacologyRules should load pharmacology_rules.json")
require("PharmacologyRules.loadFromBundledJson();" in herbcraft_java, "Herbcraft initializer must load pharmacology rules JSON")
for key in expected_pharmacology:
    require(key in pharmacology_rules_java, f"PharmacologyRules missing JSON key loader for {key}")
for token in ["temperature_values", "seven_emotion_rules", "readTemperatureValues", "readSevenEmotionRules", "SevenEmotionRule"]:
    require(token in pharmacology_rules_java, f"PharmacologyRules missing data-driven pharmacology token {token}")
for token in ["sameQiThreshold", "sweetHealMultiplier", "pungentNauseaChance", "rules.temperatureValue", "rules.sevenEmotionRules()", "matchesSevenEmotionRule"]:
    require(token in pharmacology_resolver_java, f"PharmacologyResolver should use PharmacologyRules/data-driven relation token {token}")
require('case "cold" -> -25' not in pharmacology_resolver_java and 'case "hot" -> 25' not in pharmacology_resolver_java, "PharmacologyResolver should not hardcode temperature values")

hodge = load("cuisine/src/main/resources/data/herbcraft_cuisine/hodgepodge_rules.json")
require(hodge.get("min_herbs") == 3, "hodgepodge min_herbs should be 3")
require(hodge.get("normal_max_herbs") == 4, "hodgepodge normal_max_herbs should be 4")
require(hodge.get("max_herbs") == 7, "hodgepodge max_herbs should be 7")
require(hodge.get("nutrition_cap") == 10, "hodgepodge nutrition_cap should be 10")
require(abs(hodge.get("chance_bonus", 0)-0.15)<1e-6, "hodgepodge chance_bonus should be 0.15")
clash = hodge.get("clash", {})
require(abs(clash.get("base_chance", 0)-0.30)<1e-6, "hodgepodge clash base chance should be 0.30")
require(abs(clash.get("max_chance", 0)-0.80)<1e-6, "hodgepodge clash max chance should be 0.80")

hodge_item = (ROOT / "cuisine/src/main/java/com/herbcraft/cuisine/item/HodgepodgeItem.java").read_text(encoding="utf-8")
hodge_recipe = (ROOT / "cuisine/src/main/java/com/herbcraft/cuisine/recipe/HodgepodgeRecipe.java").read_text(encoding="utf-8")
cuisine_java = (ROOT / "cuisine/src/main/java/com/herbcraft/cuisine/HerbcraftCuisine.java").read_text(encoding="utf-8")
require("HodgepodgeRules.current()" in hodge_item, "HodgepodgeItem should read HodgepodgeRules")
require("HodgepodgeRules.current()" in hodge_recipe, "HodgepodgeRecipe should read HodgepodgeRules")
for token in ["CHANCE_BONUS", "CLASH_CHANCE", "CLASH_NAUSEA_SECONDS", "MAX_STACK_GAIN", "MIN_HERBS", "MAX_HERBS", "NUTRITION_CAP"]:
    require(token not in hodge_item and token not in hodge_recipe, f"hodgepodge hardcoded constant remains: {token}")
require("HodgepodgeRules.loadFromBundledJson();" in cuisine_java, "Cuisine initializer must load hodgepodge rules JSON")

essences = load("alchemy/src/main/resources/data/herbcraft_alchemy/essences.json")
require(len(essences.get("essences", [])) == 45, "essences.json should contain 45 current essence definitions")
ids = {entry.get("id") for entry in essences.get("essences", [])}
for required in ["dandelion", "golden_dandelion", "poppy", "tulip", "fern", "wither_rose", "crimson_fungus", "warped_fungus", "brown_mushroom"]:
    require(required in ids, f"essences.json missing expected essence {required}")
for entry in essences.get("essences", []):
    require(entry.get("source_items"), f"essence {entry.get('id')} missing source_items")
    require(entry.get("effects"), f"essence {entry.get('id')} missing effects")
    for effect in entry.get("effects", []):
        require("effect" in effect and "duration_seconds" in effect and "amplifier" in effect and "positive" in effect, f"essence {entry.get('id')} has malformed effect")
require(set(next(e for e in essences["essences"] if e["id"] == "tulip")["source_items"]) == {"minecraft:red_tulip", "minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip"}, "tulip source variants should be data-driven")
require(set(next(e for e in essences["essences"] if e["id"] == "fern")["source_items"]) == {"minecraft:fern", "minecraft:large_fern"}, "fern source variants should be data-driven")

alchemy_registry = (ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java").read_text(encoding="utf-8")
alchemy_usage = (ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyUsageHints.java").read_text(encoding="utf-8")
require("data/herbcraft_alchemy/essences.json" in alchemy_registry, "AlchemyRegistries should load essences.json")
require(alchemy_registry.count("essenceFromJson(\"") == 45, "AlchemyRegistries should initialize current static essence fields through essenceFromJson")
require("public static final Item ESSENCE_" in alchemy_registry and "= essence(" not in "\n".join(line for line in alchemy_registry.splitlines() if "public static final Item ESSENCE_" in line), "static essence fields should not hardcode effect specs directly")
require("sourceItems()" in alchemy_usage and "VARIANT_SOURCES" not in alchemy_usage, "AlchemyUsageHints should use source_items from essences.json")

bonus = load("alchemy/src/main/resources/data/herbcraft_alchemy/potion_bonus_pools.json")
require(len(bonus.get("rules", [])) >= 10, "potion bonus pools should include harmful/beneficial matching rules and fallbacks")
require(any(r.get("kind") == "harmful" and r.get("fallback") for r in bonus.get("rules", [])), "potion bonus pools need a harmful fallback")
require(any(r.get("kind") == "beneficial" and r.get("fallback") for r in bonus.get("rules", [])), "potion bonus pools need a beneficial fallback")
require(any("minecraft:poison" in r.get("when_any", []) and "minecraft:wither" in r.get("when_any", []) for r in bonus.get("rules", [])), "potion bonus pools missing poison/wither harmful rule")
require(any("minecraft:regeneration" in r.get("when_any", []) and "minecraft:instant_health" in r.get("when_any", []) for r in bonus.get("rules", [])), "potion bonus pools missing healing beneficial rule")

bonus_java = (ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java").read_text(encoding="utf-8")
alchemy_init = (ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java").read_text(encoding="utf-8")
require("potion_bonus_pools.json" in bonus_java, "AdvancedPotionStackInitializer should load potion_bonus_pools.json")
require("BONUS_RULES" in bonus_java and "BonusRule" in bonus_java, "AdvancedPotionStackInitializer should use parsed bonus rules")
require("MobEffects." not in bonus_java, "AdvancedPotionStackInitializer bonus pool should not hardcode MobEffects after externalization")
require("loadBonusPoolsFromBundledJson" in alchemy_init, "Alchemy initializer must load potion bonus pool JSON")

special_potions = load("alchemy/src/main/resources/data/herbcraft_alchemy/special_potions.json")
sp_ids = {entry.get("id") for entry in special_potions.get("potions", [])}
for required in ["undead_venom", "cardiac_toxin", "wither_venom_iii", "witch_tulip_murky", "witch_azure_bluet_murky", "witch_pitcher_plant_murky", "witch_azalea_murky", "witch_closed_eyeblossom_murky"]:
    require(required in sp_ids, f"special_potions.json missing {required}")
require(len(special_potions.get("witch_throw_pool", [])) == 5, "witch_throw_pool should contain five safe witch potion entries")
require(sum(entry.get("weight", 0) for entry in special_potions.get("witch_throw_pool", [])) == 13, "witch_throw_pool weights should preserve previous total weight 13")
require("special_potions.json" in alchemy_registry, "AlchemyRegistries should load special_potions.json")
require("registerSpecialPotion(\"undead_venom\")" in alchemy_registry, "undead venom should be registered from special_potions.json")
require("registerSpecialPotion(\"cardiac_toxin\")" in alchemy_registry, "cardiac toxin should be registered from special_potions.json")
require("registerSpecialPotion(\"wither_venom_iii\")" in alchemy_registry, "wither venom III should be registered from special_potions.json")
require("pickWitchThrowPotion" in alchemy_registry and "WITCH_THROW_POOL" in alchemy_registry, "witch throw potion pool should be data-backed")
witch_mixin = (ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/mixin/WitchHerbcraftPotionMixin.java").read_text(encoding="utf-8")
require("AlchemyRegistries.pickWitchThrowPotion" in witch_mixin and "nextInt(13)" not in witch_mixin, "witch mixin should use data-backed throw pool")
require("registerExtraEssencesFromJson" in alchemy_registry, "AlchemyRegistries should register extra JSON essences beyond compatibility static fields")


# Codex guide entries are data-backed so module-owned guide pages do not leak into Core-only installs.
core_guides = load("core/src/main/resources/data/herbcraft/codex_guides.json").get("entries", [])
cuisine_guides = load("cuisine/src/main/resources/data/herbcraft_cuisine/codex_guides.json").get("entries", [])
alchemy_guides = load("alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json").get("entries", [])
require({e.get("id") for e in core_guides} == {"guide_recognizing", "guide_knowledge_states", "guide_flavors", "guide_seven_emotions", "guide_herb_stack", "guide_ingredients", "guide_errata"}, "core codex_guides.json should contain only Core guide entries")
cuisine_expected = {"guide_cuisine", "guide_cuisine_overview", "guide_cuisine_water_bowl", "guide_cuisine_dishes", "guide_cuisine_hodgepodge", "guide_cuisine_immunity", "guide_cuisine_hidden_catalyst"}
alchemy_expected = {"guide_alchemy", "guide_alchemy_overview", "guide_alchemy_essences", "guide_alchemy_potions", "guide_alchemy_clarified_murky", "guide_alchemy_quality_bonus", "guide_alchemy_coatings", "guide_alchemy_armor_support", "guide_alchemy_witch"}
require({e.get("id") for e in cuisine_guides} == cuisine_expected, "cuisine codex_guides.json should contain gateway plus Cuisine overview guide entries")
require({e.get("id") for e in alchemy_guides} == alchemy_expected, "alchemy codex_guides.json should contain gateway plus Alchemy overview guide entries")
for entries, label in [(core_guides, "core"), (cuisine_guides, "cuisine"), (alchemy_guides, "alchemy")]:
    for entry in entries:
        require(entry.get("kind"), f"{label} guide {entry.get('id')} missing kind")
        require(entry.get("always_visible") is True, f"{label} guide {entry.get('id')} should be always_visible")
for entry in core_guides:
    require(entry.get("chapter") == "herbal" and entry.get("section") == "overview", f"core guide {entry.get('id')} should target herbal/overview")
for entry in cuisine_guides:
    if entry.get("id") == "guide_cuisine":
        require(entry.get("chapter") == "herbal" and entry.get("section") == "overview", "Cuisine gateway should target herbal/overview")
    else:
        require(entry.get("chapter") == "cuisine" and entry.get("section") == "overview", f"Cuisine module guide {entry.get('id')} should target cuisine/overview")
for entry in alchemy_guides:
    if entry.get("id") == "guide_alchemy":
        require(entry.get("chapter") == "herbal" and entry.get("section") == "overview", "Alchemy gateway should target herbal/overview")
    else:
        require(entry.get("chapter") == "alchemy" and entry.get("section") == "overview", f"Alchemy module guide {entry.get('id')} should target alchemy/overview")
guide_registry = (ROOT / "core/src/main/java/com/herbcraft/knowledge/CodexGuideRegistry.java").read_text(encoding="utf-8")
herbal_chapter_java = "\n".join([
    (ROOT / "core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java").read_text(encoding="utf-8"),
    (ROOT / "core/src/main/java/com/herbcraft/knowledge/HerbalChapterRegistration.java").read_text(encoding="utf-8"),
])
cuisine_chapter_java = (ROOT / "cuisine/src/main/java/com/herbcraft/cuisine/knowledge/CuisineChapter.java").read_text(encoding="utf-8")
alchemy_chapter_java = (ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java").read_text(encoding="utf-8")
require("loadFromBundledJson" in guide_registry and "registerProvider" in guide_registry, "CodexGuideRegistry should load guide JSON and use providers")
require("data/herbcraft/codex_guides.json" in herbal_chapter_java, "HerbalChapter should load core codex_guides.json")
require("data/herbcraft_cuisine/codex_guides.json" in cuisine_chapter_java, "CuisineChapter should load cuisine codex_guides.json")
require("data/herbcraft_alchemy/codex_guides.json" in alchemy_chapter_java, "AlchemyChapter should load alchemy codex_guides.json")
require("registerGuideEntries()" not in herbal_chapter_java, "HerbalChapter should not keep hardcoded guide entry table")
require('"cuisine"' not in herbal_chapter_java and '"alchemy"' not in herbal_chapter_java, "Core HerbalChapter should not hardcode addon guide kinds")
require("HerbNatureAPI.natureLine(first.nature())" not in herbal_chapter_java, "Flavor guide should not print exact first tasted item nature")


# One-time guide hints are data-backed and module-owned.
core_hints = load("core/src/main/resources/data/herbcraft/guide_hints.json").get("hints", [])
cuisine_hints = load("cuisine/src/main/resources/data/herbcraft_cuisine/guide_hints.json").get("hints", [])
alchemy_hints = load("alchemy/src/main/resources/data/herbcraft_alchemy/guide_hints.json").get("hints", [])
require({h.get("id") for h in core_hints} == {"herbcraft:first_herb_bite", "herbcraft:first_codex_open", "herbcraft:first_page_read", "herbcraft:first_ingredient_tasted", "herbcraft:first_correction"}, "core guide_hints.json should contain only Core hint entries")
require({h.get("id") for h in cuisine_hints} == {"herbcraft_cuisine:first_water_bowl", "herbcraft_cuisine:first_dish"}, "cuisine guide_hints.json should contain only Cuisine hint entries")
require({h.get("id") for h in alchemy_hints} == {"herbcraft_alchemy:first_essence"}, "alchemy guide_hints.json should contain only Alchemy hint entries")
for hints, label, prefix in [(core_hints, "core", "herbcraft:"), (cuisine_hints, "cuisine", "herbcraft_cuisine:"), (alchemy_hints, "alchemy", "herbcraft_alchemy:")]:
    for hint in hints:
        require(str(hint.get("id", "")).startswith(prefix), f"{label} hint id should use module prefix: {hint}")
        require(str(hint.get("trigger", "")).startswith(prefix), f"{label} hint trigger should use module prefix: {hint}")
        require(hint.get("message_key"), f"{label} hint missing message_key: {hint}")
        require(hint.get("once") is True, f"{label} hint should be once=true: {hint}")
guide_hint_manager = (ROOT / "core/src/main/java/com/herbcraft/guide/GuideHintManager.java").read_text(encoding="utf-8")
player_data_java = (ROOT / "core/src/main/java/com/herbcraft/HerbcraftPlayerData.java").read_text(encoding="utf-8")
player_mixin_java = (ROOT / "core/src/main/java/com/herbcraft/mixin/PlayerMixin.java").read_text(encoding="utf-8")
require("GuideHintManager" in guide_hint_manager and "loadFromBundledJson" in guide_hint_manager and "herbcraft$seenGuideHints" in guide_hint_manager, "GuideHintManager should load JSON and persist once-only hints")
require("herbcraft$seenGuideHints" in player_data_java, "HerbcraftPlayerData should expose seen guide hints")
require("HerbcraftGuideHints" in player_mixin_java, "PlayerMixin should save/load guide hints")
require("data/herbcraft/guide_hints.json" in herbcraft_java, "Herbcraft initializer should load core guide_hints.json")
require("data/herbcraft_cuisine/guide_hints.json" in cuisine_chapter_java or "data/herbcraft_cuisine/guide_hints.json" in (ROOT / "cuisine/src/main/java/com/herbcraft/cuisine/HerbcraftCuisine.java").read_text(encoding="utf-8"), "Cuisine should load cuisine guide_hints.json")
require("data/herbcraft_alchemy/guide_hints.json" in alchemy_chapter_java or "data/herbcraft_alchemy/guide_hints.json" in (ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java").read_text(encoding="utf-8"), "Alchemy should load alchemy guide_hints.json")

for rel in ["core/src/main/resources/data/herbcraft/codex_loot_injections.json", "alchemy/src/main/resources/data/herbcraft_alchemy/codex_loot_injections.json", "cuisine/src/main/resources/data/herbcraft_cuisine/codex_loot_injections.json"]:
    data = load(rel)
    require(data.get("page_injections"), f"{rel} should contain page_injections")
    for entry in data.get("page_injections", []):
        require("loot_table" in entry and "chance" in entry and "chapter" in entry and "page_kind" in entry, f"malformed codex loot entry in {rel}")
core_codex = (ROOT / "core/src/main/java/com/herbcraft/knowledge/CodexLoot.java").read_text(encoding="utf-8")
loot_rules_java = (ROOT / "core/src/main/java/com/herbcraft/knowledge/CodexLootRules.java").read_text(encoding="utf-8")
alchemy_codex = (ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyCodexHooks.java").read_text(encoding="utf-8")
cuisine_codex = (ROOT / "cuisine/src/main/java/com/herbcraft/cuisine/knowledge/CuisineCodexHooks.java").read_text(encoding="utf-8")
require("codex_loot_injections.json" in core_codex and "CodexLootRules.load" in core_codex, "core CodexLoot should be JSON-backed")
require("CodexLootRules.load" in alchemy_codex and "ALCHEMY_TABLES" not in alchemy_codex, "alchemy Codex hooks should be JSON-backed")
require("CodexLootRules.load" in cuisine_codex and "CUISINE_TABLES" not in cuisine_codex, "cuisine Codex hooks should be JSON-backed")
require("ResourceKey.create(Registries.LOOT_TABLE" in loot_rules_java, "CodexLootRules should convert loot table IDs to ResourceKeys")

if errors:
    print("FAIL")
    for err in errors:
        print(" -", err)
    raise SystemExit(1)
print("PASS - counters, hodgepodge rules, ingredients, pharmacology rules, codex guide entries, guide hints, codex loot injections, alchemy essences, special potions, witch pool, and potion bonus pools are externalized to JSON and loaded by Java runtime code.")
