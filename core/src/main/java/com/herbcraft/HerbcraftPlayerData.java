package com.herbcraft;

import com.herbcraft.api.KnowledgeFlags;
import com.herbcraft.api.PlayerClaimMark;
import com.herbcraft.nutrition.PlayerNutritionState;
import com.herbcraft.pharmacology.PlayerPharmacologyState;
import java.util.Map;
import java.util.Set;

public interface HerbcraftPlayerData {
   int herbcraft$getHerbStack(long var1);
   int herbcraft$addHerbStack(long var1);
   void herbcraft$clearHerbStack();
   int herbcraft$decrementHerbStack(long var1);
   PlayerPharmacologyState herbcraft$pharmacologyState();
   void herbcraft$clearPharmacologyState();
   int herbcraft$incrementWindowCounter(String var1, long var2, int var4);
   Map<String, Integer> herbcraft$knowledge();
   Map<String, KnowledgeFlags> herbcraft$knowledgeFlags();
   Map<String, Set<String>> herbcraft$experiencedEffects();
   Map<String, Map<String, PlayerClaimMark>> herbcraft$claimMarks();
   Map<String, Set<String>> herbcraft$verifiedTrueClaims();
   Map<String, Set<String>> herbcraft$verifiedFalseClaims();
   Map<String, Long> herbcraft$effectImmunities();
   void herbcraft$clearEffectImmunities();
   Set<String> herbcraft$seenGuideHints();
   PlayerNutritionState herbcraft$nutritionState();
}
