package com.herbcraft.alchemy.coating;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public record WeaponCoating(String essenceId, WeaponCoating.Mode mode, int remainingUses, long expiresAt, String processingStyle) {
   public WeaponCoating(String essenceId, WeaponCoating.Mode mode, int remainingUses, long expiresAt) {
      this(essenceId, mode, remainingUses, expiresAt, "base");
   }

   public static final Codec<WeaponCoating> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.fieldOf("essence").forGetter(WeaponCoating::essenceId),
            WeaponCoating.Mode.CODEC.fieldOf("mode").forGetter(WeaponCoating::mode),
            Codec.INT.fieldOf("remaining_uses").forGetter(WeaponCoating::remainingUses),
            Codec.LONG.optionalFieldOf("expires_at", -1L).forGetter(WeaponCoating::expiresAt),
            Codec.STRING.optionalFieldOf("processing_style", "base").forGetter(WeaponCoating::processingStyle)
         )
         .apply(instance, WeaponCoating::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, WeaponCoating> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      WeaponCoating::essenceId,
      WeaponCoating.Mode.STREAM_CODEC,
      WeaponCoating::mode,
      ByteBufCodecs.VAR_INT,
      WeaponCoating::remainingUses,
      ByteBufCodecs.VAR_LONG,
      WeaponCoating::expiresAt,
      ByteBufCodecs.STRING_UTF8,
      WeaponCoating::processingStyle,
      WeaponCoating::new
   );

   public static enum Mode implements StringRepresentable {
      FULL("full"),
      POSITIVE("positive"),
      NEGATIVE("negative");

      public static final Codec<WeaponCoating.Mode> CODEC = StringRepresentable.fromEnum(WeaponCoating.Mode::values);
      public static final StreamCodec<ByteBuf, WeaponCoating.Mode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
         .map(WeaponCoating.Mode::byName, WeaponCoating.Mode::getSerializedName);
      private final String serializedName;

      private Mode(String serializedName) {
         this.serializedName = serializedName;
      }

      public String getSerializedName() {
         return this.serializedName;
      }

      public static WeaponCoating.Mode byName(String name) {
         for (WeaponCoating.Mode mode : values()) {
            if (mode.serializedName.equals(name)) {
               return mode;
            }
         }

         return FULL;
      }
   }
}
