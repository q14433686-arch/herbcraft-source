package com.herbcraft.alchemy.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Source-essence pharmacology projection persisted on processed potion stacks.
 *
 * <p>P2 preserves the source identity as queryable runtime metadata instead of collapsing a
 * processed potion into style-only semantics.</p>
 */
public record PotionPharmacologyProjection(
   String sourceEssence,
   String temperature,
   List<String> flavors,
   List<String> tasteFlavors,
   List<String> traits
) {
   public static final Codec<PotionPharmacologyProjection> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.fieldOf("source_essence").forGetter(PotionPharmacologyProjection::sourceEssence),
            Codec.STRING.fieldOf("temperature").forGetter(PotionPharmacologyProjection::temperature),
            Codec.STRING.listOf().fieldOf("flavors").forGetter(PotionPharmacologyProjection::flavors),
            Codec.STRING.listOf().fieldOf("taste_flavors").forGetter(PotionPharmacologyProjection::tasteFlavors),
            Codec.STRING.listOf().fieldOf("traits").forGetter(PotionPharmacologyProjection::traits)
         )
         .apply(instance, PotionPharmacologyProjection::new)
   );

   public static final StreamCodec<RegistryFriendlyByteBuf, PotionPharmacologyProjection> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      PotionPharmacologyProjection::sourceEssence,
      ByteBufCodecs.STRING_UTF8,
      PotionPharmacologyProjection::temperature,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      PotionPharmacologyProjection::flavors,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      PotionPharmacologyProjection::tasteFlavors,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      PotionPharmacologyProjection::traits,
      PotionPharmacologyProjection::new
   );

   public PotionPharmacologyProjection {
      flavors = List.copyOf(flavors == null ? List.of() : flavors);
      tasteFlavors = List.copyOf(tasteFlavors == null ? List.of() : tasteFlavors);
      traits = List.copyOf(traits == null ? List.of() : traits);
   }
}
