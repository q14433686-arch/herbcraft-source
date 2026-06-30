package com.herbcraft.cuisine;

import com.herbcraft.cuisine.knowledge.CuisineAcquisitionHooks;
import com.herbcraft.cuisine.knowledge.CuisineChapter;
import com.herbcraft.cuisine.knowledge.CuisineCodexHooks;
import com.herbcraft.cuisine.knowledge.CuisineUsageHints;
import com.herbcraft.cuisine.logic.WaterBowlInteraction;
import com.herbcraft.guide.GuideHintManager;
import com.herbcraft.cuisine.registry.CuisineComponents;
import com.herbcraft.cuisine.registry.CuisineItems;
import com.herbcraft.cuisine.registry.CuisineRecipes;
import com.herbcraft.cuisine.registry.DishRegistry;
import com.herbcraft.cuisine.registry.HodgepodgeRules;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HerbcraftCuisine implements ModInitializer {
   public static final String MOD_ID = "herbcraft_cuisine";
   public static final Logger LOGGER = LoggerFactory.getLogger("herbcraft_cuisine");

   public void onInitialize() {
      CuisineComponents.initialize();
      CuisineItems.initialize();
      HodgepodgeRules.loadFromBundledJson();
      GuideHintManager.loadFromBundledJson("data/herbcraft_cuisine/guide_hints.json");
      DishRegistry.initialize();
      CuisineRecipes.initialize();
      WaterBowlInteraction.register();
      CuisineChapter.register();
      CuisineCodexHooks.register();
      CuisineAcquisitionHooks.register();
      CuisineUsageHints.register();
      Registry.register(
         BuiltInRegistries.CREATIVE_MODE_TAB,
         Identifier.fromNamespaceAndPath("herbcraft_cuisine", "cuisine"),
         CreativeModeTab.builder(Row.TOP, 0)
            .title(Component.translatable("itemGroup.herbcraft_cuisine.cuisine"))
            .icon(() -> new ItemStack(DishRegistry.dishes().isEmpty() ? CuisineItems.HODGEPODGE : DishRegistry.dishes().getFirst().item()))
            .displayItems((parameters, output) -> {
               output.accept(CuisineItems.WATER_BOWL);
               DishRegistry.dishes().forEach(dish -> {
                  if (dish.rawItem() != null) {
                     output.accept(dish.rawItem());
                  }

                  output.accept(dish.item());
               });
               output.accept(CuisineItems.HODGEPODGE);
            })
            .build()
      );
      LOGGER.info("Herbcraft: Cuisine loaded. Registered {} curated dishes and the hodgepodge bowl.", DishRegistry.dishes().size());
   }
}
