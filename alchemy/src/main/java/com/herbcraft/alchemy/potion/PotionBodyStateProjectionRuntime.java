package com.herbcraft.alchemy.potion;

import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.pharmacology.PharmacologyRules;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

/** Builds and applies the conservative P3 body-state projection for processed potions. */
public final class PotionBodyStateProjectionRuntime {
   private PotionBodyStateProjectionRuntime() {
   }

   public static boolean applyMetadata(ItemStack stack) {
      Optional<PotionProcessingRules.Style> style = AdvancedPotionStackInitializer.processingStyleId(stack).flatMap(PotionProcessingRules::style);
      Optional<PotionPharmacologyProjection> source = PotionPharmacologyProjectionRuntime.projection(stack);
      if (style.isEmpty() || source.isEmpty()) {
         return false;
      }
      NatureProfile profile = new NatureProfile(source.get().temperature(), source.get().flavors(), source.get().tasteFlavors(), source.get().traits());
      PotionBodyStateProjection next = project(style.get(), profile, stack.get(AlchemyPotionComponents.POTION_BURDEN));
      PotionBodyStateProjection current = stack.get(AlchemyPotionComponents.POTION_BODY_STATE);
      if (next.equals(current)) {
         return false;
      }
      stack.set(AlchemyPotionComponents.POTION_BODY_STATE, next);
      return true;
   }

   public static Optional<PotionBodyStateProjection> projection(ItemStack stack) {
      PotionBodyStateProjection current = stack.get(AlchemyPotionComponents.POTION_BODY_STATE);
      if (current != null) {
         return Optional.of(current);
      }
      return applyMetadata(stack) ? Optional.ofNullable(stack.get(AlchemyPotionComponents.POTION_BODY_STATE)) : Optional.empty();
   }

   static PotionBodyStateProjection project(PotionProcessingRules.Style style, NatureProfile profile, PotionBurdenComponent burden) {
      int medicinalWeight = burden == null ? style.burdenWeight() : burden.burdenWeight();
      int sourceThermal = PharmacologyRules.current().temperatureValue(profile.temperature());
      int styleThermal = styleThermalShift(style.thermalShift());
      int thermalValue = sourceThermal + styleThermal;
      String thermalTendency = thermalTendency(thermalValue);
      int qiValue = sourceQiValue(profile) + styleQiBias(style.qiBias());
      String qiTendency = qiTendency(qiValue);
      String medicinalProfile = medicinalProfile(style.burdenProfile(), thermalValue, qiValue, profile, style);
      Set<String> traits = new LinkedHashSet<>();
      traits.addAll(profile.traits());
      traits.addAll(style.traitAffinity());
      traits.add(medicinalProfile);
      if (!"neutral".equals(thermalTendency)) {
         traits.add(thermalTendency);
      }
      if (!"balanced".equals(qiTendency)) {
         traits.add(qiTendency);
      }
      return new PotionBodyStateProjection(medicinalProfile, medicinalWeight, thermalTendency, thermalValue, qiTendency, qiValue, List.copyOf(traits));
   }

   private static int sourceQiValue(NatureProfile profile) {
      int value = 0;
      if (profile.flavors().contains("pungent")) value += 8;
      if (profile.flavors().contains("sweet")) value += 4;
      if (profile.flavors().contains("bitter")) value -= 3;
      if (profile.flavors().contains("salty")) value -= 2;
      if (profile.traits().contains("stirring")) value += 8;
      if (profile.traits().contains("sedating")) value -= 8;
      if (profile.traits().contains("clearing")) value -= 4;
      if (profile.traits().contains("nourishing")) value += 3;
      return value;
   }

   private static int styleThermalShift(String key) {
      return switch (key) {
         case "warming_tail" -> 4;
         case "heated_spike" -> 8;
         case "tempered_clear" -> 0;
         case "unstable_skew" -> 0;
         case "balanced_middle" -> 0;
         default -> 0;
      };
   }

   private static int styleQiBias(String key) {
      return switch (key) {
         case "lingering_extension" -> 4;
         case "forced_surge" -> 10;
         case "settled_clarity" -> -2;
         case "turbid_deviation" -> 0;
         case "coordinated_flow" -> 1;
         default -> 0;
      };
   }

   private static String thermalTendency(int value) {
      if (value >= 10) return "warming";
      if (value <= -10) return "chilling";
      return "neutral";
   }

   private static String qiTendency(int value) {
      if (value >= 10) return "scattering";
      if (value <= -10) return "settling";
      return "balanced";
   }

   private static String medicinalProfile(String burdenProfile, int thermalValue, int qiValue, NatureProfile profile, PotionProcessingRules.Style style) {
      if ("dirty_edge".equals(burdenProfile) || style.traitAffinity().contains("toxic")) {
         return "muddy";
      }
      if ("sharp_peak".equals(burdenProfile) || qiValue >= 14) {
         return "overdriven";
      }
      if (thermalValue >= 10) {
         return "warming";
      }
      if (thermalValue <= -10) {
         return "chilling";
      }
      if (qiValue >= 10) {
         return "scattering";
      }
      if (qiValue <= -10 || profile.traits().contains("sedating") || profile.traits().contains("resinous")) {
         return "stagnating";
      }
      return "steady";
   }
}
