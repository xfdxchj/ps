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

		//==== 待实装规则（权威清单 96 条，状态 STATE_PENDING）====
		//数据来源：docs/CHALLENGE_RULES_AUTHORITATIVE.md 的「三、权威规则全表」。
		//登记顺序 = 展示顺序（按分组：地图/战斗/怪物/经济/药剂/环境/装备/特殊/格林）。
		//这些条目在 UI 中置灰显示（见 WndChallenges），勾选无效，直到各自实装。
		registerPending(all);

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
		add(all, 1,  "牢地碎破",   "shattered_land",   "地图", T_MON, 3, T_HARD,   "s:6;2,3,4,5");
		add(all, 2,  "楼层混乱",   "floor_shuffle",    "地图", T_TWO, 2, T_HARD,   "s:3,4,5");
		add(all, 3,  "区域错位",   "region_shift",     "地图", T_TWO, 2, T_HARD,   "s:2,4,5");
		add(all, 4,  "精英迁徙",   "elite_migration",  "地图", T_MON, 2, T_HARD,   "s:14,75");
		add(all, 5,  "怪物入侵",   "monster_invasion", "地图", T_MON, 1, T_HARD,   "s:2,3,4");
		add(all, 6,  "完整地牢",   "full_dungeon",     "地图", T_TWO, 2, T_HARD,   "s:1");
		add(all, 7,  "跳级生",     "skip_student",     "地图", T_BEN, 1, T_MED,    "");

		//---- 战斗 ----
		add(all, 8,  "混乱",       "chaos",            "战斗", T_TWO, 1, T_MED,    "");
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
				"精英怪的生命上限、伤害、命中、闪避各提高 20%。本项会让精英怪出现，无需其它挑战配合。");		add(all, 15, "首领护卫",   "boss_guard",       "战斗", T_MON, 3, T_MED,    "");
		done(all, 16, "大力水手",   "popeye",           "战斗", T_TWO, 1, T_EASY,   "",
				"玩家近战物理攻击 +25%，攻击速度 -20%。");
		add(all, 17, "情人节",     "valentine",        "战斗", T_BEN, 1, T_MED,    "");
		add(all, 18, "老龄化",     "aging",            "战斗", T_BEN, 3, T_MED,    "");
		done(all, 19, "风驰电掣",   "swift",            "战斗", T_TWO, 1, T_EASY,   "s:103",
				"玩家攻速 +20%，怪物移速 +20%。");
		add(all, 20, "等我启动",   "wind_up",          "战斗", T_TWO, 2, T_MED,    "");
		add(all, 21, "法术连击",   "spell_combo",      "战斗", T_BEN, 2, T_MED,    "");
		add(all, 22, "物极必反",   "overkill_reverse", "战斗", T_TWO, 3, T_MED,    "");
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
		add(all, 68, "极端状态",   "extreme_state",    "战斗", T_TWO, 3, T_MED,    "s:104");
		add(all, 69, "九九归一",   "nine_to_one",      "战斗", T_TWO, 2, T_MED,    "");
		done(all, 78, "烈火焚身",   "immolation",       "战斗", T_MON, 3, T_EASY,   "",
				"玩家受击时 13% 概率燃烧。");
		add(all, 103,"弹幕地狱",   "bullet_hell",      "战斗", T_TWO, 2, T_MED,    "s:76");
		add(all, 121,"中世纪骑士", "medieval_knight",  "战斗", T_TWO, 2, T_MED,    "x:110");
		done(all, 135,"亡者之怒",   "wrath_of_dead",    "战斗", T_RISK,2, T_EASY,   "s:68,124,128",
				"玩家生命低于 10% 时，造成的伤害翻倍。");

		//---- 怪物 ----
		add(all, 29, "雇佣童工",   "child_labor",      "怪物", T_TWO, 2, T_MED,    "");
		done(all, 30, "人口密集",   "crowded",          "怪物", T_MON, 2, T_EASY,   "s:75",
				"普通怪物生成数量提高 20%（与 119 怪物浪潮可叠加）。");
		add(all, 73, "神秘复苏",   "mystic_revival",   "怪物", T_MON, 2, T_MED,    "s:77,86");
		done(all, 75, "精英地牢",   "elite_dungeon",    "怪物", T_MON, 3, T_MED,    "s:14,30,97",
				"13% 的怪物被替换为其稀有变种（白化老鼠、寄居蟹、强盗等）。");
		add(all, 76, "原始状态",   "primal_state",     "怪物", T_MON, 2, T_MED,    "s:103");
		add(all, 77, "亡灵法师",   "necromancer",      "怪物", T_MON, 3, T_MED,    "s:73,86");
		add(all, 86, "复仇之魂",   "vengeful_spirit",  "怪物", T_MON, 2, T_MED,    "s:73,77");
		add(all, 87, "盗贼鼠群",   "thief_rats",       "怪物", T_TWO, 1, T_EASY,   "");
		add(all, 97, "我的世界",   "minecraft",        "怪物", T_MON, 3, T_HARD,   "s:75,77");
		add(all, 100,"镜像对决",   "mirror_match",     "怪物", T_MON, 3, T_HARD,   "r:77");
		done(all, 119,"怪物浪潮",   "monster_wave",     "怪物", T_TWO, 2, T_EASY,   "s:30",
				"怪物生成数量 ×4；普通怪物的生命与伤害变为原来的 20%（Boss 不削弱）。");
		add(all, 122,"我的世界II", "minecraft_ii",     "怪物", T_MON, 3, T_MED,    "s:97");

		//---- 经济 ----
		add(all, 33, "黑市",       "black_market",     "经济", T_TWO, 1, T_MED,    "");
		done(all, 34, "赏金制度",   "bounty",           "经济", T_BEN, 1, T_EASY,   "",
				"击杀精英怪额外获得 30 金币，击杀 Boss 额外获得 120 金币。");
		done(all, 35, "丰饶",       "abundance",        "经济", T_BEN, 1, T_EASY,   "x:36",
				"资源及物品掉落增加 25%。");
		done(all, 36, "贫瘠",       "barren",           "经济", T_RES, 2, T_EASY,   "x:35",
				"资源及物品掉落减少 25%（不会降到 0，保证通关所需的最少资源）。");
		done(all, 37, "高价回收",   "high_buyback",     "经济", T_BEN, 1, T_EASY,   "x:32",
				"把物品卖给商店所得 ×1.5。");
		add(all, 38, "盲盒",       "loot_box",         "经济", T_TWO, 1, T_MED,    "");
		add(all, 39, "All or Nothing","all_or_nothing", "经济", T_RISK,3, T_MED,   "");
		add(all, 40, "贷款",       "loan",             "经济", T_TWO, 2, T_MED,    "");
		add(all, 41, "钱是万能",   "money_is_power",   "经济", T_BEN, 2, T_MED,    "");
		add(all, 42, "等价交换",   "equivalent_exchange","经济",T_TWO,1, T_MED,    "");
		done(all, 43, "一贫如洗",   "destitute",        "经济", T_RES, 2, T_EASY,   "",
				"每次进入新区域（每 5 层）时，金币减少 20%。");
		done(all, 44, "慷慨商人",   "generous_merchant","经济", T_TWO, 1, T_EASY,   "s:32",
				"商店商品数量 +30%，商店售价 ×1.25（与通货膨胀可共存）。");
		add(all, 88, "拍卖行",     "auction_house",    "经济", T_TWO, 2, T_MED,    "x:101");
		//END(限制): 101 摧毁商店 → 以下所有商店相关规则全部失效（可共存，只警告）。
		//32 通货膨胀 / 33 黑市 / 34 赏金制度 / 37 高价回收 / 38 盲盒 /
		//39 All or Nothing / 40 贷款 / 41 钱是万能 / 42 等价交换 / 44 慷慨商人 / 88 拍卖行
		//（88 拍卖行同时还是严格互斥：商店没了它根本无法运作）
		add(all, 101,"全员恶人",   "all_villains",     "经济", T_RISK,3, T_HARD,   "x:88;r:32,33,34,37,38,39,40,41,42,44");

		//---- 药剂 ----
		add(all, 46, "药剂不稳定", "unstable_potions", "药剂", T_TWO, 2, T_MED,    "");
		done(all, 47, "稀缺补给",   "scarce_supplies",  "药剂", T_RES, 2, T_EASY,   "x:48",
				"消耗品（药水、卷轴、食物、炸弹）生成数量减少 40%。");
		done(all, 48, "过量补给",   "excess_supplies",  "药剂", T_BEN, 1, T_EASY,   "x:47",
				"消耗品（药水、卷轴、食物、炸弹）生成数量增加 50%。");
		add(all, 124,"野生狗奶",   "wild_milk",        "药剂", T_RISK,3, T_MED,    "");

		//---- 环境 ----
		add(all, 49, "切尔诺贝利", "chernobyl",        "环境", T_TWO, 3, T_MED,    "s:111");
		add(all, 52, "陷阱泛滥",   "trap_overflow",    "环境", T_MON, 2, T_HARD,   "");
		done(all, 54, "我爱花花",   "flower_lover",     "环境", T_NEU, 1, T_EASY,   "",
				"13% 的草地被替换为随机植物。纯趣味，不影响数值。");
		add(all, 74, "热带雨林",   "rainforest",       "环境", T_MON, 2, T_MED,    "");
		done(all, 80, "冰天雪地",   "frozen_world",     "环境", T_TWO, 2, T_EASY,   "",
				"玩家每回合 13% 概率寒冷、2% 概率冰冻。");
		done(all, 90, "雷暴",       "thunderstorm",     "环境", T_TWO, 2, T_EASY,   "",
				"每回合 5% 概率闪电随机劈中一个角色，伤害 = 3 × 层数 ÷ 5，并可能点燃。");

		//---- 装备 ----
		add(all, 55, "不稳定强化", "unstable_upgrade", "装备", T_TWO, 2, T_MED,    "s:108");
		add(all, 56, "装备绑定",   "equip_binding",    "装备", T_RES, 2, T_MED,    "");
		add(all, 57, "残缺装备",   "broken_equipment", "装备", T_RES, 2, T_MED,    "r:108");
		add(all, 58, "随机附魔",   "random_enchant",   "装备", T_BEN, 1, T_MED,    "s:108");
		add(all, 59, "诅咒装备",   "cursed_equipment", "装备", T_RES, 2, T_MED,    "r:108");
		add(all, 60, "家传法杖",   "heirloom_wand",    "装备", T_BEN, 1, T_MED,    "");
		add(all, 79, "高级附魔台", "advanced_enchant", "装备", T_BEN, 1, T_MED,    "");
		add(all, 81, "搏杀赌徒",   "gambler",          "装备", T_TWO, 2, T_MED,    "x:126");
		add(all, 108,"装备觉醒",   "awakening",        "装备", T_BEN, 1, T_MED,    "r:57,59");

		//---- 特殊 / 娱乐 ----
		done(all, 61, "炸弹狂魔",   "bomb_fanatic",     "特殊", T_BEN, 1, T_EASY,   "",
				"每层额外掉落 1 个炸弹。");
		done(all, 62, "芙莉莲",     "frieren",          "特殊", T_BEN, 1, T_EASY,   "",
				"宝箱数量提高约 20%。");
		add(all, 63, "鼠鼠可爱",   "cute_rats",        "特殊", T_NEU, 1, T_HARD,   "");
		done(all, 64, "宝物猎人",   "treasure_hunter",  "特殊", T_TWO, 1, T_EASY,   "",
				"普通怪物掉落减少 30%，每个宝箱额外增加 1 件物品。");
		add(all, 65, "及时雨",     "timely_rain",      "特殊", T_BEN, 2, T_MED,    "s:104");
		add(all, 67, "宝箱危机",   "chest_crisis",     "特殊", T_RISK,3, T_MED,    "");
		done(all, 70, "生活部长",   "life_minister",    "特殊", T_NEU, 1, T_MED,    "",
				"每回合 3% 概率停止行动，说出「首先，我是生活部部长」。");
		add(all, 71, "喝大了",     "drunk",            "特殊", T_TWO, 2, T_MED,    "");
		done(all, 72, "前程似锦",   "bright_future",    "特殊", T_NEU, 1, T_MED,    "",
				"每回合 3% 概率停止行动，说出「王同学，我祝你前～程～似锦」。");
		done(all, 95, "耗子尾汁",   "rat_tail_soup",    "特殊", T_BEN, 1, T_MED,    "",
				"每回合 3% 概率显示「耗子尾汁」，并播放随机音效。");
		done(all, 96, "奥利给",     "oligei",           "特殊", T_TWO, 1, T_MED,    "",
				"每回合 3% 概率停止行动，喊「奥利给」，获得 1 回合狂暴。");
		add(all, 104,"命悬一线",   "close_call",       "特殊", T_TWO, 2, T_MED,    "s:65,68");
		done(all, 118,"天意侵蚀",   "providence",       "特殊", T_TWO, 1, T_MED,    "",
				"每回合 13% 概率随机播放一段新三国音效。");
		add(all, 120,"404",        "error_404",        "特殊", T_RISK,2, T_MED,    "");
		done(all, 123,"大学生",     "college_student",  "特殊", T_TWO, 1, T_EASY,   "",
				"玩家每回合 3% 概率受到 1 点伤害（不会致死，生命值至少保留 1）。");
		add(all, 134,"黄金蜂蜜酒", "golden_mead",      "特殊", T_TWO, 3, T_MED,    "s:128");
		done(all, 137,"奶龙大笑",   "milk_dragon",      "特殊", T_NEU, 1, T_MED,    "",
				"每回合 3% 概率触发奶龙大笑音效。");
		done(all, 138,"荒诞世界",   "absurd_world",     "特殊", T_NEU, 1, T_EASY,   "",
				"怪物贴图随机变化。纯外观，不影响属性与 AI。");

		//---- 格林系列（125–133、136）· 链式前置 ----
		add(all, 125,"格林之器",   "grimm_weapon",     "格林", T_BEN, 3, T_SER,    "s:126,127,128,129,130,131,132");
		//END(互斥): 126 移除升级系统，而 81 依赖升级卷轴产出 —— 二者功能冲突，必须互斥。
		add(all, 126,"格林之心",   "grimm_heart",      "格林", T_RISK,3, T_SER,    "x:81;s:125,127,128,129,130,131,132");
		add(all, 127,"格林之戒",   "grimm_ring",       "格林", T_BEN, 2, T_SER,    "s:125,126,128,129,130,131,132");
		add(all, 128,"格林之术",   "grimm_art",        "格林", T_BEN, 2, T_SER,    "s:125,126,127,129,130,131,132");
		add(all, 129,"心爱的少女", "beloved_girl",     "格林", T_TWO, 2, T_SER,    "s:125,126,127,128,130,131,132");
		//END(限制): 130 会把全部 BGM 换成格林主题；118/137 是"每回合概率触发音效"。
		//两者可共存，但 130 启用时 118/137 的音效不响 —— 属于功能失效，只警告不禁用。
		done(all, 130,"格林之音",   "grimm_music",      "格林", T_NEU, 1, T_SER,    "r:118,137;s:125,126,127,128,129,131,132",
				"将所有常规区域与 Boss 战 BGM 替换为格林（黑魂）主题 BGM。纯娱乐规则，不影响战斗平衡。");
		add(all, 131,"格林之敌",   "grimm_enemy",      "格林", T_NEU, 1, T_SER,    "s:125,126,127,128,129,130,132");
		add(all, 132,"黑暗之魂",   "dark_soul",        "格林", T_TWO, 2, T_SER,    "p:125,126,127,128,129,130,131");
		add(all, 133,"格林之器2",  "grimm_weapon_2",   "格林", T_BEN, 3, T_SER,    "s:125,126,127,128,129,130,131,132");
		add(all, 136,"格林之器3",  "grimm_weapon_3",   "格林", T_BEN, 3, T_SER,    "s:125,126,127,128,129,130,131,132");

		//==== 扩展包：139–147（表内有 ID 的新规则）====
		//等级 / 倾向 / 关系均按清单给定；ID 139–147 经核实为原表空号，可直接使用。
		add(all, 139,"紊乱法杖",   "chaos_wand",       "装备", T_TWO, 2, T_MED,    "s:60,21");
		add(all, 140,"枪枪爆头",   "headshot",         "战斗", T_BEN, 2, T_MED,    "s:103,76");
		add(all, 141,"禁魔空间",   "anti_magic_zone",  "环境", T_MON, 2, T_MED,    "x:139,21,60");
		add(all, 142,"无下限术士", "no_lower_limit",   "怪物", T_MON, 2, T_MED,    "s:140");
		add(all, 143,"吾为王者",   "i_am_king",        "怪物", T_MON, 3, T_MED,    "s:117,15");
		add(all, 144,"破碎权柄",   "broken_authority", "怪物", T_MON, 3, T_MED,    "s:75,143");
		done(all, 145,"神圣附体",   "holy_possession",  "特殊", T_BEN, 1, T_EASY,   "",
				"经验获取增加 20%。");
		done(all, 146,"醍醐灌顶",   "enlightenment",    "特殊", T_BEN, 2, T_EASY,   "x:147",
				"每个天赋层级额外获得 1 点天赋点（与神圣灵感药水可叠加）。");
		done(all, 147,"就业紧张",   "job_crisis",       "特殊", T_RISK,3, T_EASY,   "x:146",
				"职业天赋全部失效（所有天赋加成一并无效）。");

		//==== 扩展包：148–168（清单未给 ID，按清单顺序编号）====
		add(all, 148,"飞天神偷",   "flying_thief",     "怪物", T_MON, 2, T_MED,    "");
		add(all, 149,"黏糊蜂蜜",   "sticky_honey",     "环境", T_MON, 1, T_MED,    "");
		add(all, 150,"淹没地牢",   "flooded_dungeon",  "地图", T_MON, 2, T_HARD,   "s:74");
		add(all, 151,"圣明神明",   "holy_divinity",    "特殊", T_TWO, 3, T_HARD,   "s:145");
		add(all, 152,"和平地牢",   "peaceful_dungeon", "怪物", T_TWO, 2, T_HARD,   "");
		add(all, 153,"恶魔地牢",   "demon_dungeon",    "怪物", T_MON, 2, T_MED,    "s:158");
		add(all, 154,"废弃地牢",   "abandoned_dungeon","地图", T_TWO, 2, T_HARD,   "s:112");
		done(all, 155,"家传戒指",   "heirloom_ring",    "装备", T_BEN, 1, T_EASY,   "s:60",
				"开局额外获得一枚神射戒指（已鉴定）。");
		done(all, 156,"家传铠甲",   "heirloom_armor",   "装备", T_BEN, 1, T_EASY,   "",
				"开局额外获得一件板甲（已鉴定）。");
		add(all, 157,"附魔扩充",   "enchant_expansion","装备", T_BEN, 1, T_MED,    "s:58,108");
		add(all, 158,"神圣之力",   "holy_power",       "特殊", T_BEN, 2, T_MED,    "s:153");
		add(all, 159,"绵羊地牢",   "sheep_dungeon",    "环境", T_MON, 2, T_MED,    "");
		add(all, 160,"氪金大佬",   "whale",            "经济", T_BEN, 2, T_MED,    "s:41");
		add(all, 161,"钱就是命",   "money_is_life",    "经济", T_BEN, 2, T_MED,    "s:41");
		add(all, 162,"真实地牢",   "realistic_dungeon","环境", T_RES, 2, T_MED,    "");
		add(all, 163,"古代升级",   "ancient_upgrade",  "特殊", T_BEN, 2, T_MED,    "");
		add(all, 164,"魔法地牢",   "magic_dungeon",    "怪物", T_MON, 2, T_MED,    "s:141");
		done(all, 165,"神圣之光",   "holy_light",       "特殊", T_BEN, 1, T_EASY,   "s:145",
				"每回合 13% 概率回复 2% 最大生命（满血时不触发）。");
		//166 神圣天使：前置为 4 条神圣类规则（145 神圣附体 / 158 神圣之力 / 165 神圣之光 / 151 圣明神明）
		add(all, 166,"神圣天使",   "holy_angel",       "特殊", T_BEN, 3, T_SER,    "p:145,158,165,151");
		add(all, 167,"黄金地牢",   "golden_dungeon",   "经济", T_TWO, 3, T_HARD,   "p:41");
		add(all, 168,"怪物地牢",   "monster_dungeon",  "怪物", T_MON, 3, T_MED,    "");
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

	/** 已实装（可以真正勾选生效）的规则。 */
	public static List<ChallengeDef> implemented() {
		List<ChallengeDef> out = new ArrayList<>();
		for (ChallengeDef def : ALL) {
			if (def.isImplemented()) out.add(def);
		}
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
}
