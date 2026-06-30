package com.herbcraft.alchemy.potion;

import com.herbcraft.alchemy.AlchemyExpansionEvents;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Item-level burden metadata helpers and consumption-time player burden application. */
public final class PotionBurdenRuntime {
   private PotionBurdenRuntime() {
   }

   public static boolean applyMetadata(ItemStack stack, String styleId) {
      Optional<PotionProcessingRules.Style> style = PotionProcessingRules.style(styleId);
      if (style.isEmpty()) {
         return false;
      }
      PotionBurdenComponent current = stack.get(AlchemyPotionComponents.POTION_BURDEN);
      PotionBurdenComponent next = new PotionBurdenComponent(style.get().burdenProfile(), style.get().burdenWeight(), style.get().tailWeight());
      if (next.equals(current)) {
         return false;
      }
      stack.set(AlchemyPotionComponents.POTION_BURDEN, next);
      return true;
   }

   public static void onPotionConsumed(ServerPlayer player, ItemStack stack) {
      PotionBurdenComponent burden = stack.get(AlchemyPotionComponents.POTION_BURDEN);
      if (burden == null) {
         AdvancedPotionStackInitializer.initializeIfNeeded(stack, player.getRandom());
         burden = stack.get(AlchemyPotionComponents.POTION_BURDEN);
      }
      if (burden != null) {
         PotionRelationRuntime.onPotionConsumed(player, stack);
         PotionBodyStateConsumptionRuntime.onPotionConsumed(player, stack);
         int legacyBuffer = AlchemyExpansionEvents.POTION_BURDEN_BUFFER.invoker().bufferPotionBurden(player, stack, burden);
         AlchemyExpansionEvents.PotionBurdenBufferResult detailedBuffer = AlchemyExpansionEvents.POTION_BURDEN_BUFFER_DETAILED.invoker().bufferPotionBurdenDetailed(player, stack, burden);
         AlchemyExpansionEvents.PotionBurdenBufferResult buffer = detailedBuffer.plus(new AlchemyExpansionEvents.PotionBurdenBufferResult(legacyBuffer, legacyBuffer / 2, 1.0F, 0));
         int active = Math.max(0, burden.burdenWeight() - buffer.activeBuffer());
         int tail = Math.max(0, burden.tailWeight() - buffer.tailBuffer());
         PlayerPotionBurdenState state = ((AlchemyPlayerData)player).herbcraftAlchemy$potionBurdenState();
         state.add(active, tail, PotionProcessingRules.burdenRuntime());
         state.rememberLastPotionContext(stack, burden, buffer);
         AlchemyExpansionEvents.POTION_BURDEN_APPLIED.invoker().onPotionBurdenApplied(player, stack.copy(), burden, active, tail);
      }
   }
}
