package com.herbcraft.compat.jade;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Client-side display component for herb blocks.
 *
 * IMPORTANT: since Minecraft 1.21.6, Jade forbids a provider that implements
 * both IComponentProvider and IServerDataProvider on the physical client
 * (WailaCommonRegistration.checkDataProvider throws). Server data lives in
 * {@link HerbNatureServerDataProvider}; this class must stay display-only.
 */
public enum HerbNatureProvider implements IBlockComponentProvider {
   INSTANCE;

   @Override
   public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
      Item item = accessor.getBlockState().getBlock().asItem();
      HerbJadeData.appendTooltip(tooltip, accessor.getServerData(), HerbJadeData.isHerb(item));
   }

   @Override
   public Identifier getUid() {
      return HerbcraftJadePlugin.HERB_NATURE;
   }
}
