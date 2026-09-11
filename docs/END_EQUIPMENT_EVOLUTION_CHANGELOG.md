# 装备进化族 · 落地改动清单（对照用 / CI 排查用）

行为基准仓库：`_EndShatteredBuild`（Java，终焉 fork）
本文列出为“装备进化族”（破印三分支 + 刺杀匕首三分支）新增/修改的文件。**编译未在本会话跑通**
（环境缺 gradle-9.4.0 发行包且外网不通、沙箱仅工作区可写），请在能联网的机器 `./gradlew :core:compileJava`
（或用 GitHub CI）验证后在下方“CI 记录”登记报错，再回我修。

所有路径相对 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/`。

## 一、战士侧：破印 → 贴护甲的“技能键”三成品
载体方案（按清单 1.1/1.3）：破印子类各自经 `Armor.affixSeal` 贴到护甲；护甲 `actions()` 在**贴了子破印且护甲已穿戴且技能可点**时多返回一行技能键，`execute()` 回调用到该成品。

- 改 `items/BrokenSeal.java`：新增泛化技能键抽口
  `armorSkillKey()`(默认 null) / `armorSkillUsable(Hero)` / `armorSkillEffect(Hero)`（默认 no-op）。原版破印行为不变。
- 改 `items/armor/Armor.java`：
  - `actions(Hero)`：在 `AC_DETACH` 之后，`isEquipped && seal!=null && seal.armorSkillKey()!=null && seal.armorSkillUsable` → 追加该动作串。
  - `execute(Hero,String)`：`DETACH` 仍走原 detachSeal；否则若 `action==seal.armorSkillKey() && isEquipped` → `seal.armorSkillEffect(hero)`。
- 新增 `endcontent/armor/BladeShieldSeal.java`：KEY=`BLADE_SHIELD`；消耗 20% 当前HP → 30% 最大HP 的 `Barrier`；`BloodShieldCooldown` cd=200。
- 新增 `endcontent/armor/BloodRageSeal.java`  ：KEY=`BLOOD_RAGE`；扣 30% 当前HP → `actors.buffs.EndRageAttack.DURATION`(攻击 +100%，不改攻速)；`RageCooldown` cd=200。
- 新增 `endcontent/armor/FlyWeaponSeal.java` ：KEY=`FLY_WEAPON`；耗 1 枚 `MetalShard`，把所装近战作为 `MissileSprite(item=Weapon)` 单程掷向指定格，造成 `damageRoll*80%`；`ThrowWeaponCooldown` cd=20。
- 新增（复用已有，未删）：`endcontent/artifacts/buffs/` 下的 `BloodShieldCooldown/RageCooldown/ThrowWeaponCooldown`。
- 消息键（放 `###armor` 段，按钮由 `Item.actionName→Messages.get(ac_+action)` 读）：
  - `items/items.properties` + `items/items_zh.properties`：
    `items.armor.armor.ac_blade_shield=B刀锋护盾/Blade Shield`、`ac_blood_rage=狂暴/Blood Rage`、`ac_fly_weapon=飞掷武器/Toss Weapon`。

## 二、盗贼侧：匕首 → 三个独立成品（炼金产出）
- 改 `endcontent/weapons/AssassinDagger.java`：瘦身为**基础匕首**（min~2..，max~6+2lvl，STRReq 10，耐久 0＝无限，非堆叠非骨）。旧“四态 Type enum/右键自我升级”已去掉。
- 新增 `endcontent/weapons/EmbedDagger.java`（abstract，`extends MissileWeapon`）：严格**嵌住→回收**状态机公共基类——投掷方向命中则记录 `stuckEnemy/stuckCell`（见下），提供 `AC_RECOVER`、`recoverable(hero)`/`execute`→抽象 `recover(hero)`，携 `behindCell(...)` 几何 + `moveTo(ScrollOfTeleportation.appear…)`，`storeInBundle/restoreFromBundle` 只存 stickCell。
- 新增 `DaggerTeleport.java`（刺杀·传送，extends EmbedDagger）——守护嵌态：回收→传送到敌人背后空位（或落点格），成功才 `Invisibility` 1 回合；随后 `TeleportCooldown` 100。
- 新增 `DaggerExecution.java`（刺杀·处决，extends EmbedDagger）——守护嵌态：回收时目标非 BOSS/miniBOSS 且 0<HP<25% 则处决（HP=0→die/damage(-1)+DeathMark idiom），`ExecutionCooldown` 50；否则普通 poke。
- 新增 `DaggerTrident.java`（刺杀·三叉戟，extends HeavyBoomerang）：真回旋自动回手，无嵌态/无附加（“回返”走 Boomerang 一套）。
- 新增 `AssassinateDagger.java`（基础背刺，extends MissileWeapon）：数值背刺刃 min3+lv/max10+2lv（无脆弱的“背后判定”，info 说明）。

## 三、炼金配方（三料·方向料分向，仿 EvolveSpiritBowRecipe）
方向料用仓库现成物品区分三向（两族共用同一套料，映射在各配方便改）。
- 新增 `endcontent/evolved/EvolveSealRecipe.java`：
  基底原版 `BrokenSeal` + 1 `MetalShard` + 方向料 →
  速度药水 PotionOfHaste → `BladeShieldSeal` ；浮空药水 PotionOfLevitation → `BloodRageSeal` ；复仇卷轴 ScrollOfRetribution → `FlyWeaponSeal`。
  消耗：邪能/Math 方向料各−1、原版破印归零(炉内当空气移走)。产物 identify + `Catalog.setSeen`。
- 新增 `endcontent/evolved/EvolveDaggerRecipe.java`：
  基底 `AssassinDagger` + 1 `MetalShard` + 方向料 →
  速度药水→`DaggerTrident`；浮空药水→`DaggerTeleport`；复仇卷轴→`DaggerExecution`。
  已锻造成品不得回作基底（防互锻）。
- 改 `items/Recipe.java`：两条配方加进 `variableRecipes`（任意料槽数均判定）。

## 四、注册 / 便利 / 图鉴
- 改 `items/Generator.java`：`MIS_T5` 增加 `AssassinDagger/DaggerTrident/DaggerTeleport/DaggerExecution`（prob 0，不入世界掉落，仅入图鉴投掷组；probs 补同数量的 0）。
- 改 `journal/Catalog.java`：`MISC_EQUIPMENT.addItems` 追加 `BladeShieldSeal/BloodRageSeal/FlyWeaponSeal`。
- 改 `Dungeon.java`（CONVENIENCE）：**删除**旧 `WarbandSeal` 给件；保留/新增 MetalShard×6、炼金工具箱、基础 `AssassinDagger`、外加一原料级原版 `BrokenSeal` 与三破印成品（identified，直接试贴）。
- 删 `endcontent/artifacts/WarbandSeal.java`、`endcontent/artifacts/buffs/RageBuff.java`（旧案冗余；审计确认其仅 Dungeon 一处用、未注册任何生成器/图鉴）。

## 五、明确范围外 / 待你确认
- 编译级验证：本会话未能跑 `:core`(`gradle` wrapper 需 9.4.0 且无网络) → 务必在正常环境编译一次。
- 数值/手感、UI 文案措辞（血盾/狂暴名字为占位中文）可按需改；动作常数与 `ac_*` 键需同步。

## CI 记录（填你在正常机器跑的结果）
1. build result（成功 / 报错文件列表）
2. 报错行 → 应改哪

## 六、复核后新增改动（本轮）
- 盗贼/开 `Actor/HeroClass.initRogue`：开局投掷物改用 `AssassinDagger`（命中传送+与 ThrowingKnife 数值一致）。
- `AssassinDagger`：改 **stackable 投掷垛、defaultQuantity()=3**（避免掷后快捷整格消失；数量扣至 0 熄、可拾回续用）；保留基础“命中传送(behind+隐身+TeleportCooldown)”。
- 破印护甲技能：`Armor.actions` 不再因冷却/资源把技能键整个去掉（键常留）；`*Seal.armorSkillEffect` 开头若处冷却弹「…仍冷却 x 回合」并 return；`FlyWeaponSeal` 改为**不耗邪能**。
- `DaggerTrident`：由 `HeavyBoomerang`(会回旋) 改为普通 `MissileWeapon` 高数值投掷（不回旋），STRReq 12。
- 冷却 Buff(TeleportCooldown/Execution/Rage/BloodShield/ThrowWeapon) 目前**只在抛/回时生效,角色 Buff 条上不显示读秒** —— 见下方待办。

## 七、待办（部分已完成，见第八节）
- “刺杀只给一把”与“投掷垛可拾回续用”二者在非回手模型下冲突：当前取 `defaultQuantity()=3`(垛)。若要真单把+不消失只能走“回手/virtual 本体留”模型,请下轮确认。

## 八、第四轮改动（用户复核后）
1. **冷却 Buff 显示 `nofound` 修复**：本 fork `core/src/main/assets/interfaces/buffs.png` 仅 **128×64** → 大图集(16×16)=32 帧、小图集(7×7)=162 帧。原先选用的 `RAGE=38 / THROWN_WEP=85 / TARGETED=54` 超出大片帧数故显示 nofound。已改为 **索引<32**：`BloodShieldCooldown=ARMOR(20)`、`RageCooldown=FURY(18)`、`ThrowWeaponCooldown=MARK(27)`、`TeleportCooldown=INVISIBLE(12)`、`ExecutionCooldown=CRIPPLE(23)`；并在 4 处施加点补 `BuffIndicator.refreshHero()` 让图标即时出现。
2. **老法杖灌注进阶杖上限仍 10**：根因＝`items/weapon/melee/MagesStaff.java` 的 `updateWand()`(L378) 与 `restoreFromBundle()`(L453) 只用 `instanceof EndModeWand` 豁免，13 把 `EvolvedWand*` 不实现该接口 → 仍被 “+1 封顶 10”。已改为**同时用既有 `evolvedWandImbued()`（包名 endcontent.evolved.）豁免**，进阶杖保留自身 `updateLevel()` 的 20 上限。
3. **炼金手册看不到配方**：手册＝`ui/QuickRecipe.getRecipes(pageIdx)` 的硬编码分页（0..8，对应 `Document.ALCHEMY_GUIDE` 的 9 个页）。已把 dagger/seal 两条进化配方各 3 个方向样例追加到 **case 6（Weapons/装备页）**，并对 `sampleOutput` 做 null 守卫。
4. **移除监狱牢笼/木桶装饰**：`levels/PrisonBossLevel.addCagesToCells()` 原逻辑是在墙边随机放至多 5 个 `Terrain.REGION_DECO`（该区域装饰＝牢笼/木桶）。已改为**空方法体**（3 处调用点与其它逻辑不动，随时可恢复）。

