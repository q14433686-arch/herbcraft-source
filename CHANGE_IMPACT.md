# CHANGE_IMPACT

Use this checklist whenever `core/src/main/resources/data/herbcraft/herbs.json` changes.

## Raw eating effects

- [ ] Stable `nutrition` and `saturation_modifier` are intentional.
- [ ] Optional `food_rolls.nutrition` / `food_rolls.saturation` values are intentional; visible hunger rolls should usually be small and hidden saturation rolls should be clamped/validator-backed.
- [ ] If the item is juicy or drying and Homeostatic compat exists, matching `environment/drinkable` data is still correct.
- [ ] `medicinal` / always edible behavior is intentional.
- [ ] `consume_seconds` matches item feel.
- [ ] Effects use valid vanilla `MobEffect` IDs.
- [ ] Positive/negative flags match effect semantics.
- [ ] Probability, duration, and amplifier are sane.
- [ ] Special flags such as `drink`, `extinguish`, `bucketRemainder`, `shortUseProjectile`, and `clearPositiveEffects` still work.

## Essence / potion linkage

- [ ] If the herb is extractable, essence color/model/lang still exists.
- [ ] Base potion effects still match raw-herb identity.
- [ ] Clarified and murky variants still make design sense.
- [ ] High-tier quality/bonus initialization still applies where appropriate.
- [ ] Brewing catalyst interactions are not broken by renamed potion paths.

## Tooltip and Codex linkage

- [ ] `zh_cn` and `en_us` lang keys remain paired.
- [ ] Herb Codex entry text still matches behavior.
- [ ] Unknown/heard/mastered information layering is preserved.
- [ ] Tooltips do not reveal hidden dish recipes too early.

## Hodgepodge and cuisine

- [ ] `#herbcraft:herb` includes the item only if it should be usable in hodgepodge.
- [ ] Mushrooms remain excluded from `#herbcraft:herb`.
- [ ] Hodgepodge recipe still accepts empty bowl + valid 3/4 herbs.
- [ ] Water bowl + random herbs still does not become hodgepodge.
- [ ] Fixed raw cuisine recipes still use `herbcraft_cuisine:water_bowl`, not `minecraft:bowl`.
- [ ] Curated dish ingredient lists still match recipes.
- [ ] Hidden dishes remain water-bowl raw recipes, not direct final crafting recipes.

## Villager trade economy

- [ ] Farmer/cleric/fletcher/smith trade prices still make sense.
- [ ] Potion-buy trades match potion kind and do not require randomized quality/bonus components.
- [ ] Every custom villager trade JSON is referenced by a `replace:false` tag.

## Weapon coating

- [ ] Coating effect duration/amplifier/use count remains balanced.
- [ ] Coated-weapon trades still carry valid `weapon_coating` components.

## Required checks

- [ ] `./gradlew build --rerun-tasks`
- [ ] `python3 scripts/check_lang_diff.py`
- [ ] `python3 scripts/validate_recipe_isolation.py`
- [ ] `python3 scripts/validate_trade_tags.py`
- [ ] `python3 scripts/validate_raw_herb_food_balance.py`
- [ ] `python3 scripts/validate_homeostatic_compat.py`
- [ ] `python3 scripts/validate_item_textures.py`
- [ ] `python3 scripts/validate_p1_codex_advancements.py`
- [ ] `python3 scripts/verify_built_jars.py`

## Ingredients chapter changes

When changing `data/herbcraft/ingredients.json`:

- [ ] Item IDs are valid registered items.
- [ ] Section assignments are one of: `meat`, `seafood`, `grain`, `fruit`, `soup`, `special`.
- [ ] Items have corresponding nutrition profiles in `nutrition_profiles.json`.
- [ ] `zh_cn` and `en_us` section lang keys exist if new sections are added.
- [ ] Removed items no longer appear in the Codex or tooltip system.

## Knowledge system changes

When changing knowledge, codex, page, herb nature, alchemy essence, or cuisine dish data:

- Check old-save migration compatibility for `HerbcraftKnowledgeFlags` and the legacy `HerbcraftKnowledge` mirror.
- If adding a new herb with rumors, add hand-authored claims in `data/herbcraft/knowledge_rumors.json`; do not generate fake claims randomly in Java.
- If adding a downstream practical route (essence, potion, dish, coating, catalyst), register it with `KnowledgePracticeRegistry` so mastery gating remains correct.
- If changing raw eating effects, verify `experiencedEffects` still records only effects that actually triggered.
- If changing Codex UI layout, recheck claim marker hitboxes and C2S `ClaimMarkPayload` behavior.
