#!/usr/bin/env python3
from pathlib import Path
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
ALCH = ROOT / 'alchemy'
checks = []

relation = (ALCH / 'src/main/java/com/herbcraft/alchemy/potion/PotionRelationRuntime.java').read_text(encoding='utf-8')
for token in [
    'onPotionConsumed(ServerPlayer player, ItemStack stack)',
    'SevenEmotionType',
    'relationModifier',
    'XIANG_FAN',
    'XIANG_SHA',
    'XIANG_WEI',
    'XIANG_XU',
    'XIANG_SHI',
    'XIANG_E',
    'player.sendOverlayMessage',
    'state.recordDiscovery(',
]:
    checks.append((f'PotionRelationRuntime token {token}', token in relation))

burden = (ALCH / 'src/main/java/com/herbcraft/alchemy/potion/PotionBurdenRuntime.java').read_text(encoding='utf-8')
checks.append(('PotionBurdenRuntime hooks PotionRelationRuntime', 'PotionRelationRuntime.onPotionConsumed(player, stack);' in burden))

chapter = (ALCH / 'src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java').read_text(encoding='utf-8')
for token in [
    'book.herbcraft_alchemy.guide.potions.relation_integration',
    'book.herbcraft_alchemy.guide.quality_bonus.catalyst_semantics',
]:
    checks.append((f'AlchemyChapter token {token}', token in chapter))

zh = (ALCH / 'src/main/resources/assets/herbcraft/lang/zh_cn.json').read_text(encoding='utf-8')
en = (ALCH / 'src/main/resources/assets/herbcraft/lang/en_us.json').read_text(encoding='utf-8')
for text, name in [(zh, 'zh_cn.json'), (en, 'en_us.json')]:
    for key in [
        '"book.herbcraft_alchemy.guide.potions.relation_integration"',
        '"book.herbcraft_alchemy.guide.quality_bonus.catalyst_semantics"',
        '"tooltip.herbcraft.distilled_essence.orientation.purified.hint"',
        '"tooltip.herbcraft.distilled_essence.orientation.sustained.hint"',
        '"tooltip.herbcraft.distilled_essence.orientation.intense.hint"',
        '"tooltip.herbcraft.distilled_essence.orientation.harmonized.hint"',
    ]:
        checks.append((f'{name} contains {key}', key in text))

rules = (ALCH / 'src/main/resources/data/herbcraft_alchemy/distillation_rules.json').read_text(encoding='utf-8')
for token in ['minecraft:honeycomb', 'minecraft:redstone', 'minecraft:glowstone_dust', 'minecraft:amethyst_shard']:
    checks.append((f'distillation_rules token {token}', token in rules))

jar_path = ROOT / 'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar_path.exists():
    with zipfile.ZipFile(jar_path) as zf:
        names = set(zf.namelist())
    checks.append(('Jar contains PotionRelationRuntime.class', 'com/herbcraft/alchemy/potion/PotionRelationRuntime.class' in names))
else:
    checks.append(('Alchemy jar exists', False))

failed = [label for label, ok in checks if not ok]
if failed:
    print('FAIL')
    for label in failed:
        print(' -', label)
    sys.exit(1)
print('PASS')
