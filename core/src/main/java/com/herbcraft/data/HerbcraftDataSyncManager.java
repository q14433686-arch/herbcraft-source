package com.herbcraft.data;

import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.nutrition.NutritionProfile;
import com.herbcraft.nutrition.NutritionRegistry;
import com.herbcraft.knowledge.IngredientsChapter;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class HerbcraftDataSyncManager {
   private static boolean dirty = true;

   private HerbcraftDataSyncManager() {
   }

   public static void register() {
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
      ServerTickEvents.END_SERVER_TICK.register(HerbcraftDataSyncManager::syncIfDirty);
   }

   public static void markDirty() {
      dirty = true;
   }

   public static void sync(ServerPlayer player) {
      ServerPlayNetworking.send(player, HerbcraftDataSyncPayload.build());
   }

   private static void syncIfDirty(MinecraftServer server) {
      if (!dirty) {
         return;
      }
      dirty = false;
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         sync(player);
      }
   }

   public static void applyClientPayload(HerbcraftDataSyncPayload payload) {
      Map<Identifier, NutritionProfile> nutrition = new LinkedHashMap<>();
      payload.nutritionProfiles().forEach((itemId, values) -> {
         Identifier id = Identifier.tryParse(itemId);
         if (id != null) {
            nutrition.put(id, new NutritionProfile(Map.copyOf(values)));
         }
      });
      NutritionRegistry.replaceWithSyncedData(nutrition);

      Map<Identifier, NatureProfile> natures = new LinkedHashMap<>();
      payload.natureProfiles().forEach((itemId, profile) -> {
         Identifier id = Identifier.tryParse(itemId);
         if (id != null) {
            natures.put(id, profile.toProfile());
         }
      });
      HerbNatureRegistry.replaceWithSyncedData(natures);

      Map<Identifier, String> ingredients = new LinkedHashMap<>();
      payload.ingredientEntryIds().forEach((itemId, entryId) -> {
         Identifier id = Identifier.tryParse(itemId);
         if (id != null) {
            ingredients.put(id, entryId);
         }
      });
      IngredientsChapter.replaceClientIngredientEntries(ingredients);
   }
}
