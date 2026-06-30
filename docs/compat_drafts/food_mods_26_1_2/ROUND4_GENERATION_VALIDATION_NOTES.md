# Round 4 Regeneration / Validation Notes — 26.1.2 Food-Mod Compatibility Draft

Status: Round 4 completed as generated three-mod draft; superseded by Round 5 final Farmer's Delight + More Delight pack  
Date: 2026-06-17

> Post-Round-5 note: this document records the historical three-mod generation pass. After client testing, RMF was removed and `generated_pack/` was regenerated as Farmer's Delight + More Delight only. See `ROUND5_CLIENT_TEST_REPORT.md`.

## Input

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv
```

## Generated output

Round 4 regenerated the checked-in draft datapack directory:

```text
docs/compat_drafts/food_mods_26_1_2/generated_pack/
```

Command used:

```bash
rm -rf docs/compat_drafts/food_mods_26_1_2/generated_pack
python3 scripts/generate_compat_pack.py \
  --items docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv \
  --output docs/compat_drafts/food_mods_26_1_2/generated_pack \
  --namespace food_mods_herbcraft \
  --pack-name "Herbcraft Compat Draft: 26.1.2 Food Mods"
```

Generated files:

```text
docs/compat_drafts/food_mods_26_1_2/generated_pack/README.md
docs/compat_drafts/food_mods_26_1_2/generated_pack/pack.mcmeta
docs/compat_drafts/food_mods_26_1_2/generated_pack/data/food_mods_herbcraft/herbcraft/nutrition_profiles/generated.json
docs/compat_drafts/food_mods_26_1_2/generated_pack/data/food_mods_herbcraft/herbcraft/ingredients/generated.json
docs/compat_drafts/food_mods_26_1_2/generated_pack/data/food_mods_herbcraft/herbcraft/herb_natures/generated.json
```

No `knowledge_rumors/generated.json` was generated because Round 3 intentionally left all `rumor_key` fields blank.

## Counts

```text
CSV rows: 317
nutrition_profiles: 317
ingredients: 317
herb_natures: 14
knowledge_rumors: 0
```

Source distribution remains:

```text
Farmer's Delight Refabricated: 82
More Delight: 29
Reg's More Foods: 206
```

## Validation performed

A local JSON/CSV consistency check was run after generation. It verified:

- all generated JSON files parse;
- `schema_version` is `1`;
- nutrition dimensions are valid and positive;
- ingredient sections are one of `meat`, `seafood`, `grain`, `fruit`, `soup`, `special`;
- herb nature temperatures/flavors/traits are within Herbcraft's current allowed sets;
- generated nutrition item set equals Round 3 CSV nutrition rows;
- generated ingredient item set equals Round 3 CSV ingredient rows;
- generated herb nature item set equals Round 3 CSV nature rows;
- no duplicate item IDs exist inside each generated file;
- no non-empty knowledge rumor output exists.

Result:

```text
ROUND4 GENERATED PACK CHECK OK
nutrition_profiles: 317
ingredients: 317
herb_natures: 14
knowledge_rumors: 0
```

The external extension spec validator was also run:

```bash
python3 scripts/validate_external_extension_spec.py
```

Result: passed.

## Chinese review file

The author requested a Chinese-readable Markdown table for names and properties. It was added as:

```text
docs/compat_drafts/food_mods_26_1_2/ROUND4_CHINESE_NATURE_TABLE.md
```

It lists all 14 generated herb nature rows with:

```text
中文名
模组
物品 ID
食材录分类
营养草案
性
性味
入口味
特质
审阅说明
```

It also includes a Chinese glossary for all current Herbcraft nature temperatures, flavors, and traits.

## Important caveats

- This generated pack is still a draft, not a release artifact.
- Round 2 nutrition/section values remain draft values. Some RMF entries, especially `rmf:coffee_beans`, still need author/client review for section and nutrition fit.
- Reg's More Foods item identity remains the largest technical risk because the project has both datapack/resource-pack and modded wrapper forms.
- Do not publish this pack before Round 5 real-client testing.

## Next round

Round 5 should test the generated pack in a real client with:

```text
Minecraft 26.1.2
Fabric Loader 0.19.3+
Fabric API 0.151.0+26.1.2+
Herbcraft Core/Cuisine/Alchemy 1.1.2
Farmer's Delight Refabricated 26.1-3.6.5
More Delight 26.05.26-26.1-fabric
Reg's More Foods 1.4.1+26.1+mod
Generated Herbcraft compat datapack from Round 4
```

Test focus:

- datapack loads without warnings/errors;
- external 食材录 entries appear after eating;
- nutrition profiles affect Herbcraft nutrition state;
- herb nature rows sync to client and show after the appropriate knowledge/taste gates;
- no stale Round 1 nature rows remain;
- RMF `rmf:*` item IDs actually match real client items;
- no crash on login, `/reload`, eating, tooltip display, or Codex open.
