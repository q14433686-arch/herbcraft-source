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

rules_path = ROOT / 'alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json'
require(rules_path.exists(), 'missing potion_processing_rules.json')
if rules_path.exists():
    data = json.loads(rules_path.read_text(encoding='utf-8'))
    require(data.get('schema_version') == 1, 'potion_processing_rules.json schema_version must be 1')
    styles = {entry.get('id'): entry for entry in data.get('styles', [])}
    for style in ['sustained', 'intense', 'purified', 'murky_edge', 'harmonized']:
        require(style in styles, f'missing processing style {style}')
    for style, suffix, profile in [
        ('sustained', '_long', 'slow_tail'),
        ('intense', '_strong', 'sharp_peak'),
        ('purified', '_clarified', 'clean_stable'),
        ('murky_edge', '_murky', 'dirty_edge'),
    ]:
        entry = styles.get(style, {})
        require(suffix in entry.get('path_suffixes', []), f'{style} should map suffix {suffix}')
        require(entry.get('burden_profile') == profile, f'{style} should expose burden profile {profile}')
        require(entry.get('lang_key'), f'{style} missing lang_key')
        require(entry.get('burden_hint_key'), f'{style} missing burden_hint_key')
        for key in ['thermal_shift', 'qi_bias', 'clarity_level', 'relation_modifier', 'thermal_hint_key', 'qi_hint_key', 'clarity_hint_key', 'relation_hint_key']:
            require(entry.get(key), f'{style} missing P1 semantic key {key}')
        require(isinstance(entry.get('flavor_bias'), list) and entry.get('flavor_bias'), f'{style} should define non-empty flavor_bias')
        require(isinstance(entry.get('trait_affinity'), list) and entry.get('trait_affinity'), f'{style} should define non-empty trait_affinity')
    for style in ['sustained', 'intense', 'purified']:
        entry = styles.get(style, {})
        require(entry.get('first_batch') is True, f'{style} must be first_batch=true')
        require(entry.get('implemented') is True, f'{style} should be implemented=true after P4 runtime behavior pass')
        require(isinstance(entry.get('runtime'), dict), f'{style} should define runtime behavior after P4')
        require(entry.get('deferred') is False, f'{style} should not be deferred')
    for style in ['murky_edge', 'harmonized']:
        entry = styles.get(style, {})
        require(entry.get('first_batch') is False, f'{style} must not be first batch')
        require(entry.get('implemented') is False, f'{style} should be reserved, not implemented')
        require(entry.get('deferred') is True, f'{style} must be deferred=true')
    require({'undead_venom', 'cardiac_toxin', 'wither_venom_iii'} <= set(styles.get('murky_edge', {}).get('exact_paths', [])), 'murky_edge should map current risky special potion exact paths')

rules_java = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java')
initializer = read('alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java')
tooltips = read('alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java')
acquisition = read('alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java')
knowledge_support = read('alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyKnowledgeSupport.java')
alchemy_init = read('alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java')
verify = read('scripts/verify_built_jars.py')
alchemy_chapter = read('alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java')

for token in ['potion_processing_rules.json', 'record Style', 'record RuntimeProfile', 'processingStyleId(String potionPath)', 'isImplemented', 'isDeferred', 'path_suffixes', 'exact_paths', 'duration_multiplier', 'amplifier_delta']:
    require(token in rules_java, f'PotionProcessingRules missing token {token}')
require('PotionProcessingRules.loadFromBundledJson();' in alchemy_init, 'HerbcraftAlchemy should load potion processing rules')
for token in ['potionPath(ItemStack stack)', 'processingStyleId(ItemStack stack)', 'processingStyleId(PotionContents contents)', 'processingStyleId(String path)', 'PotionProcessingRules::processingStyleId']:
    require(token in initializer, f'AdvancedPotionStackInitializer missing style facade token {token}')
require('path.endsWith("_clarified")' in initializer and 'path.endsWith("_murky")' in initializer, 'current advanced potion path classification should remain intact')
require('path.endsWith("_long")' not in initializer.split('public static boolean isAdvancedPotionPath',1)[1].split('private static String inferSourceId',1)[0], 'P4 must not route _long through legacy advanced quality/bonus classification')
require('path.endsWith("_strong")' not in initializer.split('public static boolean isAdvancedPotionPath',1)[1].split('private static String inferSourceId',1)[0], 'P4 must not route _strong through legacy advanced quality/bonus classification')
require('PotionProcessingRuntime.apply' in initializer, 'AdvancedPotionStackInitializer should apply P4 runtime styles')
require('POTION_PROCESSING_STYLE' in initializer, 'AdvancedPotionStackInitializer should mark processed style to avoid repeated runtime rewriting')
for token in ['PotionProcessingRules', 'tooltip.herbcraft.potion.processing', 'tooltip.herbcraft.potion.source_essence', 'tooltip.herbcraft.potion.codex_hint_locked', 'tooltip.herbcraft.potion.codex_hint_tasted', 'tooltip.herbcraft.potion.codex_hint_mastered', 'PotionPharmacologyProjectionRuntime', 'style.deferred()', 'style.langKey()', 'legacyAdvancedPath']:
    require(token in tooltips, f'AdvancedPotionTooltips missing compact style projection token {token}')
for token in ['tooltip.herbcraft.potion.thermal_shift', 'tooltip.herbcraft.potion.qi_bias', 'tooltip.herbcraft.potion.clarity_level', 'tooltip.herbcraft.potion.relation_modifier', 'tooltip.herbcraft.potion.flavor_bias', 'tooltip.herbcraft.potion.trait_affinity']:
    require(token in alchemy_chapter, f'AlchemyChapter should carry detailed style token {token} after moving verbose tooltip content into Codex')
require('else if (legacyAdvancedPath)' in tooltips, 'uninitialized quality text must only display for legacy advanced paths, not long/strong style-only potions')
require('processingStyleId(potionName)' in acquisition or 'processingStyleId(potionName)' in knowledge_support, 'Alchemy progression should route by processing style id')
require('"purified"::equals' in knowledge_support and '"murky_edge"::equals' in knowledge_support, 'Alchemy taste/use progression should preserve clarified/murky grants through style ids')
require('awardPotionAdvancements' not in acquisition and 'awardPotionAdvancements' in knowledge_support, 'Potion style advancement grants should be on the taste/use path, not broad inventory acquisition')
require('data/herbcraft_alchemy/potion_processing_rules.json' in verify, 'verify_built_jars should check potion_processing_rules.json')
require('PotionProcessingRules.class' in verify, 'verify_built_jars should check PotionProcessingRules class')

require('book.herbcraft_alchemy.guide.potions.processing_semantics' in alchemy_chapter, 'AlchemyChapter should project P1 processing semantics into the potion guide')
require('book.herbcraft_alchemy.guide.quality_bonus.processing_meaning' in alchemy_chapter, 'AlchemyChapter should project P1 processing semantics into the quality/aftertaste guide')

for lang in ['en_us', 'zh_cn']:
    path = ROOT / f'alchemy/src/main/resources/assets/herbcraft/lang/{lang}.json'
    lang_data = json.loads(path.read_text(encoding='utf-8'))
    for key in [
        'tooltip.herbcraft.potion.processing',
        'tooltip.herbcraft.potion.processing.sustained',
        'tooltip.herbcraft.potion.processing.intense',
        'tooltip.herbcraft.potion.processing.purified',
        'tooltip.herbcraft.potion.processing.murky_edge',
        'tooltip.herbcraft.potion.processing.harmonized',
        'tooltip.herbcraft.potion.burden_profile',
        'tooltip.herbcraft.potion.thermal_shift',
        'tooltip.herbcraft.potion.qi_bias',
        'tooltip.herbcraft.potion.clarity_level',
        'tooltip.herbcraft.potion.relation_modifier',
        'tooltip.herbcraft.potion.flavor_bias',
        'tooltip.herbcraft.potion.trait_affinity',
        'tooltip.herbcraft.potion.burden_profile.slow_tail',
        'tooltip.herbcraft.potion.burden_profile.sharp_peak',
        'tooltip.herbcraft.potion.burden_profile.clean_stable',
        'tooltip.herbcraft.potion.burden_profile.dirty_edge',
        'tooltip.herbcraft.potion.burden_profile.harmonized_bearing',
        'tooltip.herbcraft.potion.thermal_shift.warming_tail',
        'tooltip.herbcraft.potion.thermal_shift.heated_spike',
        'tooltip.herbcraft.potion.thermal_shift.tempered_clear',
        'tooltip.herbcraft.potion.thermal_shift.unstable_skew',
        'tooltip.herbcraft.potion.thermal_shift.balanced_middle',
        'tooltip.herbcraft.potion.qi_bias.lingering_extension',
        'tooltip.herbcraft.potion.qi_bias.forced_surge',
        'tooltip.herbcraft.potion.qi_bias.settled_clarity',
        'tooltip.herbcraft.potion.qi_bias.turbid_deviation',
        'tooltip.herbcraft.potion.qi_bias.coordinated_flow',
        'tooltip.herbcraft.potion.clarity_level.drawn_out',
        'tooltip.herbcraft.potion.clarity_level.compressed_edge',
        'tooltip.herbcraft.potion.clarity_level.purged_turbidity',
        'tooltip.herbcraft.potion.clarity_level.muddy_unsettled',
        'tooltip.herbcraft.potion.clarity_level.layered_harmony',
        'tooltip.herbcraft.potion.relation_modifier.tail_carrying',
        'tooltip.herbcraft.potion.relation_modifier.conflict_amplifying',
        'tooltip.herbcraft.potion.relation_modifier.conflict_buffering',
        'tooltip.herbcraft.potion.relation_modifier.volatile_conflict',
        'tooltip.herbcraft.potion.relation_modifier.compatibility_weaving',
        'book.herbcraft_alchemy.guide.potions.processing_semantics',
        'book.herbcraft_alchemy.guide.quality_bonus.processing_meaning',
    ]:
        require(key in lang_data, f'missing {lang} lang key {key}')

jar = ROOT / 'alchemy/build/libs/herbcraft-alchemy-1.1.3.jar'
if jar.exists():
    with zipfile.ZipFile(jar) as z:
        names = set(z.namelist())
    require('data/herbcraft_alchemy/potion_processing_rules.json' in names, 'built Alchemy jar missing potion_processing_rules.json')
    require('com/herbcraft/alchemy/potion/PotionProcessingRules.class' in names, 'built Alchemy jar missing PotionProcessingRules.class')

if errors:
    print('FAIL')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print('PASS - potion processing styles are data-backed, loaded, projected to tooltips/acquisition, and wired for P4 runtime without routing long/strong through legacy quality/bonus classification.')
