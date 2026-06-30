package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.knowledge.AlchemyKnowledgeSupport;
import com.herbcraft.alchemy.potion.PotionBurdenRuntime;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownSplashPotion.class)
public abstract class ThrownSplashPotionKnowledgeMixin {
   @Inject(method = "onHitAsPotion", at = @At("HEAD"))
   private void herbcraftAlchemy$markSplashPotionTasted(ServerLevel level, ItemStack stack, HitResult hitResult, CallbackInfo ci) {
      if (AlchemyKnowledgeSupport.potionBaseId(stack).isEmpty()) {
         return;
      }
      ThrownSplashPotion self = (ThrownSplashPotion)(Object)this;
      AABB movedBox = self.getBoundingBox().move(hitResult.getLocation().subtract(self.position()));
      AABB affectedBox = movedBox.inflate(4.0D, 2.0D, 4.0D);
      List<ServerPlayer> players = self.level().getEntitiesOfClass(ServerPlayer.class, affectedBox);
      float margin = ProjectileUtil.computeMargin(self);
      for (ServerPlayer player : players) {
         if (!player.isAffectedByPotions()) {
            continue;
         }
         double distance = movedBox.distanceToSqr(player.getBoundingBox().inflate((double)margin));
         if (distance < 16.0D) {
            AlchemyKnowledgeSupport.markPotionTasted(player, stack);
            PotionBurdenRuntime.onPotionConsumed(player, stack);
         }
      }
   }
}
