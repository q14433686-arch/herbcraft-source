package com.herbcraft.nature;

import java.util.List;

public record NatureProfile(String temperature, List<String> flavors, List<String> tasteFlavors, List<String> traits) {
   public static final NatureProfile DEFAULT = new NatureProfile("neutral", List.of("bland"), List.of("bland"), List.of());

   public NatureProfile(String temperature, List<String> flavors, List<String> traits) {
      this(temperature, flavors, flavors, traits);
   }

   public NatureProfile {
      flavors = List.copyOf(flavors == null || flavors.isEmpty() ? List.of("bland") : flavors);
      tasteFlavors = List.copyOf(tasteFlavors == null || tasteFlavors.isEmpty() ? flavors : tasteFlavors);
      traits = List.copyOf(traits == null ? List.of() : traits);
      if (temperature == null || temperature.isBlank()) {
         temperature = "neutral";
      }
   }
}
