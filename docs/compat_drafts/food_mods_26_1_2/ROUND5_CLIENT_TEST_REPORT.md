# Round 5 Client Test Report — 26.1.2 Food-Mod Compatibility Draft

Status: **client-tested PASS for Farmer's Delight Refabricated + More Delight; RMF removed; built-in Core integration implemented after this report**  
Date: 2026-06-17

> Post-report update: Farmer+More compatibility is now also bundled into Herbcraft Core as optional data, and a generic food-nature pharmacology bridge was added. See `docs/reports/FARMER_MORE_BUILTIN_COMPAT_AND_NATURE_BRIDGE_20260617.md`.

## Summary

The author completed the real-client test pass after the Round 5 preflight package was prepared.

Client result reported by the author:

```text
Farmer's Delight Refabricated: PASS
More Delight: PASS
Reg's More Foods: remove from scope
No objective crashes or functional failures for the tested Farmer's Delight / More Delight items.
All registered foods tested by the author were recorded in the Herbcraft Codex / 食材录 flow.
RMF did not crash, but its datapack-driven identity model causes item-ID/vanilla-item collision and lookup risk.
```

Round 5 decision:

```text
Keep Farmer's Delight Refabricated + More Delight.
Delete Reg's More Foods from the generated compat pack.
Do not attempt to rebuild RMF into a real mod from Herbcraft's side.
```

## Final generated artifact

Final generated folder datapack:

```text
docs/compat_drafts/food_mods_26_1_2/generated_pack/
```

Final standalone datapack decision:

```text
No standalone public datapack for old jars. The generated datapack remains under docs/compat_drafts/.../generated_pack/ as reference/example material only because older jars lack the food-nature bridge.
```

The older three-mod Round 5 draft zip was removed from `release_output/` to avoid accidental use.

## Final scope

Included:

```text
Farmer's Delight Refabricated 26.1-3.6.5
More Delight 26.05.26-26.1-fabric
```

Excluded:

```text
Reg's More Foods 1.4.1+26.1+mod
```

Reason for exclusion:

RMF behaves like a datapack-driven food set rather than a conventional mod adding stable independent item IDs. It can rewrite existing item forms and collide with vanilla/Herbcraft-known item identity. The result is not a crash, but it is unreliable and low-value for a Herbcraft compatibility pack. Herbcraft should not compensate by reconstructing RMF as a real mod.

## Final counts

```text
CSV source rows: 108
Farmer's Delight rows: 79
More Delight rows: 29
RMF rows: 0
nutrition_profiles: 108
ingredients: 108
herb_natures: 5
knowledge_rumors: 0
```

Final source CSV:

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round5_farmer_moredelight.csv
```

Final Chinese nature table:

```text
docs/compat_drafts/food_mods_26_1_2/ROUND5_FINAL_CHINESE_NATURE_TABLE.md
```

## Validation completed after RMF removal

The generated pack was regenerated from the filtered Round 5 CSV with namespace:

```text
farmers_moredelight_herbcraft
```

Generated paths:

```text
docs/compat_drafts/food_mods_26_1_2/generated_pack/pack.mcmeta
docs/compat_drafts/food_mods_26_1_2/generated_pack/data/farmers_moredelight_herbcraft/herbcraft/nutrition_profiles/generated.json
docs/compat_drafts/food_mods_26_1_2/generated_pack/data/farmers_moredelight_herbcraft/herbcraft/ingredients/generated.json
docs/compat_drafts/food_mods_26_1_2/generated_pack/data/farmers_moredelight_herbcraft/herbcraft/herb_natures/generated.json
```

Validation confirmed:

- all generated JSON files parse;
- `schema_version` is `1`;
- nutrition dimensions are valid;
- ingredient sections are valid;
- herb nature values are valid;
- no duplicate item IDs exist inside generated files;
- no `rmf:*` IDs remain in the final CSV, generated folder, or final zip;
- generated item sets match `compat_review_round5_farmer_moredelight.csv`;
- no rumor file is generated because all `rumor_key` fields are blank.

`python3 scripts/validate_external_extension_spec.py` passed.

## Coffee bean note

Coffee beans were part of the RMF draft and are no longer in the final compat pack.

Author design note retained for future use if a stable real mod provides coffee beans:

```text
咖啡豆可视作加工过的可可豆。以后可以考虑：
- 比原版可可豆副作用低一些；
- 正面 buff 时间更长一些；
- 是否给类似急迫的效果；
- 或借“苦味”走镇缓 / 收束方向。
```

No coffee-bean mechanic or balance change was implemented in Round 5.

## Non-edible seed correction

The final data now removes:

```text
farmersdelight:cabbage_seeds
farmersdelight:tomato_seeds
```

Reason: Farmer's Delight does not make these seed items edible. If they remain in 食材录 or 百草志, players can never truly taste or master them. We chose removal over forcing new FOOD/CONSUMABLE components onto target-mod seed items.

## Publish decision

Current decision:

```text
Farmer's Delight + More Delight compat pack: release candidate / author may publish after final naming/packaging review.
Reg's More Foods compat: rejected for this pack; do not publish in the main compat pack.
```

Historical standalone datapack wording, not the chosen 1.1.3 route:

```text
This datapack adds Herbcraft 食材录 / nutrition compatibility for Farmer's Delight Refabricated and More Delight on Minecraft 26.1.2. It intentionally does not include Reg's More Foods because RMF's datapack-driven item identity is not stable enough for this compatibility layer.
```

Suggested public wording if shipped through refreshed Herbcraft Core:

```text
Herbcraft Core now includes optional data-driven compatibility for Farmer's Delight Refabricated and More Delight: 食材录 nutrition entries plus selected 药食志 nature records. Reg's More Foods is intentionally excluded because its datapack-driven item identity is not stable enough for this layer.
```

## Remaining optional polish before upload

- Decide final public filename and display name.
- Optionally make a short release note in Chinese/English.
- Optionally re-check the final zip in a fresh world once after renaming, to avoid uploading the wrong artifact.
