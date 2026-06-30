package com.herbcraft.alchemy.armor;

import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

/** Any armor piece + implemented distilled essence orientation -> same armor with support component. */
public class ArmorSupportRecipe extends CustomRecipe {
   public boolean matches(CraftingInput input, Level level) {
      return analyze(input).isPresent();
   }

   public ItemStack assemble(CraftingInput input) {
      return analyze(input).map(result -> {
         ItemStack stack = result.armor().copyWithCount(1);
         stack.set(ArmorSupportComponents.ARMOR_SUPPORT, new ArmorSupportComponent(result.support().id(), result.distilled().sourceEssence(), result.distilled().orientation(), result.support().bufferAmount()));
         return stack;
      }).orElse(ItemStack.EMPTY);
   }

   private static Optional<Result> analyze(CraftingInput input) {
      ItemStack armor = ItemStack.EMPTY;
      DistilledEssenceComponent distilled = null;
      int filled = 0;
      for (int i = 0; i < input.size(); i++) {
         ItemStack stack = input.getItem(i);
         if (stack.isEmpty()) continue;
         filled++;
         DistilledEssenceComponent candidate = stack.get(DistillationComponents.DISTILLED_ESSENCE);
         if (candidate != null) {
            if (distilled != null) return Optional.empty();
            distilled = candidate;
         } else if (isSupportedArmor(stack)) {
            if (!armor.isEmpty()) return Optional.empty();
            armor = stack;
         } else {
            return Optional.empty();
         }
      }
      if (filled != 2 || armor.isEmpty() || distilled == null) return Optional.empty();
      ItemStack finalArmor = armor;
      DistilledEssenceComponent finalDistilled = distilled;
      return ArmorSupportRules.supportForOrientation(finalDistilled.orientation()).map(support -> new Result(finalArmor, finalDistilled, support));
   }

   private static boolean isSupportedArmor(ItemStack stack) {
      Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
      return equippable != null && equippable.slot().isArmor() && equippable.slot() != EquipmentSlot.BODY;
   }

   public RecipeSerializer<ArmorSupportRecipe> getSerializer() {
      return ArmorSupportRecipes.ARMOR_SUPPORT;
   }

   public CraftingBookCategory category() {
      return CraftingBookCategory.EQUIPMENT;
   }

   private record Result(ItemStack armor, DistilledEssenceComponent distilled, ArmorSupportRules.Support support) {
   }
}
