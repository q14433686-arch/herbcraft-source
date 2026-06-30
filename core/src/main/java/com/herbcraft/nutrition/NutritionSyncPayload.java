package com.herbcraft.nutrition;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record NutritionSyncPayload(Map<String, Integer> values, int statusOrdinal) implements CustomPacketPayload {
   public static final Type<NutritionSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("herbcraft", "nutrition_sync"));
   public static final StreamCodec<RegistryFriendlyByteBuf, NutritionSyncPayload> STREAM_CODEC =
      CustomPacketPayload.codec(NutritionSyncPayload::write, NutritionSyncPayload::read);

   public Type<NutritionSyncPayload> type() { return TYPE; }

   public static NutritionSyncPayload build(ServerPlayer player) {
      PlayerNutritionState state = NutritionManager.nutritionState(player);
      int statusCode = computeClientStatus(state, player.level().getGameTime());
      return new NutritionSyncPayload(state.snapshot(), statusCode);
   }

   /** Simplified status for client display: 0=starved, 1=malnourished, 2=imbalanced, 3=normal, 4=abundant, 5=balanced, 6=correcting */
   private static int computeClientStatus(PlayerNutritionState state, long gameTime) {
      NutritionRules rules = NutritionRules.current();
      int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
      boolean allCrit = true;
      for (String dim : NutritionProfile.DIMENSIONS) {
         int v = state.get(dim);
         if (v < min) min = v;
         if (v > max) max = v;
         if (v >= rules.malnutritionLow) allCrit = false;
      }
      if (allCrit) return 0; // starved
      if (min < rules.malnutritionLow) return 1; // malnourished
      if (state.isCorrectionActive(rules, gameTime)) return 6; // correcting
      if (state.hasPunitiveImbalance(rules)) return 2; // imbalanced
      boolean balanced = true;
      for (String dim : NutritionProfile.DIMENSIONS) {
         int v = state.get(dim);
         if (v < rules.fullBalanceMin || v > rules.fullBalanceMax) { balanced = false; break; }
      }
      if (balanced) return 5; // balanced
      if (max > rules.abundanceThreshold) return 4; // abundant
      return 3; // normal
   }

   private void write(RegistryFriendlyByteBuf buf) {
      for (String dim : NutritionProfile.DIMENSIONS) {
         buf.writeVarInt(values.getOrDefault(dim, 0));
      }
      buf.writeVarInt(statusOrdinal);
   }

   private static NutritionSyncPayload read(RegistryFriendlyByteBuf buf) {
      Map<String, Integer> values = new LinkedHashMap<>();
      for (String dim : NutritionProfile.DIMENSIONS) {
         values.put(dim, buf.readVarInt());
      }
      int statusOrdinal = buf.readVarInt();
      return new NutritionSyncPayload(values, statusOrdinal);
   }
}
