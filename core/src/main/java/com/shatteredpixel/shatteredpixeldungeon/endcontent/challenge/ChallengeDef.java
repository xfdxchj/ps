/*
 * 破碎的地牢 (End fork) — 挑战规则框架
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * END(挑战框架): 一条挑战规则的**完整元数据**。
 *
 * <p>设计目标：**新增一条规则 = 注册表里加一行数据 + 两行文案**，
 * 不需要改 UI、不需要改调度代码、不需要碰掩码结构。
 *
 * <p>字段直接对应权威清单
 * （{@code docs/CHALLENGE_RULES_AUTHORITATIVE.md}）。
 */
public final class ChallengeDef {

	//==== 倾向（对应原表"强度倾向"列）====

	public static final int TENDENCY_MONSTER   = 0;   // 怪物强化
	public static final int TENDENCY_RESOURCE  = 1;   // 资源压力
	public static final int TENDENCY_TWOSIDED  = 2;   // 双刃剑
	public static final int TENDENCY_BENEFIT   = 3;   // 玩家收益
	public static final int TENDENCY_RISK      = 4;   // 高风险
	public static final int TENDENCY_NEUTRAL   = 5;   // 中性

	/** 倾向中文名（UI 展示与配色用）。 */
	public static final String[] TENDENCY_NAMES = {
			"怪物强化", "资源压力", "双刃剑", "玩家收益", "高风险", "中性"
	};

	/** 倾向配色（UI 用）。 */
	public static final int[] TENDENCY_COLORS = {
			0xE8503A,   // 怪物强化 - 红
			0xC98A2B,   // 资源压力 - 橙
			0xB8A02E,   // 双刃剑 - 黄
			0x3ABE5A,   // 玩家收益 - 绿
			0xD93025,   // 高风险 - 深红
			0x9E9E9E    // 中性 - 灰
	};

	//==== 实现难度分档（排期用，不影响玩法）====

	public static final int TIER_EASY   = 0;   // 易：改数值/概率
	public static final int TIER_MEDIUM = 1;   // 中：需新 buff/物品/生成逻辑
	public static final int TIER_HARD   = 2;   // 难：需改地图生成/新怪物/新系统
	public static final int TIER_SERIES = 3;   // 系列：格林系列等内容包

	//==== 实装状态 ====

	public static final int STATE_PENDING = 0;   // ⬜ 待实装
	public static final int STATE_STUBBED = 1;   // 🔧 在架（有注册项，逻辑待接）
	public static final int STATE_DONE    = 2;   // ✅ 已实装

	//==== 字段 ====

	/** 表 ID（权威清单原值，含断层）。**同时是掩码位号**。 */
	public final int id;

	/** 规范中文名。 */
	public final String name;

	/** 英文名（用于文案 key 与内部标识）。 */
	public final String key;

	/** 分组（地图/战斗/怪物/经济/…），UI 分组用。 */
	public final String group;

	/** 强度倾向（{@code TENDENCY_*}）。 */
	public final int tendency;

	/** 等级 1/2/3，通过等级累加用。 */
	public final int level;

	/** 实现难度分档（{@code TIER_*}）。 */
	public final int tier;

	/** 实装状态（{@code STATE_*}）。 */
	public final int state;

	/** 是否计入"通过等级"与随机挑战骰子（测试/纯外观项为 false）。 */
	public final boolean countsForLevel;

	/** 类型标签（原表"类型标签"列，逗号分隔原文，UI 可展示）。 */
	public final String tags;

	/** 具体效果描述（原表"具体效果描述"列原文）。 */
	public final String effect;

	/** 关系列表（互斥/联动/限制/前置）。 */
	public final List<ChallengeRelation> relations;

	/**
	 * END(挑战框架): 对应的**旧 int 掩码位**（{@code Challenges.*} 常量），没有则为 0。
	 *
	 * <p>为什么需要它：本框架用**表 ID 当位号**，而原版 {@code Dungeon.challenges}
	 * 用的是另一套独立位定义（{@code NO_FOOD=1}、{@code STRONGER_BOSSES=256}…），
	 * 两套位号体系**完全不同**，不能直接互转。这张映射是两者之间唯一的桥梁：
	 * 读旧存档时把旧位翻译成表 ID，写存档时反向同步回 {@code int}，
	 * 从而让 143 处老的 {@code Dungeon.isChallenged(int)} 调用点继续正常工作。
	 */
	public final int legacyBit;

	private ChallengeDef(Builder b) {
		this.id = b.id;
		this.name = b.name;
		this.key = b.key;
		this.group = b.group;
		this.tendency = b.tendency;
		this.level = b.level;
		this.tier = b.tier;
		this.state = b.state;
		this.countsForLevel = b.countsForLevel;
		this.tags = b.tags;
		this.effect = b.effect;
		this.legacyBit = b.legacyBit;
		this.relations = Collections.unmodifiableList(b.relations);
	}

	/** 是否已实装（可以真正勾选生效）。 */
	public boolean isImplemented() {
		return state == STATE_DONE;
	}

	/** 是否在架（已注册，逻辑待接）——UI 可放行但标注。 */
	public boolean isUsable() {
		return state == STATE_DONE || state == STATE_STUBBED;
	}

	/** 取指定类型的关系，没有则返回空列表。 */
	public List<ChallengeRelation> relationsOf(ChallengeRelation.Type type) {
		List<ChallengeRelation> out = new ArrayList<>();
		for (ChallengeRelation r : relations) {
			if (r.type == type) out.add(r);
		}
		return out;
	}

	/** 是否与其他规则存在互斥关系。 */
	public boolean hasExclusive() {
		return !relationsOf(ChallengeRelation.Type.EXCLUSIVE).isEmpty();
	}

	/** 是否有前置条件（必须全部满足才能勾选）。 */
	public boolean hasPrerequisite() {
		return !relationsOf(ChallengeRelation.Type.PREREQUISITE).isEmpty();
	}

	/**
	 * END(挑战框架): 前置条件是否已全部满足。
	 *
	 * <p>用于「132 黑暗之魂」这类链式解锁：需 125~131 全选。
	 * 没有前置条件时恒为 {@code true}。
	 */
	public boolean prerequisitesMet(ChallengeMask mask) {
		if (mask == null) return !hasPrerequisite();
		for (ChallengeRelation r : relationsOf(ChallengeRelation.Type.PREREQUISITE)) {
			for (int id : r.targets) {
				if (!mask.has(id)) return false;
			}
		}
		return true;
	}

	/**
	 * 与指定掩码中已启用规则冲突的互斥项 ID（没有则为空）。
	 * <p>UI 用它判断"这条是否该置灰"。
	 *
	 * <p><b>互斥是双向的</b>：这里不只查"我声明了和谁互斥"，也查
	 * "谁声明了和我互斥"。注册表是手工录入的 118 条数据，单向遗漏很容易发生
	 * （例如 32 与 37 只写了一侧），只认一侧会导致反方向漏判置灰。
	 */
	public List<Integer> conflictingWith(ChallengeMask mask) {
		List<Integer> out = new ArrayList<>();
		if (mask == null) return out;

		//方向 1：本条声明了与对方互斥
		for (ChallengeRelation r : relationsOf(ChallengeRelation.Type.EXCLUSIVE)) {
			for (int id : r.targets) {
				if (mask.has(id) && !out.contains(id)) out.add(id);
			}
		}

		//方向 2：对方声明了与本条互斥（补上单向录入的缺口）
		for (ChallengeDef other : ChallengeRegistry.ALL) {
			if (other == this || !mask.has(other.id)) continue;
			if (out.contains(other.id)) continue;
			for (ChallengeRelation r : other.relationsOf(ChallengeRelation.Type.EXCLUSIVE)) {
				for (int t : r.targets) {
					if (t == this.id) { out.add(other.id); break; }
				}
			}
		}

		return out;
	}

	/**
	 * END(挑战框架): 与掩码中已启用规则存在**限制**关系（部分内容不生效）的 ID。
	 *
	 * <p>与互斥的区别：限制**允许共存**，而且**不是整条失效** ——
	 * 只是"本条规则对对方所涉及的那部分内容不生效"，其余部分照常工作。
	 *
	 * <p>例：57 残缺装备（装备 13% 概率残缺）与 108 装备觉醒 ——
	 * **只有那 13% 残缺的装备**不能觉醒，其余 87% 的正常装备照样能觉醒。
	 * 130 格林之音与 118/137：接管 BGM 后概率音效不响，但规则本身仍可启用。
	 *
	 * <p>UI 用它显示**提示**（不置灰、不阻止勾选）。
	 * 双向检测 —— 单侧录入也能互相感知。
	 */
	public List<Integer> restrictedBy(ChallengeMask mask) {
		List<Integer> out = new ArrayList<>();
		if (mask == null) return out;

		//方向 1：本条声明了限制对方
		for (ChallengeRelation r : relationsOf(ChallengeRelation.Type.RESTRICTION)) {
			for (int id : r.targets) {
				if (mask.has(id) && !out.contains(id)) out.add(id);
			}
		}

		//方向 2：对方声明了限制本条
		for (ChallengeDef other : ChallengeRegistry.ALL) {
			if (other == this || !mask.has(other.id)) continue;
			if (out.contains(other.id)) continue;
			for (ChallengeRelation r : other.relationsOf(ChallengeRelation.Type.RESTRICTION)) {
				for (int t : r.targets) {
					if (t == this.id) { out.add(other.id); break; }
				}
			}
		}

		return out;
	}

	/** 倾向中文名。 */
	public String tendencyName() {
		return (tendency >= 0 && tendency < TENDENCY_NAMES.length)
				? TENDENCY_NAMES[tendency] : "?";
	}

	/** 倾向配色。 */
	public int tendencyColor() {
		return (tendency >= 0 && tendency < TENDENCY_COLORS.length)
				? TENDENCY_COLORS[tendency] : 0x9E9E9E;
	}

	/** 文案 key（供 messages 查表：{@code challenges.<key>}）。 */
	public String messageKey() {
		return key;
	}

	@Override
	public String toString() {
		return "ChallengeDef{" + id + " " + name + " L" + level + "}";
	}

	//==== Builder ====

	public static Builder at(int id, String name, String key) {
		return new Builder(id, name, key);
	}

	public static final class Builder {
		private final int id;
		private final String name;
		private final String key;
		private String group = "特殊";
		private int tendency = TENDENCY_NEUTRAL;
		private int level = 1;
		private int tier = TIER_EASY;
		private int state = STATE_PENDING;
		private boolean countsForLevel = true;
		private String tags = "";
		private String effect = "";
		private int legacyBit = 0;
		private final List<ChallengeRelation> relations = new ArrayList<>();

		private Builder(int id, String name, String key) {
			this.id = id;
			this.name = name;
			this.key = key;
		}

		/** 对应的旧 {@code Challenges.*} 掩码位（已实装项才需要）。 */
		public Builder legacyBit(int bit) { this.legacyBit = bit; return this; }

		public Builder group(String g) { this.group = g; return this; }

		public Builder tendency(int t) { this.tendency = t; return this; }

		public Builder level(int l) { this.level = l; return this; }

		public Builder tier(int t) { this.tier = t; return this; }

		public Builder state(int s) { this.state = s; return this; }

		public Builder noLevelCount() { this.countsForLevel = false; return this; }

		public Builder tags(String t) { this.tags = t; return this; }

		public Builder effect(String e) { this.effect = e; return this; }

		/** 互斥：不能同时启用。 */
		public Builder exclusive(int... ids) {
			relations.add(new ChallengeRelation(ChallengeRelation.Type.EXCLUSIVE, ids));
			return this;
		}

		/** 联动：可同时启用且有特殊交互。 */
		public Builder synergy(int... ids) {
			relations.add(new ChallengeRelation(ChallengeRelation.Type.SYNERGY, ids));
			return this;
		}

		/** 限制：可同时启用但功能互相无效。 */
		public Builder restrict(int... ids) {
			relations.add(new ChallengeRelation(ChallengeRelation.Type.RESTRICTION, ids));
			return this;
		}

		/** 前置：必须满足才能勾选。 */
		public Builder requires(int... ids) {
			relations.add(new ChallengeRelation(ChallengeRelation.Type.PREREQUISITE, ids));
			return this;
		}

		public ChallengeDef build() {
			return new ChallengeDef(this);
		}
	}

	/** 供注册表内部批量构造用。 */
	static List<ChallengeDef> list(ChallengeDef... defs) {
		return new ArrayList<>(Arrays.asList(defs));
	}
}
