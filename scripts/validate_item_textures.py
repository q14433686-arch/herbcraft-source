#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path
import json
import tempfile
import urllib.request
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
VANILLA = Path(tempfile.gettempdir()) / "herbcraft_vanilla_item_templates_26_1_2"
CLIENT_JAR = VANILLA / "client-26.1.2.jar"
MC_VERSION_JSON = "https://piston-meta.mojang.com/v1/packages/6c33e70785b01eeccf81024401556c531310b8ad/26.1.2.json"
REQUIRED_TEMPLATES = ["bowl.png", "potion.png", "potion_overlay.png", "paper.png", "written_book.png"]

BOWL_CONTENT_MASK = {
    (x, y)
    for y, row in enumerate([
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        ".....XXXXXX.....",
        "...XXXXXXXXXX...",
        "...XXXXXXXXXX...",
        ".....XXXXXX.X...",
        ".........XX.....",
        "................",
        "................",
        "................",
        "................",
        "................",
    ])
    for x, ch in enumerate(row)
    if ch == "X"
}


def ensure_vanilla_templates() -> None:
    VANILLA.mkdir(parents=True, exist_ok=True)
    if not CLIENT_JAR.exists():
        version = json.load(urllib.request.urlopen(MC_VERSION_JSON))
        urllib.request.urlretrieve(version["downloads"]["client"]["url"], CLIENT_JAR)
    with zipfile.ZipFile(CLIENT_JAR) as zf:
        for name in REQUIRED_TEMPLATES:
            out = VANILLA / name
            if not out.exists():
                out.write_bytes(zf.read(f"assets/minecraft/textures/item/{name}"))

errors: list[str] = []


def read_json(p: Path):
    try:
        return json.loads(p.read_text())
    except Exception as e:
        errors.append(f"invalid json {p}: {e}")
        return {}


def texture_file(namespace: str, tex: str) -> Path | None:
    # tex format: namespace:item/foo or minecraft:item/foo
    if ":" not in tex:
        errors.append(f"texture id lacks namespace: {tex}")
        return None
    ns, path = tex.split(":", 1)
    if ns == "minecraft":
        return VANILLA / (Path(path).name + ".png")
    for module in ["core", "alchemy", "cuisine"]:
        candidate = ROOT / module / "src/main/resources/assets" / ns / "textures" / f"{path}.png"
        if candidate.exists():
            return candidate
    return ROOT / "__missing__" / ns / f"{path}.png"


def validate_references() -> None:
    model_files = list(ROOT.glob("*/src/main/resources/assets/*/models/item/*.json"))
    item_files = list(ROOT.glob("*/src/main/resources/assets/*/items/*.json"))
    if len(model_files) != len(item_files):
        errors.append(f"item model/item definition count mismatch: models={len(model_files)}, items={len(item_files)}")
    model_ids = set()
    for mf in model_files:
        module = mf.parts[mf.parts.index("src") - 1]
        ns = mf.parts[mf.parts.index("assets") + 1]
        model_ids.add(f"{ns}:item/{mf.stem}")
        data = read_json(mf)
        tex = data.get("textures", {}).get("layer0")
        if not tex:
            errors.append(f"missing layer0 texture in {mf}")
            continue
        tf = texture_file(ns, tex)
        if tf is None or not tf.exists():
            errors.append(f"missing texture for {mf}: {tex} -> {tf}")
    for item in item_files:
        data = read_json(item)
        model = data.get("model", {}).get("model")
        if model not in model_ids:
            errors.append(f"item definition {item} points to missing model {model}")


def validate_bowl_containers() -> None:
    bowl = Image.open(VANILLA / "bowl.png").convert("RGBA")
    for png in (ROOT / "cuisine/src/main/resources/assets/herbcraft_cuisine/textures/item").glob("*.png"):
        img = Image.open(png).convert("RGBA")
        if img.size != (16, 16):
            errors.append(f"{png} is not 16x16")
            continue
        for y in range(16):
            for x in range(16):
                if (x, y) not in BOWL_CONTENT_MASK and img.getpixel((x, y)) != bowl.getpixel((x, y)):
                    errors.append(f"{png.name}: bowl container changed at {(x, y)}")
                    return


def validate_essence_bottles() -> None:
    bottle = Image.open(VANILLA / "potion.png").convert("RGBA")
    liquid = Image.open(VANILLA / "potion_overlay.png").convert("RGBA")
    for png in (ROOT / "alchemy/src/main/resources/assets/herbcraft/textures/item").glob("essence_*.png"):
        img = Image.open(png).convert("RGBA")
        if img.size != (16, 16):
            errors.append(f"{png} is not 16x16")
            continue
        for y in range(16):
            for x in range(16):
                bp = bottle.getpixel((x, y))
                lp = liquid.getpixel((x, y))
                ip = img.getpixel((x, y))
                if bp[3] and ip != bp:
                    errors.append(f"{png.name}: vanilla bottle pixel changed at {(x, y)}")
                    return
                if not bp[3] and not lp[3] and ip[3]:
                    errors.append(f"{png.name}: extra non-vanilla outline pixel at {(x, y)}")
                    return


def main() -> int:
    ensure_vanilla_templates()
    validate_references()
    validate_bowl_containers()
    validate_essence_bottles()
    if errors:
        print("FAIL")
        for e in errors[:80]:
            print(" -", e)
        if len(errors) > 80:
            print(f" ... {len(errors)-80} more")
        return 1
    print("PASS - custom item texture references are complete; cuisine bowls preserve vanilla bowl pixels outside content; essence bottles preserve vanilla bottle pixels and use only vanilla liquid overlay area.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
