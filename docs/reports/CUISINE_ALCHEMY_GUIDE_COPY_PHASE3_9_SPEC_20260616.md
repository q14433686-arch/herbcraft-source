# Cuisine / Alchemy Module Guide Copy Spec — 2026-06-16

Status: detailed source-audit / copywriting spec / for author review  
Baseline: Herbcraft `1.1.1`, planning target `1.1.2`

This is a more concrete follow-up to:

```text
docs/reports/CUISINE_ALCHEMY_GUIDANCE_DIRECTION_20260616.md
```

The author agreed with Direction C:

> Keep `膳房与药膳` and `炼金与精华` as short gateway pages under `百草经总纲`, but add real module-owned overview sections and guide subentries inside `膳房录` and `炼金要术`.

This document is based on current source, recipes, and advancement hooks. It proposes exact staged copy and reveal conditions that should feel natural without telling players every hidden fact at once.

## Implementation update — 2026-06-16

P3.9 Direction C has been implemented after this spec was written. Cuisine and Alchemy now each register an `overview` section and module-owned guide subentries through their existing `codex_guides.json` files. The current implementation uses existing advancement completion and representative entry knowledge state as staged reveal conditions; no new save data was added for this step.

## 1. Source facts: Cuisine route

### 1.1 Current module entry points

Current guide/hint data:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/codex_guides.json
cuisine/src/main/resources/data/herbcraft_cuisine/guide_hints.json
```

Current chapter code:

```text
cuisine/src/main/java/com/herbcraft/cuisine/knowledge/CuisineChapter.java
cuisine/src/main/java/com/herbcraft/cuisine/knowledge/CuisineAcquisitionHooks.java
```

Current food/recipe data:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json
cuisine/src/main/resources/data/herbcraft_cuisine/hodgepodge_rules.json
cuisine/src/main/resources/data/herbcraft_cuisine/recipe/*.json
```

Important mechanics from source:

- `water_bowl` exists and is acquired from water source / water cauldron.
- Simple fixed dishes use `minecraft:bowl` directly.
- Medicinal/raw dishes use `herbcraft_cuisine:water_bowl` to craft a raw dish, then cooking turns raw into finished dish.
- 百草盅 uses a custom recipe: empty bowl + valid herbs; mushrooms rejected; water bowl rejected.
- Hidden dishes use water-bowl raw recipes and cooking routes.
- `hundred_flower_feast` and `deadly_hodgepodge` can act as Alchemy catalysts when Alchemy is installed.
- Dish clearing and hodgepodge clear-positive paths skip Herbcraft nutrition effects.

### 1.2 Current Cuisine advancements available as reveal conditions

```text
water_bowl
first_dish
first_hodgepodge
immunity_meal
hundred_flower_feast
deadly_hodgepodge
```

These are enough for staged guide lines without adding new player data.

### 1.3 Important recipe facts to respect

Do not reveal hidden dish full recipes too early.

Known source facts for writer reference:

- Hundred-Flower Feast raw recipe:
  ```text
  water_bowl + dandelion + oxeye_daisy + cornflower + blue_orchid + allium + pink_petals + torchflower
  ```
- Deadly Hodgepodge raw recipe:
  ```text
  water_bowl + wither_rose + lily_of_the_valley + flowering_azalea_leaves + closed_eyeblossom
  ```
- Both cook through smelting/smoking/campfire into final dishes.
- Both can be Alchemy catalysts, but the guide should only hint this until the player discovers/eats them.

## 2. Source facts: Alchemy route

### 2.1 Current module entry points

Current guide/hint data:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json
alchemy/src/main/resources/data/herbcraft_alchemy/guide_hints.json
```

Current chapter and acquisition code:

```text
alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java
alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java
```

Current alchemy data:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/essences.json
alchemy/src/main/resources/data/herbcraft_alchemy/potion_bonus_pools.json
alchemy/src/main/resources/data/herbcraft_alchemy/special_potions.json
alchemy/src/main/resources/data/herbcraft_alchemy/coating_rules.json
alchemy/src/main/resources/data/herbcraft_alchemy/witch_loot.json
alchemy/src/main/resources/data/herbcraft/recipe/essence_*.json
alchemy/src/main/resources/data/herbcraft/recipe/*adhesive*.json
```

Important mechanics from source:

- 40 current essence definitions are data-backed by `essences.json`.
- Static essence fields still exist, but initialize via JSON.
- Essence extraction recipes use 8 source items + glass bottle.
- Potions are registered from essence definitions and special potion data.
- High-tier potion quality/bonus is initialized through inventory scanning.
- Clarified/murky/special potions are advanced lines.
- Coatings use custom smithing recipes, adhesive items, `weapon_coating` component, and data-backed coating rules.
- Witches use safe data-backed potion throw pool and witch loot data.
- Hidden Cuisine catalysts upgrade clarified/murky potions to exceptional quality while preserving components.

### 2.2 Current Alchemy advancements available as reveal conditions

```text
first_essence
first_potion
first_clarified
first_murky
epic_potion
first_coating
witch_knowledge
survive_witch_brew
undead_toxin
```

These are enough for staged guide lines without adding new player data.

## 3. Structural recommendation

Use the existing `CodexGuideRegistry` and module-owned `codex_guides.json`.

Keep the current gateway entries under `herbal/overview`:

```text
guide_cuisine  -> 百草经总纲 / 膳房与药膳
guide_alchemy  -> 百草经总纲 / 炼金与精华
```

Add module-local overview sections:

```text
cuisine / overview  -> 膳房总述
alchemy / overview  -> 炼金总述
```

Then add module guide entries under those sections.

This keeps `百草经总纲` as a map and lets the module chapters become actual manuals.

## 4. Proposed Cuisine guide entries

Add these entries to:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/codex_guides.json
```

Keep existing `guide_cuisine` as `chapter: herbal, section: overview`.

Add:

```text
guide_cuisine_overview        -> chapter cuisine, section overview
guide_cuisine_water_bowl      -> chapter cuisine, section overview
guide_cuisine_dishes          -> chapter cuisine, section overview
guide_cuisine_hodgepodge      -> chapter cuisine, section overview
guide_cuisine_immunity        -> chapter cuisine, section overview
guide_cuisine_hidden_catalyst -> chapter cuisine, section overview
```

## 5. Cuisine copy: gateway page under 百草经总纲

This page should stay short. It should point to `膳房录`, not explain everything.

### Base lines

Chinese:

```text
膳房是把草木之性做成准备的路。若装有《膳房录》，可在其中细看水碗、药膳与百草盅。
```

English:

```text
Cuisine turns wild nature into preparation. If the Cuisine chapter is present, its pages explain water bowls, medicinal dishes, and hodgepodge.
```

### After first dish

Chinese:

```text
你已吃过药膳。总纲只指路，真正的做法与体会，应去《膳房录》慢慢翻。
```

English:

```text
You have eaten a medicinal dish. This overview only points the way; the Cuisine chapter holds the craft itself.
```

## 6. Cuisine copy: module chapter guide pages

### 6.1 `guide_cuisine_overview` / 膳房总述

Base:

```text
膳房不赌一口药性，而把草木之性化成准备。
水碗是入口，药膳是稳法，百草盅是快法。
```

After `first_dish`:

```text
你已吃过药膳：烹饪能把草木之性调得更稳。
```

Next-step hint if no `water_bowl` yet:

```text
若不知从何开始，先试着以空碗取水。
```

### 6.2 `guide_cuisine_water_bowl` / 水碗与药膳

Base:

```text
空碗可盛水。水碗可饮，也常作药膳之基。
```

After `water_bowl`:

```text
你已知水碗：可缓一时不适，也可入菜，却不真正修复五养。
```

Next-step hint after `water_bowl` but before `first_dish`:

```text
有了水碗，许多生坯药膳便有了开端；做成之后，还需经火候成菜。
```

This line is sourced from raw-dish recipes using `water_bowl` and cooking outputs.

### 6.3 `guide_cuisine_dishes` / 固定菜与准备

Base:

```text
固定菜重在可控。比起生食，它们少些赌性，多些准备。
```

After `first_dish`:

```text
你已吃过一道菜。其原料、清除与免疫之法，会逐渐写进膳房录。
```

If no `first_dish` but `water_bowl` done:

```text
有些菜只需空碗与原料，有些药膳则先成生坯，再经火候。
```

This stays recipe-general without revealing hidden dish formulas.

### 6.4 `guide_cuisine_hodgepodge` / 百草盅

Base:

```text
百草盅成得快，也更难读。多味同盅，可能相助，也可能相冲。
```

After `first_hodgepodge`:

```text
你已尝过百草盅：它能汇合原料之性，但也容易把冲突一并端上桌。
```

Source-grounded note:

```text
空碗可入盅，水碗不可；红菇与棕菇仍归原版迷之炖菜，不入百草盅。
```

This is useful and not a hidden spoiler because it explains recipe isolation that prevents confusion.

### 6.5 `guide_cuisine_immunity` / 清除与免疫

Base:

```text
牛奶是事后清除，药膳重在事前准备。
```

After `immunity_meal`:

```text
你已见过食疗免疫：某些菜不只解除不适，也能让相同不适暂时难以近身。
```

Optional line after `first_dish`:

```text
不是每道菜都能免疫；真正懂它，要看它清了什么，又挡了什么。
```

This reinforces the difference between milk and medicinal cuisine.

### 6.6 `guide_cuisine_hidden_catalyst` / 隐秘菜肴与炼金催化

Base, before hidden advancements:

```text
少数菜肴似乎不止能入口。传闻说，极端之膳也可能触及炼金。
```

After `hundred_flower_feast`:

```text
你已见过百花宴：百花同席，清正之药性也能被推至极致。
```

After `deadly_hodgepodge`:

```text
你已见过剧毒蛊：毒性驯得其法，也能成为炼金催化。
```

If Alchemy is not installed:

```text
若无炼金分册，此处只作传闻；若有炼金之术，或可另见用途。
```

Implementation note:

- The last line is tricky because Cuisine should not hard-depend on Alchemy.
- Easiest implementation: keep wording vague and do not explicitly detect Alchemy.
- Better implementation: if a safe mod-loaded check is available, only mention “炼金用途” when Alchemy is loaded.
- Do **not** reveal catalyst exact behavior before the player has reached the relevant dish/alchemy state.

## 7. Proposed Alchemy guide entries

Add these entries to:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json
```

Keep existing `guide_alchemy` as `chapter: herbal, section: overview`.

Add:

```text
guide_alchemy_overview        -> chapter alchemy, section overview
guide_alchemy_essences        -> chapter alchemy, section overview
guide_alchemy_potions         -> chapter alchemy, section overview
guide_alchemy_clarified_murky -> chapter alchemy, section overview
guide_alchemy_quality_bonus   -> chapter alchemy, section overview
guide_alchemy_coatings        -> chapter alchemy, section overview
guide_alchemy_witch           -> chapter alchemy, section overview
```

## 8. Alchemy copy: gateway page under 百草经总纲

This page should stay short. It points to `炼金要术`.

### Base lines

Chinese:

```text
炼金是把草木之性装入瓶与刃的路。若装有《炼金要术》，可在其中细看精华、药水与淬层。
```

English:

```text
Alchemy puts herb nature into bottles and blades. If the Alchemy chapter is present, its pages explain essences, potions, and coatings.
```

### After `first_essence`

Chinese:

```text
你已取得精华。总纲只指路，真正的配方与分路，应去《炼金要术》细看。
```

English:

```text
You have obtained an essence. This overview only points the way; the Alchemy chapter holds the paths and refinements.
```

## 9. Alchemy copy: module chapter guide pages

### 9.1 `guide_alchemy_overview` / 炼金总述

Base:

```text
炼金将草木之性入瓶。它比生食更稳定，也更讲契约。
精华是入口，药水是路径，淬层是外用。
```

After `first_essence`:

```text
你已取得精华：草木之性已有了瓶中形。
```

Next-step if no `first_essence`:

```text
若不知从何开始，先寻可萃之物，以玻璃瓶收其精华。
```

Source basis: essence recipes use source items + glass bottle.

### 9.2 `guide_alchemy_essences` / 精华

Base:

```text
并非所有草木都适合萃取。可萃之物，会把一部分性味交给玻璃瓶。
```

After `first_essence`:

```text
你已见过精华。它既是药水的根，也是部分炼金用途的钥匙。
```

Optional mastery line after multiple essences can be added later if desired:

```text
不同精华不只颜色不同，入瓶之后也会走向不同药路。
```

### 9.3 `guide_alchemy_potions` / 草药药水

Base:

```text
药水让药性按配方履约。比起生食，它少些偶然，多些代价。
```

After `first_potion`:

```text
你已取得草药药水：药性不再只在口中短暂翻涌，而能被瓶中保存。
```

Next-step after essence but before potion:

```text
精华只是根。若要成药，还需循炼药台之法，让药性入水。
```

Do not list exact brewing ingredients here; those are recipe/path knowledge.

### 9.4 `guide_alchemy_clarified_murky` / 澄清与浊化

Base:

```text
同一味药，清浊两路。澄清取其正，浊化留其毒。
```

After `first_clarified`:

```text
你已见过澄清：杂毒退去，正向药性更稳。
```

After `first_murky`:

```text
你已见过浊化：毒性被保留，也可用于另一种目的。
```

After both:

```text
清与浊不是强弱之分，而是用途之分。
```

This line is important: it helps players avoid thinking murky is simply “bad potion.”

### 9.5 `guide_alchemy_quality_bonus` / 品质与余韵

Base:

```text
高阶药水并非每瓶都一样。品质与余韵，会让同一路药水产生差别。
```

After `epic_potion`:

```text
你已见过极品药性：精炼至此，药水已经不只是原方的重复。
```

After `first_clarified` or `first_murky` but before exceptional:

```text
当药路走深，瓶中或有余韵，或有杂毒；并非每次都完全相同。
```

This hints at randomized high-tier components without exposing exact pools.

### 9.6 `guide_alchemy_coatings` / 武器淬层

Base:

```text
药性不一定只入口。以附着之法，草木之性也可暂附于刃。
```

After `first_coating`:

```text
你已见过淬层：每次命中，都是一次短暂的药性释放。
```

Before `first_coating`, after `first_essence`:

```text
精华若不入瓶，也可借附着之物暂留于武器。
```

Do not list adhesive recipes directly in this guide; recipe book / entry knowledge should cover that.

### 9.7 `guide_alchemy_witch` / 女巫与浊毒

Base:

```text
女巫的瓶中，也有草木余味。她们懂得一些危险而短促的浊化法。
```

After `witch_knowledge`:

```text
你已窥见女巫手稿：并非所有浊毒都为杀戮而生，有些只是扰乱与试探。
```

After `survive_witch_brew`:

```text
你从巫酿中活了下来。短促的浊毒，也能教人辨别安全边界。
```

After `undead_toxin`:

```text
你已触及亡灵毒液：有些药路不是为了治人，而是为了约束不该活动之物。
```

## 10. Staging and spoiler policy

### Safe to show from the start

- Route philosophy.
- Existence of water bowl, essence, potions, hodgepodge, coatings.
- The fact that Cuisine is safer/preparatory and Alchemy is more stable/contractual.

### Show only after relevant advancement

- Water bowl specifics after `water_bowl`.
- Dish stability after `first_dish`.
- Hodgepodge conflict after `first_hodgepodge`.
- Immunity purpose after `immunity_meal`.
- Clarified/murky meaning after `first_clarified` / `first_murky`.
- Quality/bonus after `epic_potion` or at least after advanced potion lines.
- Coating behavior after `first_coating`.
- Witch route after `witch_knowledge` / `survive_witch_brew`.

### Avoid showing too early

- Exact hidden dish recipes.
- Exact catalyst behavior for Hundred-Flower Feast / Deadly Hodgepodge before discovery.
- Exact potion bonus pools.
- Exact adhesive/coating recipes unless already learned through recipes/entries.
- Full T3 special potion recipes.

## 11. Recommended implementation details

### 11.1 Cuisine

- Add section in `CuisineChapter.register()`:

```java
KnowledgeAPI.registerSection("cuisine", "overview", Component.translatable("book.herbcraft_cuisine.section.overview"));
```

- Expand `cuisine codex_guides.json` with entries targeting:

```text
chapter: cuisine
section: overview
```

- Add providers in `CuisineChapter` for each `kind`, or one provider that switches on kind.

Suggested kinds:

```text
herbcraft_cuisine:overview
herbcraft_cuisine:water_bowl
herbcraft_cuisine:dishes
herbcraft_cuisine:hodgepodge
herbcraft_cuisine:immunity
herbcraft_cuisine:hidden_catalyst
```

### 11.2 Alchemy

- Add section in `AlchemyChapter.register()`:

```java
KnowledgeAPI.registerSection("alchemy", "overview", Component.translatable("book.herbcraft_alchemy.section.overview"));
```

- Expand `alchemy codex_guides.json` with entries targeting:

```text
chapter: alchemy
section: overview
```

Suggested kinds:

```text
herbcraft_alchemy:overview
herbcraft_alchemy:essences
herbcraft_alchemy:potions
herbcraft_alchemy:clarified_murky
herbcraft_alchemy:quality_bonus
herbcraft_alchemy:coatings
herbcraft_alchemy:witch
```

### 11.3 Validator updates

Update `validate_p1_codex_advancements.py` or add a new guide validator to check:

- Cuisine guide JSON includes the gateway guide plus module overview guide entries.
- Alchemy guide JSON includes the gateway guide plus module overview guide entries.
- Module guide entries target their own chapter/overview section.
- Lang keys exist in the correct module language files.
- Core still does not own Cuisine/Alchemy guide keys.

## 12. Recommended decision

Proceed with Direction C in a future modification turn.

Do not expand the `百草经总纲` gateway pages too much. Their job is to say:

> This route exists. Go to the proper module chapter for the craft.

Make the real teaching live inside:

```text
膳房录 / overview
炼金要术 / overview
```

This gives the two addon modules proper Codex status without making the Core overview bloated or breaking module ownership.
