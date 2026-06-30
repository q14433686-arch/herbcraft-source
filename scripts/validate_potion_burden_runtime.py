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
burden_runtime = rules.get('burden_runtime', {})
require(burden_runtime.get('decay_interval_ticks') == 200, 'burden decay interval should be 200 ticks')
require(burden_runtime.get('active_decay_amount') == 5, 'active burden decay should be 5 per interval')
require(burden_runtime.get('tail_decay_amount') == 2, 'tail burden decay should be 2 per interval')
require(burden_runtime.get('max_burden') == 1000, 'max burden should be 1000')
for entry in rules.get('styles', []):
    require(isinstance(entry.get('burden_weight'), int) and entry['burden_weight'] >= 0, f'{entry.get("id")} missing non-negative burden_weight')
    require(isinstance(entry.get('tail_weight'), int) and entry['tail_weight'] >= 0, f'{entry.get("id")} missing non-negative tail_weight')
    require(entry.get('burden_profile'), f'{entry.get("id")} missing burden_profile')

components = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java')
component = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenComponent.java')
rules_java = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java')
metadata = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenRuntime.java')
player_state = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenState.java')
player_runtime = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PlayerPotionBurdenRuntime.java')
player_mixin = read('alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyPlayerMixin.java')
item_mixin = read('alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyItemMixin.java')
initializer = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java')
alchemy_init = read('alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java')
mixins = read('alchemy/src/main/resources/herbcraft_alchemy.mixins.json')
tooltips = read('alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java')

for token in ['POTION_BURDEN', 'potion_burden', 'PotionBurdenComponent.CODEC', 'PotionBurdenComponent.STREAM_CODEC']:
    require(token in components, f'AlchemyPotionComponents missing burden component token {token}')
for token in ['record PotionBurdenComponent', 'burden_weight', 'tail_weight', 'network.codec.StreamCodec']:
    require(token in component, f'PotionBurdenComponent missing token {token}')
for token in ['record BurdenRuntime', 'burdenRuntime()', 'burden_weight', 'tail_weight', 'burden_runtime']:
    require(token in rules_java, f'PotionProcessingRules missing burden token {token}')
for token in ['applyMetadata', 'onPotionConsumed', 'POTION_BURDEN', 'PlayerPotionBurdenState state', 'state.add', 'rememberLastPotionContext']:
    require(token in metadata, f'PotionBurdenRuntime missing token {token}')
for token in ['tickDecay', 'activeDecayAmount', 'tailDecayAmount', 'setDirect', 'copyFrom', 'clear()']:
    require(token in player_state, f'PlayerPotionBurdenState missing token {token}')
for token in ['ServerPlayerEvents.COPY_FROM', 'if (alive)', 'ServerTickEvents.END_LEVEL_TICK', 'tickDecay(gameTime']:
    require(token in player_runtime, f'PlayerPotionBurdenRuntime missing lifecycle token {token}')
for token in ['implements AlchemyPlayerData', 'HerbcraftAlchemyPotionBurden', 'addAdditionalSaveData', 'readAdditionalSaveData', 'clearPotionBurdenOnDeath']:
    require(token in player_mixin, f'AlchemyPlayerMixin missing persistence/death token {token}')
for token in ['finishUsingItem', 'HEAD', 'initializeIfNeeded', 'RETURN', 'onPotionConsumed']:
    require(token in item_mixin, f'AlchemyItemMixin missing use hook token {token}')
require('PlayerPotionBurdenRuntime.register();' in alchemy_init, 'HerbcraftAlchemy should register burden runtime lifecycle')
for token in ['AlchemyItemMixin', 'AlchemyPlayerMixin']:
    require(token in mixins, f'mixin config missing {token}')
for token in ['PotionBurdenRuntime.applyMetadata', 'needsBurdenMetadata']:
    require(token in initializer, f'AdvancedPotionStackInitializer missing burden metadata token {token}')
require('tooltip.herbcraft.potion.burden_weights' not in tooltips and 'POTION_BURDEN' not in tooltips, 'burden weights should stay out of the compact knowledge-gated item tooltip after Codex migration')

for lang in ['en_us', 'zh_cn']:
    lang_data = json.loads((ROOT / f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json').read_text(encoding='utf-8'))
    require('tooltip.herbcraft.potion.burden_weights' in lang_data, f'missing burden weight tooltip key in {lang}')

jar = ROOT / 'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z:
        names = set(z.namelist())
    for cls in [
        'com/herbcraft/alchemy/potion/PotionBurdenComponent.class',
        'com/herbcraft/alchemy/potion/PotionBurdenRuntime.class',
        'com/herbcraft/alchemy/potion/PlayerPotionBurdenState.class',
        'com/herbcraft/alchemy/potion/PlayerPotionBurdenRuntime.class',
        'com/herbcraft/alchemy/mixin/AlchemyItemMixin.class',
        'com/herbcraft/alchemy/mixin/AlchemyPlayerMixin.class',
    ]:
        require(cls in names, f'built Alchemy jar missing {cls}')

if errors:
    print('FAIL')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print('PASS - potion burden metadata, player burden persistence/decay, use hooks, lifecycle policy, and tooltip projection are wired for P5-P6.')
