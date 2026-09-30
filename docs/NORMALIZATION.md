# 数值规范化说明

目标：同一个数值只在一个地方定义，代码和文本都引用它，避免「改了代码忘了改说明」。

## 已经单源的系统

| 系统 | 单一来源 |
|---|---|
| 凝霜法杖「冰天雪地」 | `endcontent/evolved/FrostBalance.java` |
| 原神戒指词条 | `endcontent/challenge/RingAffix.java` |
| 词条强化石 | 读 `RingAffix` 的常量 |
| 寰宇支配之剑 | `UniverseSword` 内的命名常量 |
| 魔虚罗适应 | `endcontent/challenge/AdaptiveResistance.java` |
| 挑战数值（登神/复仇狂怒/68倍率/通用概率/金币商店/祷告/时间/不死等） | `endcontent/challenge/ChallengeBalance.java`（已迁 60 个常量） |
| 存档兼容 | `Bundle.getClassArray()` 跳过缺失类 |

这些系统的物品/技能说明由常量拼接，改常量即同时改文本。

## 新增内容的规则

1. 数值写在专门的常量类或该系统的类里，不散落在多个方法里。
2. 说明文本由常量拼接，不要手写死数字。
3. 删类 / 删枚举 / 改存档 key 必须留兼容处理。
4. 一次性脚本、对比报告放 `_tmp/`，不进正式代码。

## 已知历史债务

`ChallengeEffects.java` 里仍有约 **258 个** `static final` 数值常量（已从 318 迁出 60 个），分散在多个段落。
这是历史遗留，不做一次性大迁移（风险太高），只做增量收口：

- 新挑战的数值一律放独立类，不再往 `ChallengeEffects` 里加常量。
- 改动某个老段落时，顺手把该段落的常量提到对应系统类，并在说明文本里引用。
- 清单可随时用注册表/正则重新生成，放在 `_tmp/` 对比。

## 检查脚本

- `_tmp/check_alignment.py`：用户文本 vs 游戏文本对齐。
- `_tmp/check_challenge_text.py`：挑战 key 与 properties 文本覆盖检查。
- `_tmp/challenge_constants.txt`：`ChallengeEffects` 现有数值常量清单。
