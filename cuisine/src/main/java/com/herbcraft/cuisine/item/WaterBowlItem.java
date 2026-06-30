package com.herbcraft.cuisine.item;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.cuisine.registry.CuisineItems;
import com.herbcraft.logic.LastMealTracker;
import com.herbcraft.guide.GuideHintManager;
import com.herbcraft.nutrition.NutritionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class WaterBowlItem extends Item {
   private static final float CLEAR_NAUSEA_CHANCE = 0.15F;

   public WaterBowlItem(Properties properties) {
      super(properties);
   }

   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
      if (!level.isClientSide()) {
         entity.setRemainingFireTicks(0);
         if (entity instanceof Player player && player.getRandom().nextFloat() < 0.15F) {
            player.removeEffect(MobEffects.NAUSEA);
            if (player instanceof ServerPlayer serverPlayer) {
               NutritionManager.relieveDietaryImbalance(serverPlayer);
            }
         }

         if (entity instanceof ServerPlayer serverPlayer) {
            HerbcraftAdvancements.grant(serverPlayer, "herbcraft_cuisine", "water_bowl");
            GuideHintManager.show(serverPlayer, "herbcraft_cuisine:first_water_bowl");
            LastMealTracker.record(serverPlayer, CuisineItems.WATER_BOWL);
         }
      }

      return super.finishUsingItem(stack, level, entity);
   }
}
