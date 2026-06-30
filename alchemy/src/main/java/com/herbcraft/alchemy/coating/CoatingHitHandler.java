package com.herbcraft.alchemy.coating;

import com.herbcraft.alchemy.registry.AlchemyRegistries;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class CoatingHitHandler {
   private CoatingHitHandler() {
   }

   public static void register() {
      ServerLivingEntityEvents.AFTER_DAMAGE.register(CoatingHitHandler::afterDamage);
   }

   private static void afterDamage(LivingEntity target, DamageSource source, float baseDamage, float damageTaken, boolean blocked) {
      if (!blocked && !target.level().isClientSide() && source.getEntity() instanceof LivingEntity attacker && source.getDirectEntity() == attacker) {
         ItemStack weapon = attacker.getMainHandItem();
         if (!weapon.isEmpty()) {
            WeaponCoating coating = (WeaponCoating)weapon.get(CoatingComponents.WEAPON_COATING);
            if (coating != null) {
               long now = attacker.level().getGameTime();
               if (coating.expiresAt() >= 0L && now > coating.expiresAt()) {
                  CoatingSystem.removeCoating(weapon);
               } else {
                  Optional<AlchemyRegistries.EssenceInfo> info = AlchemyRegistries.essenceInfoById(coating.essenceId());
                  if (info.isEmpty()) {
                     CoatingSystem.removeCoating(weapon);
                  } else {
                     List<AlchemyRegistries.EssenceEffect> effects = CoatingSystem.effectsFor(info.get(), coating);
                     if (effects.isEmpty()) {
                        CoatingSystem.removeCoating(weapon);
                     } else {
                        boolean selfBuff = coating.mode() == WeaponCoating.Mode.POSITIVE;
                        LivingEntity receiver = selfBuff ? attacker : target;

                        for (AlchemyRegistries.EssenceEffect effect : effects) {
                           receiver.addEffect(new MobEffectInstance(effect.effect(), CoatingSystem.hitDurationTicks(effect), effect.amplifier()), attacker);
                        }

                        if (coating.mode() != WeaponCoating.Mode.FULL) {
                           AlchemyRegistries.EssenceEffect rolled = CoatingSystem.rollRandomEffect(coating.mode(), attacker.getRandom());
                           receiver.addEffect(new MobEffectInstance(rolled.effect(), CoatingSystem.hitDurationTicks(rolled), rolled.amplifier()), attacker);
                        }

                        int remaining = coating.remainingUses() - 1;
                        if (remaining <= 0) {
                           CoatingSystem.removeCoating(weapon);
                        } else {
                           long expiresAt = coating.expiresAt() >= 0L ? coating.expiresAt() : now + CoatingSystem.expiryTicksFor(coating.mode());
                           weapon.set(CoatingComponents.WEAPON_COATING, new WeaponCoating(coating.essenceId(), coating.mode(), remaining, expiresAt, coating.processingStyle()));
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
