#!/usr/bin/env python3
"""Validate draft Herbcraft external extension example data.

This is a source-tree/schema validator. It does not inspect a live modded
Minecraft registry, so it checks syntax and Herbcraft-supported value domains
rather than proving every third-party item exists.
"""

from __future__ import annotations

import csv
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
EXAMPLE = ROOT / "docs" / "examples" / "external_extension_compat_pack"
STARTUP_FOOD_EXAMPLE = ROOT / "docs" / "examples" / "startup_herb_food_extension_mod"
API_ADDON_EXAMPLE = ROOT / "docs" / "examples" / "herbcraft_readonly_event_addon"
P4_RUNTIME_TEST_PACK = ROOT / "docs" / "compat_drafts" / "p4_runtime_datapack_test" / "herbcraft_p4_runtime_test_datapack"
BUNDLED_FARMER_MORE_COMPAT = ROOT / "core" / "src" / "main" / "resources" / "data" / "farmers_moredelight_herbcraft" / "herbcraft"
FINAL_FARMER_MORE_CSV = ROOT / "docs" / "compat_drafts" / "food_mods_26_1_2" / "compat_review_round5_farmer_moredelight.csv"
JSON_SPEC = ROOT / "docs" / "JSON_EXTENSION_SPEC_1_1_3.md"
P4_READINESS_REPORT = ROOT / "docs" / "reports" / "P4_EXTENSION_READINESS_1_1_3_20260617.md"
READONLY_API = ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "api" / "HerbcraftDataAPI.java"
EVENT_API = ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "api" / "HerbcraftEvents.java"
ID_RE = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")

TEMPERATURES = {"cold", "cool", "neutral", "warm", "hot"}
FLAVORS = {"sweet", "bitter", "pungent", "sour", "astringent", "salty", "bland"}
TRAITS = {
    "clearing", "toxic", "nourishing", "stirring", "sedating", "aquatic", "fiery",
    "withered", "nether", "floral", "resinous", "fungal", "lunar", "solar", "leafy",
    "icy", "slimy", "honeyed", "earthy", "addictive", "curio",
}
DIMENSIONS = {"grain", "flesh", "oil", "vege", "fruit"}
PAGE_KINDS = {"common", "rare", "secret", "errata"}


def load(path: Path) -> dict:
    try:
        with path.open("r", encoding="utf-8") as handle:
            data = json.load(handle)
    except Exception as exc:  # noqa: BLE001
        raise SystemExit(f"JSON parse failed: {path}: {exc}") from exc
    if not isinstance(data, dict):
        raise SystemExit(f"Expected JSON object: {path}")
    return data


def require_id(value: str, path: Path, field: str) -> None:
    if not isinstance(value, str) or not ID_RE.match(value):
        raise SystemExit(f"Invalid identifier in {path} field {field}: {value!r}")


def entries(root: dict) -> list[dict]:
    if "entries" in root:
        if not isinstance(root["entries"], list):
            raise SystemExit("entries must be a list")
        return root["entries"]
    return [root]


def validate_natures() -> int:
    count = 0
    for path in (EXAMPLE / "data").glob("*/herbcraft/herb_natures/*.json"):
        for entry in entries(load(path)):
            require_id(entry.get("item"), path, "item")
            temp = entry.get("temperature", "neutral")
            if temp not in TEMPERATURES:
                raise SystemExit(f"Unknown temperature in {path}: {temp}")
            for field, allowed in (("flavors", FLAVORS), ("taste_flavors", FLAVORS), ("traits", TRAITS)):
                values = entry.get(field, [])
                if not isinstance(values, list):
                    raise SystemExit(f"{field} must be a list in {path}")
                for value in values:
                    if value not in allowed:
                        raise SystemExit(f"Unknown {field} value in {path}: {value}")
            count += 1
    return count


def validate_nutrition() -> int:
    count = 0
    for path in (EXAMPLE / "data").glob("*/herbcraft/nutrition_profiles/*.json"):
        for entry in entries(load(path)):
            require_id(entry.get("item"), path, "item")
            values = entry.get("values", entry)
            for dim, val in values.items():
                if dim in {"schema_version", "item"}:
                    continue
                if dim not in DIMENSIONS:
                    raise SystemExit(f"Unknown nutrition dimension in {path}: {dim}")
                if not isinstance(val, int) or val < 0:
                    raise SystemExit(f"Nutrition value must be non-negative int in {path}: {dim}={val!r}")
            count += 1
    return count


def validate_ingredients() -> int:
    count = 0
    allowed_sections = {"meat", "seafood", "grain", "fruit", "soup", "special"}
    for path in (EXAMPLE / "data").glob("*/herbcraft/ingredients/*.json"):
        for entry in entries(load(path)):
            require_id(entry.get("item"), path, "item")
            section = entry.get("section", "special")
            if section not in allowed_sections:
                raise SystemExit(f"Unknown ingredient section in {path}: {section}")
            count += 1
    return count


def validate_counters() -> int:
    count = 0
    for path in (EXAMPLE / "data").glob("*/herbcraft/special_counters/*.json"):
        root = load(path)
        rule_entries = root.get("counters") or root.get("entries") or [root]
        for entry in rule_entries:
            for item in entry.get("items", []):
                require_id(item, path, "items[]")
            for bucket in ("always_effects", "threshold_effects"):
                for effect in entry.get(bucket, []):
                    require_id(effect.get("effect"), path, f"{bucket}.effect")
                    if bucket == "threshold_effects" and int(effect.get("min_count", 1)) < 1:
                        raise SystemExit(f"min_count must be >= 1 in {path}")
            count += 1
    return count


def validate_rumors() -> int:
    count = 0
    lang_keys: set[str] = set()
    for lang in (EXAMPLE / "assets").glob("*/lang/*.json"):
        lang_keys.update(load(lang).keys())
    for path in (EXAMPLE / "data").glob("*/herbcraft/knowledge_rumors/*.json"):
        for entry in entries(load(path)):
            entry_key = entry.get("entry")
            if not isinstance(entry_key, str) or "/" not in entry_key:
                raise SystemExit(f"knowledge rumor entry must be chapter/entry in {path}: {entry_key!r}")
            for claim in entry.get("claims", []):
                text_key = claim.get("text_key")
                if text_key not in lang_keys:
                    raise SystemExit(f"Missing example lang key for {path}: {text_key}")
            count += 1
    return count


def validate_herb_foods() -> int:
    count = 0
    required = {
        "item",
        "nutrition",
        "saturation_modifier",
        "consume_seconds",
        "medicinal",
        "stack_group",
    }
    for path in (STARTUP_FOOD_EXAMPLE / "data").glob("*/herbcraft/herbs/*.json"):
        for entry in entries(load(path)):
            missing = sorted(required - set(entry))
            if missing:
                raise SystemExit(f"Missing herb food fields in {path}: {', '.join(missing)}")
            require_id(entry.get("item"), path, "item")
            if not isinstance(entry.get("nutrition"), int) or entry["nutrition"] < 0:
                raise SystemExit(f"nutrition must be non-negative int in {path}")
            if not isinstance(entry.get("saturation_modifier"), (int, float)):
                raise SystemExit(f"saturation_modifier must be number in {path}")
            if not isinstance(entry.get("consume_seconds"), (int, float)) or entry["consume_seconds"] <= 0:
                raise SystemExit(f"consume_seconds must be positive number in {path}")
            for field in ("medicinal", "stack_group"):
                if not isinstance(entry.get(field), bool):
                    raise SystemExit(f"{field} must be boolean in {path}")
            rolls = entry.get("food_rolls", {})
            if not isinstance(rolls, dict):
                raise SystemExit(f"food_rolls must be an object in {path}")
            for roll_name, whole_amount in (("nutrition", True), ("saturation", False)):
                if roll_name not in rolls:
                    continue
                roll = rolls[roll_name]
                if not isinstance(roll, dict):
                    raise SystemExit(f"food_rolls.{roll_name} must be an object in {path}")
                amount = roll.get("amount")
                chance = roll.get("chance")
                if not isinstance(amount, (int, float)) or amount < 0:
                    raise SystemExit(f"food_rolls.{roll_name}.amount must be non-negative number in {path}")
                if whole_amount and not isinstance(amount, int):
                    raise SystemExit(f"food_rolls.nutrition.amount must be an integer in {path}")
                if not isinstance(chance, (int, float)) or not (0.0 <= float(chance) <= 1.0):
                    raise SystemExit(f"food_rolls.{roll_name}.chance must be 0..1 in {path}")
            for effect in entry.get("effects", []):
                require_id(effect.get("effect"), path, "effects.effect")
            count += 1
    return count


def validate_codex_loot() -> int:
    count = 0
    for path in (EXAMPLE / "data").glob("*/herbcraft/codex_loot_injections/*.json"): 
        root = load(path)
        loot_entries = root.get("page_injections") or root.get("entries") or [root]
        for entry in loot_entries:
            require_id(entry.get("loot_table"), path, "loot_table")
            chance = entry.get("chance")
            if not isinstance(chance, (int, float)) or not (0.0 <= float(chance) <= 1.0):
                raise SystemExit(f"chance must be 0..1 in {path}: {chance!r}")
            kind = entry.get("page_kind", "common")
            if kind not in PAGE_KINDS:
                raise SystemExit(f"Unknown page_kind in {path}: {kind}")
            count += 1
    return count


def validate_json_extension_spec_doc() -> int:
    if not JSON_SPEC.exists():
        raise SystemExit(f"Missing formal JSON extension spec: {JSON_SPEC}")
    text = JSON_SPEC.read_text(encoding="utf-8").lower()
    required_tokens = [
        "p4.1 formal extension specification",
        "data/<namespace>/herbcraft/herb_natures/*.json",
        "data/<namespace>/herbcraft/nutrition_profiles/*.json",
        "data/<namespace>/herbcraft/ingredients/*.json",
        "data/<namespace>/herbcraft/special_counters/*.json",
        "data/<namespace>/herbcraft/knowledge_rumors/*.json",
        "data/<namespace>/herbcraft/codex_loot_injections/*.json",
        "data/<namespace>/herbcraft/herbs/*.json",
        "reload-safe",
        "startup-only",
        "food-nature bridge",
        "custom dynamically registered trait types",
        "reg's more foods intentionally excluded",
    ]
    missing = [token for token in required_tokens if token not in text]
    if missing:
        raise SystemExit(f"Formal JSON extension spec missing required tokens: {', '.join(missing)}")
    return len(required_tokens)


def validate_readonly_api_source() -> int:
    if not READONLY_API.exists():
        raise SystemExit(f"Missing P4.2 read-only API source: {READONLY_API}")
    text = READONLY_API.read_text(encoding="utf-8")
    required_tokens = [
        "public final class HerbcraftDataAPI",
        "Optional<NatureProfile> nature(Identifier itemId)",
        "Optional<NutritionProfile> nutrition(Identifier itemId)",
        "boolean hasIngredientEntry(Identifier itemId)",
        "Optional<String> ingredientEntryId(Identifier itemId)",
        "Optional<HerbDefinition> herb(Item item)",
        "boolean hasFoodNatureEntry(Identifier itemId)",
        "Optional<String> foodNatureEntryId(Identifier itemId)",
        "boolean hasFoodNatureData(Identifier itemId)",
        "Map<Identifier, NatureProfile> natureProfiles()",
        "Map<Identifier, NutritionProfile> nutritionProfiles()",
        "Map<Identifier, String> ingredientEntryIds()",
        "isRegisteredItem",
    ]
    missing = [token for token in required_tokens if token not in text]
    if missing:
        raise SystemExit(f"P4.2 read-only API source missing required tokens: {', '.join(missing)}")
    forbidden = ["registerNature", "registerNutrition", "registerIngredient", "registerHerbFood"]
    present = [token for token in forbidden if token in text]
    if present:
        raise SystemExit(f"P4.2 read-only API must not expose write registration methods: {', '.join(present)}")
    return len(required_tokens)


def validate_p4_readiness_docs() -> int:
    required_docs = [
        ROOT / "docs" / "JSON_EXTENSION_SPEC_1_1_3.md",
        ROOT / "docs" / "PUBLIC_EXTENSION_API_DESIGN.md",
        ROOT / "docs" / "EXTENDING_HERBCRAFT.md",
        ROOT / "docs" / "EXTERNAL_EXTENSION_SPEC_DRAFT.md",
        ROOT / "docs" / "DOCUMENTATION_INDEX.md",
        P4_READINESS_REPORT,
    ]
    for path in required_docs:
        if not path.exists():
            raise SystemExit(f"Missing P4 readiness doc: {path}")
    checks = {
        ROOT / "docs" / "JSON_EXTENSION_SPEC_1_1_3.md": ["P4.5 validators and docs final sweep", "completed"],
        ROOT / "docs" / "PUBLIC_EXTENSION_API_DESIGN.md": ["P4.5", "write APIs not implemented"],
        ROOT / "docs" / "EXTERNAL_EXTENSION_SPEC_DRAFT.md": ["P4.1-P4.5 completed", "Write APIs remain deferred"],
        ROOT / "docs" / "DOCUMENTATION_INDEX.md": ["P4.1-P4.5 are complete", "Write/registration APIs remain deferred"],
        P4_READINESS_REPORT: ["P4.1-P4.5 complete", "A fully compilable standalone example addon is not required", "Remaining client tests"],
    }
    total = 0
    for path, tokens in checks.items():
        text = path.read_text(encoding="utf-8")
        for token in tokens:
            if token not in text:
                raise SystemExit(f"P4 readiness doc {path} missing token: {token}")
            total += 1
    return total


def validate_p4_runtime_test_pack() -> int:
    required = [
        P4_RUNTIME_TEST_PACK / "pack.mcmeta",
        P4_RUNTIME_TEST_PACK / "data" / "p4_runtime_test" / "herbcraft" / "nutrition_profiles" / "generated.json",
        P4_RUNTIME_TEST_PACK / "data" / "p4_runtime_test" / "herbcraft" / "ingredients" / "generated.json",
        P4_RUNTIME_TEST_PACK / "data" / "p4_runtime_test" / "herbcraft" / "herb_natures" / "generated.json",
        P4_RUNTIME_TEST_PACK / "data" / "p4_runtime_test" / "herbcraft" / "special_counters" / "honey_bottle_counter.json",
    ]
    for path in required:
        if not path.exists():
            raise SystemExit(f"Missing P4 runtime test datapack file: {path}")
    nutrition = entries(load(required[1]))
    ingredients = entries(load(required[2]))
    natures = entries(load(required[3]))
    counters = entries(load(required[4]))
    if {entry["item"] for entry in nutrition} != {"minecraft:honey_bottle", "minecraft:bread", "minecraft:baked_potato"}:
        raise SystemExit("P4 runtime test nutrition item set changed unexpectedly")
    if {entry["item"] for entry in ingredients} != {"minecraft:honey_bottle", "minecraft:bread", "minecraft:baked_potato"}:
        raise SystemExit("P4 runtime test ingredient item set changed unexpectedly")
    if {entry["item"] for entry in natures} != {"minecraft:honey_bottle", "minecraft:bread", "minecraft:baked_potato"}:
        raise SystemExit("P4 runtime test nature item set changed unexpectedly")
    if counters[0].get("id") != "p4_runtime_test:honey_bottle_counter":
        raise SystemExit("P4 runtime test counter id changed unexpectedly")
    return len(required)


def validate_p4_examples() -> int:
    required_json_example = [
        EXAMPLE / "pack.mcmeta",
        EXAMPLE / "README.md",
        EXAMPLE / "data" / "example_compat" / "herbcraft" / "herb_natures" / "allium.json",
        EXAMPLE / "data" / "example_compat" / "herbcraft" / "nutrition_profiles" / "honey_bottle.json",
        EXAMPLE / "data" / "example_compat" / "herbcraft" / "ingredients" / "honey_bottle.json",
        EXAMPLE / "data" / "example_compat" / "herbcraft" / "knowledge_rumors" / "allium.json",
        EXAMPLE / "data" / "example_compat" / "herbcraft" / "codex_loot_injections" / "village_temple.json",
        EXAMPLE / "data" / "example_compat" / "herbcraft" / "special_counters" / "sweet_overuse.json",
    ]
    for path in required_json_example:
        if not path.exists():
            raise SystemExit(f"Missing P4.4 JSON example file: {path}")
    json_readme = (EXAMPLE / "README.md").read_text(encoding="utf-8")
    for token in ("JSON_EXTENSION_SPEC_1_1_3.md", "herb_natures", "nutrition_profiles", "ingredients", "knowledge_rumors", "codex_loot_injections"):
        if token not in json_readme:
            raise SystemExit(f"P4.4 JSON example README missing token: {token}")

    required_api_example = [
        API_ADDON_EXAMPLE / "README.md",
        API_ADDON_EXAMPLE / "fabric.mod.json",
        API_ADDON_EXAMPLE / "src" / "main" / "java" / "com" / "example" / "herbcraftapi" / "ExampleHerbcraftApiAddon.java",
    ]
    for path in required_api_example:
        if not path.exists():
            raise SystemExit(f"Missing P4.4 API addon example file: {path}")
    java = required_api_example[-1].read_text(encoding="utf-8")
    for token in (
        "HerbcraftDataAPI",
        "HerbcraftEvents",
        "NUTRITION_APPLIED",
        "INGREDIENT_DISCOVERED",
        "FOOD_NATURE_RESOLVED",
        "EXTERNAL_DATA_RELOADED",
    ):
        if token not in java:
            raise SystemExit(f"P4.4 API addon example missing token: {token}")
    forbidden = ["registerNature", "registerNutrition", "registerIngredient", "registerHerbFood"]
    present = [token for token in forbidden if token in java]
    if present:
        raise SystemExit(f"P4.4 API addon example must not use write registration methods: {', '.join(present)}")
    return len(required_json_example) + len(required_api_example)


def validate_event_api_source() -> int:
    if not EVENT_API.exists():
        raise SystemExit(f"Missing P4.3 event API source: {EVENT_API}")
    text = EVENT_API.read_text(encoding="utf-8")
    required_tokens = [
        "public final class HerbcraftEvents",
        "Event<HerbConsumed> HERB_CONSUMED",
        "Event<NutritionApplied> NUTRITION_APPLIED",
        "Event<IngredientDiscovered> INGREDIENT_DISCOVERED",
        "Event<FoodNatureResolved> FOOD_NATURE_RESOLVED",
        "Event<ExternalDataReloaded> EXTERNAL_DATA_RELOADED",
        "onHerbConsumed",
        "onNutritionApplied",
        "onIngredientDiscovered",
        "onFoodNatureResolved",
        "onExternalDataReloaded",
        "read-only notifications",
    ]
    missing = [token for token in required_tokens if token not in text]
    if missing:
        raise SystemExit(f"P4.3 event API source missing required tokens: {', '.join(missing)}")
    player_mixin = (ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "mixin" / "PlayerMixin.java").read_text(encoding="utf-8")
    for token in ("herbcraft$windowCounters", "HerbcraftWindowCounters", "herbcraft$migrateLegacyWindowCounter", "CounterState"):
        if token not in player_mixin:
            raise SystemExit(f"Dynamic special counter storage missing token: {token}")
    for old_token in ("herbcraft$poppyCount", "herbcraft$torchflowerCount", "herbcraft$sugarCount", "herbcraft$slimeCount"):
        if old_token in player_mixin:
            raise SystemExit(f"Hardcoded special counter field still present: {old_token}")

    ingredients_chapter = (ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "knowledge" / "IngredientsChapter.java").read_text(encoding="utf-8")
    for token in ("missingOptional", "Skipped {} optional ingredient entries", "BuiltInRegistries.ITEM.containsKey"):
        if token not in ingredients_chapter:
            raise SystemExit(f"Optional ingredient missing-item aggregation missing token: {token}")

    source_checks = {
        "NutritionManager.java": ["NUTRITION_APPLIED", "INGREDIENT_DISCOVERED"],
        "PharmacologyResolver.java": ["FOOD_NATURE_RESOLVED", "recordFoodNatureFlavorDiscoveries"],
        "HerbcraftExternalDataReloadListener.java": ["EXTERNAL_DATA_RELOADED"],
        "HerbConsumeEvents.java": ["HERB_CONSUMED"],
        "HerbalChapter.java": ["foodNatureItemIds", "foodNatureEntryId", "HerbKnowledgeStats"],
        "HerbNatureItemTooltips.java": ["HerbcraftDataAPI.hasFoodNatureData", "HerbalChapter.foodNatureEntryId"],
        "HerbJadeData.java": ["FOOD_NATURE", "HerbcraftDataAPI.hasFoodNatureData", "HerbalChapter.foodNatureEntryId"],
    }
    for relative, tokens in source_checks.items():
        matches = list((ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft").rglob(relative))
        if not matches:
            raise SystemExit(f"Missing event integration source: {relative}")
        source = matches[0].read_text(encoding="utf-8")
        if relative == "NutritionManager.java":
            hook = ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "nutrition" / "NutritionDiscoveryHooks.java"
            source += "\n" + hook.read_text(encoding="utf-8")
        if relative == "HerbalChapter.java":
            knowledge_dir = ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "knowledge"
            source += "\n" + "\n".join(path.read_text(encoding="utf-8") for path in knowledge_dir.glob("HerbalChapter*.java"))
        for token in tokens:
            if token not in source:
                raise SystemExit(f"Event token {token} missing from {relative}")
    return len(required_tokens)


def validate_bundled_farmer_more_compat() -> int:
    if not BUNDLED_FARMER_MORE_COMPAT.exists():
        raise SystemExit(f"Missing bundled Farmer+More compat data: {BUNDLED_FARMER_MORE_COMPAT}")
    if not FINAL_FARMER_MORE_CSV.exists():
        raise SystemExit(f"Missing final Farmer+More CSV: {FINAL_FARMER_MORE_CSV}")
    with FINAL_FARMER_MORE_CSV.open("r", encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle))
    if not rows:
        raise SystemExit("Final Farmer+More CSV must not be empty")
    if any(row["item"].startswith("rmf:") or row["source_mod"] == "rmf" for row in rows):
        raise SystemExit("Final Farmer+More CSV must not contain RMF rows")
    expected = {
        "nutrition_profiles": {row["item"] for row in rows if any((row[dim] or "").strip() for dim in DIMENSIONS)},
        "ingredients": {row["item"] for row in rows if row.get("ingredient_section", "").strip()},
        "herb_natures": {row["item"] for row in rows if any(row.get(field, "").strip() for field in ("temperature", "flavors", "taste_flavors", "traits"))},
    }
    disallowed_items = {"farmersdelight:cabbage_seeds", "farmersdelight:tomato_seeds", "farmersdelight:rice"}
    if any(row["item"] in disallowed_items for row in rows):
        raise SystemExit("Final Farmer+More CSV must not contain non-edible Farmer's Delight seed rows")
    expected_counts = {surface: len(ids) for surface, ids in expected.items()}
    for surface, count in expected_counts.items():
        path = BUNDLED_FARMER_MORE_COMPAT / surface / "generated.json"
        root = load(path)
        found_entries = entries(root)
        if len(found_entries) != count:
            raise SystemExit(f"Bundled Farmer+More {surface} should have {count} entries, found {len(found_entries)}")
        found_ids = {entry.get("item", "") for entry in found_entries}
        if any(item.startswith("rmf:") for item in found_ids):
            raise SystemExit(f"Bundled Farmer+More {surface} must not contain RMF item IDs")
        if found_ids != expected[surface]:
            raise SystemExit(f"Bundled Farmer+More {surface} item set does not match final CSV")
    item_mixin = (ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "mixin" / "ItemMixin.java").read_text(encoding="utf-8")
    resolver = (ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "pharmacology" / "PharmacologyResolver.java").read_text(encoding="utf-8")
    herbal_chapter = (ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "knowledge" / "HerbalChapter.java").read_text(encoding="utf-8")
    reload_listener = (ROOT / "core" / "src" / "main" / "java" / "com" / "herbcraft" / "data" / "HerbcraftExternalDataReloadListener.java").read_text(encoding="utf-8")
    required_tokens = [
        "resolveFoodNatureOnly",
        "HerbNatureAPI.getExplicitNature",
        "NutritionRegistry.get(stack)",
        "HerbFoodRegistry.get(stack.getItem()) != null",
        "HerbalChapter.applyFoodNatureEntries",
        "foodNatureEntryId", 
    ]
    combined = item_mixin + "\n" + resolver + "\n" + herbal_chapter + "\n" + reload_listener
    for token in required_tokens:
        if token not in combined:
            raise SystemExit(f"Missing food-nature pharmacology bridge token: {token}")
    return sum(expected_counts.values())


def main() -> int:
    if not EXAMPLE.exists():
        raise SystemExit(f"Missing example pack: {EXAMPLE}")
    counts = {
        "natures": validate_natures(),
        "nutrition": validate_nutrition(),
        "ingredients": validate_ingredients(),
        "counters": validate_counters(),
        "rumors": validate_rumors(),
        "codex_loot": validate_codex_loot(),
        "herb_foods": validate_herb_foods(),
        "json_spec_tokens": validate_json_extension_spec_doc(),
        "readonly_api_tokens": validate_readonly_api_source(),
        "event_api_tokens": validate_event_api_source(),
        "p4_examples": validate_p4_examples(),
        "p4_readiness_docs": validate_p4_readiness_docs(),
        "p4_runtime_test_pack": validate_p4_runtime_test_pack(),
        "bundled_farmer_more": validate_bundled_farmer_more_compat(),
    }
    print("EXTERNAL EXTENSION SPEC CHECK OK:", ", ".join(f"{k}={v}" for k, v in counts.items()))
    return 0


if __name__ == "__main__":
    sys.exit(main())
