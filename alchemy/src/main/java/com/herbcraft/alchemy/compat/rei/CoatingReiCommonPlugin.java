package com.herbcraft.alchemy.compat.rei;

import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistillationItems;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CoatingReiCommonPlugin implements REICommonPlugin {
   public void registerItemComparators(ItemComparatorRegistry registry) {
      registry.register(this::distilledHash, DistillationItems.DISTILLED_ESSENCE);
      registry.register(this::potionHash, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
   }

   private long distilledHash(ComparisonContext context, ItemStack stack) {
      DistilledEssenceComponent component = stack.get(DistillationComponents.DISTILLED_ESSENCE);
      return component == null ? 0L : Objects.hash(component.sourceEssence(), component.orientation());
   }

   private long potionHash(ComparisonContext context, ItemStack stack) {
      return Objects.hash(
         AdvancedPotionStackInitializer.potionPath(stack).orElse("empty"),
         stack.get(AlchemyPotionComponents.POTION_PROCESSING_STYLE),
         stack.get(AlchemyPotionComponents.POTION_SOURCE)
      );
   }
}
