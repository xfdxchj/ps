# 《破碎的像素地牢：终焉扩展》—— 基于原版 SPD 3.3.8 的全部改动清单

> **基线**：官方 Shattered Pixel Dungeon **3.3.8**（commit `1ebe444` 工程骨架建立）
> **统计**：203 次提交，1537 个 Java 源文件
> **包名**：沿用 `com.shatteredpixel.shatteredpixeldungeon`（与原版一致）
> **本文档目的**：按功能模块列出所有在原版 SPD 之上新增/修改的内容，便于审计与交接。

---

## 目录

1. [工程基础与构建](#1-工程基础与构建)
2. [宝石系统](#2-宝石系统)
3. [进化法杖系统](#3-进化法杖系统)
4. [灵能弓进化](#4-灵能弓进化)
5. [装备进化族（破印 / 匕首）](#5-装备进化族破印--匕首)
6. [挑战区系统框架](#6-挑战区系统框架)
7. [空洞遗迹（魔绫移植）](#7-空洞遗迹魔绫移植)
8. [银河深渊（魔绫移植）](#8-银河深渊魔绫移植)
9. [方舟 3 区（明日方舟移植）](#9-方舟-3-区明日方舟移植)
10. [六王](#10-六王)
11. [挑战项变更](#11-挑战项变更)
12. [便利功能](#12-便利功能)
13. [原版内容调整（删除 / 禁用 / 修正）](#13-原版内容调整删除--禁用--修正)
14. [资源 / 地形 / 音乐](#14-资源--地形--音乐)
15. [附录：全局机制层兼容桩](#15-附录全局机制层兼容桩)

---

## 1. 工程基础与构建

| 改动 | 说明 |
|---|---|
| 工程骨架 | 官方 SPD 3.3.8 fork，core / SPD-classes / services / desktop / android（双产物） |
| 桌面化配置 | `settings.gradle` 剔 android 需 SDK 但保留 android 用于双产物；desktop 移除 beryx 打包插件 |
| CI | `.github/workflows/build.yml`：desktop distribution + Android debug APK artifacts；`setup-android@v3` 改用预安装 SDK + `local.properties`，避免 `tools` 包不存在报错 |
| 定名 | `Shattered Pixel Dungeon: End` |
| 桌面运行兼容 | `Game.version` 恒非空；`vendor/version` 段判空；`Implementation-*` 缺失时 null-safe |
| 存档兼容 | `Level.storeInBundle`/`Dungeon.saveGame` 写版本用 `max(Game.versionCode, v2_5_4)`，避免开发态 -1 写档；`InterlevelScene` 兼容无 `VERSION` 键的存档 |
| 存档迁移 | 首次运行若 fork 存档目录为空，自动从原版 SPD / "Tomorrow RogueNight" 复制 game1-6 + badges/rankings/journal/bones |

---

## 2. 宝石系统

### 新增类（`endcontent/`）
- `EndGem.java` —— 5 类宝石词典
- `EndGemProfile.java` —— 线性成长模型 `value()`/`bonusAt`
- `items/EndGemItem.java` —— 宝石物品，天生已鉴定，拾取自动登记图鉴
- `items/VoidShard.java`

### 修改原版类
- `Weapon.java` / `Armor.java`：新增 `gem`(int, -1 无) + `hasGem()/gemType()`，bundle `end_gem` 键持久化，不随 reset 丢失
- `Hero.java`：数值切面注入（命中/攻击向武器、减伤/闪避/生命向护甲）
- `Weapon.gem`/`Armor.gem`：升级/装备时重算属性（MAX_HP 宝石重算最大生命并 `heal(true)`）

### 数值曲线
| 属性 | 每级加成 |
|---|---|
| 攻击 | +2 |
| 防御 | +1 |
| 命中 | +lvl |
| 闪避 | +2 |
| 最大生命 | +5 |

### 经济
- 每个主游戏商店（6/11/16/20 层…）刷新 1 颗随机宝石，`EndGemItem.value()=50×quantity`（与 Ankh 同价）

---

## 3. 进化法杖系统

### 新增类（`endcontent/evolved/`）
- **13 把进化法杖**（继承对应原版法杖，新物品、独特中文名 + `glowing()`）：
  - `EvolvedWandOfMagicMissile` —— 魔弹伤害 ×2
  - `EvolvedWandOfFireblast` —— 灵炎（不被水熄灭，用 `SpiritFire` buff）
  - `EvolvedWandOfLightning` —— 自电转盾（40% 伤害转护盾）
  - `EvolvedWandOfBlastWave` —— 伤害 +50%、撞墙眩晕 ×2、可调推距 1/3/5
  - `EvolvedWandOfCorrosion` —— 缠绕 +1 回合
  - `EvolvedWandOfCorruption` —— 触发 +30%
  - `EvolvedWandOfDisintegration` —— 命中视野/落空 50% 省充；分裂形态
  - `EvolvedWandOfFrost` —— 双形态（冰霜直击 / 冰雪区域 3×3 持续 4 回合）
  - `EvolvedWandOfLivingEarth` —— 泥沙 + 伤害同源 +40%
  - `EvolvedWandOfPrismaticLight` —— 光束改 3 格宽矩形、致盲/增伤
  - `EvolvedWandOfRegrowth` —— 取消次数限制
  - `EvolvedWandOfTransfusion` —— 护盾吸 20% 生命
  - `EvolvedWandOfWarding` —— 消耗充能直接成高阶段哨兵
- `EndWandEvolution.java` —— 源→进化映射注册表，`evolve()` 保留 level+curCharges
- `EvolveWandRecipe.java` —— 配方：原版法杖 + 强化符石 + 星陨花之种 → 进化法杖
- `EndModeWand.java` —— 双形态法杖接口（背包窗口切换）
- `SpiritFire.java` —— 复制原版 `Burning`，去掉"站水里熄灭"判定
- `EndFrostField.java` —— Blob 子类，冰雪区域机制

### 统一进化基础
- 进化产物继承源法杖等级（至少 +8）
- 真实等级 `level()`/`buffedLvl()` 均按原等级
- 充能上限提升至 **20**（10 起步、每级 +1、上限 20）

### B2/B4 双形态
- `EvolvedWandOfDisintegration`：单线 +20% / 分裂（耗 1 充）
- `EvolvedWandOfPrismaticLight`：固定 3 格宽矩形光带
- `EvolvedWandOfFrost`：冰霜直击(耗1) / 冰雪区域(耗2)
- 老魔杖注入进化法杖后保留自身充能上限（`MagesStaff` 对 `EndModeWand` 豁免封顶）

### 图鉴
- 13 把进化法杖 + `EndGemItem` 注册进 `journal/Catalog`
- 炼成进化法杖时自动 `identify()` + `Catalog.setSeen` + 即时日志

---

## 4. 灵能弓进化

### 新增类（`endcontent/evolved/`）
- `EndSpiritBowMight.java` —— 附魔灵弓（双模式：稳固本体 +50% 触发倍率 / 每击随机附魔含稀有；本体 5 选 1）
- `EndSpiritBowStorm.java` —— 雷鸣灵弓（攻速 +50%，命中链闪电 1 格、50% 箭伤）
- `EndSpiritBowSummon.java` —— 唤魔灵弓（击杀 20% 召同款友军，生命 30%）
- `EvolveSpiritBowRecipe.java` —— 三选一配方：原版灵能弓 + 灵能核心 + 方向料
  - 升级卷轴 → 附魔灵弓
  - 雷鸣魔药 → 雷鸣灵弓
  - 唤魔晶柱 → 唤魔灵弓
- `SpiritBowCoreRecipe.java` —— 灵能核心配方：2 升级卷轴 + 50 液金
- `SpiritBowCore.java` —— 灵能核心物品（`endcontent/items/`）

### 约束
- 已锻成品弓不可再当基底
- 奥术灵弓分支已移除

---

## 5. 装备进化族（破印 / 匕首）

### 破印三分支（护甲技能键）
新增类（`endcontent/armor/`）：
- `BladeShieldSeal.java` —— 消耗 20% 当前 HP → 30% 最大 HP Barrier，cd=200
- `BloodRageSeal.java` —— 扣 30% 当前 HP → 攻击 +100%（`EndRageAttack`），cd=200
- `FlyWeaponSeal.java` —— 耗 1 MetalShard，把所装近战单程掷向指定格，80% 伤害，cd=20

修改原版类：
- `BrokenSeal.java`：新增 `armorSkillKey()`/`armorSkillUsable()`/`armorSkillEffect()` 抽口
- `Armor.java`：`actions()` 在贴了子破印且已穿戴时追加技能键；`execute()` 调用对应效果

冷却 Buff（`endcontent/artifacts/buffs/` + `endcontent/weapons/buffs/`）：
- `BloodShieldCooldown`、`RageCooldown`、`ThrowWeaponCooldown`、`ExecutionCooldown`、`TeleportCooldown`、`RecoverCooldown`

### 匕首三分支（炼金产出）
新增类（`endcontent/weapons/`）：
- `AssassinDagger.java` —— 基础匕首（defaultQuantity=1，命中传送）
- `EmbedDagger.java` —— 嵌住→回收状态机基类
- `DaggerTeleport.java` —— 回收传送至敌人背后 + 隐身 1 回合
- `DaggerExecution.java` —— 非 BOSS 且 0<HP<25% 处决
- `DaggerTrident.java` —— 高数值投掷（不回旋）
- `AssassinateDagger.java` —— 基础背刺
- `FlyingBlade.java`

配方（`endcontent/evolved/`）：
- `EvolveSealRecipe.java` —— 破印 + MetalShard + 方向料 → 三破印
- `EvolveDaggerRecipe.java` —— AssassinDagger + MetalShard + 方向料 → 三叉戟/传送/处决

### 注册
- `Generator.MIS_T5` 增加 AssassinDagger 系列（prob=0，不入世界掉落，仅图鉴）
- `Catalog.MISC_EQUIPMENT` 追加三破印
- 盗贼开局投掷物改用 AssassinDagger

---

## 6. 挑战区系统框架

### 新增类
- `endcontent/challenge/ChallengeArea.java` —— 区域注册表 + 层号动态调度

### 机制
- 主线 1-25F 后，从 26F 起把选中区域按 id 顺序串联
- `areaAtDepth(depth)` 动态计算某层属于哪个区、区内偏移
- `createAreaLevel(areaId, floorIn, depth)` 按区内偏移创建关卡
- `WndChallengeAreas` —— 开局**单选**窗口（`firstSelectedOnly` 数据层兜底），每个区域带 `desc` 描述
- 存档隔离：`Statistics.challengeMask` 按存档记录，不依赖全局 `SPDSettings`

### 已注册区域
| id | 名称 | 层数 | 来源 |
|---|---|---|---|
| 1 | 空洞遗迹 | 8 | 魔绫 |
| 3 | 银河深渊·火龙 | 1 | 魔绫 |
| 4 | 伊比利亚·海嗣 | 2 | 方舟 |
| 5 | 嘉维尔·雨林 | 2 | 方舟 |
| 6 | 汐斯塔·海滨 | 2 | 方舟 |
| 7 | 六大天王 | 6 | 原创 |

### 兼容桩
- `Dungeon.extrastage_Sea` / `Dungeon.extrastage_Gavial` —— 方舟原版 31-40F 分支标志，由挑战区勾选推导

---

## 7. 空洞遗迹（魔绫移植）

### 关卡
- `HollowLevel.java` —— 常规层（27-30F）
- `HollowExitLevel.java` —— 入口层（26F）
- `hollow/CerDogBossLevel.java` —— 冥犬 Boss 层（31F）
- `hollow/TheatreLevel.java` —— 剧院层（32F）
- `hollow/MorpheusBossLevel.java` —— 四柱 Boss 层（33F）

### Boss（`actors/Boss.java` 精简基类）
- `DeadDogCerberus`（冥犬，1151 行）
- Morphs（三阶段 Boss，在 MorpheusBossLevel 登场）
  - ShubNiggurath / Nyarlathotep / YogSoul / MyCoreHeart
  - TowerGodsBad / TowerTimeBad / TowerMachineBad / TowerMindBad

### 怪物
- Vampire、Butcher、Crumb、Ghost_Halloween、Pumking_Ghost、PumkingBomber、ApprenticeWitch、Frankenstein、DeadDogCerberus

### NPC
- SliceGirl、DeathRong、ZeroBoat、SliceAlter、CerbusSleep、MorphsNPC、GodNPC

### 武器/物品
- `DeathRongBoat.java`（空洞遗迹专属武器）
- `StarCrystal.java`（星晶）
- 传说武器 7 把（`items/weapon/melee/legend/`）：
  - MoonDao、ForestBow、GoldLongGun、SaiPlus、KingAxe、RiceSword、DiedCrossBow
  - `LegendWeapon.java` 基类，`Item.LengedsItem` 接口
- `DragonShiled.java`（护甲）
- `EndFloorSkip.java`（深渊传送符，直接到 25F）

### 剧情系统
- `custom/utils/` 下 `WndDialog`、`Plot`、`Choice`、`ChoiceButton`、`Script`、`SkipIndicator`（889 行）
- 3 个剧情 plot：MorphsNPCPlot、MorphsEndTheaterPlot、MorphsGodEndTheaterPlot

### 资源
- 空洞遗迹贴图/音乐/42 个精灵
- `Assets` 扩展：BOAT/TYPHON/SCSR/MPHON/BBAT/BATEX/YOW_SENTRY/NCSBR/BLEED_SENTRY 等
- `Window` 4 色（CYELLOW/CWHITE/Pink_COLOR）
- `Badge` 扩展：HALOFIRE_DIED/CITY_END/NYZ_SHOP

---

## 8. 银河深渊（魔绫移植）

### 关卡
- `GalaxyLevel.java` —— 熔岩洞贴图常规层
- `LaveCavesBossLevel.java` —— 火龙 Boss 层

### 内容
- FireDragon Boss 及依赖链：DragonWall、DiedClearElemet、ClearElemtGuard、ClearGuardSprite、ColdMagicRat、5 个 quest 道具、DragonShiled、3 个精灵
- `Terrain.LAVA` 地形常量
- `Statistics.Galaxy_Rules` 开关
- `Galaxy` 简化为单层直接进火龙场地（BossRush 式）

---

## 9. 方舟 3 区（明日方舟移植）

### 关卡（12 个文件，当前仅路由 Boss 层）

> **当前布局**：每区 **2 层**（仅 Boss 竞技场），常规层已被移除。
> `SeaLevel_part1/2`、`GavialLevel/2`、`SiestaLevel_part1/2` 文件仍存在但不再被路由（遗留自 10 层设计）。

**伊比利亚·海嗣（areaId=4，2 层）**
- floorIn 0 → `SeaBossLevel1.java`（Boss1）
- floorIn 1 → `SeaBossLevel2.java`（Boss2）

**嘉维尔·雨林（areaId=5，2 层）**
- floorIn 0 → `GavialBossLevel1.java`（Boss1）
- floorIn 1 → `GavialBossLevel2.java`（Boss2）

**汐斯塔·海滨（areaId=6，2 层）**
- floorIn 0 → `SiestaBossLevel_part1.java`（Boss1）
- floorIn 1 → `SiestaBossLevel_part2.java`（Boss2）

**未路由的常规层文件（遗留）**：`SeaLevel_part1/2`、`GavialLevel/2`、`SiestaLevel_part1/2`

### Boss / 怪物（方舟移植，部分）
- Pompeii（汐斯塔 Boss2）、SiestaBoss（汐斯塔 Boss1）
- SeaBoss1、Isharmla（伊比利亚分部件 Boss）
- Eunectes、Tomimi（嘉维尔 Boss）
- Ceylon（汐斯塔 NPC）
- LavaSlug、Talu_BlackSnake、Ergate、Schwarz、Volcano、Cronin、Wraith_donut、Blast、BossSlug
- Tiacauh 一族（Brave/Lancer/Ripper/Shaman/Ritualist/Warrior/Fanatic/Addict/Sniper/Shredder）
- 海嗣一族（SeaCapsule/SeaLeef/SeaReaper/SeaRunner/Sea_Octo/HeavyBoat/Piersailor/FloatingSeaDrifter/NetherseaBrandguider/IsharmlaSeabornHead/Body/Tail）

### Painter / Trap / Item
- `levels/painters/SiestaPainter.java`、`IberiaPainter.java`、`GavialPainter.java`
- `levels/traps/HallucinationTrap.java`、`OblivionTrap.java`、`OriginiumTrap.java`
- `items/quest/Obsidian.java`、`items/ArmorUpKit.java`
- `levels/rooms/special/CoreRoom.java`
- `items/NewGameItem/Certificate.java`

### 音乐
- 12 首区域/Boss 曲目（伊比利亚/嘉维尔/汐斯塔 各 game1/2 + boss1/2）

### 地形修正
- 6 个方舟常规关卡覆写 `initRooms()` 移除 `RegionDeco*`/`LibraryHall` 房间（避免 fork 自有地形在方舟图集里渲染为垃圾）
- 图集索引重映射：CHASM 家族 5 个模式从方舟索引移到 fork 索引
- 清空方舟图集索引 31-39（fork 自有地形：CRYSTAL_DOOR/HERO_LKD_DR/LAVA/REGION_DECO/MINE_*）

---

## 10. 六王

### 关卡（`levels/boss/`）
- `SixKingsLevelBase.java` —— 基类（29×29 竞技场，sealOnEnter）
- `SixKingsLevel1-6.java` —— **6 层 Boss 战**（无序章 NPC，直接从法术王开始）

### Boss（`actors/mobs/bosses/sixkings/`）
- `ImmortalKing` —— 不死王
- `SpellKing` —— 法术王（元素光束 + 闪电弧）
- `DebuffKing` —— 减益王（3×3 毒气/腐蚀/麻痹/混乱/火区域）
- `GuidingKing` —— 引导王（召唤 6 个原版 YogFist）
- `RangeKing` —— 远程王（5×5 炮击区域预警 + 冲击波）
- `OmniKing` —— 全能王（终焉）

### 辅助
- `ArtilleryShell.java` —— 炮击预警
- `DebuffAreas.java` —— 减益区域辅助
- `SixKingSprite.java` —— 精灵（用原版 warrior.png）
- `CompleteNote.java` / `NoteFragment.java` —— 笔记碎片收集系统

### 音乐
- 1 首序章 + 5 首 Boss 曲目（boss.ogg / boss2-5.ogg / boss_kalt.ogg）

---

## 11. 挑战项变更

### 新增挑战（`Challenges.java`）
| 常量 | 值 | 说明 |
|---|---|---|
| `CONVENIENCE` | 512 | 便利测试挑战（开局送测试包，非难度项） |
| `COSTLY_ALCHEMY` | 1024 | 炼金无望：炼金合成需 1.5× 能量 |
| `INFLATION` | 2048 | 通货膨胀：商店售价 +50%，卖出 -50% |
| `DHXD` | 8192 | 银河深渊相关开关 |

- `MAX_VALUE` → 4095，`MAX_CHALS` → 12

### 禁用职业
- `DUELIST`、`CLERIC` 的 `isUnlocked()` 恒为 false（全局不可选，仍保枚举/存档兼容）
- 中间曾回退一次，最终仍为禁用

---

## 12. 便利功能

### CONVENIENCE 挑战开局包
- 金币 +300
- 2 颗随机类型宝石（已鉴定）
- 13 把进化法杖（已鉴定）
- 3 把进阶灵能弓
- 升级卷轴 ×15、强化符石 ×6、液金 ×150、灵能核心 ×3
- 雷鸣魔药 ×3、唤魔晶柱 ×3
- 容器袋（法杖袋/卷轴袋/药水带/种子带）
- 板甲、随机传说武器 1 把
- 深渊传送符（`EndFloorSkip`，使用直接到 25F）
- 标记所有自造物品为已见过（`Catalog.setSeen`）
- 刺杀匕首系列、破印系列（已鉴定，可直接试贴）

### 便利入口
- 挑战入口不再要求先通关即可开（本地测试可用便利开局）

---

## 13. 原版内容调整（删除 / 禁用 / 修正）

### 删除/移除
- **下水道木桶**：`SewerLevel.buildFlagMaps()` 中 `REGION_DECO`/`REGION_DECO_ALT` 桶状装饰重写为 `EMPTY`，木桶不再生成
- **监狱牢笼/木桶装饰**：`PrisonBossLevel.addCagesToCells()` 改为空方法体
- **BossRush 区**：经核实 ShubNiggurath 等 8 个 Boss 本就是 Morphs 在 33F 登场的，BossRush 属重复且破坏性，已删除
- **桃神试炼/深影领域/森林灾厄**：无战斗 Boss，删除
- **枪械系统**：GunWeapon、ShotgunWeapon、C1_9mm、CatGun、CrabGun、SnowHunter、3 个弹匣、3 个配件、2 个枪械 Buff 全部移除（含 Generator 掉落池、Dungeon 便利给件、Recipe 弹药配方）
- **ClearSword（清道夫）**：传说武器，已移除
- **方舟常规层**：SeaLevel_part1/2、GavialLevel/2、SiestaLevel_part1/2 不再被路由（每区仅保留 2 层 Boss 竞技场）
- **六王序章**：引路人 NPC 及其楼层删除，直接从法术王开始

### 修正
- `DungeonTileSheets` 常量逐对比对：19/25 共享常量一致，CHASM 家族已重映射
- `Hero.actTransition`：移除"depth>=26 仅 REGULAR_ENTRANCE 可踩"限制（此前导致下楼变上楼）；恢复 REGULAR_EXIT
- `Level.activateTransition`：按 transition TYPE 正确判断 ASCEND/DESCEND
- `RiceSword.max()`：`hero.buff(Hunger.class)` 前加 `hero != null` 守卫（修图鉴 NPE）
- `MeleeWeapon.max/min/STRReq`：加 `Dungeon.hero` 守卫
- 冷却 Buff 图标改用 <32 的既有帧（buffs.png 大片仅 32 帧）
- **存档路径稳定**：`DesktopLauncher` 中 `title` 回落固定名，避免 unpacked 运行写入 `null/` 目录
- **导入原版存档**：首次运行若 fork 存档目录为空，自动从原版 SPD（或"Tomorrow RogueNight"）复制 game1-6 + badges/rankings/journal/bones
- **火龙文案**：补齐 firedragon 17 个汉化键（此前仅 1 个导致 NO TEXT FOUND）
- **灵能核心图标**：`SpiritBowCore` 改用专用 `SPIRIT_BOW_CORE` 图标常量（此前用 `SOMETHING` 占位）

---

## 14. 资源 / 地形 / 音乐

### 图集
- `items.png` 图集扩展与修正（曾扩到 640 导致错乱，还原为 512，最终扩展到 1024）
- `ItemSpriteSheet` 新增常量：`LENGYWEAPONS` 块 16 个、`ARMOR_ANCITY`/`WATERSOUL`/`STAR_CRYSTAL`/`DEATHRONG_BOAT`/`INFO_CERTI`/`SPIRIT_BOW_CORE`
- 每个新常量配套 `assignItemRect`

### 地形
- `Terrain.LAVA` 常量
- `Terrain.REGION_DECO`/`REGION_DECO_ALT`（桶/牢笼装饰，已移除生成）
- 方舟图集索引重映射：CHASM 家族 5 个模式移到 fork 索引；清空索引 31-39（fork 自有地形）
- 6 个方舟常规关卡覆写 `initRooms()` 移除 `RegionDeco*`/`LibraryHall` 房间

### 调试工具
- `ui/TerrainDebugOverlay.java` —— 地形调试覆盖层（默认关闭 `enabled=false`），鼠标悬停显示格号/地形值/图集索引/邻居值

### 音乐
- 空洞遗迹：区域/Boss 曲目
- 银河深渊：火龙 Boss 曲目
- 方舟 3 区：12 首区域/Boss 曲目（当前仅 Boss 层使用）
- 六王：6 首主线 Boss 曲目（boss/boss2-5/boss_kalt）

### 精灵
- 空洞遗迹：42 个精灵
- 方舟 mobs/Boss 精灵
- 六王精灵（用原版 warrior.png + YogFist 精灵）

---

## 15. 附录：全局机制层兼容桩

方舟 MOD 的全局机制层 6 个枢纽，在 fork 中的处理方式：

| 符号 | fork 现状 | 处理方式 |
|---|---|---|
| `actors.buffs.Silence` | 缺失 | 新建桩（FlavourBuff 子类） |
| `actors.buffs.Camouflage` | fork 有 `items.armor.glyphs.Camouflage` | 新建 `actors.buffs.Camouflage` 桩 |
| `items.Skill.Skill` | 缺失 | 新建桩（Item 子类 + `doSkill()`） |
| `items.rings.RingOfAmplified` | 缺失 | 新建桩（Ring 子类） |
| `utils.BArray` | fork 有 `com.watabou.utils.BArray` | import 重定向，不建桩 |
| `TomorrowRogueNight` | fork 无 | 全量替换为 `ShatteredPixelDungeon` |

---

## 变更统计

| 类别 | 数量 |
|---|---|
| 总提交数 | 203 |
| Java 源文件总数 | 1537 |
| 新增挑战区 | 6 个（空洞/银河/伊比利亚/嘉维尔/汐斯塔/六王） |
| 挑战区总层数 | 21 层（空洞 8 + 银河 1 + 方舟 3×2 + 六王 6） |
| 新增关卡 | 约 26 个（含未路由的方舟常规层） |
| 进化法杖 | 13 把 |
| 进化灵能弓 | 3 把 |
| 传说武器 | 7 把 |
| 破印/匕首进化 | 6 个成品 |
| 宝石类型 | 5 类 |
| 新增挑战项 | 4 个（CONVENIENCE/COSTLY_ALCHEMY/INFLATION/DHXD） |
| 禁用职业 | 2 个（DUELIST/CLERIC） |
| 方舟移植怪物/Boss | 约 60+ 个 |
| 新增音乐曲目 | 约 25 首 |
