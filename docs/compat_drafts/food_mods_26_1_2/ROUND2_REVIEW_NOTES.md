# Round 2 Review Notes — 26.1.2 Food-Mod Compatibility Draft

Status: Round 2 completed as AI-reviewed draft; author review still required  
Date: 2026-06-16

## Input

```text
compat_review_draft.csv
item_ids_audit.csv
```

Target mods:

```text
Farmer's Delight Refabricated 26.1-3.6.5
More Delight 26.05.26-26.1-fabric
Reg's More Foods 1.4.1+26.1+mod
```

## Output

```text
compat_review_round2.csv
round2_removed_false_positives.csv
```

Round 2 did not overwrite `compat_review_draft.csv`, so the original heuristic draft remains available for comparison.

## Round 2 counts

```text
Round 1 draft candidates: 396
Round 2 kept entries: 317
Round 2 removed entries: 79
```

Kept by source:

```text
Farmer's Delight Refabricated: 82
More Delight: 29
Reg's More Foods: 206
```

Removed by source:

```text
Farmer's Delight Refabricated: 17
More Delight: 0
Reg's More Foods: 62
```

Sections after review:

```text
grain: 93
soup: 81
meat: 64
fruit: 48
special: 22
seafood: 9
```

Generated-data preview if using Round 2 CSV:

```text
nutrition_profiles: 317
ingredients: 317
herb_natures: 18
```

## Removal rules applied

Entries were removed when they were obviously not appropriate as player-facing Ingredient Codex food entries:

- debug/admin entries;
- tooltip/description helper keys;
- pseudo localization subkeys containing `.` rather than stable item IDs;
- model-only entries with blank English names;
- tools, containers, cabinets, signs, crates, pans, knives, UI/model state entries;
- decorative/non-food items;
- animal food such as dog/cat food;
- Farmer's Delight wild crop blocks;
- Farmer's Delight placeable feast/block forms when slice/serving entries exist.

The full removal list is in:

```text
round2_removed_false_positives.csv
```

## Nutrition review approach

Round 2 replaces the first heuristic values with more conservative, vanilla-scaled draft values. It uses current Herbcraft scale references such as:

```text
minecraft:bread              grain 36
minecraft:cooked_beef        flesh 41, oil 16
minecraft:apple              fruit 23, vege 5
minecraft:carrot             vege 17, fruit 4
minecraft:pumpkin_pie        grain 33, fruit 16, oil 8
minecraft:milk_bucket        flesh 14, oil 10
minecraft:mushroom_stew      vege 30, grain 13
```

General Round 2 rules:

- raw crop ingredients are lower than full meals;
- raw meat cuts are lower than cooked meat;
- soups/drinks include more `fruit` / Fluids but should not automatically become huge food;
- sandwiches/burgers/wraps are mixed nutrition, usually grain + flesh + oil + vege;
- sweets and pies get grain + fruit + oil;
- salads lean vege + fruit with a little oil;
- RMF remains marked as identity-risk until client-tested.

## Herb nature policy

Round 2 adds/keeps herb nature only for a small subset of raw plant/crop-like ingredients. Most prepared foods remain Ingredient Codex + nutrition only.

Current Round 2 nature entries: 18.

Examples:

```text
cabbage / cabbage_leaf: sweet;bland, leafy;nourishing
tomato: sweet;sour, nourishing
onion: pungent;sweet, nourishing;stirring
rice: sweet;bland, nourishing
seeds: bland, curio
selected RMF raw fruits/crops: sweet, nourishing
```

These are still draft values and should be reviewed before publishing.

## Reg's More Foods warning

RMF is kept in scope because the author requested all three target mods, but it remains highest risk.

Reason:

- It has a datapack/resource-pack distribution as well as a modded wrapper.
- The project description/wiki notes that datapack food identity can behave unusually in recipe viewers because many datapack items may share underlying namespaced IDs.
- The Round 2 CSV uses item-like IDs visible in the modded wrapper assets/lang, but these must be checked in a real client.

All RMF kept rows are marked:

```text
review_status = rmf_identity_risk_review
```

## Next round

Round 3 should focus on herb nature and rumor decisions:

1. Review the 18 current herb nature rows.
2. Decide whether any additional raw plant/spice-like items deserve nature data.
3. Remove nature data from anything that feels too speculative.
4. Decide whether any rumors are worth adding, or leave rumor fields blank for the first compat release.

Do not publish this compat pack before Round 5 client testing.
