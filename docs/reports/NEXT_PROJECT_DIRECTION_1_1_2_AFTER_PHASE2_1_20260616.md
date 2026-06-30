# Next Project Direction after Codex Guidance Phase 2.1 — 2026-06-16

Status: discussion / planning / implementation guidance  
Baseline path: `release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip`  
Current code line: Herbcraft `1.1.1`, planning target `1.1.2`

This note is based on:

- `NEXT_TASK.md`
- `docs/EXTERNAL_EXTENSION_SPEC_DRAFT.md`
- `CHANGELOG.md`
- `CURRENT_BASELINE.md`
- `reference_cache/26_1_porting_notes.md`
- current source state after `1.1.2` Codex guidance Phase 2.1

It is written to avoid relying on memory. Treat the listed files and current source as authoritative.

## 1. Current position

Implemented and validated in source/build:

- `1.1.2` onboarding Phase 1 text polish.
- `1.1.2` Codex guidance Phase 2.1:
  - guide registration is data-backed by module-owned `codex_guides.json` files;
  - Core no longer owns Cuisine/Alchemy guide entries;
  - Cuisine/Alchemy guide pages are addon-owned;
  - 食材录 overview exists;
  - 五味/七情 guide lines use persisted player pharmacology discoveries;
  - exact item nature remains in item entries, not the system guide.
- Full build and validator suite passed in the previous modification turn.

Still pending:

- Real-client testing of Phase 2.1 across module combinations.
- Decision and implementation of Phase 3 one-time hint system.
- Final `1.1.2` release preparation/versioning.

## 2. Immediate next work: client verification gate

Before adding more systems, the author should finish real-client checks for Phase 2.1.

### 2.1 Module combination matrix

Test these four combinations:

| Installed jars | Expected guide pages under 百草经总纲 |
|---|---|
| Core only | Core guide pages only. No `膳房与药膳`, no `炼金与精华`. |
| Core + Cuisine | Core guide pages + `膳房与药膳`; no `炼金与精华`. |
| Core + Alchemy | Core guide pages + `炼金与精华`; no `膳房与药膳`. |
| Core + Cuisine + Alchemy | All guide pages. |

Why this matters:

- It proves module-owned guide registration works.
- It proves Core still runs alone.
- It prevents a repeat of addon content leaking into Core-only installs.

### 2.2 Pharmacology discovery checks

Test several herb-eating sequences and then inspect 百草经 guide pages.

Recommended checks:

1. Trigger at least one simple flavor principle.
   - Example targets: bitter, pungent, sweet, salty near water.
   - Confirm `五味与性味` gains an explanatory line only after the principle is discovered.
2. Trigger at least one 七情 relation.
   - Example targets: `XIANG_XU`, `XIANG_SHA`, `XIANG_WEI`, `XIANG_E`, `XIANG_FAN` if feasible.
   - Confirm `七情与配伍` shows a separate line for the discovered relation.
   - Confirm it shows the latest item pair in chronological order: previous herb → current herb.
3. Trigger the same relation twice.
   - Confirm the count line appears and increments.
4. Confirm the guide does **not** print exact first tasted item nature in `五味与性味`.
   - Exact nature should remain in the individual herb entry after tasting.

### 2.3 UX checks

- Text should remain readable and not overflow.
- Guide entries should feel like field notes, not a wiki dump.
- The guide should not reveal hidden dishes, exact thresholds, correction matrix weights, or unearned herb effects.

## 3. If client check passes: Phase 3 one-time hint system

If Phase 2.1 passes, the next useful feature is the one-time hint system.

This should be small and data-backed where practical.

### 3.1 Recommended design

Add a Core manager:

```text
core/src/main/java/com/herbcraft/guide/GuideHintManager.java
```

Responsibilities:

- show a hint only once per player;
- support private chat and actionbar delivery;
- load hint definitions from JSON;
- support module-owned hint files;
- keep messages in lang files;
- avoid hidden recipe/effect spoilers.

### 3.2 Suggested JSON files

Use module-owned JSON, parallel to `codex_guides.json`:

```text
core/src/main/resources/data/herbcraft/guide_hints.json
cuisine/src/main/resources/data/herbcraft_cuisine/guide_hints.json
alchemy/src/main/resources/data/herbcraft_alchemy/guide_hints.json
```

Example shape:

```json
{
  "schema_version": 1,
  "hints": [
    {
      "id": "first_herb_bite",
      "trigger": "herbcraft:first_herb_bite",
      "message_key": "guide.herbcraft.first_herb_bite",
      "delivery": "chat_private",
      "once": true
    }
  ]
}
```

Fields:

- `id`: stable player-save key.
- `trigger`: Java-owned trigger ID.
- `message_key`: lang key.
- `delivery`: `chat_private` or `actionbar`.
- `once`: should be `true` for `1.1.2`.
- optional future fields: conditions, required module, cooldown.

### 3.3 Player persistence

Add a persisted set of seen hint IDs.

Possible location:

- `PlayerMixin` as `Set<String> herbcraft$seenGuideHints`.
- Access through `HerbcraftPlayerData` methods.

Encoding can mirror existing set encoders:

```text
HerbcraftGuideHints = first_herb_bite;first_codex_open;first_page_read
```

This is small and should be stable.

### 3.4 Why not use hidden advancements only

Hidden advancements could work as once-only flags, but they are less clean:

- hint state is not really advancement state;
- it clutters advancement data;
- it is harder to support chat vs actionbar behavior;
- module-owned hint JSON is cleaner and matches the data-driven direction.

Recommendation: use `GuideHintManager` + persisted seen-hint set.

## 4. First hint set for `1.1.2`

Keep the first implementation small.

### Core hints

#### `first_herb_bite`

Trigger:

- `HerbcraftAdvancementHooks.onHerbConsumed(...)` or the same event path that grants `first_bite`.

Delivery:

- private chat.

Chinese:

```text
草木入喉，性味难明。以书与蒲公英装订《百草经》，可记亲尝所得。
```

English:

```text
A wild taste lingers. Bind a Book with a Dandelion to make a Herbal Codex and record what you learn by tasting.
```

#### `first_codex_open`

Trigger:

- `CodexBookItem.use(...)`.

Delivery:

- private chat.

Chinese:

```text
左侧为目录，右侧为条目。听闻未必可信，亲尝才会入册。
```

English:

```text
The left page is the index; the right page is the record. Rumors are not proof — tasting makes knowledge your own.
```

#### `first_page_read`

Trigger:

- successful `TatteredPageItem.use(...)`.

Delivery:

- private chat or actionbar. Private chat preferred because it teaches a system.

Chinese:

```text
残页只载听闻。可先存疑，待亲尝或勘误后再信。
```

English:

```text
A page gives rumor, not certainty. Mark doubts now; tasting or errata may prove them later.
```

#### `first_ingredient_tasted`

Trigger:

- `NutritionManager.onFoodConsumed(...)` when item has an `IngredientsChapter` entry and this is the first such hint.

Delivery:

- private chat.

Chinese:

```text
凡食皆养。普通食物不入药性，却会记入《食材录》的五养。按 [N] 可查看长期营养状态。
```

English:

```text
Every meal nourishes. Ordinary foods do not carry herb nature, but the Ingredient Codex records their long-term nutrition. Press [N] to view your nutrition.
```

#### `first_correction`

Trigger:

- first time `NutritionManager` enters active `correcting` state.

Delivery:

- actionbar or private chat. Actionbar may fit bodily feedback; private chat may be clearer.

Chinese:

```text
偏食可回正。换食相补之物，过盛之养会慢慢平复。
```

English:

```text
An uneven diet can be corrected. Eat what balances the excess, and the surplus will settle over time.
```

### Cuisine hints

#### `first_water_bowl`

Trigger:

- first water bowl use or first successful relief. Prefer first use, because it teaches earlier.

Chinese:

```text
水碗可缓一时不适，也可入药膳；但它不会真正修复五养。
```

English:

```text
A water bowl can soothe you for a while and serves cuisine, but it does not truly repair nutrition.
```

#### `first_dish`

Trigger:

- first Herbcraft Cuisine dish eaten.

Chinese:

```text
烹饪不是赌药性。药膳重在清除、免疫与稳定准备。
```

English:

```text
Cuisine is not raw gambling. Medicinal dishes are preparation: cleansing, immunity, and steadier nourishment.
```

### Alchemy hints

#### `first_essence`

Trigger:

- first essence acquired, same path as `first_essence` advancement.

Chinese:

```text
精华使草木之性入瓶。炼金比生食更稳定，也更讲契约。
```

English:

```text
An essence bottles a plant's nature. Alchemy is steadier than raw eating, but it asks for a contract.
```

## 5. Implementation constraints from 26.1 notes

From `reference_cache/26_1_porting_notes.md`:

- Use Java 25.
- Do not add old `TradeOfferHelper`; not relevant here.
- Do not add unnecessary Mixins. The hint system can use existing event/hook points.
- Avoid deprecated tooltip APIs; not relevant unless hint text is added to tooltips.
- Do not create `ItemStack` too early in static initialization. Hint JSON/lang should not require early `ItemStack` construction.
- Keep module independence: Core cannot depend on Cuisine/Alchemy.

## 6. Suggested validators for Phase 3

Add or extend validators to check:

- Core `guide_hints.json` does not contain Cuisine/Alchemy triggers.
- Cuisine/Alchemy hint files contain only their module hints.
- All `message_key` values exist in the correct lang files.
- `GuideHintManager` persists seen hint IDs.
- No hint text contains hidden recipe details or exact numerical thresholds.
- Built jars contain guide hint JSON files if added.

## 7. Release/versioning after Phase 3

Once Phase 2.1 client test and Phase 3 hints pass, prepare `1.1.2` as an actual release.

Likely release steps:

1. Bump Gradle subproject version to `1.1.2`.
2. Bump all three `fabric.mod.json` versions to `1.1.2`.
3. Bump addon dependencies to `herbcraft >=1.1.2` if required.
4. Update `verify_built_jars.py` expected version.
5. Update README / release docs / changelog for `1.1.2`.
6. Rebuild into the existing local baseline directory naming convention unless the author changes it:

```text
release_output/herbcraft_test_20260614_review/
herbcraft-source-current-20260614.zip
```

Important: do not change the baseline source zip name unless the author asks.

## 8. Recommendation

The best next order is:

1. Author client-tests Phase 2.1.
2. Fix any Phase 2.1 client issues.
3. Implement the small data-backed one-time hint system.
4. Client-test hints with a fresh player/world.
5. Then bump to `1.1.2` and package.

Do not start P4 API, 药引子, Homeostatic temperature gameplay, or Hardcore Nutrition until `1.1.2` guidance polish is stable.
