package com.herbcraft.knowledge;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents.Modify;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public final class CodexLoot {
   private static List<CodexLootRules.PageInjection> herbalRules = CodexLootRules.load("data/herbcraft/codex_loot_injections.json");

   private CodexLoot() {
   }

   public static void applyExternalData(ResourceManager manager) {
      List<CodexLootRules.PageInjection> merged = new ArrayList<>(CodexLootRules.load("data/herbcraft/codex_loot_injections.json"));
      merged.addAll(CodexLootRules.loadExternal(manager, "herbcraft/codex_loot_injections"));
      herbalRules = List.copyOf(merged);
   }

   public static void register() {
      LootTableEvents.MODIFY.register((Modify)(key, tableBuilder, source, registries) -> {
         if (!source.isBuiltin()) {
            return;
         }
         for (CodexLootRules.PageInjection rule : herbalRules) {
            if (rule.lootTable().equals(key)) {
               addTatteredPagePool(tableBuilder, rule.chance(), rule.chapter(), rule.entry(), rule.kind());
            }
         }
      });
   }

   public static void addTatteredPagePool(Builder tableBuilder, float chance, String chapter, String entry) {
      addTatteredPagePool(tableBuilder, chance, chapter, entry, PageKind.COMMON);
   }

   public static void addTatteredPagePool(Builder tableBuilder, float chance, String chapter, String entry, PageKind kind) {
      tableBuilder.pool(
         LootPool.lootPool()
            .when(LootItemRandomChanceCondition.randomChance(chance))
            .add(
               LootItem.lootTableItem(CodexItems.TATTERED_PAGE)
                  .apply(SetComponentsFunction.setComponent(CodexComponents.PAGE_ENTRY, new CodexComponents.PageEntry(chapter, entry)))
                  .apply(SetComponentsFunction.setComponent(CodexComponents.PAGE_KIND, kind))
            )
            .build()
      );
   }
}
