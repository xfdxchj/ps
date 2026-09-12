# 六大天王 · Boss 设计工具箱

> 一个自包含的设计套件：**你负责创意和美术，代码骨架已经搭好**。
> 放在 `_EndShatteredBuild/docs/BOSS_DESIGN_KIT/`，随时可查。

---

## 📁 目录说明

```
BOSS_DESIGN_KIT/
├── README.md                    ← 本文件：总览与流程
├── 01-贴图规格.md                ← 精灵图怎么画（含示例图）
├── 02-代码模板.md                ← 可直接复制的 Java 模板
├── 03-六王配方.md                ← 六种 Boss 类型的实现要点
├── 04-关卡与注册.md              ← 场地 + 接入挑战区
├── 05-避坑清单.md                ← 我踩过的 9 个坑
├── 06-开发流程.md                ← 从 0 到能玩的完整步骤
├── templates/                   ← Java 模板文件（复制即用）
│   ├── BossTemplate.java
│   ├── BossSpriteTemplate.java
│   └── BossLevelTemplate.java
├── examples/
│   ├── sprites/                 ← 示例贴图（生成脚本产出）
│   │   ├── example_10frame.png  ← 标准 10 帧动画
│   │   ├── example_static.png   ← 单帧静态
│   │   └── example_32px.png     ← 32×32 大 Boss
│   └── java/                    ← 完整可跑的示例 Boss
│       ├── ExampleBoss.java
│       ├── ExampleBossSprite.java
│       └── ExampleBossLevel.java
└── tools/
    ├── gen_template_sprite.py   ← 生成占位贴图（先用这个开工）
    └── check_boss.py            ← 检查你的 Boss 是否合规
```

---

## 🚀 三步开工

### 第 1 步：生成占位贴图（立刻能跑）

```powershell
cd E:\破碎的地牢\_EndShatteredBuild\docs\BOSS_DESIGN_KIT\tools
python gen_template_sprite.py --name myboss --frames 10 --size 16
```

会在 `examples/sprites/` 生成 `myboss.png`（带网格线的占位图）。
**先用它跑通流程，之后再用自己的美术替换。**

### 第 2 步：复制模板 → 填设计

| 模板 | 目标位置 |
|---|---|
| `templates/BossTemplate.java` | `actors/mobs/bosses/sixkings/你的Boss.java` |
| `templates/BossSpriteTemplate.java` | `sprites/sixkings/你的BossSprite.java` |
| `templates/BossLevelTemplate.java` | `levels/boss/SixKingsLevel1.java` |

每个模板里都有 `// TODO:` 标记，**按注释填即可**。

### 第 3 步：编译 + 测试

```powershell
cd E:\破碎的地牢\_EndShatteredBuild
& .\tools\localcompile.ps1        # 必须输出 COMPILE OK
python ..\_tools\find_nofound.py  # 检查文案键是否齐全
```

---

## 🎭 六大天王设定（你的设计蓝图）

| # | 王 | 类型 | 定位 | 关键机制 |
|---|---|---|---|---|
| 1 | **法术王** | 远程法术 | 元素伤害 | 随机元素切换，AOE |
| 2 | **坦克王** | 高血高防 | 消耗战 | 减伤 + 反伤，推进慢 |
| 3 | **Debuff 王** | 削弱 | 控制 | 命中施加多种负面 |
| 4 | **远程王** | 远距离 | 风筝 | 远射 + 近身后撤 |
| 5 | **召唤王** | 召唤 | 数量压制 | 定期召小怪，本体脆 |
| 6 | **全能王** | 全能 | 关底 | 三阶段，血量触发变身 |

**建议顺序**（难度递增）：`法术 → 远程 → Debuff → 召唤 → 坦克 → 全能`

---

## ⚡ 核心概念速查

### Boss 的生命周期

```
build() 关卡生成
  ↓
createMobs() → new 你的Boss() → GameScene.add(boss)
  ↓
每回合 act()        ← 你的 AI 逻辑在这里
  ↓
被攻击 → defenseProc()   ← 减伤/反伤在这里
  ↓
攻击命中 → attackHook()  ← 附加效果在这里（注意：attack() 是 final）
  ↓
HP <= 0 → die()          ← 死亡处理（基类已自动 unseal）
```

### 关键数值（在 `{ }` 初始化块里设）

```java
HT = 300;                          // 生命上限
EXP = 30;                          // 击杀经验
baseMin = 15;  baseMax = 25;       // 伤害下限/上限
baseAcc = 30;                      // 命中
baseEva = 15;                      // 闪避
baseMinDef = 5; baseMaxDef = 12;   // 防御下限/上限
```

### 常用属性（`properties.add(...)`）

| 属性 | 效果 |
|---|---|
| `BOSS` | 免疫即死/恐惧，显示 Boss 血条 |
| `LARGE` | 占据 2×2，不可推动 |
| `IMMOVABLE` | 完全不可移动 |
| `STATIC` | 免疫眩晕/减速/魅惑等 |
| `UNDEAD` | 亡灵（受特定效果影响） |
| `DEMONIC` | 恶魔 |
| `INORGANIC` | 免疫流血/中毒 |
| `FIERY` / `ICY` / `ACIDIC` / `ELECTRIC` | 元素属性 |
| `DEMONIC` / `DRONE` / `SEA` / `INFECTED` | 方舟扩展属性 |

---

## 📖 继续阅读

- **画贴图** → `01-贴图规格.md`
- **写代码** → `02-代码模板.md`
- **六王差异** → `03-六王配方.md`
- **场地接入** → `04-关卡与注册.md`
- **别踩坑** → `05-避坑清单.md`
- **完整流程** → `06-开发流程.md`

---

## 🔗 相关源码位置

| 用途 | 路径 |
|---|---|
| Boss 基类 | `core/.../actors/Boss.java` |
| Mob 基类 | `core/.../actors/mobs/Mob.java` |
| 精灵基类 | `core/.../sprites/MobSprite.java` |
| 关卡基类 | `core/.../levels/Level.java` |
| 挑战区注册 | `core/.../endcontent/challenge/ChallengeArea.java` |
| 文案文件 | `core/.../assets/messages/actors/actors.properties` |
| **可参考的现成 Boss** | `core/.../actors/mobs/bosses/hollow/`（魔绫四柱等） |
| **可参考的现成 Boss** | `core/.../actors/mobs/bosses/hollow/Morphs.java` |
