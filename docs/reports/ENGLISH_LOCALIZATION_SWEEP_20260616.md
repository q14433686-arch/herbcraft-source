# English Localization Sweep — 2026-06-16

Status: implemented in `1.1.2` source line

## 1. Reason

Client testing in English found that the Herb Codex chapter list used the abbreviation:

```text
Ch.%s · %s
```

This was understandable to some English readers as “Chapter”, but it was not clear enough and looked like an untranslated UI prefix. The author requested a broader English localization sweep, not just a one-key fix.

## 2. Checks performed

Scanned current language files:

```text
core/src/main/resources/assets/herbcraft/lang/en_us.json
alchemy/src/main/resources/assets/herbcraft/lang/en_us.json
cuisine/src/main/resources/assets/herbcraft_cuisine/lang/en_us.json
```

Checked for:

- CJK characters accidentally left in English language files;
- ambiguous `Ch.` abbreviation in visible English UI;
- old `Turbid` wording after the project had standardized on `murky` / `_murky` for English-facing alchemy text;
- nutrition term mismatches such as `Vege` vs clearer English wording;
- language key parity between `zh_cn` and `en_us`.

## 3. Changes made

### Core English localization

```text
book.herbcraft.chapter_format
  Ch.%s · %s -> Chapter %s · %s
```

Nutrition wording was aligned to the shorter in-game term `Greens`:

```text
nutrition.herbcraft.dimension.vege        Vege -> Greens
effect.herbcraft.vege_deficiency          Vegetable Deficiency -> Greens Deficiency
nutrition.herbcraft.state.malnourished.vege
  Low on vegetables. Feeling parched. -> Low on greens. Feeling parched.
nutrition.herbcraft.state.abundant.vege
  Vegetable intake is abundant. -> Greens are abundant.
book.herbcraft.section.ingredients.fruit
  Fruits & Vegetables -> Produce
```

### Alchemy English localization

All visible `Turbid` / `turbid` wording in current Alchemy English language data was changed to `Murky` / `murky`, matching the internal `_murky` route name and current guide wording.

Examples:

```text
Potion of Turbid Poppy -> Potion of Murky Poppy
Splash Potion of Witch-Brewed Turbid Tulip -> Splash Potion of Witch-Brewed Murky Tulip
tooltip.herbcraft.potion.type.murky: Turbid -> Murky
```

### Current release copy

Updated `docs/RELEASE_PAGE_COPY_1.1.2.md` to use `Greens` instead of `Vegetables` in the current English public copy.

## 4. Validator improvement

Updated:

```text
scripts/check_lang_diff.py
```

It still checks `zh_cn` / `en_us` key parity, and now also fails if current English language values contain:

```text
CJK characters
Ch.
Turbid / turbid
```

This prevents the exact class of issue from silently returning.

## 5. Validation

Completed:

```bash
python3 scripts/check_lang_diff.py
```

Result: all current language key sets match, and English quality checks pass.

Full build and release validator suite should be rerun after this localization change before final upload.
