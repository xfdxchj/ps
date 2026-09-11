# 挑战区移植 · 进度与方案（魔绫 → 终焉 fork）

最后更新：本轮（挑战系统启动）
目标：把「魔绫像素地牢」的挑战区（区域+怪物+Boss）移植进 `_EndShatteredBuild`；
玩法：**开局可多选挑战区 → 打完主线 25F → 26 层进挑战区 → 按 id 顺序依次进入（下楼切换）**。

---

## 一、已确认的源端机制（魔绫，`Magic_Ling_Pixel_Dungeon-stable`）

### 1. 挑战区 = `Dungeon.branch` + `Dungeon.depth`
- `Dungeon.newLevel()`（魔绫 `Dungeon.java:392`）：
  ```java
  if (branch == 0) level = createStandardLevel();
  else             level = createBranchLevel();
  ```
- 映射表在 **`levels/LevelRules.java`**（542 行）：
  - `createStandardLevel()`：主线 depth→关卡；**含 Hollow 开关分支**
    - `case 26: Hollow_Holiday ? new HollowExitLevel() : new LastLevel();`
    - `case 27..30: Hollow_Holiday ? new HollowLevel() : new DeadEndLevel();`
    - `case 31: TheatreLevel() / CerDogBossLevel()`；`case 32: TheatreLevel()`；`case 33: MorpheusBossLevel()`
  - `createBranchLevel()`：**按 branch 号的挑战区表**
    - `branch 1`: depth0 Hotel / 5 DragonCave / 11-14 Mining / 17-18 AncientMysteryCity / 20 DwarfGeneralBoss / **31 PacmanHollowActorLevel**
    - `branch 2`: 4 MiniBoss / 8 MiniSkyShadowBoss / **31 MoveBoxHollowActorLevel**
    - `branch 3`: 5 LaveCavesBoss / 11-14 DragonFestival / **31 AllSearchHollowActorLevel**
    - `branch 4`: **25 HollowExitLevel** / 17-18 Garden / 10-13 MiniChestMaze
    - `branch 5`: 17 Garden / 0 HiroFlower
    - `branch 6`: LinkLevel；`branch 7`: ShopBossLevel
    - `branch 8`: BossRush 表；`branch 10`: **26 GalaxyKeyBossLevel**, 25 ZeroHallsBossLevel
    - `branch 12`: 一整串 Boss（depth0..23 = SewerBoss→…→SLMKing）← **"按 depth 顺序连打"的现成范例**
- 种子：`seedForDepth(depth,branch)` 用 `depth + 30*branch`（注释：depth 1-30、branch ≥0）

### 2. **"下楼切换"机制（与用户要求一致）**
- 楼梯/传送点带 `destBranch`；`scenes/InterlevelScene.java`：
  ```java
  Dungeon.branch = curTransition.destBranch;   // L402
  Dungeon.branch = curTransition.destBranch;   // L445
  Dungeon.branch = returnBranch;               // L469
  ```
- 即：**下楼即切换 branch** —— 正是用户要的"下楼后切换"。

### 3. 进入挑战的开关
- `Statistics.Hollow_Holiday`（`Statistics.java:298`，存档键 `HOLLOW_DAY`，重置 L739）
- 置 true 处：`items/books/questbookslist/HollowCityBook.java:43`、`levels/rooms/special/BoilerRoom.java:243,310`
- 其他相关统计位（`Statistics.*`）：`bossRushMode`、`RandMode`、`AbyssCityRules`、`ExFruit`、`difficultyDLCEXLevel`、`snow`、`gdzHelpDungeon` 等

---

## 二、本 fork（`_EndShatteredBuild`）现状与差距

| 项 | 本 fork | 处理 |
|---|---|---|
| `Dungeon.branch` 机制 | ✅ 已有（0 主线 / 1 支线：MiningLevel+VaultLevel） | 扩展 branch 号即可 |
| `Terrain.CUSTOM_DECO` | ✅ 已有(=23) | 直接用 |
| `Terrain.REGION_DECO` | ✅ 已有(=33) | 直接用 |
| `Music.playModeBGM(String,boolean)` | ❌ 原先没有 | **✅ 本轮已加**（见下） |
| `Game.runOnRenderThread(Callback)` | ✅ 已有（`Game.java:309`） | 直接用 |
| `Level.extraGlass` | ❌ 无 | 移植时删该行或加字段 |
| `Assets.Music.HOLLOW_*` / `Assets.Environment.TILES_HOLLOW/WATER_HOLLOW` | ❌ 无 | 需加键 + 复制资源 |
| `Statistics.Hollow_Holiday` 等 | ❌ 无 | 需加字段 |
| `LevelRules` 两表 | ❌ 无（本 fork 映射写在 `Dungeon.newLevel()` 里） | 可照搬 LevelRules 结构 |
| 魔绫类簇（关卡/怪物/Boss/NPC/painter/物品/plot） | ❌ 无 | 分步移植 |

---

## 三、已做的代码改动（地基，全部已落地）

1. `SPD-classes/src/main/java/com/watabou/noosa/audio/Music.java`
   - ✅ 新增 `public static void playModeBGM(String name, boolean loop)`（照魔绫实现，含桌面端 `Game.runOnRenderThread` 线程安全处理）。**所有挑战区关卡类的公共依赖，加一次全解决。**（`Game.runOnRenderThread` 本 fork 已存在于 `Game.java:309`）
2. `core/.../Assets.java`
   - ✅ `Environment` 新增：`TILES_HOLLOW`(`environment/tiles_halloween.png`)、`TILES_HOLLOW_CS`(`tiles_halloween-cs.png`)、`WATER_HOLLOW`(`environment/water7.png`)、`HOLLOW_OP`(`custom_tiles/cerberus_behind.png`)、`HOLLOW_PO`(`custom_tiles/cerberus_above.png`)
   - ✅ `Music` 新增：`HOLLOW_CITY`(`music/hollow/Mischief_Managed-Easy.ogg`)、`HOLLOW_CITY_HARD`(`music/hollow/Mischief_Managed.ogg`)
3. `core/.../Statistics.java`
   - ✅ 新增字段 `public static boolean Hollow_Holiday = false;`、`public static int AbyssCityRules = 0;`
   - ✅ 同步：`reset()` 复位、`storeInBundle`/`restoreFromBundle` 用键 `HOLLOW_DAY` / `AbyssRules`
4. `core/.../levels/Level.java`
   - ✅ 新增 `public boolean extraGlass = true;`（魔绫 `Level.java:158` 同名字段；`HollowLevel` 初始化块会设 false）

> ⚠️ 仍缺（下一步必须先补，否则搬过来编译不过）：
> - `Badges.Badge.KILL_DOG`（`HollowLevel` 用到）→ 加徽章常量或删该判分支
> - `Terrain.CUSTOM_DECO` ✅ 已有
> - 资源文件本体（`tiles_halloween.png` 等）需从魔绫 assets 复制过来

---

## 四、Hollow 类簇清单（第一个要移植的区）

源路径前缀：`Magic_Ling_Pixel_Dungeon-stable\core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\`

**关卡（9）**
- `levels\HollowLevel.java`（extends RegularLevel）、`levels\HollowExitLevel.java`
- `levels\hollow\`：`AllSearchHollowActorLevel`、`CerDogBossLevel`、`MorpheusBossLevel`、`MoveBoxHollowActorLevel`、`PacmanHollowActorLevel`、`TheatreLevel`、`ZeroHallsBossLevel`

**怪物（16）**
- `actors\mobs\hollow\`：ApprenticeWitch、Butcher、Crumb、Frankenstein、Ghost_Halloween、HollowMimic、PumkingBomber、Pumking_Ghost、Vampire
- `actors\mobs\hollow\allsearch\`：HelpTeleportPoint、ShadowHunstman
- `actors\mobs\hollow\minigame\`：GhostTemplate、Ghost_Anger、Ghost_Junko、Ghost_Pink、Ghost_Smart

**NPC（8）**：`actors\mobs\npcs\hollow\`：CerbusSleep、DeathRong、DeathRongShop、MorphsNPC、SliceAlter、SliceGirl、Typhon、ZeroBoat

**Painter（1）**：`levels\painters\HollowPainter.java`

**Boss（约 15）**：`actors\mobs\bosses\hollow\`：BleedCrystal、DeadDogCerberus、FireSuperDr、Morphs、MyCoreHeart、Nyarlathotep、ShubNiggurath、TowerGods/TowerGodsBad、TowerMachine/Bad、TowerMind/Bad、TowerTime/Bad、TowerParalysis、YogSoul

**另需**：`items\food\hollow\`（Gelatin/Sugar_Block/WhiteSugar_B 等）、`levels\rooms\hollow\**`（crystal/escape/gold/guard/locked/Slice…）、`custom\utils\plot\hollow\**`（12 个剧情 plot）、精灵图、`messages` 文案、`environment\tiles_hollow*.png`、`music\hollow\*.ogg`

---

## 五、HollowLevel 已知依赖（移植时要逐个消解）

来自 `levels\HollowLevel.java`：
- `Assets.Environment.TILES_HOLLOW` / `WATER_HOLLOW` → 加键 + 贴图
- `Assets.Music.HOLLOW_CITY` / `HOLLOW_CITY_HARD` → 加键 + 音频
- `Music.playModeBGM` → ✅ 已解决
- `extraGlass` → 删或加字段
- `Terrain.CUSTOM_DECO` → ✅ 已有
- `Statistics.AbyssCityRules`、`Statistics.Hollow_Holiday` → 加字段
- `Badges.Badge.KILL_DOG` → 加徽章或删该判分支
- 类依赖：`HollowMimic`、`Vampire`、`SliceGirl`、`HollowPainter`
- 物品依赖：`ScrollOfChallenge`、`ScrollOfMetamorphosis`、`ScrollOfAntiMagic`、`ScrollOfSirensSong`（后两个本 fork 已有 `ScrollOfAntiMagic`）

---

## 六、建议的搬运顺序（每步以"能编译"为界）

1. **地基**：`Music.playModeBGM`（✅ 已完成）
2. **加扩展点**：`Assets.Environment.*` / `Assets.Music.*` 新键、`Statistics.*` 新字段、`Level.extraGlass`（或删除用法）
3. **搬 painter + 关卡骨架**：`HollowPainter` + `HollowLevel` + `HollowExitLevel`（先把 HollowLevel 裁到能编译：去掉暂不移植的类引用）
4. **搬怪物**：`mobs/hollow/*`（含 allsearch/minigame）
5. **搬 Boss**：`bosses/hollow/*`
6. **搬 NPC + 房型 + 食物 + plot**
7. **接注册表**：把 `LevelRules` 风格的表引入本 fork（或用本 fork `Dungeon.newLevel()` 直接接），26 层入口 + 下楼切分支
8. **多选 UI**：开局选择多个区域 → 存列表 → 按下楼顺序消费
9. 资源复制：`environment/tiles_hollow*`、`music/hollow/*`、精灵图、`messages` 文案
10. 其余 5 区（BossRush/Galaxy/Peach/DeepShadow/ForestHard）按同模板铺开
11. 方舟 3 区：等 `cfr.jar` 反编译源码到位后接入

---

## 七、当前阻塞 / 待办

- **本机不能编译验证**（wrapper 需 gradle 9.4.0、无外网、沙箱只写工作区）→ 每步仍需在你机器或 GitHub CI 上 `./gradlew :core:compileJava` 验证。
- **方舟三区**：需反编译器（推荐 CFR：`https://www.benf.org/other/cfr/cfr-0.152.jar`，放到 `E:\破碎的地牢\cfr.jar`）。
- 本地有 **7 个提交未 push**（`680abe4` … 加本轮）；建议先推基线再继续大改。
