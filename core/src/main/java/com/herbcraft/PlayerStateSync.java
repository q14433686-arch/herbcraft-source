package com.herbcraft;

import com.herbcraft.api.KnowledgeFlags;
import com.herbcraft.api.PlayerClaimMark;
import com.herbcraft.knowledge.KnowledgeSyncManager;
import com.herbcraft.nutrition.NutritionManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * Copies custom Herbcraft player state from the old player entity to the new respawned one.
 *
 * <p>Reason: on death/respawn Minecraft creates a new ServerPlayer instance. Mixin fields stored
 * directly on Player are not preserved automatically across this replacement, even if they are
 * persisted to disk later. Therefore all custom per-player runtime/persistent state that should
 * survive death must be copied explicitly during Fabric's COPY_FROM stage.</p>
 */
public final class PlayerStateSync {
   private PlayerStateSync() {
   }

   public static void register() {
      ServerPlayerEvents.COPY_FROM.register(PlayerStateSync::copyFromOldPlayer);
      ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
         NutritionManager.onLogin(newPlayer);
         KnowledgeSyncManager.sync(newPlayer);
         // Important: do NOT push a Codex screen open on respawn. The codex snapshot packet is also
         // used by the client as an "open the Codex UI now" signal. Sending it here would recreate
         // the Codex screen after death and could restore stale client-side last-viewed UI state.
      });
   }

   private static void copyFromOldPlayer(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
      HerbcraftPlayerData oldData = (HerbcraftPlayerData) oldPlayer;
      HerbcraftPlayerData newData = (HerbcraftPlayerData) newPlayer;

      // Death-reset runtime state: do not copy herb stack, effect immunities, pharmacology state,
      // or nutrition values/recent meals. Those are explicitly reset on death.
      copyWindowCounters(oldData, newData);
      copyKnowledge(oldData, newData);
      copyKnowledgeFlags(oldData, newData);
      copyExperiencedEffects(oldData, newData);
      copyClaimMarks(oldData, newData);
      copyVerifiedClaims(oldData, newData);
      copyGuideHints(oldData, newData);
   }

   private static void copyWindowCounters(HerbcraftPlayerData oldData, HerbcraftPlayerData newData) {
      // No public accessor exists for the counter map, and window counters are transient runtime
      // state; keep death-reset semantics by intentionally not copying them.
   }

   private static void copyKnowledge(HerbcraftPlayerData oldData, HerbcraftPlayerData newData) {
      newData.herbcraft$knowledge().clear();
      newData.herbcraft$knowledge().putAll(oldData.herbcraft$knowledge());
   }

   private static void copyKnowledgeFlags(HerbcraftPlayerData oldData, HerbcraftPlayerData newData) {
      newData.herbcraft$knowledgeFlags().clear();
      oldData.herbcraft$knowledgeFlags().forEach((key, flags) ->
         newData.herbcraft$knowledgeFlags().put(key, KnowledgeFlags.decode(flags.encode()))
      );
   }

   private static void copyExperiencedEffects(HerbcraftPlayerData oldData, HerbcraftPlayerData newData) {
      newData.herbcraft$experiencedEffects().clear();
      oldData.herbcraft$experiencedEffects().forEach((key, set) ->
         newData.herbcraft$experiencedEffects().put(key, new LinkedHashSet<>(set))
      );
   }

   private static void copyClaimMarks(HerbcraftPlayerData oldData, HerbcraftPlayerData newData) {
      newData.herbcraft$claimMarks().clear();
      oldData.herbcraft$claimMarks().forEach((entryKey, marks) -> {
         Map<String, PlayerClaimMark> copied = new HashMap<>();
         copied.putAll(marks);
         newData.herbcraft$claimMarks().put(entryKey, copied);
      });
   }

   private static void copyVerifiedClaims(HerbcraftPlayerData oldData, HerbcraftPlayerData newData) {
      newData.herbcraft$verifiedTrueClaims().clear();
      oldData.herbcraft$verifiedTrueClaims().forEach((key, set) ->
         newData.herbcraft$verifiedTrueClaims().put(key, new HashSet<>(set))
      );

      newData.herbcraft$verifiedFalseClaims().clear();
      oldData.herbcraft$verifiedFalseClaims().forEach((key, set) ->
         newData.herbcraft$verifiedFalseClaims().put(key, new HashSet<>(set))
      );
   }

   private static void copyGuideHints(HerbcraftPlayerData oldData, HerbcraftPlayerData newData) {
      Set<String> target = newData.herbcraft$seenGuideHints();
      target.clear();
      target.addAll(oldData.herbcraft$seenGuideHints());
   }
}
