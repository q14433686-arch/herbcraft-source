# Herbcraft / 百草可食

Herbcraft is a Fabric mod suite for Minecraft Java Edition `26.3`. (The 26.3 port is currently in validation — see `docs/reports/PORTING_26_3_FABRIC_20261013.md`.) It turns vanilla plants, odd natural materials, cuisine, alchemy, and knowledge discovery into a layered survival system.

**Author:** q14433686-arch · **Co-developer:** Arena.ai Agent

## Modules

This repository is a three-module Gradle project:

| Module | Mod ID | Current version | Role |
|---|---|---:|---|
| `core` | `herbcraft` | `1.1.4` | Edible herb definitions, raw eating effects, Herb Codex / 百草经, tattered pages, knowledge API, effect immunity, animal food tags, nutrition system, Ingredient Codex / 食材录. |
| `alchemy` | `herbcraft_alchemy` | `1.1.4` | Essences, herbal potions, clarified/murky potion lines, potion quality/bonus components, weapon coating, witch integration, catalyst brewing experiments. |
| `cuisine` | `herbcraft_cuisine` | `1.1.3` | Water bowl, curated dishes, raw-to-cooked medicinal dishes, Herbal Hodgepodge / 百草盅, Hundred-Flower Feast / 百花宴, Deadly Hodgepodge / 剧毒蛊. |

## Locked environment

- Minecraft `26.3`
- Fabric Loader `0.19.3`
- Fabric API `0.162.0+26.3`
- Java `25`
- Gradle Wrapper `9.6.0`
- Fabric Loom `1.17-SNAPSHOT`
- Mojang official mappings

## Design philosophy

1. **Eating herbs is gambling; brewing potions is contract; cuisine is craft.**
2. **Information is earned.** Unknown Codex entries, hidden recipes, Jade/tooltip hints, and recipe hints must not spoil full information too early.
3. **Data first, code second.** Core herb values live in `data/herbcraft/herbs.json`; raw herb probabilistic food restoration uses optional `food_rolls`; recipes, trades, tags, loot, guides, hints, pharmacology rules, and language should stay data-driven unless behavior truly needs Java.

## Optional compatibility

- **Jade**: optional knowledge-gated overlays for herbs/items/blocks.
- **Homeostatic / 稳态**: optional data compatibility is included. If Homeostatic is installed, selected Herbcraft Cuisine foods and selected Herbcraft-modified edible vanilla items affect Homeostatic hydration. Herbcraft does not hard-depend on Homeostatic.
- **Farmer's Delight Refabricated + More Delight**: optional built-in Core data compatibility is included in the current Core `1.1.4` line. If those mods are installed, their foods can enter 食材录/nutrition, and selected Farmer's Delight foods can enter 百草志/药食志 nature pharmacology. Reg's More Foods is intentionally excluded due unstable datapack-driven item identity.

## Data-driven extension docs

For maintainers and addon authors:

- `CURRENT_BASELINE.md` is the short current-state anchor.
- `docs/DOCUMENTATION_INDEX.md` maps current docs vs historical context so old reports do not mislead future maintainers.
- `docs/DATA_DRIVEN_ARCHITECTURE.md` explains which systems are JSON-backed and which IDs must remain stable.
- `docs/EXTENDING_HERBCRAFT.md` describes the current extension surface.
- `docs/JSON_EXTENSION_SPEC_1_1_4.md` is the formal JSON extension spec for supported datapack/addon paths.
- `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md` tracks the external extension roadmap and implementation history.
- `docs/examples/external_extension_compat_pack/` shows reload-safe JSON metadata paths.
- `docs/examples/herbcraft_readonly_event_addon/` shows read-only Java API and observational event usage.
- `docs/examples/startup_herb_food_extension_mod/` shows the startup-only addon herb food path.
- `docs/HOMEOSTATIC_COMPAT.md` documents Homeostatic compatibility.

## Development discipline

- Keep stable IDs stable. Do not rename existing item, potion, recipe, component, knowledge, or trade IDs without explicit author approval.
- Prefer vanilla/Fabric data systems: tags, recipes, loot events, data-driven villager trades.
- Do not use old `TradeOfferHelper`; MC 26.1 villager trading is data-driven.
- Do not replace vanilla loot tables. Use Fabric loot table modify events and guard with `source.isBuiltin()`.
- Do not add Mixins casually. Existing approved Mixins are for short-use projectiles, special bucket handling, effect immunity, potion trade matching, witch throws, and brewing stand catalyst experiments.
- Keep modules removable: `core` must run alone; `cuisine` and `alchemy` must not be required by core.

## Build

Use Java 25:

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

## Important current warning

The repository has recently suffered from source/release desynchronization. Before trusting any jar, run a clean build and inspect `herbcraft_alchemy.mixins.json` against actual class files. See `PROJECT_STATE.md` and `BUGS_TODO.md`.

## Release guide for the author

Upload release jars through **GitHub Releases**. Do **not** put release jars in the source repository. Local jar/source-zip staging may use `release_output/`, which is git-ignored.

For the next AI handoff, the author only needs to say:

> 请先读 AI_ONBOARDING.md
