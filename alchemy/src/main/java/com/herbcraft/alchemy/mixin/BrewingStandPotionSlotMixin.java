package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.distillation.DistilledEssenceBrewing;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class BrewingStandPotionSlotMixin {
   // MC 26.3: PotionSlot.mayPlaceItem(ItemStack) was removed (slot now carries a RecipeAccess).
   // The instance mayPlace(ItemStack) below is the only acceptance check left, so it is the one to hook.
   @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
   private void herbcraft_alchemy$allowDistillationInputs(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
      if (DistilledEssenceBrewing.isDistillationInput(stack)) {
         cir.setReturnValue(true);
      }
   }
}
