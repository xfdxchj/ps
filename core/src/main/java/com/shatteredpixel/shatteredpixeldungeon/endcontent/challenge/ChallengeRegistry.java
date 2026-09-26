/*
 * 破碎的地牢 (End fork) — 挑战规则框架
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * END(挑战框架): 挑战规则的**注册表**。
 *
 * <p>这里是规则元数据的唯一入口：{@link ChallengeMask#activeCount()}、
 * {@link ChallengeMask#passLevel()} 与 {@link ChallengeMask#toString()} 都从
 * {@link #ALL} 读取「等级 / 是否计入通过等级」等信息。
 *
 * <h3>当前登记范围</h3>
 * 权威清单共 108 个槽位（见 {@code docs/CHALLENGE_RULES_AUTHORITATIVE.md}），
 * 本注册表**只登记已实装**的规则：
 * <ul>
 *   <li>经典挑战 9 条（表 ID 109–117，对应 {@code Challenges} 里的原版常量）</li>
 *   <li>新正式挑战 2 条（表 ID 32 通货膨胀、45 炼金无望）</li>
 *   <li>测试用「便利测试包」1 条（非表内，位号 0，{@code countsForLevel=false}）</li>
 * </ul>
 * 其余 96 条待实装规则，在各自实装时再往 {@link #ALL} 里登记一行。
 *
 * <p>**位号 = 表 ID**（不是数组下标），表内编号有断层，见 {@link ChallengeMask} 说明。
 */
public final class ChallengeRegistry {

	/** 全部已登记规则（登记顺序 = 展示顺序）。不可修改。 */
	public static final List<ChallengeDef> ALL;

	/** 表 ID → 规则定义。 */
	private static final Map<Integer, ChallengeDef> BY_ID = new HashMap<>();

	static {
		List<ChallengeDef> all = new ArrayList<>();

		//==== 经典挑战（表 ID 109–117，均已实装）====
		//legacyBit = 原版 Challenges.* 常量，用于与 Dungeon.challenges(int) 互转。
		all.add(ChallengeDef.at(109, "缩餐节食", "no_food")
				.group("经典").tendency(ChallengeDef.TENDENCY_RESOURCE).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.NO_FOOD).build());
		all.add(ChallengeDef.at(110, "信念护体", "no_armor")
				.group("经典").tendency(ChallengeDef.TENDENCY_RESOURCE).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.NO_ARMOR).build());
		all.add(ChallengeDef.at(111, "恐药异症", "no_healing")
				.group("经典").tendency(ChallengeDef.TENDENCY_RESOURCE).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.NO_HEALING).build());
		all.add(ChallengeDef.at(112, "荒芜之地", "no_herbalism")
				.group("经典").tendency(ChallengeDef.TENDENCY_RESOURCE).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.NO_HERBALISM).build());
		all.add(ChallengeDef.at(113, "集群智能", "swarm_intelligence")
				.group("经典").tendency(ChallengeDef.TENDENCY_MONSTER).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.SWARM_INTELLIGENCE).build());
		all.add(ChallengeDef.at(114, "没入黑暗", "darkness")
				.group("经典").tendency(ChallengeDef.TENDENCY_RESOURCE).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.DARKNESS).build());
		all.add(ChallengeDef.at(115, "禁忌咒文", "no_scrolls")
				.group("经典").tendency(ChallengeDef.TENDENCY_RESOURCE).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.NO_SCROLLS).build());
		//END(说明): 116 精英强敌**保持原版行为**（部分怪物被替换为冠军怪物）。
		//精英类规则（14/34/64/75/4）会**额外**开启精英生成，
		//所以即使不勾 116，勾了 14 也能看到精英。
		all.add(ChallengeDef.at(116, "精英强敌", "champion_enemies")
				.group("经典").tendency(ChallengeDef.TENDENCY_MONSTER).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.CHAMPION_ENEMIES)
				.effect("地牢中部分怪物被替换为拥有特殊能力的冠军怪物。")
				.build());
		all.add(ChallengeDef.at(117, "绝命头目", "stronger_bosses")
				.group("经典").tendency(ChallengeDef.TENDENCY_MONSTER).level(3)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.STRONGER_BOSSES).build());

		//==== 新正式挑战（已实装）====
		all.add(ChallengeDef.at(45, "炼金无望", "costly_alchemy")
				.group("药剂").tendency(ChallengeDef.TENDENCY_RESOURCE).level(1)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.COSTLY_ALCHEMY).build());
		all.add(ChallengeDef.at(32, "通货膨胀", "inflation")
				.group("经济").tendency(ChallengeDef.TENDENCY_RESOURCE).level(2)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.INFLATION)
				.exclusive(37).build());
		//==== 测试项（非表内，位号 0；不计入通过等级）====
		all.add(ChallengeDef.at(0, "便利测试包", "convenience")
				.group("特殊").tendency(ChallengeDef.TENDENCY_NEUTRAL).level(1)
				.tier(ChallengeDef.TIER_EASY).state(ChallengeDef.STATE_DONE)
				.legacyBit(legacy.CONVENIENCE)
				.noLevelCount().build());

		//==== END(新增·第二批 14 条): 时间 / 怪物 / 药剂 ====
		//文档所有者口述定义，见 docs/CHALLENGE_OWNER_SPEC_2.md。
		//ID 用 188-201 —— 避开挑战区占用的 181-187（180 + 区内序号）。
		registerBatch2(all);

		//==== 待实装规则（权威清单 96 条，状态 STATE_PENDING）====
		//数据来源：docs/CHALLENGE_RULES_AUTHORITATIVE.md 的「三、权威规则全表」。
		//登记顺序 = 展示顺序（按分组：地图/战斗/怪物/经济/药剂/环境/装备/特殊/格林）。
		//这些条目在 UI 中置灰显示（见 WndChallenges），勾选无效，直到各自实装。
		//==== END(新增·第三批): 202+ ====
		registerBatch3(all);

		registerPending(all);

		//==== END(改造·挑战区并入挑战列表): 6 个挑战区 ====
		//原先 6 个区有独立的选单窗口（WndChallengeAreas）与独立的存档键
		//（SPDSettings.challengeAreas()）。文档所有者要求把它们**并入挑战规则列表**，
		//但勾选后仍然走原本的挑战区流程（通关 25F 后进入 26F+）。
		//
		//ID 用 ChallengeArea.CHAL_ID_BASE + 区内序号（201/203/204/205/206/207）——
		//不能直接用区内序号，因为 1 和 2 已被「牢地碎破」「楼层混乱」占用。
		//
		//**单选**：与原设计一致（同时勾多个区会串接层号、剧情分支冲突）。
		//互斥关系用 x: 声明，由注册表统一处理。
		//==== END(移植·英烈地牢): 第一批 ====
		done(all, 218, "登神长阶", "ascension", "英烈", ChallengeDef.TENDENCY_MONSTER, 3, ChallengeDef.TIER_MEDIUM, "",
			"怪物死亡时 **13%** 概率原地复活（最多 **6 次**）：每次复活生命上限翻倍、回满生命并净化负面状态。");

		done(all, 219, "惊喜礼物", "mimics", "英烈", ChallengeDef.TENDENCY_MONSTER, 2, ChallengeDef.TIER_EASY, "",
			"地牢里**所有普通宝箱都变成宝箱怪**（击杀后掉落原本该在箱里的东西）。");

		done(all, 220, "同仇敌忾", "revenge", "英烈", ChallengeDef.TENDENCY_MONSTER, 1, ChallengeDef.TIER_MEDIUM, "",
			"怪物受到**致死伤害**时，把**过量伤害**转嫁给它视野内的其它怪物。");
		done(all, 221, "复仇狂怒", "revenge_fury", "英烈", ChallengeDef.TENDENCY_MONSTER, 2, ChallengeDef.TIER_MEDIUM, "s:220",
			"同伴在它眼前死亡时，怪物进入**复仇狂怒**：造成的伤害**翻倍**、受到的伤害 **+20%×层**（可叠加，Boss 免疫）。");

		done(all, 222, "集体荣誉", "stacking_champions", "英烈", ChallengeDef.TENDENCY_MONSTER, 2, ChallengeDef.TIER_MEDIUM, "",
			"怪物会**以堆的形式**出现；同堆的怪物**共享精英词条**，并且可以多带一个词条。");
		done(all, 223, "堆积威胁", "stacking", "英烈", ChallengeDef.TENDENCY_MONSTER, 1, ChallengeDef.TIER_MEDIUM, "s:222,224",
			"怪物以**堆积**方式生成（同格 2~3 只，发现目标后散开）；怪物数量 **+50%**。");
		done(all, 224, "成群结队", "stacking_spawn", "英烈", ChallengeDef.TENDENCY_MONSTER, 1, ChallengeDef.TIER_EASY, "s:223",
			"重新生成的怪物**至少 2 只成堆**出现。");
		done(all, 225, "全副武装", "elite_champions", "英烈", ChallengeDef.TENDENCY_MONSTER, 3, ChallengeDef.TIER_MEDIUM, "s:116",
			"精英出现率提升；精英有 **1/3** 概率成为**高阶精英**（多带一个词条 + 1 个跟班）。");
		done(all, 226, "现代战争", "dungeon_of_champions", "英烈", ChallengeDef.TENDENCY_MONSTER, 3, ChallengeDef.TIER_HARD, "s:116,225",
			"精英出现率进一步提高；精英**必定多带一个词条**；高阶精英带 **2 个跟班**。");
		done(all, 227, "星界军团", "legion", "英烈", ChallengeDef.TENDENCY_MONSTER, 3, ChallengeDef.TIER_MEDIUM, "",
			"每隔一段时间生成**一大波敌人**；每生成一波，**楼层封锁 20 回合**（仍可信仰之跃）。");

		done(all, 228, "我的世界", "jingmi", "音乐", ChallengeDef.TENDENCY_NEUTRAL, 2, ChallengeDef.TIER_SERIES, "",
			"**静谧花园**：把**地牢美术**（地形/怪物/特效）与 **BGM/音效** 换成静谧花园 4.0 的版本。界面、字形与标题画**不替换**。纯外观，不影响数值。");

		//==== END(ReReARPD 枪械移植): 枪械工坊 ====
		done(all, 229, "枪械工坊", "gunsmith", "装备", ChallengeDef.TENDENCY_NEUTRAL, 2, ChallengeDef.TIER_MEDIUM, "",
			"武器掉落池中加入 **10 系 27 把枪械**；开局获得一把随机 **T1 枪** 与 **枪械改造工具**。\n枪械打空弹匣后需花回合装填（装填时间已减半）；射击伤害吃**投掷加成**（力量/神射之戒），不消耗子弹。\n炼金进阶：**强化符石+20液态金属**→升级之尘；**嬗变卷轴+不稳定法术**→进化法术；**T5枪+升级之尘+进化法术**→枪械蓝图，对对应 T5 枪使用 → T6 战术型（+0 时 80%）。");

		registerChallengeAreas(all);

		ALL = Collections.unmodifiableList(all);

		for (ChallengeDef def : ALL) {
			BY_ID.put(def.id, def);
		}
	}

	//==== 登记的紧凑写法辅助（倾向 / 档位简写）====

	/** 旧掩码常量表（{@code Challenges.*}）。用别名缩短登记行的长度。 */
	private static final class legacy {
		static final int NO_FOOD            = com.shatteredpixel.shatteredpixeldungeon.Challenges.NO_FOOD;
		static final int NO_ARMOR           = com.shatteredpixel.shatteredpixeldungeon.Challenges.NO_ARMOR;
		static final int NO_HEALING         = com.shatteredpixel.shatteredpixeldungeon.Challenges.NO_HEALING;
		static final int NO_HERBALISM       = com.shatteredpixel.shatteredpixeldungeon.Challenges.NO_HERBALISM;
		static final int SWARM_INTELLIGENCE = com.shatteredpixel.shatteredpixeldungeon.Challenges.SWARM_INTELLIGENCE;
		static final int DARKNESS           = com.shatteredpixel.shatteredpixeldungeon.Challenges.DARKNESS;
		static final int NO_SCROLLS         = com.shatteredpixel.shatteredpixeldungeon.Challenges.NO_SCROLLS;
		static final int CHAMPION_ENEMIES   = com.shatteredpixel.shatteredpixeldungeon.Challenges.CHAMPION_ENEMIES;
		static final int STRONGER_BOSSES    = com.shatteredpixel.shatteredpixeldungeon.Challenges.STRONGER_BOSSES;
		static final int CONVENIENCE        = com.shatteredpixel.shatteredpixeldungeon.Challenges.CONVENIENCE;
		static final int COSTLY_ALCHEMY     = com.shatteredpixel.shatteredpixeldungeon.Challenges.COSTLY_ALCHEMY;
		static final int INFLATION          = com.shatteredpixel.shatteredpixeldungeon.Challenges.INFLATION;
	}

	private static final int T_MON  = ChallengeDef.TENDENCY_MONSTER;
	private static final int T_RES  = ChallengeDef.TENDENCY_RESOURCE;
	private static final int T_TWO  = ChallengeDef.TENDENCY_TWOSIDED;
	private static final int T_BEN  = ChallengeDef.TENDENCY_BENEFIT;
	private static final int T_RISK = ChallengeDef.TENDENCY_RISK;
	private static final int T_NEU  = ChallengeDef.TENDENCY_NEUTRAL;

	private static final int T_EASY = ChallengeDef.TIER_EASY;
	private static final int T_MED  = ChallengeDef.TIER_MEDIUM;
	private static final int T_HARD = ChallengeDef.TIER_HARD;
	private static final int T_SER  = ChallengeDef.TIER_SERIES;

	/**
	 * END(挑战框架): 批量登记待实装规则。
	 *
	 * <p>每条用一行紧凑数据描述：{@code id, 中文名, key, 分组, 倾向, 等级, 档, 关系}。
	 * 关系串前缀：{@code x}=互斥 {@code s}=联动 {@code r}=限制 {@code p}=前置，
	 * 冒号后是逗号分隔的 ID；多条关系用 {@code ;} 分隔。
	 */
	private static void registerPending(List<ChallengeDef> all) {

		//---- 地图变化 ----
		//END(修正): 原关系串 "s:6;2,3,4,5" 里第二段缺前缀（会被当成未知类型解析）。
		//1 与 2/3/4/5 同属"地图变化"系列，统一标为联动。
		done(all, 1,  "牢地碎破",   "shattered_land",   "地图", T_MON, 3, T_HARD,   "s:6,2,3,4,5",
				"区域倒置：1 区刷 5 区的怪、2 区刷 4 区、4 区刷 2 区、5 区刷 1 区；所有怪物数值按专用配置表重配。");
		done(all, 2,  "楼层混乱",   "floor_shuffle",    "地图", T_TWO, 2, T_HARD,   "s:3,4,5",
				"普通楼层随机重排：每个区域内的 4 个普通层打乱顺序，Boss 层位置不变（第 5/10/15/20/25 步仍是 Boss）。楼层编号与游玩顺序分离。");
		done(all, 3,  "区域错位",   "region_shift",     "地图", T_TWO, 2, T_HARD,   "s:2,4,5",
				"地图生态向相邻区域偏移：每个区域的生成内容会混入邻区的风格。");
		done(all, 4,  "精英迁徙",   "elite_migration",  "地图", T_MON, 2, T_HARD,   "s:14,75",
				"取消精英怪的楼层限制：原本只在自己区域出现的精英怪，现在任何楼层都可能出现。");
		done(all, 5,  "怪物入侵",   "monster_invasion", "地图", T_MON, 1, T_HARD,   "s:2,3,4",
				"每层额外混入 2 只其它区域的普通怪。");
		done(all, 6,  "完整地牢",   "full_dungeon",     "地图", T_TWO, 2, T_HARD,   "s:1",
				"每个区域的常规层由 4 层增至 9 层，Boss 层不变 —— 主线从 25 层加长到 50 层。");
		done(all, 7,  "跳级生",     "skip_student",     "地图", T_BEN, 1, T_EASY,   "",
				"开局获得一张跳级券，使用后直接前往下一区域的第一层，并获得 +2 力量、+3 升级卷轴、+4 力量药水。");

		//---- 战斗 ----
		done(all, 8,  "混乱",       "chaos",            "战斗", T_TWO, 1, T_MED,    "",
				"战斗命中时 25% 概率给被打的一方挂一个随机 buff（加速/虚弱/寒冷/隐身/致盲/祝福）。");
		//==== 易档第一批（已实装）====
		done(all, 9,  "狂暴",       "berserk",          "战斗", T_MON, 2, T_EASY,   "",
				"怪物受击后获得 20% 攻击提升，持续 2 回合，不叠加。");
		done(all, 10, "巨型化",     "giant",            "战斗", T_MON, 2, T_EASY,   "",
				"13% 的怪物生命值提高 50%，体型增大（不影响寻路）。");
		done(all, 11, "脆弱",       "fragile",          "战斗", T_TWO, 2, T_EASY,   "",
				"玩家与怪物受到的伤害提高 13%。");
		done(all, 12, "玻璃大炮",   "glass_cannon",     "战斗", T_TWO, 2, T_EASY,   "",
				"玩家攻击提高 20%，生命上限降低 13%。");
		done(all, 13, "狂热",       "frenzy",           "战斗", T_MON, 2, T_EASY,   "",
				"怪物每次成功攻击后攻速 +13%，最多叠加 3 层（上限 +39%），持续 5 回合。");
		done(all, 14, "精英强化",   "elite_boost",      "战斗", T_MON, 2, T_EASY,   "s:4,75",
				"精英怪的生命上限、伤害、命中、闪避各提高 20%。本项会让精英怪出现，无需其它挑战配合。");
		done(all, 15, "首领护卫",   "boss_guard",       "战斗", T_MON, 3, T_MED,    "",
				"每个 Boss 战额外生成 3 个精英护卫（生命/伤害/命中/闪避各 +20%）。");
		done(all, 16, "大力水手",   "popeye",           "战斗", T_TWO, 1, T_EASY,   "",
				"玩家近战物理攻击 +25%，攻击速度 -20%。");
		done(all, 17, "情人节",     "valentine",        "战斗", T_BEN, 1, T_EASY,   "",
				"玩家攻击命中后，13% 概率魅惑目标。");
		done(all, 18, "老龄化",     "aging",            "战斗", T_BEN, 3, T_EASY,   "",
				"普通怪物每回合 13% 概率睡眠 1 回合（Boss 与精英怪免疫）。");
		done(all, 19, "风驰电掣",   "swift",            "战斗", T_TWO, 1, T_EASY,   "s:103",
				"玩家攻速 +20%，怪物移速 +20%。");
		done(all, 20, "等我启动",   "wind_up",          "战斗", T_TWO, 2, T_MED,    "",
				"对**同一目标**的伤害递增：第一次 20%，第二次 50%，第三次及以后 110%。换目标就重新计算。");
		done(all, 21, "法术连击",   "spell_combo",      "战斗", T_BEN, 2, T_MED,    "",
				"施法后 13% 概率**立即再施放一次**，不消耗充能与回合；单次最多追加一次。");
		done(all, 22, "物极必反",   "overkill_reverse", "战斗", T_TWO, 3, T_EASY,   "",
				"单次伤害超过目标最大生命 150% 时，该次伤害被完全免疫（只对怪物生效）。");
		done(all, 23, "血流成河",   "bloodbath",        "战斗", T_TWO, 1, T_EASY,   "",
				"任何攻击命中后 13% 概率使目标流血。");
		done(all, 24, "以牙还牙",   "retaliation",      "战斗", T_MON, 1, T_EASY,   "",
				"玩家攻击后，怪物 13% 概率立即反击（每回合每怪最多一次，不消耗怪物回合）。");
		done(all, 25, "越战越勇",   "growing_fury",     "战斗", T_MON, 2, T_EASY,   "",
				"怪物每损失 20% 生命，攻击力提高 10%，最多叠加 4 层（上限 +40%）。");
		done(all, 26, "破釜沉舟",   "last_stand",       "战斗", T_BEN, 2, T_EASY,   "",
				"玩家生命低于 30% 时，攻击提高 30%、攻速提高 20%。");
		done(all, 27, "极致攻哈",   "all_out",          "战斗", T_TWO, 1, T_EASY,   "",
				"玩家与怪物的攻击各提高 20%。");
		done(all, 28, "不动如山",   "immovable",        "战斗", T_MON, 2, T_EASY,   "",
				"怪物受击时 13% 概率完全免疫该次伤害。");
		done(all, 68, "极端状态",   "extreme_state",    "战斗", T_TWO, 3, T_MED,    "s:104",
				"开局生命上限降到 **10%**（最低 10 点），但**攻击力与命中翻倍**。");
		done(all, 69, "九九归一",   "nine_to_one",      "战斗", T_TWO, 2, T_EASY,   "",
				"最终伤害为 9 的倍数时，改为 1 点。");
		done(all, 78, "烈火焚身",   "immolation",       "战斗", T_MON, 3, T_EASY,   "",
				"玩家受击时 13% 概率燃烧。");
		//（103 弹幕地狱已删除 —— 散射效果始终不可靠，文档所有者决定移除。）
		done(all, 121,"中世纪骑士", "medieval_knight",  "战斗", T_TWO, 2, T_EASY,   "x:110",
				"玩家护甲值 +60%，移动速度 -50%。");
		done(all, 135,"亡者之怒",   "wrath_of_dead",    "战斗", T_RISK,2, T_EASY,   "s:68,124,128",
				"玩家生命低于 10% 时，造成的伤害翻倍。");

		//---- 怪物 ----
		done(all, 29, "雇佣童工",   "child_labor",      "怪物", T_TWO, 2, T_MED,    "",
				"13% 的怪物生命降到 20%，但移速 ×2。");
		done(all, 30, "人口密集",   "crowded",          "怪物", T_MON, 2, T_EASY,   "s:75",
				"普通怪物生成数量提高 20%（与 119 怪物浪潮可叠加）。");
		done(all, 73, "神秘复苏",   "mystic_revival",   "怪物", T_MON, 2, T_MED,    "s:77,86",
				"13% 的怪物一出生就是**幽灵**（1 血、飞行、无经验）。");
		//END: 原联动串为 "s:14,30,97"，其中 97 已删除，故移除该引用（保留 14/30）。
		done(all, 75, "精英地牢",   "elite_dungeon",    "怪物", T_MON, 3, T_MED,    "s:14,30",
				"13% 的怪物被替换为其稀有变种（白化老鼠、寄居蟹、强盗等）。");
		done(all, 76, "原始状态",   "primal_state",     "怪物", T_MON, 2, T_MED,    "s:103",
				"非远程怪物在打不到你的时候会**扔石头**（最多 6 格）。");
		done(all, 77, "亡灵法师",   "necromancer",      "怪物", T_MON, 3, T_MED,    "s:73,86",
				"怪物死亡后 **20%** 在原地留下一个幽灵。");
		done(all, 86, "复仇之魂",   "vengeful_spirit",  "怪物", T_MON, 2, T_MED,    "s:73,77",
				"被击杀的怪物有 **10%** 概率在**下一层**以幽灵形式复仇（每层最多 5 只）。");
		done(all, 87, "盗贼鼠群",   "thief_rats",       "怪物", T_TWO, 1, T_EASY,   "",
				"怪物攻击命中时 5% 概率偷走 5% 金币；击杀该怪物后双倍返还。");
		//END(已取消): 97 我的世界 / 122 我的世界II —— 按文档所有者要求删除，不做。
		//（原效果与"经验药水"相关。）122 对 97 的联动引用一并移除。
		done(all, 100,"镜像对决",   "mirror_duel",      "怪物", T_MON, 3, T_HARD,   "",
				"每层 **13%** 概率生成一只**敌对镜像**：外观/攻击/生命跟随玩家，**只能普通攻击**（没有背包，用不了道具与法杖）。击杀后掉落一件**同等级的随机装备**。");
		done(all, 119,"怪物浪潮",   "monster_wave",     "怪物", T_TWO, 2, T_EASY,   "s:30",
				"怪物生成数量 ×4；**怪物本身的生命与伤害不变**（Boss 不受影响）。");

		//---- 经济 ----
		done(all, 33, "黑市",       "black_market",     "经济", T_TWO, 1, T_MED,    "",
				"每家商店额外上架 **1-2 件特殊商品**（神器 / 法杖 / 稀有符石，以及本 MOD 的专属道具）。");
		done(all, 34, "赏金制度",   "bounty",           "经济", T_BEN, 1, T_EASY,   "",
				"击杀精英怪额外获得 30 金币，击杀 Boss 额外获得 120 金币。");
		done(all, 35, "丰饶",       "abundance",        "经济", T_BEN, 1, T_EASY,   "x:36",
				"资源及物品掉落增加 25%。");
		done(all, 36, "贫瘠",       "barren",           "经济", T_RES, 2, T_EASY,   "x:35",
				"资源及物品掉落减少 25%（不会降到 0，保证通关所需的最少资源）。");
		done(all, 37, "高价回收",   "high_buyback",     "经济", T_BEN, 1, T_EASY,   "x:32",
				"把物品卖给商店所得 ×1.5。");
		done(all, 38, "盲盒",       "loot_box",         "经济", T_TWO, 1, T_MED,    "",
				"每家商店上架 **2 个盲盒**。打开后：60% 普通消耗品 / 30% 装备 / 10% 稀罕物。");
		done(all, 39, "All or Nothing","all_or_nothing", "经济", T_RISK,3, T_MED,   "",
				"开局获得**赌徒之骰**：使用后选择一件可堆叠物品赌博 —— 50% 数量翻倍，50% 数量清零。");
		done(all, 40, "贷款",       "loan",             "经济", T_TWO, 2, T_EASY,   "",
				"可在商店贷款金币（100/300/500/1000 自选），1000 回合内偿还本金的 110%。同一时间只能欠一笔。");
		done(all, 41, "钱是万能",   "money_is_power",   "经济", T_BEN, 2, T_MED,    "",
				"开局获得**万能钱袋**：用金币直接买任何物品（药水/卷轴/装备/神器/力量药水/升级卷轴）。与 160 不重叠 —— 160 是升级，41 是购买。");
		done(all, 42, "等价交换",   "equivalent_exchange","经济",T_TWO,1, T_MED,    "",
				"开局获得 **3 张交换契约**：选择一件物品，随机换成**同类别**的另一件，等级保留。");
		done(all, 43, "一贫如洗",   "destitute",        "经济", T_RES, 2, T_EASY,   "",
				"每次进入新区域（每 5 层）时，金币减少 20%。");
		done(all, 44, "慷慨商人",   "generous_merchant","经济", T_TWO, 1, T_EASY,   "s:32",
				"商店商品数量 +30%，商店售价 ×1.25（与通货膨胀可共存）。");
		//END(已取消): 101 全员恶人 —— 按文档所有者要求**彻底删除**，不做。
		//原效果是"摧毁所有商店"，会连带让十余条商店规则失效，实现与维护成本都不划算。
		//同时已解除 88 拍卖行对它的互斥引用（此处原为 "x:101"）。
		done(all, 88, "拍卖行",     "auction_house",    "经济", T_TWO, 2, T_MED,    "",
				"商店价格**波动 0.5~1.8 倍**（每件商品首次看到时定格）；每层 **35%** 概率被 NPC 全场抬价 **×1.5**。");

		//---- 药剂 ----
		done(all, 46, "药剂不稳定", "unstable_potions", "药剂", T_TWO, 2, T_EASY,   "",
				"饮用药水后 13% 概率追加一个随机效果（正负各半）。");
		done(all, 47, "稀缺补给",   "scarce_supplies",  "药剂", T_RES, 2, T_EASY,   "x:48",
				"消耗品（药水、卷轴、食物、炸弹）生成数量减少 40%。");
		done(all, 48, "过量补给",   "excess_supplies",  "药剂", T_BEN, 1, T_EASY,   "x:47",
				"消耗品（药水、卷轴、食物、炸弹）生成数量增加 50%。");
		done(all, 124,"野生狗奶",   "wild_milk",        "药剂", T_RISK,3, T_MED,    "",
				"使用后**全属性降低 75%**（攻击/命中/闪避只剩四分之一），但期间**不会死亡**（生命最低保留 1）。**永久生效**。");

		//---- 环境 ----
		done(all, 49, "切尔诺贝利", "chernobyl",        "环境", T_TWO, 3, T_MED,    "s:111",
				"全图铺满毒气，玩家受影响、怪物免疫。开局与每层额外获得净化药水。");
		done(all, 52, "陷阱泛滥",   "trap_overflow",    "环境", T_MON, 2, T_EASY,   "",
				"地图陷阱数量翻倍。");
		done(all, 54, "我爱花花",   "flower_lover",     "环境", T_NEU, 1, T_EASY,   "",
				"13% 的草地被替换为随机植物。纯趣味，不影响数值。");
		done(all, 74, "热带雨林",   "rainforest",       "环境", T_MON, 2, T_MED,    "",
				"水中有 13% 概率生成食人鱼。");
		done(all, 80, "冰天雪地",   "frozen_world",     "环境", T_TWO, 2, T_EASY,   "",
				"玩家每回合 **1%** 概率寒冷、**1%** 概率冰冻（原表为 13%/2%，实测过高已下调）。");
		done(all, 90, "雷暴",       "thunderstorm",     "环境", T_TWO, 2, T_EASY,   "",
				"每回合 5% 概率闪电随机劈中一个角色，伤害 = 3 × 层数 ÷ 5，并可能点燃。");

		//---- 装备 ----
		done(all, 55, "不稳定强化", "unstable_upgrade", "装备", T_TWO, 2, T_EASY,   "s:108",
				"使用升级卷轴强化时，13% 概率额外 +2 级。");
		done(all, 56, "装备绑定",   "equip_binding",    "装备", T_RES, 2, T_MED,    "",
				"获得的装备自动绑定，无法丢弃、投掷或出售；使用驱邪卷轴可以解绑。");
		done(all, 57, "残缺装备",   "broken_equipment", "装备", T_RES, 2, T_EASY,   "r:108",
				"随机附魔时有 13% 概率获得「残缺」词缀：攻击力降低 20%，且不能进行装备觉醒。");
		done(all, 58, "随机附魔",   "random_enchant",   "装备", T_BEN, 1, T_EASY,   "s:108",
				"装备获得时 50% 概率带随机附魔词缀（与装备觉醒独立计算）。");
		done(all, 59, "诅咒装备",   "cursed_equipment", "装备", T_RES, 2, T_EASY,   "r:108",
				"装备获得诅咒的概率提高 13%（原本 30%，提高后 43%）。");
		done(all, 60, "家传法杖",   "heirloom_wand",    "装备", T_BEN, 1, T_EASY,   "",
				"开局额外获得 13 种进阶法杖中随机的一支（已鉴定）。");
		done(all, 79, "高级附魔台", "advanced_enchant", "装备", T_BEN, 1, T_EASY,   "",
				"每进入一个新区域（每 5 层）获得 1 个附魔秘卷。");
		done(all, 81, "搏杀赌徒",   "gambler",          "装备", T_TWO, 2, T_MED,    "x:126",
				"开局获得 +3 财富戒指；财富戒指有 8% 概率产出升级卷轴；常规的升级卷轴投放被完全取消。");
		done(all, 108,"装备觉醒",   "awakening",        "装备", T_BEN, 1, T_MED,    "r:57,59",
				"武器击杀 50 只怪、或护甲格挡 100 次后觉醒，获得一条随机附魔词缀（每件一次；残缺/诅咒装备不能觉醒）。");

		//---- 特殊 / 娱乐 ----
		done(all, 61, "炸弹狂魔",   "bomb_fanatic",     "特殊", T_BEN, 1, T_EASY,   "",
				"每层额外掉落 1 个炸弹。");
		done(all, 62, "芙莉莲",     "frieren",          "特殊", T_BEN, 1, T_EASY,   "",
				"宝箱数量提高约 20%。");
		done(all, 63, "鼠鼠可爱",   "cute_rats",        "特殊", T_NEU, 1, T_HARD,   "",
				"所有怪物的**贴图、名字与攻击按钮图标**都变成小鼠（Boss 除外）。纯外观，属性与 AI 不变。");
		done(all, 64, "宝物猎人",   "treasure_hunter",  "特殊", T_TWO, 1, T_EASY,   "",
				"普通怪物掉落减少 30%，每个宝箱额外增加 1 件物品。");
		done(all, 65, "及时雨",     "timely_rain",      "特殊", T_BEN, 2, T_EASY,   "s:104",
				"整局第一次致命伤害不会死，保留 1 点生命（触发顺序在命悬一线之前）。");
		done(all, 67, "宝箱危机",   "mimic_threat",     "特殊", T_RISK,3, T_HARD,   "",
				"每层 **20%** 概率生成一只**宝箱怪**（普通 / 黑檀 / 黄金），藏在普通堆或门口。");
		done(all, 70, "生活部长",   "life_minister",    "音乐", T_NEU, 1, T_MED,    "",
				"每回合 3% 概率停止行动，说出「首先，我是生活部部长」。");
		done(all, 71, "喝大了",     "drunk",            "特殊", T_TWO, 2, T_EASY,   "",
				"每回合 3% 概率触发眩晕 3 回合。");
		done(all, 72, "前程似锦",   "bright_future",    "音乐", T_NEU, 1, T_MED,    "",
				"每回合 3% 概率停止行动，说出「王同学，我祝你前～程～似锦」。");
		done(all, 95, "耗子尾汁",   "rat_tail_soup",    "音乐", T_BEN, 1, T_MED,    "",
				"每回合 3% 概率显示「耗子尾汁」，并播放随机音效。");
		done(all, 96, "奥利给",     "oligei",           "音乐", T_TWO, 1, T_MED,    "",
				"每回合 **1%** 概率停止行动，喊「奥利给」，获得 1 回合狂暴。");
		done(all, 104,"命悬一线",   "close_call",       "特殊", T_TWO, 2, T_MED,    "s:65,68",
				"生命不显示数值，只显示状态描述；致命伤害时 13% 概率保留 1 点生命。");
		done(all, 118,"天意侵蚀",   "providence",       "音乐", T_TWO, 1, T_MED,    "",
				"每回合 13% 概率随机播放一段新三国音效。");
		done(all, 120,"404",        "error_404",        "特殊", T_RISK,2, T_MED,    "",
				"**每回合 0.5%** 概率被直接送回主界面（期望约 200 回合一次）。");
		done(all, 123,"大学生",     "college_student",  "特殊", T_TWO, 1, T_EASY,   "",
				"玩家每回合 3% 概率受到 1 点伤害（不会致死，生命值至少保留 1）。");
		//END(分类调整): 134 黄金蜂蜜酒归入**格林系列**（原在「特殊」组）。
		//它是格林内容的一部分：与 128 镇魂歌同源（都是"以生命换力量"的格林玩法），
		//且被 135 亡者之怒 联动引用。归入格林组后，UI 上与其它格林规则一起展示。
		done(all, 137,"奶龙大笑",   "milk_dragon",      "音乐", T_NEU, 1, T_MED,    "",
				"每回合 3% 概率触发奶龙大笑音效。");
		done(all, 138,"荒诞世界",   "absurd_world",     "特殊", T_NEU, 1, T_EASY,   "",
				"怪物贴图随机变化。纯外观，不影响属性与 AI。");

		//---- 格林系列（125–133、136）· 链式前置 ----
		done(all, 125,"格林之器",   "grimm_weapon",     "格林", T_BEN, 3, T_SER,    "s:126,127,128,129,130,131,132",
				"按 5 阶武器在关卡中**掉落**获得**勇剑**：三段循环（3 连击 / 必中 / 4 连击），从第 21 层起有概率出现。");
		//END(互斥): 126 移除升级系统，而 81 依赖升级卷轴产出 —— 二者功能冲突，必须互斥。
		done(all, 126,"格林之心",   "grimm_heart",      "格林", T_RISK,3, T_SER,    "x:81;s:125,127,128,129,130,131,132",
				"**关闭等级系统**，改为黑之魂：杀怪攒魂、死亡得魂并用魂献祭属性；每次死亡退回上一层而非结束游戏。");
		done(all, 127,"格林之戒",   "grimm_ring",       "格林", T_BEN, 2, T_SER,    "s:125,126,128,129,130,131,132",
				"增加戒指**黑兔戒指**：每回合第一次命中后返还一个回合（一回合可攻击两次）。无法升级。");
		done(all, 128,"格林之术",   "grimm_art",        "格林", T_BEN, 2, T_SER,    "s:125,126,127,129,130,131,132",
				"增加道具**镇魂歌**：使用后获得 3 回合不死（生命到 0 也不死），状态结束时若已欠下死亡则仍会死去。");
		done(all, 129,"心爱的少女", "beloved_girl",     "格林", T_TWO, 2, T_SER,    "s:125,126,127,128,130,131,132",
				"每 2 层刷新一枚童话残片（共 9 种，对应九位少女）。3 枚不同残片炼成 1 张残页，3 张残页炼成《心爱的少女》。");
		//END(限制): 130 会把全部 BGM 换成格林主题；118/137 是"每回合概率触发音效"。
		//两者可共存，但 130 启用时 118/137 的音效不响 —— 属于功能失效，只警告不禁用。
		done(all, 130,"格林之音",   "grimm_music",      "格林", T_NEU, 1, T_SER,    "r:118,137;s:125,126,127,128,129,131,132",
				"将所有区域的**常规层**与**全部 Boss 战** BGM 替换为格林（黑魂）主题。纯娱乐规则，不影响战斗平衡。");
		done(all, 131,"格林之敌",   "grimm_enemy",      "格林", T_NEU, 1, T_SER,    "s:125,126,127,128,129,130,132",
				"将最终 Boss 古神替换为**莉耶芙**：贴图、名字、战斗对话、图鉴介绍全部替换。纯外观与文本，不影响数值。");
		done(all, 132,"黑暗之魂",   "dark_soul",        "格林", T_TWO, 2, T_SER,    "p:125,126,127,128,129,130,131,133,134,136;s:125,126,127,128,129,130,131",
				"终极联动：**古神护符替换为「爱丽丝」**（贴图、名字、说明文本全部替换 —— 它其实是一本书）。需要 125~131 与 133/134/136 全部勾选才生效。");
		done(all, 133,"格林之器2",  "grimm_weapon_2",   "格林", T_BEN, 3, T_SER,    "s:125,126,127,128,129,130,131,132",
				"按 5 阶武器在关卡中**掉落**获得**神天使双剑**：每次攻击命中数 +1（最多 7 次），每次造成 60% 伤害，切层清零。从第 21 层起有概率出现。");
		done(all, 136,"格林之器3",  "grimm_weapon_3",   "格林", T_BEN, 3, T_SER,    "s:125,126,127,128,129,130,131,132",
				"按 5 阶武器在关卡中**掉落**获得**怨恨之剑**：生命越低伤害越高，1 血时跃升 3 倍。从第 21 层起有概率出现。");
		//END(分类调整): 134 从「特殊」移入「格林」——它是格林内容
		//（与 128 镇魂歌同源的"以生命换力量"玩法）。联动保留 128，并接入格林系列。
		done(all, 134,"黄金蜂蜜酒", "golden_mead",      "格林", T_TWO, 3, T_MED,
				"s:128,125,126,127,129,130,131,132,133,136",
				"使用后获得**发狂**：攻击力 +50%，但每回合扣除最大生命的一半（最低保留 1 点）。与镇魂歌联动。");

		//==== 扩展包：139–147（表内有 ID 的新规则）====
		//等级 / 倾向 / 关系均按清单给定；ID 139–147 经核实为原表空号，可直接使用。
		done(all, 139,"紊乱法杖",   "chaos_wand",       "装备", T_TWO, 2, T_MED,    "s:60,21",
				"任何法杖施法时 **13%** 概率变成**另一种法杖**的效果（不是新物品，是全局规则）。等级沿用当前法杖。");
		done(all, 140,"枪枪爆头",   "headshot",         "战斗", T_BEN, 2, T_EASY,   "s:103,76",
				"玩家与目标距离 5 格以上时，远程攻击伤害必定为最大值。");
		done(all, 141,"禁魔空间",   "anti_magic_zone",  "环境", T_MON, 2, T_EASY,   "x:139,21,60",
				"所有魔法伤害降低 20%（玩家与怪物都受影响）。");
		done(all, 142,"无下限术士", "no_lower_limit",   "怪物", T_MON, 2, T_MED,    "s:140",
				"怪物受到远程攻击时，13% 概率完全免疫该次伤害。");
		done(all, 143,"吾为王者",   "i_am_king",        "怪物", T_MON, 3, T_MED,    "s:117,15",
				"所有 Boss 的命中与闪避提升 20%。");
		done(all, 144,"破碎权柄",   "broken_authority", "怪物", T_MON, 3, T_MED,    "s:75,143",
				"每个 Boss 生命**降至 33%** 后，**每 5 回合召唤 1 只稀有怪**（带随机精英词缀）。");
		done(all, 145,"神圣附体",   "holy_possession",  "神明", T_BEN, 1, T_EASY,   "",
				"经验获取增加 20%。");
		done(all, 146,"醍醐灌顶",   "enlightenment",    "特殊", T_BEN, 2, T_EASY,   "x:147",
				"每个天赋层级额外获得 1 点天赋点（与神圣灵感药水可叠加）。");
		done(all, 147,"就业紧张",   "job_crisis",       "特殊", T_RISK,3, T_EASY,   "x:146",
				"职业天赋全部失效（所有天赋加成一并无效）。");

		//==== 扩展包：148–168（清单未给 ID，按清单顺序编号）====
		done(all, 148,"飞天神偷",   "flying_thief",     "怪物", T_MON, 2, T_MED,    "",
				"13% 的怪物获得永久隐身（Boss 除外）。");
		done(all, 149,"黏糊蜂蜜",   "sticky_honey",     "环境", T_MON, 1, T_MED,    "",
				"每一层额外刷新 2 只蜜蜂。");
		done(all, 150,"淹没地牢",   "flooded_dungeon",  "地图", T_MON, 2, T_HARD,   "s:74;x:154",
				"每一层都是水域生态（整层被水淹没），水中 **3%** 生成幻影食人鱼（原表 20%，实测过多已下调）。与「废弃地牢」互斥。");
		done(all, 151,"圣明神明",   "holy_divinity",    "神明", T_TWO, 3, T_HARD,   "s:145",
				"玩家**生命 / 命中 / 闪避 +50%**、**攻击 +30%**，但**每 5 回合有 1 回合**必须停下来祷告（那回合无法行动）。");
		done(all, 152,"和平地牢",   "peaceful_dungeon", "怪物", T_TWO, 2, T_HARD,   "",
				"所有怪物**不会主动攻击你**，直到你**主动攻击任何怪物**（违反合约）。违反后本层怪物属性 +50%（Boss 层为 Boss 生命 +50%），**每下一层重置**。");
		done(all, 153,"恶魔地牢",   "demon_dungeon",    "怪物", T_MON, 2, T_MED,    "s:158",
				"所有怪物被视为恶魔类（只改属性标记，不改外观与数值）。");
		done(all, 154,"废弃地牢",   "abandoned_dungeon","地图", T_TWO, 2, T_HARD,   "s:112;x:150",
				"每一层都是草木生态（整层长满植被），踩踏植物时 **3%** 概率被缠绕 3 回合（原表 20%，实测过多已下调）。与「淹没地牢」互斥。");
		done(all, 155,"家传戒指",   "heirloom_ring",    "装备", T_BEN, 1, T_EASY,   "s:60",
				"开局额外获得一枚神射戒指（已鉴定）。");
		done(all, 156,"家传铠甲",   "heirloom_armor",   "装备", T_BEN, 1, T_EASY,   "",
				"开局额外获得一件板甲（已鉴定）。");
		done(all, 157,"附魔扩充",   "enchant_expansion","装备", T_BEN, 1, T_MED,    "s:58,108",
				"附魔池新增两条：**锋利**（近战伤害 +20%）与**力量**（远程伤害 +20%）。");
		done(all, 158,"神圣之力",   "holy_power",       "神明", T_BEN, 2, T_MED,    "s:153",
				"对恶魔类目标造成的伤害提升 30%。与「恶魔地牢」联动时收益最大化。");
		done(all, 159,"绵羊地牢",   "sheep_dungeon",    "环境", T_MON, 2, T_MED,    "",
				"玩家周围 7x7 范围内 13% 概率生成 1~2 只绵羊，触发后有 20 回合冷却。");
		done(all, 160,"氪金大佬",   "whale",            "经济", T_BEN, 2, T_MED,    "s:41",
				"可以**消耗金币给物品升级**：费用 = 100 × (当前等级 + 1)，**无等级上限**。");
		done(all, 161,"钱就是命",   "money_is_life",    "经济", T_BEN, 2, T_MED,    "s:41",
				"受到**致命伤**时用金币抵消伤害：每 1 点伤害消耗 `12 - 2×当前区域` 金币（最低 2 金），把生命保留在 1。**越深越省**。");
		done(all, 162,"真实地牢",   "realistic_dungeon","环境", T_RES, 2, T_EASY,   "",
				"空气稀薄：每 50-5x(层数/5) 回合必须停下深呼吸一次（层数越深间隔越短）。");
		done(all, 163,"古代升级",   "ancient_upgrade",  "特殊", T_BEN, 2, T_MED,    "",
				"玩家每达到 3 级，伤害的下限与上限各提升 10%。");
		done(all, 164,"魔法地牢",   "magic_dungeon",    "怪物", T_MON, 2, T_MED,    "s:141",
				"怪物有 **13%** 概率使用**随机一种魔法**（以法杖法术表示）。");
		done(all, 165,"神圣之光",   "holy_light",       "神明", T_BEN, 1, T_EASY,   "s:145",
				"每回合 13% 概率回复 2% 最大生命（满血时不触发）。");
		//166 神圣天使：前置为 4 条神圣类规则（145 神圣附体 / 158 神圣之力 / 165 神圣之光 / 151 圣明神明）
		done(all, 166,"神圣天使",   "holy_angel",       "神明", T_BEN, 3, T_SER,    "p:145,158,165,151",
				"集齐所有神圣类挑战后**变为天使**：祷告**不再消耗回合**。");
		done(all, 167,"黄金地牢",   "golden_dungeon",   "经济", T_TWO, 3, T_HARD,   "p:41",
				"怪物**不掉落任何物品**；地面生成的物品**全部换算成金币**。任务/剧情物品除外。");
		done(all, 168,"怪物地牢",   "monster_dungeon",  "怪物", T_MON, 3, T_MED,    "",
				"所有与怪物相关的概率规则提升至至少 25%。");
	}

	/** 登记一条待实装规则（{@code STATE_PENDING}）。 */
	private static void add(List<ChallengeDef> all, int id, String name, String key,
							String group, int tendency, int level, int tier, String relations) {
		ChallengeDef.Builder b = ChallengeDef.at(id, name, key)
				.group(group)
				.tendency(tendency)
				.level(level)
				.tier(tier)
				.state(ChallengeDef.STATE_PENDING);

		applyRelations(b, relations);
		all.add(b.build());
	}

	/**
	 * END(挑战框架): 登记一条**已实装**规则（{@code STATE_DONE}）+ 效果描述。
	 *
	 * <p>与 {@link #add} 的唯一区别是状态与 {@code effect}。
	 * 易档规则实装后用它替换 `add(...)` 调用即可。
	 */
	private static void done(List<ChallengeDef> all, int id, String name, String key,
							 String group, int tendency, int level, int tier,
							 String relations, String effect) {
		ChallengeDef.Builder b = ChallengeDef.at(id, name, key)
				.group(group)
				.tendency(tendency)
				.level(level)
				.tier(tier)
				.state(ChallengeDef.STATE_DONE)
				.effect(effect);

		applyRelations(b, relations);
		all.add(b.build());
	}

	/**
	 * 解析关系串。
	 * <p>前缀：{@code x}=互斥 {@code s}=联动 {@code r}=限制 {@code p}=前置；
	 * 冒号后是逗号分隔的 ID 列表；多条关系用 {@code ;} 分隔。
	 * <p>例：{@code "x:110"} → 互斥 110；{@code "s:6;2"} → 联动 6 与 2。
	 */
	private static void applyRelations(ChallengeDef.Builder b, String spec) {
		if (spec == null || spec.isEmpty()) return;

		for (String part : spec.split(";")) {
			part = part.trim();
			if (part.isEmpty()) continue;

			char kind = part.charAt(0);
			int colon = part.indexOf(':');
			if (colon < 0) continue;

			int[] ids = parseIds(part.substring(colon + 1));
			if (ids.length == 0) continue;

			switch (kind) {
				case 'x': b.exclusive(ids); break;
				case 's': b.synergy(ids);   break;
				case 'r': b.restrict(ids);  break;
				case 'p': b.requires(ids);  break;
				default: break;
			}
		}
	}

	private static int[] parseIds(String csv) {
		String[] parts = csv.split(",");
		List<Integer> ids = new ArrayList<>();
		for (String s : parts) {
			s = s.trim();
			if (s.isEmpty()) continue;
			try {
				ids.add(Integer.parseInt(s));
			} catch (NumberFormatException ignored) {
				//数据写错时跳过该 ID，不让整张表崩掉
			}
		}
		int[] out = new int[ids.size()];
		for (int i = 0; i < out.length; i++) out[i] = ids.get(i);
		return out;
	}

	/** 全部出现过的分组（按 {@link #ALL} 登记顺序）。 */
	public static List<String> groups() {
		List<String> out = new ArrayList<>();
		for (ChallengeDef def : ALL) {
			if (!out.contains(def.group)) out.add(def.group);
		}
		return out;
	}

	/** 按表 ID 取规则定义，没有则返回 null。 */
	public static ChallengeDef byId(int id) {
		return BY_ID.get(id);
	}

	/** 按分组取规则（保持登记顺序）。 */
	public static List<ChallengeDef> inGroup(String group) {
		List<ChallengeDef> out = new ArrayList<>();
		for (ChallengeDef def : ALL) {
			if (def.group.equals(group)) out.add(def);
		}
		return out;
	}

	//==== 与旧 int 掩码的互转（存档兼容的关键）====

	/**
	 * END(存档兼容): 旧 int 掩码 → 新 {@link ChallengeMask}。
	 *
	 * <p>把每个已实装规则的 {@code legacyBit} 映射到它的**表 ID 位**。
	 * 旧 int 里没有对应项的位（理论上不存在）会被忽略。
	 */
	public static ChallengeMask fromLegacyInt(int legacyMask) {
		ChallengeMask m = ChallengeMask.empty();
		if (legacyMask == 0) return m;
		for (ChallengeDef def : ALL) {
			if (def.legacyBit != 0 && (legacyMask & def.legacyBit) != 0) {
				m = m.with(def.id);
			}
		}
		return m;
	}

	/**
	 * END(存档兼容): 新 {@link ChallengeMask} → 旧 int 掩码。
	 *
	 * <p>只导出**已实装且有 legacyBit** 的规则，供 143 处
	 * {@code Dungeon.isChallenged(int)} 老调用点继续工作。
	 * 待实装规则没有旧位，不会出现在结果里（它们本来也还没有逻辑）。
	 */
	public static int toLegacyInt(ChallengeMask mask) {
		if (mask == null) return 0;
		int out = 0;
		for (ChallengeDef def : ALL) {
			if (def.legacyBit != 0 && mask.has(def.id)) {
				out |= def.legacyBit;
			}
		}
		return out;
	}

	private ChallengeRegistry() {}

	/**
	 * END(新增·第二批): 登记文档所有者口述的 14 条规则。
	 *
	 * <p>ID 分配（188-201，避开挑战区占用的 181-187）：
	 * <pre>
	 *   188 时间之力  189 时间加速  190 时间倒转  191 时间之神
	 *   192 不死之身  193 强化怪物  194 怪物之王
	 *   195 药水永恒  196 是药三分毒 197 再来一瓶
	 *   198 幸运药水  199 混合药水  200 炼金术士  201 药水盛宴
	 * </pre>
	 *
	 * <p>新增「时间」分组（原来没有）。
	 */
	/**
	 * END(新增·第三批): 202 起的规则。
	 *
	 * <p>ID 分配（避开第二批占用的 188-201）：
	 * <pre>
	 *   202 为何无泪  203 为何无力  204 为何无悔  205 为何无忌
	 *   206 为何无视  207 为何无避  208 为何无敌
	 * </pre>
	 */
	private static void registerBatch3(List<ChallengeDef> all) {

		//==================== 为何无X（七宗"无"）====================
		//文档所有者定稿：每条 -33%，集齐全部后变为 -50%。
		//所以这里用 p: 前置把它们串成一条链 —— 最后一条（为何无敌）
		//要求前面六条全部勾选，它才生效。
		done(all, 202, "为何无泪",   "why_no_tears",    "为何", T_TWO, 2, T_MED,  "s:203,204,205,206,207",
				"受到的伤害增加 33%。");
		done(all, 203, "为何无力",   "why_no_strength", "为何", T_TWO, 2, T_MED,  "s:202,204,205,206,207",
				"造成的伤害降低 33%。");
		done(all, 204, "为何无悔",   "why_no_regret",   "为何", T_TWO, 2, T_MED,  "s:202,203,205,206,207",
				"生命回复速度降低 33%。");
		done(all, 205, "为何无忌",   "why_no_fear",     "为何", T_TWO, 2, T_MED,  "s:202,203,204,206,207",
				"生命上限降低 33%。");
		done(all, 206, "为何无视",   "why_no_sight",    "为何", T_TWO, 2, T_MED,  "s:202,203,204,205,207",
				"命中降低 33%。");
		done(all, 207, "为何无避",   "why_no_escape",   "为何", T_TWO, 2, T_MED,  "s:202,203,204,205,206",
				"闪避降低 33%。");
		//==== END(无尽系列·六条独立挑战) ====
		//文档所有者定稿：这六条各自独立注册，各自控制自己的内容。
		done(all, 217, "爆裂魔法",   "meteor_wand",     "装备", T_BEN, 3, T_SER,  "",
				"杖头那颗石头永远在微微发热。\n\n-开局获得**爆裂魔法**法杖\n-伤害 **4-8**，每次施法消耗 **1 充能**\n-落点为中心**炸开**，造成范围伤害\n-**3×3 内**全额伤害\n-强化到 **+1 以上**范围扩至 **5×5**，外圈只有 **25%** 伤害");

		done(all, 211, "寰宇支配之剑", "universe_sword",  "无尽", T_BEN, 3, T_SER,  "p:214",
				"剑身上浮着整片星图，每一次挥动都像在挪动某个世界。\n\n-解锁**寰宇支配之剑**\n-炼金：**2 个无尽锭 + 剑核心 + 任意近战武器样品**\n-基础伤害 60-80，每级 +5~+10\n-附带你**最大生命 10%** 的额外伤害\n-造成目标**最大生命 10%** 的百分比伤害\n-每命中一次，伤害与攻速**翻倍**，最多 3 次");

		done(all, 212, "虚空不灭之甲", "void_armor",      "无尽", T_BEN, 3, T_SER,  "p:214",
				"甲面上什么也映不出来——包括你自己。\n\n-解锁**虚空不灭之甲**\n-炼金：**2 个无尽锭 + 甲核心 + 任意护甲样品**\n-护甲值 5-10，每级 +5~+10\n-免伤 **15 + 升级等级** %（最多 50%）\n-**15 + 升级等级** % 概率完全免疫一次伤害\n-每 **50 回合**回复 **50%** 生命\n-受到致命伤时触发**祝福十字架**，冷却 50 回合");

		done(all, 213, "轮回噬灭之戒", "reincarnation_ring","无尽", T_BEN, 3, T_SER,  "p:214",
				"戒指内侧刻着一圈咬住自己尾巴的蛇。\n\n-解锁**轮回噬灭之戒**\n-炼金：**2 个无尽锭 + 戒核心 + 任意戒指样品**\n-拥有**所有戒指**的效果\n-提供的效果等级**翻倍**（+100%）\n-每升级 1 级额外折算 **0.5 级**（升级效果 +50%）");

		done(all, 214, "无尽碎片",   "infinity_shard",  "无尽", T_BEN, 3, T_SER,  "",
				"装备被熔尽之后剩下的东西，边缘还在轻轻发烫。\n\n-解锁**无尽炼金体系**：\n· 15 级以上的装备可炼金转化为**无尽核心**\n· 分解装备获得**无尽碎片**（按装备阶数给 **1~5 个**）\n· **100 个碎片**合成 1 个**无尽锭**\n· **2 个无尽锭 + 对应核心 + 同类型样品装备**炼金合成对应顶级装备\n· 样品类型：剑→近战武器 / 甲→护甲 / 戒→戒指 / 弓→可堆叠投掷武器");

		done(all, 215, "天堂陨落长弓", "heaven_fall_bow", "无尽", T_BEN, 3, T_SER,  "p:214",
				"弓弦上残留着某种从天而降的东西。\n\n-解锁**天堂陨落长弓**\n-炼金：**2 个无尽锭 + 弓核心 + 任意可堆叠投掷武器样品**\n-每次攻击**连射 3 发**\n-**无视目标护甲**\n-命中后弹道向**四个方向分裂**\n-**神射戒**对其伤害加成提升 **200%**\n-换成**轮回噬灭之戒**则提升 **300%**");

		done(all, 216, "蕴生之剑",   "nurtured_sword",  "装备", T_BEN, 3, T_SER,  "",
				"剑身上刻着一圈年轮。它记得自己每一次被磨利的样子。\n\n-**开局即可获得**蕴生之剑\n-每 **+3 级**提升一阶（最高 5 阶）\n-阶数越高，**基础伤害与每级成长**都越高\n-伤害随**阶数与强化等级**一起涨（按代码公式实算）：\n  · 1 阶 +0 → **1-10**\n  · 2 阶 +3 → **5-27**\n  · 3 阶 +6 → **9-50**\n  · 4 阶 +9 → **13-79**\n  · 5 阶 +12 → **17-114**");

		done(all, 210, "永无止境",   "endless",         "无尽", T_RISK,3, T_SER,  "",
				"唯有不停攀登之人，才配知晓终点在何处。\n\n-通关 25 层后**不再结束**，而是从第 1 层重新开始\n-楼层号继续上涨（26/27/28…），地牢却是原版的第 1/2/3 层\n-每次循环，怪物获得一种新的**诅咒**，最多 **9 次**\n-区域倍率随轮回**加算**（每多一轮 +1 倍基础值，1 区起始 1200%）\n-**九次轮回之后**抵达原版第 26 层：可选择结束，或在**古神护符**处选「陷入无尽轮回」继续玩下去");

		done(all, 209, "无尽贪婪",   "infinite_greed",  "特殊", T_RISK,3, T_HARD, "s:188,191",
				"唯有贪婪之人，才可登阶成神。\n\n-解除**人物等级上限**（原本是 30 级）\n-满级后继续获得经验会照常升级");

		done(all, 208, "为何无敌",   "why_invincible",  "为何", T_TWO, 3, T_SER,  "p:202,203,204,205,206,207",
				"集齐前面六条「为何无X」后生效：所有百分比由 33% 变为 50%。");
	}

	private static void registerBatch2(List<ChallengeDef> all) {

		//==================== 时间类 ====================
		done(all, 188, "时间之力",   "time_power",      "特殊", T_BEN, 2, T_MED,  "s:189,191",
				"开局获得**时间沙漏**：一件可以操纵时间流向的道具。");
		done(all, 189, "时间加速",   "time_accel",      "特殊", T_TWO, 1, T_EASY, "s:188,191",
				"**游戏动画速度提升 2 倍**。战斗、移动、特效全部加快 —— 节省时间，但怪物也行动得更快。");
		//（190 时间倒转已取消 —— 需要序列化整个游戏状态，属于引擎级功能，文档所有者决定不做。）
		done(all, 191, "时间之神",   "time_god",        "特殊", T_BEN, 3, T_SER,  "p:188,189",
				"集齐全部时间类挑战后：**每 10 回合获得 2 回合时间气泡**（期间时间静止）。");

		//==================== 怪物类 ====================
		done(all, 192, "不死之身",   "undying",         "怪物", T_MON, 3, T_HARD, "",
				"**怪物无法死亡** —— 生命归零时改为**麻痹 50 回合**。\n\n它们会醒来，而且记得你。");
		//（193 强化怪物已删除 —— 它与 10 巨型化数值完全相同：
		//  13% / 体型 +50% / 生命 +50%，文档所有者确认是同一条。）
		done(all, 194, "怪物之王",   "king_of_mobs",    "怪物", T_MON, 3, T_SER,  "p:10,14,73,86,113,116,142,143,192",
				"选择**所有怪物增强类**挑战后：**所有怪物获得 20% 免伤**。");

		//==================== 药剂类 ====================
		done(all, 195, "药水永恒",   "lasting_potions", "药剂", T_BEN, 1, T_EASY, "",
				"**药水持续时间 +20%**。");
		done(all, 196, "是药三分毒", "toxic_potions",   "药剂", T_TWO, 1, T_EASY, "",
				"喝下药水有 **13%** 概率**中毒**。");
		done(all, 197, "再来一瓶",   "one_more",        "药剂", T_BEN, 2, T_EASY, "",
				"喝下药水有 **13%** 概率**不消耗**这瓶药水。");
		done(all, 198, "幸运药水",   "lucky_potion",    "药剂", T_BEN, 2, T_MED,  "",
				"新增道具**幸运药水**：使用后 **50 回合**内，财富戒指与幸运附魔的**效果 +50%**。\n\n获取方式：财富/幸运掉落。");
		done(all, 199, "混合药水",   "mixed_potions",   "药剂", T_TWO, 2, T_MED,  "",
				"除力量药水外，**刷新出来的药水全部变为紊乱药水**（商店购买等其它途径不受影响）。");
		done(all, 200, "炼金术士",   "alchemist",       "药剂", T_BEN, 1, T_EASY, "",
				"制作秘药时有 **13%** 概率**获得两份**。");
		done(all, 201, "药水盛宴",   "potion_feast",    "药剂", T_BEN, 1, T_EASY, "",
				"**药水刷新率提升 20%**。");
	}

	/**
	 * END(改造·挑战区并入挑战列表): 把 6 个挑战区注册成挑战规则。
	 *
	 * <p>它们的分组是「挑战区」，倾向写"中性"（这些区本身不改变数值，
	 * 只是"多一段内容"），等级 1。
	 *
	 * <p><b>单选</b>：用 {@code x:} 互斥把每一对都连起来 ——
	 * 这样勾选任意一个会自动取消其余五个，与原 {@code WndChallengeAreas}
	 * 的单选行为一致。
	 *
	 * <p>所有区都标记为**已实装**（{@code done()}）：它们本来就是可玩的内容，
	 * 只是入口换了地方。
	 */
	private static void registerChallengeAreas(List<ChallengeDef> all) {
		//先把所有区的 chalId 收齐，供互斥声明使用
		StringBuilder excl = new StringBuilder();
		for (ChallengeArea a : ChallengeArea.ALL) {
			if (excl.length() > 0) excl.append(",");
			excl.append(a.chalId);
		}
		String allIds = excl.toString();

		for (ChallengeArea a : ChallengeArea.ALL) {
			//互斥列表 = 除自己以外的所有区
			StringBuilder mine = new StringBuilder();
			for (ChallengeArea b : ChallengeArea.ALL) {
				if (b == a) continue;
				if (mine.length() > 0) mine.append(",");
				mine.append(b.chalId);
			}
			mine.append(";s:7");      //与 7 跳级生联动（跳级会跳过区域入口）

			String name = a.name + (a.implemented ? "" : "（未实装）");
			String effect = (a.desc == null ? "" : a.desc)
					+ "\n\n勾选后：通关 25F 会进入本区的 "
					+ a.floors + " 层内容（26F 起）。";

			done(all, a.chalId, name, "area_" + a.id, "挑战区",
					ChallengeDef.TENDENCY_NEUTRAL, 1, ChallengeDef.TIER_HARD,
					"x:" + mine, effect);
		}
	}
}
