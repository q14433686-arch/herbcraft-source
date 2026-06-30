#!/usr/bin/env python3
"""Generate a draft Herbcraft data-compat pack from a small CSV table.

This is intentionally a scaffold generator, not an automatic balance oracle.
It writes Herbcraft extension data under data/<namespace>/herbcraft/ for:

- nutrition_profiles
- ingredients
- herb_natures (optional rows)
- knowledge_rumors (optional skeleton rows)

CSV columns:
item,ingredient_section,grain,flesh,oil,vege,fruit,temperature,flavors,taste_flavors,traits,rumor_key

Only `item` is required. Omitted/blank numeric values are ignored.
Use semicolons for list fields such as flavors and traits.
"""

from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path
from typing import Any

DIMENSIONS = ["grain", "flesh", "oil", "vege", "fruit"]
TEMPERATURES = {"cold", "cool", "neutral", "warm", "hot"}
FLAVORS = {"sweet", "bitter", "pungent", "sour", "astringent", "salty", "bland"}
TRAITS = {
    "clearing", "toxic", "nourishing", "stirring", "sedating", "aquatic", "fiery",
    "withered", "nether", "floral", "resinous", "fungal", "lunar", "solar", "leafy",
    "icy", "slimy", "honeyed", "earthy", "addictive", "curio",
}
SECTIONS = {"meat", "seafood", "grain", "fruit", "soup", "special"}


def split_list(value: str) -> list[str]:
    return [part.strip() for part in value.replace(",", ";").split(";") if part.strip()]


def int_or_none(value: str) -> int | None:
    if value is None or str(value).strip() == "":
        return None
    parsed = int(float(str(value).strip()))
    return parsed if parsed > 0 else None


def validate_list(values: list[str], allowed: set[str], field: str, item: str) -> None:
    unknown = [value for value in values if value not in allowed]
    if unknown:
        raise SystemExit(f"{item}: unknown {field}: {', '.join(unknown)}")


def read_rows(path: Path) -> list[dict[str, str]]:
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        rows = []
        for row in reader:
            if not row.get("item") or row["item"].strip().startswith("#"):
                continue
            rows.append({key: (value or "").strip() for key, value in row.items()})
        return rows


def write_json(path: Path, data: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def generate(args: argparse.Namespace) -> None:
    rows = read_rows(args.items)
    namespace = args.namespace
    root = args.output
    data_root = root / "data" / namespace / "herbcraft"

    nutrition_entries = []
    ingredient_entries = []
    nature_entries = []
    rumor_entries = []

    for row in rows:
        item = row["item"]
        if ":" not in item:
            raise SystemExit(f"Malformed item id: {item}")

        values = {}
        for dim in DIMENSIONS:
            parsed = int_or_none(row.get(dim, ""))
            if parsed is not None:
                values[dim] = parsed
        if values:
            nutrition_entries.append({"item": item, "values": values})

        section = row.get("ingredient_section", "").strip()
        if section:
            if section not in SECTIONS:
                raise SystemExit(f"{item}: unknown ingredient_section {section!r}; use one of {sorted(SECTIONS)}")
            ingredient_entries.append({"item": item, "section": section})

        temperature = row.get("temperature", "").strip()
        flavors = split_list(row.get("flavors", ""))
        taste_flavors = split_list(row.get("taste_flavors", ""))
        traits = split_list(row.get("traits", ""))
        if temperature or flavors or taste_flavors or traits:
            if not temperature:
                temperature = "neutral"
            if temperature not in TEMPERATURES:
                raise SystemExit(f"{item}: unknown temperature {temperature!r}")
            validate_list(flavors, FLAVORS, "flavor", item)
            validate_list(taste_flavors, FLAVORS, "taste_flavor", item)
            validate_list(traits, TRAITS, "trait", item)
            if not flavors:
                flavors = ["bland"]
            entry: dict[str, Any] = {
                "item": item,
                "temperature": temperature,
                "flavors": flavors,
                "traits": traits,
            }
            if taste_flavors:
                entry["taste_flavors"] = taste_flavors
            nature_entries.append(entry)

        rumor_key = row.get("rumor_key", "").strip()
        if rumor_key:
            rumor_entries.append({
                "entry": "herbal/" + item.split(":", 1)[1],
                "claims": [
                    {
                        "id": "compat_note",
                        "type": "note",
                        "text_key": rumor_key,
                        "truth": True,
                        "source_tier": "common",
                        "suspicious": False,
                    }
                ],
            })

    root.mkdir(parents=True, exist_ok=True)
    write_json(root / "pack.mcmeta", {"pack": {"pack_format": args.pack_format, "description": args.pack_name}})
    if nutrition_entries:
        write_json(data_root / "nutrition_profiles" / "generated.json", {"schema_version": 1, "entries": nutrition_entries})
    if ingredient_entries:
        write_json(data_root / "ingredients" / "generated.json", {"schema_version": 1, "entries": ingredient_entries})
    if nature_entries:
        write_json(data_root / "herb_natures" / "generated.json", {"schema_version": 1, "entries": nature_entries})
    if rumor_entries:
        write_json(data_root / "knowledge_rumors" / "generated.json", {"schema_version": 1, "entries": rumor_entries})

    readme = f"""# {args.pack_name}

Generated draft Herbcraft compatibility data pack.

Namespace: `{namespace}`
Rows read: {len(rows)}

Generated surfaces:

- nutrition_profiles: {len(nutrition_entries)} entries
- ingredients: {len(ingredient_entries)} entries
- herb_natures: {len(nature_entries)} entries
- knowledge_rumors: {len(rumor_entries)} entries

Review all values manually before release. This generator does not know real balance, cuisine context, or herb pharmacology; it only writes valid draft JSON from the CSV table.
"""
    (root / "README.md").write_text(readme, encoding="utf-8")
    print(f"Generated compat pack draft at {root}")


def write_example(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=[
            "item", "ingredient_section", "grain", "flesh", "oil", "vege", "fruit",
            "temperature", "flavors", "taste_flavors", "traits", "rumor_key",
        ])
        writer.writeheader()
        writer.writerow({
            "item": "farmersdelight:cabbage",
            "ingredient_section": "fruit",
            "vege": 18,
            "fruit": 4,
            "temperature": "cool",
            "flavors": "sweet;bland",
            "taste_flavors": "sweet;bland",
            "traits": "leafy;nourishing",
            "rumor_key": "knowledge.farmersdelight_compat.cabbage.note",
        })
        writer.writerow({
            "item": "farmersdelight:cooked_rice",
            "ingredient_section": "grain",
            "grain": 24,
        })
    print(f"Wrote example CSV to {path}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--items", type=Path, help="CSV table to read")
    parser.add_argument("--output", type=Path, help="Output datapack directory")
    parser.add_argument("--namespace", default="herbcraft_compat", help="Output data namespace")
    parser.add_argument("--pack-name", default="Herbcraft Compat Draft", help="pack.mcmeta description")
    parser.add_argument("--pack-format", type=int, default=81, help="Minecraft data pack format")
    parser.add_argument("--write-example", type=Path, help="Write an example CSV and exit")
    args = parser.parse_args()
    if args.write_example:
        write_example(args.write_example)
        return
    if not args.items or not args.output:
        parser.error("--items and --output are required unless --write-example is used")
    generate(args)


if __name__ == "__main__":
    main()
