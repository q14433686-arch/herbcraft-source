package com.herbcraft.alchemy.distillation;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public final class DistillationItems {
   public static final Item DISTILLED_ESSENCE = register("distilled_essence");

   private DistillationItems() {
   }

   public static void initialize() {
   }

   public static ItemStack stack(String sourceEssence, String orientation) {
      ItemStack stack = new ItemStack(DISTILLED_ESSENCE);
      stack.set(DistillationComponents.DISTILLED_ESSENCE, new DistilledEssenceComponent(sourceEssence, orientation));
      return stack;
   }

   private static Item register(String name) {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", name);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      Item item = new Item(new Properties().stacksTo(16).setId(key));
      Registry.register(BuiltInRegistries.ITEM, key, item);
      return item;
   }
}
