package com.herbcraft.cuisine.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.cuisine.item.DishItem;
import com.herbcraft.logic.ConsumeSoundResolver;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.Consumable.Builder;

public final class DishRegistry {
   private static final List<DishRegistry.Dish> DISHES = new ArrayList<>();
   private static List<DishRegistry.Dish> view = Collections.emptyList();

   private DishRegistry() {
   }

   public static List<DishRegistry.Dish> dishes() {
      return view;
   }

   public static boolean isCuratedCombo(List<Item> herbs) {
      for (DishRegistry.Dish dish : view) {
         if (dish.ingredients().size() == herbs.size() && multisetMatches(dish.ingredients(), herbs)) {
            return true;
         }
      }

      return false;
   }

   private static boolean multisetMatches(List<DishRegistry.IngredientSpec> specs, List<Item> herbs) {
      return matchRemaining(specs, herbs, new boolean[herbs.size()], 0);
   }

   private static boolean matchRemaining(List<DishRegistry.IngredientSpec> specs, List<Item> herbs, boolean[] used, int index) {
      if (index == specs.size()) {
         return true;
      } else {
         DishRegistry.IngredientSpec spec = specs.get(index);

         for (int i = 0; i < herbs.size(); i++) {
            if (!used[i] && spec.matches(herbs.get(i))) {
               used[i] = true;
               if (matchRemaining(specs, herbs, used, index + 1)) {
                  return true;
               }

               used[i] = false;
            }
         }

         return false;
      }
   }

   public static void initialize() {
      try {
         try (InputStream input = DishRegistry.class.getClassLoader().getResourceAsStream("data/herbcraft_cuisine/dishes.json")) {
            if (input == null) {
               throw new IllegalStateException("Missing data/herbcraft_cuisine/dishes.json");
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();

            for (JsonElement element : root.getAsJsonArray("dishes")) {
               register(element.getAsJsonObject(), false);
            }

            if (root.has("medicinal_dishes")) {
               for (JsonElement element : root.getAsJsonArray("medicinal_dishes")) {
                  register(element.getAsJsonObject(), true);
               }
            }

            view = List.copyOf(DISHES);
         }
      } catch (Exception var6) {
         throw new RuntimeException("Failed to load herbcraft_cuisine dishes.json", var6);
      }
   }

   private static void register(JsonObject json, boolean medicinal) {
      String id = json.get("id").getAsString();
      int nutrition = json.get("nutrition").getAsInt();
      float saturation = json.get("saturation_modifier").getAsFloat();
      boolean alwaysEdible = json.has("can_always_eat") && json.get("can_always_eat").getAsBoolean();
      boolean clearFire = json.has("clear_fire") && json.get("clear_fire").getAsBoolean();
      List<DishRegistry.DishEffect> effects = new ArrayList<>();

      for (JsonElement element : json.getAsJsonArray("effects")) {
         JsonObject effectJson = element.getAsJsonObject();
         effects.add(
            new DishRegistry.DishEffect(
               effectHolder(effectJson.get("effect").getAsString()),
               effectJson.get("amplifier").getAsInt(),
               effectJson.get("duration_seconds").getAsInt(),
               effectJson.has("chance") ? effectJson.get("chance").getAsFloat() : 1.0F
            )
         );
      }

      List<Holder<MobEffect>> clears = new ArrayList<>();
      String clearsKey = json.has("remove_effects") ? "remove_effects" : "clears";
      if (json.has(clearsKey)) {
         for (JsonElement element : json.getAsJsonArray(clearsKey)) {
            clears.add(effectHolder(element.getAsString()));
         }
      }

      List<DishRegistry.EffectImmunity> immunities = new ArrayList<>();
      if (json.has("effect_immunities")) {
         for (JsonElement element : json.getAsJsonArray("effect_immunities")) {
            JsonObject immunityJson = element.getAsJsonObject();
            immunities.add(
               new DishRegistry.EffectImmunity(Identifier.parse(immunityJson.get("effect").getAsString()), immunityJson.get("duration_seconds").getAsInt())
            );
         }
      }

      List<DishRegistry.DamageEvent> damageEvents = new ArrayList<>();
      if (json.has("damage_events")) {
         for (JsonElement element : json.getAsJsonArray("damage_events")) {
            JsonObject eventJson = element.getAsJsonObject();
            damageEvents.add(new DishRegistry.DamageEvent(eventJson.has("chance") ? eventJson.get("chance").getAsFloat() : 1.0F, eventJson.get("amount").getAsFloat()));
         }
      }

      boolean clearAllEffects = json.has("clear_all_effects") && json.get("clear_all_effects").getAsBoolean();
      boolean clearFrozen = json.has("clear_frozen") && json.get("clear_frozen").getAsBoolean();
      int igniteSeconds = json.has("ignite_seconds") ? json.get("ignite_seconds").getAsInt() : 0;

      List<Item> counterItems = new ArrayList<>();

      for (JsonElement element : json.getAsJsonArray("counter_items")) {
         counterItems.add((Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(element.getAsString())));
      }

      List<DishRegistry.IngredientSpec> ingredients = new ArrayList<>();

      for (JsonElement element : json.getAsJsonArray("ingredients")) {
         String raw = element.getAsString();
         if (raw.startsWith("#")) {
            ingredients.add(new DishRegistry.IngredientSpec(null, TagKey.create(Registries.ITEM, Identifier.parse(raw.substring(1)))));
         } else {
            ingredients.add(new DishRegistry.IngredientSpec((Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(raw)), null));
         }
      }

      float consumeSeconds = json.has("consume_seconds") ? json.get("consume_seconds").getAsFloat() : (isDrinkLikeDish(id, medicinal) ? 1.6F : 1.6F);
      Builder consumable = Consumables.defaultFood().consumeSeconds(consumeSeconds).sound(ConsumeSoundResolver.soundForCuisine(isDrinkLikeDish(id, medicinal)));

      Item rawItem = medicinal ? registerRawItem(id) : null;
      Identifier itemId = Identifier.fromNamespaceAndPath("herbcraft_cuisine", id);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, itemId);
      Item item = new DishItem(
         new Properties().setId(key).stacksTo(1).food(new FoodProperties(nutrition, saturation, alwaysEdible), consumable.build()).usingConvertsTo(Items.BOWL),
         List.copyOf(counterItems),
         clearFire,
         clearFrozen,
         clearAllEffects,
         igniteSeconds,
         List.copyOf(clears),
         List.copyOf(immunities),
         List.copyOf(effects),
         List.copyOf(damageEvents)
      );
      Registry.register(BuiltInRegistries.ITEM, key, item);
      DISHES.add(
         new DishRegistry.Dish(
            id,
            item,
            nutrition,
            saturation,
            List.copyOf(effects),
            List.copyOf(clears),
            List.copyOf(immunities),
            List.copyOf(counterItems),
            List.copyOf(ingredients),
            clearFire,
            clearFrozen,
            clearAllEffects,
            igniteSeconds,
            List.copyOf(damageEvents),
            rawItem
         )
      );
   }

   private static boolean isDrinkLikeDish(String id, boolean medicinal) {
      return medicinal
         || id.contains("soup")
         || id.contains("stew")
         || id.contains("bowl")
         || id.contains("brew")
         || id.contains("hotpot")
         || id.contains("porridge");
   }

   private static Item registerRawItem(String dishId) {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft_cuisine", "raw_" + dishId);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      Item item = new Item(new Properties().setId(key).stacksTo(16));
      Registry.register(BuiltInRegistries.ITEM, key, item);
      return item;
   }

   private static Holder<MobEffect> effectHolder(String id) {
      return BuiltInRegistries.MOB_EFFECT
         .get(Identifier.parse(id))
         .map(holder -> (Holder<MobEffect>)holder)
         .orElseThrow(() -> new IllegalStateException("Unknown effect in dishes.json: " + id));
   }

   public record Dish(
      String id,
      Item item,
      int nutrition,
      float saturation,
      List<DishRegistry.DishEffect> effects,
      List<Holder<MobEffect>> clears,
      List<DishRegistry.EffectImmunity> immunities,
      List<Item> counterItems,
      List<DishRegistry.IngredientSpec> ingredients,
      boolean clearFire,
      boolean clearFrozen,
      boolean clearAllEffects,
      int igniteSeconds,
      List<DishRegistry.DamageEvent> damageEvents,
      Item rawItem
   ) {
   }

   public record DishEffect(Holder<MobEffect> effect, int amplifier, int durationSeconds, float chance) {
   }

   public record EffectImmunity(Identifier effectId, int durationSeconds) {
   }

   public record DamageEvent(float chance, float amount) {
   }

   public record IngredientSpec(Item item, TagKey<Item> tag) {
      public boolean matches(Item candidate) {
         return this.tag != null ? BuiltInRegistries.ITEM.wrapAsHolder(candidate).is(this.tag) : candidate == this.item;
      }
   }
}
