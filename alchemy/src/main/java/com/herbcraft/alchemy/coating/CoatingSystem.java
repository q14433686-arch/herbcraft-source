package com.herbcraft.alchemy.coating;

import com.herbcraft.alchemy.registry.AlchemyRegistries;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CoatingSystem {
   public static final TagKey<Item> COATABLE_WEAPONS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("herbcraft", "coatable_weapons"));

   private CoatingSystem() {
   }

   public static boolean isCoatable(ItemStack stack) {
      return !stack.isEmpty() && stack.is(COATABLE_WEAPONS);
   }

   public static long expiryTicksFor(WeaponCoating.Mode mode) {
      CoatingRules rules = CoatingRules.current();
      return mode == WeaponCoating.Mode.FULL ? rules.expiryTicks : rules.refinedExpiryTicks;
   }

   public static List<AlchemyRegistries.EssenceEffect> effectsFor(AlchemyRegistries.EssenceInfo info, WeaponCoating coating) {
      return effectsFor(info, coating.mode(), coating.processingStyle());
   }

   public static List<AlchemyRegistries.EssenceEffect> effectsFor(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode) {
      return effectsFor(info, mode, "base");
   }

   public static List<AlchemyRegistries.EssenceEffect> effectsFor(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode, String processingStyle) {
      if (mode == WeaponCoating.Mode.FULL) {
         return transform(info.effects(), processingStyle);
      } else {
         CoatingRules rules = CoatingRules.current();
         boolean positive = mode == WeaponCoating.Mode.POSITIVE;
         List<AlchemyRegistries.EssenceEffect> kept = new ArrayList<>(info.effects().stream().filter(e -> e.positive() == positive).toList());
         Holder<MobEffect> signature = positive ? rules.positiveSignature : rules.negativeSignature;
         kept.removeIf(e -> e.effect().equals(signature));
         kept.add(new AlchemyRegistries.EssenceEffect(signature, 0, rules.signatureDurationSeconds, positive));
         return transform(List.copyOf(kept), processingStyle);
      }
   }

   private static List<AlchemyRegistries.EssenceEffect> transform(List<AlchemyRegistries.EssenceEffect> effects, String processingStyle) {
      ProcessedCoatingRules.Style style = ProcessedCoatingRules.styleOrBase(processingStyle);
      if ("base".equals(style.id())) {
         return effects;
      }
      return effects.stream()
         .map(effect -> new AlchemyRegistries.EssenceEffect(effect.effect(), Math.max(0, effect.amplifier() + style.amplifierDelta()), Math.max(1, Math.round(effect.durationSeconds() * style.hitDurationMultiplier())), effect.positive()))
         .toList();
   }

   public static AlchemyRegistries.EssenceEffect rollRandomEffect(WeaponCoating.Mode mode, RandomSource random) {
      CoatingRules rules = CoatingRules.current();
      boolean positive = mode == WeaponCoating.Mode.POSITIVE;
      List<Holder<MobEffect>> pool = positive ? rules.positiveRandomPool : rules.negativeRandomPool;
      Holder<MobEffect> effect = pool.get(random.nextInt(pool.size()));
      int amplifier = rules.randomMinAmplifier + random.nextInt(Math.max(1, rules.randomMaxAmplifier - rules.randomMinAmplifier + 1));
      if (effect.equals(MobEffects.LEVITATION)) {
         amplifier = Math.min(amplifier, 1);
      }

      return new AlchemyRegistries.EssenceEffect(effect, amplifier, rules.randomDurationSeconds * 10, positive);
   }

   public static int usesFor(AlchemyRegistries.EssenceInfo info, WeaponCoating coating) {
      return usesFor(info, coating.mode(), coating.processingStyle());
   }

   public static int usesFor(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode) {
      return usesFor(info, mode, "base");
   }

   public static int usesFor(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode, String processingStyle) {
      CoatingRules rules = CoatingRules.current();
      int score = info.effects().stream().mapToInt(e -> e.amplifier() + 1).sum();
      int base = rules.useRules.isEmpty() ? 12 : rules.useRules.getLast().uses();
      for (CoatingRules.UseRule rule : rules.useRules) {
         if (score <= rule.maxScore()) {
            base = rule.uses();
            break;
         }
      }

      int uses = mode == WeaponCoating.Mode.FULL ? base : base * rules.refinedUsesMultiplier;
      return Math.max(1, Math.round(uses * ProcessedCoatingRules.styleOrBase(processingStyle).usesMultiplier()));
   }

   public static int hitDurationTicks(AlchemyRegistries.EssenceEffect effect) {
      CoatingRules rules = CoatingRules.current();
      int seconds = Math.round(effect.durationSeconds() * rules.hitDurationMultiplier);
      return Math.max(rules.hitDurationMinSeconds, Math.min(rules.hitDurationMaxSeconds, seconds)) * 20;
   }

   public static void applyCoating(ItemStack weapon, AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode) {
      applyCoating(weapon, info, mode, "base");
   }

   public static void applyCoating(ItemStack weapon, AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode, String processingStyle) {
      String style = ProcessedCoatingRules.styleOrBase(processingStyle).id();
      WeaponCoating coating = new WeaponCoating(info.id(), mode, usesFor(info, mode, style), -1L, style);
      weapon.set(CoatingComponents.WEAPON_COATING, coating);
   }

   public static void removeCoating(ItemStack weapon) {
      weapon.remove(CoatingComponents.WEAPON_COATING);
   }
}
