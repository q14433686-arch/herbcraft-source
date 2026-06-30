package com.herbcraft.cuisine.knowledge;

import com.herbcraft.api.HerbConsumeEvents;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeDisplayState;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.api.KnowledgeAPI.Entry;
import com.herbcraft.api.KnowledgeAPI.State;
import com.herbcraft.cuisine.registry.DishRegistry;
import com.herbcraft.knowledge.CodexGuideRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;

public final class CuisineChapter {
   public static final String CHAPTER_ID = "cuisine";

   private CuisineChapter() {
   }

   public static void register() {
      KnowledgeAPI.registerChapter("cuisine", Component.translatable("book.herbcraft_cuisine.chapter"), 100);
      KnowledgeAPI.registerSection("cuisine", "overview", Component.translatable("book.herbcraft_cuisine.section.overview"));

      for (String section : List.of("cold", "soups", "medicinal")) {
         KnowledgeAPI.registerSection("cuisine", section, Component.translatable("book.herbcraft_cuisine.section." + section));
      }

      registerGuideProviders();
      CodexGuideRegistry.loadFromBundledJson("data/herbcraft_cuisine/codex_guides.json");

      for (DishRegistry.Dish dish : DishRegistry.dishes()) {
         KnowledgeAPI.registerEntry(
            "cuisine",
            new Entry(
               dish.id(),
               sectionOf(dish),
               Component.translatable(dish.item().getDescriptionId()),
               (player, state) -> lines(player, dish, state),
               player -> mastered(player, dish),
               player -> anyIngredientEaten(player, dish)
            )
         );
      }

      HerbConsumeEvents.register(CuisineChapter::onHerbEaten);
   }

   private static void registerGuideProviders() {
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_cuisine", "cuisine"), player -> guideLines(player, "gateway"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_cuisine", "overview"), player -> guideLines(player, "overview"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_cuisine", "water_bowl"), player -> guideLines(player, "water_bowl"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_cuisine", "dishes"), player -> guideLines(player, "dishes"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_cuisine", "hodgepodge"), player -> guideLines(player, "hodgepodge"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_cuisine", "immunity"), player -> guideLines(player, "immunity"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_cuisine", "hidden_catalyst"), player -> guideLines(player, "hidden_catalyst"));
   }

   private static List<Component> guideLines(ServerPlayer player, String guide) {
      List<Component> lines = new ArrayList<>();
      switch (guide) {
         case "gateway" -> {
            add(lines, "book.herbcraft_cuisine.guide.cuisine.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "first_dish")) add(lines, "book.herbcraft_cuisine.guide.cuisine.first_dish", ChatFormatting.DARK_AQUA);
            else add(lines, "book.herbcraft_cuisine.guide.cuisine.none", ChatFormatting.DARK_GRAY);
         }
         case "overview" -> {
            add(lines, "book.herbcraft_cuisine.guide.overview.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft_cuisine.guide.overview.line2", ChatFormatting.GRAY);
            if (!advancementDone(player, "water_bowl")) add(lines, "book.herbcraft_cuisine.guide.overview.no_water", ChatFormatting.DARK_GRAY);
            if (advancementDone(player, "first_dish")) add(lines, "book.herbcraft_cuisine.guide.overview.first_dish", ChatFormatting.DARK_AQUA);
         }
         case "water_bowl" -> {
            add(lines, "book.herbcraft_cuisine.guide.water_bowl.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "water_bowl")) {
               add(lines, "book.herbcraft_cuisine.guide.water_bowl.known", ChatFormatting.DARK_AQUA);
               if (!advancementDone(player, "first_dish")) add(lines, "book.herbcraft_cuisine.guide.water_bowl.next", ChatFormatting.DARK_GRAY);
            }
         }
         case "dishes" -> {
            add(lines, "book.herbcraft_cuisine.guide.dishes.line1", ChatFormatting.GRAY);
            KnowledgeDisplayState salad = entryState(player, "dandelion_salad");
            if (salad == KnowledgeDisplayState.UNKNOWN) add(lines, "book.herbcraft_cuisine.guide.dishes.unknown", ChatFormatting.DARK_GRAY);
            else if (salad.ordinal() < KnowledgeDisplayState.MASTERED.ordinal()) add(lines, "book.herbcraft_cuisine.guide.dishes.heard", ChatFormatting.DARK_AQUA);
            if (advancementDone(player, "water_bowl") && !advancementDone(player, "first_dish")) add(lines, "book.herbcraft_cuisine.guide.dishes.water_path", ChatFormatting.DARK_GRAY);
            if (advancementDone(player, "first_dish")) add(lines, "book.herbcraft_cuisine.guide.dishes.first_dish", ChatFormatting.DARK_AQUA);
         }
         case "hodgepodge" -> {
            add(lines, "book.herbcraft_cuisine.guide.hodgepodge.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft_cuisine.guide.hodgepodge.isolation", ChatFormatting.DARK_GRAY);
            if (advancementDone(player, "first_hodgepodge")) add(lines, "book.herbcraft_cuisine.guide.hodgepodge.first", ChatFormatting.DARK_AQUA);
         }
         case "immunity" -> {
            add(lines, "book.herbcraft_cuisine.guide.immunity.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "first_dish") && !advancementDone(player, "immunity_meal")) add(lines, "book.herbcraft_cuisine.guide.immunity.after_dish", ChatFormatting.DARK_GRAY);
            if (advancementDone(player, "immunity_meal")) add(lines, "book.herbcraft_cuisine.guide.immunity.known", ChatFormatting.DARK_AQUA);
         }
         case "hidden_catalyst" -> {
            add(lines, "book.herbcraft_cuisine.guide.hidden_catalyst.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "hundred_flower_feast")) add(lines, "book.herbcraft_cuisine.guide.hidden_catalyst.flower", ChatFormatting.GOLD);
            if (advancementDone(player, "deadly_hodgepodge")) add(lines, "book.herbcraft_cuisine.guide.hidden_catalyst.deadly", ChatFormatting.RED);
            if (!advancementDone(player, "hundred_flower_feast") && !advancementDone(player, "deadly_hodgepodge")) add(lines, "book.herbcraft_cuisine.guide.hidden_catalyst.hint", ChatFormatting.DARK_GRAY);
         }
         default -> add(lines, "book.herbcraft_cuisine.guide.cuisine.line1", ChatFormatting.GRAY);
      }
      return lines;
   }

   private static void add(List<Component> lines, String key, ChatFormatting style) {
      lines.add(Component.translatable(key).withStyle(style));
   }

   private static boolean advancementDone(ServerPlayer player, String path) {
      var server = player.level().getServer();
      if (server == null) return false;
      var advancement = server.getAdvancements().get(Identifier.fromNamespaceAndPath("herbcraft_cuisine", path));
      return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
   }

   private static KnowledgeDisplayState entryState(ServerPlayer player, String entryId) {
      Entry entry = KnowledgeAPI.chapter(CHAPTER_ID).map(chapter -> chapter.entry(entryId)).orElse(null);
      return entry == null ? KnowledgeDisplayState.UNKNOWN : KnowledgeAPI.displayState(player, CHAPTER_ID, entry);
   }

   private static String sectionOf(DishRegistry.Dish dish) {
      if (dish.clears().isEmpty() && !dish.clearFire()) {
         return dish.rawItem() != null ? "soups" : "cold";
      } else {
         return "medicinal";
      }
   }

   private static void onHerbEaten(ServerPlayer player, Item eaten) {
      List<ResourceKey<Recipe<?>>> keys = new ArrayList<>();

      for (DishRegistry.Dish dish : DishRegistry.dishes()) {
         boolean contains = dish.ingredients().stream().anyMatch(spec -> spec.matches(eaten));
         if (contains) {
            keys.add(recipeKey(dish.rawItem() != null ? "raw_" + dish.id() : dish.id()));
         }
      }

      KnowledgeManager.awardRecipes(player, keys);
   }

   private static ResourceKey<Recipe<?>> recipeKey(String path) {
      return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("herbcraft_cuisine", path));
   }

   private static boolean mastered(ServerPlayer player, DishRegistry.Dish dish) {
      return player.getStats().getValue(Stats.ITEM_USED.get(dish.item())) > 0 || player.getStats().getValue(Stats.ITEM_CRAFTED.get(dish.item())) > 0;
   }

   private static boolean anyIngredientEaten(ServerPlayer player, DishRegistry.Dish dish) {
      for (DishRegistry.IngredientSpec spec : dish.ingredients()) {
         if (spec.item() != null && player.getStats().getValue(Stats.ITEM_USED.get(spec.item())) > 0) {
            return true;
         }
      }

      return false;
   }

   private static List<Component> lines(ServerPlayer player, DishRegistry.Dish dish, State state) {
      List<Component> lines = new ArrayList<>();
      MutableComponent ingredients = Component.empty();
      boolean first = true;

      for (DishRegistry.IngredientSpec spec : dish.ingredients()) {
         if (!first) {
            ingredients.append(Component.literal(" + "));
         }

         if (spec.tag() != null) {
            ingredients.append(Component.literal("#" + spec.tag().location().getPath()));
         } else if (state != State.MASTERED && player.getStats().getValue(Stats.ITEM_USED.get(spec.item())) <= 0) {
            ingredients.append(Component.translatable("tooltip.herbcraft.page.unknown"));
         } else {
            ingredients.append(Component.translatable(spec.item().getDescriptionId()));
         }

         first = false;
      }

      lines.add(ingredients.withStyle(ChatFormatting.DARK_GRAY));
      if (state == State.HEARD) {
         lines.add(
            Component.translatable("book.herbcraft_cuisine.entry.heard").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
         );
         return lines;
      } else {
         for (DishRegistry.DishEffect effect : dish.effects()) {
            MutableComponent name = Component.translatable(((MobEffect)effect.effect().value()).getDescriptionId());
            if (effect.amplifier() > 0) {
               name = Component.translatable("potion.withAmplifier", new Object[]{name, Component.translatable("potion.potency." + effect.amplifier())});
            }

            int seconds = effect.durationSeconds();
            MutableComponent line = Component.translatable("potion.withDuration", new Object[]{name, String.format("%d:%02d", seconds / 60, seconds % 60)});
            if (effect.chance() < 1.0F) {
               line = line.append(Component.literal(" " + Math.round(effect.chance() * 100.0F) + "%"));
            }

            boolean positive = ((MobEffect)effect.effect().value()).isBeneficial();
            lines.add(line.withStyle(positive ? ChatFormatting.DARK_BLUE : ChatFormatting.DARK_RED));
         }

         return lines;
      }
   }
}
