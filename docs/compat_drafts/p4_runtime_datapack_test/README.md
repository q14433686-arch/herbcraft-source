# Herbcraft P4 Runtime Test Datapack

This is a small runtime datapack for testing Herbcraft `1.1.3` reload-safe external data and food-nature integration.

Zip to use in a world `datapacks/` folder:

```text
release_output/herbcraft_test_20260614_review/herbcraft-p4-runtime-test-datapack-20260617.zip
```

What it adds to vanilla foods:

```text
minecraft:honey_bottle: nutrition + 食材录 + herb_natures + custom special_counter
minecraft:bread: nutrition + 食材录 + herb_natures
minecraft:baked_potato: nutrition + 食材录 + herb_natures
```

Suggested tests:

1. Install the datapack, run `/reload`, then eat/drink `minecraft:honey_bottle`.
2. Confirm honey bottle enters 食材录 and 药食志 after knowledge sync.
3. Drink two honey bottles inside 60 seconds; the custom external counter should apply `minecraft:glowing` for 5 seconds.
4. Eat `minecraft:bread` then `minecraft:baked_potato` close together; they share `nourishing + earthy`, so they should be able to discover 相须.
5. Hover/drop these items and check tooltip/Jade after sync.

Boundary reminder:

This datapack does not make non-food items edible. It only attaches Herbcraft metadata to already edible vanilla items.
