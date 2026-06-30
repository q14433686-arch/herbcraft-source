# ALCHEMY_CORE_LINKAGE_P1_P5_PLAN

Status: authoritative implementation plan for the next Alchemy/Core linkage line after the distillation slot fix.  
Last updated: 2026-06-30

This file exists so future turns do not rely on chat memory or ad-hoc summaries when continuing the distillation / essence / Core pharmacology linkage work.

## Mandatory use rule

At the start of every future turn that continues this work:

1. read `AGENT_BRIEF.md`
2. regenerate `docs/reports/CURRENT_AGENT_BRIEF.md`
3. read `CURRENT_BASELINE.md`
4. read `NEXT_TASK.md`
5. read this file: `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md`
6. if the current turn changes scope, progress, order, or implementation details of P1-P5, update this file before completion

Do not continue the Alchemy/Core linkage line from stale chat memory alone.

## Why this plan exists

The current Alchemy distillation/processing line is mechanically functional, but still too internally local to Alchemy. It does not yet adequately project Herbcraft Core pharmacology surfaces such as:

- temperature / 寒热
- flavors / 性味
- traits / 特质
- medicinal accumulation / 药性累计
- qi/body-state tendency
- seven-relation / 七情配伍 interactions

The current catalyst layer is also mechanically readable but still too close to vanilla modifier semantics unless it is reinterpreted and extended as Herbcraft-specific processing logic.

## Architecture rule for this plan

- Java owns process flow, runtime state, sync, persistence, tooltips/UI projection, events, and validation.
- JSON owns content tables, processing semantics, affinity tables, pharmacology projection tables, catalyst semantics, burden profiles, and compatibility/balance values where loaders exist.
- Do not hardcode third-party or large content/rules tables into Java when JSON loaders can express them.

## Frozen P1-P5 definitions

### P1 — Processing Style pharmacology semantics layer

Goal:

- upgrade `purified`, `sustained`, `intense`, and `harmonized` from mainly numeric style labels into explicit pharmacological processing semantics

Required outcomes:

- extend processing-style JSON with semantic fields such as:
  - `thermal_shift`
  - `flavor_bias`
  - `trait_affinity`
  - `burden_profile`
  - `relation_modifier`
  - `qi_bias`
  - `clarity_level`
- make tooltip and guide-facing projection explain the pharmacological meaning of the style, not only stronger/longer/stable behavior
- keep Java focused on reading/querying/projecting these semantics, not hardcoding large style behavior tables

Boundary:

- P1 establishes vocabulary and rule structure first
- conservative gameplay projection is allowed, but full body/relation integration belongs to later P stages

### P2 — Source essence -> Core pharmacology projection layer

Goal:

- ensure processed outputs still preserve the identity of the source essence rather than collapsing into mostly style-only behavior

Required outcomes:

- project source essence into Core pharmacology surfaces through runtime-readable metadata/query paths
- support, at minimum:
  - source temperature
  - source flavors / taste flavors
  - source traits
- expose or persist a source-pharmacology projection layer usable by tooltips/runtime logic/future validators
- allow different source essences under the same style to remain meaningfully different

Boundary:

- do not require full new gameplay complexity for every source in P2
- P2 is about preserving source identity and making it queryable/usable

### P3 — Processed potion body-state / medicinal accumulation integration layer

Goal:

- make processed potion consumption enter Herbcraft’s body/pharmacology system, not remain only a localized Alchemy buff route

Required outcomes:

- processed potion consumption should project into one or more Core-side body-state surfaces, conservatively at first:
  - medicinal accumulation / 药性累计
  - thermal tendency
  - qi/body-state tendency
  - burden profile type rather than only scalar burden weight
- burden should begin moving toward typed profiles such as examples:
  - warming
  - chilling
  - scattering
  - stagnating
  - muddy
  - overdriven
- processed potion use should feel like taking medicine, not only acquiring potion effects

Boundary:

- first pass should remain conservative and source-verifiable
- do not immediately over-harden the main mod into a punitive/hardcore system

### P4 — Seven-relation integration layer

Goal:

- make processed potion use participate in Herbcraft’s recent-intake relation system rather than sitting outside it

Required outcomes:

- processed potion events should enter the recent pharmacology/relation context
- first implementation can be conservative but must be visible/meaningful
- style/source/temperature/flavor/trait should be able to influence relation outcomes or relation risk modifiers
- `purified` / `harmonized` may buffer conflict risk
- `intense` may amplify conflict risk

Boundary:

- P4 first pass does not need to model every possible advanced relation nuance
- it must, however, make processed potions feel like true participants in 七情 rather than isolated potion items

### P5 — Processing material / catalyst semantics upgrade layer

Goal:

- solve the current creativity/identity gap where catalyst handling is understandable but still too close to vanilla modifier semantics

Required outcomes:

#### P5-A

- reinterpret current catalysts in Herbcraft pharmacology/process language:
  - honeycomb = 澄滤 / 驯浊 / reducing turbidity
  - redstone = 引尾 / 续势 / extending tail and momentum
  - glowstone = 聚锋 / 峻化 / sharpening and intensifying
  - amethyst = 调承 / 调和 / stabilizing or harmonizing future co-processing
- project this wording into docs/tooltips/guides/JSON semantics

#### P5-B

- prepare or implement a second-layer Herbcraft-specific processing-material / 药引 or 炮制辅料 path
- examples for future or first-batch extension may include categories such as:
  - honeyed processing aids
  - wine-processed aids
  - salt-processed aids
  - ginger/pungent warming aids
  - ash/carbon drying or settling aids
  - floral/dew purification aids

Boundary:

- two-round compressed execution may only reach P5-A + P5-B structure/prep
- four-round execution may include first playable P5-B content if scope remains controlled

## Execution shapes

### Preferred four-round implementation track

Round 1:

- P1 processing semantics layer

Round 2:

- P2 source-pharmacology projection layer

Round 3:

- P3 body-state / medicinal accumulation integration layer

Round 4:

- P4 seven-relation integration layer
- P5 catalyst/material semantics upgrade layer

### Allowed two-round compressed mapping

Round A:

- P1
- P2
- P5-A

Round B:

- P3
- P4
- P5-B implementation or structure/prep depending on scope

## Per-stage validator expectation

Every completed P stage should add or extend targeted validation.

Expected validator direction:

- P1: processing semantic schema / projection validator
- P2: source-pharmacology projection validator
- P3: processed potion pharmacology/body-state integration validator
- P4: processed potion relation integration validator
- P5: catalyst/material semantic consistency validator

## Current status at plan creation

Current completed precondition state before P1-P5 begins:

- distillation bottom-slot / ingredient-slot brewing interaction bug is fixed in source
- release jars and source zip refreshed after the distillation slot fix
- distillation foundation, distillation routing, burden/runtime, armor support, and processed weapon coating foundations exist
- broader Alchemy/Core pharmacology linkage work has not yet started

## Next-action rule

Until superseded by direct user instruction, the next turn in this line should:

1. re-read this file
2. choose the active round shape (two-round compressed or four-round preferred)
3. implement the next incomplete P stage in order
4. update this file with progress and any scope clarifications
5. update `NEXT_TASK.md`, `CURRENT_BASELINE.md`, `CHANGELOG.md`, and a turn report

## Progress log

### 2026-06-30

- Plan created and frozen as the authoritative handoff file for the Alchemy/Core linkage line.
- Began the preferred four-round track and completed Round 1 / P1.
- P1 status: complete in the current source line. The semantic schema already existed in `data/herbcraft_alchemy/potion_processing_rules.json` and `PotionProcessingRules` (`thermal_shift`, `flavor_bias`, `trait_affinity`, `burden_profile`, `relation_modifier`, `qi_bias`, `clarity_level`, plus hint/lang-key projection). This turn closed the remaining guide-facing gap by extending `AlchemyChapter` so the Alchemy Codex now explicitly teaches that processing styles carry pharmacological meaning beyond pure strong/long/stable behavior. Added new zh/en guide copy keys for processing semantics and extended `scripts/validate_potion_processing_rules.py` to guard the guide projection.
- Distillation slot-fix work remains the prerequisite state before this P1 start; it is already complete.
- Completed Round 2 / P2. Added persisted/network-synced processed-potion source pharmacology projection metadata through `PotionPharmacologyProjection`, `AlchemyPotionComponents.POTION_PHARMACOLOGY`, and `PotionPharmacologyProjectionRuntime`. The projection now preserves and exposes source essence identity (`source_essence`, `temperature`, `flavors`, `taste_flavors`, `traits`) on processed potion stacks, is queryable through `AlchemyProcessingAPI.potionPharmacology(...)`, and is projected in `AdvancedPotionTooltips`. Added validator `scripts/validate_potion_source_projection.py` and jar verification coverage.
- Completed Round 3 / P3. Added persisted/network-synced `PotionBodyStateProjection` metadata (`medicinal_profile`, `medicinal_weight`, `thermal_tendency`, `thermal_value`, `qi_tendency`, `qi_value`, `body_traits`) plus `PotionBodyStateProjectionRuntime` and `PotionBodyStateConsumptionRuntime`. Processed potion initialization now derives conservative Core-facing body-state projection from source pharmacology + processing style, tooltips project medicinal accumulation / body thermal tendency / qi tendency, and processed potion consumption now enters Core `PlayerPharmacologyState` recent-use + qi state through the existing Alchemy consume path. Added read-only `AlchemyProcessingAPI.potionBodyState(...)`, validator `scripts/validate_potion_body_state_projection.py`, and jar verification coverage.
- Completed Round 4 / P4 + P5. Added `PotionRelationRuntime` so processed potion consumption now conservatively participates in Core recent-intake / seven-relation context using source pharmacology + body-state + style semantics, with visible overlay/discovery/qi-state outcomes through existing `SevenEmotionType` paths. Also completed the catalyst semantics wording upgrade: distillation orientation hints and Alchemy guide copy now explicitly project honeycomb as 澄滤/驯浊, redstone as 引尾/续势, glowstone as 聚锋/峻化, and amethyst as 调承/调和. Added validator `scripts/validate_potion_relation_and_catalyst_semantics.py` and jar verification coverage.
- Preferred four-round implementation track status: complete through P1-P5 in the current source line.
