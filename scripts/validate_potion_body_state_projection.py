#!/usr/bin/env python3
from pathlib import Path
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
ALCHEMY = ROOT / "alchemy"

checks = []

components = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java").read_text(encoding="utf-8")
checks.append(("POTION_BODY_STATE component registered", "POTION_BODY_STATE" in components and "potion_body_state" in components))

projection = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/potion/PotionBodyStateProjection.java").read_text(encoding="utf-8")
for token in ["medicinalProfile", "medicinalWeight", "thermalTendency", "thermalValue", "qiTendency", "qiValue", "bodyTraits", "CODEC", "STREAM_CODEC"]:
    checks.append((f"PotionBodyStateProjection token {token}", token in projection))

runtime = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/potion/PotionBodyStateProjectionRuntime.java").read_text(encoding="utf-8")
for token in ["applyMetadata(ItemStack stack)", "POTION_BODY_STATE", "PotionPharmacologyProjectionRuntime", "PharmacologyRules.current().temperatureValue", "medicinalProfile(", "thermalTendency(", "qiTendency("]:
    checks.append((f"PotionBodyStateProjectionRuntime token {token}", token in runtime))

consume = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/potion/PotionBodyStateConsumptionRuntime.java").read_text(encoding="utf-8")
for token in ["onPotionConsumed(ServerPlayer player, ItemStack stack)", "herbcraft$pharmacologyState()", "state.addUse(", "state.setQiBalance("]:
    checks.append((f"PotionBodyStateConsumptionRuntime token {token}", token in consume))

initializer = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java").read_text(encoding="utf-8")
checks.append(("Initializer applies body-state metadata", "PotionBodyStateProjectionRuntime.applyMetadata(stack);" in initializer))

burden = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/potion/PotionBurdenRuntime.java").read_text(encoding="utf-8")
checks.append(("Consumption runtime hooked from burden consumption path", "PotionBodyStateConsumptionRuntime.onPotionConsumed(player, stack);" in burden))

api = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/AlchemyProcessingAPI.java").read_text(encoding="utf-8")
checks.append(("AlchemyProcessingAPI exposes potionBodyState", "potionBodyState(ItemStack stack)" in api))

tooltip = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java").read_text(encoding="utf-8")
chapter = (ALCHEMY / "src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java").read_text(encoding="utf-8")
for token in ["tooltip.herbcraft.potion.medicinal_profile", "tooltip.herbcraft.potion.medicinal_weight", "tooltip.herbcraft.potion.body_thermal", "tooltip.herbcraft.potion.body_qi"]:
    checks.append((f"Body-state detail moved out of long tooltip {token}", token not in tooltip))
checks.append(("Tooltip has compact Codex hint", "tooltip.herbcraft.potion.codex_hint" in tooltip))
checks.append(("Codex carries body-state processing reminder", "book.herbcraft_alchemy.entry.potion_processing_bodystate" in chapter and "book.herbcraft_alchemy.entry.potion_mastered_semantics" in chapter))

for lang_name in ["zh_cn.json", "en_us.json"]:
    lang = (ALCHEMY / f"src/main/resources/assets/herbcraft/lang/{lang_name}").read_text(encoding="utf-8")
    for key in [
        '"tooltip.herbcraft.potion.medicinal_profile"',
        '"tooltip.herbcraft.potion.medicinal_weight"',
        '"tooltip.herbcraft.potion.body_thermal"',
        '"tooltip.herbcraft.potion.body_qi"',
        '"tooltip.herbcraft.potion.medicinal_profile.warming"',
        '"tooltip.herbcraft.potion.body_qi.scattering"'
    ]:
        checks.append((f"{lang_name} contains {key}", key in lang))

jar_path = ROOT / "alchemy/build/libs/herbcraft-alchemy-1.1.3.jar"
if jar_path.exists():
    with zipfile.ZipFile(jar_path) as zf:
        names = set(zf.namelist())
    for cls in [
        "com/herbcraft/alchemy/potion/PotionBodyStateProjection.class",
        "com/herbcraft/alchemy/potion/PotionBodyStateProjectionRuntime.class",
        "com/herbcraft/alchemy/potion/PotionBodyStateConsumptionRuntime.class",
    ]:
        checks.append((f"Jar contains {cls}", cls in names))
else:
    checks.append(("Alchemy jar exists for class verification", False))

failed = [label for label, ok in checks if not ok]
if failed:
    print("FAIL")
    for label in failed:
        print(f" - {label}")
    sys.exit(1)
print("PASS")
