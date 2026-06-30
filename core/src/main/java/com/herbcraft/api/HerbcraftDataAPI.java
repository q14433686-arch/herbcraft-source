package com.herbcraft.api;

import com.herbcraft.knowledge.HerbalChapter;
import com.herbcraft.knowledge.IngredientsChapter;
import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.nutrition.NutritionProfile;
import com.herbcraft.nutrition.NutritionRegistry;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Stable read-only data facade for code addons.
 *
 * <p>P4.2 intentionally exposes queries only. It does not register content, does
 * not mutate Herbcraft registries, and does not change reload order. Addons
 * should still express content through the JSON paths documented in
 * {@code docs/JSON_EXTENSION_SPEC_1_1_3.md} whenever possible.</p>
 *
 * <p>Identifier-based nature/nutrition queries read Herbcraft's current data
 * tables by ID. Optional bundled compat data may exist even when the target mod
 * item is absent, so addons that need a live item should also check
 * {@link #isRegisteredItem(Identifier)} or query by {@link Item} /
 * {@link ItemStack}.</p>
 */
public final class HerbcraftDataAPI {
   private HerbcraftDataAPI() {
   }

   /** Returns whether the item ID currently exists in Minecraft's item registry. */
   public static boolean isRegisteredItem(Identifier itemId) {
      return itemId != null && BuiltInRegistries.ITEM.containsKey(itemId);
   }

   /** Returns the item registry ID for a live item object, if available. */
   public static Optional<Identifier> itemId(Item item) {
      if (item == null) {
         return Optional.empty();
      }
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return id == null ? Optional.empty() : Optional.of(id);
   }

   /** Returns the explicit Herbcraft nature profile for an item ID, if one is loaded. */
   public static Optional<NatureProfile> nature(Identifier itemId) {
      return itemId == null ? Optional.empty() : HerbNatureRegistry.explicit(itemId);
   }

   /** Returns the explicit Herbcraft nature profile for a live item, if one is loaded. */
   public static Optional<NatureProfile> nature(Item item) {
      return itemId(item).flatMap(HerbcraftDataAPI::nature);
   }

   /** Returns the explicit Herbcraft nature profile for an item stack, if one is loaded. */
   public static Optional<NatureProfile> nature(ItemStack stack) {
      return stack == null || stack.isEmpty() ? Optional.empty() : nature(stack.getItem());
   }

   public static boolean hasNature(Identifier itemId) {
      return nature(itemId).isPresent();
   }

   public static boolean hasNature(Item item) {
      return nature(item).isPresent();
   }

   public static boolean hasNature(ItemStack stack) {
      return nature(stack).isPresent();
   }

   /** Returns the Herbcraft nutrition profile for an item ID, if one is loaded. */
   public static Optional<NutritionProfile> nutrition(Identifier itemId) {
      return itemId == null ? Optional.empty() : NutritionRegistry.get(itemId);
   }

   /** Returns the Herbcraft nutrition profile for a live item, if one is loaded. */
   public static Optional<NutritionProfile> nutrition(Item item) {
      return item == null ? Optional.empty() : NutritionRegistry.get(item);
   }

   /** Returns the Herbcraft nutrition profile for an item stack, if one is loaded. */
   public static Optional<NutritionProfile> nutrition(ItemStack stack) {
      return stack == null || stack.isEmpty() ? Optional.empty() : NutritionRegistry.get(stack);
   }

   public static boolean hasNutrition(Identifier itemId) {
      return nutrition(itemId).isPresent();
   }

   public static boolean hasNutrition(Item item) {
      return nutrition(item).isPresent();
   }

   public static boolean hasNutrition(ItemStack stack) {
      return nutrition(stack).isPresent();
   }

   /** Returns whether the item has a 食材录 / Ingredient Codex entry. */
   public static boolean hasIngredientEntry(Identifier itemId) {
      return itemId != null && IngredientsChapter.hasEntry(itemId);
   }

   public static boolean hasIngredientEntry(Item item) {
      return itemId(item).map(HerbcraftDataAPI::hasIngredientEntry).orElse(false);
   }

   public static boolean hasIngredientEntry(ItemStack stack) {
      return stack != null && !stack.isEmpty() && hasIngredientEntry(stack.getItem());
   }

   /** Returns the namespace-aware 食材录 entry ID if the item has one. */
   public static Optional<String> ingredientEntryId(Identifier itemId) {
      return hasIngredientEntry(itemId) ? Optional.of(IngredientsChapter.entryId(itemId)) : Optional.empty();
   }

   public static Optional<String> ingredientEntryId(Item item) {
      return itemId(item).flatMap(HerbcraftDataAPI::ingredientEntryId);
   }

   public static Optional<String> ingredientEntryId(ItemStack stack) {
      return stack == null || stack.isEmpty() ? Optional.empty() : ingredientEntryId(stack.getItem());
   }

   /** Returns whether the live item is a full Herbcraft herb food definition. */
   public static boolean isHerb(Item item) {
      return herb(item).isPresent();
   }

   public static boolean isHerb(ItemStack stack) {
      return stack != null && !stack.isEmpty() && isHerb(stack.getItem());
   }

   /** Returns the read-only Herbcraft herb definition for a live item, if any. */
   public static Optional<HerbDefinition> herb(Item item) {
      return item == null ? Optional.empty() : HerbcraftAPI.getDefinition(item);
   }

   public static Optional<HerbDefinition> herb(ItemStack stack) {
      return stack == null || stack.isEmpty() ? Optional.empty() : herb(stack.getItem());
   }

   /**
    * Returns whether the item currently has a 百草志 / 药食志 food-nature entry.
    *
    * <p>This is stricter than {@link #hasNature(Identifier)}: it only becomes
    * true when the runtime item exists and Herbcraft has registered the Codex
    * food-nature entry from current data.</p>
    */
   public static boolean hasFoodNatureEntry(Identifier itemId) {
      return itemId != null && HerbalChapter.hasFoodNatureEntry(itemId);
   }

   public static boolean hasFoodNatureEntry(Item item) {
      return itemId(item).map(HerbcraftDataAPI::hasFoodNatureEntry).orElse(false);
   }

   public static boolean hasFoodNatureEntry(ItemStack stack) {
      return stack != null && !stack.isEmpty() && hasFoodNatureEntry(stack.getItem());
   }

   /** Returns the 药食志 entry ID if one is currently registered. */
   public static Optional<String> foodNatureEntryId(Identifier itemId) {
      return hasFoodNatureEntry(itemId) ? Optional.of(HerbalChapter.foodNatureEntryId(itemId)) : Optional.empty();
   }

   public static Optional<String> foodNatureEntryId(Item item) {
      return itemId(item).flatMap(HerbcraftDataAPI::foodNatureEntryId);
   }

   public static Optional<String> foodNatureEntryId(ItemStack stack) {
      return stack == null || stack.isEmpty() ? Optional.empty() : foodNatureEntryId(stack.getItem());
   }

   /** Returns true when current data can drive the food-nature bridge for this item ID. */
   public static boolean hasFoodNatureData(Identifier itemId) {
      return hasNature(itemId) && hasNutrition(itemId) && hasIngredientEntry(itemId);
   }

   public static boolean hasFoodNatureData(Item item) {
      return itemId(item).map(HerbcraftDataAPI::hasFoodNatureData).orElse(false);
   }

   public static boolean hasFoodNatureData(ItemStack stack) {
      return stack != null && !stack.isEmpty() && hasFoodNatureData(stack.getItem());
   }

   /** Snapshot of current loaded nature profiles keyed by item ID. */
   public static Map<Identifier, NatureProfile> natureProfiles() {
      return HerbNatureRegistry.profiles();
   }

   /** Snapshot of current loaded nutrition profiles keyed by item ID. */
   public static Map<Identifier, NutritionProfile> nutritionProfiles() {
      return NutritionRegistry.profiles();
   }

   /** Snapshot of current item ID to 食材录 entry ID mappings. */
   public static Map<Identifier, String> ingredientEntryIds() {
      return IngredientsChapter.itemEntryIds();
   }
}
