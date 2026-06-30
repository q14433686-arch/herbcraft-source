package com.herbcraft.nutrition;

import com.herbcraft.api.HerbcraftEvents;
import com.herbcraft.guide.GuideHintManager;
import com.herbcraft.knowledge.IngredientsChapter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Knowledge/discovery side effects triggered by food consumption. */
public final class NutritionDiscoveryHooks {
   private NutritionDiscoveryHooks() {
   }

   public static void onFoodConsumed(ServerPlayer player, ItemStack stack, NutritionProfile profile) {
      Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
      if (itemId != null) {
         HerbcraftEvents.NUTRITION_APPLIED.invoker().onNutritionApplied(player, stack.copy(), itemId, profile);
      }

      if (itemId != null && IngredientsChapter.hasEntry(itemId)) {
         String entryId = IngredientsChapter.entryId(itemId);
         com.herbcraft.api.KnowledgeManager.markEncountered(player, IngredientsChapter.CHAPTER_ID, entryId);
         boolean firstTaste = com.herbcraft.api.KnowledgeManager.markTasted(player, IngredientsChapter.CHAPTER_ID, entryId);
         com.herbcraft.api.KnowledgeAPI.markPracticed(player, com.herbcraft.api.KnowledgeAPI.key(IngredientsChapter.CHAPTER_ID, entryId));
         GuideHintManager.show(player, "herbcraft:first_ingredient_tasted");
         if (firstTaste) {
            HerbcraftEvents.INGREDIENT_DISCOVERED.invoker().onIngredientDiscovered(player, itemId, entryId);
         }
      }
   }
}
