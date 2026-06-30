package com.herbcraft.pharmacology;

public record PharmacologyResult(
   float probabilityMultiplier,
   float probabilityBonus,
   float positiveDurationMultiplier,
   float negativeDurationMultiplier,
   int positiveAmplifierBonus,
   float healMultiplier,
   float damageMultiplier,
   float immunityMultiplier,
   boolean cancelAll,
   SevenEmotionType sevenEmotionType
) {
   public static final PharmacologyResult NEUTRAL = new PharmacologyResult(1.0F, 0.0F, 1.0F, 1.0F, 0, 1.0F, 1.0F, 1.0F, false, SevenEmotionType.NONE);

   public float chance(float base) {
      return Math.max(0.0F, Math.min(1.0F, base * this.probabilityMultiplier + this.probabilityBonus));
   }

   public int duration(int baseTicks, boolean positive) {
      float multiplier = positive ? this.positiveDurationMultiplier : this.negativeDurationMultiplier;
      return Math.max(1, Math.round(baseTicks * multiplier));
   }
}
