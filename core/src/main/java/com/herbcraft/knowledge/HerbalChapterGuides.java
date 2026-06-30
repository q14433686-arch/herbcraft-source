package com.herbcraft.knowledge;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** Core herbal guide-provider wiring. */
public final class HerbalChapterGuides {
   private HerbalChapterGuides() {
   }

   public static void registerCoreGuideProviders() {
      CodexGuideRegistry.registerProvider(id("recognizing"), player -> HerbalChapterTextBuilders.guideLines(player, "recognizing"));
      CodexGuideRegistry.registerProvider(id("knowledge_states"), player -> HerbalChapterTextBuilders.guideLines(player, "knowledge_states"));
      CodexGuideRegistry.registerProvider(id("flavors"), player -> HerbalChapterTextBuilders.guideLines(player, "flavors"));
      CodexGuideRegistry.registerProvider(id("seven_emotions"), player -> HerbalChapterTextBuilders.guideLines(player, "seven_emotions"));
      CodexGuideRegistry.registerProvider(id("herb_stack"), player -> HerbalChapterTextBuilders.guideLines(player, "herb_stack"));
      CodexGuideRegistry.registerProvider(id("ingredients"), player -> HerbalChapterTextBuilders.guideLines(player, "ingredients"));
      CodexGuideRegistry.registerProvider(id("errata"), player -> HerbalChapterTextBuilders.guideLines(player, "errata"));
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("herbcraft", path);
   }
}
