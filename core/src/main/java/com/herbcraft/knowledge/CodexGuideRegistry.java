package com.herbcraft.knowledge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import com.herbcraft.api.KnowledgeAPI;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Data-backed Codex guide entry registration.
 *
 * JSON owns which guide entries exist and which module owns them. Java owns the
 * dynamic line providers because those lines depend on player knowledge, stats,
 * advancements, and pharmacology discovery state.
 */
public final class CodexGuideRegistry {
   private static final Map<Identifier, GuideLineProvider> PROVIDERS = new LinkedHashMap<>();

   private CodexGuideRegistry() {}

   public static void registerProvider(Identifier kind, GuideLineProvider provider) {
      if (kind == null || provider == null) {
         return;
      }
      PROVIDERS.put(kind, provider);
   }

   public static void loadFromBundledJson(String resourcePath) {
      try (InputStream input = CodexGuideRegistry.class.getClassLoader().getResourceAsStream(resourcePath)) {
         if (input == null) {
            Herbcraft.LOGGER.warn("Missing Codex guide data {}; no guide entries registered from this file", resourcePath);
            return;
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonArray entries = root.getAsJsonArray("entries");
         if (entries == null) {
            return;
         }
         int registered = 0;
         for (JsonElement element : entries) {
            if (!element.isJsonObject()) {
               continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String id = string(entry, "id", "");
            String chapter = string(entry, "chapter", "herbal");
            String section = string(entry, "section", "overview");
            String titleKey = string(entry, "title_key", "");
            Identifier kind = Identifier.tryParse(string(entry, "kind", ""));
            boolean alwaysVisible = !entry.has("always_visible") || entry.get("always_visible").getAsBoolean();
            if (id.isBlank() || titleKey.isBlank() || kind == null) {
               Herbcraft.LOGGER.warn("Skipping malformed Codex guide entry in {}: {}", resourcePath, entry);
               continue;
            }
            if (!alwaysVisible || !id.startsWith("guide_")) {
               Herbcraft.LOGGER.warn("Skipping Codex guide {} from {}: guide entries must be always_visible and use guide_* IDs", id, resourcePath);
               continue;
            }
            GuideLineProvider provider = PROVIDERS.get(kind);
            if (provider == null) {
               Herbcraft.LOGGER.warn("Skipping Codex guide {} from {}: no provider registered for kind {}", id, resourcePath, kind);
               continue;
            }
            KnowledgeAPI.registerEntry(
               chapter,
               new KnowledgeAPI.Entry(
                  id,
                  section,
                  Component.translatable(titleKey),
                  (player, state) -> provider.lines(player),
                  player -> true,
                  player -> true
               )
            );
            registered++;
         }
         Herbcraft.LOGGER.info("Loaded {} Codex guide entries from {}", registered, resourcePath);
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load Codex guide data {}", resourcePath, e);
      }
   }

   private static String string(JsonObject object, String key, String fallback) {
      return object.has(key) ? object.get(key).getAsString() : fallback;
   }

   @FunctionalInterface
   public interface GuideLineProvider {
      List<Component> lines(ServerPlayer player);
   }

   public static List<Component> missingProviderLine(Identifier kind) {
      return List.of(Component.literal("Missing guide provider: " + kind).withStyle(ChatFormatting.RED));
   }
}
