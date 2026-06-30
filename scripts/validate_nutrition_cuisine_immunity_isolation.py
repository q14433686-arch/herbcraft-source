#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
manager = (root / "core/src/main/java/com/herbcraft/logic/EffectImmunityManager.java").read_text()
mixin = (root / "core/src/main/java/com/herbcraft/mixin/LivingEntityMixin.java").read_text()
nutrition = "\n".join([
    (root / "core/src/main/java/com/herbcraft/nutrition/NutritionManager.java").read_text(),
    (root / "core/src/main/java/com/herbcraft/nutrition/NutritionEffectApplicator.java").read_text(),
])

errors = []

def require(cond, msg):
    if not cond:
        errors.append(msg)

require("runBypassingImmunity" in manager, "EffectImmunityManager lacks scoped bypass API")
require("ThreadLocal<Integer> BYPASS_DEPTH" in manager, "EffectImmunityManager bypass is not scoped/thread-local")
require("isBypassingImmunity" in manager, "EffectImmunityManager lacks bypass query")
require("EffectImmunityManager.isBypassingImmunity()" in mixin, "LivingEntityMixin does not respect immunity bypass")
require("EffectImmunityManager.runBypassingImmunity" in nutrition, "NutritionManager does not apply nutrition effects through immunity bypass")
require("static void addNutritionEffect" in nutrition, "Nutrition subsystem lacks central nutrition effect helper")
require("evaluateAndApply(player, state);" in nutrition.split("public static void onFoodConsumed", 1)[1], "NutritionManager does not immediately re-evaluate after food consumption")

raw_adds = []
for i, line in enumerate(nutrition.splitlines(), 1):
    if "player.addEffect(" in line and "runBypassingImmunity" not in line:
        raw_adds.append((i, line.strip()))
require(not raw_adds, "NutritionManager still has direct player.addEffect calls outside bypass: " + "; ".join(f"{i}:{line}" for i, line in raw_adds))

if errors:
    print("FAIL")
    for e in errors:
        print(" -", e)
    raise SystemExit(1)
print("PASS - nutrition effects bypass dietary immunities and food consumption re-evaluates nutrition immediately.")
