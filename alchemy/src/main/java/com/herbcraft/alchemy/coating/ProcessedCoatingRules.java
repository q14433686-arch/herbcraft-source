package com.herbcraft.alchemy.coating;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.alchemy.HerbcraftAlchemy;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ProcessedCoatingRules {
   private static final String RESOURCE_PATH = "data/herbcraft_alchemy/processed_coating_rules.json";
   private static final Map<String, Style> STYLES = new LinkedHashMap<>();
   private static final Style BASE = new Style("base", true, 1.0F, 1.0F, 0, "tooltip.herbcraft.coating.processing.base");

   private ProcessedCoatingRules() {}

   public static void loadFromBundledJson() {
      STYLES.clear();
      STYLES.put(BASE.id(), BASE);
      try (InputStream input = ProcessedCoatingRules.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
         if (input == null) throw new IllegalStateException("Missing " + RESOURCE_PATH);
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         if (root.has("schema_version") && root.get("schema_version").getAsInt() != 1) {
            throw new IllegalStateException("Unsupported processed_coating_rules schema_version: " + root.get("schema_version"));
         }
         for (JsonElement element : root.getAsJsonArray("styles")) {
            Style style = parse(element.getAsJsonObject());
            STYLES.put(style.id(), style);
         }
         HerbcraftAlchemy.LOGGER.info("Loaded {} processed coating style rules.", STYLES.size());
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy processed_coating_rules.json", e);
      }
   }

   private static Style parse(JsonObject json) {
      return new Style(
         str(json, "id", "base"),
         json.has("implemented") && json.get("implemented").getAsBoolean(),
         f(json, "uses_multiplier", 1.0F),
         f(json, "hit_duration_multiplier", 1.0F),
         i(json, "amplifier_delta", 0),
         str(json, "lang_key", "tooltip.herbcraft.coating.processing.base")
      );
   }

   public static Style styleOrBase(String id) {
      Style style = STYLES.get(id == null || id.isBlank() ? "base" : id);
      return style != null && style.implemented() ? style : BASE;
   }

   public static Optional<Style> style(String id) {
      return Optional.ofNullable(STYLES.get(id));
   }

   public static List<Style> styles() {
      return List.copyOf(STYLES.values());
   }

   private static String str(JsonObject json, String key, String fallback) { return json.has(key) ? json.get(key).getAsString() : fallback; }
   private static int i(JsonObject json, String key, int fallback) { return json.has(key) ? json.get(key).getAsInt() : fallback; }
   private static float f(JsonObject json, String key, float fallback) { return json.has(key) ? json.get(key).getAsFloat() : fallback; }

   public record Style(String id, boolean implemented, float usesMultiplier, float hitDurationMultiplier, int amplifierDelta, String langKey) {}
}
