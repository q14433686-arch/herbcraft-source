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

facade = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java').read_text()
registration = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/HerbalChapterRegistration.java').read_text()
projection = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/HerbalChapterFoodNatureProjection.java').read_text()
guides = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/HerbalChapterGuides.java').read_text()
text = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/HerbalChapterTextBuilders.java').read_text()
classifier = (ROOT / 'core/src/main/java/com/herbcraft/knowledge/HerbalChapterClassifier.java').read_text()

require('HerbalChapterRegistration.register();' in facade, 'herbal chapter facade delegates registration')
require('HerbalChapterFoodNatureProjection.applyFoodNatureEntries();' in facade, 'herbal chapter facade delegates food-nature projection')
require('HerbalChapterFoodNatureProjection.foodNatureEntryId(itemId);' in facade, 'herbal chapter facade delegates food-nature entry id mapping')
require('HerbalChapterFoodNatureProjection.hasFoodNatureEntry(itemId);' in facade, 'herbal chapter facade delegates food-nature existence checks')
require('class HerbalChapterRegistration' in registration and 'CodexGuideRegistry.loadFromBundledJson' in registration, 'registration layer exists and owns guide JSON registration wiring')
require('class HerbalChapterFoodNatureProjection' in projection and 'Loaded {} food-nature entries into the Herbal Codex' in projection, 'food-nature projection layer exists')
require('class HerbalChapterGuides' in guides and 'registerCoreGuideProviders' in guides, 'guide-provider layer exists')
require('class HerbalChapterTextBuilders' in text and 'guideLines' in text and 'foodNatureLines' in text and 'overviewLines' in text, 'text-builder layer exists')
require('class HerbalChapterClassifier' in classifier and 'classify(String path)' in classifier, 'classifier layer exists')

if errs:
    raise SystemExit(1)
print('All HerbalChapter split phase 3 validation checks passed.')
