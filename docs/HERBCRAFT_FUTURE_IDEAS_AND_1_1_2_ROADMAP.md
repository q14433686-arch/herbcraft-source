# Herbcraft Future Ideas and 1.1.2 Roadmap

Date: 2026-06-16  
Status: planning / idea bank; partially implemented for current `1.1.2` polish  
Baseline: Herbcraft `1.1.1`, Minecraft `26.1.2`

> Update 2026-06-16: several `1.1.2` polish candidates from this roadmap are now implemented in source: data-backed/module-owned Codex guide entries, one-time guide hints, pharmacology rule externalization/tuning, Cuisine/Alchemy overview guides, raw herb `food_rolls`, seed/sugar-cane food tuning, and plain 五味/七情 Codex microcopy. Treat the roadmap below as an idea bank, not a live task list; use `CURRENT_BASELINE.md`, `NEXT_TASK.md`, and `docs/DOCUMENTATION_INDEX.md` for current status.

This document collects post-`1.1.1` ideas so future work can stay aligned with Herbcraft's core identity instead of becoming random feature creep.

Core identity:

> 生食是赌博，厨房是手艺，炼金是契约，百草经是玩家认知成长。  
> Eating herbs is a gamble; cuisine is craft; alchemy is contract; the Codex is player knowledge made visible.

## 0. Immediate release philosophy

`1.1.1` has passed local client testing and has been published on CurseForge. The next version should be a steady `1.1.2`, not a huge expansion.

### Do first

- Keep `1.1.1` stable.
- Use real player feedback from CurseForge and the second platform once it passes review.
- Fix obvious bugs, text mistakes, bad recipes, or hard conflicts.
- Improve first-time-player understanding.
- Avoid destabilizing core mechanics right after public release.

### Do not rush

- Do not immediately add a large new survival burden.
- Do not merge hard-core nutrition into the base mod.
- Do not implement P4 Java API before the extension semantics are clearly designed.
- Do not add large systems that bypass the knowledge-gated design.

## 1. Recommended scope for `1.1.2`

Detailed onboarding/Codex activation design is in:

```text
docs/NEW_PLAYER_GUIDANCE_1_1_2_DESIGN.md
```

`1.1.2` should feel like a polished release, not a new era.

### 1.1 New player guidance

The strongest candidate for `1.1.2` is better onboarding. Herbcraft has many systems; new players need a gentle path without spoiling everything.

Possible changes:

1. First Herbcraft herb eaten:
   - Send a private message hinting that the player has tasted a medicinal property.
   - Mention the Herb Codex indirectly.
2. First Herb Codex opened:
   - Make the overview / 总纲 clearer as an in-game guide.
3. First vanilla food with nutrition profile eaten:
   - Hint that ordinary food is recorded in 食材录.
4. First nutrition imbalance/correction:
   - Explain `饮食回正中` in one short line.
5. First water bowl use:
   - Make clear it gives temporary relief, not true nutrition repair.

Tone should stay diegetic and concise. Avoid tutorial spam.

### 1.2 Codex overview polish

The `百草经总纲` and 食材录 explanations can become the player's in-game manual.

Good topics for overview pages:

- Raw herbs are risky and knowledge-gated.
- Heard claims are not guaranteed truth.
- Tasted/practiced knowledge is more reliable.
- Cooking is safer than raw herb gambling.
- Alchemy stabilizes and contracts herb effects.
- Nutrition values are long-term tendencies, not instant food stats.
- 津液 / Fluids is internally `fruit` for compatibility.

### 1.3 Tooltip wording polish

Polish only the text layer, not mechanics:

- Nutrition tooltip wording for 食材录 entries.
- Water bowl relief wording.
- `饮食回正中` status wording.
- Hidden recipe hint wording so it helps without spoiling.
- English long lines in Codex/Jade/tooltips.

### 1.4 Client sanity report

Since the author has tested and reported `1.1.1` as running perfectly with no obvious bugs, record it in a new report under `docs/reports/`.

Suggested file:

```text
docs/reports/CLIENT_VALIDATION_1_1_1_RELEASE_20260616.md
```

Include:

- Tested jars.
- Tested module combinations.
- Food/nutrition checks.
- 食材录 checks.
- Codex/Jade checks.
- Hidden cuisine catalyst checks.
- Known remaining non-blockers.

### 1.5 Release-page improvements

The public page should explain the player-facing fantasy, not just list features.

Suggested short pitch:

```text
万物皆可尝，但知识要靠亲身试出来。生食冒险，烹饪求稳，炼金提纯，百草经记录你的经验。
```

English:

```text
Eat wild plants, learn their nature, cook safer meals, distill essences, and build your own Codex of survival knowledge.
```

## 2. Medium-term idea bank

These ideas are not all for `1.1.2`. They are preserved here for later design sessions.

## 2.1 药引子 / 引经药 / guiding ingredients

Concept:

> Some ingredients do not provide strong effects by themselves, but guide, soften, redirect, or stabilize other herb effects.

This fits Herbcraft's existing pharmacology theme better than adding random new buffs.

Possible names:

- 药引
- 引经
- 佐使
- 调和物
- Guiding ingredient
- Adjuvant

Potential examples:

| Ingredient | Direction |
|---|---|
| Honey / honeycomb | Softens side effects, improves clarified/stable outcomes. |
| Kelp / salt-like sea items | Guides toward 津液 / Fluids and aquatic traits. |
| Ginger-like future item or hot foods | Guides toward warmth / yang / warming behavior. |
| Ice / snow | Guides toward cold/cooling behavior. |
| Golden dandelion | Stabilizes positive effects or improves knowledge/practice routes. |
| Toxic flowers | Strengthens murky routes, increases risk, may improve poison-facing recipes. |

Implementation ideas:

- As cuisine recipe modifiers.
- As alchemy catalyst modifiers.
- As hodgepodge secondary traits.
- As data-backed rules, not scattered Java literals.

Important constraint:

Do not make 药引子 a second hidden enchantment system. It should be readable through Codex and recipes.

## 2.2 Personal Codex notes / player annotations

Concept:

Players can write short personal notes on Codex entries.

Examples:

- “别空腹吃这个。”
- “下界前带两份。”
- “配海带汤不错。”
- “村民会收这个。”

Why it fits:

The Codex is already the center of player knowledge. Personal notes would make it feel like the player's own field book.

Risks:

- Needs text input UI.
- Needs save data and sync.
- Needs moderation considerations on servers.
- Should be optional and not required for gameplay.

Recommended timing:

Not `1.1.2`. Consider after the Codex UI is stable and player demand exists.

## 2.3 Stronger rumor / errata chains

Concept:

Add a small number of memorable rumors that are partially true, conditional, or false.

Examples:

- “连食铃兰三次，可见幽光。”
- “雪中嚼花，寒性更甚。”
- “毒物入汤，未必皆毒。”

Gameplay purpose:

- Make Heard state feel flavorful.
- Encourage experiments.
- Make errata pages more meaningful.

Important constraint:

Do not generate fake claims randomly in Java. Claims should remain hand-authored JSON/lang data.

## 2.4 Light environmental interactions

Concept:

Small environmental modifiers that make the world matter without becoming a heavy climate system.

Possible examples:

- Rain: cold/cool herbs become slightly stronger or riskier.
- Nether: warm/hot/nether traits become more stable.
- Snow/ice biomes: snow/ice foods and cold traits gain contextual meaning.
- Swamps: toxic/clearing rumors or loot feel more likely.
- Villages: basic Codex pages and food knowledge become easier to find.

Important constraint:

Keep this small and explainable. Do not make the base mod an invisible simulation trap.

## 2.5 Homeostatic temperature compatibility

Current state:

- Thirst/drinkable data compatibility exists.
- Temperature gameplay integration is not implemented.
- Prior notes say no safe item-temperature data surface was found in that research round.

Future direction:

- Prefer Homeostatic's own data format if it exposes one.
- Prefer public API if available.
- Use soft dependency adapter if safe.
- Avoid direct core-temperature manipulation unless thoroughly tested.

Good fit examples:

- Ice/snow foods: mild cooling.
- Hot soups/stews: mild warming.
- Nether foods: heat risk or heat support.

Bad fit examples:

- Permanent body-temperature manipulation.
- Negative residue bugs similar to the old negative water amount issue.
- Hard dependency from Herbcraft Core to Homeostatic.

## 2.6 Hardcore Nutrition addon

Recommended as a separate addon, not base Herbcraft.

Possible name ideas:

- Herbcraft: Harsh Nutrition
- Herbcraft: Famine
- 百草可食：饥馑篇
- 百草可食：五养严律

Concept:

The base mod remains knowledge/craft/alchemy-focused and generally playable. The addon makes nutrition and environment genuinely demanding.

Possible features:

- Stronger deficiency penalties.
- High flesh/oil/grain excess consequences.
- More serious hydration/津液 interactions.
- Homeostatic integration as recommended or optional dependency.
- Seasonal or biome scarcity incentives.
- Long-term recovery instead of quick correction.

Important constraint:

Do not harden the main mod by default. Keep the strict survival fantasy opt-in.

## 2.7 P4 public Java API

The API should be designed before coding.

Use cases:

- Mods registering their items as Herbcraft-natured.
- Mods adding nutrition profiles.
- Mods adding special counters.
- Mods adding herb food definitions at safe startup time.
- Mods adding knowledge rumors or Codex hooks.

Risks:

- Public API is a compatibility contract.
- Bad call timing can break registries or default components.
- Server reload-safe data and startup-only edible data must stay separate.

Recommended deliverable before implementation:

```text
docs/PUBLIC_EXTENSION_API_DESIGN.md
```

## 3. Suggested roadmap

### `1.1.2` — polish and guidance

Best candidates:

- New-player hints.
- Codex overview polish.
- 食材录 wording/UI sanity improvements.
- Record official 1.1.1 client validation report.
- Release-page copy/screenshots.
- Small bug/text fixes only.

### `1.2.0` — extension stability

Best candidates:

- P4 API design document.
- Better addon examples.
- More validators.
- Improved resource generators.
- External 食材录/nutrition/rumor documentation if needed.

### `1.3.0+` — new mechanics

Best candidates:

- 药引子 / guiding ingredients.
- Personal Codex annotations.
- Richer rumor/errata chains.
- Light environmental interactions.
- Homeostatic temperature addon/adapter if safe.

### Separate addon

- Hardcore Nutrition / 饥馑篇.

## 4. Design tests for any future idea

Before implementing any new feature, ask:

1. Does it strengthen the central fantasy?
2. Does it respect knowledge gating?
3. Does it keep modules independent?
4. Is content stored in JSON where a loader exists?
5. Are stable IDs preserved?
6. Can a validator prevent future regression?
7. Can a new player understand it through Codex, tooltip, advancement, or recipe hints?
8. Is this base-mod material, or should it be an optional addon?

If the answer is unclear, write a design note first instead of coding immediately.
