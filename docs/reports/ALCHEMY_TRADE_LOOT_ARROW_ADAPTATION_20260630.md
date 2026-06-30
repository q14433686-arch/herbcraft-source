# Alchemy trade, world-loot, and potion-arrow adaptation — 2026-06-30

## Scope

This pass adapts the newer Alchemy mechanics to existing acquisition surfaces without adding new rule systems:

- villager trades;
- world loot / chest loot / entity-style loot injection surfaces;
- tipped arrows created from Herbcraft processed/special potions.

The implementation intentionally reuses existing mechanics:

- data-driven `villager_trade` JSON plus trade tags;
- vanilla `minecraft:set_potion` and `minecraft:set_components` item modifiers;
- vanilla `minecraft:crafting_imbue` tipped-arrow recipe shape;
- existing potion processing / burden / knowledge hooks.

## Villager trade additions

Added additional trade JSON and tag references for the newer Alchemy outputs:

### Cleric / toolsmith potions

- `herbcraft:cleric/4/emerald_dandelion_long`
- `herbcraft:cleric/4/emerald_poppy_strong`
- `herbcraft:cleric/5/emerald_wither_venom_iii`
- `herbcraft:toolsmith/4/emerald_cocoa_beans_long`
- `herbcraft:toolsmith/5/emerald_crimson_fungus_strong`

These reuse `minecraft:set_potion` and existing registered potion IDs.

### Fletcher potion arrows

- `herbcraft:fletcher/4/emerald_arrow_dandelion_long_arrow`
- `herbcraft:fletcher/4/emerald_arrow_poppy_strong_arrow`
- `herbcraft:fletcher/5/emerald_arrow_wither_venom_iii_arrow`

These reuse `minecraft:set_potion` on `minecraft:tipped_arrow`, matching existing fletcher trade style.

### Wandering trader distilled essence samples

- `herbcraft:wanderer/emerald_distilled_essence_dandelion_sustained`
- `herbcraft:wanderer/emerald_distilled_essence_poppy_intense`

These reuse `minecraft:set_components` with `herbcraft_alchemy:distilled_essence`.

### Armorer armor-support samples

- `herbcraft:armorer/4/emerald_iron_chestplate_steady_bearing`
- `herbcraft:armorer/5/emerald_diamond_chestplate_braced_bearing`

These reuse `minecraft:set_components` with `herbcraft_alchemy:armor_support`.

All new trades are referenced by existing `data/minecraft/tags/villager_trade/**/level_*.json` or wandering-trader tags with `replace=false`.

## World loot additions

Added:

- `data/herbcraft_alchemy/alchemy_loot_injections.json`

`AlchemyCodexHooks` now loads this JSON and injects item pools through the existing Fabric loot v3 modify hook, guarded by `source.isBuiltin()`.

Current injected examples include:

- processed potions in village/stronghold/trial contexts;
- tipped arrows in fletcher/jungle-temple contexts;
- componentized distilled essences in bastion/ancient-city contexts;
- componentized armor support in village armorer contexts.

Supported item injection fields:

- `loot_table`
- `chance`
- `item`
- optional `count`
- optional `potion`
- optional `components.herbcraft_alchemy:distilled_essence`
- optional `components.herbcraft_alchemy:armor_support`

This is deliberately Alchemy-bundled/startup content, consistent with the current Alchemy rule-table policy.

## Tipped arrow adaptation

### Crafting

Added:

- `data/herbcraft/recipe/advanced_tipped_arrow.json`

It mirrors vanilla `minecraft:crafting_imbue`:

- source: `minecraft:lingering_potion`
- material: `minecraft:arrow`
- result: `minecraft:tipped_arrow`, count 8

Vanilla `ImbueRecipe` copies `POTION_CONTENTS` from the center lingering potion to the arrow result, so Herbcraft potion IDs and rewritten custom potion contents are reused rather than inventing a new arrow system.

### Runtime hit behavior

Added:

- `AlchemyPotionArrowMixin`

It hooks `Arrow#doPostHurtEffects`:

1. At `HEAD`, it initializes the arrow's pickup-origin stack before vanilla applies potion effects. This lets processed potion metadata rewrite/update `POTION_CONTENTS` before hit effects are applied.
2. At `RETURN`, if the target is a `ServerPlayer`, it marks the potion as tasted and applies potion-burden runtime from the same arrow stack.

`AlchemyKnowledgeSupport.isPotionContainer(...)` now includes `Items.TIPPED_ARROW`, so arrow exposure participates in the same knowledge/progression path as drink/splash/lingering exposure.

## Files changed

- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyCodexHooks.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyKnowledgeSupport.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyPotionArrowMixin.java`
- `alchemy/src/main/resources/herbcraft_alchemy.mixins.json`
- `alchemy/src/main/resources/data/herbcraft/recipe/advanced_tipped_arrow.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/alchemy_loot_injections.json`
- new/updated `alchemy/src/main/resources/data/herbcraft/villager_trade/**`
- updated `alchemy/src/main/resources/data/minecraft/tags/villager_trade/**`
- `scripts/validate_alchemy_trade_loot_arrow_adaptation.py`

## Validation

Passed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
python3 scripts/validate_alchemy_trade_loot_arrow_adaptation.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/validate_alchemy_advancement_and_sync.py
python3 scripts/validate_potion_processing_rules.py
python3 scripts/validate_potion_burden_runtime.py
python3 scripts/validate_extended_features.py
python3 scripts/check_lang_diff.py
python3 scripts/verify_built_jars.py
```

## Client-only checks still recommended

1. Confirm new villager trades appear at expected professions/levels and give componentized items correctly.
2. Confirm generated distilled essences and armor support from trades/loot show correct tooltips and grant acquisition progress when obtained.
3. Craft tipped arrows from Herbcraft lingering potions and confirm arrow hits apply the processed/special potion effects.
4. Confirm arrow hits grant potion taste/progression and burden behavior only on actual hit exposure, not on mere inventory possession.
5. Confirm new chest injections feel rare enough and do not overload early villages.
