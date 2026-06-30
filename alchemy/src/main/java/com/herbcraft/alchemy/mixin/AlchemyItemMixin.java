package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.knowledge.AlchemyKnowledgeSupport;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.PotionBurdenRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class AlchemyItemMixin {
   @Inject(method = "finishUsingItem", at = @At("HEAD"))
   private void herbcraftAlchemy$initializeProcessedPotionBeforeUse(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
      if (!level.isClientSide() && entity instanceof ServerPlayer player) {
         AdvancedPotionStackInitializer.initializeIfNeeded(stack, player.getRandom());
      }
   }

   @Inject(method = "finishUsingItem", at = @At("RETURN"))
   private void herbcraftAlchemy$applyPotionBurdenAfterUse(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
      if (!level.isClientSide() && entity instanceof ServerPlayer player) {
         if (AlchemyKnowledgeSupport.isPotionContainer(stack)) {
            AlchemyKnowledgeSupport.markPotionTasted(player, stack);
         }
         PotionBurdenRuntime.onPotionConsumed(player, stack);
      }
   }
}
