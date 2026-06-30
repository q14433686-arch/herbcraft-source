package com.herbcraft.alchemy.potion;

import com.herbcraft.alchemy.AlchemyExpansionEvents;
import com.herbcraft.alchemy.effect.AlchemyMobEffects;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/** Conservative P7 burden-stage consequence layer using Alchemy custom effects. */
public final class PotionBurdenConsequenceRuntime {
   private PotionBurdenConsequenceRuntime() {
   }

   public static void apply(ServerPlayer player, PlayerPotionBurdenState state) {
      PotionProcessingRules.BurdenStage stage = PotionProcessingRules.burdenStageFor(state.totalBurden());
      PotionProcessingRules.BurdenConsequenceProfile profile = PotionProcessingRules.burdenConsequenceProfile(state.dominantProfile());
      applyStageEffect(player, state, stage, profile);
      applyProfileExhaustion(player, profile);
      if (!stage.id().equals(state.lastStageId())) {
         String oldStage = state.lastStageId();
         state.setLastStageId(stage.id());
         if (!stage.messageKey().isBlank()) {
            player.sendSystemMessage(Component.translatable(stage.messageKey()).withStyle(stageStyle(stage)));
         }
         if (!profile.messageKey().isBlank() && !"steady".equals(profile.id()) && !"steady".equals(stage.id())) {
            player.sendSystemMessage(Component.translatable(profile.messageKey()).withStyle(profileStyle(profile)));
         }
         AlchemyExpansionEvents.POTION_BURDEN_STAGE_CHANGED.invoker().onPotionBurdenStageChanged(player, oldStage, stage.id(), state.totalBurden());
      }
   }

   public static void clear(ServerPlayer player) {
      clearStageEffects(player);
   }

   private static void applyStageEffect(ServerPlayer player, PlayerPotionBurdenState state, PotionProcessingRules.BurdenStage stage, PotionProcessingRules.BurdenConsequenceProfile profile) {
      clearStageEffects(player);
      if ("steady".equals(stage.id())) {
         return;
      }
      String effectId = profile.effect() == null || profile.effect().isBlank() ? stage.effect() : profile.effect();
      if (effectId == null || effectId.isBlank()) {
         return;
      }
      float stageMultiplier = switch (stage.id()) {
         case "overload" -> profile.overloadDurationMultiplier();
         case "strain" -> profile.strainDurationMultiplier();
         default -> 0.75F;
      };
      int duration = Math.max(20, Math.round(stage.effectDurationTicks() * Math.max(0.1F, stageMultiplier) * state.consequenceDurationMultiplier()));
      BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId)).ifPresent(effect ->
         player.addEffect(new MobEffectInstance((Holder<MobEffect>)effect, duration, 0, false, true, true))
      );
   }

   private static void applyProfileExhaustion(ServerPlayer player, PotionProcessingRules.BurdenConsequenceProfile profile) {
      if (profile.exhaustionPerTick() > 0.0F) {
         player.causeFoodExhaustion(profile.exhaustionPerTick());
      }
   }

   private static void clearStageEffects(ServerPlayer player) {
      List<Holder<MobEffect>> active = player.getActiveEffects()
         .stream()
         .map(MobEffectInstance::getEffect)
         .filter(AlchemyMobEffects::isBurdenStageEffect)
         .toList();
      active.forEach(player::removeEffect);
   }

   private static ChatFormatting stageStyle(PotionProcessingRules.BurdenStage stage) {
      return switch (stage.id()) {
         case "overload" -> ChatFormatting.RED;
         case "strain" -> ChatFormatting.GOLD;
         case "warming" -> ChatFormatting.YELLOW;
         default -> ChatFormatting.GRAY;
      };
   }

   private static ChatFormatting profileStyle(PotionProcessingRules.BurdenConsequenceProfile profile) {
      return switch (profile.id()) {
         case "muddy" -> ChatFormatting.DARK_GREEN;
         case "overdriven" -> ChatFormatting.GOLD;
         case "warming" -> ChatFormatting.RED;
         case "chilling" -> ChatFormatting.AQUA;
         case "scattering" -> ChatFormatting.YELLOW;
         case "stagnating" -> ChatFormatting.DARK_PURPLE;
         default -> ChatFormatting.GRAY;
      };
   }
}
