package com.herbcraft.knowledge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import com.herbcraft.data.ExternalDataResources;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class KnowledgeRumorRegistry {
   private static final Map<String, List<KnowledgeClaim>> CLAIMS = new HashMap<>();

   private KnowledgeRumorRegistry() {
   }

   public static void loadFromBundledJson() {
      CLAIMS.clear();
      try (InputStream in = KnowledgeRumorRegistry.class.getClassLoader().getResourceAsStream("data/herbcraft/knowledge_rumors.json")) {
         if (in == null) {
            return;
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
         for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String key = normalizeEntryKey(entry.getKey());
            CLAIMS.put(key, parseClaims(key, entry.getValue().getAsJsonObject()));
         }
      } catch (Exception ex) {
         Herbcraft.LOGGER.warn("Failed to load knowledge_rumors.json", ex);
      }
   }

   public static void applyExternalData(ResourceManager manager) {
      ExternalDataResources.forEachJson(manager, "herbcraft/knowledge_rumors", (resourceId, root) -> {
         if (root.has("entries")) {
            for (JsonElement element : root.getAsJsonArray("entries")) {
               mergeExternalRumor(resourceId, element.getAsJsonObject());
            }
         } else {
            mergeExternalRumor(resourceId, root);
         }
      });
   }

   private static void mergeExternalRumor(Identifier resourceId, JsonObject json) {
      if (!json.has("entry")) {
         Herbcraft.LOGGER.warn("Skipping external knowledge rumor {} without entry field", resourceId);
         return;
      }
      String key = normalizeEntryKey(json.get("entry").getAsString());
      Map<String, KnowledgeClaim> merged = new HashMap<>();
      for (KnowledgeClaim claim : CLAIMS.getOrDefault(key, List.of())) {
         merged.put(claim.id(), claim);
      }
      for (KnowledgeClaim claim : parseClaims(key, json)) {
         if (merged.containsKey(claim.id())) {
            Herbcraft.LOGGER.warn("External knowledge rumor {} replaces claim {} for {}", resourceId, claim.id(), key);
         }
         merged.put(claim.id(), claim);
      }
      CLAIMS.put(key, List.copyOf(merged.values()));
   }

   private static String normalizeEntryKey(String raw) {
      return raw.contains("/") ? raw : "herbal/" + raw.substring(raw.lastIndexOf(':') + 1);
   }

   private static List<KnowledgeClaim> parseClaims(String key, JsonObject obj) {
      List<KnowledgeClaim> list = new ArrayList<>();
      if (obj.has("claims")) {
         for (JsonElement claimElement : obj.getAsJsonArray("claims")) {
            JsonObject claimJson = claimElement.getAsJsonObject();
            PageKind tier = PageKind.byName(claimJson.has("source_tier") ? claimJson.get("source_tier").getAsString() : "common");
            boolean truth = claimJson.has("truth") && claimJson.get("truth").getAsBoolean();
            boolean suspicious = claimJson.has("suspicious") ? claimJson.get("suspicious").getAsBoolean() : !truth;
            list.add(new KnowledgeClaim(
               key,
               claimJson.get("id").getAsString(),
               claimJson.has("type") ? claimJson.get("type").getAsString() : "note",
               claimJson.get("text_key").getAsString(),
               truth,
               tier,
               suspicious
            ));
         }
      }
      return List.copyOf(list);
   }

   public static List<KnowledgeClaim> claims(String entryKey) {
      return CLAIMS.getOrDefault(entryKey, List.of());
   }

   public static KnowledgeClaim claim(String entryKey, String claimId) {
      return claims(entryKey).stream().filter(c -> c.id().equals(claimId)).findFirst().orElse(null);
   }
}
