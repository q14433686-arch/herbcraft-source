package com.herbcraft.pharmacology;

import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.api.HerbcraftEvents;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.knowledge.HerbalChapter;
import com.herbcraft.knowledge.IngredientsChapter;
import com.herbcraft.logic.EffectResolver;
import com.herbcraft.logic.HerbStackManager;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.nutrition.NutritionRegistry;
import com.herbcraft.registry.HerbEntry;
import com.herbcraft.registry.HerbFoodRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PharmacologyResolver {
   private PharmacologyResolver() {
   }

   public static void resolveFoodNatureOnly(ItemStack stack, Player player, long gameTime) {
      if (stack.isEmpty() || HerbFoodRegistry.get(stack.getItem()) != null) {
         return;
      }
      Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      if (id == null || HerbNatureAPI.getExplicitNature(id).isEmpty() || NutritionRegistry.get(stack).isEmpty() || !IngredientsChapter.hasEntry(id)) {
         return;
      }
      NatureProfile profile = HerbNatureAPI.getExplicitNature(id).orElse(NatureProfile.DEFAULT);
      // Nature-only food bridge: let explicitly profiled ordinary foods participate in
      // qi / flavor / seven-emotion pharmacology without overriding their FOOD or
      // CONSUMABLE components through the startup-only herbs path.
      HerbEntry inertEntry = new HerbEntry(stack.getItem(), 0, 0.0F, 1.6F, false, false);
      PharmacologyResult result = resolve(stack, player, inertEntry, gameTime);
      if (player instanceof ServerPlayer serverPlayer) {
         recordFoodNatureFlavorDiscoveries(serverPlayer, profile, id.toString(), gameTime);
         if (HerbalChapter.hasFoodNatureEntry(id)) {
            String entryId = HerbalChapter.foodNatureEntryId(id);
            KnowledgeManager.markEncountered(serverPlayer, HerbalChapter.CHAPTER_ID, entryId);
            KnowledgeManager.markTasted(serverPlayer, HerbalChapter.CHAPTER_ID, entryId);
         }
         HerbcraftEvents.FOOD_NATURE_RESOLVED.invoker().onFoodNatureResolved(serverPlayer, stack.copy(), id, profile, result);
      }
   }

   private static void recordFoodNatureFlavorDiscoveries(ServerPlayer player, NatureProfile profile, String currentItemId, long gameTime) {
      PlayerPharmacologyState state = ((HerbcraftPlayerData)player).herbcraft$pharmacologyState();
      for (String flavor : profile.flavors()) {
         switch (flavor) {
            case "sweet" -> state.recordDiscovery("flavor_sweet", currentItemId, "", gameTime);
            case "pungent" -> state.recordDiscovery("flavor_pungent", currentItemId, "", gameTime);
            case "sour" -> state.recordDiscovery("flavor_sour", currentItemId, "", gameTime);
            case "astringent" -> state.recordDiscovery("flavor_astringent", currentItemId, "", gameTime);
            case "bland" -> state.recordDiscovery("flavor_bland", currentItemId, "", gameTime);
            default -> {
            }
         }
      }
   }

   public static PharmacologyResult resolve(ItemStack stack, Player player, HerbEntry entry, long gameTime) {
      PharmacologyRules rules = PharmacologyRules.current();
      PlayerPharmacologyState state = ((HerbcraftPlayerData)player).herbcraft$pharmacologyState();
      long nowMillis = System.currentTimeMillis();
      state.regressQi(gameTime, rules);
      state.cleanup(gameTime, nowMillis, rules.windowTicks);

      NatureProfile profile = HerbNatureAPI.getNatureOrFallback(stack);
      Identifier currentIdentifier = BuiltInRegistries.ITEM.getKey(stack.getItem());
      String currentItemId = currentIdentifier == null ? "" : currentIdentifier.toString();
      boolean hasPositiveEffects = entry.effects.stream().anyMatch(HerbEntry.Effect::positive);
      boolean hasNegativeEffects = entry.effects.stream().anyMatch(effect -> !effect.positive()) || !entry.damageEvents.isEmpty();
      boolean hasTimedEffects = !entry.effects.isEmpty();
      boolean hasHealing = !entry.healEvents.isEmpty() || entry.effects.stream().anyMatch(effect -> healingLike(effect.effect()));
      float probabilityMultiplier = 1.0F;
      float probabilityBonus = 0.0F;
      float positiveDurationMultiplier = 1.0F;
      float negativeDurationMultiplier = 1.0F;
      int positiveAmplifierBonus = 0;
      float healMultiplier = 1.0F;
      float damageMultiplier = 1.0F;
      float immunityMultiplier = 1.0F;
      boolean cancelAll = false;
      SevenEmotionType sevenEmotion = SevenEmotionType.NONE;
      String overlayKey = null;

      int tempValue = rules.temperatureValue(profile.temperature());
      int qiBefore = state.qiBalance();
      if (tempValue != 0 && qiBefore != 0 && Integer.signum(tempValue) != Integer.signum(qiBefore)) {
         probabilityMultiplier *= rules.harmonizeNegativeProbabilityMultiplier;
         negativeDurationMultiplier *= rules.harmonizeNegativeDurationMultiplier;
         overlayKey = "message.herbcraft.pharmacology.harmonize";
         state.recordDiscovery("temperature_harmonize", currentItemId, "", gameTime);
      } else if (tempValue != 0 && qiBefore != 0 && Integer.signum(tempValue) == Integer.signum(qiBefore) && Math.abs(qiBefore) >= rules.sameQiThreshold) {
         probabilityMultiplier *= rules.sameQiNegativeProbabilityMultiplier;
         negativeDurationMultiplier *= rules.sameQiNegativeDurationMultiplier;
         overlayKey = tempValue > 0 ? "message.herbcraft.pharmacology.same_qi_hot" : "message.herbcraft.pharmacology.same_qi_cold";
         state.recordDiscovery(tempValue > 0 ? "temperature_same_hot" : "temperature_same_cold", currentItemId, "", gameTime);
      }

      for (String flavor : profile.flavors()) {
         switch (flavor) {
            case "sweet" -> {
               healMultiplier *= rules.sweetHealMultiplier;
               if (hasHealing) {
                  state.recordDiscovery("flavor_sweet", currentItemId, "", gameTime);
               }
            }
            case "bitter" -> {
               immunityMultiplier *= rules.bitterImmunityMultiplier;
               HerbStackManager.decrement(player, gameTime);
               state.recordDiscovery("flavor_bitter", currentItemId, "", gameTime);
            }
            case "pungent" -> {
               positiveDurationMultiplier *= rules.pungentPositiveDurationMultiplier;
               boolean nausea = EffectResolver.roll(player, rules.pungentNauseaChance);
               if (nausea) {
                  player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, rules.pungentNauseaTicks, 0));
               }
               if (hasPositiveEffects || nausea) {
                  state.recordDiscovery("flavor_pungent", currentItemId, "", gameTime);
               }
            }
            case "sour" -> {
               positiveDurationMultiplier *= rules.sourPositiveDurationMultiplier;
               negativeDurationMultiplier *= rules.sourNegativeDurationMultiplier;
               if (hasTimedEffects) {
                  state.recordDiscovery("flavor_sour", currentItemId, "", gameTime);
               }
            }
            case "astringent" -> {
               positiveDurationMultiplier *= rules.sourPositiveDurationMultiplier;
               negativeDurationMultiplier *= rules.sourNegativeDurationMultiplier;
               if (hasTimedEffects) {
                  state.recordDiscovery("flavor_astringent", currentItemId, "", gameTime);
               }
            }
            case "salty" -> {
               immunityMultiplier *= rules.saltyImmunityMultiplier;
               state.recordDiscovery("flavor_salty", currentItemId, "", gameTime);
               if (nearWater(player)) {
                  positiveDurationMultiplier *= rules.saltyWaterDurationMultiplier;
                  negativeDurationMultiplier *= rules.saltyWaterDurationMultiplier;
                  state.recordDiscovery("flavor_salty_water", currentItemId, "", gameTime);
               }
            }
            case "bland" -> {
               probabilityMultiplier *= rules.blandSideEffectProbabilityMultiplier;
               if (hasNegativeEffects) {
                  state.recordDiscovery("flavor_bland", currentItemId, "", gameTime);
               }
            }
            default -> {
            }
         }
      }

      if (profile.flavors().contains("astringent") && player.getHealth() < 8.0F && state.consumeAstringentHealBonus(gameTime, rules.astringentCooldownTicks)) {
         healMultiplier += 0.35F;
         state.recordDiscovery("flavor_astringent", currentItemId, "", gameTime);
      }

      if (gameTime < state.toxicProtectionExpiresAt() && hasTrait(profile, "toxic")) {
         negativeDurationMultiplier *= 0.6F;
      }

      PlayerPharmacologyState.RecentUse latest = state.latest().orElse(null);
      if (latest != null) {
         SevenEmotionType type = judgeSevenEmotion(profile, latest, state.recentUses(), gameTime, rules);
         sevenEmotion = type;
         String sevenDiscovery = discoveryKey(type);
         if (sevenDiscovery != null) {
            state.recordDiscovery(sevenDiscovery, currentItemId, latest.itemId(), gameTime);
         }
         switch (type) {
            case XIANG_FAN -> {
               cancelAll = true;
               state.setQiBalance(0);
               if (rules.xiangFanAppliesEffects) {
                  player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 1));
                  player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
               }
            }
            case XIANG_SHA -> {
               negativeDurationMultiplier *= 0.6F;
               state.setToxicProtectionExpiresAt(gameTime + 400L);
               clearRecentToxicNegatives(player, latest);
            }
            case XIANG_WEI -> negativeDurationMultiplier *= 0.5F;
            case XIANG_XU -> {
               positiveAmplifierBonus += 1;
               healMultiplier *= 1.2F;
            }
            case XIANG_SHI -> positiveDurationMultiplier *= 1.15F;
            case XIANG_E -> {
               probabilityMultiplier *= 0.75F;
               healMultiplier *= 0.75F;
               damageMultiplier *= 0.75F;
            }
            case SINGLE -> probabilityBonus += 0.05F;
            default -> {
            }
         }
      }

      state.setQiBalance(qiBefore + tempValue);
      if (currentIdentifier != null) {
         state.addUse(currentIdentifier.toString(), profile, gameTime, nowMillis);
      }

      if (sevenEmotion.messageKey() != null) {
         player.sendOverlayMessage(Component.translatable(sevenEmotion.messageKey()));
      } else if (overlayKey != null) {
         player.sendOverlayMessage(Component.translatable(overlayKey));
      }

      return new PharmacologyResult(probabilityMultiplier, probabilityBonus, positiveDurationMultiplier, negativeDurationMultiplier, positiveAmplifierBonus, healMultiplier, damageMultiplier, immunityMultiplier, cancelAll, sevenEmotion);
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

   public static boolean healingLike(Holder<MobEffect> effect) {
      return effect.is(MobEffects.INSTANT_HEALTH) || effect.is(MobEffects.REGENERATION) || effect.is(MobEffects.ABSORPTION) || effect.is(MobEffects.SATURATION);
   }

   private static SevenEmotionType judgeSevenEmotion(NatureProfile current, PlayerPharmacologyState.RecentUse latest, List<PlayerPharmacologyState.RecentUse> history, long gameTime, PharmacologyRules rules) {
      NatureProfile previous = latest.profile();
      for (PharmacologyRules.SevenEmotionRule rule : rules.sevenEmotionRules()) {
         if (matchesSevenEmotionRule(rule, current, previous, latest, history, gameTime)) {
            return rule.type();
         }
      }
      return SevenEmotionType.NONE;
   }

   private static boolean matchesSevenEmotionRule(PharmacologyRules.SevenEmotionRule rule, NatureProfile current, NatureProfile previous, PlayerPharmacologyState.RecentUse latest, List<PlayerPharmacologyState.RecentUse> history, long gameTime) {
      if (rule.hotColdConflict() && !isHotColdConflict(current, previous)) {
         return false;
      }
      if (rule.withinTicks() >= 0 && gameTime - latest.gameTime() >= rule.withinTicks()) {
         return false;
      }
      if (!rule.eitherTraits().isEmpty() && rule.eitherTraits().stream().noneMatch(trait -> hasTrait(current, trait) || hasTrait(previous, trait))) {
         return false;
      }
      if (!hasAll(current, rule.currentTraits()) || !hasAll(previous, rule.previousTraits())) {
         return false;
      }
      if (!rule.currentAnyTraits().isEmpty() && !hasAny(current, rule.currentAnyTraits().toArray(String[]::new))) {
         return false;
      }
      if (!rule.previousAnyTraits().isEmpty() && !hasAny(previous, rule.previousAnyTraits().toArray(String[]::new))) {
         return false;
      }
      if (!rule.traitConflicts().isEmpty()) {
         boolean anyConflict = false;
         for (PharmacologyRules.TraitConflict conflict : rule.traitConflicts()) {
            if ((hasTrait(current, conflict.first()) && hasTrait(previous, conflict.second()))
               || (hasTrait(current, conflict.second()) && hasTrait(previous, conflict.first()))) {
               anyConflict = true;
               break;
            }
         }
         if (!anyConflict) {
            return false;
         }
      }
      if (rule.sharedTraitsMin() > 0 && sharedTraits(current, previous) < rule.sharedTraitsMin()) {
         return false;
      }
      if (rule.notSameProfile() && sameProfile(current, previous)) {
         return false;
      }
      if (rule.singleNoSharedWithHistory() && (history.isEmpty() || !history.stream().allMatch(use -> sharedTraits(current, use.profile()) == 0))) {
         return false;
      }
      return true;
   }

   private static void clearRecentToxicNegatives(Player player, PlayerPharmacologyState.RecentUse latest) {
      try {
         ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(latest.itemId())));
         HerbEntry entry = HerbFoodRegistry.get(stack);
         if (entry != null) {
            entry.effects.stream().filter(effect -> !effect.positive()).forEach(effect -> player.removeEffect(effect.effect()));
         }
      } catch (Exception ignored) {
      }
   }

   private static boolean nearWater(Player player) {
      BlockPos center = player.blockPosition();
      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
         if (player.level().getFluidState(pos).is(FluidTags.WATER)) {
            return true;
         }
      }
      return player.isInWaterOrRain();
   }

   private static boolean isHotColdConflict(NatureProfile a, NatureProfile b) {
      return ("hot".equals(a.temperature()) && "cold".equals(b.temperature())) || ("cold".equals(a.temperature()) && "hot".equals(b.temperature()));
   }

   private static boolean hasTrait(NatureProfile profile, String trait) {
      return profile.traits().contains(trait);
   }

   private static boolean hasAll(NatureProfile profile, List<String> traits) {
      for (String trait : traits) {
         if (!profile.traits().contains(trait)) {
            return false;
         }
      }
      return true;
   }

   private static boolean hasAny(NatureProfile profile, String... traits) {
      for (String trait : traits) {
         if (profile.traits().contains(trait)) {
            return true;
         }
      }
      return false;
   }

   private static int sharedTraits(NatureProfile a, NatureProfile b) {
      Set<String> traits = new HashSet<>(a.traits());
      traits.retainAll(b.traits());
      return traits.size();
   }

   private static boolean sameProfile(NatureProfile a, NatureProfile b) {
      return a.temperature().equals(b.temperature()) && a.flavors().equals(b.flavors()) && a.traits().equals(b.traits());
   }
}
