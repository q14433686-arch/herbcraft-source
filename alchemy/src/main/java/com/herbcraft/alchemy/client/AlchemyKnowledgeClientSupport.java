package com.herbcraft.alchemy.client;

import com.herbcraft.alchemy.armor.ArmorSupportComponent;
import com.herbcraft.alchemy.armor.ArmorSupportComponents;
import com.herbcraft.alchemy.coating.CoatingComponents;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import com.herbcraft.alchemy.knowledge.AlchemyKnowledgeSupport;
import com.herbcraft.knowledge.client.KnowledgeClientCache;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public final class AlchemyKnowledgeClientSupport {
   private AlchemyKnowledgeClientSupport() {
   }

   public static int potionState(ItemStack stack) {
      return AlchemyKnowledgeSupport.potionBaseId(stack).map(id -> KnowledgeClientCache.state("alchemy", "potion_" + id)).orElse(0);
   }

   public static int distilledEssenceState(ItemStack stack) {
      DistilledEssenceComponent component = stack.get(DistillationComponents.DISTILLED_ESSENCE);
      return component == null ? 0 : KnowledgeClientCache.state("alchemy", "essence_" + component.sourceEssence());
   }

   public static int armorSupportState(ItemStack stack) {
      ArmorSupportComponent component = stack.get(ArmorSupportComponents.ARMOR_SUPPORT);
      return component == null ? 0 : KnowledgeClientCache.state("alchemy", "essence_" + component.sourceEssence());
   }

   public static int coatingState(ItemStack stack) {
      WeaponCoating coating = stack.get(CoatingComponents.WEAPON_COATING);
      return coating == null ? 0 : KnowledgeClientCache.state("alchemy", "essence_" + coating.essenceId());
   }

   public static boolean knowsAtLeast(ItemStack stack, int requiredPotionState) {
      return potionState(stack) >= requiredPotionState;
   }

   public static Optional<String> stateKey(int state) {
      return switch (state) {
         case 1 -> Optional.of("encountered");
         case 2 -> Optional.of("heard");
         case 3 -> Optional.of("tasted");
         case 4 -> Optional.of("mastered");
         default -> Optional.empty();
      };
   }
}
