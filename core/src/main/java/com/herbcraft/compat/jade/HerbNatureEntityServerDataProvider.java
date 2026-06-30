package com.herbcraft.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Server-data half for herb dropped items. Display half:
 * {@link HerbNatureEntityProvider}.
 */
public enum HerbNatureEntityServerDataProvider implements IServerDataProvider<EntityAccessor> {
   INSTANCE;

   @Override
   public void appendServerData(CompoundTag data, EntityAccessor accessor) {
      Entity entity = accessor.getEntity();
      if (entity instanceof ItemEntity itemEntity && accessor.getPlayer() instanceof ServerPlayer serverPlayer) {
         HerbJadeData.write(data, serverPlayer, itemEntity.getItem().getItem());
      }
   }

   @Override
   public boolean shouldRequestData(EntityAccessor accessor) {
      Entity entity = accessor.getEntity();
      return entity instanceof ItemEntity itemEntity && HerbJadeData.isHerb(itemEntity.getItem().getItem());
   }

   @Override
   public Identifier getUid() {
      return HerbcraftJadePlugin.HERB_NATURE_ITEM_ENTITY;
   }
}
