package com.herbcraft.knowledge.client;

import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.api.HerbUsageAPI;
import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.api.HerbcraftDataAPI;
import com.herbcraft.knowledge.HerbalChapter;
import com.herbcraft.nature.NatureProfile;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class HerbNatureItemTooltips {
   private HerbNatureItemTooltips() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register(HerbNatureItemTooltips::appendTooltip);
   }

   private static void appendTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      if (Minecraft.getInstance().gui.screen() == null) {
         return;
      }
      if (stack.isEmpty()) {
         return;
      }

      Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      if (id == null) {
         return;
      }
      boolean fullHerb = HerbcraftAPI.getDefinition(stack.getItem()).isPresent();
      boolean foodNature = !fullHerb && HerbcraftDataAPI.hasFoodNatureData(id);
      if (!fullHerb && !foodNature) {
         return;
      }
      String entryId = fullHerb ? id.getPath() : HerbalChapter.foodNatureEntryId(id);

      int state = KnowledgeClientCache.state("herbal", entryId);
      lines.add(Component.translatable("jade.herbcraft.title").withStyle(ChatFormatting.GREEN));
      if (state <= 0) {
         lines.add(Component.translatable("jade.herbcraft.unknown").withStyle(ChatFormatting.GRAY));
         return;
      }

      lines.add(Component.translatable("knowledge.herbcraft.state." + stateKey(state)).withStyle(state >= 4 ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
      if (state == 1) {
         lines.add(Component.translatable("knowledge.herbcraft.encountered.description").withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.nature_unknown").withStyle(ChatFormatting.DARK_GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.traits_unknown").withStyle(ChatFormatting.DARK_GRAY));
         return;
      }
      if (state == 2) {
         lines.add(Component.translatable("book.herbcraft.entry.heard").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         lines.add(Component.translatable("knowledge.herbcraft.heard.not_tasted").withStyle(ChatFormatting.DARK_GRAY));
         return;
      }
      NatureProfile nature = HerbNatureAPI.getNatureOrFallback(stack);
      if (state == 3) {
         lines.add(HerbNatureAPI.natureLine(nature).append(Component.translatable("knowledge.herbcraft.claim.verified_by_taste").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.AQUA));
         lines.add(Component.translatable("knowledge.herbcraft.tasted.precise_unknown").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
         return;
      }
      if (state >= 4) {
         lines.add(HerbNatureAPI.natureLine(nature).withStyle(ChatFormatting.AQUA));
         if (fullHerb) {
            List<String> usages = HerbUsageAPI.usageKeys(stack.getItem());
            if (!usages.isEmpty()) {
               lines.add(HerbUsageAPI.usageLine(usages).withStyle(ChatFormatting.DARK_GRAY));
            } else {
               lines.add(Component.translatable("jade.herbcraft.usage.basic").withStyle(ChatFormatting.DARK_GRAY));
            }
         } else {
            lines.add(Component.translatable("knowledge.herbcraft.food_nature.codex_hint").withStyle(ChatFormatting.DARK_GRAY));
         }
      }
   }

   private static String stateKey(int state) {
      return switch (state) {
         case 1 -> "encountered";
         case 2 -> "heard";
         case 3 -> "tasted";
         case 4 -> "mastered";
         default -> "unknown";
      };
   }
}
