package com.herbcraft.knowledge;

import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.api.KnowledgeAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Core chapter/section/entry registration for the herbal Codex. */
public final class HerbalChapterRegistration {
   private HerbalChapterRegistration() {
   }

   public static void register() {
      KnowledgeAPI.registerChapter(HerbalChapter.CHAPTER_ID, Component.translatable("book.herbcraft.chapter.herbal"), 0);
      KnowledgeAPI.registerSection(HerbalChapter.CHAPTER_ID, "overview", Component.translatable("book.herbcraft.section.herbal.overview"));
      KnowledgeAPI.registerEntry(
         HerbalChapter.CHAPTER_ID,
         new KnowledgeAPI.Entry(
            "overview",
            "overview",
            Component.translatable("book.herbcraft.entry.overview.title"),
            (player, state) -> HerbalChapterTextBuilders.overviewLines(),
            player -> true,
            player -> true
         )
      );

      HerbalChapterGuides.registerCoreGuideProviders();
      CodexGuideRegistry.loadFromBundledJson("data/herbcraft/codex_guides.json");

      for (String section : List.of("flowers", "foliage", "sea", "nether", "ice", "curios")) {
         KnowledgeAPI.registerSection(HerbalChapter.CHAPTER_ID, section, Component.translatable("book.herbcraft.section.herbal." + section));
      }

      HerbcraftAPI.getHerbRegistry().forEach((item, definition) -> {
         String entryId = BuiltInRegistries.ITEM.getKey(item).getPath();
         KnowledgeAPI.registerEntry(
            HerbalChapter.CHAPTER_ID,
            new KnowledgeAPI.Entry(
               entryId,
               HerbalChapterClassifier.classify(entryId),
               Component.translatable(item.getDescriptionId()),
               (player, state) -> HerbalChapterTextBuilders.lines(player, entryId, definition),
               null,
               null
            )
         );
      });
   }
}
