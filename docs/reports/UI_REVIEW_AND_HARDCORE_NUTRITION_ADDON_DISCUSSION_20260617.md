# UI Review and Hardcore Nutrition Addon Discussion

Date: 2026-06-17  
Scope: 1.1.3 UI audit + "硬核营养学" addon direction design review  
Status: discussion/analysis — no code changes in this turn

---

## Part 1: Current UI Review — Known Issues and Improvement Candidates

### 1.1 Codex Screen (`CodexScreen.java`)

**Current state:**

- Left-right split layout with scroll, scissor guards, and text trimming.
- Left panel: tree view (chapters → sections → entries) with indent and expand/collapse.
- Right panel: entry detail with wrapped text, claim hitboxes, scroll.
- Responsive sizing: `320..520` width × `210..330` height, clamped to window.
- `CodexTextLayout.trimWithEllipsis()` protects left-panel overflow; tooltip appears on hover for trimmed labels.
- Scissor enabled on both panels.

**Known issues and improvement candidates:**

| ID | Priority | Issue | Notes |
|---|---:|---|---|
| UI-C1 | P2 | Left panel cannot be resized by the user | Left panel width is hardcoded to 34% ± clamp. On narrow windows or when chapter/section names are long (especially Chinese 4-char chapter titles + section counts), trimming can be aggressive. Not a blocker, but a future UX enhancement candidate. |
| UI-C2 | P2 | No search / filter | With 96+ herbs + 39 ingredients + guide pages, finding a specific entry requires manual scrolling. A future search bar or type-to-filter would improve usability, especially for players with many tasted herbs. |
| UI-C3 | P3 | No keyboard navigation | Scrolling and node expansion are mouse-only. Arrow keys / Enter could be nice-to-have. |
| UI-C4 | P3 | Static expand state across opens | `expandedNodes` is static; closing and reopening the Codex preserves expand state. This is intentional, but if many chapters are expanded, re-entering the Codex can dump the player into a long list. |
| UI-C5 | P1 | Claim mark hitbox size is small (16×12) | `ClaimHitbox` is `rx, ry, rx+16, ry+12`. On high-DPI screens or small font scaling, this can be hard to click. Consider widening the clickable area to the full text line width. |
| UI-C6 | P2 | Right panel has no visual "empty state" illustration | When no entry is selected, only a gray hint text appears. A small decorative element (an herb silhouette or open book icon) would make the empty state feel less bare. This is purely cosmetic. |
| UI-C7 | P2 | No breadcrumb or "back" button | After selecting an entry deep in a chapter, there's no visual indicator of which chapter/section it belongs to (other than the left panel highlight). A small header breadcrumb like `百草志 > 花卉 > 蒲公英` would help orientation. |

**No P0 blockers found.** The Codex UI is functional and guarded against overflow. The main improvement candidates are quality-of-life (search, keyboard nav, resizable panels).

### 1.2 Nutrition Panel (`NutritionPanelScreen.java`)

**Current state:**

- Fixed `240×170` panel centered on screen.
- Five dimension rows with item icon + label + colored bar + percentage.
- Status line at bottom (correcting / well-nourished / etc.) with wrapped text.
- Scissor guard on entire panel.
- Title, labels, hints, and status text all use `CodexTextLayout.trimWithEllipsis()` or `drawCenteredWrappedLimited()`.
- Key hint at bottom.
- Toggle with N key.

**Known issues and improvement candidates:**

| ID | Priority | Issue | Notes |
|---|---:|---|---|
| UI-N1 | P2 | Fixed panel size may feel small on large monitors | `240×170` is compact. On 1440p or 4K displays, the panel might feel tiny compared to the screen. A future pass could make the panel scale with GUI scale or allow a larger variant. |
| UI-N2 | P3 | No bar segment markers | The bar does not visually indicate thresholds like `balancedMin` or `imbalanceHigh`. Adding thin tick marks at the correction threshold (750) and the imbalance threshold would help players understand target ranges without reading docs. |
| UI-N3 | P2 | Status line is 2-line max | `drawCenteredWrappedLimited(..., 2, ...)` clips status text at 2 lines. Some translated status descriptions might be longer. Currently safe in Chinese and English, but could clip in verbose languages. |
| UI-N4 | P3 | No recent-meal or correction-progress indicator | The panel shows current bar state and status text, but does not show how quickly correction is progressing or what recent meals contributed. This is intentional simplicity, but a future "correction arrow" (↑↓) next to changing dimensions could help. |
| UI-N5 | P2 | Panel does not show actual dimension values | Only percentages are shown. Debug users may want to see raw values. This is intentional for non-hardcore players, but a future toggle (e.g., Shift+N) or tooltip-on-hover for the bar could show the actual integer value. |
| UI-N6 | **P1** | **Hardcore Nutrition addon cannot cleanly extend this panel to 6+ dimensions** | The panel layout assumes exactly 5 dimensions (`NutritionProfile.DIMENSIONS`). Adding a sixth dimension (e.g., `cocoa_fat`) would overflow the panel height or require row height reduction. This is the main UI blocker for the Hardcore Nutrition addon. See Part 2 below. |

### 1.3 Item Tooltips (`HerbNatureItemTooltips.java`, `NutritionItemTooltips.java`)

**Current state:**

- Nature tooltips: knowledge-gated, progressive reveal (unknown → encountered → heard → tasted → mastered).
- Nutrition tooltips: fuzzy for non-mastered herbs and ingredients; precise for mastered herbs.
- Food-nature bridge items show nature tooltips with Codex hint.
- Both check `Minecraft.getInstance().screen == null` to avoid double-rendering in some contexts.

**Known issues:**

| ID | Priority | Issue | Notes |
|---|---:|---|---|
| UI-T1 | P3 | Tooltip can get long for mastered herbs with many flavors + usages + nutrition | Not a bug, but on items that are both herbs and ingredients with nutrition profiles, the tooltip stack can be 8+ lines. A future pass could group/compact these lines. |
| UI-T2 | P2 | `screen == null` guard may suppress tooltips in some mod-added containers | Some mods render tooltips outside standard Screen classes. The current guard is conservative and safe for vanilla/Fabric, but could miss edge cases with custom inventory renders. |

### 1.4 Jade (`HerbJadeData.java`, `HerbNatureProvider.java`, `HerbNatureEntityProvider.java`)

**Current state:**

- Server writes nature/knowledge data to Jade NBT compound; client reads and appends tooltip.
- Knowledge-gated, matching the tooltip flow.
- Food-nature bridge items are recognized.
- Missing-item guard prevents Air entries.

**Known issues:**

| ID | Priority | Issue | Notes |
|---|---:|---|---|
| UI-J1 | P3 | Entity provider (animal eating) may not show nature data for all contexts | `HerbNatureEntityProvider` checks if the entity is holding/eating an herb item. Some mob-interaction contexts (item frames, etc.) may not trigger. Not a gameplay issue. |

### 1.5 Summary: UI is stable, no P0 blockers

The current `1.1.3` UI is well-guarded:

- Codex uses scissor + trim + scroll on both panels.
- Nutrition panel uses scissor + trim + centered wrap.
- Tooltips and Jade are knowledge-gated and safe.
- Text overflow has been addressed across English and Chinese.

**The most significant UI design issue going forward is extensibility**: the nutrition panel and NutritionProfile.DIMENSIONS are hardcoded to exactly 5 dimensions. This is the core constraint for the Hardcore Nutrition addon discussion below.

---

## Part 2: "硬核营养学" (Hardcore Nutrition) Addon — Direction Discussion

### 2.1 Positioning and Identity

Based on the existing `docs/HARDCORE_NUTRITION_ADDON_PLAN.md`, `docs/HERBCRAFT_FUTURE_IDEAS_AND_1_1_2_ROADMAP.md`, and the `NEXT_TASK.md` explicit rule "Do not make main-mod nutrition more hardcore; reserve stricter mechanics for a future addon," the recommended direction is:

```
百草可食：硬核营养学 / Herbcraft: Hardcore Nutrition
Mod ID: herbcraft_hardcore_nutrition
Dependency: herbcraft >= 1.1.3
Optional dependency: herbcraft_cuisine (for cuisine profile rebalancing)
```

**Core principle:** The base mod stays gentle; the addon is opt-in harsh survival nutrition.

### 2.2 Feature Direction Options

Based on existing planning docs and the current architecture, there are three tiers of addon ambition:

#### Tier A: Data-Only Addon (No Core Changes Required)

What it can do today with `1.1.3` extension paths:

| Feature | How | JSON Path |
|---|---|---|
| Stricter nutrition rules | Override `nutrition_rules.json` | Bundled in addon mod resources |
| Harsher nutrition effects | Override `nutrition_effects.json` | Bundled in addon mod resources |
| Different 0.6x scaling → 0.3x or 1.0x | Override `nutrition_profiles.json` | Bundled in addon mod resources |
| Faster correction decay | Override `nutrition_correction_rules.json` | Bundled in addon mod resources |
| More aggressive excess penalties | Tune thresholds in nutrition rules | Bundled in addon mod resources |
| New special counters for food categories | Add counters | `data/<ns>/herbcraft/special_counters/*.json` |
| Custom food-nature data for more items | Add nature profiles | `data/<ns>/herbcraft/herb_natures/*.json` |

**Pro:** Ships as a pure data addon; no Java changes needed; compatible with existing Herbcraft Core.
**Con:** Cannot add new nutrition dimensions; cannot change the panel layout; cannot add fundamentally new mechanics like vitamins/minerals/fiber.

#### Tier B: Addon with Modest Core Preparation (Recommended Next Step)

Requires a small Core preparation pass before the addon can ship cleanly:

**Core changes needed:**

1. **Dynamic nutrition dimension registry** — `NutritionProfile.DIMENSIONS` becomes data-backed rather than a hardcoded 5-element array.
2. **Panel layout for N dimensions** — `NutritionPanelScreen` iterates the dynamic dimension list and adjusts row height / panel height when dimensions > 5.
3. **Serialization / sync for dynamic dimensions** — `NutritionSyncPayload`, `PlayerNutritionState`, save/load, and `NutritionProfile` must support an arbitrary number of dimensions.
4. **Tooltip / Codex dimension iteration** — `NutritionItemTooltips`, `IngredientsChapter`, and `NutritionRegistry` must iterate dynamic dimensions.
5. **Correction matrix extensibility** — `nutrition_correction_rules.json` must support N×N matrix, not hardcoded 5×5.

**Addon content:**

| Feature | Description |
|---|---|
| 6th dimension: `cocoa_fat` / 可可脂 | Tracked separately; deficiency increases exhaustion; excess causes sluggishness. |
| Harsher decay/check tuning | Faster decay, lower correction thresholds, longer recovery. |
| New debuff: `herbcraft:severe_excess` | A new effect for extreme excess in any dimension, causing Slowness + Mining Fatigue. |
| Harder dietary imbalance | Tighter spread, stronger penalties, shorter grace period. |
| Optional 7th dimension: `fiber` / 膳食纤维 | Tracks plant fiber intake; deficiency causes periodic weakness. |
| Homeostatic temperature bridge (optional) | If Homeostatic temperature API becomes available, hot/cold foods affect body temperature. |

**Pro:** Clean addon architecture; new dimensions participate in all existing UI/Codex/tooltip flows; the base mod stays gentle.
**Con:** Requires a non-trivial Core preparation pass (dynamic dimensions are a significant refactor).

#### Tier C: Full Survival Nutrition Overhaul (Long-term, Not Recommended Now)

A much larger scope that would also add:

- Vitamin/mineral sub-dimensions
- Cooking nutritional value changes (raw vs. cooked vs. processed)
- Seasonal food availability
- Temperature/weather nutritional cost
- Metabolic rate system

**Verdict:** Too large for now. Tier B is the right target.

### 2.3 Recommended Approach: Tier A First, Then Tier B

**Phase 1 (can ship with 1.1.3):** A data-only Tier A addon that demonstrates the concept with stricter rules, harsher effects, and rebalanced profiles. No new dimensions. This lets players feel the "hardcore nutrition" fantasy immediately.

**Phase 2 (requires Core 1.2.0 prep):** The Tier B addon with dynamic dimensions. This requires a Core refactor that should be versioned as 1.2.0 since it changes internal data structures.

### 2.4 Core Preparation Pass Design (for Phase 2 / Tier B)

If the decision is to proceed with Tier B eventually, here is the recommended Core preparation:

```
1. NutritionProfile.DIMENSIONS → NutritionDimensionRegistry.dimensions()
   - data/herbcraft/nutrition_dimensions.json defines base 5
   - addon appends via data/<ns>/herbcraft/nutrition_dimensions/*.json
   - startup-only (like herbs.json); not hot-reloadable

2. PlayerNutritionState → Map<String, Integer> instead of int[5]
   - save/load iterates the dimension registry
   - unknown dimensions default to balanced mid-value

3. NutritionSyncPayload → sends dimension IDs + values
   - client caches dynamic dimension list

4. NutritionPanelScreen → iterates NutritionDimensionRegistry
   - auto-scales ROW_H and PANEL_H based on dimension count
   - 5 dims: current layout (ROW_H=20, PANEL_H=170)
   - 6 dims: ROW_H=18, PANEL_H=190
   - 7 dims: ROW_H=16, PANEL_H=200
   - 8+ dims: scrollable panel with ROW_H=15

5. NutritionItemTooltips → iterates dynamic dimensions
6. Correction matrix → N×N, sparse JSON
7. Validators → updated to handle variable dimension count
```

### 2.5 Addon Skeleton Recommendation

```
herbcraft_hardcore_nutrition/
├── fabric.mod.json          # depends: herbcraft >=1.2.0
├── data/
│   └── herbcraft_hardcore_nutrition/
│       └── herbcraft/
│           ├── nutrition_dimensions/
│           │   └── hardcore.json         # adds cocoa_fat, fiber
│           ├── nutrition_profiles/
│           │   └── hardcore_profiles.json # rebalanced + new dims
│           ├── nutrition_rules_override.json  # stricter thresholds
│           └── nutrition_effects_override.json # harsher penalties
├── assets/
│   └── herbcraft_hardcore_nutrition/
│       ├── lang/
│       │   ├── en_us.json
│       │   └── zh_cn.json
│       └── textures/
│           └── mob_effect/
│               ├── cocoa_fat_deficiency.png
│               └── fiber_deficiency.png
└── src/main/java/
    └── com/herbcraft/hardcore/
        └── HardcoreNutritionInit.java   # registers extra effects
```

### 2.6 UI Implications for Hardcore Nutrition

If the addon adds dimensions, the following UI surfaces must adapt:

| Surface | Current | Addon Impact |
|---|---|---|
| Nutrition Panel | 5 rows, fixed 170px height | Must expand to 6-7 rows; auto-scale |
| Nutrition Tooltip (fuzzy) | Iterates `DIMENSIONS` | Must iterate dynamic list |
| Nutrition Tooltip (precise) | Iterates `DIMENSIONS` | Must iterate dynamic list |
| Correction matrix | 5×5 | Must expand to N×N |
| Debug commands | Iterates `DIMENSIONS` | Must iterate dynamic list |
| Nutrition Sync Payload | Fixed 5 values | Must send N dimension-value pairs |
| Save/Load | Fixed 5 fields | Must save/load N fields |

### 2.7 Open Design Questions for Author Decision

1. **Tier A first or skip to Tier B?** A data-only "harsher rules" addon can ship immediately with 1.1.3 and requires zero Core changes. Should we build that as a proof-of-concept?

2. **Which new dimensions?** The existing plan mentions `cocoa_fat`. Other candidates:
   - `fiber` / 膳食纤维 — plant fiber, deficiency causes weakness
   - `mineral` / 矿物质 — bone/shell, deficiency causes fragility
   - `vitamin` / 维生素 — fruit/vegetable variety, deficiency causes vision issues
   
   Recommendation: start with one (cocoa_fat or fiber), not three.

3. **Addon naming:** 
   - `百草可食：硬核营养学` / `Herbcraft: Hardcore Nutrition`
   - `百草可食：饥馑篇` / `Herbcraft: Famine`
   - `百草可食：五养严律` / `Herbcraft: Strict Nourishment`

4. **Version target:** Core prep should be a `1.2.0` bump since it changes internal data structures (NutritionProfile.DIMENSIONS, PlayerNutritionState serialization).

5. **Should the addon also rebalance Cuisine dishes?** If the addon depends on `herbcraft_cuisine` optionally, it could supply stricter dish profiles. But this adds compatibility testing surface.

---

## Part 3: Conclusions and Recommendations

### UI

- Current `1.1.3` UI has **no P0 blockers**.
- The most impactful UI improvements for the current release would be claim hitbox widening (UI-C5, P1) and search/filter in the Codex (UI-C2, P2). Neither is a release blocker.
- The **main UI architecture limitation** is the hardcoded 5-dimension nutrition panel, which blocks Tier B Hardcore Nutrition.

### Hardcore Nutrition

- **Recommended:** Ship a **Tier A data-only addon** first with 1.1.3 (stricter rules, harsher effects, rebalanced profiles). No Core changes needed.
- **Plan:** Design the Tier B dynamic-dimension Core refactor for 1.2.0, then build the full Hardcore Nutrition addon with new dimensions.
- **Do not** harden the base mod. The addon is the opt-in system.

### No code changes were made in this turn.

This is a discussion/analysis document. All findings are based on source review of the current `1.1.3` working tree.
