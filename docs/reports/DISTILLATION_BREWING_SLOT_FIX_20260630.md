# Distillation Brewing Slot Fix

Date: 2026-06-30

## User-reported issue

The user reported that no essence item could currently be placed into the brewing-stand bottom slots, which prevented the entire distillation route from functioning in practice.

## Root cause

The source already had two partial pieces of support:

- `BrewingStandCatalystMixin` allowed automation/block-entity placement rules for:
  - bottom slots (`slot 0..2`) accepting base essence inputs for distillation;
  - ingredient slot (`slot 3`) accepting hidden Cuisine catalysts or distillation catalysts.
- `DistilledEssenceBrewing` already implemented the actual custom brewing logic for:
  - essence + catalyst -> distilled essence;
  - base potion + distilled essence -> processed potion path.

Two separate menu-layer gaps existed:

- `BrewingStandIngredientSlotMixin` accepted `HiddenCuisineCatalystBrewing.isCatalystItem(stack)` but originally did **not** accept `DistilledEssenceBrewing.isCatalystItem(stack)`.
- More importantly for the user-reported symptom, the brewing stand bottom potion slots are controlled by `BrewingStandMenu$PotionSlot`, not by the ingredient slot. Those potion slots normally only accept potion containers, so plain essence items were still rejected in the GUI even though `BrewingStandBlockEntity.canPlaceItem(...)` had been patched.

This created a split acceptance bug:

- block entity / automation checks said the distillation path was valid;
- runtime brew execution existed;
- but menu slot placement rules still rejected real user placement in the live GUI.

That mismatch made the feature effectively unusable in normal client interaction and is exactly the kind of sync/boundary bug worth checking elsewhere.

## Fix implemented

Updated:

- `alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandIngredientSlotMixin.java`

Change:

- ingredient-slot `mayPlace(...)` now accepts both:
  - `HiddenCuisineCatalystBrewing.isCatalystItem(stack)`
  - `DistilledEssenceBrewing.isCatalystItem(stack)`

This restores parity between:

1. menu/UI placement acceptance;
2. brewing-stand block entity placement acceptance;
3. custom brewability/runtime execution.

## Additional review / similar-bug audit

Reviewed the current brewing path for similar split-boundary issues:

- `BrewingStandCatalystMixin.canPlaceItem(...)` already allows:
  - essence inputs in bottom slots;
  - hidden Cuisine catalysts and distillation catalysts in the ingredient slot.
- `BrewingStandCatalystMixin.isBrewable(...)` already delegates to both hidden Cuisine and distillation logic.
- `BrewingStandCatalystMixin.doBrew(...)` already routes to both custom brew executors.
- `DistilledEssenceBrewing` already handles both:
  - essence -> distilled essence;
  - potion + distilled essence -> processed potion.

So the observed failure point was the GUI/menu ingredient-slot acceptance layer, not the runtime brew logic itself.

## Sync/boundary note

This bug is a classic acceptance-layer mismatch:

- runtime logic existed;
- block-entity placement rules partly existed;
- menu-slot acceptance was incomplete.

That means the immediate lesson is to audit custom station features for three layers together:

1. menu/UI slot acceptance;
2. block-entity or container placement rules / automation rules;
3. actual recipe/brew runtime execution.

For this specific fix, no new packet sync path was needed. The issue was not network desync in the packet sense, but a server/client interaction-surface mismatch inside the brewing-stand stack acceptance flow.

## Validation

Executed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/validate_distillation_brewing_slot_acceptance.py
python3 scripts/validate_distillation_foundation.py
python3 scripts/validate_distillation_brewing_routing.py
python3 scripts/verify_built_jars.py
```

Results:

- build — PASS
- `validate_distillation_brewing_slot_acceptance.py` — PASS
- `validate_distillation_foundation.py` — PASS
- `validate_distillation_brewing_routing.py` — PASS
- `verify_built_jars.py` — PASS
- after refreshing `release_output/`: `verify_built_jars.py` — PASS, `check_lang_diff.py` — PASS

## Release-output refresh

Refreshed local release artifacts under:

```text
release_output/herbcraft_test_20260614_review/
```

Updated files:

- `herbcraft-1.1.3.jar`
- `herbcraft-alchemy-1.1.3.jar`
- `herbcraft-cuisine-1.1.3.jar`
- `herbcraft-source-current-20260614.zip`

## Client-only verification still required

Needs real-client confirmation:

1. base essence can be placed in brewing-stand bottom slots;
2. honeycomb / redstone / glowstone dust can be placed in the ingredient slot for distillation;
3. distillation actually consumes inputs and produces the correct distilled essence;
4. distilled essence can then be placed in the brewing ingredient slot for processed-potion routing;
5. mismatched source essence still rejects routing correctly.

## Follow-up discussion anchor

The user’s broader design concerns are valid and source-consistent:

- current distillation is mechanically functional but still thin in pharmacology/system linkage;
- processing/distillation styles currently project mostly into Alchemy-local behavior and not enough into Core’s性味 / 气机 / 七情 / 寒热 / 特质 surfaces;
- catalyst creativity is still conservative because it currently reuses familiar vanilla materials.

Those should be discussed after the functional brewing-slot fix is verified.
