package com.herbcraft.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class HerbcraftAdvancements {
   private static final Identifier ROOT = Identifier.fromNamespaceAndPath("herbcraft", "root");
   private static final String CRITERION = "trigger";
   private HerbcraftAdvancements() {}
   public static void grant(ServerPlayer player, String path) { grant(player, "herbcraft", path); }
   public static void grant(ServerPlayer player, String namespace, String path) { grant(player, Identifier.fromNamespaceAndPath(namespace, path)); }
   public static void grant(ServerPlayer player, Identifier id) {
      if (!ROOT.equals(id)) grantOne(player, ROOT);
      grantOne(player, id);
   }
   private static void grantOne(ServerPlayer player, Identifier id) {
      MinecraftServer server = player.level().getServer();
      if (server == null) return;
      AdvancementHolder advancement = server.getAdvancements().get(id);
      if (advancement != null) player.getAdvancements().award(advancement, CRITERION);
   }
}
