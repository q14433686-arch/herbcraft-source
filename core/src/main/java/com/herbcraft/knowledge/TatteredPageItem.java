package com.herbcraft.knowledge;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeDisplayState;
import com.herbcraft.api.KnowledgeFlags;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.api.PlayerClaimMark;
import com.herbcraft.guide.GuideHintManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class TatteredPageItem extends Item {
   public TatteredPageItem(Properties properties) {
      super(properties);
   }

   @Override
   public net.minecraft.network.chat.Component getName(ItemStack stack) {
      PageKind kind = (PageKind) stack.get(CodexComponents.PAGE_KIND);
      if (kind != null && kind != PageKind.COMMON) {
         return net.minecraft.network.chat.Component.translatable("item.herbcraft.tattered_page." + kind.getSerializedName());
      }
      return super.getName(stack);
   }

      public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return InteractionResult.SUCCESS;
      } else {
         CodexComponents.PageEntry target = (CodexComponents.PageEntry)stack.get(CodexComponents.PAGE_ENTRY);
         String chapterId = target != null && !target.chapter().isEmpty() ? target.chapter() : anyChapter(serverPlayer);
         if (chapterId == null) {
            return InteractionResult.FAIL;
         } else {
            PageKind kind = (PageKind)stack.get(CodexComponents.PAGE_KIND);
            if (kind == null) kind = PageKind.COMMON;
            Component learnedTitle = null;
            String entryId = target != null && !target.entry().isEmpty() ? target.entry() : null;
            if (entryId == null) {
               // Different page kinds target different knowledge states
               Optional<KnowledgeAPI.Entry> random = pickTargetEntry(serverPlayer, chapterId, kind);
               if (random.isEmpty()) {
                  serverPlayer.sendOverlayMessage(Component.translatable("message.herbcraft.page.nothing_new"));
                  return InteractionResult.FAIL;
               }
               entryId = random.get().id();
               learnedTitle = random.get().title();
            } else {
               String finalEntryId = entryId;
               learnedTitle = KnowledgeAPI.chapter(chapterId).map(chapter -> chapter.entries().stream().filter(e -> e.id().equals(finalEntryId)).findFirst().map(KnowledgeAPI.Entry::title).orElse(null)).orElse(null);
            }
            switch (kind) {
               case ERRATA -> {
                  KnowledgeFlags flags = KnowledgeAPI.flags(serverPlayer, KnowledgeAPI.key(chapterId, entryId));
                  if (!flags.heard()) {
                     KnowledgeManager.markHeard(serverPlayer, chapterId, entryId, false);
                  }
                  if (!resolveErrata(serverPlayer, chapterId + "/" + entryId)) {
                     // No unverified suspicious claims — don't consume the page
                     return InteractionResult.FAIL;
                  }
               }
               case RARE -> {
                  // Rare page: ancient detailed account = heard + tasted + practiced → pushes toward mastery
                  KnowledgeManager.markHeard(serverPlayer, chapterId, entryId, false);
                  KnowledgeAPI.markTasted(serverPlayer, KnowledgeAPI.key(chapterId, entryId));
                  KnowledgeAPI.markPracticed(serverPlayer, KnowledgeAPI.key(chapterId, entryId));
               }
               case TRADE -> {
                  // Trade page: merchant knowledge = heard; if already heard, adds practice credit
                  boolean wasNew = KnowledgeManager.markHeard(serverPlayer, chapterId, entryId, false);
                  if (!wasNew) {
                     // Already heard → merchant's repeated testimony counts as practice evidence
                     KnowledgeAPI.markTasted(serverPlayer, KnowledgeAPI.key(chapterId, entryId));
                  }
               }
               default -> {
                  // Common page: basic hear
                  KnowledgeManager.markHeard(serverPlayer, chapterId, entryId, false);
               }
            }

            HerbcraftAdvancements.grant(serverPlayer, "first_page");
            GuideHintManager.show(serverPlayer, "herbcraft:first_page_read");
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverPlayer.sendOverlayMessage(Component.translatable("message.herbcraft.page.learned", new Object[]{learnedTitle}));
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
         }
      }
   }


   private static boolean resolveErrata(ServerPlayer player, String entryKey) {
      // Only verify suspicious claims — don't waste errata pages on normal claims
      KnowledgeClaim picked = null;
      for (KnowledgeClaim claim : KnowledgeRumorRegistry.claims(entryKey)) {
         boolean resolved = KnowledgeAPI.verifiedTrueClaims(player).getOrDefault(entryKey, java.util.Set.of()).contains(claim.id()) || KnowledgeAPI.verifiedFalseClaims(player).getOrDefault(entryKey, java.util.Set.of()).contains(claim.id());
         if (!resolved && claim.suspicious()) { picked = claim; break; }
      }
      if (picked == null) { player.sendOverlayMessage(Component.translatable("knowledge.herbcraft.errata.none")); return false; }
      KnowledgeAPI.verifyClaim(player, entryKey, picked.id(), picked.truth());
      PlayerClaimMark mark = KnowledgeAPI.claimMark(player, entryKey, picked.id());
      Component verdict = Component.translatable(picked.truth() ? "knowledge.herbcraft.errata.true" : "knowledge.herbcraft.errata.false");
      if ((mark == PlayerClaimMark.THINK_TRUE && picked.truth()) || (mark == PlayerClaimMark.THINK_FALSE && !picked.truth())) verdict = verdict.copy().append(Component.literal(" ")).append(Component.translatable("knowledge.herbcraft.errata.player_correct"));
      else if (mark != PlayerClaimMark.UNMARKED) verdict = verdict.copy().append(Component.literal(" ")).append(Component.translatable("knowledge.herbcraft.errata.player_wrong"));
      player.sendOverlayMessage(verdict);
      return true;
   }

   /**
    * Pick a target entry based on page kind:
    * COMMON  → unknown entries only (learn something new)
    * TRADE   → prefer unknown, but can also pick encountered/heard (merchant expands knowledge)
    * RARE    → any non-mastered entry (rare page can advance any incomplete knowledge)
    * ERRATA  → heard or tasted entries only (need existing claims to verify)
    */
   private static Optional<KnowledgeAPI.Entry> pickTargetEntry(ServerPlayer player, String chapterId, PageKind kind) {
      KnowledgeAPI.Chapter chapter = KnowledgeAPI.chapter(chapterId).orElse(null);
      if (chapter == null) return Optional.empty();

      java.util.List<KnowledgeAPI.Entry> unknown = new java.util.ArrayList<>();
      java.util.List<KnowledgeAPI.Entry> partial = new java.util.ArrayList<>(); // encountered, heard, tasted
      java.util.List<KnowledgeAPI.Entry> heardOrTasted = new java.util.ArrayList<>();

      for (KnowledgeAPI.Entry e : chapter.entries()) {
         KnowledgeDisplayState state = KnowledgeAPI.displayState(player, chapterId, e);
         switch (state) {
            case UNKNOWN -> unknown.add(e);
            case ENCOUNTERED, HEARD, TASTED -> { partial.add(e); if (state == KnowledgeDisplayState.HEARD || state == KnowledgeDisplayState.TASTED) heardOrTasted.add(e); }
            default -> {} // MASTERED — skip
         }
      }

      java.util.List<KnowledgeAPI.Entry> candidates = switch (kind) {
         case COMMON -> unknown;
         case TRADE -> unknown.isEmpty() ? partial : unknown; // prefer unknown, fallback to partial
         case RARE -> partial; // Rare pages enhance existing knowledge, not discover new
         case ERRATA -> {
            // Only pick entries that have unverified suspicious claims
            java.util.List<KnowledgeAPI.Entry> withClaims = new java.util.ArrayList<>();
            for (KnowledgeAPI.Entry e : heardOrTasted.isEmpty() ? partial : heardOrTasted) {
               String eKey = chapterId + "/" + e.id();
               for (KnowledgeClaim c : KnowledgeRumorRegistry.claims(eKey)) {
                  if (c.suspicious()) {
                     boolean resolved = KnowledgeAPI.verifiedTrueClaims(player).getOrDefault(eKey, java.util.Set.of()).contains(c.id())
                        || KnowledgeAPI.verifiedFalseClaims(player).getOrDefault(eKey, java.util.Set.of()).contains(c.id());
                     if (!resolved) { withClaims.add(e); break; }
                  }
               }
            }
            yield withClaims;
         }
      };

      if (candidates.isEmpty()) return Optional.empty();
      KnowledgeAPI.Entry picked = candidates.get(player.getRandom().nextInt(candidates.size()));
      return Optional.of(picked);
   }

      private static String anyChapter(ServerPlayer player) {
      List<String> candidates = new ArrayList<>();

      for (KnowledgeAPI.Chapter chapter : KnowledgeAPI.chapters()) {
         for (KnowledgeAPI.Entry entry : chapter.entries()) {
            if (KnowledgeAPI.state(player, chapter.id(), entry) == KnowledgeAPI.State.UNKNOWN) {
               candidates.add(chapter.id());
               break;
            }
         }
      }

      return candidates.isEmpty() ? null : candidates.get(player.getRandom().nextInt(candidates.size()));
   }
}
