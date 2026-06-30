package com.herbcraft.cuisine.logic;

import com.herbcraft.cuisine.registry.CuisineItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public final class WaterBowlInteraction {
   private WaterBowlInteraction() {
   }

   public static void register() {
      UseBlockCallback.EVENT.register(WaterBowlInteraction::useOnBlock);
      UseItemCallback.EVENT.register(WaterBowlInteraction::useInAir);
   }

   private static InteractionResult useOnBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
      ItemStack held = player.getItemInHand(hand);
      if (held.is(Items.BOWL) && !player.isSpectator()) {
         BlockPos pos = hit.getBlockPos();
         BlockState state = level.getBlockState(pos);
         if (!state.is(Blocks.WATER_CAULDRON)) {
            return InteractionResult.PASS;
         } else {
            if (!level.isClientSide()) {
               LayeredCauldronBlock.lowerFillLevel(state, level, pos);
               fillBowl(player, hand, held);
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static InteractionResult useInAir(Player player, Level level, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      if (held.is(Items.BOWL) && !player.isSpectator()) {
         HitResult hit = player.pick(player.blockInteractionRange(), 0.0F, true);
         if (hit.getType() == Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            if (!level.getFluidState(pos).isSourceOfType(Fluids.WATER)) {
               return InteractionResult.PASS;
            } else {
               if (!level.isClientSide()) {
                  fillBowl(player, hand, held);
               }

               return InteractionResult.SUCCESS;
            }
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static void fillBowl(Player player, InteractionHand hand, ItemStack held) {
      player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
      ItemStack result = ItemUtils.createFilledResult(held, player, new ItemStack(CuisineItems.WATER_BOWL));
      player.setItemInHand(hand, result);
   }
}
