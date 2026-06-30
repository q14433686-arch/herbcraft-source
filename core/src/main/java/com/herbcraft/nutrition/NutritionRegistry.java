package com.herbcraft.nutrition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import com.herbcraft.data.ExternalDataResources;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class NutritionRegistry {
   private static final Map<Identifier, NutritionProfile> PROFILES = new LinkedHashMap<>();

   private NutritionRegistry() {}

   public static void loadFromBundledJson() {
      PROFILES.clear();
      try (InputStream in = NutritionRegistry.class.getClassLoader()
               .getResourceAsStream("data/herbcraft/nutrition_profiles.json")) {
         if (in == null) return;
         JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
         for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            putProfile(Identifier.parse(entry.getKey()), entry.getValue().getAsJsonObject());
         }
         Herbcraft.LOGGER.info("Loaded {} nutrition profiles", PROFILES.size());
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load nutrition_profiles.json", e);
      }
   }

   public static void applyExternalData(ResourceManager manager) {
      int before = PROFILES.size();
      ExternalDataResources.forEachJson(manager, "herbcraft/nutrition_profiles", (resourceId, root) -> {
         if (root.has("entries")) {
            for (JsonElement element : root.getAsJsonArray("entries")) {
               putExternalProfile(resourceId, element.getAsJsonObject());
            }
         } else {
            putExternalProfile(resourceId, root);
         }
      });
      if (PROFILES.size() != before) {
         Herbcraft.LOGGER.info("Loaded {} total nutrition profiles after external data merge", PROFILES.size());
      }
   }

   private static void putExternalProfile(Identifier resourceId, JsonObject json) {
      if (!json.has("item")) {
         Herbcraft.LOGGER.warn("Skipping external nutrition profile {} without item field", resourceId);
         return;
      }
      Identifier itemId = Identifier.parse(json.get("item").getAsString());
      if (PROFILES.containsKey(itemId)) {
         Herbcraft.LOGGER.warn("External nutrition profile {} overrides existing profile for {}", resourceId, itemId);
      }
      JsonObject values = json.has("values") ? json.getAsJsonObject("values") : json;
      putProfile(itemId, values);
   }

   private static void putProfile(Identifier id, JsonObject obj) {
      Map<String, Integer> values = new LinkedHashMap<>();
      for (String dim : NutritionProfile.DIMENSIONS) {
         if (obj.has(dim)) {
            int val = obj.get(dim).getAsInt();
            if (val > 0) values.put(dim, val);
         }
      }
      if (!values.isEmpty()) {
         PROFILES.put(id, new NutritionProfile(Map.copyOf(values)));
      }
   }

   public static Map<Identifier, NutritionProfile> profiles() {
      return Map.copyOf(PROFILES);
   }

   public static void replaceWithSyncedData(Map<Identifier, NutritionProfile> profiles) {
      PROFILES.clear();
      PROFILES.putAll(profiles);
   }

   public static Optional<NutritionProfile> get(Identifier id) {
      return Optional.ofNullable(PROFILES.get(id));
   }

   public static Optional<NutritionProfile> get(Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return id == null ? Optional.empty() : get(id);
   }

   public static Optional<NutritionProfile> get(ItemStack stack) {
      return stack.isEmpty() ? Optional.empty() : get(stack.getItem());
   }
}
