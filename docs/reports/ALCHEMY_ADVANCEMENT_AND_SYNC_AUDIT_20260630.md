# Alchemy advancement and data-sync audit — 2026-06-30

## Scope

This pass reviewed the newer Alchemy progression hooks, guide gating, and sync assumptions after the burden-profile / armor-support / distillation / processed-potion work.

User-facing concerns checked:

1. Alchemy advancement tree had not kept up with new effects/rules.
2. Existing progression checks were too broad and could grant logical milestones just from holding items.
3. New effects/rules might have client/server data-sync issues.

## Progression fixes

### New advancements

Added four code-granted Alchemy advancements:

- `herbcraft_alchemy:first_distilled_essence`
  - parent: `herbcraft_alchemy:first_essence`
  - represents obtaining a componentized distilled essence with an orientation.
- `herbcraft_alchemy:first_sustained`
  - parent: `herbcraft_alchemy:first_potion`
  - represents actually tasting/being affected by a sustained processed potion.
- `herbcraft_alchemy:first_intense`
  - parent: `herbcraft_alchemy:first_potion`
  - represents actually tasting/being affected by an intense processed potion.
- `herbcraft_alchemy:first_armor_support`
  - parent: `herbcraft_alchemy:first_distilled_essence`
  - represents obtaining armor with an armor-support component.

All four use `minecraft:impossible` plus the standard `trigger` criterion and are awarded from Java hooks, avoiding item-name or broad inventory trigger ambiguity.

### Narrowed potion progression semantics

Before this pass, `AlchemyAcquisitionHooks` could grant potion milestones during inventory scanning for any Herbcraft potion stack. This meant a player could receive tasting/progression milestones by merely picking up or holding a potion.

Now:

- Inventory acquisition still handles concrete item possession milestones:
  - raw essence -> `first_essence`;
  - distilled essence component -> `first_distilled_essence`;
  - coated weapon -> `first_coating`;
  - armor support component -> `first_armor_support`.
- Inventory acquisition no longer grants potion-use milestones.
- Potion stacks found in inventory only initialize missing runtime components and mark Codex knowledge as seen/heard.
- Actual potion progression is awarded from `AlchemyKnowledgeSupport.markPotionTasted(...)`, which is reached from the existing use/exposure hooks:
  - drinking via `AlchemyItemMixin`;
  - splash exposure via `ThrownSplashPotionKnowledgeMixin`;
  - lingering cloud exposure via `AreaEffectCloudKnowledgeMixin`.

The taste/use path now grants:

- `first_potion`;
- `first_clarified` for `purified` style;
- `first_sustained` for `sustained` style;
- `first_intense` for `intense` style;
- `first_murky` for `murky_edge` style;
- `undead_toxin` for `undead_venom`;
- `witch_knowledge` for `witch_*` paths;
- `epic_potion` when the stack carries exceptional potion quality.

### Special potion source mapping

Both inventory knowledge discovery and taste progression now normalize special potion paths back to valid essence base IDs:

- `undead_venom` -> `flowering_azalea`;
- `cardiac_toxin` -> `lily_of_the_valley`;
- `wither_venom_iii` -> `wither_rose`;
- `witch_*_murky` paths strip the `witch_` prefix after style suffix normalization.

This prevents special potion entries from failing knowledge lookup just because the path is not itself an essence ID.

### Guide/Codex gating update

`AlchemyChapter` now accounts for the new advancement milestones:

- armor-support guide hints unlock from distilled essence / armor-support progress rather than broad raw-essence progress;
- potion processing semantics include sustained and intense milestones;
- quality/advanced processing hints include sustained and intense alongside clarified/murky;
- first armor-support completion has an explicit guide line.

The regeneration helper `scripts/rebuild_advancements_and_lang.py` was updated so future regeneration preserves the expanded 13-file Alchemy advancement tree and does not recreate a broad `witch_knowledge` inventory trigger.

## Data-sync audit conclusion

### Component data

The newer jar-facing item stack components are synchronized over the network:

- `AlchemyPotionComponents`
  - `POTION_QUALITY`
  - `POTION_BONUS`
  - `POTION_SOURCE`
  - `POTION_PROCESSING_STYLE`
  - `POTION_BURDEN`
  - `POTION_PHARMACOLOGY`
  - `POTION_BODY_STATE`
  - `ADVANCED_POTION_INITIALIZED`
- `DistillationComponents.DISTILLED_ESSENCE`
- `ArmorSupportComponents.ARMOR_SUPPORT`

The new validator checks that every declared Alchemy data component in those component holder classes has a `.networkSynchronized(...)` call.

### Rule tables

The newer Alchemy rule tables remain bundled/startup rules loaded from the mod jar:

- `potion_processing_rules.json` through `PotionProcessingRules.loadFromBundledJson()`;
- `armor_support_rules.json` through `ArmorSupportRules.loadFromBundledJson()`;
- `processed_coating_rules.json` through `ProcessedCoatingRules.loadFromBundledJson()`;
- `distillation_rules.json` through `DistillationRules.loadFromBundledJson()`;
- `potion_burden_effects.json` through static registration-time `AlchemyBurdenEffectRules` loading.

The burden-effect table is registration-time content because it defines mob-effect registry properties/attribute modifiers. It is not safe to hot-reload as normal server datapack content without a larger registry strategy.

### Current safe assumption

Normal use is safe when client and server run the same Alchemy jar:

- authoritative runtime item stack data is synchronized through components;
- both physical sides load the same bundled rule JSON from the same mod jar;
- tooltips and viewer display metadata read the local bundled rule tables.

### Remaining sync limitation

Alchemy rule tables are not currently sent through `HerbcraftDataSyncPayload` and are not hot-reloaded into client caches. If a future version allows server datapacks or server-side override of Alchemy processing/support/distillation/coating rules, a dedicated Alchemy rule sync payload should be added. Until then, client/server Alchemy jar parity is a requirement for tooltip/viewer text consistency.

## Files changed in this audit

- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyKnowledgeSupport.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java`
- `alchemy/src/main/resources/data/herbcraft_alchemy/advancement/first_distilled_essence.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/advancement/first_sustained.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/advancement/first_intense.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/advancement/first_armor_support.json`
- `alchemy/src/main/resources/assets/herbcraft/lang/en_us.json`
- `alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json`
- `scripts/rebuild_advancements_and_lang.py`
- `scripts/validate_alchemy_advancement_and_sync.py`
- `scripts/validate_extended_features.py`
- `scripts/validate_p1_codex_advancements.py`
- `scripts/validate_potion_processing_rules.py`

## Validation

Passed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
for s in scripts/validate_*.py; do python3 "$s"; done
python3 scripts/check_lang_diff.py
python3 scripts/verify_built_jars.py
```

Notable new/updated validators:

- `scripts/validate_alchemy_advancement_and_sync.py`
- `scripts/validate_extended_features.py`
- `scripts/validate_p1_codex_advancements.py`
- `scripts/validate_potion_processing_rules.py`

## Client-only checks still recommended

1. Confirm that simply picking up a processed potion no longer grants `first_potion`, `first_clarified`, `first_sustained`, `first_intense`, `first_murky`, `witch_knowledge`, `undead_toxin`, or `epic_potion`.
2. Confirm drinking/splash/lingering exposure grants the relevant potion milestones.
3. Confirm obtaining distilled essence grants `first_distilled_essence`.
4. Confirm obtaining supported armor grants `first_armor_support`.
5. Confirm guide text now appears at the expected stages.
6. Confirm client/server with matching jars shows consistent tooltip/rule text for processed potions, distilled essences, coatings, and armor support.
