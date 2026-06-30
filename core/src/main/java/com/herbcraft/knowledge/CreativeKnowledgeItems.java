package com.herbcraft.knowledge;

import com.herbcraft.api.KnowledgeAPI;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.ModifyOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CreativeKnowledgeItems {
   private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools_and_utilities")
   );

   private CreativeKnowledgeItems() {
   }

   public static void register() {
      CreativeModeTabEvents.modifyOutputEvent(TOOLS_AND_UTILITIES).register((ModifyOutput)output -> {
         List<ItemStack> stacks = new ArrayList<>();
         stacks.add(new ItemStack(CodexItems.HERBAL_CODEX));
         stacks.addAll(tatteredPageVariants());
         output.insertAfter(Items.WRITABLE_BOOK, stacks);
      });
   }

   private static List<ItemStack> tatteredPageVariants() {
      List<ItemStack> stacks = new ArrayList<>();

      for (KnowledgeAPI.Chapter chapter : KnowledgeAPI.chapters()) {
         for (KnowledgeAPI.Entry entry : chapter.entries()) {
            ItemStack stack = new ItemStack(CodexItems.TATTERED_PAGE);
            stack.set(CodexComponents.PAGE_ENTRY, new CodexComponents.PageEntry(chapter.id(), entry.id()));
            stacks.add(stack);
         }
      }

      for (KnowledgeAPI.Chapter chapter : KnowledgeAPI.chapters()) {
         for (PageKind kind : new PageKind[]{PageKind.TRADE, PageKind.RARE, PageKind.ERRATA}) {
            ItemStack stack = new ItemStack(CodexItems.TATTERED_PAGE);
            stack.set(CodexComponents.PAGE_ENTRY, new CodexComponents.PageEntry(chapter.id(), ""));
            stack.set(CodexComponents.PAGE_KIND, kind);
            stacks.add(stack);
         }
      }

      return stacks;
   }
}
