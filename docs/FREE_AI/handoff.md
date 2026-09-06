# 给「免费 AI」的交接包 (Free-AI handoff)

> 用途：用克Claude免费额度 / Gemini 免费档 / DeepSeek网页 / Kimi 等免费 AI 做**低成本、可外包**的工作，
> 把结果交回本会话的主力 agent。主力只做你难以外包的（跨 ~1200 文件的架构、编译排错、跨系统缝合）。
> 用法：整包把这些文件发给免费 AI，要求它仅针对你给它的一个**具体子任务**输出。

## 一、工程一句话背景（给任何 AI 先读）
我们 fork 了开源游戏《Shattered Pixel Dungeon》(官方 3.3.8)做大型扩展 MOD
《破碎的像素地牢：终焉扩展》。《开发.txt》设计装备宝石 / 法杖蜕变 / 灵能弓 / 26F挑战 / 无尽。
本文件夹是"装载新代码 + 从两个国产 MOD(魔绫源码、方舟jar反编译)借鉴机"。
游戏包名沿旧 `com.shatteredpixel.shatteredpixeldungeon`；构建=Gradle(在 GitHub Actions 编译)；
代码目录：`core/src/main/java/...` （约1190 .java）；引擎基座在 `SPD-classes/`(com.watabou)。

## 二、只读信息来源（你基本只用这几个）
- 主设计：《破碎的地牢\开发.txt》
- 工程 S代码：`E:\破碎的地牢\_EndShatteredBuild\`(我们改的)
- 借鉴素材 A(魔绫·完整源码)：`E:\破碎的地牢\Magic_Ling_Pixel_Dungeon-stable\...` → 直接读 .java 即可
- 借鉴素材 B(方舟·class 需反解)：`E:\破碎的地牢\MOD素材提取\方舟\class\...` + `...\resources`,原创无法直接改
- 我方代码清单：查 `_EndShatteredBuild/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/endcontent`

## 三、常见"外包给免费AI"的任务(挑一个,别一次全做)
1) **翻译代码注释/数据到正版 message 资源**：把我方以中文字面/`Messages.get(this,"..")` 形式的内容
   整理成 SPD 标准的 `messages/items/....properties` 结构并生成键 (需要让我方类不再硬编码文案)。
2) **读某段大文件给我精炼**：要它读 Hero.java 某方法区间,把"对 gem 该在哪行插入 + 为什么"讲清。
3) **AI盲编译审查**：给它指定若干文件的原文 =要求当"静态编译器"逐字节审,专门找：缺 import、字段/方法签名不符、
   SPD 空引用风险、枚举/静态/private 边界错——不臆测,给"行号+建议行"。
4) **搬运方舟/魔绫某系统**：给它一个 class 的反解源码(方舟)或魔绫 .java,请它产出 一份"移植到官方SPD某版本 的最小差异清单"。

## 四、返回给我时的固定格式(重要)
如果你要用免费AI，请让它**只输出下面结构**,你再原样贴我:
```
[任务ID]
[结论/你要我做的动作]
[逐条证据: 文件 + 行号 + 原样片段]
[若需修改] 给出"建议替换的完整代码块"(用纯文本,别用 markdown 表格包裹长代码)
[你不确定的点] 
```
把它的输出完整贴进对话即可;我会核到你给得越完整,我越省周转。

## 五、优先抓手(哪些适合先外包,哪些别)
- 适合外包：改文案为资源properties、给某个新文件做盲编译审、把一段反编译类翻译成"移植 diff 清单"、生成 .gitattributes/… 子任务。
- 别外包(会错)：跨大多文件的架构决策、编译红日志真排查(需看真实 GitHub Actions 栈)、Java 与引擎原生依赖冲突。这些我(能读全部core/能排错)来做更省。
