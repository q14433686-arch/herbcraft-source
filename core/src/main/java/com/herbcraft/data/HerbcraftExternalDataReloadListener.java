package com.herbcraft.data;

import com.herbcraft.Herbcraft;
import com.herbcraft.api.HerbcraftEvents;
import com.herbcraft.knowledge.CodexLoot;
import com.herbcraft.knowledge.HerbalChapter;
import com.herbcraft.knowledge.IngredientsChapter;
import com.herbcraft.knowledge.KnowledgeRumorRegistry;
import com.herbcraft.logic.SpecialCounters;
import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.nutrition.NutritionRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class HerbcraftExternalDataReloadListener implements SimpleSynchronousResourceReloadListener {
   public static final Identifier ID = Identifier.fromNamespaceAndPath(Herbcraft.MOD_ID, "external_data");

   @Override
   public Identifier getFabricId() {
      return ID;
   }

   @Override
   public void onResourceManagerReload(ResourceManager manager) {
      // Keep bundled baseline as the floor, then merge additive external paths on top.
      HerbNatureRegistry.loadFromBundledJson();
      HerbNatureRegistry.applyExternalData(manager);

      NutritionRegistry.loadFromBundledJson();
      NutritionRegistry.applyExternalData(manager);

      IngredientsChapter.applyExternalData(manager);
      HerbalChapter.applyFoodNatureEntries();

      SpecialCounters.loadFromBundledJson();
      SpecialCounters.applyExternalData(manager);

      KnowledgeRumorRegistry.loadFromBundledJson();
      KnowledgeRumorRegistry.applyExternalData(manager);

      CodexLoot.applyExternalData(manager);

      HerbcraftDataSyncManager.markDirty();
      HerbcraftEvents.EXTERNAL_DATA_RELOADED.invoker().onExternalDataReloaded(
         HerbNatureRegistry.profiles().size(),
         NutritionRegistry.profiles().size(),
         IngredientsChapter.itemEntryIds().size()
      );
      Herbcraft.LOGGER.info("Reloaded Herbcraft external data merge layer");
   }
}
