# Herbcraft 1.1.2 New Player Guidance / Codex Activation Design

Date: 2026-06-16  
Status: design record / mostly implemented for current `1.1.2` polish  
Baseline: Herbcraft `1.1.1`

This document records the agreed direction for making Herbcraft easier to learn without weakening its knowledge-gated design.

Core problem:

> Herbcraft already has a dedicated advancement tab and many milestones, but they mostly record what the player did. They do not yet teach the player what to do next. The Herb Codex / 百草经 is flavorful, but it currently behaves too much like a directory or collector's book instead of a practical field guide.

Target for `1.1.2`:

> Make 百草经 and advancements into gentle guide rails. Teach the systems in stages, preserve mystery, avoid spoilers, and do not change balance.


## Implementation status

- Phase 1 text-only polish implemented on 2026-06-16:
  - route-sign advancement descriptions updated for Core, Cuisine, and Alchemy;
  - four-line `百草经总纲` rewritten;
  - 食材录 wording polished;
  - no balance changed.
- Phase 2 Codex guide entries implemented on 2026-06-16:
  - always-visible guide subentries added under `百草经总纲`;
  - guide lines become clearer from existing player discoveries such as tasted herb count, first tasted herb nature, discovered flavor count, herb-stack/cuisine/alchemy advancements, tasted ingredient count, heard claims, and verified claims;
  - 食材录 now has an always-visible overview entry.
- Phase 2.1 implemented on 2026-06-16:
  - guide entry registration is now data-backed by module-owned `codex_guides.json` files;
  - Cuisine/Alchemy guide entries are registered by their own modules and no longer appear in Core-only installs;
  - 五味与性味 no longer prints exact item/nature in the guide page;
  - flavor, temperature, and seven-emotion principles are tracked in player pharmacology state and can reveal individual guide lines with latest item examples.
- P3.7 one-time guide hints implemented on 2026-06-16:
  - data-backed module-owned `guide_hints.json` files;
  - `GuideHintManager` with persisted seen IDs.
- P3.9 Cuisine/Alchemy module overview guides implemented on 2026-06-16:
  - Cuisine and Alchemy now have their own overview sections/sub-guide entries in `膳房录` and `炼金要术`.
- Pharmacology guide microcopy implemented on 2026-06-16:
  - `五味与性味` now plainly says 性味 is pharmacology, not nutrition;
  - `七情与配伍` now plainly says 七情 is recent-herb interaction, not a fixed recipe list.
- Remaining work:
  - real-client UX check of guide pages, hints, and microcopy across module combinations.


## Post-client review note — 2026-06-16

Client check confirmed the original 9 guide entries appeared and text rendered normally. The two issues found in that review have since been addressed in source:

1. `膳房与药膳` and `炼金与精华` are now addon-owned and data-backed, so they only appear when their module is installed.
2. `五味与性味` no longer prints exact first tasted item/nature in the guide page; exact item nature remains in item entries, while principles unlock through player discoveries.

Detailed source audit and recommended fixes are recorded in:

```text
docs/reports/CODEX_GUIDANCE_PHASE2_CLIENT_REVIEW_20260616.md
```


## Phase 2.1 data-driven and principle-discovery spec — 2026-06-16

The author requested that guide entries be made data-driven immediately and that the Codex remember which item pairs triggered 七情 / pharmacology principles. This has now been implemented; the implementation/text spec is recorded in:

```text
docs/reports/CODEX_GUIDANCE_PHASE2_1_IMPLEMENTATION_SPEC_20260616.md
```

Key decisions proposed there:

- use bundled module guide JSON files (`data/herbcraft/codex_guides.json`, `data/herbcraft_cuisine/codex_guides.json`, `data/herbcraft_alchemy/codex_guides.json`);
- addon-specific guide entries must be owned by their addon modules;
- track latest item pair + count for discovered 七情/principle events;
- flavor/seven-emotion guide lines should reveal concrete meanings only after the player triggers them.

## 1. Agreed style

The guidance text should be:

- poetic, but still plain-spoken;
- allowed to use a little classical phrasing;
- not too literary or obscure;
- short enough to read during normal play;
- diegetic where possible: it should feel like the game world is speaking, not like a wiki popup.

Good style:

```text
草木入喉，性味难明。以书与蒲公英装订《百草经》，可记亲尝所得。
```

Too obscure:

```text
百卉入口，阴阳既分，五气流注，非谙经者不可轻辨。
```

Too dry:

```text
Craft a Codex with 1 Book and 1 Dandelion to view herb data.
```

English should be clearer than the Chinese if needed, but still have atmosphere.

Example:

```text
A wild taste lingers. Bind a Book with a Dandelion to make a Herbal Codex and record what you learn by tasting.
```

## 2. Agreed delivery mode: mixed guidance

No single UI channel should carry all onboarding.

Use each channel for what it does best:

| Channel | Best use | Notes |
|---|---|---|
| Advancement descriptions | route signs / milestone hints | Good for telling players what this branch is about. |
| One-time private chat messages | immediate next-step guidance | Use after first key actions; avoid spam. |
| ActionBar | bodily sensations / urgent short feedback | Good for herb stack, overload, correction, malnutrition. |
| Codex overview pages | durable in-world manual | Best place to explain systems without breaking immersion. |
| Codex entry state lines | next action for this entry | Tell player what to do next based on Unknown/Encountered/Heard/Tasted/Mastered. |
| Tooltips / Jade | contextual reminders | Must remain knowledge-gated. |
| Recipe book / recipes | practical route confirmation | Do not reveal hidden recipes too early. |

## 3. Knowledge gate rule for guidance

Guidance may teach **systems**, but must not spoil **content**.

Allowed to explain:

- herbs can be eaten;
- eating reveals knowledge;
- heard knowledge may be unreliable;
- cooking is safer than raw herb gambling;
- alchemy stabilizes herb effects;
- ordinary foods are recorded in 食材录;
- nutrition is long-term and can be corrected by varied eating;
- water bowl relief is temporary;
- `N` opens the nutrition panel.

Not allowed to reveal early:

- exact herb effects before tasting/mastery;
- hidden dish recipes;
- exact correction matrix weights;
- rumor truth values;
- advanced potion routes before the player reaches that route;
- exact nutrition thresholds in ordinary in-game hints.

## 4. Codex redesign direction

### 4.1 The four-line 总纲 must be rewritten

Current four-line 总纲 is atmospheric but too thin as guidance. `1.1.2` should revise it.

Draft Chinese:

```text
1. 百草可食，并不代表百草无害；第一次亲尝，只能得到模糊体感。
2. 残页带来听闻，听闻未必为真；亲尝与实践，才会使条目渐趋谙熟。
3. 生食是冒险，膳房是准备，炼金是提纯；三路各有所长。
4. 普通食物记入《食材录》，长期五养可按 [N] 查看。
```

Draft English:

```text
1. Edible does not mean harmless. A first taste gives only a rough impression.
2. Pages bring rumors, not proof. Tasting and practice turn rumor into knowledge.
3. Raw eating is risk; cuisine is preparation; alchemy is refinement.
4. Ordinary foods belong to the Ingredient Codex. Press [N] to view long-term nutrition.
```

### 4.2 Add guide sub-entries under 总纲

The Codex should have real guide pages, not only individual item entries.

Possible entries:

```text
百草经总纲
认识草木
听闻、亲尝与谙熟
五味与性味
七情与配伍
药性蓄积
膳房与药膳
炼金与精华
食材录与五养
残页与校勘
```

These guide entries should start vague and become clearer as the player actually discovers things.

### 4.3 Progressive clarity model for guide entries

Guide pages should not be static wiki pages. They should respond to player knowledge.

Example: 五味与性味 guide page.

Before tasting any herb:

```text
草木皆有性味。未曾入口，只能从残页与传闻中窥见一二。
```

After tasting one herb, e.g. dandelion:

```text
你已从蒲公英中辨出一缕性味：性平，味甘苦。
同为“甘”或“苦”的草木，未必同效，但可作为比较的起点。
```

After tasting multiple herbs with different flavors:

```text
甘多缓，苦多清，辛多行散。你已能分辨几类常见味感，但仍需亲尝印证。
```

After triggering a pharmacology relation:

```text
草木并非孤立。相近者可相须，相冲者可相反；配伍之理，需从食用顺序中体会。
```

After triggering specific seven-emotion relation(s):

```text
你曾见过“相须”：两味相近，正向药力更盛。
```

Important: this guide should teach principles only after the player has encountered them. It should not list all seven emotions from the start.

### 4.4 Entry-specific next-step lines

Each herb entry can show a short next-step hint based on knowledge state.

Unknown:

```text
尚无记录。见过、读残页或亲尝后，此处才会留下线索。
```

Encountered:

```text
你认得它的形貌，但未知其性。亲尝可验证，残页可得听闻。
```

Heard:

```text
此为听闻，不可尽信。可先标记存疑，待亲尝或勘误后再信。
```

Tasted:

```text
你已知其性味。若它另有用途，烹饪、入盅或萃取可使记录更完整。
```

Mastered:

```text
此条已谙熟，可作为烹饪、炼金或配伍参考。
```

This is one of the best ways to make the Codex useful without spoiling content.

## 5. 食材录 overview page

Agreed: 食材录 needs a 总述 / overview page.

Purpose:

- Explain why ordinary food is in the Codex.
- Explain that ordinary foods do not use herb pharmacology.
- Explain eating-gated nutrition knowledge.
- Point the player to the nutrition panel.

Draft Chinese:

```text
食材不涉药性，不生药性蓄积，也不入七情配伍。
但凡食皆养，谷粮、血肉、膏脂、菜蔬与津液会长期影响身体。
亲自吃过的食材，才会在此留下大致营养印象。
按 [N] 可查看当前五养状态。
```

Draft English:

```text
Ingredients are not herbs: they do not build herb stack or trigger pharmacology.
But every meal still nourishes. Grain, Flesh, Oil, Greens, and Fluids shape your long-term condition.
Only foods you have tasted reveal their rough nutrition here.
Press [N] to view your current nutrition state.
```

Recommended implementation:

- Add an always-mastered `overview` entry in `IngredientsChapter`.
- Keep the existing 39 food entries data-driven in `ingredients.json`.
- Do not expose exact values before eating.

## 6. One-time hint system: recommendation

The author has not yet fully decided the implementation style. Recommendation:

### 6.1 Do not rely only on advancements

Advancements are useful route signs, but they are not ideal for all hints:

- Some hints should not be public/announced.
- Some hints should not show toast.
- Some hints may be tied to UI actions or knowledge states rather than achievements.

### 6.2 Recommended implementation: `GuideHintManager`

Add a small Core-only helper:

```java
GuideHintManager.showOnce(ServerPlayer player, String hintId, Component message, Delivery delivery)
```

Possible delivery enum:

```text
CHAT_PRIVATE
ACTION_BAR
```

Store a small persisted set of seen hint IDs on the player, e.g. in `PlayerMixin` NBT.

Example hint IDs:

```text
first_herb_bite
first_codex_open
first_page_read
first_ingredient_tasted
first_nutrition_panel
first_correction
first_water_bowl
first_dish
first_essence
first_clarified
first_murky
first_coating
```

Messages should be in lang files, not hardcoded text.

### 6.3 Alternative: hidden advancement gating

Possible but less ideal:

- Use hidden advancements as once-only flags.
- Check if advancement is already completed before showing message.

Pros:

- No new player NBT structure.

Cons:

- More advancement JSON clutter.
- Some hints are not really achievements.
- Harder to control chat/actionbar separately from toast behavior.

Recommendation: use `GuideHintManager` if implementing more than 3-4 hints.

## 7. First recommended hint set for `1.1.2`

Keep it small.

### 7.1 First Herbcraft herb eaten

Trigger: first `first_bite` / herb consumption.

Chinese:

```text
草木入喉，性味难明。以书与蒲公英装订《百草经》，可记亲尝所得。
```

English:

```text
A wild taste lingers. Bind a Book with a Dandelion to make a Herbal Codex and record what you learn by tasting.
```

### 7.2 First Codex opened

Trigger: `CodexBookItem.use()` first time.

Chinese:

```text
左侧为目录，右侧为条目。听闻未必可信，亲尝才会入册。
```

English:

```text
The left page is the index; the right page is the record. Rumors are not proof — tasting makes knowledge your own.
```

### 7.3 First tattered page read

Trigger: first successful heard knowledge from a tattered page.

Chinese:

```text
残页只载听闻。可先存疑，待亲尝或勘误后再信。
```

English:

```text
A page gives rumor, not certainty. Mark doubts now; tasting or errata may prove them later.
```

### 7.4 First ingredient tasted

Trigger: first 食材录 entry mastered by eating.

Chinese:

```text
凡食皆养。普通食物不入药性，却会记入《食材录》的五养。
```

Optional second line:

```text
按 [N] 可查看长期营养状态。
```

English:

```text
Every meal nourishes. Ordinary foods do not carry herb nature, but the Ingredient Codex records their long-term nutrition.
```

### 7.5 First correcting state

Trigger: first `饮食回正中` state.

Chinese:

```text
偏食可回正。换食相补之物，过盛之养会慢慢平复。
```

English:

```text
An uneven diet can be corrected. Eat what balances the excess, and the surplus will settle over time.
```

### 7.6 First water bowl use / success

Trigger: first successful water bowl relief, or first water bowl use.

Chinese:

```text
水碗可缓一时不适，也可入药膳；但它不会真正修复五养。
```

English:

```text
A water bowl can soothe you for a while and serves cuisine, but it does not truly repair nutrition.
```

### 7.7 First dish cooked/eaten

Trigger: `first_dish`.

Chinese:

```text
烹饪不是赌药性。药膳重在清除、免疫与稳定准备。
```

English:

```text
Cuisine is not raw gambling. Medicinal dishes are preparation: cleansing, immunity, and steadier nourishment.
```

### 7.8 First essence obtained

Trigger: `first_essence`.

Chinese:

```text
精华使草木之性入瓶。炼金比生食更稳定，也更讲契约。
```

English:

```text
An essence bottles a plant's nature. Alchemy is steadier than raw eating, but it asks for a contract.
```

## 8. Advancement text as route signs

`1.1.2` can improve descriptions without adding many new advancements.

Examples:

Root:

Current meaning problem: root may be granted before the player owns a Codex. Avoid saying “获得百草经”.

Suggested:

```text
踏入百草可食之道，从此草木入口皆有学问。
```

First bite:

```text
第一次亲尝草木。若想记录所知，可用书与蒲公英装订《百草经》。
```

First page:

```text
残页带来听闻；听闻未必是真，亲尝才能验证。
```

Herb stack 3:

```text
短时连食会蓄积药性，收益与风险都会上升。
```

Herb stack 8:

```text
药势已盛，再贪一口就可能反噬。
```

Water bowl:

```text
盛水入碗，可饮可烹；缓一时不适，却不改长期五养。
```

First essence:

```text
将草木之性萃入瓶中，开始炼金之路。
```

## 9. Implementation phases

### Phase 1: text-only polish — implemented

Low risk.

- Rewrite root advancement description.
- Rewrite first-bite / first-page / herb-stack descriptions.
- Rewrite current four-line 总纲.
- Improve 食材录 wording.
- No mechanics change.

### Phase 2: guide overview entries — implemented

Moderate-low risk.

- Add always-mastered guide subentries under `herbal/overview`.
- Add 食材录总述 entry.
- Keep all content vague until player earns knowledge.

### Phase 3: one-time hint system — implemented

Moderate risk due to player-data persistence.

- Add `GuideHintManager`.
- Add persisted seen-hint IDs.
- Add lang keys for first key actions.
- Trigger a small number of hints.

### Phase 4: progressive principle unlocks — implemented for current 五味/温性/七情 guide discovery

Higher design complexity.

- Track or derive discovered flavors / temperatures / seven-emotion interactions.
- Guide pages become clearer as the player experiences systems.
- Avoid listing complete mechanics too early.

Possible implementation approaches:

1. Derived from existing knowledge flags and tasted herbs.
2. Hidden advancement/criteria flags for first seven-emotion events.
3. New player-data set for discovered principles.

Recommended: start with derived data where possible; add new persistent principle flags only when needed.

## 10. Design questions resolved for current implementation

Current answers:

1. Core owns seven guide subentries under 百草经总纲; Cuisine/Alchemy own their own gateway and module overview guide entries.
2. Guide entries are data-backed and progressive where useful.
3. `GuideHintManager` is implemented with module-owned `guide_hints.json` files and persisted seen IDs.
4. 七情/五味/温性 discoveries are tracked in player pharmacology state, not hidden advancements.
5. 食材录 overview is always visible.
6. First herb eaten hints the Codex recipe.
7. 五味/七情 pages now include small plain-language microcopy so they teach the system without adding more advancements.

## 11. Design summary

`1.1.2` should not be about more content.

It should make the existing content legible:

```text
Advancements become route signs.
The Codex becomes a field guide.
Guide pages start vague and clear up through play.
One-time hints tell the player the next step, not the answer.
食材录 explains ordinary food and long-term nutrition.
```

This keeps Herbcraft's central promise intact:

> 信息是行动的奖励，但玩家必须先知道有哪些行动值得尝试。  
> Information is earned, but the player must first know what actions are worth trying.
