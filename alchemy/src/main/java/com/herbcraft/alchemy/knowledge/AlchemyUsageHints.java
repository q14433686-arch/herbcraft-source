package com.herbcraft.alchemy.knowledge;

import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.api.HerbUsageAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Pushes "可萃精华" usage hints into core's HerbUsageAPI for every raw material
 * listed by the data-driven essence definitions in data/herbcraft_alchemy/essences.json.
 */
public final class AlchemyUsageHints {
   private static final String KEY = "usage.herbcraft.essence";

   private AlchemyUsageHints() {
   }

   public static void register() {
      for (Item essenceItem : AlchemyRegistries.essenceItems()) {
         AlchemyRegistries.essenceInfo(essenceItem).ifPresent(info -> info.sourceItems().forEach(AlchemyUsageHints::registerSource));
      }
   }

   private static void registerSource(String idString) {
      Identifier id = Identifier.parse(idString);
      if (BuiltInRegistries.ITEM.containsKey(id)) {
         HerbUsageAPI.registerUsage((Item)BuiltInRegistries.ITEM.getValue(id), KEY);
      }
   }
}
