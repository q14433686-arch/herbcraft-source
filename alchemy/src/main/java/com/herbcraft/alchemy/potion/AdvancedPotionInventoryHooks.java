package com.herbcraft.alchemy.potion;

import com.herbcraft.knowledge.KnowledgeInventoryScanner;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class AdvancedPotionInventoryHooks {
   private AdvancedPotionInventoryHooks() {
   }

   public static void register() {
      KnowledgeInventoryScanner.registerHandler(AdvancedPotionInventoryHooks::onStack);
   }

   private static void onStack(ServerPlayer player, ItemStack stack) {
      AdvancedPotionStackInitializer.initializeIfNeeded(stack, player.getRandom());
   }
}
