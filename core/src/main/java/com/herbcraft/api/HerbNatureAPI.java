package com.herbcraft.api;

import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.nature.NatureProfile;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class HerbNatureAPI {
   private HerbNatureAPI() {
   }

   public static Optional<NatureProfile> getExplicitNature(Identifier herbId) {
      return HerbNatureRegistry.explicit(herbId);
   }

   public static NatureProfile getNatureOrFallback(Identifier herbId) {
      return HerbNatureRegistry.getOrFallback(herbId);
   }

   public static NatureProfile getNatureOrFallback(ItemStack stack) {
      return HerbNatureRegistry.getOrFallback(stack);
   }

   public static boolean hasNature(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }
      Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      return id != null && HerbNatureRegistry.explicit(id).isPresent();
   }

   public static MutableComponent natureLine(NatureProfile profile) {
      return Component.translatable("nature.herbcraft.line", temperature(profile), flavors(profile));
   }

   public static MutableComponent traitHint(NatureProfile profile) {
      if (profile.traits().isEmpty()) {
         return Component.translatable("nature.herbcraft.hint.generic");
      }
      return Component.translatable("nature.herbcraft.hint." + profile.traits().getFirst());
   }

   public static MutableComponent temperature(NatureProfile profile) {
      return Component.translatable("nature.herbcraft.temperature." + profile.temperature());
   }

   public static MutableComponent tasteFlavors(NatureProfile profile) {
      MutableComponent result = Component.empty();
      boolean first = true;
      for (String flavor : profile.tasteFlavors()) {
         if (!first) {
            result.append(Component.literal("，"));
         }
         result.append(Component.translatable("nature.herbcraft.flavor." + flavor));
         first = false;
      }
      return result;
   }

   public static MutableComponent flavors(NatureProfile profile) {
      MutableComponent result = Component.empty();
      boolean first = true;
      for (String flavor : profile.flavors()) {
         if (!first) {
            result.append(Component.literal("，"));
         }
         result.append(Component.translatable("nature.herbcraft.flavor." + flavor));
         first = false;
      }
      return result;
   }
}
