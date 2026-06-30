package com.herbcraft.logic;

import com.herbcraft.registry.HerbEntry;
import com.herbcraft.registry.HerbFoodRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class ProjectileUseHandler {
   private ProjectileUseHandler() {
   }

   public static boolean isShortUseProjectile(ItemStack stack) {
      HerbEntry entry = HerbFoodRegistry.get(stack);
      return entry != null && entry.shortUseProjectile;
   }

   public static InteractionResult start(Level level, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return InteractionResult.CONSUME;
   }

   public static boolean release(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (!isShortUseProjectile(stack)) {
         return false;
      } else {
         int usedTicks = Math.max(entity.getTicksUsingItem(), stack.getUseDuration(entity) - timeLeft);
         if (usedTicks < 6 && entity instanceof Player player) {
            throwProjectile(stack, level, player);
         }

         return true;
      }
   }

   private static void throwProjectile(ItemStack stack, Level level, Player player) {
      if (level instanceof ServerLevel serverLevel) {
         ItemStack projectileStack = stack.copyWithCount(1);
         Projectile projectile;
         if (stack.getItem() == Items.SNOWBALL) {
            projectile = new Snowball(level, player, projectileStack);
         } else {
            projectile = new ThrownEgg(level, player, projectileStack);
         }

         projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
         serverLevel.addFreshEntity(projectile);
      }

      if (!player.getAbilities().instabuild) {
         stack.consume(1, player);
      }
   }
}
