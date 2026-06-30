package com.herbcraft.pharmacology;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PharmacologyRules {
   private static PharmacologyRules INSTANCE = defaults();

   public final int windowTicks;
   public final int regressionIntervalTicks;
   public final int regressionAmount;
   public final int sameQiThreshold;
   public final float harmonizeNegativeProbabilityMultiplier;
   public final float harmonizeNegativeDurationMultiplier;
   public final float sameQiNegativeProbabilityMultiplier;
   public final float sameQiNegativeDurationMultiplier;
   public final float sweetHealMultiplier;
   public final float bitterImmunityMultiplier;
   public final int baseImmunityTicks;
   public final float pungentPositiveDurationMultiplier;
   public final float pungentNauseaChance;
   public final int pungentNauseaTicks;
   public final float sourPositiveDurationMultiplier;
   public final float sourNegativeDurationMultiplier;
   public final int astringentCooldownTicks;
   public final float saltyImmunityMultiplier;
   public final float saltyWaterDurationMultiplier;
   public final float blandSideEffectProbabilityMultiplier;
   public final int xiangFanWindowTicks;
   public final boolean xiangFanAppliesEffects;
   private final Map<String, Integer> temperatureValues;
   private final List<SevenEmotionRule> sevenEmotionRules;

   private PharmacologyRules(
      int windowTicks,
      int regressionIntervalTicks,
      int regressionAmount,
      int sameQiThreshold,
      float harmonizeNegativeProbabilityMultiplier,
      float harmonizeNegativeDurationMultiplier,
      float sameQiNegativeProbabilityMultiplier,
      float sameQiNegativeDurationMultiplier,
      float sweetHealMultiplier,
      float bitterImmunityMultiplier,
      int baseImmunityTicks,
      float pungentPositiveDurationMultiplier,
      float pungentNauseaChance,
      int pungentNauseaTicks,
      float sourPositiveDurationMultiplier,
      float sourNegativeDurationMultiplier,
      int astringentCooldownTicks,
      float saltyImmunityMultiplier,
      float saltyWaterDurationMultiplier,
      float blandSideEffectProbabilityMultiplier,
      int xiangFanWindowTicks,
      boolean xiangFanAppliesEffects,
      Map<String, Integer> temperatureValues,
      List<SevenEmotionRule> sevenEmotionRules
   ) {
      this.windowTicks = windowTicks;
      this.regressionIntervalTicks = regressionIntervalTicks;
      this.regressionAmount = regressionAmount;
      this.sameQiThreshold = sameQiThreshold;
      this.harmonizeNegativeProbabilityMultiplier = harmonizeNegativeProbabilityMultiplier;
      this.harmonizeNegativeDurationMultiplier = harmonizeNegativeDurationMultiplier;
      this.sameQiNegativeProbabilityMultiplier = sameQiNegativeProbabilityMultiplier;
      this.sameQiNegativeDurationMultiplier = sameQiNegativeDurationMultiplier;
      this.sweetHealMultiplier = sweetHealMultiplier;
      this.bitterImmunityMultiplier = bitterImmunityMultiplier;
      this.baseImmunityTicks = baseImmunityTicks;
      this.pungentPositiveDurationMultiplier = pungentPositiveDurationMultiplier;
      this.pungentNauseaChance = pungentNauseaChance;
      this.pungentNauseaTicks = pungentNauseaTicks;
      this.sourPositiveDurationMultiplier = sourPositiveDurationMultiplier;
      this.sourNegativeDurationMultiplier = sourNegativeDurationMultiplier;
      this.astringentCooldownTicks = astringentCooldownTicks;
      this.saltyImmunityMultiplier = saltyImmunityMultiplier;
      this.saltyWaterDurationMultiplier = saltyWaterDurationMultiplier;
      this.blandSideEffectProbabilityMultiplier = blandSideEffectProbabilityMultiplier;
      this.xiangFanWindowTicks = xiangFanWindowTicks;
      this.xiangFanAppliesEffects = xiangFanAppliesEffects;
      this.temperatureValues = Map.copyOf(temperatureValues);
      this.sevenEmotionRules = List.copyOf(sevenEmotionRules);
   }

   public static PharmacologyRules current() {
      return INSTANCE;
   }

   public int temperatureValue(String temperature) {
      return this.temperatureValues.getOrDefault(temperature, 0);
   }

   public Map<String, Integer> temperatureValues() {
      return this.temperatureValues;
   }

   public List<SevenEmotionRule> sevenEmotionRules() {
      return this.sevenEmotionRules;
   }

   public static void loadFromBundledJson() {
      INSTANCE = defaults();
      try (InputStream input = PharmacologyRules.class.getClassLoader().getResourceAsStream("data/herbcraft/pharmacology_rules.json")) {
         if (input == null) {
            return;
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         INSTANCE = new PharmacologyRules(
            intValue(root, "window_ticks", INSTANCE.windowTicks),
            intValue(root, "qi_regression_interval_ticks", INSTANCE.regressionIntervalTicks),
            intValue(root, "qi_regression_amount", INSTANCE.regressionAmount),
            intValue(root, "same_qi_threshold", INSTANCE.sameQiThreshold),
            floatValue(root, "harmonize_negative_probability_multiplier", INSTANCE.harmonizeNegativeProbabilityMultiplier),
            floatValue(root, "harmonize_negative_duration_multiplier", INSTANCE.harmonizeNegativeDurationMultiplier),
            floatValue(root, "same_qi_negative_probability_multiplier", INSTANCE.sameQiNegativeProbabilityMultiplier),
            floatValue(root, "same_qi_negative_duration_multiplier", INSTANCE.sameQiNegativeDurationMultiplier),
            floatValue(root, "sweet_heal_multiplier", INSTANCE.sweetHealMultiplier),
            floatValue(root, "bitter_immunity_multiplier", INSTANCE.bitterImmunityMultiplier),
            intValue(root, "base_immunity_ticks", INSTANCE.baseImmunityTicks),
            floatValue(root, "pungent_positive_duration_multiplier", INSTANCE.pungentPositiveDurationMultiplier),
            floatValue(root, "pungent_nausea_chance", INSTANCE.pungentNauseaChance),
            intValue(root, "pungent_nausea_ticks", INSTANCE.pungentNauseaTicks),
            floatValue(root, "sour_positive_duration_multiplier", INSTANCE.sourPositiveDurationMultiplier),
            floatValue(root, "sour_negative_duration_multiplier", INSTANCE.sourNegativeDurationMultiplier),
            intValue(root, "astringent_cooldown_ticks", INSTANCE.astringentCooldownTicks),
            floatValue(root, "salty_immunity_multiplier", INSTANCE.saltyImmunityMultiplier),
            floatValue(root, "salty_water_duration_multiplier", INSTANCE.saltyWaterDurationMultiplier),
            floatValue(root, "bland_side_effect_probability_multiplier", INSTANCE.blandSideEffectProbabilityMultiplier),
            intValue(root, "xiang_fan_window_ticks", INSTANCE.xiangFanWindowTicks),
            boolValue(root, "xiang_fan_applies_effects", INSTANCE.xiangFanAppliesEffects),
            readTemperatureValues(root, INSTANCE.temperatureValues),
            readSevenEmotionRules(root, INSTANCE.sevenEmotionRules)
         );
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load pharmacology_rules.json; using defaults", e);
      }
   }

   private static PharmacologyRules defaults() {
      return new PharmacologyRules(
         600, 600, 5, 50, 0.7F, 0.75F, 1.3F, 1.25F, 1.15F, 1.2F, 600, 1.15F, 0.08F, 60, 1.1F, 0.9F, 600,
         1.15F, 1.1F, 0.85F, 200, true, defaultTemperatureValues(), defaultSevenEmotionRules()
      );
   }

   private static Map<String, Integer> defaultTemperatureValues() {
      Map<String, Integer> values = new LinkedHashMap<>();
      values.put("cold", -15);
      values.put("cool", -8);
      values.put("neutral", 0);
      values.put("warm", 8);
      values.put("hot", 15);
      return values;
   }

   private static List<SevenEmotionRule> defaultSevenEmotionRules() {
      return List.of(
         new SevenEmotionRule(SevenEmotionType.XIANG_FAN, true, 200, List.of("toxic"), List.of(), List.of(), List.of(), List.of(), List.of(), 0, false, false),
         new SevenEmotionRule(SevenEmotionType.XIANG_SHA, false, -1, List.of(), List.of("clearing"), List.of("toxic"), List.of(), List.of(), List.of(), 0, false, false),
         new SevenEmotionRule(SevenEmotionType.XIANG_WEI, false, -1, List.of(), List.of("toxic"), List.of("clearing"), List.of(), List.of(), List.of(), 0, false, false),
         new SevenEmotionRule(SevenEmotionType.XIANG_E, false, -1, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(new TraitConflict("stirring", "sedating")), 0, false, false),
         new SevenEmotionRule(SevenEmotionType.XIANG_XU, false, -1, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), 2, false, false),
         new SevenEmotionRule(SevenEmotionType.XIANG_SHI, false, -1, List.of(), List.of(), List.of(), List.of("nourishing", "clearing", "sedating"), List.of("stirring", "clearing", "nourishing"), List.of(), 0, true, false),
         new SevenEmotionRule(SevenEmotionType.SINGLE, false, -1, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), 0, false, true)
      );
   }

   private static Map<String, Integer> readTemperatureValues(JsonObject root, Map<String, Integer> fallback) {
      Map<String, Integer> values = new LinkedHashMap<>(fallback);
      JsonObject json = root.getAsJsonObject("temperature_values");
      if (json != null) {
         for (String key : List.of("cold", "cool", "neutral", "warm", "hot")) {
            if (json.has(key)) {
               values.put(key, json.get(key).getAsInt());
            }
         }
      }
      return values;
   }

   private static List<SevenEmotionRule> readSevenEmotionRules(JsonObject root, List<SevenEmotionRule> fallback) {
      JsonArray array = root.getAsJsonArray("seven_emotion_rules");
      if (array == null) {
         return fallback;
      }
      List<SevenEmotionRule> rules = new ArrayList<>();
      for (JsonElement element : array) {
         if (!element.isJsonObject()) {
            continue;
         }
         JsonObject json = element.getAsJsonObject();
         SevenEmotionType type = emotionType(stringValue(json, "type", ""));
         if (type == null) {
            continue;
         }
         rules.add(new SevenEmotionRule(
            type,
            boolValue(json, "hot_cold_conflict", false),
            intValue(json, "within_ticks", -1),
            stringList(json, "either_traits"),
            stringList(json, "current_traits"),
            stringList(json, "previous_traits"),
            stringList(json, "current_any_traits"),
            stringList(json, "previous_any_traits"),
            traitConflicts(json),
            intValue(json, "shared_traits_min", 0),
            boolValue(json, "not_same_profile", false),
            boolValue(json, "single_no_shared_with_history", false)
         ));
      }
      return rules.isEmpty() ? fallback : rules;
   }

   private static SevenEmotionType emotionType(String value) {
      return switch (value) {
         case "xiang_fan" -> SevenEmotionType.XIANG_FAN;
         case "xiang_sha" -> SevenEmotionType.XIANG_SHA;
         case "xiang_wei" -> SevenEmotionType.XIANG_WEI;
         case "xiang_e" -> SevenEmotionType.XIANG_E;
         case "xiang_xu" -> SevenEmotionType.XIANG_XU;
         case "xiang_shi" -> SevenEmotionType.XIANG_SHI;
         case "single" -> SevenEmotionType.SINGLE;
         default -> null;
      };
   }

   private static List<String> stringList(JsonObject root, String key) {
      JsonArray array = root.getAsJsonArray(key);
      if (array == null) {
         return List.of();
      }
      List<String> values = new ArrayList<>();
      for (JsonElement element : array) {
         values.add(element.getAsString());
      }
      return List.copyOf(values);
   }

   private static List<TraitConflict> traitConflicts(JsonObject root) {
      JsonArray array = root.getAsJsonArray("trait_conflicts");
      if (array == null) {
         return List.of();
      }
      List<TraitConflict> conflicts = new ArrayList<>();
      for (JsonElement element : array) {
         if (element.isJsonArray()) {
            JsonArray pair = element.getAsJsonArray();
            if (pair.size() >= 2) {
               conflicts.add(new TraitConflict(pair.get(0).getAsString(), pair.get(1).getAsString()));
            }
         }
      }
      return List.copyOf(conflicts);
   }

   private static String stringValue(JsonObject root, String key, String fallback) {
      return root.has(key) ? root.get(key).getAsString() : fallback;
   }

   private static int intValue(JsonObject root, String key, int fallback) {
      return root.has(key) ? root.get(key).getAsInt() : fallback;
   }

   private static float floatValue(JsonObject root, String key, float fallback) {
      return root.has(key) ? root.get(key).getAsFloat() : fallback;
   }

   private static boolean boolValue(JsonObject root, String key, boolean fallback) {
      return root.has(key) ? root.get(key).getAsBoolean() : fallback;
   }

   public record SevenEmotionRule(
      SevenEmotionType type,
      boolean hotColdConflict,
      int withinTicks,
      List<String> eitherTraits,
      List<String> currentTraits,
      List<String> previousTraits,
      List<String> currentAnyTraits,
      List<String> previousAnyTraits,
      List<TraitConflict> traitConflicts,
      int sharedTraitsMin,
      boolean notSameProfile,
      boolean singleNoSharedWithHistory
   ) {
      public SevenEmotionRule {
         eitherTraits = List.copyOf(eitherTraits);
         currentTraits = List.copyOf(currentTraits);
         previousTraits = List.copyOf(previousTraits);
         currentAnyTraits = List.copyOf(currentAnyTraits);
         previousAnyTraits = List.copyOf(previousAnyTraits);
         traitConflicts = List.copyOf(traitConflicts);
      }
   }

   public record TraitConflict(String first, String second) {
   }
}
