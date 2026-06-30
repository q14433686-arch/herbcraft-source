package com.herbcraft.knowledge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Explicit UI-open signal for the Codex screen.
 *
 * <p>This payload intentionally carries no data beyond a request token. The actual screen content
 * should be supplied separately by {@link CodexSnapshotPayload}, which is treated as pure data.
 * This prevents future lifecycle/state sync paths from accidentally opening the Codex UI just by
 * reusing the snapshot payload.</p>
 */
public record OpenCodexPayload(boolean requestOpen) implements CustomPacketPayload {
   public static final Type<OpenCodexPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("herbcraft", "open_codex"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenCodexPayload> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.BOOL, OpenCodexPayload::requestOpen, OpenCodexPayload::new);

   @Override
   public Type<OpenCodexPayload> type() {
      return TYPE;
   }

   public static OpenCodexPayload open() {
      return new OpenCodexPayload(true);
   }
}
