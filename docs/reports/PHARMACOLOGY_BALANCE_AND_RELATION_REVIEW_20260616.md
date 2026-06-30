# Pharmacology Balance and Relation-Coverage Review — 2026-06-16

Status: source-audit / design recommendation  
Baseline: after `1.1.2` Codex guidance Phase 2.1 + guide hints implementation

This report answers two design questions:

1. Does hot/cold qi accumulate too quickly?
2. Are non-`相须` / non-`相反` relations rare because they are not implemented, hardcoded badly, or because trait intersections are narrow?


## Implementation update — 2026-06-16

The recommended first step has been implemented after this review:

- `temperature_values` is now data-backed in `pharmacology_rules.json`.
- Current values are `cold -15`, `cool -8`, `neutral 0`, `warm 8`, `hot 15`.
- `qi_regression_interval_ticks` is now `600` with `qi_regression_amount 5`.
- Ordered `seven_emotion_rules` is now data-backed in `pharmacology_rules.json`, preserving current behavior while allowing future tuning without Java edits.
- Java still owns the generic rule evaluator and effect application, while JSON owns relation conditions/priority.

## 1. Current source facts

Relevant files:

```text
core/src/main/java/com/herbcraft/pharmacology/PharmacologyResolver.java
core/src/main/java/com/herbcraft/pharmacology/PharmacologyRules.java
core/src/main/resources/data/herbcraft/pharmacology_rules.json
core/src/main/resources/data/herbcraft/herb_natures.json
```

Current data-backed values in `pharmacology_rules.json` before the implementation update were:

```json
{
  "window_ticks": 600,
  "qi_regression_interval_ticks": 1200,
  "qi_regression_amount": 5,
  "same_qi_threshold": 50,
  "xiang_fan_window_ticks": 200
}
```

Previous Java-hardcoded temperature values were:

```java
cold -> -25
cool -> -12
warm -> 12
hot  -> 25
neutral -> 0
```

Current same-qi check happens **before** adding the current bite's temperature:

```text
1st hot exact: qiBefore 0  -> no same-hot warning, qi becomes 25
2nd hot exact: qiBefore 25 -> no same-hot warning, qi becomes 50
3rd hot exact: qiBefore 50 -> same-hot warning/tracking triggers
```

So the author's observation is correct: eating three exact-hot items such as blaze powder can already trigger 热性渐盛. Cold exact items behave similarly.

## 2. Is this too fast?

Likely yes for intended feel.

The pharmacology system is not meant to be as long-term as nutrition, but it also should not feel like a three-bite binary switch unless the player is deliberately chain-eating extreme herbs.

Current behavior has two competing properties:

- Accumulation is fast: exact hot/cold reaches threshold on the 3rd bite.
- Regression is slow: `5` per `1200` ticks / 60 seconds, so a 50-point bias takes about 10 minutes to fully return to zero.

This means the hidden qi can be triggered quickly and then linger for a long time. That combination can feel harsher/more mysterious than intended.

## 3. Recommended qi tuning direction

### Option A — raise `same_qi_threshold`

Keep exact hot/cold as `25`, but raise threshold.

| Threshold | Trigger timing for exact hot/cold |
|---:|---|
| 50 current | 3rd bite triggers. |
| 75 | 4th bite triggers. |
| 100 | 5th bite triggers. |

Pros:

- Simple JSON-only change.
- No Java change if only threshold changes.

Cons:

- Qi still jumps in large chunks.
- Warm/cool become even less likely to matter.

### Option B — data-drive temperature values and reduce exact hot/cold amount

Move temperature values into `pharmacology_rules.json`:

```json
"temperature_values": {
  "cold": -15,
  "cool": -8,
  "neutral": 0,
  "warm": 8,
  "hot": 15
}
```

With `same_qi_threshold = 50`, exact hot/cold triggers on the 5th bite:

```text
1st 15, 2nd 30, 3rd 45, 4th after-check becomes 60, 5th triggers because qiBefore >= 50
```

Pros:

- Better hidden-gradient feel.
- Fully follows data-first rule.
- Lets future tuning happen in JSON.

Cons:

- Requires small Java loader addition.
- Validator should be updated.

### Option C — combine data-driven temperature values with faster regression

Example:

```json
"temperature_values": {
  "cold": -15,
  "cool": -8,
  "neutral": 0,
  "warm": 8,
  "hot": 15
},
"same_qi_threshold": 50,
"qi_regression_interval_ticks": 600,
"qi_regression_amount": 5
```

This makes qi still hidden and meaningful, but less sticky.

Approximate recovery:

- 60 bias returns to 0 in about 6 minutes.
- This is longer than a momentary effect, shorter than nutrition.

Recommended for `1.1.2`:

> Option B or C. Data-drive temperature values now; choose exact values after one client feel pass.

My current recommendation:

```json
"temperature_values": {
  "cold": -15,
  "cool": -8,
  "neutral": 0,
  "warm": 8,
  "hot": 15
},
"same_qi_threshold": 50,
"qi_regression_interval_ticks": 600,
"qi_regression_amount": 5
```

This preserves mystery but reduces the “three blaze powders and immediately too hot” feeling.

## 4. Relation coverage source audit

The relation logic is currently partly data-driven and partly Java-hardcoded.

Data-driven parts:

- Item temperature/flavors/traits are in `herb_natures.json`.
- Windows/thresholds/multipliers are in `pharmacology_rules.json`.

Hardcoded parts:

- Relation priority order.
- Trait conditions for each 七情 relation.
- Temperature exact values.
- The fact that `SINGLE` has no message unless now tracked by Codex discovery.

Current Java conditions:

```text
XIANG_FAN: hot/cold conflict within xiang_fan_window_ticks and either herb has toxic.
XIANG_SHA: current has clearing, previous has toxic.
XIANG_WEI: current has toxic, previous has clearing.
XIANG_E: stirring/sedating conflict in either direction.
XIANG_XU: shared traits >= 2.
XIANG_SHI: current has nourishing/clearing/sedating AND previous has stirring/clearing/nourishing AND profiles are not identical.
SINGLE: current has no shared traits with recent history.
```

## 5. Are other relations too rare?

They are implemented, but in normal play they may feel less common than `相须` / `相反` for three reasons.

### 5.1 Trait distribution makes `相须` easy

Many flowers share `floral`; many clearing flowers share `clearing`. `shared traits >= 2` is easy among common flowers.

Examples:

```text
dandelion -> blue_orchid
blue_orchid -> azure_bluet
poppy -> lily_of_the_valley
```

### 5.2 `相反` has high priority and memorable effects

`XIANG_FAN` is checked first and has strong visible feedback, so it dominates cases where exact hot/cold toxic conflict exists.

### 5.3 Other relations require directional trait patterns

`相杀`, `相畏`, `相恶`, and `相使` require specific current/previous roles. In casual eating, players may not hit those pairs often.

Examples:

- `相杀`: toxic -> clearing
- `相畏`: clearing -> toxic
- `相恶`: sedating ↔ stirring
- `相使`: current nourishing/clearing/sedating after previous stirring/clearing/nourishing, unless an earlier relation wins

## 6. Source-generated relation availability

A source scan of all current herb/nature pairs found many possible pairs, so the system is not missing implementations.

Approximate pair counts from current source:

```text
XIANG_FAN: 44
XIANG_SHA: 55
XIANG_WEI: 55
XIANG_E:   70
XIANG_XU:  102
XIANG_SHI: 287
SINGLE:    many, but not usually surfaced strongly
```

This means the issue is not “relations are absent.” It is more about discoverability, pair distribution, and hardcoded criteria.

## 7. Recommended relation-system direction

### Short-term `1.1.2`

- Keep current relation logic for now.
- Add/keep the Codex discovery records so players can learn which pair caused what.
- Add `SINGLE` display if desired; this has now been accepted and implemented in the current work line.
- Provide a test matrix for players/author to intentionally try each relation.

### Medium-term improvement

Data-drive 七情 relationship rules.

Possible JSON addition to `pharmacology_rules.json`:

```json
"seven_emotion_rules": [
  {
    "type": "xiang_fan",
    "priority": 10,
    "hot_cold_conflict": true,
    "within_ticks_key": "xiang_fan_window_ticks",
    "either_traits": ["toxic"]
  },
  {
    "type": "xiang_sha",
    "priority": 20,
    "current_traits": ["clearing"],
    "previous_traits": ["toxic"]
  },
  {
    "type": "xiang_wei",
    "priority": 30,
    "current_traits": ["toxic"],
    "previous_traits": ["clearing"]
  },
  {
    "type": "xiang_e",
    "priority": 40,
    "trait_conflicts": [["stirring", "sedating"]]
  },
  {
    "type": "xiang_xu",
    "priority": 50,
    "shared_traits_min": 2
  },
  {
    "type": "xiang_shi",
    "priority": 60,
    "current_any_traits": ["nourishing", "clearing", "sedating"],
    "previous_any_traits": ["stirring", "clearing", "nourishing"],
    "not_same_profile": true
  }
]
```

Benefits:

- Future tuning no longer requires Java edits.
- Validators can check rule ordering and required fields.
- More relation types or broader conditions can be adjusted safely.

Risk:

- Slightly larger implementation.
- Must preserve current behavior by default.

Recommended timing:

- Not necessary before the next client pass unless relation balance feels bad.
- Good candidate for `1.1.2` if the author wants a full data-driven pharmacology pass now.
- Otherwise `1.1.3` / `1.2.0`.

## 8. Concrete answer to the author’s question

### Is hot/cold accumulation too fast?

Yes, probably. Exact hot/cold reaches same-qi warning on the 3rd bite. The clean fix is to data-drive `temperature_values` and reduce exact hot/cold from `25` to about `15`, optionally with faster qi regression.

### Are the other 七情 effects missing?

No. `相杀`, `相畏`, `相恶`, and `相使` are implemented and recorded, but they require directional trait pairs and can be less likely in casual eating.

### Is 七情 fully data-driven?

Not fully.

- Traits/natures are data-driven in `herb_natures.json`.
- Windows/thresholds/multipliers are data-driven in `pharmacology_rules.json`.
- But relation conditions and temperature values are still Java logic.

### What should be done next?

Recommended next implementation after client-testing guide hints:

1. Data-drive `temperature_values` in `pharmacology_rules.json` and tune exact hot/cold to around `15`.
2. Consider whether to also data-drive `seven_emotion_rules` now or later.
3. If not data-driving all 七情 rules now, at least keep the Codex discovery/test matrix so players can learn relations through play.
