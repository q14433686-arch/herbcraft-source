package com.herbcraft.alchemy.potion;

import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.pharmacology.PharmacologyRules;
import com.herbcraft.pharmacology.PlayerPharmacologyState;
import com.herbcraft.pharmacology.SevenEmotionType;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** P4 conservative processed-potion participation in Core recent-intake / seven-relation context. */
public final class PotionRelationRuntime {
   private PotionRelationRuntime() {
   }

   public static void onPotionConsumed(ServerPlayer player, ItemStack stack) {
      Optional<PotionPharmacologyProjection> source = PotionPharmacologyProjectionRuntime.projection(stack);
      Optional<PotionBodyStateProjection> body = PotionBodyStateProjectionRuntime.projection(stack);
      Optional<PotionProcessingRules.Style> style = AdvancedPotionStackInitializer.processingStyleId(stack).flatMap(PotionProcessingRules::style);
      if (source.isEmpty() || body.isEmpty() || style.isEmpty()) {
         return;
      }
      PlayerPharmacologyState state = ((HerbcraftPlayerData)player).herbcraft$pharmacologyState();
      PlayerPharmacologyState.RecentUse latest = state.latest().orElse(null);
      if (latest == null) {
         return;
      }
      NatureProfile current = relationProfile(source.get(), body.get(), style.get());
      SevenEmotionType type = judgeRelation(current, latest, state.recentUses(), player.level().getGameTime(), PharmacologyRules.current(), style.get());
      if (type == SevenEmotionType.NONE) {
         return;
      }
      String currentItemId = source.get().sourceEssence();
      String discovery = discoveryKey(type);
      if (discovery != null) {
         state.recordDiscovery(discovery, currentItemId, latest.itemId(), player.level().getGameTime());
      }
      switch (type) {
         case XIANG_FAN -> {
            state.setQiBalance(0);
         }
         case XIANG_SHA -> state.setToxicProtectionExpiresAt(player.level().getGameTime() + 400L);
         case XIANG_XU -> state.setQiBalance(state.qiBalance() + 6);
         case XIANG_E -> state.setQiBalance(state.qiBalance() - Integer.signum(state.qiBalance()) * 4);
         default -> {
         }
      }
      if (type.messageKey() != null) {
         player.sendOverlayMessage(Component.translatable(type.messageKey()));
      }
   }

   private static NatureProfile relationProfile(PotionPharmacologyProjection source, PotionBodyStateProjection body, PotionProcessingRules.Style style) {
      Set<String> traits = new HashSet<>(source.traits());
      traits.addAll(style.traitAffinity());
      traits.addAll(body.bodyTraits());
      return new NatureProfile(source.temperature(), source.flavors(), source.tasteFlavors(), List.copyOf(traits));
   }

   private static SevenEmotionType judgeRelation(
      NatureProfile current,
      PlayerPharmacologyState.RecentUse latest,
      List<PlayerPharmacologyState.RecentUse> history,
      long gameTime,
      PharmacologyRules rules,
      PotionProcessingRules.Style style
   ) {
      NatureProfile previous = latest.profile();
      boolean hotColdConflict = isHotColdConflict(current, previous);
      int sharedTraits = sharedTraits(current, previous);
      boolean stirringVsSedating = (hasTrait(current, "stirring") && hasTrait(previous, "sedating")) || (hasTrait(current, "sedating") && hasTrait(previous, "stirring"));
      boolean clearingVsToxic = (hasTrait(current, "clearing") && hasTrait(previous, "toxic")) || (hasTrait(current, "toxic") && hasTrait(previous, "clearing"));
      String relationModifier = style.relationModifier();
      if ((hotColdConflict && "conflict_amplifying".equals(relationModifier)) || (hotColdConflict && hasTrait(current, "toxic"))) {
         return SevenEmotionType.XIANG_FAN;
      }
      if (clearingVsToxic && !"conflict_amplifying".equals(relationModifier)) {
         return "conflict_buffering".equals(relationModifier) ? SevenEmotionType.XIANG_SHA : SevenEmotionType.XIANG_WEI;
      }
      if (sharedTraits >= 2 && !hotColdConflict) {
         return "compatibility_weaving".equals(relationModifier) ? SevenEmotionType.XIANG_SHI : SevenEmotionType.XIANG_XU;
      }
      if (stirringVsSedating || "volatile_conflict".equals(relationModifier)) {
         return SevenEmotionType.XIANG_E;
      }
      if (!history.isEmpty() && history.stream().allMatch(use -> sharedTraits(current, use.profile()) == 0)) {
         return SevenEmotionType.SINGLE;
      }
      return SevenEmotionType.NONE;
   }

   private static String discoveryKey(SevenEmotionType type) {
      return switch (type) {
         case XIANG_XU -> "seven_xiang_xu";
         case XIANG_SHI -> "seven_xiang_shi";
         case XIANG_SHA -> "seven_xiang_sha";
         case XIANG_WEI -> "seven_xiang_wei";
         case XIANG_E -> "seven_xiang_e";
         case XIANG_FAN -> "seven_xiang_fan";
         case SINGLE -> "seven_single";
         default -> null;
      };
   }

   private static boolean isHotColdConflict(NatureProfile a, NatureProfile b) {
      return ("hot".equals(a.temperature()) && "cold".equals(b.temperature())) || ("cold".equals(a.temperature()) && "hot".equals(b.temperature()));
   }

   private static boolean hasTrait(NatureProfile profile, String trait) {
      return profile.traits().contains(trait);
   }

   private static int sharedTraits(NatureProfile a, NatureProfile b) {
      Set<String> traits = new HashSet<>(a.traits());
      traits.retainAll(b.traits());
      return traits.size();
   }
}
