package com.herbcraft.knowledge;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.guide.GuideHintManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class CodexBookItem extends Item {
   public CodexBookItem(Properties properties) {
      super(properties);
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (player instanceof ServerPlayer serverPlayer) {
         HerbcraftAdvancements.grant(serverPlayer, "open_codex");
         GuideHintManager.show(serverPlayer, "herbcraft:first_codex_open");
         ServerPlayNetworking.send(serverPlayer, CodexSnapshotPayload.build(serverPlayer));
         ServerPlayNetworking.send(serverPlayer, OpenCodexPayload.open());
      }

      return InteractionResult.SUCCESS;
   }
}
