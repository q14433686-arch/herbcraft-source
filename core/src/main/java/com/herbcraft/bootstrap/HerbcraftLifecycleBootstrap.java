package com.herbcraft.bootstrap;

import com.herbcraft.PlayerStateSync;
import com.herbcraft.data.HerbcraftDataSyncManager;
import com.herbcraft.knowledge.KnowledgeSyncManager;
import com.herbcraft.nutrition.NutritionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/** Join/respawn/tick lifecycle registration only. */
public final class HerbcraftLifecycleBootstrap {
   private HerbcraftLifecycleBootstrap() {
   }

   public static void initialize() {
      KnowledgeSyncManager.register();
      HerbcraftDataSyncManager.register();
      PlayerStateSync.register();
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> NutritionManager.onLogin(handler.getPlayer()));
      ServerTickEvents.END_LEVEL_TICK.register(level -> {
         if (!level.isClientSide()) {
            long gameTime = level.getGameTime();
            for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
               if (player.level() == level) {
                  NutritionManager.tick(player, gameTime);
               }
            }
         }
      });
   }
}
