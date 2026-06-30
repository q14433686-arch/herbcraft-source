package com.herbcraft.compat.jade;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin for Herbcraft.
 *
 * Layout (verified against Jade 26.1.0+fabric sources):
 * - register(): server data providers only. These must NOT implement
 *   IComponentProvider (checkDataProvider throws since MC 1.21.6).
 * - registerClient(): display-only component providers. Jade's
 *   tryAddConfig() auto-creates a boolean config per provider UID, so no
 *   manual addConfig is needed (and calling it with the same UID would
 *   throw "Duplicate config key").
 */
@WailaPlugin
public final class HerbcraftJadePlugin implements IWailaPlugin {
   public static final Identifier HERB_NATURE = Identifier.fromNamespaceAndPath("herbcraft", "herb_nature");
   public static final Identifier HERB_NATURE_ITEM_ENTITY = Identifier.fromNamespaceAndPath("herbcraft", "herb_nature_item_entity");

   @Override
   public void register(IWailaCommonRegistration registration) {
      registration.registerBlockDataProvider(HerbNatureServerDataProvider.INSTANCE, Block.class);
      registration.registerEntityDataProvider(HerbNatureEntityServerDataProvider.INSTANCE, ItemEntity.class); // Single dropped-item Herbcraft source; ItemStack tooltip skips when no screen is open.
   }

   @Override
   public void registerClient(IWailaClientRegistration registration) {
      registration.registerBlockComponent(HerbNatureProvider.INSTANCE, Block.class);
      registration.registerEntityComponent(HerbNatureEntityProvider.INSTANCE, ItemEntity.class); // Single dropped-item Herbcraft source; avoids duplicate ItemStack tooltip path.
   }
}
