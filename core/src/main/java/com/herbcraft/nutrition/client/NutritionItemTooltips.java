package com.herbcraft.nutrition.client;

import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.knowledge.IngredientsChapter;
import com.herbcraft.knowledge.client.KnowledgeClientCache;
import com.herbcraft.nutrition.NutritionProfile;
import com.herbcraft.nutrition.NutritionRegistry;
import java.util.List;
import java.util.StringJoiner;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

/**
 * Client-side tooltip that shows per-item nutrition values.
 * For Herbcraft herbs: gated behind knowledge state (unknown→fuzzy→precise).
 * For Ingredient Codex foods: gated behind eating, including synced external ingredient entries.
 */
public final class NutritionItemTooltips {
   private NutritionItemTooltips() {}

   public static void register() {
      ItemTooltipCallback.EVENT.register(NutritionItemTooltips::appendTooltip);
   }

   private static void appendTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      // Skip when no screen is open (consistent with HerbNatureItemTooltips)
      if (net.minecraft.client.Minecraft.getInstance().screen == null) return;
      NutritionRegistry.get(stack).ifPresent(profile -> {
         if (profile.isEmpty()) return;

         // Determine if this is a Herbcraft herb (knowledge-gated) or other food
         boolean isHerb = HerbcraftAPI.getDefinition(stack.getItem()).isPresent();

         if (isHerb) {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id == null) return;
            int state = KnowledgeClientCache.state("herbal", id.getPath());

            // UNKNOWN (0) or ENCOUNTERED (1) → no nutrition info
            if (state <= 1) return;

            // HEARD (2) → fuzzy with "rumored" label
            if (state == 2) {
               appendFuzzy(lines, profile, "nutrition.herbcraft.tooltip.rumor");
               return;
            }

            // TASTED (3) → fuzzy without "rumored" label
            if (state == 3) {
               appendFuzzy(lines, profile, "nutrition.herbcraft.tooltip.felt");
               return;
            }

            // MASTERED (4+) → precise
            appendPrecise(lines, profile);
         } else {
            // Non-herb foods: show fuzzy nutrition only after eating (knowledge earned)
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (itemId == null) return;
            // Cuisine/Herbcraft module items: always show fuzzy (cuisine chapter has its own gating)
            if (itemId.getNamespace().equals("herbcraft_cuisine") || itemId.getNamespace().equals("herbcraft")) {
               appendFuzzy(lines, profile, "nutrition.herbcraft.tooltip.felt");
               return;
            }
            // Ingredient Codex entries, including external namespace-aware entries, are gated behind eating.
            if (!IngredientsChapter.hasEntry(itemId)) return;
            int ingredientState = KnowledgeClientCache.state("ingredients", IngredientsChapter.entryId(itemId));
            if (ingredientState >= 3) { // TASTED(3) or MASTERED(4) — has eaten
               appendFuzzy(lines, profile, "nutrition.herbcraft.tooltip.felt");
            }
            // else: not eaten yet, show nothing
         }
      });
   }

   private static void appendPrecise(List<Component> lines, NutritionProfile profile) {
      lines.add(Component.translatable("nutrition.herbcraft.header").withStyle(ChatFormatting.GRAY));
      for (String dim : NutritionProfile.DIMENSIONS) {
         int val = profile.get(dim);
         if (val > 0) {
            lines.add(Component.literal("  ")
               .append(Component.translatable("nutrition.herbcraft.dimension." + dim).withStyle(ChatFormatting.GRAY))
               .append(Component.literal(" ").withStyle(ChatFormatting.GRAY))
               .append(Component.literal("+" + val).withStyle(ChatFormatting.GREEN)));
         }
      }
   }

   private static void appendFuzzy(List<Component> lines, NutritionProfile profile, String headerKey) {
      // Build fuzzy description: "微含菜蔬、富含果养"
      MutableComponent desc = Component.empty();
      boolean first = true;
      for (String dim : NutritionProfile.DIMENSIONS) {
         int val = profile.get(dim);
         if (val > 0) {
            String level = fuzzyLevel(val);
            if (!first) desc.append(Component.literal("、"));
            desc.append(Component.translatable("nutrition.herbcraft.level." + level))
                .append(Component.translatable("nutrition.herbcraft.dimension." + dim));
            first = false;
         }
      }
      if (!first) {
         lines.add(Component.translatable(headerKey).withStyle(ChatFormatting.GRAY)
            .append(desc.withStyle(ChatFormatting.DARK_AQUA)));
      }
   }

   private static String fuzzyLevel(int value) {
      if (value <= 30) return "trace";    // 微含
      if (value <= 80) return "rich";     // 富含
      return "heavy";                      // 极重
   }
}
