# Core / Alchemy 1.1.4 Version Bump — 2026-06-30

## Scope

This pass updates version metadata and release-facing documentation for the current Fabric line:

- Core `herbcraft`: `1.1.3` -> `1.1.4`
- Alchemy `herbcraft_alchemy`: `1.1.3` -> `1.1.4`
- Cuisine `herbcraft_cuisine`: remains `1.1.3`

The Cuisine module was intentionally not bumped because the requested scope was Core and Alchemy only.

## Metadata changes

- `build.gradle` now assigns:
  - `1.1.4` to Core and Alchemy;
  - `1.1.3` to Cuisine.
- Core `fabric.mod.json` version is now `1.1.4`.
- Alchemy `fabric.mod.json` version is now `1.1.4`.
- Alchemy now depends on Core `>=1.1.4`.
- Cuisine `fabric.mod.json` remains `1.1.3` and depends on Core `>=1.1.3`, so it can run with Core `1.1.4`.
- `scripts/verify_built_jars.py` now validates per-module versions instead of assuming one shared version string for all modules.

## Documentation changes

Current release-facing docs were updated for the partial bump:

- `README.md`
- `AI_ONBOARDING.md`
- `PROJECT_STATE.md`
- `CURRENT_BASELINE.md`
- `NEXT_TASK.md`
- `docs/DOCUMENTATION_INDEX.md`
- `docs/EXTENDING_HERBCRAFT.md`
- `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md`
- `docs/PUBLIC_EXTENSION_API_DESIGN.md`
- `docs/DATA_DRIVEN_ARCHITECTURE.md`
- `docs/AI_COLLABORATION_GUIDE.md`
- `docs/RELEASE_PAGE_COPY_1.1.4.md`
- `docs/MCMOD_RELEASE_COPY_1.1.4.md`
- `docs/JSON_EXTENSION_SPEC_1_1_4.md`
- `docs/ADDON_AUTHOR_QUICKSTART_1_1_4.md`

Historical 1.1.3 docs/reports were preserved as historical records rather than rewritten wholesale.

## Behavior boundary

This is a version/documentation/package pass only.

No intentional gameplay changes were made to:

- Java mechanics;
- JSON balance/rule tables;
- recipes;
- trades;
- loot injection contents;
- advancements;
- language strings;
- textures/models.

## Expected release output

`release_output/herbcraft_test_20260614_review/` should contain the refreshed current useful set:

- `herbcraft-1.1.4.jar`
- `herbcraft-alchemy-1.1.4.jar`
- `herbcraft-cuisine-1.1.3.jar`
- `herbcraft-source-current-20260614.zip`
- `herbcraft-p4-runtime-test-datapack-20260617.zip`

## Validation

Run after this report is written:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace
python3 scripts/check_lang_diff.py
python3 scripts/verify_built_jars.py
```

Because this pass changes jar metadata and docs only, targeted built-jar metadata verification is the key required validation. Broader gameplay validators are not expected to detect behavior changes from this pass.

## Client-only note

No new live-client behavior is introduced by the version bump itself. If these jars are used for client testing, confirm the mod list shows:

- Core `1.1.4`;
- Alchemy `1.1.4`;
- Cuisine `1.1.3`.
