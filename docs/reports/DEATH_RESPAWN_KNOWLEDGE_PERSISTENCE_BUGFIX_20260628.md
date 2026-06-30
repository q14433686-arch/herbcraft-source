# Death / Respawn Knowledge Persistence Bugfix Report

**Date:** 2026-06-28  
**Project Version:** `1.1.3`  
**Severity:** Critical / malignant player-data sync bug

## 1. User-Reported Symptom

Players reported that after death, previously tasted items / Codex discovery records appeared to be fully cleared.

This is a severe bug because it destroys player-facing long-term progression and undermines trust in the knowledge system.

## 2. Root Cause

The root cause was not the Codex UI, not JSON data, and not the disk save format itself.

The actual bug was at the **player respawn synchronization boundary**:

- Herbcraft stores custom player state in Mixin fields attached to `Player` (`PlayerMixin`).
- On death/respawn, Minecraft replaces the old `ServerPlayer` object with a new `ServerPlayer` instance.
- Herbcraft had NBT read/write hooks for world saves, but **did not copy custom Herbcraft player fields from the old player instance to the new respawned player instance**.
- Therefore, after death, the new player object started with empty in-memory Herbcraft maps/sets, making tasted knowledge / Codex state appear wiped immediately after respawn.

This affected all custom player data that depended on Mixin-backed runtime fields and was expected to persist across death.

## 3. Source Audit Findings

### 3.1 Directly affected persistent knowledge-layer state

The following player-bound states were expected to survive death, and previously had no respawn-copy protection:

- `herbcraft$knowledge()`
- `herbcraft$knowledgeFlags()`
- `herbcraft$experiencedEffects()`
- `herbcraft$claimMarks()`
- `herbcraft$verifiedTrueClaims()`
- `herbcraft$verifiedFalseClaims()`
- `herbcraft$seenGuideHints()`

These are now explicitly copied during respawn.

### 3.2 States reviewed and intentionally NOT copied

The following states already have explicit death-reset semantics in source and should remain cleared after death:

- herb stack (`herbcraft$clearHerbStack()`)
- effect immunities (`herbcraft$clearEffectImmunities()`)
- pharmacology runtime state (`herbcraft$clearPharmacologyState()`)
- nutrition runtime state / recent meals (`herbcraft$nutritionState.resetOnDeath()`)

These were deliberately left non-persistent across death.

### 3.3 Same-level bug review

A same-level bug would mean: custom player state stored on Mixin fields, expected to survive death, but lacking old-player -> new-player copy on respawn.

This audit found the major affected set to be the knowledge-layer state listed above. No additional death-persistent state of equal severity was found beyond that set in the current source.

A lower-risk reviewed case is `windowCounters`: these are transient runtime counters and are now explicitly documented as intentionally non-copied.

## 4. Implemented Fix

Added a dedicated sync class:

- `core/src/main/java/com/herbcraft/PlayerStateSync.java`

Registered:

- `ServerPlayerEvents.COPY_FROM`
- `ServerPlayerEvents.AFTER_RESPAWN`

Behavior:

- During `COPY_FROM`, copy all death-persistent Herbcraft knowledge-layer state from old player to new player.
- During `AFTER_RESPAWN`, immediately refresh:
  - nutrition sync/state application,
  - knowledge sync payload,
  - Codex snapshot payload.

## 5. Validation

Executed and passed:

1. `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS  
2. `python3 scripts/validate_player_state_respawn_copy.py` — PASS  
3. `python3 scripts/verify_built_jars.py` — PASS  

## 6. Client-Only Verification Still Required

The sandbox cannot fully simulate interactive death->respawn gameplay UX. The following must still be confirmed in a real runtime environment:

1. Taste several herbs / ingredients.
2. Confirm Codex / 食材录 entries advance as expected.
3. Die and respawn.
4. Re-open Codex and verify tasted/mastered/heard progress is still present.
5. Verify claim marks and seen guide hints also persist.
6. Verify herb stack, nutrition runtime state, and other intended death-reset runtime states still reset normally.

## 7. Conclusion

This bug was caused by a missing respawn-copy bridge for custom player state, not by the knowledge logic itself.

The root cause has been fixed in source, the affected persistence boundary has been audited, and a dedicated validator has been added to reduce the chance of future regressions.
