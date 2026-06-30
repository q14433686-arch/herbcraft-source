# Example Startup-Only Herbcraft Food Extension Mod

This example demonstrates the P3 startup-only resource path:

```text
data/<namespace>/herbcraft/herbs/*.json
```

Unlike the P2 server datapack merge layer, this path is intended for loaded Fabric mod/addon resources so Herbcraft can read it during initialization before default item food components are applied.

It is not promised as a world datapack `/reload` feature.

## Test entry

This example makes the vanilla item below Herbcraft-edible:

```text
minecraft:glow_ink_sac
```

It is intentionally marked `medicinal: true` so it can be eaten even at full hunger during client testing.

The example also includes companion P2 metadata:

```text
data/example_food/herbcraft/herb_natures/glow_ink_sac.json
data/example_food/herbcraft/nutrition_profiles/glow_ink_sac.json
data/example_food/herbcraft/knowledge_rumors/glow_ink_sac.json
```

These companion files help test Codex/Jade/tooltip/nutrition behavior together with P3 edibility.
