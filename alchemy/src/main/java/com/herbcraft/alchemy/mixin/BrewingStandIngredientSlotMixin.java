package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.distillation.DistilledEssenceBrewing;
import com.herbcraft.alchemy.potion.HiddenCuisineCatalystBrewing;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$IngredientsSlot")
public abstract class BrewingStandIngredientSlotMixin {
   @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
   private void herbcraft_alchemy$allowCustomBrewingCatalysts(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
      if (HiddenCuisineCatalystBrewing.isCatalystItem(stack) || DistilledEssenceBrewing.isCatalystItem(stack)) {
         cir.setReturnValue(true);
      }
   }
}
