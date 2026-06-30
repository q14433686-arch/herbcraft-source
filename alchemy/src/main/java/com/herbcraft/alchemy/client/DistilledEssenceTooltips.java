package com.herbcraft.alchemy.client;

import com.herbcraft.alchemy.distillation.DistillationComponents;
import com.herbcraft.alchemy.distillation.DistillationRules;
import com.herbcraft.alchemy.distillation.DistilledEssenceComponent;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class DistilledEssenceTooltips {
   private DistilledEssenceTooltips() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register(DistilledEssenceTooltips::appendTooltip);
   }

   private static void appendTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      DistilledEssenceComponent component = stack.get(DistillationComponents.DISTILLED_ESSENCE);
      if (component == null) {
         return;
      }
      int state = AlchemyKnowledgeClientSupport.distilledEssenceState(stack);
      DistillationRules.orientation(component.orientation()).ifPresent(orientation -> {
         lines.add(Component.translatable("tooltip.herbcraft.distilled_essence.orientation", Component.translatable(orientation.langKey())).withStyle(ChatFormatting.AQUA));
         if (state >= 4 && !orientation.descriptionKey().isBlank()) {
            lines.add(Component.translatable(orientation.descriptionKey()).withStyle(ChatFormatting.GRAY));
         }
      });
      AlchemyRegistries.essenceInfoById(component.sourceEssence()).ifPresent(info ->
         lines.add(Component.translatable("tooltip.herbcraft.distilled_essence.source", Component.translatable(info.item().getDescriptionId())).withStyle(ChatFormatting.DARK_GRAY))
      );
   }
}
