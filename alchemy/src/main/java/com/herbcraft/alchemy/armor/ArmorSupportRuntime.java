package com.herbcraft.alchemy.armor;

import com.herbcraft.alchemy.AlchemyExpansionEvents;
import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.potion.PotionBodyStateProjection;
import com.herbcraft.alchemy.potion.PotionBurdenComponent;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class ArmorSupportRuntime {
   private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

   private ArmorSupportRuntime() {
   }

   public static void register() {
      AlchemyExpansionEvents.POTION_BURDEN_BUFFER_DETAILED.register(ArmorSupportRuntime::bufferPotionBurden);
   }

   private static AlchemyExpansionEvents.PotionBurdenBufferResult bufferPotionBurden(ServerPlayer player, ItemStack potion, PotionBurdenComponent burden) {
      int activeBuffer = 0;
      int tailBuffer = 0;
      int tailDecayBonus = 0;
      float consequenceMultiplier = 1.0F;
      int longestEffect = 0;
      String effectId = "";
      String styleId = AdvancedPotionStackInitializer.processingStyleId(potion).orElse("");
      PotionBodyStateProjection body = potion.get(AlchemyPotionComponents.POTION_BODY_STATE);
      String medicinalProfile = body == null ? "" : body.medicinalProfile();
      for (EquipmentSlot slot : ARMOR_SLOTS) {
         ArmorSupportComponent component = player.getItemBySlot(slot).get(ArmorSupportComponents.ARMOR_SUPPORT);
         if (component == null) continue;
         ArmorSupportRules.Support support = ArmorSupportRules.support(component.supportId()).orElse(null);
         if (support == null) continue;
         ModeScale scale = modeScale(component.mode());
         boolean matched = matches(support, styleId, burden.profile(), medicinalProfile);
         activeBuffer += Math.round((support.activeBufferAmount() + (matched ? support.matchActiveBonus() : 0)) * scale.bufferScale());
         tailBuffer += Math.round((support.tailBufferAmount() + (matched ? support.matchTailBonus() : 0)) * scale.tailScale());
         if (matched && support.consequenceMitigationProfiles().contains(medicinalProfile)) {
            consequenceMultiplier = Math.min(consequenceMultiplier, Math.max(0.05F, support.consequenceDurationMultiplier()));
         }
         if (matched && support.tailDecayBonus() > 0) {
            tailDecayBonus += Math.round(support.tailDecayBonus() * scale.tailScale());
         }
         if (support.effectDurationTicks() > longestEffect) {
            longestEffect = support.effectDurationTicks();
            effectId = support.effect();
         }
      }
      if ((activeBuffer > 0 || tailBuffer > 0) && !effectId.isBlank()) {
         final String id = effectId;
         final int duration = Math.max(20, longestEffect);
         BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).ifPresent(effect ->
            player.addEffect(new MobEffectInstance((Holder<MobEffect>)effect, duration, 0, false, true, true))
         );
      }
      return new AlchemyExpansionEvents.PotionBurdenBufferResult(activeBuffer, tailBuffer, consequenceMultiplier, tailDecayBonus);
   }

   private static boolean matches(ArmorSupportRules.Support support, String styleId, String burdenProfile, String medicinalProfile) {
      return support.matchingStyles().contains(styleId)
         || support.matchingBurdenProfiles().contains(burdenProfile)
         || support.matchingMedicinalProfiles().contains(medicinalProfile);
   }

   private static ModeScale modeScale(ArmorSupportMode mode) {
      return switch (mode) {
         case CLEAR -> new ModeScale(1.5F, 1.25F);
         case DEEP -> new ModeScale(1.25F, 1.5F);
         case ATTACHED -> new ModeScale(1.0F, 1.0F);
      };
   }

   private record ModeScale(float bufferScale, float tailScale) {
   }
}
