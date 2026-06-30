package com.herbcraft.nutrition;

import com.herbcraft.HerbcraftPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Thin public facade for nutrition APIs used by the rest of the mod.
 * Runtime/effect/discovery/debug responsibilities are delegated to focused services.
 */
public final class NutritionManager {
   private NutritionManager() {}

   public static void onFoodConsumed(ServerPlayer player, ItemStack stack) {
      NutritionRegistry.get(stack).ifPresent(profile -> {
         if (!profile.isEmpty()) {
            PlayerNutritionState state = nutritionState(player);
            net.minecraft.resources.Identifier itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            state.recordMeal(player.level().getGameTime(), itemId == null ? "unknown" : itemId.toString(), profile);
            state.addFromProfile(profile);
            NutritionDiscoveryHooks.onFoodConsumed(player, stack, profile);
            NutritionRuntimeService.evaluateAndApply(player, state);
         }
      });
   }

   public static void tick(ServerPlayer player, long gameTime) {
      NutritionRuntimeService.tick(player, gameTime);
   }

   public static void onLogin(ServerPlayer player) {
      NutritionRuntimeService.onLogin(player);
   }

   public static void onDeath(ServerPlayer player) {
      NutritionRuntimeService.onDeath(player);
   }

   public static PlayerNutritionState nutritionState(ServerPlayer player) {
      return ((HerbcraftPlayerData) player).herbcraft$nutritionState();
   }

   public static void relieveDietaryImbalance(ServerPlayer player) {
      NutritionRuntimeService.relieveDietaryImbalance(player);
   }

   public static void setNutritionForDebug(ServerPlayer player, String dimension, int value) {
      NutritionDebugService.setNutritionForDebug(player, dimension, value);
   }

   public static void applyNutritionPresetForDebug(ServerPlayer player, String preset) {
      NutritionDebugService.applyNutritionPresetForDebug(player, preset);
   }

   public static void syncToClient(ServerPlayer player) {
      net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, NutritionSyncPayload.build(player));
   }
}
