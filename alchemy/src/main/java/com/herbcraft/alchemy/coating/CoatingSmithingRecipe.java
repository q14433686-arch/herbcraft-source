package com.herbcraft.alchemy.coating;

import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistillationItems;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
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
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.Recipe.CommonInfo;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class CoatingSmithingRecipe extends SimpleSmithingRecipe {
   public static final MapCodec<CoatingSmithingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(CoatingSmithingRecipe.RecipeMode.CODEC.fieldOf("mode").forGetter(recipe -> recipe.mode))
         .apply(instance, CoatingSmithingRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, CoatingSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
      CoatingSmithingRecipe.RecipeMode.STREAM_CODEC, recipe -> recipe.mode, CoatingSmithingRecipe::new
   );
   private final CoatingSmithingRecipe.RecipeMode mode;
   private final ItemStack displayTemplate;
   private final ItemStack displayBase;
   private final ItemStack displayAddition;
   private final ItemStack displayOutput;

   public CoatingSmithingRecipe(CoatingSmithingRecipe.RecipeMode mode) {
      this(mode, null, null, null, null);
   }

   public CoatingSmithingRecipe(CoatingSmithingRecipe.RecipeMode mode, ItemStack displayTemplate, ItemStack displayBase, ItemStack displayAddition, ItemStack displayOutput) {
      super(new CommonInfo(false));
      this.mode = mode;
      this.displayTemplate = displayTemplate == null ? null : displayTemplate.copy();
      this.displayBase = displayBase == null ? null : displayBase.copy();
      this.displayAddition = displayAddition == null ? null : displayAddition.copy();
      this.displayOutput = displayOutput == null ? null : displayOutput.copy();
   }

   public RecipeSerializer<CoatingSmithingRecipe> getSerializer() {
      return CoatingRecipes.COATING_SMITHING;
   }

   public boolean isSpecial() {
      return false;
   }

   public CoatingSmithingRecipe.RecipeMode recipeMode() {
      return this.mode;
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

   public List<RecipeDisplay> display() {
      return this.mode == CoatingSmithingRecipe.RecipeMode.APPLY ? this.applyDisplays() : this.refineDisplays();
   }

   private List<RecipeDisplay> applyDisplays() {
      SlotDisplay template = new SlotDisplay.ItemSlotDisplay(CoatingItems.BLANK_ADHESIVE);
      SlotDisplay base = this.baseIngredient().display();
      SlotDisplay station = smithingTableDisplay();
      List<RecipeDisplay> displays = new ArrayList<>();
      for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
         ItemStack result = sampleCoatedWeapon(info, WeaponCoating.Mode.FULL);
         displays.add(new SmithingRecipeDisplay(template, base, new SlotDisplay.ItemSlotDisplay(info.item()), stackDisplay(result), station));
      }
      AlchemyRegistries.essenceInfoById("dandelion").ifPresent(info -> {
         for (ProcessedCoatingRules.Style style : ProcessedCoatingRules.styles()) {
            if (style.implemented() && !"base".equals(style.id())) {
               ItemStack distilled = DistillationItems.stack(info.id(), style.id());
               ItemStack result = sampleCoatedWeapon(info, WeaponCoating.Mode.FULL, style.id());
               displays.add(new SmithingRecipeDisplay(template, base, stackDisplay(distilled), stackDisplay(result), station));
            }
         }
      });
      return displays;
   }

   private List<RecipeDisplay> refineDisplays() {
      SlotDisplay station = smithingTableDisplay();
      List<RecipeDisplay> displays = new ArrayList<>();
      for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
         if (info.effects().stream().anyMatch(AlchemyRegistries.EssenceEffect::positive)) {
            displays.add(refineDisplay(info, CoatingItems.AMETHYST_ADHESIVE, WeaponCoating.Mode.POSITIVE));
         }
         if (info.effects().stream().anyMatch(effect -> !effect.positive())) {
            displays.add(refineDisplay(info, CoatingItems.ECHO_ADHESIVE, WeaponCoating.Mode.NEGATIVE));
         }
      }
      return displays;
   }

   private RecipeDisplay refineDisplay(AlchemyRegistries.EssenceInfo info, ItemLike templateItem, WeaponCoating.Mode refinedMode) {
      ItemStack full = sampleCoatedWeapon(info, WeaponCoating.Mode.FULL);
      ItemStack refined = sampleCoatedWeapon(info, refinedMode);
      return new SmithingRecipeDisplay(
         new SlotDisplay.ItemSlotDisplay(templateItem.asItem()),
         stackDisplay(full),
         SlotDisplay.Empty.INSTANCE,
         stackDisplay(refined),
         smithingTableDisplay()
      );
   }

   private static ItemStack sampleCoatedWeapon(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode) {
      return sampleCoatedWeapon(info, mode, "base");
   }

   private static ItemStack sampleCoatedWeapon(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode, String processingStyle) {
      ItemStack stack = new ItemStack(Items.IRON_SWORD);
      CoatingSystem.applyCoating(stack, info, mode, processingStyle);
      return stack;
   }

   private static SlotDisplay stackDisplay(ItemStack stack) {
      return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
   }

   private static SlotDisplay smithingTableDisplay() {
      return new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE);
   }

   public Optional<Ingredient> templateIngredient() {
      return Optional.of(
         this.mode == CoatingSmithingRecipe.RecipeMode.APPLY
            ? Ingredient.of(CoatingItems.BLANK_ADHESIVE)
            : Ingredient.of(new ItemLike[]{CoatingItems.AMETHYST_ADHESIVE, CoatingItems.ECHO_ADHESIVE})
      );
   }

   public Ingredient baseIngredient() {
      return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(CoatingSystem.COATABLE_WEAPONS));
   }

   public Optional<Ingredient> additionIngredient() {
      return this.mode == CoatingSmithingRecipe.RecipeMode.APPLY ? Optional.of(Ingredient.of(java.util.stream.Stream.concat(AlchemyRegistries.essenceItems().stream(), java.util.stream.Stream.of(DistillationItems.DISTILLED_ESSENCE)))) : Optional.empty();
   }

   public boolean matches(SmithingRecipeInput input, Level level) {
      ItemStack template = input.template();
      ItemStack base = input.base();
      ItemStack addition = input.addition();
      if (!CoatingSystem.isCoatable(base)) {
         return false;
      } else if (this.mode == CoatingSmithingRecipe.RecipeMode.APPLY) {
         return template.is(CoatingItems.BLANK_ADHESIVE) && (AlchemyRegistries.essenceInfo(addition.getItem()).isPresent() || distilledEssence(addition).isPresent());
      } else {
         boolean amethyst = template.is(CoatingItems.AMETHYST_ADHESIVE);
         boolean echo = template.is(CoatingItems.ECHO_ADHESIVE);
         if ((amethyst || echo) && addition.isEmpty()) {
            WeaponCoating coating = (WeaponCoating)base.get(CoatingComponents.WEAPON_COATING);
            return coating != null && coating.mode() == WeaponCoating.Mode.FULL
               ? AlchemyRegistries.essenceInfoById(coating.essenceId())
                  .map(info -> info.effects().stream().anyMatch(e -> e.positive() == amethyst))
                  .orElse(false)
               : false;
         } else {
            return false;
         }
      }
   }

   public ItemStack assemble(SmithingRecipeInput input) {
      ItemStack result = input.base().copyWithCount(1);
      if (this.mode == CoatingSmithingRecipe.RecipeMode.APPLY) {
         Optional<AlchemyRegistries.EssenceInfo> info = AlchemyRegistries.essenceInfo(input.addition().getItem());
         String processingStyle = "base";
         Optional<DistilledEssenceComponent> distilled = distilledEssence(input.addition());
         if (info.isEmpty() && distilled.isPresent()) {
            info = AlchemyRegistries.essenceInfoById(distilled.get().sourceEssence());
            processingStyle = distilled.get().orientation();
         }
         if (info.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            CoatingSystem.applyCoating(result, info.get(), WeaponCoating.Mode.FULL, processingStyle);
            return result;
         }
      } else {
         WeaponCoating coating = (WeaponCoating)result.get(CoatingComponents.WEAPON_COATING);
         if (coating == null) {
            return ItemStack.EMPTY;
         } else {
            Optional<AlchemyRegistries.EssenceInfo> info = AlchemyRegistries.essenceInfoById(coating.essenceId());
            if (info.isEmpty()) {
               return ItemStack.EMPTY;
            } else {
               WeaponCoating.Mode newMode = input.template().is(CoatingItems.AMETHYST_ADHESIVE) ? WeaponCoating.Mode.POSITIVE : WeaponCoating.Mode.NEGATIVE;
               CoatingSystem.applyCoating(result, info.get(), newMode, coating.processingStyle());
               return result;
            }
         }
      }
   }

   private static Optional<DistilledEssenceComponent> distilledEssence(ItemStack stack) {
      DistilledEssenceComponent component = stack.get(DistillationComponents.DISTILLED_ESSENCE);
      if (component == null || AlchemyRegistries.essenceInfoById(component.sourceEssence()).isEmpty() || !ProcessedCoatingRules.styleOrBase(component.orientation()).implemented() || "base".equals(ProcessedCoatingRules.styleOrBase(component.orientation()).id())) {
         return Optional.empty();
      }
      return Optional.of(component);
   }

   protected PlacementInfo createPlacementInfo() {
      return PlacementInfo.createFromOptionals(List.of(this.templateIngredient(), Optional.of(this.baseIngredient()), this.additionIngredient()));
   }

   public static enum RecipeMode implements StringRepresentable {
      APPLY("apply"),
      REFINE("refine");

      public static final Codec<CoatingSmithingRecipe.RecipeMode> CODEC = StringRepresentable.fromEnum(CoatingSmithingRecipe.RecipeMode::values);
      public static final StreamCodec<ByteBuf, CoatingSmithingRecipe.RecipeMode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
         .map(CoatingSmithingRecipe.RecipeMode::byName, CoatingSmithingRecipe.RecipeMode::getSerializedName);
      private final String serializedName;

      private RecipeMode(String serializedName) {
         this.serializedName = serializedName;
      }

      public String getSerializedName() {
         return this.serializedName;
      }

      public static CoatingSmithingRecipe.RecipeMode byName(String name) {
         return "refine".equals(name) ? REFINE : APPLY;
      }
   }
}
