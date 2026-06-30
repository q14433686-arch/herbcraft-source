# Homeostatic Compatibility

Herbcraft includes optional data compatibility for Homeostatic. This compatibility is data-only: Herbcraft does not hard-depend on Homeostatic.

If Homeostatic is installed, it reads:

```text
data/<namespace>/environment/drinkable/*.json
```

If Homeostatic is absent, these files are inert.

## Homeostatic format

The current Homeostatic `26.1` source uses this item format:

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

Fields:

- `type`: item id.
- `amount`: water level change.
- `saturation`: water saturation change.
- `effect_potency`: Homeostatic thirst effect potency.
- `effect_duration`: Homeostatic thirst effect duration.
- `effect_chance`: chance to apply the thirst effect.

Homeostatic water bar reference:

- `MAX_WATER_LEVEL = 20`
- `MAX_SATURATION_LEVEL = 5.0`

## Important implementation note: no negative amount

Client testing showed that negative `amount` values can push Homeostatic's internal water level below zero. The HUD may later appear to recover partially while the internal value remains `<= 0`, causing dehydration damage to continue.

Therefore Herbcraft compatibility must not use negative `amount` values.

For drying or thirst-aggravating foods, use:

```json
{
  "amount": 0,
  "saturation": 0.0,
  "effect_potency": 45,
  "effect_duration": 200,
  "effect_chance": 0.5
}
```

or stronger chances/durations for severe cases.

## Round 1: Cuisine custom edible outputs

Implemented path:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/environment/drinkable/
```

Coverage:

- 33 Herbcraft Cuisine custom edible outputs.
- No raw dish items.
- No hodgepodge because Homeostatic item data cannot inspect `hodgepodge_contents`.
- No `minecraft:potion` because item-id matching would affect all potions.

Water bowl is intentionally not pure free water:

```json
{
  "type": "herbcraft_cuisine:water_bowl",
  "amount": 6,
  "saturation": 1.2,
  "effect_potency": 45,
  "effect_duration": 200,
  "effect_chance": 0.85
}
```

Some drink-like dishes are marked `can_always_eat` in `dishes.json` so they can hydrate even when the hunger bar is full:

- `pine_needle_soup`
- `blue_ice_brew`
- `seafarer_stew`
- `pine_needle_broth`
- `eye_opening_soup`
- `warped_swift_soup`
- `pale_night_soup`

Not every dish is always edible. Large meals, hodgepodge, feasts, and toxins should not become infinite buff triggers.

## Round 2: selected Core/vanilla Herbcraft edible items

Implemented path:

```text
core/src/main/resources/data/herbcraft/environment/drinkable/
```

Round 2 covers selected Herbcraft-modified vanilla edible items not already covered by Homeostatic's known generated vanilla data.

### Positive hydration examples

- `minecraft:snowball`
- `minecraft:ice`
- `minecraft:packed_ice`
- `minecraft:blue_ice`
- `minecraft:powder_snow_bucket`
- `minecraft:kelp`
- `minecraft:seagrass`
- `minecraft:sea_pickle`
- `minecraft:sugar_cane` (`amount 3`, `saturation 0.5`, no thirst-risk effect; validator-anchored)
- `minecraft:cactus_flower`
- `minecraft:melon`

Snowball is included because Herbcraft explicitly makes snowballs edible via short-tap throw / long-hold eat behavior.

### Thirst-risk examples

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
- toxic flowers/leaves/fungi

These use `amount: 0` plus Homeostatic thirst effect chance/duration/potency. They do not use negative amount.

## Homeostatic vanilla duplicates intentionally avoided

The current Homeostatic source already defines drinkable data for items such as:

- `minecraft:milk_bucket`
- `minecraft:honey_bottle`
- `minecraft:melon_slice`
- `minecraft:glistering_melon_slice`
- `minecraft:golden_carrot`
- `minecraft:apple`
- `minecraft:suspicious_stew`
- `minecraft:poisonous_potato`

Herbcraft does not duplicate these entries in its compatibility data.

## Validator

Run:

```bash
python3 scripts/validate_homeostatic_compat.py
```

The validator checks:

- required fields exist;
- no negative `amount` values;
- no raw cuisine items;
- no hodgepodge;
- no `minecraft:potion`;
- Cuisine entries are known edible Cuisine outputs;
- Core entries are known Herbcraft edible definitions;
- known Homeostatic vanilla drinkables are not duplicated;
- risk entries have meaningful thirst-effect data;
- water bowl values remain anchored;
- sugar cane remains anchored at `amount 3 / saturation 0.5 / effect_chance 0.0`.

## Future work

Potential future compatibility areas:

- Homeostatic temperature integration for ice/snow/hot foods.
- Optional hardcore nutrition addon that recommends or depends on Homeostatic.
- More granular handling if Homeostatic exposes a stable public Java API for direct water clamping or dehydration state control.
