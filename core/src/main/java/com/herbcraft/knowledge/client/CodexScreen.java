package com.herbcraft.knowledge.client;

import com.herbcraft.knowledge.ClaimMarkPayload;
import com.herbcraft.knowledge.CodexSnapshotPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public class CodexScreen extends Screen {
   private static String lastViewedKey = null;
   private static final Set<String> expandedNodes = new HashSet<>();

   public static void clearClientSessionState() {
      lastViewedKey = null;
      expandedNodes.clear();
   }
   private CodexSnapshotPayload snapshot;
   private final List<CodexScreen.Row> rows = new ArrayList<>();
   private final List<CodexScreen.ClaimHitbox> claimHitboxes = new ArrayList<>();
   private double leftScroll = 0.0;
   private double rightScroll = 0.0;
   private CodexSnapshotPayload.EntrySnap selected;
   private String selectedKey;
   // Scrollbar drag state: 0 = not dragging, 1 = left scrollbar, 2 = right scrollbar
   private int draggingScrollbar = 0;
   private double dragStartY = 0;
   private double dragStartScroll = 0;

   public CodexScreen(CodexSnapshotPayload snapshot) {
      super(Component.translatable("book.herbcraft.codex.title"));
      this.snapshot = snapshot;
      this.rebuildRows();
      if (lastViewedKey != null) {
         this.selectByKey(lastViewedKey);
      }
   }

   public void replaceSnapshot(CodexSnapshotPayload snapshot) {
      this.snapshot = snapshot;
      this.rebuildRows();
      if (this.selectedKey != null) {
         this.selectByKey(this.selectedKey);
      } else if (lastViewedKey != null) {
         this.selectByKey(lastViewedKey);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
      // T key toggles light/dark theme while Codex is open
      if (event.key() == InputConstants.KEY_T) {
         CodexTheme.toggleDarkMode();
         this.rebuildRows();
         return true;
      }
      return super.keyPressed(event);
   }

   private void rebuildRows() {
      this.rows.clear();
      CodexTheme t = CodexTheme.get();
      int chapterOrdinal = 0;

      for (CodexSnapshotPayload.ChapterSnap chapter : this.snapshot.chapters()) {
         String chapterKey = "c" + ++chapterOrdinal;
         Component header = Component.translatable("book.herbcraft.chapter_format", new Object[]{chapterOrdinal, chapter.title()})
            .withColor(t.chapterColor);
         this.rows.add(new CodexScreen.Row(chapterKey, 0, this.prefixed(chapterKey, header), true, null));
         if (expandedNodes.contains(chapterKey)) {
            int sectionIndex = 0;

            for (CodexSnapshotPayload.SectionSnap section : chapter.sections()) {
               String sectionKey = chapterKey + "/s" + ++sectionIndex;
               int known = (int)section.entries().stream().filter(e -> e.state() > 0).count();
               Component sectionLabel = section.title()
                  .copy()
                  .withColor(t.sectionColor)
                  .append(Component.literal(" " + known + "/" + section.entries().size()).withColor(t.sectionCountColor));
               this.rows.add(new CodexScreen.Row(sectionKey, 1, this.prefixed(sectionKey, sectionLabel), true, null));
               if (expandedNodes.contains(sectionKey)) {
                  int entryIndex = 0;

                  for (CodexSnapshotPayload.EntrySnap entry : section.entries()) {
                     String entryKey = sectionKey + "/e" + ++entryIndex;
                     Component label = entry.state() == 0
                        ? Component.translatable("tooltip.herbcraft.page.unknown").withColor(t.entryUnknownColor)
                        : entry.title().copy().withColor(entry.state() >= 4 ? t.entryMasteredColor : entry.state() >= 3 ? t.entryPracticedColor : t.entryTastedColor);
                     this.rows.add(new CodexScreen.Row(entryKey, 2, label, false, entry));
                  }
               }
            }
         }
      }
   }

   private Component prefixed(String key, Component label) {
      CodexTheme t = CodexTheme.get();
      String arrow = expandedNodes.contains(key) ? (t.collapseArrow + " ") : (t.expandArrow + " ");
      return Component.literal(arrow).withColor(t.arrowColor).copy().append(label);
   }

   private void selectByKey(String key) {
      for (CodexScreen.Row row : this.rows) {
         if (row.key().equals(key) && row.entry() != null) {
            this.selected = row.entry();
            this.selectedKey = key;
            this.rightScroll = 0.0;
            return;
         }
      }
   }

   public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(extractor, mouseX, mouseY, partialTick);
      CodexTheme t = CodexTheme.get();
      CodexScreen.Layout layout = this.layout();
      this.leftScroll = clamp(this.leftScroll, this.maxLeftScroll(layout));
      this.rightScroll = clamp(this.rightScroll, this.maxRightScroll(layout));
      // Full-screen overlay
      extractor.fill(0, 0, this.width, this.height, t.overlayColor);
      // Book background
      if (t.useBackgroundTexture) {
         blitTiled(extractor, t.backgroundTexture, layout.x, layout.y, layout.width, layout.height, 16, 16);
      } else {
         extractor.fill(layout.x, layout.y, layout.x + layout.width, layout.y + layout.height, t.bookBgColor);
      }
      // Header bar
      if (t.useHeaderTexture) {
         blitTiled(extractor, t.headerTexture, layout.x, layout.y, layout.width, t.headerHeight, 32, 16);
      } else {
         extractor.fill(layout.x, layout.y, layout.x + layout.width, layout.y + t.headerHeight, t.headerColor);
      }
      // Left panel background
      if (t.leftUseBackgroundTexture) {
         blitTiled(extractor, t.leftBackgroundTexture, layout.leftX, layout.leftY, layout.leftWidth, layout.bottom - layout.leftY, 16, 16);
      } else {
         extractor.fill(layout.leftX, layout.leftY, layout.leftX + layout.leftWidth, layout.bottom, t.leftBgColor);
      }
      // Divider
      if (t.dividerUseTexture) {
         blitTiled(extractor, t.dividerTexture, layout.rightX - 4, layout.leftY, 4, layout.bottom - layout.leftY, 4, 32);
      } else {
         extractor.fill(layout.rightX - t.dividerWidth, layout.leftY, layout.rightX, layout.bottom, t.dividerColor);
      }
      // Corner decorations
      if (t.useCornerTextures) {
         int cs = t.cornerSize;
         blitExact(extractor, t.cornerTextureTL, layout.x, layout.y, cs, cs, cs, cs);
         blitExact(extractor, t.cornerTextureTR, layout.x + layout.width - cs, layout.y, cs, cs, cs, cs);
         blitExact(extractor, t.cornerTextureBL, layout.x, layout.y + layout.height - cs, cs, cs, cs, cs);
         blitExact(extractor, t.cornerTextureBR, layout.x + layout.width - cs, layout.y + layout.height - cs, cs, cs, cs, cs);
      }
      // Title
      this.drawTitleWrapped(
         extractor,
         this.title.copy().withColor(t.titleColor).withStyle(ChatFormatting.BOLD),
         layout.x + t.titleOffsetX,
         layout.y + t.titleOffsetY,
         layout.width - 16,
         2,
         t.titleColor
      );
      // Completion badge
      if (t.completionBadgeEnabled) {
         int totalEntries = 0;
         int knownEntries = 0;
         for (CodexSnapshotPayload.ChapterSnap ch : this.snapshot.chapters()) {
            for (CodexSnapshotPayload.SectionSnap sec : ch.sections()) {
               totalEntries += sec.entries().size();
               knownEntries += (int) sec.entries().stream().filter(e -> e.state() > 0).count();
            }
         }
         String badge = String.format(t.completionFormat, knownEntries, totalEntries);
         int badgeWidth = this.font.width(badge);
         extractor.text(this.font, badge, layout.x + layout.width - t.completionMarginRight - badgeWidth, layout.y + t.titleOffsetY, t.completionColor, t.textShadow);
      }
      // Scrollbar for left panel
      if (t.scrollbarEnabled) {
         int maxScroll = this.maxLeftScroll(layout);
         if (maxScroll > 0) {
            int trackX = layout.leftX + layout.leftWidth - t.scrollbarMarginRight - t.scrollbarWidth;
            int trackTop = layout.leftY;
            int trackBottom = layout.bottom;
            int trackHeight = trackBottom - trackTop;
            extractor.fill(trackX, trackTop, trackX + t.scrollbarWidth, trackBottom, t.scrollbarTrackColor);
            int thumbHeight = Math.max(t.scrollbarThumbMinHeight, (int)((float)(trackHeight * trackHeight) / (this.rows.size() * t.leftRowHeight + 16)));
            int thumbY = trackTop + (int)((float)this.leftScroll / maxScroll * (trackHeight - thumbHeight));
            extractor.fill(trackX, thumbY, trackX + t.scrollbarWidth, thumbY + thumbHeight, t.scrollbarThumbColor);
         }
      }
      this.renderLeftPanel(extractor, mouseX, mouseY, layout);
      this.renderRightPanel(extractor, layout);
      // Scrollbar for right panel
      if (t.scrollbarEnabled) {
         int maxScroll = this.maxRightScroll(layout);
         if (maxScroll > 0) {
            int trackX = layout.rightX + layout.rightWidth - t.scrollbarMarginRight - t.scrollbarWidth;
            int trackTop = layout.rightY;
            int trackBottom = layout.bottom;
            int trackHeight = trackBottom - trackTop;
            extractor.fill(trackX, trackTop, trackX + t.scrollbarWidth, trackBottom, t.scrollbarTrackColor);
            int contentH = this.contentHeight(layout);
            int thumbHeight = Math.max(t.scrollbarThumbMinHeight, (int)((float)(trackHeight * trackHeight) / contentH));
            int thumbY = trackTop + (int)((float)this.rightScroll / maxScroll * (trackHeight - thumbHeight));
            extractor.fill(trackX, thumbY, trackX + t.scrollbarWidth, thumbY + thumbHeight, t.scrollbarThumbColor);
         }
      }
   }

   private void renderLeftPanel(GuiGraphicsExtractor extractor, int mouseX, int mouseY, CodexScreen.Layout layout) {
      CodexTheme t = CodexTheme.get();
      extractor.enableScissor(layout.leftX, layout.leftY, layout.leftX + layout.leftWidth, layout.bottom);
      int y = layout.leftY + t.leftPadding - (int)this.leftScroll;
      int textMaxWidth = layout.leftWidth - t.leftTextPaddingLeft * 2;

      for (CodexScreen.Row row : this.rows) {
         if (y > layout.leftY - t.leftRowHeight && y < layout.bottom) {
            boolean hover = mouseX >= layout.leftX && mouseX < layout.leftX + layout.leftWidth && mouseY >= y && mouseY < y + t.leftRowHeight;
            boolean isSelected = row.key().equals(this.selectedKey);
            if (hover || isSelected) {
               extractor.fill(layout.leftX, y - 1, layout.leftX + layout.leftWidth, y + t.leftRowHeight - 1, isSelected ? t.leftSelectedColor : t.leftHoverColor);
            }

            int textX = layout.leftX + t.leftTextPaddingLeft + row.indent() * t.leftIndentPx;
            int rowMaxWidth = Math.max(8, textMaxWidth - row.indent() * t.leftIndentPx);
            Component display = CodexTextLayout.trimWithEllipsis(this.font, row.label(), rowMaxWidth);
            extractor.text(this.font, display, textX, y, t.leftTextColor, t.textShadow);
            if (hover && row.entry() != null && row.entry().state() > 0 && CodexTextLayout.isTrimmed(this.font, row.label(), rowMaxWidth)) {
               extractor.setTooltipForNextFrame(this.font, row.label(), mouseX, mouseY);
            } else if (hover && row.isNode() && CodexTextLayout.isTrimmed(this.font, row.label(), rowMaxWidth)) {
               extractor.setTooltipForNextFrame(this.font, row.label(), mouseX, mouseY);
            }
         }

         y += t.leftRowHeight;
      }

      extractor.disableScissor();
   }

   private void renderRightPanel(GuiGraphicsExtractor extractor, CodexScreen.Layout layout) {
      CodexTheme t = CodexTheme.get();
      extractor.enableScissor(layout.rightX, layout.rightY, layout.rightX + layout.rightWidth, layout.bottom);
      int rx = layout.rightX + t.rightPadding;
      int ry = layout.rightY + t.rightPadding - (int)this.rightScroll;
      int textWidth = layout.rightWidth - t.rightPadding * 2;
      this.claimHitboxes.clear();
      if (this.selected == null) {
         ry = this.drawWrapped(extractor, Component.translatable("book.herbcraft.codex.hint").withColor(t.rightHintColor), rx, ry, textWidth, t.rightHintColor);
      } else if (this.selected.state() == 0) {
         ry = this.drawWrapped(
            extractor,
            Component.translatable("book.herbcraft.codex.unrecorded").withColor(t.rightUnrecordedColor).withStyle(ChatFormatting.ITALIC),
            rx,
            ry,
            textWidth,
            t.rightUnrecordedColor
         );
      } else {
         // --- Layer 2: Item icon in entry header ---
         int headerTextX = rx;
         if (t.showEntryItemIcon && !this.selected.itemId().isEmpty()) {
            Identifier itemIdentifier = Identifier.tryParse(this.selected.itemId());
            if (itemIdentifier != null && BuiltInRegistries.ITEM.containsKey(itemIdentifier)) {
               ItemStack iconStack = new ItemStack(BuiltInRegistries.ITEM.getValue(itemIdentifier));
               extractor.fakeItem(iconStack, rx, ry - 2);
               headerTextX = rx + t.entryItemIconSize + 4;
            }
         }

         Component header = this.selected
            .title()
            .copy()
            .withColor(t.rightTitleColor)
            .withStyle(ChatFormatting.BOLD)
            .append(Component.literal("  "))
            .append(
               Component.translatable("knowledge.herbcraft.state." + stateKey(this.selected.state()))
                  .withColor(this.selected.state() == 4 ? t.rightStateMasteredColor : t.rightStateOtherColor)
            );
         int headerTextWidth = textWidth - (headerTextX - rx);
         ry = this.drawTitleWrapped(extractor, header, headerTextX, ry, headerTextWidth, t.rightTitleMaxLines, t.rightTitleColor) + t.rightSeparatorMarginTop;

         // --- Layer 2: Nature flavor tags (for tasted/mastered entries with nature data) ---
         if (t.natureTagsEnabled && this.selected.state() >= 3 && !this.selected.temperature().isEmpty()) {
            ry = this.renderNatureTags(extractor, rx, ry, textWidth, t);
         }

         extractor.fill(rx, ry, rx + textWidth, ry + t.rightSeparatorHeight, t.rightSeparatorColor);
         ry += t.rightSeparatorMarginBottom;

         java.util.Map<Integer, CodexSnapshotPayload.ClaimSnap> claimsByLine = new java.util.HashMap<>();
         for (CodexSnapshotPayload.ClaimSnap claim : this.selected.claims()) {
            claimsByLine.put(claim.lineIndex(), claim);
         }
         int lineIndex = 0;
         for (Component line : this.selected.lines()) {
            CodexSnapshotPayload.ClaimSnap claim = claimsByLine.get(lineIndex);
            if (claim != null) {
               // Claim hitbox: extend to full text width if theme says so
               int hitX2 = t.claimExtendFullWidth ? rx + textWidth : rx + 16;
               int wrappedH = CodexTextLayout.wrappedHeight(this.font, line, textWidth, t.rightLineHeight);
               int hitHeight = Math.max(t.claimMinHeight, wrappedH);
               this.claimHitboxes.add(new CodexScreen.ClaimHitbox(rx, ry, hitX2, ry + hitHeight, claim.entryKey(), claim.claimId()));
            }
            ry = this.drawWrapped(extractor, line, rx, ry, textWidth, t.rightTextColor) + 2;
            lineIndex++;
         }
      }

      extractor.disableScissor();
   }

   /**
    * Layer 2: Render nature temperature + flavor tags as colored inline labels.
    * Returns the new Y position after the tags.
    */
   private int renderNatureTags(GuiGraphicsExtractor extractor, int x, int y, int maxWidth, CodexTheme t) {
      int tagX = x;
      int tagH = t.natureTagHeight;
      int padH = t.natureTagPaddingH;
      int padV = t.natureTagPaddingV;
      int spacing = t.natureTagSpacing;

      // Temperature tag
      String temp = this.selected.temperature();
      if (!temp.isEmpty() && !"neutral".equals(temp)) {
         Component tempLabel = Component.translatable("nature.herbcraft.temperature." + temp);
         int tempColor = t.natureTemperatureColor(temp);
         int tagWidth = this.font.width(tempLabel) + padH * 2;
         extractor.fill(tagX, y, tagX + tagWidth, y + tagH, tempColor);
         extractor.text(this.font, tempLabel, tagX + padH, y + padV, 0xFFFFFFFF, t.textShadow);
         tagX += tagWidth + spacing;
      }

      // Flavor tags
      String flavorsStr = this.selected.flavors();
      if (!flavorsStr.isEmpty()) {
         for (String flavor : flavorsStr.split(",")) {
            flavor = flavor.trim();
            if (flavor.isEmpty() || "bland".equals(flavor)) continue;
            Component flavorLabel = Component.translatable("nature.herbcraft.flavor." + flavor);
            int flavorColor = t.natureFlavorColor(flavor);
            int tagWidth = this.font.width(flavorLabel) + padH * 2;
            // Wrap to next line if needed
            if (tagX + tagWidth > x + maxWidth && tagX > x) {
               tagX = x;
               y += tagH + spacing;
            }
            extractor.fill(tagX, y, tagX + tagWidth, y + tagH, flavorColor);
            extractor.text(this.font, flavorLabel, tagX + padH, y + padV, 0xFFFFFFFF, t.textShadow);
            tagX += tagWidth + spacing;
         }
      }

      return y + tagH + spacing + 2;
   }

   private int drawWrapped(GuiGraphicsExtractor extractor, Component text, int x, int y, int width, int color) {
      CodexTheme t = CodexTheme.get();
      for (FormattedCharSequence wrapped : CodexTextLayout.wrap(this.font, text, width)) {
         extractor.text(this.font, wrapped, x, y, color, t.textShadow);
         y += t.rightLineHeight;
      }

      return y;
   }

   private int drawTitleWrapped(GuiGraphicsExtractor extractor, Component text, int x, int y, int width, int maxLines, int color) {
      CodexTheme t = CodexTheme.get();
      List<FormattedCharSequence> lines = CodexTextLayout.wrap(this.font, text, width);
      int drawn = Math.min(maxLines, lines.size());

      for (int i = 0; i < drawn; i++) {
         extractor.text(this.font, lines.get(i), x, y, color, t.textShadow);
         y += t.rightLineHeight;
      }

      return y;
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      CodexTheme t = CodexTheme.get();
      CodexScreen.Layout layout = this.layout();
      if (event.button() == 0 && t.scrollbarEnabled) {
         // Check left scrollbar click
         int leftMaxScroll = this.maxLeftScroll(layout);
         if (leftMaxScroll > 0) {
            int trackX = layout.leftX + layout.leftWidth - t.scrollbarMarginRight - t.scrollbarWidth;
            if (event.x() >= trackX - 2 && event.x() <= trackX + t.scrollbarWidth + 2
                && event.y() >= layout.leftY && event.y() <= layout.bottom) {
               this.draggingScrollbar = 1;
               this.dragStartY = event.y();
               this.dragStartScroll = this.leftScroll;
               this.setDragging(true);
               return true;
            }
         }
         // Check right scrollbar click
         int rightMaxScroll = this.maxRightScroll(layout);
         if (rightMaxScroll > 0) {
            int trackX = layout.rightX + layout.rightWidth - t.scrollbarMarginRight - t.scrollbarWidth;
            if (event.x() >= trackX - 2 && event.x() <= trackX + t.scrollbarWidth + 2
                && event.y() >= layout.rightY && event.y() <= layout.bottom) {
               this.draggingScrollbar = 2;
               this.dragStartY = event.y();
               this.dragStartScroll = this.rightScroll;
               this.setDragging(true);
               return true;
            }
         }
      }
      if (event.button() == 0 && event.x() >= layout.rightX && event.x() < layout.rightX + layout.rightWidth && event.y() >= layout.rightY && event.y() <= layout.bottom) {
         for (CodexScreen.ClaimHitbox hitbox : this.claimHitboxes) {
            if (event.x() >= hitbox.x1() && event.x() <= hitbox.x2() && event.y() >= hitbox.y1() && event.y() <= hitbox.y2()) {
               ClientPlayNetworking.send(new ClaimMarkPayload(hitbox.entryKey(), hitbox.claimId()));
               return true;
            }
         }
      }
      if (event.button() == 0 && event.x() >= layout.leftX && event.x() < layout.leftX + layout.leftWidth && event.y() >= layout.leftY) {
         int index = (int)((event.y() - layout.leftY - (double)t.leftPadding + this.leftScroll) / (double)t.leftRowHeight);
         if (index >= 0 && index < this.rows.size()) {
            CodexScreen.Row row = this.rows.get(index);
            if (row.isNode()) {
               if (!expandedNodes.remove(row.key())) {
                  expandedNodes.add(row.key());
               }

               this.rebuildRows();
            } else {
               this.selected = row.entry();
               this.selectedKey = row.key();
               lastViewedKey = row.key();
               this.rightScroll = 0.0;
            }

            return true;
         }
      }

      return super.mouseClicked(event, doubled);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      if (this.draggingScrollbar != 0) {
         CodexTheme t = CodexTheme.get();
         CodexScreen.Layout layout = this.layout();
         double currentY = event.y();
         double movedPixels = currentY - this.dragStartY;
         if (this.draggingScrollbar == 1) {
            // Left scrollbar drag
            int maxScroll = this.maxLeftScroll(layout);
            int trackHeight = layout.bottom - layout.leftY;
            int totalContentHeight = this.rows.size() * t.leftRowHeight + t.leftPadding * 2;
            int thumbHeight = Math.max(t.scrollbarThumbMinHeight, (int)((float)(trackHeight * trackHeight) / totalContentHeight));
            double scrollRange = trackHeight - thumbHeight;
            if (scrollRange > 0) {
               double scrollDelta = movedPixels / scrollRange * maxScroll;
               this.leftScroll = clamp(this.dragStartScroll + scrollDelta, maxScroll);
            }
         } else {
            // Right scrollbar drag
            int maxScroll = this.maxRightScroll(layout);
            int trackHeight = layout.bottom - layout.rightY;
            int contentH = this.contentHeight(layout);
            int thumbHeight = Math.max(t.scrollbarThumbMinHeight, (int)((float)(trackHeight * trackHeight) / contentH));
            double scrollRange = trackHeight - thumbHeight;
            if (scrollRange > 0) {
               double scrollDelta = movedPixels / scrollRange * maxScroll;
               this.rightScroll = clamp(this.dragStartScroll + scrollDelta, maxScroll);
            }
         }
         return true;
      }
      return super.mouseDragged(event, deltaX, deltaY);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.draggingScrollbar != 0 && event.button() == 0) {
         this.draggingScrollbar = 0;
         this.setDragging(false);
         return true;
      }
      return super.mouseReleased(event);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
      CodexTheme t = CodexTheme.get();
      CodexScreen.Layout layout = this.layout();
      if (mouseX < layout.rightX) {
         this.leftScroll = clamp(this.leftScroll - deltaY * t.leftRowHeight * 2.0, this.maxLeftScroll(layout));
      } else {
         this.rightScroll = clamp(this.rightScroll - deltaY * t.leftRowHeight * 2.0, this.maxRightScroll(layout));
      }

      return true;
   }

   private int contentHeight(CodexScreen.Layout layout) {
      CodexTheme t = CodexTheme.get();
      int textWidth = layout.rightWidth - t.rightPadding * 2;
      if (this.selected == null) {
         return CodexTextLayout.wrappedHeight(this.font, Component.translatable("book.herbcraft.codex.hint"), textWidth, t.rightLineHeight) + t.rightPadding * 2;
      } else if (this.selected.state() == 0) {
         return CodexTextLayout.wrappedHeight(this.font, Component.translatable("book.herbcraft.codex.unrecorded"), textWidth, t.rightLineHeight) + t.rightPadding * 2;
      } else {
         int height = Math.min(t.rightTitleMaxLines, CodexTextLayout.wrap(this.font, this.selected.title(), textWidth).size()) * t.rightLineHeight + t.rightLineHeight;

         for (Component line : this.selected.lines()) {
            height += CodexTextLayout.wrappedHeight(this.font, line, textWidth, t.rightLineHeight) + 2;
         }

         return height + t.rightPadding * 2;
      }
   }

   private int maxLeftScroll(CodexScreen.Layout layout) {
      CodexTheme t = CodexTheme.get();
      return Math.max(0, this.rows.size() * t.leftRowHeight + t.leftPadding * 2 - (layout.height - t.headerHeight));
   }

   private int maxRightScroll(CodexScreen.Layout layout) {
      CodexTheme t = CodexTheme.get();
      return Math.max(0, this.contentHeight(layout) - (layout.height - t.headerHeight));
   }

   private CodexScreen.Layout layout() {
      CodexTheme t = CodexTheme.get();
      int bookWidth = clampInt(this.width - t.screenMargin, t.minBookWidth, t.maxBookWidth);
      int bookHeight = clampInt(this.height - t.screenMargin, t.minBookHeight, t.maxBookHeight);
      bookWidth = Math.min(bookWidth, this.width);
      bookHeight = Math.min(bookHeight, this.height);
      int x = (this.width - bookWidth) / 2;
      int y = (this.height - bookHeight) / 2;
      int leftWidth = clampInt((int)(bookWidth * t.leftWidthRatio), t.leftMinWidth, t.leftMaxWidth);
      if (leftWidth > bookWidth - t.leftMinRightSpace) {
         leftWidth = Math.max(t.leftFallbackMinWidth, bookWidth - t.leftMinRightSpace);
      }

      int rightX = x + leftWidth + t.dividerWidth;
      int rightWidth = bookWidth - leftWidth - t.dividerWidth;
      return new CodexScreen.Layout(x, y, bookWidth, bookHeight, x, y + t.headerHeight, leftWidth, rightX, y + t.headerHeight, rightWidth, y + bookHeight);
   }

   /**
    * Tile a texture across a rectangular area by repeating blit calls.
    */
   private static void blitTiled(GuiGraphicsExtractor extractor, String textureStr, int x, int y, int areaWidth, int areaHeight, int texWidth, int texHeight) {
      Identifier tex = Identifier.tryParse(textureStr);
      if (tex == null) return;
      for (int ty = 0; ty < areaHeight; ty += texHeight) {
         int h = Math.min(texHeight, areaHeight - ty);
         for (int tx = 0; tx < areaWidth; tx += texWidth) {
            int w = Math.min(texWidth, areaWidth - tx);
            extractor.blit(RenderPipelines.GUI_TEXTURED, tex, x + tx, y + ty, 0.0F, 0.0F, w, h, texWidth, texHeight);
         }
      }
   }

   /**
    * Blit a texture at exact size (no tiling).
    */
   private static void blitExact(GuiGraphicsExtractor extractor, String textureStr, int x, int y, int width, int height, int texWidth, int texHeight) {
      Identifier tex = Identifier.tryParse(textureStr);
      if (tex == null) return;
      extractor.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0F, 0.0F, width, height, texWidth, texHeight);
   }

   private static double clamp(double value, double max) {
      return Math.max(0.0, Math.min(value, max));
   }

   private static String stateKey(int state) {
      return switch (state) { case 1 -> "encountered"; case 2 -> "heard"; case 3 -> "tasted"; case 4 -> "mastered"; default -> "unknown"; };
   }

   private static int clampInt(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }

   private record Layout(int x, int y, int width, int height, int leftX, int leftY, int leftWidth, int rightX, int rightY, int rightWidth, int bottom) {
   }

   private record Row(String key, int indent, Component label, boolean isNode, CodexSnapshotPayload.EntrySnap entry) {
   }
   private record ClaimHitbox(int x1, int y1, int x2, int y2, String entryKey, String claimId) {
   }
}
