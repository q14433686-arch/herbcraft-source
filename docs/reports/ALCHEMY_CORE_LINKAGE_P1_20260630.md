# Alchemy/Core Linkage P1 2026-06-30

## Scope

This turn started the preferred four-round Alchemy/Core linkage track and completed **Round 1 / P1** from `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md`.

P1 target:

- upgrade processing styles from mostly numeric/route labels into explicit pharmacological semantics;
- keep the semantics JSON-owned and Java-projected;
- make guide/tooltip-facing projection explain pharmacological meaning rather than only stronger/longer/stable behavior.

## Source review result before implementation

Current source already contained the core P1 semantic structure in `data/herbcraft_alchemy/potion_processing_rules.json` and `PotionProcessingRules`:

- `thermal_shift`
- `flavor_bias`
- `trait_affinity`
- `burden_profile`
- `relation_modifier`
- `qi_bias`
- `clarity_level`
- corresponding localized hint keys

Current tooltip projection in `AdvancedPotionTooltips` already displayed these semantics.

The remaining P1 gap was not the JSON schema itself, but the **guide-facing projection** requirement from the plan: the Codex/guide side still focused on classic route explanations (essence / potion / clarified / murky / quality) and did not yet explicitly teach the pharmacological meaning of processing styles.

## Implemented this turn

### 1. Guide-facing P1 semantics projection

Updated `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java`:

- in the `potions` guide branch, added a new projected line once the player has entered potion processing (`first_potion` or related advanced-path progress);
- in the `quality_bonus` guide branch, added a new projected line explaining that processing style now carries pharmacological semantics beyond simple quality variance.

This keeps Java within the allowed architecture role:

- Java decides when guide text should appear and where it is projected;
- the actual semantic vocabulary and style meaning remain JSON/data/lang driven rather than hardcoded as gameplay tables.

### 2. New localized guide copy

Added matching language keys in both:

- `alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json`
- `alchemy/src/main/resources/assets/herbcraft/lang/en_us.json`

New keys:

- `book.herbcraft_alchemy.guide.potions.processing_semantics`
- `book.herbcraft_alchemy.guide.quality_bonus.processing_meaning`

These lines explain, at a guide level, that:

- `sustained` is about drawing out tail/momentum;
- `intense` is about sharpening and forcing a surge;
- `purified` is about filtering murk into steadier medicine;
- processing style now also projects thermal bias, qi movement, clarity level, and relation tendency.

### 3. Validator expansion

Extended `scripts/validate_potion_processing_rules.py` so P1 now also guards:

- `AlchemyChapter` projection of the new guide semantics;
- presence of the two new language keys in both languages.

## What was intentionally not changed

To keep P1 conservative and aligned with the plan boundary, this turn did **not** yet implement:

- source essence -> Core pharmacology projection metadata/runtime (P2);
- processed potion entry into Core medicinal accumulation/body-state systems (P3);
- seven-relation runtime participation (P4);
- catalyst semantic reinterpretation or herb-specific second-layer processing materials (P5).

## Validation

Executed:

```bash
JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks
python3 scripts/validate_potion_processing_rules.py
python3 scripts/check_lang_diff.py
python3 scripts/verify_built_jars.py
```

Results:

- Gradle build — PASS (`BUILD SUCCESSFUL`)
- `validate_potion_processing_rules.py` — PASS
- `check_lang_diff.py` — PASS
- `verify_built_jars.py` — PASS

## Release output refresh

Because this turn changed jar-facing source/lang/validator content, the local publishing output set was refreshed under:

```text
release_output/herbcraft_test_20260614_review/
```

Refreshed files:

- `herbcraft-1.1.3.jar`
- `herbcraft-alchemy-1.1.3.jar`
- `herbcraft-cuisine-1.1.3.jar`
- `herbcraft-source-current-20260614.zip`
- `herbcraft-p4-runtime-test-datapack-20260617.zip`

## Client-only checks still pending

The following remain source-level only and require real client confirmation later:

- whether the added guide text reads naturally in the in-game Codex;
- whether the new semantic explanation is sufficiently clear for players without over-spoiling later systems.

## Outcome

P1 is now complete for the current four-round track in the practical/source sense:

- semantic vocabulary is present in JSON;
- semantic tooltip projection exists;
- guide-facing projection now exists;
- validator coverage was extended accordingly.

The next planned stage is **P2: source essence -> Core pharmacology projection layer**.
