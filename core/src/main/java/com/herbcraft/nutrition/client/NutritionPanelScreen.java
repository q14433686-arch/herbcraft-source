package com.herbcraft.nutrition.client;

import com.herbcraft.knowledge.client.CodexTextLayout;
import com.herbcraft.nutrition.NutritionProfile;
import com.herbcraft.nutrition.NutritionRules;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * Nutrition panel screen with data-driven theme support.
 * Colors, sizes, dimension list, and icons are read from nutrition_theme.json.
 * Resource packs and addon mods can override the theme.
 */
public class NutritionPanelScreen extends Screen {

   public NutritionPanelScreen() {
      super(Component.translatable("nutrition.herbcraft.panel.title"));
   }

   @Override
   public boolean isPauseScreen() { return false; }

   @Override
   public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
      if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_N) {
         this.onClose();
         return true;
      }
      return super.keyPressed(event);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(extractor, mouseX, mouseY, partialTick);

      NutritionTheme t = NutritionTheme.get();
      NutritionRules rules = NutritionRules.current();
      int maxValue = rules.maxValue;

      int panelW = t.panelWidth;
      int rowH = t.effectiveRowHeight();
      int dimCount = t.dimensions.size();
      int panelH = t.computePanelHeight();

      int cx = this.width / 2;
      int cy = this.height / 2;
      int panelX = cx - panelW / 2;
      int panelY = cy - panelH / 2;

      // 1. Full-screen overlay
      extractor.fill(0, 0, this.width, this.height, t.overlayColor);

      // 2. Panel border
      extractor.fill(panelX - t.borderWidth, panelY - t.borderWidth,
         panelX + panelW + t.borderWidth, panelY + panelH + t.borderWidth, t.borderColor);
      // 3. Panel background
      extractor.fill(panelX, panelY, panelX + panelW, panelY + panelH, t.bgColor);
      // Scissor guard
      extractor.enableScissor(panelX, panelY, panelX + panelW, panelY + panelH);

      // 4. Title
      int innerTextWidth = panelW - t.padding * 2;
      Component title = Component.translatable("nutrition.herbcraft.panel.title");
      drawCenteredTrimmed(extractor, title, cx, panelY + t.padding, innerTextWidth, t.titleColor);

      // 5. Dimension rows
      int rowStartY = panelY + t.headerHeight;

      for (int i = 0; i < dimCount; i++) {
         NutritionTheme.DimensionStyle dim = t.dimensions.get(i);
         int value = NutritionClientCache.get(dim.id());
         int y = rowStartY + i * rowH;

         int iconX = panelX + t.iconOffsetX;
         int labelX = panelX + t.labelOffsetX;
         int barX = panelX + t.barOffsetX;
         int valueX = panelX + t.valueOffsetX;

         // Item icon (16x16)
         ItemStack iconStack = dim.createIconStack();
         extractor.fakeItem(iconStack, iconX, y + t.iconOffsetY);

         // Label
         Component label = Component.translatable("nutrition.herbcraft.dimension." + dim.id());
         int labelMaxWidth = Math.max(8, barX - labelX - 4);
         drawTrimmed(extractor, label, labelX, y + 1, labelMaxWidth, t.labelColor);

         // Bar border (1px)
         extractor.fill(barX - 1, y - 1, barX + t.barWidth + 1, y + t.barHeight + 1, t.barBorderColor);
         // Bar background
         extractor.fill(barX, y, barX + t.barWidth, y + t.barHeight, t.barBgColor);
         // Bar fill
         int fillW = maxValue > 0 ? Math.min((int)((float)value / maxValue * t.barWidth), t.barWidth) : 0;
         if (fillW > 0) {
            int barColor;
            if (value < rules.malnutritionLow) {
               barColor = t.dangerColor;
            } else if (value > rules.imbalanceHigh) {
               barColor = t.overflowColor;
            } else {
               barColor = dim.barColor();
            }
            extractor.fill(barX, y, barX + fillW, y + t.barHeight, barColor);
         }

         // Threshold marks
         if (t.showThresholdMarks && maxValue > 0) {
            drawThresholdMark(extractor, barX, y, t, rules.balancedMin, maxValue);
            drawThresholdMark(extractor, barX, y, t, rules.balancedMax, maxValue);
            drawThresholdMark(extractor, barX, y, t, rules.imbalanceHigh, maxValue);
         }

         // Value text
         float percent = maxValue > 0 ? (float) value / maxValue * 100f : 0f;
         String valueText = String.format("%.1f%%", percent);
         extractor.text(this.font, valueText, valueX, y + 1, t.valueColor);
      }

      // 6. Status line
      int statusY = rowStartY + dimCount * rowH + t.statusMarginTop;
      int statusColor = NutritionClientCache.isBad() ? t.statusColorBad
         : NutritionClientCache.isCorrecting() ? t.statusColorCorrecting
         : NutritionClientCache.isGood() ? t.statusColorGood
         : t.statusColorNormal;
      Component statusText = Component.translatable("nutrition.herbcraft.panel.state")
         .append(Component.translatable(NutritionClientCache.statusTranslationKey()));
      drawCenteredWrappedLimited(extractor, statusText, cx, statusY, innerTextWidth, t.statusMaxLines, statusColor);

      // 7. Key hint
      int hintY = panelY + panelH - 14;
      Component hint = Component.translatable("nutrition.herbcraft.panel.hint");
      drawCenteredTrimmed(extractor, hint, cx, hintY, innerTextWidth, t.hintColor);
      extractor.disableScissor();
   }

   private void drawThresholdMark(GuiGraphicsExtractor extractor, int barX, int y, NutritionTheme t, int threshold, int maxValue) {
      int markX = barX + (int)((float)threshold / maxValue * t.barWidth);
      if (markX > barX && markX < barX + t.barWidth) {
         extractor.fill(markX, y, markX + t.thresholdMarkWidth, y + t.barHeight, t.thresholdMarkColor);
      }
   }

   private void drawTrimmed(GuiGraphicsExtractor extractor, Component text, int x, int y, int maxWidth, int color) {
      Component display = CodexTextLayout.trimWithEllipsis(this.font, text, Math.max(1, maxWidth));
      extractor.text(this.font, display.getVisualOrderText(), x, y, color);
   }

   private void drawCenteredTrimmed(GuiGraphicsExtractor extractor, Component text, int centerX, int y, int maxWidth, int color) {
      Component display = CodexTextLayout.trimWithEllipsis(this.font, text, Math.max(1, maxWidth));
      int x = centerX - Math.min(this.font.width(display), maxWidth) / 2;
      extractor.text(this.font, display.getVisualOrderText(), x, y, color);
   }

   private int drawCenteredWrappedLimited(GuiGraphicsExtractor extractor, Component text, int centerX, int y, int maxWidth, int maxLines, int color) {
      List<FormattedCharSequence> lines = CodexTextLayout.wrap(this.font, text, Math.max(1, maxWidth));
      int drawn = Math.min(Math.max(1, maxLines), lines.size());
      for (int i = 0; i < drawn; i++) {
         FormattedCharSequence line = lines.get(i);
         int x = centerX - Math.min(this.font.width(line), maxWidth) / 2;
         extractor.text(this.font, line, x, y, color);
         y += 10;
      }
      return y;
   }
}
