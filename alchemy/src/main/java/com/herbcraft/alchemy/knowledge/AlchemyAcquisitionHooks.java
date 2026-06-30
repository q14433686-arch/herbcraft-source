package com.herbcraft.alchemy.knowledge;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.alchemy.armor.ArmorSupportComponent;
import com.herbcraft.alchemy.armor.ArmorSupportComponents;
import com.herbcraft.alchemy.coating.CoatingComponents;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.guide.GuideHintManager;
import com.herbcraft.api.KnowledgePracticeRegistry;
import com.herbcraft.api.KnowledgeAPI.State;
import com.herbcraft.knowledge.KnowledgeInventoryScanner;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public final class AlchemyAcquisitionHooks {
   private AlchemyAcquisitionHooks() {
   }

   public static void register() {
      AlchemyRegistries.essenceItems().forEach(item -> AlchemyRegistries.essenceInfo(item).ifPresent(info -> KnowledgePracticeRegistry.registerRoute("herbal/" + info.id(), "alchemy:essence")));
      KnowledgeInventoryScanner.registerHandler(AlchemyAcquisitionHooks::onStack);
   }

   private static void onStack(ServerPlayer player, ItemStack stack) {
      Optional<AlchemyRegistries.EssenceInfo> essence = AlchemyRegistries.essenceInfo(stack.getItem());
      if (essence.isPresent()) {
         String id = essence.get().id();
         HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_essence");
         GuideHintManager.show(player, "herbcraft_alchemy:first_essence");
         boolean crafted = player.getStats().getValue(Stats.ITEM_CRAFTED.get(stack.getItem())) > 0;
         if (crafted) {
            KnowledgeManager.markPracticed(player, "alchemy", "essence_" + id, true);
            KnowledgeManager.markPracticed(player, "herbal", id, false);
         } else {
            // TODO: a future trade-specific hook can distinguish bought essences from other acquisition paths.
            KnowledgeManager.markHeard(player, "alchemy", "essence_" + id, true);
            KnowledgeManager.markHeard(player, "herbal", id, false);
         }
         KnowledgeManager.markHeard(player, "alchemy", "potion_" + id, false);
      }

      DistilledEssenceComponent distilled = stack.get(DistillationComponents.DISTILLED_ESSENCE);
      if (distilled != null && AlchemyRegistries.essenceInfoById(distilled.sourceEssence()).isPresent()) {
         HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_distilled_essence");
         KnowledgeManager.markEncountered(player, "alchemy", "essence_" + distilled.sourceEssence());
         KnowledgeManager.markHeard(player, "alchemy", "potion_" + distilled.sourceEssence(), false);
      }

      PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
      if (contents != null) {
         AdvancedPotionStackInitializer.initializeIfNeeded(stack, player.getRandom());
         contents = stack.get(DataComponents.POTION_CONTENTS);
         AdvancedPotionStackInitializer.potionPath(contents).ifPresent(potionName -> {
            if (isHerbcraftPotionPath(potionName)) {
               String base = sourceBaseId(potionName);
               KnowledgeManager.markEncountered(player, "alchemy", "potion_" + base);
               KnowledgeManager.markHeard(player, "alchemy", "potion_" + base, true);
               KnowledgeManager.markHeard(player, "herbal", base, false);
            }
         });
      }

      WeaponCoating coating = stack.get(CoatingComponents.WEAPON_COATING);
      if (coating != null) {
         HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_coating");
         KnowledgeManager.unlock(player, "alchemy", "coating_" + coating.essenceId(), State.MASTERED);
      }

      ArmorSupportComponent support = stack.get(ArmorSupportComponents.ARMOR_SUPPORT);
      if (support != null) {
         HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_armor_support");
         KnowledgeManager.markHeard(player, "alchemy", "essence_" + support.sourceEssence(), false);
         KnowledgeManager.markHeard(player, "alchemy", "potion_" + support.sourceEssence(), false);
      }
   }

   private static boolean isHerbcraftPotionPath(String potionName) {
      return AlchemyRegistries.essenceInfoById(sourceBaseId(potionName)).isPresent()
         || AdvancedPotionStackInitializer.processingStyleId(potionName).isPresent();
   }

   private static String sourceBaseId(String potionName) {
      if ("undead_venom".equals(potionName)) {
         return "flowering_azalea";
      }
      if ("cardiac_toxin".equals(potionName)) {
         return "lily_of_the_valley";
      }
      if ("wither_venom_iii".equals(potionName)) {
         return "wither_rose";
      }
      String stripped = stripModifierSuffix(potionName);
      return stripped.startsWith("witch_") ? stripped.substring("witch_".length()) : stripped;
   }

   private static String stripModifierSuffix(String potionName) {
      for (String suffix : new String[]{"_long", "_strong", "_clarified", "_murky"}) {
         if (potionName.endsWith(suffix)) {
            return potionName.substring(0, potionName.length() - suffix.length());
         }
      }

      return potionName;
   }
}
