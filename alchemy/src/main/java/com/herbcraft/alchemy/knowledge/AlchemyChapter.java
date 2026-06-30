package com.herbcraft.alchemy.knowledge;

import com.herbcraft.alchemy.coating.CoatingSystem;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.alchemy.potion.PotionProcessingRules;
import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeDisplayState;
import com.herbcraft.api.KnowledgeAPI.Entry;
import com.herbcraft.api.KnowledgeAPI.State;
import com.herbcraft.knowledge.CodexGuideRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

public final class AlchemyChapter {
   public static final String CHAPTER_ID = "alchemy";

   private AlchemyChapter() {
   }

   public static void register() {
      KnowledgeAPI.registerChapter("alchemy", Component.translatable("book.herbcraft_alchemy.chapter"), 200);
      KnowledgeAPI.registerSection("alchemy", "overview", Component.translatable("book.herbcraft_alchemy.section.overview"));
      KnowledgeAPI.registerSection("alchemy", "essences", Component.translatable("book.herbcraft_alchemy.section.essences"));
      KnowledgeAPI.registerSection("alchemy", "potions", Component.translatable("book.herbcraft_alchemy.section.potions"));
      KnowledgeAPI.registerSection("alchemy", "coatings", Component.translatable("book.herbcraft_alchemy.section.coatings"));

      registerGuideProviders();
      CodexGuideRegistry.loadFromBundledJson("data/herbcraft_alchemy/codex_guides.json");

      for (Item item : AlchemyRegistries.essenceItems()) {
         AlchemyRegistries.essenceInfo(item)
            .ifPresent(
               info -> {
                  KnowledgeAPI.registerEntry(
                     "alchemy",
                     new Entry(
                        "essence_" + info.id(),
                        "essences",
                        Component.translatable(item.getDescriptionId()),
                        (player, state) -> essenceLines(info, state),
                        player -> player.getStats().getValue(Stats.ITEM_CRAFTED.get(item)) > 0,
                        null
                     )
                  );
                  KnowledgeAPI.registerEntry(
                     "alchemy",
                     new Entry(
                        "potion_" + info.id(),
                        "potions",
                        Component.translatable("item.minecraft.potion.effect." + info.id()),
                        (player, state) -> potionLines(player, info, state),
                        null,
                        null
                     )
                  );
                  KnowledgeAPI.registerEntry(
                     "alchemy",
                     new Entry(
                        "coating_" + info.id(),
                        "coatings",
                        Component.translatable("book.herbcraft_alchemy.coating_name", new Object[]{Component.translatable(item.getDescriptionId())}),
                        (player, state) -> coatingLines(info, state),
                        null,
                        null
                     )
                  );
               }
            );
      }
   }

   private static void registerGuideProviders() {
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "alchemy"), player -> guideLines(player, "gateway"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "overview"), player -> guideLines(player, "overview"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "essences"), player -> guideLines(player, "essences"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "potions"), player -> guideLines(player, "potions"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "clarified_murky"), player -> guideLines(player, "clarified_murky"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "quality_bonus"), player -> guideLines(player, "quality_bonus"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "coatings"), player -> guideLines(player, "coatings"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "armor_support"), player -> guideLines(player, "armor_support"));
      CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "witch"), player -> guideLines(player, "witch"));
   }

   private static List<Component> guideLines(ServerPlayer player, String guide) {
      List<Component> lines = new ArrayList<>();
      switch (guide) {
         case "gateway" -> {
            add(lines, "book.herbcraft_alchemy.guide.alchemy.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "first_essence")) add(lines, "book.herbcraft_alchemy.guide.alchemy.essence", ChatFormatting.DARK_AQUA);
            else add(lines, "book.herbcraft_alchemy.guide.alchemy.none", ChatFormatting.DARK_GRAY);
         }
         case "overview" -> {
            add(lines, "book.herbcraft_alchemy.guide.overview.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft_alchemy.guide.overview.line2", ChatFormatting.GRAY);
            add(lines, "book.herbcraft_alchemy.guide.overview.linkage_rounds", ChatFormatting.DARK_GREEN);
            if (!advancementDone(player, "first_essence")) add(lines, "book.herbcraft_alchemy.guide.overview.no_essence", ChatFormatting.DARK_GRAY);
            if (advancementDone(player, "first_essence")) add(lines, "book.herbcraft_alchemy.guide.overview.first_essence", ChatFormatting.DARK_AQUA);
            if (advancementDone(player, "first_potion")) add(lines, "book.herbcraft_alchemy.guide.overview.potion_taste_gate", ChatFormatting.BLUE);
            if (advancementDone(player, "first_distilled_essence") || advancementDone(player, "first_armor_support")) add(lines, "book.herbcraft_alchemy.guide.overview.armor_support_hint", ChatFormatting.AQUA);
         }
         case "essences" -> {
            add(lines, "book.herbcraft_alchemy.guide.essences.line1", ChatFormatting.GRAY);
            int knownEssences = knownEntryCount(player, "essence_", KnowledgeDisplayState.HEARD);
            if (knownEssences > 0) lines.add(Component.translatable("book.herbcraft_alchemy.guide.essences.known", knownEssences).withStyle(ChatFormatting.DARK_AQUA));
            else add(lines, "book.herbcraft_alchemy.guide.essences.none", ChatFormatting.DARK_GRAY);
         }
         case "potions" -> {
            add(lines, "book.herbcraft_alchemy.guide.potions.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "first_essence") && !advancementDone(player, "first_potion")) add(lines, "book.herbcraft_alchemy.guide.potions.after_essence", ChatFormatting.DARK_GRAY);
            if (advancementDone(player, "first_potion")) add(lines, "book.herbcraft_alchemy.guide.potions.first", ChatFormatting.DARK_AQUA);
            if (advancementDone(player, "first_potion") || advancementDone(player, "first_clarified") || advancementDone(player, "first_murky") || advancementDone(player, "first_sustained") || advancementDone(player, "first_intense")) {
               add(lines, "book.herbcraft_alchemy.guide.potions.processing_semantics", ChatFormatting.BLUE);
               add(lines, "book.herbcraft_alchemy.guide.potions.relation_integration", ChatFormatting.DARK_GREEN);
            }
         }
         case "clarified_murky" -> {
            add(lines, "book.herbcraft_alchemy.guide.clarified_murky.line1", ChatFormatting.GRAY);
            boolean clarified = advancementDone(player, "first_clarified");
            boolean murky = advancementDone(player, "first_murky");
            if (clarified) add(lines, "book.herbcraft_alchemy.guide.clarified_murky.clarified", ChatFormatting.DARK_AQUA);
            if (murky) add(lines, "book.herbcraft_alchemy.guide.clarified_murky.murky", ChatFormatting.GOLD);
            if (clarified && murky) add(lines, "book.herbcraft_alchemy.guide.clarified_murky.both", ChatFormatting.GRAY);
         }
         case "quality_bonus" -> {
            add(lines, "book.herbcraft_alchemy.guide.quality_bonus.line1", ChatFormatting.GRAY);
            if ((advancementDone(player, "first_clarified") || advancementDone(player, "first_murky") || advancementDone(player, "first_sustained") || advancementDone(player, "first_intense")) && !advancementDone(player, "epic_potion")) add(lines, "book.herbcraft_alchemy.guide.quality_bonus.after_advanced", ChatFormatting.DARK_GRAY);
            if (advancementDone(player, "epic_potion")) add(lines, "book.herbcraft_alchemy.guide.quality_bonus.epic", ChatFormatting.GOLD);
            if (advancementDone(player, "first_potion")) {
               add(lines, "book.herbcraft_alchemy.guide.quality_bonus.processing_meaning", ChatFormatting.DARK_GREEN);
               add(lines, "book.herbcraft_alchemy.guide.quality_bonus.catalyst_semantics", ChatFormatting.AQUA);
            }
         }
         case "coatings" -> {
            add(lines, "book.herbcraft_alchemy.guide.coatings.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "first_essence") && !advancementDone(player, "first_coating")) add(lines, "book.herbcraft_alchemy.guide.coatings.after_essence", ChatFormatting.DARK_GRAY);
            if (advancementDone(player, "first_coating")) add(lines, "book.herbcraft_alchemy.guide.coatings.first", ChatFormatting.DARK_AQUA);
         }
         case "armor_support" -> {
            add(lines, "book.herbcraft_alchemy.guide.armor_support.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft_alchemy.guide.armor_support.line2", ChatFormatting.DARK_AQUA);
            if (advancementDone(player, "first_distilled_essence") && !advancementDone(player, "first_armor_support")) add(lines, "book.herbcraft_alchemy.guide.armor_support.after_essence", ChatFormatting.BLUE);
            if (advancementDone(player, "first_armor_support")) add(lines, "book.herbcraft_alchemy.guide.armor_support.first", ChatFormatting.DARK_AQUA);
         }
         case "witch" -> {
            add(lines, "book.herbcraft_alchemy.guide.witch.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "witch_knowledge")) add(lines, "book.herbcraft_alchemy.guide.witch.knowledge", ChatFormatting.DARK_AQUA);
            if (advancementDone(player, "survive_witch_brew")) add(lines, "book.herbcraft_alchemy.guide.witch.survive", ChatFormatting.GOLD);
            if (advancementDone(player, "undead_toxin")) add(lines, "book.herbcraft_alchemy.guide.witch.undead", ChatFormatting.RED);
         }
         default -> add(lines, "book.herbcraft_alchemy.guide.alchemy.line1", ChatFormatting.GRAY);
      }
      return lines;
   }

   private static void add(List<Component> lines, String key, ChatFormatting style) {
      lines.add(Component.translatable(key).withStyle(style));
   }

   private static boolean advancementDone(ServerPlayer player, String path) {
      var server = player.level().getServer();
      if (server == null) return false;
      var advancement = server.getAdvancements().get(Identifier.fromNamespaceAndPath("herbcraft_alchemy", path));
      return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
   }

   private static int knownEntryCount(ServerPlayer player, String prefix, KnowledgeDisplayState minimum) {
      return KnowledgeAPI.chapter(CHAPTER_ID).map(chapter -> {
         int count = 0;
         for (Entry entry : chapter.entries()) {
            if (entry.id().startsWith(prefix) && KnowledgeAPI.displayState(player, CHAPTER_ID, entry).ordinal() >= minimum.ordinal()) {
               count++;
            }
         }
         return count;
      }).orElse(0);
   }

   private static List<Component> essenceLines(AlchemyRegistries.EssenceInfo info, State state) {
      List<Component> lines = new ArrayList<>();
      if (state == State.HEARD) {
         lines.add(Component.translatable("book.herbcraft_alchemy.entry.heard").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
         return lines;
      } else {
         for (AlchemyRegistries.EssenceEffect effect : info.effects()) {
            lines.add(effectLine(((MobEffect)effect.effect().value()).getDescriptionId(), effect.amplifier(), effect.durationSeconds(), effect.positive()));
         }

         return lines;
      }
   }

   private static List<Component> potionLines(ServerPlayer player, AlchemyRegistries.EssenceInfo info, State state) {
      List<Component> lines = new ArrayList<>();
      KnowledgeDisplayState displayState = exactDisplayState(player, "potion_" + info.id());
      if (displayState.ordinal() < KnowledgeDisplayState.TASTED.ordinal()) {
         if (state == State.HEARD || displayState.ordinal() >= KnowledgeDisplayState.HEARD.ordinal()) {
            lines.add(Component.translatable("book.herbcraft_alchemy.entry.heard").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
         } else {
            lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_seen").withStyle(ChatFormatting.DARK_GRAY));
         }
         lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_taste_needed").withStyle(ChatFormatting.DARK_GRAY));
         return lines;
      }
      lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_tasted").withStyle(ChatFormatting.DARK_AQUA));
      lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_source", sourceName(info)).withStyle(ChatFormatting.GRAY));
      for (AlchemyRegistries.EssenceEffect effect : info.effects()) {
         lines.add(effectLine(((MobEffect)effect.effect().value()).getDescriptionId(), effect.amplifier(), effect.durationSeconds(), effect.positive()));
      }
      if (displayState == KnowledgeDisplayState.MASTERED) {
         lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_mastered_semantics").withStyle(ChatFormatting.DARK_AQUA));
         sourceNature(info).ifPresent(nature -> {
            lines.add(Component.translatable("tooltip.herbcraft.potion.source_temperature", Component.translatable("nature.herbcraft.temperature." + nature.temperature())).withStyle(ChatFormatting.AQUA));
            if (!nature.tasteFlavors().isEmpty()) lines.add(Component.translatable("tooltip.herbcraft.potion.source_taste", joinKeys(nature.tasteFlavors(), "nature.herbcraft.flavor.")).withStyle(ChatFormatting.GOLD));
            if (!nature.traits().isEmpty()) lines.add(Component.translatable("tooltip.herbcraft.potion.source_traits", joinKeys(nature.traits(), "nature.herbcraft.hint.")).withStyle(ChatFormatting.DARK_PURPLE));
         });
         PotionProcessingRules.styles().stream().filter(PotionProcessingRules.Style::implemented).forEach(style -> {
            lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_style_separator").withStyle(ChatFormatting.DARK_GRAY));
            lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_style_header", Component.translatable(style.langKey())).withStyle(styleFormatting(style)));
            if (!style.thermalHintKey().isBlank()) lines.add(Component.translatable("tooltip.herbcraft.potion.thermal_shift", Component.translatable(style.thermalHintKey())).withStyle(ChatFormatting.DARK_GRAY));
            if (!style.qiHintKey().isBlank()) lines.add(Component.translatable("tooltip.herbcraft.potion.qi_bias", Component.translatable(style.qiHintKey())).withStyle(ChatFormatting.DARK_GRAY));
            if (!style.clarityHintKey().isBlank()) lines.add(Component.translatable("tooltip.herbcraft.potion.clarity_level", Component.translatable(style.clarityHintKey())).withStyle(ChatFormatting.DARK_GRAY));
            if (!style.relationHintKey().isBlank()) lines.add(Component.translatable("tooltip.herbcraft.potion.relation_modifier", Component.translatable(style.relationHintKey())).withStyle(ChatFormatting.DARK_GRAY));
            if (!style.flavorBias().isEmpty()) lines.add(Component.translatable("tooltip.herbcraft.potion.flavor_bias", joinKeys(style.flavorBias(), "nature.herbcraft.flavor.")).withStyle(ChatFormatting.GOLD));
            if (!style.traitAffinity().isEmpty()) lines.add(Component.translatable("tooltip.herbcraft.potion.trait_affinity", joinKeys(style.traitAffinity(), "nature.herbcraft.hint.")).withStyle(ChatFormatting.DARK_PURPLE));
         });
         lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_splash_lingering_semantics").withStyle(ChatFormatting.BLUE));
      } else {
         lines.add(Component.translatable("book.herbcraft_alchemy.entry.potion_processing_bodystate").withStyle(ChatFormatting.GRAY));
      }
      return lines;
   }

   private static java.util.Optional<NatureProfile> sourceNature(AlchemyRegistries.EssenceInfo info) {
      if (info.sourceItems().isEmpty()) {
         return java.util.Optional.empty();
      }
      try {
         return java.util.Optional.of(HerbNatureAPI.getNatureOrFallback(Identifier.parse(info.sourceItems().get(0))));
      } catch (Exception ignored) {
         return java.util.Optional.empty();
      }
   }

   private static Component sourceName(AlchemyRegistries.EssenceInfo info) {
      if (!info.sourceItems().isEmpty()) {
         try {
            return Component.translatable(BuiltInRegistries.ITEM.getValue(Identifier.parse(info.sourceItems().get(0))).getDescriptionId());
         } catch (Exception ignored) {
         }
      }
      return Component.translatable(info.item().getDescriptionId());
   }

   private static Component joinKeys(List<String> values, String keyPrefix) {
      MutableComponent joined = Component.empty();
      for (int i = 0; i < values.size(); i++) {
         if (i > 0) joined.append(Component.literal(", "));
         joined.append(Component.translatable(keyPrefix + values.get(i)));
      }
      return joined;
   }

   private static ChatFormatting styleFormatting(PotionProcessingRules.Style style) {
      return switch (style.id()) {
         case "sustained" -> ChatFormatting.GREEN;
         case "intense" -> ChatFormatting.GOLD;
         case "purified" -> ChatFormatting.AQUA;
         default -> ChatFormatting.GRAY;
      };
   }

   private static KnowledgeDisplayState exactDisplayState(ServerPlayer player, String entryId) {
      return KnowledgeAPI.chapter(CHAPTER_ID).map(chapter -> {
         Entry entry = chapter.entry(entryId);
         return entry == null ? KnowledgeDisplayState.UNKNOWN : KnowledgeAPI.displayState(player, CHAPTER_ID, entry);
      }).orElse(KnowledgeDisplayState.UNKNOWN);
   }

   private static List<Component> coatingLines(AlchemyRegistries.EssenceInfo info, State state) {
      List<Component> lines = new ArrayList<>();
      if (state == State.HEARD) {
         lines.add(Component.translatable("book.herbcraft_alchemy.entry.heard").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
         return lines;
      } else {
         List<AlchemyRegistries.EssenceEffect> effects = CoatingSystem.effectsFor(info, WeaponCoating.Mode.FULL);
         lines.add(
            Component.translatable("book.herbcraft_alchemy.coating_uses", new Object[]{CoatingSystem.usesFor(info, WeaponCoating.Mode.FULL)})
               .withStyle(ChatFormatting.GRAY)
         );

         for (AlchemyRegistries.EssenceEffect effect : effects) {
            lines.add(
               effectLine(
                  ((MobEffect)effect.effect().value()).getDescriptionId(), effect.amplifier(), CoatingSystem.hitDurationTicks(effect) / 20, effect.positive()
               )
            );
         }

         return lines;
      }
   }

   private static Component effectLine(String descriptionId, int amplifier, int seconds, boolean positive) {
      MutableComponent name = Component.translatable(descriptionId);
      if (amplifier > 0) {
         name = Component.translatable("potion.withAmplifier", new Object[]{name, Component.translatable("potion.potency." + amplifier)});
      }

      return Component.translatable("potion.withDuration", new Object[]{name, String.format("%d:%02d", seconds / 60, seconds % 60)})
         .withStyle(positive ? ChatFormatting.BLUE : ChatFormatting.RED);
   }
}
