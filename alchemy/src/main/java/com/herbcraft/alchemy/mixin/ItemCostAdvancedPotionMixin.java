package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.trading.ItemCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCost.class)
public abstract class ItemCostAdvancedPotionMixin {
   @Inject(method = "test", at = @At("HEAD"), cancellable = true)
   private void herbcraft_alchemy$matchAdvancedPotionByPotionId(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
      ItemCost self = (ItemCost)(Object)this;
      if (stack.is(self.item())) {
         PotionContents expectedContents = (PotionContents)self.itemStack().get(DataComponents.POTION_CONTENTS);
         if (expectedContents != null) {
            Optional<String> expectedPath = AdvancedPotionStackInitializer.advancedPotionPath(expectedContents);
            if (!expectedPath.isEmpty()) {
               Optional<String> actualPath = AdvancedPotionStackInitializer.advancedPotionPath(stack);
               if (actualPath.isPresent() && actualPath.get().equals(expectedPath.get())) {
                  cir.setReturnValue(true);
               }
            }
         }
      }
   }
}
