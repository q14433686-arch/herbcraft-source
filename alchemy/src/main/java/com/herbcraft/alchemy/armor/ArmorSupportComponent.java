package com.herbcraft.alchemy.armor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Alchemy armor-side support component applied from a distilled essence. */
public record ArmorSupportComponent(String supportId, String sourceEssence, String orientation, int bufferAmount, ArmorSupportMode mode) {
   public ArmorSupportComponent(String supportId, String sourceEssence, String orientation, int bufferAmount) {
      this(supportId, sourceEssence, orientation, bufferAmount, ArmorSupportMode.ATTACHED);
   }

   public static final Codec<ArmorSupportComponent> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.fieldOf("support_id").forGetter(ArmorSupportComponent::supportId),
            Codec.STRING.fieldOf("source_essence").forGetter(ArmorSupportComponent::sourceEssence),
            Codec.STRING.fieldOf("orientation").forGetter(ArmorSupportComponent::orientation),
            Codec.INT.fieldOf("buffer_amount").forGetter(ArmorSupportComponent::bufferAmount),
            ArmorSupportMode.CODEC.optionalFieldOf("mode", ArmorSupportMode.ATTACHED).forGetter(ArmorSupportComponent::mode)
         ).apply(instance, ArmorSupportComponent::new)
   );

   public static final StreamCodec<RegistryFriendlyByteBuf, ArmorSupportComponent> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      ArmorSupportComponent::supportId,
      ByteBufCodecs.STRING_UTF8,
      ArmorSupportComponent::sourceEssence,
      ByteBufCodecs.STRING_UTF8,
      ArmorSupportComponent::orientation,
      ByteBufCodecs.VAR_INT,
      ArmorSupportComponent::bufferAmount,
      ArmorSupportMode.STREAM_CODEC,
      ArmorSupportComponent::mode,
      ArmorSupportComponent::new
   );
}
