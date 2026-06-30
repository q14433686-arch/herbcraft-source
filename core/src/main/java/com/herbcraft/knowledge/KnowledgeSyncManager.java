package com.herbcraft.knowledge;

import com.herbcraft.api.KnowledgeAPI;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class KnowledgeSyncManager {
   private static final int SYNC_INTERVAL_TICKS = 20;

   private KnowledgeSyncManager() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         if (server.getTickCount() % SYNC_INTERVAL_TICKS == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
               sync(player);
            }
         }
      });
   }

   public static void sync(ServerPlayer player) {
      ServerPlayNetworking.send(player, new KnowledgeSyncPayload(buildStates(player)));
   }

   private static Map<String, Integer> buildStates(ServerPlayer player) {
      Map<String, Integer> states = new LinkedHashMap<>();
      for (KnowledgeAPI.Chapter chapter : KnowledgeAPI.chapters()) {
         for (KnowledgeAPI.Entry entry : chapter.entries()) {
            com.herbcraft.api.KnowledgeDisplayState state = KnowledgeAPI.displayState(player, chapter.id(), entry);
            states.put(chapter.id() + "/" + entry.id(), state.ordinal());
         }
      }
      return states;
   }
}
