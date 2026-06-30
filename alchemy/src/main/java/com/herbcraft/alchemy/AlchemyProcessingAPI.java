package com.herbcraft.alchemy;

import com.herbcraft.alchemy.potion.AlchemyPlayerData;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.PlayerPotionBurdenState;
import com.herbcraft.alchemy.potion.PotionBurdenComponent;
import com.herbcraft.alchemy.potion.PotionBodyStateProjection;
import com.herbcraft.alchemy.potion.PotionBodyStateProjectionRuntime;
import com.herbcraft.alchemy.potion.PotionPharmacologyProjection;
import com.herbcraft.alchemy.potion.PotionPharmacologyProjectionRuntime;
import com.herbcraft.alchemy.potion.PotionProcessingRules;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Read-only Alchemy processing queries for future distillation/armor/coating integrations. */
public final class AlchemyProcessingAPI {
   private AlchemyProcessingAPI() {
   }

   public static Optional<String> processingStyleId(ItemStack stack) {
      return AdvancedPotionStackInitializer.processingStyleId(stack);
   }

   public static Optional<PotionProcessingRules.Style> processingStyle(ItemStack stack) {
      return processingStyleId(stack).flatMap(PotionProcessingRules::style);
   }

   public static Optional<PotionBurdenComponent> potionBurden(ItemStack stack) {
      return Optional.ofNullable(stack.get(AlchemyPotionComponents.POTION_BURDEN));
   }

   public static Optional<PotionPharmacologyProjection> potionPharmacology(ItemStack stack) {
      return PotionPharmacologyProjectionRuntime.projection(stack);
   }

   public static Optional<PotionBodyStateProjection> potionBodyState(ItemStack stack) {
      return PotionBodyStateProjectionRuntime.projection(stack);
   }

   public static PlayerPotionBurdenState playerBurden(ServerPlayer player) {
      return ((AlchemyPlayerData)player).herbcraftAlchemy$potionBurdenState();
   }

   public static PotionProcessingRules.BurdenStage playerBurdenStage(ServerPlayer player) {
      return PotionProcessingRules.burdenStageFor(playerBurden(player).totalBurden());
   }
}
