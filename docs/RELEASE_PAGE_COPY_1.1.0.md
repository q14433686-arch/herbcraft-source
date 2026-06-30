> Historical release-copy snapshot. Do not use as current implementation documentation. See `CURRENT_BASELINE.md` and `docs/DOCUMENTATION_INDEX.md`.

> Superseded by `RELEASE_PAGE_COPY_1.1.1.md` for the current release. Kept for historical reference.

# Herbcraft 1.1.0 Release Page Copy

This file is written for copy/paste into Modrinth and CurseForge project pages. It uses English first because CurseForge moderation requires English names and requires English descriptions/summaries to appear before other languages when multiple languages are used. Chinese copy is provided after English.

## Platform notes checked before writing

- Modrinth project pages require a clear and honest project summary/description. Summaries should be short, plain text, and not merely repeat the title. Descriptions should explain what the project does and how to use it; Markdown is supported.
- CurseForge moderation expects a clear one-sentence summary, a functional description of what the project adds/changes, English first if multilingual, no misleading external download links, no generic “adds items” descriptions, and concise readable formatting.

## Shared technical metadata

- Game: Minecraft Java Edition `26.1.2`
- Mod loader: Fabric
- Fabric Loader: `0.19.3` or newer
- Fabric API: `0.151.0+26.1.2` or newer
- Java: `25` or newer
- License: GPL-3.0
- Author: `q14433686-arch`
- Contributor: `Arena.ai Agent`
- Current release version: `1.1.0`
- Mapping/dev target: Mojang official mappings, Fabric Loom 1.16.x line

## Upload/dependency matrix

### Herbcraft Core

- File: `herbcraft-1.1.0.jar`
- Mod ID: `herbcraft`
- Required dependencies:
  - Fabric Loader `>=0.19.3`
  - Fabric API `>=0.151.0+26.1.2`
  - Minecraft `26.1.2`
  - Java `>=25`
- Optional/suggested:
  - Jade `>=26.1.0` for knowledge-gated block/item overlay information.

### Herbcraft: Alchemy

- File: `herbcraft-alchemy-1.1.0.jar`
- Mod ID: `herbcraft_alchemy`
- Required dependencies:
  - Herbcraft Core `>=1.1.0`
  - Fabric Loader `>=0.19.3`
  - Fabric API `>=0.151.0+26.1.2`
  - Minecraft `26.1.2`
  - Java `>=25`

### Herbcraft: Cuisine

- File: `herbcraft-cuisine-1.1.0.jar`
- Mod ID: `herbcraft_cuisine`
- Required dependencies:
  - Herbcraft Core `>=1.1.0`
  - Fabric Loader `>=0.19.3`
  - Fabric API `>=0.151.0+26.1.2`
  - Minecraft `26.1.2`
  - Java `>=25`

---

# Modrinth copy

## Project 1: Herbcraft | 百草可食

### Summary / Short description

A Fabric survival mod where edible plants, nutrition, herbal knowledge, and risk turn vanilla nature into a living survival system.

### Description

# Herbcraft | 百草可食

**Herbcraft** turns vanilla plants, natural materials, nutrition, and discovery into a layered survival system for Minecraft `26.1.2` on Fabric.

Almost everything that looks like it could be tasted probably can be — flowers, leaves, saplings, seeds, ice, resin, honey materials, eggs, snowballs, fungi, herbs, odd natural materials, and more. Eating raw herbs is not simply “free food”: it is a gamble with probabilities, short-term accumulation, pharmacological interactions, and knowledge progression.

## Main features

### Edible vanilla nature

- Many vanilla plants and natural materials gain food/consumable behavior.
- Raw items can heal, harm, cleanse, strengthen, slow, poison, confuse, warm, cool, or trigger special interactions.
- Some items keep special vanilla behavior, such as short-tap throwing vs. long-hold eating for eggs and snowballs.

### Herb stack and pharmacology

- Repeatedly eating stack-group herbs builds **herb stack** for about a minute.
- Higher herb stack increases positive effect chance, can improve positive effect strength, and can extend positive duration.
- Too much herb stack causes overload.
- Herb nature data includes temperature, pharmacological flavors, taste flavors, and traits.
- Seven-emotions-style herb interactions can harmonize, conflict, suppress toxins, or amplify effects depending on what you ate recently.

### Nutrition system

Herbcraft includes a gentle five-dimensional nutrition system:

- Grain
- Flesh
- Oil
- Vegetables
- Fluids

The main mod is intentionally not a hardcore nutrition overhaul. It gives light reminders and mild mechanical feedback rather than harsh survival punishment.

Custom nutrition effects include:

- **Grain Deficiency**: lower block breaking efficiency.
- **Flesh Deficiency**: lower attack damage.
- **Oil Deficiency**: lower movement speed.
- **Vegetable Deficiency**: slightly increased food exhaustion over time.
- **Fluid Deficiency**: slight movement penalty.
- **Dietary Imbalance**: light movement penalty and light exhaustion over time.
- **Well Nourished**: armor and knockback-resistance bonus for maintaining balanced nutrition.

A debug command is available for testing with gamemaster permission:

```mcfunction
/herbcraft nutrition get @s
/herbcraft nutrition set @s grain 0
/herbcraft nutrition preset @s balanced
/herbcraft nutrition preset @s starved
```

### Herb Codex / 百草经

The Herb Codex is a knowledge-gated in-game reference system.

- Unknown entries do not reveal full information.
- Encountered entries show that you have seen an item without revealing true details.
- Heard entries can show rumors and claims.
- Tasted entries show personally verified information.
- Mastered entries reveal full practical details.
- Tattered pages, rumors, claim markers, and errata pages support a “learn by rumor, test by practice” loop.

### Jade integration

If Jade is installed, Herbcraft adds knowledge-gated overlay information. The overlay respects the same knowledge tiers as the Codex.

## Modules

This is the **core** module. It can be played alone.

Optional addon jars:

- **Herbcraft: Cuisine** adds water bowls, dishes, medicinal cuisine, hodgepodge, Hundred-Flower Feast, and Deadly Hodgepodge.
- **Herbcraft: Alchemy** adds essences, herbal potions, clarified/murky potion lines, high-tier potion quality, weapon coatings, and witch/villager integrations.

## Requirements

- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`
- Optional: Jade `26.1.0+fabric` for overlay integration

## Notes

- This is a test-version line despite the internal jar version being `1.1.0`.
- Balance is intentionally gentle in the main mod. A stricter “hardcore nutrition” addon may be developed separately in the future.

### 中文简介

**百草可食 Herbcraft** 将原版植物、自然材料、生食风险、营养、药性和百草经知识系统串成一套生存玩法。你可以吃许多原版自然物，但生吃不是免费收益，而是一场带概率、药性蓄积、七情互动和知识解锁的尝试。主模组的营养系统是温和提醒型，不是硬核惩罚型；更严格的营养玩法适合未来独立附属。

---

## Project 2: Herbcraft: Alchemy | 百草可食：炼金分册

### Summary / Short description

An optional Herbcraft addon for essences, herbal potions, clarified and murky brewing lines, potion quality, and weapon coatings.

### Description

# Herbcraft: Alchemy | 百草可食：炼金分册

**Herbcraft: Alchemy** is an optional addon for **Herbcraft Core**. It turns herbal knowledge into essences, potion lines, high-tier potion quality, weapon coatings, and dangerous but controlled alchemical tools.

## Main features

### Essences

- Craft herbal essences from eligible plants and natural materials.
- 40 essence items are registered in this test version.
- Essences are used for brewing and weapon coatings.

### Herbal potion lines

Alchemy adds Herbcraft potion lines based on essence effects:

- Base herbal potions from awkward potion + essence.
- Clarified potions using honeycomb, focused on positive effects.
- Murky potions using fermented spider eye, focused on harmful effects.
- Long and strong variants following vanilla-style redstone/glowstone logic.
- Special high-tier toxin lines such as undead venom, cardiac toxin, and wither venom III.

### Advanced potion quality

High-tier potion stacks can receive persistent data components:

- Potion quality: standard, fine, exceptional.
- Bonus effect component: “aftertaste” or “impurity” style bonus effects.
- Potion source tracking.
- One-time initialization to avoid rerolling the same stack.

### Hidden cuisine catalysts

If **Herbcraft: Cuisine** is also installed:

- Hundred-Flower Feast can upgrade clarified potions to exceptional quality.
- Deadly Hodgepodge can upgrade murky potions to exceptional quality.
- The brewing path copies the input stack and preserves existing components.

### Weapon coatings

- Apply essence-based coatings to compatible melee weapons through smithing.
- Coatings are stored as item components.
- Coatings have limited uses and time limits.
- Full coatings apply essence effects to targets.
- Refined positive/negative modes can focus effects and add random bonuses.

### Witch and villager integration

- Witches can throw short, safe Herbcraft splash potion variants.
- Data-driven 26.1 villager trades are included for alchemy items, potions, arrows, essences, and coating-related items.

## Requirements

- Herbcraft Core `1.1.0+`
- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`

## Notes

- This addon depends on Herbcraft Core.
- It does not require Herbcraft: Cuisine, but some cross-module catalyst features only appear when Cuisine is installed.

### 中文简介

**百草可食：炼金分册** 是 Herbcraft Core 的可选附属，加入精华、草药药水、澄清/浊化路线、高阶药水品质、余韵/杂毒组件、武器淬层、女巫安全药水和 26.1 数据驱动村民交易。若同时安装膳房分册，百花宴与剧毒蛊还可以作为高级炼药催化剂。

---

## Project 3: Herbcraft: Cuisine | 百草可食：膳房分册

### Summary / Short description

An optional Herbcraft addon that turns herbs into water bowls, medicinal dishes, hodgepodge, and rare feast/toxin cuisine.

### Description

# Herbcraft: Cuisine | 百草可食：膳房分册

**Herbcraft: Cuisine** is an optional addon for **Herbcraft Core**. It turns herbs and natural materials into controlled cooking routes: water bowls, fixed dishes, medicinal cuisine, hodgepodge, and hidden high-tier dishes.

Where raw herb eating is a gamble, cuisine is preparation.

## Main features

### Water bowl

- Fill an empty bowl from water sources or water cauldrons.
- Drink it to clear fire.
- 15% chance to clear vanilla nausea.
- 15% success also temporarily relieves Herbcraft dietary imbalance for 60 seconds.
- It does not repair nutrition values directly.

### Fixed dishes and medicinal cuisine

Cuisine registers 32 dish entries in this test version:

- 10 fixed dishes.
- 22 medicinal or hidden dishes.

Medicinal dishes can:

- Clear selected vanilla effects.
- Grant temporary effect immunity through Herbcraft Core.
- Apply buffs.
- Clear fire or freezing.
- Interact with Herbcraft nutrition through nutrition profiles rather than simply deleting nutrition states.

### Raw dish cooking route

Many medicinal dishes use a two-step route:

1. Craft a raw dish with a water bowl and specific ingredients.
2. Cook it by furnace, smoker, or campfire.

This separates fixed cuisine from random hodgepodge and avoids vanilla suspicious stew conflicts.

### Hodgepodge / 百草盅

- Craft with an empty bowl and valid Herbcraft herbs.
- Stores ingredient IDs as item components.
- Dynamically resolves herb effects when eaten.
- Increases effect chance compared with eating ingredients raw.
- Can cause clash reactions when positive and negative herbs are mixed.
- Does not use water bowl, so it remains isolated from fixed medicinal recipes.

### Hidden dishes

Cuisine includes two hidden high-tier dishes:

- Hundred-Flower Feast / 百花宴
- Deadly Hodgepodge / 剧毒蛊

They are real source/built-jar items with raw recipes and cooking routes.

If Herbcraft: Alchemy is also installed, they can act as advanced brewing catalysts:

- Hundred-Flower Feast upgrades clarified potions.
- Deadly Hodgepodge upgrades murky potions.

### Cuisine Codex integration

Cuisine adds its own Codex chapter and knowledge hooks. Dish entries can hide unknown ingredients and unlock through practice, acquisition, and related raw-herb knowledge.

## Requirements

- Herbcraft Core `1.1.0+`
- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`

## Notes

- This addon depends on Herbcraft Core.
- It does not require Herbcraft: Alchemy, but hidden feast/toxin catalyst behavior requires Alchemy to be installed.

### 中文简介

**百草可食：膳房分册** 是 Herbcraft Core 的可选附属，加入盛水的碗、固定菜、药膳、生坯烹饪、百草盅、百花宴与剧毒蛊。它的定位不是生吃的赌博，而是用准备和配方换取更可控的清除、免疫、营养和战斗收益。

---

# CurseForge copy

CurseForge prefers English first and concise descriptions. The following is shorter and avoids burying functional information.

## Project 1: Herbcraft | 百草可食

### Summary

A Fabric survival mod that makes vanilla nature edible, risky, nutritious, and learnable through an in-game Herb Codex.

### Description

# Herbcraft | 百草可食

Herbcraft is a Fabric survival mod for Minecraft `26.1.2`. It expands vanilla plants and natural materials into a system of edible herbs, raw-eating risks, nutrition, pharmacology, and knowledge discovery.

## What it adds

- Many vanilla plants and natural materials become edible or drinkable.
- Raw eating can heal, harm, cleanse, strengthen, poison, confuse, or trigger special effects.
- Repeated herb eating builds a temporary herb stack that can improve positive effects but can also overload.
- Herb nature data includes temperature, pharmacological flavors, taste flavors, and traits.
- Recent herb combinations can harmonize, conflict, suppress toxins, or strengthen effects.
- A gentle five-part nutrition system tracks grain, flesh, oil, vegetables, and fluids.
- Custom nutrition effects warn about serious deficiencies or dietary imbalance without making the main mod overly hardcore.
- A balanced diet grants Well Nourished, a small armor and knockback-resistance bonus.
- The Herb Codex gates information behind discovery, rumors, tasting, practice, and mastery.
- Optional Jade integration shows knowledge-gated herb information in overlays.

## Requirements

- Minecraft `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`
- Optional: Jade `26.1.0+fabric`

## Optional addons

- Herbcraft: Cuisine adds water bowls, medicinal dishes, hodgepodge, and hidden feast/toxin dishes.
- Herbcraft: Alchemy adds essences, herbal potions, potion quality, and weapon coatings.

## 中文简介

百草可食让大量原版植物与自然材料可以食用，并加入药性、营养、百草经、传闻、亲尝和实践解锁。主模组的营养系统偏温和提醒，不是硬核生存惩罚。

---

## Project 2: Herbcraft: Alchemy | 百草可食：炼金分册

### Summary

An optional Herbcraft addon for essences, herbal potions, potion quality, and essence-based weapon coatings.

### Description

# Herbcraft: Alchemy | 百草可食：炼金分册

Herbcraft: Alchemy is an optional addon for Herbcraft Core. It turns Herbcraft herbs into essences, potion lines, advanced potion stacks, and weapon coatings.

## What it adds

- 40 herbal essence items.
- Base herbal potions brewed from awkward potions and essences.
- Clarified potion lines focused on positive effects.
- Murky potion lines focused on negative effects.
- Long, strong, and special toxin variants.
- Advanced potion quality: standard, fine, and exceptional.
- Bonus potion components such as aftertaste or impurity effects.
- Weapon coatings applied through smithing and stored as item components.
- Safe short-duration Herbcraft potion variants for witches.
- Data-driven villager trades for essences, potions, arrows, and coatings.
- If Cuisine is installed, Hundred-Flower Feast and Deadly Hodgepodge can upgrade clarified/murky potions to exceptional quality.

## Requirements

- Herbcraft Core `1.1.0+`
- Minecraft `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`

## 中文简介

炼金分册加入精华、草药药水、澄清/浊化药水、高阶药水品质、余韵/杂毒、武器淬层、女巫药水和村民交易。它需要安装百草可食核心。

---

## Project 3: Herbcraft: Cuisine | 百草可食：膳房分册

### Summary

An optional Herbcraft addon for water bowls, medicinal dishes, hodgepodge, and hidden feast/toxin cuisine.

### Description

# Herbcraft: Cuisine | 百草可食：膳房分册

Herbcraft: Cuisine is an optional addon for Herbcraft Core. It turns herbs and natural materials into prepared food systems: water bowls, fixed dishes, medicinal cuisine, hodgepodge, and hidden high-tier dishes.

## What it adds

- Water bowls filled from water sources or water cauldrons.
- Water bowls can clear fire, may clear vanilla nausea, and may temporarily relieve Herbcraft dietary imbalance.
- 32 registered dishes in this test version.
- Medicinal dishes that can clear selected effects, grant temporary immunities, apply buffs, and provide nutrition.
- Raw dish recipes that must be cooked by furnace, smoker, or campfire.
- Hodgepodge bowls that store their ingredients and dynamically resolve herb effects when eaten.
- Recipe isolation so water-bowl dishes, hodgepodge, and vanilla suspicious stew do not overwrite each other.
- Hidden Hundred-Flower Feast and Deadly Hodgepodge dishes.
- Cuisine Codex entries and knowledge hooks.
- If Alchemy is installed, hidden dishes can serve as brewing catalysts for clarified or murky potions.

## Requirements

- Herbcraft Core `1.1.0+`
- Minecraft `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2+`
- Java `25+`

## 中文简介

膳房分册加入盛水的碗、固定菜、药膳、生坯烹饪、百草盅、百花宴和剧毒蛊。它需要安装百草可食核心；若同时安装炼金分册，隐藏菜还可以作为高级炼药催化剂。

---

# Version changelog for file uploads

Use this as the version changelog for all three 1.1.0 files, then add module-specific notes if desired.

## 1.1.0

- Updated the Herbcraft test baseline to Minecraft `26.1.2`, Fabric Loader `0.19.3+`, Fabric API `0.151.0+26.1.2+`, and Java `25+`.
- Added/updated the custom Herbcraft nutrition system with seven custom effects.
- Replaced vanilla nutrition debuffs with Herbcraft nutrition effects.
- Added Well Nourished as a custom balanced-diet reward.
- Added sustained severe malnutrition states while keeping warning states light and non-hardcore.
- Added water bowl temporary relief for dietary imbalance on the same 15% success chance as vanilla nausea relief.
- Added `/herbcraft nutrition get|set|preset` gamemaster debug commands for testing.
- Improved nutrition effect icons using vanilla-derived item textures with clear status arrows.
- Ensured cuisine/medicine effect clearing does not remove Herbcraft nutrition effects.
- Verified hidden cuisine dishes exist in source and built jars.
- Verified alchemy hidden cuisine brewing catalyst mixin classes exist and build.
- Updated handoff docs, validators, and release baseline output.

## Suggested file dependencies when uploading versions

### Core file

- Required: Fabric API
- Optional: Jade

### Alchemy file

- Required: Fabric API
- Required: Herbcraft Core `>=1.1.0`

### Cuisine file

- Required: Fabric API
- Required: Herbcraft Core `>=1.1.0`

### Cross-module note

Alchemy and Cuisine are independent addons that both depend on Core. They do not require each other, but installing both enables hidden cuisine catalyst brewing interactions.
