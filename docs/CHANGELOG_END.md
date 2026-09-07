# 《破碎的像素地牢：终焉扩展》开发日志 (CHANGELOG)

> 工程：`E:\破碎的地牢\_EndShatteredBuild`（官方 SPD 3.3.8 fork；包名沿用 `com.shatteredpixel.shatteredpixeldungeon`）
> 目标：把 `../开发.txt` 的设计一点点做成真可玩的 MOD；每批在 GitHub Actions(`:desktop:compileJava` / `:android:assembleDebug`) 验证后推送。
> 用词：**[已提交]** = 已在本地 git 提交（可能尚未推上远端）；**[WIP]** = 进行中；**[规划]** = 设计/路线，未写码。

---

## 分批提交记录（新→旧，含 commit 短号）

### [新提交] — 灵能弓三选一锻造（原四选一去④奥术）+ 去自造占位料
- **配方（三选一铁匠炉）**：`endcontent/evolved/EvolveSpiritBowRecipe` + `SpiritBowCoreRecipe`(2 升级卷轴+50 液金→灵能核心) 接进 `Recipe.variableRecipes`；`Recipe.usableInRecipe` 放行非诅咒灵能弓。锅：1 把原版灵能弓 + 1 灵能核心 + 任一特殊料 → 对应成品弓：
  - ① 附魔灵弓 ← 消耗 `升级卷轴 ScrollOfUpgrade` → `EndSpiritBowMight`
  - ② 雷鸣灵弓 ← 消耗 `雷鸣魔药 ShockingBrew` → `EndSpiritBowStorm`
  - ③ 唤魔灵弓 ← 消耗 `唤魔晶柱 SummonElemental` → `EndSpiritBowSummon`
  - (④ 奥术灵弓已按本轮需求移除，含其 ArcaneResin 分支整支删除)
  - 已锻成品弓不可再入锅当基底；`sampleOutput`/`brew` 均按“投入的特殊料”产出正确成品，预览与实锻一致。
- **删除自造占位材料**：`endcontent/items/SpiritBowMaterial` + `MaterialOf{Might,Storm,Summon,Arcane}` 全移除（新配方全走本 fork 原装料），内容零残留引用。
- **① 附魔灵弓（每次攻击触发一个随机附魔）**：`EndSpiritBowMight` 命中时临时摘下身上静态附魔，每击用 `Weapon.Enchantment.random()` 在常见/稀见/**稀有**全池掷一把正面向附魔并当场执行一次——故每次攻击都必定触发【随机】附魔（可含 Grim/Vampiric/腐化 等稀有），而非触发弓上那把固定附魔；不修改 Weapon/主类，效果强度随本弓 buffedLvl 自然缩放。锻造时不再把源弓附魔强拷到这弓上。
- **② 雷鸣灵弓 / ③ 唤魔灵弓**：接入各成品弓自带机制（雷鸣：命中后经 `Shocking.arc` 向外链 1 格、对敌对各单位结算 20% 箭伤；唤魔：命中击杀后延迟一帧以 10% 掷召随机元素盟友、生命压至其自身最大生命 ~30%）——沿用本 fork `Weapon.proc` 于敌人扣血前触发,故击杀判定皆以延迟 Actor 方式实现。

### [已提交] `7795016` — 进化法杖机制调整：湮解/棱光/凝霜
- **湮解(湮解法杖)**：伤害倍率对调——普通·单线形态伤害 +20%（×1.2），分裂(三束)形态不额外提升（收益在多目标覆盖）；分裂形态充能消耗由 2 改为 **1**。
- **棱光(棱辉法杖)**：灵光光束不再用发散锥，改为**固定 3 格宽矩形光带**——沿主束路径逐格向垂直方向左右各扩 1 格（`BEAM_WIDTH=3`，不随距离变宽），光带内每个敌对单位独立结算致盲/增伤；`fx` 同步画 3 条平行射线示意宽度。
- **冰霜(凝霜法杖)**：接入 `EndModeWand` 双形态——
  - 形态0「冰霜直击」(默认，耗1)：原命中点直击 + 3×3 附加寒冷；
  - 形态1「冰雪区域」(耗3)：选中位置铺开 3×3 **持续冰雪区域**（`FIELD_TURNS=4` 回合）；
  - 新增 `endcontent/evolved/EndFrostField`(Blob 子类，仿原版 Blizzard/Fire 范式)：每回合对区域内敌人造成 **50% 面板伤害 + 全额 Chill**；对已冻结(冰封 `Frost`)的敌人**破除冻结**并造成 **150% 面板伤害**（每回合触发）；区域不扩散只随回合衰减；持久化伤害/时长，读档后伤害归因回退为区域自身。
- 三把法杖的形态选择/持久化走既有 `EndModeWand` 契约（背包-法杖窗口切换）。

### [已提交] `c62e047` — 进化法杖与宝石登记进游戏内图鉴/日志；合成弹配方日志
- `journal/Catalog`：13 把进化法杖注册进「法杖(WANDS)」图鉴组，`EndGemItem` 注册进「杂项消耗品(MISC_CONSUMABLES)」图鉴组——之前便利挑战里 `Catalog.setSeen` 对这些类无效(未注册)，现已真正生效。
- `items/EndGemItem`：宝石**天生已鉴定**(拾取即自动登记图鉴，走 `Item.collect→setSeen`)；首次拾取弹即时日志「图鉴新增：XX宝石」。
- `evolved/EvolveWandRecipe.brew`：炼成进化法杖时产物自动 `identify()` + `Catalog.setSeen` + 即时日志「炼成进化法杖：XX 已记入图鉴/日志」。

### [已提交] `4d84134` — fix(load): 兼容无 VERSION 键的存档(按当前版本处理)
- 避免 `ascend` 时把旧格式存档误判删除。

### [已提交] `dea3080` — fix(wnd): EvolvedWand 模式按钮需 add() 才可见
- 背包-法杖窗口的形态切换按钮此前只 setPos 未 attach 到列表，改为 `add()`，保证按钮实际渲染可点。

### [已提交] `d8a7b93` — desktop: 存档加载失败时打印可见日志
- `InterlevelScene/GamesInProgress` 加载失败新增桌面可见日志输出，便于定位坏档。

### [已提交] `389c6d5` — 进化法杖系统：13把专属机制 + 统一进化基础 + 灵炎/B2/B4模式改版
- **13 把进化法杖专属机制全部接通**：魔弹(弹数×2)、爆炎(灵炎不熄)、闪电(自电转盾)、冲击波(伤害+50%/撞墙眩晕×2/可调推距1-3-5)、腐蚀(+1回合缠绕)、腐化(触发+30%)、解离(命中视野/落空50%省充)、冰霜(3×3寒冷)、活体大地(泥沙+伤害同源+40%)、棱光、再生(取消次数限制)、注魂(护盾吸20%生命)、哨戒(消耗充能直接成高阶段哨兵)。
- **统一进化基础**：进化产物继承源法杖等级(至少+8，`EndWandEvolution.evolve`)，真实等级 `level()`/`buffedLvl()` 均按原等级；充能上限提升至 **20**（`updateLevel`：10 起步、每级 +1、上限20）。(此前曾用 buffedLvl 假偏移的方案已删)
- **灵炎**：新 buff `SpiritFire`(复制原版 `Burning`，仅去掉"站水里熄灭"判定)；爆炎进化用它替换普通燃烧。
- **B2/B4 模式改版**：湮解/棱光改为可手选双形态(见 `dea3080` UI 修复前的 `notes-B2-B4.md` 设计)；`EndModeWand` 接口 + `WndUseItem` 形态选择行。
- 同时修 desktop 运行空指针(version 恒非空、null-safe vendor/segment)。

### [已提交] `61eac4c` — fix(desktop): Game.version 恒非空
- `Game.version` 不可为 null（缺包描述时回落常量），避免 `isDebug`/`Document` 等判空崩溃。

### [已提交] `5ae9722` — fix(desktop): 未打包运行缺 Implementation-* 元数据时 null-safe
- vendor/version 段判空，桌面 IDE 直跑不再抛异常。

### [已提交] `6bc7ced` — 便利测试挑战 + 移除决斗家/牧师可选
- 新增第 10 个“挑战”项 `Challenges.CONVENIENCE`(=512)，
  - 同步 `MAX_VALUE`→1023、`MAX_CHALS`→10、`NAME_IDS`/`MASKS` 各追加 `convenience` / `CONVENIENCE`；
  - 新增中/英`challenges.convenience(+_desc)` 消息（`misc.properties` / `misc_zh.properties`）；
  - 挑战 UI（开局勾选)、赛后概览等因用共享数组自动多出一行，无需逐处改。
- `Dungeon.init()`：勾选该挑战时，每局开局给测试便利包＝金币+300 与 2 颗随机类型宝石(`EndGemItem`) 预 identify 入包。
- `HeroClass`：新增 `isDisabledForEnd()`；`DUELIST`、`CLERIC` 的 `isUnlocked()` 恒为 false ＝ 全局不可选(仍保枚举/存档兼容)。

### [已提交] `47ef49a` — M2: 全部 13 把法杖进化(成新物品) + 独特附魔光泽
- 在上一批 3 把基础上，把其余 10 把(`BlastWave/Corrosion/Corruption/Disintegration/Frost/LivingEarth/PrismaticLight/Regrowth/Transfusion/Warding`)量产成 `EvolvedWandXxx`（继承原法杖=新物品、独特中文名 + `glowing()`）。
- `endcontent/evolved/EndWandEvolution` 注册表含全部源→进化映射；`evolve()`＝构造进化类并保留`level`+`curCharges`。
- 每把深机制(魔弹×2、灵炎火焰不熄、雷髓自电转盾…)留 `TODO(endcontent/M2)` 钩子，待 CI 后逐个接入。

### [已提交] `44b6e98` — M2 脚手架 + 3 把样板进化法杖
- 首个进化框架 + `EvolvedWandOfMagicMissile/OfFireblast/OfLightning` 样板。

### [已提交] `09a75b6` — 商店卖宝石（与复活章同价）
- `ShopRoom.generateItems()`：每个主游戏商店(6/11/16/20层…)都刷新 1 颗类型随机宝石。
- `EndGemItem.value()=50×quantity`（与 Ankh 相同价值 → 两商店售价恒同价）。

### [已提交] `46f2e3e` — 免费 AI 交接包
- `docs/FREE_AI/handoff.md` + `review-gems-M1.md`：可用免费 AI 外包的只读审代码/翻译任务与返回格式。

### [已提交] `121eae9` — 宝石系统 M1 功能接入
- Hero 数值切面：命中/攻击(武器向)、减伤/闪避/生命(护甲向) 静态注入，见 `Hero.java`“`//END gem`”处；
- `EndGemItem`：对武器/护甲“镶嵌/使用”，禁止二嵌，消耗后刷新(chatbox)；
- (图标暂用 `STONE_AUGMENTATION` 占位；文案自造中文)。

### [已提交] `3afceda` — bugfix：CI 里 gradlew 无 x 权限
- GitHub Actions ubuntu：先 `chmod +x gradlew`，修 `./gradlew 许可被拒(126)`。

### [已提交] `b1d094b` / `a6bc480` — 宝石系统数据/持久化
- roadmap M1 分四步打勾；`Weapon.gem`/`Armor.gem`(int, EndGem序号,-1无) + `hasGem/gemType`，bundle `end_gem` 键、`contains` 守卫 restore；不随 reset 丢失。

### [已提交] `f03abc6` — 内容种子
- `endcontent.EndGem`(5 类词典)/`EndGemProfile`(线性成长模型 `value()`/`bonusAt`)；`docs/END_ROADMAP.md`。

### [已提交] `1ebe444` — 工程骨架
- 官方 SPD 3.3.8 fork：core / SPD-classes / services / desktop / android(双产物)；定名 `Shattered Pixel Dungeon: End`；
- `settings.gradle` 桌面化(剔 android 需 SDK 但保留 android 用于双产物)；desktop 移除 beryx 打包插件；
- 配 `.github/workflows/build.yml`(desktop distribution + Android debug APK artifacts)、`.gitignore/.gitattributes`；git init main。

---

## [已提交/进行中] 用户追加项
- `82022a6` docs: development changelog added above（本节为其工作状态追踪）。

### 储物木桶（已删除）
- 定位：“储物木桶”= 下水道层 `SewerLevel` 的 `Terrain.REGION_DECO/REGION_DECO_ALT` 桶状装饰(占一格、易燃、可被烧成水/空地)。
- 最终做法：在 `SewerLevel.buildFlagMaps()` 中、计算可走性之前把这些桶格重写为普通可走 `Terrain.EMPTY` —— 木桶彻底不再生成；`destroy()` 相应清空烧桶行为。
- 只动下水道的桶，**保留各区的雕像/塑像(那是 `Terrain.STATUE`，非本类)不受影响**。

### 便利功能（仅挑战开启）
1. 条目仍准确：本批还含 —— **开局把各类物品标记为 `Catalog.setSeen`(已见过)**(需枚举物品类型；待细化可见入口)。
2. **不消耗的“召选道具”**：使用不消耗，弹“任选一件物品获得”选择器。
3.（框架建议）Evolved 法杖专属机制 + 锻造/换装入口、余下挑战区域与无尽模式。

---

## 行为注意 / 回滚点
- 决斗家/牧师是“整体开不了”(`isUnlocked=false`)，不是删除枚举；要临时拿回只把那行去掉即可。
- 便利测试挑战若不想它计分，后续可考虑不纳入 `activeChallenges()`(见 roadmap)，目前它计入分数(+25%)与挑战数。
- 每次改动更新本日志并把对应的 commit 号补进来。
