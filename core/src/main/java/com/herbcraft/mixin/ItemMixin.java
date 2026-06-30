package com.herbcraft.mixin;

import com.herbcraft.Herbcraft;
import com.herbcraft.nutrition.NutritionManager;
import com.herbcraft.pharmacology.PharmacologyResolver;
import com.herbcraft.registry.HerbFoodRegistry;
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
public abstract class ItemMixin {
   @Inject(method = "finishUsingItem", at = @At("RETURN"))
   private void herbcraft$applyCustomFoodLogicOnlyAfterFinished(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
      boolean herbcraftHerb = HerbFoodRegistry.get(stack.getItem()) != null;
      if (herbcraftHerb) {
         Herbcraft.applyAfterConsume(stack, level, entity);
      }
      // Nutrition recording for ALL foods (Herbcraft + vanilla)
      if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {
         NutritionManager.onFoodConsumed(serverPlayer, stack);
         if (!herbcraftHerb) {
            PharmacologyResolver.resolveFoodNatureOnly(stack, serverPlayer, level.getGameTime());
         }
      }
   }

   @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
   private void herbcraft$releaseShortUseProjectile(ItemStack stack, Level level, LivingEntity entity, int timeLeft, CallbackInfoReturnable<Boolean> cir) {
      if (Herbcraft.isShortUseProjectile(stack)) {
         cir.setReturnValue(Herbcraft.releaseShortUseProjectile(stack, level, entity, timeLeft));
      }
   }
}
