package com.herbcraft.mixin;

import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.api.KnowledgeFlags;
import com.herbcraft.api.PlayerClaimMark;
import com.herbcraft.nutrition.NutritionManager;
import com.herbcraft.nutrition.PlayerNutritionState;
import com.herbcraft.pharmacology.PlayerPharmacologyState;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements HerbcraftPlayerData {
   @Unique
   private int herbcraft$herbStack;
   @Unique
   private long herbcraft$herbExpiresAt;
   @Unique
   private final Map<String, CounterState> herbcraft$windowCounters = new HashMap<>();
   @Unique
   private final Map<String, Integer> herbcraft$knowledgeMap = new HashMap<>();
   @Unique
   private final Map<String, KnowledgeFlags> herbcraft$knowledgeFlagsMap = new HashMap<>();
   @Unique
   private final Map<String, Set<String>> herbcraft$experiencedEffectsMap = new HashMap<>();
   @Unique
   private final Map<String, Map<String, PlayerClaimMark>> herbcraft$claimMarksMap = new HashMap<>();
   @Unique
   private final Map<String, Set<String>> herbcraft$verifiedTrueClaimsMap = new HashMap<>();
   @Unique
   private final Map<String, Set<String>> herbcraft$verifiedFalseClaimsMap = new HashMap<>();
   @Unique
   private final Map<String, Long> herbcraft$effectImmunityMap = new HashMap<>();
   @Unique
   private final Set<String> herbcraft$seenGuideHintsSet = new HashSet<>();
   @Unique
   private final PlayerPharmacologyState herbcraft$pharmacologyState = new PlayerPharmacologyState();
   @Unique
   private final PlayerNutritionState herbcraft$nutritionState = new PlayerNutritionState();

   @Override
   public Map<String, Integer> herbcraft$knowledge() {
      return this.herbcraft$knowledgeMap;
   }

   @Override
   public Map<String, KnowledgeFlags> herbcraft$knowledgeFlags() { return this.herbcraft$knowledgeFlagsMap; }

   @Override
   public Map<String, Set<String>> herbcraft$experiencedEffects() { return this.herbcraft$experiencedEffectsMap; }

   @Override
   public Map<String, Map<String, PlayerClaimMark>> herbcraft$claimMarks() { return this.herbcraft$claimMarksMap; }

   @Override
   public Map<String, Set<String>> herbcraft$verifiedTrueClaims() { return this.herbcraft$verifiedTrueClaimsMap; }

   @Override
   public Map<String, Set<String>> herbcraft$verifiedFalseClaims() { return this.herbcraft$verifiedFalseClaimsMap; }

   @Override
   public Map<String, Long> herbcraft$effectImmunities() {
      return this.herbcraft$effectImmunityMap;
   }

   @Override
   public void herbcraft$clearEffectImmunities() {
      this.herbcraft$effectImmunityMap.clear();
   }

   @Override
   public Set<String> herbcraft$seenGuideHints() {
      return this.herbcraft$seenGuideHintsSet;
   }

   @Override
   public PlayerNutritionState herbcraft$nutritionState() {
      return this.herbcraft$nutritionState;
   }

   @Override
   public int herbcraft$getHerbStack(long gameTime) {
      if (gameTime > this.herbcraft$herbExpiresAt) {
         if (this.herbcraft$herbStack > 0) {
            ((Player)(Object)this).sendSystemMessage(Component.translatable("message.herbcraft.herb_stack.fade"));
         }
         this.herbcraft$herbStack = 0;
      }

      return this.herbcraft$herbStack;
   }

   @Override
   public int herbcraft$addHerbStack(long gameTime) {
      if (gameTime > this.herbcraft$herbExpiresAt) {
         this.herbcraft$herbStack = 0;
      }

      this.herbcraft$herbStack++;
      this.herbcraft$herbExpiresAt = gameTime + 1200L;
      if (this.herbcraft$herbStack == 3 || this.herbcraft$herbStack == 5 || this.herbcraft$herbStack == 8) {
         ((Player)(Object)this).sendSystemMessage(Component.translatable("message.herbcraft.herb_stack.stage_" + this.herbcraft$herbStack));
      }
      return this.herbcraft$herbStack;
   }

   @Override
   public void herbcraft$clearHerbStack() {
      this.herbcraft$herbStack = 0;
      this.herbcraft$herbExpiresAt = 0L;
   }

   @Override
   public int herbcraft$decrementHerbStack(long gameTime) {
      this.herbcraft$getHerbStack(gameTime);
      if (this.herbcraft$herbStack > 0) {
         this.herbcraft$herbStack--;
      }
      if (this.herbcraft$herbStack <= 0) {
         this.herbcraft$herbExpiresAt = 0L;
      }
      return this.herbcraft$herbStack;
   }

   @Override
   public PlayerPharmacologyState herbcraft$pharmacologyState() {
      return this.herbcraft$pharmacologyState;
   }

   @Override
   public void herbcraft$clearPharmacologyState() {
      this.herbcraft$pharmacologyState.clear();
   }

   @Override
   public int herbcraft$incrementWindowCounter(String counter, long gameTime, int windowTicks) {
      if (counter == null || counter.isBlank()) {
         return 0;
      }
      CounterState state = this.herbcraft$windowCounters.get(counter);
      if (state == null || gameTime > state.expiresAt) {
         state = new CounterState(0, 0L);
         this.herbcraft$windowCounters.put(counter, state);
      }
      state.count++;
      state.expiresAt = gameTime + Math.max(1, windowTicks);
      return state.count;
   }

   @Unique
   private String herbcraft$encodeWindowCounters() {
      StringBuilder builder = new StringBuilder();
      this.herbcraft$windowCounters.forEach((id, state) -> {
         if (state.count > 0 && state.expiresAt > 0L) {
            if (builder.length() > 0) {
               builder.append(';');
            }
            builder.append(id).append('|').append(state.count).append('|').append(state.expiresAt);
         }
      });
      return builder.toString();
   }

   @Unique
   private void herbcraft$decodeWindowCounters(String encoded) {
      this.herbcraft$windowCounters.clear();
      if (encoded == null || encoded.isBlank()) {
         return;
      }
      for (String entry : encoded.split(";")) {
         String[] parts = entry.split("\\|", -1);
         if (parts.length != 3 || parts[0].isBlank()) {
            continue;
         }
         try {
            int count = Integer.parseInt(parts[1]);
            long expiresAt = Long.parseLong(parts[2]);
            if (count > 0 && expiresAt > 0L) {
               this.herbcraft$windowCounters.put(parts[0], new CounterState(count, expiresAt));
            }
         } catch (NumberFormatException ignored) {
         }
      }
   }

   @Unique
   private void herbcraft$migrateLegacyWindowCounter(String id, int count, long expiresAt) {
      if (count > 0 && expiresAt > 0L && !this.herbcraft$windowCounters.containsKey(id)) {
         this.herbcraft$windowCounters.put(id, new CounterState(count, expiresAt));
      }
   }

   @Unique
   private static final class CounterState {
      int count;
      long expiresAt;

      CounterState(int count, long expiresAt) {
         this.count = count;
         this.expiresAt = expiresAt;
      }
   }


   @Unique
   private static String herbcraft$encodeFlags(Map<String, KnowledgeFlags> map) {
      StringBuilder builder = new StringBuilder();
      map.forEach((key, flags) -> { if (builder.length() > 0) builder.append(';'); builder.append(key).append('=').append(flags.encode()); });
      return builder.toString();
   }
   @Unique
   private static void herbcraft$decodeFlags(String value, Map<String, KnowledgeFlags> target) {
      target.clear(); if (value == null || value.isBlank()) return;
      for (String entry : value.split(";")) { int split = entry.indexOf('='); if (split > 0) target.put(entry.substring(0, split), KnowledgeFlags.decode(entry.substring(split + 1))); }
   }
   @Unique
   private static String herbcraft$encodeSets(Map<String, Set<String>> map) {
      StringBuilder builder = new StringBuilder();
      map.forEach((key, set) -> { if (builder.length() > 0) builder.append(';'); builder.append(key).append('=').append(String.join(",", set)); });
      return builder.toString();
   }
   @Unique
   private static void herbcraft$decodeSets(String value, Map<String, Set<String>> target) {
      target.clear(); if (value == null || value.isBlank()) return;
      for (String entry : value.split(";")) { int split = entry.indexOf('='); if (split > 0) { Set<String> set = new HashSet<>(); String rest = entry.substring(split + 1); if (!rest.isBlank()) for (String v : rest.split(",")) if (!v.isBlank()) set.add(v); target.put(entry.substring(0, split), set); } }
   }
   @Unique
   private static String herbcraft$encodeMarks(Map<String, Map<String, PlayerClaimMark>> map) {
      StringBuilder builder = new StringBuilder();
      map.forEach((entryKey, marks) -> marks.forEach((claimId, mark) -> { if (builder.length() > 0) builder.append(';'); builder.append(entryKey).append('|').append(claimId).append('|').append(mark.ordinal()); }));
      return builder.toString();
   }
   @Unique
   private static void herbcraft$decodeMarks(String value, Map<String, Map<String, PlayerClaimMark>> target) {
      target.clear(); if (value == null || value.isBlank()) return;
      for (String entry : value.split(";")) { String[] p = entry.split("\\|"); if (p.length == 3) try { PlayerClaimMark mark = PlayerClaimMark.values()[Math.max(0, Math.min(PlayerClaimMark.values().length - 1, Integer.parseInt(p[2])))]; if (mark != PlayerClaimMark.UNMARKED) target.computeIfAbsent(p[0], k -> new HashMap<>()).put(p[1], mark); } catch (Exception ignored) {} }
   }

   @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
   private void herbcraft$writeData(ValueOutput output, CallbackInfo ci) {
      output.putInt("HerbcraftHerbStack", this.herbcraft$herbStack);
      output.putLong("HerbcraftHerbExpiresAt", this.herbcraft$herbExpiresAt);
      output.putString("HerbcraftWindowCounters", this.herbcraft$encodeWindowCounters());
      StringBuilder builder = new StringBuilder();
      this.herbcraft$knowledgeMap.forEach((key, state) -> {
         if (builder.length() > 0) {
            builder.append(';');
         }

         builder.append(key).append(':').append(state);
      });
      output.putString("HerbcraftKnowledge", builder.toString());
      output.putString("HerbcraftKnowledgeFlags", herbcraft$encodeFlags(this.herbcraft$knowledgeFlagsMap));
      output.putString("HerbcraftExperiencedEffects", herbcraft$encodeSets(this.herbcraft$experiencedEffectsMap));
      output.putString("HerbcraftClaimMarks", herbcraft$encodeMarks(this.herbcraft$claimMarksMap));
      output.putString("HerbcraftVerifiedTrueClaims", herbcraft$encodeSets(this.herbcraft$verifiedTrueClaimsMap));
      output.putString("HerbcraftVerifiedFalseClaims", herbcraft$encodeSets(this.herbcraft$verifiedFalseClaimsMap));
      StringBuilder immunityBuilder = new StringBuilder();
      this.herbcraft$effectImmunityMap.forEach((key, expiresAt) -> {
         if (immunityBuilder.length() > 0) {
            immunityBuilder.append(';');
         }

         immunityBuilder.append(key).append('|').append(expiresAt);
      });
      output.putString("HerbcraftEffectImmunities", immunityBuilder.toString());
      output.putString("HerbcraftGuideHints", String.join(";", this.herbcraft$seenGuideHintsSet));
      output.putString("HerbcraftPharmacologyState", this.herbcraft$pharmacologyState.encode());
      {
         net.minecraft.world.level.storage.ValueOutput nOut = output.child("HerbcraftNutrition");
         for (String dim : com.herbcraft.nutrition.NutritionProfile.DIMENSIONS) {
            nOut.putInt(dim, this.herbcraft$nutritionState.get(dim));
         }
         nOut.putString("RecentMeals", this.herbcraft$nutritionState.encodeRecentMeals());
      }
   }

   @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
   private void herbcraft$readData(ValueInput input, CallbackInfo ci) {
      this.herbcraft$herbStack = input.getIntOr("HerbcraftHerbStack", 0);
      this.herbcraft$herbExpiresAt = input.getLongOr("HerbcraftHerbExpiresAt", 0L);
      this.herbcraft$decodeWindowCounters(input.getStringOr("HerbcraftWindowCounters", ""));
      this.herbcraft$migrateLegacyWindowCounter("poppy", input.getIntOr("HerbcraftPoppyCount", 0), input.getLongOr("HerbcraftPoppyExpiresAt", 0L));
      this.herbcraft$migrateLegacyWindowCounter("torchflower", input.getIntOr("HerbcraftTorchflowerCount", 0), input.getLongOr("HerbcraftTorchflowerExpiresAt", 0L));
      this.herbcraft$migrateLegacyWindowCounter("sugar", input.getIntOr("HerbcraftSugarCount", 0), input.getLongOr("HerbcraftSugarExpiresAt", 0L));
      this.herbcraft$migrateLegacyWindowCounter("slime", input.getIntOr("HerbcraftSlimeCount", 0), input.getLongOr("HerbcraftSlimeExpiresAt", 0L));
      this.herbcraft$knowledgeMap.clear();
      String knowledge = input.getStringOr("HerbcraftKnowledge", "");
      if (!knowledge.isEmpty()) {
         for (String entry : knowledge.split(";")) {
            int split = entry.lastIndexOf(58);
            if (split > 0) {
               try {
                  this.herbcraft$knowledgeMap.put(entry.substring(0, split), Integer.parseInt(entry.substring(split + 1)));
               } catch (NumberFormatException var10) {
               }
            }
         }
      }

      herbcraft$decodeFlags(input.getStringOr("HerbcraftKnowledgeFlags", ""), this.herbcraft$knowledgeFlagsMap);
      if (this.herbcraft$knowledgeFlagsMap.isEmpty() && !this.herbcraft$knowledgeMap.isEmpty()) {
         this.herbcraft$knowledgeMap.forEach((key, state) -> { KnowledgeFlags flags = new KnowledgeFlags(); flags.setLegacy(state); this.herbcraft$knowledgeFlagsMap.put(key, flags); });
      }
      herbcraft$decodeSets(input.getStringOr("HerbcraftExperiencedEffects", ""), this.herbcraft$experiencedEffectsMap);
      herbcraft$decodeMarks(input.getStringOr("HerbcraftClaimMarks", ""), this.herbcraft$claimMarksMap);
      herbcraft$decodeSets(input.getStringOr("HerbcraftVerifiedTrueClaims", ""), this.herbcraft$verifiedTrueClaimsMap);
      herbcraft$decodeSets(input.getStringOr("HerbcraftVerifiedFalseClaims", ""), this.herbcraft$verifiedFalseClaimsMap);

      this.herbcraft$effectImmunityMap.clear();
      String immunities = input.getStringOr("HerbcraftEffectImmunities", "");
      if (!immunities.isEmpty()) {
         for (String entry : immunities.split(";")) {
            int split = entry.lastIndexOf('|');
            if (split > 0) {
               try {
                  this.herbcraft$effectImmunityMap.put(entry.substring(0, split), Long.parseLong(entry.substring(split + 1)));
               } catch (NumberFormatException var11) {
               }
            }
         }
      }
      this.herbcraft$seenGuideHintsSet.clear();
      String guideHints = input.getStringOr("HerbcraftGuideHints", "");
      if (!guideHints.isBlank()) {
         for (String hint : guideHints.split(";")) {
            if (!hint.isBlank()) {
               this.herbcraft$seenGuideHintsSet.add(hint);
            }
         }
      }
      this.herbcraft$pharmacologyState.decode(input.getStringOr("HerbcraftPharmacologyState", ""));
      input.child("HerbcraftNutrition").ifPresent(nIn -> {
         for (String dim : com.herbcraft.nutrition.NutritionProfile.DIMENSIONS) {
            nIn.getInt(dim).ifPresent(v -> this.herbcraft$nutritionState.setDirect(dim, v));
         }
         this.herbcraft$nutritionState.decodeRecentMeals(nIn.getStringOr("RecentMeals", ""));
      });
   }

   @Inject(method = "die", at = @At("TAIL"))
   private void herbcraft$clearOnDeath(DamageSource damageSource, CallbackInfo ci) {
      this.herbcraft$clearHerbStack();
      this.herbcraft$clearEffectImmunities();
      this.herbcraft$clearPharmacologyState();
      this.herbcraft$nutritionState.resetOnDeath();
   }
}
