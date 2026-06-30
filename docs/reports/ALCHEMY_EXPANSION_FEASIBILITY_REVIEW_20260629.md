# Alchemy Expansion Feasibility Review — 2026-06-29

## Scope

Reviewed the attached planning document `ALCHEMY_EXPANSION_MASTER_STATUS.md` against the current `1.1.3` source tree.

Goal: understand the design, locate related code, verify what is actually implemented, and outline a feasible implementation order / step logic.

Priority rule applied:

```text
User-provided current request > current source/resources > current docs > older memory/chat summaries
```

Because the attached document contains implementation-status claims, those claims were checked against current source. Where the document conflicts with source, source is treated as the implementation truth.

## Executive finding

The design direction is technically feasible, but the attached document is ahead of the current repository in the potion-processing section.

Current source supports a strong Alchemy baseline:

- JSON-backed essence definitions;
- generated base / `_long` / `_strong` / `_clarified` / `_murky` potion paths;
- advanced-potion quality and bonus components;
- tooltip projection for current advanced potion type and quality;
- JSON-backed coating rules and working weapon coating runtime;
- JEI/REI projection for weapon coating.

However, the current source does **not** yet contain the new potion-processing foundation described in the attached document:

- no `data/herbcraft_alchemy/potion_processing_rules.json`;
- no `PotionProcessingRules` loader/query layer;
- no `processingStyleId(...)` API;
- no `PotionProcessingRuntime`;
- no `PotionBurdenComponent` / `PotionBurdenRuntime` / `PlayerPotionBurdenRuntime`;
- no player-level potion burden persistent state;
- no `murky_edge` or `harmonized` reserved style rows in source/resources.

Therefore, the implementation status for potion processing in the current repository should be treated as:

```text
Mechanism-layer status: RESERVED / NOT YET IMPLEMENTED beyond current advanced-potion groundwork
Content-layer status: PLANNED / FIRST-BATCH DESIGN, not source-implemented
```

This is not a blocker; it only means the first coding pass must begin with the missing foundation rather than assuming it already exists.

## Source verification commands

The following source checks were run:

```bash
grep -R "potion_processing_rules\|PotionProcessingRules\|processingStyleId\|PotionProcessingRuntime\|PotionBurden\|PlayerPotionBurden\|murky_edge\|harmonized" -n alchemy/src core/src
find alchemy/src/main/java/com/herbcraft/alchemy/potion -type f
find alchemy/src/main/resources/data/herbcraft_alchemy -type f
```

Result: all new potion-processing/burden tokens from the attached document are absent in the current source. Existing Alchemy potion files are limited to:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionInventoryHooks.java
alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java
alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java
alchemy/src/main/java/com/herbcraft/alchemy/potion/HiddenCuisineCatalystBrewing.java
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBonusComponent.java
```

Current Alchemy data files under `data/herbcraft_alchemy/` include `essences.json`, `potion_bonus_pools.json`, `special_potions.json`, `coating_rules.json`, `witch_loot.json`, guide/hint/loot data, but no `potion_processing_rules.json`.

## Related current code map

### Current Alchemy initialization

File: `alchemy/src/main/java/com/herbcraft/alchemy/HerbcraftAlchemy.java`

Relevant current responsibilities:

- initializes coating components/items/recipes/rules;
- initializes current potion data components;
- loads `potion_bonus_pools.json` through `AdvancedPotionStackInitializer.loadBonusPoolsFromBundledJson()`;
- initializes registries, Codex hooks, acquisition hooks, and inventory hooks.

There is currently no call to load potion-processing rules or burden rules.

### Current advanced potion initialization

File: `alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java`

Implemented now:

- loads `data/herbcraft_alchemy/potion_bonus_pools.json`;
- identifies advanced potion paths by suffix/special IDs;
- assigns `POTION_SOURCE`;
- rolls `POTION_QUALITY`;
- may apply a random `POTION_BONUS`;
- applies quality as duration scaling, with cap handling;
- maps `highTierTypeKey(...)` to current tooltip categories: `clarified`, `advanced`, `murky`, `t3`.

Not implemented yet:

- no external style registry;
- no style alias mapping (`_long` -> `sustained`, `_strong` -> `intense`, etc.);
- no style-specific runtime rewrite beyond existing quality/bonus flow;
- no burden metadata assignment.

### Current potion data components

File: `alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java`

Implemented components:

- `herbcraft_alchemy:potion_quality`;
- `herbcraft_alchemy:potion_bonus`;
- `herbcraft_alchemy:potion_source`;
- `herbcraft_alchemy:advanced_potion_initialized`.

Missing components needed for the attached plan:

- style id component is not present;
- burden metadata component is not present;
- any component holding burden profile / burden weight / tail burden is not present.

### Current potion tooltip projection

File: `alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java`

Implemented now:

- shows current type based on `AdvancedPotionStackInitializer.highTierTypeKey(...)`;
- shows quality and duration multiplier;
- shows bonus effect line.

Missing relative to attached plan:

- no style metadata lookup;
- no localized processing-style names from JSON;
- no burden-profile hint or burden weight/tail weight projection.

### Current acquisition / advancement routing

File: `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java`

Implemented now:

- grants first potion / first clarified / first murky / toxin / witch / epic potion advancements;
- uses suffix and special-ID checks directly.

Missing relative to attached plan:

- no style-id-based acquisition routing;
- no distinction for reserved/deferred styles such as `murky_edge` or `harmonized`.

### Current potion path generation

File: `alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java`

Implemented now:

- data-backed essence specs from `essences.json`;
- base potion registration;
- `_clarified` from positive effects via honeycomb;
- `_murky` from negative effects via fermented spider eye;
- `_long` via redstone;
- `_strong` via glowstone;
- special potions and witch throw pool from JSON.

This is the best foundation for style mapping because the potion path family already exists and can be projected into style IDs without changing registry IDs.

### Current per-player persistent/runtime surfaces

Files:

- `core/src/main/java/com/herbcraft/HerbcraftPlayerData.java`
- `core/src/main/java/com/herbcraft/mixin/PlayerMixin.java`
- `core/src/main/java/com/herbcraft/PlayerStateSync.java`
- `core/src/main/java/com/herbcraft/bootstrap/HerbcraftLifecycleBootstrap.java`

Existing useful surfaces:

- player mixin already stores custom Herbcraft knowledge, flags, counters, pharmacology state, guide hints, and nutrition state;
- save/load hooks already exist;
- death reset and respawn-copy policy already exists;
- server tick loop already exists for nutrition.

Feasibility implication: player-level potion burden is implementable, but adding it requires careful ownership decisions. Since the current burden system would live in Alchemy, either Alchemy needs its own mixin/persistence hooks or Core must expose a generic extension state/event surface. Avoid adding Alchemy-specific fields to Core unless deliberately choosing a cross-module core-owned burden API.

## Per-system feasibility verdict

### 1. Distillation / re-distillation

Current source status: not implemented.

Feasibility: high, but should not be first if the immediate goal is validating the attached potion-processing claims.

Best implementation path:

1. define `distillation_rules.json` / `distillation_processes.json` only after deciding whether outputs are new items, components on existing essences, or processing-state metadata;
2. avoid registering many new items until a generator/scaffold and validator exist;
3. keep base essences useful by making processed outputs specialized rather than universally superior;
4. later route distillation orientation into potion/weapon/armor branches.

Risk: if implemented as a large new item tier first, asset/lang/recipe surface grows quickly and may violate the “content conservative, mechanism broad” rule.

### 2. Armor-side protective alchemy

Current source status: not implemented.

Feasibility: medium-high, but broader than potion processing because it needs equipment-slot scanning, runtime triggers, possible components on armor stacks, tooltip/UI projection, and possibly event hooks.

Best implementation path:

1. first define mechanism vocabulary and JSON schema;
2. implement armor coating/protection component and simple passive tick evaluation;
3. add tooltip projection;
4. add recipe/viewer projection;
5. only then expand environment/status/slot-synergy domains.

Risk: if started now, it can sprawl into a second equipment system before potion-processing foundation is settled.

### 3. Potion processing / strengthening

Current source status: current advanced-potion groundwork exists, but the attached new style/burden implementation is not present.

Feasibility: high and likely the safest first implementation candidate, because it can be layered over the existing `_long` / `_strong` / `_clarified` / `_murky` path family without changing stable potion IDs.

Important correction to attached document:

- Treat `sustained`, `intense`, and `purified` as confirmed design styles, not as already implemented runtime styles.
- Treat `murky_edge` and `harmonized` as deferred/reserved design styles, not as current source rows.

### 4. Weapon coating follow-up

Current source status: implemented baseline, future distillation-aware branch not implemented.

Feasibility: high after distillation/style metadata exists.

Best implementation path: do not extend coating first. Current coating is stable and recently touched for JEI/REI visibility. Keep it as baseline until potion/distillation metadata exists, then feed advanced variants from data-backed style/processed-essence rules.

## Recommended immediate implementation order

### Phase 0 — documentation/status reconciliation

Before coding, decide whether `ALCHEMY_EXPANSION_MASTER_STATUS.md` is meant to describe:

1. a future desired state, or
2. already-implemented current source.

Current source proves option 2 is false for potion processing. If this file is imported into the repo, section 7 must be corrected to avoid future agents assuming nonexistent classes/components exist.

### Phase 1 — potion processing style foundation, no gameplay behavior change

Goal: add the source-of-truth style/rules foundation while preserving current potion output behavior.

Proposed files:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/potion_processing_rules.json
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRules.java
scripts/validate_potion_processing_rules.py
```

Proposed JSON shape, first pass:

```json
{
  "schema_version": 1,
  "styles": [
    {
      "id": "sustained",
      "implemented": false,
      "first_batch": true,
      "aliases": ["long"],
      "path_suffixes": ["_long"],
      "burden_profile": "slow_tail",
      "lang_key": "tooltip.herbcraft.potion.processing.sustained"
    },
    {
      "id": "intense",
      "implemented": false,
      "first_batch": true,
      "aliases": ["strong"],
      "path_suffixes": ["_strong"],
      "burden_profile": "sharp_peak",
      "lang_key": "tooltip.herbcraft.potion.processing.intense"
    },
    {
      "id": "purified",
      "implemented": false,
      "first_batch": true,
      "aliases": ["clarified"],
      "path_suffixes": ["_clarified"],
      "burden_profile": "clean_stable",
      "lang_key": "tooltip.herbcraft.potion.processing.purified"
    },
    {
      "id": "murky_edge",
      "implemented": false,
      "first_batch": false,
      "path_suffixes": ["_murky"],
      "deferred": true
    },
    {
      "id": "harmonized",
      "implemented": false,
      "first_batch": false,
      "path_suffixes": [],
      "deferred": true
    }
  ]
}
```

Notes:

- `implemented` should initially be `false` if the first phase is purely foundation.
- Do not claim runtime differentiation until a later phase actually rewrites effects.
- Validator must require first-batch and deferred rows to remain visible.

### Phase 2 — source projection into existing potion path family

Goal: expose style IDs without changing current potion mechanics.

Suggested changes:

- Add `PotionProcessingRules.processingStyleId(String potionPath)`.
- Add `AdvancedPotionStackInitializer.processingStyleId(ItemStack/PotionContents/String)` as a read facade.
- Replace raw suffix logic in tooltip/acquisition with style queries where safe.
- Keep fallback behavior for old suffixes.

Validation:

- style JSON loads;
- `_long -> sustained`, `_strong -> intense`, `_clarified -> purified`, `_murky -> murky_edge`;
- no potion registry IDs are renamed;
- existing advancement behavior remains equivalent.

### Phase 3 — tooltip and documentation projection

Goal: make player-facing style/burden language visible without changing effects yet.

Suggested changes:

- Add lang keys for style names and burden-profile hints.
- Update `AdvancedPotionTooltips` to display style metadata.
- Keep current `type` / `quality` lines stable or migrate carefully to avoid localization breakage.

Validation:

- `check_lang_diff.py`;
- a new validator for style tooltip keys;
- built jar contains JSON and class.

### Phase 4 — first runtime effect differentiation

Goal: implement actual `sustained`, `intense`, `purified` behavior.

Suggested class:

```text
alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionProcessingRuntime.java
```

Suggested semantics:

- `sustained`: longer duration, lower peak where possible;
- `intense`: higher peak or stronger effect, shorter duration;
- `purified`: stable/clean projection; avoid treating it as weak.

Implementation should run before/around current quality scaling in `AdvancedPotionStackInitializer.initializeIfNeeded(...)`.

Critical caution:

- Current `_long` and `_strong` already have registry-time duration/amplifier transformations in `AlchemyRegistries`. Runtime style rewriting must avoid double-scaling into absurd values.
- Use explicit caps and validators.

### Phase 5 — burden metadata on potion stack

Goal: attach item-level burden metadata only.

Suggested additions:

```text
PotionBurdenComponent
AlchemyPotionComponents.POTION_BURDEN
```

Do not add player penalties yet. This phase only proves metadata persistence/sync/tooltip.

### Phase 6 — player-level burden runtime

Goal: add consumption-time player burden and decay.

Main design choice:

- If burden is Alchemy-only, implement Alchemy-owned mixin/interface and avoid Core module coupling.
- If burden should interact broadly with Core pharmacology later, define a Core-owned generic burden/accumulation API first.

Suggested minimum:

- alchemy mixin into potion consumption path;
- per-player burden state save/load;
- death reset policy;
- respawn copy policy if intended;
- server tick decay registration from Alchemy initializer.

### Phase 7 — consequences and cross-system integration

Only after burden state is stable:

- threshold stages (`steady`, `warming`, `strain`, `overload`);
- movement penalty / backlash damage;
- non-instant effect application pushes burden;
- possible Core pharmacology hooks.

### Phase 8 — distillation / armor / coating integration

After style and burden foundation is stable:

- distillation can feed style eligibility;
- armor can buffer/alter burden or environment response;
- weapon coating can receive advanced processed-essence branches.

## Required validator plan

New validators should be added before or with implementation:

```text
scripts/validate_potion_processing_rules.py
scripts/validate_potion_processing_runtime.py
scripts/validate_potion_burden_runtime.py
```

Minimum checks:

- `potion_processing_rules.json` exists and has `schema_version: 1`;
- first-batch styles exist: `sustained`, `intense`, `purified`;
- deferred styles exist: `murky_edge`, `harmonized`;
- suffix mapping is data-backed, not Java hardcoded only;
- current potion IDs are preserved;
- tooltip lang keys exist in zh/en;
- burden component is persistent and network-synced if implemented;
- player burden reset/copy policy is explicit;
- jar verification includes new JSON/classes.

## Feasibility conclusion

Recommended first coding target: **Potion processing foundation Phase 1 + Phase 2**.

Reason:

- It has the strongest existing source foundation.
- It can be implemented without changing stable registry IDs.
- It directly reconciles the attached document with actual source state.
- It creates the metadata surface needed later by burden, distillation, armor, and advanced coating.

Do **not** start with armor or distillation unless the planner explicitly wants a larger, riskier foundation pass. Those systems are feasible but have wider unknowns and should benefit from potion-processing style metadata first.
