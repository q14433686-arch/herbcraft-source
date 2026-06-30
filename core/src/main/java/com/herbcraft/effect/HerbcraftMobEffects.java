package com.herbcraft.effect;

import com.herbcraft.Herbcraft;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class HerbcraftMobEffects {
   public static final Holder<MobEffect> GRAIN_DEFICIENCY = register("grain_deficiency");
   public static final Holder<MobEffect> FLESH_DEFICIENCY = register("flesh_deficiency");
   public static final Holder<MobEffect> OIL_DEFICIENCY = register("oil_deficiency");
   public static final Holder<MobEffect> VEGE_DEFICIENCY = register("vege_deficiency");
   public static final Holder<MobEffect> FRUIT_DEFICIENCY = register("fruit_deficiency");
   public static final Holder<MobEffect> DIETARY_IMBALANCE = register("dietary_imbalance");
   public static final Holder<MobEffect> WELL_NOURISHED = register("well_nourished");

   private static final Set<Identifier> NUTRITION_EFFECT_IDS = Set.of(
      id("grain_deficiency"),
      id("flesh_deficiency"),
      id("oil_deficiency"),
      id("vege_deficiency"),
      id("fruit_deficiency"),
      id("dietary_imbalance"),
      id("well_nourished")
   );

   private HerbcraftMobEffects() {
   }

   public static void initialize() {
      Herbcraft.LOGGER.info("Registered {} Herbcraft nutrition effects.", NUTRITION_EFFECT_IDS.size());
   }

   public static boolean isNutritionEffect(Holder<MobEffect> effect) {
      return effect.unwrapKey().map(key -> NUTRITION_EFFECT_IDS.contains(key.identifier())).orElse(false);
   }

   public static boolean isNutritionEffect(MobEffect effect) {
      Identifier key = BuiltInRegistries.MOB_EFFECT.getKey(effect);
      return key != null && NUTRITION_EFFECT_IDS.contains(key);
   }

   private static Holder<MobEffect> register(String name) {
      NutritionEffectRules.EffectRule rule = NutritionEffectRules.current().effect(name);
      NutritionMobEffect effect = new NutritionMobEffect(rule);
      for (NutritionEffectRules.AttributeRule attribute : rule.attributes()) {
         effect.addAttributeModifier(attribute.attribute(), id(attribute.id()), attribute.amount(), attribute.operation());
      }
      ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, id(name));
      return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, key, effect);
   }

   private static Identifier id(String name) {
      return Identifier.fromNamespaceAndPath(Herbcraft.MOD_ID, name);
   }
}
