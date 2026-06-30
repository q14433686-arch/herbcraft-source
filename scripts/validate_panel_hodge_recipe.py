#!/usr/bin/env python3
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
fail=[]
def check(name, cond):
    print(('PASS' if cond else 'FAIL')+' - '+name)
    if not cond: fail.append(name)
codex=(root/'core/src/main/java/com/herbcraft/knowledge/client/CodexScreen.java').read_text()
theme=json.loads((root/'core/src/main/resources/assets/herbcraft/ui/codex_theme.json').read_text())
for name, section, key, expected in [
    ('full-screen background half transparent','overlay','color','0x80000000'),
    ('main panel translucent','book','background_color','0x90181818'),
    ('header translucent','book','header_color','0xA0183018'),
    ('left panel translucent','left_panel','background_color','0xA0101010'),
    ('divider semi-transparent','divider','color','0xD0D0D0D0')]:
    check('codex theme '+name, theme.get(section,{}).get(key)==expected)
check('codex theme hover remains translucent', theme.get('left_panel',{}).get('hover_color','').startswith('0x40') or theme.get('left_panel',{}).get('hover_color','').startswith('0xB0'))
check('codex theme selected remains translucent', theme.get('left_panel',{}).get('selected_color','').startswith('0x60') or theme.get('left_panel',{}).get('selected_color','').startswith('0xB0'))
check('codex still does not pause', 'isPauseScreen()' in codex and 'return false;' in codex)
check('codex uses scissor clipping', 'enableScissor' in codex and 'disableScissor' in codex)
recipe=root/'cuisine/src/main/resources/data/herbcraft_cuisine/recipe/hodgepodge.json'
check('hodgepodge recipe json exists', recipe.exists())
if recipe.exists(): check('hodgepodge recipe uses custom serializer', json.loads(recipe.read_text())=={'type':'herbcraft_cuisine:hodgepodge'})
hrecipe=(root/'cuisine/src/main/java/com/herbcraft/cuisine/recipe/HodgepodgeRecipe.java').read_text()
check('recipe matches bowl + 3-4 herbs through data-backed rules', 'bowls == 1' in hrecipe and 'rules.minHerbs' in hrecipe and 'rules.normalMaxHerbs' in hrecipe)
check('recipe writes snapshot', 'HODGEPODGE_CONTENTS' in hrecipe and 'result.set' in hrecipe)
check('recipe checks herb tag', 'HODGEPODGE_HERBS' in hrecipe and 'TagKey.create' in hrecipe)
check('recipe checks HerbcraftAPI definition', 'HerbcraftAPI.getDefinition(item)' in hrecipe)
tag=root/'core/src/main/resources/data/herbcraft/tags/item/herb.json'
check('herb tag exists', tag.exists())
if tag.exists():
    data=json.loads(tag.read_text()); vals=set(data.get('values',[]))
    check('herb tag replace=false', data.get('replace') is False)
    for item in ['minecraft:dandelion','minecraft:oxeye_daisy','minecraft:cornflower','minecraft:poppy','minecraft:lily_of_the_valley','minecraft:azalea_leaves']:
        check('herb tag contains '+item, item in vals)
if fail: raise SystemExit('failed checks: '+', '.join(fail))
print('All panel translucency and hodgepodge recipe checks passed.')
