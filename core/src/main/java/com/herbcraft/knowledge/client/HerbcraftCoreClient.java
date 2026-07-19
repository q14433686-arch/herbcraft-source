package com.herbcraft.knowledge.client;

import com.herbcraft.data.HerbcraftDataSyncManager;
import com.herbcraft.data.HerbcraftDataSyncPayload;
import com.herbcraft.knowledge.CodexSnapshotPayload;
import com.herbcraft.knowledge.KnowledgeSyncPayload;
import com.herbcraft.knowledge.OpenCodexPayload;
import com.herbcraft.nutrition.NutritionSyncPayload;
import com.herbcraft.nutrition.client.NutritionClientCache;
import com.herbcraft.nutrition.client.NutritionItemTooltips;
import com.herbcraft.nutrition.client.NutritionPanelScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.herbcraft.nutrition.client.NutritionTheme;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;

public final class HerbcraftCoreClient implements ClientModInitializer {
   private static final KeyMapping.Category HERBCRAFT_CATEGORY = KeyMapping.Category.register(
      Identifier.fromNamespaceAndPath("herbcraft", "keybinds")
   );
   private static final KeyMapping OPEN_NUTRITION = new KeyMapping(
      "key.herbcraft.open_nutrition_panel",
      InputConstants.Type.KEYSYM,
      GLFW.GLFW_KEY_N,
      HERBCRAFT_CATEGORY
   );

   public void onInitializeClient() {
      CodexClientTooltips.register();
      HerbNatureItemTooltips.register();
      NutritionItemTooltips.register();
      ClientPlayNetworking.registerGlobalReceiver(CodexSnapshotPayload.TYPE, (payload, context) -> {
         CodexClientState.setLatestSnapshot(payload);
         if (context.client().gui.screen() instanceof CodexScreen codexScreen) {
            codexScreen.replaceSnapshot(payload);
         }
      });
      ClientPlayNetworking.registerGlobalReceiver(OpenCodexPayload.TYPE, (payload, context) -> {
         if (!payload.requestOpen()) {
            return;
         }
         CodexSnapshotPayload snapshot = CodexClientState.latestSnapshot();
         if (snapshot != null) {
            context.client().gui.setScreen(new CodexScreen(snapshot));
         }
      });
      ClientPlayNetworking.registerGlobalReceiver(KnowledgeSyncPayload.TYPE, (payload, context) -> KnowledgeClientCache.update(payload.states()));
      ClientPlayNetworking.registerGlobalReceiver(HerbcraftDataSyncPayload.TYPE, (payload, context) -> HerbcraftDataSyncManager.applyClientPayload(payload));
      ClientPlayNetworking.registerGlobalReceiver(NutritionSyncPayload.TYPE, (payload, context) ->
         NutritionClientCache.update(payload.values(), payload.statusOrdinal()));
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         CodexScreen.clearClientSessionState();
         CodexClientState.clear();
      });
      KeyMappingHelper.registerKeyMapping(OPEN_NUTRITION);
      // Reload UI themes when resource packs change
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
         new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
               return Identifier.fromNamespaceAndPath("herbcraft", "ui_theme_reload");
            }
            @Override
            public void onResourceManagerReload(ResourceManager manager) {
               CodexTheme.reload();
               NutritionTheme.reload();
            }
         }
      );
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         while (OPEN_NUTRITION.consumeClick()) {
            if (client.gui.screen() == null) {
               client.gui.setScreen(new NutritionPanelScreen());
            } else if (client.gui.screen() instanceof NutritionPanelScreen) {
               client.gui.setScreen(null);
            }
         }
      });
   }
}
