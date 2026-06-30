package com.herbcraft.mixin;

import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.logic.EffectImmunityManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
   private void herbcraft$blockImmuneEffects(MobEffectInstance effectInstance, Entity source, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity self = (LivingEntity)(Object)this;
      if (EffectImmunityManager.isBypassingImmunity()) {
         return;
      }
      if (self instanceof ServerPlayer player) {
         Identifier effectId = BuiltInRegistries.MOB_EFFECT.getKey(effectInstance.getEffect().value());
         if (effectId != null && HerbcraftAPI.isImmuneToEffect(player, effectId)) {
            cir.setReturnValue(false);
         }
      }
   }
}
