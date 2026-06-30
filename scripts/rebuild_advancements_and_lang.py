#!/usr/bin/env python3
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2)+'\n')

def adv(ns,path,parent,icon,title,desc,trigger='minecraft:impossible',conditions=None,hidden=False,background=False):
    d={
        'criteria': {'trigger': {'trigger': trigger}},
        'display': {
            'title': {'translate': title},
            'description': {'translate': desc},
            'icon': {'id': icon},
            'frame': 'task',
            'show_toast': True,
            'announce_to_chat': True,
            'hidden': hidden
        },
        'requirements': [['trigger']]
    }
    if conditions is not None: d['criteria']['trigger']['conditions']=conditions
    if parent: d['parent']=parent
    if background:
        d['display']['background']='minecraft:gui/advancements/backgrounds/stone'
        d['display']['show_toast']=False
        d['display']['announce_to_chat']=False
    mod={'herbcraft':'core','herbcraft_cuisine':'cuisine','herbcraft_alchemy':'alchemy'}[ns]
    write(ROOT/f'{mod}/src/main/resources/data/{ns}/advancement/{path}.json', d)

# clean old advancement dirs
for p in [ROOT/'core/src/main/resources/data/herbcraft/advancement', ROOT/'cuisine/src/main/resources/data/herbcraft_cuisine/advancement', ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/advancement']:
    if p.exists():
        for f in p.glob('*.json'): f.unlink()

# Core and knowledge, one tree under herbcraft:root
adv('herbcraft','root',None,'herbcraft:herbal_codex','advancements.herbcraft.root.title','advancements.herbcraft.root.description','minecraft:inventory_changed',{'items':[{'items':'herbcraft:herbal_codex'}]},False,True)
adv('herbcraft','first_bite','herbcraft:root','minecraft:dandelion','advancements.herbcraft.first_bite.title','advancements.herbcraft.first_bite.description','minecraft:consume_item',{'item':{'items':'#herbcraft:herb'}})
adv('herbcraft','eat_grass','herbcraft:first_bite','minecraft:short_grass','advancements.herbcraft.eat_grass.title','advancements.herbcraft.eat_grass.description','minecraft:consume_item',{'item':{'items':['minecraft:short_grass','minecraft:tall_grass']}})
adv('herbcraft','bitter_medicine','herbcraft:first_bite','minecraft:wither_rose','advancements.herbcraft.bitter_medicine.title','advancements.herbcraft.bitter_medicine.description')
adv('herbcraft','counter_poison','herbcraft:bitter_medicine','minecraft:fermented_spider_eye','advancements.herbcraft.counter_poison.title','advancements.herbcraft.counter_poison.description')
adv('herbcraft','herb_stack_3','herbcraft:first_bite','minecraft:fern','advancements.herbcraft.herb_stack_3.title','advancements.herbcraft.herb_stack_3.description')
adv('herbcraft','herb_stack_5','herbcraft:herb_stack_3','minecraft:large_fern','advancements.herbcraft.herb_stack_5.title','advancements.herbcraft.herb_stack_5.description')
adv('herbcraft','herb_stack_8','herbcraft:herb_stack_5','minecraft:spore_blossom','advancements.herbcraft.herb_stack_8.title','advancements.herbcraft.herb_stack_8.description')
adv('herbcraft','herb_overload','herbcraft:herb_stack_8','minecraft:poisonous_potato','advancements.herbcraft.herb_overload.title','advancements.herbcraft.herb_overload.description')
adv('herbcraft','feed_cow_grass','herbcraft:first_bite','minecraft:wheat','advancements.herbcraft.feed_cow_grass.title','advancements.herbcraft.feed_cow_grass.description','minecraft:bred_animals')
adv('herbcraft','open_codex','herbcraft:root','herbcraft:herbal_codex','advancements.herbcraft.open_codex.title','advancements.herbcraft.open_codex.description')
adv('herbcraft','first_page','herbcraft:open_codex','herbcraft:tattered_page','advancements.herbcraft.first_page.title','advancements.herbcraft.first_page.description','minecraft:consume_item',{'item':{'items':'herbcraft:tattered_page'}})
adv('herbcraft','master_first_entry','herbcraft:first_page','minecraft:experience_bottle','advancements.herbcraft.master_first_entry.title','advancements.herbcraft.master_first_entry.description')
adv('herbcraft','master_25_entries','herbcraft:master_first_entry','minecraft:bookshelf','advancements.herbcraft.master_25_entries.title','advancements.herbcraft.master_25_entries.description')
adv('herbcraft','master_all_core','herbcraft:master_25_entries','minecraft:nether_star','advancements.herbcraft.master_all_core.title','advancements.herbcraft.master_all_core.description',hidden=True)
adv('herbcraft','villager_trade_herbcraft','herbcraft:root','minecraft:emerald','advancements.herbcraft.villager_trade_herbcraft.title','advancements.herbcraft.villager_trade_herbcraft.description')

# Cuisine still parented to herbcraft root so no separate tab root.
for name, icon, parent, hidden in [
    ('first_dish','minecraft:bowl','herbcraft:root',False),
    ('water_bowl','herbcraft_cuisine:water_bowl','herbcraft_cuisine:first_dish',False),
    ('first_hodgepodge','herbcraft_cuisine:hodgepodge','herbcraft_cuisine:water_bowl',False),
    ('immunity_meal','minecraft:milk_bucket','herbcraft_cuisine:first_dish',False),
    ('hundred_flower_feast','herbcraft_cuisine:hundred_flower_feast','herbcraft_cuisine:first_dish',True),
    ('deadly_hodgepodge','herbcraft_cuisine:deadly_hodgepodge','herbcraft_cuisine:first_dish',True)]:
    trig='minecraft:inventory_changed'; cond={'items':[{'items':icon}]}
    if name in ('immunity_meal','deadly_hodgepodge'): trig='minecraft:impossible'; cond=None
    if name=='first_hodgepodge': trig='minecraft:recipe_crafted'; cond={'recipe_id':'herbcraft_cuisine:hodgepodge'}
    adv('herbcraft_cuisine',name,parent,icon,f'advancements.herbcraft_cuisine.{name}.title',f'advancements.herbcraft_cuisine.{name}.description',trig,cond,hidden)

# Alchemy parented to herbcraft root.
for name, icon, parent, hidden in [
    ('first_essence','herbcraft:essence_dandelion','herbcraft:root',False),
    ('first_distilled_essence','herbcraft:distilled_essence','herbcraft_alchemy:first_essence',False),
    ('first_potion','minecraft:potion','herbcraft_alchemy:first_essence',False),
    ('first_clarified','minecraft:potion','herbcraft_alchemy:first_potion',False),
    ('first_sustained','minecraft:redstone','herbcraft_alchemy:first_potion',False),
    ('first_intense','minecraft:glowstone_dust','herbcraft_alchemy:first_potion',False),
    ('first_murky','minecraft:potion','herbcraft_alchemy:first_potion',False),
    ('epic_potion','minecraft:potion','herbcraft_alchemy:first_clarified',True),
    ('first_coating','minecraft:iron_sword','herbcraft_alchemy:first_essence',False),
    ('first_armor_support','minecraft:iron_chestplate','herbcraft_alchemy:first_distilled_essence',False),
    ('undead_toxin','minecraft:potion','herbcraft_alchemy:first_murky',True),
    ('witch_knowledge','herbcraft:tattered_page','herbcraft_alchemy:first_essence',True),
    ('survive_witch_brew','minecraft:splash_potion','herbcraft_alchemy:witch_knowledge',True)]:
    trig='minecraft:inventory_changed' if name in ('first_essence',) else 'minecraft:impossible'
    cond={'items':[{'items':icon}]} if trig=='minecraft:inventory_changed' else None
    adv('herbcraft_alchemy',name,parent,icon,f'advancements.herbcraft_alchemy.{name}.title',f'advancements.herbcraft_alchemy.{name}.description',trig,cond,hidden)

core_lang={
'root':('百草可食','从一片叶、一碗汤、一瓶药开始。','Herbcraft','From leaf, bowl, and bottle.'),'first_bite':('浅尝辄止','第一次食用任意 Herbcraft 登记的可食原料。','First Bite','Eat any Herbcraft edible ingredient.'),'eat_grass':('真·吃草','食用短草或高草。','Actually Eating Grass','Eat short grass or tall grass.'),'bitter_medicine':('良药苦口','食用带负面效果的草药并活下来。','Bitter Medicine','Survive after eating a harmful herb.'),'counter_poison':('以毒攻毒','通过 Herbcraft 食物或草药清除负面效果。','Counter Poison with Poison','Clear a harmful effect through Herbcraft.'),'herb_stack_3':('药性初起','药性蓄积达到 3 层。','Potency Stirs','Reach herb stack 3.'),'herb_stack_5':('药力入脉','药性蓄积达到 5 层。','Potency in the Veins','Reach herb stack 5.'),'herb_stack_8':('满溢之前','药性蓄积达到 8 层。','Before Overflow','Reach herb stack 8.'),'herb_overload':('是药三分毒','触发药性过载。','Every Medicine Has Poison','Trigger herbal overload.'),'feed_cow_grass':('它本来就该吃这个','用草类植物喂养牛或羊。','It Should Eat This','Feed cattle or sheep with grass.'),'open_codex':('翻开百草经','打开百草经界面。','Open the Codex','Open the Herbal Codex.'),'first_page':('残页余墨','使用任意染渍残页。','Ink on a Tattered Page','Use any tattered page.'),'master_first_entry':('亲尝才知','任意知识条目达到谙熟。','Taste Teaches','Master any knowledge entry.'),'master_25_entries':('半部百草','25 个知识条目达到谙熟。','Half a Codex','Master 25 entries.'),'master_all_core':('百草谙熟','谙熟所有核心百草条目。','Master of Herbs','Master every core herb entry.'),'villager_trade_herbcraft':('草药也能卖钱','完成任意 Herbcraft 村民交易。','Herbs Have a Price','Complete any Herbcraft villager trade.')}
extra_core={'zh_cn':{'book.herbcraft.section.herbal.overview':'总纲','book.herbcraft.entry.overview.title':'百草经总纲','book.herbcraft.overview.line1':'百草可食并非鼓励乱吃，而是记录草木、药性与膳房的关系。','book.herbcraft.overview.line2':'空碗 + 3~4 味草药，可以乱炖成百草盅。','book.herbcraft.overview.line3':'空碗只走百草盅或原版炖菜；水碗不会触发乱炖。','book.herbcraft.overview.line4':'水碗用于调和固定药膳：先成生坯，再经火候烹熟。','book.herbcraft.overview.line5':'残页能补全听闻，有些秘膳只会在残页或实践后显露。','book.herbcraft.overview.line6':'连续食用会积蓄药性，力量会增强，也可能反噬。','book.herbcraft.usage.heard':'用途隐约可辨：可食、可入盅，或许还可入膳。','book.herbcraft.usage.mastered.raw':'用途：可生食，具体药性如下。','book.herbcraft.usage.mastered.hodgepodge':'空碗 + 3~4 味草药 → 百草盅。','book.herbcraft.usage.mastered.water_bowl':'水碗 + 指定材料 → 药膳生坯。','book.herbcraft.usage.mastered.hidden':'若与秘膳有关，需残页或亲手实践才会显露全貌。'},'en_us':{'book.herbcraft.section.herbal.overview':'Overview','book.herbcraft.entry.overview.title':'Herbcraft Overview','book.herbcraft.overview.line1':'Herbcraft is not blind eating; it records plants, potency, and cuisine.','book.herbcraft.overview.line2':'Empty bowl + 3-4 herbs can become Herbal Hodgepodge.','book.herbcraft.overview.line3':'Empty bowls are for hodgepodge or vanilla stews; water bowls do not stew randomly.','book.herbcraft.overview.line4':'Water bowls temper fixed medicinal dishes: craft a raw dish, then cook it.','book.herbcraft.overview.line5':'Tattered pages reveal rumours; some secret dishes surface only through pages or practice.','book.herbcraft.overview.line6':'Repeated herbs build potency. Power may sharpen, or turn against you.','book.herbcraft.usage.heard':'Its uses are vague: edible, stewable, perhaps cookable.','book.herbcraft.usage.mastered.raw':'Use: edible raw; exact potency follows.','book.herbcraft.usage.mastered.hodgepodge':'Empty bowl + 3-4 herbs -> Herbal Hodgepodge.','book.herbcraft.usage.mastered.water_bowl':'Water bowl + fixed ingredients -> raw medicinal dish.','book.herbcraft.usage.mastered.hidden':'Secret dishes require a page or firsthand practice to reveal fully.'}}
for lang in ('zh_cn','en_us'):
    p=ROOT/f'core/src/main/resources/assets/herbcraft/lang/{lang}.json'; d=json.loads(p.read_text()); d.update(extra_core[lang])
    for k,(zt,zd,et,ed) in core_lang.items():
        d[f'advancements.herbcraft.{k}.title']=zt if lang=='zh_cn' else et
        d[f'advancements.herbcraft.{k}.description']=zd if lang=='zh_cn' else ed
    p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
# cuisine/alchemy simple advancement lang keys omitted? Add generic if absent
for ns,mod,arr in [('herbcraft_cuisine','cuisine',['first_dish','water_bowl','first_hodgepodge','immunity_meal','hundred_flower_feast','deadly_hodgepodge']),('herbcraft_alchemy','alchemy',['first_essence','first_distilled_essence','first_potion','first_clarified','first_sustained','first_intense','first_murky','epic_potion','first_coating','first_armor_support','undead_toxin','witch_knowledge','survive_witch_brew'])]:
    for lang in ('zh_cn','en_us'):
        p=ROOT/f'{mod}/src/main/resources/assets/herbcraft{"_cuisine" if mod=="cuisine" else ""}/lang/{lang}.json'; d=json.loads(p.read_text())
        for k in arr:
            d.setdefault(f'advancements.{ns}.{k}.title', k)
            d.setdefault(f'advancements.{ns}.{k}.description', k)
        p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
print('advancements rebuilt')
