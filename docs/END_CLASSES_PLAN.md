# 装备进化族 · 落地清单（破印三分支 + 刺杀匕首三分支）

用途：给下一会话开工用。设计原则 = “完全照 EvolveWandRecipe / 进化法杖的形态”：
炼金把一个基础物 → 进化成 3 个**独立成品物**，各自一套能力；
战士侧以 **Armor（护甲）为载体，装备后额外多一个可触发技能的按键**；
盗贼匕首同样炼金进化为 3 个独立版本。

## 0. 必须遵守的硬事实（会崩的地方早处理，别再撞）
- 仓库真基类路径与名（用库内存在，勿重写）：
  - `items.armor.Armor`
  - `items.artifacts.Artifact`
  - `items.weapon.missiles.MissileWeapon`（含 `min()/max()/durabilityPerUse()/onThrow(int)/STRReq(int)`）
  - `items.quest.MetalShard` 中文名“邪能碎片”，升级共用料
  - 附 神引可投近战：MissileSprite 的参数是 `Item`；回返需 HeavyBoomerang 一套
- Artifact 必须给一个自身被动（`passiveBuff`）否则装备时 NPE；若不想被动就 **不要 extends Artifact**，改用 Extend item 或 ShieldBuff 逻辑。

## 1. 战士：破印 → 炼金三分支（各自为独立 Armor 能力物）

### 1.1 载体结论
原版 `items/BrokenSeal` 是"贴到 Armor 的被动盾物品"，不适合在其按钮上堆三种。方案：
新增三个**可 affix 到 Armor 并额外提供技能键**的“ArmorGlyph/附加能力”——具体实现为在 Armor 的
`actions(Hero)` 中按装备的 seal 分支追加 `AC_*`，并在 `Armor.execute` 处理回调。
（即：破印 affix 到甲之后，甲右键/使用多一行“技能按钮”。）

### 1.2 想产出的三个独立成品（各自成 subclass，继承基类 BreakSeal/或 WarriorShield 机制）
用“破印+邪能 MetalShard”在炼金（Alchemy Registry Recipe）选产出其一，成品类型分别为：

- `endcontent/armor/BladeShieldSeal`：血盾。
  使用消耗 20% 当前 HP，获得 30% 最大 HP 的 Shield（用 ShieldBuff / 就地 shield），冷却 Buff 200。
- `endcontent/armor/BloodRageSeal`：狂暴 +100% 攻击
  （已建：`actors.buffs.EndRageAttack`；在 `Char.attack` 已 ×2；此型装备即带冷却100-200）不碰攻速。
- `endcontent/armor/RingReturn`……命名别较真；飞武 = 带上可用的“飞行近战”按钮，
  无 button 时投您装备的近战(单程动画+80%伤害，用 MissileSprite(item=近战Weapon)，并把邪能耗掉)。

> 进化方式：破印（原版） + `MetalShard` → 从 3 类中按放在第二料/选单挑一支（参照 `EvolveWandRecipe` 三料弹窗；若用它自己 `WndOptions`）。

### 1.3 armor 加按键（最小改）
`Armor.actions(Hero)` 与 `Armor.execute(Hero,String)`：仅在 armor 上的 seal 存在某种 `BrokenSeal`/affixed 型时，
返回对应 ACTION 串并在 execute 回调到该能力物→跳过现有 end 风险过大；改这两处主方法。

## 2. 盗贼：匕首 → 炼金三分支（3 个独立 MissileWeapon）
- 基础：`endcontent/weapons/AssassinDagger`（MissileWeapon）：
  - 数值 min2~6、力量10、durabilityPerUse()→0（无限）
- 进化成三支独立子类（继承该基类或母 Missile）：
  - `.../AssassinateDagger`（基础背刺） type=Backstab
  - `.../DaggerTrident`：三叉戟数值/成长 + 会回（无附加其它）
  - `.../DaggerTeleport`：嵌中后可“传送到它背后格”，隐身1、冷却100
  - `.../DaggerExecution`：非Boss HP<25% 处决；冷却50；无传送
> 炼金用 `MetalShard` 产出其一（参照 EvolveWandRecipe；一次只一支不可并存）。

## 3. 输入/便利
- 便利挑战(GameScene CONVENIENCE)：给 MetalShard×6、原版/AlchemistsToolkit、进化的基件
   破印(若改成 Armor affix 物给一套)、匕首基础
- 不要给无实基的半成品；每根都带 Catalog register 与 identify。

## 4. 实现顺序（一次一条，每步 CI 绿）
1) 匕首基础参数修准 → 编译。
2) 匕首炼金配方产出 3 子类其一（小：三支子类文件 + Recipe）→ 编译。
3) 战士：affix触发技能键（改 Armor actions/execute 一处）→ 编译。
4) 破印三支子能力文件（复用 Char 攻击已挂 EndRageAttack、ShieldBuff、近战抛射动画）→ 编译。
5) 便利 & Catalog 收尾。

## 5. 禁止在下一步里假装“完成”的点
- 凡 “要 +X%” “传送背后” 均需真接口接入且得 CI。
- Armor actions/execute 属于主文件改动，逐处贴可能还有牵涉后续方法（先读后改，每次一条）。
