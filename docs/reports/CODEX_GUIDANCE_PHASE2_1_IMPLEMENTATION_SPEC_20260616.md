# Codex Guidance Phase 2.1 Implementation Spec — 2026-06-16

Status: discussion / design spec for author review; **no code implemented in this sub-turn**  
Baseline: Herbcraft `1.1.1`, planning target `1.1.2`

This spec responds to the Phase 2 client review:

1. `膳房与药膳` and `炼金与精华` currently appear even when their addon modules are absent.
2. `五味与性味` is too explicit as a guide page because it prints exact first tasted item/nature. Exact item knowledge should remain in the item's own entry.
3. The author wants the guide system to become data-driven now, not postponed.
4. The author wants Codex guide pages to remember which items triggered 七情 / special pharmacology principles and reveal principle explanations gradually.

## 1. Design decision summary

### 1.1 Guide entries should be data-driven

Do not keep the guide-entry list as a Java `List.of(...)` table in `HerbalChapter`.

Use bundled module JSON resources:

```text
core/src/main/resources/data/herbcraft/codex_guides.json
cuisine/src/main/resources/data/herbcraft_cuisine/codex_guides.json
alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json
```

Why three files rather than only two:

- Core guide entries should also stop being a Java content table.
- Cuisine and Alchemy guide entries must be owned by their own addon jars.
- If an addon jar is absent, its guide JSON is absent and its guide page is never registered.

This preserves module independence and follows the project's data-first rule.

### 1.2 Java still owns mechanics

The JSON should own:

- which guide entries exist;
- which chapter/section they belong to;
- title key;
- base lines;
- simple conditions;
- which dynamic provider or principle keys are used.

Java should own:

- registering guide entries from JSON;
- resolving player progress conditions;
- formatting components and item names;
- reading player discovery state;
- persistence of discovered principles.

This follows the established project rule:

> Java owns mechanics; JSON owns content and balance/data tables.

### 1.3 Addon guide pages must be addon-owned

Immediate target behavior:

| Installed modules | Guide pages visible under 百草经总纲 |
|---|---|
| Core only | Core guides only: recognizing, knowledge states, flavors, seven emotions, herb stack, ingredients, errata. |
| Core + Cuisine | Core guides + `膳房与药膳`. |
| Core + Alchemy | Core guides + `炼金与精华`. |
| Core + Cuisine + Alchemy | All guide pages. |

No Mixin is needed.

Preferred registration path:

- Core loads `data/herbcraft/codex_guides.json` during `HerbalChapter.register()`.
- Cuisine loads `data/herbcraft_cuisine/codex_guides.json` during `CuisineChapter.register()` or the Cuisine initializer after Core has registered the `herbal/overview` section.
- Alchemy loads `data/herbcraft_alchemy/codex_guides.json` during `AlchemyChapter.register()` or the Alchemy initializer after Core has registered the `herbal/overview` section.

Because Cuisine/Alchemy depend on Core, their entrypoints run with Core present; if the addon is absent, its guide JSON and registration call are absent.

## 2. Proposed guide JSON schema

### 2.1 Core file

Path:

```text
core/src/main/resources/data/herbcraft/codex_guides.json
```

Example:

```json
{
  "schema_version": 1,
  "entries": [
    {
      "id": "guide_recognizing",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft.guide.recognizing.title",
      "kind": "herbcraft:recognizing",
      "always_visible": true
    },
    {
      "id": "guide_knowledge_states",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft.guide.knowledge_states.title",
      "kind": "herbcraft:knowledge_states",
      "always_visible": true
    },
    {
      "id": "guide_flavors",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft.guide.flavors.title",
      "kind": "herbcraft:flavors",
      "always_visible": true
    },
    {
      "id": "guide_seven_emotions",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft.guide.seven_emotions.title",
      "kind": "herbcraft:seven_emotions",
      "always_visible": true
    },
    {
      "id": "guide_herb_stack",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft.guide.herb_stack.title",
      "kind": "herbcraft:herb_stack",
      "always_visible": true
    },
    {
      "id": "guide_ingredients",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft.guide.ingredients.title",
      "kind": "herbcraft:ingredients",
      "always_visible": true
    },
    {
      "id": "guide_errata",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft.guide.errata.title",
      "kind": "herbcraft:errata",
      "always_visible": true
    }
  ]
}
```

Core file must **not** contain:

```text
guide_cuisine
guide_alchemy
```

### 2.2 Cuisine file

Path:

```text
cuisine/src/main/resources/data/herbcraft_cuisine/codex_guides.json
```

Example:

```json
{
  "schema_version": 1,
  "entries": [
    {
      "id": "guide_cuisine",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft_cuisine.guide.cuisine.title",
      "kind": "herbcraft_cuisine:cuisine",
      "always_visible": true
    }
  ]
}
```

### 2.3 Alchemy file

Path:

```text
alchemy/src/main/resources/data/herbcraft_alchemy/codex_guides.json
```

Example:

```json
{
  "schema_version": 1,
  "entries": [
    {
      "id": "guide_alchemy",
      "chapter": "herbal",
      "section": "overview",
      "title_key": "book.herbcraft_alchemy.guide.alchemy.title",
      "kind": "herbcraft_alchemy:alchemy",
      "always_visible": true
    }
  ]
}
```

## 3. Proposed Java structure

### 3.1 Core registry/loader

Add a small core loader, e.g.:

```text
core/src/main/java/com/herbcraft/knowledge/CodexGuideRegistry.java
```

Responsibilities:

- Load a bundled guide JSON file by resource path.
- Parse `entries`.
- Validate required fields.
- Register each entry through `KnowledgeAPI.registerEntry(...)`.
- Use a supplied line provider map or a global line resolver.

Possible API:

```java
public final class CodexGuideRegistry {
    public static void loadFromBundledJson(String resourcePath, GuideLineProvider provider) { ... }
}

@FunctionalInterface
public interface GuideLineProvider {
    List<Component> lines(ServerPlayer player, String kind);
}
```

Or:

```java
CodexGuideRegistry.registerProvider(Identifier kind, GuideLineProvider provider);
CodexGuideRegistry.loadFromBundledJson("data/herbcraft/codex_guides.json");
```

The second form is more extensible.

### 3.2 Core registration

In `HerbalChapter.register()`:

```java
CodexGuideRegistry.registerProvider(id("recognizing"), HerbcraftGuideLines::recognizing);
...
CodexGuideRegistry.loadFromBundledJson("data/herbcraft/codex_guides.json");
```

Replace the current hardcoded `List.of(...)` guide table.

### 3.3 Cuisine registration

In `CuisineChapter.register()` or Cuisine initializer:

```java
CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_cuisine", "cuisine"), CuisineGuideLines::cuisine);
CodexGuideRegistry.loadFromBundledJson("data/herbcraft_cuisine/codex_guides.json");
```

This means `guide_cuisine` only exists if Cuisine is installed.

### 3.4 Alchemy registration

In `AlchemyChapter.register()` or Alchemy initializer:

```java
CodexGuideRegistry.registerProvider(Identifier.fromNamespaceAndPath("herbcraft_alchemy", "alchemy"), AlchemyGuideLines::alchemy);
CodexGuideRegistry.loadFromBundledJson("data/herbcraft_alchemy/codex_guides.json");
```

This means `guide_alchemy` only exists if Alchemy is installed.

### 3.5 `KnowledgeAPI` always-visible handling

Current implementation treats all IDs starting with `guide_` as always visible.

That is acceptable for now, but a more data-driven version should let the JSON decide:

```json
"always_visible": true
```

Implementation options:

1. Keep `guide_` convention for `1.1.2` simplicity.
2. Add an `alwaysVisible` predicate/field to `KnowledgeAPI.Entry` later.

Recommendation for immediate `1.1.2`:

- Keep `guide_` convention if changing `KnowledgeAPI.Entry` record is too invasive.
- Still store `always_visible` in JSON and validate it, so future migration is smooth.

## 4. Progressive principle discovery tracking

The author wants the Codex to remember which items triggered pharmacology relations and show approximate examples.

This is a good idea because:

- The player has already eaten the two items.
- The player may not remember which pair caused the message.
- The Codex becomes useful as a field notebook, not just a directory.

## 4.1 What to track

Track two categories:

### Flavor / nature principle discoveries

Potential keys:

```text
flavor_sweet
flavor_bitter
flavor_pungent
flavor_sour
flavor_astringent
flavor_salty
flavor_salty_water
flavor_bland
temperature_harmonize
temperature_same_hot
temperature_same_cold
```

### Seven-emotion discoveries

Potential keys:

```text
seven_xiang_xu
seven_xiang_shi
seven_xiang_sha
seven_xiang_wei
seven_xiang_e
seven_xiang_fan
seven_single
```

## 4.2 What data to store

Add to `PlayerPharmacologyState` or a related player knowledge store:

```java
Map<String, PrincipleDiscovery> discoveries;
```

Suggested record:

```java
record PrincipleDiscovery(
    String key,
    String currentItemId,
    String previousItemId,
    int count,
    long gameTime
) {}
```

For flavor discoveries where only one item is involved:

- `currentItemId` = item just eaten.
- `previousItemId` = empty string.

For 七情:

- `currentItemId` = item just eaten.
- `previousItemId` = immediately previous herb that caused the relation.
- Order should be preserved because `toxic` after `clearing` and `clearing` after `toxic` can mean different relations.

## 4.3 First vs latest example

The author specifically wants to know what recently triggered the relation.

Recommendation:

- Store latest example pair.
- Increment `count` each time.

Then Codex can say:

```text
最近一次，是 %s 与 %s 触发了“相须”。
```

If we later want both first and latest, add fields, but latest+count is enough for `1.1.2`.

## 4.4 Persistence format

Current `PlayerPharmacologyState.encode()` uses:

```text
header#recentUses
```

Backward-compatible extension:

```text
header#recentUses#discoveries
```

Discovery encoding example:

```text
seven_xiang_xu|minecraft:dandelion|minecraft:oxeye_daisy|2|123456;flavor_bitter|minecraft:poppy||1|123500
```

`decode()` should handle old two-part strings by leaving discoveries empty.

## 4.5 Where to record discoveries

In `PharmacologyResolver.resolve(...)`:

### Flavor examples

Record a flavor only when its rule actually matters.

Suggested conditions:

| Discovery | Condition |
|---|---|
| `flavor_sweet` | Herb has `sweet` and has healing-like effect or heal event, or heal multiplier was used. |
| `flavor_bitter` | Herb has `bitter`; bitter always affects immunity multiplier and herb stack decrement behavior. |
| `flavor_pungent` | Herb has `pungent`; record if it affects positive duration or rolls nausea. |
| `flavor_sour` | Herb has `sour`; record if entry has effects whose duration can be modified. |
| `flavor_astringent` | Herb has `astringent`; record when duration modifier applies, and separately if low-health heal bonus triggers. |
| `flavor_salty` | Herb has `salty`; record normal salty. |
| `flavor_salty_water` | Herb has `salty` and `nearWater(player)` is true. |
| `flavor_bland` | Herb has `bland`; record if it reduces side-effect probability. |

### Temperature examples

Record when `overlayKey` is set:

```text
message.herbcraft.pharmacology.harmonize -> temperature_harmonize
message.herbcraft.pharmacology.same_qi_hot -> temperature_same_hot
message.herbcraft.pharmacology.same_qi_cold -> temperature_same_cold
```

### Seven emotions

After `judgeSevenEmotion(...)`, record when type is not `NONE`:

```text
XIANG_XU -> seven_xiang_xu
XIANG_SHI -> seven_xiang_shi
XIANG_SHA -> seven_xiang_sha
XIANG_WEI -> seven_xiang_wei
XIANG_E -> seven_xiang_e
XIANG_FAN -> seven_xiang_fan
SINGLE -> seven_single
```

Use `latest.itemId()` as previous and current stack item ID as current.

## 5. Progressive guide copy: Chinese

These lines are intended as final or near-final copy. They should be placed in lang files and shown only when conditions are met.

## 5.1 五味与性味

### Base lines, always visible

```text
草木有寒热，也有甘、苦、辛、酸、咸、淡、涩等味。
未曾亲尝之前，性味只能从残页与传闻中窥见一二。
```

### After first tasted herb

```text
你已亲尝过一种草木，开始能分辨寒热与若干味感。具体性味可在对应条目查看。
```

### After several flavors discovered

```text
你已辨出 %s 类味感。同味未必同效，却可作为比较的起点。
```

### Flavor principle lines, shown only after discovery

```text
你曾见过“甘”：甘味多偏缓，常与补益、恢复相近。
你曾见过“苦”：苦味多有清解之意，也可能削去积蓄的药性。
你曾见过“辛”：辛味多行散，能推送药力，也可能扰胃。
你曾见过“酸”：酸味多收敛，常使药感更久，也能收短不适。
你曾见过“涩”：涩味能收敛，危急时也可能护住一口气。
你曾见过“咸”：咸味入水，近水时更显其性。
你曾见过“淡”：淡味寡而不争，常能压低副作用的锋芒。
```

### Temperature principle lines

```text
你曾见过寒热相济：寒热相反时，偏性会被稍稍调和。
你曾见过热性渐盛：热上加热，反噬更近。
你曾见过寒意积聚：寒上加寒，药力渐沉。
```

## 5.2 七情与配伍

### Base lines, always visible

```text
草木并非孤立。相近者可相须，相冲者可相反。
一味难知配伍。多尝几种，再回来看此页。
```

### After at least two tasted herbs, but no specific relation discovered

```text
你已亲尝 %s 种草木，足以察觉：食用顺序也会改变药性。
```

### Relation lines, shown one per discovered relation

Use current/previous item names as arguments. Suggested wording preserves uncertainty while remaining useful.

```text
你曾见过“相须”：%s 与 %s 性近相助，正向药力更盛。
你曾见过“相使”：%s 引动 %s，药势行得更远。
你曾见过“相杀”：%s 的清解压过 %s 的毒性。
你曾见过“相畏”：%s 遇 %s 制约，毒性反而收敛。
你曾见过“相恶”：%s 与 %s 相忤，整体力道转弱。
你曾见过“相反”：%s 与 %s 寒热毒性相激，药效会反噬。
你曾见过“单行”：%s 独行其性，与近食草木少有牵连。
```

### Optional count/recency line

If a relation has happened more than once:

```text
此类配伍你已见过 %s 次；最近一次仍可作为参照。
```

## 6. Progressive guide copy: English

## 6.1 Nature and Flavors

Base:

```text
Herbs carry warmth or cold, and flavors such as sweet, bitter, pungent, sour, salty, bland, and astringent.
Before tasting, nature and flavor can only be guessed from rumor and pages.
```

After first tasted herb:

```text
You have tasted one herb and begun to sense warmth, cold, and flavor. Exact nature belongs in that herb's own entry.
```

After several flavors:

```text
You have recognized %s flavor types. Similar flavor is a clue, not a guarantee.
```

Flavor principle lines:

```text
You have seen Sweet: sweet flavor tends to soften and often leans toward nourishment or recovery.
You have seen Bitter: bitter flavor often clears, and may shave down gathered herb momentum.
You have seen Pungent: pungent flavor moves medicine outward, but may trouble the stomach.
You have seen Sour: sour flavor gathers and holds; it can lengthen good sensations and shorten discomfort.
You have seen Astringent: astringency draws inward, and in danger may help you hold on.
You have seen Salty: salty flavor follows water; near water, its nature shows more clearly.
You have seen Bland: bland flavor does not contend, often dulling the edge of side effects.
```

Temperature lines:

```text
You have seen hot and cold balance each other: opposing nature can soften the edge.
You have seen heat build on heat: backlash draws closer.
You have seen cold gather on cold: medicine sinks and slows.
```

## 6.2 Relations Between Herbs

Base:

```text
Herbs do not stand alone. Some support each other; some clash.
One herb cannot teach pairing. Taste a few more, then return.
```

After two tasted herbs:

```text
You have tasted %s herbs, enough to suspect that order can change medicine.
```

Relation lines:

```text
You have seen Mutual Need: %s and %s support each other, strengthening beneficial medicine.
You have seen Guiding Use: %s draws %s onward, carrying the medicine farther.
You have seen Mutual Killing: %s clears and suppresses the toxin of %s.
You have seen Mutual Fear: %s is restrained by %s, and its toxin withdraws.
You have seen Mutual Aversion: %s and %s work against each other, weakening the whole dose.
You have seen Mutual Incompatibility: %s and %s clash in hot/cold toxicity, turning medicine back on you.
You have seen Single Action: %s acts mostly alone, with little tie to the herbs just eaten.
```

Optional count line:

```text
You have seen this relation %s times; the latest example remains a useful reference.
```

## 7. Data-driven guide conditions

If guide lines become data-driven too, line JSON can use simple conditions.

Example:

```json
{
  "key": "book.herbcraft.guide.flavors.sweet",
  "style": "gray",
  "when": {
    "principle": "flavor_sweet"
  }
}
```

Example with item args:

```json
{
  "key": "book.herbcraft.guide.seven.xiang_xu",
  "style": "dark_aqua",
  "when": {
    "principle": "seven_xiang_xu"
  },
  "args": [
    { "type": "principle_current_item", "principle": "seven_xiang_xu" },
    { "type": "principle_previous_item", "principle": "seven_xiang_xu" }
  ]
}
```

Supported condition ideas:

```text
advancement_done
advancement_not_done
tasted_herb_count_min
discovered_flavor_count_min
tasted_ingredient_count_min
principle
principle_count_min
heard_herb_count_min
verified_claim_count_min
```

Supported styles:

```text
gray
dark_gray
dark_aqua
gold
green
red
italic
```

Recommendation:

- For `1.1.2`, data-drive guide entry registration first.
- Keep line providers in Java if full conditional line JSON is too much for this patch.
- But design the JSON schema so conditional lines can be added later without changing entry IDs.

## 8. Immediate implementation order recommended

1. Add `CodexGuideRegistry` loader.
2. Add module guide JSON files:
   - Core: core guides only.
   - Cuisine: `guide_cuisine` only.
   - Alchemy: `guide_alchemy` only.
3. Move Cuisine/Alchemy guide line providers into their own modules, or at minimum register their provider from their own modules.
4. Remove unconditional `cuisine`/`alchemy` from Core `HerbalChapter`.
5. Soften `五味与性味` guide immediately: no exact item/nature line in the guide page.
6. Add `PlayerPharmacologyState` principle discovery tracking if accepted for `1.1.2`.
7. Add progressive flavor/seven-emotion lines using the copy above.
8. Update validators.
9. Client-test module combinations:
   - Core only.
   - Core + Cuisine.
   - Core + Alchemy.
   - Core + Cuisine + Alchemy.

## 9. Validator requirements

Update validators to enforce:

- Core `codex_guides.json` does not contain `guide_cuisine` or `guide_alchemy`.
- Cuisine `codex_guides.json` contains `guide_cuisine`.
- Alchemy `codex_guides.json` contains `guide_alchemy`.
- `HerbalChapter.java` does not contain a hardcoded guide `List.of(...)` content table.
- `HerbalChapter.java` does not directly print `HerbNatureAPI.natureLine(first.nature())` in the guide page.
- Lang keys exist in the correct module lang files.
- Built jars contain the new `codex_guides.json` files.

## 10. Open author decisions before coding

1. Should principle discovery tracking be included in `1.1.2`, or should `1.1.2` only data-drive guide registration and soften the text?
2. If tracking is included, should it store only latest example pair + count, or first + latest + count?
3. Should flavor principles unlock when the flavor is merely consumed, or only when the flavor rule visibly affects an outcome?
4. Should relation item order be shown as “current 与 previous” or more natural “previous 与 current”? Recommended for player memory: show chronological order: previous → current.
5. Should `single action` / 单行 be shown, or omitted because it is less important and may confuse new players?

Recommended answers:

1. Include seven-emotion tracking now; flavor tracking can be lighter if needed.
2. Store latest pair + count.
3. Unlock flavors only when the corresponding rule is relevant, not merely because the herb has that flavor.
4. Show chronological order.
5. Omit `single action` from the first implementation unless the author strongly wants it.
