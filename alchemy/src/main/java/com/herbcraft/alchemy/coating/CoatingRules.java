package com.herbcraft.alchemy.coating;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.alchemy.HerbcraftAlchemy;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public final class CoatingRules {
   private static CoatingRules INSTANCE = defaults();

   public final long expiryTicks;
   public final long refinedExpiryTicks;
   public final int signatureDurationSeconds;
   public final int randomDurationSeconds;
   public final int randomMinAmplifier;
   public final int randomMaxAmplifier;
   public final int hitDurationMinSeconds;
   public final int hitDurationMaxSeconds;
   public final float hitDurationMultiplier;
   public final Holder<MobEffect> positiveSignature;
   public final Holder<MobEffect> negativeSignature;
   public final List<Holder<MobEffect>> positiveRandomPool;
   public final List<Holder<MobEffect>> negativeRandomPool;
   public final List<CoatingRules.UseRule> useRules;
   public final int refinedUsesMultiplier;

   private CoatingRules(
      long expiryTicks,
      long refinedExpiryTicks,
      int signatureDurationSeconds,
      int randomDurationSeconds,
      int randomMinAmplifier,
      int randomMaxAmplifier,
      int hitDurationMinSeconds,
      int hitDurationMaxSeconds,
      float hitDurationMultiplier,
      Holder<MobEffect> positiveSignature,
      Holder<MobEffect> negativeSignature,
      List<Holder<MobEffect>> positiveRandomPool,
      List<Holder<MobEffect>> negativeRandomPool,
      List<CoatingRules.UseRule> useRules,
      int refinedUsesMultiplier
   ) {
      this.expiryTicks = expiryTicks;
      this.refinedExpiryTicks = refinedExpiryTicks;
      this.signatureDurationSeconds = signatureDurationSeconds;
      this.randomDurationSeconds = randomDurationSeconds;
      this.randomMinAmplifier = randomMinAmplifier;
      this.randomMaxAmplifier = randomMaxAmplifier;
      this.hitDurationMinSeconds = hitDurationMinSeconds;
      this.hitDurationMaxSeconds = hitDurationMaxSeconds;
      this.hitDurationMultiplier = hitDurationMultiplier;
      this.positiveSignature = positiveSignature;
      this.negativeSignature = negativeSignature;
      this.positiveRandomPool = List.copyOf(positiveRandomPool);
      this.negativeRandomPool = List.copyOf(negativeRandomPool);
      this.useRules = List.copyOf(useRules);
      this.refinedUsesMultiplier = refinedUsesMultiplier;
   }

   public static CoatingRules current() {
      return INSTANCE;
   }

   public static void loadFromBundledJson() {
      INSTANCE = defaults();
      try (InputStream input = CoatingRules.class.getClassLoader().getResourceAsStream("data/herbcraft_alchemy/coating_rules.json")) {
         if (input == null) {
            throw new IllegalStateException("Missing data/herbcraft_alchemy/coating_rules.json");
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         List<Holder<MobEffect>> positivePool = parseEffectList(root, "positive_random_pool");
         List<Holder<MobEffect>> negativePool = parseEffectList(root, "negative_random_pool");
         List<CoatingRules.UseRule> useRules = new ArrayList<>();
         for (JsonElement element : root.getAsJsonArray("use_rules")) {
            JsonObject json = element.getAsJsonObject();
            useRules.add(new CoatingRules.UseRule(json.get("max_score").getAsInt(), json.get("uses").getAsInt()));
         }
         INSTANCE = new CoatingRules(
            l(root, "expiry_ticks", 12000L),
            l(root, "refined_expiry_ticks", 14400L),
            i(root, "signature_duration_seconds", 5),
            i(root, "random_duration_seconds", 4),
            i(root, "random_min_amplifier", 1),
            i(root, "random_max_amplifier", 3),
            i(root, "hit_duration_min_seconds", 2),
            i(root, "hit_duration_max_seconds", 8),
            f(root, "hit_duration_multiplier", 0.1F),
            effect(root.get("positive_signature").getAsString()),
            effect(root.get("negative_signature").getAsString()),
            positivePool,
            negativePool,
            useRules,
            i(root, "refined_uses_multiplier", 2)
         );
         HerbcraftAlchemy.LOGGER.info("Loaded Herbcraft Alchemy coating rules.");
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy coating_rules.json", e);
      }
   }

   private static List<Holder<MobEffect>> parseEffectList(JsonObject root, String key) {
      List<Holder<MobEffect>> list = new ArrayList<>();
      for (JsonElement element : root.getAsJsonArray(key)) {
         list.add(effect(element.getAsString()));
      }
      return list;
   }

   private static CoatingRules defaults() {
      return new CoatingRules(
         12000L,
         14400L,
         5,
         4,
         1,
         3,
         2,
         8,
         0.1F,
         effect("minecraft:regeneration"),
         effect("minecraft:darkness"),
         List.of(effect("minecraft:speed"), effect("minecraft:resistance"), effect("minecraft:strength"), effect("minecraft:haste"), effect("minecraft:jump_boost"), effect("minecraft:absorption")),
         List.of(effect("minecraft:hunger"), effect("minecraft:weakness"), effect("minecraft:slowness"), effect("minecraft:nausea"), effect("minecraft:mining_fatigue"), effect("minecraft:blindness"), effect("minecraft:levitation")),
         List.of(new CoatingRules.UseRule(1, 24), new CoatingRules.UseRule(2, 20), new CoatingRules.UseRule(3, 16), new CoatingRules.UseRule(999, 12)),
         2
      );
   }

   private static Holder<MobEffect> effect(String id) {
      return BuiltInRegistries.MOB_EFFECT
         .get(Identifier.parse(id))
         .map(holder -> (Holder<MobEffect>)holder)
         .orElseThrow(() -> new IllegalStateException("Unknown effect in coating_rules.json: " + id));
   }

   private static int i(JsonObject json, String key, int fallback) {
      return json.has(key) ? json.get(key).getAsInt() : fallback;
   }

   private static long l(JsonObject json, String key, long fallback) {
      return json.has(key) ? json.get(key).getAsLong() : fallback;
   }

   private static float f(JsonObject json, String key, float fallback) {
      return json.has(key) ? json.get(key).getAsFloat() : fallback;
   }

   public record UseRule(int maxScore, int uses) {
   }
}
