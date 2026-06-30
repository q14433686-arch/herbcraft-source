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

sync = (ROOT / 'core/src/main/java/com/herbcraft/PlayerStateSync.java').read_text()
lifecycle = (ROOT / 'core/src/main/java/com/herbcraft/bootstrap/HerbcraftLifecycleBootstrap.java').read_text()
player = (ROOT / 'core/src/main/java/com/herbcraft/mixin/PlayerMixin.java').read_text()

require('ServerPlayerEvents.COPY_FROM.register' in sync, 'respawn copy handler is registered')
require('copyFromOldPlayer' in sync, 'respawn copy handler implementation exists')
require('herbcraft$knowledge()' in sync, 'knowledge map is copied on respawn')
require('herbcraft$knowledgeFlags()' in sync, 'knowledge flags are copied on respawn')
require('herbcraft$experiencedEffects()' in sync, 'experienced effects are copied on respawn')
require('herbcraft$claimMarks()' in sync, 'claim marks are copied on respawn')
require('herbcraft$verifiedTrueClaims()' in sync and 'herbcraft$verifiedFalseClaims()' in sync, 'verified claims are copied on respawn')
require('herbcraft$seenGuideHints()' in sync, 'seen guide hints are copied on respawn')
require('NutritionManager.onLogin(newPlayer)' in sync, 'nutrition is re-synced after respawn')
require('KnowledgeSyncManager.sync(newPlayer)' in sync, 'knowledge sync is forced after respawn')
require('CodexSnapshotPayload.build(newPlayer)' not in sync, 'respawn path does not resend codex snapshot UI-open packet')
require('PlayerStateSync.register();' in lifecycle, 'lifecycle bootstrap registers player respawn sync')
require('herbcraft$clearOnDeath' in player and 'herbcraft$clearPharmacologyState();' in player and 'herbcraft$nutritionState.resetOnDeath();' in player, 'death-reset semantics still exist for runtime combat/nutrition state')
require('copyWindowCounters' in sync, 'window counter copy decision is explicit and documented')

if errs:
    raise SystemExit(1)
print('All player respawn copy validation checks passed.')
