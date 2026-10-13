#!/usr/bin/env python3
from pathlib import Path
from zipfile import ZipFile
from PIL import Image, ImageEnhance, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
CLIENT_JAR = Path.home() / ".gradle/caches/fabric-loom/26.3/minecraft-client.jar"
OUT = ROOT / "core/src/main/resources/assets/herbcraft/textures/mob_effect"
OUT.mkdir(parents=True, exist_ok=True)

EFFECTS = {
    "grain_deficiency": "wheat.png",
    "flesh_deficiency": "cooked_beef.png",
    "oil_deficiency": "honeycomb.png",
    "vege_deficiency": "kelp.png",
    "fruit_deficiency": "melon_slice.png",
    "dietary_imbalance": "suspicious_stew.png",
    "well_nourished": "golden_apple.png",
}


def load_item(name: str) -> Image.Image:
    path = f"assets/minecraft/textures/item/{name}"
    with ZipFile(CLIENT_JAR) as zf:
        with zf.open(path) as f:
            return Image.open(f).convert("RGBA")


def canvas() -> Image.Image:
    return Image.new("RGBA", (18, 18), (0, 0, 0, 0))


def paste_center(base: Image.Image) -> Image.Image:
    img = canvas()
    img.alpha_composite(base.resize((16, 16), Image.Resampling.NEAREST), (1, 1))
    return img


def tint(img: Image.Image, factor: float = 0.72) -> Image.Image:
    # Muted, slightly greyed deficiency look; keeps vanilla silhouette recognizable.
    r, g, b, a = img.split()
    grey = Image.merge("RGB", (r, g, b)).convert("L").convert("RGB")
    rgb = Image.blend(Image.merge("RGB", (r, g, b)), grey, 0.35)
    rgb = ImageEnhance.Brightness(rgb).enhance(factor)
    out = Image.merge("RGBA", (*rgb.split(), a))
    return out


def mirror_left_to_right(img: Image.Image) -> None:
    """Make the vanilla-item base visually symmetrical before adding status arrows.

    The first generated icons used bite notches on the right side; in the 18x18
    effect HUD that looked like broken/missing pixels rather than intentional
    damage. Mirroring the intact left half keeps the vanilla silhouette stable
    and lets the red/green arrow carry the status meaning.
    """
    px = img.load()
    for y in range(18):
        for x in range(9, 18):
            px[x, y] = px[17 - x, y]


def red_down_arrow(img: Image.Image) -> None:
    red = (220, 28, 28, 255)
    dark = (92, 0, 0, 255)
    draw = ImageDraw.Draw(img)
    # Complete lower-right down arrow: outlined stem plus a filled triangular head.
    draw.rectangle((12, 7, 16, 13), fill=dark)
    draw.polygon([(10, 12), (17, 12), (14, 17)], fill=dark)
    draw.rectangle((13, 8, 15, 12), fill=red)
    draw.polygon([(11, 13), (16, 13), (14, 16)], fill=red)


def add_cracks(img: Image.Image) -> None:
    px = img.load()
    dark = (78, 45, 24, 220)
    for x, y in [(5, 5), (6, 6), (7, 7), (8, 8), (9, 8), (10, 9), (11, 10), (7, 8), (6, 9), (5, 10)]:
        if 0 <= x < 18 and 0 <= y < 18 and px[x, y][3] > 0:
            px[x, y] = dark


def make_deficiency(item: str, bite=True, cracks=False) -> Image.Image:
    img = paste_center(tint(load_item(item)))
    mirror_left_to_right(img)
    if cracks:
        add_cracks(img)
    red_down_arrow(img)
    return img


def make_imbalance() -> Image.Image:
    base = load_item(EFFECTS["dietary_imbalance"]).resize((16, 16), Image.Resampling.NEAREST)
    base = ImageEnhance.Brightness(base).enhance(0.85)
    img = canvas()
    img.alpha_composite(base, (1, 1))
    mirror_left_to_right(img)
    px = img.load()
    warn = (230, 170, 35, 255)
    dark = (80, 40, 0, 255)
    for x, y in [(13, 4), (14, 4), (12, 5), (15, 5), (12, 6), (15, 6), (13, 7), (14, 7), (13, 8), (14, 8), (13, 10), (14, 10)]:
        px[x, y] = dark
    for x, y in [(13, 5), (14, 5), (13, 6), (14, 6), (13, 7), (14, 7), (13, 9), (14, 9)]:
        px[x, y] = warn
    return img


def make_well_nourished() -> Image.Image:
    img = paste_center(load_item(EFFECTS["well_nourished"]))
    mirror_left_to_right(img)
    px = img.load()
    green = (70, 210, 90, 255)
    dark = (15, 85, 25, 255)
    draw = ImageDraw.Draw(img)
    # Complete lower-right up arrow, opposite to deficiency arrows.
    draw.rectangle((12, 6, 16, 13), fill=dark)
    draw.polygon([(10, 8), (17, 8), (14, 3)], fill=dark)
    draw.rectangle((13, 8, 15, 13), fill=green)
    draw.polygon([(11, 7), (16, 7), (14, 4)], fill=green)
    # Five tiny nutrition dots along the top edge.
    dots = [(3, 2, (226, 194, 92, 255)), (5, 2, (180, 80, 80, 255)), (7, 2, (222, 190, 88, 255)), (9, 2, (80, 170, 80, 255)), (11, 2, (210, 80, 150, 255))]
    for x, y, c in dots:
        px[x, y] = c
    return img


def main():
    if not CLIENT_JAR.exists():
        raise SystemExit(f"Missing Minecraft client jar: {CLIENT_JAR}")
    images = {
        "grain_deficiency": make_deficiency(EFFECTS["grain_deficiency"], bite=True),
        "flesh_deficiency": make_deficiency(EFFECTS["flesh_deficiency"], bite=True),
        "oil_deficiency": make_deficiency(EFFECTS["oil_deficiency"], bite=False, cracks=True),
        "vege_deficiency": make_deficiency(EFFECTS["vege_deficiency"], bite=True),
        "fruit_deficiency": make_deficiency(EFFECTS["fruit_deficiency"], bite=True),
        "dietary_imbalance": make_imbalance(),
        "well_nourished": make_well_nourished(),
    }
    for name, img in images.items():
        img.save(OUT / f"{name}.png")
    print(f"Generated {len(images)} Herbcraft nutrition effect icons in {OUT}")


if __name__ == "__main__":
    main()
