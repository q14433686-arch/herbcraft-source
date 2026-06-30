# Herbcraft AI Collaboration Guide

Status: prompt and workflow guide for future AI/helper sessions  
Target: Herbcraft Core/Alchemy `1.1.4+` and Cuisine `1.1.3+`

Use this file when asking another AI or developer to work on Herbcraft. It exists so the author does not need to repeat long context every time.

## 1. Minimal prompt for another AI

Copy this:

```text
请先读根目录的 AGENT_BRIEF.md。

如果你能运行脚本，请先执行：

python3 scripts/generate_agent_brief.py --write

然后阅读：

docs/reports/CURRENT_AGENT_BRIEF.md

再按其中列出的顺序阅读 CURRENT_BASELINE.md、docs/DOCUMENTATION_INDEX.md、NEXT_TASK.md、docs/JSON_EXTENSION_SPEC_1_1_3.md、docs/EXTENDING_HERBCRAFT.md、CHANGELOG.md。

执行任务时必须遵循：
用户本轮需求 > 当前源码/资源 > 当前文档 > 旧记忆/旧聊天记录。

Herbcraft 的规则是：
Java 负责机制、加载、注册、同步、UI、验证和事件；
JSON 负责内容、数值、规则表、兼容表和文案。
如果 JSON 能表达，就不要把内容硬编码进 Java。

本轮我的目标是：
【在这里写你的具体需求】

完成后必须更新 CURRENT_BASELINE.md、NEXT_TASK.md、CHANGELOG.md 和相关文档；
如果改了源码或资源，要刷新 release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip；
如果改了 jar 内容，要重新 build 并刷新 release_output 下的 jars；
最后明确说明跑了哪些验证，哪些只能客户端确认。
```

## 2. Even shorter prompt

```text
请先读 AGENT_BRIEF.md，并执行：
python3 scripts/generate_agent_brief.py --write
然后阅读 docs/reports/CURRENT_AGENT_BRIEF.md，再按里面的文档顺序接手。

本轮目标是：【写你的目标】

必须遵循：用户本轮需求 > 当前源码/资源 > 当前文档 > 旧记忆。
Herbcraft 规则：Java 管机制，JSON 管内容；能数据驱动就不要硬编码。
```

## 3. Which docs matter for which task?

| Task type | Read first |
|---|---|
| Any task | `AGENT_BRIEF.md`, generated `CURRENT_AGENT_BRIEF.md`, `CURRENT_BASELINE.md`, `NEXT_TASK.md` |
| Add datapack compatibility | `docs/JSON_EXTENSION_SPEC_1_1_3.md`, `docs/ADDON_AUTHOR_QUICKSTART_1_1_3.md`, `docs/examples/external_extension_compat_pack/` |
| Add code addon integration | `docs/ADDON_AUTHOR_QUICKSTART_1_1_3.md`, `docs/examples/herbcraft_readonly_event_addon/`, `docs/PUBLIC_EXTENSION_API_DESIGN.md` |
| Change nutrition | `data/herbcraft/nutrition_rules.json`, `nutrition_profiles.json`, `nutrition_correction_rules.json`, validators, `docs/HERBCRAFT_FULL_SYSTEM_WIKI_1_1_3.md` |
| Change pharmacology | `data/herbcraft/pharmacology_rules.json`, `herb_natures.json`, `PharmacologyResolver`, `docs/HERBCRAFT_FULL_SYSTEM_WIKI_1_1_3.md` |
| Change raw herb food values | `data/herbcraft/herbs.json`, `scripts/validate_raw_herb_food_balance.py`, raw herb reports |
| Change Farmer+More compat | `docs/compat_drafts/food_mods_26_1_2/compat_review_round5_farmer_moredelight.csv`, Core bundled `data/farmers_moredelight_herbcraft/herbcraft/` |
| Publish/release | `CURRENT_BASELINE.md`, `docs/RELEASE_PAGE_COPY_1.1.4.md`, `docs/MCMOD_RELEASE_COPY_1.1.4.md`, `CHANGELOG.md` |

## 4. What not to let an AI do silently

Do not let an AI silently:

```text
- hardcode third-party mod item lists in Java;
- add Java write/registration APIs when JSON can express content;
- add datapack /reload FOOD or CONSUMABLE mutation claims;
- put non-edible items into 食材录 or 药食志;
- re-add RMF to the main Farmer+More compat;
- treat old docs or chat memory as more authoritative than current source/resources;
- skip CURRENT_BASELINE.md updates after a modifying turn;
- leave release_output jars/source zip stale after code/resource changes.
```

## 5. End-of-turn checklist for AI helpers

A modifying turn is not done until the helper reports:

```text
changed files
whether jars changed
whether source zip changed
which validators were run
what still needs client testing
what the next task is
```

At minimum, update:

```text
CURRENT_BASELINE.md
NEXT_TASK.md
CHANGELOG.md
```

and refresh:

```text
release_output/herbcraft_test_20260614_review/herbcraft-source-current-20260614.zip
```
