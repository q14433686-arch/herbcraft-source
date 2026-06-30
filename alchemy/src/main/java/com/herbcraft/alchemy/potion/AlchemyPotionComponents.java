package com.herbcraft.alchemy.potion;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public final class AlchemyPotionComponents {
   public static final DataComponentType<AlchemyPotionComponents.PotionQuality> POTION_QUALITY = (DataComponentType<AlchemyPotionComponents.PotionQuality>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "potion_quality"),
      DataComponentType.<AlchemyPotionComponents.PotionQuality>builder()
         .persistent(AlchemyPotionComponents.PotionQuality.CODEC)
         .networkSynchronized(AlchemyPotionComponents.PotionQuality.STREAM_CODEC)
         .cacheEncoding()
         .build()
   );
   public static final DataComponentType<PotionBonusComponent> POTION_BONUS = (DataComponentType<PotionBonusComponent>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "potion_bonus"),
      DataComponentType.<PotionBonusComponent>builder().persistent(PotionBonusComponent.CODEC).networkSynchronized(PotionBonusComponent.STREAM_CODEC).cacheEncoding().build()
   );
   public static final DataComponentType<String> POTION_SOURCE = (DataComponentType<String>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "potion_source"),
      DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).cacheEncoding().build()
   );
   public static final DataComponentType<String> POTION_PROCESSING_STYLE = (DataComponentType<String>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "potion_processing_style"),
      DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).cacheEncoding().build()
   );
   public static final DataComponentType<PotionBurdenComponent> POTION_BURDEN = (DataComponentType<PotionBurdenComponent>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "potion_burden"),
      DataComponentType.<PotionBurdenComponent>builder().persistent(PotionBurdenComponent.CODEC).networkSynchronized(PotionBurdenComponent.STREAM_CODEC).cacheEncoding().build()
   );
   public static final DataComponentType<PotionPharmacologyProjection> POTION_PHARMACOLOGY = (DataComponentType<PotionPharmacologyProjection>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "potion_pharmacology"),
      DataComponentType.<PotionPharmacologyProjection>builder().persistent(PotionPharmacologyProjection.CODEC).networkSynchronized(PotionPharmacologyProjection.STREAM_CODEC).cacheEncoding().build()
   );
   public static final DataComponentType<PotionBodyStateProjection> POTION_BODY_STATE = (DataComponentType<PotionBodyStateProjection>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "potion_body_state"),
      DataComponentType.<PotionBodyStateProjection>builder().persistent(PotionBodyStateProjection.CODEC).networkSynchronized(PotionBodyStateProjection.STREAM_CODEC).cacheEncoding().build()
   );
   public static final DataComponentType<Boolean> ADVANCED_POTION_INITIALIZED = (DataComponentType<Boolean>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "advanced_potion_initialized"),
      DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding().build()
   );

   private AlchemyPotionComponents() {
   }

   public static void initialize() {
   }

   public static enum PotionQuality implements StringRepresentable {
      STANDARD("standard", 1.0F),
      FINE("fine", 1.5F),
      EXCEPTIONAL("exceptional", 2.0F);

      public static final Codec<AlchemyPotionComponents.PotionQuality> CODEC = StringRepresentable.fromEnum(AlchemyPotionComponents.PotionQuality::values);
      public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyPotionComponents.PotionQuality> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(
         CODEC
      );
      private final String serializedName;
      private final float durationMultiplier;

      private PotionQuality(String serializedName, float durationMultiplier) {
         this.serializedName = serializedName;
         this.durationMultiplier = durationMultiplier;
      }

      public String getSerializedName() {
         return this.serializedName;
      }

      public float durationMultiplier() {
         return this.durationMultiplier;
      }

      public static AlchemyPotionComponents.PotionQuality roll(RandomSource random) {
         return roll(random, 0.0F);
      }

      public static AlchemyPotionComponents.PotionQuality roll(RandomSource random, float exceptionalBonus) {
         int exceptionalThreshold = Math.max(0, Math.min(100, Math.round((0.05F + exceptionalBonus) * 100.0F)));
         int fineThreshold = Math.max(exceptionalThreshold, 30);
         int value = random.nextInt(100);
         if (value < exceptionalThreshold) {
            return EXCEPTIONAL;
         }
         return value < exceptionalThreshold + 25 ? FINE : STANDARD;
      }
   }
}
