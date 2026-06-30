package com.herbcraft.alchemy.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** P3 processed-potion projection into Core-facing body-state surfaces. */
public record PotionBodyStateProjection(
   String medicinalProfile,
   int medicinalWeight,
   String thermalTendency,
   int thermalValue,
   String qiTendency,
   int qiValue,
   List<String> bodyTraits
) {
   public static final Codec<PotionBodyStateProjection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Codec.STRING.fieldOf("medicinal_profile").forGetter(PotionBodyStateProjection::medicinalProfile),
      Codec.INT.fieldOf("medicinal_weight").forGetter(PotionBodyStateProjection::medicinalWeight),
      Codec.STRING.fieldOf("thermal_tendency").forGetter(PotionBodyStateProjection::thermalTendency),
      Codec.INT.fieldOf("thermal_value").forGetter(PotionBodyStateProjection::thermalValue),
      Codec.STRING.fieldOf("qi_tendency").forGetter(PotionBodyStateProjection::qiTendency),
      Codec.INT.fieldOf("qi_value").forGetter(PotionBodyStateProjection::qiValue),
      Codec.STRING.listOf().fieldOf("body_traits").forGetter(PotionBodyStateProjection::bodyTraits)
   ).apply(instance, PotionBodyStateProjection::new));

   public static final StreamCodec<RegistryFriendlyByteBuf, PotionBodyStateProjection> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      PotionBodyStateProjection::medicinalProfile,
      ByteBufCodecs.INT,
      PotionBodyStateProjection::medicinalWeight,
      ByteBufCodecs.STRING_UTF8,
      PotionBodyStateProjection::thermalTendency,
      ByteBufCodecs.INT,
      PotionBodyStateProjection::thermalValue,
      ByteBufCodecs.STRING_UTF8,
      PotionBodyStateProjection::qiTendency,
      ByteBufCodecs.INT,
      PotionBodyStateProjection::qiValue,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      PotionBodyStateProjection::bodyTraits,
      PotionBodyStateProjection::new
   );
}
