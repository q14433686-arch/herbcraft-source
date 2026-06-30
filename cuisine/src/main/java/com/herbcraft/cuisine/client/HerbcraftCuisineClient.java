package com.herbcraft.cuisine.client;

import net.fabricmc.api.ClientModInitializer;

public final class HerbcraftCuisineClient implements ClientModInitializer {
   public void onInitializeClient() {
      CuisineTooltips.register();
   }
}
