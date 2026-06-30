package com.herbcraft.cuisine.registry;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public final class CuisineComponents {
   public static final DataComponentType<List<String>> HODGEPODGE_CONTENTS = (DataComponentType<List<String>>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_cuisine", "hodgepodge_contents"),
      DataComponentType.<List<String>>builder()
         .persistent(Codec.STRING.listOf())
         .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
         .cacheEncoding()
         .build()
   );

   private CuisineComponents() {
   }

   public static void initialize() {
   }
}
