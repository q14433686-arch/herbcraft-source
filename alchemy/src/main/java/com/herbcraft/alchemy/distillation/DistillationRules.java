package com.herbcraft.alchemy.distillation;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/** Data-backed B3 distillation orientation/catalyst rules. */
public final class DistillationRules {
   private static final String RESOURCE_PATH = "data/herbcraft_alchemy/distillation_rules.json";
   private static final Map<String, Orientation> ORIENTATIONS = new LinkedHashMap<>();

   private DistillationRules() {
   }

   public static void loadFromBundledJson() {
      ORIENTATIONS.clear();
      try (InputStream input = DistillationRules.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
         if (input == null) {
            throw new IllegalStateException("Missing " + RESOURCE_PATH);
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         if (root.has("schema_version") && root.get("schema_version").getAsInt() != 1) {
            throw new IllegalStateException("Unsupported distillation_rules schema_version: " + root.get("schema_version"));
         }
         for (JsonElement element : root.getAsJsonArray("orientations")) {
            Orientation orientation = parseOrientation(element.getAsJsonObject());
            ORIENTATIONS.put(orientation.id(), orientation);
         }
         HerbcraftAlchemy.LOGGER.info("Loaded {} Alchemy distillation orientations.", ORIENTATIONS.size());
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy distillation_rules.json", e);
      }
   }

   private static Orientation parseOrientation(JsonObject json) {
      String id = json.get("id").getAsString();
      Identifier catalystId = Identifier.parse(json.get("catalyst").getAsString());
      Item catalyst = BuiltInRegistries.ITEM.get(catalystId).map(holder -> holder.value()).orElseThrow(() -> new IllegalStateException("Unknown distillation catalyst: " + catalystId));
      return new Orientation(
         id,
         json.has("implemented") && json.get("implemented").getAsBoolean(),
         catalyst,
         catalystId.toString(),
         str(json, "processing_style_hint", ""),
         str(json, "lang_key", "tooltip.herbcraft.distilled_essence.orientation." + id),
         str(json, "description_key", "tooltip.herbcraft.distilled_essence.orientation." + id + ".hint"),
         str(json, "color", "FFFFFF")
      );
   }

   public static Optional<Orientation> orientation(String id) {
      return Optional.ofNullable(ORIENTATIONS.get(id));
   }

   public static List<Orientation> implementedOrientations() {
      return ORIENTATIONS.values().stream().filter(Orientation::implemented).toList();
   }

   public static List<Orientation> orientations() {
      return List.copyOf(ORIENTATIONS.values());
   }

   private static String str(JsonObject json, String key, String fallback) {
      return json.has(key) ? json.get(key).getAsString() : fallback;
   }

   public record Orientation(String id, boolean implemented, Item catalyst, String catalystId, String processingStyleHint, String langKey, String descriptionKey, String color) {
   }
}
