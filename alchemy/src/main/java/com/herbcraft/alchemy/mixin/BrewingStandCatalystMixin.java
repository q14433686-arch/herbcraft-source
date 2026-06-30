package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.distillation.DistilledEssenceBrewing;
import com.herbcraft.alchemy.potion.HiddenCuisineCatalystBrewing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandCatalystMixin {
   @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
   private void herbcraft_alchemy$allowHiddenCuisineCatalystInAutomation(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
      if ((slot >= 0 && slot < 3 && DistilledEssenceBrewing.isDistillationInput(stack))
         || (slot == 3 && (HiddenCuisineCatalystBrewing.isCatalystItem(stack) || DistilledEssenceBrewing.isCatalystItem(stack)))) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
   private static void herbcraft_alchemy$allowHiddenCuisineCatalystBrew(PotionBrewing brewing, NonNullList<ItemStack> items, CallbackInfoReturnable<Boolean> cir) {
      if (HiddenCuisineCatalystBrewing.isBrewable(items) || DistilledEssenceBrewing.isBrewable(items)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
   private static void herbcraft_alchemy$brewHiddenCuisineCatalyst(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
      if (HiddenCuisineCatalystBrewing.isBrewable(items)) {
         HiddenCuisineCatalystBrewing.brew(items);
         level.levelEvent(1035, pos, 0);
         ci.cancel();
      } else if (DistilledEssenceBrewing.isBrewable(items)) {
         DistilledEssenceBrewing.brew(items);
         level.levelEvent(1035, pos, 0);
         ci.cancel();
      }
   }
}
