package com.herbcraft.api;

import com.herbcraft.Herbcraft;
import java.util.Map;
import java.util.Optional;
import com.herbcraft.logic.EffectImmunityManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public final class HerbcraftAPI {
   private HerbcraftAPI() {
   }

   public static Map<Item, HerbDefinition> getHerbRegistry() {
      return Herbcraft.getHerbDefinitions();
   }

   public static Optional<HerbDefinition> getDefinition(Item item) {
      return Optional.ofNullable(getHerbRegistry().get(item));
   }

   public static void grantEffectImmunity(ServerPlayer player, Identifier effectId, int durationTicks) {
      EffectImmunityManager.grant(player, effectId, durationTicks);
   }

   public static boolean isImmuneToEffect(ServerPlayer player, Identifier effectId) {
      return EffectImmunityManager.isImmune(player, effectId);
   }
}
