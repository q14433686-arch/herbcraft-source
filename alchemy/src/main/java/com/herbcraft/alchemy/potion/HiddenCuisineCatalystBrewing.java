package com.herbcraft.alchemy.potion;

import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public final class HiddenCuisineCatalystBrewing {
   private static final Identifier HUNDRED_FLOWER_FEAST = Identifier.fromNamespaceAndPath("herbcraft_cuisine", "hundred_flower_feast");
   private static final Identifier DEADLY_HODGEPODGE = Identifier.fromNamespaceAndPath("herbcraft_cuisine", "deadly_hodgepodge");

   private HiddenCuisineCatalystBrewing() {
   }

   public static boolean isCatalystItem(ItemStack stack) {
      Identifier ingredientId = BuiltInRegistries.ITEM.getKey(stack.getItem());
      return HUNDRED_FLOWER_FEAST.equals(ingredientId) || DEADLY_HODGEPODGE.equals(ingredientId);
   }

   public static boolean isBrewable(NonNullList<ItemStack> items) {
      ItemStack ingredient = items.get(3);
      Identifier ingredientId = BuiltInRegistries.ITEM.getKey(ingredient.getItem());
      if (!isCatalystItem(ingredient)) {
         return false;
      }
      for (int slot = 0; slot < 3; slot++) {
         if (canUpgrade(items.get(slot), ingredientId)) {
            return true;
         }
      }
      return false;
   }

   public static void brew(NonNullList<ItemStack> items) {
      ItemStack ingredient = items.get(3);
      Identifier ingredientId = BuiltInRegistries.ITEM.getKey(ingredient.getItem());
      boolean changed = false;
      for (int slot = 0; slot < 3; slot++) {
         ItemStack stack = items.get(slot);
         if (canUpgrade(stack, ingredientId)) {
            ItemStack upgraded = stack.copy();
            upgraded.set(AlchemyPotionComponents.POTION_QUALITY, AlchemyPotionComponents.PotionQuality.EXCEPTIONAL);
            items.set(slot, upgraded);
            changed = true;
         }
      }
      if (changed) {
         ingredient.shrink(1);
      }
   }

   private static boolean canUpgrade(ItemStack stack, Identifier catalystId) {
      if (stack.isEmpty()) {
         return false;
      }
      PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
      if (contents == null) {
         return false;
      }
      Optional<String> path = AdvancedPotionStackInitializer.advancedPotionPath(contents);
      if (path.isEmpty()) {
         return false;
      }
      if (HUNDRED_FLOWER_FEAST.equals(catalystId)) {
         return path.get().endsWith("_clarified");
      }
      if (DEADLY_HODGEPODGE.equals(catalystId)) {
         return path.get().endsWith("_murky");
      }
      return false;
   }
}
