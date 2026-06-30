# Codex UI Claim Hitbox Bugfix Report

**Date:** 2026-06-18  
**Project Version:** `1.1.3`  
**Minecraft:** `26.1.2`  

## 1. Issue Description

The user reported an issue with the Herb Codex UI: clicking on errata (勘误) options / claim marks produced no reaction in-game.

### Root Cause Analysis
In `CodexScreen.java`:
1. `renderRightPanel(...)` successfully cleared and populated `this.claimHitboxes` during the screen rendering phase (`extractRenderState`).
2. However, right after rendering the panels, if `scrollbarEnabled` was active in the theme, `extractRenderState` invoked `maxRightScroll(...)`.
3. `maxRightScroll(...)` called `contentHeight(...)` to calculate the required scrollbar thumb size.
4. `contentHeight(...)` contained an erroneous `this.claimHitboxes.clear()` call (likely leftover from a structural copy-paste of `renderRightPanel`).
5. As a result, by the end of every rendering frame, `this.claimHitboxes` was **completely wiped out**.
6. Furthermore, during mouse click events (`mouseClicked`), `maxRightScroll(...)` was also invoked before checking `claimHitboxes`, guaranteeing that `claimHitboxes` was always empty when mouse clicks were evaluated.

## 2. Implemented Fixes

We resolved this issue in `core/src/main/java/com/herbcraft/knowledge/client/CodexScreen.java`:
1. **Removed Unwarranted Clearing:** Removed `this.claimHitboxes.clear()` from `contentHeight(...)`. `contentHeight` is purely a mathematical query and must not mutate interactive hitbox state.
2. **Robust Multi-Line Hitbox Height:** In `renderRightPanel`, replaced the single-line height approximation with robust wrapped text height:
   ```java
   int wrappedH = CodexTextLayout.wrappedHeight(this.font, line, textWidth, t.rightLineHeight);
   int hitHeight = Math.max(t.claimMinHeight, wrappedH);
   this.claimHitboxes.add(new CodexScreen.ClaimHitbox(rx, ry, hitX2, ry + hitHeight, claim.entryKey(), claim.claimId()));
   ```
   This ensures that claims spanning multiple lines have fully accurate clickable hitbox heights.
3. **Strict Screen Area Scissor Bounds:** In `mouseClicked`, added explicit coordinate bounds checks before evaluating claim hitboxes:
   ```java
   if (event.button() == 0 && event.x() >= layout.rightX && event.x() < layout.rightX + layout.rightWidth && event.y() >= layout.rightY && event.y() <= layout.bottom) { ... }
   ```
   This guarantees that clicking outside the right panel detail area (e.g., on headers or the left directory) will never accidentally trigger an off-panel scrolled hitbox.

## 3. Safeguards & Validation

- Added `scripts/validate_codex_hitboxes.py` to automate AST/textual validation, guarding against `contentHeight` ever clearing hitboxes again and checking that multi-line hitboxes and strict click bounds are intact.
- Re-executed the complete Gradle build (`BUILD SUCCESSFUL`) and all project validators.
- Rebuilt and refreshed `herbcraft-1.1.3.jar` and the publishing artifacts.
