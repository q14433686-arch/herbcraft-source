package com.herbcraft.nutrition;

import java.util.Map;

/**
 * Immutable nutrition contribution of a single food item.
 * Dimensions: grain, flesh, oil, vege, fruit.
 * Only non-zero dimensions are stored.
 */
public record NutritionProfile(Map<String, Integer> values) {
   public static final String[] DIMENSIONS = {"grain", "flesh", "oil", "vege", "fruit"};

   public int get(String dimension) {
      return values.getOrDefault(dimension, 0);
   }

   public boolean isEmpty() {
      return values.isEmpty() || values.values().stream().allMatch(v -> v == 0);
   }
}
