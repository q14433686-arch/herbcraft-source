package com.herbcraft.alchemy.compat.rei;

import com.herbcraft.alchemy.armor.ArmorSupportComponent;
import com.herbcraft.alchemy.armor.ArmorSupportComponents;
import com.herbcraft.alchemy.armor.ArmorSupportMode;
import com.herbcraft.alchemy.armor.ArmorSupportRules;
import com.herbcraft.alchemy.coating.CoatingItems;
import com.herbcraft.alchemy.coating.CoatingSystem;
import com.herbcraft.alchemy.coating.ProcessedCoatingRules;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.distillation.DistillationItems;
import com.herbcraft.alchemy.distillation.DistillationRules;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.SmithingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultSmithingDisplay;
import me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public final class CoatingReiPlugin implements REIClientPlugin {
   public void registerDisplays(DisplayRegistry registry) {
      for (AlchemyRegistries.EssenceInfo info : AlchemyRegistries.essenceInfos()) {
         registry.add(applyDisplay(info));
         for (ProcessedCoatingRules.Style style : processedStyles()) {
            for (ItemStack base : coatableWeaponStacks()) {
               registry.add(processedApplyDisplay(info, style, base));
            }
         }
         if (info.effects().stream().anyMatch(AlchemyRegistries.EssenceEffect::positive)) {
            registry.add(refineDisplay(info, true));
            for (ProcessedCoatingRules.Style style : processedStyles()) {
               registry.add(processedRefineDisplay(info, true, style));
            }
         }
         if (info.effects().stream().anyMatch(effect -> !effect.positive())) {
            registry.add(refineDisplay(info, false));
            for (ProcessedCoatingRules.Style style : processedStyles()) {
               registry.add(processedRefineDisplay(info, false, style));
            }
         }
         for (ArmorSupportRules.Support support : ArmorSupportRules.supports()) {
            if (support.implemented()) {
               for (ItemStack base : armorBaseStacks()) {
                  registry.add(armorSupportApplyDisplay(info, support, base));
               }
            }
         }
         for (DistillationRules.Orientation orientation : DistillationRules.implementedOrientations()) {
            registry.add(brewingDistillationDisplay(info, orientation.id()));
            targetPotion(info.id(), orientation.id()).ifPresent(target -> {
               for (Item bottle : potionBottleItems()) {
                  basePotion(info.id()).ifPresent(base -> registry.add(brewingPotionDisplay(info, orientation.id(), bottle, base, target)));
               }
            });
         }
      }
      for (ArmorSupportRules.Support support : ArmorSupportRules.supports()) {
         if (support.implemented()) {
            registry.add(armorSupportRefineDisplay(support, true));
            registry.add(armorSupportRefineDisplay(support, false));
         }
      }
   }

   private static DefaultSmithingDisplay applyDisplay(AlchemyRegistries.EssenceInfo info) {
      return new DefaultSmithingDisplay(
         List.of(
            entry(CoatingItems.BLANK_ADHESIVE),
            entryStacks(coatableWeaponStacks()),
            entry(info.item())
         ),
         List.of(entryStacks(coatedWeaponStacks(info, WeaponCoating.Mode.FULL, "base"))),
         Optional.of(SmithingDisplay.SmithingRecipeType.TRANSFORM),
         Optional.of(id("coating_apply_" + info.id()))
      );
   }

   private static DefaultSmithingDisplay processedApplyDisplay(AlchemyRegistries.EssenceInfo info, ProcessedCoatingRules.Style style, ItemStack base) {
      ItemStack output = coatedWeaponStack(base, info, WeaponCoating.Mode.FULL, style.id());
      return new DefaultSmithingDisplay(
         List.of(
            entry(CoatingItems.BLANK_ADHESIVE),
            entryStack(base.copyWithCount(1)),
            entryStack(DistillationItems.stack(info.id(), style.id()))
         ),
         List.of(entryStack(output)),
         Optional.of(SmithingDisplay.SmithingRecipeType.TRANSFORM),
         Optional.of(id("coating_apply_" + style.id() + "_" + info.id() + "_" + itemKey(base.getItem())))
      );
   }

   private static DefaultSmithingDisplay refineDisplay(AlchemyRegistries.EssenceInfo info, boolean positive) {
      return processedRefineDisplay(info, positive, null);
   }

   private static DefaultSmithingDisplay processedRefineDisplay(AlchemyRegistries.EssenceInfo info, boolean positive, ProcessedCoatingRules.Style style) {
      WeaponCoating.Mode refinedMode = positive ? WeaponCoating.Mode.POSITIVE : WeaponCoating.Mode.NEGATIVE;
      String styleId = style == null ? "base" : style.id();
      return new DefaultSmithingDisplay(
         List.of(
            entry(positive ? CoatingItems.AMETHYST_ADHESIVE : CoatingItems.ECHO_ADHESIVE),
            entryStacks(coatedWeaponStacks(info, WeaponCoating.Mode.FULL, styleId)),
            EntryIngredient.empty()
         ),
         List.of(entryStacks(coatedWeaponStacks(info, refinedMode, styleId))),
         Optional.of(SmithingDisplay.SmithingRecipeType.TRANSFORM),
         Optional.of(id("coating_refine_" + (positive ? "positive_" : "negative_") + styleId + "_" + info.id()))
      );
   }

   private static DefaultSmithingDisplay armorSupportApplyDisplay(AlchemyRegistries.EssenceInfo info, ArmorSupportRules.Support support, ItemStack base) {
      return new DefaultSmithingDisplay(
         List.of(
            entry(CoatingItems.BLANK_ADHESIVE),
            entryStack(base.copyWithCount(1)),
            entryStack(DistillationItems.stack(info.id(), support.orientation()))
         ),
         List.of(entryStack(supportedArmorStack(base, info, support, ArmorSupportMode.ATTACHED))),
         Optional.of(SmithingDisplay.SmithingRecipeType.TRANSFORM),
         Optional.of(id("armor_support_apply_" + support.orientation() + "_" + info.id() + "_" + itemKey(base.getItem())))
      );
   }

   private static DefaultSmithingDisplay armorSupportRefineDisplay(ArmorSupportRules.Support support, boolean clear) {
      return new DefaultSmithingDisplay(
         List.of(
            entry(clear ? CoatingItems.AMETHYST_ADHESIVE : CoatingItems.ECHO_ADHESIVE),
            entryStacks(supportedArmorStacks(sampleEssence(), support, ArmorSupportMode.ATTACHED)),
            EntryIngredient.empty()
         ),
         List.of(entryStacks(supportedArmorStacks(sampleEssence(), support, clear ? ArmorSupportMode.CLEAR : ArmorSupportMode.DEEP))),
         Optional.of(SmithingDisplay.SmithingRecipeType.TRANSFORM),
         Optional.of(id("armor_support_refine_" + (clear ? "clear_" : "deep_") + support.orientation()))
      );
   }

   private static DefaultBrewingDisplay brewingDistillationDisplay(AlchemyRegistries.EssenceInfo info, String orientationId) {
      Optional<DistillationRules.Orientation> orientation = DistillationRules.orientation(orientationId).filter(DistillationRules.Orientation::implemented);
      ItemStack catalyst = orientation.map(value -> new ItemStack(value.catalyst())).orElse(ItemStack.EMPTY);
      return new DefaultBrewingDisplay(
         entry(new ItemStack(info.item())),
         entry(catalyst),
         entry(DistillationItems.stack(info.id(), orientationId))
      );
   }

   private static DefaultBrewingDisplay brewingPotionDisplay(AlchemyRegistries.EssenceInfo info, String orientationId, Item bottle, Holder<Potion> base, Holder<Potion> target) {
      return new DefaultBrewingDisplay(
         entry(potionStack(bottle, base)),
         entry(DistillationItems.stack(info.id(), orientationId)),
         entry(processedPotionStack(bottle, target))
      );
   }

   private static EntryIngredient entry(net.minecraft.world.level.ItemLike item) {
      return EntryIngredient.of(EntryStacks.of(item));
   }

   private static EntryIngredient entry(ItemStack stack) {
      return EntryIngredient.of(EntryStacks.of(stack));
   }

   private static EntryIngredient entryStack(ItemStack stack) {
      return EntryIngredient.of(EntryStacks.of(stack));
   }

   private static EntryIngredient entryStacks(List<ItemStack> stacks) {
      return EntryIngredient.of(stacks.stream().map(EntryStacks::of).toList());
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

   private static List<ItemStack> coatedWeaponStacks(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode, String processingStyle) {
      List<ItemStack> stacks = new ArrayList<>();
      for (ItemStack base : coatableWeaponStacks()) {
         stacks.add(coatedWeaponStack(base, info, mode, processingStyle));
      }
      return List.copyOf(stacks);
   }

   private static ItemStack coatedWeaponStack(ItemStack base, AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode, String processingStyle) {
      ItemStack coated = base.copyWithCount(1);
      CoatingSystem.applyCoating(coated, info, mode, processingStyle);
      return coated;
   }

   private static List<ItemStack> supportedArmorStacks(AlchemyRegistries.EssenceInfo info, ArmorSupportRules.Support support, ArmorSupportMode mode) {
      List<ItemStack> stacks = new ArrayList<>();
      for (ItemStack base : armorBaseStacks()) {
         stacks.add(supportedArmorStack(base, info, support, mode));
      }
      return List.copyOf(stacks);
   }

   private static ItemStack supportedArmorStack(ItemStack base, AlchemyRegistries.EssenceInfo info, ArmorSupportRules.Support support, ArmorSupportMode mode) {
      ItemStack stack = base.copyWithCount(1);
      stack.set(ArmorSupportComponents.ARMOR_SUPPORT, new ArmorSupportComponent(support.id(), info.id(), support.orientation(), support.bufferAmount(), mode));
      return stack;
   }

   private static AlchemyRegistries.EssenceInfo sampleEssence() {
      return AlchemyRegistries.essenceInfoById("dandelion").orElseGet(() -> AlchemyRegistries.essenceInfos().getFirst());
   }

   private static List<ProcessedCoatingRules.Style> processedStyles() {
      return ProcessedCoatingRules.styles().stream().filter(style -> style.implemented() && !"base".equals(style.id())).toList();
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
      ItemStack stack = new ItemStack(item);
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
      return stack;
   }

   private static ItemStack processedPotionStack(Item item, Holder<Potion> potion) {
      ItemStack stack = potionStack(item, potion);
      AdvancedPotionStackInitializer.initializeIfNeeded(stack, RandomSource.create(0L));
      return stack;
   }

   private static List<Item> potionBottleItems() {
      return List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("herbcraft", path);
   }

   private static String itemKey(Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return (id.getNamespace() + "_" + id.getPath()).replace('/', '_');
   }
}
