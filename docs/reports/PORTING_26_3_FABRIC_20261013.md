# Minecraft 26.3 Fabric port — implementation report

Date: 2026-10-13 ~ 2026-10-14  
Branch: `arena/99a30c9a-herbcraft-source`  
Baseline: Minecraft `26.2` / Fabric Loader `0.19.3` / Fabric API `0.155.2+26.2` / Gradle Wrapper `9.5.1`  
Target: Minecraft `26.3` / Fabric Loader `0.19.3` / Fabric API `0.162.0+26.3` / Gradle Wrapper `9.6.0`

Investigation notes (with evidence for every conclusion): `reference_cache/26_3_porting_notes.md`.
CI: the three workflows live in `docs/ci/` (staged, not yet active — see below).

## Scope

26.3 is not a version-number bump for this repository. Three real migrations were required:

1. **Brewing is data-driven now.** The runtime `PotionBrewing` registry and Fabric's
   `FabricPotionBrewingBuilder` are gone, and 26.3 no longer auto-creates splash/lingering
   recipes for registered potions. Every mix *and* every container transformation must be
   shipped as `minecraft:brewing` recipe JSON.
2. **Villager trade / item-modifier schema renamed** in a way that fails *silently*.
3. **LWJGL now ships SDL3**, so `org.lwjgl.glfw` is off the classpath.

## Applied changes

### Toolchain

- `gradle.properties`: Minecraft `26.2` → `26.3`, Fabric API `0.155.2+26.2` → `0.162.0+26.3`,
  optional deps to the 26.3 lines — Jade `26.3.5+fabric`, JEI `31.12.0.65`, REI `26.3.824`,
  Architectury `22.0.5` (each verified against upstream branch/Maven metadata, not copied);
- `gradle/wrapper/gradle-wrapper.properties`: `9.5.1` → `9.6.0`;
- five `fabric.mod.json` (core, alchemy, cuisine, plus the two `docs/examples/` addons):
  `~26.3`, `>=0.162.0+26.3`, `>=0.19.3`, `suggests.jade >=26.3.5`;
- `README.md`: intro line and the whole "Locked environment" block.

### Brewing (520 new recipe files)

New directory `alchemy/src/main/resources/data/herbcraft/recipe/brewing/`:

| File pattern | Count | Meaning |
|---|---:|---|
| `base_<id>.json` | 40 | awkward potion + `herbcraft:essence_<id>` → base potion |
| `clarified_<id>.json` | 31 | base + honeycomb → clarified line (essences with positive effects) |
| `murky_<id>.json` | 17 | base + fermented spider eye → murky line (essences with negative effects) |
| `long_<id>.json` | 40 | base + redstone → long variant |
| `strong_<id>.json` | 40 | base + glowstone dust → strong variant (`wither_rose` outputs `herbcraft:wither_venom_iii`) |
| `undead_venom.json`, `cardiac_toxin.json` | 2 | the two remaining special mixes |
| `splash_<potion>.json` | 175 | every registered potion + gunpowder → splash potion |
| `lingering_<potion>.json` | 175 | every registered potion + dragon breath → lingering potion |

That is 170 mixes + 350 container transformations = 520 files, covering all 175 registered
potions (168 essence variants — base/clarified/murky/long/strong across the 40 essences — plus
the 8 special potions). The five `witch_*_murky` specials have no mix because the witch loot
integration produces them, but they still get splash/lingering recipes.

Java changes:

- `alchemy/src/main/java/com/herbcraft/alchemy/registry/AlchemyRegistries.java`: mix
  registration removed (the builder it used no longer exists); the potion IDs the recipes
  reference are still owned here, and the file now documents where the JSON comes from;
- `alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandCatalystMixin.java`:
  **correction (found by the runtime smoke test on 2026-10-13, see the follow-up section):** the
  `doBrew` HEAD injection was *not* unaffected. 26.3 changed `doBrew` to
  `(ServerLevel, BlockPos, BrewingStandBlockEntity)`, so the injector failed mixin apply at startup.
  It now takes the block entity, copies the four slots into a `NonNullList`, runs the same Java rules,
  and writes the slots back.

Tooling:

- `scripts/generate_brewing_recipes.py` — regenerates the whole directory from
  `essences.json` / `special_potions.json`; self-checks that every special-mix input and
  output potion exists; check mode by default, `--write` to apply;
- `scripts/validate_brewing_recipes.py` — fails if any file drifts from the generator, if a
  removed-API mix registration reappears in Java (`FabricPotionBrewingBuilder` / `addMix` /
  `PotionBrewing`), if a recipe references an unknown potion, or if
  `BrewingStandCatalystMixin` is dropped from the mixin config.

### Villager trades (80 migrated files)

- `scripts/migrate_villager_trades_26_3.py` — `given_item_modifiers` → `given_item_modifier`,
  inline modifier `function` → `type`, and adds the now-required `count` to
  `wants` / `gives` / `additional_wants` (61 `gives`, 12 `wants`, 2 `additional_wants`).
  Idempotent; check mode by default, `--write` to apply. Numeric `max_uses` / `xp` /
  `reputation_discount` values are still valid inline providers and were left untouched.
- `scripts/generate_essence_resources.py` — the generated wanderer essence trade now emits
  `count`;
- `scripts/validate_alchemy_trade_loot_arrow_adaptation.py` — reads the new field names.

### SDL input (3 files)

- `core/src/main/java/com/herbcraft/knowledge/client/HerbcraftCoreClient.java`:
  keybind is now `InputConstants.Type.KEYBOARD` + `InputConstants.KEY_N`;
- `core/src/main/java/com/herbcraft/knowledge/client/CodexScreen.java`: `keyPressed` compares
  `InputConstants.KEY_T`;
- `core/src/main/java/com/herbcraft/nutrition/client/NutritionPanelScreen.java`:
  `keyPressed` compares `InputConstants.KEY_N`.

### Vanilla-tooling scripts bumped to 26.3

- `scripts/validate_item_textures.py`, `scripts/generate_item_textures.py`: client jar name,
  piston-meta URL (`11f1aefe473eefba0af62e3286c33b692807b2eb/26.3.json`), temp directory;
- `scripts/generate_nutrition_effect_icons.py`: loom cache path `26.3`;
- `scripts/validate_animal_food_tags.py`: version string.

## Audited and deliberately not changed

| Area | Result |
|---|---|
| Rendering (blaze3d → renderpearl, `RenderPipeline`, `PoseStack#mulPose`, shaders) | no raw GL / custom render types / custom shaders; GUI uses `GuiGraphicsExtractor` + `RenderPipelines.GUI_TEXTURED`, both still present in 26.3 |
| `FriendlyByteBuf#readMap/writeMap`, `openUri`, pack resources, block `codec()`, `PushReaction`, `RegistryLayer`, `BundleContents`, `SlotDisplay.TagSlotDisplay` | no call sites |
| Loot tables (`functions` → `modifier`, `condition` single, tag entry `name` → `items`) | repository ships no loot tables; the **code-level** loot injection API did change and is fixed — see below |
| Advancements (entity predicate `type` mandatory, `nbt` → `minecraft:nbt`, `enter_block.recipe` → `recipes`) | all 35 advancements use only `minecraft:impossible` |
| Vanilla tags (`#dowses_campfires` → `#douses_campfires`, explorer-map tags) | not referenced; referenced tags (`leaves`, `swords`, `spears`, `shovels`, `pickaxes`, `hoes`, `axes`) are unchanged |
| Cooking recipes | all 66 already carry the required `cookingtime` |
| Jade / REI / JEI adapters | APIs verified one-by-one against upstream 26.3 branches; no source change needed |

### Loot injection code (found by CI, fixed)

`:core` and `:cuisine` compiled clean on the first CI run; `:alchemy` failed with exactly
three errors, all in `AlchemyCodexHooks.addItemLoot`. Fixed in `e64f1bf`:

| 26.2 code | 26.3 change | fix |
|---|---|---|
| `import ...loot.providers.number.ConstantValue` | number providers split into `providers/number/ints` (`ContextIntProvider`) and `providers/number/floats` (`ContextFloatProvider`); `ConstantValue` only survives in floats | `providers.number.ints.ContextIntProviders.exactly(int)` → `Holder<ContextIntProvider>` |
| `entries.LootPoolSingletonContainer.Builder<?>` as the declared type of the entry | that builder type no longer exists in `entries` | infer the local with `var` (`LootItem.lootTableItem(Item)` and its builder's `apply(LootItemFunction)` both still exist — proven by `CodexLoot.addTatteredPagePool` compiling) |
| `SetItemCountFunction.setCount(ConstantValue.exactly(n))` | `set_count` is an int-valued field; 26.3 moved `rolls`/`bonusRolls` to `Holder<ContextIntProvider>` and made every other loot-function number field int-valued too | pass the int provider |

`SetPotionFunction.setPotion(Holder<Potion>)` was not part of the error set and needed no change.

No item IDs, recipe IDs, trade IDs, component IDs, potion IDs, or gameplay balance were changed.

## Verification performed

- `python3 scripts/check_version_consistency.py` → 23/23 pass on 26.3;
- `python3 scripts/generate_brewing_recipes.py` (check mode) and
  `python3 scripts/validate_brewing_recipes.py` → 520 recipes consistent, no removed-API
  residue in Java, `BrewingStandCatalystMixin` still registered;
- `python3 scripts/migrate_villager_trades_26_3.py` (check mode) → 134 trade files all in the
  26.3 schema;
- the full static validator suite (60+ scripts) re-run in the sandbox: green, except the ones
  that require a built jar or the vanilla client jar (no network to `maven.fabricmc.net` here);
- fixed `scripts/validate_codex_payload_separation.py`, which had been failing since 26.2
  because it still matched the pre-26.2 `Minecraft.screen` / `setScreen` strings.

## CI status (the priority item) — live and green

Per the user's instruction the CI trio was built **first** and mirrors
`q14433686-arch/TaCZ_Refabricated_Unofficial`'s `docs/ci/` convention. The maintainer then
activated it from the GitHub web UI, so the three workflows now live in
`.github/workflows/` (byte-identical to the staged copies except for the later-added
`chmod +x gradlew` line). What they caught, in order:

| File | Purpose |
|---|---|
| `compile-check.yml` | `compileJava` on all three modules; pushes the log back to `build-reports/compile-java.log` on `arena/**` branches |
| `consistency.yml` | version-declaration gate (this is why the 26.3 bump is caught if any file is missed) |
| `build.yml` | full `./gradlew build`, jar artifacts, and four static gates (lang parity, mixin integrity, version consistency, built-jar verification) |

Two data gates were added to `build.yml` (cheap, and both target the silent-failure class of
bug this port is about):

```yaml
- name: Brewing recipes match the generator (26.3 data-driven brewing)
  run: python3 scripts/validate_brewing_recipes.py

- name: Villager trades use the 26.3 schema
  run: python3 scripts/migrate_villager_trades_26_3.py
```

### Result

| commit | runs | outcome |
|---|---|---|
| `eb0ade3`, `eb9f72a` | build, compile-check | failure — `./gradlew: Permission denied`; `gradlew` had been committed with git mode `100644`, and `actions/checkout` preserves it. Fixed in `e2ba41c` with `git update-index --chmod=+x gradlew` (`100755`) plus an explicit `chmod +x gradlew` in both staged workflows |
| `e2ba41c` | build, compile-check | failure — real code errors: `:core`/`:cuisine` clean, `:alchemy` 3 loot-API errors (see "Loot injection code") |
| `e64f1bf` | build, compile-check | **success** — three `compileJava`, full `./gradlew build`, jar verification, and all six static gates green |

The compile-check workflow's log push-back (`build-reports/compile-java.log`) is the only way to
read compiler output from this sandbox: `gh run view --log-failed` and the jobs-log API both fail
here with `EOF` (`results-receiver.actions.githubusercontent.com` /
`productionresultssa3.blob.core.windows.net` are unreachable).

## Remaining work

1. **Real 26.3 client launch** — the dedicated-server runtime smoke now runs in CI and passes
   (see the follow-up section). Still to check by hand, because it cannot run in this sandbox
   (no network to `maven.fabricmc.net`, so no local Gradle run and no game launch): the brewing
   stand UI with the 520 data-driven recipes, villager trades, Jade/JEI/REI displays, en_us/zh_cn. The compile run already cleared
   the previously-suspected items (`CoatingSmithingRecipe`, `ArmorSupportSmithingRecipe`, JEI
   registration calls) — they all compile.
2. `consistency.yml` has never executed: its `paths` filter only matches version-declaration
   files, which no commit has touched yet. Touching `gradle.properties` triggers it.
3. The 26.2-referencing docs were refreshed (`CURRENT_BASELINE.md`, `NEXT_TASK.md`,
   `PROJECT_STATE.md`, `AI_ONBOARDING.md`, `docs/DOCUMENTATION_INDEX.md`, `CHANGELOG.md`);
   their older entries are kept deliberately as history.

## Follow-up on branch `arena/dcbd25af-herbcraft-source` (2026-10-13)

- The 26.3 work above was recovered onto the session branch by fast-forward (same commits, `95f259c`).
- **Runtime smoke** is now automated: `.github/workflows/runtime-smoke.yml` (copy in `docs/ci/`) starts `:core`, `:alchemy` and `:cuisine` dedicated servers and passes only on `Done (` with no mixin failure. It turns "Remaining work item 1" from manual into CI. It passes on `81b5e07` (see below).
- Theme colour parsing: 6-digit RGB values from theme JSON get an opaque alpha (see `CURRENT_BASELINE.md`).
- The static audit against `TaCZ_Refabricated_Unofficial/docs/PORTING_NOTES.md` (colour alpha, singular data directories, item models, `blit` sizes, `BlockItem` description ids) found no further issues.

- **Runtime smoke, three CI runs:**
  - `aedee0a`: `:core` ✅ and `:cuisine` ✅ reach `Done`; `:alchemy` ❌ failed mixin apply on `BrewingStandCatalystMixin#doBrew` (expected `(ServerLevel, BlockPos, BrewingStandBlockEntity, CallbackInfo)`).
  - `5e497c7`: `doBrew` fixed; `:alchemy` ❌ next failed on `BrewingStandPotionSlotMixin#mayPlaceItem`, a static method that 26.3 removed.
  - `81b5e07`: ✅ all three modules reach `Done`, no mixin apply failure. The 26.3 member signatures used to fix the mixins are in `docs/reports/MC_26.3_MIXIN_TARGET_SIGNATURES.txt`, captured from the compile-check log by a temporary `probeMcApi` task (removed again).
  - Lesson: compile success does not check mixin descriptors or target names. Each runtime failure only surfaces one class at a time, so the full target listing was the fastest way out.

## Follow-up 2026-10-14: Codex clicks dead on 26.3 (SDL mouse ids)

- Symptom: 百草经 (Herb Codex) left-clicks did nothing.
- Cause: 26.3 input is SDL. `MouseButtonEvent.button()` is left = 1 / middle = 2 / right = 3. `CodexScreen` used `== 0`.
- Fix: `InputConstants.MOUSE_BUTTON_LEFT` at all four checks; `@Override` added on `mouseClicked` and `mouseScrolled`; validator updated (`scripts/validate_codex_hitboxes.py`).
- Lesson: the earlier note "mouseClicked unchanged in 26.3" was based on reading source alone. The enum-like button ids changed, and nothing in compile-check can catch a numeric comparison.
