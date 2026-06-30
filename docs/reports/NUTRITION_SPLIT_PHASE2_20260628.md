# Nutrition Split Phase 2 Report

**Date:** 2026-06-28  
**Project Version:** `1.1.3`  
**Type:** Structural decomposition / coupling reduction

## 1. Goal

After bootstrap split Phase 1, the next highest-value coupling hotspot was `NutritionManager`.

The goal of Phase 2 was to separate runtime evaluation, effect application, discovery side effects, and debug helpers while keeping the public `NutritionManager` API stable.

## 2. Pre-Refactor Problem

`NutritionManager` previously combined too many concerns in one class:

- runtime tick logic;
- login/death orchestration;
- nutritional state evaluation;
- effect application/removal;
- abundant attribute modifier handling;
- ingredient Codex discovery side effects;
- guide-hint side effects;
- debug mutation/preset helpers;
- client sync triggering.

That made it a high-risk maintenance hotspot.

## 3. Implemented Split

### `NutritionManager`
Now acts as a thin facade for external callers.

### `NutritionRuntimeService`
Owns:
- tick loop logic;
- login/death runtime handling;
- dietary imbalance relief timing;
- evaluation and client sync orchestration.

### `NutritionEffectApplicator`
Owns:
- deficiency/excess effect application;
- abundant attribute modifiers;
- clearing nutrition effects;
- balanced bonus application.

### `NutritionDiscoveryHooks`
Owns:
- ingredient Codex discovery side effects;
- guide hint trigger on first ingredient taste;
- event emission related to nutrition consume discovery.

### `NutritionDebugService`
Owns:
- debug direct-set helper;
- debug preset helper;
- correction meal seeding for test presets.

## 4. Behavior Expectations

This phase was intended to preserve gameplay behavior and external call sites.

- Existing callers still invoke `NutritionManager`.
- The split is structural, not a mechanic redesign.

## 5. Validation

Executed and passed:

1. `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS  
2. `python3 scripts/validate_nutrition_split_phase2.py` — PASS  
3. `python3 scripts/validate_bootstrap_split_phase1.py` — PASS  
4. `python3 scripts/validate_coupling_hotspots.py` — PASS  
5. `python3 scripts/validate_codex_payload_separation.py` — PASS  
6. `python3 scripts/validate_codex_respawn_ui_state.py` — PASS  
7. `python3 scripts/validate_player_state_respawn_copy.py` — PASS  
8. `python3 scripts/verify_built_jars.py` — PASS  

## 6. Remaining Work

The next major multi-responsibility hotspot remains:

- `HerbalChapter`

That is the recommended Phase 3 target.

## 7. Conclusion

Phase 2 successfully reduced coupling inside the nutrition subsystem while preserving the public facade and build/runtime wiring. The nutrition path now has clearer ownership boundaries and is safer to evolve incrementally.
