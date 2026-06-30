# Hardcore Nutrition Addon Plan

This document records the recommended architecture for a future Herbcraft addon tentatively called “Hardcore Nutrition”.

## Recommendation

Do **not** make a second independent core.

Make it an addon that depends on Herbcraft Core.

Suggested module identity:

- Mod name: `Herbcraft: Hardcore Nutrition` / `百草可食：硬核营养学`
- Mod ID suggestion: `herbcraft_hardcore_nutrition`
- Required dependency: `herbcraft >=1.1.0`
- Optional dependency: `herbcraft_cuisine` if it wants to rebalance cuisine recipes directly.

## Why addon, not separate core?

Herbcraft Core already owns:

- player nutrition state,
- five base nutrition dimensions,
- custom nutrition effects,
- nutrition profile parsing,
- nutrition tooltips,
- debug commands,
- water bowl dietary-imbalance relief,
- cuisine/nutrition effect isolation.

A second nutrition core would duplicate state and create conflicts:

- two nutrition tick systems,
- two tooltip systems,
- two effect systems,
- unclear ownership over player data,
- harder compatibility with cuisine/alchemy.

An addon can instead extend and harden the existing system.

## What the addon should add

The first new dimension you mentioned is cocoa fat / 可可脂.

Potential internal dimension key:

```text
cocoa_fat
```

Possible display names:

- CN: `可可脂`
- EN: `Cocoa Fat`

Possible mechanics:

- deficiency: lower stamina-like performance or increase exhaustion;
- excess: dietary imbalance or mild sluggishness;
- well-balanced interaction: optional enhancement to `well_nourished` if cocoa_fat is also in range.

## Required Core changes before this addon is clean

Currently, Herbcraft Core still has a fixed five-dimension array in `NutritionProfile.DIMENSIONS`.

For a true sixth dimension addon, Core should eventually expose a dimension registry/data layer, for example:

```json
// data/herbcraft/nutrition_dimensions.json
{
  "dimensions": [
    { "id": "grain", "name_key": "nutrition.herbcraft.dimension.grain" },
    { "id": "flesh", "name_key": "nutrition.herbcraft.dimension.flesh" },
    { "id": "oil", "name_key": "nutrition.herbcraft.dimension.oil" },
    { "id": "vege", "name_key": "nutrition.herbcraft.dimension.vege" },
    { "id": "fruit", "name_key": "nutrition.herbcraft.dimension.fruit" }
  ]
}
```

Then the addon could append:

```json
{ "id": "cocoa_fat", "name_key": "nutrition.herbcraft.dimension.cocoa_fat" }
```

Until that exists, adding a sixth dimension requires modifying Core code.

## Short-term addon option

If you do not want to refactor Core dimensions yet, the addon can still be useful by changing existing data:

- stricter `nutrition_rules.json`,
- harsher `nutrition_effects.json`,
- adjusted `nutrition_profiles.json`,
- extra food/cuisine profiles,
- extra effects that attach to existing dimensions.

But it cannot cleanly add `cocoa_fat` as a real sixth tracked dimension without Core support.

## Recommended implementation sequence

1. Core preparation pass:
   - externalize nutrition dimension definitions;
   - make `NutritionProfile.DIMENSIONS` data-backed or registry-backed;
   - update tooltips, panel, state save/load, debug commands, and validators to iterate dynamic dimensions.
2. Addon skeleton:
   - mod depends on `herbcraft`;
   - supplies `nutrition_dimensions` entry for `cocoa_fat`;
   - supplies stricter nutrition rules/effects/profiles.
3. Addon balance pass:
   - faster decay;
   - stronger severe deficiency penalties;
   - harsher excess/imbalance behavior;
   - optional new hardcore-only effects.
4. Client UX pass:
   - nutrition panel must render six or more dimensions cleanly;
   - tooltips must remain readable;
   - debug commands must handle addon dimensions.

## Design boundary

Main Herbcraft should stay gentle. Hardcore Nutrition should be the opt-in strict system.

This keeps the main mod friendly while allowing a future pack/modpack to choose a harsher nutrition model.
