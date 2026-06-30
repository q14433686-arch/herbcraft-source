package com.herbcraft.alchemy.distillation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Component carried by the B3 mixed-route distilled essence container item. */
public record DistilledEssenceComponent(String sourceEssence, String orientation) {
   public static final Codec<DistilledEssenceComponent> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.fieldOf("source_essence").forGetter(DistilledEssenceComponent::sourceEssence),
            Codec.STRING.fieldOf("orientation").forGetter(DistilledEssenceComponent::orientation)
         )
         .apply(instance, DistilledEssenceComponent::new)
   );

   public static final StreamCodec<RegistryFriendlyByteBuf, DistilledEssenceComponent> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      DistilledEssenceComponent::sourceEssence,
      ByteBufCodecs.STRING_UTF8,
      DistilledEssenceComponent::orientation,
      DistilledEssenceComponent::new
   );
}
