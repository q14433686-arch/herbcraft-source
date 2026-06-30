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

screen = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/client/CodexScreen.java').read_text()
client = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/client/HerbcraftCoreClient.java').read_text()
sync = (ROOT / 'core/src/main/java/com/herbcraft/PlayerStateSync.java').read_text()

require('private static String lastViewedKey = null;' in screen, 'codex screen remembers last viewed key explicitly')
require('private static final Set<String> expandedNodes = new HashSet<>()' in screen, 'codex screen remembers expanded nodes explicitly')
require('public static void clearClientSessionState()' in screen, 'codex screen exposes client-session reset hook')
require('lastViewedKey = null;' in screen and 'expandedNodes.clear();' in screen, 'codex session reset clears remembered page and expanded nodes')
require('ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {' in client and 'CodexScreen.clearClientSessionState();' in client and 'CodexClientState.clear();' in client, 'client disconnect clears codex client session state and cached snapshot')
require('ServerPlayNetworking.send(newPlayer, com.herbcraft.knowledge.CodexSnapshotPayload.build(newPlayer));' not in sync, 'respawn path does not force-open codex UI')
require('KnowledgeSyncManager.sync(newPlayer);' in sync, 'respawn still refreshes lightweight knowledge sync')
require('NutritionManager.onLogin(newPlayer);' in sync, 'respawn still refreshes nutrition state')

if errs:
    raise SystemExit(1)
print('All codex respawn UI state validation checks passed.')
