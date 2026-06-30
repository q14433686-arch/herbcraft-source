package com.herbcraft.alchemy.effect;

import com.herbcraft.alchemy.HerbcraftAlchemy;
import com.herbcraft.effect.SimpleHerbcraftMobEffect;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;

public final class AlchemyMobEffects {
   public static final Holder<MobEffect> POTION_WARMING = register("potion_warming");
   public static final Holder<MobEffect> POTION_STRAIN = register("potion_strain");
   public static final Holder<MobEffect> POTION_OVERLOAD = register("potion_overload");
   public static final Holder<MobEffect> POTION_HEAT_STIR = register("potion_heat_stir");
   public static final Holder<MobEffect> POTION_COLD_STAGNATION = register("potion_cold_stagnation");
   public static final Holder<MobEffect> POTION_OVERDRIVE_REBOUND = register("potion_overdrive_rebound");
   public static final Holder<MobEffect> POTION_TURBID_DISTURBANCE = register("potion_turbid_disturbance");
   public static final Holder<MobEffect> POTION_QI_SCATTERING = register("potion_qi_scattering");
   public static final Holder<MobEffect> POTION_QI_STAGNATION = register("potion_qi_stagnation");
   public static final Holder<MobEffect> CLEAR_BEARING = register("clear_bearing");

   private static final Set<Identifier> BURDEN_STAGE_EFFECT_IDS = Set.of(
      id("potion_warming"),
      id("potion_strain"),
      id("potion_overload"),
      id("potion_heat_stir"),
      id("potion_cold_stagnation"),
      id("potion_overdrive_rebound"),
      id("potion_turbid_disturbance"),
      id("potion_qi_scattering"),
      id("potion_qi_stagnation")
   );

   private AlchemyMobEffects() {
   }

   public static void initialize() {
      HerbcraftAlchemy.LOGGER.info("Registered {} Herbcraft Alchemy burden effects.", AlchemyBurdenEffectRules.current().effects().size());
   }

   public static boolean isBurdenStageEffect(Holder<MobEffect> effect) {
      return effect.unwrapKey().map(key -> BURDEN_STAGE_EFFECT_IDS.contains(key.identifier())).orElse(false);
   }

   private static Holder<MobEffect> register(String name) {
      AlchemyBurdenEffectRules.EffectRule rule = AlchemyBurdenEffectRules.current().effect(name);
      SimpleHerbcraftMobEffect effect = new SimpleHerbcraftMobEffect(rule.category(), rule.color());
      for (AlchemyBurdenEffectRules.AttributeRule attribute : rule.attributes()) {
         effect.addAttributeModifier(attribute.attribute(), id(attribute.id()), attribute.amount(), attribute.operation());
      }
      ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, id(name));
      return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, key, effect);
   }

   private static Identifier id(String name) {
      return Identifier.fromNamespaceAndPath(HerbcraftAlchemy.MOD_ID, name);
   }
}
