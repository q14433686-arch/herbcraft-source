package com.herbcraft.logic;

import com.herbcraft.HerbcraftPlayerData;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class EffectImmunityManager {
   private static final ThreadLocal<Integer> BYPASS_DEPTH = ThreadLocal.withInitial(() -> 0);

   private EffectImmunityManager() {
   }

   /**
    * Runs an internal Herbcraft effect application without consulting dietary effect immunities.
    *
    * <p>Dietary immunities model short-term medical prevention against outside status effects
    * such as poison, weakness, nausea or mining fatigue. Herbcraft's own nutrition system uses
    * some of the same vanilla MobEffects as physiological feedback for malnutrition/imbalance;
    * those effects must not be prevented by a medicinal dish that happens to grant immunity to
    * the same vanilla effect id.</p>
    */
   public static void runBypassingImmunity(Runnable action) {
      int depth = BYPASS_DEPTH.get();
      BYPASS_DEPTH.set(depth + 1);
      try {
         action.run();
      } finally {
         if (depth == 0) {
            BYPASS_DEPTH.remove();
         } else {
            BYPASS_DEPTH.set(depth);
         }
      }
   }

   public static boolean isBypassingImmunity() {
      return BYPASS_DEPTH.get() > 0;
   }

   public static void grant(ServerPlayer player, Identifier effectId, int durationTicks) {
      if (durationTicks <= 0) {
         return;
      }
      long now = player.level().getGameTime();
      long expiresAt = now + durationTicks;
      Map<String, Long> immunities = ((HerbcraftPlayerData)player).herbcraft$effectImmunities();
      cleanup(immunities, now);
      immunities.merge(effectId.toString(), expiresAt, Math::max);
   }

   public static boolean isImmune(ServerPlayer player, Identifier effectId) {
      long now = player.level().getGameTime();
      Map<String, Long> immunities = ((HerbcraftPlayerData)player).herbcraft$effectImmunities();
      Long expiresAt = immunities.get(effectId.toString());
      if (expiresAt == null) {
         return false;
      }
      if (expiresAt <= now) {
         immunities.remove(effectId.toString());
         return false;
      }
      return true;
   }

   public static void cleanup(Map<String, Long> immunities, long now) {
      Iterator<Map.Entry<String, Long>> iterator = immunities.entrySet().iterator();
      while (iterator.hasNext()) {
         if (iterator.next().getValue() <= now) {
            iterator.remove();
         }
      }
   }
}
