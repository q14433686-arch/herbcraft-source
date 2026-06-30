package com.herbcraft.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class NutritionEffectRules {
   private static final NutritionEffectRules INSTANCE = load();

   public final Map<String, NutritionEffectRules.EffectRule> effects;
   public final Timing timing;

   private NutritionEffectRules(Map<String, NutritionEffectRules.EffectRule> effects, Timing timing) {
      this.effects = Map.copyOf(effects);
      this.timing = timing;
   }

   public static NutritionEffectRules current() {
      return INSTANCE;
   }

   public NutritionEffectRules.EffectRule effect(String id) {
      NutritionEffectRules.EffectRule rule = effects.get(id);
      if (rule == null) {
         throw new IllegalStateException("Missing nutrition effect rule: " + id);
      }
      return rule;
   }

   private static NutritionEffectRules load() {
      try (InputStream input = NutritionEffectRules.class.getClassLoader().getResourceAsStream("data/herbcraft/nutrition_effects.json")) {
         if (input == null) {
            throw new IllegalStateException("Missing data/herbcraft/nutrition_effects.json");
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonObject timingJson = root.getAsJsonObject("timing");
         Timing timing = new Timing(
            i(timingJson, "warning_debuff_ticks", 100),
            i(timingJson, "excess_debuff_ticks", 80),
            i(timingJson, "dietary_imbalance_relief_ticks", 1200),
            i(timingJson, "sustained_extra_ticks", 200),
            i(timingJson, "sustained_min_ticks", 400),
            i(timingJson, "well_nourished_ticks", 1800)
         );
         Map<String, EffectRule> effects = new LinkedHashMap<>();
         JsonObject effectsJson = root.getAsJsonObject("effects");
         for (Map.Entry<String, JsonElement> entry : effectsJson.entrySet()) {
            JsonObject json = entry.getValue().getAsJsonObject();
            List<AttributeRule> attributes = new ArrayList<>();
            if (json.has("attributes")) {
               for (JsonElement attrElement : json.getAsJsonArray("attributes")) {
                  JsonObject attr = attrElement.getAsJsonObject();
                  attributes.add(
                     new AttributeRule(
                        attribute(attr.get("attribute").getAsString()),
                        attr.get("id").getAsString(),
                        attr.get("amount").getAsDouble(),
                        operation(attr.get("operation").getAsString())
                     )
                  );
               }
            }
            PeriodicExhaustion exhaustion = PeriodicExhaustion.NONE;
            if (json.has("periodic_exhaustion")) {
               JsonObject e = json.getAsJsonObject("periodic_exhaustion");
               exhaustion = new PeriodicExhaustion(
                  i(e, "interval_ticks", 0),
                  f(e, "amount", 0.0F),
                  !e.has("scale_with_amplifier") || e.get("scale_with_amplifier").getAsBoolean()
               );
            }
            effects.put(
               entry.getKey(),
               new EffectRule(category(json.get("category").getAsString()), Integer.parseUnsignedInt(json.get("color").getAsString(), 16), List.copyOf(attributes), exhaustion)
            );
         }
         Herbcraft.LOGGER.info("Loaded {} Herbcraft nutrition effect rules.", effects.size());
         return new NutritionEffectRules(effects, timing);
      } catch (Exception e) {
         throw new RuntimeException("Failed to load data/herbcraft/nutrition_effects.json", e);
      }
   }

   private static Holder<Attribute> attribute(String id) {
      return BuiltInRegistries.ATTRIBUTE
         .get(Identifier.parse(id))
         .map(holder -> (Holder<Attribute>)holder)
         .orElseThrow(() -> new IllegalStateException("Unknown attribute in nutrition_effects.json: " + id));
   }

   private static AttributeModifier.Operation operation(String value) {
      return switch (value.toLowerCase(Locale.ROOT)) {
         case "add_value" -> AttributeModifier.Operation.ADD_VALUE;
         case "add_multiplied_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
         case "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
         default -> throw new IllegalStateException("Unknown attribute operation in nutrition_effects.json: " + value);
      };
   }

   private static MobEffectCategory category(String value) {
      return switch (value.toLowerCase(Locale.ROOT)) {
         case "beneficial" -> MobEffectCategory.BENEFICIAL;
         case "neutral" -> MobEffectCategory.NEUTRAL;
         case "harmful" -> MobEffectCategory.HARMFUL;
         default -> throw new IllegalStateException("Unknown nutrition effect category: " + value);
      };
   }

   private static int i(JsonObject json, String key, int fallback) {
      return json.has(key) ? json.get(key).getAsInt() : fallback;
   }

   private static float f(JsonObject json, String key, float fallback) {
      return json.has(key) ? json.get(key).getAsFloat() : fallback;
   }

   public record Timing(int warningDebuffTicks, int excessDebuffTicks, int dietaryImbalanceReliefTicks, int sustainedExtraTicks, int sustainedMinTicks, int wellNourishedTicks) {
   }

   public record EffectRule(MobEffectCategory category, int color, List<NutritionEffectRules.AttributeRule> attributes, NutritionEffectRules.PeriodicExhaustion exhaustion) {
   }

   public record AttributeRule(Holder<Attribute> attribute, String id, double amount, AttributeModifier.Operation operation) {
   }

   public record PeriodicExhaustion(int intervalTicks, float amount, boolean scaleWithAmplifier) {
      static final PeriodicExhaustion NONE = new PeriodicExhaustion(0, 0.0F, true);
   }
}
