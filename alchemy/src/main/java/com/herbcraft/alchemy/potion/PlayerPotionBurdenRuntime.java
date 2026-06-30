package com.herbcraft.alchemy.potion;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Server lifecycle hooks for Alchemy-owned player potion burden state. */
public final class PlayerPotionBurdenRuntime {
   private PlayerPotionBurdenRuntime() {
   }

   public static void register() {
      ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
         if (alive) {
            ((AlchemyPlayerData)newPlayer).herbcraftAlchemy$potionBurdenState()
               .copyFrom(((AlchemyPlayerData)oldPlayer).herbcraftAlchemy$potionBurdenState(), PotionProcessingRules.burdenRuntime());
         }
      });
      ServerTickEvents.END_LEVEL_TICK.register(level -> {
         if (!level.isClientSide()) {
            long gameTime = level.getGameTime();
            for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
               if (player.level() == level) {
                  PlayerPotionBurdenState state = ((AlchemyPlayerData)player).herbcraftAlchemy$potionBurdenState();
                  state.tickDecay(gameTime, PotionProcessingRules.burdenRuntime());
                  PotionBurdenConsequenceRuntime.apply(player, state);
               }
            }
         }
      });
   }
}
