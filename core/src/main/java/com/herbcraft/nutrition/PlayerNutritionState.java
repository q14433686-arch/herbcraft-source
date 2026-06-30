package com.herbcraft.nutrition;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Per-player five-dimension nutrition state. Runtime evaluation is owned by NutritionManager.
 */
public final class PlayerNutritionState {
   private final Map<String, Integer> values = new LinkedHashMap<>();
   private final Deque<PlayerNutritionState.RecentMeal> recentMeals = new ArrayDeque<>();
   private long lastDecayTick = 0;
   private long lastCheckTick = 0;
   private long lastWarningTick = 0;
   private long lastCorrectionDecayTick = 0;
   private long lastFluidSurplusDecayTick = 0;
   private int badStateStreak = 0;

   public PlayerNutritionState() {
      NutritionRules rules = NutritionRules.current();
      for (String dim : NutritionProfile.DIMENSIONS) {
         values.put(dim, rules.initialValue);
      }
   }

   public int get(String dim) { return values.getOrDefault(dim, 0); }

   public void setDirect(String dim, int value) {
      values.put(dim, Math.max(0, Math.min(NutritionRules.current().maxValue, value)));
   }

   public void add(String dim, int amount) {
      NutritionRules rules = NutritionRules.current();
      int current = get(dim);
      values.put(dim, Math.min(rules.maxValue, Math.max(0, current + amount)));
   }

   public void addFromProfile(NutritionProfile profile) {
      for (String dim : NutritionProfile.DIMENSIONS) {
         int val = profile.get(dim);
         if (val > 0) add(dim, val);
      }
   }

   public void recordMeal(long gameTime, String itemId, NutritionProfile profile) {
      if (profile == null || profile.isEmpty()) {
         return;
      }
      Map<String, Integer> mealValues = new LinkedHashMap<>();
      for (String dim : NutritionProfile.DIMENSIONS) {
         int value = profile.get(dim);
         if (value > 0) {
            mealValues.put(dim, value);
         }
      }
      if (mealValues.isEmpty()) {
         return;
      }
      recentMeals.addLast(new RecentMeal(gameTime, itemId == null ? "unknown" : itemId, Map.copyOf(mealValues)));
      pruneRecentMeals(gameTime, NutritionRules.current());
   }

   public boolean tickDecay(long gameTime) {
      NutritionRules rules = NutritionRules.current();
      if (rules.decayIntervalTicks <= 0) return false;
      if (gameTime - lastDecayTick < rules.decayIntervalTicks) return false;
      lastDecayTick = gameTime;
      boolean changed = false;
      for (String dim : NutritionProfile.DIMENSIONS) {
         int current = get(dim);
         if (current > 0) {
            values.put(dim, Math.max(0, current - rules.decayAmount));
            changed = true;
         }
      }
      return changed;
   }

   public boolean tickCorrectionDecay(long gameTime) {
      NutritionRules rules = NutritionRules.current();
      pruneRecentMeals(gameTime, rules);
      Map<String, Integer> originalValues = new LinkedHashMap<>(values);
      Map<String, Integer> decayByDimension = new LinkedHashMap<>();
      Map<String, Integer> targetByDimension = new LinkedHashMap<>();

      boolean matrixActive = false;
      if (rules.correctionExtraDecayIntervalTicks > 0 && gameTime - lastCorrectionDecayTick >= rules.correctionExtraDecayIntervalTicks) {
         for (String dim : highPunitiveDimensions(rules, originalValues)) {
            if (correctionScore(dim, rules, gameTime) >= rules.correctionScoreThreshold && originalValues.getOrDefault(dim, 0) > rules.correctionTargetValue) {
               matrixActive = true;
               addDecay(decayByDimension, targetByDimension, dim, correctionDecayAmount(dim, rules, gameTime), rules.correctionTargetValue);
            }
         }
         if (matrixActive) {
            lastCorrectionDecayTick = gameTime;
         }
      }

      boolean fluidSurplusActive = false;
      if (rules.fluidSurplusDecayIntervalTicks > 0 && gameTime - lastFluidSurplusDecayTick >= rules.fluidSurplusDecayIntervalTicks) {
         fluidSurplusActive = originalValues.getOrDefault("fruit", 0) >= rules.fluidSurplusThreshold;
         if (fluidSurplusActive) {
            // Collect non-fruit dimensions that are above the surplus threshold
            List<String> excessDims = new java.util.ArrayList<>();
            for (String dim : NutritionProfile.DIMENSIONS) {
               if (!"fruit".equals(dim) && originalValues.getOrDefault(dim, 0) > rules.fluidSurplusThreshold) {
                  excessDims.add(dim);
               }
            }
            // Find which excess dimensions have at least one correction-matrix relationship
            // with another excess dimension. Only those participate in fluid surplus decay.
            java.util.Set<String> eligibleDims = new java.util.LinkedHashSet<>();
            for (int i = 0; i < excessDims.size(); i++) {
               for (int j = i + 1; j < excessDims.size(); j++) {
                  String a = excessDims.get(i);
                  String b = excessDims.get(j);
                  if (rules.correctionWeight(a, b) > 0 || rules.correctionWeight(b, a) > 0) {
                     eligibleDims.add(a);
                     eligibleDims.add(b);
                  }
               }
            }
            for (String dim : eligibleDims) {
               addDecay(decayByDimension, targetByDimension, dim, rules.fluidSurplusDecayAmount, rules.fluidSurplusTargetValue);
            }
            fluidSurplusActive = !eligibleDims.isEmpty();
         }
         if (fluidSurplusActive) {
            lastFluidSurplusDecayTick = gameTime;
         }
      }

      boolean changed = false;
      for (Map.Entry<String, Integer> entry : decayByDimension.entrySet()) {
         String dim = entry.getKey();
         int original = originalValues.getOrDefault(dim, 0);
         int target = targetByDimension.getOrDefault(dim, 0);
         int updated = Math.max(target, original - entry.getValue());
         if (updated != original) {
            setDirect(dim, updated);
            changed = true;
         }
      }
      return changed;
   }

   /** Check if enough time has passed for a status evaluation. */
   public boolean shouldCheck(long gameTime) {
      NutritionRules rules = NutritionRules.current();
      if (rules.checkIntervalTicks <= 0) return false;
      if (gameTime - lastCheckTick < rules.checkIntervalTicks) return false;
      lastCheckTick = gameTime;
      return true;
   }

   public void incrementBadStreak() { badStateStreak++; }
   public void clearBadStreak() { badStateStreak = 0; }

   public boolean shouldApplyDebuff() {
      return badStateStreak >= NutritionRules.current().badStateGraceChecks;
   }

   public boolean canWarn(long gameTime) {
      NutritionRules rules = NutritionRules.current();
      if (gameTime - lastWarningTick < rules.warningCooldownTicks) return false;
      lastWarningTick = gameTime;
      return true;
   }

   public List<String> highPunitiveDimensions(NutritionRules rules) {
      return highPunitiveDimensions(rules, values);
   }

   private List<String> highPunitiveDimensions(NutritionRules rules, Map<String, Integer> sourceValues) {
      return java.util.Arrays.stream(NutritionProfile.DIMENSIONS)
         .filter(dim -> !(rules.fruitHighNonPunitive && "fruit".equals(dim)))
         .filter(dim -> sourceValues.getOrDefault(dim, 0) >= rules.correctionHighThreshold)
         .toList();
   }

   public boolean hasPunitiveImbalance(NutritionRules rules) {
      for (String highDim : highPunitiveDimensions(rules)) {
         int high = get(highDim);
         int min = Integer.MAX_VALUE;
         for (String dim : NutritionProfile.DIMENSIONS) {
            if (!dim.equals(highDim)) {
               min = Math.min(min, get(dim));
            }
         }
         if (high - min > rules.imbalanceSpread && high > rules.imbalanceHigh) {
            return true;
         }
      }
      return false;
   }

   public boolean isCorrectionActive(NutritionRules rules, long gameTime) {
      return isMatrixCorrectionActive(rules, gameTime) || isFluidSurplusActive(rules);
   }

   public boolean isMatrixCorrectionActive(NutritionRules rules, long gameTime) {
      pruneRecentMeals(gameTime, rules);
      for (String highDim : highPunitiveDimensions(rules)) {
         if (correctionScore(highDim, rules, gameTime) >= rules.correctionScoreThreshold && get(highDim) > rules.correctionTargetValue) {
            return true;
         }
      }
      return false;
   }

   public boolean isFluidSurplusActive(NutritionRules rules) {
      if (get("fruit") < rules.fluidSurplusThreshold) {
         return false;
      }
      // Collect non-fruit excess dimensions, then check for correction-matrix relationships
      java.util.List<String> excessDims = new java.util.ArrayList<>();
      for (String dim : NutritionProfile.DIMENSIONS) {
         if (!"fruit".equals(dim) && get(dim) > rules.fluidSurplusThreshold) {
            excessDims.add(dim);
         }
      }
      for (int i = 0; i < excessDims.size(); i++) {
         for (int j = i + 1; j < excessDims.size(); j++) {
            String a = excessDims.get(i);
            String b = excessDims.get(j);
            if (rules.correctionWeight(a, b) > 0 || rules.correctionWeight(b, a) > 0) {
               return true;
            }
         }
      }
      return false;
   }

   public int correctionScore(String highDim, NutritionRules rules, long gameTime) {
      pruneRecentMeals(gameTime, rules);
      float score = 0.0F;
      long cutoff = gameTime - rules.correctionRecentWindowTicks;
      for (RecentMeal meal : recentMeals) {
         if (meal.gameTime() < cutoff) {
            continue;
         }
         float corrective = 0.0F;
         for (String dim : NutritionProfile.DIMENSIONS) {
            if (!dim.equals(highDim)) {
               corrective += meal.values().getOrDefault(dim, 0) * rules.correctionWeight(highDim, dim);
            }
         }
         // A meal dominated by the already-excessive dimension should not count as corrective
         // just because it has trace secondary nutrition. Mixed dishes count when their
         // weighted corrective side outweighs the excessive side.
         if (corrective > meal.values().getOrDefault(highDim, 0) * rules.selfPenaltyMultiplier) {
            score += corrective;
         }
      }
      return Math.round(score);
   }

   public int correctionDecayAmount(String highDim, NutritionRules rules, long gameTime) {
      int score = correctionScore(highDim, rules, gameTime);
      int amount = rules.correctionExtraDecayMin + score / 12;
      int base = Math.max(rules.correctionExtraDecayMin, Math.min(rules.correctionExtraDecayMax, amount));
      // In the correctionHighThreshold..imbalanceHigh-1 "elevated but not excess" range,
      // use half decay rate so correction is noticeable but not as aggressive as the excess range.
      int dimValue = get(highDim);
      if (dimValue < rules.imbalanceHigh) {
         base = Math.max(1, base / 2);
      }
      return base;
   }

   private void addDecay(Map<String, Integer> decayByDimension, Map<String, Integer> targetByDimension, String dim, int amount, int target) {
      if (amount <= 0) {
         return;
      }
      decayByDimension.merge(dim, amount, Integer::sum);
      targetByDimension.merge(dim, target, Math::min);
   }

   public void pruneRecentMeals(long gameTime, NutritionRules rules) {
      if (rules.recentMealWindowTicks > 0) {
         long cutoff = gameTime - rules.recentMealWindowTicks;
         while (!recentMeals.isEmpty() && recentMeals.peekFirst().gameTime() < cutoff) {
            recentMeals.removeFirst();
         }
      }
      while (rules.recentMealLimit > 0 && recentMeals.size() > rules.recentMealLimit) {
         recentMeals.removeFirst();
      }
   }

   public String encodeRecentMeals() {
      StringJoiner joiner = new StringJoiner(";");
      for (RecentMeal meal : recentMeals) {
         StringBuilder builder = new StringBuilder();
         builder.append(meal.gameTime()).append(',').append(meal.itemId());
         for (String dim : NutritionProfile.DIMENSIONS) {
            builder.append(',').append(meal.values().getOrDefault(dim, 0));
         }
         joiner.add(builder.toString());
      }
      return joiner.toString();
   }

   public void decodeRecentMeals(String encoded) {
      recentMeals.clear();
      if (encoded == null || encoded.isBlank()) {
         return;
      }
      for (String entry : encoded.split(";")) {
         String[] parts = entry.split(",");
         if (parts.length != 2 + NutritionProfile.DIMENSIONS.length) {
            continue;
         }
         try {
            long gameTime = Long.parseLong(parts[0]);
            String itemId = parts[1];
            Map<String, Integer> mealValues = new LinkedHashMap<>();
            for (int i = 0; i < NutritionProfile.DIMENSIONS.length; i++) {
               int value = Integer.parseInt(parts[2 + i]);
               if (value > 0) {
                  mealValues.put(NutritionProfile.DIMENSIONS[i], value);
               }
            }
            if (!mealValues.isEmpty()) {
               recentMeals.addLast(new RecentMeal(gameTime, itemId, Map.copyOf(mealValues)));
            }
         } catch (Exception ignored) {
         }
      }
      NutritionRules rules = NutritionRules.current();
      while (rules.recentMealLimit > 0 && recentMeals.size() > rules.recentMealLimit) {
         recentMeals.removeFirst();
      }
   }

   public int recentMealCount() {
      return recentMeals.size();
   }

   public void resetOnDeath() {
      NutritionRules rules = NutritionRules.current();
      for (String dim : NutritionProfile.DIMENSIONS) {
         values.put(dim, rules.initialValue);
      }
      recentMeals.clear();
      badStateStreak = 0;
      lastCorrectionDecayTick = 0;
      lastFluidSurplusDecayTick = 0;
   }

   public Map<String, Integer> snapshot() { return Map.copyOf(values); }

   public record RecentMeal(long gameTime, String itemId, Map<String, Integer> values) {
   }
}
