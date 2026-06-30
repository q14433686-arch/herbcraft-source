package com.herbcraft.logic;

import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.advancement.HerbcraftAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class HerbStackManager {
   private HerbStackManager() {
   }

   public static int updateAndGet(Player player, long gameTime, boolean stackGroup) {
      HerbcraftPlayerData data = (HerbcraftPlayerData)player;
      int herbStack = stackGroup ? data.herbcraft$addHerbStack(gameTime) : data.herbcraft$getHerbStack(gameTime);
      if (stackGroup && player instanceof ServerPlayer serverPlayer) {
         if (herbStack == 3 || herbStack == 5 || herbStack == 8) {
            HerbcraftAdvancements.grant(serverPlayer, "herb_stack_" + herbStack);
         }
      }
      return herbStack;
   }

   public static int decrement(Player player, long gameTime) {
      return ((HerbcraftPlayerData)player).herbcraft$decrementHerbStack(gameTime);
   }

   public static void overloadIfNeeded(Player player, int herbStack) {
      if (herbStack >= 10) {
         player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.herbcraft.herb_stack.overload"));
         player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.herbcraft.herb_stack.overload"));
         if (player instanceof ServerPlayer serverPlayer) {
            HerbcraftAdvancements.grant(serverPlayer, "herb_overload");
         }
         EffectResolver.addOverloadEffects(player);
         ((HerbcraftPlayerData)player).herbcraft$clearHerbStack();
      }
   }
}
