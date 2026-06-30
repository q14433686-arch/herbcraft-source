package com.herbcraft.bootstrap;

import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.data.HerbcraftDataSyncPayload;
import com.herbcraft.knowledge.ClaimMarkPayload;
import com.herbcraft.knowledge.CodexSnapshotPayload;
import com.herbcraft.knowledge.KnowledgeSyncPayload;
import com.herbcraft.knowledge.OpenCodexPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Packet type registration and packet handler wiring only. */
public final class HerbcraftNetworkingBootstrap {
   private HerbcraftNetworkingBootstrap() {
   }

   public static void initialize() {
      PayloadTypeRegistry.clientboundPlay().register(CodexSnapshotPayload.TYPE, CodexSnapshotPayload.STREAM_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(OpenCodexPayload.TYPE, OpenCodexPayload.STREAM_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(KnowledgeSyncPayload.TYPE, KnowledgeSyncPayload.STREAM_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(HerbcraftDataSyncPayload.TYPE, HerbcraftDataSyncPayload.STREAM_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(com.herbcraft.nutrition.NutritionSyncPayload.TYPE, com.herbcraft.nutrition.NutritionSyncPayload.STREAM_CODEC);
      PayloadTypeRegistry.serverboundPlay().register(ClaimMarkPayload.TYPE, ClaimMarkPayload.STREAM_CODEC);

      ServerPlayNetworking.registerGlobalReceiver(ClaimMarkPayload.TYPE, (payload, context) -> {
         KnowledgeAPI.cycleClaimMark(context.player(), payload.entryKey(), payload.claimId());
         ServerPlayNetworking.send(context.player(), CodexSnapshotPayload.build(context.player()));
      });
   }
}
