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

## 六点五、实测依赖链（A 方案=老实体搬，本轮实测）

按用户选择"老实体搬"，已实测 Hollow 的依赖链深度。**结论：依赖是链式的，一层层向外扩**。

### 已补的零散符号（本轮，已提交）
- `Badges.Badge.KILL_DOG(152)` + `Badges.KILL_DOG()`（`HollowLevel:200` 用到）
- `Char.Property.HOLLOW`（`Char.java` 枚举，加在 `DEMONIC` 之后）
- `BuffIndicator.SCARY(86) / SCARY_PINK(87) / SCARY_RED(88) / IMELSAZE(89)`
  - ⚠️ **注意**：本 fork `buffs.png` 只有 128×64 → 大片(16×16)=32 帧。**索引 ≥32 会显示 `nofound`**（此前冷却图标就踩过这个坑）。这 4 个图标要想正常显示，**必须往 `assets/interfaces/buffs.png` 补 4 帧**，否则只能先借 <32 的既有索引。
- `Window.Pink_COLOR = 0xFF1493`

### `ElementalBuff` 体系（6 文件 / 约 390 行，HollowMimic 依赖）
源路径 `actors/buffs/ElementalBuff/`：
- `ElementalBuff.java`(79) — abstract extends Buff；只用 Buff/Hero/Bundle ✅可直搬
- `ElementalBaseBuff.java`(62) — 同上 ✅可直搬
- `ElementalFABuff.java`(6) — abstract extends FlavourBuff ✅可直搬
- `BaseBuff/ScaryBuff.java`(103) — 依赖 ↓
- `DamageBuff/ScaryDamageBuff.java`(97) — 依赖 ↓
- `Immunities/ScaryImmunitiesBuff.java`(46) — 依赖 ↓

### 该体系牵出的**新符号**（本 fork 全部缺失，需补）
| 符号 | 本 fork 状态 | 备注 |
|---|---|---|
| `BuffIndicator.SCARY / SCARY_PINK / SCARY_RED / IMELSAZE` | ❌ 无（本 fork 最高 `THROWN_WEP=85`） | 加 4 个常量即可（建议 86..89） |
| `Window.Pink_COLOR` | ❌ 无 | 魔绫定义 `0xFF1493`；本 fork `Window` 无颜色常量区，需加 |
| `IconFloatingText.HEARTDEMON` | ❌ **本 fork 无 `effects/IconFloatingText.java` 整个类** | 魔绫该类 208 行（自定义浮动文字体系），搬它需连带其依赖 |
| `TimeReset.MobsWither` | ❌ 本 fork 无 `TimeReset` | 且 `MobsWither extends DwarfGeneral.Wither` → **又牵出魔绫 Boss 类 `DwarfGeneral`** |

### 其他已确认缺失（HollowLevel/HollowMimic 直接需要）
- `Char.Property.HOLLOW` — 本 fork `Char.Property` 无此项（有 BOSS/MINIBOSS/UNDEAD/DEMONIC/INORGANIC/FIERY/ICY/ACIDIC/ELECTRIC/LARGE/IMMOVABLE…）→ 加枚举项
- `MimicSprite.HollowWall` — 本 fork `MimicSprite` 只有 `Golden/Crystal/Ebony` → 加内部类 + 精灵图
- `Mimic.items/setLevel/generatePrize` — ✅ 本 fork 都有（`Mimic.java:67/259/332`）
- `Terrain.CUSTOM_DECO`(23) / `REGION_DECO`(33) — ✅ 已有

### 实测结论（供下轮直接照做）
搬 `HollowMimic` 这一小组的顺序应为：
1. 补 `Char.Property.HOLLOW`、`BuffIndicator.SCARY*`、`Window.Pink_COLOR`
2. 搬 `ElementalBuff` 全 6 文件（此时会缺 `IconFloatingText`、`TimeReset.MobsWither`）
3. 决定 `IconFloatingText` / `TimeReset` 的处置：
   - **保真(A)**：连 `IconFloatingText`(208行) 与 `TimeReset`(172行，含 `DwarfGeneral.Wither`) 一起搬 → 继续扩链
   - **实用(B)**：把 `showStatusWithIcon(...IconFloatingText.HEARTDEMON)` 降级为普通 `showStatus`；把 `MobsWither` 降级为本 fork 已有的等价 debuff
4. 补 `MimicSprite.HollowWall` + 精灵资源
5. 再搬 `HollowMimic` → `HollowPainter` → `HollowLevel`/`HollowExitLevel`

> ⚠️ 越往上（Boss/painter/plot）依赖越重，`bosses/hollow/*` 那 15 个大概率还挂别的体系。**建议每搬一层就立刻在你机器上 `:core:compileJava` 验一次**，否则错误会累积。

## 六点六、搬运进度（第 3 轮 · Hollow 主体已落地）

### 已搬入本 fork 的 Hollow 内容（commit `4241245`，+1236 行）
**关卡（3）**
- `levels/HollowLevel.java`（extends RegularLevel，289 行）— 27–30F 常规层
- `levels/HollowExitLevel.java`（extends Level，502 行）— 26F 入口层，含 `BRANCH_EXIT`→depth+1 的下楼切换
- `levels/painters/HollowPainter.java`（76 行）

**怪物（2）**
- `actors/mobs/hollow/Vampire.java`（172 行）+ `sprites/VampireSprite.java`（75 行）
- `actors/mobs/hollow/HollowMimic.java`（93 行）

**恐惧/元素体系（7）**：`actors/buffs/ElementalBuff/` 下
`ElementalBuff` / `ElementalBaseBuff` / `ElementalFABuff` / `BaseBuff/ScaryBuff` / `DamageBuff/ScaryDamageBuff` / `DamageBuff/Wither`(抽自 DwarfGeneral) / `Immunities/ScaryImmunitiesBuff`

**物品（3）**：`items/food/hollow/` 的 `Gelatin`、`Sugar_Block`、`WhiteSugar_B`

**特效（1）**：`effects/particles/FrostFlameParticle`

### 为搬运而补的扩展点
| 文件 | 新增 |
|---|---|
| `SPD-classes/.../Music.java` | `playModeBGM(String,boolean)` |
| `Assets.java` | Environment: TILES_HOLLOW / TILES_HOLLOW_CS / WATER_HOLLOW / HOLLOW_OP / HOLLOW_PO / HALL_OPX / HALL_POX；Music: HOLLOW_CITY / HOLLOW_CITY_HARD；Sprites: VAMPIRE |
| `Statistics.java` | `Hollow_Holiday` / `AbyssCityRules` / `NoTime`（含 reset + 存读档） |
| `levels/Level.java` | `extraGlass` |
| `ui/BuffIndicator.java` | `SCARY(86)/SCARY_PINK(87)/SCARY_RED(88)/IMELSAZE(89)` ⚠️需补图集帧 |
| `ui/Window.java` | `Pink_COLOR` / `GDX_COLOR` |
| `effects/FloatingText.java` | `HEARTDEMON(26)` / `HEARTDEMON_DMG(28)` |
| `sprites/ItemSpriteSheet.java` | `GELATIN/SUGAR_BLOCK/WHITE_SUGAR_B`（占用 DOCUMENTS 块空位 DOCUMENTS+7..9） |
| `sprites/MimicSprite.java` | 内部类 `HollowWall` |
| `Badges.java` | `Badge.KILL_DOG(152)` + `KILL_DOG()` |
| `actors/Char.java` | `Property.HOLLOW` |
| `Dungeon.java` | 26F→`HollowExitLevel`、27–30F→`HollowLevel`（由 `Statistics.Hollow_Holiday` 开关） |

### 移植时的“降级/省略”记录（与魔绫原版的差异）
1. `Vampire` 的 `isAnimal = true` 已略去（本 fork `Mob` 无该字段/等价属性）。
2. `HollowLevel.createMobs()` 中 **depth27 生成 NPC `SliceGirl` 的分支暂缺**（依赖 NTNPC/SlicePlot/WndDialog/WndQuest/SliceGirlSprite 整条链），代码内已留 `TODO(待搬)`。
3. `HollowExitLevel` 的消息键由魔绫 `NewLastLevel` 改为本 fork 已有的 `LastLevel`。
4. `ScaryBuff` 的 `IconFloatingText.HEARTDEMON` → 本 fork 的 `FloatingText.HEARTDEMON`；`TimeReset.MobsWither` → 抽出的独立 `Wither`。
5. `BuffIndicator.SCARY*(86-89)` 超出本 fork `buffs.png`(128×64→大片32帧) 范围 → **会显示 nofound，必须补图集帧**。

### 仍未搬（下轮继续）
- NPC：`npcs/hollow/*`（SliceGirl/SliceAlter/MorphsNPC/CerbusSleep/DeathRong*/Typhon/ZeroBoat）
- 怪物：`mobs/hollow/` 其余（Butcher/Crumb/Frankenstein/ApprenticeWitch/Ghost_Halloween/Pumking*/allsearch/minigame）
- Boss：`bosses/hollow/*`（约 15 个，含 TowerGods/Morphs/Nyarlathotep/ShubNiggurath/DeadDogCerberus…）
- 小游戏关：`levels/hollow/*`（AllSearch/MoveBox/Pacman/CerdoG/Morpheus/Theatre/ZeroHalls）
- 房型 `rooms/hollow/*`、剧情 `custom/utils/plot/hollow/*`、`items/food/hollow/Sugar.java`
- **资源文件**：`environment/tiles_halloween*.png`、`environment/water7.png`、`music/hollow/*.ogg`、`sprites/hollow/vampire.png`、`custom_tiles/hall_*.png`、`cerberus_*.png`、`text_icons.png` 帧、`buffs.png` 帧、`messages` 文案
- **选中/进入 UI**（多选区域 → 26F 起按 id 顺序串）与 `Statistics.Hollow_Holiday` 的置位入口
- 其余 5 区（BossRush/Galaxy/Peach/DeepShadow/ForestHard）
- 方舟 3 区（等 `cfr.jar` 反编译）

## 六点七、搬运进度（第 4 轮 · 资源 + NPC 链）

### 已复制的资源（commit `248b8d5`）
- 环境贴图：`tiles_halloween.png`、`tiles_halloween-cs.png`、`water7.png`、`custom_tiles/{hall_behind-opendoor,hall_above_b,cerberus_behind,cerberus_above}.png`
- 音乐：`music/hollow/*`（Mischief_Managed(-Easy).ogg、morpheus、movebox、pacman、seach）
- 精灵：`sprites/hollow/**`（42 个文件，含 vampire/mimicry/Cerberus/morpheus/nyarlathotep/shub_niggurath/tower_*/yogsoul 等全部 Boss 图）

### 已搬代码
- `actors/mobs/npcs/NTNPC.java`（对话型 NPC 基类）
- `actors/mobs/npcs/hollow/SliceGirl.java` + `sprites/SliceGirlSprite.java`
- `actors/mobs/npcs/hollow/DeathRong.java` + `sprites/DeathRongSprite.java`
- `effects/particles/HalomethaneFlameParticle.java`
- 为它们补的扩展：`NPC.throwItem()`、`WndQuest` 的多段对话（`chating(...)`/3 参构造/hide 翻页）、`Assets.Sprites.SWTICH/SWTICH_ALTER/ZEROBOAT`、`Statistics.defalult_deaddog`
- **恢复了 `HollowLevel` 27F 生成 `SliceGirl` 的分支**（原 TODO 已消除）

### 本轮新增的“降级”记录
- `SliceGirl.interact()` / `DeathRong.interact()`：魔绫原版打开 `WndDialog(plot)`（依赖未搬的 `custom/utils/plot/**` + `WndDialog`），
  已改为等价的 `WndQuest` 文本对话（**保留 NPC 出现与交互，剧情演出待搬**）。两者代码内均留 `TODO(待搬)`。
- 需要补的 messages 键：`actors.mobs.npcs.hollow.slicegirl.hello`、`...deathrong.hello` / `hello_end`（**尚未加，需补文案**）

## 六点八、搬运进度（第 5 轮 · 常规怪物 + 刷怪接线）

### 已搬入的怪物（7 个，含各自精灵）
| 怪物 | 文件 | 精灵 |
|---|---|---|
| Vampire | `mobs/hollow/Vampire` | `VampireSprite` |
| HollowMimic | `mobs/hollow/HollowMimic` | `MimicSprite.HollowWall` |
| Butcher | `mobs/hollow/Butcher` | `ButcherSprite` |
| Crumb | `mobs/hollow/Crumb` | `CrumbSprite` |
| Ghost_Halloween | `mobs/hollow/Ghost_Halloween` | `GhostHalloweenSprite` |
| Pumking_Ghost | `mobs/hollow/Pumking_Ghost` | `PumkingGhostSprite` |
| PumkingBomber | `mobs/hollow/PumkingBomber` | `PumkingBomberSprite` |

### 为它们补的扩展点
- `Assets.Sprites`：`GHOST_HE`/`GHOST_HP`/`BTSLIMH`/`CRUMB`/`ZOMBIE`/`APWHEEL`/`SWTICH`/`SWTICH_ALTER`/`ZEROBOAT`/`GHOST_MINI`/`TELE_FOCU`
  - ⚠️ 魔绫原键名 `BOMB`（指向 gingerbread.png）已改名为 **`HOLLOW_BOMBER`**，避免与“炸弹”语义混淆
- `Hunger.damgeExtraHungry(int)`（Crumb 偷食加饥饿）
- `Bomb.explodeMobs(int)`（PumkingBomber 落点只炸怪不炸物品）
- `NPC.throwItem()`、`WndQuest` 多段对话（上轮）

### 刷怪接线（关键）
- `actors/mobs/MobSpawner.getMobRotation(depth)`：新增 **depth 27/28/29/30** 的空洞怪物轮换表
  （27: Butcher/Crumb/Ghost_Halloween；28: +PumkingBomber/Pumking_Ghost；29/30: +Vampire）
  → 这样 `HollowLevel` 的 `super.createMobs()` 就会真的在这些层刷出它们。

### 移植调整（与魔绫原版的差异）
1. **attack 签名**：魔绫 `attack(Char,float,float,float,DamageType)`（5参）→ 本 fork `attack(Char,float,float,float)`（4参，无 DamageType）
   （涉及 `Ghost_Halloween`、`Pumking_Ghost`）
2. `Crumb` 用到的 `Hunger.damgeExtraHungry` 已按本 fork 的 `affectHunger` 语义重实现
3. `PumkingBomber` 用到的 `Bomb.explodeMobs` 已移植（去掉 `DrTerror` 与 DamageType）
4. `PumkingBomberSprite` 的纹理键 `BOMB` → `HOLLOW_BOMBER`

### 仍未搬
- `mobs/hollow/` 剩余：`ApprenticeWitch`（需 `blobs/HalomethaneFire` + `buffs/HalomethaneBurning`）、`Frankenstein`（需 `PaswordBadges`）、`allsearch/*`（属未搬小游戏关）、`minigame/*`（同上）
- Boss 15 个、小游戏关 7 个、房型、剧情 plot、`WndDialog`(546行)
- 多选 UI + `Hollow_Holiday` 置位入口；`buffs.png` 补帧；其余 5 区；方舟 3 区

## 六点九、搬运进度（第 6 轮 · 进入挑战区的入口）

### 新增“挑战区域”入口（commit `a9f6f86`）
| 文件 | 作用 |
|---|---|
| `endcontent/challenge/ChallengeArea.java` | **区域注册表**：id / 中文名 / 是否已实装；`isSelected`/`toggle`/`applySelection(mask)` |
| `windows/WndChallengeAreas.java` | 开局**多选窗口**（仿 `WndChallenges` 的 CheckBox 列表） |
| `SPDSettings.challengeAreas()` | 保存勾选结果（位掩码，bit = 区域 id） |
| `scenes/HeroSelectScene.java` | 开局界面新增「挑战区域」按钮（在原「挑战」按钮下方） |
| `Dungeon.init()` | 开局时 `ChallengeArea.applySelection(SPDSettings.challengeAreas())` → 写入 `Statistics.Hollow_Holiday` |

区域 id（进入顺序）：1 空洞遗迹(已实装) / 2 Boss Rush / 3 银河深渊 / 4 桃神试炼 / 5 深影领域 / 6 森林灾厄
（后 5 个在窗口里显示为「（未实装）」且不可勾选，避免选了没反应）

### 完整链路（现已贯通）
```
开局勾选「空洞遗迹」
   → Dungeon.init() 置 Statistics.Hollow_Holiday = true
   → 主线 1F..25F 照常
   → depth 26：Dungeon.newLevel() 因 Hollow_Holiday 生成 HollowExitLevel（入口层）
   → 踩 BRANCH_EXIT 楼梯 → depth 27..30 的 HollowLevel（27 有 NPC、27-30 刷空洞怪）
   → 30F 之后未接 Boss（Boss 尚未搬完）
```

### ⚠️ 设计后果（重要，需你确认是否接受）
本 fork 的 **26F 原本是 `LastLevel`（放护符/结局的层）**。按魔绫的一致设计，`Hollow_Holiday=true` 时 **26F 被替换为 `HollowExitLevel`**：
- **即：选了挑战区域后，主线 26F 的护符/结局被挑战区取代**（走挑战 → 之后才能回主线结局）。
- 这与魔绫行为一致，但**改变了主线收尾流程**。若你希望“先拿护符结束主线、再另开挑战”，需要另设计入口（例如 26F 之后再加一层），请明确。

### 仍未做
- `buffs.png` 补帧（否则 SCARY 等图标 nofound）
- Boss 15 个、小游戏关 7 个、房型、剧情 plot
- Hollow 30F 之后的 Boss 层接线（`levels/hollow/CerDogBossLevel` 等）
- 其余 5 区；方舟 3 区

## 六点十、首次 CI 编译反馈与修复（重要 · 移植避坑清单）

CI 跑 `./gradlew :desktop:compileJava :desktop:installDist` 报 **13 个错误**，全部是"用了本 fork 不存在的符号"。
已修复（commit `fc90529`）：

| 错误 | 文件 | 修复方式 |
|---|---|---|
| `Terrain.SIGN` / `Terrain.SIGN_SP` 找不到（4 处） | `HollowExitLevel` | 本 fork 无这两个地形：`SIGN`→`CUSTOM_DECO`(SOLID，注释即"旧 sign 用的 ID")、`SIGN_SP`→`CUSTOM_DECO_EMPTY`(可通行)。**并把 `tileName/tileDesc` 的 switch 合并**（否则 case 重复） |
| `PotionOfHolyWater` 找不到（4 处） | `HollowLevel` | 本 fork 无圣水：改用 `PotionOfPurity`（现有净化药水，语义最接近） |
| `Weakness.set(int)` 找不到（1 处） | `Vampire` | 本 fork 的 `Weakness` 是 `FlavourBuff`（无 `set()`）：改用 `Buff.prolong(enemy, Weakness.class, duration)` |

### ⚠️ 由此得出的移植避坑清单（后续区域务必先查）
移植任何魔绫类之前，**先 grep 本 fork 是否真的存在**这些高频"魔绫扩展"：
1. **地形**：`Terrain.SIGN`/`SIGN_SP`/`HOLES` 等 —— 本 fork 无，用 `CUSTOM_DECO`/`CUSTOM_DECO_EMPTY`/`REGION_DECO` 替代
2. **物品**：`PotionOfHolyWater` 等 —— 用本 fork 现有同类替代
3. **Buff 接口**：魔绫很多 buff 有 `set(int)`/`set(int,int)`；本 fork 多数 `FlavourBuff` 只能用 `Buff.affect(cls, dur)` / `Buff.prolong(cls, dur)`
4. **`Char.DamageType`**：魔绫 `damage(int,Object,DamageType)` / `attack(...,DamageType)` —— 本 fork **无 DamageType**，一律去掉该参数
5. **`CharSprite.State`**：魔绫有 `HALOMETHANEBURNING`/`ROSESHIELDED` 等 —— 本 fork 无，改用现有 `State`（如 `AURA`/`BURNING`）或省略纯视觉
6. **`Assets.Sprites.*` / `Assets.Music.*` / `Assets.Environment.*`**：魔绫的键本 fork 大多没有，需自己加键（**图集帧数有限**，如 `buffs.png` 仅 128×64 → 大片 32 帧，索引 ≥32 会 `nofound`）
7. **`ItemSpriteSheet.EMPTY`**：本 fork 无，用 `SOMETHING`
8. **`isAnimal` 字段**：本 fork `Mob` 无，删掉
9. **`Char.Property.*`**：魔绫新增项（如 `HOLLOW`）需自己加到枚举

## 六点十一、DeadDogCerberus（冥犬 Boss）移植方案（已勘定，待执行）

### 关键障碍：魔绫有 `Boss` 基类，本 fork 没有
- 魔绫：`actors/Boss.java`（自造基类），提供 `initProperty()` / `initBaseStatus(min,max,acc,eva,ht,mid,mad)` / `initStatus(exp)` 与字段 `baseMin/baseMax/baseAcc/baseEva/baseHT/baseMinDef/baseMaxDef`
- **本 fork 无 `actors/Boss.java`**；本 fork 的 Boss（Goo/DwarfKing/Tengu/YogDzewa）**全部 `extends Mob`**，在 init 块里直接写 `HP/HT/EXP/defenseSkill`，并各自 override `damageRoll()/attackSkill()`

### 好消息：`DeadDogCerberus` 只用这套框架 5 处
| 行 | 魔绫写法 | 本 fork 改写 |
|---|---|---|
| 118 | `initProperty();` | 直接 `properties.add(...)`（init 块里已手写 BOSS/DEMONIC/ACIDIC） |
| 119 | `initBaseStatus(20,60,20,26,1000,0,0)` | `damageRoll()`→`Random.NormalFloat(20,60)`；`attackSkill()`→20；`defenseSkill`→26；`HP=HT`→1000 |
| 120 | `initStatus(100);` | `EXP = 100;` |
| 174 | `damage(int dmg, Object src, DamageType type)` | `damage(int dmg, Object src)`（本 fork 无 DamageType） |
| 1152 | `GetBossLoot(pos);` | 省略或替换（BossRush 掉落，非必需） |

### 其它必须降级的地方
- `import items.props.Prop`（第 49 行）**实际未被使用** → 删掉；`Prop` 还牵出 `Conducts` + `Statistics.propPositive*` 整套"道具词条"系统，**不值得为未使用的 import 去搬**
- `GameRules.PropsScore()`（1157）→ **省略**（道具计分，非核心；`GameRules` 还依赖 `com.nlf.calendar.Lunar/Solar` 农历库，本 fork 无）
- `Typhon`（1163-1165，死后生成剧情 NPC）→ **省略**（依赖 `TyphonPlot`+`WndDialog` 未搬）；可留 TODO
- `DriedRose.GhostHero`（149）→ 需确认本 fork 有此内部类（`items/artifacts/DriedRose` 存在，内部类待核）
- `ComboAttackThis` / `HunterReady` / `CriticalBite` 等字段被 `DeadDogCerberusSprite` 读取，**必须保留同名 public 字段**

### 前置件状态
- ✅ 已搬：`DeadDogCerberusSprite`、`LanFireGo`、`RoseShiled`、`DeadFireFlameParticle`、`Assets.Sprites.NCSBR`、`BuffIndicator.ROSEBARRIER`
- ✅ 判断为**不必搬**：`GameRules`、`Typhon`、`Prop`（理由见上）

### 执行顺序建议
1. 按上表把 `DeadDogCerberus` 改成 `extends Mob` 版本（5 处改写 + 3 处降级）
2. 确认 `DriedRose.GhostHero` 存在（否则删该 for 循环）
3. 搬 `levels/hollow/CerDogBossLevel`（Boss 层）并接到 30F 之后
4. 跑 CI

## 七、当前阻塞 / 待办



- **本机不能编译验证**（wrapper 需 gradle 9.4.0、无外网、沙箱只写工作区）→ 每步仍需在你机器或 GitHub CI 上 `./gradlew :core:compileJava` 验证。
- **方舟三区**：需反编译器（推荐 CFR：`https://www.benf.org/other/cfr/cfr-0.152.jar`，放到 `E:\破碎的地牢\cfr.jar`）。
- 本地有 **7 个提交未 push**（`680abe4` … 加本轮）；建议先推基线再继续大改。
