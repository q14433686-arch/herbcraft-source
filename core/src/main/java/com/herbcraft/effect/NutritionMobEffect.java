package com.herbcraft.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Herbcraft nutrition status effect with optional periodic exhaustion feedback. */
public class NutritionMobEffect extends SimpleHerbcraftMobEffect {
   private final int exhaustionIntervalTicks;
   private final float exhaustionAmount;
   private final boolean scaleExhaustionWithAmplifier;

   public NutritionMobEffect(NutritionEffectRules.EffectRule rule) {
      super(rule.category(), rule.color());
      this.exhaustionIntervalTicks = Math.max(0, rule.exhaustion().intervalTicks());
      this.exhaustionAmount = Math.max(0.0F, rule.exhaustion().amount());
      this.scaleExhaustionWithAmplifier = rule.exhaustion().scaleWithAmplifier();
   }

   public NutritionMobEffect(MobEffectCategory category, int color) {
      super(category, color);
      this.exhaustionIntervalTicks = 0;
      this.exhaustionAmount = 0.0F;
      this.scaleExhaustionWithAmplifier = true;
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int durationTicks, int amplifier) {
      return this.exhaustionIntervalTicks > 0 && durationTicks % this.exhaustionIntervalTicks == 0;
   }

   @Override
   public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
      if (this.exhaustionAmount > 0.0F && entity instanceof ServerPlayer player) {
         float multiplier = this.scaleExhaustionWithAmplifier ? amplifier + 1 : 1.0F;
         player.causeFoodExhaustion(this.exhaustionAmount * multiplier);
      }
      return true;
   }
}
