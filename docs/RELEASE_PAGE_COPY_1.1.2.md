# Herbcraft 1.1.2 Release Page Copy

This file is written for copy/paste into Modrinth and CurseForge project pages. It uses **English first** and Chinese after, because CurseForge requires English names and requires English descriptions/summaries to appear before other languages when multilingual copy is used. It also keeps functional information near the top so moderation and players can quickly understand what each module does.

## Platform requirements checked

### CurseForge

Current CurseForge moderation policy expects:

- a clear, informative description explaining what the project adds or changes;
- a short clear summary, preferably no more than one sentence;
- English names, and English summaries/descriptions before other languages when multilingual;
- no external download links for files;
- project names should not include game versions/file versions;
- AI-generated or AI-enhanced showcase images that may misrepresent actual content need a clear disclaimer;
- project avatar should be square and at least 400×400.

Reference: CurseForge moderation policies, “Clear and Informative Description”, “Clear Summary”, “English content”, “Third Party Downloads”, “AI Misleading Content Disclosure”, and avatar rules.

### Modrinth

Current Modrinth content rules expect:

- clear and honest project purpose/function;
- description should explain what the project specifically does/adds, why someone should download it, and any critical information before download;
- descriptions should be accessible and have a plain-text readable version;
- English-language translation is expected unless the project is exclusively for another language;
- project summaries should be short, unformatted, and not simply repeat the title;
- dependencies should be specified in each version’s Dependencies section;
- additional files should only be used for special purposes such as source JARs.

Reference: Modrinth Content Rules, especially sections 2, 2.1, 2.2, and 5.

## Shared technical metadata

- Game: Minecraft Java Edition `26.1.2`
- Mod loader: Fabric
- Fabric Loader: `0.19.3+`
- Fabric API: `0.151.0+26.1.2+`
- Java: `25+`
- License: GPL-3.0
- Author: `q14433686-arch`
- Contributor: `Arena.ai Agent`
- Current release version: `1.1.2`
- Development target: Mojang official mappings, Fabric Loom `1.16.x`

## Upload/dependency matrix

### Herbcraft Core

- File: `herbcraft-1.1.2.jar`
- Mod ID: `herbcraft`
- Required dependencies:
  - Fabric Loader `>=0.19.3`
  - Fabric API `>=0.151.0+26.1.2`
  - Minecraft `26.1.2`
  - Java `>=25`
- Optional/suggested:
  - Jade `>=26.1.0+fabric` for knowledge-gated block/item overlays.

### Herbcraft: Alchemy

- File: `herbcraft-alchemy-1.1.2.jar`
- Mod ID: `herbcraft_alchemy`
- Required dependencies:
  - Herbcraft Core `>=1.1.2`
  - Fabric Loader `>=0.19.3`
  - Fabric API `>=0.151.0+26.1.2`
  - Minecraft `26.1.2`
  - Java `>=25`

### Herbcraft: Cuisine

- File: `herbcraft-cuisine-1.1.2.jar`
- Mod ID: `herbcraft_cuisine`
- Required dependencies:
  - Herbcraft Core `>=1.1.2`
  - Fabric Loader `>=0.19.3`
  - Fabric API `>=0.151.0+26.1.2`
  - Minecraft `26.1.2`
  - Java `>=25`

---

# Project 1: Herbcraft

## Summary / short description

A Fabric survival mod where wild plants become edible, risky, learnable, and part of a living Herb Codex.

## Full description

# Herbcraft | 百草可食

**Herbcraft** is a Fabric survival mod for Minecraft `26.1.2`. It makes many vanilla plants and natural materials edible, then turns eating them into a system of risk, discovery, herb pharmacology, long-term nutrition, and in-game knowledge.

Also searchable as **BCKS** / **Baicao Keshi** / **百草可食**.

This is not a simple “eat a flower, get a buff” mod. Raw herbs are uncertain. Some help, some hurt, some teach you only after you taste them, and the Herb Codex records what you learn.

## What it adds

### Edible wild nature

Herbcraft makes many vanilla natural items edible or drinkable, including flowers, petals, grasses, ferns, leaves, saplings, seeds, kelp, seagrass, ice, snowballs, resin, honey materials, fungi, eggs, and strange alchemical materials.

Raw herb eating is deliberately not a free food source. In `1.1.2`, common forage was rebalanced with data-backed food rolls:

- very common scenery such as petals, wildflowers, ferns, leaves, and saplings no longer reliably restores visible hunger;
- some items only sometimes restore visible hunger;
- some fibrous items mainly restore hidden saturation/fullness;
- seeds and oily seeds feel more substantial without replacing real food;
- sugar cane gives quick visible relief and a little real hidden fullness.

### Herb nature, flavors, and relations

Each herb can have:

- heat/cold nature;
- pharmacological flavors such as sweet, bitter, pungent, sour, salty, bland, and astringent;
- traits such as toxic, clearing, nourishing, stirring, sedating, aquatic, fiery, leafy, fungal, resinous, and more.

Herbcraft tracks recent herb use. Eating herbs close together can produce relationship effects: some herbs support each other, some restrain toxins, some weaken each other, and some clash badly.

### Herb stack and overdose

Eating certain herbs in quick succession builds a temporary herb stack. Higher herb stack can improve positive effects, but pushing too far can cause overload. Edible does not mean safe without measure.

### Herb Codex / 百草经

The Herb Codex is an in-game knowledge system.

- Seeing an item does not reveal everything.
- Rumors from tattered pages may be incomplete or false.
- Tasting records personal experience.
- Practice and mastery reveal more precise information.
- The Codex contains guide pages for recognizing herbs, knowledge states, flavors/nature, herb relations, herb stack, ingredient nutrition, pages, and errata.
- In `1.1.2`, guide pages and one-time hints were improved so the Codex teaches the systems more clearly without spoiling item tables.

### Gentle nutrition system

Herbcraft adds a gentle five-part nutrition system:

- Grain
- Flesh
- Oil
- Greens
- Fluids

This is not meant to be a harsh survival overhaul. Deficiencies and imbalances give mild, readable feedback. Balanced nutrition can grant a small **Well Nourished** bonus.

### Ingredient Codex / 食材录

Ordinary vanilla foods do not use herb pharmacology, but they still affect long-term nutrition. After you eat them, they can appear in the Ingredient Codex with rough nutrition information.

### Optional integrations

- **Jade**: knowledge-gated block/item overlay information.
- **Homeostatic**: optional data-only thirst compatibility. If Homeostatic is installed, selected Herbcraft foods and water-like items can affect hydration. Herbcraft does not require Homeostatic.

## Modules

This is the **Core** module. It can be played alone.

Optional addons:

- **Herbcraft: Alchemy** — essences, herbal potions, potion quality, murky/clarified brewing, weapon coatings, witch/villager integrations.
- **Herbcraft: Cuisine** — water bowls, medicinal dishes, hodgepodge, hidden feasts/toxins, and safer prepared routes.

Core is required for both addons. Alchemy and Cuisine are independent of each other, but installing both unlocks hidden cuisine catalyst interactions.

## Requirements

- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`
- Optional: Jade `26.1.0+fabric`

## Chinese description / 中文简介

**百草可食 Herbcraft** 让许多原版植物、自然材料和奇物可以入口，并把“吃”变成一套风险、药性、知识和营养系统。

你可以吃花草、树叶、树苗、种子、海带、冰雪、树脂、蜂蜜材料、菌类、鸡蛋和炼药材料等物品；但生吃不是免费收益。常见草木在 `1.1.2` 中被重新平衡，不再稳定当作前期食物。百草经会记录你见过、听闻、亲尝和真正掌握的知识。五味、寒热、七情配伍和药性蓄积会影响草药效果；普通食物则进入《食材录》，影响长期五养。

主模组的营养系统是温和提醒型，不是硬核惩罚型。更严格的营养玩法更适合未来独立附属。

## Version changelog: 1.1.2

Use for the Core file changelog.

### Added

- Added data-backed Codex guide entries under the Herb Codex overview.
- Added one-time guide hints for early learning steps such as first herb bite, first Codex open, first page read, first ingredient tasted, and first correction state.
- Added persisted pharmacology principle discovery for flavors, heat/cold interactions, and herb relations; the Codex can now show discovered principles and recent examples.
- Added `food_rolls` support for raw herb food definitions, allowing probabilistic visible hunger and direct hidden saturation rolls.
- Added a raw herb food balance validator.

### Changed

- Rebalanced common raw forage so wildflowers, petals, ferns, leaves, saplings, seeds, and some aquatic plants no longer act as reliable early food.
- Strengthened seed and oily-seed hidden saturation after client testing: ordinary seeds now feel meaningful, and melon/pumpkin seeds feel more substantial.
- Sugar cane now remains only +1 visible hunger, but gives a better short hidden-fullness feel and is anchored at Homeostatic hydration amount 3 when Homeostatic is installed.
- Removed the guaranteed vanilla `minecraft:saturation` effect from blue orchid so it no longer bypasses food-roll balance.
- Made the 五味与性味 and 七情与配伍 Codex guide pages clearer for new players.
- Data-backed pharmacology tuning now owns temperature values and ordered seven-emotion relation rules.
- Exact hot/cold same-qi buildup was softened so repeated same-temperature herbs ramp up more gradually.
- Nutrition threshold logic now follows `nutrition_rules.json` more consistently instead of hardcoded Java thresholds.

### Fixed / cleanup

- Prevented several documentation/source mismatches that could mislead future pack authors or maintainers.
- Strengthened validators for Codex guide text, raw herb balance, Homeostatic compatibility, nutrition correction, and built jars.

---

# Project 2: Herbcraft: Alchemy

## Summary / short description

An optional Herbcraft addon for essences, herbal potions, potion quality, murky and clarified brewing, and weapon coatings.

## Full description

# Herbcraft: Alchemy | 百草可食：炼金分册

**Herbcraft: Alchemy** is an optional addon for **Herbcraft Core**. It turns herb knowledge into essences, potions, clarified and murky brewing, advanced potion quality, and temporary weapon coatings.

If raw herb eating is a gamble, alchemy is a contract: more preparation, more control, more consequence.

## What it adds

### Essences

- 40 herbal essence definitions.
- Essences are crafted from eligible herbs and natural materials.
- Essence data is JSON-backed.
- Extra JSON essence definitions can register dynamically, though polished addons still need their own recipes/assets/lang.

### Herbal potion lines

Alchemy adds potion routes based on Herbcraft essences:

- base herbal potions;
- clarified potions focused on positive effects;
- murky potions focused on negative effects;
- long and strong variants;
- special toxin lines such as undead venom, cardiac toxin, and wither venom.

### Potion quality and bonus effects

Advanced potion stacks can carry persistent components:

- potion quality;
- potion bonus / aftertaste / impurity effects;
- potion source tracking;
- one-time initialization so stacks do not reroll.

### Weapon coatings

- Apply essence-based coatings to compatible weapons through smithing.
- Coatings are temporary and limited-use, not permanent enchantments.
- Coating rules are data-backed.

### Witch and villager integration

- Witches can use safe, short Herbcraft potion variants.
- Witch loot can include pages, essences, or potions.
- 26.1 data-driven villager trades cover essences, potions, arrows, and coating-related items.

### Optional Cuisine catalyst link

If **Herbcraft: Cuisine** is also installed, hidden dishes can act as high-tier brewing catalysts:

- Hundred-Flower Feast upgrades clarified routes.
- Deadly Hodgepodge upgrades murky routes.

Cuisine is not required for Alchemy, but installing both modules enables this cross-module secret.

## Requirements

- Herbcraft Core `>=1.1.2`
- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`

## Chinese description / 中文简介

**百草可食：炼金分册** 是 Herbcraft Core 的可选附属。它加入草药精华、草药药水、澄清/浊化路线、药水品质、余韵/杂毒、武器淬层、女巫联动和数据驱动村民交易。

如果说生吃是赌博，炼金就是契约：准备更多，结果更可控，代价也更明确。炼金分册不需要膳房分册；但如果同时安装膳房分册，百花宴和剧毒蛊可以作为隐藏炼金催化剂。

## Version changelog: 1.1.2

Use for the Alchemy file changelog.

### Added / improved

- Added module-owned Alchemy Codex guide pages inside `炼金要术`, including overview guidance for essences, herbal potions, clarified/murky routes, quality/bonus effects, coatings, and witch-related content.
- Added one-time Alchemy guide hint support for first essence acquisition.
- Alchemy guide entries are now data-backed by module-owned `codex_guides.json` and appear only when Alchemy is installed.
- Pharmacology relation/tuning data used by Core/Alchemy interactions is now more data-driven through `pharmacology_rules.json`.

### Clarified

- Cuisine hidden catalyst wording now clearly says catalyst linkage requires the Alchemy chapter/module to be installed.
- Current docs and validators were updated for the `1.1.2` package line.

### Validation

- Built and verified with Java 25.
- Built-jar validation checks Alchemy metadata, resources, mixins, JSON data, and Java 25 bytecode.

---

# Project 3: Herbcraft: Cuisine

## Summary / short description

An optional Herbcraft addon for water bowls, medicinal dishes, hodgepodge, hidden meals, and safer prepared herb routes.

## Full description

# Herbcraft: Cuisine | 百草可食：膳房分册

**Herbcraft: Cuisine** is an optional addon for **Herbcraft Core**. It turns raw herbs and natural materials into water bowls, fixed dishes, medicinal meals, hodgepodge, and hidden high-tier cuisine.

If raw herb eating is a gamble, cuisine is craft: use preparation and heat to make wild nature steadier.

## What it adds

### Water bowls

- Fill bowls from water sources or water cauldrons.
- Drink them for brief relief.
- Use them as a base for many medicinal dishes.
- Water bowls do not directly repair long-term nutrition.

### Fixed dishes and medicinal meals

Cuisine adds curated dishes and medicinal cooking routes. Depending on the dish, meals can:

- clear selected effects;
- grant temporary effect immunity;
- restore health or add short buffs;
- help prepare for specific dangers;
- provide nutrition through Herbcraft’s long-term nutrition system.

Many medicinal meals use a two-step route:

1. craft a raw dish with a water bowl and specific ingredients;
2. cook it in a furnace, smoker, or campfire.

### Herbal Hodgepodge / 百草盅

Hodgepodge is the fast, experimental path:

- craft it with an empty bowl and valid Herbcraft herbs;
- it stores its ingredients as item data;
- it resolves herb effects when eaten;
- mixed positive and negative herbs can clash.

Hodgepodge is intentionally separate from water-bowl dishes and vanilla suspicious stew.

### Hidden meals

Cuisine includes hidden high-tier dishes:

- Hundred-Flower Feast / 百花宴
- Deadly Hodgepodge / 剧毒蛊

These are not listed as ordinary recipes at the start. They are discovered through pages, practice, and knowledge.

### Optional Alchemy catalyst link

If **Herbcraft: Alchemy** is also installed, some hidden meals can become alchemical catalysts.

The Cuisine Codex now says this clearly: the cooking chapter can hint at the path, but the alchemy linkage needs the Alchemy chapter/module present.

## Requirements

- Herbcraft Core `>=1.1.2`
- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`

## Chinese description / 中文简介

**百草可食：膳房分册** 是 Herbcraft Core 的可选附属。它加入盛水的碗、固定菜、药膳、生坯烹饪、百草盅、百花宴和剧毒蛊。

它的定位不是生吃的赌博，而是用准备、配方和火候换取更可控的清除、免疫、营养和战斗收益。膳房分册不需要炼金分册；但如果同时安装炼金分册，某些隐藏料理可以作为高级炼药催化剂。

## Version changelog: 1.1.2

Use for the Cuisine file changelog.

### Added / improved

- Added module-owned Cuisine Codex guide pages inside `膳房录`, including overview guidance for water bowls, fixed dishes, hodgepodge, cleansing/immunity, and hidden catalysts.
- Added one-time Cuisine guide hint support for first water bowl and first dish.
- Cuisine guide entries are now data-backed by module-owned `codex_guides.json` and appear only when Cuisine is installed.
- Cuisine hidden catalyst guide copy now explicitly says Alchemy linkage requires the Alchemy chapter/module to be installed.

### Clarified

- Hidden meals remain Cuisine content, but their catalyst behavior is a cross-module feature that only appears when Alchemy is present.
- Current docs and validators were updated for the `1.1.2` package line.

### Validation

- Built and verified with Java 25.
- Built-jar validation checks Cuisine metadata, resources, recipes, guide data, Homeostatic compatibility data, and Java 25 bytecode.

---

# Shared 1.1.2 changelog for all three files

Use this if you want one changelog text for all three version uploads.

## Herbcraft 1.1.2

### New player guidance and Codex polish

- Added data-backed guide pages for the Herb Codex.
- Added module-owned guide pages for Cuisine and Alchemy, so addon guidance only appears when that addon is installed.
- Added one-time guide hints for early learning steps.
- Clarified 五味与性味 / Nature and Flavors: this is herb pharmacology, not nutrition.
- Clarified 七情与配伍 / Relations Between Herbs: this is short-window interaction between recently eaten herbs, not a fixed recipe list.
- Guide pages now reveal discovered flavor, temperature, and relation principles through player experience.

### Raw herb food rebalance

- Added data-backed `food_rolls` for probabilistic visible hunger and direct hidden saturation restoration.
- Common forage such as wildflowers, petals, ferns, leaves, saplings, and similar scenery is no longer reliable early food.
- Seeds were strengthened after client testing so successful saturation rolls feel meaningful.
- Melon and pumpkin seeds now feel more substantial due to oilier seed identity.
- Sugar cane still restores only +1 visible hunger, but now gives better short hidden fullness.
- Blue orchid no longer bypasses the new balance with a guaranteed vanilla Saturation effect.

### Nutrition and pharmacology tuning

- Pharmacology temperature values and seven-emotion relation rules are now data-backed.
- Same-temperature hot/cold buildup is less abrupt than before.
- Nutrition correction logic now follows JSON thresholds/spread more consistently.
- High Fluids / 津液 remains non-punitive in the base mod, while low Fluids still matters.

### Cuisine and Alchemy guidance

- Cuisine and Alchemy now have their own Codex overview sections.
- Cuisine hidden catalyst text now clearly states that alchemy linkage requires the Alchemy module/chapter.
- Hidden Cuisine catalysts still preserve potion components when used with Alchemy.

### Validation / packaging

- Bumped all modules to `1.1.2`.
- Alchemy and Cuisine now depend on Herbcraft Core `>=1.1.2`.
- Full build and validator suite passed on Java 25.

# Suggested upload dependencies

## Core file

- Required: Fabric API
- Optional: Jade

## Alchemy file

- Required: Fabric API
- Required: Herbcraft Core `>=1.1.2`

## Cuisine file

- Required: Fabric API
- Required: Herbcraft Core `>=1.1.2`

## Cross-module note

Alchemy and Cuisine are independent addons. They do not require each other, but installing both enables hidden Cuisine catalyst brewing interactions.
