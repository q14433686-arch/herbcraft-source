# External Ingredients, Client Data Sync, Compat Generator, and P4 API Design — 2026-06-16

Status: implemented infrastructure + design draft; balance/content values for real compat packs still require author review  
Baseline: Herbcraft `1.1.2`

## 1. Scope

This pass implements the infrastructure discussed for future compatibility work:

```text
external 食材录 / Ingredient Codex extension
server-to-client data sync for nutrition/nature/ingredient metadata
draft compatibility pack generator
P4 public Java API design document
```

It does **not** create a finished Farmer's Delight/Croptopia/other-mod compatibility pack yet. The current generator produces draft JSON that must be reviewed and balanced manually.

## 2. External Ingredient Codex extension

New external path:

```text
data/<namespace>/herbcraft/ingredients/*.json
```

Supported forms:

```json
{
  "schema_version": 1,
  "item": "farmersdelight:cabbage",
  "section": "fruit"
}
```

```json
{
  "schema_version": 1,
  "entries": [
    { "item": "farmersdelight:cabbage", "section": "fruit" },
    { "item": "farmersdelight:cooked_rice", "section": "grain" }
  ]
}
```

Currently supported section keys:

```text
meat, seafood, grain, fruit, soup, special
```

Implementation files:

```text
core/src/main/java/com/herbcraft/knowledge/IngredientsChapter.java
core/src/main/java/com/herbcraft/data/HerbcraftExternalDataReloadListener.java
```

Important behavior:

- External non-vanilla ingredient entries use namespace-aware Codex IDs to avoid path collisions.
- Example: `farmersdelight:cabbage` becomes `ingredients/farmersdelight__cabbage` internally.
- Existing vanilla entries keep their old path-only IDs for compatibility.
- External ingredients still need matching nutrition profiles if they should show nutrition in 食材录/tooltips.

Example file added:

```text
docs/examples/external_extension_compat_pack/data/example_compat/herbcraft/ingredients/lavender_tea.json
```

## 3. Client data sync

New sync classes:

```text
core/src/main/java/com/herbcraft/data/HerbcraftDataSyncPayload.java
core/src/main/java/com/herbcraft/data/HerbcraftDataSyncManager.java
```

Synced server-authoritative data:

```text
nutrition profiles
herb nature profiles
ingredient item -> Codex entry ID map
```

Why:

- External datapack/addon nutrition and nature data should not only exist on the server.
- Client tooltips need the same data to display correct knowledge-gated nutrition/nature information in multiplayer.
- Ingredient Codex tooltip gating needs namespace-aware entry IDs for external mod items.

Client receiver:

```text
core/src/main/java/com/herbcraft/knowledge/client/HerbcraftCoreClient.java
```

Registry update hooks:

```text
NutritionRegistry.replaceWithSyncedData(...)
HerbNatureRegistry.replaceWithSyncedData(...)
IngredientsChapter.replaceClientIngredientEntries(...)
```

Sync timing:

- player join;
- after the external data reload listener marks data dirty, the next server tick syncs all online players once.

## 4. Compat pack generator

New script:

```text
scripts/generate_compat_pack.py
```

It reads a CSV table and writes a draft datapack layout for:

```text
nutrition_profiles
ingredients
herb_natures
knowledge_rumors
```

Example workflow:

```bash
python3 scripts/generate_compat_pack.py --write-example /tmp/herbcraft_compat_example.csv
python3 scripts/generate_compat_pack.py \
  --items /tmp/herbcraft_compat_example.csv \
  --output /tmp/herbcraft_compat_pack \
  --namespace farmersdelight_herbcraft \
  --pack-name "Herbcraft Compat: Farmer Example"
```

The generated pack is a draft. The script does not know real balance, recipe context, or herb pharmacology; it only turns reviewed CSV rows into valid Herbcraft extension JSON.

## 5. P4 API design draft

New design document:

```text
docs/PUBLIC_EXTENSION_API_DESIGN.md
```

Main decisions:

- Read-only APIs should come first.
- JSON should remain the preferred integration path.
- Reload-safe data and startup-only herb edible component changes must stay separate.
- Public Java registration APIs should not be implemented until call timing, conflict rules, sync, and examples are documented and tested.

## 6. Documentation and validators updated

Updated:

```text
docs/DATA_DRIVEN_ARCHITECTURE.md
docs/EXTENDING_HERBCRAFT.md
docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md
docs/DOCUMENTATION_INDEX.md
scripts/validate_external_extension_spec.py
scripts/validate_externalized_rules.py
scripts/verify_built_jars.py
```

Validator coverage now checks:

- external ingredient example schema;
- `IngredientsChapter` external/sync support tokens;
- `HerbcraftDataSyncPayload` / `HerbcraftDataSyncManager` presence;
- built jar contains the data sync classes.

## 7. Validation completed

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 bash ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
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
python3 scripts/generate_compat_pack.py --write-example /tmp/herbcraft_compat_example.csv
python3 scripts/generate_compat_pack.py --items /tmp/herbcraft_compat_example.csv --output /tmp/herbcraft_compat_pack --namespace farmersdelight_herbcraft --pack-name "Herbcraft Compat: Farmer Example"
```

Result: all passed.

## 8. Draft item audit for first food-mod compatibility batch

The first requested compatibility batch has been inventoried as a draft:

```text
docs/compat_drafts/food_mods_26_1_2/item_ids_audit.csv
docs/compat_drafts/food_mods_26_1_2/compat_review_draft.csv
docs/compat_drafts/food_mods_26_1_2/generated_pack/
```

Sources checked:

- Farmer's Delight Refabricated `26.1-3.6.5` for `26.1.2` Fabric.
- More Delight `26.05.26-26.1-fabric` for `26.1.2` Fabric.
- Reg's More Foods `1.4.1+26.1+mod` for `26.1.2` / 26.1.x.

Draft counts:

```text
all item-like IDs found: 774
food candidates selected by heuristic: 396
generated nutrition_profiles entries: 396
generated ingredients entries: 396
generated herb_natures entries: 10
```

Important caveat:

- Farmer's Delight and More Delight are normal Fabric mods with identifiable item IDs.
- Reg's More Foods also has a datapack/resource-pack distribution. The modded wrapper exposes `rmf:*` item-like IDs, but the author notes datapack item identity can behave unusually in recipe viewers because many datapack foods may share underlying namespaced IDs. Treat the RMF part as highest-risk and require real client testing before release.
- The generated CSV/pack values are heuristic drafts. They are not final balance.
- Third-party jars were used for local inspection only and were deleted after the audit; no external mod binaries are stored in this repository.

## 9. Five-round compatibility work tracker

Do not hardcode support for Farmer's Delight, More Delight, or Reg's More Foods in Java. The compatibility work should stay data-first: CSV review -> generated JSON -> manual tuning -> client test -> optional independent compat pack.

This tracker is the active handoff record for the five planned rounds. Update it at the end of each round so task progress is not lost.

### Round 1 — item ID audit and first draft table

Status: **completed as draft / needs author review**

Done:

- Downloaded and inspected current target releases for local audit only.
- Deleted third-party jars after inspection; no external mod binaries are stored in this repository.
- Wrote:

```text
docs/compat_drafts/food_mods_26_1_2/item_ids_audit.csv
docs/compat_drafts/food_mods_26_1_2/compat_review_draft.csv
docs/compat_drafts/food_mods_26_1_2/generated_pack/
```

Current draft counts:

```text
all item-like IDs found: 774
food candidates selected by heuristic: 396
generated nutrition_profiles entries: 396
generated ingredients entries: 396
generated herb_natures entries: 10
```

Round 1 acceptance before moving on:

- Author reviews `item_ids_audit.csv` and confirms obvious non-food false positives to remove.
- Author reviews whether all three targets remain in scope for one compat pack or should be split later.

### Round 2 — manual 食材录 section and nutrition review

Status: **completed as AI-reviewed draft / author review still required**

Input file:

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_draft.csv
```

Completed tasks:

1. Removed obvious false-positive food candidates.
2. Corrected many `ingredient_section` values.
3. Replaced the first rough nutrition heuristic with more conservative vanilla-scaled draft values.
4. Kept most prepared dishes as 食材录 + nutrition only.
5. Marked RMF rows with an identity-risk review status.

Round 2 outputs:

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round2.csv
docs/compat_drafts/food_mods_26_1_2/round2_removed_false_positives.csv
docs/compat_drafts/food_mods_26_1_2/ROUND2_REVIEW_NOTES.md
```

Round 2 counts:

```text
Round 1 draft candidates: 396
Round 2 kept entries: 317
Round 2 removed entries: 79
Round 2 generated-data preview: 317 nutrition_profiles, 317 ingredients, 18 herb_natures
```

Round 2 did not overwrite `compat_review_draft.csv`, so the original heuristic draft remains available for comparison.

### Round 3 — herb nature / rumor selection

Status: **completed as AI-reviewed draft / author review still required**

Input file:

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round2.csv
```

Round 3 outputs:

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv
docs/compat_drafts/food_mods_26_1_2/ROUND3_NATURE_RUMOR_NOTES.md
```

Completed decisions:

1. Kept/adjusted herb nature only for conservative raw crop, seed, staple grain, pungent bulb, and clearly botanical stimulant candidates.
2. Removed herb nature from generic RMF fruits/berries; they remain 食材录 + nutrition only.
3. Added no More Delight herb nature rows because the current More Delight list is prepared-food heavy.
4. Added no `knowledge_rumors`; first compat draft should avoid noisy hearsay and has no dedicated rumor language resources yet.

Round 3 generated-data preview:

```text
nutrition_profiles: 317
ingredients: 317
herb_natures: 14
knowledge_rumors: 0
```

Round 3 was smoke-tested through the generator to `/tmp/herbcraft_round3_pack`; Round 4 has now regenerated the checked-in draft pack from the same Round 3 CSV.

### Round 4 — regenerate compat pack and validate

Status: **completed as generated draft / not publishable before client test**

Round 4 outputs:

```text
docs/compat_drafts/food_mods_26_1_2/generated_pack/
docs/compat_drafts/food_mods_26_1_2/ROUND4_GENERATION_VALIDATION_NOTES.md
docs/compat_drafts/food_mods_26_1_2/ROUND4_CHINESE_NATURE_TABLE.md
```

Regeneration command used:

```bash
rm -rf docs/compat_drafts/food_mods_26_1_2/generated_pack
python3 scripts/generate_compat_pack.py \
  --items docs/compat_drafts/food_mods_26_1_2/compat_review_round3.csv \
  --output docs/compat_drafts/food_mods_26_1_2/generated_pack \
  --namespace food_mods_herbcraft \
  --pack-name "Herbcraft Compat Draft: 26.1.2 Food Mods"
```

Validated counts:

```text
CSV rows: 317
nutrition_profiles: 317
ingredients: 317
herb_natures: 14
knowledge_rumors: 0
```

Validation confirmed generated JSON schema shape, allowed section/nature values, no duplicate item IDs, and that generated item sets match the Round 3 CSV. The Chinese nature table lists all 14 generated herb nature rows with Chinese names, nutrition draft, 性、性味、入口味、特质, and notes.

Post-Round-5 note: this three-mod Round 4 generated output was superseded after client testing. The current `generated_pack/` now contains the Farmer's Delight + More Delight only pack; RMF was removed.

### Round 5 — real-client test and publish decision

Status: **completed by author client test for Farmer's Delight + More Delight; RMF removed; Farmer+More release candidate prepared**

Round 5 outputs:

```text
docs/compat_drafts/food_mods_26_1_2/compat_review_round5_farmer_moredelight.csv
docs/compat_drafts/food_mods_26_1_2/generated_pack/
docs/compat_drafts/food_mods_26_1_2/ROUND5_CLIENT_TEST_CHECKLIST.md
docs/compat_drafts/food_mods_26_1_2/ROUND5_CLIENT_TEST_REPORT.md
docs/compat_drafts/food_mods_26_1_2/ROUND5_FINAL_CHINESE_NATURE_TABLE.md
docs/compat_drafts/food_mods_26_1_2/generated_pack/  # reference only after 1.1.3 Core integration
```

Author client-test result:

```text
Farmer's Delight Refabricated: PASS
More Delight: PASS
Reg's More Foods: remove from scope
No objective crash or functional failure was observed for the tested Farmer's Delight / More Delight items.
All tested registered foods were recorded in the Herbcraft Codex / 食材录 flow.
RMF did not crash, but its datapack-driven identity model caused vanilla-item / existing-item collision and lookup risk.
```

Final generated scope:

```text
Farmer's Delight Refabricated 26.1-3.6.5
More Delight 26.05.26-26.1-fabric
```

Removed:

```text
Reg's More Foods 1.4.1+26.1+mod
```

Reason for RMF removal:

```text
RMF behaves more like a datapack-driven food set than a conventional mod with stable independent item IDs. It may rewrite existing item forms and collide with vanilla/Herbcraft-known items. This is not worth supporting in the main compat pack, and Herbcraft should not rebuild RMF into a real mod.
```

Final counts after RMF removal:

```text
CSV rows: 109
Farmer's Delight rows: 79
More Delight rows: 29
RMF rows: 0
nutrition_profiles: 108
ingredients: 108
herb_natures: 5
knowledge_rumors: 0
```

Final namespace:

```text
farmers_moredelight_herbcraft
```

Publish decision:

```text
Farmer's Delight + More Delight: release candidate / may publish after final naming and author approval.
Reg's More Foods: rejected for this main compat pack.
```

Coffee bean deferred note:

```text
Coffee beans were removed with RMF. If a future stable real mod provides coffee beans, they can be treated as processed cocoa beans and reviewed separately for lower side-effect risk, longer positive buff, Haste-like direction, or bitter/settling behavior. No Java hardcoding.
```

Farmer's Delight Refabricated remains the highest-value target because it has current 26.1.x Fabric support and a large food/cooking ecosystem. More Delight is a natural second-layer addon. Reg's More Foods is intentionally excluded from this pack because its datapack/resource-pack identity model is not stable enough for this compatibility layer.

Post-Round-5 integration update:

```text
Farmer+More data is now bundled into Herbcraft Core under data/farmers_moredelight_herbcraft/herbcraft/.
A generic food-nature pharmacology bridge was added so explicit nature+nutrition ordinary foods can participate in nature pharmacology without overriding target mod FOOD/CONSUMABLE components.
百草志 now registers `药食志` entries for those nature-bearing foods after tasting.
Non-edible Farmer's Delight seed rows (`cabbage_seeds`, `tomato_seeds`, `rice`) were removed from the final data.
Missing optional target-mod item IDs are now guarded with registry `containsKey` checks so absent compat items do not become Air placeholder Codex entries.
See docs/reports/FARMER_MORE_BUILTIN_COMPAT_AND_NATURE_BRIDGE_20260617.md.
```
