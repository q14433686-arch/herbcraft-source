package com.herbcraft.cuisine.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.cuisine.HerbcraftCuisine;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class HodgepodgeRules {
   private static HodgepodgeRules INSTANCE = defaults();

   public final int minHerbs;
   public final int maxHerbs;
   public final int normalMaxHerbs;
   public final int nutritionCap;
   public final float chanceBonus;
   public final int maxStackGain;
   public final int floralBonusMinCount;
   public final float clashBaseChance;
   public final float clashToxicBonus;
   public final float clashClearingReduction;
   public final float clashBlandReduction;
   public final float clashMinChance;
   public final float clashMaxChance;
   public final int clashNauseaSeconds;
   public final float clashToxicWithoutClearingDurationMultiplier;

   private HodgepodgeRules(
      int minHerbs,
      int maxHerbs,
      int normalMaxHerbs,
      int nutritionCap,
      float chanceBonus,
      int maxStackGain,
      int floralBonusMinCount,
      float clashBaseChance,
      float clashToxicBonus,
      float clashClearingReduction,
      float clashBlandReduction,
      float clashMinChance,
      float clashMaxChance,
      int clashNauseaSeconds,
      float clashToxicWithoutClearingDurationMultiplier
   ) {
      this.minHerbs = minHerbs;
      this.maxHerbs = maxHerbs;
      this.normalMaxHerbs = normalMaxHerbs;
      this.nutritionCap = nutritionCap;
      this.chanceBonus = chanceBonus;
      this.maxStackGain = maxStackGain;
      this.floralBonusMinCount = floralBonusMinCount;
      this.clashBaseChance = clashBaseChance;
      this.clashToxicBonus = clashToxicBonus;
      this.clashClearingReduction = clashClearingReduction;
      this.clashBlandReduction = clashBlandReduction;
      this.clashMinChance = clashMinChance;
      this.clashMaxChance = clashMaxChance;
      this.clashNauseaSeconds = clashNauseaSeconds;
      this.clashToxicWithoutClearingDurationMultiplier = clashToxicWithoutClearingDurationMultiplier;
   }

   public static HodgepodgeRules current() {
      return INSTANCE;
   }

   public static void loadFromBundledJson() {
      INSTANCE = defaults();
      try (InputStream input = HodgepodgeRules.class.getClassLoader().getResourceAsStream("data/herbcraft_cuisine/hodgepodge_rules.json")) {
         if (input == null) {
            return;
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonObject clash = root.has("clash") ? root.getAsJsonObject("clash") : new JsonObject();
         INSTANCE = new HodgepodgeRules(
            i(root, "min_herbs", 3),
            i(root, "max_herbs", 7),
            i(root, "normal_max_herbs", 4),
            i(root, "nutrition_cap", 10),
            f(root, "chance_bonus", 0.15F),
            i(root, "max_stack_gain", 3),
            i(root, "floral_bonus_min_count", 2),
            f(clash, "base_chance", 0.30F),
            f(clash, "toxic_bonus", 0.10F),
            f(clash, "clearing_reduction", 0.15F),
            f(clash, "bland_reduction", 0.10F),
            f(clash, "min_chance", 0.0F),
            f(clash, "max_chance", 0.80F),
            i(clash, "nausea_seconds", 10),
            f(clash, "toxic_without_clearing_duration_multiplier", 1.5F)
         );
         HerbcraftCuisine.LOGGER.info("Loaded Herbcraft Cuisine hodgepodge rules.");
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_cuisine hodgepodge_rules.json", e);
      }
   }

   private static HodgepodgeRules defaults() {
      return new HodgepodgeRules(3, 7, 4, 10, 0.15F, 3, 2, 0.30F, 0.10F, 0.15F, 0.10F, 0.0F, 0.80F, 10, 1.5F);
   }

   private static int i(JsonObject json, String key, int fallback) {
      return json.has(key) ? json.get(key).getAsInt() : fallback;
   }

   private static float f(JsonObject json, String key, float fallback) {
      return json.has(key) ? json.get(key).getAsFloat() : fallback;
   }
}
