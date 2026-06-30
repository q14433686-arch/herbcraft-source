# Codex Payload / UI Responsibility Separation Report

**Date:** 2026-06-28  
**Project Version:** `1.1.3`  
**Type:** Architecture hardening / sync-vs-UI decoupling

## 1. Why This Refactor Was Needed

Recent death/respawn bugs exposed a deeper architectural problem in the Codex path:

- `CodexSnapshotPayload` was being used as both:
  1. a data snapshot transport; and
  2. a client-side command to open the Codex UI.

Because the client registered this payload by immediately calling `setScreen(new CodexScreen(payload))`, any server lifecycle path that reused the snapshot packet could accidentally open the Codex UI.

This is exactly the kind of coupling that turns an otherwise-correct sync refresh into a UX bug.

## 2. Pre-Refactor Behavior

Before this refactor:

- `CodexBookItem` sent `CodexSnapshotPayload`.
- The client received it and immediately opened `CodexScreen`.
- Claim-mark refreshes also reused `CodexSnapshotPayload`.
- Any future or accidental lifecycle resend of `CodexSnapshotPayload` risked opening the Codex unintentionally.

This made the packet semantically ambiguous.

## 3. Refactor Implemented

### 3.1 New explicit UI-open payload

Added:

- `core/src/main/java/com/herbcraft/knowledge/OpenCodexPayload.java`

This payload exists solely to mean:

> open the Codex UI now

### 3.2 Snapshot payload is now pure data

`CodexSnapshotPayload` remains the full Codex data snapshot, but it is no longer responsible for opening UI.

Client handling now:

- stores the latest snapshot in `CodexClientState`;
- if Codex is already open, refreshes the screen in place via `CodexScreen.replaceSnapshot(...)`;
- otherwise does nothing visually.

### 3.3 Codex book now sends explicit sequence

Opening the book now sends:

1. `CodexSnapshotPayload`
2. `OpenCodexPayload`

This preserves existing UX while making intent explicit.

## 4. Architecture Result

After refactor:

- **data sync** and **UI opening** are separate responsibilities;
- lifecycle sync paths can safely resend snapshots without forcing UI open;
- Codex claim-mark or snapshot refreshes can update the already-open screen in place;
- future maintenance risk is reduced because packet semantics are now self-describing.

## 5. Related Client Session Cleanup

The client disconnect path now clears:

- remembered Codex UI session state (`lastViewedKey`, `expandedNodes`);
- cached latest snapshot (`CodexClientState`).

This reduces stale-state leakage across play sessions.

## 6. Validation

Executed and passed:

1. `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build` — PASS  
2. `python3 scripts/validate_codex_payload_separation.py` — PASS  
3. `python3 scripts/validate_codex_respawn_ui_state.py` — PASS  
4. `python3 scripts/validate_player_state_respawn_copy.py` — PASS  
5. `python3 scripts/verify_built_jars.py` — PASS  

## 7. Runtime Checks Still Needed

A real client should confirm:

1. Using the Codex book still opens the Codex correctly.
2. With Codex already open, claim-mark changes refresh the current screen rather than reopening/replacing it awkwardly.
3. Death/respawn does not auto-open Codex.
4. Death-persistent knowledge still remains intact.
5. Reconnect/disconnect does not preserve stale unintended Codex screen state.

## 8. Conclusion

This refactor converts the Codex networking path from an ambiguous dual-purpose packet design into a clearer and safer architecture:

- `CodexSnapshotPayload` = data
- `OpenCodexPayload` = UI-open intent

That separation should significantly reduce future sync-layer bugs that manifest as unwanted UI behavior.
