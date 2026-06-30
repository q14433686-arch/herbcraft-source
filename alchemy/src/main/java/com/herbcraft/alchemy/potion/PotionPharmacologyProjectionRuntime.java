package com.herbcraft.alchemy.potion;

import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.nature.NatureProfile;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** P2 source-essence -> Core pharmacology projection metadata helpers. */
public final class PotionPharmacologyProjectionRuntime {
   private PotionPharmacologyProjectionRuntime() {
   }

   public static boolean applyMetadata(ItemStack stack) {
      String source = stack.get(AlchemyPotionComponents.POTION_SOURCE);
      if (source == null || source.isBlank()) {
         return false;
      }
      NatureProfile profile = HerbNatureAPI.getNatureOrFallback(Identifier.parse(source));
      PotionPharmacologyProjection next = new PotionPharmacologyProjection(
         source,
         profile.temperature(),
         profile.flavors(),
         profile.tasteFlavors(),
         profile.traits()
      );
      PotionPharmacologyProjection current = stack.get(AlchemyPotionComponents.POTION_PHARMACOLOGY);
      if (next.equals(current)) {
         return false;
      }
      stack.set(AlchemyPotionComponents.POTION_PHARMACOLOGY, next);
      return true;
   }

   public static Optional<PotionPharmacologyProjection> projection(ItemStack stack) {
      PotionPharmacologyProjection current = stack.get(AlchemyPotionComponents.POTION_PHARMACOLOGY);
      if (current != null) {
         return Optional.of(current);
      }
      return applyMetadata(stack) ? Optional.ofNullable(stack.get(AlchemyPotionComponents.POTION_PHARMACOLOGY)) : Optional.empty();
   }
}
