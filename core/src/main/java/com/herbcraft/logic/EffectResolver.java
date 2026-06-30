package com.herbcraft.logic;

import com.herbcraft.api.HerbConsumeEvents;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.effect.HerbcraftMobEffects;
import com.herbcraft.pharmacology.PharmacologyResolver;
import com.herbcraft.pharmacology.PharmacologyResult;
import com.herbcraft.registry.HerbEntry;
import com.herbcraft.registry.HerbFoodRegistry;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class EffectResolver {
   private static final int SECOND = 20;

   private EffectResolver() {
   }

   public static void applyAfterConsume(ItemStack stack, Level level, LivingEntity entity) {
      if (!level.isClientSide() && entity instanceof Player player) {
         HerbEntry entry = HerbFoodRegistry.get(stack);
         if (entry != null) {
            long gameTime = level.getGameTime();
            int herbStack = HerbStackManager.updateAndGet(player, gameTime, entry.stackGroup);
            entry.removeEffects.forEach(player::removeEffect);
            if (entry.clearPositiveEffects) {
               List<Holder<MobEffect>> positiveEffects = player.getActiveEffects()
                  .stream()
                  .map(MobEffectInstance::getEffect)
                  .filter(effect -> ((MobEffect)effect.value()).isBeneficial())
                  .filter(effect -> !HerbcraftMobEffects.isNutritionEffect(effect))
                  .toList();
               positiveEffects.forEach(player::removeEffect);
            }

            if (entry.extinguish) {
               player.setRemainingFireTicks(0);
            }

            PharmacologyResult pharmacology = PharmacologyResolver.resolve(stack, player, entry, gameTime);
            applyFoodRolls(player, entry);
            if (!pharmacology.cancelAll() && player instanceof ServerPlayer serverPlayer) {
               int immunityTicks = Math.round(600 * pharmacology.immunityMultiplier());
               entry.removeEffects.forEach(effect -> com.herbcraft.api.HerbcraftAPI.grantEffectImmunity(serverPlayer, BuiltInRegistries.MOB_EFFECT.getKey((MobEffect)effect.value()), immunityTicks));
            }

            if (!pharmacology.cancelAll()) {
               entry.effects.forEach(effect -> {
                  if (applyEffect(player, effect, entry.stackGroup, herbStack, pharmacology) && player instanceof ServerPlayer serverPlayer) {
                     net.minecraft.resources.Identifier itemId = BuiltInRegistries.ITEM.getKey(entry.item);
                     net.minecraft.resources.Identifier effectId = BuiltInRegistries.MOB_EFFECT.getKey((MobEffect)effect.effect().value());
                     if (itemId != null && effectId != null) {
                        KnowledgeManager.addExperiencedEffect(serverPlayer, "herbal", itemId.getPath(), effectId.toString());
                     }
                  }
               });
               if (level instanceof ServerLevel serverLevel) {
                  entry.damageEvents.forEach(event -> {
                     if (roll(player, pharmacology.chance(event.chance()))) {
                        player.hurtServer(serverLevel, level.damageSources().magic(), event.amount() * pharmacology.damageMultiplier());
                     }
                  });
               }

               entry.healEvents.forEach(event -> {
                  if (roll(player, pharmacology.chance(event.chance()))) {
                     player.heal(event.amount() * pharmacology.healMultiplier());
                  }
               });
            }
            if (entry.bucketRemainder && !player.getAbilities().instabuild) {
               player.addItem(new ItemStack(Items.BUCKET));
            }

            SpecialCounters.apply(player, entry.item, gameTime);
            HerbStackManager.overloadIfNeeded(player, herbStack);
            if (player instanceof ServerPlayer serverPlayer) {
               net.minecraft.resources.Identifier itemId = BuiltInRegistries.ITEM.getKey(entry.item);
               if (itemId != null) {
                  KnowledgeManager.markTasted(serverPlayer, "herbal", itemId.getPath());
               }
               HerbConsumeEvents.fire(serverPlayer, entry.item);
            }
         }
      }
   }

   private static boolean applyEffect(Player player, HerbEntry.Effect effect, boolean herbGroup, int herbStack, PharmacologyResult pharmacology) {
      float probability = pharmacology.chance(effect.chance());
      int amplifier = effect.amplifier();
      int durationTicks = pharmacology.duration(effect.durationTicks(), effect.positive());
      if (effect.positive() && herbGroup) {
         probability = Math.min(probability + 0.1F * Math.max(0, herbStack - 1), 0.9F);
         if (herbStack >= 5) {
            amplifier = Math.min(amplifier + 1, 1);
         }

         amplifier += pharmacology.positiveAmplifierBonus();

         if (herbStack >= 8) {
            durationTicks = Math.round(durationTicks * 1.5F);
         }
      }

      if (roll(player, probability)) {
         return player.addEffect(new MobEffectInstance(effect.effect(), durationTicks, amplifier));
      }
      return false;
   }

   private static void applyFoodRolls(Player player, HerbEntry entry) {
      if (entry.nutritionRoll != null && roll(player, entry.nutritionRoll.chance())) {
         int amount = Math.round(entry.nutritionRoll.amount());
         if (amount > 0) {
            player.getFoodData().eat(amount, 0.0F);
         }
      }
      if (entry.saturationRoll != null && roll(player, entry.saturationRoll.chance())) {
         addDirectSaturation(player, entry.saturationRoll.amount());
      }
   }

   private static void addDirectSaturation(Player player, float amount) {
      if (amount <= 0.0F) {
         return;
      }
      FoodData foodData = player.getFoodData();
      float target = Math.max(0.0F, Math.min(foodData.getFoodLevel(), foodData.getSaturationLevel() + amount));
      foodData.setSaturation(target);
   }

   public static void addOverloadEffects(Player player) {
      player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
      player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
   }

   public static boolean roll(Player player, float probability) {
      return probability >= 1.0F || player.getRandom().nextFloat() < probability;
   }
}
