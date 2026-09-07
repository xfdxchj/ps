# B2 / B4 进化法杖模式改版 设计说明（终焉扩展）

本文件记录 EvolvedWandOfPrismaticLight（棱辉）与 EvolvedWandOfDisintegration（湮解）
的 B4/B2 改版所采用的**具体设计决定**，便于评审与后续微调。

## 0. 需求变化（本次）
用户先后澄清三点，决定实现口径：

- 物理动作：【局内背包 → 点开法杖弹出的“使用/详情窗口”】（`WndUseItem`）里做模式选择，
  而不是主菜单、也不是按剩余充能自动判断。
- B4 棱光：**也做成手选形态**（不再保留最初“充能≥3才触发”的自动逻辑）。
- 旧的 M2 描述效果被**取代**：
  - 棱光原“伤害+30%”移除；
  - 湮解原“命中得视野 / 落空省充”移除（改版的单线形态不再带这些特判）。

## 1. 通用契约：`EndModeWand`
`core/…/endcontent/evolved/EndModeWand.java`
- `int modeCount()`, `int modeIndex()`, `void setModeIndex(int)`, `String modeName(int)`。
- 两张进化法杖实现该接口。
- `WndUseItem` 在“使用该物品”窗口里，若 `item instanceof EndModeWand` 则追加一排
  “发射形态”按钮。
- 形态保存在法杖 item 自身（`storeInBundle/restoreFromBundle` 里多了 `end_mode` 字段），
  关窗换层后会保留。回到主菜单再进局仍记住你选的形态。

## 2. B4 棱辉法杖（PrismaticLight）
文件：`EvolvedWandOfPrismaticLight.java`

- `mode 0 = 普攻·直射`：耗 1 充。只命中瞄准落点一个敌对目标（单发、不穿透），
  附带照亮落点周围地形（父类 affectMap 地貌亮的窄版）+ 基础致盲/对亡灵·恶魔增伤。
- `mode 1 = 灵光光束`：耗 3 充。用 `ConeAOE` 以 **8° 名义发散角**（小角度≈2格宽的准直光束）
  打出**能穿透单位**（参数 `STOP_SOLID | IGNORE_SOFT_SOLID`，不因任何单位停止）的光带；
  对光带内每一个敌对单位独立 `affectTarget`，也把更宽的地点亮开。
- 瞄准阶段：`collisionProperties(int)` 若处于灵光形态，返回“只被实墙停/忽略软墙”，
  使玩家可把目标选在怪物身后/穿串上（正常形态则沿用父类魔弹式单点停止）。
- `chargesPerCast()` 与形态绑定：其值在尝试施放、动画、扣充上保持一致
  （不足 3 充却开着灵光形态时，本次会 fizzle，直到攒满 3 点）。
- **移除**的旧覆盖：原先 `min(int)/max(int) × 1.30` 的方法已删除，数值回归父类 DamageWand 规则。

## 3. B2 湮解法杖（Disintegration）
文件：`EvolvedWandOfDisintegration.java`

- `mode 0 = 湮解·单线`：耗 1 充。单条穿透柱，可轰开途经易燃地形；不再带旧 M2 视野/省充。
- `mode 1 = 湮解·分裂`：耗 2 充（=每发 +1）。以瞄准方向为核心，额外向对称 **±45°**
  各投一束，共 **3 束**。每条都沿自己弹道穿透命中。
- 伤害：分裂形态整体 **×1.2**；命中越多个单位，本发伤害等级随之提高（每只独立 roll，
  继承父类“越穿越痛”的基调）。
- “模式开关”采用与 B4 一致的手选（`EndModeWand`），在背包-法杖窗口里点选。
- **移除**的旧 M2 覆盖：不再继承父类的视野看破与 50% 落空省充逻辑。
  注意：正式湮解进化实际通过 `EndWandEvolution` 从原版 `WandOfDisintegration` 换装而来，
  因此它的碰撞/穿透仍按父类 `WONT_STOP`（可穿墙模型由扫描射程 `distance()` 截断）。

## 4. UI 接线
文件：`core/…/windows/WndUseItem.java`
- 新增仅当 `item instanceof EndModeWand` 时的形态选择行（放在法杖操作按钮之后）。
- 每次点击形态 → `setModeIndex` + 重开该窗口，让当前形态高亮（沿用 `TITLE_COLOR`）。

## 5. 数值可调点标注
- 棱光：`BEAM_DEGREES = 8f`（名义带宽）；耗充 1/3（在 `chargesPerCast`）。
- 湮解：`SIDE_DEG = 45f`；分裂耗充 在 `chargesPerCast`（2）；伤害倍率 `damageScale()` =1.2。
- 若要改，只动上方常量/方法即可，逻辑分离。
