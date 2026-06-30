# Herbcraft 1.1.4 Release Page Copy

Status: current package notes for the partial version bump line.

## Current files

- Core / `herbcraft`: `herbcraft-1.1.4.jar`
- Alchemy / `herbcraft_alchemy`: `herbcraft-alchemy-1.1.4.jar`
- Cuisine / `herbcraft_cuisine`: `herbcraft-cuisine-1.1.3.jar`

## Dependencies

- Core requires Minecraft `26.1.2`, Fabric Loader `0.19.3+`, Fabric API `0.151.0+26.1.2+`, Java `25+`.
- Alchemy `1.1.4` requires Herbcraft Core `>=1.1.4`.
- Cuisine remains `1.1.3` and requires Herbcraft Core `>=1.1.3`; it is compatible with Core `1.1.4`.

## Version changelog: 1.1.4

- Bumped Herbcraft Core from `1.1.3` to `1.1.4`.
- Bumped Herbcraft Alchemy from `1.1.3` to `1.1.4`.
- Updated Alchemy's required Core dependency to `>=1.1.4`.
- Cuisine was intentionally not bumped in this pass and remains `1.1.3`.
- No gameplay, JSON balance, recipe, loot, advancement, language, or texture behavior was intentionally changed by this version-only pass.

## Packaging note

Use the refreshed files from `release_output/herbcraft_test_20260614_review/`. The source archive is still named `herbcraft-source-current-20260614.zip` by project convention.
