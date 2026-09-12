# 方舟移植侦察报告（明日方舟地牢 → 终焉 fork）

最后更新：本轮侦察（阶段 1 完成，未改动任何代码）
数据来源：`_arknights_src/`（CFR 反编译，3257 文件）、`_EndShatteredBuild`、`shattered-pixel-dungeon-master`、`Magic_Ling_Pixel_Dungeon-stable`
分析工具：`_tools/arknights_recon.py`（可重复执行，输出 `_tools/_recon_out/recon.json` + `recon_report.md`）

---

## 〇、三条颠覆性结论（先看这个）

1. **fork 已经内置了挑战区框架，并且已经把方舟 3 区注册进去了。**
   [ChallengeArea.java](file:///e:/破碎的地牢/_EndShatteredBuild/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/endcontent/challenge/ChallengeArea.java#L47-L58) 里：
   ```java
   public static final ChallengeArea IBERIA = new ChallengeArea(4, "伊比利亚·海嗣", 4, false);
   public static final ChallengeArea GAVIAL = new ChallengeArea(5, "嘉维尔·雨林", 4, false);
   public static final ChallengeArea SIESTA = new ChallengeArea(6, "汐斯塔·海滨", 4, false);
   ```
   末位 `implemented=false`。选中窗口 [WndChallengeAreas.java](file:///e:/破碎的地牢/_EndShatteredBuild/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndChallengeAreas.java#L46-L54) 已列出这 3 项、显示「（未实装）」并禁用勾选。
   **接入点 = 实现 `createAreaLevel()` 的 3 个分支 + 把 `implemented` 改 true**，不需要从零改 `Dungeon.newLevel()`。

2. **方舟的「3 个区」不是 3 套并列剧情线，而是同一层段的 3 种互斥分支。**
   `Dungeon.newLevel()` 里 31-40F 由 `extrastage_Gavial` / `extrastage_Sea` 二选一（都不选 = Siesta）：
   ```
   31-34F → 区第1章     35F → Boss1     36-39F → 区第2章     40F → Boss2
   ```
   两个布尔由 NPC `Irene` 对话切换，存进 Dungeon bundle。**所以是"三选一"，不是"三个区都要走"。**

3. **手册里"老版 SPD 的 builders 体系需要补齐"的假设不成立。**
   fork **已有** `levels/builders/`（Builder / RegularBuilder / LoopBuilder / LineBuilder / FigureEightBuilder / BranchesBuilder / GridBuilder）、`levels/rooms/secret/`、`levels/rooms/connection/`、`journal.*`、`actors/Boss.java`、`com.watabou.utils.BArray`。
   真正的跨版本差异只有 **13 处包路径搬家**（见第四节），其中 1 处（`BArray`）是 67 处改动的总根源。

---

## 一、规模总账

| 指标 | 数值 |
|---|---|
| 方舟 MOD 相关类（去掉反编译捆绑的 gdx/lwjgl/jorbis/json，约 1600 个噪声类） | **1749** |
| fork 现有相关类 | 1555 |
| **方舟新增类（fork 完全没有的 FQN）** | **798**（78,360 行）+ 3 个 `com.watabou.*` |
| **需要打补丁的 fork 现成文件（方舟改过）** | **259** |
| 方舟新增类里，魔绫已有同 FQN（可直接复制） | **31** |
| 方舟新增类里，原版 SPD master 已有的 | 0（全是 MOD 原创命名） |
| 方舟 desktop jar 资源条目 | 504（fork 现有 551） |

### 新增类按包分布（Top 15）

| 包 | 类数 | 行数 |
|---|---|---|
| `sprites` | 160 | 6443 |
| `actors.mobs` | 71 | 12856 |
| `items.weapon.melee` | 64 | 7211 |
| `items.Skill.SK1` | 42 | 2378 |
| `items.Skill.SK2` | 40 | 2256 |
| `actors.mobs.npcs` | 35 | 3899 |
| `actors.buffs` | 34 | 3273 |
| `items` | 23 | 4069 |
| `items.Skill.SK3` | 22 | 1440 |
| `items.testtool` | 20 | 6714 |
| `levels` | 20 | 4960 |
| `items.wands.SP` | 19 | 3151 |
| `sprites.skins` | 16 | 560 |
| `items.quest` | 15 | 951 |
| `items.NewGameItem` | 14 | 918 |

> 注意：`sprites/actors.mobs/items.weapon.melee/items.Skill.*` 这四大块**不是 3 区专属**，是整个方舟 MOD 的通用内容。3 区真正独占的只是其中最上面一层（见第三节）。

---

## 二、依赖结构（为什么"只搬一个区"做不到）

### 结论：单一强连通分量

从任一区关卡出发做 BFS，4 个区的可达集合**完全一致（1676 个类）**，因为：

```
区域关卡 → Dungeon（Dungeon 直接 import 了全部 12 个区关卡）→ 一切
```

[SeaBossLevel1.java](file:///e:/破碎的地牢/_arknights_src/com/shatteredpixel/shatteredpixeldungeon/levels/SeaBossLevel1.java#L6-L23) 的 import 是干净的（只依赖方舟自己 + 原版 CityLevel/HallsLevel），**但 [Dungeon.java](file:///e:/破碎的地牢/_arknights_src/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java#L339-L392) 是所有区的总枢纽**，`TomorrowRogueNight`（MOD 的 Application 类）又是 69 个类的枢纽。

**推论**：不存在"只搬 Siesta 不碰别处"的路径。可行策略是
**「接入层先通 + 内容按区增量搬 + 全局机制用 shim 兜住」**，而不是"整包搬"或"严格隔离"。

### 三层结构

| 层 | 内容 | 是否必须 |
|---|---|---|
| L1 接入层 | `Dungeon` / `ChallengeArea` / `GameScene` / `InterlevelScene` / 挑战选择 UI | **必须，且量很小** |
| L2 内容层 | 798 新增类 + 259 补丁文件 | 必须，工作量主体 |
| L3 全局机制层 | `utils.BArray`(67) / `actors.buffs.Silence`(114) / `Camouflage`(26) / `items.Skill.Skill`(56) / `RingOfAmplified`(26) / `TomorrowRogueNight`(69) | **绕不开，但可以先用兼容桩** |

---

## 三、3 个区各自的完整类清单与规模

### 区一：伊比利亚 / 海嗣（areaId = 4）

| 关卡（root） | 行数 |
|---|---|
| `levels.SeaLevel_part1` | 131 |
| `levels.SeaLevel_part2` | 136 |
| `levels.SeaBossLevel1` | 226 |
| `levels.SeaBossLevel2` | 199 |

**深度 1 新增类：10 个 / 2171 行**
`Isharmla`(266, 分部件 Boss 头)、`SeaBoss1`(229)、`miniboss.TheEndspeaker`(1192, 巨大)、`npcs.SeaObject`(53)、`items.food.SanityPotion`(53)、`levels.features.SeaPlatform`(142)、`levels.painters.IberiaPainter`(37)、`levels.traps.HallucinationTrap`(34)、`levels.traps.OriginiumTrap`(54)、`TomorrowRogueNight`(111, 全局类，非区专属)

**深度 2 新增类：90 个 / 11489 行**（含 `SeaCapsule`/`SeaLeef`/`SeaReaper`/`SeaRunner`/`Sea_Octo`/`HeavyBoat`/`Piersailor`/`FloatingSeaDrifter`/`NetherseaBrandguider`/`IsharmlaSeabornHead/Body/Tail` 等海嗣一族）

### 区二：嘉维尔 / 雨林（areaId = 5）

| 关卡 | 行数 |
|---|---|
| `levels.GavialLevel` | 103 |
| `levels.GavialLevel2` | 103 |
| `levels.GavialBossLevel1` | 152 |
| `levels.GavialBossLevel2` | 159 |

**深度 1 新增类：6 个 / 691 行**
`Eunectes`(166)、`Tomimi`(366)、`levels.painters.GavialPainter`(37)、`HallucinationTrap`(34)、`OblivionTrap`(34)、`OriginiumTrap`(54)

**深度 2 新增类：81 个 / 12029 行**（含 Tiacauh 一族：Brave/Lancer/Ripper/Shaman/Ritualist/Warrior/Fanatic、`Talu_BlackSnake`、`TheBigUglyThing`、`MudrockZealot` 等）

### 区三：汐斯塔 / 海滨（areaId = 6）—— **建议首选试点**

| 关卡 | 行数 |
|---|---|
| `levels.SiestaLevel_part1` | 148 |
| `levels.SiestaLevel_part2` | 114 |
| `levels.SiestaBossLevel_part1` | 264 |
| `levels.SiestaBossLevel_part2` | 262 |

**深度 1 新增类：10 个 / 1500 行**
`Pompeii`(516)、`SiestaBoss`(367)、`npcs.Ceylon`(197)、`items.ArmorUpKit`(118)、`items.quest.Obsidian`(53)、`levels.painters.SiestaPainter`(37)、`levels.rooms.special.CoreRoom`(90)、`HallucinationTrap`(34)、`OblivionTrap`(34)、`OriginiumTrap`(54)

**深度 2 新增类：90 个 / 13470 行**（含 `Blast`/`BossSlug`/`Volcano`/`Ergate`/`Schwarz`/`Cronin`/`LavaSlug`/`Wraith_donut` 等）

### 附：罗德岛（`Dungeon.isInRhodes()`，branch 1-4，depth 0，非挑战区）

| 关卡 | 行数 |
|---|---|
| `NewRhodesLevel1/2/3/4` | 170 / 338 / 154 / 168 |

**深度 1 新增类：33 个 / 3327 行** —— 一整批 NPC（Dobermann、Closure、GreenCat、Jessica、FrostLeaf、Npc_Astesia、Weedy、Purestream、SkinModel、NPC_Guard/Mage/Pilot/Irene/Shu/Phantom/Gglow…）、`Closure_*Box` 系列 9 个箱子、`ChenSword`/`EX42`/`NEARL_AXE`。

> 罗德岛是**独立入口**（`depth==0 && 1<=branch<=4`），与 3 区无耦合，可单独评估是否要搬。

---

## 四、跨版本差异：必须处理的包路径搬家（13 处）

这 13 处是"照抄会编译不过"的直接原因，全部来自包结构调整：

| 方舟里的 FQN | fork 里的对应 FQN | 影响面 |
|---|---|---|
| `utils.BArray` | `com.watabou.utils.BArray` | **67 处引用**，最优先 |
| `actors.mobs.Beam` | `effects.Beam` | 4 处 |
| `levels.rooms.special.MassGraveRoom` | `levels.rooms.quest.MassGraveRoom` | 房间注册 |
| `levels.rooms.standard.BlacksmithRoom` | `levels.rooms.quest.BlacksmithRoom` | 房间注册 |
| `levels.rooms.standard.RitualSiteRoom` | `levels.rooms.quest.RitualSiteRoom` | 房间注册 |
| `levels.rooms.special.RotGardenRoom` | `levels.rooms.quest.RotGardenRoom` | 房间注册 |
| `items.keys.SkeletonKey` | `items.artifacts.SkeletonKey` | 少量 |
| `items.quest.RatSkull` | `items.trinkets.RatSkull` | 少量 |
| `actors.buffs.Camouflage` | `items.armor.glyphs.Camouflage` | 26 处 |
| `items.WndGuess` | `items.stones.WndGuess` | 少量 |
| `items.Skill.SK1.SpiritArrow` | `items.weapon.SpiritArrow` | 中 |
| `items.Skill.SK3.SpiritArrow` | `items.weapon.SpiritArrow` | 中 |
| `items.Skill.SK1.Camouflage` | `items.armor.glyphs.Camouflage` | 中 |

**处理方式**：不要改方舟源码的语义，先做「导入重定向表」，在搬类时逐条替换 import。`utils.BArray` 建议**直接删掉方舟那份、全量改用 `com.watabou.utils.BArray`**，一次性消掉 67 处改动。

---

## 五、缺失符号清单（fork 需要新增的 798 类，按子系统）

### 必须打补丁的 fork 现成文件：259 个，分布如下

| 子系统 | 文件数 | 说明 |
|---|---|---|
| `actors.mobs` | 51 | 每个原版怪物都被方舟改过（加 `Silence`/`Camouflage` 判定、换精灵） |
| `ui` | 16 | Toolbar/StatusPane/InventoryPane 等被 `BArray`、`Buttton` 波及 |
| `items.weapon` | 15 | Weapon/SpiritBow/SpiritArrow/MissileWeapon 等被 `Camouflage`/`ReflowBuff`/`WindEnergy` 波及 |
| `windows` | 15 | WndBag/WndJournal/WndGame 等 |
| `items.wands` | 13 | 所有法杖被 `RingOfAmplified` 波及 |
| `scenes` | 13 | 含 `GameScene`/`InterlevelScene`（接入层关键） |
| `items.potions` | 12 | `AlchemicalCatalyst` 波及 |
| `levels.rooms` | 12 | 房间注册表 |
| `levels.traps` | 12 | 陷阱 |
| `items` / `items.artifacts` | 9 / 9 | Item/Recipe/Amulet 等基类 |
| `levels` | 8 | **含 `Dungeon` 之外最关键的 `Level`/`RegularLevel`/`CavesLevel`/`CityLevel`/`HallsLevel`/`LastLevel`** |
| 其余 | ~60 | `actors.buffs` 7、`items.bombs` 7、`items.scrolls` 6、`journal` 4、`actors.hero` 3、`tiles` 2、`mechanics` 2… |

### 全局机制层「绕不开」的 6 个枢纽（先做兼容桩）

| 符号 | 被依赖次数 | 性质 |
|---|---|---|
| `actors.buffs.Silence` | 114 | 方舟全 MOD 的"沉默"机制，横扫 51 个原版怪物 |
| `TomorrowRogueNight` | 69 | MOD 的 Application 类 |
| `utils.BArray` | 67 | 见第四节，**建议直接重定向到 `com.watabou.utils.BArray`** |
| `items.Skill.Skill` | 56 | 技能系统基类 |
| `actors.buffs.Camouflage` | 26 | 潜行机制 |
| `items.rings.RingOfAmplified` | 26 | 法杖增幅戒指 |

> `TomorrowRogueNight` 已被证实**不需要移植**：fork 的 [ShatteredPixelDungeon.java](file:///e:/破碎的地牢/_EndShatteredBuild/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java#L83-L102) 已经有 `switchNoFade` 与 `seamlessResetScene`，把 69 处引用全量替换成 `ShatteredPixelDungeon` 即可。

### 可直接从魔绫复制的 31 个类

```
utils.BArray                          custom.messages.M
custom.dict.DictSpriteSheet           desktop.DesktopLauncher / DesktopLaunchValidator /
                                        DesktopPlatformSupport / DesktopWindowListener
services.news.NewsArticle / NewsImpl / NewsService / ShatteredNews
services.updates.UpdateImpl           scenes.IntroScene
ui.GoldIndicator / SimpleButton       windows.RewardButton / WndClass
items.bombs.Flashbang / ShockBomb     items.keys.SkeletonKey
items.scrolls.exotic.ScrollOfAffection / ScrollOfPetrification / ScrollOfPolymorph
items.stones.StoneOfDisarming         items.weapon.curses.Exhausting / Fragile
actors.blobs.WaterOfTransmutation     actors.mobs.Yog
levels.NewCityBossLevel / NewHallsBossLevel   levels.rooms.special.VaultRoom
```

---

## 六、接入层设计（阶段 A 的全部内容）

### 现状（无需改动，已就绪）

- [Dungeon.java:260](file:///e:/破碎的地牢/_EndShatteredBuild/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java#L260) 开局调用 `ChallengeArea.applySelection(SPDSettings.challengeAreas())`
- [Dungeon.java:525-547](file:///e:/破碎的地牢/_EndShatteredBuild/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java#L525-L547) 26F 起走 `ChallengeArea.areaAtDepth()` / `createAreaLevel()`
- [WndChallengeAreas.java](file:///e:/破碎的地牢/_EndShatteredBuild/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndChallengeAreas.java) 已渲染 5 个区域（2 实装 + 3 未实装）

### 需要做的（✅ 阶段 A 已完成）

1. ✅ **实现 `createAreaLevel()` 的方舟分支**：按 `areaId` 分派、`floorIn` → 关卡；关卡类尚未搬运，当前返回 `DeadEndLevel` 占位并标注 `//TODO(方舟B1)`。
2. ✅ **`implemented` 改 true**（3 行）。
3. ✅ **`floors` 设为 10**（见下）。
4. ✅ **加 `extrastage_*` 兼容桩**：`Dungeon` 新增两个静态布尔，由 `ChallengeArea.applySelection()` 推导：
   ```java
   public static boolean extrastage_Gavial = false;
   public static boolean extrastage_Sea    = false;
   ```
   现有 8 个**读取**这两个标志的位置：`Dungeon`、`actors.hero.Hero`、`actors.mobs.MobRotation`、`scenes.GameScene`、`scenes.ChangesScene`、`tiles.TerrainFeaturesTilemap`、`actors.buffs.NervousImpairment`、`windows.WndPilot`；写入方是 `actors.mobs.npcs.NPC_Irene`（对话切换）。搬方舟内容时直接读这两个桩即可，不要逐个改写调用点。

> ⚠️ 已知局限（阶段 A 未处理）：`Dungeon.extrastage_*` 与 `Statistics.challengeMask` 一样**不写进存档**，
> 因此「继续游戏」后标志会回到默认（= Siesta）。单局内不影响。B1 若要修，最小改动是在 `Statistics`
> 的 save/restore 里补 `challengeMask`（`Hollow_Holiday` 已有同样写法可照抄）。

### 每区层数：已拍板方案 A（保真 10 层）

> ✅ **已拍板：方案 A（保真 10 层）**，三个区注册值已从占位的 `4` 改为 `10`。
>
> | floorIn | 关卡 | 说明 |
> |---|---|---|
> | 0-3 | 第 1 章（`XxxLevel_part1`） | 4 层常规 |
> | **4** | **Boss1（`XxxBossLevel_part1`，Iberia/Gavial 为 `BossLevel1`）** | 关底 |
> | 5-8 | 第 2 章（`XxxLevel_part2`） | 4 层常规 |
> | **9** | **Boss2（`XxxBossLevel_part2`，Iberia/Gavial 为 `BossLevel2`）** | 最终关底 |

> 层号实际占用：单选某区 → 26F 起连续 10 层；全选 5 区 → 挑战区共 8+1+10×3 = 39 层（26F..64F）。

---

## 七、资源需求

- 方舟 jar 资源 504 项（桌面版资源直接放在 jar 根：`sprites/ sounds/ environment/ interfaces/ music/ messages/ effects/ fonts/ gdx/ icons/ splashes/`）
- 3 区 + 罗德岛引用 **148 项，其中 103 项 fork 缺失**

### 新增地形图（必须抠）
```
environment/tiles_iberia.png      tiles_iberia2.png      (海嗣)
environment/tiles_sargon.png      tiles_sargon2.png      (雨林)
environment/tiles_siesta.png      tiles_siesta2.png      (海滨)
environment/tiles_rhodos.png                             (罗德岛)
environment/water5.png            water6.png            (新水面)
```

### 新增 custom_tiles
```
iberia_boss1_1.png  iberia_boss1_2.png  iberia_boss2.png
gavial_boss2.png    prison_exit_new.png
rhodes_27f.png  rhodes_28f.png  rhodes_29f.png  rhodes_30f.png
```

### 新增精灵（约 50 个）
`Dobermann, gavial, tomimi, pompeii, kaltsit, phantom, ceylon, closure, jessica, cronin, keeper, weedy, handclap, endspeaker1-4, skadi_mula, jumama, pillar, belfry, civilian, first_talk, pink_doggi, npc_frost/guard/irene/jessi/mage/purestream/pilot, archetto, astesia, franka, frost, gladiia, grani, lappy, mudrock_skin, nova, schwarz_skin, specter, sussurro, talru, tomimi_skin, weedy_skin`

### 新增音效（约 28 个）
`hit_gun/hit_shotgun/hit_pistol/hit_revolver/hit_spear/hit_sword/hit_sword2/hit_knife/hit_whip/hit_punch/hit_ringout/hit_glutony/hit_dusk/hit_dualstrike/hit_sniping/hit_splash/hit_gunlance/hit_chainsaw/hit_chainsaw2/hit_ar`, `skill_basic/skill_beep/skill_babynight/skill_crossbow/skill_mon1/skill_mon2/skill_surtr/skill_silverslash`, `reload/shining/splash/frost`

### 消息键
方舟 jar 里 properties **齐全**：`messages/{actors,custom,items,journal,levels,misc,plants,private,scenes,ui,windows}`（含 `_en` 与 `.bak`）。可直接抽取合并。
⚠️ 合并时务必遵守规则 2（键 = 完整包路径去掉 `com.shatteredpixel.shatteredpixeldungeon.` 前缀），搬完跑 `_tools/scan_all_new_keys.py`。

---

## 八、风险评级与分阶段实施方案

### 风险矩阵

| 阶段 | 内容 | 规模 | 风险 | 理由 |
|---|---|---|---|---|
| **A 接入层** | ChallengeArea 分支 + extrastage 桩 + implemented | ~50 行 | **低** | 框架已就绪，改动点集中且可回滚 |
| **B1 单区内容** | 选 1 区（建议 Siesta），搬 4 关卡 + ~100 新增类 + 相关补丁 | ~1.5 万行 | **中** | 依赖清晰，但补丁面涉及 levels/rooms/traps |
| **B2 其余 2 区** | 复制 B1 流程 | ~2.5 万行 | **中** | 重复劳动，模式已验证 |
| **B3 全局机制** | `Silence`/`Skill`/`Camouflage`/`RingOfAmplified` | ~5 千行 + 259 文件补丁 | **高** | 横扫 51 个原版怪物与 16 个 UI，回归面极大 |
| **C 资源** | 地形图/精灵/音效/messages/图集扩充 | 103 项 | **中** | 有规则 3/4/5 的坑，需逐个验证像素 |
| **D 罗德岛** | NewRhodesLevel1-4 + 33 NPC | ~3 千行 | **低-中** | 独立入口，与 3 区解耦，可最后做或不做 |

### 建议执行顺序

```
A  接入层打通（不改任何方舟内容，只加桩 + 注册）
   └─ 验收：能勾选"汐斯塔·海滨"，进 26F 走到一个占位关卡

B1 Siesta 单区打通
   ├─ B1-1 重定向 13 处包路径（尤其 BArray）
   ├─ B1-2 搬 4 个关卡 + SiestaPainter + CoreRoom + 3 个 OriginiumTrap 系陷阱
   ├─ B1-3 搬 SiestaBoss / Pompeii / Ceylon / Blast / BossSlug / Volcano 等
   ├─ B1-4 搬精灵（走 sprites 包，避开图集坑）
   ├─ B1-5 抠地形图 + custom_tiles（务必查 2 的幂 + assignItemRect + 非透明像素）
   ├─ B1-6 合并 messages（跑 scan_all_new_keys.py）
   └─ 验收：汐斯塔从入口走到通关，编译 COMPILE OK

B2 Iberia / Gavial 复制 B1 流程

B3 全局机制层（按需分批，每批必编译）
   └─ 先 shim（Silence 空实现 / Camouflage 直通）→ 再逐区替换为真实逻辑

C  资源补全（与 B1/B2 并行）

D  罗德岛（可选）
```

### 每批必须执行的验证

```
cd E:\破碎的地牢\_EndShatteredBuild; & .\tools\localcompile.ps1
```
输出 `COMPILE OK` 才算通过；失败看 `E:\破碎的地牢\_javac_out\javac.log` 全文。

---

## 九、工具与产出

| 文件 | 用途 |
|---|---|
| `_tools/arknights_recon.py` | 本次侦察脚本，可重复跑（改 `REGIONS` 即可换分析起点） |
| `_tools/_recon_out/recon.json` | 全量结构化数据：798 新类、259 补丁文件及各自依赖、13 处搬家、148 项资源 |
| `_tools/_recon_out/recon_report.md` | 同上的可读版 |
| `_EndShatteredBuild/docs/ARKNIGHTS_PORT_MANUAL.md` | 施工手册（规则 + Prompt） |
| `_EndShatteredBuild/docs/ARKNIGHTS_PORT_PLAN.md` | 本文件 |

### 本报告对施工手册的修正

| 手册原说法 | 实际情况 |
|---|---|
| 方舟用 `builders/RegularBuilder` 体系，本 fork 需补齐 | **fork 已有全套 builders**，无需补 |
| 3 区 = 3 套独立剧情线 | **3 区是 31-40F 的三选一互斥分支**，由 `extrastage_*` 控制 |
| 本 fork 无 `actors.Boss`（需自建） | **fork 已有** `actors/Boss.java` |
| 本 fork 无 `journal.*` | **fork 已有** `journal.Bestiary/Catalog/Journal/Notes` |
| 本 fork 无 `utils.BArray` 对应物 | **fork 有** `com.watabou.utils.BArray`，方舟是重复造轮子 |
| 从零改 `Dungeon.newLevel()` 接方舟 | **改用已有 `ChallengeArea` 注册表**，量小得多 |
