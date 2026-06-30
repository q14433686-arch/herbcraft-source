package com.herbcraft.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Server-data half of the herb block provider. Deliberately does NOT
 * implement IComponentProvider (Jade 1.21.6+ restriction); the display
 * half is {@link HerbNatureProvider}.
 */
public enum HerbNatureServerDataProvider implements IServerDataProvider<BlockAccessor> {
   INSTANCE;

   @Override
   public void appendServerData(CompoundTag data, BlockAccessor accessor) {
      Item item = accessor.getBlockState().getBlock().asItem();
      if (accessor.getPlayer() instanceof ServerPlayer serverPlayer) {
         HerbJadeData.write(data, serverPlayer, item);
      }
   }

   @Override
   public boolean shouldRequestData(BlockAccessor accessor) {
      return HerbJadeData.isHerb(accessor.getBlockState().getBlock().asItem());
   }

   @Override
   public Identifier getUid() {
      return HerbcraftJadePlugin.HERB_NATURE;
   }
}
