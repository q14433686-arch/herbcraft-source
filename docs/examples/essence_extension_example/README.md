# Herbcraft Essence Extension Example

This example is not loaded by Herbcraft by default. It documents the files a mod/datapack-style extension should provide if it adds a new alchemy essence entry.

Example essence: `lavender`

Example source item: `examplemod:lavender`

## Required idea

Adding an entry to `data/herbcraft_alchemy/essences.json` is enough for Java to register an extra essence item and potion line at runtime, but a polished entry still needs resources and recipes:

- item definition
- item model
- item texture
- recipe
- language keys

Use the project helper to check coverage:

```bash
python3 scripts/generate_essence_resources.py --check
```

For project-local generated skeletons:

```bash
python3 scripts/generate_essence_resources.py --write-missing
```

Do not overwrite hand-made textures unless you know what you are doing.

## Files shown in this example

```text
data/herbcraft_alchemy/essences.json
assets/herbcraft/items/essence_lavender.json
assets/herbcraft/models/item/essence_lavender.json
assets/herbcraft/lang/en_us.json
assets/herbcraft/lang/zh_cn.json
data/herbcraft/recipe/essence_lavender.json
```

A texture file is also required in real use:

```text
assets/herbcraft/textures/item/essence_lavender.png
```

It is omitted here as a placeholder instruction; real extensions should provide a proper 16x16 texture.
