# P3 Startup Herb Food Extension Smoke Test Report

Date: 2026-06-15  
Scope: P3 startup-only external herb food extension  
Command target: `:core:runServer`

## Implementation under test

P3 adds startup-only scanning for mod/addon resources matching:

```text
data/<namespace>/herbcraft/herbs/*.json
```

This is intentionally **not** a world datapack `/reload` feature. It is scanned during Herbcraft initialization from loaded Fabric mod containers before `DefaultItemComponentEvents.MODIFY` is registered, so entries can participate in default `FOOD` / `CONSUMABLE` component registration.

## Temporary smoke mod

A temporary mod jar was created under the run directory:

```text
core/run/mods/herbcraft_p3_smoke-1.0.0.jar
```

The jar contains:

```text
fabric.mod.json
data/smoke_food/herbcraft/herbs/glow_ink_sac.json
assets/smoke_food/lang/en_us.json
```

The test entry targets an item that is not part of the built-in Herbcraft herb list:

```text
minecraft:glow_ink_sac
```

Test herb food JSON:

```json
{
  "schema_version": 1,
  "item": "minecraft:glow_ink_sac",
  "nutrition": 1,
  "saturation_modifier": 0.1,
  "consume_seconds": 1.2,
  "medicinal": false,
  "stack_group": false,
  "effects": [
    { "effect": "minecraft:glowing", "chance": 1.0, "amplifier": 0, "duration_seconds": 8, "positive": true }
  ]
}
```

The temporary mod jar is under `core/run/` and is not part of release packaging.

## Command run

```bash
timeout 100s env JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon :core:runServer --args nogui
```

Exit code was `124` because `timeout` intentionally terminated the still-running dedicated server after startup evidence was collected.

## Successful evidence

Fabric loaded the temporary mod:

```text
- herbcraft_p3_smoke 1.0.0
```

Herbcraft loaded the external startup herb food entry:

```text
[main/INFO] (herbcraft) Loaded 96 herb definitions from herbs.json
[main/INFO] (herbcraft) Loaded 1 external startup herb food definitions; 97 total edible definitions
[main/INFO] (herbcraft) herbcraft loaded: registered 97 edible definitions and the Herbal Codex framework.
```

Server reached startup:

```text
[Server thread/INFO] (Minecraft) Done (0.663s)! For help, type "help"
```

The existing P2 external datapack also remained loaded during this smoke run and still merged successfully:

```text
[main/INFO] (herbcraft) Reloaded Herbcraft external data merge layer
```

## Errors/warnings observed

### Expected timeout exit

The command exited with `124` because the server was intentionally stopped by `timeout` after startup. This is expected for the smoke harness.

### Benign dev warning

Fabric emitted the existing untranslated item tag dev warning. It is unrelated to P3.

## Result

P3 startup herb food extension smoke result: **PASS**

The temporary external Fabric mod was loaded, Herbcraft discovered `data/smoke_food/herbcraft/herbs/glow_ink_sac.json` during startup, total edible definitions increased from 96 to 97 before the Herbal Codex framework registration completed, and the dedicated server reached `Done` without a crash.
