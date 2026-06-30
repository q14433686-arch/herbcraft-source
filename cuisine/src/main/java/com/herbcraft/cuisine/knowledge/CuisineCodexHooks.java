package com.herbcraft.cuisine.knowledge;

import com.herbcraft.knowledge.CodexLoot;
import com.herbcraft.knowledge.CodexLootRules;
import java.util.List;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents.Modify;

public final class CuisineCodexHooks {
   private static final List<CodexLootRules.PageInjection> CUISINE_RULES = CodexLootRules.load("data/herbcraft_cuisine/codex_loot_injections.json");

   private CuisineCodexHooks() {
   }

   public static void register() {
      LootTableEvents.MODIFY.register((Modify)(key, tableBuilder, source, registries) -> {
         if (!source.isBuiltin()) {
            return;
         }
         for (CodexLootRules.PageInjection rule : CUISINE_RULES) {
            if (rule.lootTable().equals(key)) {
               CodexLoot.addTatteredPagePool(tableBuilder, rule.chance(), rule.chapter(), rule.entry(), rule.kind());
            }
         }
      });
   }
}
