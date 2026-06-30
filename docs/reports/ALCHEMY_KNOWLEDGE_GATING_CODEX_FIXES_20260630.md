# Alchemy knowledge gating / Codex fixes

Date: 2026-06-30  
Scope: post Alchemy/Core linkage display, knowledge-gating, and 百草经/Codex design fixes requested by the author.

## Fixed

- Fixed processed-potion tooltip translation-key leakage for `性味偏向` and `特质亲和`:
  - flavor labels now use Core keys under `nature.herbcraft.flavor.*`;
  - trait labels now use Core keys under `nature.herbcraft.hint.*`;
  - the stale `tooltip.herbcraft.flavor.*` / `tooltip.herbcraft.trait.*` prefixes are no longer used.
- Added Alchemy client knowledge support so potion tooltips read the same ordinal knowledge state style as Core:
  - `0 UNKNOWN`, `1 ENCOUNTERED`, `2 HEARD`, `3 TASTED`, `4 MASTERED`.
- Gated processed-potion semantic tooltip details:
  - state `< 3`: no direct source/body/style semantic disclosure;
  - state `>= 3`: source essence identity may be shown as a tasted clue;
  - state `>= 4`: full style/source/body-state semantics may be shown, including thermal, flavor, trait, qi, clarity, relation, and body-state projections.
- Added shared server-side Alchemy knowledge helper for potion base-id mapping. `_long`, `_strong`, `_clarified`, and `_murky` all map to the base `alchemy/potion_<base>` entry.
- Adapted potion knowledge recognition to normal, splash, and lingering potion containers.
- Added tasted knowledge updates for:
  - drinking normal potions through `finishUsingItem`;
  - being affected by a thrown splash potion;
  - standing inside a lingering potion area cloud.
- Changed potion acquisition/scanning so owned Herbcraft potion stacks mark the Alchemy potion entry encountered + heard instead of only heard.
- Updated Alchemy potion Codex entries so potion entries no longer reuse essence detail rules blindly:
  - before tasting, the entry records that the bottle has been seen/heard but deeper pharmacology is not confirmed;
  - after actual `TASTED`/`MASTERED`, the entry can show the potion's effect lines and processing/body-state reminder.
- Added “炼金总述” lines explaining the new linkage update: source pharmacology preservation, processing/body-state/relation/material semantics, and potion taste gates.
- Added an Alchemy Codex guide entry for armor support / 护甲护持 and associated zh/en language keys.
- Gated distilled-essence and armor-support tooltip explanatory details behind mastered source-essence knowledge so those items do not leak deep semantics unconditionally.

## Files touched

- `alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/client/AlchemyKnowledgeClientSupport.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/client/ArmorSupportTooltips.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/client/DistilledEssenceTooltips.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyKnowledgeSupport.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyAcquisitionHooks.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/knowledge/AlchemyChapter.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/mixin/AlchemyItemMixin.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/mixin/ThrownSplashPotionKnowledgeMixin.java`
- `alchemy/src/main/java/com/herbcraft/alchemy/mixin/AreaEffectCloudKnowledgeMixin.java`
- `alchemy/src/main/resources/herbcraft_alchemy.mixins.json`
- `alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json`
- `alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json`
- `alchemy/src/main/resources/assets/herbcraft/lang/en_us.json`
- `scripts/validate_alchemy_knowledge_gating_and_codex.py`
- `scripts/verify_built_jars.py`

## Validation

- `chmod +x gradlew` — PASS.
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx768m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/validate_potion_body_state_projection.py` — PASS.
- `python3 scripts/validate_potion_relation_and_catalyst_semantics.py` — PASS.
- `python3 scripts/validate_alchemy_knowledge_gating_and_codex.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

## Output

Refreshed local release/test output under `release_output/herbcraft_test_20260614_review/`:

- `herbcraft-1.1.3.jar`
- `herbcraft-alchemy-1.1.3.jar`
- `herbcraft-cuisine-1.1.3.jar`
- `herbcraft-source-current-20260614.zip`
- `herbcraft-p4-runtime-test-datapack-20260617.zip`

## Client-only follow-up

Source-level checks cannot fully replace live-client UI confirmation. Please verify in-client:

- processed potion tooltips at unknown / encountered / heard / tasted / mastered stages;
- no raw untranslated `tooltip.herbcraft.flavor.*` or `tooltip.herbcraft.trait.*` keys appear;
- splash/lingering potion “亲尝” state updates when the player is actually affected;
- Alchemy Codex potion entries switch from seen/heard wording to tasted wording;
- the new armor-support guide page appears and reads correctly.

## Crash hotfix after client log

The author provided `RawOutput.log` showing a bootstrap crash:

```text
Mixin transformation of net.minecraft.world.entity.AreaEffectCloud failed
@Shadow field tickCount was not located in the target class net.minecraft.world.entity.AreaEffectCloud
```

Root cause: `AreaEffectCloudKnowledgeMixin` shadowed inherited `Entity.tickCount`; Mixin shadow lookup only located target-class fields in this context. The mixin now accesses the inherited public field through the casted `AreaEffectCloud self` instance (`self.tickCount`) and no longer declares `@Shadow public int tickCount`. The validator now checks that the inherited field is not shadowed.

Revalidation after the hotfix:

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/validate_potion_body_state_projection.py` — PASS.
- `python3 scripts/validate_potion_relation_and_catalyst_semantics.py` — PASS.
- `python3 scripts/validate_alchemy_knowledge_gating_and_codex.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

## Tooltip length / splash-lingering feature follow-up

After client review the author reported four remaining design problems: processed-potion tooltips were still too long, mastered potion Codex entries had too little explanation, `来源药材` still displayed a raw identifier, and splash/lingering potions were not participating in the new runtime features beyond vanilla potion effects.

Follow-up fixes:

- Moved verbose processed-potion semantic details out of the item tooltip and into the Alchemy Codex potion entry. Tooltips now keep only compact identity/status lines: type, processing style, localized source herb once known, a Codex hint, quality, and bonus effect if present.
- Localized `来源药材` in the tooltip and Codex through the source item / essence item display name instead of showing raw IDs such as `minecraft:dandelion`.
- Expanded mastered potion Codex entries with source nature and implemented processing-style details: source temperature/taste/traits, thermal shift, qi bias, clarity, relation modifier, flavor bias, and trait affinity.
- Added Codex text explicitly explaining that splash and lingering forms belong to the same potion lineage and should count for tasting, burden, body-state medicine, and relation experience.
- Extended splash-potion exposure to call `PotionBurdenRuntime.onPotionConsumed(player, stack)` for each affected player, so source pharmacology, body-state projection, relation participation, burden, armor support buffering, and extension events run for splash potions.
- Extended lingering-cloud exposure to synthesize a lingering potion stack from the cloud `PotionContents`, process each player once per cloud, and route that exposure through `PotionBurdenRuntime.onPotionConsumed(...)` as well as tasted knowledge.
- Updated validators so long tooltip semantics are expected in the Codex, not in `AdvancedPotionTooltips`, and so splash/lingering runtime participation is guarded.

Revalidation after this follow-up:

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/validate_potion_body_state_projection.py` — PASS.
- `python3 scripts/validate_potion_relation_and_catalyst_semantics.py` — PASS.
- `python3 scripts/validate_alchemy_knowledge_gating_and_codex.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.

## Codex readability / Core guide regression follow-up

After further client review the author reported: mastered potion pages were visually cluttered, Core overview guide pages `七情与配伍` and `五味与性味` had lost recent triggered examples, and `食材录与五养` / `残页与校勘` contained English/raw fallback text.

Fixes:

- Added a visible separator line before each implemented processing-style block in mastered Alchemy potion Codex entries.
- Repaired Core guide discovery rendering: `五味与性味` now reads the actual discovery keys recorded by `PharmacologyResolver` (`flavor_*`, `temperature_*`) instead of stale `same_qi` / `counter_qi` keys.
- Repaired `七情与配伍` guide examples to read actual recorded `seven_*` discovery keys and display latest current/previous item examples plus count text.
- Added a safe localized unknown-item fallback for guide examples.
- Added/overrode missing Core guide lang keys used by current Java (`ingredients.line2`, `ingredients.progress`, `errata.line2`, `errata.progress`) so `食材录与五养` and `残页与校勘` no longer show raw English translation keys in Chinese.
- Updated `validate_p1_codex_advancements.py` so the current Alchemy armor-support guide and repaired Core guide keys remain covered.

Revalidation:

- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' build --rerun-tasks --stacktrace` — PASS.
- `python3 scripts/validate_p1_codex_advancements.py` — PASS.
- `python3 scripts/validate_potion_processing_rules.py` — PASS.
- `python3 scripts/validate_potion_source_projection.py` — PASS.
- `python3 scripts/validate_potion_body_state_projection.py` — PASS.
- `python3 scripts/validate_potion_relation_and_catalyst_semantics.py` — PASS.
- `python3 scripts/validate_alchemy_knowledge_gating_and_codex.py` — PASS.
- `python3 scripts/check_lang_diff.py` — PASS.
- `python3 scripts/verify_built_jars.py` — PASS.
