# Herbcraft Extension Generator / API Plan

This document defines the next recommended implementation sequence for making Herbcraft easier to extend without turning the current stable 1.1.0 codebase into an uncontrolled dynamic platform too quickly.

## Current source facts checked

Current data-backed systems include:

- `core/src/main/resources/data/herbcraft/herbs.json`
- `core/src/main/resources/data/herbcraft/herb_natures.json`
- `core/src/main/resources/data/herbcraft/nutrition_profiles.json`
- `core/src/main/resources/data/herbcraft/nutrition_effects.json`
- `core/src/main/resources/data/herbcraft/special_counters.json`
- `core/src/main/resources/data/herbcraft/codex_loot_injections.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/essences.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/potion_bonus_pools.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/special_potions.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/coating_rules.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/witch_loot.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/codex_loot_injections.json`
- `cuisine/src/main/resources/data/herbcraft_cuisine/dishes.json`
- `cuisine/src/main/resources/data/herbcraft_cuisine/hodgepodge_rules.json`
- `cuisine/src/main/resources/data/herbcraft_cuisine/codex_loot_injections.json`

Current alchemy essence behavior:

- `AlchemyRegistries` reads `data/herbcraft_alchemy/essences.json`.
- The current 40 built-in essence fields are still present for compatibility, but initialize through `essenceFromJson(...)`.
- Extra essence entries beyond the 40 compatibility fields can be registered dynamically at `AlchemyRegistries.initialize()` time.
- However, extra dynamic essences still need assets, lang, recipes, and optional docs/trades to feel complete.

Existing essence resource patterns:

- Item definition: `alchemy/src/main/resources/assets/herbcraft/items/essence_<id>.json`
- Item model: `alchemy/src/main/resources/assets/herbcraft/models/item/essence_<id>.json`
- Texture: `alchemy/src/main/resources/assets/herbcraft/textures/item/essence_<id>.png`
- Recipe: `alchemy/src/main/resources/data/herbcraft/recipe/essence_<id>.json`
- Lang keys live in:
  - `alchemy/src/main/resources/assets/herbcraft/lang/en_us.json`
  - `alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json`

## Goal

Make it easy to add or maintain Herbcraft-compatible content without manually editing many scattered files.

The first implementation target is not a public API. It is a safe project-local generator and documentation pipeline.

## Phase 1: project-local essence resource generator

Status: implemented as `scripts/generate_essence_resources.py` with `--check`, `--write-missing`, `--overwrite-generated`, and `--scaffold-extension` modes. Scaffold mode supports single or multiple source items, optional trade generation, and optional Codex rumor skeleton generation.

### New script

Create:

```text
scripts/generate_essence_resources.py
```

### Input

Primary input:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/essences.json
```

Optional CLI flags:

```bash
python3 scripts/generate_essence_resources.py --check
python3 scripts/generate_essence_resources.py --write-missing
python3 scripts/generate_essence_resources.py --overwrite-generated
```

Recommended default behavior:

- `--check`: validate required files, do not write.
- `--write-missing`: generate only missing files.
- No default destructive overwrite.

### Outputs to generate/check

For every essence id in `essences.json`:

1. Item definition:

```text
alchemy/src/main/resources/assets/herbcraft/items/essence_<id>.json
```

Expected structure:

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "herbcraft:item/essence_<id>"
  }
}
```

2. Item model:

```text
alchemy/src/main/resources/assets/herbcraft/models/item/essence_<id>.json
```

Expected structure:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "herbcraft:item/essence_<id>"
  }
}
```

3. Recipe(s):

Default for one source item:

```text
alchemy/src/main/resources/data/herbcraft/recipe/essence_<id>.json
```

Default recipe shape:

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    "minecraft:source",
    "minecraft:source",
    "minecraft:source",
    "minecraft:source",
    "minecraft:source",
    "minecraft:source",
    "minecraft:source",
    "minecraft:source",
    "minecraft:glass_bottle"
  ],
  "result": {
    "id": "herbcraft:essence_<id>",
    "count": 1
  }
}
```

For multiple `source_items`, follow the existing convention:

```text
essence_<id>_from_<source_path>.json
```

Examples currently present:

- `essence_tulip_from_red_tulip.json`
- `essence_tulip_from_orange_tulip.json`
- `essence_tulip_from_white_tulip.json`
- `essence_tulip_from_pink_tulip.json`
- `essence_fern_from_fern.json`
- `essence_fern_from_large_fern.json`

4. Lang skeleton checks:

- `item.herbcraft.essence_<id>` should exist in `en_us.json` and `zh_cn.json`.
- The generator may add placeholder English names by converting snake_case to Title Case, but should not invent polished Chinese names unless explicitly configured.
- Suggested behavior:
  - `--check`: fail if missing.
  - `--write-missing`: add English placeholder and Chinese placeholder equal to English or `TODO: <id>` only if author approves.

5. Texture checks:

- Must check whether `essence_<id>.png` exists.
- Do **not** overwrite existing hand-made textures.
- Optionally generate a placeholder bottle-like texture only when missing and only under `--write-missing`.
- If generating placeholders, mark them visually as generated and document that they need review.

### Validator integration

Extend or add:

```text
scripts/validate_essence_resources.py
```

Checks:

- Every `essences.json` id has item definition, model, texture, lang keys, and at least one recipe.
- Every source item has a corresponding extraction recipe.
- Existing 40 built-in essence resources remain present.
- Extra dynamic essences must not be registered without resource coverage unless explicitly marked as development-only.

## Phase 2: example extension pack

Status: initial static example exists under `docs/examples/essence_extension_example/`. The generator can also create one-off standalone scaffolds with `--scaffold-extension`.

Create a documentation/example folder, not loaded by the mod by default:

```text
docs/examples/essence_extension_example/
```

Example should include a fictional or clearly documented sample such as `examplemod:lavender`.

Recommended structure:

```text
README.md
alchemy/src/main/resources/data/herbcraft_alchemy/essences.json
alchemy/src/main/resources/assets/herbcraft/lang/en_us.json
alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json
alchemy/src/main/resources/assets/herbcraft/items/essence_lavender.json
alchemy/src/main/resources/assets/herbcraft/models/item/essence_lavender.json
alchemy/src/main/resources/assets/herbcraft/textures/item/essence_lavender.png
alchemy/src/main/resources/data/herbcraft/recipe/essence_lavender.json
```

The example README should explain:

- How to add an essence entry.
- How source item IDs work.
- Which files are mandatory for a polished entry.
- How to run the validator.
- Why registry IDs must remain stable.

## Phase 3: public extension documentation

Status: initial public extension documentation exists as `docs/EXTENDING_HERBCRAFT.md`.

Create:

```text
docs/EXTENDING_HERBCRAFT.md
```

Recommended sections:

1. Compatibility warning and supported version.
2. Stable ID policy.
3. How to add edible herb definitions.
4. How to add herb nature data.
5. How to add nutrition profiles.
6. How to add alchemy essences.
7. How to add cuisine dishes.
8. How to add knowledge rumors.
9. How to add Codex loot injections.
10. How to validate your pack.
11. What is not yet supported.

Current limitations to document honestly:

- Dynamic alchemy essences can register from JSON, but extra entries still need resource files.
- Automatic trade generation is not implemented.
- Automatic Codex prose generation is not implemented.
- REI integration is not implemented.
- UI layout constants are not data-driven and should not be treated as public API.

## What not to do yet

Do not data-drive these until there is a concrete need:

- Network payload IDs.
- Data component IDs.
- Recipe serializer IDs.
- Core item IDs.
- UI pixel/layout constants.

These are stability-sensitive implementation details, not gameplay balance tables.

## Recommended next implementation order

1. Implement `scripts/generate_essence_resources.py` with `--check` and `--write-missing`.
2. Add `scripts/validate_essence_resources.py` or integrate checks into `validate_externalized_rules.py`.
3. Add `docs/examples/essence_extension_example/`.
4. Add `docs/EXTENDING_HERBCRAFT.md`.
5. Only after those are stable, consider broader datagen for cuisine/trades/Codex text.
