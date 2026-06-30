package com.herbcraft.api;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

/**
 * Legacy herb-consumption event facade.
 *
 * <p>New addons should prefer {@link HerbcraftEvents#HERB_CONSUMED}. This
 * class remains as a compatibility shim for older internal/addon call sites.</p>
 */
public final class HerbConsumeEvents {
   private HerbConsumeEvents() {
   }

   public static void register(BiConsumer<ServerPlayer, Item> listener) {
      HerbcraftEvents.HERB_CONSUMED.register(listener::accept);
   }

   public static void fire(ServerPlayer player, Item item) {
      HerbcraftEvents.HERB_CONSUMED.invoker().onHerbConsumed(player, item);
   }
}
