package com.herbcraft.alchemy.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Item-stack metadata describing the burden tendency of a processed potion. */
public record PotionBurdenComponent(String profile, int burdenWeight, int tailWeight) {
   public static final Codec<PotionBurdenComponent> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.fieldOf("profile").forGetter(PotionBurdenComponent::profile),
            Codec.INT.fieldOf("burden_weight").forGetter(PotionBurdenComponent::burdenWeight),
            Codec.INT.fieldOf("tail_weight").forGetter(PotionBurdenComponent::tailWeight)
         )
         .apply(instance, PotionBurdenComponent::new)
   );

   public static final StreamCodec<RegistryFriendlyByteBuf, PotionBurdenComponent> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      PotionBurdenComponent::profile,
      ByteBufCodecs.VAR_INT,
      PotionBurdenComponent::burdenWeight,
      ByteBufCodecs.VAR_INT,
      PotionBurdenComponent::tailWeight,
      PotionBurdenComponent::new
   );
}
