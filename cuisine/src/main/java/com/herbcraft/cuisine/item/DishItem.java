package com.herbcraft.cuisine.item;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.cuisine.registry.DishRegistry;
import com.herbcraft.effect.HerbcraftMobEffects;
import com.herbcraft.logic.EffectResolver;
import com.herbcraft.guide.GuideHintManager;
import com.herbcraft.logic.LastMealTracker;
import com.herbcraft.logic.SpecialCounters;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class DishItem extends Item {
   private final List<Item> counterItems;
   private final boolean clearFire;
   private final boolean clearFrozen;
   private final boolean clearAllEffects;
   private final int igniteSeconds;
   private final List<Holder<MobEffect>> clears;
   private final List<DishRegistry.EffectImmunity> immunities;
   private final List<DishRegistry.DishEffect> effects;
   private final List<DishRegistry.DamageEvent> damageEvents;

   public DishItem(
      Properties properties,
      List<Item> counterItems,
      boolean clearFire,
      boolean clearFrozen,
      boolean clearAllEffects,
      int igniteSeconds,
      List<Holder<MobEffect>> clears,
      List<DishRegistry.EffectImmunity> immunities,
      List<DishRegistry.DishEffect> effects,
      List<DishRegistry.DamageEvent> damageEvents
   ) {
      super(properties);
      this.counterItems = counterItems;
      this.clearFire = clearFire;
      this.clearFrozen = clearFrozen;
      this.clearAllEffects = clearAllEffects;
      this.igniteSeconds = igniteSeconds;
      this.clears = clears;
      this.immunities = immunities;
      this.effects = effects;
      this.damageEvents = damageEvents;
   }

   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
      if (!level.isClientSide()) {
         if (this.clearFire) {
            entity.setRemainingFireTicks(0);
         }

         if (this.clearFrozen) {
            entity.setTicksFrozen(0);
         }

         if (this.igniteSeconds > 0) {
            entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), this.igniteSeconds * 20));
         }

         if (entity instanceof Player player) {
            long gameTime = level.getGameTime();
            this.counterItems.forEach(item -> SpecialCounters.apply(player, item, gameTime));
            if (this.clearAllEffects) {
               List<Holder<MobEffect>> active = player.getActiveEffects()
                  .stream()
                  .map(MobEffectInstance::getEffect)
                  .filter(effect -> !HerbcraftMobEffects.isNutritionEffect(effect))
                  .toList();
               active.forEach(player::removeEffect);
            }
            this.clears.forEach(player::removeEffect);
            if (player instanceof ServerPlayer serverPlayer) {
               this.immunities.forEach(immunity -> HerbcraftAPI.grantEffectImmunity(serverPlayer, immunity.effectId(), immunity.durationSeconds() * 20));
            }
            this.effects.forEach(effect -> {
               if (EffectResolver.roll(player, effect.chance())) {
                  player.addEffect(new MobEffectInstance(effect.effect(), effect.durationSeconds() * 20, effect.amplifier()));
               }
            });
            if (level instanceof ServerLevel serverLevel) {
               this.damageEvents.forEach(event -> {
                  if (EffectResolver.roll(player, event.chance())) {
                     player.hurtServer(serverLevel, level.damageSources().generic(), event.amount());
                  }
               });
            }
         }

         if (entity instanceof ServerPlayer serverPlayer) {
            awardCuisineAdvancements(serverPlayer);
            GuideHintManager.show(serverPlayer, "herbcraft_cuisine:first_dish");
            LastMealTracker.record(serverPlayer, this);
         }
      }

      return super.finishUsingItem(stack, level, entity);
   }

   private void awardCuisineAdvancements(ServerPlayer player) {
      HerbcraftAdvancements.grant(player, "herbcraft_cuisine", "first_dish");
      if (this.clearAllEffects || !this.clears.isEmpty() || !this.immunities.isEmpty()) {
         HerbcraftAdvancements.grant(player, "herbcraft_cuisine", "immunity_meal");
      }
      Identifier id = BuiltInRegistries.ITEM.getKey(this);
      if (id != null && "herbcraft_cuisine".equals(id.getNamespace())) {
         if ("hundred_flower_feast".equals(id.getPath())) HerbcraftAdvancements.grant(player, "herbcraft_cuisine", "hundred_flower_feast");
         if ("deadly_hodgepodge".equals(id.getPath())) HerbcraftAdvancements.grant(player, "herbcraft_cuisine", "deadly_hodgepodge");
      }
   }
}
