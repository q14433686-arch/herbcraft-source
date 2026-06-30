package com.herbcraft.alchemy.coating;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class CoatingRecipes {
   public static final RecipeSerializer<CoatingSmithingRecipe> COATING_SMITHING = (RecipeSerializer<CoatingSmithingRecipe>)Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER,
      Identifier.fromNamespaceAndPath("herbcraft", "coating_smithing"),
      new RecipeSerializer(CoatingSmithingRecipe.MAP_CODEC, CoatingSmithingRecipe.STREAM_CODEC)
   );

   private CoatingRecipes() {
   }

   public static void initialize() {
   }
}
