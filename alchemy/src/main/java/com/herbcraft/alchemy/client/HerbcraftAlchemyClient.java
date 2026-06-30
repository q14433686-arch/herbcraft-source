package com.herbcraft.alchemy.client;

import net.fabricmc.api.ClientModInitializer;

public final class HerbcraftAlchemyClient implements ClientModInitializer {
   public void onInitializeClient() {
      CoatingTooltips.register();
      AdvancedPotionTooltips.register();
      DistilledEssenceTooltips.register();
      ArmorSupportTooltips.register();
   }
}
