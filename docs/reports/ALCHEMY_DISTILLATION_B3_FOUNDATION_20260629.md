# Alchemy distillation B3 foundation — 2026-06-29

## Scope

Implemented the B3 mixed-route distillation foundation requested after the A-round custom burden effects work.

B3 means:

```text
Use one visible/styled distilled essence container item, while source essence and distillation orientation are carried by a persistent component.
```

This avoids registering one new item per herb/source/orientation combination, while still giving players a concrete item to see, craft, tooltip, and use in future systems.

## What changed

### Distillation rule JSON

Added:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/distillation_rules.json
```

First-batch orientations:

```text
purified   catalyst minecraft:honeycomb       processing_style_hint purified
sustained  catalyst minecraft:redstone        processing_style_hint sustained
intense    catalyst minecraft:glowstone_dust  processing_style_hint intense
```

Reserved orientation:

```text
harmonized catalyst minecraft:amethyst_shard  implemented=false
```

### Distilled essence item and component

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistillationItems.java
alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistillationComponents.java
alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistilledEssenceComponent.java
```

Registered item:

```text
herbcraft:distilled_essence
```

Registered component:

```text
herbcraft_alchemy:distilled_essence
```

Component fields:

```text
source_essence
orientation
```

Example meaning:

```text
item: herbcraft:distilled_essence
component source_essence=dandelion, orientation=purified
```

### Custom distillation recipe foundation

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistillationRecipe.java
alchemy/src/main/java/com/herbcraft/alchemy/distillation/DistillationRecipes.java
```

Serializer:

```text
herbcraft:distillation
```

Recipe behavior:

```text
one Herbcraft essence + the orientation catalyst -> one componentized distilled essence
```

Added first recipe entries:

```text
data/herbcraft/recipe/distilled_essence_purified.json
data/herbcraft/recipe/distilled_essence_sustained.json
data/herbcraft/recipe/distilled_essence_intense.json
```

These recipes are generic: any current data-backed essence may be used as the source, and the output keeps that source in its component.

### Tooltip and API

Added:

```text
alchemy/src/main/java/com/herbcraft/alchemy/client/DistilledEssenceTooltips.java
alchemy/src/main/java/com/herbcraft/alchemy/AlchemyDistillationAPI.java
```

Tooltip shows:

```text
Distillation orientation
Source essence
Orientation hint
```

Read-only API queries:

```java
distilledEssence(ItemStack)
orientation(String)
orientation(ItemStack)
```

### Texture/style consistency

Added assets:

```text
assets/herbcraft/items/distilled_essence.json
assets/herbcraft/models/item/distilled_essence.json
assets/herbcraft/textures/item/distilled_essence.png
```

The texture is based on the existing essence-bottle visual language: same small bottle silhouette, same pixel-art scale, recolored pale distillate, with subtle sparkle highlights. This keeps it visually part of the current essence family rather than looking like a separate mod item.

### Creative tab / initialization

- `herbcraft:distilled_essence` is added to the Alchemy creative tab.
- Alchemy startup initializes distillation components, rules, item, and recipe serializer.
- Client startup registers distilled essence tooltip projection.

## What this does not implement yet

This B3 pass does not yet:

- route distilled essences into potion processing recipes;
- replace existing `_long` / `_strong` / `_clarified` recipe paths;
- add JEI/REI custom displays for distillation;
- add distillation block/station UI;
- add armor support using distilled essences;
- add processed weapon coating recipes using distilled essences;
- register per-source distilled essence items.

The point of B3 is to create a stable, non-explosive foundation: one item plus source/orientation component.

## Validation

Build:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
```

Result: PASS.

New validator:

```bash
python3 scripts/validate_distillation_foundation.py
```

Result: PASS.

`verify_built_jars.py` also passes and now checks distillation classes/resources/assets.

## Client-only checks

A real client should confirm:

1. `herbcraft:distilled_essence` appears in the Alchemy creative tab.
2. Crafting works:
   - any essence + honeycomb -> purified distilled essence;
   - any essence + redstone -> sustained distilled essence;
   - any essence + glowstone dust -> intense distilled essence.
3. Tooltip shows the source essence and orientation.
4. The item texture visually matches the existing essence-bottle family.
5. Existing essence, potion, and coating recipes remain unchanged.

## Next recommended board

Recommended next board is C:

```text
C — distillation routing into potion processing
```

That should decide how componentized distilled essences influence or unlock `sustained`, `intense`, and `purified` potion processing without breaking the current stable potion IDs.
