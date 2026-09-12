# 方舟移植作战手册（明日方舟地牢 → 终焉 fork）

最后更新：本轮（魔绫 Hollow/Galaxy 已可跑到 33F、楼梯锁问题刚修完，方舟侦察启动前）
定位：**给其他 AI 的施工手册**。方舟 ≠ 魔绫，难度曲线完全不同，务必先读完本文再动手。

---

## 一、先给结论：方舟 ≠ 魔绫，难度曲线完全不同

| | 魔绫 | **方舟（明日方舟地牢）** |
|---|---|---|
| 基础版本 | **接近本 fork**（同代 SPD） | **较老版 SPD**（用 `builders/RegularBuilder` 体系） |
| 包名 | 相同 | **相同**（`com.shatteredpixel.shatteredpixeldungeon`） |
| 与魔绫耦合 | — | 零耦合，完全独立 |
| 源码 | 有 | **需反编译**（已放 `cfr-0.152.jar`，已跑过一次 → `_arknights_src/` 3257 文件） |

**关键判断**：方舟移植**不是"照着搬"**，而是**跨版本适配**。老版 SPD 有很多本 fork 已重构掉的东西（`builders` 体系、`rooms/secret` 等）。

---

## 二、已摸清的情报（可直接用，省几轮）

### 方舟 3 个区 = 3 套独立剧情线

| 区 | 关卡 | Boss 关 |
|---|---|---|
| **伊比利亚 / 海嗣** | `SeaLevel_part1/2` | `SeaBossLevel1` / `SeaBossLevel2` |
| **嘉维尔 / 雨林** | `GavialLevel` / `GavialLevel2` | `GavialBossLevel1` / `GavialBossLevel2` |
| **汐斯塔 / 海滨** | `SiestaLevel_part1/2` | `SiestaBossLevel_part1/part2` |

→ **3 区 × 2 Boss = 6 个 Boss 战关卡**

### 已确认的自包含性（重要）

`SeaBossLevel1` 的依赖**全在方舟内部或原版**：

```
TomorrowRogueNight  ← 方舟自己的类
actors.mobs.SeaBoss1 / SeaObject  ← 方舟 Boss / NPC
levels.CityLevel / HallsLevel     ← 原版关卡（仅继承）
```

**没有一条魔绫依赖。**

### 已知的方舟特色内容

- **海嗣**：`IsharmlaSeabornHead` / `Body` / `Tail`（**分部件 Boss**）
- 武器：`SanktaBet`、`RhodesSword`、`MinosFury`
- 精灵：`Ursus_InfantrySprite`、`SeabornSprite`
- 地形：`SeaPlatform`、`SeaTerror`
- `NewRhodesLevel1-4`、`IberiaPainter`、`IberiaPart1QuestRoom`

---

## 三、硬性规则（血泪教训，务必写进 prompt）

这些是几十轮踩出来的，**每条都真实发生过**：

### 🔴 规则 1：基线必须先验证编译

```
每改一批 → 立刻跑 E:\破碎的地牢\_EndShatteredBuild\tools\localcompile.ps1
```

- 本机**不能跑 gradle**（无网络 / 无 SDK），但 **javac 直编可用**（1458 个源文件）
- **不要攒一大批再编译** —— 错误会叠在一起无法定位

### 🔴 规则 2：`Messages` 键用完整包路径

```java
// Messages.get 会去掉 com.shatteredpixel.shatteredpixeldungeon. 前缀
key = c.getName().replace("com.shatteredpixel.shatteredpixeldungeon.", "") + "." + k;
```

**踩过的坑**：`EndFloorSkip` 在 `endcontent.items` 包，写了 `items.endfloorskip.teleport`，
**正确是 `items.endcontent.items.endfloorskip.teleport`** → 结果显示 `NO TEXT FOUND`。

**→ 每次搬完类，跑 `_tools/scan_all_new_keys.py` 自动检查**

### 🔴 规则 3：图集尺寸必须是 2 的幂

**踩过的坑**：把 `items.png` 从 512 扩到 **640**（=512+128，**非 2 的幂**）→ **所有物品图案全部错乱**。
**正确**：扩到 **1024**（2¹⁰）。

### 🔴 规则 4：`ItemSpriteSheet` 新常量必须配 `assignItemRect`

否则 `frame = null` → **打开背包直接崩溃**。

### 🔴 规则 5：新图标必须验证"有像素"

**踩过的坑**：把 9 个图标放在 `DOCUMENTS` 块的空白格 → **图标全透明 → 物品"看不见"**（但确实在背包里）。
**→ 用 Python/PIL 检查该位置的非透明像素数 > 0**

### 🔴 规则 6：关卡楼梯格号不能靠肉眼数

**踩过的坑**：手工数出 26F 楼梯是格 240，**实际是 279** → 踩楼梯毫无反应。
**→ 用 `_tools/locate_stairs.py` 从 `code_map` 数组自动算格号**

### 🔴 规则 7：`level.locked` 会直接禁止踩楼梯

```java
// Hero.actTransition
} else if (!Dungeon.level.locked && transition != null && transition.inside(pos)) {
```

**踩过的坑**：33F 进关 `seal()` 锁门，但**四柱 Boss 的 `die()` 里没有 `unseal()`** → 打完门还锁着。
**→ Boss 基类里统一 `die() → unseal()` + 进关防御性解锁**

### 🔴 规则 8：资源路径必须核对大小写和目录

| 写错的 | 魔绫真实 |
|---|---|
| `sprites/boss/firedragon.png` | `sprites/boss/`**`fireDragon`**`.png` |
| `sprites/hollow/diedclear.png` | `sprites/boss/`**`DiedElement`**`.png` |
| `sprites/hollow/coldrat.png` | **`sprites/`**`coldrat.png` |

### 🔴 规则 9：写完 .java 不要用 PowerShell `Set-Content`

会写入 **UTF-8 BOM** → javac 报 `非法字符: '\ufeff'`。
**→ 用 `edit`/`write` 工具，或 `New-Object System.Text.UTF8Encoding($false)`**

### 🔴 规则 10：本 fork 缺失的"框架"要先查

本 fork 没有：

- `actors.Boss`（魔绫自造基类）→ 已建精简版
- `Char.DamageType` → 去掉该参数
- `Terrain.SIGN` / `SIGN_SP` / `LAVA`（LAVA 已加）
- `Level.playBossMusic()` / `GameScene.bossReady()`
- `journal.Notes` / `journal.Bestiary` / `Armor.AlowGlyph`
- `custom.testmode`（7163 行）
- 各种 Buff 的 `set(int)`

**→ 写每个类之前，先 `grep` 本 fork 确认符号存在**

---

## 四、任务拆分建议

### 建议的开工顺序（按风险从低到高）

#### 阶段 1：侦察（不需要改代码，产出报告）

1. 用 CFR 反编译（已完成，见 `_arknights_src/`）
2. 统计方舟 3 区的完整类清单：
   - 每个 Boss / 关卡 / NPC / 道具的规模
   - 完整依赖树（递归到原版为止）
3. 对比本 fork，列出【缺失符号清单】
4. 特别标注：哪些依赖"老版 SPD 独有"（builders 体系等）

**产出**：一份 `docs/ARKNIGHTS_PORT_PLAN.md`，含依赖图和风险评级

#### 阶段 2：基础适配层（一次投入，长期受益）

1. 补齐老版 SPD 的 builders 体系（如需要）
2. 补充缺失的 Terrain / Assets 键 / 消息键
3. 用 Python 把方舟图集里需要的图标抠到本 fork 图集（`y=34..38` 有 5 行空白）

#### 阶段 3：单点打通（选风险最低的一个区）

建议从【汐斯塔 Siesta】开始（关卡最少）。
目标：1 个区能从入口走到 Boss 并通关。

#### 阶段 4：复制到其余 2 区

---

## 五、可直接复制的 Prompt（给其他 AI）

````
你是移植工程师。任务：把"明日方舟地牢"MOD 的内容移植到
E:\破碎的地牢\_EndShatteredBuild（终焉 fork，SPD 较新版本）。

【工作目录】
- 目标仓库：E:\破碎的地牢\_EndShatteredBuild
- 方舟反编译源码：E:\破碎的地牢\_arknights_src
- 魔绫参考源码（已完成移植，可参考做法）：
  E:\破碎的地牢\Magic_Ling_Pixel_Dungeon-stable\Magic_Ling_Pixel_Dungeon-stable
- 移植文档（必读）：E:\破碎的地牢\_EndShatteredBuild\docs\CHALLENGE_PORT_PLAN.md
- 诊断工具：E:\破碎的地牢\_tools\ 和 _EndShatteredBuild\tools\

【编译验证 —— 每改一批必须执行】
cd E:\破碎的地牢\_EndShatteredBuild; & .\tools\localcompile.ps1
输出 COMPILE OK 才算通过。
失败时看 E:\破碎的地牢\_javac_out\javac.log（摘要会被截断，必须看全文）。

【铁律（违反会浪费大量返工）】
1. 新类的 Messages 键 = 类的【完整包路径(去掉com.shatteredpixel.shatteredpixeldungeon.)】+ "." + 键名
   例：endcontent.items.EndFloorSkip → items.endcontent.items.endfloorskip.name
   搬完跑 _tools/scan_all_new_keys.py 自检
2. 图集尺寸必须是 2 的幂（512/1024）。改成 640 会让所有图案错乱
3. ItemSpriteSheet 新常量必须 assignItemRect，否则打开背包崩溃
4. 新图标位置必须验证有非透明像素（用 Python/PIL），否则物品"看不见"
5. 关卡楼梯格号用 _tools/locate_stairs.py 自动算，不要肉眼数
6. level.locked=true 时踩楼梯无效；Boss 死了要 unseal()
7. 资源路径要核对大小写/目录，别猜
8. 不用 PowerShell Set-Content 改 .java（会加 BOM 导致 javac 失败）
9. 移植前先 grep 本 fork 确认符号存在（本 fork 无 actors.Boss / Char.DamageType /
   Terrain.SIGN / playBossMusic / bossReady / journal.* / custom.testmode 等）
10. 大文件用「复制源文件 + edit 打补丁」，不要派子代理（会在读源码时耗尽 token）

【第一步任务】
只做侦察，不改代码，产出 E:\破碎的地牢\_EndShatteredBuild\docs\ARKNIGHTS_PORT_PLAN.md：
- 方舟 3 区（Iberia/Gavial/Siesta）的完整类清单与规模
- 每类对本 fork 的【缺失符号清单】（递归依赖）
- 标注"老版 SPD 独有"的依赖（如 builders 体系）
- 给出分阶段实施方案与风险评级
````

---

## 六、结论与建议

**强烈建议其他 AI 先只做"阶段 1 侦察 + 写文档"**，原因：

1. 方舟是**跨版本**的，盲目开搬会重演魔绫 Galaxy 那种"依赖爬坡不收敛"
2. **侦察报告能判断"值不值得搬"** —— 万一 builders 体系差异太大，可能不划算
3. 10 条坑已写成规则，**能省掉大量返工**

**另外提醒**：魔绫那边的 Hollow/Galaxy 现在**已经能跑到 33F**（日志已证明），只是**楼梯锁**的问题刚修完。
**建议先推送验证一次**，确认能通关，再开方舟。
