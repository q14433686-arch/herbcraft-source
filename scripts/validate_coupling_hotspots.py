#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errs = []

def require(cond, msg):
    if cond:
        print('PASS - ' + msg)
    else:
        print('FAIL - ' + msg)
        errs.append(msg)

main = (ROOT / 'core/src/main/java/com/herbcraft/Herbcraft.java').read_text()
client = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/client/HerbcraftCoreClient.java').read_text()
report = (ROOT / 'docs/reports/HIGH_COUPLING_AUDIT_20260628.md').read_text()

# Guard the highest-risk fixed boundary.
require('context.client().setScreen(new CodexScreen(payload))' not in client, 'codex snapshot is not directly coupled to setScreen in client receiver')
require('ClientPlayNetworking.registerGlobalReceiver(OpenCodexPayload.TYPE' in client, 'codex UI opening uses explicit payload receiver')

# Defensive cache-lifecycle baseline.
require('CodexClientState.clear();' in client and 'CodexScreen.clearClientSessionState();' in client, 'disconnect clears codex client caches/session state')

# Audit artifact presence.
require('## P1 — High-risk coupling that can plausibly create future bugs' in report, 'high coupling audit report contains high-risk section')
require('P1.1 `Herbcraft.java` is an oversized central orchestrator' in report, 'audit report records oversized main bootstrap hotspot')
require('P1.2 `NutritionManager` mixes state mutation, effect application, user messaging, knowledge unlocks, debug presets, and sync' in report, 'audit report records nutrition manager hotspot')
require('P1.3 `HerbalChapter` combines Codex registration, dynamic food-nature projection, classification heuristics, guide-provider logic, and player-facing line generation' in report, 'audit report records herbal chapter hotspot')

if errs:
    raise SystemExit(1)
print('All coupling hotspot validation checks passed.')
