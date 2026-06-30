#!/usr/bin/env python3
import json, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]
ADV=[('herbcraft',ROOT/'core/src/main/resources/data/herbcraft/advancement'),('herbcraft_alchemy',ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/advancement'),('herbcraft_cuisine',ROOT/'cuisine/src/main/resources/data/herbcraft_cuisine/advancement')]
LANG={'herbcraft':[ROOT/'core/src/main/resources/assets/herbcraft/lang/zh_cn.json',ROOT/'core/src/main/resources/assets/herbcraft/lang/en_us.json'],'herbcraft_alchemy':[ROOT/'alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json',ROOT/'alchemy/src/main/resources/assets/herbcraft/lang/en_us.json'],'herbcraft_cuisine':[ROOT/'cuisine/src/main/resources/assets/herbcraft_cuisine/lang/zh_cn.json',ROOT/'cuisine/src/main/resources/assets/herbcraft_cuisine/lang/en_us.json']}
all={}; roots=[]; keys=[]
for ns,d in ADV:
 for p in sorted(d.glob('*.json')):
  data=json.loads(p.read_text(encoding='utf-8')); aid=f'{ns}:{p.stem}'; all[aid]=data
  if 'parent' not in data: roots.append(aid)
  if data.get('criteria',{}).get('trigger',{}).get('trigger')!='minecraft:impossible': errors.append(f'{aid} criterion not impossible')
  if data.get('requirements') != [['trigger']]: errors.append(f'{aid} bad requirements')
  for f in ['title','description']:
   k=data.get('display',{}).get(f,{}).get('translate')
   if not k: errors.append(f'{aid} missing {f}')
   else:
    keys.append((ns,k,aid))
    if k.startswith('advancement.'): errors.append(f'{aid} uses old singular {k}')
    if not k.startswith('advancements.'): errors.append(f'{aid} bad key {k}')
if roots != ['herbcraft:root']: errors.append(f'expected one root herbcraft:root got {roots}')
for aid,data in all.items():
 if aid=='herbcraft:root': continue
 cur=data.get('parent'); seen={aid}
 while cur and cur!='herbcraft:root':
  if cur in seen: errors.append(f'{aid} cycle'); break
  seen.add(cur); pd=all.get(cur)
  if not pd: errors.append(f'{aid} missing parent {cur}'); break
  cur=pd.get('parent')
 if not cur: errors.append(f'{aid} chain missing root')
for ns,k,aid in keys:
 for lp in LANG[ns]:
  if k not in json.loads(lp.read_text(encoding='utf-8')): errors.append(f'{aid} missing lang {k} in {lp}')

for lp in LANG['herbcraft']:
 data=json.loads(lp.read_text(encoding='utf-8'))
 for k in ['book.herbcraft.guide.recognizing.title','book.herbcraft.guide.knowledge_states.title','book.herbcraft.guide.flavors.title','book.herbcraft.guide.flavors.line2','book.herbcraft.guide.seven_emotions.title','book.herbcraft.guide.seven_emotions.line2','book.herbcraft.guide.herb_stack.title','book.herbcraft.guide.ingredients.title','book.herbcraft.guide.ingredients.line2','book.herbcraft.guide.ingredients.progress','book.herbcraft.guide.errata.title','book.herbcraft.guide.errata.line2','book.herbcraft.guide.errata.progress','book.herbcraft.ingredients.overview.title','book.herbcraft.section.ingredients.overview','book.herbcraft.guide.flavors.tasted_once','book.herbcraft.guide.seven.xiang_xu','book.herbcraft.guide.seven.xiang_shi','book.herbcraft.guide.seven.xiang_fan','book.herbcraft.guide.seven.single']:
  if k not in data: errors.append(f'missing Codex guide lang {k} in {lp}')
 for forbidden in ['book.herbcraft.guide.cuisine.title','book.herbcraft.guide.alchemy.title']:
  if forbidden in data: errors.append(f'Core lang should not own addon guide key {forbidden} in {lp}')
for ns, required in {'herbcraft_cuisine':['book.herbcraft_cuisine.guide.cuisine.title','book.herbcraft_cuisine.guide.cuisine.line1'], 'herbcraft_alchemy':['book.herbcraft_alchemy.guide.alchemy.title','book.herbcraft_alchemy.guide.alchemy.line1']}.items():
 for lp in LANG[ns]:
  data=json.loads(lp.read_text(encoding='utf-8'))
  for k in required:
   if k not in data: errors.append(f'missing addon Codex guide lang {k} in {lp}')
  if ns == 'herbcraft_cuisine':
   catalyst_text = ' '.join(data.get(k, '') for k in ['book.herbcraft_cuisine.guide.hidden_catalyst.line1','book.herbcraft_cuisine.guide.hidden_catalyst.hint','book.herbcraft_cuisine.guide.hidden_catalyst.deadly'])
   if lp.name == 'zh_cn.json' and '炼金分册' not in catalyst_text:
    errors.append(f'Cuisine hidden catalyst guide should mention 炼金分册 in {lp}')
   if lp.name == 'en_us.json' and 'Alchemy chapter' not in catalyst_text:
    errors.append(f'Cuisine hidden catalyst guide should mention Alchemy chapter in {lp}')

def load_json(rel):
 p=ROOT/rel
 if not p.exists(): errors.append(f'missing {rel}'); return {}
 return json.loads(p.read_text(encoding='utf-8'))
core_guides=load_json('core/src/main/resources/data/herbcraft/codex_guides.json').get('entries',[])
cuisine_guides=load_json('cuisine/src/main/resources/data/herbcraft_cuisine/codex_guides.json').get('entries',[])
alchemy_guides=load_json('alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json').get('entries',[])
core_ids={e.get('id') for e in core_guides}
if {'guide_cuisine','guide_alchemy'} & core_ids: errors.append('Core codex_guides.json must not include addon guide entries')
for required in ['guide_recognizing','guide_knowledge_states','guide_flavors','guide_seven_emotions','guide_herb_stack','guide_ingredients','guide_errata']:
 if required not in core_ids: errors.append(f'core codex_guides.json missing {required}')
cuisine_expected={'guide_cuisine','guide_cuisine_overview','guide_cuisine_water_bowl','guide_cuisine_dishes','guide_cuisine_hodgepodge','guide_cuisine_immunity','guide_cuisine_hidden_catalyst'}
alchemy_expected={'guide_alchemy','guide_alchemy_overview','guide_alchemy_essences','guide_alchemy_potions','guide_alchemy_clarified_murky','guide_alchemy_quality_bonus','guide_alchemy_coatings','guide_alchemy_armor_support','guide_alchemy_witch'}
if {e.get('id') for e in cuisine_guides} != cuisine_expected: errors.append('Cuisine codex_guides.json should contain gateway plus Cuisine overview guide entries')
if {e.get('id') for e in alchemy_guides} != alchemy_expected: errors.append('Alchemy codex_guides.json should contain gateway plus Alchemy overview guide entries')
for entries, rel in [(core_guides,'core codex guides'),(cuisine_guides,'cuisine codex guides'),(alchemy_guides,'alchemy codex guides')]:
 for e in entries:
  if not e.get('id','').startswith('guide_'): errors.append(f'{rel} entry id must start guide_: {e}')
  if e.get('always_visible') is not True: errors.append(f'{rel} entry should be always_visible=true: {e}')
for e in core_guides:
 if e.get('chapter')!='herbal' or e.get('section')!='overview': errors.append(f'core guide should target herbal/overview: {e}')
for e in cuisine_guides:
 if e.get('id')=='guide_cuisine':
  if e.get('chapter')!='herbal' or e.get('section')!='overview': errors.append(f'Cuisine gateway should target herbal/overview: {e}')
 elif e.get('chapter')!='cuisine' or e.get('section')!='overview': errors.append(f'Cuisine module guide should target cuisine/overview: {e}')
for e in alchemy_guides:
 if e.get('id')=='guide_alchemy':
  if e.get('chapter')!='herbal' or e.get('section')!='overview': errors.append(f'Alchemy gateway should target herbal/overview: {e}')
 elif e.get('chapter')!='alchemy' or e.get('section')!='overview': errors.append(f'Alchemy module guide should target alchemy/overview: {e}')
if len(all)!=35: errors.append(f'expected 35 advancements got {len(all)}')
if 'herbcraft:gui/advancements/backgrounds/herbcraft' != all.get('herbcraft:root',{}).get('display',{}).get('background'): errors.append('root background must use 1.21.5+ texture-id format herbcraft:gui/advancements/backgrounds/herbcraft')
for aid,data in all.items():
 bg=data.get('display',{}).get('background')
 if bg and ('textures/' in bg or bg.endswith('.png')): errors.append(f'{aid} background uses pre-1.21.5 format (textures/ prefix or .png suffix): {bg}')
if not (ROOT/'core/src/main/resources/assets/herbcraft/textures/gui/advancements/backgrounds/herbcraft.png').exists(): errors.append('root background png missing')
for rel, needles in {
 'core/src/main/java/com/herbcraft/knowledge/CodexGuideRegistry.java':['loadFromBundledJson','registerProvider','always_visible'],
 'core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java':['"overview"','overviewLines()','book.herbcraft.entry.overview.title','registerCoreGuideProviders()','CodexGuideRegistry.loadFromBundledJson("data/herbcraft/codex_guides.json")','book.herbcraft.guide.flavors.line2','book.herbcraft.guide.seven_emotions.line2','addFlavorPrinciples','addSevenEmotionPrinciples'],
 'cuisine/src/main/java/com/herbcraft/cuisine/knowledge/CuisineChapter.java':['KnowledgeAPI.registerSection("cuisine", "overview"','CodexGuideRegistry.loadFromBundledJson("data/herbcraft_cuisine/codex_guides.json")','book.herbcraft_cuisine.guide.overview.line1','book.herbcraft_cuisine.guide.hidden_catalyst.line1'],
 'alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java':['KnowledgeAPI.registerSection("alchemy", "overview"','CodexGuideRegistry.loadFromBundledJson("data/herbcraft_alchemy/codex_guides.json")','book.herbcraft_alchemy.guide.overview.line1','book.herbcraft_alchemy.guide.witch.line1'],
 'core/src/main/java/com/herbcraft/knowledge/IngredientsChapter.java':['"overview"','ingredients.overview.title','overviewLines()'],
 'core/src/main/java/com/herbcraft/api/KnowledgeAPI.java':['entry.id().startsWith("guide_")'],
 'core/src/main/java/com/herbcraft/advancement/HerbcraftAdvancements.java':['player.getAdvancements().award'],
 'core/src/main/java/com/herbcraft/advancement/HerbcraftAdvancementHooks.java':['KnowledgeInventoryScanner.registerHandler','first_bite']
}.items():
 p=ROOT/rel
 if not p.exists(): errors.append(f'missing {rel}'); continue
 t=p.read_text(encoding='utf-8')
 if rel == 'core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java':
  t += '\n' + '\n'.join(path.read_text(encoding='utf-8') for path in (ROOT/'core/src/main/java/com/herbcraft/knowledge').glob('HerbalChapter*.java'))
 for n in needles:
  if n not in t: errors.append(f'{rel} missing {n}')
herbal_text=(ROOT/'core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java').read_text(encoding='utf-8')
if 'registerGuideEntries()' in herbal_text: errors.append('HerbalChapter should not keep hardcoded registerGuideEntries()')
if '"cuisine"' in herbal_text or '"alchemy"' in herbal_text: errors.append('Core HerbalChapter should not hardcode addon guide kinds')
if 'HerbNatureAPI.natureLine(first.nature())' in herbal_text: errors.append('Guide flavor page should not print exact first tasted item nature')
pharm_state=(ROOT/'core/src/main/java/com/herbcraft/pharmacology/PlayerPharmacologyState.java').read_text(encoding='utf-8')
for token in ['PrincipleDiscovery','recordDiscovery','split("#", 3)']:
 if token not in pharm_state: errors.append(f'PlayerPharmacologyState missing principle discovery token {token}')
pharm_resolver=(ROOT/'core/src/main/java/com/herbcraft/pharmacology/PharmacologyResolver.java').read_text(encoding='utf-8')
for token in ['flavor_bitter','flavor_pungent','seven_xiang_xu','seven_xiang_fan','seven_single','temperature_harmonize','recordDiscovery']:
 if token not in pharm_resolver: errors.append(f'PharmacologyResolver missing discovery token {token}')
if errors:
 print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - P1 codex overview, data-driven guide registration, principle discovery hooks, and unified advancement tree validation passed.')
