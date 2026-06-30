#!/usr/bin/env python3
import json, struct, sys, zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
CORE_VERSION = '1.1.4'
ALCHEMY_VERSION = '1.1.4'
CUISINE_VERSION = '1.1.3'
JARS = {
    f'core/build/libs/herbcraft-{CORE_VERSION}.jar': {
        'id': 'herbcraft',
        'version': CORE_VERSION,
        'classes': [
            'com/herbcraft/Herbcraft.class',
            'com/herbcraft/knowledge/client/HerbcraftCoreClient.class',
            'com/herbcraft/effect/HerbcraftMobEffects.class',
            'com/herbcraft/effect/NutritionEffectRules.class',
            'com/herbcraft/nutrition/NutritionDebugCommands.class',
            'com/herbcraft/knowledge/CodexGuideRegistry.class',
            'com/herbcraft/guide/GuideHintManager.class',
            'com/herbcraft/knowledge/IngredientsChapter.class',
            'com/herbcraft/pharmacology/PharmacologyRules.class',
            'com/herbcraft/registry/HerbEntry$FoodRoll.class',
            'com/herbcraft/data/HerbcraftDataSyncPayload.class',
            'com/herbcraft/data/HerbcraftDataSyncManager.class',
            'com/herbcraft/api/HerbcraftDataAPI.class',
            'com/herbcraft/api/HerbcraftEvents.class',
            'com/herbcraft/api/HerbcraftEvents$NutritionApplied.class',
            'com/herbcraft/api/HerbcraftEvents$FoodNatureResolved.class',
            'com/herbcraft/api/HerbcraftEvents$ExternalDataReloaded.class',
        ],
        'resources': [
            'fabric.mod.json',
            'herbcraft.mixins.json',
            'data/herbcraft/herbs.json',
            'data/herbcraft/special_counters.json',
            'data/herbcraft/nutrition_effects.json',
            'data/herbcraft/nutrition_correction_rules.json',
            'data/herbcraft/pharmacology_rules.json',
            'data/herbcraft/ingredients.json',
            'data/herbcraft/codex_guides.json',
            'data/herbcraft/guide_hints.json',
            'data/herbcraft/codex_loot_injections.json',
            'data/herbcraft/environment/drinkable/snowball.json',
            'data/herbcraft/environment/drinkable/honey_block.json',
            'data/herbcraft/tags/item/herb.json',
            'assets/herbcraft/textures/mob_effect/well_nourished.png',
        ],
    },
    f'alchemy/build/libs/herbcraft-alchemy-{ALCHEMY_VERSION}.jar': {
        'id': 'herbcraft_alchemy',
        'version': ALCHEMY_VERSION,
        'classes': [
            'com/herbcraft/alchemy/HerbcraftAlchemy.class',
            'com/herbcraft/alchemy/effect/AlchemyMobEffects.class',
            'com/herbcraft/alchemy/effect/AlchemyBurdenEffectRules.class',
            'com/herbcraft/alchemy/client/HerbcraftAlchemyClient.class',
            'com/herbcraft/alchemy/potion/AlchemyPotionComponents.class',
            'com/herbcraft/alchemy/potion/PotionProcessingRules.class',
            'com/herbcraft/alchemy/potion/PotionProcessingRules$RuntimeProfile.class',
            'com/herbcraft/alchemy/potion/PotionProcessingRules$BurdenRuntime.class',
            'com/herbcraft/alchemy/potion/PotionProcessingRules$BurdenStage.class',
            'com/herbcraft/alchemy/potion/PotionProcessingRuntime.class',
            'com/herbcraft/alchemy/potion/PotionBurdenComponent.class',
            'com/herbcraft/alchemy/potion/PotionPharmacologyProjection.class',
            'com/herbcraft/alchemy/potion/PotionPharmacologyProjectionRuntime.class',
            'com/herbcraft/alchemy/potion/PotionBodyStateProjection.class',
            'com/herbcraft/alchemy/potion/PotionBodyStateProjectionRuntime.class',
            'com/herbcraft/alchemy/potion/PotionBodyStateConsumptionRuntime.class',
            'com/herbcraft/alchemy/potion/PotionRelationRuntime.class',
            'com/herbcraft/alchemy/potion/PotionBurdenRuntime.class',
            'com/herbcraft/alchemy/potion/PlayerPotionBurdenState.class',
            'com/herbcraft/alchemy/potion/PlayerPotionBurdenRuntime.class',
            'com/herbcraft/alchemy/potion/PotionBurdenConsequenceRuntime.class',
            'com/herbcraft/alchemy/potion/PotionProcessingRules$BurdenConsequenceProfile.class',
            'com/herbcraft/alchemy/AlchemyExpansionEvents.class',
            'com/herbcraft/alchemy/AlchemyProcessingAPI.class',
            'com/herbcraft/alchemy/AlchemyDistillationAPI.class',
            'com/herbcraft/alchemy/armor/ArmorSupportRules.class',
            'com/herbcraft/alchemy/armor/ArmorSupportComponent.class',
            'com/herbcraft/alchemy/armor/ArmorSupportComponents.class',
            'com/herbcraft/alchemy/armor/ArmorSupportRecipe.class',
            'com/herbcraft/alchemy/armor/ArmorSupportSmithingRecipe.class',
            'com/herbcraft/alchemy/armor/ArmorSupportRecipes.class',
            'com/herbcraft/alchemy/armor/ArmorSupportRuntime.class',
            'com/herbcraft/alchemy/distillation/DistillationRules.class',
            'com/herbcraft/alchemy/distillation/DistillationItems.class',
            'com/herbcraft/alchemy/distillation/DistillationComponents.class',
            'com/herbcraft/alchemy/distillation/DistillationRecipe.class',
            'com/herbcraft/alchemy/distillation/DistillationRecipes.class',
            'com/herbcraft/alchemy/distillation/DistilledEssenceBrewing.class',
            'com/herbcraft/alchemy/distillation/DistilledEssenceComponent.class',
            'com/herbcraft/alchemy/client/AdvancedPotionTooltips.class',
            'com/herbcraft/alchemy/client/AlchemyKnowledgeClientSupport.class',
            'com/herbcraft/alchemy/knowledge/AlchemyKnowledgeSupport.class',
            'com/herbcraft/alchemy/mixin/AlchemyItemMixin.class',
            'com/herbcraft/alchemy/mixin/AlchemyPlayerMixin.class',
            'com/herbcraft/alchemy/mixin/ItemCostAdvancedPotionMixin.class',
            'com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.class',
            'com/herbcraft/alchemy/mixin/ThrownSplashPotionKnowledgeMixin.class',
            'com/herbcraft/alchemy/mixin/AreaEffectCloudKnowledgeMixin.class',
            'com/herbcraft/alchemy/coating/CoatingSmithingRecipe.class',
            'com/herbcraft/alchemy/coating/ProcessedCoatingRules.class',
            'com/herbcraft/alchemy/compat/jei/CoatingJeiPlugin.class',
            'com/herbcraft/alchemy/compat/jei/CoatingJeiPlugin$HerbcraftBrewingRecipe.class',
            'com/herbcraft/alchemy/compat/jei/CoatingJeiPlugin$ComponentAwareBrewingRecipeManagerPlugin.class',
            'com/herbcraft/alchemy/compat/rei/CoatingReiPlugin.class',
            'com/herbcraft/alchemy/compat/rei/CoatingReiCommonPlugin.class',
        ],
        'resources': [
            'fabric.mod.json',
            'herbcraft_alchemy.mixins.json',
            'data/herbcraft_alchemy/essences.json',
            'data/herbcraft_alchemy/potion_bonus_pools.json',
            'data/herbcraft_alchemy/potion_processing_rules.json',
            'data/herbcraft_alchemy/potion_burden_effects.json',
            'data/herbcraft_alchemy/distillation_rules.json',
            'data/herbcraft_alchemy/armor_support_rules.json',
            'data/herbcraft_alchemy/special_potions.json',
            'data/herbcraft_alchemy/coating_rules.json',
            'data/herbcraft_alchemy/processed_coating_rules.json',
            'data/herbcraft_alchemy/witch_loot.json',
            'data/herbcraft_alchemy/codex_loot_injections.json',
            'data/herbcraft_alchemy/codex_guides.json',
            'data/herbcraft_alchemy/guide_hints.json',
            'data/herbcraft/recipe/blank_adhesive.json',
            'data/herbcraft/recipe/armor_support_apply.json',
            'data/herbcraft/recipe/armor_support_refine.json',
            'assets/herbcraft/items/distilled_essence.json',
            'assets/herbcraft/models/item/distilled_essence.json',
            'assets/herbcraft/textures/item/distilled_essence.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_warming.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_strain.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_overload.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_heat_stir.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_cold_stagnation.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_overdrive_rebound.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_turbid_disturbance.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_qi_scattering.png',
            'assets/herbcraft_alchemy/textures/mob_effect/potion_qi_stagnation.png',
            'assets/herbcraft_alchemy/textures/mob_effect/clear_bearing.png',
        ],
    },
    f'cuisine/build/libs/herbcraft-cuisine-{CUISINE_VERSION}.jar': {
        'id': 'herbcraft_cuisine',
        'version': CUISINE_VERSION,
        'classes': [
            'com/herbcraft/cuisine/HerbcraftCuisine.class',
            'com/herbcraft/cuisine/client/HerbcraftCuisineClient.class',
            'com/herbcraft/cuisine/recipe/HodgepodgeRecipe.class',
            'com/herbcraft/cuisine/registry/HodgepodgeRules.class',
        ],
        'resources': [
            'fabric.mod.json',
            'data/herbcraft_cuisine/dishes.json',
            'data/herbcraft_cuisine/hodgepodge_rules.json',
            'data/herbcraft_cuisine/codex_loot_injections.json',
            'data/herbcraft_cuisine/codex_guides.json',
            'data/herbcraft_cuisine/guide_hints.json',
            'data/herbcraft_cuisine/environment/drinkable/water_bowl.json',
            'data/herbcraft_cuisine/environment/drinkable/deadly_hodgepodge.json',
            'data/herbcraft_cuisine/recipe/hodgepodge.json',
            'data/herbcraft_cuisine/recipe/raw_hundred_flower_feast.json',
            'data/herbcraft_cuisine/recipe/raw_deadly_hodgepodge.json',
        ],
    },
}

def json_load(zf, n):
    with zf.open(n) as f: return json.load(f)

def major(b):
    assert b[:4] == b'\xca\xfe\xba\xbe'
    return struct.unpack('>H', b[6:8])[0]

errs=[]
for rel, spec in JARS.items():
    p=ROOT/rel
    if not p.exists():
        errs.append(f'missing {rel}'); continue
    with zipfile.ZipFile(p) as zf:
        names=set(zf.namelist())
        mod=json_load(zf,'fabric.mod.json')
        if mod.get('id') != spec['id']:
            errs.append(f'{rel}: id {mod.get("id")} != {spec["id"]}')
        if mod.get('version') != spec['version']:
            errs.append(f'{rel}: version {mod.get("version")} != {spec["version"]}')
        for dep in ['fabricloader','minecraft','java','fabric-api']:
            if dep not in mod.get('depends',{}): errs.append(f'{rel}: missing dep {dep}')
        if spec['id'] == 'herbcraft_alchemy':
            expected_core_dep = f'>={CORE_VERSION}'
            if mod.get('depends',{}).get('herbcraft') != expected_core_dep:
                errs.append(f'{rel}: alchemy must depend on herbcraft {expected_core_dep}')
        if spec['id'] == 'herbcraft_cuisine':
            expected_core_dep = '>=1.1.3'
            if mod.get('depends',{}).get('herbcraft') != expected_core_dep:
                errs.append(f'{rel}: cuisine must depend on herbcraft {expected_core_dep}')
        for r in spec['resources']:
            if r not in names: errs.append(f'{rel}: missing {r}')
            elif r.endswith('.json'): json_load(zf,r)
        for c in spec['classes']:
            if c not in names: errs.append(f'{rel}: missing {c}')
            elif major(zf.read(c)) != 69: errs.append(f'{rel}: {c} is not Java 25 class version')
if errs:
    print('VERIFY FAILED')
    print('\n'.join('- '+e for e in errs))
    sys.exit(1)
print('VERIFY OK: all built jars contain expected metadata/resources/classes and Java 25 bytecode.')
