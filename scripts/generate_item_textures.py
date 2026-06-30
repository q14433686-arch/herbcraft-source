#!/usr/bin/env python3
"""
Generate Herbcraft item textures from vanilla Minecraft templates.

Rules intentionally mirror the texture handoff requirements:
- Essence bottles use vanilla potion bottle pixels. Only the liquid overlay is tinted.
- Cuisine bowls use vanilla bowl pixels. Only the bowl-mouth content mask is replaced.
- No text-to-image generation is used.
"""
from __future__ import annotations

import colorsys
import hashlib
import json
import re
import shutil
import subprocess
import urllib.request
import zipfile
from pathlib import Path
from typing import Iterable

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
VANILLA = ROOT / "build" / "vanilla_item_templates"
CLIENT_JAR = VANILLA / "client-26.1.2.jar"
MC_VERSION_JSON = "https://piston-meta.mojang.com/v1/packages/6c33e70785b01eeccf81024401556c531310b8ad/26.1.2.json"

TEMPLATE_ITEMS = [
    "potion.png",
    "potion_overlay.png",
    "bowl.png",
    "mushroom_stew.png",
    "suspicious_stew.png",
    "paper.png",
    "written_book.png",
    "amethyst_shard.png",
    "slime_ball.png",
    "echo_shard.png",
]

BOWL_CONTENT_MASK = {
    # Conservative soup area derived from vanilla mushroom_stew - bowl.png.
    # Pixels outside this mask remain byte-for-byte equal to minecraft:bowl.
    (x, y)
    for y, row in enumerate(
        [
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
        ]
    )
    for x, ch in enumerate(row)
    if ch == "X"
}

ESSENCE_OVERRIDES = {
    "dandelion": (245, 215, 65),
    "golden_dandelion": (255, 198, 38),
    "poppy": (205, 42, 38),
    "blue_orchid": (83, 160, 224),
    "azure_bluet": (224, 224, 202),
    "tulip": (231, 87, 69),
    "oxeye_daisy": (238, 238, 210),
    "cornflower": (78, 112, 218),
    "lily_of_the_valley": (230, 236, 220),
    "wither_rose": (45, 34, 47),
    "torchflower": (255, 145, 45),
    "closed_eyeblossom": (76, 58, 102),
    "open_eyeblossom": (245, 177, 76),
    "cactus_flower": (230, 117, 184),
    "pink_petals": (252, 142, 189),
    "sunflower": (255, 205, 43),
    "lilac": (177, 117, 211),
    "rose_bush": (210, 52, 69),
    "peony": (238, 138, 191),
    "pitcher_plant": (107, 73, 153),
    "fern": (74, 142, 58),
    "spore_blossom": (208, 112, 191),
    "cocoa_beans": (131, 73, 38),
    "oak_leaves": (83, 150, 64),
    "spruce_leaves": (50, 103, 72),
    "birch_leaves": (129, 169, 70),
    "acacia_leaves": (91, 137, 56),
    "mangrove_leaves": (70, 122, 67),
    "cherry_leaves": (247, 154, 183),
    "pale_oak_leaves": (194, 199, 179),
    "azalea_leaves": (78, 143, 72),
    "flowering_azalea_leaves": (83, 146, 76),
    "cherry_sapling": (245, 159, 190),
    "azalea": (89, 151, 77),
    "flowering_azalea": (103, 162, 87),
    "kelp": (62, 126, 87),
    "sea_pickle": (154, 205, 76),
    "crimson_fungus": (151, 39, 57),
    "warped_fungus": (47, 177, 167),
    "brown_mushroom": (151, 96, 66),
}

BOWL_PALETTES = {
    "water": ((75, 142, 224), (125, 194, 246), (37, 86, 168)),
    "blue_ice": ((104, 190, 245), (196, 241, 255), (56, 118, 210)),
    "cherry": ((234, 119, 154), (255, 179, 200), (168, 65, 94)),
    "crimson": ((172, 42, 48), (232, 88, 70), (105, 24, 42)),
    "warped": ((50, 176, 161), (121, 232, 213), (29, 96, 111)),
    "golden": ((235, 179, 46), (255, 227, 91), (160, 99, 24)),
    "honey": ((230, 169, 49), (255, 222, 91), (154, 87, 26)),
    "honeycomb": ((226, 157, 42), (255, 205, 74), (142, 84, 18)),
    "lily": ((229, 232, 205), (255, 255, 237), (111, 143, 99)),
    "omen": ((89, 65, 124), (153, 119, 190), (45, 32, 77)),
    "pale": ((176, 181, 160), (223, 227, 204), (87, 91, 84)),
    "peony": ((225, 119, 171), (255, 178, 215), (140, 61, 107)),
    "pine": ((63, 119, 68), (119, 174, 87), (35, 73, 51)),
    "pumpkin": ((217, 119, 37), (255, 169, 72), (139, 66, 20)),
    "resin": ((205, 122, 50), (244, 175, 78), (125, 66, 31)),
    "sea": ((54, 137, 149), (118, 210, 206), (28, 78, 101)),
    "soul": ((90, 166, 184), (157, 232, 232), (56, 77, 114)),
    "star": ((102, 83, 170), (218, 203, 255), (49, 41, 104)),
    "snowcap": ((186, 226, 241), (250, 255, 255), (103, 158, 196)),
    "three_flower": ((216, 105, 149), (255, 213, 102), (86, 136, 196)),
    "twin_fungus": ((127, 72, 100), (64, 190, 176), (93, 35, 62)),
    "dandelion": ((111, 171, 65), (245, 218, 65), (58, 101, 43)),
    "bitter": ((83, 118, 62), (151, 164, 75), (42, 69, 39)),
    "analgesic": ((168, 125, 88), (221, 176, 121), (93, 67, 55)),
    "eye": ((199, 142, 58), (250, 215, 93), (76, 57, 44)),
    "fatigue": ((141, 116, 81), (219, 190, 126), (73, 65, 61)),
    "stomach": ((174, 104, 50), (235, 162, 78), (95, 55, 28)),
    "default": ((159, 88, 55), (214, 143, 92), (94, 51, 37)),
}

SPECIAL_SPARKLE_KEYWORDS = ("golden", "star", "soul", "wither", "echo", "amethyst", "rare", "open_eyeblossom", "sea_pickle")


def ensure_vanilla_assets() -> None:
    VANILLA.mkdir(parents=True, exist_ok=True)
    if not CLIENT_JAR.exists():
        version = json.load(urllib.request.urlopen(MC_VERSION_JSON))
        client_url = version["downloads"]["client"]["url"]
        urllib.request.urlretrieve(client_url, CLIENT_JAR)
    with zipfile.ZipFile(CLIENT_JAR) as zf:
        for item in TEMPLATE_ITEMS:
            out = VANILLA / item
            if not out.exists():
                out.write_bytes(zf.read(f"assets/minecraft/textures/item/{item}"))


def read_rgba(name: str) -> Image.Image:
    return Image.open(VANILLA / name).convert("RGBA")


def stable_int(s: str) -> int:
    return int(hashlib.sha1(s.encode("utf-8")).hexdigest()[:8], 16)


def clamp(v: int) -> int:
    return max(0, min(255, int(v)))


def adjust(color: tuple[int, int, int], factor: float) -> tuple[int, int, int]:
    return tuple(clamp(c * factor) for c in color)


def item_texture_average(item_id: str) -> tuple[int, int, int] | None:
    candidates = [item_id]
    if item_id == "tulip":
        candidates = ["red_tulip", "orange_tulip", "pink_tulip", "white_tulip"]
    elif item_id == "golden_dandelion":
        return ESSENCE_OVERRIDES[item_id]
    with zipfile.ZipFile(CLIENT_JAR) as zf:
        cols = []
        for cid in candidates:
            path = f"assets/minecraft/textures/item/{cid}.png"
            if path not in zf.namelist():
                continue
            with zf.open(path) as f:
                img = Image.open(f).convert("RGBA")
                for r, g, b, a in img.getdata():
                    if a > 0 and not (r < 35 and g < 35 and b < 35):
                        cols.append((r, g, b))
        if not cols:
            return None
        return tuple(sum(c[i] for c in cols) // len(cols) for i in range(3))


def essence_color(essence_name: str) -> tuple[int, int, int]:
    base = essence_name.removeprefix("essence_")
    return ESSENCE_OVERRIDES.get(base) or item_texture_average(base) or (154, 205, 94)


def composite_essence(color: tuple[int, int, int], rare: bool = False) -> Image.Image:
    bottle = read_rgba("potion.png")
    liquid = read_rgba("potion_overlay.png")
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # Tint vanilla potion liquid overlay by preserving its grayscale luminance.
    for y in range(16):
        for x in range(16):
            r, g, b, a = liquid.getpixel((x, y))
            if a:
                lum = (r + g + b) / 3.0 / 255.0
                # Keep the vanilla highlight/shadow structure, just shift hue.
                tinted = tuple(clamp(c * (0.55 + 0.55 * lum)) for c in color)
                out.putpixel((x, y), (*tinted, a))
    if rare:
        for x, y in [(6, 8), (9, 7), (8, 10)]:
            if liquid.getpixel((x, y))[3]:
                out.putpixel((x, y), (*adjust(color, 1.45), 255))
    out.alpha_composite(bottle)
    return out


def cuisine_palette(name: str) -> tuple[tuple[int, int, int], tuple[int, int, int], tuple[int, int, int]]:
    key_order = [
        "blue_ice", "cherry", "crimson", "warped", "golden", "honeycomb", "honey", "lily", "omen", "pale", "peony", "pine", "pumpkin", "resin", "seafarer", "sea", "soul", "star", "snowcap", "three_flower", "twin_fungus", "dandelion", "bitter", "analgesic", "eye", "fatigue", "stomach", "water"
    ]
    normalized = name.replace("raw_", "")
    for key in key_order:
        lookup = "sea" if key == "seafarer" else key
        if key in normalized:
            return BOWL_PALETTES[lookup]
    return BOWL_PALETTES["default"]


def add_content_pixel(img: Image.Image, x: int, y: int, col: tuple[int, int, int]) -> None:
    if (x, y) in BOWL_CONTENT_MASK:
        img.putpixel((x, y), (*col, 255))


def make_bowl_texture(name: str) -> Image.Image:
    bowl = read_rgba("bowl.png")
    out = bowl.copy()
    base, hi, dark = cuisine_palette(name)
    raw = name.startswith("raw_")
    if raw:
        base = adjust(base, 0.72)
        hi = adjust(hi, 0.85)
        dark = adjust(dark, 0.75)
    # Base broth/filling in the exact conservative vanilla soup mask.
    for x, y in BOWL_CONTENT_MASK:
        col = base
        if y <= 7:
            col = tuple(clamp(base[i] * 1.08) for i in range(3))
        if x <= 4 or x >= 11 or y >= 9:
            col = tuple(clamp((base[i] * 2 + dark[i]) / 3) for i in range(3))
        out.putpixel((x, y), (*col, 255))
    rnd = stable_int(name)
    bits = [(rnd >> i) & 0xFF for i in range(0, 32, 8)]
    # Ingredient garnish, always inside the mask.
    garnish = [
        (5 + bits[0] % 3, 7 + bits[1] % 2, hi),
        (9 + bits[2] % 2, 8 + bits[3] % 2, dark),
    ]
    # Give rare/special dishes a tiny bright accent, not a new container.
    if any(k in name for k in ("golden", "star", "soul", "wither", "blue_ice", "resin", "honeycomb")):
        garnish.append((8, 7, adjust(hi, 1.18)))
    if "water" in name:
        garnish = [(7, 7, hi), (10, 8, adjust(hi, 1.05))]
    for x, y, col in garnish:
        add_content_pixel(out, x, y, col)
        # a tiny 2-pixel clump for food, one-pixel glint for water
        if "water" not in name:
            add_content_pixel(out, x + 1, y, adjust(col, 0.85))
    return out


def write_item_definition(namespace_dir: Path, item_name: str, model_ns: str) -> None:
    items_dir = namespace_dir / "items"
    items_dir.mkdir(parents=True, exist_ok=True)
    path = items_dir / f"{item_name}.json"
    path.write_text(json.dumps({"model": {"type": "minecraft:model", "model": f"{model_ns}:item/{item_name}"}}, indent=2) + "\n")


def update_model_texture(model_file: Path, texture: str) -> None:
    data = json.loads(model_file.read_text())
    data.setdefault("parent", "minecraft:item/generated")
    data["textures"] = {"layer0": texture}
    model_file.write_text(json.dumps(data, indent=2) + "\n")


def generate_essences() -> int:
    model_dir = ROOT / "alchemy/src/main/resources/assets/herbcraft/models/item"
    texture_dir = ROOT / "alchemy/src/main/resources/assets/herbcraft/textures/item"
    asset_ns = ROOT / "alchemy/src/main/resources/assets/herbcraft"
    texture_dir.mkdir(parents=True, exist_ok=True)
    count = 0
    for model in sorted(model_dir.glob("essence_*.json")):
        name = model.stem
        color = essence_color(name)
        rare = any(k in name for k in SPECIAL_SPARKLE_KEYWORDS)
        img = composite_essence(color, rare=rare)
        img.save(texture_dir / f"{name}.png")
        update_model_texture(model, f"herbcraft:item/{name}")
        write_item_definition(asset_ns, name, "herbcraft")
        count += 1
    return count


def generate_cuisine() -> int:
    model_dir = ROOT / "cuisine/src/main/resources/assets/herbcraft_cuisine/models/item"
    texture_dir = ROOT / "cuisine/src/main/resources/assets/herbcraft_cuisine/textures/item"
    texture_dir.mkdir(parents=True, exist_ok=True)
    count = 0
    for model in sorted(model_dir.glob("*.json")):
        name = model.stem
        img = make_bowl_texture(name)
        img.save(texture_dir / f"{name}.png")
        update_model_texture(model, f"herbcraft_cuisine:item/{name}")
        count += 1
    return count


def generate_adhesives() -> int:
    # Keep vanilla-like bases but make the three adhesive items distinct and self-contained.
    mapping = {
        "blank_adhesive": ("slime_ball.png", (225, 239, 198)),
        "amethyst_adhesive": ("amethyst_shard.png", (204, 135, 255)),
        "echo_adhesive": ("echo_shard.png", (79, 222, 214)),
    }
    model_dir = ROOT / "alchemy/src/main/resources/assets/herbcraft/models/item"
    texture_dir = ROOT / "alchemy/src/main/resources/assets/herbcraft/textures/item"
    texture_dir.mkdir(parents=True, exist_ok=True)
    for name, (base_png, accent) in mapping.items():
        img = read_rgba(base_png)
        # Minimal accent pixels; no model points to vanilla texture directly.
        for x, y in [(6, 6), (9, 7), (7, 10)]:
            r, g, b, a = img.getpixel((x, y))
            if a:
                img.putpixel((x, y), (*accent, a))
        img.save(texture_dir / f"{name}.png")
        update_model_texture(model_dir / f"{name}.json", f"herbcraft:item/{name}")
    return len(mapping)



def generate_core_items() -> int:
    texture_dir = ROOT / "core/src/main/resources/assets/herbcraft/textures/item"
    model_dir = ROOT / "core/src/main/resources/assets/herbcraft/models/item"
    texture_dir.mkdir(parents=True, exist_ok=True)

    codex = read_rgba("written_book.png")
    # Minimal herbal identity marks, preserving vanilla book silhouette.
    for x, y, col in [
        (6, 6, (73, 142, 68)), (7, 5, (97, 174, 85)), (8, 6, (73, 142, 68)),
        (7, 7, (43, 94, 48)), (10, 9, (205, 176, 71)),
    ]:
        if codex.getpixel((x, y))[3]:
            codex.putpixel((x, y), (*col, 255))
    codex.save(texture_dir / "herbal_codex.png")
    update_model_texture(model_dir / "herbal_codex.json", "herbcraft:item/herbal_codex")

    page = read_rgba("paper.png")
    # Tattered herbal note: tiny green/brown glyphs only on existing paper pixels.
    for x, y, col in [
        (5, 5, (72, 128, 62)), (6, 6, (72, 128, 62)), (7, 5, (102, 158, 70)),
        (10, 6, (116, 82, 46)), (5, 9, (116, 82, 46)), (8, 10, (72, 128, 62)),
    ]:
        if page.getpixel((x, y))[3]:
            page.putpixel((x, y), (*col, 255))
    page.save(texture_dir / "tattered_page.png")
    update_model_texture(model_dir / "tattered_page.json", "herbcraft:item/tattered_page")
    return 2

def validate_container_pixels() -> None:
    bowl = read_rgba("bowl.png")
    tex_dir = ROOT / "cuisine/src/main/resources/assets/herbcraft_cuisine/textures/item"
    for png in tex_dir.glob("*.png"):
        img = Image.open(png).convert("RGBA")
        for y in range(16):
            for x in range(16):
                if (x, y) not in BOWL_CONTENT_MASK and img.getpixel((x, y)) != bowl.getpixel((x, y)):
                    raise SystemExit(f"Bowl container pixel changed outside content mask: {png.name} at {(x, y)}")


def main() -> None:
    ensure_vanilla_assets()
    e = generate_essences()
    c = generate_cuisine()
    a = generate_adhesives()
    core = generate_core_items()
    validate_container_pixels()
    print(f"Generated {e} essence bottle textures, {c} cuisine bowl textures, {a} adhesive textures, {core} core textures.")


if __name__ == "__main__":
    main()
