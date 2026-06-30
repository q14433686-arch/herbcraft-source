# Cuisine / Alchemy Guidance Direction Discussion — 2026-06-16

Status: discussion / source-audit / design proposal  
Baseline: Herbcraft `1.1.1`, planning target `1.1.2`

This report discusses whether `膳房录` / Cuisine and `炼金要术` / Alchemy need stronger in-Codex guidance beyond their current advancement routes and single guide entries under `百草经总纲`.

## 1. Source state checked

Relevant current files:

```text
cuisine/src/main/java/com/herbcraft/cuisine/knowledge/CuisineChapter.java
cuisine/src/main/resources/data/herbcraft_cuisine/codex_guides.json
cuisine/src/main/resources/data/herbcraft_cuisine/guide_hints.json
cuisine/src/main/resources/data/herbcraft_cuisine/advancement/*.json

alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java
alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json
alchemy/src/main/resources/data/herbcraft_alchemy/guide_hints.json
alchemy/src/main/resources/data/herbcraft_alchemy/advancement/*.json
```

Current Cuisine guide state:

- `data/herbcraft_cuisine/codex_guides.json` registers one guide entry:
  - `guide_cuisine`
  - chapter: `herbal`
  - section: `overview`
  - title: `膳房与药膳`
- Its lines are produced by `CuisineChapter.guideLines(...)`.
- It responds to existing advancements:
  - `water_bowl`
  - `first_dish`
  - `first_hodgepodge`
  - `immunity_meal`

Current Alchemy guide state:

- `data/herbcraft_alchemy/codex_guides.json` registers one guide entry:
  - `guide_alchemy`
  - chapter: `herbal`
  - section: `overview`
  - title: `炼金与精华`
- Its lines are produced by `AlchemyChapter.guideLines(...)`.
- It responds to existing advancements:
  - `first_essence`
  - `first_potion`
  - `first_clarified` / `first_murky`
  - `first_coating`

Current advancement state:

- Cuisine already has a usable route-sign advancement line:
  - `water_bowl`
  - `first_dish`
  - `first_hodgepodge`
  - `immunity_meal`
  - `hundred_flower_feast`
  - `deadly_hodgepodge`
- Alchemy already has a usable route-sign advancement line:
  - `first_essence`
  - `first_potion`
  - `first_clarified`
  - `first_murky`
  - `epic_potion`
  - `first_coating`
  - `witch_knowledge`
  - `survive_witch_brew`
  - `undead_toxin`

## 2. Diagnosis

The author’s concern is valid:

> Cuisine and Alchemy already have advancement guidance, but inside the Codex their presence under `百草经总纲` is still light. They exist as single gateway pages, not as deeper field-guide structures.

The current single guide pages are good as **route doors**, but they are not enough to teach each route in depth.

The Codex currently has three layers:

1. `百草经总纲`: broad Core guidance.
2. `膳房录` / `炼金要术`: actual module chapter entries.
3. Advancements: milestone route signs.

What is missing:

- Module-specific overview pages inside `膳房录` and `炼金要术`.
- Sub-guide pages for key route concepts.
- A clearer difference between:
  - Core 总纲 = “what route exists?”
  - Module chapter overview = “how this route works?”
  - Individual entries = “what this item/dish/potion does?”

## 3. Design principle

Do not overload `百草经总纲` with full Cuisine/Alchemy manuals.

Recommended meaning of each layer:

| Layer | Role |
|---|---|
| `百草经总纲` | Broad map: tells the player that Cuisine and Alchemy exist and what their philosophy is. |
| `膳房录` overview | Cuisine manual: water bowl, dish stability, hodgepodge, immunity meals, hidden feast/toxin direction. |
| `炼金要术` overview | Alchemy manual: essences, potions, clarified/murky split, quality/bonus, coatings, witch/advanced routes. |
| Individual entries | Specific earned knowledge, still gated by practice/knowledge state. |
| Advancements | Route signs and milestone confirmations. |
| One-time hints | First-step nudges, not full explanation. |

This keeps the Codex useful without turning the main 总纲 into a wiki dump.

## 4. Proposed directions

## Direction A — Keep only the current gateway pages

Do nothing major.

Current `膳房与药膳` and `炼金与精华` remain as single guide entries under `百草经总纲`.

Pros:

- Already implemented.
- Low risk.
- No additional UI clutter.

Cons:

- Does not solve the author’s concern.
- Module chapters still feel more like content lists than learning manuals.
- Players may still not understand how to progress within Cuisine or Alchemy.

Recommendation: not enough for `1.1.2` if the goal is smoother onboarding.

## Direction B — Expand the existing two gateway pages only

Keep `guide_cuisine` and `guide_alchemy` under `百草经总纲`, but add more dynamic lines to those pages.

Cuisine page could reveal:

- before water bowl: “先以空碗取水。”
- after water bowl: “水碗可饮可烹，不改长期五养。”
- after first dish: “烹饪把风险变成准备。”
- after hodgepodge: “百草盅快，但配伍难读。”
- after immunity meal: “药膳可提供免疫。”
- after hidden dishes: “百花宴/剧毒蛊可触及炼金催化。”

Alchemy page could reveal:

- before essence: “先从精华开始。”
- after essence: “精华是草木入瓶。”
- after potion: “药水按配方履约。”
- after clarified/murky: “澄清取正，浊化留毒。”
- after quality/exceptional: “品质改变药性强弱。”
- after coating: “草木之性可附于刃。”
- after witch route: “女巫也在用草药炼金。”

Pros:

- Simple; existing providers can be expanded.
- Uses existing advancement flags.
- No new chapter structure.

Cons:

- `百草经总纲` becomes crowded.
- These pages become too important compared to their own module chapters.
- Still does not make `膳房录` / `炼金要术` themselves feel like manuals.

Recommendation: acceptable short-term, but not ideal.

## Direction C — Add module-owned overview sections and sub-guide pages inside the module chapters

This is the recommended direction.

### Cuisine chapter structure proposal

Add section:

```text
膳房总述 / overview
```

Add guide entries in Cuisine-owned `codex_guides.json`, targeting chapter `cuisine`:

```text
guide_cuisine_overview
guide_cuisine_water_bowl
guide_cuisine_dishes
guide_cuisine_hodgepodge
guide_cuisine_immunity
guide_cuisine_hidden_catalysts
```

Current `guide_cuisine` under `herbal/overview` remains as a short gateway page.

### Alchemy chapter structure proposal

Add section:

```text
炼金总述 / overview
```

Add guide entries in Alchemy-owned `codex_guides.json`, targeting chapter `alchemy`:

```text
guide_alchemy_overview
guide_alchemy_essences
guide_alchemy_potions
guide_alchemy_clarified_murky
guide_alchemy_quality_bonus
guide_alchemy_coatings
guide_alchemy_witch
```

Current `guide_alchemy` under `herbal/overview` remains as a short gateway page.

Pros:

- Best learning structure.
- Keeps 总纲 as a map, not a wiki.
- Makes module chapters valuable even before the player has many entries.
- Fully respects module ownership.
- Can stay data-backed via each module’s `codex_guides.json`.

Cons:

- Requires adding overview sections to `CuisineChapter` and `AlchemyChapter`.
- Requires more guide line providers / lang keys.
- Needs client check for chapter tree readability.

Recommendation: best target for `1.1.2` if we want the Codex to become a real field guide.

## Direction D — Add one-time hints only, no new Codex pages

Use the already implemented `guide_hints.json` / `GuideHintManager` system to teach Cuisine/Alchemy through first-use messages.

Pros:

- Lightweight.
- Already has infrastructure.
- Helps first-time behavior.

Cons:

- Hints disappear after reading.
- Does not make Codex more useful as a permanent reference.
- Not enough by itself.

Recommendation: useful as supplement, not replacement.

## Direction E — Add route breadcrumbs inside individual Cuisine/Alchemy entries

Example for a Cuisine dish entry:

- Heard: “你知道它是一道药膳，但未亲食。”
- Mastered: “此菜可清除/免疫/补养。”
- If hidden catalyst: “此菜似乎还能影响炼金。”

Example for an Alchemy essence/potion entry:

- Essence heard: “此精华可作为药水的根。”
- Potion heard: “此药水路线尚未亲验。”
- Mastered: “可澄清/浊化/长效/强化。”

Pros:

- Makes individual entries more actionable.
- Reinforces knowledge gates.

Cons:

- Larger text-writing pass.
- More risk of spoilers if not careful.

Recommendation: later polish after overview guides.

## 5. Recommended `1.1.2` plan

Recommended order:

1. Keep current `guide_cuisine` and `guide_alchemy` under `百草经总纲` as short route gateway pages.
2. Add module chapter overview sections:
   - `cuisine/overview`
   - `alchemy/overview`
3. Add data-backed module guide entries to existing module `codex_guides.json` files.
4. Use existing advancements as reveal conditions first; do not add new save data unless needed.
5. Add/adjust one-time hints only as first-step nudges.
6. Client-test chapter tree readability.

## 6. Proposed Cuisine guide entries and copy

### `guide_cuisine_overview`

Base:

```text
膳房不赌一口药性，而把草木之性化成准备。
水碗是入口，药膳是稳法，百草盅是快法。
```

After first dish:

```text
你已吃过药膳：烹饪能把草木之性调得更稳。
```

### `guide_cuisine_water_bowl`

Base:

```text
空碗可盛水。水碗可饮，也常作药膳之基。
```

After water bowl:

```text
你已知水碗：可缓一时不适，却不真正修复五养。
```

### `guide_cuisine_dishes`

Base:

```text
固定菜重在可控。比起生食，它们少些赌性，多些准备。
```

After first dish:

```text
你已吃过一道菜。其原料、清除与免疫之法，会逐渐写进膳房录。
```

### `guide_cuisine_hodgepodge`

Base:

```text
百草盅成得快，也更难读。多味同盅，可能相助，也可能相冲。
```

After first hodgepodge:

```text
你已尝过百草盅：它能汇合原料之性，但也容易把冲突一并端上桌。
```

### `guide_cuisine_immunity`

Base:

```text
牛奶是事后清除，药膳重在事前准备。
```

After immunity meal:

```text
你已见过食疗免疫：某些菜不只解除不适，也能让相同不适暂时难以近身。
```

### `guide_cuisine_hidden_catalysts`

Before hidden dishes:

```text
少数菜肴似乎不止能入口。传闻说，极端之膳也可能触及炼金。
```

After hundred flower feast:

```text
你已见过百花宴：百花同席，清正之药性也能被推至极致。
```

After deadly hodgepodge:

```text
你已见过剧毒蛊：毒性驯得其法，也能成为炼金催化。
```

## 7. Proposed Alchemy guide entries and copy

### `guide_alchemy_overview`

Base:

```text
炼金将草木之性入瓶。它比生食更稳定，也更讲契约。
精华是入口，药水是路径，淬层是外用。
```

After first essence:

```text
你已取得精华：草木之性已有了瓶中形。
```

### `guide_alchemy_essences`

Base:

```text
并非所有草木都适合萃取。可萃之物，会把一部分性味交给玻璃瓶。
```

After first essence:

```text
你已见过精华。它既是药水的根，也是部分炼金用途的钥匙。
```

### `guide_alchemy_potions`

Base:

```text
药水让药性按配方履约。比起生食，它少些偶然，多些代价。
```

After first potion:

```text
你已取得草药药水：药性不再只在口中短暂翻涌，而能被瓶中保存。
```

### `guide_alchemy_clarified_murky`

Base:

```text
同一味药，清浊两路。澄清取其正，浊化留其毒。
```

After clarified:

```text
你已见过澄清：杂毒退去，正向药性更稳。
```

After murky:

```text
你已见过浊化：毒性被保留，也可用于另一种目的。
```

### `guide_alchemy_quality_bonus`

Base:

```text
高阶药水并非每瓶都一样。品质与余韵，会让同一路药水产生差别。
```

After exceptional:

```text
你已见过极品药性：精炼至此，药水已经不只是原方的重复。
```

### `guide_alchemy_coatings`

Base:

```text
药性不一定只入口。以附着之法，草木之性也可暂附于刃。 
```

After first coating:

```text
你已见过淬层：每次命中，都是一次短暂的药性释放。
```

### `guide_alchemy_witch`

Base:

```text
女巫的瓶中，也有草木余味。她们懂得一些危险而短促的浊化法。
```

After witch knowledge:

```text
你已窥见女巫手稿：并非所有浊毒都为杀戮而生，有些只是扰乱与试探。
```

## 8. Implementation suggestion

Use current data-driven guide mechanism.

Cuisine `codex_guides.json` could expand from one entry to multiple entries:

```json
{
  "id": "guide_cuisine_overview",
  "chapter": "cuisine",
  "section": "overview",
  "title_key": "book.herbcraft_cuisine.guide.overview.title",
  "kind": "herbcraft_cuisine:overview",
  "always_visible": true
}
```

Alchemy equivalent:

```json
{
  "id": "guide_alchemy_overview",
  "chapter": "alchemy",
  "section": "overview",
  "title_key": "book.herbcraft_alchemy.guide.overview.title",
  "kind": "herbcraft_alchemy:overview",
  "always_visible": true
}
```

Need code changes:

- `CuisineChapter.register()` should register section `overview` before loading module guide JSON.
- `AlchemyChapter.register()` should register section `overview` before loading module guide JSON.
- Add provider functions for the new guide kinds.
- Update validators so module guide files can contain both the `herbal/overview` gateway guide and their own chapter guide entries.

## 9. Recommended immediate decision

I recommend Direction C for `1.1.2`:

> Keep the two 总纲 gateway pages, but add proper module-owned overview sections and guide subentries inside `膳房录` and `炼金要术`.

This gives Cuisine/Alchemy a higher Codex status without cluttering the main 总纲.

It also fits the current data-driven architecture: the entries live in module-owned `codex_guides.json`, and the modules own their own guide providers.

## 10. What not to do

- Do not move all Cuisine/Alchemy guidance into Core.
- Do not add Mixins.
- Do not expose hidden dish recipes in guide pages.
- Do not explain exact potion recipes before the player reaches those lines.
- Do not make guide pages replace actual item/dish/potion entries.

The guide pages should teach route logic. Specific entries should still hold earned details.
