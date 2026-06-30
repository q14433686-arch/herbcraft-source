# Death / Respawn Codex Stale-Page Reopen Bugfix Report

**Date:** 2026-06-28  
**Project Version:** `1.1.3`  
**Severity:** High / client UI session-state bug

## 1. User-Reported Symptom

After dying and respawning, players could see the Codex (`百草经`) appear on the previously viewed page. The bug did not trigger only on a fresh first entry into the world.

## 2. Root Cause

This bug had two interacting causes:

### Cause A — static client UI state leaked across screen instances

`CodexScreen` stored client-session state in static fields:

- `lastViewedKey`
- `expandedNodes`

When a new `CodexScreen` instance was created, it attempted to restore the previous selection using `lastViewedKey`.

Because the Minecraft client process stays alive across death/respawn, these static fields remained populated after death.

This explains why the bug did **not** happen only on a fresh initial world entry: the static fields start empty at first load.

### Cause B — respawn path was incorrectly sending a UI-opening payload

`CodexSnapshotPayload` is not merely a passive data cache packet. On the client it is registered as:

```java
ClientPlayNetworking.registerGlobalReceiver(CodexSnapshotPayload.TYPE, (payload, context) -> context.client().setScreen(new CodexScreen(payload)));
```

So sending `CodexSnapshotPayload` after respawn does not just refresh data — it actively opens the Codex screen.

The earlier death-persistence fix had added a respawn-time `CodexSnapshotPayload` send. Combined with the static `lastViewedKey`, this recreated the exact stale-page reopen behavior.

## 3. Fix Implemented

### 3.1 Removed respawn-time Codex UI opening

In `PlayerStateSync`, removed respawn-time sending of:

- `CodexSnapshotPayload.build(newPlayer)`

Respawn now only refreshes:

- `NutritionManager.onLogin(newPlayer)`
- `KnowledgeSyncManager.sync(newPlayer)`

### 3.2 Added explicit Codex client-session reset hook

In `CodexScreen`, added:

- `clearClientSessionState()`

This clears:

- `lastViewedKey`
- `expandedNodes`

### 3.3 Clear client session state on disconnect

Wired client disconnect cleanup in `HerbcraftCoreClient` so stale Codex UI session state does not leak across client play sessions.

## 4. Same-Level Review

Same-level bug pattern here means:

- a payload serving both as data sync and UI-open trigger is used in an automatic lifecycle path (respawn/join/etc.), or
- static client UI state survives beyond the intended session boundary and causes stale re-entry behavior.

This audit specifically confirmed the problematic combination on the Codex path. No equivalent second UI reopen path of the same severity was found in the current source during this pass.

## 5. Validation

Executed and passed:

1. `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS  
2. `python3 scripts/validate_codex_respawn_ui_state.py` — PASS  
3. `python3 scripts/validate_player_state_respawn_copy.py` — PASS  
4. `python3 scripts/verify_built_jars.py` — PASS  

## 6. Client-Only Verification Still Required

A real client should confirm:

1. Open Codex and navigate to a non-default page.
2. Die and respawn.
3. Confirm Codex does not auto-open.
4. If manually reopened, confirm it does not incorrectly jump into an unintended stale page due to respawn behavior.
5. Confirm the earlier death-persistence fix still works: tasted knowledge and Codex progress remain intact.

## 7. Conclusion

The bug was caused by a UI session-state leak plus misuse of a UI-opening payload in the respawn lifecycle.

The respawn path no longer forces the Codex open, and the client now has an explicit Codex session reset hook. This should eliminate the stale previous-page reopen behavior while preserving death-persistent knowledge progress.
