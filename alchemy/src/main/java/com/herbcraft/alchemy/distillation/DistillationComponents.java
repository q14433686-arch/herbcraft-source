package com.herbcraft.alchemy.distillation;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class DistillationComponents {
   public static final DataComponentType<DistilledEssenceComponent> DISTILLED_ESSENCE = (DataComponentType<DistilledEssenceComponent>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft_alchemy", "distilled_essence"),
      DataComponentType.<DistilledEssenceComponent>builder()
         .persistent(DistilledEssenceComponent.CODEC)
         .networkSynchronized(DistilledEssenceComponent.STREAM_CODEC)
         .cacheEncoding()
         .build()
   );

   private DistillationComponents() {
   }

   public static void initialize() {
   }
}
