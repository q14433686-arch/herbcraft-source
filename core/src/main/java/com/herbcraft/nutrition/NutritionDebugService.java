package com.herbcraft.nutrition;

import java.util.Map;
import net.minecraft.server.level.ServerPlayer;

/** Debug-only nutrition state mutation helpers. */
public final class NutritionDebugService {
   private NutritionDebugService() {
   }

   public static void setNutritionForDebug(ServerPlayer player, String dimension, int value) {
      PlayerNutritionState state = NutritionManager.nutritionState(player);
      if ("all".equals(dimension)) {
         for (String dim : NutritionProfile.DIMENSIONS) {
            state.setDirect(dim, value);
         }
      } else {
         state.setDirect(dimension, value);
      }
      NutritionRuntimeService.clearDietaryImbalanceRelief(player);
      NutritionRuntimeService.evaluateAndApply(player, state);
   }

   public static void applyNutritionPresetForDebug(ServerPlayer player, String preset) {
      PlayerNutritionState state = NutritionManager.nutritionState(player);
      for (String dim : NutritionProfile.DIMENSIONS) {
         state.setDirect(dim, 500);
      }
      switch (preset) {
         case "starved" -> {
            for (String dim : NutritionProfile.DIMENSIONS) state.setDirect(dim, 0);
         }
         case "balanced" -> {
            for (String dim : NutritionProfile.DIMENSIONS) state.setDirect(dim, 500);
         }
         case "grain_low" -> state.setDirect("grain", 0);
         case "flesh_low" -> state.setDirect("flesh", 0);
         case "oil_low" -> state.setDirect("oil", 0);
         case "vege_low" -> state.setDirect("vege", 0);
         case "fruit_low" -> state.setDirect("fruit", 0);
         case "imbalanced_grain" -> setImbalancedPreset(state, "grain");
         case "imbalanced_flesh" -> setImbalancedPreset(state, "flesh");
         case "imbalanced_oil" -> setImbalancedPreset(state, "oil");
         case "imbalanced_vege" -> setImbalancedPreset(state, "vege");
         case "imbalanced_fruit" -> setImbalancedPreset(state, "fruit");
         case "excess_flesh" -> state.setDirect("flesh", 900);
         case "excess_oil" -> state.setDirect("oil", 900);
         case "excess_fruit", "excess_fruit_nonpunitive" -> state.setDirect("fruit", 900);
         case "fluid_surplus_pair" -> {
            state.setDirect("flesh", 900);
            state.setDirect("vege", 900);
            state.setDirect("fruit", 900);
         }
         case "fluid_surplus_single" -> {
            state.setDirect("flesh", 900);
            state.setDirect("fruit", 900);
         }
         case "fluid_surplus_unrelated" -> {
            state.setDirect("flesh", 900);
            state.setDirect("oil", 900);
            state.setDirect("fruit", 900);
         }
         case "correcting_flesh" -> {
            state.setDirect("flesh", 900);
            state.setDirect("grain", 550);
            state.setDirect("oil", 500);
            state.setDirect("vege", 500);
            state.setDirect("fruit", 550);
            seedCorrectionMeal(player, state, Map.of("vege", 80, "fruit", 30));
         }
         case "correcting_oil" -> {
            state.setDirect("oil", 900);
            state.setDirect("grain", 500);
            state.setDirect("flesh", 550);
            state.setDirect("vege", 500);
            state.setDirect("fruit", 550);
            seedCorrectionMeal(player, state, Map.of("vege", 60, "grain", 30));
         }
         case "correcting_grain" -> {
            state.setDirect("grain", 900);
            state.setDirect("flesh", 550);
            state.setDirect("oil", 500);
            state.setDirect("vege", 500);
            state.setDirect("fruit", 550);
            seedCorrectionMeal(player, state, Map.of("vege", 80, "fruit", 30));
         }
         default -> {
         }
      }
      NutritionRuntimeService.clearDietaryImbalanceRelief(player);
      NutritionRuntimeService.evaluateAndApply(player, state);
   }

   private static void seedCorrectionMeal(ServerPlayer player, PlayerNutritionState state, Map<String, Integer> values) {
      state.recordMeal(player.level().getGameTime(), "herbcraft:debug_corrective_meal", new NutritionProfile(Map.copyOf(values)));
   }

   private static void setImbalancedPreset(PlayerNutritionState state, String highDimension) {
      for (String dim : NutritionProfile.DIMENSIONS) {
         state.setDirect(dim, dim.equals(highDimension) ? 950 : 350);
      }
   }
}
