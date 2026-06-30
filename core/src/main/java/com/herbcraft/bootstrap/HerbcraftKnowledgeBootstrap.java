package com.herbcraft.bootstrap;

import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.advancement.HerbcraftAdvancementHooks;
import com.herbcraft.knowledge.CodexComponents;
import com.herbcraft.knowledge.CodexItems;
import com.herbcraft.knowledge.CodexLoot;
import com.herbcraft.knowledge.CreativeKnowledgeItems;
import com.herbcraft.knowledge.HerbalChapter;
import com.herbcraft.knowledge.IngredientsChapter;
import com.herbcraft.knowledge.KnowledgeInventoryScanner;
import com.herbcraft.logic.LastMealTracker;
import com.herbcraft.nutrition.NutritionDebugCommands;
import com.herbcraft.registry.HerbFoodRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** Knowledge/codex/bootstrap responsibilities only. */
public final class HerbcraftKnowledgeBootstrap {
   private HerbcraftKnowledgeBootstrap() {
   }

   public static void initialize() {
      NutritionDebugCommands.register();
      HerbFoodRegistry.getHerbDefinitions().keySet().forEach(item -> com.herbcraft.api.HerbUsageAPI.registerUsage(item, "usage.herbcraft.edible"));
      CodexComponents.initialize();
      CodexItems.initialize();
      HerbalChapter.register();
      IngredientsChapter.register();
      CodexLoot.register();
      KnowledgeInventoryScanner.register();
      KnowledgeInventoryScanner.registerHandler((player, stack) -> {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         if (id != null) {
            if (HerbFoodRegistry.get(stack.getItem()) != null) {
               KnowledgeManager.markEncountered(player, "herbal", id.getPath());
            } else if (HerbalChapter.hasFoodNatureEntry(id)) {
               KnowledgeManager.markEncountered(player, "herbal", HerbalChapter.foodNatureEntryId(id));
            }
            if (IngredientsChapter.hasEntry(id)) {
               KnowledgeManager.markEncountered(player, IngredientsChapter.CHAPTER_ID, IngredientsChapter.entryId(id));
            }
         }
      });
      CreativeKnowledgeItems.register();
      LastMealTracker.register();
      HerbcraftAdvancementHooks.register();
   }
}
