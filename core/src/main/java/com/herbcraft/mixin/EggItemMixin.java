package com.herbcraft.mixin;

import com.herbcraft.Herbcraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EggItem.class)
public abstract class EggItemMixin {
   @Inject(method = "use", at = @At("HEAD"), cancellable = true)
   private void herbcraft$startEatingOrThrowing(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      ItemStack stack = player.getItemInHand(hand);
      if (Herbcraft.isShortUseProjectile(stack)) {
         cir.setReturnValue(Herbcraft.startShortUseProjectileEating(level, player, hand));
      }
   }
}
