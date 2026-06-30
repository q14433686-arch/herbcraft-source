# P2 External Datapack Smoke Test Report

Date: 2026-06-15  
Scope: P2 low-risk external server-data merge layer  
Command target: `:core:runServer`  
Reason for Core-only target: P2 merge layer currently lives in Core systems (`herb_natures`, `nutrition_profiles`, `special_counters`, `knowledge_rumors`, `codex_loot_injections`). Root `runServer` attempts multiple subproject server runs and is not the right smoke target.

## Test datapack

Temporary datapack path used for smoke testing:

```text
core/run/world/datapacks/herbcraft_p2_external_smoke/
```

The datapack is under `run/` and is not part of release packaging.

Files included:

```text
pack.mcmeta
data/smoke_ext/herbcraft/herb_natures/allium_override.json
data/smoke_ext/herbcraft/nutrition_profiles/honey_bottle_override.json
data/smoke_ext/herbcraft/special_counters/honey_smoke_counter.json
data/smoke_ext/herbcraft/knowledge_rumors/allium_smoke_claim.json
data/smoke_ext/herbcraft/codex_loot_injections/village_temple_smoke.json
assets/smoke_ext/lang/en_us.json
assets/smoke_ext/lang/zh_cn.json
```

The smoke pack intentionally uses vanilla item IDs:

```text
minecraft:allium
minecraft:honey_bottle
```

so no external mod dependency is required.

## Commands run

Initial root-level attempt:

```bash
timeout 180s env JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew runServer --args nogui
```

Result: not used as final smoke evidence. Root `runServer` tried multiple subproject server tasks and the Gradle daemon disappeared after Fabric startup logs. This is treated as a task-selection issue, not a P2 runtime result.

Final Core-only smoke command:

```bash
timeout 90s env JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon :core:runServer --args nogui
```

Exit code was `124` because `timeout` intentionally terminated the still-running dedicated server after startup evidence was collected.

## Successful evidence

Final run reached server startup:

```text
[Server thread/INFO] (Minecraft) Done (0.487s)! For help, type "help"
```

P2 external datapack resources were discovered and merged:

```text
[main/WARN] (herbcraft) External herb nature smoke_ext:herbcraft/herb_natures/allium_override.json overrides existing nature profile for minecraft:allium
[main/WARN] (herbcraft) External nutrition profile smoke_ext:herbcraft/nutrition_profiles/honey_bottle_override.json overrides existing profile for minecraft:honey_bottle
[main/INFO] (herbcraft) Loaded 6 total special counter rules after external data merge
[main/INFO] (herbcraft) Reloaded Herbcraft external data merge layer
```

The intentional override warnings confirm conflict logging works for item-keyed P2 data.

## Errors/warnings observed

### Resolved pack metadata warning

A first datapack run used a too-new `pack_format` and produced:

```text
Pack declares support for version newer than 81, but is missing mandatory fields min_format and max_format
```

The smoke pack was then changed to:

```json
{
  "pack": {
    "pack_format": 81,
    "description": "Herbcraft P2 external data smoke pack"
  }
}
```

The final run no longer produced that metadata warning.

### Expected timeout exit

The final command exited with `124` due to the deliberate `timeout`. The server had already reached `Done`, so this is a successful smoke result.

### Benign dev warning

Fabric emitted an untranslated item tag dev warning after startup. This is unrelated to the P2 external merge layer.

## Result

P2 server smoke test result: **PASS**

The external datapack loaded, Herbcraft merged all tested P2 paths, conflict warnings appeared where expected, and the dedicated server reached `Done` without a crash.
