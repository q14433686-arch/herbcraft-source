package com.herbcraft.alchemy.distillation;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class DistillationRecipes {
   public static final RecipeSerializer<DistillationRecipe> DISTILLATION = (RecipeSerializer<DistillationRecipe>)Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER,
      Identifier.fromNamespaceAndPath("herbcraft", "distillation"),
      new RecipeSerializer(DistillationRecipe.MAP_CODEC, DistillationRecipe.STREAM_CODEC)
   );

   private DistillationRecipes() {
   }

   public static void initialize() {
   }
}
