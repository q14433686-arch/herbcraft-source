package com.herbcraft.knowledge.client;

import com.herbcraft.knowledge.CodexComponents;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class CodexClientTooltips {
   private CodexClientTooltips() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register(CodexClientTooltips::appendTooltip);
   }

   private static void appendTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      CodexComponents.PageEntry page = (CodexComponents.PageEntry)stack.get(CodexComponents.PAGE_ENTRY);
      if (page != null) {
         Component subject;
         if (!page.entry().isEmpty()) {
            subject = Component.translatable("tooltip.herbcraft.page.unknown");
         } else if (page.chapter().isEmpty()) {
            subject = Component.translatable("tooltip.herbcraft.page.unknown");
         } else {
            subject = Component.translatable("book.herbcraft.chapter_name." + page.chapter());
         }

         lines.add(Component.translatable("tooltip.herbcraft.page.about", new Object[]{subject}).withStyle(ChatFormatting.GRAY));
      }
   }
}
