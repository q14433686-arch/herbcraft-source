package com.herbcraft.cuisine.client;

import com.herbcraft.cuisine.registry.CuisineItems;
import com.herbcraft.cuisine.registry.DishRegistry;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class CuisineTooltips {
   private CuisineTooltips() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register(CuisineTooltips::appendTooltip);
   }

   private static void appendTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      if (stack.is(CuisineItems.WATER_BOWL)) {
         lines.add(Component.translatable("tooltip.herbcraft_cuisine.water_bowl.nature").withStyle(ChatFormatting.GRAY));
      } else if (stack.is(CuisineItems.HODGEPODGE)) {
         lines.add(Component.translatable("tooltip.herbcraft_cuisine.hodgepodge").withStyle(ChatFormatting.GRAY));
      } else {
         for (DishRegistry.Dish dish : DishRegistry.dishes()) {
            if (dish.rawItem() != null && stack.is(dish.rawItem())) {
               lines.add(Component.translatable("tooltip.herbcraft_cuisine.raw.needs_cooking").withStyle(ChatFormatting.GRAY));
               lines.add(Component.translatable("tooltip.herbcraft_cuisine.raw.stations").withStyle(ChatFormatting.DARK_GRAY));
               return;
            }

            if (stack.is(dish.item())) {
               appendDishTooltip(dish, lines);
               return;
            }
         }
      }
   }

   private static void appendDishTooltip(DishRegistry.Dish dish, List<Component> lines) {
      if (!dish.clears().isEmpty() || dish.clearFire()) {
         MutableComponent cured = Component.empty();
         boolean first = true;

         for (Holder<MobEffect> holder : dish.clears()) {
            if (!first) {
               cured.append(Component.literal(", "));
            }

            cured.append(Component.translatable(((MobEffect)holder.value()).getDescriptionId()));
            first = false;
         }

         if (dish.clearFire()) {
            if (!first) {
               cured.append(Component.literal(", "));
            }

            cured.append(Component.translatable("tooltip.herbcraft_cuisine.clears.fire"));
         }

         lines.add(Component.translatable("tooltip.herbcraft_cuisine.clears", new Object[]{cured}).withStyle(ChatFormatting.GREEN));
      }

      for (DishRegistry.DishEffect effect : dish.effects()) {
         boolean positive = ((MobEffect)effect.effect().value()).isBeneficial();
         MutableComponent line = effectLine(effect);
         if (effect.chance() < 1.0F) {
            line = line.append(Component.literal(" " + Math.round(effect.chance() * 100.0F) + "%"));
         }

         lines.add(Component.literal(" ").append(line.withStyle(positive ? ChatFormatting.BLUE : ChatFormatting.RED)));
      }
   }

   private static MutableComponent effectLine(DishRegistry.DishEffect effect) {
      MutableComponent name = Component.translatable(((MobEffect)effect.effect().value()).getDescriptionId());
      if (effect.amplifier() > 0) {
         name = Component.translatable("potion.withAmplifier", new Object[]{name, Component.translatable("potion.potency." + effect.amplifier())});
      }

      int seconds = effect.durationSeconds();
      return Component.translatable("potion.withDuration", new Object[]{name, String.format("%d:%02d", seconds / 60, seconds % 60)});
   }
}
