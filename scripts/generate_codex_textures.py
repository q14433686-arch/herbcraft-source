#!/usr/bin/env python3
"""
Generate all Codex / Nutrition panel UI textures as Minecraft-style pixel art.
Style: aged xuan paper (做旧宣纸), bamboo border (竹节边框), scroll/ribbon header (卷轴绸带).
All textures are 16x16 or 32x32 pixel art, RGBA, matching Minecraft vanilla style.
"""

from PIL import Image
import random
import os

OUT = "core/src/main/resources/assets/herbcraft/textures/gui"

def ensure_dir(path):
    os.makedirs(os.path.dirname(path), exist_ok=True)

def save(img, name):
    path = os.path.join(OUT, name)
    ensure_dir(path)
    img.save(path)
    print(f"  wrote {path} ({img.width}x{img.height})")

# --- Color palette ---
# Aged xuan paper tones
PAPER_BASE     = (228, 218, 192, 255)   # warm cream
PAPER_LIGHT    = (235, 225, 200, 255)   # lighter patch
PAPER_DARK     = (215, 205, 180, 255)   # slightly darker fiber
PAPER_SPECK    = (190, 175, 150, 255)   # age spot
PAPER_LEFT     = (210, 200, 178, 255)   # left panel: slightly darker paper

# Bamboo tones
BAMBOO_LIGHT   = (168, 148, 100, 255)   # bamboo highlight
BAMBOO_MID     = (138, 118, 75, 255)    # bamboo body
BAMBOO_DARK    = (108, 90, 58, 255)     # bamboo shadow
BAMBOO_NODE    = (98, 80, 50, 255)      # bamboo node/knot
BAMBOO_RING    = (120, 100, 65, 255)    # node ring

# Scroll/ribbon header tones (暗红绸带)
RIBBON_DARK    = (88, 30, 28, 255)      # deep dark red
RIBBON_MID     = (108, 42, 38, 255)     # dark red body
RIBBON_LIGHT   = (128, 55, 48, 255)     # red highlight
RIBBON_EDGE    = (72, 24, 22, 255)      # shadow edge
RIBBON_SHEEN   = (145, 68, 58, 255)     # silk sheen

# Scrollbar tones
SCROLL_TRACK   = (180, 165, 140, 128)   # semi-transparent warm
SCROLL_THUMB   = (138, 118, 75, 220)    # bamboo colored thumb

# Corner decoration
CORNER_GOLD    = (185, 155, 85, 255)    # brass/gold fitting
CORNER_DARK    = (110, 88, 50, 255)     # dark accent

TRANSPARENT    = (0, 0, 0, 0)


def gen_paper_background():
    """16x16 tileable aged xuan paper."""
    random.seed(42)  # reproducible
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            r = random.random()
            if r < 0.06:
                c = PAPER_SPECK
            elif r < 0.25:
                c = PAPER_DARK
            elif r < 0.55:
                c = PAPER_BASE
            else:
                c = PAPER_LIGHT
            img.putpixel((x, y), c)
    # Add a couple of subtle diagonal fiber streaks
    for i in range(16):
        x = (i * 3 + 5) % 16
        y = (i * 3 + 2) % 16
        if random.random() < 0.4:
            cur = img.getpixel((x, y))
            darker = tuple(max(0, c - 8) for c in cur[:3]) + (255,)
            img.putpixel((x, y), darker)
    save(img, "codex_background.png")

def gen_left_panel_bg():
    """16x16 tileable left panel paper (slightly darker/aged)."""
    random.seed(99)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            r = random.random()
            if r < 0.08:
                c = PAPER_SPECK
            elif r < 0.35:
                c = PAPER_LEFT
            elif r < 0.65:
                c = PAPER_DARK
            else:
                c = PAPER_BASE
            img.putpixel((x, y), c)
    save(img, "codex_left_panel.png")

def gen_header():
    """32x16 scroll/ribbon header (horizontally tileable)."""
    img = Image.new("RGBA", (32, 16))
    for y in range(16):
        for x in range(32):
            if y == 0 or y == 15:
                c = RIBBON_EDGE
            elif y == 1 or y == 14:
                c = RIBBON_DARK
            elif y == 2 or y == 13:
                c = RIBBON_MID
            elif y == 7:
                # Silk sheen line in the middle
                c = RIBBON_SHEEN if (x + y) % 5 != 0 else RIBBON_LIGHT
            elif y in (3, 4, 5, 6, 8, 9, 10, 11, 12):
                c = RIBBON_MID if (x + y) % 7 != 0 else RIBBON_LIGHT
            else:
                c = RIBBON_MID
            img.putpixel((x, y), c)
    # Add subtle diagonal weave pattern
    for y in range(2, 14):
        for x in range(32):
            if (x + y) % 4 == 0:
                cur = img.getpixel((x, y))
                lighter = tuple(min(255, c + 5) for c in cur[:3]) + (255,)
                img.putpixel((x, y), lighter)
    save(img, "codex_header.png")

def gen_divider():
    """4x32 vertical bamboo divider."""
    img = Image.new("RGBA", (4, 32))
    for y in range(32):
        is_node = (y % 16 == 7 or y % 16 == 8)
        for x in range(4):
            if x == 0:
                c = BAMBOO_DARK
            elif x == 3:
                c = BAMBOO_DARK
            elif x == 1:
                c = BAMBOO_LIGHT if not is_node else BAMBOO_RING
            else:  # x == 2
                c = BAMBOO_MID if not is_node else BAMBOO_NODE
            img.putpixel((x, y), c)
    save(img, "codex_divider.png")

def gen_corners():
    """8x8 corner decorations (brass/bamboo fitting)."""
    # Top-left corner
    img = Image.new("RGBA", (8, 8), TRANSPARENT)
    # L-shaped brass corner
    for x in range(6):
        img.putpixel((x, 0), CORNER_DARK)
        img.putpixel((x, 1), CORNER_GOLD)
    for y in range(6):
        img.putpixel((0, y), CORNER_DARK)
        img.putpixel((1, y), CORNER_GOLD)
    # Inner highlight
    img.putpixel((2, 2), CORNER_GOLD)
    img.putpixel((1, 1), CORNER_DARK)
    save(img, "codex_corner_tl.png")

    # Top-right (mirror)
    img_tr = img.transpose(Image.FLIP_LEFT_RIGHT)
    save(img_tr, "codex_corner_tr.png")

    # Bottom-left (mirror)
    img_bl = img.transpose(Image.FLIP_TOP_BOTTOM)
    save(img_bl, "codex_corner_bl.png")

    # Bottom-right (mirror)
    img_br = img.transpose(Image.FLIP_LEFT_RIGHT).transpose(Image.FLIP_TOP_BOTTOM)
    save(img_br, "codex_corner_br.png")

def gen_scrollbar():
    """Scrollbar track (4x16) and thumb (4x8)."""
    # Track: semi-transparent warm
    track = Image.new("RGBA", (4, 16))
    for y in range(16):
        for x in range(4):
            if x == 0 or x == 3:
                track.putpixel((x, y), (160, 145, 120, 64))
            else:
                track.putpixel((x, y), SCROLL_TRACK)
    save(track, "codex_scrollbar_track.png")

    # Thumb: bamboo colored, solid
    thumb = Image.new("RGBA", (4, 8))
    for y in range(8):
        for x in range(4):
            if y == 0 or y == 7:
                thumb.putpixel((x, y), BAMBOO_DARK)
            elif x == 0 or x == 3:
                thumb.putpixel((x, y), BAMBOO_MID)
            else:
                thumb.putpixel((x, y), BAMBOO_LIGHT)
    save(thumb, "codex_scrollbar_thumb.png")

def gen_nutrition_panel_bg():
    """16x16 tileable nutrition panel background (slightly darker parchment)."""
    random.seed(77)
    img = Image.new("RGBA", (16, 16))
    dark_base = (45, 38, 30, 220)
    dark_light = (52, 44, 35, 220)
    dark_darker = (38, 32, 25, 220)
    for y in range(16):
        for x in range(16):
            r = random.random()
            if r < 0.3:
                c = dark_darker
            elif r < 0.6:
                c = dark_base
            else:
                c = dark_light
            img.putpixel((x, y), c)
    save(img, "nutrition_panel_bg.png")

def gen_back_button():
    """16x16 back/return arrow icon."""
    img = Image.new("RGBA", (16, 16), TRANSPARENT)
    arrow_color = BAMBOO_MID
    # Simple left-pointing arrow: < shape
    #   col 3-4: tip
    #   col 5-6: mid
    #   col 7-10: shaft
    for row, cols in [
        (7, [3, 4]),
        (6, [5, 6]),    (8, [5, 6]),
        (5, [7]),       (9, [7]),
        (4, [8]),       (10, [8]),
    ]:
        for c in cols:
            img.putpixel((c, row), arrow_color)
    # Horizontal shaft
    for x in range(5, 12):
        img.putpixel((x, 7), arrow_color)
    save(img, "codex_back_button.png")


if __name__ == "__main__":
    print("Generating Codex UI textures (pixel art style)...")
    gen_paper_background()
    gen_left_panel_bg()
    gen_header()
    gen_divider()
    gen_corners()
    gen_scrollbar()
    gen_nutrition_panel_bg()
    gen_back_button()
    print("Done. All textures written to", OUT)
