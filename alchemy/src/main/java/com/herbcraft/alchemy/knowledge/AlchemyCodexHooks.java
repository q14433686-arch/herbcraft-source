package com.herbcraft.alchemy.knowledge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.alchemy.HerbcraftAlchemy;
import com.herbcraft.knowledge.CodexLoot;
import com.herbcraft.knowledge.CodexLootRules;
import com.herbcraft.knowledge.PageKind;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents.Modify;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public final class AlchemyCodexHooks {
   private static final List<CodexLootRules.PageInjection> ALCHEMY_RULES = CodexLootRules.load("data/herbcraft_alchemy/codex_loot_injections.json");
   private static final AlchemyCodexHooks.WitchLootRules WITCH_RULES = loadWitchLootRules();
   private static final List<AlchemyCodexHooks.ItemLootInjection> ITEM_LOOT_RULES = loadItemLootRules();
   private static final ResourceKey<LootTable> WITCH_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("minecraft", "entities/witch"));

   private AlchemyCodexHooks() {}

   public static void register() {
      LootTableEvents.MODIFY.register((Modify)(key, tableBuilder, source, registries) -> {
         if (!source.isBuiltin()) {
            return;
         }
         for (CodexLootRules.PageInjection rule : ALCHEMY_RULES) {
            if (rule.lootTable().equals(key)) {
               CodexLoot.addTatteredPagePool(tableBuilder, rule.chance(), rule.chapter(), rule.entry(), rule.kind());
            }
         }
         for (ItemLootInjection rule : ITEM_LOOT_RULES) {
            if (rule.lootTable().equals(key)) {
               addItemLoot(tableBuilder, rule);
            }
         }
         if (key.equals(WITCH_LOOT)) addWitchLoot(tableBuilder);
      });
   }

   private static void addWitchLoot(LootTable.Builder tableBuilder) {
      if (WITCH_RULES.page() != null) {
         CodexLoot.addTatteredPagePool(
            tableBuilder,
            WITCH_RULES.page().chance(),
            WITCH_RULES.page().chapter(),
            WITCH_RULES.page().entry(),
            WITCH_RULES.page().kind()
         );
      }
      if (WITCH_RULES.essenceChance() > 0.0F && !WITCH_RULES.essenceItems().isEmpty()) {
         LootPool.Builder pool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(WITCH_RULES.essenceChance()));
         WITCH_RULES.essenceItems().forEach(item -> pool.add(LootItem.lootTableItem(item)));
         tableBuilder.pool(pool.build());
      }
      if (WITCH_RULES.potionChance() > 0.0F && !WITCH_RULES.potions().isEmpty()) {
         LootPool.Builder pool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(WITCH_RULES.potionChance()));
         for (Holder<Potion> potion : WITCH_RULES.potions()) {
            pool.add(LootItem.lootTableItem(Items.POTION).apply(SetComponentsFunction.setComponent(DataComponents.POTION_CONTENTS, new PotionContents(potion))));
         }
         tableBuilder.pool(pool.build());
      }
   }


   private static void addItemLoot(LootTable.Builder tableBuilder, ItemLootInjection rule) {
      LootPool.Builder pool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(rule.chance()));
      var entry = LootItem.lootTableItem(rule.item());
      if (rule.count() > 1) {
         entry.apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(rule.count())));
      }
      if (rule.potion() != null) {
         entry.apply(SetPotionFunction.setPotion(rule.potion()));
      }
      if (rule.distilledEssence() != null) {
         entry.apply(SetComponentsFunction.setComponent(com.herbcraft.alchemy.distillation.DistillationComponents.DISTILLED_ESSENCE, rule.distilledEssence()));
      }
      if (rule.armorSupport() != null) {
         entry.apply(SetComponentsFunction.setComponent(com.herbcraft.alchemy.armor.ArmorSupportComponents.ARMOR_SUPPORT, rule.armorSupport()));
      }
      pool.add(entry);
      tableBuilder.pool(pool.build());
   }

   private static List<ItemLootInjection> loadItemLootRules() {
      List<ItemLootInjection> rules = new ArrayList<>();
      try (InputStream input = AlchemyCodexHooks.class.getClassLoader().getResourceAsStream("data/herbcraft_alchemy/alchemy_loot_injections.json")) {
         if (input == null) {
            HerbcraftAlchemy.LOGGER.info("No data/herbcraft_alchemy/alchemy_loot_injections.json found; skipping Alchemy item loot injections.");
            return List.of();
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         if (!root.has("item_injections")) {
            return List.of();
         }
         for (JsonElement element : root.getAsJsonArray("item_injections")) {
            JsonObject json = element.getAsJsonObject();
            ResourceKey<LootTable> lootTable = ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(json.get("loot_table").getAsString()));
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(json.get("item").getAsString()));
            float chance = json.get("chance").getAsFloat();
            int count = json.has("count") ? json.get("count").getAsInt() : 1;
            Holder<Potion> potion = null;
            if (json.has("potion")) {
               Identifier potionId = Identifier.parse(json.get("potion").getAsString());
               potion = BuiltInRegistries.POTION.get(ResourceKey.create(Registries.POTION, potionId)).orElseThrow(() -> new IllegalStateException("Unknown potion in alchemy_loot_injections.json: " + potionId));
            }
            com.herbcraft.alchemy.distillation.DistilledEssenceComponent distilled = null;
            com.herbcraft.alchemy.armor.ArmorSupportComponent support = null;
            if (json.has("components")) {
               JsonObject components = json.getAsJsonObject("components");
               if (components.has("herbcraft_alchemy:distilled_essence")) {
                  JsonObject c = components.getAsJsonObject("herbcraft_alchemy:distilled_essence");
                  distilled = new com.herbcraft.alchemy.distillation.DistilledEssenceComponent(c.get("source_essence").getAsString(), c.get("orientation").getAsString());
               }
               if (components.has("herbcraft_alchemy:armor_support")) {
                  JsonObject c = components.getAsJsonObject("herbcraft_alchemy:armor_support");
                  support = new com.herbcraft.alchemy.armor.ArmorSupportComponent(
                     c.get("support_id").getAsString(),
                     c.get("source_essence").getAsString(),
                     c.get("orientation").getAsString(),
                     c.get("buffer_amount").getAsInt(),
                     c.has("mode") ? com.herbcraft.alchemy.armor.ArmorSupportMode.byName(c.get("mode").getAsString()) : com.herbcraft.alchemy.armor.ArmorSupportMode.ATTACHED
                  );
               }
            }
            rules.add(new ItemLootInjection(lootTable, chance, item, count, potion, distilled, support));
         }
         HerbcraftAlchemy.LOGGER.info("Loaded {} Herbcraft Alchemy item loot injection rules.", rules.size());
         return List.copyOf(rules);
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy alchemy_loot_injections.json", e);
      }
   }

   private static WitchLootRules loadWitchLootRules() {
      try (InputStream input = AlchemyCodexHooks.class.getClassLoader().getResourceAsStream("data/herbcraft_alchemy/witch_loot.json")) {
         if (input == null) {
            throw new IllegalStateException("Missing data/herbcraft_alchemy/witch_loot.json");
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         WitchPage page = null;
         if (root.has("page")) {
            JsonObject json = root.getAsJsonObject("page");
            page = new WitchPage(
               json.get("chance").getAsFloat(),
               json.has("chapter") ? json.get("chapter").getAsString() : "alchemy",
               json.has("entry") ? json.get("entry").getAsString() : "",
               json.has("page_kind") ? PageKind.valueOf(json.get("page_kind").getAsString().toUpperCase(Locale.ROOT)) : PageKind.TRADE
            );
         }
         float essenceChance = 0.0F;
         List<Item> essenceItems = new ArrayList<>();
         if (root.has("essence_pool")) {
            JsonObject json = root.getAsJsonObject("essence_pool");
            essenceChance = json.get("chance").getAsFloat();
            for (JsonElement element : json.getAsJsonArray("items")) {
               Identifier id = Identifier.parse(element.getAsString());
               Item item = BuiltInRegistries.ITEM.getValue(id);
               if (item != null) essenceItems.add(item);
            }
         }
         float potionChance = 0.0F;
         List<Holder<Potion>> potions = new ArrayList<>();
         if (root.has("potion_pool")) {
            JsonObject json = root.getAsJsonObject("potion_pool");
            potionChance = json.get("chance").getAsFloat();
            for (JsonElement element : json.getAsJsonArray("potions")) {
               Identifier id = Identifier.parse(element.getAsString());
               BuiltInRegistries.POTION.get(ResourceKey.create(Registries.POTION, id)).ifPresent(holder -> potions.add((Holder<Potion>)holder));
            }
         }
         HerbcraftAlchemy.LOGGER.info("Loaded Herbcraft Alchemy witch loot rules.");
         return new WitchLootRules(page, essenceChance, List.copyOf(essenceItems), potionChance, List.copyOf(potions));
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy witch_loot.json", e);
      }
   }

   private record ItemLootInjection(ResourceKey<LootTable> lootTable, float chance, Item item, int count, Holder<Potion> potion, com.herbcraft.alchemy.distillation.DistilledEssenceComponent distilledEssence, com.herbcraft.alchemy.armor.ArmorSupportComponent armorSupport) {
   }

   private record WitchPage(float chance, String chapter, String entry, PageKind kind) {
   }

   private record WitchLootRules(WitchPage page, float essenceChance, List<Item> essenceItems, float potionChance, List<Holder<Potion>> potions) {
   }
}
