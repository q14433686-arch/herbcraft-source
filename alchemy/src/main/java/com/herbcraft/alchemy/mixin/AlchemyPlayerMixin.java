package com.herbcraft.alchemy.mixin;

import com.herbcraft.alchemy.potion.AlchemyPlayerData;
import com.herbcraft.alchemy.potion.PlayerPotionBurdenState;
import com.herbcraft.alchemy.potion.PotionBurdenConsequenceRuntime;
import com.herbcraft.alchemy.potion.PotionProcessingRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class AlchemyPlayerMixin implements AlchemyPlayerData {
   @Unique
   private final PlayerPotionBurdenState herbcraftAlchemy$potionBurdenState = new PlayerPotionBurdenState();

   @Override
   public PlayerPotionBurdenState herbcraftAlchemy$potionBurdenState() {
      return this.herbcraftAlchemy$potionBurdenState;
   }

   @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
   private void herbcraftAlchemy$writePotionBurden(ValueOutput output, CallbackInfo ci) {
      ValueOutput burdenOut = output.child("HerbcraftAlchemyPotionBurden");
      burdenOut.putInt("Burden", this.herbcraftAlchemy$potionBurdenState.burden());
      burdenOut.putInt("TailBurden", this.herbcraftAlchemy$potionBurdenState.tailBurden());
      burdenOut.putLong("LastDecayGameTime", this.herbcraftAlchemy$potionBurdenState.lastDecayGameTime());
      burdenOut.putString("LastStage", this.herbcraftAlchemy$potionBurdenState.lastStageId());
      burdenOut.putString("DominantProfile", this.herbcraftAlchemy$potionBurdenState.dominantProfile());
      burdenOut.putString("LastBurdenProfile", this.herbcraftAlchemy$potionBurdenState.lastBurdenProfile());
      burdenOut.putString("LastThermalTendency", this.herbcraftAlchemy$potionBurdenState.lastThermalTendency());
      burdenOut.putString("LastQiTendency", this.herbcraftAlchemy$potionBurdenState.lastQiTendency());
      burdenOut.putFloat("ConsequenceDurationMultiplier", this.herbcraftAlchemy$potionBurdenState.consequenceDurationMultiplier());
      burdenOut.putInt("TailDecayBonus", this.herbcraftAlchemy$potionBurdenState.tailDecayBonus());
   }

   @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
   private void herbcraftAlchemy$readPotionBurden(ValueInput input, CallbackInfo ci) {
      input.child("HerbcraftAlchemyPotionBurden").ifPresent(burdenIn ->
         this.herbcraftAlchemy$potionBurdenState.setDirect(
            burdenIn.getIntOr("Burden", 0),
            burdenIn.getIntOr("TailBurden", 0),
            burdenIn.getLongOr("LastDecayGameTime", 0L),
            burdenIn.getStringOr("LastStage", "steady"),
            burdenIn.getStringOr("DominantProfile", "steady"),
            burdenIn.getStringOr("LastBurdenProfile", "none"),
            burdenIn.getStringOr("LastThermalTendency", "neutral"),
            burdenIn.getStringOr("LastQiTendency", "balanced"),
            burdenIn.getFloatOr("ConsequenceDurationMultiplier", 1.0F),
            burdenIn.getIntOr("TailDecayBonus", 0),
            PotionProcessingRules.burdenRuntime()
         )
      );
   }

   @Inject(method = "die", at = @At("TAIL"))
   private void herbcraftAlchemy$clearPotionBurdenOnDeath(DamageSource damageSource, CallbackInfo ci) {
      this.herbcraftAlchemy$potionBurdenState.clear();
      if ((Object)this instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
         PotionBurdenConsequenceRuntime.clear(serverPlayer);
      }
   }
}
