package com.herbcraft.knowledge;

import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.nutrition.NutritionRegistry;
import com.herbcraft.registry.HerbFoodRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Dynamic food-nature projection into the herbal Codex. */
public final class HerbalChapterFoodNatureProjection {
   private static final String FOOD_NATURE_SECTION = "foods";
   private static final Set<String> FOOD_NATURE_ENTRY_IDS = new LinkedHashSet<>();

   private HerbalChapterFoodNatureProjection() {
   }

   public static void applyFoodNatureEntries() {
      clearFoodNatureEntries();
      List<FoodNatureCandidate> candidates = new ArrayList<>();
      for (Identifier itemId : HerbNatureRegistry.profiles().keySet()) {
         if (!BuiltInRegistries.ITEM.containsKey(itemId) || NutritionRegistry.get(itemId).isEmpty() || !IngredientsChapter.hasEntry(itemId)) {
            continue;
         }
         Item item = BuiltInRegistries.ITEM.getValue(itemId);
         if (HerbFoodRegistry.get(item) != null) {
            continue;
         }
         candidates.add(new FoodNatureCandidate(itemId, item, foodNatureEntryId(itemId)));
      }
      if (candidates.isEmpty()) {
         return;
      }
      KnowledgeAPI.registerSection(HerbalChapter.CHAPTER_ID, FOOD_NATURE_SECTION, Component.translatable("book.herbcraft.section.herbal.foods"));
      for (FoodNatureCandidate candidate : candidates) {
         KnowledgeAPI.registerEntry(
            HerbalChapter.CHAPTER_ID,
            new KnowledgeAPI.Entry(
               candidate.entryId(),
               FOOD_NATURE_SECTION,
               Component.translatable(candidate.item().getDescriptionId()),
               (player, state) -> HerbalChapterTextBuilders.foodNatureLines(player, candidate.item(), candidate.entryId()),
               null,
               null
            )
         );
         FOOD_NATURE_ENTRY_IDS.add(candidate.entryId());
      }
      com.herbcraft.Herbcraft.LOGGER.info("Loaded {} food-nature entries into the Herbal Codex", candidates.size());
   }

   public static String foodNatureEntryId(Identifier itemId) {
      if ("minecraft".equals(itemId.getNamespace())) {
         return itemId.getPath();
      }
      return itemId.getNamespace() + "__" + itemId.getPath().replace('/', '_');
   }

   public static boolean hasFoodNatureEntry(Identifier itemId) {
      return KnowledgeAPI.chapter(HerbalChapter.CHAPTER_ID).map(chapter -> chapter.entry(foodNatureEntryId(itemId)) != null).orElse(false);
   }

   private static void clearFoodNatureEntries() {
      for (String entryId : FOOD_NATURE_ENTRY_IDS) {
         KnowledgeAPI.removeEntry(HerbalChapter.CHAPTER_ID, entryId);
      }
      FOOD_NATURE_ENTRY_IDS.clear();
      KnowledgeAPI.removeSectionIfEmpty(HerbalChapter.CHAPTER_ID, FOOD_NATURE_SECTION);
   }

   private record FoodNatureCandidate(Identifier itemId, Item item, String entryId) {
   }
}
