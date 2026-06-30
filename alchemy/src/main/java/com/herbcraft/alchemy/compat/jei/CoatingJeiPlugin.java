package com.herbcraft.alchemy.compat.jei;

import com.herbcraft.alchemy.armor.ArmorSupportComponent;
import com.herbcraft.alchemy.armor.ArmorSupportComponents;
import com.herbcraft.alchemy.armor.ArmorSupportMode;
import com.herbcraft.alchemy.armor.ArmorSupportRules;
import com.herbcraft.alchemy.armor.ArmorSupportSmithingRecipe;
import com.herbcraft.alchemy.coating.CoatingItems;
import com.herbcraft.alchemy.coating.CoatingSmithingRecipe;
import com.herbcraft.alchemy.coating.CoatingSystem;
import com.herbcraft.alchemy.coating.ProcessedCoatingRules;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistillationItems;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import com.herbcraft.alchemy.distillation.DistillationRules;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public final class CoatingJeiPlugin implements IModPlugin {
   private static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath("herbcraft", "alchemy_viewers_jei");
   private static final ResourceKey<Recipe<?>> COATING_REFINE_RECIPE_ID = recipeKey("coating_refine");
   private static final ResourceKey<Recipe<?>> ARMOR_SUPPORT_REFINE_RECIPE_ID = recipeKey("armor_support_refine");

   public Identifier getPluginUid() {
      return PLUGIN_ID;
   }

   public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
      registration.getSmithingCategory().addExtension(CoatingSmithingRecipe.class, new CoatingSmithingExtension());
      registration.getSmithingCategory().addExtension(ArmorSupportSmithingRecipe.class, new ArmorSupportSmithingExtension());
   }

   public void registerItemSubtypes(ISubtypeRegistration registration) {
      registration.registerSubtypeInterpreter(DistillationItems.DISTILLED_ESSENCE, (stack, context) -> distilledSubtype(stack));
      for (Item bottle : potionBottleItems()) {
         registration.registerSubtypeInterpreter(bottle, (stack, context) -> potionSubtype(stack));
      }
   }

   public void registerRecipes(IRecipeRegistration registration) {
      List<RecipeHolder<net.minecraft.world.item.crafting.SmithingRecipe>> smithing = new ArrayList<>();
      smithing.addAll(coatingApplyRecipes());
      smithing.add(new RecipeHolder<>(COATING_REFINE_RECIPE_ID, new CoatingSmithingRecipe(CoatingSmithingRecipe.RecipeMode.REFINE)));
      smithing.addAll(armorSupportApplyRecipes());
      smithing.add(new RecipeHolder<>(ARMOR_SUPPORT_REFINE_RECIPE_ID, new ArmorSupportSmithingRecipe(ArmorSupportSmithingRecipe.RecipeMode.REFINE)));
      registration.addRecipes(RecipeTypes.SMITHING, smithing);
   }

   public void registerAdvanced(IAdvancedRegistration registration) {
      registration.addSimpleRecipeManagerPlugin(RecipeTypes.BREWING, new ComponentAwareBrewingRecipeManagerPlugin());
   }

   private static List<RecipeHolder<net.minecraft.world.item.crafting.SmithingRecipe>> coatingApplyRecipes() {
      List<RecipeHolder<net.minecraft.world.item.crafting.SmithingRecipe>> recipes = new ArrayList<>();
      for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
         for (ItemStack base : coatableWeaponStacks()) {
            recipes.add(coatingApplyRecipe(info, base, new ItemStack(info.item()), "base", "essence"));
            for (ProcessedCoatingRules.Style style : processedStyles()) {
               recipes.add(coatingApplyRecipe(info, base, DistillationItems.stack(info.id(), style.id()), style.id(), style.id()));
            }
         }
      }
      return List.copyOf(recipes);
   }

   private static RecipeHolder<net.minecraft.world.item.crafting.SmithingRecipe> coatingApplyRecipe(AlchemyRegistries.EssenceInfo info, ItemStack base, ItemStack addition, String processingStyle, String additionKey) {
      ItemStack output = base.copyWithCount(1);
      CoatingSystem.applyCoating(output, info, WeaponCoating.Mode.FULL, processingStyle);
      String baseKey = itemKey(base.getItem());
      return new RecipeHolder<>(
         recipeKey("coating_apply_" + additionKey + "_" + info.id() + "_" + baseKey),
         new CoatingSmithingRecipe(CoatingSmithingRecipe.RecipeMode.APPLY, new ItemStack(CoatingItems.BLANK_ADHESIVE), base.copyWithCount(1), addition.copy(), output)
      );
   }

   private static List<RecipeHolder<net.minecraft.world.item.crafting.SmithingRecipe>> armorSupportApplyRecipes() {
      List<RecipeHolder<net.minecraft.world.item.crafting.SmithingRecipe>> recipes = new ArrayList<>();
      for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
         for (ArmorSupportRules.Support support : ArmorSupportRules.supports()) {
            if (support.implemented()) {
               for (ItemStack base : armorBaseStacks()) {
                  ItemStack addition = DistillationItems.stack(info.id(), support.orientation());
                  ItemStack output = supportedArmorStack(base, info, support, ArmorSupportMode.ATTACHED);
                  recipes.add(new RecipeHolder<>(
                     recipeKey("armor_support_apply_" + support.orientation() + "_" + info.id() + "_" + itemKey(base.getItem())),
                     new ArmorSupportSmithingRecipe(ArmorSupportSmithingRecipe.RecipeMode.APPLY, new ItemStack(CoatingItems.BLANK_ADHESIVE), base.copyWithCount(1), addition, output)
                  ));
               }
            }
         }
      }
      return List.copyOf(recipes);
   }

   private static List<IJeiBrewingRecipe> brewingRecipes() {
      List<IJeiBrewingRecipe> recipes = new ArrayList<>();
      for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
         for (DistillationRules.Orientation orientation : DistillationRules.implementedOrientations()) {
            ItemStack distilled = DistillationItems.stack(info.id(), orientation.id());
            recipes.add(brewingRecipe(
               List.of(new ItemStack(orientation.catalyst())),
               List.of(new ItemStack(info.item())),
               distilled.copy(),
               id("jei_distilled_essence_" + info.id() + "_" + orientation.id())
            ));
            targetPotion(info.id(), orientation.id()).ifPresent(target -> {
               for (Item bottle : potionBottleItems()) {
                  recipes.add(brewingRecipe(
                     List.of(distilled.copy()),
                     List.of(potionStack(bottle, basePotion(info.id()).orElse(null))),
                     processedPotionStack(bottle, target),
                     id("jei_processed_potion_" + info.id() + "_" + orientation.id() + "_" + bottleName(bottle))
                  ));
               }
            });
         }
      }
      return List.copyOf(recipes.stream().filter(recipe -> !recipe.getPotionInputs().isEmpty() && !recipe.getPotionInputs().getFirst().isEmpty() && !recipe.getPotionOutput().isEmpty()).toList());
   }

   private static IJeiBrewingRecipe brewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, Identifier uid) {
      return new HerbcraftBrewingRecipe(ingredients, potionInputs, potionOutput, uid);
   }

   private record HerbcraftBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, Identifier uid) implements IJeiBrewingRecipe {
      private HerbcraftBrewingRecipe {
         ingredients = List.copyOf(ingredients.stream().map(ItemStack::copy).toList());
         potionInputs = List.copyOf(potionInputs.stream().map(ItemStack::copy).toList());
         potionOutput = potionOutput.copy();
      }

      public List<ItemStack> getIngredients() {
         return List.copyOf(this.ingredients.stream().map(ItemStack::copy).toList());
      }

      public List<ItemStack> getPotionInputs() {
         return List.copyOf(this.potionInputs.stream().map(ItemStack::copy).toList());
      }

      public ItemStack getPotionOutput() {
         return this.potionOutput.copy();
      }

      public int getBrewingSteps() {
         return 1;
      }

      public Identifier getUid() {
         return this.uid;
      }
   }

   private static Optional<Holder<Potion>> basePotion(String path) {
      return potion(path);
   }

   private static Optional<Holder<Potion>> targetPotion(String sourceEssence, String orientation) {
      String targetPath = switch (orientation) {
         case "purified" -> sourceEssence + "_clarified";
         case "sustained" -> sourceEssence + "_long";
         case "intense" -> "wither_rose".equals(sourceEssence) ? "wither_venom_iii" : sourceEssence + "_strong";
         default -> "";
      };
      return targetPath.isBlank() ? Optional.empty() : potion(targetPath);
   }

   private static Optional<Holder<Potion>> potion(String path) {
      return BuiltInRegistries.POTION.get(ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath("herbcraft", path))).map(holder -> (Holder<Potion>)holder);
   }

   private static ItemStack potionStack(Item item, Holder<Potion> potion) {
      if (potion == null) {
         return ItemStack.EMPTY;
      }
      ItemStack stack = new ItemStack(item);
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
      return stack;
   }

   private static ItemStack processedPotionStack(Item item, Holder<Potion> potion) {
      ItemStack stack = potionStack(item, potion);
      if (!stack.isEmpty()) {
         AdvancedPotionStackInitializer.initializeIfNeeded(stack, RandomSource.create(0L));
      }
      return stack;
   }

   private static String distilledSubtype(ItemStack stack) {
      DistilledEssenceComponent component = stack.get(DistillationComponents.DISTILLED_ESSENCE);
      return component == null ? "empty" : component.sourceEssence() + "|" + component.orientation();
   }

   private static String potionSubtype(ItemStack stack) {
      String path = AdvancedPotionStackInitializer.potionPath(stack).orElse("empty");
      String style = Optional.ofNullable(stack.get(AlchemyPotionComponents.POTION_PROCESSING_STYLE)).orElse("base");
      String source = Optional.ofNullable(stack.get(AlchemyPotionComponents.POTION_SOURCE)).orElse("unknown");
      return path + "|" + style + "|" + source;
   }

   private static List<Item> potionBottleItems() {
      return List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
   }

   private static String bottleName(Item item) {
      if (item == Items.SPLASH_POTION) return "splash";
      if (item == Items.LINGERING_POTION) return "lingering";
      return "normal";
   }

   private static ResourceKey<Recipe<?>> recipeKey(String path) {
      return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("herbcraft", path));
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("herbcraft", path);
   }

   private static final class ComponentAwareBrewingRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<IJeiBrewingRecipe> {
      public boolean isHandledInput(mezz.jei.api.ingredients.ITypedIngredient<?> ingredient) {
         return ingredient.getItemStack().map(stack -> brewingRecipes().stream().anyMatch(recipe -> containsSameStack(recipe.getIngredients(), stack) || containsSameStack(recipe.getPotionInputs(), stack))).orElse(false);
      }

      public boolean isHandledOutput(mezz.jei.api.ingredients.ITypedIngredient<?> ingredient) {
         return ingredient.getItemStack().map(stack -> brewingRecipes().stream().anyMatch(recipe -> sameStack(recipe.getPotionOutput(), stack))).orElse(false);
      }

      public List<IJeiBrewingRecipe> getRecipesForInput(mezz.jei.api.ingredients.ITypedIngredient<?> ingredient) {
         return ingredient.getItemStack()
            .map(stack -> brewingRecipes().stream().filter(recipe -> containsSameStack(recipe.getIngredients(), stack) || containsSameStack(recipe.getPotionInputs(), stack)).toList())
            .orElse(List.of());
      }

      public List<IJeiBrewingRecipe> getRecipesForOutput(mezz.jei.api.ingredients.ITypedIngredient<?> ingredient) {
         return ingredient.getItemStack()
            .map(stack -> brewingRecipes().stream().filter(recipe -> sameStack(recipe.getPotionOutput(), stack)).toList())
            .orElse(List.of());
      }

      public List<IJeiBrewingRecipe> getAllRecipes() {
         return brewingRecipes();
      }
   }

   private static boolean containsSameStack(List<ItemStack> stacks, ItemStack target) {
      return stacks.stream().anyMatch(stack -> sameStack(stack, target));
   }

   private static boolean sameStack(ItemStack left, ItemStack right) {
      if (left.is(DistillationItems.DISTILLED_ESSENCE) || right.is(DistillationItems.DISTILLED_ESSENCE)) {
         return left.is(DistillationItems.DISTILLED_ESSENCE) && right.is(DistillationItems.DISTILLED_ESSENCE) && distilledSubtype(left).equals(distilledSubtype(right));
      }
      if (potionBottleItems().contains(left.getItem()) || potionBottleItems().contains(right.getItem())) {
         return left.is(right.getItem()) && potionSubtype(left).equals(potionSubtype(right));
      }
      return ItemStack.isSameItemSameComponents(left, right);
   }

   private static final class CoatingSmithingExtension implements ISmithingCategoryExtension<CoatingSmithingRecipe> {
      public <T extends IIngredientAcceptor<T>> void setTemplate(CoatingSmithingRecipe recipe, T acceptor) {
         Optional<ItemStack> display = recipe.displayTemplate();
         if (display.isPresent()) {
            acceptor.addItemStacks(List.of(display.get()));
         } else if (recipe.recipeMode() == CoatingSmithingRecipe.RecipeMode.APPLY) {
            acceptor.add(CoatingItems.BLANK_ADHESIVE);
         } else {
            acceptor.addItemStacks(List.of(new ItemStack(CoatingItems.AMETHYST_ADHESIVE), new ItemStack(CoatingItems.ECHO_ADHESIVE)));
         }
      }

      public <T extends IIngredientAcceptor<T>> void setBase(CoatingSmithingRecipe recipe, T acceptor) {
         Optional<ItemStack> display = recipe.displayBase();
         if (display.isPresent()) {
            acceptor.addItemStacks(List.of(display.get()));
         } else if (recipe.recipeMode() == CoatingSmithingRecipe.RecipeMode.APPLY) {
            acceptor.add(recipe.baseIngredient());
         } else {
            acceptor.addItemStacks(fullCoatedSamples());
         }
      }

      public <T extends IIngredientAcceptor<T>> void setAddition(CoatingSmithingRecipe recipe, T acceptor) {
         Optional<ItemStack> display = recipe.displayAddition();
         if (display.isPresent()) {
            acceptor.addItemStacks(List.of(display.get()));
         } else if (recipe.recipeMode() == CoatingSmithingRecipe.RecipeMode.APPLY) {
            acceptor.addItemStacks(coatingAdditionSamples());
         }
      }

      public <T extends IIngredientAcceptor<T>> void setOutput(CoatingSmithingRecipe recipe, T acceptor) {
         Optional<ItemStack> display = recipe.displayOutput();
         if (display.isPresent()) {
            acceptor.addItemStacks(List.of(display.get()));
         } else if (recipe.recipeMode() == CoatingSmithingRecipe.RecipeMode.APPLY) {
            acceptor.addItemStacks(fullCoatedSamples());
         } else {
            acceptor.addItemStacks(refinedSamples());
         }
      }

      private static List<ItemStack> coatingAdditionSamples() {
         List<ItemStack> stacks = new ArrayList<>();
         for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
            stacks.add(new ItemStack(info.item()));
            for (ProcessedCoatingRules.Style style : processedStyles()) {
               stacks.add(DistillationItems.stack(info.id(), style.id()));
            }
         }
         return List.copyOf(stacks);
      }

      private static List<ItemStack> fullCoatedSamples() {
         List<ItemStack> stacks = new ArrayList<>();
         for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
            stacks.add(sample(info, WeaponCoating.Mode.FULL, "base"));
            for (ProcessedCoatingRules.Style style : processedStyles()) {
               stacks.add(sample(info, WeaponCoating.Mode.FULL, style.id()));
            }
         }
         return List.copyOf(stacks);
      }

      private static List<ItemStack> refinedSamples() {
         List<ItemStack> stacks = new ArrayList<>();
         for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
            List<String> styles = new ArrayList<>();
            styles.add("base");
            styles.addAll(processedStyles().stream().map(ProcessedCoatingRules.Style::id).toList());
            for (String style : styles) {
               if (info.effects().stream().anyMatch(AlchemyRegistries.EssenceEffect::positive)) {
                  stacks.add(sample(info, WeaponCoating.Mode.POSITIVE, style));
               }
               if (info.effects().stream().anyMatch(effect -> !effect.positive())) {
                  stacks.add(sample(info, WeaponCoating.Mode.NEGATIVE, style));
               }
            }
         }
         return List.copyOf(stacks);
      }

      private static ItemStack sample(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode, String processingStyle) {
         ItemStack stack = new ItemStack(Items.IRON_SWORD);
         CoatingSystem.applyCoating(stack, info, mode, processingStyle);
         return stack;
      }
   }

   private static final class ArmorSupportSmithingExtension implements ISmithingCategoryExtension<ArmorSupportSmithingRecipe> {
      public <T extends IIngredientAcceptor<T>> void setTemplate(ArmorSupportSmithingRecipe recipe, T acceptor) {
         Optional<ItemStack> display = recipe.displayTemplate();
         if (display.isPresent()) {
            acceptor.addItemStacks(List.of(display.get()));
         } else if (recipe.recipeMode() == ArmorSupportSmithingRecipe.RecipeMode.APPLY) {
            acceptor.add(CoatingItems.BLANK_ADHESIVE);
         } else {
            acceptor.addItemStacks(List.of(new ItemStack(CoatingItems.AMETHYST_ADHESIVE), new ItemStack(CoatingItems.ECHO_ADHESIVE)));
         }
      }

      public <T extends IIngredientAcceptor<T>> void setBase(ArmorSupportSmithingRecipe recipe, T acceptor) {
         Optional<ItemStack> display = recipe.displayBase();
         if (display.isPresent()) {
            acceptor.addItemStacks(List.of(display.get()));
         } else if (recipe.recipeMode() == ArmorSupportSmithingRecipe.RecipeMode.APPLY) {
            acceptor.add(recipe.baseIngredient());
         } else {
            acceptor.addItemStacks(supportedArmorSamples(ArmorSupportMode.ATTACHED));
         }
      }

      public <T extends IIngredientAcceptor<T>> void setAddition(ArmorSupportSmithingRecipe recipe, T acceptor) {
         Optional<ItemStack> display = recipe.displayAddition();
         if (display.isPresent()) {
            acceptor.addItemStacks(List.of(display.get()));
         } else if (recipe.recipeMode() == ArmorSupportSmithingRecipe.RecipeMode.APPLY) {
            acceptor.addItemStacks(armorSupportDistilledSamples());
         }
      }

      public <T extends IIngredientAcceptor<T>> void setOutput(ArmorSupportSmithingRecipe recipe, T acceptor) {
         Optional<ItemStack> display = recipe.displayOutput();
         if (display.isPresent()) {
            acceptor.addItemStacks(List.of(display.get()));
         } else if (recipe.recipeMode() == ArmorSupportSmithingRecipe.RecipeMode.APPLY) {
            acceptor.addItemStacks(supportedArmorSamples(ArmorSupportMode.ATTACHED));
         } else {
            List<ItemStack> stacks = new ArrayList<>();
            stacks.addAll(supportedArmorSamples(ArmorSupportMode.CLEAR));
            stacks.addAll(supportedArmorSamples(ArmorSupportMode.DEEP));
            acceptor.addItemStacks(List.copyOf(stacks));
         }
      }
   }

   private static List<ProcessedCoatingRules.Style> processedStyles() {
      return ProcessedCoatingRules.styles().stream().filter(style -> style.implemented() && !"base".equals(style.id())).toList();
   }

   private static List<ItemStack> armorSupportDistilledSamples() {
      List<ItemStack> stacks = new ArrayList<>();
      for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
         for (ArmorSupportRules.Support support : ArmorSupportRules.supports()) {
            if (support.implemented()) {
               stacks.add(DistillationItems.stack(info.id(), support.orientation()));
            }
         }
      }
      return List.copyOf(stacks);
   }

   private static List<ItemStack> supportedArmorSamples(ArmorSupportMode mode) {
      List<ItemStack> stacks = new ArrayList<>();
      for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
         for (ArmorSupportRules.Support support : ArmorSupportRules.supports()) {
            if (support.implemented()) {
               stacks.add(supportedArmorStack(new ItemStack(Items.IRON_CHESTPLATE), info, support, mode));
            }
         }
      }
      return stacks.isEmpty() ? List.of(new ItemStack(Items.IRON_CHESTPLATE)) : List.copyOf(stacks);
   }

   private static ItemStack supportedArmorStack(ItemStack base, AlchemyRegistries.EssenceInfo info, ArmorSupportRules.Support support, ArmorSupportMode mode) {
      ItemStack stack = base.copyWithCount(1);
      stack.set(ArmorSupportComponents.ARMOR_SUPPORT, new ArmorSupportComponent(support.id(), info.id(), support.orientation(), support.bufferAmount(), mode));
      return stack;
   }

   private static List<ItemStack> coatableWeaponStacks() {
      List<ItemStack> stacks = new ArrayList<>();
      for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(CoatingSystem.COATABLE_WEAPONS)) {
         stacks.add(new ItemStack(holder.value()));
      }
      return stacks.isEmpty() ? List.of(new ItemStack(Items.IRON_SWORD)) : List.copyOf(stacks);
   }

   private static List<ItemStack> armorBaseStacks() {
      return List.of(new ItemStack(Items.LEATHER_HELMET), new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS));
   }

   private static String itemKey(Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return (id.getNamespace() + "_" + id.getPath()).replace('/', '_');
   }
}
