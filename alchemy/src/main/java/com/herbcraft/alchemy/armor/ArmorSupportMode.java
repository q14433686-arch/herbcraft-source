package com.herbcraft.alchemy.armor;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ArmorSupportMode implements StringRepresentable {
   ATTACHED("attached"),
   CLEAR("clear"),
   DEEP("deep");

   public static final StringRepresentable.EnumCodec<ArmorSupportMode> CODEC = StringRepresentable.fromEnum(ArmorSupportMode::values);
   public static final StreamCodec<ByteBuf, ArmorSupportMode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ArmorSupportMode::byName, ArmorSupportMode::getSerializedName);
   private final String serializedName;

   ArmorSupportMode(String serializedName) {
      this.serializedName = serializedName;
   }

   @Override
   public String getSerializedName() {
      return serializedName;
   }

   public static ArmorSupportMode byName(String name) {
      for (ArmorSupportMode mode : values()) {
         if (mode.serializedName.equals(name)) {
            return mode;
         }
      }
      return ATTACHED;
   }
}
