# Nutrition Imbalance Redesign Options

Date: 2026-06-15  
Scope: discussion/design only; no gameplay code changed in this turn.

## Trigger

After P3 real-client validation passed, survival testing revealed a broader nutrition-design issue:

```text
Nutrition values decay too slowly downward: current default is -2 per dimension per 1200 ticks / 60 seconds.
If one dimension is pushed very high by combat recovery, hunger recovery, repeated soups/drinks, or repeated one-sided eating, it can take too long to return to balance.
The dietary imbalance debuff therefore behaves too much like a long memory of previous accumulated values, instead of reflecting the player's current corrective diet structure.
```

The immediate example was high `fruit` / 津液 / Fluids caused by hydration-related foods, but the problem is systemic and can also affect high `flesh`, `oil`, `grain`, or `vege`.

## Source facts checked

### Current decay

`nutrition_rules.json` currently says:

```json
"decay_interval_ticks": 1200,
"decay_amount": 2
```

`PlayerNutritionState.tickDecay(...)` applies this uniformly:

```java
for (String dim : NutritionProfile.DIMENSIONS) {
   int current = get(dim);
   if (current > 0) {
      values.put(dim, Math.max(0, current - rules.decayAmount));
   }
}
```

That means every dimension decays by only 2 per minute. A value of 900 takes about 150 minutes to naturally fall to 600 if not reduced by any other mechanic.

### Current imbalance is based on accumulated stored values

`NutritionManager.evaluateAndApply(...)` currently derives imbalance from current stored dimension values:

```java
if (max - min > 500 && max > rules.imbalanceHigh) {
   state.incrementBadStreak();
   if (!isDietaryImbalanceRelieved(player) && state.shouldApplyDebuff()) {
      addNutritionEffect(player, DIETARY_IMBALANCE);
   }
}
```

This means imbalance is mainly a function of stored totals, not recent corrective food structure.

### Current relief is temporary and does not repair values

Water bowl can call:

```java
NutritionManager.relieveDietaryImbalance(player)
```

which removes the debuff and suppresses reapplication temporarily, but it does not change the underlying high/low nutrition values.

## Design goal clarified

The author wants the base mod's nutrition system to behave more like this:

```text
Current diet structure should matter more than old accumulated totals.
Old bad eating can create a problem, but the player should be able to respond with a corrective meal pattern.
Corrective foods should not need to wait tens of minutes for old high values to decay naturally.
Main-mod nutrition should remain advisory/light, not hardcore punishment.
```

## Candidate options

### Option A — Simple faster global decay

Increase `decay_amount` or lower `decay_interval_ticks`.

Example:

```json
"decay_interval_ticks": 600,
"decay_amount": 4
```

Pros:

- Very easy.
- JSON-only.
- Fixes long tail somewhat.

Cons:

- Blunt instrument.
- Also drains low dimensions faster, making deficiency more common.
- Does not distinguish corrective eating from starvation or ordinary time passing.
- Can make the entire nutrition system feel more noisy.

Verdict:

```text
Useful as a small supporting tweak, but not sufficient as the main solution.
```

### Option B — Corrective meal relief + accelerated high-dimension decay

This is close to the author's proposal.

Concept:

```text
If the player is imbalanced because one dimension is too high, eating a food that contributes to one or more underrepresented/corrective dimensions temporarily relieves dietary imbalance and accelerates decay of the high dimension until it approaches a target band.
```

Example:

```text
High flesh imbalance:
  eating vege/fruit/grain corrective food grants relief for several minutes
  during relief, flesh decays faster until it is no longer excessive

High oil imbalance:
  eating vege/grain/fruit corrective food grants relief
  oil decays faster

High fruit/津液 imbalance:
  either no negative debuff in base mod, or corrective solids help normalize it
```

Possible implementation components:

```text
- Track active correction windows per player.
- Store which high dimensions are being corrected.
- During nutrition tick, apply extra decay to those high dimensions.
- Suppress dietary imbalance while the correction window is active.
- End correction when the high dimension returns near normal or the timer expires.
```

Pros:

- Matches player intuition: "I ate too much X, now I eat balancing foods."
- Keeps old bad diet meaningful, but gives players an active recovery path.
- Fits main-mod light-advisory philosophy.
- Can be data-driven later with rules like correction duration/decay multiplier.

Cons:

- Needs code changes.
- Needs careful messaging/UI so players understand why imbalance is relieved.
- Needs definition of which food dimensions correct which high dimensions.

Verdict:

```text
Best gameplay direction.
```

### Option C — Recent diet window instead of cumulative totals for imbalance

Keep cumulative nutrition values for deficiency/abundance, but calculate dietary imbalance from a recent meal history window.

Concept:

```text
Last N meals or last M minutes form a diet-structure vector.
Dietary imbalance is based on that recent vector, not only stored totals.
```

Example:

```text
Stored flesh = 900 from past eating.
But recent meals include vegetables/grain/fluids.
Result: no persistent dietary imbalance, because current diet structure is now corrective.
```

Pros:

- Very close to the author's stated realism goal.
- Separates "body stores" from "current diet structure".
- Reduces punishment from old values.

Cons:

- Requires meal history storage and serialization.
- More complex to debug.
- Needs UI/command support to explain recent diet status.
- More invasive than Option B.

Verdict:

```text
Conceptually excellent, but probably too large for a quick 1.1.x stabilization pass unless implemented very simply.
```

### Option D — Separate imbalance debt/status from nutrition values

Keep numeric dimensions as slow-moving body stores, but do not directly derive debuffs from raw max/min every time. Instead, eating patterns create or clear an "imbalance debt" state.

Concept:

```text
Repeated one-sided eating creates a dietary_imbalance debt.
Corrective eating reduces or clears that debt.
Raw values still affect deficiency/abundance, but persistent debuff is controlled by debt state.
```

Pros:

- Clean separation between accumulated nutrition and current dietary behavior.
- Can avoid long-tail punishment from high stored values.
- Allows rich future hard-core addon rules.

Cons:

- Needs new state, persistence, commands, sync, docs.
- Higher implementation burden.

Verdict:

```text
Good long-term architecture; may be overkill before P4/API work.
```

### Option E — High values are non-punitive; only low values punish

The simplest light-main-mod philosophy:

```text
Deficiency causes debuffs.
Excess only prevents well_nourished and maybe gives mild messages.
Dietary imbalance debuff is removed or heavily limited.
```

Pros:

- Very player-friendly.
- Avoids punishment for Homeostatic hydration and survival recovery.
- Simple code changes.

Cons:

- Dietary imbalance becomes much less important.
- Does not solve the conceptual problem of old high values, only stops punishing them.

Verdict:

```text
Safe fallback if we want minimum implementation risk.
```

## Recommended hybrid

Recommended direction for the base mod:

```text
Option B + a small part of Option E.
```

Concrete policy:

1. Dietary imbalance should be treated as a **recoverable current-diet condition**, not a pure raw-value punishment.
2. Eating corrective food should temporarily relieve imbalance and accelerate decay of the excessive dimensions.
3. High `fruit` / 津液 / Fluids should be non-punitive or least punitive in the base mod, because it overlaps with Homeostatic hydration and normal soup/drink survival.
4. Low values should remain meaningful and can still cause deficiency.
5. High values can still block `well_nourished` by being outside the 400..600 full-balance band.
6. Hardcore Nutrition addon can later reintroduce stricter overconsumption, water intoxication, electrolyte imbalance, or body-store simulation.

## Possible first implementation plan

A conservative implementation could add:

```json
"correction_window_ticks": 3600,
"correction_extra_decay_interval_ticks": 200,
"correction_extra_decay_amount": 4,
"correction_target_value": 650
```

Runtime behavior:

```text
When dietary imbalance is detected, record high dimensions.
When the player eats a food containing dimensions that are not high and help the low side, start a correction window.
During the correction window:
  - suppress dietary_imbalance reapplication;
  - apply extra decay to recorded high dimensions only;
  - stop when high dimensions fall to correction_target_value or timer expires.
```

Example:

```text
Player has flesh 920, vege 360, grain 410, oil 500, fruit 520.
Player eats a vege/grain soup.
Result:
  - dietary imbalance relief starts for 3 minutes;
  - flesh receives accelerated decay;
  - if flesh returns near 650, relief naturally ends as state is no longer imbalanced.
```

For `fruit` / 津液 specifically:

```text
Either do not record fruit as a high punitive dimension in base mod, or give it the mildest correction behavior.
```

## Questions to decide before coding

1. Should `fruit` / 津液 high value be fully non-punitive, or just lower priority?
2. Should corrective foods be defined generically as "any non-high dimension", or by a table such as flesh corrected by vege/grain/fruit?
3. How long should the correction window last? Suggested first value: 1800-3600 ticks.
4. Should accelerated decay affect only the highest dimension or all dimensions above 650/850?
5. Should correction be messaged to the player, e.g. "饮食渐趋调和"?
6. Should water bowl's current dietary-imbalance relief be replaced by or integrated into this correction system?

## Current no-change status

No source gameplay code or JSON balance values changed in this turn. This report is a design discussion record for the next implementation decision.
