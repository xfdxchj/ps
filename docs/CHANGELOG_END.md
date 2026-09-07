# 《破碎的像素地牢：终焉扩展》开发日志 (CHANGELOG)

> 工程：`E:\破碎的地牢\_EndShatteredBuild`（官方 SPD 3.3.8 fork；包名沿用 `com.shatteredpixel.shatteredpixeldungeon`）
> 目标：把 `../开发.txt` 的设计一点点做成真可玩的 MOD；每批在 GitHub Actions(`:desktop:compileJava` / `:android:assembleDebug`) 验证后推送。
> 用词：**[已提交]** = 已在本地 git 提交（可能尚未推上远端）；**[WIP]** = 进行中；**[规划]** = 设计/路线，未写码。

---

## 分批提交记录（新→旧，含 commit 短号）

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
