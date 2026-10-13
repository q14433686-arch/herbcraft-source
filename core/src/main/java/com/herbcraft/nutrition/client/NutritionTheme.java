package com.herbcraft.nutrition.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Data-driven theme for the Nutrition Panel UI.
 * Reads from assets/herbcraft/ui/nutrition_theme.json.
 * Resource packs can override this file to restyle the panel.
 * Addon mods can add dimension entries to extend the panel for extra dimensions.
 */
public final class NutritionTheme {
   private static final Logger LOGGER = LoggerFactory.getLogger("HerbcraftNutritionTheme");
   private static final Identifier THEME_ID = Identifier.fromNamespaceAndPath("herbcraft", "ui/nutrition_theme.json");
   private static NutritionTheme INSTANCE;

   // Overlay
   public final int overlayColor;

   // Panel
   public final int panelWidth;
   public final int headerHeight;
   public final int footerHeight;
   public final int padding;
   public final int bgColor;
   public final int borderColor;
   public final int borderWidth;
   public final int titleColor;
   public final int hintColor;
   public final int valueColor;
   public final boolean textShadow;

   // Rows
   public final int baseRowHeight;
   public final boolean autoScale;
   public final List<ScaleRule> scaleRules;
   public final int iconOffsetX;
   public final int iconOffsetY;
   public final int labelOffsetX;
   public final int barOffsetX;
   public final int valueOffsetX;
   public final int labelColor;

   // Bar
   public final int barWidth;
   public final int barHeight;
   public final int barBgColor;
   public final int barBorderColor;
   public final int dangerColor;
   public final int overflowColor;
   public final boolean showThresholdMarks;
   public final int thresholdMarkColor;
   public final int thresholdMarkWidth;

   // Status
   public final int statusMarginTop;
   public final int statusMaxLines;
   public final int statusColorNormal;
   public final int statusColorBad;
   public final int statusColorCorrecting;
   public final int statusColorGood;

   // Dimensions (ordered, extensible by addons)
   public final List<DimensionStyle> dimensions;

   public record ScaleRule(int maxDimensions, int rowHeight) {}
   public record DimensionStyle(String id, int barColor, String iconItem) {
      public ItemStack createIconStack() {
         Identifier itemId = Identifier.tryParse(iconItem);
         if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
            return new ItemStack(BuiltInRegistries.ITEM.getValue(itemId));
         }
         return new ItemStack(Items.BARRIER);
      }
   }

   private NutritionTheme(JsonObject root) {
      JsonObject overlay = obj(root, "overlay");
      this.overlayColor = color(overlay, "color", 0x80000000);

      JsonObject panel = obj(root, "panel");
      this.panelWidth = intVal(panel, "base_width", 240);
      this.headerHeight = intVal(panel, "header_height", 28);
      this.footerHeight = intVal(panel, "footer_height", 24);
      this.padding = intVal(panel, "padding", 8);
      this.bgColor = color(panel, "background_color", 0xD0101010);
      this.borderColor = color(panel, "border_color", 0xFFC0A060);
      this.borderWidth = intVal(panel, "border_width", 2);
      this.titleColor = color(panel, "title_color", 0xFFDAA520);
      this.hintColor = color(panel, "hint_color", 0xFF888888);
      this.valueColor = color(panel, "value_color", 0xFFCCCCCC);
      this.textShadow = bool(panel, "text_shadow", true);

      JsonObject rows = obj(root, "rows");
      this.baseRowHeight = intVal(rows, "height", 20);
      this.autoScale = bool(rows, "auto_scale", true);
      this.scaleRules = parseScaleRules(rows);
      this.iconOffsetX = intVal(rows, "icon_offset_x", 8);
      this.iconOffsetY = intVal(rows, "icon_offset_y", -4);
      this.labelOffsetX = intVal(rows, "label_offset_x", 30);
      this.barOffsetX = intVal(rows, "bar_offset_x", 80);
      this.valueOffsetX = intVal(rows, "value_offset_x", 186);
      this.labelColor = color(rows, "label_color", 0xFFAAAAAA);

      JsonObject bar = obj(root, "bar");
      this.barWidth = intVal(bar, "width", 100);
      this.barHeight = intVal(bar, "height", 8);
      this.barBgColor = color(bar, "background_color", 0xFF333333);
      this.barBorderColor = color(bar, "border_color", 0xFF666666);
      this.dangerColor = color(bar, "danger_color", 0xFFFF0000);
      this.overflowColor = color(bar, "overflow_color", 0xFFFFFFFF);
      this.showThresholdMarks = bool(bar, "show_threshold_marks", false);
      this.thresholdMarkColor = color(bar, "threshold_mark_color", 0x80FFFFFF);
      this.thresholdMarkWidth = intVal(bar, "threshold_mark_width", 1);

      JsonObject status = obj(root, "status");
      this.statusMarginTop = intVal(status, "margin_top", 6);
      this.statusMaxLines = intVal(status, "max_lines", 2);
      this.statusColorNormal = color(status, "color_normal", 0xFFCCCCCC);
      this.statusColorBad = color(status, "color_bad", 0xFFFF5555);
      this.statusColorCorrecting = color(status, "color_correcting", 0xFF55FFAA);
      this.statusColorGood = color(status, "color_good", 0xFF55FF55);

      this.dimensions = parseDimensions(root);
   }

   /** Compute effective row height for the current dimension count. */
   public int effectiveRowHeight() {
      if (!autoScale || scaleRules.isEmpty()) return baseRowHeight;
      int count = dimensions.size();
      for (ScaleRule rule : scaleRules) {
         if (count <= rule.maxDimensions) return rule.rowHeight;
      }
      return scaleRules.getLast().rowHeight;
   }

   /** Compute total panel height dynamically. */
   public int computePanelHeight() {
      return headerHeight + dimensions.size() * effectiveRowHeight() + statusMarginTop + statusMaxLines * 10 + footerHeight;
   }

   /** Find dimension style by ID, or null. */
   public DimensionStyle dimension(String id) {
      for (DimensionStyle d : dimensions) {
         if (d.id.equals(id)) return d;
      }
      return null;
   }

   /** Get current theme, loading from resources if needed. */
   public static NutritionTheme get() {
      if (INSTANCE == null) {
         INSTANCE = load();
      }
      return INSTANCE;
   }

   /** Force reload (call when resource packs change). */
   public static void reload() {
      INSTANCE = load();
   }

   private static NutritionTheme load() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.getResourceManager() != null) {
            Optional<Resource> res = mc.getResourceManager().getResource(THEME_ID);
            if (res.isPresent()) {
               try (InputStreamReader reader = new InputStreamReader(res.get().open(), StandardCharsets.UTF_8)) {
                  JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                  LOGGER.info("Loaded nutrition theme from {}", THEME_ID);
                  return new NutritionTheme(root);
               }
            }
         }
      } catch (Exception e) {
         LOGGER.warn("Failed to load nutrition theme, using defaults", e);
      }
      return new NutritionTheme(new JsonObject());
   }

   private static List<DimensionStyle> parseDimensions(JsonObject root) {
      List<DimensionStyle> result = new ArrayList<>();
      if (root != null && root.has("dimensions") && root.get("dimensions").isJsonArray()) {
         for (JsonElement elem : root.getAsJsonArray("dimensions")) {
            if (elem.isJsonObject()) {
               JsonObject d = elem.getAsJsonObject();
               result.add(new DimensionStyle(
                  str(d, "id", "unknown"),
                  color(d, "bar_color", 0xFFAAAAAA),
                  str(d, "icon_item", "minecraft:barrier")
               ));
            }
         }
      }
      if (result.isEmpty()) {
         // Hardcoded fallback matching current 5 dimensions
         result.add(new DimensionStyle("grain", 0xFFDAA520, "minecraft:bread"));
         result.add(new DimensionStyle("flesh", 0xFFCD5C5C, "minecraft:cooked_beef"));
         result.add(new DimensionStyle("oil", 0xFFFF8C00, "minecraft:honeycomb"));
         result.add(new DimensionStyle("vege", 0xFF32CD32, "minecraft:carrot"));
         result.add(new DimensionStyle("fruit", 0xFFDDA0DD, "minecraft:apple"));
      }
      return List.copyOf(result);
   }

   private static List<ScaleRule> parseScaleRules(JsonObject rows) {
      List<ScaleRule> rules = new ArrayList<>();
      if (rows != null && rows.has("scale_rules") && rows.get("scale_rules").isJsonArray()) {
         for (JsonElement elem : rows.getAsJsonArray("scale_rules")) {
            if (elem.isJsonObject()) {
               JsonObject r = elem.getAsJsonObject();
               rules.add(new ScaleRule(intVal(r, "max_dimensions", 99), intVal(r, "row_height", 20)));
            }
         }
      }
      if (rules.isEmpty()) {
         rules.add(new ScaleRule(5, 20));
         rules.add(new ScaleRule(6, 18));
         rules.add(new ScaleRule(7, 16));
         rules.add(new ScaleRule(99, 15));
      }
      return List.copyOf(rules);
   }

   // --- JSON helpers ---

   private static JsonObject obj(JsonObject parent, String key) {
      if (parent != null && parent.has(key) && parent.get(key).isJsonObject()) return parent.getAsJsonObject(key);
      return new JsonObject();
   }

   private static int intVal(JsonObject obj, String key, int def) {
      if (obj != null && obj.has(key)) return obj.get(key).getAsInt();
      return def;
   }

   private static boolean bool(JsonObject obj, String key, boolean def) {
      if (obj != null && obj.has(key)) return obj.get(key).getAsBoolean();
      return def;
   }

   private static String str(JsonObject obj, String key, String def) {
      if (obj != null && obj.has(key)) return obj.get(key).getAsString();
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
         } catch (NumberFormatException e) { return def; }
      }
      return def;
   }
}
