# 灵能弓四选一改造 — 总览 + 实现建议

依据：灵能弓改造（标题“三”为笔误 = 四选一）。材料路径分两段：
1. **晋升核心**：2× 升级卷轴 + 100 液金 → 合成"灵能核心"。
2. **灵能核心 + 灵能弓 + 特殊材料** → 从下列 **四选一** 永久改造之一：
   - ① 附魔强化（Enchantment Focus）
   - ② 雷鸣秘药（Thundering Salve）
   - ③ 唤魔晶柱（Summon Crystal）
   - ④ 奥术聚酯（Arcane Resin，攻击转为魔法伤害）

本文档是“改动总览 + 每方向的实现落点/建议”，未落码。依你选择**先出文档评审**。

> 仓库：`E:\破碎的地牢\_EndShatteredBuild`（Java / 词: 由研究只收集 _ShardDungeon_SpiritBow_ThreeWay_Research.md）。
> 以下引用类路径均相对 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/`。

---

## 0. 现状事实关（很关键，决定是否要先补材料）

研究确认（详见 `_ShardDungeon_SpiritBow_ThreeWay_Research.md`）：
- 灵能弓 = 本 fork 的 **`items/weapon/SpiritBow extends Weapon`**（起点弓，等级跟随 `hero.lvl/5`，`isUpgradable()` 为 **false**；仅能通过 附魔卷轴/石/诅咒注入 获得附魔）。伤害与命中全线 `SpiritBow`/其 `SpiritBow.SpiritArrow`（箭头委托弓 `proc/hasEnchant/damageRoll`）。
- 附魔体系：`Weapon.Enchantment`（Weapon.java:529 起）；`procChanceMultiplier` = `genericProcChanceMultiplier(attacker)`，其核心乘法器来自 **`RingOfArcana.enchantPowerMultiplier` = 1.175^buffBonus**（Weapon.java:560 / RingOfArcana.java:60）。
- **液金**：本仓库只有 vanilla 的 `items/LiquidMetal`（弹道修类/制造弹箭的“液体”，并非通用升级材料）。**无**专用“液金=100 可消耗”“特殊材料”“自定义升级卷轴”类。
- **特殊材料**：`SpecialMaterial` 类**不存在**。
- 自定义“把某物变形态”的 recipe 先例：`endcontent/evolved/EvolveWandRecipe.java` + `EndWandEvolution`（两原料、动态 testIngredients、`usableInRecipe` 需特判）。
- 在物品上用一次性材料/开选择 UI 的先例：`items/endcontent/EndGemItem.java`（enum socket 幂等写入 Weapon.gem）+ `endcontent/evolved/EndModeWand.java`（背包 WndUseItem 切换形态）。

**因此“材料三件套”并非开局就可用**：需要新建 1 个可消耗材料物品（合成用 Recipe）+（可选）1 枚合成“灵能核心”的中间产物；在此之前先保证：
- 给 `Recipe` 注入途径 + `usableInRecipe` 特判 `SpiritBow`（参考 EvolveWandRecipe.testIngredients）。
- 物品 `info()` / 描述需要新增消息键（`assets/messages/items/items*.properties` 里按全小写 FQN 追加）。

更省事的替代（需你确认是否可接受）：
> 直接把 2 熔石+100 液金量化为“普通合成”的一步，或让“灵能核心”变成一个真正的 `Item` 产物并允许被右键用于弓——避免改武器升级判定。

---

## 总改动草图（贯穿所有方向）

新增：
- `endcontent/endBow/...` 或复用 `items.weapon`：
  - `SpiritBowCore.java`（灵能核心，可被消耗）。二选一合成产物。
  - `SpecialBowMaterial.java`（特殊材料——若没有则新建 1 个，落到已有端使用的材料习惯：仿 `EndGemItem`）。
  - `Salve` / 三个升级“方向”可选择作为 **弓状态字段 + 一个内部 enum**（建议 `SpiritBow.Upgrade` storing ordinal 到 Weapon 类似 `gem` 的 int），并把“四选一 UI”做成 `SpiritBow` 实现 `EndModeWand`（复用我们给 B2/B4 做的「背包→法杖详情窗口里一排 RedButton」）。
  - `SpiritBowEnchantFocus`（可选包装），或直接给 Weapon.Enchantment / Weapon 加一个 **`procChanceBonus`** 以叠加到 `genericProcChanceMultiplier`。

通用钩子：
- Weapon 上加一个“额外附带效果 / 命中 proc 号”的统一口子比直接在 `SpiritBow.proc` 里堆 if 好维护：新 `enum`+在 `SpiritBow.proc()`（research 已指其在 plant 干完自调 `super.proc`）前转给私有分支，依方向各走不同逻辑。

---

## ① 附魔强化（Enchantment Focus）

需求解读：基础附魔触发概率提到 **50%**；五选一从“正向附魔候选里选 5 种让人挑”；奥术戒使真实普攻触发＞100% 时，把溢出“转成强化附魔效果”。

落点/实现建议：
- “触发概率”的实际作用面 = `Weapon.Enchantment.genericProcChanceMultiplier` / 每把法 `.proc(...)`. SpiritBow 的 enchant 同样跑此路径；若想真的“每次平射一发触发”，要接 `SpiritBow.SpiritArrow.proc`(research)或 bow 的 `proc`。
- 建议新增：
  - `Weapon.int procChanceBonusFlat`（或 bow field），并让 `genericProcChanceMultiplier` 或弓自己的加法处应用 +50%（做“保底 50%”：`chance = max(chance, 0.5f)` 更贴合描述）。
  - 溢出派强：当 `total>1f`（奥术戒拉高）时，把 `(total-1f)` 作为“Enchant Focus 额外层”，在成品附魔效果上乘 (1 + 溢出) 或额外施加其正面增幅。
- 五选一：在“选 direction ①”后的流程里如 `enchant.random` 候选后让它从 `Enchantment.common+` 挑 5 个提供 `WndOptions` 让玩家选其一写回弓。若有现成 `WndEnchantSelect`（research 提示该卷轴用了自己的 select 窗口）可直接复用其选择机制（限制 src）。
- 风险：弓 `buffedLvl()` 无视 Buff，但 enchant 强度也按此缩放；不要与旧“附魔 harden / curse infusion”冲突；UI 文案 in English+zh 需新增 key。

---

## ② 雷鸣秘药（Thundering Salve）

需求：攻击附带 20% 伤害的闪电链（复用闪电法杖的链）。

参照物（research）：
- `items/weapon/enchantments/Shocking.java` static `arc(Char attacker, Char defender, int dist, ArrayList<Char> affected, ArrayList<Lightning.Arc> arcs)` — 触发链 + VFX 一步到位（`ShockElemental.meleeProc` 用它）。
- `WandOfLightning` 及其 fork `endcontent/evolved/EvolvedWandOfLightning`（copy 主力）。

实现建议：
- 在 `SpiritBow.proc()`（当方向=雷鸣）里，命中造成 `base = 该hit总伤`，链伤害取 `round(base*0.2f)`，调用 `Shocking.arc(curUser, defender, 距离(如2/水/等), ...)` 逐只结算电伤；VFX 用 `new Lightning(arcs,null)` 加入 sprite.parent。
- 判定一次只跳与主伤害同属一次射击；链不触发弓的其他 enchant，需避免嵌套（用 guard flag）。
- 注意 `arc` 只对“能传导 path”的电目标。若要同 WandOfLightning 减减电伤 per-target 可带 decay。
- 文案 key：新增一条状态/射击说明。

---

## ③ 唤魔晶柱（Summon Crystal）

需求：击杀敌人 10% 生成随机元素盟友；盟实力≈30% 自身 maxHP。

参照物：
- `items/spells/SummonElemental.java`（已打包好 “可生成各类元素并设为法术 Ally” = Reflection 取 enum 子类、`GameScene.add`、`setSummonedALly`、HP=HT 等）。
- `endcontent/.../endcontent` 无（research 强调 SummonElemental 就是模板）。Ally 元素子类清单 `{NewbornFireElemental? FireElemental/AllyNewBorn, FrostElemental, ShockElemental, ChaosElemental}`.

实现建议：
- 在 `SpiritBow`/箭头命中后检测击杀：若击杀目标（方向=唤魔），掷 10%；选随机一个元素子类 `Random.element`，按需 `Enchant focus` 降智力；生成 `new X`，设 `alignment=ALLY .. HUNTING`，位置取空邻格（参考 SummonElemental 找相邻空位 / `ScrollOfTeleportation.appear`），`elem.HP = Math.round(elem.HT*0.3f)`（或先用 setSummonedALly() 让它的 scale 自动压下来再一次 setHP）。
- 用与 SummonElemental `InvisAlly` 不同的短命/存在标记避免它与玩家主动召唤冲突（可只限“晶柱召唤时不叠加”）。
- 边界：不在此生成 Boss/忽略非 Mob 击杀；防“最大盟友数”刷屏（可选上限 2-3）。
- 消息 key 新。

---

## ④ 奥术聚酯（Arcane Resin）—— 攻击转为“魔法伤害”

需求一句话，但语义不明最该先定：把一个弓的基础普攻“算成魔法伤害”究竟想影响什么？
常见歧义：
- (a) 让它吃“魔抗/法术抗性”而非护甲（反死 Anti-magic / resist magic）？
- (b) 只是希望它“穿透物抗/护甲（无视物抗）”？
- (c) 作为伤害来源带 magic 标签以触发法术类的相关交互？

研究结论（section D）：Char.damage 仅 `damage(int damage, Object src)` 单入口；“物理 vs 魔法”实质由 `src` 是否命中 `AntiMagic.RESISTS` 等决定。物理（弓/箭）普通途径会先进 `defenseRoll/dr`（物抗），因此要达到你要的魔法语义需：
- 要么自造一个 `src`（如 `SpiritBow.ArcaneDamage` implements Char/空），让经 `damage(…, 该src)` 命中走魔法判定，绕过物/甲减伤（具体实现语法需从 Char.java/对应的 `subtractArmor`/resist 分支逐行核实）；
- 要么仅在 `RingOfArcana` 之类判断上改动。

**这一步强烈建议先书面定语义(a/b/c)再定代码**，避免做成看着像但游戏内实际“多减一次/少减一次”不对。

---

## 建议推进顺序

1.（人评）确认四方向语义（尤④）+ 材料“100 液金/特殊材料”是否要新建 + UI 放“背包窗口”是否接受。
2. 搭地基：新增灵能核心中间产物 + 特殊材料 + SpiritBow 方向/四选一 `EndModeWand` 状态与 `WndUseItem` 选择 + Recipe 注入特判。做一条跑（推荐先 ①附魔强化，最不依赖新物模型）。
3. 依次实现 ①→②→③→④，逐一 device-test（说明无法纯编译验证 UI 文案与手感）。

仍待你拍板的点汇总见上述；答复后可照此推进到“先跑通 ①”。
