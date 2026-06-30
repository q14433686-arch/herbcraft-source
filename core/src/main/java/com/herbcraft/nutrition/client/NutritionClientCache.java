package com.herbcraft.nutrition.client;

import java.util.Map;

public final class NutritionClientCache {
   private static Map<String, Integer> values = Map.of();
   private static int statusCode = 3; // 0=starved,1=malnourished,2=imbalanced,3=normal,4=abundant,5=balanced,6=correcting

   private static final String[] STATUS_KEYS = {
      "nutrition.herbcraft.state.starved",
      "nutrition.herbcraft.state.malnourished",
      "nutrition.herbcraft.state.imbalanced",
      "nutrition.herbcraft.state.normal",
      "nutrition.herbcraft.state.abundant",
      "nutrition.herbcraft.state.balanced",
      "nutrition.herbcraft.state.correcting"
   };

   private NutritionClientCache() {}

   public static void update(Map<String, Integer> newValues, int code) {
      values = Map.copyOf(newValues);
      statusCode = code;
   }

   public static int get(String dim) { return values.getOrDefault(dim, 0); }
   public static int statusCode() { return statusCode; }
   public static String statusTranslationKey() {
      return statusCode >= 0 && statusCode < STATUS_KEYS.length ? STATUS_KEYS[statusCode] : STATUS_KEYS[3];
   }
   public static boolean isBad() { return statusCode <= 2; }
   public static boolean isGood() { return statusCode >= 4; }
   public static boolean isCorrecting() { return statusCode == 6; }
}
