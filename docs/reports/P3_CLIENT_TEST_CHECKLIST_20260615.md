# P3 Client Test Checklist: Startup Food Extension Addon

Date: 2026-06-15  
Test addon jar produced from: `docs/examples/startup_herb_food_extension_mod/`  
Addon jar output path:

```text
release_output/herbcraft_test_20260614_review/herbcraft-p3-startup-food-test-addon-1.0.0.jar
```

This is a **test addon**, not a normal Herbcraft release jar and not something to upload as part of the public mod release.

## Purpose

This checklist verifies that P3 works in a real client:

```text
data/<namespace>/herbcraft/herbs/*.json
```

The test addon makes this vanilla item Herbcraft-edible at startup:

```text
minecraft:glow_ink_sac
```

It also includes companion P2 metadata for the same item:

```text
data/example_food/herbcraft/herb_natures/glow_ink_sac.json
data/example_food/herbcraft/nutrition_profiles/glow_ink_sac.json
data/example_food/herbcraft/knowledge_rumors/glow_ink_sac.json
```

## Install files for the client test

Put these jars into the client `mods/` folder:

```text
herbcraft-1.1.1.jar
herbcraft-alchemy-1.1.1.jar              # optional for this specific P3 test, but use it if testing full suite
herbcraft-cuisine-1.1.1.jar              # optional for this specific P3 test, but use it if testing full suite
herbcraft-p3-startup-food-test-addon-1.0.0.jar
fabric-api-0.151.0+26.1.2.jar
```

Optional but useful:

```text
Jade 26.1.0+fabric
Homeostatic 26.1 branch/build if you also want to ensure no interference
```

## Expected startup log evidence

In `latest.log`, search for:

```text
example_food 1.0.0
Loaded 1 external startup herb food definitions; 97 total edible definitions
Reloaded Herbcraft external data merge layer
```

If these lines are absent, the addon probably was not loaded or the P3 resource path was not scanned.

## Test table

| # | Area | Steps | Expected result | Result |
|---:|---|---|---|---|
| 1 | Startup | Launch client with the test addon jar installed. Open `latest.log`. | `example_food 1.0.0` appears in the mod list. |  |
| 2 | P3 scan | Search `latest.log` for `external startup herb food`. | Log contains `Loaded 1 external startup herb food definitions; 97 total edible definitions`. |  |
| 3 | P2 companion data | Search `latest.log` for `Reloaded Herbcraft external data merge layer`. | P2 reload layer still runs with the addon installed. |  |
| 4 | Obtain item | In a test world, run `/give @p minecraft:glow_ink_sac 16`. | Glow Ink Sac is obtained normally. |  |
| 5 | Edibility at full hunger | Set survival/adventure as needed and right-click/eat a Glow Ink Sac while hunger is full. | It should be edible because the test JSON uses `medicinal: true`. Eating should take about `1.2s`. |  |
| 6 | Consume effect | After eating one Glow Ink Sac, check active effects. | Player receives `minecraft:glowing` for about `8s`. |  |
| 7 | Stack consumption | Count the item stack before and after eating. | Stack decreases by 1 unless creative-mode behavior prevents consumption. |  |
| 8 | Codex item | Run `/give @p herbcraft:herbal_codex 1` and open the Codex. | No crash. `Glow Ink Sac` should have a Herbcraft herbal entry because P3 added it before chapter registration. |  |
| 9 | Knowledge progression | Pick up and then eat Glow Ink Sac; reopen Codex. | Entry should progress at least through encountered/tasted-style states, consistent with other herbs. |  |
| 10 | P2 nature display | Check Codex/Jade/tooltip after knowledge unlocks. | If server/client sync covers this path, nature should reflect the addon data: `cool`, `salty/bland`, traits `aquatic/lunar/curio`. If not visible in client-only tooltip, record it as a sync/UI follow-up. |  |
| 11 | Nutrition profile | If commands are enabled, compare `/herbcraft nutrition get @p` before/after eating. | Internal `fruit` / displayed `Fluids` / `津液` should increase slightly from the addon profile (`fruit: 4`) if the external profile is active. |  |
| 12 | Hodgepodge/tag boundary | Try using Glow Ink Sac in hodgepodge only if Cuisine is installed. | It should **not** automatically be accepted as a hodgepodge herb unless an addon also provides relevant item tags. P3 does not auto-edit tags. |  |
| 13 | Regression scan | Play for a few minutes with normal Herbcraft items. | Existing herbs, Codex, nutrition, Cuisine, and Alchemy should behave as before. |  |

## What to report back

Please report:

```text
1. Did the client launch? yes/no
2. Did latest.log show example_food 1.0.0? yes/no
3. Did latest.log show 97 total edible definitions? yes/no
4. Could Glow Ink Sac be eaten at full hunger? yes/no
5. Did it give Glowing for ~8s? yes/no
6. Did Codex/Jade/tooltip show anything wrong or crash? details
7. Did nutrition command show Fluids/fruit change? yes/no/not tested
8. Did any existing Herbcraft feature regress? details
```

## Important boundary reminder

This test validates startup addon behavior. It does **not** validate world datapack `/reload` changing FOOD/CONSUMABLE components. P3 is intentionally startup-only.
