# Round 5 Real-Client Test Checklist — Farmer's Delight + More Delight Final Scope

Status: author client test passed for Farmer's Delight + More Delight; RMF removed  
Date: 2026-06-17

This checklist is the final post-RMF-removal sanity list for the current generated compat pack.

## 0. Test artifact

Folder datapack:

```text
docs/compat_drafts/food_mods_26_1_2/generated_pack/
```

Reference folder datapack only. After `1.1.3`, Farmer+More compatibility is built into Core; do not publish a standalone datapack as the old-jar route because older jars lack the food-nature bridge.

## 1. Required mods

```text
Minecraft 26.1.2
Fabric Loader 0.19.3+
Fabric API 0.151.0+26.1.2+
Herbcraft Core 1.1.3
Herbcraft Cuisine 1.1.3, optional but recommended for full stack smoke
Herbcraft Alchemy 1.1.3, optional but recommended for full stack smoke
Farmer's Delight Refabricated 26.1-3.6.5
More Delight 26.05.26-26.1-fabric
Round 5 Farmer's Delight + More Delight built-in Core compatibility
```

Excluded after client test:

```text
Reg's More Foods 1.4.1+26.1+mod
```

Reason: RMF is datapack-driven and can collide with vanilla/Herbcraft-known item identity. It is intentionally not in the final generated pack.

## 2. Load test

1. Create or open a test world.
2. Put the zipped datapack into the world's `datapacks/` folder, or enable the folder datapack before world creation.
3. Run:

```mcfunction
/datapack list
/reload
/datapack list
```

Expected:

- The datapack appears enabled.
- `/reload` completes without crash.
- Logs do not show Herbcraft JSON parse errors for:
  - `nutrition_profiles`
  - `ingredients`
  - `herb_natures`

## 3. 食材录 / nutrition smoke test

Suggested Farmer's Delight items:

```text
farmersdelight:cabbage
farmersdelight:onion
farmersdelight:tomato
farmersdelight:cooked_rice
farmersdelight:cooked_rice
farmersdelight:bacon_sandwich
farmersdelight:beef_stew
farmersdelight:apple_cider
```

Suggested More Delight items:

```text
moredelight:toast
moredelight:toast_with_blueberries
moredelight:tomato_sandwich
moredelight:carrot_soup
moredelight:loaded_hamburger
```

Expected:

- No crash on eating.
- Entries that exist as real runtime item IDs record 食材录 knowledge after eating.
- Nutrition values apply when the item's runtime ID matches the generated profile.
- Tooltip/Codex behavior remains stable.

Author reported this pass as successful for the sampled items.

## 4. Final herb nature rows

The final generated pack contains 6 `herb_natures` rows, all from Farmer's Delight. Non-edible seed rows were removed:

```text
farmersdelight:cabbage
farmersdelight:cabbage_leaf
farmersdelight:onion
farmersdelight:pumpkin_slice
farmersdelight:cooked_rice
farmersdelight:tomato
```

Use this table for Chinese review:

```text
docs/compat_drafts/food_mods_26_1_2/ROUND5_FINAL_CHINESE_NATURE_TABLE.md
```

Current Herbcraft `1.1.3` note: external `herb_natures` rows are valid metadata and sync to clients. Ordinary third-party foods do not become full startup `herbs` entries unless explicitly registered through the startup-only `herbs` path, but foods with nutrition + 食材录 + herb_natures can use the food-nature bridge and appear in 药食志.

## 5. Coffee bean deferred note

Coffee beans were removed because they only appeared through RMF in this batch.

Future design, if a stable real mod provides coffee beans:

```text
咖啡豆可视作加工过的可可豆。以后可以考虑：
- 比原版可可豆副作用低一些；
- 正面 buff 时间更长一些；
- 是否给类似急迫的效果；
- 或借“苦味”走镇缓 / 收束方向。
```

Do not implement this by Java hardcoding.

## 6. Pass criteria

The final Farmer's Delight + More Delight compat pack can be treated as a release candidate if:

- the datapack loads;
- `/reload` succeeds;
- sampled foods record 食材录 knowledge;
- nutrition applies;
- tooltip/Codex screens do not crash;
- final zip contains no `rmf:*` IDs;
- the author accepts the final Chinese nature table.

Author has already reported the key client pass for Farmer's Delight + More Delight. One optional final fresh-world sanity check after public renaming is still recommended before upload.
