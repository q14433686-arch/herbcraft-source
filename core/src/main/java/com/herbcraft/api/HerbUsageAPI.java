package com.herbcraft.api;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Usage-hint registry ("用途层").
 *
 * Core owns the registry; addons (alchemy/cuisine) push translation keys for
 * the items or item tags they can consume. Core never imports addon classes,
 * preserving module orthogonality. Registration happens during mod init on
 * both logical sides, so the data is identical on client and server and does
 * not need network sync.
 */
public final class HerbUsageAPI {
   private static final Map<Item, LinkedHashSet<String>> ITEM_USAGES = new LinkedHashMap<>();
   private static final Map<TagKey<Item>, LinkedHashSet<String>> TAG_USAGES = new LinkedHashMap<>();

   private HerbUsageAPI() {
   }

   public static synchronized void registerUsage(Item item, String translationKey) {
      if (item != null && translationKey != null && !translationKey.isBlank()) {
         ITEM_USAGES.computeIfAbsent(item, key -> new LinkedHashSet<>()).add(translationKey);
      }
   }

   public static synchronized void registerTagUsage(TagKey<Item> tag, String translationKey) {
      if (tag != null && translationKey != null && !translationKey.isBlank()) {
         TAG_USAGES.computeIfAbsent(tag, key -> new LinkedHashSet<>()).add(translationKey);
      }
   }

   public static synchronized List<String> usageKeys(Item item) {
      if (item == null) {
         return List.of();
      }
      LinkedHashSet<String> out = new LinkedHashSet<>();
      LinkedHashSet<String> direct = ITEM_USAGES.get(item);
      if (direct != null) {
         out.addAll(direct);
      }
      for (Map.Entry<TagKey<Item>, LinkedHashSet<String>> entry : TAG_USAGES.entrySet()) {
         if (BuiltInRegistries.ITEM.wrapAsHolder(item).is(entry.getKey())) {
            out.addAll(entry.getValue());
         }
      }
      return List.copyOf(out);
   }

   public static MutableComponent usageLine(List<String> keys) {
      MutableComponent joined = Component.empty();
      boolean first = true;
      for (String key : keys) {
         if (!first) {
            joined.append(Component.translatable("usage.herbcraft.separator"));
         }
         joined.append(Component.translatable(key));
         first = false;
      }
      return Component.translatable("usage.herbcraft.line", joined);
   }
}
