package com.herbcraft.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Utility for the first external Herbcraft data merge layer.
 *
 * <p>These helpers intentionally read additive per-file paths such as
 * {@code data/<namespace>/herbcraft/herb_natures/*.json}. They do not replace
 * the bundled baseline JSON files; individual registries load bundled data
 * first and then apply these external resources on top.</p>
 */
public final class ExternalDataResources {
   private ExternalDataResources() {
   }

   public static void forEachJson(ResourceManager manager, String folder, BiConsumer<Identifier, JsonObject> consumer) {
      List<Map.Entry<Identifier, Resource>> resources = new ArrayList<>(manager.listResources(folder, id -> id.getPath().endsWith(".json")).entrySet());
      resources.sort(Comparator.comparing(entry -> entry.getKey().toString()));
      for (Map.Entry<Identifier, Resource> entry : resources) {
         Identifier id = entry.getKey();
         try (BufferedReader reader = entry.getValue().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            consumer.accept(id, root);
         } catch (Exception e) {
            Herbcraft.LOGGER.warn("Failed to load external Herbcraft data {} from pack {}", id, entry.getValue().sourcePackId(), e);
         }
      }
   }
}
