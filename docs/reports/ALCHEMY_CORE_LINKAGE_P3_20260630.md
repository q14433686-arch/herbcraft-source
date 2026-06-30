# ALCHEMY_CORE_LINKAGE_P3_20260630

Status: implemented source-level Round 3 / P3 for the preferred four-round Alchemy/Core linkage track.
Date: 2026-06-30

## Scope

This round implements the conservative first-pass body-state / medicinal accumulation integration required by `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md` P3.

User priority remained unchanged during this round:

- continue the Alchemy/Core linkage rounds first
- do not switch to the separately reported display bugs / knowledge gating / 百草经 issues yet

## Goal restatement

P3 requires processed potion consumption to enter Herbcraft Core-side pharmacology/body-state surfaces rather than remaining only a local Alchemy buff route.

Required conservative first-pass outcomes:

- medicinal accumulation / 药性累计
- thermal tendency
- qi/body-state tendency
- typed burden/body-state profile rather than only scalar burden weight

## Design chosen

A stack-level persisted/network-synced body-state projection layer was added, then consumption-time application was wired into the existing processed-potion burden consume path.

This keeps the implementation conservative and source-verifiable:

- Java owns runtime projection, persistence, sync, consumption integration, and tooltip projection
- existing Core `NatureProfile` and `PlayerPharmacologyState` remain the body-state owner surfaces
- no new large content table was hardcoded into JSON-less random branches
- first-pass typed profile logic is intentionally compact and derivation-based rather than pretending a full new medicine simulation already exists

## Implemented changes

### 1. New persisted/network-synced body-state projection component

Added:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBodyStateProjection.java`

This component stores a processed potion's conservative Core-facing body-state projection:

- `medicinal_profile`
- `medicinal_weight`
- `thermal_tendency`
- `thermal_value`
- `qi_tendency`
- `qi_value`
- `body_traits`

It includes both `CODEC` and `STREAM_CODEC`.

### 2. Registered Alchemy potion component

Changed:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/AlchemyPotionComponents.java`

Added registered data component:

- `herbcraft_alchemy:potion_body_state`
- Java symbol: `AlchemyPotionComponents.POTION_BODY_STATE`

This makes the projection persisted and network-synced like the P2 source projection.

### 3. Runtime projection builder

Added:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBodyStateProjectionRuntime.java`

Behavior:

- reads processed-potion style from the existing processing-style path
- reads source pharmacology from P2 `PotionPharmacologyProjectionRuntime`
- derives conservative body-state projection
- persists it to `POTION_BODY_STATE`

Current derived outputs:

- `thermal_value`
  - source temperature via Core `PharmacologyRules.current().temperatureValue(...)`
  - plus style thermal shift bias
- `qi_value`
  - source flavor/trait tendency
  - plus style qi bias
- `medicinal_profile`
  - one of:
    - `warming`
    - `chilling`
    - `scattering`
    - `stagnating`
    - `muddy`
    - `overdriven`
    - fallback `steady`

This fulfills the P3 plan requirement that burden/body-state projection move toward typed profiles rather than staying only scalar.

### 4. Processed potion initialization now applies P3 projection metadata

Changed:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/AdvancedPotionStackInitializer.java`

Added:

- `PotionBodyStateProjectionRuntime.applyMetadata(stack);`

So processed potion stacks now initialize with:

- P2 source pharmacology projection
- P3 body-state projection

### 5. Consumption-time application into Core player pharmacology state

Added:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBodyStateConsumptionRuntime.java`

Changed:

- `alchemy/src/main/java/com/herbcraft/alchemy/potion/PotionBurdenRuntime.java`

Behavior on processed potion consumption:

- resolve P2 source projection
- resolve P3 body-state projection
- write recent-use entry into Core `PlayerPharmacologyState`
  - uses source essence id as intake identity
  - uses source `NatureProfile` semantics for the recent-use profile
- shift player Core `qiBalance`
  - by `thermalValue + qiValue`

This is intentionally conservative:

- it does not yet replace `PharmacologyResolver` food logic
- it does not yet try to simulate every advanced medicinal interaction
- but processed potion use now enters Core-side recent body-state and qi tendency state instead of staying entirely Alchemy-local

### 6. Read-only API extension

Changed:

- `alchemy/src/main/java/com/herbcraft/alchemy/AlchemyProcessingAPI.java`

Added:

- `potionBodyState(ItemStack stack)`

So future P4/P5 work and validators can query the persisted P3 projection directly.

### 7. Tooltip projection

Changed:

- `alchemy/src/main/java/com/herbcraft/alchemy/client/AdvancedPotionTooltips.java`
- `alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json`
- `alchemy/src/main/resources/assets/herbcraft/lang/en_us.json`

New tooltip lines project the P3 body-state layer:

- medicinal/body-state profile
- medicinal accumulation weight
- body thermal tendency + numeric tendency value
- body qi tendency + numeric tendency value

New language keys include:

- `tooltip.herbcraft.potion.medicinal_profile`
- `tooltip.herbcraft.potion.medicinal_weight`
- `tooltip.herbcraft.potion.body_thermal`
- `tooltip.herbcraft.potion.body_qi`
- typed profile/tendency keys for warming/chilling/scattering/stagnating/muddy/overdriven/steady

This makes processed potion intake read more like medicine and less like a pure isolated buff object.

### 8. New validator and jar verification extension

Added:

- `scripts/validate_potion_body_state_projection.py`

Changed:

- `scripts/verify_built_jars.py`

Validator coverage now checks:

- registered `POTION_BODY_STATE`
- `potion_body_state` id
- `PotionBodyStateProjection` fields + codecs
- projection runtime tokens
- consumption runtime hook into Core pharmacology state
- `AdvancedPotionStackInitializer` metadata application
- `AlchemyProcessingAPI.potionBodyState(...)`
- tooltip/lang projection keys
- built jar class presence

## Validation run

Executed this round:

- `chmod +x gradlew` — PASS
- `JAVA_HOME=/tmp/herbcraft-jdk25/jdk-25 ./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1g -Dfile.encoding=UTF-8' build --rerun-tasks` — PASS
- `python3 scripts/validate_potion_processing_rules.py` — PASS
- `python3 scripts/validate_potion_source_projection.py` — PASS
- `python3 scripts/validate_potion_body_state_projection.py` — PASS
- `python3 scripts/check_lang_diff.py` — PASS
- `python3 scripts/verify_built_jars.py` — PASS

## Output refresh

Because source/resources/validators/jar contents changed, local publishing artifacts were refreshed under:

- `release_output/herbcraft_test_20260614_review/`

This round requires rebuilt jars and a refreshed source archive.

## Remaining risks / deferred work

1. This P3 implementation is conservative and source-level complete, but client feel still needs confirmation.
2. Body-state typed profiles are currently derived from compact Java logic rather than a larger JSON-owned projection table. This is acceptable for the conservative first pass, but if P4/P5 needs finer semantic control, a later JSON-backed projection map may be preferable.
3. Processed potion intake now enters Core recent-use and qi state, but it does not yet trigger the full seven-relation resolution pass by itself. That belongs to P4.
4. The existing P2 UX risk remains:
   - source herb in tooltip still shows raw source herb ID rather than localized name
5. Client-only verification is still required for:
   - tooltip readability
   - processed potion repeat-drink feel
   - body-state tendency messaging/legibility in real play
   - interaction feel alongside existing burden stages

## Conclusion

Round 3 / P3 is now complete at source level.

Processed potions now:

- preserve source identity from P2
- persist a conservative body-state projection
- show medicinal/body-state projection in tooltips
- enter Core player pharmacology recent-use + qi state on consumption

The next authoritative stage remains:

- Round 4 = P4 seven-relation integration layer + P5 catalyst/material semantics upgrade layer
