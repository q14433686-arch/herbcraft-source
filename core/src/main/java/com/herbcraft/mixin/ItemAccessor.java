package com.herbcraft.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Item.class)
public interface ItemAccessor {
   @Invoker("getPlayerPOVHitResult")
   static BlockHitResult herbcraft$getPlayerPOVHitResult(Level level, Player player, Fluid fluid) {
      throw new AssertionError();
   }
}
