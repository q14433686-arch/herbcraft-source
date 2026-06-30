package com.herbcraft.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.data.ExternalDataResources;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class SpecialCounters {
   private static final List<SpecialCounters.CounterRule> RULES = new ArrayList<>();

   private SpecialCounters() {
   }

   public static void loadFromBundledJson() {
      RULES.clear();
      try (InputStream input = SpecialCounters.class.getClassLoader().getResourceAsStream("data/herbcraft/special_counters.json")) {
         if (input == null) {
            throw new IllegalStateException("Missing data/herbcraft/special_counters.json");
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         for (JsonElement element : root.getAsJsonArray("counters")) {
            RULES.add(parseRule(element.getAsJsonObject()));
         }
         Herbcraft.LOGGER.info("Loaded {} special counter rules", RULES.size());
      } catch (Exception e) {
         throw new RuntimeException("Failed to load Herbcraft special_counters.json", e);
      }
   }

   public static void applyExternalData(ResourceManager manager) {
      int before = RULES.size();
      ExternalDataResources.forEachJson(manager, "herbcraft/special_counters", (resourceId, root) -> {
         if (root.has("counters")) {
            for (JsonElement element : root.getAsJsonArray("counters")) {
               addExternalRule(resourceId, element.getAsJsonObject());
            }
         } else if (root.has("entries")) {
            for (JsonElement element : root.getAsJsonArray("entries")) {
               addExternalRule(resourceId, element.getAsJsonObject());
            }
         } else {
            addExternalRule(resourceId, root);
         }
      });
      if (RULES.size() != before) {
         Herbcraft.LOGGER.info("Loaded {} total special counter rules after external data merge", RULES.size());
      }
   }

   private static void addExternalRule(Identifier resourceId, JsonObject json) {
      String id = json.has("id") ? json.get("id").getAsString() : resourceId.getNamespace() + ":" + resourceId.getPath();
      if (!json.has("id")) {
         json.addProperty("id", id);
      }
      boolean replaced = RULES.removeIf(rule -> rule.id().equals(id));
      if (replaced) {
         Herbcraft.LOGGER.warn("External special counter {} replaces existing counter rule for {}", resourceId, id);
      }
      RULES.add(parseRule(json));
   }

   public static void apply(Player player, Item item, long gameTime) {
      for (CounterRule rule : RULES) {
         if (rule.matches(item)) {
            int count = ((HerbcraftPlayerData)player).herbcraft$incrementWindowCounter(rule.id(), gameTime, rule.windowTicks());
            rule.alwaysEffects().forEach(effect -> applyEffect(player, effect));
            rule.thresholdEffects().forEach(effect -> {
               if (count >= effect.minCount() && EffectResolver.roll(player, effect.chance())) {
                  applyEffect(player, effect);
               }
            });
         }
      }
   }

   private static CounterRule parseRule(JsonObject json) {
      String id = json.get("id").getAsString();
      int windowTicks = json.has("window_ticks") ? json.get("window_ticks").getAsInt() : 1200;
      List<Item> items = new ArrayList<>();
      for (JsonElement element : json.getAsJsonArray("items")) {
         Identifier itemId = Identifier.parse(element.getAsString());
         if (BuiltInRegistries.ITEM.containsKey(itemId)) {
            items.add(BuiltInRegistries.ITEM.getValue(itemId));
         }
      }
      List<CounterEffect> always = new ArrayList<>();
      if (json.has("always_effects")) {
         for (JsonElement element : json.getAsJsonArray("always_effects")) {
            always.add(parseEffect(element.getAsJsonObject(), 0));
         }
      }
      List<CounterEffect> thresholds = new ArrayList<>();
      if (json.has("threshold_effects")) {
         for (JsonElement element : json.getAsJsonArray("threshold_effects")) {
            JsonObject effect = element.getAsJsonObject();
            thresholds.add(parseEffect(effect, effect.has("min_count") ? effect.get("min_count").getAsInt() : 1));
         }
      }
      return new CounterRule(id, List.copyOf(items), windowTicks, List.copyOf(always), List.copyOf(thresholds));
   }

   private static CounterEffect parseEffect(JsonObject json, int minCount) {
      Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT
         .get(Identifier.parse(json.get("effect").getAsString()))
         .map(holder -> (Holder<MobEffect>)holder)
         .orElseThrow(() -> new IllegalStateException("Unknown effect in special_counters.json: " + json.get("effect").getAsString()));
      int durationTicks = json.has("duration_ticks") ? json.get("duration_ticks").getAsInt() : json.get("duration_seconds").getAsInt() * 20;
      int amplifier = json.has("amplifier") ? json.get("amplifier").getAsInt() : 0;
      float chance = json.has("chance") ? json.get("chance").getAsFloat() : 1.0F;
      return new CounterEffect(effect, durationTicks, amplifier, chance, minCount);
   }

   private static void applyEffect(Player player, CounterEffect effect) {
      player.addEffect(new MobEffectInstance(effect.effect(), effect.durationTicks(), effect.amplifier()));
   }

   private record CounterRule(String id, List<Item> items, int windowTicks, List<CounterEffect> alwaysEffects, List<CounterEffect> thresholdEffects) {
      boolean matches(Item item) {
         return items.contains(item);
      }
   }

   private record CounterEffect(Holder<MobEffect> effect, int durationTicks, int amplifier, float chance, int minCount) {
   }
}
