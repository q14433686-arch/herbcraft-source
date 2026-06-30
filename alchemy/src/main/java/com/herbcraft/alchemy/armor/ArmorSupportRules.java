package com.herbcraft.alchemy.armor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.alchemy.HerbcraftAlchemy;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ArmorSupportRules {
   private static final String RESOURCE_PATH = "data/herbcraft_alchemy/armor_support_rules.json";
   private static final Map<String, Support> SUPPORTS = new LinkedHashMap<>();
   private static final Map<String, Support> SUPPORT_BY_ORIENTATION = new LinkedHashMap<>();

   private ArmorSupportRules() {
   }

   public static void loadFromBundledJson() {
      SUPPORTS.clear();
      SUPPORT_BY_ORIENTATION.clear();
      try (InputStream input = ArmorSupportRules.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
         if (input == null) throw new IllegalStateException("Missing " + RESOURCE_PATH);
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         if (root.has("schema_version") && root.get("schema_version").getAsInt() != 1) {
            throw new IllegalStateException("Unsupported armor_support_rules schema_version: " + root.get("schema_version"));
         }
         for (JsonElement element : root.getAsJsonArray("supports")) {
            Support support = parse(element.getAsJsonObject());
            SUPPORTS.put(support.id(), support);
            SUPPORT_BY_ORIENTATION.put(support.orientation(), support);
         }
         HerbcraftAlchemy.LOGGER.info("Loaded {} Alchemy armor support rules.", SUPPORTS.size());
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy armor_support_rules.json", e);
      }
   }

   private static Support parse(JsonObject json) {
      int fallbackBuffer = i(json, "buffer_amount", 0);
      return new Support(
         str(json, "id", ""),
         str(json, "orientation", ""),
         json.has("implemented") && json.get("implemented").getAsBoolean(),
         fallbackBuffer,
         i(json, "active_buffer_amount", fallbackBuffer),
         i(json, "tail_buffer_amount", Math.max(0, fallbackBuffer / 2)),
         list(json, "matching_styles"),
         list(json, "matching_burden_profiles"),
         list(json, "matching_medicinal_profiles"),
         i(json, "match_active_bonus", 0),
         i(json, "match_tail_bonus", 0),
         list(json, "consequence_mitigation_profiles"),
         f(json, "consequence_duration_multiplier", 1.0F),
         i(json, "tail_decay_bonus", 0),
         str(json, "effect", ""),
         i(json, "effect_duration_ticks", 240),
         str(json, "lang_key", ""),
         str(json, "description_key", "")
      );
   }

   public static Optional<Support> support(String id) {
      return Optional.ofNullable(SUPPORTS.get(id));
   }

   public static Optional<Support> supportForOrientation(String orientation) {
      return Optional.ofNullable(SUPPORT_BY_ORIENTATION.get(orientation)).filter(Support::implemented);
   }

   public static List<Support> supports() {
      return List.copyOf(SUPPORTS.values());
   }

   private static String str(JsonObject json, String key, String fallback) {
      return json.has(key) ? json.get(key).getAsString() : fallback;
   }

   private static int i(JsonObject json, String key, int fallback) {
      return json.has(key) ? json.get(key).getAsInt() : fallback;
   }

   private static float f(JsonObject json, String key, float fallback) {
      return json.has(key) ? json.get(key).getAsFloat() : fallback;
   }

   private static List<String> list(JsonObject json, String key) {
      if (!json.has(key) || !json.get(key).isJsonArray()) {
         return List.of();
      }
      List<String> values = new ArrayList<>();
      for (JsonElement element : json.getAsJsonArray(key)) {
         String value = element.getAsString();
         if (!value.isBlank()) {
            values.add(value);
         }
      }
      return List.copyOf(values);
   }

   public record Support(
      String id,
      String orientation,
      boolean implemented,
      int bufferAmount,
      int activeBufferAmount,
      int tailBufferAmount,
      List<String> matchingStyles,
      List<String> matchingBurdenProfiles,
      List<String> matchingMedicinalProfiles,
      int matchActiveBonus,
      int matchTailBonus,
      List<String> consequenceMitigationProfiles,
      float consequenceDurationMultiplier,
      int tailDecayBonus,
      String effect,
      int effectDurationTicks,
      String langKey,
      String descriptionKey
   ) {
   }
}
