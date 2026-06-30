package com.herbcraft.cuisine.recipe;

import com.herbcraft.api.HerbDefinition;
import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.cuisine.registry.CuisineComponents;
import com.herbcraft.cuisine.registry.CuisineItems;
import com.herbcraft.cuisine.registry.CuisineRecipes;
import com.herbcraft.cuisine.registry.HodgepodgeRules;
import com.herbcraft.cuisine.registry.DishRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class HodgepodgeRecipe extends CustomRecipe {
   private static final TagKey<Item> HODGEPODGE_HERBS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("herbcraft", "herb"));

   public boolean matches(CraftingInput input, Level level) {
      return analyze(input) != null;
   }

   public ItemStack assemble(CraftingInput input) {
      List<Item> herbs = analyze(input);
      if (herbs == null) {
         return ItemStack.EMPTY;
      } else {
         ItemStack result = new ItemStack(CuisineItems.HODGEPODGE);
         List<String> ids = herbs.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).toList();
         result.set(CuisineComponents.HODGEPODGE_CONTENTS, ids);
         int nutrition = 0;
         float saturation = 0.0F;

         for (Item item : herbs) {
            Optional<HerbDefinition> definition = HerbcraftAPI.getDefinition(item);
            if (definition.isPresent()) {
               nutrition += definition.get().nutrition();
               saturation = Math.max(saturation, definition.get().saturation());
            }
         }

         nutrition = Math.min(nutrition, HodgepodgeRules.current().nutritionCap);
         FoodProperties baseFood = (FoodProperties)result.get(DataComponents.FOOD);
         boolean alwaysEdible = baseFood != null && baseFood.canAlwaysEat();
         result.set(DataComponents.FOOD, new FoodProperties(nutrition, saturation, alwaysEdible));
         return result;
      }
   }

   private static List<Item> analyze(CraftingInput input) {
      int bowls = 0;
      List<Item> herbs = new ArrayList<>();

      for (int i = 0; i < input.size(); i++) {
         ItemStack stack = input.getItem(i);
         if (!stack.isEmpty()) {
            Item item = stack.getItem();
            if (item == Items.BOWL) {
               bowls++;
            } else {
               if (item == Items.RED_MUSHROOM || item == Items.BROWN_MUSHROOM) {
                  return null;
               }

               if (!BuiltInRegistries.ITEM.wrapAsHolder(item).is(HODGEPODGE_HERBS) || HerbcraftAPI.getDefinition(item).isEmpty()) {
                  return null;
               }

               herbs.add(item);
            }
         }
      }

      HodgepodgeRules rules = HodgepodgeRules.current();
      boolean hiddenCombo = isHiddenSpecialCombo(herbs);
      boolean validCount = herbs.size() >= rules.minHerbs && herbs.size() <= rules.normalMaxHerbs || hiddenCombo && herbs.size() <= rules.maxHerbs;
      if (bowls == 1 && validCount) {
         return DishRegistry.isCuratedCombo(herbs) && !hiddenCombo ? null : herbs;
      } else {
         return null;
      }
   }

   private static boolean isHiddenSpecialCombo(List<Item> herbs) {
      return sameMultiset(herbs, Items.DANDELION, Items.OXEYE_DAISY, Items.CORNFLOWER, Items.BLUE_ORCHID, Items.ALLIUM, Items.PINK_PETALS, Items.TORCHFLOWER)
         || sameMultiset(herbs, Items.WITHER_ROSE, Items.LILY_OF_THE_VALLEY, Items.FLOWERING_AZALEA_LEAVES, Items.CLOSED_EYEBLOSSOM);
   }

   private static boolean sameMultiset(List<Item> herbs, Item... expected) {
      if (herbs.size() != expected.length) return false;
      boolean[] used = new boolean[herbs.size()];
      for (Item item : expected) {
         boolean matched = false;
         for (int i = 0; i < herbs.size(); i++) {
            if (!used[i] && herbs.get(i) == item) { used[i] = true; matched = true; break; }
         }
         if (!matched) return false;
      }
      return true;
   }

   public RecipeSerializer<HodgepodgeRecipe> getSerializer() {
      return CuisineRecipes.HODGEPODGE;
   }

   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }
}
