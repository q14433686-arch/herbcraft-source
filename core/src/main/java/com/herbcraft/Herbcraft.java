package com.herbcraft;

import com.herbcraft.api.HerbDefinition;
import com.herbcraft.bootstrap.HerbcraftDataBootstrap;
import com.herbcraft.bootstrap.HerbcraftKnowledgeBootstrap;
import com.herbcraft.bootstrap.HerbcraftLifecycleBootstrap;
import com.herbcraft.bootstrap.HerbcraftNetworkingBootstrap;
import com.herbcraft.logic.EffectResolver;
import com.herbcraft.logic.ProjectileUseHandler;
import com.herbcraft.registry.HerbFoodRegistry;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Herbcraft implements ModInitializer {
   public static final String MOD_ID = "herbcraft";
   public static final Logger LOGGER = LoggerFactory.getLogger("herbcraft");
   public static final int SHORT_USE_THROW_THRESHOLD_TICKS = 6;

   public void onInitialize() {
      HerbcraftDataBootstrap.initialize();
      HerbcraftKnowledgeBootstrap.initialize();
      HerbcraftNetworkingBootstrap.initialize();
      HerbcraftLifecycleBootstrap.initialize();
      LOGGER.info("{} loaded: registered {} edible definitions and the Herbal Codex framework.", "herbcraft", getHerbDefinitions().size());
   }

   public static Map<Item, HerbDefinition> getHerbDefinitions() {
      return HerbFoodRegistry.getHerbDefinitions();
   }

   public static boolean isShortUseProjectile(ItemStack stack) {
      return ProjectileUseHandler.isShortUseProjectile(stack);
   }

   public static InteractionResult startShortUseProjectileEating(Level level, Player player, InteractionHand hand) {
      return ProjectileUseHandler.start(level, player, hand);
   }

   public static boolean releaseShortUseProjectile(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      return ProjectileUseHandler.release(stack, level, entity, timeLeft);
   }

   public static void applyAfterConsume(ItemStack stack, Level level, LivingEntity entity) {
      EffectResolver.applyAfterConsume(stack, level, entity);
   }
}
