package com.herbcraft.api;

import com.herbcraft.nature.NatureProfile;
import com.herbcraft.nutrition.NutritionProfile;
import com.herbcraft.pharmacology.PharmacologyResult;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Public observational events for code addons.
 *
 * <p>P4.3 events are intentionally read-only notifications. Listeners may
 * observe Herbcraft behavior, trigger their own addon-side logic, or schedule
 * later work, but these events do not let listeners replace Herbcraft's
 * nutrition, pharmacology, knowledge, or reload results.</p>
 */
public final class HerbcraftEvents {
   private HerbcraftEvents() {
   }

   /** Fired after a full Herbcraft herb food has been consumed and processed. */
   public static final Event<HerbConsumed> HERB_CONSUMED = EventFactory.createArrayBacked(
      HerbConsumed.class,
      callbacks -> (player, item) -> {
         for (HerbConsumed callback : callbacks) {
            callback.onHerbConsumed(player, item);
         }
      }
   );

   /** Fired after a non-empty Herbcraft nutrition profile has been applied to a player. */
   public static final Event<NutritionApplied> NUTRITION_APPLIED = EventFactory.createArrayBacked(
      NutritionApplied.class,
      callbacks -> (player, stack, itemId, profile) -> {
         for (NutritionApplied callback : callbacks) {
            callback.onNutritionApplied(player, stack, itemId, profile);
         }
      }
   );

   /** Fired the first time an Ingredient Codex / 食材录 entry is tasted by a player. */
   public static final Event<IngredientDiscovered> INGREDIENT_DISCOVERED = EventFactory.createArrayBacked(
      IngredientDiscovered.class,
      callbacks -> (player, itemId, entryId) -> {
         for (IngredientDiscovered callback : callbacks) {
            callback.onIngredientDiscovered(player, itemId, entryId);
         }
      }
   );

   /** Fired after the food-nature bridge resolves pharmacology for an ordinary food. */
   public static final Event<FoodNatureResolved> FOOD_NATURE_RESOLVED = EventFactory.createArrayBacked(
      FoodNatureResolved.class,
      callbacks -> (player, stack, itemId, nature, result) -> {
         for (FoodNatureResolved callback : callbacks) {
            callback.onFoodNatureResolved(player, stack, itemId, nature, result);
         }
      }
   );

   /** Fired after Herbcraft's external reload-safe data merge layer has reloaded. */
   public static final Event<ExternalDataReloaded> EXTERNAL_DATA_RELOADED = EventFactory.createArrayBacked(
      ExternalDataReloaded.class,
      callbacks -> (natureProfiles, nutritionProfiles, ingredientEntries) -> {
         for (ExternalDataReloaded callback : callbacks) {
            callback.onExternalDataReloaded(natureProfiles, nutritionProfiles, ingredientEntries);
         }
      }
   );

   @FunctionalInterface
   public interface HerbConsumed {
      void onHerbConsumed(ServerPlayer player, Item item);
   }

   @FunctionalInterface
   public interface NutritionApplied {
      void onNutritionApplied(ServerPlayer player, ItemStack stack, Identifier itemId, NutritionProfile profile);
   }

   @FunctionalInterface
   public interface IngredientDiscovered {
      void onIngredientDiscovered(ServerPlayer player, Identifier itemId, String entryId);
   }

   @FunctionalInterface
   public interface FoodNatureResolved {
      void onFoodNatureResolved(ServerPlayer player, ItemStack stack, Identifier itemId, NatureProfile nature, PharmacologyResult result);
   }

   @FunctionalInterface
   public interface ExternalDataReloaded {
      void onExternalDataReloaded(int natureProfileCount, int nutritionProfileCount, int ingredientEntryCount);
   }
}
