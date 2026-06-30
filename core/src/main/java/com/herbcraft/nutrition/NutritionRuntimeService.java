package com.herbcraft.nutrition;

import com.herbcraft.effect.HerbcraftMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Runtime ticking, evaluation, relief timing, and client sync orchestration for nutrition. */
public final class NutritionRuntimeService {
   private static final Map<UUID, Long> DIETARY_IMBALANCE_RELIEF_UNTIL = new HashMap<>();

   private NutritionRuntimeService() {
   }

   public static void tick(ServerPlayer player, long gameTime) {
      cleanupReliefMap(gameTime);
      PlayerNutritionState state = NutritionManager.nutritionState(player);
      boolean decayed = state.tickDecay(gameTime);
      boolean correctionDecayed = state.tickCorrectionDecay(gameTime);
      if (state.shouldCheck(gameTime) || correctionDecayed) {
         evaluateAndApply(player, state);
      }
      if (decayed || correctionDecayed) {
         NutritionManager.syncToClient(player);
      }
   }

   public static void onLogin(ServerPlayer player) {
      evaluateAndApply(player, NutritionManager.nutritionState(player));
      NutritionManager.syncToClient(player);
   }

   public static void onDeath(ServerPlayer player) {
      NutritionManager.nutritionState(player).resetOnDeath();
      NutritionEffectApplicator.removeAllModifiers(player);
   }

   public static void relieveDietaryImbalance(ServerPlayer player) {
      long now = player.level().getGameTime();
      DIETARY_IMBALANCE_RELIEF_UNTIL.put(player.getUUID(), now + com.herbcraft.effect.NutritionEffectRules.current().timing.dietaryImbalanceReliefTicks());
      player.removeEffect(HerbcraftMobEffects.DIETARY_IMBALANCE);
   }

   public static void clearDietaryImbalanceRelief(ServerPlayer player) {
      DIETARY_IMBALANCE_RELIEF_UNTIL.remove(player.getUUID());
   }

   public static void evaluateAndApply(ServerPlayer player, PlayerNutritionState state) {
      NutritionRules rules = NutritionRules.current();
      long gameTime = player.level().getGameTime();
      int sustainedTicks = NutritionEffectApplicator.sustainedEffectTicks(rules);
      boolean correcting = state.isCorrectionActive(rules, gameTime);

      NutritionEffectApplicator.clearNutritionEffects(player);

      int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
      boolean allCritical = true;
      for (String dim : NutritionProfile.DIMENSIONS) {
         int v = state.get(dim);
         if (v < min) min = v;
         if (v > max) max = v;
         if (v >= rules.malnutritionLow) allCritical = false;
      }

      if (allCritical) {
         NutritionEffectApplicator.addNutritionEffect(player, new net.minecraft.world.effect.MobEffectInstance(HerbcraftMobEffects.GRAIN_DEFICIENCY, sustainedTicks, 0));
         NutritionEffectApplicator.addNutritionEffect(player, new net.minecraft.world.effect.MobEffectInstance(HerbcraftMobEffects.FLESH_DEFICIENCY, sustainedTicks, 0));
         NutritionEffectApplicator.addNutritionEffect(player, new net.minecraft.world.effect.MobEffectInstance(HerbcraftMobEffects.OIL_DEFICIENCY, sustainedTicks, 0));
         NutritionEffectApplicator.addNutritionEffect(player, new net.minecraft.world.effect.MobEffectInstance(HerbcraftMobEffects.VEGE_DEFICIENCY, sustainedTicks, 0));
         NutritionEffectApplicator.addNutritionEffect(player, new net.minecraft.world.effect.MobEffectInstance(HerbcraftMobEffects.FRUIT_DEFICIENCY, sustainedTicks, 0));
         if (state.canWarn(gameTime)) {
            player.sendSystemMessage(Component.translatable("nutrition.herbcraft.state.starved").withStyle(ChatFormatting.RED));
         }
         NutritionEffectApplicator.removeAllModifiers(player);
         NutritionManager.syncToClient(player);
         return;
      }

      for (String dim : NutritionProfile.DIMENSIONS) {
         int value = state.get(dim);
         NutritionEffectApplicator.applyDimensionEffects(player, dim, value, rules);
      }

      if (state.hasPunitiveImbalance(rules)) {
         if (correcting) {
            state.clearBadStreak();
            player.removeEffect(HerbcraftMobEffects.DIETARY_IMBALANCE);
            if (state.canWarn(gameTime)) {
               player.sendSystemMessage(Component.translatable("nutrition.herbcraft.state.correcting").withStyle(ChatFormatting.GREEN));
            }
         } else {
            state.incrementBadStreak();
            if (!isDietaryImbalanceRelieved(player) && state.shouldApplyDebuff()) {
               NutritionEffectApplicator.addNutritionEffect(player, new net.minecraft.world.effect.MobEffectInstance(HerbcraftMobEffects.DIETARY_IMBALANCE, sustainedTicks, 0));
            }
            if (state.canWarn(gameTime)) {
               player.sendSystemMessage(Component.translatable("nutrition.herbcraft.state.imbalanced").withStyle(ChatFormatting.GOLD));
            }
         }
      } else {
         state.clearBadStreak();
      }

      boolean balanced = true;
      for (String dim : NutritionProfile.DIMENSIONS) {
         int v = state.get(dim);
         if (v < rules.fullBalanceMin || v > rules.fullBalanceMax) {
            balanced = false;
            break;
         }
      }
      NutritionEffectApplicator.applyBalancedModifier(player, balanced);
      NutritionManager.syncToClient(player);
   }

   private static boolean isDietaryImbalanceRelieved(ServerPlayer player) {
      long now = player.level().getGameTime();
      Long until = DIETARY_IMBALANCE_RELIEF_UNTIL.get(player.getUUID());
      if (until == null) {
         return false;
      }
      if (until <= now) {
         DIETARY_IMBALANCE_RELIEF_UNTIL.remove(player.getUUID());
         return false;
      }
      return true;
   }

   private static void cleanupReliefMap(long gameTime) {
      Iterator<Map.Entry<UUID, Long>> iterator = DIETARY_IMBALANCE_RELIEF_UNTIL.entrySet().iterator();
      while (iterator.hasNext()) {
         if (iterator.next().getValue() <= gameTime) {
            iterator.remove();
         }
      }
   }
}
