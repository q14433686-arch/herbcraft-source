package com.herbcraft.alchemy.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record PotionBonusComponent(Identifier effectId, int durationTicks, int amplifier) {
   public static final Codec<PotionBonusComponent> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Identifier.CODEC.fieldOf("effect").forGetter(PotionBonusComponent::effectId),
            Codec.INT.fieldOf("duration_ticks").forGetter(PotionBonusComponent::durationTicks),
            Codec.INT.fieldOf("amplifier").forGetter(PotionBonusComponent::amplifier)
         )
         .apply(instance, PotionBonusComponent::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, PotionBonusComponent> STREAM_CODEC = StreamCodec.composite(
      Identifier.STREAM_CODEC,
      PotionBonusComponent::effectId,
      ByteBufCodecs.VAR_INT,
      PotionBonusComponent::durationTicks,
      ByteBufCodecs.VAR_INT,
      PotionBonusComponent::amplifier,
      PotionBonusComponent::new
   );
}
