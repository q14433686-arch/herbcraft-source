package com.herbcraft.alchemy.client;

import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.potion.PotionBonusComponent;
import com.herbcraft.alchemy.potion.PotionPharmacologyProjectionRuntime;
import com.herbcraft.alchemy.potion.PotionProcessingRules;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class AdvancedPotionTooltips {
   private AdvancedPotionTooltips() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register(AdvancedPotionTooltips::appendTooltip);
   }

   private static void appendTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      AlchemyPotionComponents.PotionQuality quality = (AlchemyPotionComponents.PotionQuality)stack.get(AlchemyPotionComponents.POTION_QUALITY);
      java.util.Optional<String> path = AdvancedPotionStackInitializer.potionPath(stack);
      java.util.Optional<String> styleId = path.flatMap(AdvancedPotionStackInitializer::processingStyleId);
      if (quality != null || AdvancedPotionStackInitializer.isAdvanced(stack) || styleId.isPresent()) {
         int knowledgeState = AlchemyKnowledgeClientSupport.potionState(stack);
         String advancedPath = AdvancedPotionStackInitializer.advancedPotionPath(stack).orElse(path.orElse("advanced"));
         boolean legacyAdvancedPath = AdvancedPotionStackInitializer.isAdvancedPotionPath(advancedPath);
         String typeKey = AdvancedPotionStackInitializer.highTierTypeKey(advancedPath);
         if (legacyAdvancedPath) {
            lines.add(
               Component.translatable("tooltip.herbcraft.potion.type", new Object[]{Component.translatable("tooltip.herbcraft.potion.type." + typeKey)})
                  .withStyle(typeStyle(typeKey))
            );
         }
         styleId.flatMap(PotionProcessingRules::style).ifPresent(style ->
            lines.add(Component.translatable("tooltip.herbcraft.potion.processing", new Object[]{Component.translatable(style.langKey())}).withStyle(styleStyle(style)))
         );
         if (knowledgeState >= 3) {
            PotionPharmacologyProjectionRuntime.projection(stack).ifPresent(projection ->
               lines.add(Component.translatable("tooltip.herbcraft.potion.source_essence", new Object[]{sourceName(projection.sourceEssence())}).withStyle(ChatFormatting.GRAY))
            );
         }
         if (knowledgeState >= 4) {
            lines.add(Component.translatable("tooltip.herbcraft.potion.codex_hint_mastered").withStyle(ChatFormatting.DARK_AQUA));
         } else if (knowledgeState >= 3) {
            lines.add(Component.translatable("tooltip.herbcraft.potion.codex_hint_tasted").withStyle(ChatFormatting.DARK_GRAY));
         } else {
            lines.add(Component.translatable("tooltip.herbcraft.potion.codex_hint_locked").withStyle(ChatFormatting.DARK_GRAY));
         }
         if (quality != null) {
            String multiplier = String.format(Locale.ROOT, "%.1f", quality.durationMultiplier());
            lines.add(
               Component.translatable(
                     "tooltip.herbcraft.potion.quality",
                     new Object[]{Component.translatable("tooltip.herbcraft.potion.quality." + quality.getSerializedName()), multiplier}
                  )
                  .withStyle(qualityStyle(quality))
            );
         } else if (legacyAdvancedPath) {
            lines.add(Component.translatable("tooltip.herbcraft.potion.uninitialized").withStyle(ChatFormatting.GRAY));
         }

         PotionBonusComponent bonus = (PotionBonusComponent)stack.get(AlchemyPotionComponents.POTION_BONUS);
         if (bonus != null) {
            BuiltInRegistries.MOB_EFFECT
               .get(bonus.effectId())
               .ifPresent(
                  effect -> lines.add(
                     Component.translatable("tooltip.herbcraft.potion.bonus", new Object[]{effectLine(effect, bonus, quality)})
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                  )
               );
         }
      }
   }

   private static Component sourceName(String sourceId) {
      try {
         Identifier id = Identifier.parse(sourceId);
         String essenceId = id.getPath();
         return AlchemyRegistries.essenceInfoById(essenceId)
            .map(info -> Component.translatable(info.item().getDescriptionId()))
            .orElseGet(() -> Component.translatable(BuiltInRegistries.ITEM.getValue(id).getDescriptionId()));
      } catch (Exception ignored) {
         return Component.literal(sourceId);
      }
   }

   private static Component effectLine(Holder<MobEffect> effect, PotionBonusComponent bonus, AlchemyPotionComponents.PotionQuality quality) {
      Component name = Component.translatable(((MobEffect)effect.value()).getDescriptionId());
      if (bonus.amplifier() > 0) {
         name = Component.translatable("potion.withAmplifier", new Object[]{name, Component.translatable("potion.potency." + bonus.amplifier())});
      }

      int durationTicks = bonus.durationTicks();
      if (quality != null && !((MobEffect)effect.value()).isInstantenous()) {
         durationTicks = Math.min(9600, Math.max(1, Math.round(durationTicks * quality.durationMultiplier())));
      }

      if (!((MobEffect)effect.value()).isInstantenous()) {
         name = Component.translatable("potion.withDuration", new Object[]{name, formatTicks(durationTicks)});
      }

      return name;
   }

   private static String formatTicks(int ticks) {
      int seconds = Math.max(0, ticks / 20);
      return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
   }

   private static Component joinKeys(List<String> values, String keyPrefix) {
      net.minecraft.network.chat.MutableComponent joined = Component.empty();
      for (int i = 0; i < values.size(); i++) {
         if (i > 0) {
            joined.append(Component.literal(", "));
         }
         joined.append(Component.translatable(keyPrefix + values.get(i)));
      }
      return joined;
   }

   private static ChatFormatting typeStyle(String typeKey) {
      return !"murky".equals(typeKey) && !"t3".equals(typeKey) ? ChatFormatting.AQUA : ChatFormatting.DARK_RED;
   }

   private static ChatFormatting styleStyle(PotionProcessingRules.Style style) {
      if (style.deferred()) {
         return ChatFormatting.DARK_GRAY;
      }
      return switch (style.id()) {
         case "sustained" -> ChatFormatting.GREEN;
         case "intense" -> ChatFormatting.GOLD;
         case "purified" -> ChatFormatting.AQUA;
         default -> ChatFormatting.GRAY;
      };
   }

   private static ChatFormatting qualityStyle(AlchemyPotionComponents.PotionQuality quality) {
      return switch (quality) {
         case STANDARD -> ChatFormatting.GRAY;
         case FINE -> ChatFormatting.BLUE;
         case EXCEPTIONAL -> ChatFormatting.GOLD;
      };
   }
}
