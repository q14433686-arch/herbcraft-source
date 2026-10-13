package com.herbcraft.knowledge.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data-driven theme for the Herb Codex UI.
 * Reads from assets/herbcraft/ui/codex_theme.json.
 * Resource packs can override this file to restyle the Codex.
 */
public final class CodexTheme {
   private static final Logger LOGGER = LoggerFactory.getLogger("HerbcraftCodexTheme");
   private static final Identifier LIGHT_THEME_ID = Identifier.fromNamespaceAndPath("herbcraft", "ui/codex_theme.json");
   private static final Identifier DARK_THEME_ID = Identifier.fromNamespaceAndPath("herbcraft", "ui/codex_theme_dark.json");
   private static CodexTheme INSTANCE;
   private static boolean darkMode = false;

   // Overlay
   public final int overlayColor;

   // Book
   public final int minBookWidth;
   public final int maxBookWidth;
   public final int minBookHeight;
   public final int maxBookHeight;
   public final int screenMargin;
   public final int bookBgColor;
   public final int headerColor;
   public final int headerHeight;
   public final boolean useBackgroundTexture;
   public final String backgroundTexture;
   public final boolean useHeaderTexture;
   public final String headerTexture;
   public final boolean useCornerTextures;
   public final int cornerSize;
   public final String cornerTextureTL;
   public final String cornerTextureTR;
   public final String cornerTextureBL;
   public final String cornerTextureBR;

   // Title
   public final int titleColor;
   public final boolean titleBold;
   public final int titleOffsetX;
   public final int titleOffsetY;

   // Global text rendering
   public final boolean textShadow;

   // Completion badge
   public final boolean completionBadgeEnabled;
   public final String completionFormat;
   public final int completionColor;
   public final int completionMarginRight;

   // Left panel
   public final boolean leftUseBackgroundTexture;
   public final String leftBackgroundTexture;
   public final float leftWidthRatio;
   public final int leftMinWidth;
   public final int leftMaxWidth;
   public final int leftMinRightSpace;
   public final int leftFallbackMinWidth;
   public final int leftBgColor;
   public final int leftRowHeight;
   public final int leftIndentPx;
   public final int leftPadding;
   public final int leftTextPaddingLeft;
   public final int leftTextColor;
   public final int leftSelectedColor;
   public final int leftHoverColor;
   public final String expandArrow;
   public final String collapseArrow;
   public final boolean showEntryIcons;
   public final int entryIconSize;
   // Left panel semantic text colors (replaces ChatFormatting hardcodes)
   public final int chapterColor;
   public final int sectionColor;
   public final int sectionCountColor;
   public final int entryUnknownColor;
   public final int entryTastedColor;
   public final int entryPracticedColor;
   public final int entryMasteredColor;
   public final int arrowColor;

   // Divider
   public final int dividerColor;
   public final int dividerWidth;
   public final boolean dividerUseTexture;
   public final String dividerTexture;

   // Right panel
   public final int rightPadding;
   public final int rightLineHeight;
   public final int rightTitleMaxLines;
   public final int rightSeparatorColor;
   public final int rightSeparatorHeight;
   public final int rightSeparatorMarginTop;
   public final int rightSeparatorMarginBottom;
   public final boolean showEntryItemIcon;
   public final int entryItemIconSize;
   // Right panel semantic text colors
   public final int rightTextColor;
   public final int rightTitleColor;
   public final int rightStateMasteredColor;
   public final int rightStateOtherColor;
   public final int rightHintColor;
   public final int rightUnrecordedColor;

   // Claim hitbox
   public final boolean claimExtendFullWidth;
   public final int claimMinHeight;

   // Scrollbar
   public final boolean scrollbarEnabled;
   public final int scrollbarWidth;
   public final int scrollbarTrackColor;
   public final int scrollbarThumbColor;
   public final int scrollbarThumbMinHeight;
   public final int scrollbarMarginRight;
   public final boolean scrollbarUseTexture;
   public final String scrollbarTrackTexture;
   public final String scrollbarThumbTexture;

   // Breadcrumb
   public final boolean breadcrumbEnabled;
   public final int breadcrumbHeight;
   public final String breadcrumbSeparator;
   public final int breadcrumbColor;
   public final int breadcrumbActiveColor;

   // Back button
   public final boolean backButtonEnabled;
   public final int backButtonColor;
   public final int backButtonHoverColor;

   // Nature tags
   public final boolean natureTagsEnabled;
   public final int natureTagHeight;
   public final int natureTagPaddingH;
   public final int natureTagPaddingV;
   public final int natureTagSpacing;
   private final Map<String, Integer> natureFlavorColors;
   private final Map<String, Integer> natureTemperatureColors;

   // Search
   public final boolean searchEnabled;
   public final int searchHeight;
   public final int searchBgColor;
   public final int searchBorderColor;
   public final int searchTextColor;
   public final int searchPlaceholderColor;

   private CodexTheme(JsonObject root) {
      JsonObject overlay = obj(root, "overlay");
      this.overlayColor = color(overlay, "color", 0x80000000);

      JsonObject book = obj(root, "book");
      this.minBookWidth = intVal(book, "min_width", 320);
      this.maxBookWidth = intVal(book, "max_width", 520);
      this.minBookHeight = intVal(book, "min_height", 210);
      this.maxBookHeight = intVal(book, "max_height", 330);
      this.screenMargin = intVal(book, "screen_margin", 32);
      this.bookBgColor = color(book, "background_color", 0x90181818);
      this.headerColor = color(book, "header_color", 0xA0183018);
      this.headerHeight = intVal(book, "header_height", 20);
      this.useBackgroundTexture = bool(book, "use_background_texture", false);
      this.backgroundTexture = str(book, "background_texture", "herbcraft:textures/gui/codex_background.png");
      this.useHeaderTexture = bool(book, "use_header_texture", false);
      this.headerTexture = str(book, "header_texture", "herbcraft:textures/gui/codex_header.png");
      this.useCornerTextures = bool(book, "use_corner_textures", false);
      this.cornerTextureTL = str(book, "corner_texture_tl", "herbcraft:textures/gui/codex_corner_tl.png");
      this.cornerTextureTR = str(book, "corner_texture_tr", "herbcraft:textures/gui/codex_corner_tr.png");
      this.cornerTextureBL = str(book, "corner_texture_bl", "herbcraft:textures/gui/codex_corner_bl.png");
      this.cornerTextureBR = str(book, "corner_texture_br", "herbcraft:textures/gui/codex_corner_br.png");
      this.cornerSize = intVal(book, "corner_size", 8);

      JsonObject title = obj(root, "title");
      this.titleColor = color(title, "color", 0xFF55FF55);
      this.titleBold = bool(title, "bold", true);
      this.titleOffsetX = intVal(title, "offset_x", 8);
      this.titleOffsetY = intVal(title, "offset_y", 5);

      JsonObject textObj = obj(root, "text");
      this.textShadow = bool(textObj, "shadow", true);

      JsonObject badge = obj(root, "completion_badge");
      this.completionBadgeEnabled = bool(badge, "enabled", false);
      this.completionFormat = str(badge, "format", "%d / %d");
      this.completionColor = color(badge, "color", 0xFFAAAAAA);
      this.completionMarginRight = intVal(badge, "margin_right", 8);

      JsonObject left = obj(root, "left_panel");
      this.leftWidthRatio = floatVal(left, "width_ratio", 0.34f);
      this.leftMinWidth = intVal(left, "min_width", 130);
      this.leftMaxWidth = intVal(left, "max_width", 190);
      this.leftMinRightSpace = intVal(left, "min_right_space", 150);
      this.leftFallbackMinWidth = intVal(left, "fallback_min_width", 96);
      this.leftUseBackgroundTexture = bool(left, "use_background_texture", false);
      this.leftBackgroundTexture = str(left, "background_texture", "herbcraft:textures/gui/codex_left_panel.png");
      this.leftBgColor = color(left, "background_color", 0xA0101010);
      this.leftRowHeight = intVal(left, "row_height", 12);
      this.leftIndentPx = intVal(left, "indent_px", 9);
      this.leftPadding = intVal(left, "padding", 8);
      this.leftTextPaddingLeft = intVal(left, "text_padding_left", 8);
      this.leftTextColor = color(left, "text_color", 0xFFFFFFFF);
      this.leftSelectedColor = color(left, "selected_color", 0xB0456045);
      this.leftHoverColor = color(left, "hover_color", 0xB0303030);
      this.expandArrow = str(left, "expand_arrow", "\u25b6");
      this.collapseArrow = str(left, "collapse_arrow", "\u25bc");
      this.showEntryIcons = bool(left, "show_entry_icons", false);
      this.entryIconSize = intVal(left, "entry_icon_size", 8);
      this.chapterColor = color(left, "chapter_color", 0xFFFFAA00);
      this.sectionColor = color(left, "section_color", 0xFFFFFF55);
      this.sectionCountColor = color(left, "section_count_color", 0xFF555555);
      this.entryUnknownColor = color(left, "entry_unknown_color", 0xFF555555);
      this.entryTastedColor = color(left, "entry_tasted_color", 0xFFAAAAAA);
      this.entryPracticedColor = color(left, "entry_practiced_color", 0xFFFFFFFF);
      this.entryMasteredColor = color(left, "entry_mastered_color", 0xFF55FF55);
      this.arrowColor = color(left, "arrow_color", 0xFF00AA00);

      JsonObject div = obj(root, "divider");
      this.dividerColor = color(div, "color", 0xD0D0D0D0);
      this.dividerWidth = intVal(div, "width", 1);
      this.dividerUseTexture = bool(div, "use_texture", false);
      this.dividerTexture = str(div, "texture", "herbcraft:textures/gui/codex_divider.png");

      JsonObject right = obj(root, "right_panel");
      this.rightPadding = intVal(right, "padding", 8);
      this.rightLineHeight = intVal(right, "line_height", 10);
      this.rightTitleMaxLines = intVal(right, "title_max_lines", 2);
      this.rightSeparatorColor = color(right, "separator_color", 0xFF3C8C3C);
      this.rightSeparatorHeight = intVal(right, "separator_height", 1);
      this.rightSeparatorMarginTop = intVal(right, "separator_margin_top", 3);
      this.rightSeparatorMarginBottom = intVal(right, "separator_margin_bottom", 6);
      this.showEntryItemIcon = bool(right, "show_entry_item_icon", false);
      this.entryItemIconSize = intVal(right, "entry_item_icon_size", 16);
      this.rightTextColor = color(right, "text_color", 0xFFFFFFFF);
      this.rightTitleColor = color(right, "title_color", 0xFFFFFFFF);
      this.rightStateMasteredColor = color(right, "state_mastered_color", 0xFF55FF55);
      this.rightStateOtherColor = color(right, "state_other_color", 0xFFFFFF55);
      this.rightHintColor = color(right, "hint_color", 0xFF888888);
      this.rightUnrecordedColor = color(right, "unrecorded_color", 0xFF555555);

      JsonObject claim = obj(root, "claim_hitbox");
      this.claimExtendFullWidth = bool(claim, "extend_to_full_width", true);
      this.claimMinHeight = intVal(claim, "min_height", 12);

      JsonObject scroll = obj(root, "scrollbar");
      this.scrollbarEnabled = bool(scroll, "enabled", false);
      this.scrollbarWidth = intVal(scroll, "width", 4);
      this.scrollbarTrackColor = color(scroll, "track_color", 0x40FFFFFF);
      this.scrollbarThumbColor = color(scroll, "thumb_color", 0xA0FFFFFF);
      this.scrollbarThumbMinHeight = intVal(scroll, "thumb_min_height", 12);
      this.scrollbarMarginRight = intVal(scroll, "margin_right", 1);
      this.scrollbarUseTexture = bool(scroll, "use_texture", false);
      this.scrollbarTrackTexture = str(scroll, "track_texture", "herbcraft:textures/gui/codex_scrollbar_track.png");
      this.scrollbarThumbTexture = str(scroll, "thumb_texture", "herbcraft:textures/gui/codex_scrollbar_thumb.png");

      JsonObject bc = obj(root, "breadcrumb");
      this.breadcrumbEnabled = bool(bc, "enabled", false);
      this.breadcrumbHeight = intVal(bc, "height", 12);
      this.breadcrumbSeparator = str(bc, "separator", " \u203a ");
      this.breadcrumbColor = color(bc, "color", 0xFF888888);
      this.breadcrumbActiveColor = color(bc, "active_color", 0xFF55FF55);

      JsonObject back = obj(root, "back_button");
      this.backButtonEnabled = bool(back, "enabled", false);
      this.backButtonColor = color(back, "color", 0xFF55FF55);
      this.backButtonHoverColor = color(back, "hover_color", 0xFF88FF88);

      JsonObject natureTags = obj(root, "nature_tags");
      this.natureTagsEnabled = bool(natureTags, "enabled", false);
      this.natureTagHeight = intVal(natureTags, "tag_height", 10);
      this.natureTagPaddingH = intVal(natureTags, "tag_padding_h", 3);
      this.natureTagPaddingV = intVal(natureTags, "tag_padding_v", 1);
      this.natureTagSpacing = intVal(natureTags, "tag_spacing", 2);
      this.natureFlavorColors = parseColorMap(obj(natureTags, "tag_colors"));
      this.natureTemperatureColors = parseColorMap(obj(natureTags, "temperature_colors"));

      JsonObject search = obj(root, "search");
      this.searchEnabled = bool(search, "enabled", false);
      this.searchHeight = intVal(search, "height", 14);
      this.searchBgColor = color(search, "background_color", 0xC0000000);
      this.searchBorderColor = color(search, "border_color", 0xFF555555);
      this.searchTextColor = color(search, "text_color", 0xFFFFFFFF);
      this.searchPlaceholderColor = color(search, "placeholder_color", 0xFF666666);
   }

   /** Get current theme, loading from resources if needed. */
   public static CodexTheme get() {
      if (INSTANCE == null) {
         INSTANCE = load();
      }
      return INSTANCE;
   }

   /** Force reload (call when resource packs change). */
   public static void reload() {
      INSTANCE = load();
   }

   /** Toggle between light and dark theme. Returns true if now dark. */
   public static boolean toggleDarkMode() {
      darkMode = !darkMode;
      INSTANCE = load();
      LOGGER.info("Codex theme switched to {}", darkMode ? "dark" : "light");
      return darkMode;
   }

   public static boolean isDarkMode() {
      return darkMode;
   }

   private static CodexTheme load() {
      Identifier themeId = darkMode ? DARK_THEME_ID : LIGHT_THEME_ID;
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.getResourceManager() != null) {
            Optional<Resource> res = mc.getResourceManager().getResource(themeId);
            if (res.isPresent()) {
               try (InputStreamReader reader = new InputStreamReader(res.get().open(), StandardCharsets.UTF_8)) {
                  JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                  LOGGER.info("Loaded codex theme from {}", themeId);
                  return new CodexTheme(root);
               }
            }
            // Fallback: if dark theme file doesn't exist, use light
            if (darkMode) {
               res = mc.getResourceManager().getResource(LIGHT_THEME_ID);
               if (res.isPresent()) {
                  try (InputStreamReader reader = new InputStreamReader(res.get().open(), StandardCharsets.UTF_8)) {
                     JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                     LOGGER.warn("Dark theme not found, falling back to light theme");
                     return new CodexTheme(root);
                  }
               }
            }
         }
      } catch (Exception e) {
         LOGGER.warn("Failed to load codex theme, using defaults", e);
      }
      return new CodexTheme(new JsonObject());
   }

   /** Get the color for a flavor tag, or default gray. */
   public int natureFlavorColor(String flavor) {
      return natureFlavorColors.getOrDefault(flavor, 0xFF707070);
   }

   /** Get the color for a temperature tag, or default gray. */
   public int natureTemperatureColor(String temperature) {
      return natureTemperatureColors.getOrDefault(temperature, 0xFFAAAAAA);
   }

   private static Map<String, Integer> parseColorMap(JsonObject obj) {
      Map<String, Integer> map = new HashMap<>();
      if (obj != null) {
         for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            if (e.getValue().isJsonPrimitive()) {
               map.put(e.getKey(), color(obj, e.getKey(), 0xFFAAAAAA));
            }
         }
      }
      return map;
   }

   // --- JSON helpers ---

   private static JsonObject obj(JsonObject parent, String key) {
      if (parent != null && parent.has(key) && parent.get(key).isJsonObject()) {
         return parent.getAsJsonObject(key);
      }
      return new JsonObject();
   }

   private static int intVal(JsonObject obj, String key, int def) {
      if (obj != null && obj.has(key)) {
         return obj.get(key).getAsInt();
      }
      return def;
   }

   private static float floatVal(JsonObject obj, String key, float def) {
      if (obj != null && obj.has(key)) {
         return obj.get(key).getAsFloat();
      }
      return def;
   }

   private static boolean bool(JsonObject obj, String key, boolean def) {
      if (obj != null && obj.has(key)) {
         return obj.get(key).getAsBoolean();
      }
      return def;
   }

   private static String str(JsonObject obj, String key, String def) {
      if (obj != null && obj.has(key)) {
         return obj.get(key).getAsString();
      }
      return def;
   }

   private static int color(JsonObject obj, String key, int def) {
      if (obj != null && obj.has(key)) {
         String s = obj.get(key).getAsString().trim();
         try {
            if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
            else if (s.startsWith("#")) s = s.substring(1);
            long value = Long.parseUnsignedLong(s, 16);
            // MC 26.2+ GuiGraphicsExtractor#text/fill silently skip alpha == 0, so a bare
            // RGB value (6 hex digits) from a theme file must not become fully transparent.
            if (s.length() <= 6) value |= 0xFF000000L;
            return (int) value;
         } catch (NumberFormatException e) {
            return def;
         }
      }
      return def;
   }
}
