package com.herbcraft.alchemy;

import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistillationRules;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/** Read-only queries for the B3 mixed-route distillation foundation. */
public final class AlchemyDistillationAPI {
   private AlchemyDistillationAPI() {
   }

   public static Optional<DistilledEssenceComponent> distilledEssence(ItemStack stack) {
      return Optional.ofNullable(stack.get(DistillationComponents.DISTILLED_ESSENCE));
   }

   public static Optional<DistillationRules.Orientation> orientation(String id) {
      return DistillationRules.orientation(id);
   }

   public static Optional<DistillationRules.Orientation> orientation(ItemStack stack) {
      return distilledEssence(stack).flatMap(component -> DistillationRules.orientation(component.orientation()));
   }
}
