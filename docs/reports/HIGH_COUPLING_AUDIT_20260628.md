# High Coupling Audit Report

**Date:** 2026-06-28  
**Project Version:** `1.1.3`  
**Scope:** broader architecture audit after death/respawn + Codex UI incidents

## Executive Summary

The recent death/respawn bugs were not isolated accidents. They exposed a deeper pattern:

- several classes still combine multiple responsibilities (registration, lifecycle wiring, sync, data loading, gameplay effects, UI triggers);
- some client caches and screen state still rely on implicit session boundaries;
- some lifecycle paths are correct today, but their responsibilities are not cleanly separated enough to be future-proof.

The most severe already-triggered Codex packet/UI coupling has been fixed. However, additional same-family architectural coupling remains and should be treated as technical risk, especially for future multiplayer / reconnect / reload / respawn evolution.

---

## P0 — Already triggered / proven-dangerous coupling

### P0.1 Codex snapshot packet previously mixed UI opening with data sync

Status: **FIXED this turn**

- `CodexSnapshotPayload` previously acted as both data and UI-open trigger.
- This directly contributed to the respawn stale-page reopen bug.
- Refactor completed:
  - `CodexSnapshotPayload` = pure data
  - `OpenCodexPayload` = explicit UI-open intent

This item remains important as a pattern warning for the rest of the codebase.

---

## P1 — High-risk coupling that can plausibly create future bugs

### P1.1 `Herbcraft.java` is an oversized central orchestrator

`core/src/main/java/com/herbcraft/Herbcraft.java` currently performs too many unrelated startup responsibilities in one place:

- bundled data loading;
- startup external herb-food loading;
- reload listener registration;
- debug command registration;
- default item component modification;
- Codex item/component/chapter/loot registration;
- inventory scanner handler wiring;
- knowledge sync manager registration;
- external data sync registration;
- player respawn sync registration;
- advancement hook registration;
- payload type registration;
- serverbound receiver registration;
- login handling;
- world tick nutrition handling.

Risk:
- lifecycle logic becomes easy to wire in the wrong place;
- one future change can accidentally affect unrelated systems;
- testability and ownership boundaries are weak.

Recommendation:
- split into focused bootstraps, e.g.:
  - `HerbcraftDataBootstrap`
  - `HerbcraftKnowledgeBootstrap`
  - `HerbcraftNetworkingBootstrap`
  - `HerbcraftLifecycleBootstrap`

### P1.2 `NutritionManager` mixes state mutation, effect application, user messaging, knowledge unlocks, debug presets, and sync

`core/src/main/java/com/herbcraft/nutrition/NutritionManager.java` currently owns too many concerns:

- nutrition state mutation;
- recent-meal recording;
- correction logic;
- effect application/removal;
- attribute modifier application/removal;
- user warning/message timing;
- ingredient Codex unlock side effects;
- guide-hint side effects;
- debug preset application;
- client sync triggering.

Risk:
- nutrition bugs can accidentally break Codex discovery behavior or vice versa;
- difficult to reason about death/login/tick semantics;
- hard to isolate runtime-only state from persistent knowledge state.

Recommendation:
- split into:
  - `NutritionRuntimeService`
  - `NutritionEffectApplicator`
  - `NutritionDiscoveryHooks`
  - `NutritionDebugService`

### P1.3 `HerbalChapter` combines Codex registration, dynamic food-nature projection, classification heuristics, guide-provider logic, and player-facing line generation

`core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java` currently combines:

- section/entry registration;
- dynamic food-nature entry projection from current registries;
- section classification heuristics (`classify`);
- guide-provider registration;
- player-stat-based line generation;
- progression-stat summarization.

Risk:
- reload-sensitive content projection and static guide/view logic live in the same class;
- knowledge-content generation and data projection are tightly intertwined;
- future content changes may accidentally affect runtime UI behavior.

Recommendation:
- split into:
  - `HerbalCodexRegistration`
  - `FoodNatureCodexProjection`
  - `HerbalGuideProviders`
  - `HerbalEntryTextBuilder`

### P1.4 Claim mark update path still uses a full Codex snapshot refresh packet

Current behavior after a claim-mark click:
- server receives `ClaimMarkPayload`
- updates player state
- sends a fresh `CodexSnapshotPayload`

This is functionally valid after the payload/UI separation refactor because snapshot is now pure data. However, it is still a broad refresh path for a very narrow state change.

Risk:
- high fan-out packet use for small updates;
- future developers may again overgeneralize snapshot refresh behavior.

Recommendation:
- future optional refinement: add a tiny `ClaimMarkUpdatePayload` or a narrow Codex delta payload.

### P1.5 Client caches still rely on implicit lifecycle assumptions

Current client caches include:
- `KnowledgeClientCache`
- `NutritionClientCache`
- `CodexClientState`

The Codex cache/session state is now explicitly cleared on disconnect. The others are still lightweight and currently acceptable, but they remain global static caches with implicit lifecycle boundaries.

Risk:
- reconnect / title-screen reuse / integrated-server transitions could surface stale-cache behavior later if new features depend on these caches more heavily.

Recommendation:
- standardize a `clear-on-disconnect` policy for all client caches, even if only defensive today.

---

## P2 — Medium-risk maintainability coupling

### P2.1 Data loading responsibilities are spread across many registries/managers with inconsistent ownership style

Examples:
- `HerbFoodRegistry.loadFromBundledJson()`
- `HerbFoodRegistry.loadExternalStartupResources()`
- `HerbNatureRegistry.loadFromBundledJson()`
- `NutritionRegistry.loadFromBundledJson()`
- `KnowledgeRumorRegistry.loadFromBundledJson()`
- `GuideHintManager.loadFromBundledJson(...)`
- `CodexGuideRegistry.loadFromBundledJson(...)`

Risk:
- not all loaders clearly express whether they are:
  - startup-only,
  - reload-safe,
  - client-applied,
  - server-authoritative.

Recommendation:
- standardize loader naming / javadoc / bootstrap grouping by lifecycle semantics.

### P2.2 Join/respawn/tick/reload responsibilities are distributed across multiple managers without one boundary document in code

Examples:
- `Herbcraft.java` login tick wiring
- `HerbcraftDataSyncManager` join sync
- `PlayerStateSync` respawn copy/after-respawn refresh
- `KnowledgeSyncManager` periodic sync
- `NutritionManager` login + tick

Risk:
- the system works, but the lifecycle boundary is not obvious from one place;
- future modifications can easily duplicate or omit lifecycle actions.

Recommendation:
- add a lifecycle matrix doc or class-level design note mapping:
  - JOIN
  - RESPAWN COPY
  - AFTER_RESPAWN
  - END_SERVER_TICK
  - DATA RELOAD
  - DISCONNECT

### P2.3 Several feature classes still use static mutable state without explicit ownership notes

Examples:
- `KnowledgeAPI.CHAPTERS`
- `GuideHintManager` hint maps
- registry/profile maps across multiple systems
- client cache globals

Risk:
- not inherently broken, but implicit global ownership increases hidden coupling.

Recommendation:
- add ownership comments or narrow service wrappers around especially sensitive global state.

---

## Recommended Next Refactor Order

### Phase A — defensive low-risk hardening
1. Add disconnect clears for all client caches.
2. Add a lifecycle-boundary validator/report.
3. Add comments/javadocs identifying startup-only vs reload-safe vs client-cache responsibilities.

### Phase B — server bootstrap separation
1. Split `Herbcraft.java` into focused bootstrap helpers.
2. Separate networking registration from gameplay registration.
3. Separate lifecycle event registration from data/bootstrap registration.

### Phase C — nutrition / knowledge service decomposition
1. Break `NutritionManager` into runtime/effect/discovery/debug responsibilities.
2. Break `HerbalChapter` into registration/projection/text-provider layers.
3. Consider narrow delta payloads for Codex micro-updates.

---

## Conclusion

The codebase is not in a catastrophic state, but the recent bugs were a real warning sign. The highest-risk packet/UI coupling has now been fixed. The remaining major risks are concentrated in:

- oversized bootstrap/orchestrator classes;
- lifecycle semantics spread across too many managers;
- manager classes mixing gameplay logic with sync and discovery side effects;
- global mutable caches/state without uniformly explicit lifecycle cleanup.

These are manageable, but they should be addressed deliberately before more features are layered on top.
