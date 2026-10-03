# 五条悟替换主角 · 出图提示词

> 目标：用五条悟替换 Shattered Pixel Dungeon 的主角贴图，**不做护甲差分**（一套外观通吃）。

## 1. 游戏需要的最终格式

- 文件：`sprites/warrior.png`（或你玩的职业对应文件）
- 尺寸：**256 × 128**
- 单元格：**12 × 15**，横向 **21 列**（实际用 252px，右边 4px 空着），纵向 **8 行**
- 每行 = 一套护甲外观。既然不做差分，**8 行画成完全一样**即可（我也可以帮你把 1 行复制成 8 行）。
- 因此你最少只需要交 **一条 252×15 的横条（21 帧）**，我来复制成整张。

### 21 帧的用途（从左到右列号 0-20）

| 列 | 用途 |
|---|---|
| 0-1 | 待机（呼吸两帧） |
| 2-7 | 行走 6 帧 |
| 8-12 | 死亡 5 帧（倒地） |
| 13-15 | 攻击 3 帧 |
| 16-17 | 操作/施法 2 帧 |
| 18 | 漂浮（1 帧） |
| 19-20 | 阅读 2 帧 |

## 2. 建议先出高分辨率，我再缩小

12×15 太小，AI 画不了。**放大 12 倍**出图，我再按邻近采样缩回：

- 单帧：144 × 180
- 21 帧横条：3024 × 180（太宽）
- 更推荐 **7 列 × 3 行** 网格：约 1008 × 540（每格 144×180）

## 3. 提示词（直接粘给绘图 AI）

**中文版：**

> 像素画角色精灵图，主角是《咒术回战》的五条悟：白色刺猬头、黑色眼罩（或露眼蓝瞳）、黑色高领制服、白色领口，无护甲。俯视 3/4 视角，类似 Shattered Pixel Dungeon 的风格。**7 列 × 3 行共 21 个姿势**，按顺序：
> 第 1 行：待机2个（正面站立、轻微呼吸）、行走6个（迈步循环）；
> 第 2 行：死亡5个（从站立到倒地）、攻击3个（抬手释放术式）；
> 第 3 行：施法2个、漂浮1个、阅读2个。
> 纯色透明背景，不要网格线、不要文字、不要阴影、不要抗锯齿，像素边缘清晰，有限色板。每个角色在格子内水平居中、脚底对齐。

**English version:**

> Pixel-art character sprite sheet of Gojo Satoru from Jujutsu Kaisen: spiky white hair, black blindfold (or bright blue eyes), black high-collar uniform with white trim, no armor. Top-down 3/4 view, matching the style of Shattered Pixel Dungeon. **7 columns x 3 rows, 21 poses**, in this order:
> row 1: 2 idle (facing front, subtle breathing), 6 walk-cycle frames;
> row 2: 5 death frames (standing to lying down), 3 attack frames (raising hand to cast);
> row 3: 2 cast frames, 1 floating frame, 2 reading frames.
> Transparent background, no grid lines, no text, no drop shadow, no anti-aliasing, crisp pixel edges, limited palette. Each character centered horizontally and feet aligned to the bottom of its cell.

**负面提示词：**

> 不要网格线、不要文字/水印、不要背景、不要真实照片感、不要 3D 渲染、不要模糊/抗锯齿、不要护甲造型、不要多余角色、不要改变五官年龄。

## 4. 我拿到图后做的事

1. 按格子切图 → 邻近缩放到 12×15；
2. 按上表重排成 21 帧横条；
3. 复制成 256×128 的 8 行；
4. 覆盖对应职业的 `sprites/*.png`。
5. 如果配色/像素对不齐，我会再给你“对齐后预览图”确认。

## 5. 模板

参考 `预览/gojo_hero_template.png`：这就是目标网格（放大 6 倍），列号与用途都标好了，出图时可以照着排。
