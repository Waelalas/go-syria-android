"""
Generate Go Syria Android launcher icons.
Design: navy bg + gold location pin + dark blue car with white windshield.
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
RES = ROOT / "app" / "src" / "main" / "res"
SIZES = {
    "mipmap-mdpi":    48,
    "mipmap-hdpi":    72,
    "mipmap-xhdpi":   96,
    "mipmap-xxhdpi":  144,
    "mipmap-xxxhdpi": 192,
}
ADAPTIVE_FOREGROUND_SIZES = {
    "mipmap-mdpi":    108,
    "mipmap-hdpi":    162,
    "mipmap-xhdpi":   216,
    "mipmap-xxhdpi":  324,
    "mipmap-xxxhdpi": 432,
}

NAVY    = (10,  22,  40,  255)
GOLD    = (210, 170,  40,  255)
GOLD_D  = (160, 128,  20,  255)
CAR     = (13,  34,  72,  255)
SHADOW  = (30,  70, 130,  140)
WHITE   = (215, 230, 255, 220)
GOLD_HL = (240, 210,  90,  255)


def draw_icon(size, background=NAVY):
    # Work at 4x for anti-aliasing, then downscale
    W = size * 4
    img = Image.new("RGBA", (W, W), background)
    d = ImageDraw.Draw(img)
    s = W / 192.0

    cx = W / 2.0
    cy = W / 2.0

    # ── Shadow ellipse ────────────────────────────────────────────────────────
    sh_w, sh_h = 88*s, 18*s
    sh_x = cx - sh_w/2
    sh_y = cy + 64*s
    d.ellipse([sh_x, sh_y, sh_x+sh_w, sh_y+sh_h], fill=SHADOW)

    # ── Gold pin: outer circle ────────────────────────────────────────────────
    pin_r  = 54*s
    pin_cx = cx
    pin_cy = cy - 8*s
    d.ellipse([pin_cx-pin_r, pin_cy-pin_r, pin_cx+pin_r, pin_cy+pin_r], fill=GOLD)

    # Gold pin: tail (triangle)
    tip_y = pin_cy + 86*s
    t_half = 28*s
    tail = [
        (pin_cx - t_half, pin_cy + 20*s),
        (pin_cx + t_half, pin_cy + 20*s),
        (pin_cx,           tip_y),
    ]
    d.polygon(tail, fill=GOLD)
    # Darker inner triangle for depth
    dark_tail = [
        (pin_cx - t_half*0.55, pin_cy + 32*s),
        (pin_cx + t_half*0.55, pin_cy + 32*s),
        (pin_cx,                tip_y),
    ]
    d.polygon(dark_tail, fill=GOLD_D)

    # Highlight arc at top of pin
    hl_r = pin_r * 0.85
    hl_bbox = [pin_cx - hl_r, pin_cy - hl_r*0.9, pin_cx + hl_r*0.4, pin_cy]
    d.arc(hl_bbox, start=200, end=310, fill=GOLD_HL, width=int(5*s))

    # ── Inner navy circle (hole in pin) ──────────────────────────────────────
    inner_r = 37*s
    d.ellipse([pin_cx-inner_r, pin_cy-inner_r, pin_cx+inner_r, pin_cy+inner_r], fill=NAVY)

    # ── Car silhouette ────────────────────────────────────────────────────────
    # Body
    bw, bh = 58*s, 20*s
    bx = pin_cx - bw/2
    by = pin_cy - bh/2 + 8*s
    d.rounded_rectangle([bx, by, bx+bw, by+bh], radius=int(6*s), fill=CAR)

    # Roof — wide trapezoid / rounded rect centred slightly above body
    rw, rh = bw*0.68, bh*0.85
    rx = pin_cx - rw/2
    ry = by - rh*0.72
    d.rounded_rectangle([rx, ry, rx+rw, ry+rh], radius=int(9*s), fill=CAR)

    # Windshield (white oval on roof)
    ww, wh = rw*0.72, rh*0.52
    wx = pin_cx - ww/2
    wy = ry + rh*0.08
    d.ellipse([wx, wy, wx+ww, wy+wh], fill=WHITE)

    # Front grille line
    gl_y = by + bh - 4*s
    d.line([(bx+10*s, gl_y), (bx+bw-10*s, gl_y)], fill=WHITE, width=max(2, int(2.5*s)))

    # Headlights
    hl_size = 5*s
    for hx in [bx + 6*s, bx + bw - 6*s - hl_size]:
        d.ellipse([hx, by+3*s, hx+hl_size, by+3*s+hl_size], fill=(220, 220, 180, 200))

    # Downscale to target size with LANCZOS
    img = img.resize((size, size), Image.LANCZOS)
    return img


def draw_adaptive_foreground(size):
    art_size = round(size * 0.68)
    art = draw_icon(art_size, background=(0, 0, 0, 0))
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    offset = ((size - art_size) // 2, (size - art_size) // 2)
    out.alpha_composite(art, offset)
    return out


def make_round(img, size):
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size-1, size-1), fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(img, mask=mask)
    return out


print("Generating Go Syria icons...")
for folder, size in SIZES.items():
    out_dir = RES / folder
    out_dir.mkdir(parents=True, exist_ok=True)
    icon = draw_icon(size)
    icon.save(out_dir / "ic_launcher.png", "PNG")
    make_round(icon, size).save(out_dir / "ic_launcher_round.png", "PNG")
    foreground = draw_adaptive_foreground(ADAPTIVE_FOREGROUND_SIZES[folder])
    foreground.save(out_dir / "ic_launcher_foreground.png", "PNG")
    print(f"  {folder}: {size}x{size} icon + adaptive foreground OK")

draw_icon(512).save(ROOT / "play_store_icon_512.png", "PNG")
draw_icon(192).save(ROOT / "icon_preview_192.png", "PNG")
print("\nDone! play_store_icon_512.png + icon_preview_192.png saved.")
