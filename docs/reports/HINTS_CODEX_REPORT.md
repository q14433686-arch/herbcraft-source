# HINTS_CODEX_REPORT

## Scope

Implemented the agreed lightweight player-guidance pass:

- Herb Codex overview entry.
- Empty bowl / water bowl route tooltips.
- Hidden cuisine recipe unlock explanation.

I read the Jade compatibility note, but did not add Jade integration in this pass because that requires adding/compiling against an optional external API. The current pass keeps compatibility risk low and does not introduce a hard Jade dependency.

## Implemented

### Herb Codex overview

Updated:

```text
core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java
core/src/main/resources/assets/herbcraft/lang/zh_cn.json
core/src/main/resources/assets/herbcraft/lang/en_us.json
```

Added a new `百草经总论 / Herbcraft Overview` entry under a new overview section.

It explains:

- empty bowl + 3~4 herbs -> hodgepodge;
- water bowl -> raw medicinal cuisine base;
- tattered pages can reveal rumours and some secret-dish clues;
- repeated herb eating builds potency and can overload.

### Herb usage lines in Codex entries

For heard entries:

- shows vague usage wording only.

For mastered entries:

- shows raw-use note;
- empty bowl hodgepodge route;
- water bowl medicinal-dish route;
- hidden-dish warning without spoiling exact recipes.

### Bowl route tooltips

Updated:

```text
cuisine/src/main/java/com/herbcraft/cuisine/client/CuisineTooltips.java
cuisine/src/main/resources/assets/herbcraft_cuisine/lang/zh_cn.json
cuisine/src/main/resources/assets/herbcraft_cuisine/lang/en_us.json
```

Tooltips:

- `minecraft:bowl`: explains it is for hodgepodge / vanilla stews, not medicinal raw dishes.
- `herbcraft_cuisine:water_bowl`: explains it is the cuisine base for fixed raw dishes and does not support random hodgepodge.

### Hidden cuisine recipe hints

Updated:

```text
core/src/main/java/com/herbcraft/knowledge/client/CodexClientTooltips.java
cuisine/src/main/java/com/herbcraft/cuisine/client/CuisineTooltips.java
```

Behavior:

- Tattered pages now state that right-clicking records Codex knowledge.
- Tattered pages mention that some secret dish clues are hidden in pages.
- Raw hidden dishes get an extra “secret raw dish” hint.
- Finished hidden dishes get a “secret dish” line.

## Validation

Build:

```text
BUILD SUCCESSFUL
```

Validation commands passed:

```bash
python3 scripts/validate_recipe_isolation.py
python3 scripts/check_lang_diff.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_extended_features.py
python3 scripts/verify_built_jars.py
```

Extra jar spot checks:

- Core jar contains `百草经总论` translation.
- Cuisine jar contains the empty-bowl / water-bowl tooltip translations.

## Jade note

I did not implement Jade in this round. The attached compatibility document is useful and I agree with its design principle: Jade should reveal discovery state, not bypass the Codex. If Jade integration is added later, it should be in an isolated compat package and should not be referenced from the main Herbcraft initializers.
