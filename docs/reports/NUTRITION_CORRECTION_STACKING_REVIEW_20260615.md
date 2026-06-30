# Nutrition Correction Stacking Review

Date: 2026-06-15  
Scope: review-only diagnosis after author observed high flesh + high vege + high fluids only dropping by 10 each correction tick.  
Status: **fixed after review; report documents diagnosis and implemented fix**

## Author observation

The author tested a state similar to:

```text
flesh >= 850
vege >= 850
fruit / 津液 around 995
```

Observed behavior:

```text
flesh and vege each drop only about 10 points per correction tick.
```

The suspicion was that the relationship-matrix correction was not stacking and that high-津液 `-10` was occupying/replacing the matrix decay.

## Source checked

Files reviewed:

```text
core/src/main/java/com/herbcraft/nutrition/PlayerNutritionState.java
core/src/main/java/com/herbcraft/nutrition/NutritionManager.java
core/src/main/java/com/herbcraft/nutrition/NutritionRules.java
core/src/main/resources/data/herbcraft/nutrition_rules.json
core/src/main/resources/data/herbcraft/nutrition_correction_rules.json
```

## Current implementation facts

Current correction tick path:

```java
NutritionManager.tick(...)
  -> state.tickCorrectionDecay(gameTime)
```

Inside `PlayerNutritionState.tickCorrectionDecay(...)`, matrix correction is attempted first:

```java
for (String dim : highPunitiveDimensions(rules)) {
   if (correctionScore(dim, rules, gameTime) >= rules.correctionScoreThreshold) {
      ... subtract correctionDecayAmount(...)
   }
}
```

Then high-津液 surplus helper is applied:

```java
if (rules.fluidSurplusDecayIntervalTicks > 0 && gameTime - lastFluidSurplusDecayTick >= rules.fluidSurplusDecayIntervalTicks) {
   boolean fluidChanged = applyFluidSurplusDecay(rules);
}
```

The high-津液 helper currently does:

```java
if (get("fruit") >= rules.fluidSurplusThreshold) {
   for each non-fruit dim above 850:
      subtract 10 toward 700
}
```

So if only this helper is active, the observed result is exactly:

```text
-10 per 10 seconds for every non-fruit excessive dimension
```

## Why the matrix did not stack in this scenario

The current `correctionScore(...)` contains this condition:

```java
if (!dim.equals(highDim) && get(dim) < rules.correctionTargetValue) {
   corrective += meal.values().getOrDefault(dim, 0) * rules.correctionWeight(highDim, dim);
}
```

This means a corrective dimension only counts if its **current stored value** is below:

```text
correction_target_value = 650
```

Therefore, in the author's test:

```text
flesh >= 850
vege >= 850
fruit ~= 995
```

For high `flesh`:

```text
vege is a strong corrector by matrix, but current vege >= 850, so it is ignored.
fruit is a corrector, but current fruit ~= 995, so it is ignored.
```

For high `vege`:

```text
flesh is a strong corrector by matrix, but current flesh >= 850, so it is ignored.
fruit is a weak corrector, but current fruit ~= 995, so it is ignored.
```

Result:

```text
matrix correction score may be 0 or below threshold;
only the high-津液 helper applies;
observed -10 is expected under current code.
```

## Is this a bug?

Yes, from the latest design intent.

The current condition `get(dim) < correctionTargetValue` came from the earlier generic model where corrective foods were meant to fill underrepresented dimensions. But after adding a relationship matrix, the intended model changed:

```text
Current diet structure and recent food relationship should matter.
A dimension can be high and still serve as a counterweight for a different high dimension if the recent diet includes enough of it and the matrix says it is a valid corrector.
```

Example:

```text
High flesh and high vege can be mutually correcting if recent meals actually include sufficient vege for flesh and sufficient flesh/grain for vege.
```

The matrix and anti-self guard should decide whether a meal is corrective, not the current-value `<650` filter alone.

## Did high 津液 actually displace matrix decay?

Not directly in the tested scenario.

More precise diagnosis:

```text
The matrix correction usually never became active because corrective dimensions were filtered out by currentValue < 650.
The high-津液 helper was the only active decay path, so it looked like it displaced matrix correction.
```

However, there is a secondary stacking nuance:

```text
Matrix correction is applied before fluid surplus helper.
If matrix correction drops a dimension from just above 850 to below 850, then fluid helper will no longer apply that same tick because it checks current value > 850 after matrix decay.
```

This is not the author's main observed case, but it is worth fixing if the goal is clean additive behavior.

## Recommended next fix

### Fix 1: remove the `< correctionTargetValue` filter from matrix scoring

Change matrix scoring from:

```java
if (!dim.equals(highDim) && get(dim) < rules.correctionTargetValue) {
   corrective += meal.values().getOrDefault(dim, 0) * rules.correctionWeight(highDim, dim);
}
```

to:

```java
if (!dim.equals(highDim)) {
   corrective += meal.values().getOrDefault(dim, 0) * rules.correctionWeight(highDim, dim);
}
```

Keep the anti-self guard:

```java
if (corrective > meal.values().getOrDefault(highDim, 0) * rules.selfPenaltyMultiplier) {
   score += corrective;
}
```

This means:

```text
Relationship matrix decides correction, not whether the corrector dimension is currently low.
Meals dominated by the excessive dimension still do not correct themselves.
```

### Fix 2: make matrix + high-津液 helper stack from original tick state

Current order can cause a dimension near 850 to receive matrix decay but miss the high-津液 helper in the same tick.

Safer next implementation:

```text
1. Snapshot original values at start of correction tick.
2. Compute matrix decay for each high dimension based on original values.
3. Compute high-津液 surplus decay based on original values.
4. Sum both decays per dimension.
5. Apply final value once, clamped to the stricter applicable target.
```

For example:

```text
original flesh = 900
matrix decay = 30 toward 650
fluid surplus decay = 10 toward 700
final flesh = 860
```

This makes the system intuitive:

```text
If both mechanisms apply, they stack.
```

### Fix 3: validator should lock this behavior

Update:

```text
scripts/validate_nutrition_correction_phase.py
```

Add checks that:

```text
- correctionScore uses matrix weights;
- correctionScore no longer requires currentValue[d] < correctionTargetValue;
- fluid surplus helper can stack with matrix correction or uses an original-value delta model.
```

## Expected behavior after fix

In the author's scenario:

```text
flesh >= 850
vege >= 850
fruit >= 850
```

If recent meals provide enough matrix-valid correction:

```text
flesh should receive matrix decay + high-津液 surplus decay.
vege should receive matrix decay + high-津液 surplus decay.
```

If no recent matrix-valid meals exist:

```text
flesh and vege should still receive only high-津液 surplus decay (-10 per 10s toward 700).
```

So the post-fix result should distinguish:

```text
high fruit alone = slow -10 helper;
high fruit + recent valid corrective meals = stacked faster correction.
```

## Implemented fix

The follow-up implementation changed `PlayerNutritionState` so that:

```text
1. `correctionScore(...)` no longer requires the corrective dimension's stored value to be below `correctionTargetValue` 650.
2. The relationship matrix and anti-self guard now decide whether a recent meal is corrective.
3. `tickCorrectionDecay(...)` snapshots original tick values, computes matrix decay and high-津液 surplus decay into per-dimension deltas, stacks both mechanisms when applicable, and applies the final clamped value once.
4. `scripts/validate_nutrition_correction_phase.py` now checks that the old `<650` scoring filter is absent and that original-value stacked delta logic exists.
```

Expected fixed behavior:

```text
high fruit alone = slow -10 helper;
high fruit + recent valid matrix meals = matrix decay + high-津液 helper stacked in the same tick.
```

## Validation status

After the fix:

```text
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon build --rerun-tasks
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/verify_built_jars.py
```

passed as part of the full validation set recorded in `CURRENT_BASELINE.md`.

