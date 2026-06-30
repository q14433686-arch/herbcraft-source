package com.herbcraft.alchemy.potion;

import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.pharmacology.PlayerPharmacologyState;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Applies P3 processed-potion body-state projection onto Core pharmacology state on consumption. */
public final class PotionBodyStateConsumptionRuntime {
   private PotionBodyStateConsumptionRuntime() {
   }

   public static void onPotionConsumed(ServerPlayer player, ItemStack stack) {
      Optional<PotionPharmacologyProjection> source = PotionPharmacologyProjectionRuntime.projection(stack);
      Optional<PotionBodyStateProjection> body = PotionBodyStateProjectionRuntime.projection(stack);
      if (source.isEmpty() || body.isEmpty()) {
         return;
      }
      PlayerPharmacologyState state = ((HerbcraftPlayerData)player).herbcraft$pharmacologyState();
      long gameTime = player.level().getGameTime();
      long nowMillis = System.currentTimeMillis();
      NatureProfile profile = new NatureProfile(source.get().temperature(), source.get().flavors(), source.get().tasteFlavors(), source.get().traits());
      state.cleanup(gameTime, nowMillis, com.herbcraft.pharmacology.PharmacologyRules.current().windowTicks);
      state.addUse(source.get().sourceEssence(), profile, gameTime, nowMillis);
      state.setQiBalance(state.qiBalance() + body.get().thermalValue() + body.get().qiValue());
   }
}
