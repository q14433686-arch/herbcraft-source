package com.herbcraft.cuisine.registry;

import com.herbcraft.cuisine.recipe.HodgepodgeRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class CuisineRecipes {
   public static final RecipeSerializer<HodgepodgeRecipe> HODGEPODGE = (RecipeSerializer<HodgepodgeRecipe>)Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER,
      Identifier.fromNamespaceAndPath("herbcraft_cuisine", "hodgepodge"),
      new RecipeSerializer(MapCodec.unit(HodgepodgeRecipe::new), StreamCodec.unit(new HodgepodgeRecipe()))
   );

   private CuisineRecipes() {
   }

   public static void initialize() {
   }
}
