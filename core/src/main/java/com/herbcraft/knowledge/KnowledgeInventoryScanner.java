package com.herbcraft.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class KnowledgeInventoryScanner {
   private static final int SCAN_INTERVAL = 20;
   private static final List<BiConsumer<ServerPlayer, ItemStack>> HANDLERS = new ArrayList<>();

   private KnowledgeInventoryScanner() {
   }

   public static synchronized void registerHandler(BiConsumer<ServerPlayer, ItemStack> handler) {
      HANDLERS.add(handler);
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         if (server.getTickCount() % 20 == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
               scan(player);
            }
         }
      });
   }

   private static void scan(ServerPlayer player) {
      Inventory inventory = player.getInventory();
      int size = inventory.getContainerSize();

      for (int slot = 0; slot < size; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (!stack.isEmpty()) {
            for (BiConsumer<ServerPlayer, ItemStack> handler : HANDLERS) {
               handler.accept(player, stack);
            }
         }
      }
   }
}
