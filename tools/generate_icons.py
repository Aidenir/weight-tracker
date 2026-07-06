#!/usr/bin/env python3
"""Generate mipmap PNGs and adaptive-icon foregrounds from the source art.

Run once from the repo root:
    python3 tools/generate_icons.py
"""

from pathlib import Path
from PIL import Image

REPO = Path(__file__).resolve().parent.parent
SRC = REPO / "tools" / "icon-source.png"
RES = REPO / "app" / "src" / "main" / "res"

# Legacy raster: full 494x505 icon rescaled to each density's launcher size.
LEGACY_SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

# Adaptive foreground: full-bleed the icon across the 108dp canvas so the
# launcher's mask (circle / squircle / rounded square) lines up naturally
# with our own rounded rect. The source image already has transparent
# margin around its rounded frame, so a bit of outer clipping is safe.
ADAPTIVE_CANVAS_DP = 108
DENSITY_TO_PXDP = {
    "mdpi": 1,
    "hdpi": 1.5,
    "xhdpi": 2,
    "xxhdpi": 3,
    "xxxhdpi": 4,
}


def main() -> None:
    src = Image.open(SRC).convert("RGBA")
    print(f"Source: {src.size}")

    for density, size in LEGACY_SIZES.items():
        folder = RES / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        legacy = src.resize((size, size), Image.LANCZOS)
        legacy.save(folder / "ic_launcher.png", optimize=True)
        legacy.save(folder / "ic_launcher_round.png", optimize=True)

    for density, ratio in DENSITY_TO_PXDP.items():
        folder = RES / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        canvas_px = int(ADAPTIVE_CANVAS_DP * ratio)
        canvas = src.resize((canvas_px, canvas_px), Image.LANCZOS)
        canvas.save(folder / "ic_launcher_foreground.png", optimize=True)

    print("Done.")


if __name__ == "__main__":
    main()
