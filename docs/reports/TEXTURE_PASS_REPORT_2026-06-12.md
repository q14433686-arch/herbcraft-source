# TEXTURE_PASS_REPORT_2026-06-12

## Scope

Implemented the custom item texture pass requested by the author, using the attached requirements:

- Essence bottles must use the vanilla potion/glass bottle silhouette.
- Bowl foods must use the vanilla bowl silhouette.
- Only content/liquid pixels may change.
- Avoid missing model/texture references.
- Avoid making every custom item visually identical to vanilla, especially rare / special items.

## Implementation

Added scripts:

```text
scripts/generate_item_textures.py
scripts/validate_item_textures.py
```

Generated textures:

| Module | Count | Notes |
|---|---:|---|
| core | 2 | `herbal_codex`, `tattered_page`; vanilla book/paper-like, with subtle herbal marks. |
| alchemy | 43 | 40 essence bottles + 3 adhesive items. Essence bottles preserve vanilla potion bottle pixels. |
| cuisine | 52 | All dish/raw dish/hodgepodge/water bowl textures. Bowl body pixels are vanilla bowl outside the content mask. |

Total custom item textures generated: `97`.

## Texture rules enforced

`validate_item_textures.py` verifies:

- All item model `layer0` texture references resolve to an actual file.
- All 97 custom item definitions point to existing models.
- Cuisine bowl textures are exactly equal to vanilla `bowl.png` outside the approved bowl-mouth content mask.
- Essence bottle textures are exactly equal to vanilla `potion.png` on all vanilla bottle pixels.
- Essence textures only add liquid pixels in vanilla `potion_overlay.png` liquid area, preventing stray outline pixels.

Result:

```text
PASS - custom item texture references are complete; cuisine bowls preserve vanilla bowl pixels outside content; essence bottles preserve vanilla bottle pixels and use only vanilla liquid overlay area.
```

## Build and validation

Full build:

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew clean build --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 2m 21s
```

Validation commands:

```bash
python3 scripts/check_lang_diff.py
python3 scripts/validate_trade_tags.py
python3 scripts/validate_panel_hodge_recipe.py
python3 scripts/validate_bugfix_advanced_potion_trade.py
python3 scripts/validate_item_textures.py
python3 scripts/verify_built_jars.py
```

All passed.

Jar texture counts:

```text
core/build/libs/herbcraft-1.1.5.jar                  2 texture files
alchemy/build/libs/herbcraft-alchemy-0.2.4.jar       43 texture files
cuisine/build/libs/herbcraft-cuisine-0.3.5.jar       52 texture files
```

## Artifacts

Prepared release artifacts in:

```text
/home/user/output_texture_run/
```

Files:

```text
herbcraft-1.1.5.jar
herbcraft-alchemy-0.2.4.jar
herbcraft-cuisine-0.3.5.jar
herbcraft-1.1.5-0.2.4-0.3.5-textured-tested.zip
SHA256SUMS.txt
```

## Notes

- No text-to-image AI was used.
- Vanilla client jar templates were extracted from Minecraft `26.1.2`.
- Essence bottle container pixels are inherited from vanilla `potion.png`.
- Cuisine bowl container pixels are inherited from vanilla `bowl.png`.
- Rare/special items are differentiated only through liquid/content colors and tiny in-mask accents, not by changing containers.
- Client interactive UI/model viewing is still blocked in this sandbox due missing graphical `DISPLAY`, but static asset reference validation specifically guards against missing model/texture files.
