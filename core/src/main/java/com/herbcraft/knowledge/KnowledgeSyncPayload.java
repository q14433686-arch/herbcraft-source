package com.herbcraft.knowledge;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record KnowledgeSyncPayload(Map<String, Integer> states) implements CustomPacketPayload {
   public static final Type<KnowledgeSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("herbcraft", "knowledge_sync"));
   public static final StreamCodec<RegistryFriendlyByteBuf, KnowledgeSyncPayload> STREAM_CODEC = CustomPacketPayload.codec(
      KnowledgeSyncPayload::write,
      KnowledgeSyncPayload::read
   );

   @Override
   public Type<KnowledgeSyncPayload> type() {
      return TYPE;
   }

   private void write(RegistryFriendlyByteBuf buf) {
      buf.writeVarInt(this.states.size());
      this.states.forEach((key, state) -> {
         buf.writeUtf(key);
         buf.writeVarInt(state);
      });
   }

   private static KnowledgeSyncPayload read(RegistryFriendlyByteBuf buf) {
      int size = buf.readVarInt();
      Map<String, Integer> states = new LinkedHashMap<>();
      for (int i = 0; i < size; i++) {
         states.put(buf.readUtf(), buf.readVarInt());
      }
      return new KnowledgeSyncPayload(states);
   }
}
