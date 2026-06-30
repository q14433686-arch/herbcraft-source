# EXTENDED_FEATURES_REPORT

## Scope

Processed `Herbcraft 扩展任务.txt` against the actual 26.1.2 source/environment instead of blindly following the suggested implementation notes.

Also applied the requested glistering melon slice value correction:

```json
"minecraft:glistering_melon_slice": {
  "nutrition": 4,
  "saturation_modifier": 9.6
}
```

Effects were kept as previously fixed: guaranteed instant health + guaranteed absorption.

## Implemented

### 1. Herb stack actionbar feedback

Updated:

```text
core/src/main/java/com/herbcraft/HerbcraftPlayerData.java
core/src/main/java/com/herbcraft/mixin/PlayerMixin.java
core/src/main/java/com/herbcraft/logic/HerbStackManager.java
core/src/main/resources/assets/herbcraft/lang/zh_cn.json
core/src/main/resources/assets/herbcraft/lang/en_us.json
```

Behavior:

- Stage 3 / 5 / 8 messages are sent through `sendOverlayMessage`, which is the actual 26.1.2 method replacing the suggested `displayClientMessage(..., true)` call.
- Overload sends its own actionbar message.
- Natural herb-stack expiry sends a fade message.
- Messages are per-player.
- Stage spam is avoided with `last_herb_stack_stage`.

### 2. Hidden cuisine recipes

Added hidden/special dishes:

```text
herbcraft_cuisine:hundred_flower_feast
herbcraft_cuisine:deadly_hodgepodge
```

Updated:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/hundred_flower_feast.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/deadly_hodgepodge.json
cuisine/src/main/resources/assets/herbcraft_cuisine/items/*.json
cuisine/src/main/resources/assets/herbcraft_cuisine/models/item/*.json
cuisine/src/main/resources/assets/herbcraft_cuisine/textures/item/*.png
cuisine/src/main/resources/assets/herbcraft_cuisine/lang/*.json
```

Notes:

- `hundred_flower_feast` is a pure positive feast.
- `deadly_hodgepodge` uses `clear_all_effects` and then applies the requested emergency effect package.
- Recipe JSON uses `show_notification: false` to avoid normal recipe toast spam.
- Full REI hiding was not implemented because REI 26.1.2 compatibility remains outside the current source/environment.

### 3. Advancement tree

Added 31 advancement JSON files:

```text
core/src/main/resources/data/herbcraft/advancement/*.json                  16
cuisine/src/main/resources/data/herbcraft_cuisine/advancement/*.json       6
alchemy/src/main/resources/data/herbcraft_alchemy/advancement/*.json       9
```

Also added:

```text
core/src/main/java/com/herbcraft/logic/HerbcraftAdvancements.java
```

Currently wired manual awards include:

- Herb stack 3 / 5 / 8.
- Herb overload.
- Opening the codex.
- Using tattered pages.
- Gaining effect immunity.
- Eating cuisine dishes / the two hidden dishes.

Some highly specialized entries remain data-tree entries using `minecraft:impossible` until more exact event hooks are added, but the advancement tab/data files and translations are present and build-valid.

### 4. Witch integration

Updated:

```text
alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java
alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyCodexHooks.java
alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemyConfig.java
alchemy/src/main/java/com/herbcraft/alchemy/mixin/WitchHerbcraftPotionMixin.java
alchemy/src/main/resources/herbcraft_alchemy.mixins.json
```

Implemented:

- Witch loot table injection through Fabric loot events, guarded by `source.isBuiltin()`.
- Alchemy tattered page drop pool, 15%.
- Low-tier essence drop pool, 10%.
- Safe drinkable witch potion drop pool, 3%.
- Safe witch-only murky potion variants:
  - `witch_tulip_murky`
  - `witch_azure_bluet_murky`
  - `witch_pitcher_plant_murky`
  - `witch_azalea_murky`
  - `witch_closed_eyeblossom_murky`
- Witch thrown potion replacement via a narrow `performRangedAttack` HEAD injection, not a goal/AI rewrite.
- Config file:

```text
config/herbcraft-alchemy.json
```

Defaults:

```json
{
  "enable_witch_herbcraft_potions": true,
  "witch_herbcraft_potion_chance": 0.10
}
```

Safety:

- Witch integration uses shortened witch-only potion variants.
- No T3, no long wither, no undead toxin, no cardiac toxin, no wither venom III.

### 5. Animal food tags from previous step retained

The core animal tag feature remains present and validated.

## Validation

Build:

```text
BUILD SUCCESSFUL
```

Validation commands passed:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_panel_hodge_recipe.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_extended_features.py
python3 scripts/verify_built_jars.py
```

## Notes

- The implementation uses actual 26.1.2 paths/method names where they differ from the document.
- `displayClientMessage(Component, true)` is not present in current mappings; `sendOverlayMessage(Component)` is used.
- Animal tags use `data/minecraft/tags/item`, not the document's illustrative `data/herbcraft/tags/items` path.
- REI-specific hidden recipe behavior is deferred because there is no confirmed 26.1.2 REI integration in this source.
