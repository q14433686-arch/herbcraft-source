package com.herbcraft.alchemy;

import com.herbcraft.alchemy.coating.CoatingComponents;
import com.herbcraft.alchemy.coating.CoatingHitHandler;
import com.herbcraft.alchemy.coating.CoatingItems;
import com.herbcraft.alchemy.coating.CoatingRules;
import com.herbcraft.alchemy.coating.CoatingRecipes;
import com.herbcraft.alchemy.knowledge.AlchemyAcquisitionHooks;
import com.herbcraft.alchemy.knowledge.AlchemyChapter;
import com.herbcraft.alchemy.knowledge.AlchemyCodexHooks;
import com.herbcraft.alchemy.knowledge.AlchemyUsageHints;
import com.herbcraft.alchemy.potion.AdvancedPotionInventoryHooks;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.guide.GuideHintManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HerbcraftAlchemy implements ModInitializer {
   public static final String MOD_ID = "herbcraft_alchemy";
   public static final Logger LOGGER = LoggerFactory.getLogger("herbcraft_alchemy");

   public void onInitialize() {
      com.herbcraft.alchemy.effect.AlchemyMobEffects.initialize();
      com.herbcraft.alchemy.armor.ArmorSupportComponents.initialize();
      com.herbcraft.alchemy.distillation.DistillationComponents.initialize();
      CoatingComponents.initialize();
      AlchemyPotionComponents.initialize();
      CoatingRules.loadFromBundledJson();
      com.herbcraft.alchemy.coating.ProcessedCoatingRules.loadFromBundledJson();
      com.herbcraft.alchemy.distillation.DistillationRules.loadFromBundledJson();
      com.herbcraft.alchemy.armor.ArmorSupportRules.loadFromBundledJson();
      com.herbcraft.alchemy.distillation.DistillationItems.initialize();
      CoatingItems.initialize();
      CoatingRecipes.initialize();
      com.herbcraft.alchemy.distillation.DistillationRecipes.initialize();
      com.herbcraft.alchemy.armor.ArmorSupportRecipes.initialize();
      AdvancedPotionStackInitializer.loadBonusPoolsFromBundledJson();
      com.herbcraft.alchemy.potion.PotionProcessingRules.loadFromBundledJson();
      GuideHintManager.loadFromBundledJson("data/herbcraft_alchemy/guide_hints.json");
      AlchemyRegistries.initialize();
      CoatingHitHandler.register();
      AlchemyChapter.register();
      AlchemyCodexHooks.register();
      AlchemyUsageHints.register();
      AlchemyAcquisitionHooks.register();
      AdvancedPotionInventoryHooks.register();
      com.herbcraft.alchemy.potion.PlayerPotionBurdenRuntime.register();
      com.herbcraft.alchemy.armor.ArmorSupportRuntime.register();
      LOGGER.info("Herbcraft: Alchemy loaded. Registered essences, brewing mixes and the v3.4 weapon coating system.");
   }
}
