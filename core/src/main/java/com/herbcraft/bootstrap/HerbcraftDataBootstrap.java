package com.herbcraft.bootstrap;

import com.herbcraft.data.HerbcraftExternalDataReloadListener;
import com.herbcraft.effect.HerbcraftMobEffects;
import com.herbcraft.guide.GuideHintManager;
import com.herbcraft.knowledge.KnowledgeRumorRegistry;
import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.nutrition.NutritionRegistry;
import com.herbcraft.nutrition.NutritionRules;
import com.herbcraft.pharmacology.PharmacologyRules;
import com.herbcraft.logic.SpecialCounters;
import com.herbcraft.registry.HerbFoodRegistry;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

/** Data/bootstrap responsibilities only: bundled data load, startup external load, reload hooks, default components. */
public final class HerbcraftDataBootstrap {
   private HerbcraftDataBootstrap() {
   }

   public static void initialize() {
      HerbcraftMobEffects.initialize();
      HerbFoodRegistry.loadFromBundledJson();
      HerbFoodRegistry.loadExternalStartupResources();
      HerbNatureRegistry.loadFromBundledJson();
      PharmacologyRules.loadFromBundledJson();
      KnowledgeRumorRegistry.loadFromBundledJson();
      NutritionRegistry.loadFromBundledJson();
      NutritionRules.loadFromBundledJson();
      SpecialCounters.loadFromBundledJson();
      GuideHintManager.loadFromBundledJson("data/herbcraft/guide_hints.json");
      ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new HerbcraftExternalDataReloadListener());
      DefaultItemComponentEvents.MODIFY.register(HerbFoodRegistry::registerDefaultComponents);
   }
}
