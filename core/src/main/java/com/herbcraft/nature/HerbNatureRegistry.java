package com.herbcraft.nature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import com.herbcraft.data.ExternalDataResources;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HerbNatureRegistry {
   private static final Set<String> TEMPERATURES = Set.of("cold", "cool", "neutral", "warm", "hot");
   private static final Set<String> FLAVORS = Set.of("sweet", "bitter", "pungent", "sour", "astringent", "salty", "bland");
   private static final Set<String> TRAITS = Set.of(
      "clearing", "toxic", "nourishing", "stirring", "sedating", "aquatic", "fiery", "withered", "nether", "floral", "resinous", "fungal", "lunar", "solar", "leafy", "icy", "slimy", "honeyed", "earthy", "addictive", "curio"
   );
   private static final Map<Identifier, NatureProfile> PROFILES = new LinkedHashMap<>();

   private HerbNatureRegistry() {
   }

   public static void loadFromBundledJson() {
      PROFILES.clear();
      try (InputStream input = HerbNatureRegistry.class.getClassLoader().getResourceAsStream("data/herbcraft/herb_natures.json")) {
         if (input == null) {
            return;
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            putProfile(Identifier.parse(entry.getKey()), entry.getValue().getAsJsonObject(), entry.getKey());
         }
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft herb_natures.json", e);
      }
   }

   public static void applyExternalData(ResourceManager manager) {
      int before = PROFILES.size();
      ExternalDataResources.forEachJson(manager, "herbcraft/herb_natures", (resourceId, root) -> {
         if (root.has("entries")) {
            for (JsonElement element : root.getAsJsonArray("entries")) {
               putExternalProfile(resourceId, element.getAsJsonObject());
            }
         } else {
            putExternalProfile(resourceId, root);
         }
      });
      if (PROFILES.size() != before) {
         Herbcraft.LOGGER.info("Loaded {} total herb nature profiles after external data merge", PROFILES.size());
      }
   }

   private static void putExternalProfile(Identifier resourceId, JsonObject json) {
      if (!json.has("item")) {
         Herbcraft.LOGGER.warn("Skipping external herb nature {} without item field", resourceId);
         return;
      }
      Identifier itemId = Identifier.parse(json.get("item").getAsString());
      if (PROFILES.containsKey(itemId)) {
         Herbcraft.LOGGER.warn("External herb nature {} overrides existing nature profile for {}", resourceId, itemId);
      }
      putProfile(itemId, json, resourceId.toString());
   }

   private static void putProfile(Identifier id, JsonObject json, String source) {
      String temperature = json.has("temperature") ? json.get("temperature").getAsString() : "neutral";
      List<String> flavors = strings(json.getAsJsonArray("flavors"));
      List<String> tasteFlavors = json.has("taste_flavors") ? strings(json.getAsJsonArray("taste_flavors")) : flavors;
      List<String> traits = strings(json.getAsJsonArray("traits"));
      validate(source, temperature, flavors, tasteFlavors, traits);
      PROFILES.put(id, new NatureProfile(temperature, flavors, tasteFlavors, traits));
   }

   public static Optional<NatureProfile> explicit(Identifier id) {
      return Optional.ofNullable(PROFILES.get(id));
   }

   public static NatureProfile getOrFallback(Identifier id) {
      return explicit(id).orElse(NatureProfile.DEFAULT);
   }

   public static NatureProfile getOrFallback(Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return id == null ? NatureProfile.DEFAULT : getOrFallback(id);
   }

   public static NatureProfile getOrFallback(ItemStack stack) {
      return stack.isEmpty() ? NatureProfile.DEFAULT : getOrFallback(stack.getItem());
   }

   public static Map<Identifier, NatureProfile> profiles() {
      return Map.copyOf(PROFILES);
   }

   public static void replaceWithSyncedData(Map<Identifier, NatureProfile> profiles) {
      PROFILES.clear();
      PROFILES.putAll(profiles);
   }

   private static List<String> strings(JsonArray array) {
      if (array == null || array.isEmpty()) {
         return List.of("bland");
      }
      List<String> values = new ArrayList<>();
      for (JsonElement element : array) {
         values.add(element.getAsString());
      }
      return values;
   }

   private static void validate(String id, String temperature, List<String> flavors, List<String> tasteFlavors, List<String> traits) {
      if (!TEMPERATURES.contains(temperature)) {
         throw new IllegalStateException("Unknown herb nature temperature for " + id + ": " + temperature);
      }
      for (String flavor : flavors) {
         if (!FLAVORS.contains(flavor)) {
            throw new IllegalStateException("Unknown herb nature flavor for " + id + ": " + flavor);
         }
      }
      for (String flavor : tasteFlavors) {
         if (!FLAVORS.contains(flavor)) {
            throw new IllegalStateException("Unknown herb nature taste flavor for " + id + ": " + flavor);
         }
      }
      for (String trait : traits) {
         if (!TRAITS.contains(trait)) {
            throw new IllegalStateException("Unknown herb nature trait for " + id + ": " + trait);
         }
      }
   }
}
