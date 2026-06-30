package com.herbcraft.alchemy.armor;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ArmorSupportComponents {
   public static final DataComponentType<ArmorSupportComponent> ARMOR_SUPPORT = (DataComponentType<ArmorSupportComponent>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "armor_support"),
      DataComponentType.<ArmorSupportComponent>builder()
         .persistent(ArmorSupportComponent.CODEC)
         .networkSynchronized(ArmorSupportComponent.STREAM_CODEC)
         .cacheEncoding()
         .build()
   );

   private ArmorSupportComponents() {
   }

   public static void initialize() {
   }
}
