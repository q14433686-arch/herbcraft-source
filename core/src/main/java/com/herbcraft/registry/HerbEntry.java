package com.herbcraft.registry;

import com.herbcraft.logic.ConsumeSoundResolver;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;

public final class HerbEntry {
   public final Item item;
   public final int nutrition;
   public final float saturation;
   public final float consumeSeconds;
   public final boolean medicinal;
   public final boolean stackGroup;
   public final List<Holder<MobEffect>> removeEffects = new ArrayList<>();
   public final List<HerbEntry.Effect> effects = new ArrayList<>();
   public final List<HerbEntry.DamageEvent> damageEvents = new ArrayList<>();
   public final List<HerbEntry.HealEvent> healEvents = new ArrayList<>();
   public HerbEntry.FoodRoll nutritionRoll;
   public HerbEntry.FoodRoll saturationRoll;
   public String counter;
   public boolean clearPositiveEffects;
   public boolean extinguish;
   public boolean bucketRemainder;
   public boolean drink;
   public boolean shortUseProjectile;

   public HerbEntry(Item item, int nutrition, float saturation, float consumeSeconds, boolean medicinal, boolean stackGroup) {
      this.item = item;
      this.nutrition = nutrition;
      this.saturation = saturation;
      this.consumeSeconds = consumeSeconds;
      this.medicinal = medicinal;
      this.stackGroup = stackGroup;
   }

   public FoodProperties foodProperties() {
      Builder builder = new Builder().nutrition(this.nutrition).saturationModifier(this.saturation);
      if (this.medicinal) {
         builder.alwaysEdible();
      }

      return builder.build();
   }

   public Consumable consumable() {
      net.minecraft.world.item.component.Consumable.Builder builder = this.drink ? Consumables.defaultDrink() : Consumables.defaultFood();
      return builder.consumeSeconds(this.consumeSeconds).sound(ConsumeSoundResolver.soundForHerb(this)).build();
   }

   public record DamageEvent(float chance, float amount) {
   }

   public record Effect(Holder<MobEffect> effect, float chance, int amplifier, int durationTicks, boolean positive) {
   }

   public record FoodRoll(float amount, float chance) {
   }

   public record HealEvent(float chance, float amount) {
   }
}
