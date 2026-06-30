package com.herbcraft.alchemy.armor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ArmorSupportRecipes {
   /** Deprecated/local only: no bundled recipe uses this crafting serializer after D2 station migration. */
   public static final RecipeSerializer<ArmorSupportRecipe> ARMOR_SUPPORT = (RecipeSerializer<ArmorSupportRecipe>)Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER,
      Identifier.fromNamespaceAndPath("herbcraft", "armor_support_legacy_crafting"),
      new RecipeSerializer(MapCodec.unit(ArmorSupportRecipe::new), StreamCodec.unit(new ArmorSupportRecipe()))
   );

   public static final RecipeSerializer<ArmorSupportSmithingRecipe> ARMOR_SUPPORT_SMITHING = (RecipeSerializer<ArmorSupportSmithingRecipe>)Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER,
      Identifier.fromNamespaceAndPath("herbcraft", "armor_support_smithing"),
      new RecipeSerializer(ArmorSupportSmithingRecipe.MAP_CODEC, ArmorSupportSmithingRecipe.STREAM_CODEC)
   );

   private ArmorSupportRecipes() {
   }

   public static void initialize() {
   }
}
