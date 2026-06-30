#!/usr/bin/env python3
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

rules = json.loads((ROOT / 'alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json').read_text(encoding='utf-8'))
styles = {entry['id']: entry for entry in rules.get('styles', [])}
expected_runtime = {
    'sustained': {'duration_multiplier': 1.25, 'amplifier_delta': -1, 'max_amplifier': 1},
    'intense': {'duration_multiplier': 0.75, 'amplifier_delta': 1, 'max_amplifier': 2},
    'purified': {'duration_multiplier': 1.10, 'amplifier_delta': 0, 'max_amplifier': 1, 'force_non_ambient': True},
}
for style, expected in expected_runtime.items():
    entry = styles.get(style, {})
    require(entry.get('implemented') is True, f'{style} must be implemented=true for P4')
    require(entry.get('first_batch') is True and entry.get('deferred') is False, f'{style} must be active first-batch style')
    runtime = entry.get('runtime')
    require(isinstance(runtime, dict), f'{style} missing runtime object')
    for key, value in expected.items():
        if isinstance(value, float):
            require(abs(float(runtime.get(key, -999)) - value) < 0.0001, f'{style}.runtime.{key} expected {value}')
        else:
            require(runtime.get(key) == value, f'{style}.runtime.{key} expected {value!r}')
for style in ['murky_edge', 'harmonized']:
    entry = styles.get(style, {})
    require(entry.get('implemented') is False and entry.get('deferred') is True, f'{style} must remain deferred/not implemented in P4')
    require('runtime' not in entry, f'{style} should not define active runtime behavior in P4')

runtime_java = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRuntime.java')
initializer = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java')
components = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java')
verify = read('scripts/verify_built_jars.py')

for token in [
    'public final class PotionProcessingRuntime',
    'PotionProcessingRules.style(styleId)',
    '!style.get().implemented() || style.get().deferred()',
    'durationMultiplier()',
    'amplifierDelta()',
    'maxAmplifier()',
    'forceNonAmbient()',
    'forceVisible()',
    'forceIcon()',
    'MAX_DURATION_TICKS',
    'new PotionContents(Optional.empty()',
    'DataComponents.POTION_DURATION_SCALE, 1.0F',
    'POTION_PROCESSING_STYLE',
]:
    require(token in runtime_java, f'PotionProcessingRuntime missing token {token}')
for token in [
    'implementedStyle',
    'needsStyleRuntime',
    'PotionProcessingRuntime.apply',
    'POTION_PROCESSING_STYLE',
    'if (advanced)',
    'if (!Boolean.TRUE.equals(initialized))',
]:
    require(token in initializer, f'AdvancedPotionStackInitializer missing P4 token {token}')
require('path.filter(AdvancedPotionStackInitializer::isAdvancedPotionPath)' in initializer, 'legacy advanced quality/bonus should still be gated by isAdvancedPotionPath')
require('path.endsWith("_long")' not in initializer.split('public static boolean isAdvancedPotionPath',1)[1].split('private static String inferSourceId',1)[0], '_long must not enter legacy quality/bonus advanced classification')
require('path.endsWith("_strong")' not in initializer.split('public static boolean isAdvancedPotionPath',1)[1].split('private static String inferSourceId',1)[0], '_strong must not enter legacy quality/bonus advanced classification')
require('POTION_PROCESSING_STYLE' in components and 'potion_processing_style' in components, 'AlchemyPotionComponents should register processing style component')
require('PotionProcessingRuntime.class' in verify, 'verify_built_jars should check PotionProcessingRuntime class')
require('AlchemyPotionComponents.class' in verify, 'verify_built_jars should check AlchemyPotionComponents class')

jar = ROOT / 'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z:
        names = set(z.namelist())
    require('com/herbcraft/alchemy/potion/PotionProcessingRuntime.class' in names, 'built Alchemy jar missing PotionProcessingRuntime.class')
    require('com/herbcraft/alchemy/potion/PotionProcessingRules$RuntimeProfile.class' in names, 'built Alchemy jar missing RuntimeProfile class')

if errors:
    print('FAIL')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print('PASS - P4 potion processing runtime differentiates sustained/intense/purified, preserves deferred branches, and avoids routing long/strong through legacy quality/bonus classification.')
