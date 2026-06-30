# Round 3 Nature / Rumor Notes — 26.1.2 Food-Mod Compatibility Draft

Status: Round 3 completed as AI-reviewed draft; author review and real-client testing still required  
Date: 2026-06-17

## Input

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round2.csv
```

Target mods:

```text
Farmer's Delight Refabricated 26.1-3.6.5
More Delight 26.05.26-26.1-fabric
Reg's More Foods 1.4.1+26.1+mod
```

## Output

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv
```

Round 3 did not overwrite Round 2. It only revised the optional `herb_natures` / `rumor_key` columns and left the Round 2 nutrition and 食材录 section draft values in place.

## Counts

```text
Round 3 kept CSV entries: 317
nutrition_profiles preview: 317
ingredients preview: 317
herb_natures preview: 14
knowledge_rumors preview: 0
```

Compared with Round 2:

```text
Round 2 herb_natures: 18
Round 3 herb_natures: 14
Removed generic fruit/berry natures: 5
Added clearly botanical candidate: 1
Rumor keys: none
```

## Round 3 policy

Round 3 used this rule:

> 食材归食材，药性归药性。能吃，不等于该进百草药性表。

Most prepared foods and ordinary fruit entries remain 食材录 + nutrition only. Herb nature was kept only when the item identity is a raw crop, seed, staple grain, pungent bulb, or clearly botanical stimulant candidate.

No `knowledge_rumors` were added in this round. Reason:

- The current compat draft has no dedicated language files for rumor text keys.
- Low-value rumors would spam the Codex without teaching a system.
- The first public compat pack should let players learn these entries by tasting rather than by a wall of hearsay.

## Kept / adjusted Farmer's Delight nature rows

| Item | Round 3 nature | Reason |
|---|---|---|
| `farmersdelight:cabbage` | neutral; sweet/bland; leafy+nourishing | Mild leafy crop; foodlike, not strong medicine. |
| `farmersdelight:cabbage_leaf` | neutral; sweet/bland; leafy+nourishing | Same crop identity as cabbage. |
| `farmersdelight:cabbage_seeds` | neutral; bland; curio | Seeds now follow vanilla seed style instead of pretending to be leafy/nourishing. |
| `farmersdelight:onion` | warm; pungent/sweet; stirring+earthy | Pungent bulb; should feel warm and moving, not generic nourishing. |
| `farmersdelight:pumpkin_slice` | neutral; sweet/bland; curio | Close to vanilla pumpkin/melon style; mild curio rather than medicinal tonic. |
| `farmersdelight:rice` | neutral; sweet/bland; nourishing | Staple grain; mild nourishing profile. |
| `farmersdelight:tomato` | cool; sweet/sour; nourishing+clearing | Raw sour-sweet crop with a light clearing note. |
| `farmersdelight:tomato_seeds` | neutral; bland; curio | Seeds now follow vanilla seed style. |

## More Delight decision

More Delight's kept rows in Round 2 are almost all prepared foods: sandwiches, toast variants, soups, pasta, burgers, salads, and toppings.

Round 3 adds no More Delight herb nature rows. They remain 食材录 + nutrition only.

## Reg's More Foods decision

RMF remains highest risk because item identity must be tested in a real client. Round 3 therefore keeps nature data only for conservative raw crop analogues and one obvious botanical stimulant candidate.

Kept / adjusted:

| Item | Round 3 nature | Reason |
|---|---|---|
| `rmf:popcorn_seeds` | neutral; bland; curio | Seed-like item. |
| `rmf:pumpkin_slice` | neutral; sweet/bland; curio | Pumpkin analogue. |
| `rmf:rice` | neutral; sweet/bland; nourishing | Rice grain analogue. |
| `rmf:tomato` | cool; sweet/sour; nourishing+clearing | Tomato analogue. |
| `rmf:tomato_seeds` | neutral; bland; curio | Seed-like item. |
| `rmf:coffee_beans` | warm; bitter/pungent; stirring+earthy | Clearly botanical stimulant candidate; still needs client identity and nutrition/section review. |

Removed from herb nature but kept as 食材录 + nutrition:

| Item | Reason |
|---|---|
| `rmf:banana` | Generic fruit; vanilla-style fruits should not automatically become pharmacology entries. |
| `rmf:blackberry` | Generic berry; nutrition/食材录 is enough for the first pass. |
| `rmf:blueberry` | Generic berry; nutrition/食材录 is enough for the first pass. |
| `rmf:raspberry` | Generic berry; nutrition/食材录 is enough for the first pass. |
| `rmf:strawberry` | Generic berry; nutrition/食材录 is enough for the first pass. |

Deferred candidates, not added in Round 3:

```text
rmf:coffee
rmf:coffee_grounds
rmf:tea
rmf:iced_tea
rmf:taiga_beans
rmf:wild_carrot
farmersdelight:rotten_tomato
rmf:rotten_tomato
```

Reason: these are prepared drinks, processed intermediates, uncertain special foods, or spoilage/toxicity candidates. They may deserve design later, but not before author review and real-client identity testing.

## Generator validation preview

Round 3 CSV was smoke-tested with:

```bash
python3 scripts/generate_compat_pack.py \
  --items docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv \
  --output /tmp/herbcraft_round3_pack \
  --namespace food_mods_herbcraft_round3 \
  --pack-name "Herbcraft Compat Draft Round 3"
```

Result:

```text
nutrition_profiles/generated.json 317
ingredients/generated.json 317
herb_natures/generated.json 14
knowledge_rumors/generated.json 0
```

This was only a temporary validation preview. The checked-in `generated_pack/` directory has not been regenerated as a release candidate in Round 3.

## Remaining caveats

- Round 3 is still an AI-reviewed draft, not an author-approved balance table.
- Round 2 nutrition and section values remain draft values.
- `rmf:*` IDs still require real-client confirmation.
- Do not publish a compat pack before Round 5.

## Next round

Round 4 should regenerate the draft compat pack from:

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv
```

Suggested command:

```bash
python3 scripts/generate_compat_pack.py \
  --items docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv \
  --output docs/compat_drafts/food_mods_26_1_2/generated_pack \
  --namespace food_mods_herbcraft \
  --pack-name "Herbcraft Compat Draft: 26.1.2 Food Mods"
```

Then validate generated JSON shape and prepare for Round 5 real-client testing.
