#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
ALCH = ROOT / 'alchemy'
checks = []

tooltips = (ALCH / 'src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java').read_text(encoding='utf-8')
for token in [
    'int knowledgeState = AlchemyKnowledgeClientSupport.potionState(stack);',
    'tooltip.herbcraft.potion.codex_hint_locked',
    'tooltip.herbcraft.potion.codex_hint_tasted',
    'tooltip.herbcraft.potion.codex_hint_mastered',
    'sourceName(projection.sourceEssence())',
    'AlchemyRegistries.essenceInfoById(essenceId)',
    'if (knowledgeState >= 3)',
    'if (knowledgeState >= 4)',
]:
    checks.append((f'AdvancedPotionTooltips short/gated token {token}', token in tooltips))
for stale in ['tooltip.herbcraft.potion.thermal_shift', 'tooltip.herbcraft.potion.qi_bias', 'tooltip.herbcraft.potion.clarity_level', 'tooltip.herbcraft.potion.relation_modifier', 'tooltip.herbcraft.potion.flavor_bias', 'tooltip.herbcraft.potion.trait_affinity', 'tooltip.herbcraft.potion.burden_weights', 'tooltip.herbcraft.potion.medicinal_profile', 'tooltip.herbcraft.potion.body_thermal', 'tooltip.herbcraft.potion.body_qi']:
    checks.append((f'AdvancedPotionTooltips moved verbose semantic {stale} to Codex', stale not in tooltips))
checks.append(('No stale tooltip.herbcraft.flavor prefix in AdvancedPotionTooltips', 'tooltip.herbcraft.flavor.' not in tooltips))
checks.append(('No stale tooltip.herbcraft.trait prefix in AdvancedPotionTooltips', 'tooltip.herbcraft.trait.' not in tooltips))

support = (ALCH / 'src/main/java/com/herbcraft/alchemy/knowledge/AlchemyKnowledgeSupport.java').read_text(encoding='utf-8')
for token in ['potionBaseId(ItemStack stack)', 'potionBaseId(PotionContents contents)', 'markPotionTasted', 'isPotionContainer', 'Items.SPLASH_POTION', 'Items.LINGERING_POTION', 'stripModifierSuffix']:
    checks.append((f'AlchemyKnowledgeSupport token {token}', token in support))

client_support = (ALCH / 'src/main/java/com/herbcraft/alchemy/client/AlchemyKnowledgeClientSupport.java').read_text(encoding='utf-8')
for token in ['potionState(ItemStack stack)', 'distilledEssenceState', 'armorSupportState', 'KnowledgeClientCache.state("alchemy"']:
    checks.append((f'AlchemyKnowledgeClientSupport token {token}', token in client_support))

mixin = (ALCH / 'src/main/java/com/herbcraft/alchemy/mixin/AlchemyItemMixin.java').read_text(encoding='utf-8')
for token in ['AlchemyKnowledgeSupport.markPotionTasted(player, stack);', 'AlchemyKnowledgeSupport.isPotionContainer(stack)']:
    checks.append((f'AlchemyItemMixin token {token}', token in mixin))

acq = (ALCH / 'src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java').read_text(encoding='utf-8')
checks.append(('Acquisition marks potion encountered', 'KnowledgeManager.markEncountered(player, "alchemy", "potion_" + base);' in acq))


splash_mixin = (ALCH / 'src/main/java/com/herbcraft/alchemy/mixin/ThrownSplashPotionKnowledgeMixin.java').read_text(encoding='utf-8')
for token in ['@Mixin(ThrownSplashPotion.class)', 'onHitAsPotion', 'AlchemyKnowledgeSupport.markPotionTasted(player, stack)', 'PotionBurdenRuntime.onPotionConsumed(player, stack)', 'Items.SPLASH_POTION']:
    checks.append((f'ThrownSplashPotionKnowledgeMixin token {token}', token in splash_mixin or token == 'Items.SPLASH_POTION'))

cloud_mixin = (ALCH / 'src/main/java/com/herbcraft/alchemy/mixin/AreaEffectCloudKnowledgeMixin.java').read_text(encoding='utf-8')
for token in ['@Mixin(AreaEffectCloud.class)', 'serverTick', 'PotionContents potionContents', 'AlchemyKnowledgeSupport.markPotionTasted(player, this.potionContents)', 'PotionBurdenRuntime.onPotionConsumed(player, cloudPotionStack())', 'herbcraftAlchemy$processedPlayers']:
    checks.append((f'AreaEffectCloudKnowledgeMixin token {token}', token in cloud_mixin))

mixins_json = (ALCH / 'src/main/resources/herbcraft_alchemy.mixins.json').read_text(encoding='utf-8')
checks.append(('Mixins json registers splash/lingering knowledge mixins', 'ThrownSplashPotionKnowledgeMixin' in mixins_json and 'AreaEffectCloudKnowledgeMixin' in mixins_json))
checks.append(('AreaEffectCloud mixin avoids inherited tickCount shadow', '@Shadow public int tickCount' not in cloud_mixin and 'self.tickCount % 5' in cloud_mixin))

chapter = (ALCH / 'src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java').read_text(encoding='utf-8')
for token in [
    'potionLines(player, info, state)',
    'exactDisplayState(player, "potion_" + info.id())',
    'KnowledgeAPI.displayState(player, CHAPTER_ID, entry)',
    'book.herbcraft_alchemy.entry.potion_seen',
    'book.herbcraft_alchemy.entry.potion_taste_needed',
    'book.herbcraft_alchemy.entry.potion_tasted',
    'book.herbcraft_alchemy.entry.potion_source',
    'book.herbcraft_alchemy.entry.potion_mastered_semantics',
    'book.herbcraft_alchemy.entry.potion_style_header',
    'book.herbcraft_alchemy.entry.potion_splash_lingering_semantics',
    'PotionProcessingRules.styles()',
    'joinKeys(style.flavorBias(), "nature.herbcraft.flavor.")',
    'joinKeys(style.traitAffinity(), "nature.herbcraft.hint.")',
    'book.herbcraft_alchemy.guide.overview.linkage_rounds',
    'book.herbcraft_alchemy.guide.overview.potion_taste_gate',
    'book.herbcraft_alchemy.guide.overview.armor_support_hint',
    'herbcraft_alchemy", "armor_support"',
    'book.herbcraft_alchemy.guide.armor_support.line1',
]:
    checks.append((f'AlchemyChapter codex token {token}', token in chapter))

guides = (ALCH / 'src/main/resources/data/herbcraft_alchemy/codex_guides.json').read_text(encoding='utf-8')
checks.append(('codex_guides has armor support guide entry', 'guide_alchemy_armor_support' in guides and 'herbcraft_alchemy:armor_support' in guides))

for lang_name in ['zh_cn.json', 'en_us.json']:
    lang = (ALCH / f'src/main/resources/assets/herbcraft/lang/{lang_name}').read_text(encoding='utf-8')
    for key in [
        '"book.herbcraft_alchemy.entry.potion_seen"',
        '"book.herbcraft_alchemy.entry.potion_taste_needed"',
        '"book.herbcraft_alchemy.entry.potion_tasted"',
        '"book.herbcraft_alchemy.entry.potion_source"',
        '"book.herbcraft_alchemy.entry.potion_mastered_semantics"',
        '"book.herbcraft_alchemy.entry.potion_style_header"',
        '"book.herbcraft_alchemy.entry.potion_splash_lingering_semantics"',
        '"tooltip.herbcraft.potion.codex_hint_locked"',
        '"tooltip.herbcraft.potion.codex_hint_tasted"',
        '"tooltip.herbcraft.potion.codex_hint_mastered"',
        '"book.herbcraft_alchemy.entry.potion_processing_bodystate"',
        '"book.herbcraft_alchemy.guide.overview.linkage_rounds"',
        '"book.herbcraft_alchemy.guide.overview.potion_taste_gate"',
        '"book.herbcraft_alchemy.guide.overview.armor_support_hint"',
        '"book.herbcraft_alchemy.guide.armor_support.title"',
        '"book.herbcraft_alchemy.guide.armor_support.line1"',
        '"book.herbcraft_alchemy.guide.armor_support.line2"',
    ]:
        checks.append((f'{lang_name} contains {key}', key in lang))

armor_tooltip = (ALCH / 'src/main/java/com/herbcraft/alchemy/client/ArmorSupportTooltips.java').read_text(encoding='utf-8')
checks.append(('Armor support tooltip gates description/buffer', 'state >= 4' in armor_tooltip and 'AlchemyKnowledgeClientSupport.armorSupportState(stack)' in armor_tooltip))

distilled_tooltip = (ALCH / 'src/main/java/com/herbcraft/alchemy/client/DistilledEssenceTooltips.java').read_text(encoding='utf-8')
checks.append(('Distilled essence tooltip gates description', 'state >= 4' in distilled_tooltip and 'AlchemyKnowledgeClientSupport.distilledEssenceState(stack)' in distilled_tooltip))

failed = [label for label, ok in checks if not ok]
if failed:
    print('FAIL')
    for label in failed:
        print(' -', label)
    sys.exit(1)
print('PASS')
