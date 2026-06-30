package com.herbcraft.cuisine.knowledge;

import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.api.KnowledgePracticeRegistry;
import com.herbcraft.api.KnowledgeAPI.State;
import com.herbcraft.cuisine.registry.DishRegistry;
import com.herbcraft.knowledge.KnowledgeInventoryScanner;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CuisineAcquisitionHooks {
   private static final Map<Item, String> DISH_BY_ITEM = new HashMap<>();
   private static final Map<Item, String> DISH_BY_RAW_ITEM = new HashMap<>();

   private CuisineAcquisitionHooks() {
   }

   public static void register() {
      for (DishRegistry.Dish dish : DishRegistry.dishes()) {
         DISH_BY_ITEM.put(dish.item(), dish.id());
         if (dish.rawItem() != null) {
            DISH_BY_RAW_ITEM.put(dish.rawItem(), dish.id());
         }
         dish.ingredients().forEach(spec -> {
            if (spec.item() != null) {
               Identifier id = BuiltInRegistries.ITEM.getKey(spec.item());
               if (id != null) KnowledgePracticeRegistry.registerRoute("herbal/" + id.getPath(), "cuisine:dish");
            }
         });
      }

      KnowledgeInventoryScanner.registerHandler(CuisineAcquisitionHooks::onStack);
   }

   private static void onStack(ServerPlayer player, ItemStack stack) {
      String dishId = DISH_BY_ITEM.get(stack.getItem());
      if (dishId != null) {
         if (player.getStats().getValue(Stats.ITEM_CRAFTED.get(stack.getItem())) > 0) {
            KnowledgeManager.markPracticed(player, "cuisine", dishId, true);
            for (DishRegistry.Dish dish : DishRegistry.dishes()) {
               if (dish.id().equals(dishId)) {
                  dish.ingredients().forEach(spec -> {
                     if (spec.item() != null) {
                        Identifier ingId = BuiltInRegistries.ITEM.getKey(spec.item());
                        if (ingId != null) KnowledgeManager.markPracticed(player, "herbal", ingId.getPath(), false);
                     }
                  });
                  break;
               }
            }
         } else {
            KnowledgeManager.markHeard(player, "cuisine", dishId, true);
         }
      } else {
         String rawDishId = DISH_BY_RAW_ITEM.get(stack.getItem());
         if (rawDishId != null) {
            KnowledgeManager.markHeard(player, "cuisine", rawDishId, true);
         }
      }
   }
}
