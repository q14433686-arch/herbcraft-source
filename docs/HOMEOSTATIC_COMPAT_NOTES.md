> Historical research note (2026-06-16): this file records the first Homeostatic source-reading pass. Current implemented compatibility is documented in `docs/HOMEOSTATIC_COMPAT.md`; negative `amount` values are forbidden, and sugar cane hydration is currently anchored at `amount 3`.

# Homeostatic compatibility notes

Research date: 2026-06-14

Target mod: Homeostatic by wendall911

- Modrinth indicates Homeostatic is a temperature and thirst mod available for modern Minecraft versions including the 26.1.x line.
- Source repository: `https://github.com/wendall911/Homeostatic`, branch `26.1`.
- License shown on Modrinth/GitHub: MIT.

## What the current Homeostatic source appears to expose

The current 26.1 source uses data-driven drinkable item/fluid definitions.

Relevant source files inspected locally from the `26.1` branch:

- `Common/src/main/java/homeostatic/common/item/DrinkableItem.java`
- `Common/src/main/java/homeostatic/common/item/DrinkableItemManager.java`
- `Common/src/main/java/homeostatic/common/fluid/DrinkingFluid.java`
- `Common/src/main/java/homeostatic/common/fluid/DrinkingFluidManager.java`
- generated examples under `Common/src/generated/resources/data/*/environment/drinkable/`

Drinkable item JSON shape:

```json
{
  "type": "minecraft:milk_bucket",
  "amount": 9,
  "effect_chance": 0.0,
  "effect_duration": 0,
  "effect_potency": 0,
  "saturation": 2.1
}
```

Fields observed in `DrinkableItem`:

- `type`: item id
- `amount`: thirst/hydration amount
- `saturation`: hydration saturation
- `effect_potency`: thirst effect potency for risky/dirty drinks
- `effect_duration`: thirst effect duration
- `effect_chance`: chance to apply the thirst effect

Homeostatic loads drinkable item JSONs from:

```text
data/<namespace>/environment/drinkable/*.json
```

## Suggested Herbcraft integration strategy

Do not add a hard dependency on Homeostatic.

Instead, add optional data files in the relevant Herbcraft module if/when compatibility is desired:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/environment/drinkable/water_bowl.json
cuisine/src/main/resources/data/herbcraft_cuisine/environment/drinkable/blue_ice_brew.json
cuisine/src/main/resources/data/herbcraft_cuisine/environment/drinkable/seafarer_stew.json
```

Example for water bowl:

```json
{
  "type": "herbcraft_cuisine:water_bowl",
  "amount": 4,
  "saturation": 0.8,
  "effect_potency": 0,
  "effect_duration": 0,
  "effect_chance": 0.0
}
```

Recommended design rules:

- Water bowl should hydrate, but not as strongly as milk bucket if Homeostatic gives milk bucket amount 9 / saturation 2.1.
- Soups/stews can have modest hydration depending on recipe identity.
- Dirty/risky thirst effects should not be added to clean Herbcraft cuisine unless the dish is explicitly toxic or questionable.
- This integration can be shipped as optional data. If Homeostatic is absent, the files should be harmless because only Homeostatic reads `environment/drinkable`.

## Possible future validator

Add a script such as:

```text
scripts/validate_homeostatic_compat.py
```

Checks:

- Every Homeostatic drinkable JSON has `type`, `amount`, `saturation`, `effect_potency`, `effect_duration`, `effect_chance`.
- Every `type` item exists in Herbcraft/Cuisine resources.
- Values are conservative and do not exceed known vanilla examples without intent.

## Important uncertainty

This is based on the Homeostatic `26.1` branch source inspected in this environment. Before implementing release-grade compatibility, re-check the exact Homeostatic version targeted by the pack and run with Homeostatic installed.
