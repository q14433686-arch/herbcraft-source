package com.herbcraft.alchemy.distillation;

import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

/** Custom brewing route: matching base potion + componentized distilled essence -> processing path. */
public final class DistilledEssenceBrewing {
   private DistilledEssenceBrewing() {
   }

   public static boolean isCatalystItem(ItemStack stack) {
      return isPotionRoutingCatalyst(stack) || isDistillationCatalyst(stack);
   }

   public static boolean isDistillationInput(ItemStack stack) {
      return AlchemyRegistries.essenceInfo(stack.getItem()).isPresent();
   }

   private static boolean isPotionRoutingCatalyst(ItemStack stack) {
      return stack.is(DistillationItems.DISTILLED_ESSENCE) && stack.get(DistillationComponents.DISTILLED_ESSENCE) != null;
   }

   private static boolean isDistillationCatalyst(ItemStack stack) {
      for (DistillationRules.Orientation orientation : DistillationRules.implementedOrientations()) {
         if (stack.is(orientation.catalyst())) {
            return true;
         }
      }
      return false;
   }

   public static boolean isBrewable(NonNullList<ItemStack> items) {
      ItemStack ingredient = items.get(3);
      for (int slot = 0; slot < 3; slot++) {
         if (targetPotion(items.get(slot), ingredient).isPresent() || distilledOutput(items.get(slot), ingredient).isPresent()) {
            return true;
         }
      }
      return false;
   }

   public static void brew(NonNullList<ItemStack> items) {
      ItemStack ingredient = items.get(3);
      boolean changed = false;
      for (int slot = 0; slot < 3; slot++) {
         Optional<Holder<Potion>> target = targetPotion(items.get(slot), ingredient);
         Optional<ItemStack> distilled = distilledOutput(items.get(slot), ingredient);
         if (target.isPresent()) {
            ItemStack upgraded = items.get(slot).copy();
            upgraded.set(DataComponents.POTION_CONTENTS, new PotionContents(target.get()));
            upgraded.remove(AlchemyPotionComponents.POTION_QUALITY);
            upgraded.remove(AlchemyPotionComponents.POTION_BONUS);
            upgraded.remove(AlchemyPotionComponents.POTION_PROCESSING_STYLE);
            upgraded.remove(AlchemyPotionComponents.POTION_BURDEN);
            upgraded.remove(AlchemyPotionComponents.ADVANCED_POTION_INITIALIZED);
            items.set(slot, upgraded);
            changed = true;
         } else if (distilled.isPresent()) {
            items.set(slot, distilled.get());
            changed = true;
         }
      }
      if (changed) {
         ingredient.shrink(1);
      }
   }

   private static Optional<ItemStack> distilledOutput(ItemStack input, ItemStack catalyst) {
      Optional<AlchemyRegistries.EssenceInfo> essence = AlchemyRegistries.essenceInfo(input.getItem());
      if (essence.isEmpty()) {
         return Optional.empty();
      }
      for (DistillationRules.Orientation orientation : DistillationRules.implementedOrientations()) {
         if (catalyst.is(orientation.catalyst())) {
            return Optional.of(DistillationItems.stack(essence.get().id(), orientation.id()));
         }
      }
      return Optional.empty();
   }

   private static Optional<Holder<Potion>> targetPotion(ItemStack potionStack, ItemStack ingredient) {
      if (potionStack.isEmpty() || ingredient.isEmpty()) {
         return Optional.empty();
      }
      DistilledEssenceComponent component = ingredient.get(DistillationComponents.DISTILLED_ESSENCE);
      if (component == null) {
         return Optional.empty();
      }
      Optional<DistillationRules.Orientation> orientation = DistillationRules.orientation(component.orientation()).filter(DistillationRules.Orientation::implemented);
      if (orientation.isEmpty()) {
         return Optional.empty();
      }
      PotionContents contents = potionStack.get(DataComponents.POTION_CONTENTS);
      if (contents == null) {
         return Optional.empty();
      }
      Optional<String> path = AdvancedPotionStackInitializer.potionPath(contents);
      if (path.isEmpty() || !path.get().equals(component.sourceEssence())) {
         return Optional.empty();
      }
      return targetPotion(component.sourceEssence(), orientation.get().id());
   }

   private static Optional<Holder<Potion>> targetPotion(String sourceEssence, String orientation) {
      String targetPath = switch (orientation) {
         case "purified" -> sourceEssence + "_clarified";
         case "sustained" -> sourceEssence + "_long";
         case "intense" -> "wither_rose".equals(sourceEssence) ? "wither_venom_iii" : sourceEssence + "_strong";
         default -> "";
      };
      if (targetPath.isBlank()) {
         return Optional.empty();
      }
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", targetPath);
      return BuiltInRegistries.POTION.get(ResourceKey.create(Registries.POTION, id)).map(holder -> (Holder<Potion>)holder);
   }
}
