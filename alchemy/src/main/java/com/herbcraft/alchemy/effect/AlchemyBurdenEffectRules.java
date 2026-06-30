package com.herbcraft.alchemy.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.alchemy.HerbcraftAlchemy;
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

public final class AlchemyBurdenEffectRules {
   private static final AlchemyBurdenEffectRules INSTANCE = load();
   private final Map<String, EffectRule> effects;

   private AlchemyBurdenEffectRules(Map<String, EffectRule> effects) {
      this.effects = Map.copyOf(effects);
   }

   public static AlchemyBurdenEffectRules current() {
      return INSTANCE;
   }

   public EffectRule effect(String id) {
      EffectRule rule = effects.get(id);
      if (rule == null) {
         throw new IllegalStateException("Missing alchemy burden effect rule: " + id);
      }
      return rule;
   }

   public Map<String, EffectRule> effects() {
      return effects;
   }

   private static AlchemyBurdenEffectRules load() {
      try (InputStream input = AlchemyBurdenEffectRules.class.getClassLoader().getResourceAsStream("data/herbcraft_alchemy/potion_burden_effects.json")) {
         if (input == null) {
            throw new IllegalStateException("Missing data/herbcraft_alchemy/potion_burden_effects.json");
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         Map<String, EffectRule> effects = new LinkedHashMap<>();
         JsonObject effectsJson = root.getAsJsonObject("effects");
         for (Map.Entry<String, JsonElement> entry : effectsJson.entrySet()) {
            JsonObject json = entry.getValue().getAsJsonObject();
            List<AttributeRule> attributes = new ArrayList<>();
            if (json.has("attributes")) {
               for (JsonElement attrElement : json.getAsJsonArray("attributes")) {
                  JsonObject attr = attrElement.getAsJsonObject();
                  attributes.add(new AttributeRule(
                     attribute(attr.get("attribute").getAsString()),
                     attr.get("id").getAsString(),
                     attr.get("amount").getAsDouble(),
                     operation(attr.get("operation").getAsString())
                  ));
               }
            }
            effects.put(entry.getKey(), new EffectRule(category(json.get("category").getAsString()), Integer.parseUnsignedInt(json.get("color").getAsString(), 16), List.copyOf(attributes)));
         }
         HerbcraftAlchemy.LOGGER.info("Loaded {} Alchemy burden effect rules.", effects.size());
         return new AlchemyBurdenEffectRules(effects);
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy potion_burden_effects.json", e);
      }
   }

   private static Holder<Attribute> attribute(String id) {
      return BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(id)).map(holder -> (Holder<Attribute>)holder).orElseThrow(() -> new IllegalStateException("Unknown attribute in potion_burden_effects.json: " + id));
   }

   private static AttributeModifier.Operation operation(String value) {
      return switch (value.toLowerCase(Locale.ROOT)) {
         case "add_value" -> AttributeModifier.Operation.ADD_VALUE;
         case "add_multiplied_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
         case "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
         default -> throw new IllegalStateException("Unknown attribute operation in potion_burden_effects.json: " + value);
      };
   }

   private static MobEffectCategory category(String value) {
      return switch (value.toLowerCase(Locale.ROOT)) {
         case "beneficial" -> MobEffectCategory.BENEFICIAL;
         case "neutral" -> MobEffectCategory.NEUTRAL;
         case "harmful" -> MobEffectCategory.HARMFUL;
         default -> throw new IllegalStateException("Unknown alchemy burden effect category: " + value);
      };
   }

   public record EffectRule(MobEffectCategory category, int color, List<AttributeRule> attributes) {
   }

   public record AttributeRule(Holder<Attribute> attribute, String id, double amount, AttributeModifier.Operation operation) {
   }
}
