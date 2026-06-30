package com.herbcraft.cuisine.knowledge;

import com.herbcraft.api.HerbUsageAPI;
import com.herbcraft.cuisine.registry.DishRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Pushes cuisine usage hints into core's HerbUsageAPI.
 *
 * - Every item in #herbcraft:herb can go into the hodgepodge bowl ("可入盅").
 * - Every ingredient of a curated/medicinal dish can go into cuisine ("可入药膳").
 *   Only the category is revealed, never the dish name, so hidden dishes
 *   (hundred_flower_feast / deadly_hodgepodge) are not spoiled.
 */
public final class CuisineUsageHints {
   private static final String KEY_HODGEPODGE = "usage.herbcraft.hodgepodge";
   private static final String KEY_CUISINE = "usage.herbcraft.cuisine";
   private static final TagKey<Item> HERB_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("herbcraft", "herb"));

   private CuisineUsageHints() {
   }

   public static void register() {
      HerbUsageAPI.registerTagUsage(HERB_TAG, KEY_HODGEPODGE);
      for (DishRegistry.Dish dish : DishRegistry.dishes()) {
         for (DishRegistry.IngredientSpec spec : dish.ingredients()) {
            if (spec.item() != null) {
               HerbUsageAPI.registerUsage(spec.item(), KEY_CUISINE);
            } else if (spec.tag() != null) {
               HerbUsageAPI.registerTagUsage(spec.tag(), KEY_CUISINE);
            }
         }
      }
   }
}
