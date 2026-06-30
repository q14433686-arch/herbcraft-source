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
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;

public final class CodexLootRules {
   private CodexLootRules() {
   }

   public static List<CodexLootRules.PageInjection> load(String resourcePath) {
      List<CodexLootRules.PageInjection> rules = new ArrayList<>();
      try (InputStream input = CodexLootRules.class.getClassLoader().getResourceAsStream(resourcePath)) {
         if (input == null) {
            throw new IllegalStateException("Missing " + resourcePath);
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         addRulesFromRoot(rules, root, resourcePath);
         Herbcraft.LOGGER.info("Loaded {} Codex loot page injection rules from {}", rules.size(), resourcePath);
      } catch (Exception e) {
         throw new RuntimeException("Failed to load Codex loot rules: " + resourcePath, e);
      }
      return List.copyOf(rules);
   }

   public static List<CodexLootRules.PageInjection> loadExternal(ResourceManager manager, String folder) {
      List<CodexLootRules.PageInjection> rules = new ArrayList<>();
      ExternalDataResources.forEachJson(manager, folder, (resourceId, root) -> addRulesFromRoot(rules, root, resourceId.toString()));
      return List.copyOf(rules);
   }

   private static void addRulesFromRoot(List<CodexLootRules.PageInjection> rules, JsonObject root, String source) {
      if (root.has("page_injections")) {
         for (JsonElement element : root.getAsJsonArray("page_injections")) {
            rules.add(parseRule(element.getAsJsonObject(), source));
         }
      } else if (root.has("entries")) {
         for (JsonElement element : root.getAsJsonArray("entries")) {
            rules.add(parseRule(element.getAsJsonObject(), source));
         }
      } else if (root.has("loot_table")) {
         rules.add(parseRule(root, source));
      }
   }

   private static PageInjection parseRule(JsonObject json, String source) {
      ResourceKey<LootTable> lootTable = ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(json.get("loot_table").getAsString()));
      float chance = json.get("chance").getAsFloat();
      String chapter = json.has("chapter") ? json.get("chapter").getAsString() : "herbal";
      String entry = json.has("entry") ? json.get("entry").getAsString() : "";
      PageKind kind = json.has("page_kind")
         ? PageKind.valueOf(json.get("page_kind").getAsString().toUpperCase(Locale.ROOT))
         : PageKind.COMMON;
      return new CodexLootRules.PageInjection(lootTable, chance, chapter, entry, kind, source);
   }

   public record PageInjection(ResourceKey<LootTable> lootTable, float chance, String chapter, String entry, PageKind kind, String source) {
      public PageInjection(ResourceKey<LootTable> lootTable, float chance, String chapter, String entry, PageKind kind) {
         this(lootTable, chance, chapter, entry, kind, "bundled");
      }
   }
}
