# ALCHEMY_CORE_LINKAGE_P4_P5_20260630

Status: implemented source-level Round 4 / P4 + P5 for the preferred four-round Alchemy/Core linkage track.
Date: 2026-06-30

## Scope

This round completes the remaining two stages in `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md`:

- P4 — Seven-relation integration layer
- P5 — Processing material / catalyst semantics upgrade layer

User priority remained unchanged during this round:

- continue and finish the current Alchemy/Core linkage rounds first
- do not switch to the separately reported display bugs / knowledge gating / 百草经 issues yet

## P4 goal restatement

Processed potions should participate in Herbcraft’s recent-intake / seven-relation context instead of staying isolated from Core pharmacology relation logic.

## P4 implementation

### New runtime

Added:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionRelationRuntime.java`

This is a conservative first-pass processed-potion relation bridge.

### Behavior

On processed potion consumption, before burden is applied:

- read the P2 source pharmacology projection
- read the P3 body-state projection
- read the processing style semantics
- derive a relation-facing `NatureProfile`
  - source temperature/flavor base is preserved
  - style trait affinity is included
  - P3 body-state traits are included
- compare the current processed potion against the latest Core recent-use entry
- derive a conservative seven-relation outcome influenced by style semantics

### Current conservative relation shaping

Examples of the current source-level outcomes:

- `conflict_amplifying` styles can escalate hot/cold conflict toward `XIANG_FAN`
- `conflict_buffering` styles can push clearing-vs-toxic interactions toward `XIANG_SHA`
- `compatibility_weaving` styles can favor `XIANG_SHI`
- `volatile_conflict` styles can contribute to `XIANG_E`
- high shared-trait continuity can produce `XIANG_XU` or `XIANG_SHI`
- no shared context still conservatively falls back to `SINGLE`

### Player-state effects

`PotionRelationRuntime` currently performs conservative but visible state effects:

- records the corresponding pharmacology discovery key
- may reset or shift `qiBalance`
- may grant temporary toxic-protection timing via the existing Core state field
- sends the existing Core overlay relation message through `SevenEmotionType.messageKey()`

This keeps P4 visible and meaningful without pretending the potion path is already a fully separate second pharmacology simulator.

### Hook location

Changed:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenRuntime.java`

Processed potion consumption order now includes:

1. `PotionRelationRuntime.onPotionConsumed(...)`
2. `PotionBodyStateConsumptionRuntime.onPotionConsumed(...)`
3. Alchemy burden application/runtime

This makes processed potions true recent-intake participants before the Alchemy-local burden storage is applied.

## P5 goal restatement

Catalysts should no longer read as merely vanilla modifier materials; they should project Herbcraft-specific processing semantics.

## P5-A implementation: catalyst semantics upgrade

### Distillation tooltip semantics updated

Changed:

- `alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json`
- `alchemy/src/main/resources/assets/herbcraft/lang/en_us.json`

Updated distillation orientation hint text so catalysts now project explicit pharmacological/process meaning:

- honeycomb = 澄滤 / 驯浊 / clarifies and tames murk
- redstone = 引尾 / 续势 / draws out the tail and carries medicine onward
- glowstone = 聚锋 / 峻化 / sharpens the edge and intensifies the surge
- amethyst = 调承 / 调和 / steadies the bearing for later co-processing harmony

This reinterprets existing catalyst behavior in Herbcraft language without changing stable IDs or item routes.

### Guide / Codex teaching layer updated

Changed:

- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java`
- `alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json`
- `alchemy/src/main/resources/assets/herbcraft/lang/en_us.json`

Added new guide-facing projection lines:

- `book.herbcraft_alchemy.guide.potions.relation_integration`
- `book.herbcraft_alchemy.guide.quality_bonus.catalyst_semantics`

This means the Alchemy guide now explicitly teaches:

- processed potions now carry recent medicinal momentum into relation context
- catalysts are not just materials, but processing semantics / 炮制语义

## P5-B implementation status

This round does **not** yet add a second playable new processing-aid item family such as honeyed / wine / salt / ginger / ash / dew auxiliary material content.

What this round does complete for P5-B preparation:

- the catalyst language/guide/tooltips now frame processing materials as semantic pharmacology/process carriers rather than generic vanilla modifiers
- this gives a stable conceptual base for later auxiliary-material expansion without needing to rewrite existing orientation language again

Given the user’s instruction to finish these rounds first and avoid side-track redesign, this controlled semantic-completion scope is the safest finishing move for Round 4.

## Validators added / extended

Added:

- `scripts/validate_potion_relation_and_catalyst_semantics.py`

Validation coverage now checks:

- `PotionRelationRuntime` exists and contains seven-relation integration tokens
- processed potion consumption hooks `PotionRelationRuntime`
- `AlchemyChapter` includes the new guide-projection keys
- zh/en lang contain the new guide lines
- zh/en lang contain upgraded catalyst orientation hint keys
- `distillation_rules.json` still binds the expected catalysts
- built jar contains `PotionRelationRuntime.class`

Also extended:

- `scripts/verify_built_jars.py`

## Validation run

Executed this round:

- `chmod +x gradlew` — PASS
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew :alchemy:compileJava --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' --stacktrace` — PASS
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS
- `python3 scripts/validate_potion_processing_rules.py` — PASS
- `python3 scripts/validate_potion_source_projection.py` — PASS
- `python3 scripts/validate_potion_body_state_projection.py` — PASS
- `python3 scripts/validate_potion_relation_and_catalyst_semantics.py` — PASS
- `python3 scripts/check_lang_diff.py` — PASS
- `python3 scripts/verify_built_jars.py` — PASS

## Output refresh

Because source/resources/validators/jar contents changed, local publishing artifacts were refreshed under:

- `release_output/herbcraft_test_20260614_review/`

This round requires rebuilt jars and a refreshed source archive.

## Remaining risks / deferred work

1. P4 is a conservative integration pass, not a full second copy of `PharmacologyResolver` for potion effects.
2. The relation bridge is intentionally compact and source-verifiable; later tuning may move more of this logic into JSON-backed mapping if a larger relation-shaping table becomes necessary.
3. P5 currently completes the semantic upgrade layer and guide/tooltips wording, but not yet a brand-new auxiliary-material content family.
4. The existing P2 UX risk remains:
   - source herb in tooltip still shows raw source herb ID rather than localized name
5. Client-only verification is still required for:
   - relation overlays feeling appropriate during repeated processed-potion use
   - whether the new catalyst semantics wording reads clearly in actual UI
   - tooltip density / readability
   - broader A-E alchemy interaction feel in live play

## Conclusion

Round 4 is now complete at source level.

The preferred four-round Alchemy/Core linkage track is now finished through:

- P1 processing semantics
- P2 source pharmacology projection
- P3 body-state / medicinal accumulation integration
- P4 seven-relation integration
- P5 catalyst/material semantics upgrade

The user’s deferred follow-up issues remain intentionally untouched for the next phase:

- display bugs
- knowledge gating problems
- 百草经-related design problems
