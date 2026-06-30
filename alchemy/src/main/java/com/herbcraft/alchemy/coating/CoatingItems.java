package com.herbcraft.alchemy.coating;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public final class CoatingItems {
   public static final Item BLANK_ADHESIVE = register("blank_adhesive");
   public static final Item AMETHYST_ADHESIVE = register("amethyst_adhesive");
   public static final Item ECHO_ADHESIVE = register("echo_adhesive");

   private CoatingItems() {
   }

   public static void initialize() {
   }

   private static Item register(String name) {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", name);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      Item item = new Item(new Properties().stacksTo(16).setId(key));
      Registry.register(BuiltInRegistries.ITEM, key, item);
      return item;
   }
}
