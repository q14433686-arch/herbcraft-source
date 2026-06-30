# RUN_REPORT_2026-06-12

## Scope

User selected option B: run the current handoff source (`herbcraft-source-handoff-1.1.5-0.2.4-0.3.5.zip`) and verify the recent UI / hodgepodge work before packaging.

## Environment

- Minecraft: `26.1.2`
- Fabric Loader: `0.19.3`
- Fabric API: `0.151.0+26.1.2`
- Java: Microsoft OpenJDK `25.0.3`
- Gradle: wrapper `9.4.1`
- Fabric Loom: resolved `1.16.3`

## Build

Command:

```bash
JAVA_HOME=/home/user/jdks/jdk-25 PATH=/home/user/jdks/jdk-25/bin:$PATH ./gradlew clean build --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 1m 27s
```

## Static validation

Commands and results:

```bash
python3 scripts/check_lang_diff.py
# PASS: All zh_cn/en_us language key sets match exactly.

python3 scripts/validate_trade_tags.py
# PASS: All villager trade tag validation checks passed.

python3 scripts/validate_panel_hodge_recipe.py
# PASS: All panel translucency and hodgepodge recipe checks passed.

python3 scripts/validate_bugfix_advanced_potion_trade.py
# PASS: All advanced potion villager trade bugfix validation checks passed.
```

Note: `scripts/validate_fix_codex_hodgepodge.py` is mentioned in older docs but is not present in this handoff. The current panel/hodgepodge validation script supersedes the relevant checks.

## Jar verification

Added and ran:

```bash
python3 scripts/verify_built_jars.py
```

Result:

```text
VERIFY OK: all built jars contain expected metadata/resources/classes and Java 25 bytecode.
```

Manual jar content spot checks also passed:

- `herbcraft-1.1.5.jar` contains `data/herbcraft/tags/item/herb.json`.
- `herbcraft-cuisine-0.3.5.jar` contains `data/herbcraft_cuisine/recipe/hodgepodge.json`.
- `herbcraft-alchemy-0.2.4.jar` contains `herbcraft_alchemy.mixins.json` and `ItemCostAdvancedPotionMixin.class`.

## Server smoke tests

### Alchemy server

Command:

```bash
timeout 180 ./gradlew :alchemy:runServer --console=plain
```

Result:

- Fabric Loader loaded Minecraft `26.1.2` with Fabric Loader `0.19.3`.
- Loaded `herbcraft 1.1.5` and `herbcraft_alchemy 0.2.4`.
- Herbcraft initialized 95 herb definitions.
- Alchemy initialized successfully.
- Server reached:

```text
Done (9.443s)! For help, type "help"
```

The process was later stopped by the timeout, expected for a running server.

### Cuisine server

Command:

```bash
timeout 180 ./gradlew :cuisine:runServer --console=plain
```

Result:

- Fabric Loader loaded Minecraft `26.1.2` with Fabric Loader `0.19.3`.
- Loaded `herbcraft 1.1.5` and `herbcraft_cuisine 0.3.5`.
- Herbcraft initialized 95 herb definitions.
- Cuisine initialized 30 curated dishes and hodgepodge bowl.
- Server reached:

```text
Done (12.438s)! For help, type "help"
```

The process was later stopped by the timeout, expected for a running server.

The first-run `server.properties` missing message was observed before vanilla generated the file. It did not prevent reaching `Done`.

## Client smoke attempt

Command:

```bash
timeout 120 ./gradlew :cuisine:runClient --console=plain
```

Result:

- Client-side Fabric launch loaded `herbcraft 1.1.5` and `herbcraft_cuisine 0.3.5`.
- Both mods initialized before window creation.
- The client then crashed because the sandbox has no graphical display:

```text
Failed to initialize GLFW, errors: [GLFW 0x1000E] X11: The DISPLAY environment variable is missing
```

Conclusion: real interactive Codex UI clicking / GUI-scale verification cannot be completed in this headless sandbox. Static UI validation passed, and client mod initialization succeeded up to the expected environment limitation.

## Release artifacts

Artifacts copied to:

```text
/home/user/output_source_run/herbcraft-1.1.5.jar
/home/user/output_source_run/herbcraft-alchemy-0.2.4.jar
/home/user/output_source_run/herbcraft-cuisine-0.3.5.jar
/home/user/output_source_run/herbcraft-1.1.5-0.2.4-0.3.5-tested.zip
/home/user/output_source_run/SHA256SUMS.txt
```

## SHA256

```text
10050bb8c5938045fb64e11fa4783c17f71740215a8d744c2957534849c07df9  herbcraft-1.1.5.jar
9340183a6d59e421744d144715492a7469e60dd5028934877519ca2bfd087092  herbcraft-alchemy-0.2.4.jar
f906c26c5d126d09fa99f2e013cef31dbe20bf2322d6abb33dcd8d731a3b78a5  herbcraft-cuisine-0.3.5.jar
```

## Verdict

- Build: PASS
- Static validation: PASS
- Jar verification: PASS
- Alchemy server smoke: PASS
- Cuisine server smoke: PASS
- Client interactive UI verification: BLOCKED by missing DISPLAY in sandbox, but client mod initialization before GLFW was successful.

No code changes were required for this run, except adding `scripts/verify_built_jars.py` and this report.
