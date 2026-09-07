# 《破碎的像素地牢：终焉扩展》实现路线图
_Shattered Pixel Dungeon: End — implementation roadmap_

> 基线：官方 SPD 3.3.8 fork。命名：沿用 `com.shatteredpixel.shatteredpixeldungeon` 包名叠加新增。素材来源：两份技术报告分析的**魔绫像素地牢**与**明日方舟地牢**(jar 0.5.3) 只读借鉴（其原文件本体不动）。开发清单依据 `../开发.txt` 与 `../_analysis_temp` 分析笔记。
> 验收：每一次自研/移植单元都应在 GitHub Actions(见 `.github/workflows/build.yml`) 上 `:desktop:compileJava / :android:assembleDebug` 通过后合入。

---

## 0. 工程布局约定
- 自研系统一律放进独立子包，避免与上游类纠缠时误改主循环。
- 计划子包：
  - `...items.gems`   装备宝石（子物品/镶嵌）
  - `...items.wands` 内已有法杖 → 进化逻辑以子类/数据表增量实现，不重写原 wand
  - `...actors.buffs`（现有包）：蜕变/挑战相关的 passive/计时 buff
  - `...journal.quests` 与 `...levels`：挑战区域接原版 dungeon 区域机制
  - 无尽模式/循环难度：主要落在 `Dungeon`/`Challenges` 的扩展点
- 兼容：凡新增都会用到现状类，每个文件头注明“END: <功能>”。

## 1. 装备宝石系统（共五项宝石：攻击/防御/命中/闪避/生命）
对接点（已核实的官方类）：
- `items.EquipableItem`、`items.armor.Armor`、`items.weapon.Weapon`/`MeleeWeapon`——装备镶宝载体；
- 属性随装备等级成长 → `Weapon`/`Armor` 的 `buffedLvl()/level()` 处，且持久化跟随 item 的 bundle(参考 `enchantment`/`glyph` 的 store/restore)。
- 命中/闪避宝石 → `Char` 攻击结算(`items.weapon.Weapon.proc`,`actor` 命中判定 `accuracyFactor`…)、`armor.evasionFactor`。
- 生命宝石 → `Char` max HP（buffs `MAX_HP` 类 buff 或 actor 生命上限）。
- 掉落/图鉴：需要把宝石登记进物品生成(Generator/掉落)或仅靠确定性发现(炼金 forged)先最小实现。
### 里程碑 M1: 数据模型 + 可镶嵌标记 + 基础攻击/防御/生命加成接现有方法，能在 CI 编译。
- [x] M1.a 数据模型：`endcontent.EndGem`(5类) + `EndGemProfile`(随装备等级线性，`bonusAt(int)`)
- [x] M1.b 可镶嵌标记+持久化：`Weapon.gem`/`Armor.gem`(EndGem序号,-1=无)+`hasGem()/gemType()`，bundle `end_gem` 键，守卫式 restore，不随 reset 丢失(仿 enchant/glyph)。提交 `a6bc480`
- [ ] M1.c 数值接入：攻击宝石→武器伤害/命中点、防御宝石→护甲减伤/闪避点、生命宝石→max HP（需精读 damageRoll/accuracyFactor/evasion/drRoll 后逐个接入）
- [ ] M1.d 可获得性：参照 `ScrollOfEnchantment` 的 `InventoryScroll`/`GameScene.selectItem` 交互做"对装备使用宝石"的入口 + ItemSprite/generator 登记


## 2. 法杖蜕变/进化
对接点：`items.wands.Wand.level()/buffedLvl()/upgrade()`、`initialCharges()/chargesPerCast`；各类具体桩 `WandOfFireblast` 等 `onZap`；`+8` 判定 → 在 `Wand.upgrade()` 或新工具拦截。
各法杖专属"进化方向"（来自 `开发.txt`）需子类方法：直接对对应 wand 提供 evolve 回调，放在数据表集中描述 + 少量开关方法进各 wand。目标 `开发.txt`：「等级归零保留+8 属性、最大充能 20、充能+20%、成长效率+20%」。
### 里程碑 M2: 通用蜕变器 + 全部 13 把法杖（真实实现 + 统一成长档）【已完成】
- [x] 13 把 `EvolvedWandOf*`（新物名 + 各自真实专属机制，均有独特 `glowing()`)
- [x] 统一蜕变成长档：每把 `buffedLvl() = 8 + buffedLvl*1.2`(等同“等级归零保+8、成长+20%”)、`updateLevel()→maxCharges=20`
- [x] 真机制逐把（依 endcontent/evolved 文件实测）：
  魔弹×2 / 棱光伤害+30% / 冲击波伤害+50% / 闪电自电转等量盾 / 解离(命中得视野或省充) / 腐蚀命中缠绕 / 冰霜区域 / 注魂吸血 / 腐化概率增强 / 再生长草 / 活体大地守卫装备取向 / 哨戒 / 灵炎(火不水熄)
- [~] 细微待核（非阻塞）：LivingEarth 与 Prismatic(棱光) 各残留一行注明 TODO(其余机制已在)；火焰“不被水熄”以守卫/注释体现，建议实测确保同水不熄真的成立。

## 3. 灵能弓特殊改造
对接点：`items.weapon.melee`/`Bow`(SPD 3.3 猎人有 `SpiritBow(wand?)`) 需先定位。设计含 4 路线(附魔增强/雷鸣秘药/唤魔晶柱=击败生成元素盟友/奥术聚酯=魔法伤害)，借鉴方舟「技能书 Item 化 + 召唤物 Wandering AI」与魔绫 DirectableAlly。
### 里程碑 M3: 先选 1 条路线（唤魔晶柱）做成 + 召唤物基座。

## 4. 职业重制（删圣骑/武者，战士/盗贼新机制）
对接点：`actors.hero.HeroClass`(现为 Warrior/Mage/Rogue(Huntress)/Duelist/Cleric)。设计要把 Cleric 去掉并加回 4 职业风格，波及 UI/选择/U quest 多。工作量最大，放最后单独轨道。
### 里程碑 M4（拆细）。

## 5. 挑战系统（26F 之后 / 异界区域）
对接点在原版区域机制基础上做"额外章节区域"（参考方舟 NewRhodesLevel 思路、魔绫 Hollow/竞技场）。每区域=新 `Level` + new Boss + painter + 入口（Boss 多阶段，借鉴 Isharmla 多段、魔绫 HP-threshold）。
### 里程碑 M5：做一个可进入的 26F 挑战房骨架验证流程。

## 6. 无尽模式
持续循环 + 全局 debuff 递增 + 怪物属性成长。可复用 SPD `Challenges`/Difficulty 数据驱动而非硬编码。
### 里程碑 M6：循环计数 + 每轮一个 modifier 表。

---

## 推进节奏（自读协调用）
1. 每步新增**尽量自包含且不动上游主循环**，先编译后合并。
2. 每系统在独立分支；合入 main 前 CI 必绿。
3. 素材(魔绫/方舟)用 `Magic_Ling_Pixel_Dungeon-stable/**` 与 `明日方舟地牢.jar` 反代借鉴库定位其某系统真实实现，搬思路不搬文件。

_创建时间：本会话。保持更新。_
