# Nutrition Correction Tuning / Relationship Matrix Proposal

Date: 2026-06-15  
Scope: discussion + source review + numeric tuning proposal.  
Status: **no gameplay code changed in this turn**

## Author feedback recorded

The author reports that the current nutrition correction implementation works and has no obvious bugs, but the current correction pace is too slow. The author also clarified that correction should not be generic "any non-high dimension can correct any high dimension"; it should use a nutrition relationship matrix:

```text
- 谷粮 / grain = carbohydrates / staple energy.
- 血肉 / flesh = protein / meat.
- 膏脂 / oil = fat.
- 菜蔬 / vege = vegetables/fiber/micronutrients.
- 津液 / fruit = fluids/vitamins; low is bad, high is beneficial/non-punitive in base mod.
```

## Source facts checked

Current implementation in `PlayerNutritionState.tickCorrectionDecay(...)`:

```text
correction_extra_decay_interval_ticks = 200 ticks = 10 seconds
correction_extra_decay_min = 4
correction_extra_decay_max = 12
correction_target_value = 650
```

Current formula in `correctionDecayAmount(...)`:

```text
extraDecay = clamp(4, 12, 4 + correctionScore / 40)
```

Current correction score is generic:

```text
For high dimension H:
  corrective = sum(profile[d] where d != H and currentValue[d] < 650)
  highContribution = profile[H]
  meal contributes only if corrective > highContribution
```

This prevents "more of the same" from self-correcting, but it does not yet encode the author's desired dimension relationship matrix.

## Current timing calculation

From 1000 down to 650 requires removing:

```text
350 points
```

Natural decay is still only:

```text
2 per 60 seconds
```

Current correction timing:

| Correction score | Current extra decay / 10s | Total approx decay / min | 1000 -> 650 time |
|---:|---:|---:|---:|
| 60 | 5 | 32/min | 10.9 min |
| 110 | 6 | 38/min | 9.2 min |
| 160 | 8 | 50/min | 7.0 min |
| 240 | 10 | 62/min | 5.6 min |
| 300 | 11 | 68/min | 5.1 min |
| max cap | 12 | 74/min | 4.7 min |

Conclusion:

```text
The author's complaint is numerically correct. Even max-strength correction takes about 4.7 minutes from 1000 to 650, and ordinary correction can take 7-11 minutes.
```

## Tuning options for speed

### Option A: tick every second

If correction applies every second:

| Extra decay / second | Approx decay / min | 1000 -> 650 time |
|---:|---:|---:|
| 1 | 62/min | 5.6 min |
| 2 | 122/min | 2.9 min |
| 3 | 182/min | 1.9 min |
| 4 | 242/min | 1.4 min |
| 5 | 302/min | 1.2 min |

Pros:

```text
Smooth, responsive, easy to feel.
```

Cons:

```text
More frequent updates and sync pressure; not huge, but unnecessary if 10-second chunks are acceptable.
```

### Option B: keep 10 seconds but make each correction stronger

| Extra decay / 10s | Approx decay / min | 1000 -> 650 time |
|---:|---:|---:|
| 15 | 92/min | 3.8 min |
| 20 | 122/min | 2.9 min |
| 25 | 152/min | 2.3 min |
| 30 | 182/min | 1.9 min |
| 40 | 242/min | 1.4 min |
| 50 | 302/min | 1.2 min |

Pros:

```text
Simple and stable.
Less frequent state churn.
Easy to tune by JSON.
```

Cons:

```text
Feels more step-like: value drops in visible chunks every 10 seconds.
```

### Recommended timing model

For the next tuning implementation, prefer keeping 10-second ticks but raising the range:

```json
"correction_extra_decay_interval_ticks": 200,
"correction_extra_decay_min": 15,
"correction_extra_decay_max": 45
```

Then compute weighted relationship score into a multiplier/tier so typical results land here:

| Strength | Intended case | Extra / 10s | 1000 -> 650 time |
|---|---|---:|---:|
| weak | a few low-content foods | 15 | 3.8 min |
| medium | one decent corrective meal | 25 | 2.3 min |
| strong | curated dish / strong target match | 35 | 1.65 min |
| feast/hidden | high-value rare dish | 45 | 1.29 min |

This is much closer to survival pacing without making correction instant.

If the author wants smoother visual feedback, use the equivalent 5-second version later:

```text
8 / 13 / 18 / 23 every 5 seconds
```

## Proposed correction relationship matrix

The next implementation should replace the current generic scoring with a data-backed correction matrix.

Suggested new data file:

```text
data/herbcraft/nutrition_correction_rules.json
```

or new section in:

```text
data/herbcraft/nutrition_rules.json
```

A separate file is cleaner.

### Proposed matrix, first draft

Weights mean how much a dimension contributes when correcting an excessive target dimension.

| Excess dimension | Primary correctors | Secondary correctors | Not useful / nearly no correction |
|---|---|---|---|
| `grain` / 谷粮 / carbs | `vege 1.35`, `flesh 1.15` | `oil 0.45`, `fruit 0.35` | — |
| `flesh` / 血肉 / protein | `vege 1.60` | `fruit 0.80` | `grain 0`, `oil 0` |
| `oil` / 膏脂 / fat | `vege 1.45` | `fruit 0.80`, optional `grain 0.25` | `flesh 0` |
| `vege` / 菜蔬 / vegetable/fiber | `grain 1.20`, `flesh 1.20` | `oil 0.50`, `fruit 0.40` | — |
| `fruit` / 津液 / fluids | no punitive correction target | — | high fruit is non-punitive |

### Rationale

#### Excess grain / 谷粮

Carbs/staple excess should be corrected primarily by:

```text
菜蔬 + 血肉
```

because these shift the current diet away from pure energy/starch. Oil and fluids can help a little, but should not dominate.

#### Excess flesh / 血肉

Protein/meat excess should be corrected by:

```text
菜蔬 strongly
津液 moderately
```

Grain should not correct flesh because both can function as energy sources and do not counter each other meaningfully in this design.

#### Excess oil / 膏脂

Fat excess should be corrected by:

```text
菜蔬 strongly
津液 moderately
```

Grain is uncertain. If included, keep it weak (`0.25`) because staple/carbs can dilute a fat-heavy diet but should not be the main answer.

#### Excess vege / 菜蔬

Vegetable/fiber excess should be corrected mainly by:

```text
谷粮 + 血肉
```

Oil and fluids can help slightly, but less strongly.

#### High fruit / 津液

Base-mod policy remains:

```text
low fruit is bad;
high fruit is not punished;
high fruit can help normalize other excesses.
```

## Special high-津液 bonus rule

Author proposal: when 津液 itself is excessive, it should help gradually normalize other excessive nutrition values.

Recommended rule:

```text
If fruit >= 850:
  For each non-fruit dimension above 850:
    apply a mild automatic normalization toward 700, not 650.
```

Suggested numeric starting point:

```json
"fluid_surplus_threshold": 850,
"fluid_surplus_target_value": 700,
"fluid_surplus_decay_interval_ticks": 200,
"fluid_surplus_decay_amount": 10
```

Timing from 1000 to 700:

```text
300 points / (10 every 10s + natural 2/min) ≈ 4.8 minutes
```

If too slow:

```text
15 every 10s => about 3.3 minutes
20 every 10s => about 2.5 minutes
```

Recommended first value:

```text
10 every 10s toward 700
```

because the author described this as a benefit of high 津液, but still "缓慢".

## Proposed scoring formula with matrix

For each excessive dimension `H`, recent meal correction score becomes:

```text
score(H, meal) = sum(profile[d] * matrix[H][d] for d != H)
```

Still keep the anti-self-correction rule:

```text
score(H, meal) must be greater than profile[H] * self_penalty_weight
```

Suggested simple rule:

```text
score(H, meal) > profile[H]
```

Or softer:

```text
score(H, meal) > profile[H] * 0.75
```

Use strict `>` first to avoid same-food correction abuse.

## Proposed decay formula after matrix score

Replace current:

```text
4 + score / 40, clamped 4..12
```

with:

```text
15 + score / 12, clamped 15..45
```

Examples:

| Matrix score | Extra / 10s | 1000 -> 650 time |
|---:|---:|---:|
| 60 | 20 | 2.9 min |
| 110 | 24 | 2.4 min |
| 160 | 28 | 2.1 min |
| 240 | 35 | 1.65 min |
| 360+ | 45 cap | 1.29 min |

This preserves "more corrective nutrition = faster recovery" while staying under about 1.3 minutes even for very strong dishes.

## Example using proposed matrix

### High grain = 1000, target 650

Correctors:

```text
vege 1.35, flesh 1.15, oil 0.45, fruit 0.35
```

Examples:

| Food | Raw profile | Approx matrix score | Approx time |
|---|---|---:|---:|
| carrot | vege 29 + fruit 7 | 41.6 | below threshold alone |
| bread | grain 60 | 0 | not corrective for grain |
| cooked beef | flesh 69 + oil 27 | 91.5 | about 2.6 min if threshold met |
| dandelion salad | vege 80 + fruit 30 | 118.5 | about 2.3 min |
| hundred flower feast | vege 140 + fruit 120 + oil 60 + grain 50 | corrective ≈ 258, but self grain 50 | about 1.5 min |

### High flesh = 1000, target 650

Correctors:

```text
vege 1.60, fruit 0.80
```

Examples:

| Food | Raw profile | Approx matrix score | Approx time |
|---|---|---:|---:|
| carrot | vege 29 + fruit 7 | 52 | below threshold alone, close |
| two carrots | about 104 | about 2.4 min |
| bread | grain 60 | 0 | not corrective |
| dandelion salad | vege 80 + fruit 30 | 152 | about 2.1 min |
| hundred flower feast | vege 140 + fruit 120 + oil 60 + grain 50 | 320 | about 1.3-1.5 min |

### High oil = 1000, target 650

Correctors:

```text
vege 1.45, fruit 0.80, grain 0.25 optional
```

Examples:

| Food | Raw profile | Approx matrix score | Approx time |
|---|---|---:|---:|
| carrot | vege 29 + fruit 7 | 47.6 | below threshold alone |
| two carrots | about 95 | about 2.6 min |
| bread | grain 60 | 15 | not enough |
| dandelion salad | vege 80 + fruit 30 | 140 | about 2.2 min |
| resin_guard_stew | oil 60 + vege 60 + grain 30 | score ≈ 94.5, self oil 60 | about 2.6 min |

### High vege = 1000, target 650

Correctors:

```text
grain 1.20, flesh 1.20, oil 0.50, fruit 0.40
```

Examples:

| Food | Raw profile | Approx matrix score | Approx time |
|---|---|---:|---:|
| bread | grain 60 | 72 | about 2.8 min |
| cooked beef | flesh 69 + oil 27 | 96.3 | about 2.6 min |
| twin fungus hotpot | flesh 100 + oil 50 + vege 30 | score 145, self vege 30 | about 2.1 min |
| hundred flower feast | grain 50 + oil 60 + fruit 120 + vege 140 | score 138, self vege 140 | may fail strict anti-self rule; this may be acceptable because feast is also heavily vegetable-dominant |

## Open tuning decisions for author

1. Should high oil be corrected by grain at all?
   - Proposed: yes, but weakly (`0.25`).
2. Should the anti-self-correction rule be strict `score > self`, or softer `score > self * 0.75`?
   - Strict prevents abuse but can make complex mixed feasts fail when correcting their own dominant dimension.
3. Should high 津液 normalize other excesses toward `700` at `10`, `15`, or `20` per 10 seconds?
   - Proposed: start with `10`, tune upward if too slow.
4. Should main correction target remain `650`, while high-津液 special target is `700`?
   - Proposed: yes.
5. Should correction interval remain `200 ticks`, or become `100 ticks` for smoother feedback?
   - Proposed: keep `200` for now, raise amounts.

## Next implementation recommendation

Do **not** change core architecture again. The recent-meal system is working. Next turn should only tune logic:

```text
1. Add a data-backed correction matrix.
2. Replace generic score formula with matrix score.
3. Raise correction decay range from 4..12 to 15..45 per 10s.
4. Add high-津液 surplus special normalization to 700 for other excessive dimensions.
5. Add/update validator to lock matrix fields and timing math.
6. Update report and test table.
```
