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
data = (ROOT / 'core/src/main/java/com/herbcraft/bootstrap/HerbcraftDataBootstrap.java').read_text()
knowledge = (ROOT / 'core/src/main/java/com/herbcraft/bootstrap/HerbcraftKnowledgeBootstrap.java').read_text()
network = (ROOT / 'core/src/main/java/com/herbcraft/bootstrap/HerbcraftNetworkingBootstrap.java').read_text()
lifecycle = (ROOT / 'core/src/main/java/com/herbcraft/bootstrap/HerbcraftLifecycleBootstrap.java').read_text()

require('HerbcraftDataBootstrap.initialize();' in main, 'main initializer delegates data bootstrap')
require('HerbcraftKnowledgeBootstrap.initialize();' in main, 'main initializer delegates knowledge bootstrap')
require('HerbcraftNetworkingBootstrap.initialize();' in main, 'main initializer delegates networking bootstrap')
require('HerbcraftLifecycleBootstrap.initialize();' in main, 'main initializer delegates lifecycle bootstrap')
require('HerbFoodRegistry.loadFromBundledJson();' in data and 'registerReloadListener' in data, 'data bootstrap owns bundled/external data and reload listener wiring')
require('KnowledgeInventoryScanner.register();' in knowledge and 'CodexItems.initialize();' in knowledge, 'knowledge bootstrap owns codex and scanner setup')
require('PayloadTypeRegistry.clientboundPlay().register(CodexSnapshotPayload.TYPE' in network and 'ServerPlayNetworking.registerGlobalReceiver(ClaimMarkPayload.TYPE' in network, 'networking bootstrap owns payload and receiver registration')
require('ServerPlayConnectionEvents.JOIN.register' in lifecycle and 'ServerTickEvents.END_LEVEL_TICK.register' in lifecycle and 'PlayerStateSync.register();' in lifecycle, 'lifecycle bootstrap owns join/respawn/tick wiring')

if errs:
    raise SystemExit(1)
print('All bootstrap split phase 1 validation checks passed.')
