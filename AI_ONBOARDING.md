# AI_ONBOARDING

This is the mandatory entry document for the next AI agent working on Herbcraft.

## Absolute first step

Before editing code, you must:

0. Generate and read the current compact handoff brief:

```bash
python3 scripts/generate_agent_brief.py --write
```

Then read:

```text
docs/reports/CURRENT_AGENT_BRIEF.md
```

This generated brief summarizes what changed recently, what to do next, which release files are current, and which rules must be followed. Regenerate it at the start of a new session rather than trusting old chat memory.

1. Read `CURRENT_BASELINE.md` next. This file is the short authoritative current anchor and must be updated at the end of every modifying turn.
2. Read these files:
   - `README.md`
   - `AI_ONBOARDING.md`
   - `PROJECT_STATE.md`
   - `NEXT_TASK.md`
   - `CHANGE_IMPACT.md`
   - `BUGS_TODO.md`
   - `SOURCE_REVIEW_2026-06-14.md` if present
   - `docs/DOCUMENTATION_INDEX.md`
   - `docs/DATA_DRIVEN_ARCHITECTURE.md`
   - `docs/EXTENDING_HERBCRAFT.md`
   - `docs/HOMEOSTATIC_COMPAT.md`
   - `docs/PROJECT_SUMMARY.md` if present, but treat it as historical context because parts are now stale.
3. Inspect the actual source tree and resources, including JSON resources. Do **not** trust old TODO/NEXT notes, chat summaries, or release jars blindly.
4. Produce a **接手状态报告** using the template below.
5. Wait for the author to confirm before coding, unless the author explicitly says to proceed immediately.

## Locked environment

- Minecraft `26.3` (port in validation)
- Fabric Loader `0.19.3`
- Fabric API `0.162.0+26.3`
- Java `25`
- Gradle Wrapper `9.6.0`
- Fabric Loom `1.17-SNAPSHOT`
- Mojang official mappings

26.3 port notes are cached in:

- `reference_cache/26_3_porting_notes.md`（含每次结论的证据链与借鉴的上游移植手册）

Earlier notes remain available for historical API context:

- `reference_cache/26_2_porting_notes.md`
- `reference_cache/26_1_porting_notes.md`

If the sandbox lacks JDK 25, use:

```bash
bash scripts/setup_jdk25_temurin.sh
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks
```

26.1 notes are cached in:

- `reference_cache/26_1_porting_notes.md`

## Output/baseline convention

The current local test/release baseline is kept under:

```text
release_output/herbcraft_test_20260614_review/
```

It should contain exactly the current useful local release set:

- `herbcraft-1.1.4.jar`
- `herbcraft-alchemy-1.1.4.jar`
- `herbcraft-cuisine-1.1.3.jar`
- `herbcraft-source-current-20260614.zip`

`release_output/` is local/publishing-only and git-ignored. Public jars should be uploaded via GitHub Releases, not committed to the source repository. Keep only one current baseline unless the author explicitly asks for N/N-1 retention.

## Three design philosophies

1. **Stable IDs are sacred.** Do not rename existing IDs casually.
2. **Information is earned.** Tooltips, Jade, Codex pages, rumors, claim marks, recipe hints, and usage hints must respect knowledge tiers.
3. **Modules must remain independent.** Core must not depend on alchemy or cuisine; addons depend on core only.

## Absolute architecture rules

- No old `TradeOfferHelper`; use 26.1 data-driven villager trade JSON and tags.
- No full vanilla loot table replacement; use Fabric loot events and guard with `source.isBuiltin()`.
- No hard dependency on Jade/REI unless optional compat setup is intentional.
- Do not use deprecated `appendHoverText`; use current tooltip APIs such as Fabric item tooltip callbacks.
- Do not scatter important balance constants as random Java literals when they belong in JSON/config. If temporary Java constants are used, document them and add validators.
- For brewing, remember: vanilla/Fabric `addMix` can reset output stack components. If preserving components, copy the input `ItemStack` and modify only the intended component.
- Nutrition effects are now custom Herbcraft `MobEffect`s. Do not reintroduce vanilla `WEAKNESS`, `SLOWNESS`, `MINING_FATIGUE`, `HUNGER`, `NAUSEA`, or hidden `RESISTANCE` as NutritionManager's core state effects.
- Cuisine/medicine clearing must not remove Herbcraft nutrition effects. Use `HerbcraftMobEffects.isNutritionEffect(...)` checks.

## Current important state

### Raw herb food and Codex guidance

- `data/herbcraft/herbs.json` drives 96 raw edible definitions.
- Raw herb default FOOD/CONSUMABLE components are startup-only; world datapack `/reload` does not make arbitrary items newly edible.
- Optional `food_rolls` are implemented for bundled/startup herb food data:
  - `food_rolls.nutrition` adds probabilistic direct visible hunger after vanilla consumption.
  - `food_rolls.saturation` adds probabilistic direct hidden saturation, clamped to the current visible food level.
- P3.10 raw herb rebalance is implemented and validator-backed. Common scenery/flowers/leaves/saplings no longer provide stable early food. Seeds were strengthened after client testing (`S+2` ordinary seeds, `S+3` melon/pumpkin seeds). Sugar cane remains +1 visible hunger but now gives about +2 hidden saturation and Homeostatic hydration amount 3.
- Core Codex guide pages are data-backed by `data/herbcraft/codex_guides.json`; Cuisine/Alchemy guide entries are module-owned and only appear when their module is installed.
- 五味与性味 / 七情与配伍 guide pages now include plain beginner microcopy. Do not add extra advancements for these explanations unless the author explicitly asks; the Codex is the teaching surface.

### Core nutrition system

Nutrition has been heavily updated in this test baseline:

- Seven Herbcraft nutrition effects exist:
  - `herbcraft:grain_deficiency` / 谷气亏虚
  - `herbcraft:flesh_deficiency` / 血肉亏虚
  - `herbcraft:oil_deficiency` / 津脂不足
  - `herbcraft:vege_deficiency` / 蔬素不足
  - `herbcraft:fruit_deficiency` / 津液不足
  - `herbcraft:dietary_imbalance` / 饮食失衡
  - `herbcraft:well_nourished` / 五养调和
- NutritionManager uses those custom effects instead of vanilla negative effects.
- Serious malnutrition (`< malnutrition_low`) and starved states are sustained until the next nutrition check interval, not random short flashes.
- Warning states remain light/probabilistic.
- `well_nourished` replaces the old hidden vanilla resistance reward and currently gives `+3 armor` and `+12% knockback resistance` through attributes.
- Water bowl still has only 15% success chance; on success it clears vanilla nausea and temporarily relieves `dietary_imbalance` for 60 seconds without repairing nutrition values.
- Debug commands exist: `/herbcraft nutrition get|set|preset ...` and require gamemaster permission.
- Nutrition icons are generated from vanilla item textures by `scripts/generate_nutrition_effect_icons.py`.

### Ingredients chapter (食材录)

- 39 vanilla food entries in the knowledge system, data-driven from `data/herbcraft/ingredients.json`.
- 6 sections: meat, seafood, grain, fruit, soup, special.
- Chapter ID: `ingredients`, sortPriority 50 (between herbal:0 and cuisine:100).
- Knowledge gating: pick up → ENCOUNTERED (name visible), eat → MASTERED (fuzzy nutrition visible). No pharmacology, no herb stack, no effects.
- `NutritionManager.onFoodConsumed()` marks knowledge flags when vanilla foods are eaten.
- `NutritionItemTooltips` gates vanilla food nutrition tooltip behind eating (ingredients chapter state ≥ TASTED).

### Nutrition tuning (2026-06-16)

- All 162 nutrition profiles scaled to 0.6x for balanced pacing. `cooked_beef` flesh=41 (was 69).
- `NutritionRules` Java fallback for `correction_high_threshold` must stay at 750 to match `nutrition_rules.json`; validators cover this to avoid returning to the old 850 dead zone.
- `correction_high_threshold` lowered from 850 to 750. Matrix correction now activates at 75%.
- Graduated decay: 750-849 range uses half decay rate, 850+ uses full rate. Punitive debuffs still require 850+.
- Fluid surplus helper requires at least two non-fruit excess dimensions (>850) with a correction-matrix relationship before activating.

### Verified current source state

- Hidden cuisine dishes exist in source and built jars.
- `BrewingStandCatalystMixin.java` exists and builds.
- `herbcraft_alchemy.mixins.json` currently references classes that exist.
- `百草经总纲` is registered in `HerbalChapter`, not only in lang files.
- A core server smoke reaches `Done` with the current nutrition changes.

## Required 接手状态报告 format

```md
# 接手状态报告

## 1. 我已阅读的文件
- README.md
- AI_ONBOARDING.md
- docs/DOCUMENTATION_INDEX.md
- PROJECT_STATE.md
- NEXT_TASK.md
- CHANGE_IMPACT.md
- BUGS_TODO.md
- SOURCE_REVIEW_2026-06-14.md
- docs/PROJECT_SUMMARY.md
- 其他：...

## 2. 当前环境确认
- Minecraft:
- Fabric Loader:
- Fabric API:
- Java:
- Gradle/Loom:

## 3. 当前源码真实状态
### core
- ...
### alchemy
- ...
### cuisine
- ...

## 4. 我发现的风险点 / 明显不一致
- ...

## 5. 我建议下一步做什么
- ...

## 6. 开始编码前需要作者确认的问题
- ...
```

## Minimum validation commands before releasing

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew build --rerun-tasks
python3 scripts/check_lang_diff.py
python3 scripts/validate_custom_nutrition_effects_phase8.py
python3 scripts/validate_nutrition_correction_phase.py
python3 scripts/validate_externalized_rules.py
python3 scripts/validate_raw_herb_food_balance.py
python3 scripts/validate_essence_resources.py
python3 scripts/validate_homeostatic_compat.py
python3 scripts/validate_nutrition_cuisine_immunity_isolation.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_extended_features.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_external_extension_spec.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_catalyst_mixin.py
python3 scripts/validate_nature_jade.py
python3 scripts/validate_p1_codex_advancements.py
python3 scripts/verify_built_jars.py
```

`validate_animal_food_tags.py` derives the vanilla version from `gradle.properties` and searches `/home/user/cache/minecraft/client-26.3.jar`, Loom's downloaded client/merged jar, or `HERBCRAFT_VANILLA_CLIENT_JAR`. Run a Gradle build first if no vanilla jar cache exists.

Do not claim success if required checks were not run or if they fail.

## Mandatory end-of-turn anchor update

If you change any source, resource, script, documentation, version metadata, validator, or release output, update `CURRENT_BASELINE.md` before your final response. The update must mention what changed, whether output was refreshed, and what validation was run. This prevents future agents from anchoring to stale state.
