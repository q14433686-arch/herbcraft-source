# Herbcraft Wiki Source

Status: source-of-truth wiki workspace for Herbcraft `1.1.3+`

The current full-system source document is:

```text
docs/HERBCRAFT_FULL_SYSTEM_WIKI_1_1_3.md
```

This `docs/wiki/` directory is the recommended place to split that large document into publishable pages.

## Where should the public Wiki live?

Recommended path:

```text
1. Keep canonical Markdown in this repository under docs/wiki/.
2. Mirror selected pages to GitHub Wiki or Modrinth/CurseForge/MC百科 as needed.
3. Do not build a separate website yet.
```

Reason:

- Keeping Wiki source in the repo means it versions with code.
- It lets future AI/developers update docs in the same pull/change set as source.
- GitHub Wiki can be used later as a public mirror, but GitHub Wiki alone is easier to let drift from source.

## Suggested page split

```text
00_home.md
01_installation.md
02_core_loop.md
03_herb_codex.md
04_ingredient_codex.md
05_food_nature.md
06_raw_herb_eating.md
07_herb_stack.md
08_nature_flavors_traits_qi.md
09_seven_relations.md
10_nutrition.md
11_cuisine.md
12_alchemy.md
13_compatibility.md
14_addon_author_guide.md
15_faq.md
```

## Spoiler policy

Player-facing pages should explain systems without dumping every hidden answer.

Keep hidden/advanced item tables separate as developer appendices if needed.

## Current status

The monolithic source draft exists. Page splitting is optional future documentation work.
