package com.herbcraft.knowledge.client;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class CodexTextLayout {
   private static final String ELLIPSIS = "...";

   private CodexTextLayout() {
   }

   public static Component trimWithEllipsis(Font font, Component text, int maxWidth) {
      if (maxWidth > 0 && font.width(text) > maxWidth) {
         int ellipsisWidth = font.width("...");
         String raw = text.getString();
         if (maxWidth > ellipsisWidth && !raw.isEmpty()) {
            String prefix = font.plainSubstrByWidth(raw, maxWidth - ellipsisWidth).trim();
            return Component.literal(prefix + "...").withStyle(text.getStyle());
         } else {
            return Component.literal("...").withStyle(text.getStyle());
         }
      } else {
         return text;
      }
   }

   public static boolean isTrimmed(Font font, Component text, int maxWidth) {
      return maxWidth > 0 && font.width(text) > maxWidth;
   }

   public static List<FormattedCharSequence> wrap(Font font, Component text, int maxWidth) {
      return font.split(text, Math.max(1, maxWidth));
   }

   public static int wrappedHeight(Font font, Component text, int maxWidth, int lineHeight) {
      return wrap(font, text, maxWidth).size() * lineHeight;
   }
}
