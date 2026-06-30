package com.herbcraft.compat.jade;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Client-side display component for herb item entities (dropped items).
 * Display-only; server data lives in {@link HerbNatureEntityServerDataProvider}
 * (Jade 1.21.6+ forbids combining the two roles in one class on the client).
 */
public enum HerbNatureEntityProvider implements IEntityComponentProvider {
   INSTANCE;

   @Override
   public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
      Entity entity = accessor.getEntity();
      ItemStack stack = entity instanceof ItemEntity itemEntity ? itemEntity.getItem() : ItemStack.EMPTY;
      HerbJadeData.appendTooltip(tooltip, accessor.getServerData(), !stack.isEmpty() && HerbJadeData.isHerb(stack.getItem()));
   }

   @Override
   public Identifier getUid() {
      return HerbcraftJadePlugin.HERB_NATURE_ITEM_ENTITY;
   }
}
