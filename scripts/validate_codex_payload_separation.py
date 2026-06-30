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

book = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/CodexBookItem.java').read_text()
network = (ROOT / 'core/src/main/java/com/herbcraft/bootstrap/HerbcraftNetworkingBootstrap.java').read_text()
client = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/client/HerbcraftCoreClient.java').read_text()
state = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/client/CodexClientState.java').read_text()
screen = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/client/CodexScreen.java').read_text()
open_payload = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/OpenCodexPayload.java').read_text()

require('record OpenCodexPayload' in open_payload, 'explicit open-codex payload exists')
require('PayloadTypeRegistry.clientboundPlay().register(OpenCodexPayload.TYPE' in network, 'open-codex payload is registered server-side')
require('ServerPlayNetworking.send(serverPlayer, CodexSnapshotPayload.build(serverPlayer));' in book, 'codex book still sends snapshot data')
require('ServerPlayNetworking.send(serverPlayer, OpenCodexPayload.open());' in book, 'codex book sends explicit open-ui payload')
require('context.client().setScreen(new CodexScreen(payload))' not in client, 'snapshot receiver no longer opens UI directly')
require('CodexClientState.setLatestSnapshot(payload);' in client, 'snapshot receiver updates client codex cache')
require('context.client().screen instanceof CodexScreen codexScreen' in client, 'open codex screen can refresh live from snapshots')
require('ClientPlayNetworking.registerGlobalReceiver(OpenCodexPayload.TYPE' in client, 'client handles explicit open-codex payload')
require('context.client().setScreen(new CodexScreen(snapshot));' in client, 'explicit open-codex payload opens UI using cached snapshot')
require('class CodexClientState' in state and 'latestSnapshot' in state, 'client codex snapshot cache exists')
require('public void replaceSnapshot(CodexSnapshotPayload snapshot)' in screen, 'codex screen supports live snapshot replacement')

if errs:
    raise SystemExit(1)
print('All codex payload separation validation checks passed.')
