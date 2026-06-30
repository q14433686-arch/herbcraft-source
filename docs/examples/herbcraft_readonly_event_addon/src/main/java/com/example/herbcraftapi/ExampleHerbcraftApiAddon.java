package com.example.herbcraftapi;

import com.herbcraft.api.HerbcraftDataAPI;
import com.herbcraft.api.HerbcraftEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Documentation-only P4.4 example.
 *
 * <p>JSON should provide content. Java should query and observe Herbcraft state.
 * This example intentionally does not register Herbcraft nature, nutrition, or
 * food definitions from code.</p>
 */
public final class ExampleHerbcraftApiAddon implements ModInitializer {
   private static final Logger LOGGER = LoggerFactory.getLogger("example_herbcraft_api_addon");

   @Override
   public void onInitialize() {
      Identifier allium = Identifier.parse("minecraft:allium");
      HerbcraftDataAPI.nature(allium).ifPresent(nature -> LOGGER.info(
         "Allium nature is loaded: temperature={}, flavors={}, traits={}",
         nature.temperature(),
         nature.flavors(),
         nature.traits()
      ));

      HerbcraftEvents.NUTRITION_APPLIED.register((player, stack, itemId, profile) -> {
         HerbcraftDataAPI.ingredientEntryId(itemId).ifPresent(entryId -> LOGGER.debug(
            "{} received Herbcraft nutrition from {} / 食材录 entry {}: {}",
            player.getGameProfile().getName(),
            itemId,
            entryId,
            profile.values()
         ));
      });

      HerbcraftEvents.INGREDIENT_DISCOVERED.register((player, itemId, entryId) -> LOGGER.info(
         "{} discovered Herbcraft ingredient {} as {}",
         player.getGameProfile().getName(),
         itemId,
         entryId
      ));

      HerbcraftEvents.FOOD_NATURE_RESOLVED.register((player, stack, itemId, nature, result) -> LOGGER.debug(
         "{} resolved food-nature {}: temp={}, flavors={}, sevenEmotion={}",
         player.getGameProfile().getName(),
         itemId,
         nature.temperature(),
         nature.flavors(),
         result.sevenEmotionType()
      ));

      HerbcraftEvents.EXTERNAL_DATA_RELOADED.register((natureCount, nutritionCount, ingredientCount) -> LOGGER.info(
         "Herbcraft external data reloaded: natures={}, nutrition={}, ingredients={}",
         natureCount,
         nutritionCount,
         ingredientCount
      ));
   }
}
