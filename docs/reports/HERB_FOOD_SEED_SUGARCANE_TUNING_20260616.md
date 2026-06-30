# Herbcraft Seed and Sugar Cane Food-Roll Tuning — 2026-06-16

Status: implemented after client feel feedback  
Baseline: Herbcraft `1.1.1`, planned `1.1.2` polish candidate

## 1. Reason

After in-client survival testing, the first P3.10 food-roll pass made seeds too weak:

- seed visible hunger rolls were acceptable;
- seed hidden saturation rolls triggered only sometimes;
- when they did trigger, the previous `0.1` / `0.2` direct saturation amounts disappeared in a few seconds and did not feel like a meaningful seed/oil nibble.

Sugar cane also felt too thin as a food item: it restored only one visible hunger point and almost no lasting hidden saturation. Because sugar cane is juicy and fibrous, it should remain weak as a meal, but it should not feel like dry grass.

## 2. Implemented seed changes

The probabilities were kept mostly unchanged; the amount on a successful hidden-saturation roll was raised.

```text
wheat_seeds           H+1 @ 60%, S+2.0 @ 30%
beetroot_seeds        H+1 @ 60%, S+2.0 @ 30%
torchflower_seeds     H+1 @ 60%, S+2.0 @ 35%
melon_seeds           H+1 @ 65%, S+3.0 @ 50%
pumpkin_seeds         H+1 @ 65%, S+3.0 @ 50%
pitcher_pod           stable H+1, saturation_modifier 1.0
```

Design note:

- ordinary seeds now sometimes give a real short fullness moment;
- oily seeds, especially melon/pumpkin seeds, are allowed to feel more substantial;
- visible hunger is still not stable for common seeds, so seeds do not become bread.

## 3. Implemented sugar cane changes

```text
sugar_cane nutrition: 1
sugar_cane saturation_modifier: 1.0
```

Because vanilla converts stable saturation as:

```text
nutrition * saturation_modifier * 2
```

this means sugar cane now gives:

```text
visible hunger: +1
hidden saturation: about +2 direct saturation points
```

This follows the new feel target: sugar cane is not a full meal, but chewing cane should last longer than petals.

## 4. Hydration / Homeostatic compatibility check

Current source already has:

```json
{
  "type": "minecraft:sugar_cane",
  "amount": 3,
  "saturation": 0.5,
  "effect_potency": 0,
  "effect_duration": 0,
  "effect_chance": 0.0
}
```

No hydration value change was needed in source because the current bundled Homeostatic compat data is already `amount = 3`. The validator now explicitly anchors this so it does not regress to `1`.

## 5. Validator updates

Updated:

```text
scripts/validate_raw_herb_food_balance.py
scripts/validate_homeostatic_compat.py
```

Validation now allows seed hidden-saturation roll amounts up to `3.0`, checks exact seed/sugar-cane values, and explicitly checks sugar cane Homeostatic hydration at amount `3`.

## 6. Remaining client test focus

Retest:

1. Wheat/beetroot/torchflower seeds at low hunger: they should still be unreliable visible food, but a successful saturation roll should feel noticeable.
2. Melon/pumpkin seeds: oily seed identity should feel meaningfully fuller than wheat/beetroot seeds.
3. Sugar cane: visible hunger should remain only +1, but the hidden fullness should now last long enough to feel like chewing juicy cane.
4. Homeostatic installed: sugar cane should hydrate as `amount 3`, with no thirst-risk effect.


## 7. Validation completed

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_raw_herb_food_balance.py
python3 scripts/validate_essence_resources.py
python3 scripts/validate_homeostatic_compat.py
python3 scripts/validate_nutrition_cuisine_immunity_isolation.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_catalyst_mixin.py
python3 scripts/validate_nature_jade.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```

Result: all passed.
