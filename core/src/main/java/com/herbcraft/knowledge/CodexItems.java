package com.herbcraft.knowledge;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public final class CodexItems {
   public static final Item HERBAL_CODEX = register("herbal_codex", properties -> new CodexBookItem(properties.stacksTo(1)));
   public static final Item TATTERED_PAGE = register("tattered_page", properties -> new TatteredPageItem(properties.stacksTo(16)));

   private CodexItems() {
   }

   public static void initialize() {
   }

   private static Item register(String name, Function<Properties, Item> factory) {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", name);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      Item item = factory.apply(new Properties().setId(key));
      Registry.register(BuiltInRegistries.ITEM, key, item);
      return item;
   }
}
