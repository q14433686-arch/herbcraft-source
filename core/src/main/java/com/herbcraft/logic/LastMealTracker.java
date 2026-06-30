package com.herbcraft.logic;

import com.herbcraft.api.HerbConsumeEvents;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public final class LastMealTracker {
   private static final long WINDOW_TICKS = 60L;
   private static final Map<UUID, LastMealTracker.LastMeal> LAST_MEALS = new HashMap<>();

   private LastMealTracker() {
   }

   public static void register() {
      HerbConsumeEvents.register(LastMealTracker::record);
      ServerLivingEntityEvents.AFTER_DEATH.register(LastMealTracker::onDeath);
   }

   public static void record(ServerPlayer player, Item item) {
      LAST_MEALS.put(player.getUUID(), new LastMealTracker.LastMeal(Component.translatable(item.getDescriptionId()), player.level().getGameTime()));
   }

   private static void onDeath(LivingEntity entity, DamageSource source) {
      if (entity instanceof ServerPlayer player) {
         LastMealTracker.LastMeal meal = LAST_MEALS.remove(player.getUUID());
         if (meal != null && player.level().getGameTime() - meal.gameTime() <= 60L) {
            Component message = Component.translatable("message.herbcraft.last_meal", new Object[]{meal.itemName()}).withStyle(ChatFormatting.GRAY);
            player.level().getServer().getPlayerList().broadcastSystemMessage(message, false);
         }
      }
   }

   private record LastMeal(Component itemName, long gameTime) {
   }
}
