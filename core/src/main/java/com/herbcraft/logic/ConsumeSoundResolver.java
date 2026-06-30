package com.herbcraft.logic;

import com.herbcraft.registry.HerbEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;

public final class ConsumeSoundResolver {
   private ConsumeSoundResolver() {
   }

   public static Holder<SoundEvent> soundForHerb(HerbEntry entry) {
      if (entry.drink) {
         return SoundEvents.GENERIC_DRINK;
      } else {
         String path = itemPath(entry.item);
         if (containsAny(path, "ice", "snow", "frost")) {
            return holder(SoundEvents.GLASS_BREAK);
         } else if (containsAny(path, "resin", "slime", "honey")) {
            return holder(SoundEvents.HONEY_BLOCK_SLIDE);
         } else if (containsAny(path, "golden", "glow", "amethyst", "crystal")) {
            return holder(SoundEvents.AMETHYST_BLOCK_CHIME);
         } else {
            return containsAny(path, "crimson", "warped", "nether", "fungus", "wart") ? holder(SoundEvents.NETHER_WART_BREAK) : holder(SoundEvents.GRASS_STEP);
         }
      }
   }

   public static Holder<SoundEvent> soundForCuisine(boolean drinkLike) {
      return drinkLike ? SoundEvents.GENERIC_DRINK : SoundEvents.HONEY_DRINK;
   }

   private static String itemPath(Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return id == null ? "" : id.getPath();
   }

   private static boolean containsAny(String path, String... needles) {
      for (String needle : needles) {
         if (path.contains(needle)) {
            return true;
         }
      }

      return false;
   }

   private static Holder<SoundEvent> holder(SoundEvent event) {
      Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(event);
      return (Holder<SoundEvent>)(id == null
         ? SoundEvents.GENERIC_EAT
         : BuiltInRegistries.SOUND_EVENT
            .get(ResourceKey.create(Registries.SOUND_EVENT, id))
            .map(holder -> (Holder<SoundEvent>)holder)
            .orElse(SoundEvents.GENERIC_EAT));
   }
}
