package com.herbcraft.compat.jade;

import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.api.HerbUsageAPI;
import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.api.HerbcraftDataAPI;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.knowledge.HerbalChapter;
import com.herbcraft.nature.NatureProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ITooltip;

final class HerbJadeData {
   static final String IS_HERB = "herbcraft:is_herb";
   static final String FOOD_NATURE = "herbcraft:is_food_nature";
   static final String KNOWLEDGE_STATE = "herbcraft:knowledge_state";
   static final String TEMPERATURE = "herbcraft:nature_temperature";
   static final String FLAVORS = "herbcraft:nature_flavors";
   static final String USAGES = "herbcraft:usages";

   private HerbJadeData() {
   }

   static boolean isHerb(Item item) {
      if (item == null) {
         return false;
      }
      if (HerbcraftAPI.getHerbRegistry().containsKey(item)) {
         return true;
      }
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return id != null && HerbcraftDataAPI.hasFoodNatureData(id);
   }

   static void write(CompoundTag data, ServerPlayer player, Item item) {
      if (!isHerb(item)) {
         return;
      }
      data.putBoolean(IS_HERB, true);
      boolean foodNature = !HerbcraftAPI.getHerbRegistry().containsKey(item);
      data.putBoolean(FOOD_NATURE, foodNature);
      data.putInt(KNOWLEDGE_STATE, knowledgeState(player, item));
      NatureProfile nature = HerbNatureAPI.getNatureOrFallback(new ItemStack(item));
      data.putString(TEMPERATURE, nature.temperature());
      data.putString(FLAVORS, String.join(",", nature.flavors()));
      java.util.List<String> usages = HerbUsageAPI.usageKeys(item);
      if (!usages.isEmpty()) {
         data.putString(USAGES, String.join(",", usages));
      }
   }

   static void appendTooltip(ITooltip tooltip, CompoundTag data, boolean localHerb) {
      boolean serverHerb = data.getBooleanOr(IS_HERB, false);
      if (!serverHerb && !localHerb) {
         return;
      }
      tooltip.add(Component.translatable("jade.herbcraft.title").withStyle(ChatFormatting.GREEN));
      int state = data.getIntOr(KNOWLEDGE_STATE, 0);
      if (state <= 0) {
         tooltip.add(Component.translatable("jade.herbcraft.unknown").withStyle(ChatFormatting.GRAY));
         return;
      }
      tooltip.add(Component.translatable("knowledge.herbcraft.state." + stateKey(state)).withStyle(state >= 4 ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
      if (state == 1) {
         tooltip.add(Component.translatable("knowledge.herbcraft.encountered.description").withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.translatable("knowledge.herbcraft.line.nature_unknown").withStyle(ChatFormatting.DARK_GRAY));
         tooltip.add(Component.translatable("knowledge.herbcraft.line.traits_unknown").withStyle(ChatFormatting.DARK_GRAY));
         return;
      }
      if (state == 2) {
         tooltip.add(Component.translatable("book.herbcraft.entry.heard").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         tooltip.add(Component.translatable("knowledge.herbcraft.heard.not_tasted").withStyle(ChatFormatting.DARK_GRAY));
         return;
      }
      NatureProfile nature = new NatureProfile(data.getStringOr(TEMPERATURE, "neutral"), java.util.List.of(data.getStringOr(FLAVORS, "bland").split(",")), java.util.List.of());
      if (state == 3) {
         tooltip.add(HerbNatureAPI.natureLine(nature).append(Component.translatable("knowledge.herbcraft.claim.verified_by_taste").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.AQUA));
         tooltip.add(Component.translatable("knowledge.herbcraft.tasted.precise_unknown").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
         return;
      }
      if (state >= 4) {
         tooltip.add(HerbNatureAPI.natureLine(nature).withStyle(ChatFormatting.AQUA));
         boolean foodNature = data.getBooleanOr(FOOD_NATURE, false);
         if (foodNature) {
            tooltip.add(Component.translatable("knowledge.herbcraft.food_nature.codex_hint").withStyle(ChatFormatting.DARK_GRAY));
         } else {
            String usages = data.getStringOr(USAGES, "");
            if (!usages.isEmpty()) {
               tooltip.add(HerbUsageAPI.usageLine(java.util.List.of(usages.split(","))).withStyle(ChatFormatting.DARK_GRAY));
            } else {
               tooltip.add(Component.translatable("jade.herbcraft.usage.basic").withStyle(ChatFormatting.DARK_GRAY));
            }
         }
      }
   }

   private static String stateKey(int state) {
      return switch (state) {
         case 1 -> "encountered";
         case 2 -> "heard";
         case 3 -> "tasted";
         case 4 -> "mastered";
         default -> "unknown";
      };
   }

   private static int knowledgeState(ServerPlayer player, Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      if (id == null) {
         return 0;
      }
      String entryId = HerbcraftAPI.getHerbRegistry().containsKey(item) ? id.getPath() : HerbalChapter.foodNatureEntryId(id);
      KnowledgeAPI.Entry entry = KnowledgeAPI.chapter("herbal").map(chapter -> chapter.entry(entryId)).orElse(null);
      if (entry == null) {
         return 0;
      }
      return KnowledgeAPI.displayState(player, "herbal", entry).ordinal();
   }
}
