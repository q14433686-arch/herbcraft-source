package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.knowledge.AlchemyKnowledgeSupport;
import com.herbcraft.alchemy.potion.PotionBurdenRuntime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AreaEffectCloud.class)
public abstract class AreaEffectCloudKnowledgeMixin {
   @Shadow private PotionContents potionContents;
   @Unique private final Set<UUID> herbcraftAlchemy$processedPlayers = new HashSet<>();
   @Shadow public abstract boolean isWaiting();
   @Shadow public abstract float getRadius();

   @Inject(method = "serverTick", at = @At("HEAD"))
   private void herbcraftAlchemy$markLingeringPotionTasted(ServerLevel level, CallbackInfo ci) {
      AreaEffectCloud self = (AreaEffectCloud)(Object)this;
      if (this.isWaiting() || self.tickCount % 5 != 0 || AlchemyKnowledgeSupport.potionBaseId(this.potionContents).isEmpty()) {
         return;
      }
      double radius = (double)this.getRadius();
      double radiusSqr = radius * radius;
      List<ServerPlayer> players = self.level().getEntitiesOfClass(ServerPlayer.class, self.getBoundingBox().inflate(radius, 0.5D, radius));
      for (ServerPlayer player : players) {
         if (!player.isAffectedByPotions()) {
            continue;
         }
         double dx = player.getX() - self.getX();
         double dz = player.getZ() - self.getZ();
         if (dx * dx + dz * dz <= radiusSqr && this.herbcraftAlchemy$processedPlayers.add(player.getUUID())) {
            AlchemyKnowledgeSupport.markPotionTasted(player, this.potionContents);
            PotionBurdenRuntime.onPotionConsumed(player, cloudPotionStack());
         }
      }
   }

   @Unique
   private net.minecraft.world.item.ItemStack cloudPotionStack() {
      net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LINGERING_POTION);
      stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, this.potionContents);
      return stack;
   }
}
