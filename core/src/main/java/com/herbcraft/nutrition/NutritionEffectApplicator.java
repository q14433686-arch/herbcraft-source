package com.herbcraft.nutrition;

import com.herbcraft.effect.HerbcraftMobEffects;
import com.herbcraft.effect.NutritionEffectRules;
import com.herbcraft.logic.EffectImmunityManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Applies/removes nutrition-related effects and abundant attribute modifiers. */
public final class NutritionEffectApplicator {
   private static final Identifier FLESH_ATTACK_ID = Identifier.fromNamespaceAndPath("herbcraft", "flesh_abundant_attack");
   private static final Identifier FRUIT_SPEED_ID = Identifier.fromNamespaceAndPath("herbcraft", "fruit_abundant_speed");

   private NutritionEffectApplicator() {
   }

   public static void applyDimensionEffects(ServerPlayer player, String dim, int value, NutritionRules rules) {
      if (value < rules.malnutritionLow) {
         int sustainedTicks = sustainedEffectTicks(rules);
         switch (dim) {
            case "grain" -> addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.GRAIN_DEFICIENCY, sustainedTicks, 0));
            case "flesh" -> addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.FLESH_DEFICIENCY, sustainedTicks, 0));
            case "oil"   -> addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.OIL_DEFICIENCY, sustainedTicks, 0));
            case "vege"  -> addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.VEGE_DEFICIENCY, sustainedTicks, 0));
            case "fruit" -> addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.FRUIT_DEFICIENCY, sustainedTicks, 0));
         }
         removeAbundantModifier(player, dim);
         return;
      }

      if (value < rules.balancedMin) {
         int warningSpan = Math.max(1, rules.balancedMin - rules.malnutritionLow);
         float gradient = 1.0F - (float)(value - rules.malnutritionLow) / (float)warningSpan;
         float chance = 0.05F + gradient * 0.13F;
         switch (dim) {
            case "grain" -> { if (roll(player, chance)) addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.GRAIN_DEFICIENCY, NutritionEffectRules.current().timing.warningDebuffTicks(), 0)); }
            case "flesh" -> { if (roll(player, chance)) addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.FLESH_DEFICIENCY, NutritionEffectRules.current().timing.warningDebuffTicks(), 0)); }
            case "oil"   -> {}
            case "vege"  -> { if (roll(player, chance * 0.7F)) addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.VEGE_DEFICIENCY, NutritionEffectRules.current().timing.warningDebuffTicks(), 0)); }
            case "fruit" -> { if (roll(player, chance * 0.7F)) addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.FRUIT_DEFICIENCY, NutritionEffectRules.current().timing.warningDebuffTicks(), 0)); }
         }
         removeAbundantModifier(player, dim);
         return;
      }

      if (value < rules.balancedMax) {
         removeAbundantModifier(player, dim);
         return;
      }

      if (value < rules.imbalanceHigh) {
         applyAbundantModifier(player, dim);
         return;
      }

      applyAbundantModifier(player, dim);
      switch (dim) {
         case "flesh" -> { if (roll(player, 0.10F)) addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.DIETARY_IMBALANCE, NutritionEffectRules.current().timing.excessDebuffTicks(), 0)); }
         case "oil"   -> { if (roll(player, 0.08F)) addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.DIETARY_IMBALANCE, NutritionEffectRules.current().timing.excessDebuffTicks(), 0)); }
         case "fruit" -> { if (!rules.fruitHighNonPunitive && roll(player, 0.10F)) addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.DIETARY_IMBALANCE, NutritionEffectRules.current().timing.excessDebuffTicks(), 0)); }
         default -> {}
      }
   }

   public static void applyBalancedModifier(ServerPlayer player, boolean balanced) {
      if (balanced) {
         addNutritionEffect(player, new MobEffectInstance(HerbcraftMobEffects.WELL_NOURISHED, NutritionEffectRules.current().timing.wellNourishedTicks(), 0, true, true, true));
      }
   }

   public static void removeAllModifiers(ServerPlayer player) {
      AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
      if (attack != null) attack.removeModifier(FLESH_ATTACK_ID);
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) speed.removeModifier(FRUIT_SPEED_ID);
   }

   public static void clearNutritionEffects(ServerPlayer player) {
      java.util.List<Holder<MobEffect>> active = player.getActiveEffects()
         .stream()
         .map(MobEffectInstance::getEffect)
         .filter(HerbcraftMobEffects::isNutritionEffect)
         .toList();
      active.forEach(player::removeEffect);
   }

   public static int sustainedEffectTicks(NutritionRules rules) {
      NutritionEffectRules.Timing timing = NutritionEffectRules.current().timing;
      return Math.max(timing.sustainedMinTicks(), rules.checkIntervalTicks + timing.sustainedExtraTicks());
   }

   public static void addNutritionEffect(ServerPlayer player, MobEffectInstance effect) {
      EffectImmunityManager.runBypassingImmunity(() -> player.addEffect(effect));
   }

   private static void applyAbundantModifier(ServerPlayer player, String dim) {
      switch (dim) {
         case "flesh" -> {
            AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attack != null && !attack.hasModifier(FLESH_ATTACK_ID)) {
               attack.addTransientModifier(new AttributeModifier(FLESH_ATTACK_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
            }
         }
         case "fruit" -> {
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && !speed.hasModifier(FRUIT_SPEED_ID)) {
               speed.addTransientModifier(new AttributeModifier(FRUIT_SPEED_ID, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
         }
         default -> {}
      }
   }

   private static void removeAbundantModifier(ServerPlayer player, String dim) {
      switch (dim) {
         case "flesh" -> {
            AttributeInstance a = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (a != null) a.removeModifier(FLESH_ATTACK_ID);
         }
         case "fruit" -> {
            AttributeInstance s = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (s != null) s.removeModifier(FRUIT_SPEED_ID);
         }
         default -> {}
      }
   }

   private static boolean roll(ServerPlayer player, float chance) {
      return player.getRandom().nextFloat() < chance;
   }
}
