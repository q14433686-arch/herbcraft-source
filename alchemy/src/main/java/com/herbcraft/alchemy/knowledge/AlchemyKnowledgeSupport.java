package com.herbcraft.alchemy.knowledge;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.alchemy.armor.ArmorSupportComponent;
import com.herbcraft.alchemy.armor.ArmorSupportComponents;
import com.herbcraft.alchemy.coating.CoatingComponents;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.api.KnowledgeManager;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public final class AlchemyKnowledgeSupport {
   private AlchemyKnowledgeSupport() {
   }

   public static Optional<String> potionBaseId(ItemStack stack) {
      PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
      return potionBaseId(contents);
   }

   public static Optional<String> potionBaseId(PotionContents contents) {
      if (contents == null) {
         return Optional.empty();
      }
      return AdvancedPotionStackInitializer.potionPath(contents)
         .map(AlchemyKnowledgeSupport::sourceBaseId)
         .filter(id -> AlchemyRegistries.essenceInfoById(id).isPresent());
   }

   public static void markPotionEncountered(ServerPlayer player, ItemStack stack) {
      potionBaseId(stack).ifPresent(base -> KnowledgeManager.markEncountered(player, AlchemyChapter.CHAPTER_ID, "potion_" + base));
   }

   public static void markPotionTasted(ServerPlayer player, ItemStack stack) {
      potionBaseId(stack).ifPresent(base -> {
         markPotionTasted(player, base);
         AdvancedPotionStackInitializer.potionPath(stack).ifPresent(path -> awardPotionAdvancements(player, path, stack));
      });
   }

   public static void markPotionTasted(ServerPlayer player, PotionContents contents) {
      potionBaseId(contents).ifPresent(base -> {
         markPotionTasted(player, base);
         ItemStack stack = new ItemStack(Items.LINGERING_POTION);
         stack.set(DataComponents.POTION_CONTENTS, contents);
         AdvancedPotionStackInitializer.initializeIfNeeded(stack, player.getRandom());
         AdvancedPotionStackInitializer.potionPath(stack).ifPresent(path -> awardPotionAdvancements(player, path, stack));
      });
   }

   private static void markPotionTasted(ServerPlayer player, String base) {
      KnowledgeManager.markEncountered(player, AlchemyChapter.CHAPTER_ID, "potion_" + base);
      KnowledgeManager.markTasted(player, AlchemyChapter.CHAPTER_ID, "potion_" + base);
   }

   private static void awardPotionAdvancements(ServerPlayer player, String potionName, ItemStack stack) {
      HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_potion");
      Optional<String> styleId = AdvancedPotionStackInitializer.processingStyleId(potionName);
      if (styleId.filter("purified"::equals).isPresent()) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_clarified");
      if (styleId.filter("sustained"::equals).isPresent()) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_sustained");
      if (styleId.filter("intense"::equals).isPresent()) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_intense");
      if (styleId.filter("murky_edge"::equals).isPresent()) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_murky");
      if (potionName.equals("undead_venom")) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "undead_toxin");
      if (potionName.startsWith("witch_")) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "witch_knowledge");
      AlchemyPotionComponents.PotionQuality quality = stack.get(AlchemyPotionComponents.POTION_QUALITY);
      if (quality == AlchemyPotionComponents.PotionQuality.EXCEPTIONAL) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "epic_potion");
   }

   public static boolean isPotionContainer(ItemStack stack) {
      return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION) || stack.is(Items.TIPPED_ARROW);
   }

   public static Optional<String> distilledEssenceId(ItemStack stack) {
      DistilledEssenceComponent component = stack.get(DistillationComponents.DISTILLED_ESSENCE);
      return component == null ? Optional.empty() : Optional.of(component.sourceEssence());
   }

   public static Optional<String> armorSupportId(ItemStack stack) {
      ArmorSupportComponent component = stack.get(ArmorSupportComponents.ARMOR_SUPPORT);
      return component == null ? Optional.empty() : Optional.of(component.sourceEssence());
   }

   public static Optional<String> coatingEssenceId(ItemStack stack) {
      WeaponCoating coating = stack.get(CoatingComponents.WEAPON_COATING);
      return coating == null ? Optional.empty() : Optional.of(coating.essenceId());
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
