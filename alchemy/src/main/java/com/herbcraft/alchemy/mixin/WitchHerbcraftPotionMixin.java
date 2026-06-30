package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.HerbcraftAlchemyConfig;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Witch.class)
public abstract class WitchHerbcraftPotionMixin {
   @Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
   private void herbcraft_alchemy$throwSafeHerbcraftPotion(LivingEntity target, float pullProgress, CallbackInfo ci) {
      Witch witch = (Witch)(Object)this;
      if (!HerbcraftAlchemyConfig.enableWitchHerbcraftPotions() || witch.isDrinkingPotion() || target instanceof Raider) return;
      if (witch.getRandom().nextFloat() >= HerbcraftAlchemyConfig.witchHerbcraftPotionChance()) return;
      if (!(witch.level() instanceof ServerLevel serverLevel)) return;
      Vec3 velocity = target.getDeltaMovement();
      double dx = target.getX() + velocity.x - witch.getX();
      double dy = target.getEyeY() - 1.100000023841858D - witch.getY();
      double dz = target.getZ() + velocity.z - witch.getZ();
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      Projectile.spawnProjectileUsingShoot((level, owner, stack) -> new ThrownSplashPotion(level, owner, stack), serverLevel, PotionContents.createItemStack(Items.SPLASH_POTION, AlchemyRegistries.pickWitchThrowPotion(witch)), witch, dx, dy + horizontal * 0.2D, dz, horizontal <= 2.0D ? 0.45F : 0.75F, 8.0F);
      if (!witch.isSilent()) witch.level().playSound(null, witch.getX(), witch.getY(), witch.getZ(), SoundEvents.WITCH_THROW, witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
      ci.cancel();
   }
}
