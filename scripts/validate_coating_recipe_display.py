#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
recipe = ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingSmithingRecipe.java"
registry = ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java"

text = recipe.read_text(encoding="utf-8")
reg = registry.read_text(encoding="utf-8")
errs = []

def ok(cond, msg):
    print(("PASS - " if cond else "FAIL - ") + msg)
    if not cond:
        errs.append(msg)

ok("public List<RecipeDisplay> display()" in text, "coating smithing recipe overrides Recipe.display()")
ok("new SmithingRecipeDisplay" in text, "coating display exposes smithing-table recipe displays")
ok("public boolean isSpecial()" in text and "return false;" in text, "coating recipe is not marked special so recipe viewers do not filter it out")
ok("RecipeMode.APPLY ? this.applyDisplays() : this.refineDisplays()" in text, "apply and refine modes both provide displays")
ok("AlchemyRegistries.essenceInfos()" in text, "display entries are generated from data-backed essence registry")
ok("CoatingSystem.applyCoating(stack, info, mode" in text, "display output stack uses real coating component application")
ok("ItemStackTemplate.fromNonEmptyStack(stack)" in text, "display output preserves coated ItemStack components")
ok("Items.SMITHING_TABLE" in text, "display declares smithing table as crafting station")
ok("SlotDisplay.Empty.INSTANCE" in text, "refine display keeps empty smithing addition slot")
ok("new ItemStack(Items.IRON_SWORD)" in text, "display uses a representative coatable weapon without changing matching rules")
ok("public static List<AlchemyRegistries.EssenceInfo> essenceInfos()" in reg, "AlchemyRegistries exposes read-only essence info list for displays")
ok("return List.copyOf(INFO_BY_ID.values())" in reg, "essence info list is immutable copy and data-backed")

if errs:
    print("Coating recipe-display validation failed:")
    for err in errs:
        print("- " + err)
    sys.exit(1)
print("Coating recipe-display validation passed.")
