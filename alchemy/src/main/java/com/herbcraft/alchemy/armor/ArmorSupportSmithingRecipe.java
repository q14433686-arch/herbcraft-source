package com.herbcraft.alchemy.armor;

import com.herbcraft.alchemy.coating.CoatingItems;
import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistillationRules;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import com.herbcraft.alchemy.distillation.DistillationItems;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.Recipe.CommonInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

public class ArmorSupportSmithingRecipe extends SimpleSmithingRecipe {
   public static final MapCodec<ArmorSupportSmithingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(RecipeMode.CODEC.fieldOf("mode").forGetter(recipe -> recipe.mode)).apply(instance, ArmorSupportSmithingRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ArmorSupportSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
      RecipeMode.STREAM_CODEC, recipe -> recipe.mode, ArmorSupportSmithingRecipe::new
   );
   private final RecipeMode mode;
   private final ItemStack displayTemplate;
   private final ItemStack displayBase;
   private final ItemStack displayAddition;
   private final ItemStack displayOutput;

   public ArmorSupportSmithingRecipe(RecipeMode mode) {
      this(mode, null, null, null, null);
   }

   public ArmorSupportSmithingRecipe(RecipeMode mode, ItemStack displayTemplate, ItemStack displayBase, ItemStack displayAddition, ItemStack displayOutput) {
      super(new CommonInfo(false));
      this.mode = mode;
      this.displayTemplate = displayTemplate == null ? null : displayTemplate.copy();
      this.displayBase = displayBase == null ? null : displayBase.copy();
      this.displayAddition = displayAddition == null ? null : displayAddition.copy();
      this.displayOutput = displayOutput == null ? null : displayOutput.copy();
   }

   public RecipeMode recipeMode() {
      return mode;
   }

   public Optional<ItemStack> displayTemplate() {
      return Optional.ofNullable(this.displayTemplate).map(ItemStack::copy);
   }

   public Optional<ItemStack> displayBase() {
      return Optional.ofNullable(this.displayBase).map(ItemStack::copy);
   }

   public Optional<ItemStack> displayAddition() {
      return Optional.ofNullable(this.displayAddition).map(ItemStack::copy);
   }

   public Optional<ItemStack> displayOutput() {
      return Optional.ofNullable(this.displayOutput).map(ItemStack::copy);
   }

   public RecipeSerializer<ArmorSupportSmithingRecipe> getSerializer() {
      return ArmorSupportRecipes.ARMOR_SUPPORT_SMITHING;
   }

   public boolean isSpecial() {
      return false;
   }

   public List<RecipeDisplay> display() {
      return mode == RecipeMode.APPLY ? applyDisplays() : refineDisplays();
   }

   private List<RecipeDisplay> applyDisplays() {
      List<RecipeDisplay> displays = new ArrayList<>();
      SlotDisplay template = new SlotDisplay.ItemSlotDisplay(CoatingItems.BLANK_ADHESIVE);
      SlotDisplay base = new SlotDisplay.ItemSlotDisplay(Items.IRON_CHESTPLATE);
      SlotDisplay station = new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE);
      for (DistillationRules.Orientation orientation : DistillationRules.implementedOrientations()) {
         ItemStack distilled = DistillationItems.stack("dandelion", orientation.id());
         ItemStack output = sampleArmor("dandelion", orientation.id(), ArmorSupportMode.ATTACHED);
         displays.add(new SmithingRecipeDisplay(template, base, stackDisplay(distilled), stackDisplay(output), station));
      }
      return displays;
   }

   private List<RecipeDisplay> refineDisplays() {
      return List.of(
         refineDisplay(CoatingItems.AMETHYST_ADHESIVE, ArmorSupportMode.CLEAR),
         refineDisplay(CoatingItems.ECHO_ADHESIVE, ArmorSupportMode.DEEP)
      );
   }

   private RecipeDisplay refineDisplay(net.minecraft.world.level.ItemLike adhesive, ArmorSupportMode targetMode) {
      ItemStack base = sampleArmor("dandelion", "purified", ArmorSupportMode.ATTACHED);
      ItemStack output = sampleArmor("dandelion", "purified", targetMode);
      return new SmithingRecipeDisplay(new SlotDisplay.ItemSlotDisplay(adhesive.asItem()), stackDisplay(base), SlotDisplay.Empty.INSTANCE, stackDisplay(output), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE));
   }

   private static SlotDisplay stackDisplay(ItemStack stack) {
      return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
   }

   private static ItemStack sampleArmor(String source, String orientation, ArmorSupportMode mode) {
      ItemStack stack = new ItemStack(Items.IRON_CHESTPLATE);
      ArmorSupportRules.supportForOrientation(orientation).ifPresent(support -> stack.set(ArmorSupportComponents.ARMOR_SUPPORT, new ArmorSupportComponent(support.id(), source, orientation, support.bufferAmount(), mode)));
      return stack;
   }

   public Optional<Ingredient> templateIngredient() {
      return Optional.of(mode == RecipeMode.APPLY ? Ingredient.of(CoatingItems.BLANK_ADHESIVE) : Ingredient.of(CoatingItems.AMETHYST_ADHESIVE, CoatingItems.ECHO_ADHESIVE));
   }

   public Ingredient baseIngredient() {
      return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.TRIMMABLE_ARMOR));
   }

   public Optional<Ingredient> additionIngredient() {
      return mode == RecipeMode.APPLY ? Optional.of(Ingredient.of(DistillationItems.DISTILLED_ESSENCE)) : Optional.empty();
   }

   public boolean matches(SmithingRecipeInput input, Level level) {
      if (mode == RecipeMode.APPLY) {
         return input.template().is(CoatingItems.BLANK_ADHESIVE) && isSupportedArmor(input.base()) && input.base().get(ArmorSupportComponents.ARMOR_SUPPORT) == null && validDistilled(input.addition()).isPresent();
      }
      boolean clear = input.template().is(CoatingItems.AMETHYST_ADHESIVE);
      boolean deep = input.template().is(CoatingItems.ECHO_ADHESIVE);
      return (clear || deep) && input.addition().isEmpty() && input.base().get(ArmorSupportComponents.ARMOR_SUPPORT) != null;
   }

   public ItemStack assemble(SmithingRecipeInput input) {
      ItemStack result = input.base().copyWithCount(1);
      if (mode == RecipeMode.APPLY) {
         Optional<DistilledEssenceComponent> distilled = validDistilled(input.addition());
         if (distilled.isEmpty()) return ItemStack.EMPTY;
         Optional<ArmorSupportRules.Support> support = ArmorSupportRules.supportForOrientation(distilled.get().orientation());
         if (support.isEmpty()) return ItemStack.EMPTY;
         result.set(ArmorSupportComponents.ARMOR_SUPPORT, new ArmorSupportComponent(support.get().id(), distilled.get().sourceEssence(), distilled.get().orientation(), support.get().bufferAmount(), ArmorSupportMode.ATTACHED));
         return result;
      }
      ArmorSupportComponent current = result.get(ArmorSupportComponents.ARMOR_SUPPORT);
      if (current == null) return ItemStack.EMPTY;
      ArmorSupportMode newMode = input.template().is(CoatingItems.AMETHYST_ADHESIVE) ? ArmorSupportMode.CLEAR : ArmorSupportMode.DEEP;
      result.set(ArmorSupportComponents.ARMOR_SUPPORT, new ArmorSupportComponent(current.supportId(), current.sourceEssence(), current.orientation(), current.bufferAmount(), newMode));
      return result;
   }

   private static Optional<DistilledEssenceComponent> validDistilled(ItemStack stack) {
      DistilledEssenceComponent component = stack.get(DistillationComponents.DISTILLED_ESSENCE);
      if (component == null) return Optional.empty();
      return ArmorSupportRules.supportForOrientation(component.orientation()).isPresent() && AlchemyRegistries.essenceInfoById(component.sourceEssence()).isPresent() ? Optional.of(component) : Optional.empty();
   }

   private static boolean isSupportedArmor(ItemStack stack) {
      Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
      return equippable != null && equippable.slot().isArmor() && equippable.slot() != EquipmentSlot.BODY;
   }

   protected PlacementInfo createPlacementInfo() {
      return PlacementInfo.createFromOptionals(List.of(this.templateIngredient(), Optional.of(this.baseIngredient()), this.additionIngredient()));
   }

   public enum RecipeMode implements StringRepresentable {
      APPLY("apply"), REFINE("refine");
      public static final Codec<RecipeMode> CODEC = StringRepresentable.fromEnum(RecipeMode::values);
      public static final StreamCodec<ByteBuf, RecipeMode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(RecipeMode::byName, RecipeMode::getSerializedName);
      private final String serializedName;
      RecipeMode(String serializedName) { this.serializedName = serializedName; }
      public String getSerializedName() { return serializedName; }
      public static RecipeMode byName(String name) { return "refine".equals(name) ? REFINE : APPLY; }
   }
}
