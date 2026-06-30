package com.herbcraft.alchemy.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.alchemy.coating.CoatingItems;
import com.herbcraft.alchemy.distillation.DistillationItems;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder.BuildCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTab.Row;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public final class AlchemyRegistries {
   public static final String NAMESPACE = "herbcraft";
   private static final int NORMAL_POSITIVE_MIN_SECONDS = 15;
   private static final int NORMAL_NEGATIVE_MIN_SECONDS = 10;
   private static final int ADVANCED_MIN_SECONDS = 30;
   private static final int MAX_DURATION_SECONDS = 480;
   private static final int MAX_NORMAL_AMPLIFIER = 1;
   private static final List<AlchemyRegistries.EssenceDefinition> ESSENCES = new ArrayList<>();
   private static final List<AlchemyRegistries.ModifierMix> MODIFIER_MIXES = new ArrayList<>();
   private static final Map<Item, AlchemyRegistries.EssenceInfo> INFO_BY_ITEM = new LinkedHashMap<>();
   private static final Map<String, AlchemyRegistries.EssenceInfo> INFO_BY_ID = new LinkedHashMap<>();
   private static final Map<String, AlchemyRegistries.EssenceSpec> ESSENCE_SPECS = loadEssenceSpecs();
   private static final List<AlchemyRegistries.WeightedPotion> WITCH_THROW_POOL = new ArrayList<>();
   private static final Map<String, List<AlchemyRegistries.EffectSpec>> SPECIAL_POTION_SPECS = loadSpecialPotionSpecs();
   private static final Map<String, Holder<Potion>> SPECIAL_POTIONS = new LinkedHashMap<>();
   public static final Item ESSENCE_DANDELION = essenceFromJson("dandelion");
   public static final Item ESSENCE_GOLDEN_DANDELION = essenceFromJson("golden_dandelion");
   public static final Item ESSENCE_POPPY = essenceFromJson("poppy");
   public static final Item ESSENCE_BLUE_ORCHID = essenceFromJson("blue_orchid");
   public static final Item ESSENCE_AZURE_BLUET = essenceFromJson("azure_bluet");
   public static final Item ESSENCE_TULIP = essenceFromJson("tulip");
   public static final Item ESSENCE_OXEYE_DAISY = essenceFromJson("oxeye_daisy");
   public static final Item ESSENCE_CORNFLOWER = essenceFromJson("cornflower");
   public static final Item ESSENCE_LILY_OF_THE_VALLEY = essenceFromJson("lily_of_the_valley");
   public static final Item ESSENCE_WITHER_ROSE = essenceFromJson("wither_rose");
   public static final Item ESSENCE_TORCHFLOWER = essenceFromJson("torchflower");
   public static final Item ESSENCE_CLOSED_EYEBLOSSOM = essenceFromJson("closed_eyeblossom");
   public static final Item ESSENCE_OPEN_EYEBLOSSOM = essenceFromJson("open_eyeblossom");
   public static final Item ESSENCE_CACTUS_FLOWER = essenceFromJson("cactus_flower");
   public static final Item ESSENCE_PINK_PETALS = essenceFromJson("pink_petals");
   public static final Item ESSENCE_SUNFLOWER = essenceFromJson("sunflower");
   public static final Item ESSENCE_LILAC = essenceFromJson("lilac");
   public static final Item ESSENCE_ROSE_BUSH = essenceFromJson("rose_bush");
   public static final Item ESSENCE_PEONY = essenceFromJson("peony");
   public static final Item ESSENCE_PITCHER_PLANT = essenceFromJson("pitcher_plant");
   public static final Item ESSENCE_FERN = essenceFromJson("fern");
   public static final Item ESSENCE_SPORE_BLOSSOM = essenceFromJson("spore_blossom");
   public static final Item ESSENCE_COCOA_BEANS = essenceFromJson("cocoa_beans");
   public static final Item ESSENCE_OAK_LEAVES = essenceFromJson("oak_leaves");
   public static final Item ESSENCE_SPRUCE_LEAVES = essenceFromJson("spruce_leaves");
   public static final Item ESSENCE_BIRCH_LEAVES = essenceFromJson("birch_leaves");
   public static final Item ESSENCE_ACACIA_LEAVES = essenceFromJson("acacia_leaves");
   public static final Item ESSENCE_MANGROVE_LEAVES = essenceFromJson("mangrove_leaves");
   public static final Item ESSENCE_CHERRY_LEAVES = essenceFromJson("cherry_leaves");
   public static final Item ESSENCE_PALE_OAK_LEAVES = essenceFromJson("pale_oak_leaves");
   public static final Item ESSENCE_AZALEA_LEAVES = essenceFromJson("azalea_leaves");
   public static final Item ESSENCE_FLOWERING_AZALEA_LEAVES = essenceFromJson("flowering_azalea_leaves");
   public static final Item ESSENCE_CHERRY_SAPLING = essenceFromJson("cherry_sapling");
   public static final Item ESSENCE_AZALEA = essenceFromJson("azalea");
   public static final Item ESSENCE_FLOWERING_AZALEA = essenceFromJson("flowering_azalea");
   public static final Item ESSENCE_KELP = essenceFromJson("kelp");
   public static final Item ESSENCE_SEA_PICKLE = essenceFromJson("sea_pickle");
   public static final Item ESSENCE_CRIMSON_FUNGUS = essenceFromJson("crimson_fungus");
   public static final Item ESSENCE_WARPED_FUNGUS = essenceFromJson("warped_fungus");
   public static final Item ESSENCE_BROWN_MUSHROOM = essenceFromJson("brown_mushroom");
   private static Reference<Potion> FLOWERING_AZALEA_MURKY;
   private static Reference<Potion> LILY_OF_THE_VALLEY_MURKY;
   private static Reference<Potion> WITHER_ROSE_BASE;
   private static Reference<Potion> UNDEAD_VENOM = registerSpecialPotion("undead_venom");
   private static Reference<Potion> CARDIAC_TOXIN = registerSpecialPotion("cardiac_toxin");
   private static Reference<Potion> WITHER_VENOM_III;

   public static final Reference<Potion> WITCH_TULIP_MURKY = registerSpecialPotion("witch_tulip_murky");
   public static final Reference<Potion> WITCH_AZURE_BLUET_MURKY = registerSpecialPotion("witch_azure_bluet_murky");
   public static final Reference<Potion> WITCH_PITCHER_PLANT_MURKY = registerSpecialPotion("witch_pitcher_plant_murky");
   public static final Reference<Potion> WITCH_AZALEA_MURKY = registerSpecialPotion("witch_azalea_murky");
   public static final Reference<Potion> WITCH_CLOSED_EYEBLOSSOM_MURKY = registerSpecialPotion("witch_closed_eyeblossom_murky");
   public static final CreativeModeTab ALCHEMY_TAB = (CreativeModeTab)Registry.register(
      BuiltInRegistries.CREATIVE_MODE_TAB,
      Identifier.fromNamespaceAndPath("herbcraft", "alchemy"),
      CreativeModeTab.builder(Row.TOP, 0)
         .title(Component.translatable("itemGroup.herbcraft.alchemy"))
         .icon(() -> new ItemStack(ESSENCE_DANDELION))
         .displayItems((parameters, output) -> {
            ESSENCES.forEach(definition -> output.accept(definition.item));
            output.accept(DistillationItems.DISTILLED_ESSENCE);
            output.accept(CoatingItems.BLANK_ADHESIVE);
            output.accept(CoatingItems.AMETHYST_ADHESIVE);
            output.accept(CoatingItems.ECHO_ADHESIVE);
         })
         .build()
   );

   private AlchemyRegistries() {
   }

   public static List<Item> essenceItems() {
      buildInfoMaps();
      return List.copyOf(INFO_BY_ITEM.keySet());
   }

   public static List<AlchemyRegistries.EssenceInfo> essenceInfos() {
      buildInfoMaps();
      return List.copyOf(INFO_BY_ID.values());
   }

   public static Optional<AlchemyRegistries.EssenceInfo> essenceInfo(Item item) {
      buildInfoMaps();
      return Optional.ofNullable(INFO_BY_ITEM.get(item));
   }

   public static Optional<AlchemyRegistries.EssenceInfo> essenceInfoById(String id) {
      buildInfoMaps();
      return Optional.ofNullable(INFO_BY_ID.get(id));
   }

   private static synchronized void buildInfoMaps() {
      if (INFO_BY_ITEM.isEmpty()) {
         for (AlchemyRegistries.EssenceDefinition definition : ESSENCES) {
            List<AlchemyRegistries.EssenceEffect> effects = definition.effects
               .stream()
               .map(e -> new AlchemyRegistries.EssenceEffect(e.effect(), e.amplifier(), e.durationSeconds(), e.positive()))
               .toList();
            AlchemyRegistries.EssenceInfo info = new AlchemyRegistries.EssenceInfo(definition.id, definition.item, effects, ESSENCE_SPECS.getOrDefault(definition.id, new AlchemyRegistries.EssenceSpec(definition.id, List.of("minecraft:" + definition.id), List.of())).sourceItems());
            INFO_BY_ITEM.put(definition.item, info);
            INFO_BY_ID.put(definition.id, info);
         }
      }
   }


   private static Map<String, List<AlchemyRegistries.EffectSpec>> loadSpecialPotionSpecs() {
      Map<String, List<AlchemyRegistries.EffectSpec>> specs = new LinkedHashMap<>();
      WITCH_THROW_POOL.clear();
      try (InputStream input = AlchemyRegistries.class.getClassLoader().getResourceAsStream("data/herbcraft_alchemy/special_potions.json")) {
         if (input == null) {
            throw new IllegalStateException("Missing data/herbcraft_alchemy/special_potions.json");
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         for (JsonElement element : root.getAsJsonArray("potions")) {
            JsonObject json = element.getAsJsonObject();
            String id = json.get("id").getAsString();
            List<AlchemyRegistries.EffectSpec> effects = new ArrayList<>();
            for (JsonElement effectElement : json.getAsJsonArray("effects")) {
               effects.add(parseEffectSpec(id, effectElement.getAsJsonObject(), "special_potions.json"));
            }
            specs.put(id, List.copyOf(effects));
         }
         if (root.has("witch_throw_pool")) {
            for (JsonElement element : root.getAsJsonArray("witch_throw_pool")) {
               JsonObject json = element.getAsJsonObject();
               WITCH_THROW_POOL.add(new AlchemyRegistries.WeightedPotion(json.get("potion").getAsString(), json.get("weight").getAsInt()));
            }
         }
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy special_potions.json", e);
      }
      return specs;
   }

   private static AlchemyRegistries.EffectSpec parseEffectSpec(String ownerId, JsonObject effectJson, String source) {
      Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT
         .get(Identifier.parse(effectJson.get("effect").getAsString()))
         .map(holder -> (Holder<MobEffect>)holder)
         .orElseThrow(() -> new IllegalStateException("Unknown effect in " + source + " for " + ownerId + ": " + effectJson.get("effect").getAsString()));
      return new AlchemyRegistries.EffectSpec(
         effect,
         effectJson.get("amplifier").getAsInt(),
         effectJson.get("duration_seconds").getAsInt(),
         effectJson.get("positive").getAsBoolean()
      );
   }

   private static Reference<Potion> registerSpecialPotion(String name) {
      List<AlchemyRegistries.EffectSpec> effects = SPECIAL_POTION_SPECS.get(name);
      if (effects == null) {
         throw new IllegalStateException("Missing special potion definition in special_potions.json: " + name);
      }
      Reference<Potion> potion = registerPotion(name, effects.toArray(AlchemyRegistries.EffectSpec[]::new));
      SPECIAL_POTIONS.put(name, potion);
      return potion;
   }

   public static Holder<Potion> pickWitchThrowPotion(net.minecraft.world.entity.monster.Witch witch) {
      int total = WITCH_THROW_POOL.stream().mapToInt(AlchemyRegistries.WeightedPotion::weight).sum();
      if (total <= 0) {
         return WITCH_TULIP_MURKY;
      }
      int roll = witch.getRandom().nextInt(total);
      for (AlchemyRegistries.WeightedPotion weighted : WITCH_THROW_POOL) {
         roll -= weighted.weight();
         if (roll < 0) {
            Holder<Potion> potion = SPECIAL_POTIONS.get(weighted.potion());
            return potion == null ? WITCH_TULIP_MURKY : potion;
         }
      }
      return WITCH_TULIP_MURKY;
   }

   public static void initialize() {
      registerExtraEssencesFromJson();
      FabricPotionBrewingBuilder.BUILD.register((BuildCallback)builder -> {
         for (AlchemyRegistries.EssenceDefinition definition : ESSENCES) {
            builder.addMix(Potions.AWKWARD, definition.item, definition.basePotion);
         }

         for (AlchemyRegistries.ModifierMix mix : MODIFIER_MIXES) {
            builder.addMix(mix.from, mix.ingredient, mix.to);
         }

         builder.addMix(FLOWERING_AZALEA_MURKY, ESSENCE_WITHER_ROSE, UNDEAD_VENOM);
         builder.addMix(LILY_OF_THE_VALLEY_MURKY, ESSENCE_CLOSED_EYEBLOSSOM, CARDIAC_TOXIN);
      });
   }


   private static void registerExtraEssencesFromJson() {
      for (String id : ESSENCE_SPECS.keySet()) {
         boolean exists = ESSENCES.stream().anyMatch(definition -> definition.id().equals(id));
         if (!exists) {
            essenceFromJson(id);
         }
      }
   }

   private static Item essenceFromJson(String id) {
      AlchemyRegistries.EssenceSpec spec = ESSENCE_SPECS.get(id);
      if (spec == null) {
         throw new IllegalStateException("Missing essence definition in essences.json: " + id);
      }
      return essence(id, spec.potionName(), spec.effects().toArray(AlchemyRegistries.EffectSpec[]::new));
   }

   private static Map<String, AlchemyRegistries.EssenceSpec> loadEssenceSpecs() {
      Map<String, AlchemyRegistries.EssenceSpec> specs = new LinkedHashMap<>();
      try (InputStream input = AlchemyRegistries.class.getClassLoader().getResourceAsStream("data/herbcraft_alchemy/essences.json")) {
         if (input == null) {
            throw new IllegalStateException("Missing data/herbcraft_alchemy/essences.json");
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         for (JsonElement element : root.getAsJsonArray("essences")) {
            JsonObject json = element.getAsJsonObject();
            String id = json.get("id").getAsString();
            String potionName = json.has("potion_name") ? json.get("potion_name").getAsString() : id;
            List<String> sourceItems = new ArrayList<>();
            if (json.has("source_items")) {
               for (JsonElement sourceElement : json.getAsJsonArray("source_items")) {
                  sourceItems.add(sourceElement.getAsString());
               }
            } else {
               sourceItems.add("minecraft:" + id);
            }
            List<AlchemyRegistries.EffectSpec> effects = new ArrayList<>();
            for (JsonElement effectElement : json.getAsJsonArray("effects")) {
               JsonObject effectJson = effectElement.getAsJsonObject();
               effects.add(parseEffectSpec(id, effectJson, "essences.json"));
            }
            specs.put(id, new AlchemyRegistries.EssenceSpec(potionName, List.copyOf(sourceItems), List.copyOf(effects)));
         }
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy essences.json", e);
      }
      return specs;
   }

   private static Item essence(String id, String potionName, AlchemyRegistries.EffectSpec... effects) {
      String itemPath = "essence_" + id;
      Item item = registerItem(itemPath, Item::new, new Properties().stacksTo(64));
      Reference<Potion> basePotion = registerPotion(potionName, stabilizedNormal(effects));
      AlchemyRegistries.EssenceDefinition definition = new AlchemyRegistries.EssenceDefinition(id, item, basePotion, List.of(effects));
      ESSENCES.add(definition);
      registerModifiers(definition);
      return item;
   }

   private static void registerModifiers(AlchemyRegistries.EssenceDefinition definition) {
      List<AlchemyRegistries.EffectSpec> normalEffects = List.of(stabilizedNormal(definition.effects.toArray(AlchemyRegistries.EffectSpec[]::new)));
      List<AlchemyRegistries.EffectSpec> positive = normalEffects.stream().filter(AlchemyRegistries.EffectSpec::positive).toList();
      List<AlchemyRegistries.EffectSpec> negative = normalEffects.stream().filter(e -> !e.positive()).toList();
      if (!positive.isEmpty()) {
         Reference<Potion> clarified = registerPotion(definition.id + "_clarified", clarifiedEffects(positive));
         MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.HONEYCOMB, clarified));
      }

      if (!negative.isEmpty()) {
         Reference<Potion> murky = registerPotion(definition.id + "_murky", murkyEffects(negative));
         MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.FERMENTED_SPIDER_EYE, murky));
         if (definition.id.equals("flowering_azalea")) {
            FLOWERING_AZALEA_MURKY = murky;
         }

         if (definition.id.equals("lily_of_the_valley")) {
            LILY_OF_THE_VALLEY_MURKY = murky;
         }
      }

      Reference<Potion> extended = registerPotion(definition.id + "_long", multiplyDuration(normalEffects, 2.0F));
      MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.REDSTONE, extended));
      if (definition.id.equals("wither_rose")) {
         WITHER_ROSE_BASE = definition.basePotion;
         WITHER_VENOM_III = registerSpecialPotion("wither_venom_iii");
         MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.GLOWSTONE_DUST, WITHER_VENOM_III));
      } else {
         Reference<Potion> strong = registerPotion(definition.id + "_strong", strengthen(normalEffects));
         MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.GLOWSTONE_DUST, strong));
      }
   }

   private static AlchemyRegistries.EffectSpec[] stabilizedNormal(AlchemyRegistries.EffectSpec... effects) {
      return List.of(effects).stream().map(AlchemyRegistries::stabilizeNormal).toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static AlchemyRegistries.EffectSpec stabilizeNormal(AlchemyRegistries.EffectSpec effect) {
      int amplifier = Math.min(effect.amplifier, 1);
      if (((MobEffect)effect.effect.value()).isInstantenous()) {
         return new AlchemyRegistries.EffectSpec(effect.effect, amplifier, effect.durationSeconds, effect.positive);
      } else {
         int minSeconds = effect.positive ? 15 : 10;
         return new AlchemyRegistries.EffectSpec(effect.effect, amplifier, clampSeconds(Math.max(effect.durationSeconds, minSeconds)), effect.positive);
      }
   }

   private static AlchemyRegistries.EffectSpec[] clarifiedEffects(List<AlchemyRegistries.EffectSpec> effects) {
      return effects.stream()
         .map(e -> new AlchemyRegistries.EffectSpec(e.effect, Math.min(e.amplifier, 1), advancedDuration(e, 3.0F), true))
         .toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static AlchemyRegistries.EffectSpec[] murkyEffects(List<AlchemyRegistries.EffectSpec> effects) {
      boolean singleNegative = effects.size() == 1;
      return effects.stream().map(e -> {
         int amplifier = Math.min(e.amplifier, 1);
         if (singleNegative && amplifier == 0) {
            amplifier = 1;
         }

         return new AlchemyRegistries.EffectSpec(e.effect, amplifier, advancedDuration(e, 3.0F), false);
      }).toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static int advancedDuration(AlchemyRegistries.EffectSpec effect, float multiplier) {
      return ((MobEffect)effect.effect.value()).isInstantenous()
         ? effect.durationSeconds
         : clampSeconds(Math.max(30, Math.round(effect.durationSeconds * multiplier)));
   }

   private static AlchemyRegistries.EffectSpec[] multiplyDuration(List<AlchemyRegistries.EffectSpec> effects, float multiplier) {
      return effects.stream()
         .map(e -> new AlchemyRegistries.EffectSpec(e.effect, e.amplifier, scaleDurationSeconds(e, multiplier), e.positive))
         .toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static AlchemyRegistries.EffectSpec[] strengthen(List<AlchemyRegistries.EffectSpec> effects) {
      return effects.stream()
         .map(
            e -> new AlchemyRegistries.EffectSpec(e.effect, e.positive ? Math.min(e.amplifier + 1, 1) : e.amplifier, scaleDurationSeconds(e, 0.5F), e.positive)
         )
         .toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static int scaleDurationSeconds(AlchemyRegistries.EffectSpec effect, float multiplier) {
      return ((MobEffect)effect.effect.value()).isInstantenous() ? effect.durationSeconds : clampSeconds(Math.round(effect.durationSeconds * multiplier));
   }

   private static int clampSeconds(int seconds) {
      return Math.max(1, Math.min(480, seconds));
   }

   private static <T extends Item> T registerItem(String name, Function<Properties, T> factory, Properties properties) {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", name);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      T item = (T)factory.apply(properties.setId(key));
      Registry.register(BuiltInRegistries.ITEM, key, item);
      return item;
   }

   private static Reference<Potion> registerPotion(String name, AlchemyRegistries.EffectSpec... effects) {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", name);
      ResourceKey<Potion> key = ResourceKey.create(Registries.POTION, id);
      MobEffectInstance[] instances = new MobEffectInstance[effects.length];

      for (int i = 0; i < effects.length; i++) {
         AlchemyRegistries.EffectSpec effect = effects[i];
         instances[i] = new MobEffectInstance(effect.effect, effect.durationSeconds * 20, effect.amplifier);
      }

      return Registry.registerForHolder(BuiltInRegistries.POTION, key, new Potion(name, instances));
   }

   private record EffectSpec(Holder<MobEffect> effect, int amplifier, int durationSeconds, boolean positive) {
   }

   private record EssenceSpec(String potionName, List<String> sourceItems, List<AlchemyRegistries.EffectSpec> effects) {
   }

   private record EssenceDefinition(String id, Item item, Reference<Potion> basePotion, List<AlchemyRegistries.EffectSpec> effects) {
   }

   public record EssenceEffect(Holder<MobEffect> effect, int amplifier, int durationSeconds, boolean positive) {
   }

   public record EssenceInfo(String id, Item item, List<AlchemyRegistries.EssenceEffect> effects, List<String> sourceItems) {
   }

   private record WeightedPotion(String potion, int weight) {
   }

   private record ModifierMix(Holder<Potion> from, Item ingredient, Holder<Potion> to) {
   }
}
