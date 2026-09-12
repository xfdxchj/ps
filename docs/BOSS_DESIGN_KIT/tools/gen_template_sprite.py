#!/usr/bin/env python3
"""
生成占位 Boss 贴图（带网格/帧号/方向标记），方便先把流程跑通。

用法：
    python gen_template_sprite.py --name myboss
    python gen_template_sprite.py --name myboss --frames 10 --size 16
    python gen_template_sprite.py --name bigboss --size 32 --frames 8
    python gen_template_sprite.py --examples        # 生成 3 张示例图

输出到 ../examples/sprites/<name>.png
"""
import argparse
import os
import math

try:
    from PIL import Image, ImageDraw
except ImportError:
    raise SystemExit("需要 Pillow：pip install Pillow")

OUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "examples", "sprites")

# 帧类型配色（让占位图一眼看出每帧用途）
FRAME_KINDS = [
    ("idle",   ( 90, 160, 255)),   # 蓝
    ("idle",   (110, 180, 255)),
    ("run",    ( 90, 220, 140)),   # 绿
    ("run",    (110, 240, 160)),
    ("attack", (255, 120,  90)),   # 红
    ("attack", (255, 150, 110)),
    ("attack", (255, 180, 130)),
    ("die",    (170, 120, 200)),   # 紫
    ("die",    (150, 100, 180)),
    ("die",    (130,  80, 160)),
]


def make_sprite(name, frames=10, size=16, cols=16):
    """生成一张 16 列的多帧占位图。"""
    frames = min(frames, cols)
    w = cols * size
    h = size          # 单行
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    for i in range(cols):
        x0, y0 = i * size, 0
        x1, y1 = x0 + size - 1, h - 1

        if i < frames:
            kind, color = FRAME_KINDS[i % len(FRAME_KINDS)]
            # 半透明填充
            d.rectangle([x0, y0, x1, y1], fill=color + (170,), outline=(20, 20, 20, 255))
            # 内部圆点表示"这帧有内容"
            cx, cy = x0 + size // 2, y0 + size // 2
            r = max(2, size // 5)
            d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(255, 255, 255, 230))
            # 帧号（小尺寸时不画）
            if size >= 16:
                d.text((x0 + 1, y1 - 7), str(i), fill=(255, 255, 255, 255))
        else:
            # 空帧：只画淡边框，表示"未使用"
            d.rectangle([x0, y0, x1, y1], outline=(120, 120, 120, 90))
        # 网格线
        d.line([x0, y0, x0, y1], fill=(0, 0, 0, 60))

    path = os.path.join(OUT_DIR, f"{name}.png")
    os.makedirs(OUT_DIR, exist_ok=True)
    img.save(path)
    print(f"  OK  {path}  ({img.width}x{img.height}, {frames} 帧, {size}px)")
    return path


def make_examples():
    print("生成示例贴图：")
    make_sprite("example_10frame", frames=10, size=16)
    make_sprite("example_static",  frames=1,  size=16)
    make_sprite("example_32px",    frames=8,  size=32)
    print("\n说明：")
    print("  example_10frame  - 标准 10 帧动画（idle2 + run2 + attack3 + die3）")
    print("  example_static   - 单帧静态（所有动作都用帧 0）")
    print("  example_32px     - 32x32 大 Boss（代码里 TextureFilm 要用 32,32）")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--name", default="myboss", help="输出文件名（不含扩展名）")
    ap.add_argument("--frames", type=int, default=10, help="有效帧数（1-16）")
    ap.add_argument("--size", type=int, default=16, help="每格像素（16 或 32）")
    ap.add_argument("--examples", action="store_true", help="生成全部示例图")
    a = ap.parse_args()

    if a.examples:
        make_examples()
    else:
        make_sprite(a.name, frames=a.frames, size=a.size)
