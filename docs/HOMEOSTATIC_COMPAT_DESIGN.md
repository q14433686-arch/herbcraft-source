> Historical design note (2026-06-16): this file records the original Homeostatic compatibility design discussion. Current implemented values and validator expectations are in `docs/HOMEOSTATIC_COMPAT.md`, `CURRENT_BASELINE.md`, and `scripts/validate_homeostatic_compat.py`. Some proposed values below were superseded; for example, water bowl and sugar cane values are now validator-anchored.

# Homeostatic Compatibility Design: Herbcraft Cuisine Round 1

Status: implemented for round 1 Cuisine custom edible outputs and round 2 selected Core/vanilla Herbcraft edible items. Future changes should still be discussed before expanding coverage.

This document is based on current Herbcraft source and the Homeostatic `26.1` branch source.

## Source files checked

Herbcraft:

- `cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json`
- `cuisine/src/main/resources/assets/herbcraft_cuisine/lang/zh_cn.json`
- `cuisine/src/main/resources/assets/herbcraft_cuisine/lang/en_us.json`
- `core/src/main/resources/data/herbcraft/nutrition_profiles.json`
- `cuisine/src/main/java/com/herbcraft/cuisine/item/WaterBowlItem.java`
- `cuisine/src/main/java/com/herbcraft/cuisine/item/DishItem.java`
- `cuisine/src/main/java/com/herbcraft/cuisine/item/HodgepodgeItem.java`
- `cuisine/src/main/java/com/herbcraft/cuisine/registry/DishRegistry.java`

Homeostatic `26.1` source:

- `Common/src/main/java/homeostatic/common/item/DrinkableItem.java`
- `Common/src/main/java/homeostatic/common/item/DrinkableItemManager.java`
- `Common/src/generated/resources/data/minecraft/environment/drinkable/*.json`

## Homeostatic data format confirmed

Homeostatic loads item drinkability from:

```text
data/<namespace>/environment/drinkable/*.json
```

Observed JSON shape:

```json
{
  "type": "minecraft:milk_bucket",
  "amount": 9,
  "saturation": 2.1,
  "effect_potency": 0,
  "effect_duration": 0,
  "effect_chance": 0.0
}
```

Field meaning:

| Field | Meaning |
|---|---|
| `type` | item id |
| `amount` | thirst/hydration restored |
| `saturation` | thirst saturation / retention |
| `effect_potency` | potency of Homeostatic thirst effect for dirty/risky drinks |
| `effect_duration` | duration of thirst effect |
| `effect_chance` | chance to apply thirst effect |

Useful Homeostatic vanilla reference values from current generated data:

| Item | amount | saturation | effect chance | Notes |
|---|---:|---:|---:|---|
| `minecraft:milk_bucket` | 9 | 2.1 | 0.0 | very strong hydration |
| `minecraft:honey_bottle` | 4 | 1.0 | 0.0 | medium hydration despite sweetness |
| `minecraft:melon_slice` | 2 | 0.6 | 0.0 | light juicy food |
| `minecraft:suspicious_stew` | 1 | 0.0 | 0.3 | risky food/drink |
| `minecraft:poisonous_potato` | 1 | 0.0 | 0.2 | risky food |

## Round 1 scope

Round 1 should only add Homeostatic drinkable data for **Herbcraft Cuisine custom items**.

Do not add Homeostatic data for vanilla raw Herbcraft edible items in this round. That includes snowball, ice, powder snow bucket, sugar, honey block, blaze powder, etc. Those should be discussed in a separate Core/vanilla compatibility round to avoid conflicts with Homeostatic's own vanilla data.

Do not add Homeostatic data for `minecraft:potion`, because Homeostatic item drinkability is item-id based and would affect all potions, not just Herbcraft potions.

Do not add raw cuisine items. Raw dish items are not edible.

## General design rules

1. Water, soup, stew, brew, porridge, bowl, and hotpot items can restore thirst.
2. Sweet/sticky/resinous items should restore little or no thirst, and may be risky if they are medicinally harsh.
3. Poisonous, bitter, toxic, fiery, or destabilizing dishes may have low hydration plus a chance of Homeostatic thirst effect.
4. Values should be conservative relative to Homeostatic milk bucket (`amount 9`, `saturation 2.1`).
5. Cuisine integration should be optional data only; do not hard-depend on Homeostatic.

## Proposed JSON output path

If approved, round 1 implementation should create files under:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/environment/drinkable/
```

File names should use item paths, e.g.:

```text
water_bowl.json
blue_ice_brew.json
deadly_hodgepodge.json
```

Each file should contain one Homeostatic drinkable item definition.

## Proposed round 1 table: Herbcraft Cuisine custom items

### S tier: clear hydration

These are explicitly water-like, soup-like, cold brew, sea-based, or clean liquid dishes.

| Item ID | CN name | amount | saturation | effect chance | Notes |
|---|---|---:|---:|---:|---|
| `herbcraft_cuisine:water_bowl` | 盛水的碗 | 4 | 0.8 | 0.0 | clean water bowl; below milk bucket |
| `herbcraft_cuisine:blue_ice_brew` | 蓝冰冷萃汤 | 5 | 1.0 | 0.0 | cold brew / ice drink |
| `herbcraft_cuisine:seafarer_stew` | 海行杂炖 | 5 | 1.0 | 0.0 | sea-themed stew with water breathing |
| `herbcraft_cuisine:eye_opening_soup` | 明目开眼汤 | 4 | 0.8 | 0.0 | soup |
| `herbcraft_cuisine:warped_swift_soup` | 诡异疾汤 | 4 | 0.8 | 0.0 | soup, but not risky enough for thirst penalty |
| `herbcraft_cuisine:pine_needle_soup` | 松针热汤 | 4 | 0.8 | 0.0 | fixed soup |
| `herbcraft_cuisine:pine_needle_broth` | 松针热汤（药膳） | 4 | 0.8 | 0.0 | medicinal soup/broth |
| `herbcraft_cuisine:pale_night_soup` | 夜游苍叶汤 | 4 | 0.8 | 0.0 | soup |

### A tier: ordinary soups, stews, porridges, bowls, hotpots

These restore moderate hydration.

| Item ID | CN name | amount | saturation | effect chance | Notes |
|---|---|---:|---:|---:|---|
| `herbcraft_cuisine:soul_cleansing_stew` | 净魂雪羹 | 3 | 0.7 | 0.0 | medicinal stew |
| `herbcraft_cuisine:pumpkin_porridge` | 饱腹南瓜粥 | 3 | 0.7 | 0.0 | porridge |
| `herbcraft_cuisine:honey_leaf_soup` | 清毒蜜叶汤 | 3 | 0.7 | 0.0 | soup, honey but diluted |
| `herbcraft_cuisine:stomach_waking_bowl` | 醒胃冰盅 | 3 | 0.6 | 0.0 | bowl, cool/refreshing |
| `herbcraft_cuisine:peony_restoration_soup` | 复力牡丹汤 | 3 | 0.7 | 0.0 | soup |
| `herbcraft_cuisine:fatigue_breaking_stew` | 破倦镇痛羹 | 3 | 0.7 | 0.0 | stew |
| `herbcraft_cuisine:lily_mobility_stew` | 行身铃兰羹 | 2 | 0.4 | 0.0 | medicinal, includes slowness; less clean |
| `herbcraft_cuisine:star_falling_soup` | 坠星压浮汤 | 3 | 0.7 | 0.0 | soup |
| `herbcraft_cuisine:omen_calming_porridge` | 安兆闭眼粥 | 3 | 0.7 | 0.0 | porridge |
| `herbcraft_cuisine:twin_fungus_hotpot` | 双菌下界煲 | 3 | 0.7 | 0.0 | hotpot; not fiery like crimson variant |
| `herbcraft_cuisine:cherry_nectar_porridge` | 樱花甘露粥 | 3 | 0.7 | 0.0 | nectar/porridge |
| `herbcraft_cuisine:honeycomb_soothing_stew` | 蜜脾润喉羹 | 3 | 0.7 | 0.0 | soothing stew, diluted honeycomb |
| `herbcraft_cuisine:golden_dandelion_stew` | 金蒲公英羹 | 3 | 0.7 | 0.0 | medicinal stew despite zero food nutrition |

### B tier: moist but not drink-like

Small hydration only.

| Item ID | CN name | amount | saturation | effect chance | Notes |
|---|---|---:|---:|---:|---|
| `herbcraft_cuisine:sea_medley` | 凉拌海杂 | 3 | 0.6 | 0.0 | sea/cold dish, more hydrated than normal salad |
| `herbcraft_cuisine:snowcap_bowl` | 雪顶冰盅 | 3 | 0.7 | 0.0 | snow/ice bowl |
| `herbcraft_cuisine:dandelion_salad` | 蒲公英沙拉 | 1 | 0.2 | 0.0 | salad, light moisture |
| `herbcraft_cuisine:three_flower_platter` | 三花拼盘 | 1 | 0.2 | 0.0 | platter, light moisture |
| `herbcraft_cuisine:sapling_medley` | 嫩芽什锦 | 1 | 0.1 | 0.0 | plant medley, low moisture |
| `herbcraft_cuisine:honeyed_petals` | 蜜渍花瓣 | 1 | 0.1 | 0.0 | honeyed/sticky, but diluted enough not to penalize |
| `herbcraft_cuisine:resin_guard_stew` | 树脂护胃炖 | 1 | 0.2 | 0.0 | resinous stew; low clean hydration |
| `herbcraft_cuisine:hundred_flower_feast` | 百花宴 | 5 | 1.2 | 0.0 | high-tier feast; strong but below milk bucket hydration |

### C/D tier: risky or thirst-aggravating cuisine

These should have low hydration and a Homeostatic thirst-effect risk.

Use Homeostatic's suspicious/poisonous food reference as the risk scale (`effect_duration 200`, `effect_potency 45`).

| Item ID | CN name | amount | saturation | effect chance | effect duration | effect potency | Notes |
|---|---|---:|---:|---:|---:|---:|---|
| `herbcraft_cuisine:deadly_hodgepodge` | 剧毒蛊 | 1 | 0.0 | 0.5 | 200 | 45 | toxic hidden dish; should not be clean hydration |
| `herbcraft_cuisine:crimson_war_hotpot` | 绯红战煲 | 1 | 0.1 | 0.15 | 200 | 45 | fiery/spicy, ignites player |
| `herbcraft_cuisine:bitter_remedy` | 苦口良菜 | 1 | 0.0 | 0.2 | 200 | 45 | bitter medicine, poison trace |
| `herbcraft_cuisine:analgesic_stew` | 镇痛羹 | 2 | 0.2 | 0.2 | 200 | 45 | includes nausea; medicinally harsh |

## Explicit exclusions for round 1

Do not add drinkable JSONs for:

- Any `raw_*` cuisine item: raw dishes are not edible.
- `herbcraft_cuisine:hodgepodge`: Homeostatic item data cannot inspect `hodgepodge_contents`, so dynamic wateriness cannot be represented safely.
- Any `minecraft:*` raw Herbcraft edible item: discuss separately because Homeostatic may already define vanilla drinkables and because Core raw edibles include ice/snow, sugar/honey, toxic herbs, powders, eggs, and other categories with different logic.
- `minecraft:potion`: item-id matching would affect all potions, not just Herbcraft potions.

## Round 2 discussion target: Core raw edible items

The next design discussion should cover vanilla/Core raw edible items separately, especially:

### Likely positive hydration

- `minecraft:snowball`
- `minecraft:ice`
- `minecraft:packed_ice`
- `minecraft:blue_ice`
- `minecraft:powder_snow_bucket`
- `minecraft:kelp`
- `minecraft:seagrass`
- `minecraft:sea_pickle`
- `minecraft:sugar_cane`
- `minecraft:melon`
- `minecraft:cactus_flower`

### Likely thirst risk / no hydration

- `minecraft:sugar`
- `minecraft:honey_block`
- `minecraft:honeycomb`
- `minecraft:honeycomb_block`
- `minecraft:slime_ball`
- `minecraft:resin_clump`
- `minecraft:resin_brick`
- `minecraft:blaze_powder`
- `minecraft:magma_cream`
- `minecraft:nether_wart`
- `minecraft:fermented_spider_eye`
- poisonous flowers/leaves/fungi

Important correction: snowball must be considered edible in Herbcraft because `SnowballItemMixin` and `ProjectileUseHandler` explicitly support short-tap throw / long-hold eat behavior.

## Proposed validator for implementation round

When implementing this design, add:

```text
scripts/validate_homeostatic_compat.py
```

Checks:

- All files under `data/herbcraft_cuisine/environment/drinkable/` are valid JSON.
- Every file contains `type`, `amount`, `saturation`, `effect_potency`, `effect_duration`, `effect_chance`.
- No `raw_*` item appears.
- `herbcraft_cuisine:hodgepodge` does not appear in round 1.
- `minecraft:potion` does not appear.
- `water_bowl` amount does not exceed Homeostatic milk bucket reference (`9`).
- `deadly_hodgepodge` has nonzero `effect_chance`.
- Every `type` belongs to `herbcraft_cuisine` in round 1.

## Historical implementation gate

This was the original design gate: do not create the actual Homeostatic drinkable JSON files until the author confirms the table.

Current status as of 2026-06-16: author approval has already happened for Round 1 Cuisine and Round 2 selected Core/vanilla entries, and those data files are implemented. Use `docs/HOMEOSTATIC_COMPAT.md` and `scripts/validate_homeostatic_compat.py` for current values.


## Negative amount warning

Client testing showed that Homeostatic can store water below zero when drinkable JSON uses negative `amount`. The HUD may appear to recover partially while the internal value remains `<= 0`, causing dehydration to continue. Therefore Herbcraft compatibility data should avoid negative `amount` values and represent thirst-aggravating foods with `amount: 0` plus sufficiently strong `effect_chance`, `effect_duration`, and `effect_potency`.
