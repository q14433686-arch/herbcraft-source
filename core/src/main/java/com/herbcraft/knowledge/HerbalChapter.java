package com.herbcraft.knowledge;

import com.herbcraft.api.HerbcraftAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;

/** Thin facade for the herbal Codex chapter after Phase 3 decomposition. */
public final class HerbalChapter {
   public static final String CHAPTER_ID = "herbal";

   private HerbalChapter() {
   }

   public static void register() {
      HerbalChapterRegistration.register();
   }

   public static void applyFoodNatureEntries() {
      HerbalChapterFoodNatureProjection.applyFoodNatureEntries();
   }

   public static String foodNatureEntryId(Identifier itemId) {
      return HerbalChapterFoodNatureProjection.foodNatureEntryId(itemId);
   }

   public static boolean hasFoodNatureEntry(Identifier itemId) {
      return HerbalChapterFoodNatureProjection.hasFoodNatureEntry(itemId);
   }

   public static boolean hasEaten(ServerPlayer player, Item item) {
      return player.getStats().getValue(net.minecraft.stats.Stats.ITEM_USED.get(item)) > 0;
   }
}
