package com.herbcraft.advancement;

import com.herbcraft.api.HerbConsumeEvents;
import com.herbcraft.guide.GuideHintManager;
import com.herbcraft.knowledge.CodexItems;
import com.herbcraft.knowledge.KnowledgeInventoryScanner;
import com.herbcraft.registry.HerbEntry;
import com.herbcraft.registry.HerbFoodRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HerbcraftAdvancementHooks {
   private HerbcraftAdvancementHooks() {}
   public static void register() {
      HerbConsumeEvents.register(HerbcraftAdvancementHooks::onHerbConsumed);
      KnowledgeInventoryScanner.registerHandler((player, stack) -> {
         if (stack.is(CodexItems.HERBAL_CODEX)) HerbcraftAdvancements.grant(player, "root");
      });
      // feed_cow_grass ("它们本来就应该吃这些"): fires when a player offers an
      // animal one of the Herbcraft-extended foods and the animal actually
      // accepts that item as food. Vanilla handling continues untouched
      // because we always return PASS.
      UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
         if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && entity instanceof Animal animal) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty() && animal.isFood(stack) && isExpandedDiet(stack)) {
               HerbcraftAdvancements.grant(serverPlayer, "feed_cow_grass");
            }
         }
         return InteractionResult.PASS;
      });
   }
   private static boolean isExpandedDiet(ItemStack stack) {
      // Everything Herbcraft adds to animal diets is itself a registered herb,
      // except the camel's dead bush. Vanilla foods (wheat, seeds for chicken,
      // etc. that we did not add) are not herbs, so they do not trigger this.
      return HerbFoodRegistry.get(stack.getItem()) != null || stack.is(Items.DEAD_BUSH);
   }
   private static void onHerbConsumed(ServerPlayer player, Item item) {
      HerbcraftAdvancements.grant(player, "first_bite");
      GuideHintManager.show(player, "herbcraft:first_herb_bite");
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      if (id != null) {
         String path = id.getPath();
         if (path.contains("grass") || path.contains("fern")) HerbcraftAdvancements.grant(player, "eat_grass");
      }
      HerbEntry entry = HerbFoodRegistry.get(item);
      if (entry != null) {
         if (entry.effects.stream().anyMatch(effect -> !effect.positive()) || !entry.damageEvents.isEmpty()) HerbcraftAdvancements.grant(player, "bitter_medicine");
         if (!entry.removeEffects.isEmpty() || entry.clearPositiveEffects) HerbcraftAdvancements.grant(player, "counter_poison");
      }
   }
}
