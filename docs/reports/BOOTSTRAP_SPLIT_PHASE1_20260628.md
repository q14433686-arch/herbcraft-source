# Bootstrap Split Phase 1 Report

**Date:** 2026-06-28  
**Project Version:** `1.1.3`  
**Type:** Structural decomposition / coupling reduction

## 1. Goal

Following the broader high-coupling audit, the first structural reduction target was the oversized `Herbcraft.java` initializer.

The goal of Phase 1 was to reduce responsibility stacking without changing runtime behavior.

## 2. Pre-Refactor Problem

`Herbcraft.java` previously owned too many unrelated concerns in one class:

- bundled data loading;
- startup external data loading;
- reload listener registration;
- debug command registration;
- default item component wiring;
- Codex and knowledge bootstrap wiring;
- packet type registration;
- packet receiver registration;
- login lifecycle handling;
- world tick lifecycle handling;
- player respawn sync registration.

This made it a high-risk central hotspot.

## 3. Implemented Split

### Added focused bootstrap classes

- `HerbcraftDataBootstrap`
- `HerbcraftKnowledgeBootstrap`
- `HerbcraftNetworkingBootstrap`
- `HerbcraftLifecycleBootstrap`

### New ownership boundaries

#### `HerbcraftDataBootstrap`
Owns:
- bundled data load;
- startup external herb-food load;
- reload listener registration;
- default item component wiring.

#### `HerbcraftKnowledgeBootstrap`
Owns:
- debug command registration;
- Codex components/items/chapters/loot registration;
- inventory scanner registration and knowledge encounter handler;
- creative Codex items;
- last-meal tracker;
- advancement hooks.

#### `HerbcraftNetworkingBootstrap`
Owns:
- payload type registration;
- serverbound claim-mark receiver registration.

#### `HerbcraftLifecycleBootstrap`
Owns:
- knowledge sync manager registration;
- external data sync manager registration;
- player respawn sync registration;
- join lifecycle handling;
- world tick nutrition handling.

## 4. Behavior Expectations

This phase was intentionally behavior-preserving.

No gameplay rules, JSON semantics, packet semantics, or persistence boundaries were changed. The refactor only moved wiring responsibilities into more focused classes.

## 5. Validation

Executed and passed:

1. `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS  
2. `python3 scripts/validate_bootstrap_split_phase1.py` — PASS  
3. `python3 scripts/validate_coupling_hotspots.py` — PASS  
4. `python3 scripts/validate_codex_payload_separation.py` — PASS  
5. `python3 scripts/validate_codex_respawn_ui_state.py` — PASS  
6. `python3 scripts/validate_player_state_respawn_copy.py` — PASS  
7. `python3 scripts/verify_built_jars.py` — PASS  

## 6. Remaining Work

This reduces the largest central orchestrator hotspot, but two major multi-responsibility areas remain:

1. `NutritionManager`
2. `HerbalChapter`

Those are the recommended next decomposition targets.

## 7. Conclusion

Phase 1 successfully reduced the most obvious bootstrap-level coupling hotspot. The codebase now has clearer ownership boundaries at startup/wiring level while preserving behavior.
