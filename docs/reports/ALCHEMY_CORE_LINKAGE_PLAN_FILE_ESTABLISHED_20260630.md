# Alchemy/Core Linkage Plan File Established

Date: 2026-06-30

## Summary

To avoid future turns drifting or relying on stale chat memory, the next multi-turn Alchemy/Core linkage line has been frozen into an explicit authoritative file:

```text
ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md
```

This file now defines:

- why the current Alchemy distillation/processing line still feels too detached from Core pharmacology;
- the architecture rule for Java vs JSON ownership;
- frozen P1-P5 definitions;
- preferred four-round execution order;
- allowed two-round compressed mapping;
- validator expectations;
- progress-log usage requirements.

## Workflow rule established

Future turns continuing this line must:

1. regenerate/read `docs/reports/CURRENT_AGENT_BRIEF.md`;
2. read `CURRENT_BASELINE.md`;
3. read `NEXT_TASK.md`;
4. read `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md`;
5. update `ALCHEMY_CORE_LINKAGE_P1_P5_PLAN.md` if scope/progress/order/details change.

This is now recorded in `NEXT_TASK.md` and reflected in the current baseline/changelog.

## Why this was necessary

The user explicitly noted that a one-off discussion draft is not enough because a future turn may fail to call the latest draft. The plan therefore needed to be elevated into a stable root-level project file that future turns must re-read and update.

## Current implementation status

At the time this plan file was established:

- the distillation bottom-slot bug had been confirmed fixed by the user;
- the Alchemy/Core linkage work itself had not yet started;
- P1-P5 remained fully pending but now had stable definitions and execution order.

## Output

This was a documentation/workflow-control turn only.

No gameplay Java logic, JSON gameplay rules, recipes, or assets were changed in this specific plan-file establishment step beyond the earlier already-completed slot-fix work.
