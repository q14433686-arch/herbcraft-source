package com.herbcraft.alchemy.potion;

import com.herbcraft.alchemy.AlchemyExpansionEvents;
import net.minecraft.world.item.ItemStack;

/** Persistent per-player potion burden values owned by the Alchemy module. */
public final class PlayerPotionBurdenState {
   private int burden;
   private int tailBurden;
   private long lastDecayGameTime;
   private String lastStageId = "steady";
   private String dominantProfile = "steady";
   private String lastBurdenProfile = "none";
   private String lastThermalTendency = "neutral";
   private String lastQiTendency = "balanced";
   private float consequenceDurationMultiplier = 1.0F;
   private int tailDecayBonus;

   public int burden() {
      return burden;
   }

   public int tailBurden() {
      return tailBurden;
   }

   public int totalBurden() {
      return burden + tailBurden;
   }

   public long lastDecayGameTime() {
      return lastDecayGameTime;
   }

   public String lastStageId() {
      return lastStageId;
   }

   public String dominantProfile() {
      return dominantProfile;
   }

   public String lastBurdenProfile() {
      return lastBurdenProfile;
   }

   public String lastThermalTendency() {
      return lastThermalTendency;
   }

   public String lastQiTendency() {
      return lastQiTendency;
   }

   public float consequenceDurationMultiplier() {
      return consequenceDurationMultiplier;
   }

   public int tailDecayBonus() {
      return tailDecayBonus;
   }

   public void setLastStageId(String stageId) {
      this.lastStageId = stageId == null || stageId.isBlank() ? "steady" : stageId;
   }

   public void add(int burdenWeight, int tailWeight, PotionProcessingRules.BurdenRuntime runtime) {
      this.burden = clamp(this.burden + Math.max(0, burdenWeight), runtime.maxBurden());
      this.tailBurden = clamp(this.tailBurden + Math.max(0, tailWeight), runtime.maxBurden());
   }

   public void rememberLastPotionContext(ItemStack stack, PotionBurdenComponent burden, AlchemyExpansionEvents.PotionBurdenBufferResult buffer) {
      PotionBodyStateProjection body = stack.get(AlchemyPotionComponents.POTION_BODY_STATE);
      this.dominantProfile = dominantProfile(body, burden);
      this.lastBurdenProfile = burden == null || burden.profile().isBlank() ? "none" : burden.profile();
      this.lastThermalTendency = body == null || body.thermalTendency().isBlank() ? "neutral" : body.thermalTendency();
      this.lastQiTendency = body == null || body.qiTendency().isBlank() ? "balanced" : body.qiTendency();
      this.consequenceDurationMultiplier = buffer == null ? 1.0F : buffer.consequenceDurationMultiplier();
      this.tailDecayBonus = buffer == null ? 0 : buffer.tailDecayBonus();
   }

   public boolean tickDecay(long gameTime, PotionProcessingRules.BurdenRuntime runtime) {
      int interval = Math.max(1, runtime.decayIntervalTicks());
      if (lastDecayGameTime <= 0L) {
         lastDecayGameTime = gameTime;
         return false;
      }
      if (gameTime - lastDecayGameTime < interval) {
         return false;
      }
      long steps = Math.max(1L, (gameTime - lastDecayGameTime) / interval);
      lastDecayGameTime += steps * interval;
      int oldBurden = burden;
      int oldTail = tailBurden;
      burden = Math.max(0, burden - (int)Math.min(Integer.MAX_VALUE, steps * Math.max(0, runtime.activeDecayAmount())));
      tailBurden = Math.max(0, tailBurden - (int)Math.min(Integer.MAX_VALUE, steps * Math.max(0, runtime.tailDecayAmount() + tailDecayBonus)));
      if (burden == 0 && tailBurden == 0) {
         clearDominantContext();
      }
      return oldBurden != burden || oldTail != tailBurden;
   }

   public void setDirect(int burden, int tailBurden, long lastDecayGameTime, PotionProcessingRules.BurdenRuntime runtime) {
      this.burden = clamp(Math.max(0, burden), runtime.maxBurden());
      this.tailBurden = clamp(Math.max(0, tailBurden), runtime.maxBurden());
      this.lastDecayGameTime = Math.max(0L, lastDecayGameTime);
      if (this.burden == 0 && this.tailBurden == 0) {
         clearDominantContext();
      }
   }

   public void setDirect(int burden, int tailBurden, long lastDecayGameTime, String lastStageId, PotionProcessingRules.BurdenRuntime runtime) {
      setDirect(burden, tailBurden, lastDecayGameTime, runtime);
      setLastStageId(lastStageId);
   }

   public void setDirect(
      int burden,
      int tailBurden,
      long lastDecayGameTime,
      String lastStageId,
      String dominantProfile,
      String lastBurdenProfile,
      String lastThermalTendency,
      String lastQiTendency,
      float consequenceDurationMultiplier,
      int tailDecayBonus,
      PotionProcessingRules.BurdenRuntime runtime
   ) {
      setDirect(burden, tailBurden, lastDecayGameTime, lastStageId, runtime);
      this.dominantProfile = normalize(dominantProfile, "steady");
      this.lastBurdenProfile = normalize(lastBurdenProfile, "none");
      this.lastThermalTendency = normalize(lastThermalTendency, "neutral");
      this.lastQiTendency = normalize(lastQiTendency, "balanced");
      this.consequenceDurationMultiplier = consequenceDurationMultiplier <= 0.0F ? 1.0F : consequenceDurationMultiplier;
      this.tailDecayBonus = Math.max(0, tailDecayBonus);
   }

   public void copyFrom(PlayerPotionBurdenState other, PotionProcessingRules.BurdenRuntime runtime) {
      setDirect(
         other.burden,
         other.tailBurden,
         other.lastDecayGameTime,
         other.lastStageId,
         other.dominantProfile,
         other.lastBurdenProfile,
         other.lastThermalTendency,
         other.lastQiTendency,
         other.consequenceDurationMultiplier,
         other.tailDecayBonus,
         runtime
      );
   }

   public void clear() {
      burden = 0;
      tailBurden = 0;
      lastDecayGameTime = 0L;
      lastStageId = "steady";
      clearDominantContext();
   }

   private void clearDominantContext() {
      dominantProfile = "steady";
      lastBurdenProfile = "none";
      lastThermalTendency = "neutral";
      lastQiTendency = "balanced";
      consequenceDurationMultiplier = 1.0F;
      tailDecayBonus = 0;
   }

   private static String dominantProfile(PotionBodyStateProjection body, PotionBurdenComponent burden) {
      if (body != null && !body.medicinalProfile().isBlank()) {
         return body.medicinalProfile();
      }
      if (burden != null) {
         return switch (burden.profile()) {
            case "dirty_edge" -> "muddy";
            case "sharp_peak" -> "overdriven";
            case "slow_tail" -> "stagnating";
            case "clean_stable" -> "steady";
            default -> "steady";
         };
      }
      return "steady";
   }

   private static String normalize(String value, String fallback) {
      return value == null || value.isBlank() ? fallback : value;
   }

   private static int clamp(int value, int max) {
      return Math.max(0, Math.min(Math.max(1, max), value));
   }
}
