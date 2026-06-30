package com.herbcraft.nutrition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NutritionRules {
   private static NutritionRules INSTANCE = defaults();

   public final int maxValue;
   public final int initialValue;
   public final int decayIntervalTicks;
   public final int decayAmount;
   public final int checkIntervalTicks;
   public final int balancedMin;
   public final int balancedMax;
   public final int balancedSpread;
   public final int malnutritionLow;
   public final int imbalanceHigh;
   public final int imbalanceSpread;
   public final int badStateGraceChecks;
   public final int warningCooldownTicks;
   public final float minorDebuffChance;
   public final int minorDebuffDurationTicks;
   public final int abundanceThreshold;
   public final int fullBalanceMin;
   public final int fullBalanceMax;
   public final int recentMealLimit;
   public final int recentMealWindowTicks;
   public final int correctionRecentWindowTicks;
   public final int correctionHighThreshold;
   public final int correctionTargetValue;
   public final int correctionScoreThreshold;
   public final int correctionExtraDecayIntervalTicks;
   public final int correctionExtraDecayMin;
   public final int correctionExtraDecayMax;
   public final boolean fruitHighNonPunitive;
   public final int fluidSurplusThreshold;
   public final int fluidSurplusTargetValue;
   public final int fluidSurplusDecayIntervalTicks;
   public final int fluidSurplusDecayAmount;
   public final float selfPenaltyMultiplier;
   private final Map<String, Map<String, Float>> correctionWeights;

   private NutritionRules(int maxValue, int initialValue, int decayIntervalTicks, int decayAmount,
                           int checkIntervalTicks, int balancedMin, int balancedMax, int balancedSpread,
                           int malnutritionLow, int imbalanceHigh, int imbalanceSpread,
                           int badStateGraceChecks, int warningCooldownTicks,
                           float minorDebuffChance, int minorDebuffDurationTicks,
                           int abundanceThreshold, int fullBalanceMin, int fullBalanceMax,
                           int recentMealLimit, int recentMealWindowTicks, int correctionRecentWindowTicks,
                           int correctionHighThreshold, int correctionTargetValue, int correctionScoreThreshold,
                           int correctionExtraDecayIntervalTicks, int correctionExtraDecayMin, int correctionExtraDecayMax,
                           boolean fruitHighNonPunitive, int fluidSurplusThreshold, int fluidSurplusTargetValue,
                           int fluidSurplusDecayIntervalTicks, int fluidSurplusDecayAmount,
                           float selfPenaltyMultiplier, Map<String, Map<String, Float>> correctionWeights) {
      this.maxValue = maxValue;
      this.initialValue = initialValue;
      this.decayIntervalTicks = decayIntervalTicks;
      this.decayAmount = decayAmount;
      this.checkIntervalTicks = checkIntervalTicks;
      this.balancedMin = balancedMin;
      this.balancedMax = balancedMax;
      this.balancedSpread = balancedSpread;
      this.malnutritionLow = malnutritionLow;
      this.imbalanceHigh = imbalanceHigh;
      this.imbalanceSpread = imbalanceSpread;
      this.badStateGraceChecks = badStateGraceChecks;
      this.warningCooldownTicks = warningCooldownTicks;
      this.minorDebuffChance = minorDebuffChance;
      this.minorDebuffDurationTicks = minorDebuffDurationTicks;
      this.abundanceThreshold = abundanceThreshold;
      this.fullBalanceMin = fullBalanceMin;
      this.fullBalanceMax = fullBalanceMax;
      this.recentMealLimit = recentMealLimit;
      this.recentMealWindowTicks = recentMealWindowTicks;
      this.correctionRecentWindowTicks = correctionRecentWindowTicks;
      this.correctionHighThreshold = correctionHighThreshold;
      this.correctionTargetValue = correctionTargetValue;
      this.correctionScoreThreshold = correctionScoreThreshold;
      this.correctionExtraDecayIntervalTicks = correctionExtraDecayIntervalTicks;
      this.correctionExtraDecayMin = correctionExtraDecayMin;
      this.correctionExtraDecayMax = correctionExtraDecayMax;
      this.fruitHighNonPunitive = fruitHighNonPunitive;
      this.fluidSurplusThreshold = fluidSurplusThreshold;
      this.fluidSurplusTargetValue = fluidSurplusTargetValue;
      this.fluidSurplusDecayIntervalTicks = fluidSurplusDecayIntervalTicks;
      this.fluidSurplusDecayAmount = fluidSurplusDecayAmount;
      this.selfPenaltyMultiplier = selfPenaltyMultiplier;
      this.correctionWeights = copyWeights(correctionWeights);
   }

   public static NutritionRules current() { return INSTANCE; }

   public float correctionWeight(String highDimension, String correctiveDimension) {
      return correctionWeights.getOrDefault(highDimension, Map.of()).getOrDefault(correctiveDimension, 0.0F);
   }

   public Map<String, Map<String, Float>> correctionWeights() {
      return correctionWeights;
   }

   public static void loadFromBundledJson() {
      INSTANCE = defaults();
      try (InputStream in = NutritionRules.class.getClassLoader()
               .getResourceAsStream("data/herbcraft/nutrition_rules.json")) {
         if (in == null) return;
         JsonObject r = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
         Map<String, Map<String, Float>> weights = loadCorrectionWeights();
         INSTANCE = new NutritionRules(
            i(r,"max_value",1000), i(r,"initial_value",500),
            i(r,"decay_interval_ticks",1200), i(r,"decay_amount",2),
            i(r,"check_interval_ticks",1200), i(r,"balanced_min",350), i(r,"balanced_max",650),
            i(r,"balanced_spread",300), i(r,"malnutrition_low",150),
            i(r,"imbalance_high",850), i(r,"imbalance_spread",550),
            i(r,"bad_state_grace_checks",3), i(r,"warning_cooldown_ticks",6000),
            f(r,"minor_debuff_chance",0.12F), i(r,"minor_debuff_duration_ticks",100),
            i(r,"abundance_threshold",750), i(r,"full_balance_min",400), i(r,"full_balance_max",600),
            i(r,"recent_meal_limit",24), i(r,"recent_meal_window_ticks",7200), i(r,"correction_recent_window_ticks",3600),
            i(r,"correction_high_threshold",750), i(r,"correction_target_value",650), i(r,"correction_score_threshold",60),
            i(r,"correction_extra_decay_interval_ticks",200), i(r,"correction_extra_decay_min",15), i(r,"correction_extra_decay_max",45),
            b(r,"fruit_high_non_punitive",true), i(r,"fluid_surplus_threshold",850), i(r,"fluid_surplus_target_value",700),
            i(r,"fluid_surplus_decay_interval_ticks",200), i(r,"fluid_surplus_decay_amount",10),
            loadSelfPenaltyMultiplier(), weights
         );
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load nutrition_rules.json; using defaults", e);
      }
   }

   private static NutritionRules defaults() {
      return new NutritionRules(1000,500,1200,2,1200,350,650,300,150,850,550,3,6000,0.12F,100,750,400,600,24,7200,3600,750,650,60,200,15,45,true,850,700,200,10,1.0F,defaultCorrectionWeights());
   }

   private static float loadSelfPenaltyMultiplier() {
      try (InputStream input = NutritionRules.class.getClassLoader().getResourceAsStream("data/herbcraft/nutrition_correction_rules.json")) {
         if (input == null) return 1.0F;
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         return f(root, "self_penalty_multiplier", 1.0F);
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load nutrition_correction_rules.json self penalty; using default", e);
         return 1.0F;
      }
   }

   private static Map<String, Map<String, Float>> loadCorrectionWeights() {
      Map<String, Map<String, Float>> weights = defaultCorrectionWeights();
      try (InputStream input = NutritionRules.class.getClassLoader().getResourceAsStream("data/herbcraft/nutrition_correction_rules.json")) {
         if (input == null) return weights;
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonObject matrix = root.getAsJsonObject("matrix");
         if (matrix == null) return weights;
         Map<String, Map<String, Float>> loaded = new LinkedHashMap<>();
         for (String highDim : NutritionProfile.DIMENSIONS) {
            Map<String, Float> row = new LinkedHashMap<>();
            JsonObject rowJson = matrix.getAsJsonObject(highDim);
            if (rowJson != null) {
               for (String dim : NutritionProfile.DIMENSIONS) {
                  if (!dim.equals(highDim) && rowJson.has(dim)) {
                     row.put(dim, rowJson.get(dim).getAsFloat());
                  }
               }
            }
            loaded.put(highDim, Map.copyOf(row));
         }
         Herbcraft.LOGGER.info("Loaded Herbcraft nutrition correction matrix.");
         return loaded;
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load nutrition_correction_rules.json; using default correction matrix", e);
         return weights;
      }
   }

   private static Map<String, Map<String, Float>> defaultCorrectionWeights() {
      Map<String, Map<String, Float>> weights = new LinkedHashMap<>();
      // Fruit weights are 0.0 after 津液 decoupling — kept in sync with nutrition_correction_rules.json
      weights.put("grain", Map.of("vege", 1.35F, "flesh", 1.15F, "oil", 0.45F, "fruit", 0.0F));
      weights.put("flesh", Map.of("vege", 1.60F, "fruit", 0.0F, "grain", 0.0F, "oil", 0.0F));
      weights.put("oil", Map.of("vege", 1.45F, "fruit", 0.0F, "grain", 0.25F, "flesh", 0.0F));
      weights.put("vege", Map.of("grain", 1.20F, "flesh", 1.20F, "oil", 0.50F, "fruit", 0.0F));
      weights.put("fruit", Map.of());
      return weights;
   }

   private static Map<String, Map<String, Float>> copyWeights(Map<String, Map<String, Float>> source) {
      Map<String, Map<String, Float>> copy = new LinkedHashMap<>();
      for (Map.Entry<String, Map<String, Float>> entry : source.entrySet()) {
         copy.put(entry.getKey(), Map.copyOf(entry.getValue()));
      }
      return Map.copyOf(copy);
   }

   private static int i(JsonObject r, String k, int fb) { return r.has(k) ? r.get(k).getAsInt() : fb; }
   private static float f(JsonObject r, String k, float fb) { return r.has(k) ? r.get(k).getAsFloat() : fb; }
   private static boolean b(JsonObject r, String k, boolean fb) { return r.has(k) ? r.get(k).getAsBoolean() : fb; }
}
