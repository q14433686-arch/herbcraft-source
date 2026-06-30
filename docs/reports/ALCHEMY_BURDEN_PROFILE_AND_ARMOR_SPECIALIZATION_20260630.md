# Alchemy burden profile consequences and armor-support specialization 2026-06-30

## Summary

Implemented the requested next Alchemy refinement: potion burden consequences now use both total burden stage and the dominant medicinal profile, while armor support is no longer only a generic burden shield.

The implementation keeps the project architecture boundary:

- Java owns runtime interpretation, event wiring, persistence, effect application, and armor scanning.
- JSON owns profile tables, effect definitions, support numbers, matching rows, and tuning values.

## Design issue addressed

Before this pass, Alchemy already persisted rich semantic inputs such as:

- processing `burden_profile`;
- style `thermal_shift`, `qi_bias`, `trait_affinity`;
- source-essence nature data;
- `PotionBodyStateProjection` fields including `medicinal_profile`, `thermal_tendency`, `qi_tendency`, and `body_traits`.

However, the burden consequence layer mostly used only total burden stages:

```text
steady -> warming -> strain -> overload
```

and the runtime result was mostly one generic movement penalty. Armor support similarly worked mathematically, especially as a full set, but behaved as broad generic buffering rather than clear/steady/braced pharmacological bearing.

## Implemented: profile-specialized burden consequences

`data/herbcraft_alchemy/potion_processing_rules.json` now defines `burden_consequence_profiles`:

```text
steady
warming
chilling
overdriven
muddy
scattering
stagnating
```

Each profile can define:

- profile-specific effect ID;
- strain duration multiplier;
- overload duration multiplier;
- exhaustion-per-tick contribution;
- profile message key.

`PotionProcessingRules` now parses and exposes `BurdenConsequenceProfile` rows through `burdenConsequenceProfile(...)` and `burdenConsequenceProfiles()`.

`PlayerPotionBurdenState` now remembers the last consumed processed potion's semantic context:

- `dominantProfile`;
- `lastBurdenProfile`;
- `lastThermalTendency`;
- `lastQiTendency`;
- `consequenceDurationMultiplier` from armor support;
- `tailDecayBonus` from armor support.

`PotionBurdenConsequenceRuntime` still respects total burden stages, but when the player is in a non-steady stage it uses `dominantProfile` to choose the profile effect and message. Profiles can also add conservative exhaustion pressure for heat/scattering-style outcomes.

## Implemented: new profile consequence effects

`data/herbcraft_alchemy/potion_burden_effects.json` gained JSON-backed effects:

```text
potion_heat_stir
potion_cold_stagnation
potion_overdrive_rebound
potion_turbid_disturbance
potion_qi_scattering
potion_qi_stagnation
```

`AlchemyMobEffects` registers these effects and includes them in the burden-stage cleanup set so old profile effects do not stack indefinitely.

Language keys and 18x18 effect icons were added for all new effects.

Current conservative profile feel:

| Profile | Runtime direction |
|---|---|
| warming | heat pressure, mild exhaustion, light attack disruption |
| chilling | cold stagnation, block-break and movement drag |
| overdriven | intense peak rebound, attack disruption |
| muddy | murky disturbance, movement and attack drag |
| scattering | qi scattering, exhaustion pressure |
| stagnating | qi stagnation, movement drag |
| steady | reduced/quiet baseline |

## Implemented: armor-support specialization

`data/herbcraft_alchemy/armor_support_rules.json` now uses split active/tail buffering and matching metadata:

- `active_buffer_amount`
- `tail_buffer_amount`
- `matching_styles`
- `matching_burden_profiles`
- `matching_medicinal_profiles`
- `match_active_bonus`
- `match_tail_bonus`
- `consequence_mitigation_profiles`
- `consequence_duration_multiplier`
- `tail_decay_bonus`

Retuned support rows:

| Support | Orientation | General role |
|---|---|---|
| `clear_bearing` / 澄承 | purified | clearer support against muddy/dirty/clean-stable outcomes; mitigates muddy consequence duration |
| `steady_bearing` / 续承 | sustained | tail-heavy support; improves tail buffering and tail decay for sustained/slow-tail outcomes |
| `braced_bearing` / 峻承 | intense | active/peak support; improves active buffering against intense/sharp_peak/overdriven outcomes |

Generic base buffers were slightly raised, but the larger gain is now from matching style/profile semantics rather than universal shielding.

## Runtime/API compatibility note

The public-ish legacy event `POTION_BURDEN_BUFFER` was retained with its original `int` return shape for source compatibility. A new detailed internal/support event was added:

```java
AlchemyExpansionEvents.POTION_BURDEN_BUFFER_DETAILED
AlchemyExpansionEvents.PotionBurdenBufferResult
```

Armor support now registers on the detailed event. `PotionBurdenRuntime` combines legacy simple buffers and detailed active/tail buffers before applying player burden.

## Files changed

Major runtime/data files:

- `alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/potion_burden_effects.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/armor_support_rules.json`
- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenRuntime.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenConsequenceRuntime.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenState.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportRules.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/armor/ArmorSupportRuntime.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/AlchemyExpansionEvents.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/effect/AlchemyMobEffects.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyPlayerMixin.java`

Validation:

- Added `scripts/validate_alchemy_burden_profile_specialization.py`.
- Updated existing Alchemy burden/effect/armor/built-jar validators for the new profile effects and split support semantics.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

Validator suite executed successfully:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_raw_herb_food_balance.py
python3 scripts/validate_essence_resources.py
python3 scripts/validate_potion_processing_rules.py
python3 scripts/validate_potion_processing_runtime.py
python3 scripts/validate_potion_burden_runtime.py
python3 scripts/validate_potion_burden_consequences.py
python3 scripts/validate_alchemy_burden_effects.py
python3 scripts/validate_alchemy_expansion_boundaries.py
python3 scripts/validate_alchemy_burden_profile_specialization.py
python3 scripts/validate_distillation_foundation.py
python3 scripts/validate_distillation_brewing_routing.py
python3 scripts/validate_distillation_brewing_slot_acceptance.py
python3 scripts/validate_armor_support_foundation.py
python3 scripts/validate_processed_coating_expansion.py
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
python3 scripts/validate_coating_recipe_display.py
python3 scripts/validate_coating_viewer_plugins.py
python3 scripts/validate_bootstrap_split_phase1.py
python3 scripts/validate_nutrition_split_phase2.py
python3 scripts/validate_coupling_hotspots.py
python3 scripts/validate_codex_payload_separation.py
python3 scripts/validate_codex_respawn_ui_state.py
python3 scripts/validate_player_state_respawn_copy.py
python3 scripts/validate_potion_source_projection.py
python3 scripts/validate_potion_body_state_projection.py
python3 scripts/validate_potion_relation_and_catalyst_semantics.py
python3 scripts/validate_alchemy_knowledge_gating_and_codex.py
python3 scripts/validate_codex_hitboxes.py
python3 scripts/validate_herbalchapter_split_phase3.py
python3 scripts/validate_panel_hodge_recipe.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/verify_built_jars.py
```

Result: PASS for all listed validators.

## Client-only follow-up

The sandbox cannot judge actual gameplay feel. In a live client, verify:

1. repeated sustained/long potions produce more tail-oriented pressure and steady support visibly helps tail buildup/decay;
2. repeated intense/strong potions push toward overdriven consequences and braced support clearly reduces peak burden;
3. muddy/toxic/murky paths produce murky disturbance and clear support shortens/softens that pressure;
4. new effect names/icons render correctly;
5. consequence messages are informative but not spammy;
6. old external/simple `POTION_BURDEN_BUFFER` listeners, if any, still work as simple active/tail-half buffers.
