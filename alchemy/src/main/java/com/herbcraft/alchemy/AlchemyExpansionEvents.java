package com.herbcraft.alchemy;

import com.herbcraft.alchemy.potion.PotionBurdenComponent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Minimal Alchemy expansion hooks reserved for future distillation, armor support, and advanced
 * coating integration. These hooks are intentionally narrow and do not register content.
 */
public final class AlchemyExpansionEvents {
   private AlchemyExpansionEvents() {
   }

   public static final Event<ProcessingStyleApplied> PROCESSING_STYLE_APPLIED = EventFactory.createArrayBacked(
      ProcessingStyleApplied.class,
      callbacks -> (stack, styleId) -> {
         for (ProcessingStyleApplied callback : callbacks) {
            callback.onProcessingStyleApplied(stack, styleId);
         }
      }
   );

   public static final Event<PotionBurdenApplied> POTION_BURDEN_APPLIED = EventFactory.createArrayBacked(
      PotionBurdenApplied.class,
      callbacks -> (player, stack, burden, appliedBurden, appliedTail) -> {
         for (PotionBurdenApplied callback : callbacks) {
            callback.onPotionBurdenApplied(player, stack, burden, appliedBurden, appliedTail);
         }
      }
   );

   public static final Event<PotionBurdenStageChanged> POTION_BURDEN_STAGE_CHANGED = EventFactory.createArrayBacked(
      PotionBurdenStageChanged.class,
      callbacks -> (player, oldStage, newStage, totalBurden) -> {
         for (PotionBurdenStageChanged callback : callbacks) {
            callback.onPotionBurdenStageChanged(player, oldStage, newStage, totalBurden);
         }
      }
   );

   /** Legacy/simple buffer hook retained for source compatibility: one value buffers active burden and half as much tail burden. */
   public static final Event<PotionBurdenBuffer> POTION_BURDEN_BUFFER = EventFactory.createArrayBacked(
      PotionBurdenBuffer.class,
      callbacks -> (player, stack, burden) -> {
         int buffer = 0;
         for (PotionBurdenBuffer callback : callbacks) {
            buffer += Math.max(0, callback.bufferPotionBurden(player, stack, burden));
         }
         return Math.max(0, buffer);
      }
   );

   /** Detailed internal/support hook for active-vs-tail buffering and consequence mitigation. */
   public static final Event<PotionBurdenBufferDetailed> POTION_BURDEN_BUFFER_DETAILED = EventFactory.createArrayBacked(
      PotionBurdenBufferDetailed.class,
      callbacks -> (player, stack, burden) -> {
         PotionBurdenBufferResult result = PotionBurdenBufferResult.NONE;
         for (PotionBurdenBufferDetailed callback : callbacks) {
            result = result.plus(callback.bufferPotionBurdenDetailed(player, stack, burden));
         }
         return result;
      }
   );

   @FunctionalInterface
   public interface ProcessingStyleApplied {
      void onProcessingStyleApplied(ItemStack stack, String styleId);
   }

   @FunctionalInterface
   public interface PotionBurdenApplied {
      void onPotionBurdenApplied(ServerPlayer player, ItemStack stack, PotionBurdenComponent burden, int appliedBurden, int appliedTail);
   }

   @FunctionalInterface
   public interface PotionBurdenStageChanged {
      void onPotionBurdenStageChanged(ServerPlayer player, String oldStage, String newStage, int totalBurden);
   }

   @FunctionalInterface
   public interface PotionBurdenBuffer {
      int bufferPotionBurden(ServerPlayer player, ItemStack stack, PotionBurdenComponent burden);
   }

   @FunctionalInterface
   public interface PotionBurdenBufferDetailed {
      PotionBurdenBufferResult bufferPotionBurdenDetailed(ServerPlayer player, ItemStack stack, PotionBurdenComponent burden);
   }

   public record PotionBurdenBufferResult(int activeBuffer, int tailBuffer, float consequenceDurationMultiplier, int tailDecayBonus) {
      public static final PotionBurdenBufferResult NONE = new PotionBurdenBufferResult(0, 0, 1.0F, 0);

      public PotionBurdenBufferResult {
         activeBuffer = Math.max(0, activeBuffer);
         tailBuffer = Math.max(0, tailBuffer);
         consequenceDurationMultiplier = Math.max(0.05F, consequenceDurationMultiplier <= 0.0F ? 1.0F : consequenceDurationMultiplier);
         tailDecayBonus = Math.max(0, tailDecayBonus);
      }

      public PotionBurdenBufferResult plus(PotionBurdenBufferResult other) {
         if (other == null) {
            return this;
         }
         return new PotionBurdenBufferResult(
            this.activeBuffer + other.activeBuffer,
            this.tailBuffer + other.tailBuffer,
            Math.min(this.consequenceDurationMultiplier, other.consequenceDurationMultiplier),
            this.tailDecayBonus + other.tailDecayBonus
         );
      }
   }
}
