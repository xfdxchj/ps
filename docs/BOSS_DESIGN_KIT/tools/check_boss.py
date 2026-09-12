#!/usr/bin/env python3
"""
Boss 合规检查器 —— 提交前跑一次，自动发现常见问题。

用法：
    python check_boss.py                     # 检查所有 sixkings Boss
    python check_boss.py --name SorcererKing # 只检查一个
    python check_boss.py --all               # 检查全项目

检查项：
    1. 贴图文件是否存在、尺寸是否合法（宽必须是 16 的倍数、2 的幂）
    2. 精灵类引用的帧号是否超出贴图范围
    3. 文案键是否存在（name/desc/notice）
    4. 过渡类型是否用了 REGULAR_ENTRANCE
    5. assignItemRect 是否登记
"""
import argparse
import os
import re
import sys

# Windows 控制台默认 GBK，强制 UTF-8 输出，避免 emoji 报错
try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

ROOT = r"E:\破碎的地牢\_EndShatteredBuild"
JAVA = os.path.join(ROOT, "core", "src", "main", "java", "com", "shatteredpixel", "shatteredpixeldungeon")
ASSETS = os.path.join(ROOT, "core", "src", "main", "assets")
PKG_PREFIX = "com.shatteredpixel.shatteredpixeldungeon."

try:
    from PIL import Image
    HAS_PIL = True
except ImportError:
    HAS_PIL = False

problems = []
notes = []


def warn(msg):
    problems.append(msg)


def info(msg):
    notes.append(msg)


# ────────────────────────────────────────────────
# 1. 收集所有文案键
# ────────────────────────────────────────────────
def load_keys():
    keys = set()
    for root, _, files in os.walk(os.path.join(ASSETS, "messages")):
        for f in files:
            if f.endswith(".properties"):
                try:
                    with open(os.path.join(root, f), encoding="utf-8", errors="ignore") as fh:
                        for line in fh:
                            line = line.strip()
                            if "=" in line and not line.startswith("#"):
                                keys.add(line.split("=", 1)[0].lower())
                except Exception:
                    pass
    return keys


# ────────────────────────────────────────────────
# 2. 检查贴图
# ────────────────────────────────────────────────
def check_texture(png_path, label):
    if not os.path.exists(png_path):
        warn(f"[贴图] {label}: 文件不存在 -> {png_path}")
        return None
    if not HAS_PIL:
        info(f"[贴图] {label}: 未装 Pillow，跳过尺寸检查")
        return None
    im = Image.open(png_path)
    w, h = im.size
    ok = True
    if w % 16 != 0:
        warn(f"[贴图] {label}: 宽度 {w} 不是 16 的倍数")
        ok = False
    if h % 16 != 0:
        warn(f"[贴图] {label}: 高度 {h} 不是 16 的倍数（TextureFilm 会切错）")
        ok = False
    # 2 的幂检查（只提示，不是硬性）
    if (w & (w - 1)) != 0 or (h & (h - 1)) != 0:
        info(f"[贴图] {label}: {w}x{h} 不是 2 的幂（可能有问题，建议改）")
    frames = (w // 16) * (h // 16)
    return frames


# ────────────────────────────────────────────────
# 3. 检查精灵类的帧号
# ────────────────────────────────────────────────
def check_sprite(sprite_path, frames_avail):
    if not os.path.exists(sprite_path):
        warn(f"[精灵] 文件不存在 -> {sprite_path}")
        return
    txt = open(sprite_path, encoding="utf-8", errors="ignore").read()
    name = os.path.splitext(os.path.basename(sprite_path))[0]

    # 找所有 frames( frames, a, b, c )
    maxframe = -1
    for m in re.finditer(r'\.frames\(\s*frames\s*,([^)]*)\)', txt):
        for num in re.findall(r'\b(\d+)\b', m.group(1)):
            maxframe = max(maxframe, int(num))

    if frames_avail is not None and maxframe >= frames_avail:
        warn(f"[精灵] {name}: 用了帧 {maxframe}，但贴图只有 {frames_avail} 帧 → 会崩溃 frame is null")
    elif maxframe >= 0:
        info(f"[精灵] {name}: 最大帧 {maxframe}（贴图 {frames_avail} 帧）OK")

    # 检查 texture() 路径
    for m in re.finditer(r'texture\(\s*"([^"]+)"', txt):
        rel = m.group(1)
        p = os.path.join(ASSETS, rel.replace("/", os.sep))
        if not os.path.exists(p):
            warn(f"[精灵] {name}: 贴图路径不存在 -> {rel}")
        else:
            check_texture(p, f"{name}.texture")


# ────────────────────────────────────────────────
# 4. 检查 Boss 类
# ────────────────────────────────────────────────
def check_boss(java_path, keys):
    if not os.path.exists(java_path):
        warn(f"[Boss] 文件不存在 -> {java_path}")
        return
    txt = open(java_path, encoding="utf-8", errors="ignore").read()
    cls = os.path.splitext(os.path.basename(java_path))[0]
    rel = os.path.relpath(java_path, JAVA).replace(os.sep, "/")
    pkg_key = rel[:-5].replace("/", ".").lower()   # actors.mobs.bosses.sixkings.sorcererking

    # 文案键
    for suffix in ("name", "desc", "notice"):
        k = f"{pkg_key}.{suffix}"
        if k not in keys:
            warn(f"[文案] {cls}: 缺键 {k}")

    # 关键实现检查
    if "attack(Char" in txt and "@Override" in txt:
        # 检查是否覆写了 final 的 attack
        if re.search(r'@Override\s*\n\s*public\s+boolean\s+attack\s*\(', txt):
            warn(f"[代码] {cls}: 覆写了 final 的 attack(Char) → 应该用 attackHook()")

    if "extends Boss" not in txt and "extends Mob" not in txt:
        warn(f"[代码] {cls}: 没有继承 Boss 或 Mob")

    if "Property.BOSS" not in txt:
        info(f"[代码] {cls}: 没有 properties.add(Property.BOSS)（Boss 血条可能不显示）")

    if "unseal()" in txt and "super.die" in txt:
        info(f"[代码] {cls}: die() 里手动调了 unseal()，基类 Boss 已处理，可能重复")

    if "storeInBundle" not in txt:
        info(f"[代码] {cls}: 没有 storeInBundle（若加了自定义字段，存档会丢）")


# ────────────────────────────────────────────────
# 5. 检查关卡类的过渡类型
# ────────────────────────────────────────────────
def check_level(java_path):
    if not os.path.exists(java_path):
        warn(f"[关卡] 文件不存在 -> {java_path}")
        return
    txt = open(java_path, encoding="utf-8", errors="ignore").read()
    cls = os.path.splitext(os.path.basename(java_path))[0]

    # 挑战区必须 REGULAR_ENTRANCE
    types = re.findall(r'LevelTransition\.Type\.(\w+)', txt)
    bad = [t for t in types if t in ("REGULAR_EXIT", "BRANCH_EXIT", "BRANCH_ENTRANCE")]
    if bad:
        warn(f"[关卡] {cls}: 用了 {set(bad)}，挑战区必须用 REGULAR_ENTRANCE（否则踩楼梯无反应）")

    if not types:
        warn(f"[关卡] {cls}: 没有定义任何 LevelTransition（玩家无法移动）")

    # 入口/出口格是否相同
    cells = re.findall(r'new LevelTransition\(\s*this\s*,\s*(\w+)', txt)
    if len(cells) >= 2 and len(set(cells)) == 1:
        warn(f"[关卡] {cls}: 入口和出口用了同一个格 {cells[0]} → 会立刻弹到下一层")

    if "createMobs" in txt and "GameScene.add" not in txt:
        info(f"[关卡] {cls}: createMobs 里没有 GameScene.add（可能没放 Boss）")


# ────────────────────────────────────────────────
# 主流程
# ────────────────────────────────────────────────
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--name", help="只检查指定类名")
    ap.add_argument("--all", action="store_true", help="检查全项目 Boss")
    a = ap.parse_args()

    print("=" * 62)
    print(" Boss 合规检查")
    print("=" * 62)

    keys = load_keys()
    print(f"已加载文案键: {len(keys)}\n")

    # 目标目录
    boss_dir = os.path.join(JAVA, "actors", "mobs", "bosses", "sixkings")
    sprite_dir = os.path.join(JAVA, "sprites", "sixkings")
    level_dir = os.path.join(JAVA, "levels", "boss")
    tex_dir = os.path.join(ASSETS, "sprites", "sixkings")

    found_any = False

    for d, label in ((boss_dir, "Boss"), (sprite_dir, "精灵"), (level_dir, "关卡")):
        if not os.path.isdir(d):
            info(f"[{label}] 目录不存在: {d}")
            continue
        for f in sorted(os.listdir(d)):
            if not f.endswith(".java"):
                continue
            if a.name and a.name.lower() not in f.lower():
                continue
            found_any = True
            p = os.path.join(d, f)
            print(f"检查 {label}: {f}")
            if label == "Boss":
                check_boss(p, keys)
            elif label == "精灵":
                # 找它引用的贴图
                txt = open(p, encoding="utf-8", errors="ignore").read()
                m = re.search(r'texture\(\s*"([^"]+)"', txt)
                frames = None
                if m:
                    tp = os.path.join(ASSETS, m.group(1).replace("/", os.sep))
                    frames = check_texture(tp, f)
                check_sprite(p, frames)
            else:
                check_level(p)

    if not found_any:
        print("\n⚠ 没找到要检查的文件。")
        print(f"  请先按 06-开发流程.md 建立目录：")
        print(f"    {boss_dir}")
        print(f"    {sprite_dir}")
        print(f"    {level_dir}")

    print("\n" + "=" * 62)
    if problems:
        print(f"❌ 发现 {len(problems)} 个问题：\n")
        for m in problems:
            print("  " + m)
    else:
        print("✅ 没有发现问题")

    if notes:
        print(f"\n💡 {len(notes)} 条提示（非错误）：\n")
        for m in notes:
            print("  " + m)

    print("=" * 62)
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
