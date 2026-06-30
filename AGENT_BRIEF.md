# AGENT_BRIEF

If you are a new AI/helper working on this repository, do this first:

```bash
python3 scripts/generate_agent_brief.py --write
```

Then read:

```text
docs/reports/CURRENT_AGENT_BRIEF.md
```

After that, read the authoritative anchors listed there, especially:

```text
CURRENT_BASELINE.md
docs/DOCUMENTATION_INDEX.md
NEXT_TASK.md
docs/JSON_EXTENSION_SPEC_1_1_3.md
docs/EXTENDING_HERBCRAFT.md
CHANGELOG.md
```

Core rule:

```text
User's current request > current source/resources > current docs > old memory/chat summaries.
```

Architecture rule:

```text
Java owns mechanics/loaders/registries/sync/UI/validation/events.
JSON owns content/balance/compatibility/rules where loaders exist.
```

Do not hardcode third-party content tables in Java when JSON can express them.
