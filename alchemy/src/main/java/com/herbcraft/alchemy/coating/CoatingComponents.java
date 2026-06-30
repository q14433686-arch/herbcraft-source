package com.herbcraft.alchemy.coating;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class CoatingComponents {
   public static final DataComponentType<WeaponCoating> WEAPON_COATING = (DataComponentType<WeaponCoating>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft", "weapon_coating"),
      DataComponentType.<WeaponCoating>builder().persistent(WeaponCoating.CODEC).networkSynchronized(WeaponCoating.STREAM_CODEC).cacheEncoding().build()
   );

   private CoatingComponents() {
   }

   public static void initialize() {
   }
}
