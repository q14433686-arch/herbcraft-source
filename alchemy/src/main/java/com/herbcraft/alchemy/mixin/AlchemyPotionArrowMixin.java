package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.knowledge.AlchemyKnowledgeSupport;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.PotionBurdenRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Arrow.class)
public abstract class AlchemyPotionArrowMixin {
   @Inject(method = "doPostHurtEffects", at = @At("HEAD"))
   private void herbcraftAlchemy$initializePotionArrowBeforeHit(LivingEntity target, CallbackInfo ci) {
      if (!target.level().isClientSide()) {
         ItemStack stack = ((Arrow)(Object)this).getPickupItemStackOrigin();
         if (!stack.isEmpty()) {
            AdvancedPotionStackInitializer.initializeIfNeeded(stack, target.getRandom());
         }
      }
   }

   @Inject(method = "doPostHurtEffects", at = @At("RETURN"))
   private void herbcraftAlchemy$applyKnowledgeAndBurdenFromPotionArrow(LivingEntity target, CallbackInfo ci) {
      if (target.level().isClientSide() || !(target instanceof ServerPlayer player)) {
         return;
      }
      ItemStack stack = ((Arrow)(Object)this).getPickupItemStackOrigin();
      if (stack.isEmpty()) {
         return;
      }
      AlchemyKnowledgeSupport.markPotionTasted(player, stack);
      PotionBurdenRuntime.onPotionConsumed(player, stack);
   }
}
