package com.herbcraft.knowledge;

import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.api.HerbDefinition;
import com.herbcraft.api.HerbEffectEntry;
import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.api.HerbUsageAPI;
import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeDisplayState;
import com.herbcraft.api.KnowledgeFlags;
import com.herbcraft.api.PlayerClaimMark;
import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.pharmacology.PlayerPharmacologyState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Player-facing line/text builders for the herbal Codex. */
public final class HerbalChapterTextBuilders {
   private HerbalChapterTextBuilders() {
   }

   public static List<Component> overviewLines() {
      return List.of(
         Component.translatable("book.herbcraft.entry.overview.line1"),
         Component.translatable("book.herbcraft.entry.overview.line2"),
         Component.translatable("book.herbcraft.entry.overview.line3"),
         Component.translatable("book.herbcraft.entry.overview.line4"),
         Component.translatable("book.herbcraft.entry.overview.line5")
      );
   }

   public static List<Component> guideLines(ServerPlayer player, String guide) {
      List<Component> lines = new ArrayList<>();
      switch (guide) {
         case "recognizing" -> {
            add(lines, "book.herbcraft.guide.recognizing.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft.guide.recognizing.line2", ChatFormatting.DARK_GRAY);
            HerbKnowledgeStats stats = herbStats(player);
            if (stats.tasted() > 0) {
               lines.add(Component.translatable("book.herbcraft.guide.recognizing.tasted_count", stats.tasted()).withStyle(ChatFormatting.DARK_AQUA));
            } else {
               add(lines, "book.herbcraft.guide.recognizing.none", ChatFormatting.DARK_GRAY);
            }
         }
         case "knowledge_states" -> {
            add(lines, "book.herbcraft.guide.knowledge_states.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft.guide.knowledge_states.line2", ChatFormatting.GRAY);
            HerbKnowledgeStats stats = herbStats(player);
            if (stats.heard() > 0 || stats.tasted() > 0 || stats.mastered() > 0) {
               lines.add(Component.translatable("book.herbcraft.guide.knowledge_states.progress", stats.heard(), stats.tasted(), stats.mastered()).withStyle(ChatFormatting.DARK_AQUA));
            } else {
               add(lines, "book.herbcraft.guide.knowledge_states.none", ChatFormatting.DARK_GRAY);
            }
         }
         case "flavors" -> {
            add(lines, "book.herbcraft.guide.flavors.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft.guide.flavors.line2", ChatFormatting.GRAY);
            TastedHerb first = firstTastedHerb(player);
            if (first == null) {
               add(lines, "book.herbcraft.guide.flavors.none", ChatFormatting.DARK_GRAY);
            } else {
               add(lines, "book.herbcraft.guide.flavors.tasted_once", ChatFormatting.DARK_AQUA);
               int flavorCount = discoveredFlavorCount(player);
               if (flavorCount >= 3) {
                  lines.add(Component.translatable("book.herbcraft.guide.flavors.many", flavorCount).withStyle(ChatFormatting.GRAY));
               }
               addFlavorPrinciples(player, lines);
            }
         }
         case "seven_emotions" -> {
            add(lines, "book.herbcraft.guide.seven_emotions.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft.guide.seven_emotions.line2", ChatFormatting.GRAY);
            HerbKnowledgeStats stats = herbStats(player);
            if (stats.tasted() >= 2) {
               lines.add(Component.translatable("book.herbcraft.guide.seven_emotions.tasted", stats.tasted()).withStyle(ChatFormatting.DARK_AQUA));
               addSevenEmotionPrinciples(player, lines);
            } else {
               add(lines, "book.herbcraft.guide.seven_emotions.none", ChatFormatting.DARK_GRAY);
            }
         }
         case "herb_stack" -> {
            add(lines, "book.herbcraft.guide.herb_stack.line1", ChatFormatting.GRAY);
            if (advancementDone(player, "herbcraft", "herb_stack_3")) add(lines, "book.herbcraft.guide.herb_stack.stage3", ChatFormatting.DARK_AQUA);
            if (advancementDone(player, "herbcraft", "herb_stack_5")) add(lines, "book.herbcraft.guide.herb_stack.stage5", ChatFormatting.DARK_AQUA);
            if (advancementDone(player, "herbcraft", "herb_stack_8")) add(lines, "book.herbcraft.guide.herb_stack.stage8", ChatFormatting.GOLD);
            if (advancementDone(player, "herbcraft", "herb_overload")) add(lines, "book.herbcraft.guide.herb_stack.overload", ChatFormatting.RED);
            if (!advancementDone(player, "herbcraft", "herb_stack_3")) add(lines, "book.herbcraft.guide.herb_stack.none", ChatFormatting.DARK_GRAY);
         }
         case "ingredients" -> {
            add(lines, "book.herbcraft.guide.ingredients.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft.guide.ingredients.line2", ChatFormatting.GRAY);
            int tasted = tastedIngredientCount(player);
            if (tasted > 0) lines.add(Component.translatable("book.herbcraft.guide.ingredients.progress", tasted).withStyle(ChatFormatting.DARK_AQUA));
            else add(lines, "book.herbcraft.guide.ingredients.none", ChatFormatting.DARK_GRAY);
         }
         case "errata" -> {
            add(lines, "book.herbcraft.guide.errata.line1", ChatFormatting.GRAY);
            add(lines, "book.herbcraft.guide.errata.line2", ChatFormatting.GRAY);
            int verified = verifiedHerbalClaimCount(player);
            if (verified > 0) lines.add(Component.translatable("book.herbcraft.guide.errata.progress", verified).withStyle(ChatFormatting.DARK_AQUA));
            else add(lines, "book.herbcraft.guide.errata.none", ChatFormatting.DARK_GRAY);
         }
         default -> add(lines, "book.herbcraft.guide.empty", ChatFormatting.DARK_GRAY);
      }
      return lines;
   }

   public static List<Component> lines(ServerPlayer player, String entryId, HerbDefinition definition) {
      List<Component> lines = new ArrayList<>();
      String key = "herbal/" + entryId;
      KnowledgeAPI.Entry entry = KnowledgeAPI.chapter("herbal").map(ch -> ch.entry(entryId)).orElse(null);
      KnowledgeDisplayState display = entry == null ? KnowledgeDisplayState.UNKNOWN : KnowledgeAPI.displayState(player, "herbal", entry);
      KnowledgeFlags flags = KnowledgeAPI.flags(player, key);
      NatureProfile nature = HerbNatureAPI.getNatureOrFallback(new ItemStack(definition.item()));
      if (display == KnowledgeDisplayState.ENCOUNTERED) {
         lines.add(Component.translatable("knowledge.herbcraft.encountered.description").withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.nature_unknown").withStyle(ChatFormatting.DARK_GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.traits_unknown").withStyle(ChatFormatting.DARK_GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.effects_unknown").withStyle(ChatFormatting.DARK_GRAY));
         return lines;
      }
      if (display == KnowledgeDisplayState.HEARD) {
         lines.add(HerbNatureAPI.natureLine(nature).append(Component.translatable("knowledge.herbcraft.claim.reported").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).append(Component.translatable("knowledge.herbcraft.claim.reported").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY));
         appendClaimLines(player, key, lines);
         lines.add(Component.translatable("knowledge.herbcraft.heard.not_tasted").withStyle(ChatFormatting.DARK_GRAY));
         return lines;
      }
      if (display == KnowledgeDisplayState.TASTED) {
         lines.add(HerbNatureAPI.natureLine(nature).append(Component.translatable("knowledge.herbcraft.claim.verified_by_taste").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).append(Component.translatable("knowledge.herbcraft.claim.verified_by_taste").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.taste.line", taste(nature)).withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.feeling.line", Component.translatable("knowledge.herbcraft.feeling." + nature.temperature())).withStyle(ChatFormatting.GRAY));
         if (flags.heard()) {
            appendClaimLines(player, key, lines);
         }
         appendExperienced(player, key, definition, lines, false);
         lines.add(Component.translatable("knowledge.herbcraft.tasted.precise_unknown").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
         return lines;
      }
      if (display == KnowledgeDisplayState.MASTERED) {
         lines.add(HerbNatureAPI.natureLine(nature).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         List<String> usageKeys = HerbUsageAPI.usageKeys(definition.item());
         if (!usageKeys.isEmpty()) lines.add(HerbUsageAPI.usageLine(usageKeys).withStyle(ChatFormatting.GOLD));
         lines.add(Component.translatable("book.herbcraft.entry.nutrition", definition.nutrition(), String.format("%.1f", definition.saturation())).withStyle(ChatFormatting.GRAY));
         for (HerbEffectEntry effect : definition.effects()) lines.add(preciseEffectLine(effect));
         if (flags.tasted()) {
            lines.add(Component.translatable("knowledge.herbcraft.taste.line", taste(nature)).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("knowledge.herbcraft.feeling.line", Component.translatable("knowledge.herbcraft.feeling." + nature.temperature())).withStyle(ChatFormatting.GRAY));
            appendExperienced(player, key, definition, lines, true);
         }
         for (String route : com.herbcraft.api.KnowledgePracticeRegistry.routes(key)) lines.add(Component.translatable("knowledge.herbcraft.practice.route", Component.translatable("knowledge.herbcraft.route." + route.replace(":", "."))).withStyle(ChatFormatting.GOLD));
         appendClaimCorrections(player, key, lines);
      }
      return lines;
   }

   public static List<Component> foodNatureLines(ServerPlayer player, Item item, String entryId) {
      List<Component> lines = new ArrayList<>();
      String key = HerbalChapter.CHAPTER_ID + "/" + entryId;
      KnowledgeAPI.Entry entry = KnowledgeAPI.chapter(HerbalChapter.CHAPTER_ID).map(ch -> ch.entry(entryId)).orElse(null);
      KnowledgeDisplayState display = entry == null ? KnowledgeDisplayState.UNKNOWN : KnowledgeAPI.displayState(player, HerbalChapter.CHAPTER_ID, entry);
      KnowledgeFlags flags = KnowledgeAPI.flags(player, key);
      NatureProfile nature = HerbNatureAPI.getNatureOrFallback(new ItemStack(item));
      if (display == KnowledgeDisplayState.ENCOUNTERED) {
         lines.add(Component.translatable("knowledge.herbcraft.encountered.description").withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.nature_unknown").withStyle(ChatFormatting.DARK_GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.traits_unknown").withStyle(ChatFormatting.DARK_GRAY));
         return lines;
      }
      if (display == KnowledgeDisplayState.HEARD) {
         lines.add(HerbNatureAPI.natureLine(nature).append(Component.translatable("knowledge.herbcraft.claim.reported").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).append(Component.translatable("knowledge.herbcraft.claim.reported").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY));
         appendClaimLines(player, key, lines);
         lines.add(Component.translatable("knowledge.herbcraft.heard.not_tasted").withStyle(ChatFormatting.DARK_GRAY));
         return lines;
      }
      if (display == KnowledgeDisplayState.TASTED) {
         lines.add(HerbNatureAPI.natureLine(nature).append(Component.translatable("knowledge.herbcraft.claim.verified_by_taste").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).append(Component.translatable("knowledge.herbcraft.claim.verified_by_taste").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.taste.line", taste(nature)).withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.feeling.line", Component.translatable("knowledge.herbcraft.feeling." + nature.temperature())).withStyle(ChatFormatting.GRAY));
         if (flags.heard()) appendClaimLines(player, key, lines);
         return lines;
      }
      if (display == KnowledgeDisplayState.MASTERED) {
         lines.add(HerbNatureAPI.natureLine(nature).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         lines.add(Component.translatable("knowledge.herbcraft.taste.line", taste(nature)).withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.feeling.line", Component.translatable("knowledge.herbcraft.feeling." + nature.temperature())).withStyle(ChatFormatting.GRAY));
         appendClaimCorrections(player, key, lines);
      }
      return lines;
   }

   private static void add(List<Component> lines, String key, ChatFormatting color) {
      lines.add(Component.translatable(key).withStyle(color));
   }

   private static void appendClaimLines(ServerPlayer player, String key, List<Component> lines) {
      for (KnowledgeClaim claim : KnowledgeRumorRegistry.claims(key)) {
         PlayerClaimMark mark = KnowledgeAPI.claimMark(player, key, claim.id());
         String prefix = switch (mark) {
            case THINK_TRUE -> "✓ ";
            case THINK_FALSE -> "✕ ";
            default -> "○ ";
         };
         lines.add(Component.literal(prefix).withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable(claim.textKey()).withStyle(ChatFormatting.GRAY)));
      }
   }

   private static void appendClaimCorrections(ServerPlayer player, String key, List<Component> lines) {
      for (KnowledgeClaim claim : KnowledgeRumorRegistry.claims(key)) {
         boolean verifiedTrue = KnowledgeAPI.verifiedTrueClaims(player).getOrDefault(key, Set.of()).contains(claim.id());
         boolean verifiedFalse = KnowledgeAPI.verifiedFalseClaims(player).getOrDefault(key, Set.of()).contains(claim.id());
         if (verifiedTrue || verifiedFalse) {
            lines.add(Component.translatable(verifiedTrue ? "knowledge.herbcraft.errata.true" : "knowledge.herbcraft.errata.false").withStyle(verifiedTrue ? ChatFormatting.GREEN : ChatFormatting.RED));
         }
      }
   }

   private static MutableComponent taste(NatureProfile nature) {
      return HerbNatureAPI.tasteFlavors(nature);
   }

   private static Component preciseEffectLine(HerbEffectEntry effect) {
      MobEffect mob = effect.effect().value();
      MutableComponent name = Component.translatable(mob.getDescriptionId());
      MutableComponent chance = Component.translatable("knowledge.herbcraft.effect.chance", Math.round(effect.chance() * 100.0F));
      if (effect.durationTicks() > 0) {
         chance = chance.append(Component.literal(" ")).append(Component.translatable("knowledge.herbcraft.effect.duration_seconds", effect.durationTicks() / 20));
      }
      return Component.literal("• ").append(name).append(Component.literal(" ")).append(chance).withStyle(effect.negative() ? ChatFormatting.RED : ChatFormatting.GREEN);
   }

   private static void appendExperienced(ServerPlayer player, String key, HerbDefinition definition, List<Component> lines, boolean precise) {
      Set<String> experienced = KnowledgeAPI.experiencedEffects(player).getOrDefault(key, Set.of());
      if (experienced.isEmpty()) {
         lines.add(Component.translatable("knowledge.herbcraft.tasted.no_effect_observed").withStyle(ChatFormatting.DARK_GRAY));
         return;
      }
      for (HerbEffectEntry effect : definition.effects()) {
         Identifier effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.effect().value());
         if (effectId == null || !experienced.contains(effectId.toString())) continue;
         MobEffect mob = effect.effect().value();
         Component name = Component.translatable(mob.getDescriptionId());
         lines.add(Component.translatable("knowledge.herbcraft.tasted.experienced_format", name, precise ? Component.literal((effect.durationTicks()/20) + "s") : durationFuzzy(effect.durationTicks())).withStyle(ChatFormatting.BLUE));
      }
   }

   private static Component durationFuzzy(int ticks) {
      if (ticks <= 60) return Component.translatable("knowledge.herbcraft.duration.instant");
      if (ticks <= 200) return Component.translatable("knowledge.herbcraft.duration.seconds");
      if (ticks <= 600) return Component.translatable("knowledge.herbcraft.duration.moment");
      return Component.translatable("knowledge.herbcraft.duration.long");
   }

   private static void addFlavorPrinciples(ServerPlayer player, List<Component> lines) {
      PlayerPharmacologyState state = ((HerbcraftPlayerData) player).herbcraft$pharmacologyState();
      appendSingleDiscovery(lines, state, "flavor_sweet", "book.herbcraft.guide.flavors.sweet");
      appendSingleDiscovery(lines, state, "flavor_bitter", "book.herbcraft.guide.flavors.bitter");
      appendSingleDiscovery(lines, state, "flavor_pungent", "book.herbcraft.guide.flavors.pungent");
      appendSingleDiscovery(lines, state, "flavor_sour", "book.herbcraft.guide.flavors.sour");
      appendSingleDiscovery(lines, state, "flavor_astringent", "book.herbcraft.guide.flavors.astringent");
      appendSingleDiscovery(lines, state, "flavor_salty", "book.herbcraft.guide.flavors.salty");
      appendSingleDiscovery(lines, state, "flavor_salty_water", "book.herbcraft.guide.flavors.salty_water");
      appendSingleDiscovery(lines, state, "flavor_bland", "book.herbcraft.guide.flavors.bland");
      appendSingleDiscovery(lines, state, "temperature_harmonize", "book.herbcraft.guide.flavors.temperature_harmonize");
      appendSingleDiscovery(lines, state, "temperature_same_hot", "book.herbcraft.guide.flavors.temperature_same_hot");
      appendSingleDiscovery(lines, state, "temperature_same_cold", "book.herbcraft.guide.flavors.temperature_same_cold");
   }

   private static void addSevenEmotionPrinciples(ServerPlayer player, List<Component> lines) {
      PlayerPharmacologyState state = ((HerbcraftPlayerData) player).herbcraft$pharmacologyState();
      appendPairDiscovery(lines, state, "seven_xiang_xu", "book.herbcraft.guide.seven.xiang_xu");
      appendPairDiscovery(lines, state, "seven_xiang_shi", "book.herbcraft.guide.seven.xiang_shi");
      appendPairDiscovery(lines, state, "seven_xiang_sha", "book.herbcraft.guide.seven.xiang_sha");
      appendPairDiscovery(lines, state, "seven_xiang_wei", "book.herbcraft.guide.seven.xiang_wei");
      appendPairDiscovery(lines, state, "seven_xiang_e", "book.herbcraft.guide.seven.xiang_e");
      appendPairDiscovery(lines, state, "seven_xiang_fan", "book.herbcraft.guide.seven.xiang_fan");
      appendSingleDiscovery(lines, state, "seven_single", "book.herbcraft.guide.seven.single");
   }

   private static void appendSingleDiscovery(List<Component> lines, PlayerPharmacologyState state, String key, String translationKey) {
      state.discovery(key).ifPresent(discovery -> {
         lines.add(Component.translatable(translationKey, itemName(discovery.currentItemId())).withStyle(ChatFormatting.DARK_AQUA));
         appendDiscoveryCount(lines, discovery);
      });
   }

   private static void appendPairDiscovery(List<Component> lines, PlayerPharmacologyState state, String key, String translationKey) {
      state.discovery(key).ifPresent(discovery -> {
         lines.add(Component.translatable(translationKey, itemName(discovery.currentItemId()), itemName(discovery.previousItemId())).withStyle(ChatFormatting.DARK_AQUA));
         appendDiscoveryCount(lines, discovery);
      });
   }

   private static void appendDiscoveryCount(List<Component> lines, PlayerPharmacologyState.PrincipleDiscovery discovery) {
      if (discovery.count() > 1) {
         lines.add(Component.translatable("book.herbcraft.guide.seven.count", discovery.count()).withStyle(ChatFormatting.DARK_GRAY));
      }
   }

   private static Component itemName(String itemId) {
      if (itemId == null || itemId.isBlank()) {
         return Component.translatable("book.herbcraft.guide.example.unknown");
      }
      try {
         return Component.translatable(BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId)).getDescriptionId());
      } catch (Exception ignored) {
         return Component.literal(itemId);
      }
   }

   private static HerbKnowledgeStats herbStats(ServerPlayer player) {
      int encountered = 0, heard = 0, tasted = 0, mastered = 0;
      Set<String> counted = new LinkedHashSet<>();
      for (Item item : HerbcraftAPI.getHerbRegistry().keySet()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(item);
         if (id == null) continue;
         String entryId = id.getPath();
         String key = KnowledgeAPI.key(HerbalChapter.CHAPTER_ID, entryId);
         counted.add(entryId);
         KnowledgeFlags flags = KnowledgeAPI.flags(player, key);
         if (flags.encountered()) encountered++;
         if (flags.heard()) heard++;
         if (flags.tasted()) tasted++;
         if (KnowledgeAPI.isMastered(player, key, flags)) mastered++;
      }
      for (Identifier id : foodNatureItemIds()) {
         String entryId = HerbalChapter.foodNatureEntryId(id);
         if (!counted.add(entryId)) continue;
         String key = KnowledgeAPI.key(HerbalChapter.CHAPTER_ID, entryId);
         KnowledgeFlags flags = KnowledgeAPI.flags(player, key);
         if (flags.encountered()) encountered++;
         if (flags.heard()) heard++;
         if (flags.tasted()) tasted++;
         if (KnowledgeAPI.isMastered(player, key, flags)) mastered++;
      }
      return new HerbKnowledgeStats(encountered, heard, tasted, mastered);
   }

   private static TastedHerb firstTastedHerb(ServerPlayer player) {
      for (Item item : HerbcraftAPI.getHerbRegistry().keySet()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(item);
         if (id == null) continue;
         if (KnowledgeAPI.flags(player, KnowledgeAPI.key(HerbalChapter.CHAPTER_ID, id.getPath())).tasted()) {
            return new TastedHerb(item, HerbNatureAPI.getNatureOrFallback(new ItemStack(item)));
         }
      }
      for (Identifier id : foodNatureItemIds()) {
         if (KnowledgeAPI.flags(player, KnowledgeAPI.key(HerbalChapter.CHAPTER_ID, HerbalChapter.foodNatureEntryId(id))).tasted()) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            return new TastedHerb(item, HerbNatureAPI.getNatureOrFallback(new ItemStack(item)));
         }
      }
      return null;
   }

   private static int discoveredFlavorCount(ServerPlayer player) {
      Set<String> flavors = new LinkedHashSet<>();
      for (Item item : HerbcraftAPI.getHerbRegistry().keySet()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(item);
         if (id == null) continue;
         if (KnowledgeAPI.flags(player, KnowledgeAPI.key(HerbalChapter.CHAPTER_ID, id.getPath())).tasted()) {
            flavors.addAll(HerbNatureAPI.getNatureOrFallback(new ItemStack(item)).flavors());
         }
      }
      for (Identifier id : foodNatureItemIds()) {
         if (KnowledgeAPI.flags(player, KnowledgeAPI.key(HerbalChapter.CHAPTER_ID, HerbalChapter.foodNatureEntryId(id))).tasted()) {
            flavors.addAll(HerbNatureAPI.getNatureOrFallback(id).flavors());
         }
      }
      return flavors.size();
   }

   private static List<Identifier> foodNatureItemIds() {
      List<Identifier> ids = new ArrayList<>();
      for (Identifier id : HerbNatureRegistry.profiles().keySet()) {
         if (HerbalChapter.hasFoodNatureEntry(id) && BuiltInRegistries.ITEM.containsKey(id)) {
            ids.add(id);
         }
      }
      return ids;
   }

   private static int tastedIngredientCount(ServerPlayer player) {
      return KnowledgeAPI.chapter(IngredientsChapter.CHAPTER_ID).map(chapter -> {
         int count = 0;
         for (KnowledgeAPI.Entry entry : chapter.entries()) {
            if (!"overview".equals(entry.id()) && KnowledgeAPI.flags(player, KnowledgeAPI.key(IngredientsChapter.CHAPTER_ID, entry.id())).tasted()) {
               count++;
            }
         }
         return count;
      }).orElse(0);
   }

   private static int verifiedHerbalClaimCount(ServerPlayer player) {
      int count = 0;
      for (var entry : KnowledgeAPI.verifiedTrueClaims(player).entrySet()) if (entry.getKey().startsWith(HerbalChapter.CHAPTER_ID + "/")) count += entry.getValue().size();
      for (var entry : KnowledgeAPI.verifiedFalseClaims(player).entrySet()) if (entry.getKey().startsWith(HerbalChapter.CHAPTER_ID + "/")) count += entry.getValue().size();
      return count;
   }

   private static boolean advancementDone(ServerPlayer player, String namespace, String path) {
      var server = player.level().getServer();
      if (server == null) return false;
      var advancement = server.getAdvancements().get(Identifier.fromNamespaceAndPath(namespace, path));
      return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
   }

   private record HerbKnowledgeStats(int encountered, int heard, int tasted, int mastered) {
   }

   private record TastedHerb(Item item, NatureProfile nature) {
   }
}
