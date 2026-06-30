package com.herbcraft.guide;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import com.herbcraft.HerbcraftPlayerData;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/** Data-backed, once-per-player guide hints for early Herbcraft onboarding. */
public final class GuideHintManager {
   private static final Map<Identifier, GuideHint> HINTS_BY_ID = new LinkedHashMap<>();
   private static final Map<Identifier, List<GuideHint>> HINTS_BY_TRIGGER = new LinkedHashMap<>();

   private GuideHintManager() {}

   public static void loadFromBundledJson(String resourcePath) {
      try (InputStream input = GuideHintManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
         if (input == null) {
            Herbcraft.LOGGER.warn("Missing guide hint data {}; no hints registered from this file", resourcePath);
            return;
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonArray hints = root.getAsJsonArray("hints");
         if (hints == null) {
            return;
         }
         int loaded = 0;
         for (JsonElement element : hints) {
            if (!element.isJsonObject()) {
               continue;
            }
            JsonObject object = element.getAsJsonObject();
            Identifier id = Identifier.tryParse(string(object, "id", ""));
            Identifier trigger = Identifier.tryParse(string(object, "trigger", ""));
            String messageKey = string(object, "message_key", "");
            Delivery delivery = Delivery.from(string(object, "delivery", "chat_private"));
            boolean once = !object.has("once") || object.get("once").getAsBoolean();
            if (id == null || trigger == null || messageKey.isBlank()) {
               Herbcraft.LOGGER.warn("Skipping malformed guide hint in {}: {}", resourcePath, object);
               continue;
            }
            GuideHint hint = new GuideHint(id, trigger, messageKey, delivery, once);
            HINTS_BY_ID.put(id, hint);
            HINTS_BY_TRIGGER.computeIfAbsent(trigger, key -> new ArrayList<>()).add(hint);
            loaded++;
         }
         Herbcraft.LOGGER.info("Loaded {} Herbcraft guide hints from {}", loaded, resourcePath);
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load guide hint data {}", resourcePath, e);
      }
   }

   public static void show(ServerPlayer player, String triggerId) {
      Identifier trigger = Identifier.tryParse(triggerId);
      if (trigger != null) {
         show(player, trigger);
      }
   }

   public static void show(ServerPlayer player, Identifier trigger) {
      List<GuideHint> hints = HINTS_BY_TRIGGER.get(trigger);
      if (hints == null || hints.isEmpty()) {
         return;
      }
      HerbcraftPlayerData data = (HerbcraftPlayerData) player;
      for (GuideHint hint : hints) {
         String key = hint.id().toString();
         if (hint.once() && data.herbcraft$seenGuideHints().contains(key)) {
            continue;
         }
         Component message = Component.translatable(hint.messageKey());
         if (hint.delivery() == Delivery.ACTIONBAR) {
            player.sendOverlayMessage(message);
         } else {
            player.sendSystemMessage(message);
         }
         if (hint.once()) {
            data.herbcraft$seenGuideHints().add(key);
         }
      }
   }

   private static String string(JsonObject object, String key, String fallback) {
      return object.has(key) ? object.get(key).getAsString() : fallback;
   }

   public enum Delivery {
      CHAT_PRIVATE,
      ACTIONBAR;

      static Delivery from(String value) {
         return "actionbar".equalsIgnoreCase(value) ? ACTIONBAR : CHAT_PRIVATE;
      }
   }

   private record GuideHint(Identifier id, Identifier trigger, String messageKey, Delivery delivery, boolean once) {
   }
}
