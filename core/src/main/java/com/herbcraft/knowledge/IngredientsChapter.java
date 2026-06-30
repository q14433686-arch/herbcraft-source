package com.herbcraft.knowledge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeDisplayState;
import com.herbcraft.data.ExternalDataResources;
import com.herbcraft.nutrition.NutritionProfile;
import com.herbcraft.nutrition.NutritionRegistry;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

/**
 * "食材录" chapter — a data-driven knowledge chapter for vanilla/non-herb foods.
 * Built-in entries are loaded from {@code data/herbcraft/ingredients.json}.
 * External entries can be added under {@code data/<namespace>/herbcraft/ingredients/*.json}.
 * Gated by eating: you must eat the food to see its nutrition info.
 * No pharmacology, no herb stack, no effects — only nutrition dimensions.
 */
public final class IngredientsChapter {
   public static final String CHAPTER_ID = "ingredients";
   private static final String DATA_PATH = "data/herbcraft/ingredients.json";

   private static final Set<String> ENTRY_PATHS = new LinkedHashSet<>();
   private static final Map<Identifier, String> ITEM_ENTRY_IDS = new LinkedHashMap<>();
   private static final Map<String, String> ENTRY_SECTIONS = new LinkedHashMap<>();
   private static final Set<Identifier> EXTERNAL_ITEM_IDS = new LinkedHashSet<>();
   private static final Set<String> EXTERNAL_ENTRY_IDS = new LinkedHashSet<>();

   private IngredientsChapter() {}

   /** Check whether a vanilla-path entry exists. Kept for old call sites and vanilla entries. */
   public static boolean hasEntry(String itemPath) {
      return ENTRY_PATHS.contains(itemPath);
   }

   public static boolean hasEntry(Identifier itemId) {
      return ITEM_ENTRY_IDS.containsKey(itemId);
   }

   public static String entryId(Identifier itemId) {
      return ITEM_ENTRY_IDS.getOrDefault(itemId, defaultEntryId(itemId));
   }

   public static Map<Identifier, String> itemEntryIds() {
      return Map.copyOf(ITEM_ENTRY_IDS);
   }

   public static void replaceClientIngredientEntries(Map<Identifier, String> entries) {
      ITEM_ENTRY_IDS.clear();
      ITEM_ENTRY_IDS.putAll(entries);
      ENTRY_PATHS.clear();
      ITEM_ENTRY_IDS.forEach((id, entryId) -> {
         if ("minecraft".equals(id.getNamespace())) {
            ENTRY_PATHS.add(id.getPath());
         }
      });
   }

   public static void register() {
      KnowledgeAPI.registerChapter(CHAPTER_ID,
         Component.translatable("book.herbcraft.chapter.ingredients"), 50);
      KnowledgeAPI.registerSection(CHAPTER_ID, "overview", Component.translatable("book.herbcraft.section.ingredients.overview"));
      KnowledgeAPI.registerEntry(
         CHAPTER_ID,
         new KnowledgeAPI.Entry(
            "overview",
            "overview",
            Component.translatable("book.herbcraft.ingredients.overview.title"),
            (player, state) -> overviewLines(),
            player -> true,
            player -> true
         )
      );

      registerEntries(loadFromJson(DATA_PATH), DATA_PATH);
   }

   public static void applyExternalData(ResourceManager manager) {
      resetItemEntriesToBundled();
      ExternalDataResources.forEachJson(manager, "herbcraft/ingredients", (resourceId, root) -> registerEntries(readEntries(root), resourceId.toString(), true));
   }

   private static void resetItemEntriesToBundled() {
      Set<String> entryIds = new LinkedHashSet<>(ITEM_ENTRY_IDS.values());
      for (String entryId : entryIds) {
         KnowledgeAPI.removeEntry(CHAPTER_ID, entryId);
      }
      ENTRY_PATHS.clear();
      ITEM_ENTRY_IDS.clear();
      ENTRY_SECTIONS.clear();
      EXTERNAL_ITEM_IDS.clear();
      EXTERNAL_ENTRY_IDS.clear();
      registerEntries(loadFromJson(DATA_PATH), DATA_PATH, false);
   }

   private static List<Component> overviewLines() {
      return List.of(
         Component.translatable("book.herbcraft.ingredients.overview.line1").withStyle(ChatFormatting.GRAY),
         Component.translatable("book.herbcraft.ingredients.overview.line2").withStyle(ChatFormatting.GRAY),
         Component.translatable("book.herbcraft.ingredients.overview.line3").withStyle(ChatFormatting.DARK_AQUA),
         Component.translatable("book.herbcraft.ingredients.overview.line4").withStyle(ChatFormatting.DARK_GREEN)
      );
   }

   private static List<JsonEntry> loadFromJson(String resourcePath) {
      List<JsonEntry> entries = new ArrayList<>();
      try (InputStream in = IngredientsChapter.class.getClassLoader().getResourceAsStream(resourcePath)) {
         if (in == null) {
            Herbcraft.LOGGER.warn("Missing {}; ingredients chapter will be empty", resourcePath);
            return entries;
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
         entries.addAll(readEntries(root));
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load {}; using empty ingredients chapter", resourcePath, e);
      }
      return entries;
   }

   private static List<JsonEntry> readEntries(JsonObject root) {
      List<JsonEntry> entries = new ArrayList<>();
      if (root.has("entries")) {
         JsonArray arr = root.getAsJsonArray("entries");
         if (arr != null) {
            for (JsonElement element : arr) {
               JsonEntry entry = readEntry(element.getAsJsonObject());
               if (entry != null) entries.add(entry);
            }
         }
      } else {
         JsonEntry entry = readEntry(root);
         if (entry != null) entries.add(entry);
      }
      return entries;
   }

   private static JsonEntry readEntry(JsonObject obj) {
      String itemId = obj.has("item") ? obj.get("item").getAsString() : null;
      String section = obj.has("section") ? obj.get("section").getAsString() : null;
      return itemId == null ? null : new JsonEntry(itemId, section == null || section.isBlank() ? "special" : section);
   }

   private static void registerEntries(List<JsonEntry> entries, String source) {
      registerEntries(entries, source, false);
   }

   private static void registerEntries(List<JsonEntry> entries, String source, boolean external) {
      if (entries.isEmpty()) {
         Herbcraft.LOGGER.warn("No ingredient entries loaded from {}", source);
         return;
      }
      int registered = 0;
      int missingOptional = 0;
      List<String> missingExamples = new ArrayList<>();
      for (JsonEntry entry : entries) {
         Identifier id = Identifier.tryParse(entry.itemId);
         if (external && id != null && !BuiltInRegistries.ITEM.containsKey(id)) {
            missingOptional++;
            if (missingExamples.size() < 5) {
               missingExamples.add(entry.itemId);
            }
            continue;
         }
         if (registerEntry(entry, source, external)) registered++;
      }
      if (missingOptional > 0) {
         Herbcraft.LOGGER.info("Skipped {} optional ingredient entries from {} because their item IDs are not registered; examples: {}", missingOptional, source, String.join(", ", missingExamples));
      }
      Herbcraft.LOGGER.info("Loaded {} ingredient entries from {}", registered, source);
   }

   private static boolean registerEntry(JsonEntry entry, String source, boolean external) {
      Identifier id = Identifier.tryParse(entry.itemId);
      if (id == null) {
         Herbcraft.LOGGER.warn("Invalid ingredient item id '{}' in {}; skipping", entry.itemId, source);
         return false;
      }
      if (!BuiltInRegistries.ITEM.containsKey(id)) {
         Herbcraft.LOGGER.warn("Ingredient item '{}' from {} not found in registry; skipping", entry.itemId, source);
         return false;
      }
      Item item = BuiltInRegistries.ITEM.getValue(id);
      String entryId = defaultEntryId(id);
      if (ITEM_ENTRY_IDS.containsKey(id)) {
         Herbcraft.LOGGER.warn("Ingredient entry {} from {} overrides existing 食材录 entry for {}", entryId, source, id);
      }
      ITEM_ENTRY_IDS.put(id, entryId);
      if ("minecraft".equals(id.getNamespace())) {
         ENTRY_PATHS.add(id.getPath());
      }
      ENTRY_SECTIONS.put(entryId, entry.section);
      if (external) {
         EXTERNAL_ITEM_IDS.add(id);
         EXTERNAL_ENTRY_IDS.add(entryId);
      }
      KnowledgeAPI.registerSection(CHAPTER_ID, entry.section, Component.translatable("book.herbcraft.section.ingredients." + entry.section));

      final Item finalItem = item;
      KnowledgeAPI.registerEntry(
         CHAPTER_ID,
         new KnowledgeAPI.Entry(
            entryId,
            entry.section,
            Component.translatable(finalItem.getDescriptionId()),
            (player, state) -> lines(player, finalItem, entryId),
            player -> hasEaten(player, finalItem),
            player -> false
         )
      );
      return true;
   }

   private static String defaultEntryId(Identifier itemId) {
      if ("minecraft".equals(itemId.getNamespace())) {
         return itemId.getPath();
      }
      return itemId.getNamespace() + "__" + itemId.getPath().replace('/', '_');
   }

   private static boolean hasEaten(ServerPlayer player, Item item) {
      return player.getStats().getValue(Stats.ITEM_USED.get(item)) > 0;
   }

   private static List<Component> lines(ServerPlayer player, Item item, String entryId) {
      List<Component> lines = new ArrayList<>();

      KnowledgeAPI.Entry entry = KnowledgeAPI.chapter(CHAPTER_ID)
         .map(ch -> ch.entry(entryId)).orElse(null);
      KnowledgeDisplayState display = entry == null
         ? KnowledgeDisplayState.UNKNOWN
         : KnowledgeAPI.displayState(player, CHAPTER_ID, entry);

      if (display == KnowledgeDisplayState.UNKNOWN || display == KnowledgeDisplayState.ENCOUNTERED) {
         lines.add(Component.translatable("book.herbcraft.ingredients.unknown")
            .withStyle(ChatFormatting.GRAY));
         return lines;
      }

      // Eaten → show fuzzy nutrition
      var profileOpt = NutritionRegistry.get(new net.minecraft.world.item.ItemStack(item));
      if (profileOpt.isEmpty() || profileOpt.get().isEmpty()) {
         lines.add(Component.translatable("book.herbcraft.ingredients.no_nutrition")
            .withStyle(ChatFormatting.GRAY));
         return lines;
      }

      NutritionProfile profile = profileOpt.get();

      lines.add(Component.translatable("book.herbcraft.ingredients.nutrition_header")
         .withStyle(ChatFormatting.DARK_AQUA));

      for (String dim : NutritionProfile.DIMENSIONS) {
         int val = profile.get(dim);
         if (val > 0) {
            String level = fuzzyLevel(val);
            lines.add(Component.literal("  ")
               .append(Component.translatable("nutrition.herbcraft.level." + level)
                  .withStyle(ChatFormatting.AQUA))
               .append(Component.translatable("nutrition.herbcraft.dimension." + dim)
                  .withStyle(ChatFormatting.GRAY)));
         }
      }

      lines.add(Component.empty());
      lines.add(Component.translatable("book.herbcraft.ingredients.note")
         .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

      return lines;
   }

   private static String fuzzyLevel(int value) {
      if (value <= 15) return "trace";
      if (value <= 40) return "rich";
      return "heavy";
   }

   private record JsonEntry(String itemId, String section) {}
}
