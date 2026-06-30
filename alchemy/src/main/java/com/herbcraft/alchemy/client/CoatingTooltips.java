package com.herbcraft.alchemy.client;

import com.herbcraft.alchemy.coating.CoatingComponents;
import com.herbcraft.alchemy.coating.CoatingSystem;
import com.herbcraft.alchemy.coating.ProcessedCoatingRules;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class CoatingTooltips {
   private CoatingTooltips() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register(CoatingTooltips::appendTooltip);
   }

   private static void appendTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      Optional<AlchemyRegistries.EssenceInfo> essence = AlchemyRegistries.essenceInfo(stack.getItem());
      if (essence.isPresent()) {
         appendEssenceTooltip(essence.get(), lines);
      } else {
         WeaponCoating coating = (WeaponCoating)stack.get(CoatingComponents.WEAPON_COATING);
         if (coating != null) {
            appendCoatedWeaponTooltip(coating, lines);
         }
      }
   }

   private static void appendEssenceTooltip(AlchemyRegistries.EssenceInfo info, List<Component> lines) {
      lines.add(Component.translatable("tooltip.herbcraft.essence.header").withStyle(ChatFormatting.GRAY));
      boolean toxic = false;

      for (AlchemyRegistries.EssenceEffect effect : info.effects()) {
         lines.add(
            Component.literal(" ").append(effectLine(effect, effect.durationSeconds()).withStyle(effect.positive() ? ChatFormatting.BLUE : ChatFormatting.RED))
         );
         toxic |= effect.effect().equals(MobEffects.POISON) || effect.effect().equals(MobEffects.WITHER);
      }

      if (toxic) {
         lines.add(Component.translatable("tooltip.herbcraft.essence.toxic").withStyle(ChatFormatting.DARK_RED));
      }

      lines.add(Component.translatable("tooltip.herbcraft.essence.coatable").withStyle(ChatFormatting.GRAY));
   }

   private static void appendCoatedWeaponTooltip(WeaponCoating coating, List<Component> lines) {
      Optional<AlchemyRegistries.EssenceInfo> info = AlchemyRegistries.essenceInfoById(coating.essenceId());
      if (!info.isEmpty()) {
         List<AlchemyRegistries.EssenceEffect> effects = CoatingSystem.effectsFor(info.get(), coating);
         Component essenceName = Component.translatable("item.herbcraft.essence_" + coating.essenceId());
         Component modeName = Component.translatable("tooltip.herbcraft.coating.mode." + coating.mode().getSerializedName());
         lines.add(Component.translatable("tooltip.herbcraft.coating", new Object[]{essenceName, modeName}).withStyle(ChatFormatting.GOLD));
         ProcessedCoatingRules.Style style = ProcessedCoatingRules.styleOrBase(coating.processingStyle());
         if (!"base".equals(style.id())) {
            lines.add(Component.translatable("tooltip.herbcraft.coating.processing", Component.translatable(style.langKey())).withStyle(ChatFormatting.AQUA));
         }
         lines.add(Component.translatable("tooltip.herbcraft.coating_hit").withStyle(ChatFormatting.GRAY));
         boolean selfBuff = coating.mode() == WeaponCoating.Mode.POSITIVE;

         for (AlchemyRegistries.EssenceEffect effect : effects) {
            lines.add(
               Component.literal(" ")
                  .append(
                     effectLine(effect, CoatingSystem.hitDurationTicks(effect) / 20)
                        .withStyle(!selfBuff && !effect.positive() ? ChatFormatting.RED : ChatFormatting.BLUE)
                  )
            );
         }

         if (coating.mode() != WeaponCoating.Mode.FULL) {
            lines.add(
               Component.literal(" ")
                  .append(
                     Component.translatable("tooltip.herbcraft.coating_random." + coating.mode().getSerializedName())
                        .withStyle(selfBuff ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE)
                  )
            );
         }

         int totalUses = CoatingSystem.usesFor(info.get(), coating);
         lines.add(
            Component.translatable("tooltip.herbcraft.coating_uses_frac", new Object[]{coating.remainingUses(), totalUses}).withStyle(ChatFormatting.GRAY)
         );
         lines.add(timeLine(coating).withStyle(ChatFormatting.GRAY));
      }
   }

   private static MutableComponent effectLine(AlchemyRegistries.EssenceEffect effect, int durationSeconds) {
      MutableComponent name = Component.translatable(((MobEffect)effect.effect().value()).getDescriptionId());
      if (effect.amplifier() > 0) {
         name = Component.translatable("potion.withAmplifier", new Object[]{name, Component.translatable("potion.potency." + effect.amplifier())});
      }

      return Component.translatable("potion.withDuration", new Object[]{name, formatSeconds(durationSeconds)});
   }

   private static MutableComponent timeLine(WeaponCoating coating) {
      if (coating.expiresAt() < 0L) {
         return Component.translatable(
            "tooltip.herbcraft.coating_time_idle", new Object[]{formatSeconds((int)(CoatingSystem.expiryTicksFor(coating.mode()) / 20L))}
         );
      } else {
         long now = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : coating.expiresAt();
         long secondsLeft = Math.max(0L, (coating.expiresAt() - now) / 20L);
         return Component.translatable("tooltip.herbcraft.coating_time", new Object[]{formatSeconds((int)secondsLeft)});
      }
   }

   private static String formatSeconds(int seconds) {
      return String.format("%d:%02d", seconds / 60, seconds % 60);
   }
}
