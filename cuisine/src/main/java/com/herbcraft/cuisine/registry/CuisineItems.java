package com.herbcraft.cuisine.registry;

import com.herbcraft.cuisine.item.HodgepodgeItem;
import com.herbcraft.cuisine.item.WaterBowlItem;
import com.herbcraft.logic.ConsumeSoundResolver;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.Consumables;

public final class CuisineItems {
   public static final Item WATER_BOWL = registerWaterBowl();
   public static final Item HODGEPODGE = registerHodgepodge();

   private CuisineItems() {
   }

   public static void initialize() {
   }

   private static Item registerWaterBowl() {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft_cuisine", "water_bowl");
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      Item item = new WaterBowlItem(
         new Properties()
            .setId(key)
            .stacksTo(16)
            .food(new FoodProperties(0, 0.0F, true), Consumables.defaultDrink().consumeSeconds(1.0F).sound(ConsumeSoundResolver.soundForCuisine(true)).build())
            .usingConvertsTo(Items.BOWL)
      );
      Registry.register(BuiltInRegistries.ITEM, key, item);
      return item;
   }

   private static Item registerHodgepodge() {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft_cuisine", "hodgepodge");
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      Item item = new HodgepodgeItem(
         new Properties()
            .setId(key)
            .stacksTo(1)
            .food(new FoodProperties(4, 0.5F, false), Consumables.defaultFood().consumeSeconds(3.2F).sound(ConsumeSoundResolver.soundForCuisine(false)).build())
            .usingConvertsTo(Items.BOWL)
      );
      Registry.register(BuiltInRegistries.ITEM, key, item);
      return item;
   }
}
