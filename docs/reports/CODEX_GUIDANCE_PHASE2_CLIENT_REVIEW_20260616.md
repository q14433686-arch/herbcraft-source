# Codex Guidance Phase 2 Client Review / Source Audit — 2026-06-16

## Context

The author client-tested the new `1.1.2` Codex guidance Phase 2 pages.

Confirmed good:

- The 9 new entries under `百草经总纲` appear in the client.
- Text renders normally.

Client review found two design/architecture issues that should be fixed before treating Phase 2 as release-ready.

## Issue 1: Cuisine/Alchemy guide entries appear when addon modules are absent

### Observed behavior

When the Cuisine or Alchemy addon jar is not installed, the Core Codex still shows:

- `膳房与药膳`
- `炼金与精华`

These should not appear unless their corresponding addon is loaded.

### Source cause

Current implementation registers all guide entries from Core in `HerbalChapter.registerGuideEntries()`:

```java
for (String guide : List.of(
   "recognizing", "knowledge_states", "flavors", "seven_emotions", "herb_stack", "cuisine", "alchemy", "ingredients", "errata"
))
```

This is unconditional and Core-owned, so the two addon-specific guide pages are visible even if `herbcraft_cuisine` or `herbcraft_alchemy` is absent.

### Why this is wrong

It violates the module-independence philosophy:

- Core should run cleanly alone.
- Cuisine-specific guide content should be owned by Cuisine.
- Alchemy-specific guide content should be owned by Alchemy.
- Missing addons should not leave their guide pages in the Core guide section.

It also looks like a hardcoded Java content table, which conflicts with the current data-first direction unless clearly justified as mechanism code.

### Recommended fix

Preferred immediate fix for `1.1.2`:

1. Core `HerbalChapter` registers only Core-relevant guide entries:
   - `recognizing`
   - `knowledge_states`
   - `flavors`
   - `seven_emotions`
   - `herb_stack`
   - `ingredients`
   - `errata`
2. Cuisine module registers `guide_cuisine` into the existing Core `herbal/overview` section from `CuisineChapter.register()` or another Cuisine knowledge hook.
3. Alchemy module registers `guide_alchemy` into the existing Core `herbal/overview` section from `AlchemyChapter.register()` or another Alchemy knowledge hook.
4. Move Cuisine guide lang keys to Cuisine lang files, and Alchemy guide lang keys to Alchemy lang files, or use module-owned lang keys such as:

```text
book.herbcraft_cuisine.guide.cuisine.*
book.herbcraft_alchemy.guide.alchemy.*
```

Why this is good:

- No Mixins.
- Uses existing `KnowledgeAPI`.
- Relies on module loading naturally: if the addon jar is absent, its guide entry is never registered.
- Keeps Core independent.
- Keeps addon guide text with addon content.

Alternative long-term data-driven option:

Add a guide-entry data loader, e.g. bundled module files:

```text
data/herbcraft/codex_guides.json
data/herbcraft_cuisine/codex_guides.json
data/herbcraft_alchemy/codex_guides.json
```

Each entry could define:

```json
{
  "id": "guide_cuisine",
  "chapter": "herbal",
  "section": "overview",
  "title_key": "book.herbcraft_cuisine.guide.cuisine.title",
  "kind": "cuisine",
  "always_visible": true,
  "optional_mod": "herbcraft_cuisine"
}
```

Java would still own dynamic line generation, but the guide-entry list and module ownership would become data-backed. This is cleaner long-term but larger than the immediate `1.1.2` fix.

### Validator updates recommended

- `scripts/validate_p1_codex_advancements.py` should fail if Core `HerbalChapter` contains unconditional `"cuisine"` or `"alchemy"` in its guide list.
- Validator should require Cuisine source to register/provide `guide_cuisine` and Alchemy source to register/provide `guide_alchemy`.
- Built-jar or source checks should confirm module-specific guide lang keys are in their module lang files.

## Issue 2: 五味与性味 currently reveals too explicit item nature in the system guide

### Observed behavior

The `五味与性味` guide currently says, after the player has tasted a herb:

```text
你已从 %s 中辨出一缕性味：%s。
```

This can display an exact item and exact nature line, e.g. `性味：平，甘、苦`.

### Source cause

Current implementation in `HerbalChapter.guideLines(...)` does:

```java
Component.translatable(
   "book.herbcraft.guide.flavors.first",
   Component.translatable(first.item().getDescriptionId()),
   HerbNatureAPI.natureLine(first.nature())
)
```

### Nuance

This is not technically a raw content leak if the player has already tasted the herb, because individual herb entries already show nature after tasting.

However, as a **guide page**, this is still too direct. The guide should teach the principle gradually, not repeat exact item data. Specific item nature belongs in that item entry.

### Desired learning curve

The guide should start vague and become clearer as the player earns system understanding.

Suggested staged model:

#### Stage 0 — no tasted herbs

```text
草木皆有性味。未曾入口，只能从残页与传闻中窥见一二。
```

#### Stage 1 — first tasted herb

```text
你已亲尝过一种草木，开始能分辨寒热与若干味感。具体性味可在对应条目查看。
```

No exact item and no exact nature line needed in the guide page.

#### Stage 2 — multiple flavors discovered

```text
你已辨出数类味感。同味未必同效，却可作为比较的起点。
```

#### Stage 3 — flavor principle actually experienced

Only after the player has triggered or experienced a principle should the guide explain that principle.

Examples:

- Sweet / 甘:
  ```text
  你曾见过甘味偏缓，常与补益、恢复相近。
  ```
- Bitter / 苦:
  ```text
  苦味常有清解之意，也可能削去积蓄的药性。
  ```
- Pungent / 辛:
  ```text
  辛味多行散，能推送药力，也可能扰胃。
  ```

Do not list all flavors up front.

## Issue 3: 七情与配伍 should reveal specific relations only after triggering them

Current guide says generally that herbs may support or clash. That is acceptable as a vague starting point.

But specific relation meanings should be revealed only after actual experience.

Examples:

- After `XIANG_XU`:
  ```text
  你曾见过“相须”：两味相近，正向药力更盛。
  ```
- After `XIANG_SHI`:
  ```text
  你曾见过“相使”：一味引路，另一味药性行得更远。
  ```
- After `XIANG_SHA`:
  ```text
  你曾见过“相杀”：清解之性可削毒。
  ```
- After `XIANG_WEI`:
  ```text
  你曾见过“相畏”：毒性遇制，反而收敛。
  ```
- After `XIANG_E`:
  ```text
  你曾见过“相恶”：药性相忤，整体力道转弱。
  ```
- After `XIANG_FAN`:
  ```text
  你曾见过“相反”：寒热毒性相激，药效会反噬。
  ```

## Recommended implementation for progressive principles

### Minimal `1.1.2` fix

- Remove exact item/nature display from the flavor guide.
- Keep staged text based on existing tasted count / discovered flavor count.
- Keep seven-emotion text vague unless existing state can prove experience.

This avoids over-revealing without adding new save data.

### Better `1.1.2` or `1.1.3` fix

Add persistent principle discovery tracking.

Potential storage:

- Add a small `Set<String>` to `PlayerPharmacologyState`, or a new knowledge/principle store on `HerbcraftPlayerData`.

Potential discovered keys:

```text
flavor_sweet
flavor_bitter
flavor_pungent
flavor_sour
flavor_astringent
flavor_salty
flavor_bland
temperature_harmonize
temperature_same_hot
temperature_same_cold
seven_xiang_xu
seven_xiang_shi
seven_xiang_sha
seven_xiang_wei
seven_xiang_e
seven_xiang_fan
seven_single
```

Record them in `PharmacologyResolver.resolve(...)` only when the relevant principle actually affects the outcome or is messaged.

Then Codex guide pages can reveal exact principle explanations only when the corresponding key is present.

This creates the desired learning curve:

```text
vague concept -> personal taste -> repeated comparison -> actual principle trigger -> explicit explanation
```

## Recommended next task

Before one-time hint system work, fix Phase 2.1:

1. Remove unconditional Core registration of Cuisine/Alchemy guide entries.
2. Register module-specific guide entries from their own modules, or implement a data-backed guide-entry loader with optional module conditions.
3. Change `五味与性味` guide text so it does not print exact item/nature in the system guide.
4. Decide whether principle-discovery tracking is in-scope for `1.1.2` or deferred.
5. Update validators to catch module leakage and guide over-specificity.
6. Client-test Core-only, Core+Cuisine, Core+Alchemy, and all-three module combinations.

## Design principle reaffirmed

The Codex may teach systems, but it should not give the feeling of a wiki spoiling content.

For guide pages:

> Tell the player what kind of thing they are learning.  
> Let exact knowledge appear only after the player has earned it.  
> Keep addon-specific guidance inside the addon that owns the mechanic.
