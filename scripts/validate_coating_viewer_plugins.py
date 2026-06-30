#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errs = []

def ok(cond, msg):
    print(("PASS - " if cond else "FAIL - ") + msg)
    if not cond:
        errs.append(msg)

jei = ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/compat/jei/CoatingJeiPlugin.java"
rei = ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/compat/rei/CoatingReiPlugin.java"
rei_common = ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/compat/rei/CoatingReiCommonPlugin.java"
recipe = ROOT / "alchemy/src/main/java/com/herbcraft/alchemy/coating/CoatingSmithingRecipe.java"
build = ROOT / "build.gradle"
props = ROOT / "gradle.properties"
modjson = ROOT / "alchemy/src/main/resources/fabric.mod.json"

ok(jei.exists(), "JEI coating plugin source exists")
ok(rei.exists(), "REI coating plugin source exists")
ok(rei_common.exists(), "REI common comparator plugin source exists")
ok(recipe.exists(), "coating smithing recipe source exists")
ok(build.exists(), "build.gradle exists")
ok(props.exists(), "gradle.properties exists")
ok(modjson.exists(), "alchemy fabric.mod.json exists")

jt = jei.read_text(encoding="utf-8") if jei.exists() else ""
rt = rei.read_text(encoding="utf-8") if rei.exists() else ""
rct = rei_common.read_text(encoding="utf-8") if rei_common.exists() else ""
ct = recipe.read_text(encoding="utf-8") if recipe.exists() else ""
bt = build.read_text(encoding="utf-8") if build.exists() else ""
pt = props.read_text(encoding="utf-8") if props.exists() else ""

ok("@JeiPlugin" in jt and "implements IModPlugin" in jt, "JEI plugin is discoverable by JEI annotation scanning")
ok("registerVanillaCategoryExtensions" in jt and "getSmithingCategory().addExtension" in jt, "JEI plugin registers smithing category extensions")
ok("ArmorSupportSmithingRecipe" in jt and "new ArmorSupportSmithingRecipe" in jt, "JEI plugin explicitly adds armor-support smithing recipes")
ok("RecipeTypes.BREWING" in jt and "registerAdvanced" in jt and "ComponentAwareBrewingRecipeManagerPlugin" in jt, "JEI plugin supplies custom brewing displays through a component-aware brewing recipe manager plugin")
ok("registerItemSubtypes" in jt and "registerSubtypeInterpreter" in jt and "distilledSubtype" in jt and "DistillationComponents.DISTILLED_ESSENCE" in jt, "JEI registers component-aware subtypes for distilled essence")
ok("potionSubtype" in jt and "AdvancedPotionStackInitializer.potionPath" in jt and "AlchemyPotionComponents.POTION_PROCESSING_STYLE" in jt, "JEI registers component-aware subtypes for processed potion lookup")
ok("List.of(new ItemStack(orientation.catalyst()))" in jt and "List.of(new ItemStack(info.item()))" in jt and "HerbcraftBrewingRecipe" in jt, "JEI distilled-essence brewing display uses catalyst as top ingredient and essence as bottom input")
ok("List.of(distilled.copy())" in jt and "List.of(potionStack(bottle, basePotion(info.id()).orElse(null)))" in jt and "getRecipesForOutput" in jt and "sameStack(recipe.getPotionOutput(), stack)" in jt, "JEI processed-potion brewing display uses distilled essence as top ingredient and potion as bottom input with component-exact lookup")
ok("registration.addRecipes(RecipeTypes.BREWING" not in jt and "createBrewingRecipe" not in jt, "JEI no longer registers distilled-essence brewing through vanilla item-fuzzy brewing lookup")
ok("DistillationItems.stack" in jt and "DistillationRules.implementedOrientations" in jt, "JEI plugin exposes componentized distilled-essence brewing inputs/outputs")
ok("Items.SPLASH_POTION" in jt and "Items.LINGERING_POTION" in jt, "JEI brewing displays include splash and lingering potion routes")
ok("registerRecipes" in jt and "RecipeTypes.SMITHING" in jt and "new CoatingSmithingRecipe" in jt, "JEI plugin explicitly adds coating recipes to smithing category")
ok("coatingApplyRecipes()" in jt and "armorSupportApplyRecipes()" in jt and "displayOutput()" in jt, "JEI uses correlated display-only smithing holders for distilled-essence attachment outputs")
ok("setTemplate" in jt and "setBase" in jt and "setAddition" in jt and "setOutput" in jt, "JEI smithing extension supplies all slots")
ok("CoatingItems.BLANK_ADHESIVE" in jt and "CoatingItems.AMETHYST_ADHESIVE" in jt and "CoatingItems.ECHO_ADHESIVE" in jt, "JEI plugin exposes coating adhesive inputs")
ok("AlchemyRegistries.essenceInfos()" in jt, "JEI plugin derives displays from data-backed essence registry")
ok("CoatingSystem.applyCoating" in jt, "JEI plugin creates real coated output stacks")
ok("ProcessedCoatingRules" in jt and "coatingAdditionSamples" in jt, "JEI plugin exposes processed coating distilled-essence variants")
ok("ArmorSupportComponents.ARMOR_SUPPORT" in jt and "ArmorSupportMode" in jt, "JEI plugin creates real armor-support output stacks")

ok("implements REIClientPlugin" in rt, "REI client plugin implements REIClientPlugin")
ok("implements REICommonPlugin" in rct and "registerItemComparators" in rct and "DistillationItems.DISTILLED_ESSENCE" in rct, "REI common plugin registers component-aware comparators for distilled essence")
ok("Items.POTION" in rct and "Items.SPLASH_POTION" in rct and "Items.LINGERING_POTION" in rct and "potionHash" in rct, "REI common plugin registers component-aware comparators for processed potion lookup")
ok("registerDisplays" in rt and "registry.add" in rt, "REI plugin explicitly registers displays")
ok("DefaultSmithingDisplay" in rt and "SmithingRecipeType.TRANSFORM" in rt, "REI plugin uses the built-in smithing display category")
ok("DefaultBrewingDisplay" in rt, "REI plugin uses the built-in brewing display category")
ok("CoatingItems.BLANK_ADHESIVE" in rt and "CoatingItems.AMETHYST_ADHESIVE" in rt and "CoatingItems.ECHO_ADHESIVE" in rt, "REI plugin exposes coating adhesive inputs")
ok("AlchemyRegistries.essenceInfos()" in rt, "REI plugin derives displays from data-backed essence registry")
ok("CoatingSystem.applyCoating" in rt, "REI plugin creates real coated output stacks")
ok("ProcessedCoatingRules" in rt and "processedApplyDisplay" in rt, "REI plugin exposes processed coating distilled-essence variants")
ok("processedApplyDisplay(info, style, base)" in rt and "armorSupportApplyDisplay(info, support, base)" in rt, "REI splits distilled-essence smithing displays per base stack so outputs stay correlated")
ok("ArmorSupportComponents.ARMOR_SUPPORT" in rt and "armorSupportApplyDisplay" in rt and "armorSupportRefineDisplay" in rt, "REI plugin exposes armor-support apply/refine displays")
ok("DistillationItems.stack" in rt and "DistillationRules.implementedOrientations" in rt and "brewingDistillationDisplay" in rt, "REI plugin exposes distilled-essence brewing displays")
ok("Items.SPLASH_POTION" in rt and "Items.LINGERING_POTION" in rt and "brewingPotionDisplay" in rt, "REI plugin exposes normal/splash/lingering processed-potion brewing displays")
ok("processedPotionStack" in jt and "AdvancedPotionStackInitializer.initializeIfNeeded" in jt, "JEI processed-potion outputs use initialized runtime-style stacks for lookup matching")
ok("processedPotionStack" in rt and "AdvancedPotionStackInitializer.initializeIfNeeded" in rt, "REI processed-potion outputs use initialized runtime-style stacks for lookup matching")
ok("BuiltInRegistries.ITEM" in rt and "CoatingSystem.COATABLE_WEAPONS" in rt, "REI plugin expands the coatable-weapons tag instead of showing only iron sword")
ok("coatableWeaponStacks()" in rt and "coatedWeaponStacks" in rt, "REI plugin supplies all coatable inputs and corresponding coated outputs")

mod = json.loads(modjson.read_text(encoding="utf-8")) if modjson.exists() else {}
entrypoints = mod.get("entrypoints", {})
ok("jei_mod_plugin" in entrypoints, "fabric.mod.json declares JEI mod-plugin entrypoint")
ok("com.herbcraft.alchemy.compat.jei.CoatingJeiPlugin" in entrypoints.get("jei_mod_plugin", []), "JEI entrypoint points to coating plugin")
ok("rei_client" in entrypoints, "fabric.mod.json declares REI client entrypoint")
ok("rei_common" in entrypoints, "fabric.mod.json declares REI common comparator entrypoint")
ok("com.herbcraft.alchemy.compat.rei.CoatingReiPlugin" in entrypoints.get("rei_client", []), "REI entrypoint points to coating plugin")
ok("com.herbcraft.alchemy.compat.rei.CoatingReiCommonPlugin" in entrypoints.get("rei_common", []), "REI common entrypoint points to comparator plugin")
ok("jei" not in mod.get("depends", {}), "JEI remains optional, not a hard dependency")
ok("roughlyenoughitems" not in mod.get("depends", {}), "REI remains optional, not a hard dependency")

ok("jei_version=" in pt and "rei_version=" in pt and "architectury_version=" in pt, "viewer API versions are pinned in gradle.properties")
ok("jei-${rootProject.minecraft_version}-common-api" in bt and "jei-${rootProject.minecraft_version}-fabric-api" in bt, "JEI compile-only APIs are configured")
ok("RoughlyEnoughItems-api-fabric" in bt and "RoughlyEnoughItems-default-plugin-fabric" in bt, "REI compile-only APIs are configured")
ok("architectury-fabric" in bt, "Architectury compile-only dependency is configured for REI API types")
ok("compileOnly" in bt and "implementation \"mezz.jei" not in bt and "implementation \"me.shedaniel:RoughlyEnoughItems" not in bt, "viewer APIs are compile-only and not bundled as hard runtime deps")

ok("public CoatingSmithingRecipe.RecipeMode recipeMode()" in ct, "coating recipe exposes mode for viewer plugins")
ok("public boolean isSpecial()" in ct and "return false;" in ct, "coating recipe remains non-special for viewer indexing")
ok("public List<RecipeDisplay> display()" in ct, "vanilla display fallback remains present")
ok("displayTemplate()" in ct and "displayBase()" in ct and "displayAddition()" in ct and "displayOutput()" in ct, "coating recipe supports correlated JEI display-only stacks without changing serialized JSON")

if errs:
    print("Coating viewer plugin validation failed:")
    for err in errs:
        print("- " + err)
    sys.exit(1)
print("Coating viewer plugin validation passed.")
