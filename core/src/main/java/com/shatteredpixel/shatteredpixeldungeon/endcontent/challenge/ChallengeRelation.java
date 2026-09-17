/*
 * 破碎的地牢 (End fork) — 挑战规则框架
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

/**
 * END(挑战框架): 挑战规则的**关系类型**。
 *
 * <p>原表里"互斥 / 联动 / 限制"三栏混在一起，但它们的**行为完全不同**，
 * 拆开后 UI 才能正确响应：
 *
 * <ul>
 *   <li>{@link #EXCLUSIVE 互斥} —— 不能同时启用。勾其一时另一条**置灰**。</li>
 *   <li>{@link #SYNERGY 联动} —— 可以同时启用，且有特殊交互。UI **只提示**，不阻止。</li>
 *   <li>{@link #RESTRICTION 限制} —— 可以同时启用，且 B 的规则**只对 A 涉及的那部分内容**不生效，
 *       其余部分照常工作。UI **只提示**，不阻止。
 *       <p>例：「57 残缺装备」（装备 <b>13% 概率</b>残缺）与「108 装备觉醒」：
 *       **只有那 13% 残缺的装备**不能觉醒，另外 87% 的正常装备**照样能觉醒** ——
 *       108 整条功能并没有失效。同理「59 诅咒装备」（诅咒概率 +13%）。
 *       <p>不要把这条理解成"勾了 A，B 就废了"：那会高估影响范围。</li>
 *   <li>{@link #PREREQUISITE 前置} —— 必须满足条件才能勾选。UI **置灰**。
 *       （格林系列 132 黑暗之魂专用：需 125~131 全选）</li>
 * </ul>
 */
public final class ChallengeRelation {

	public enum Type {
		/** 不能同时启用 → UI 置灰。 */
		EXCLUSIVE,
		/** 可同时启用且有特殊交互 → UI 只提示。 */
		SYNERGY,
		/** 可同时启用但功能互相无效 → UI 只警告。 */
		RESTRICTION,
		/** 必须满足才能勾选 → UI 置灰。 */
		PREREQUISITE
	}

	/** 关系类型。 */
	public final Type type;

	/**
	 * 关联的规则 ID 列表（**表 ID，不是数组下标**）。
	 * <p>因为原表编号有断层，ID 才是稳定标识。
	 */
	public final int[] targets;

	/** 展示给玩家的说明文字（可为空，空则用默认模板）。 */
	public final String note;

	public ChallengeRelation(Type type, int[] targets, String note) {
		this.type = type;
		this.targets = (targets == null) ? new int[0] : targets;
		this.note = (note == null) ? "" : note;
	}

	public ChallengeRelation(Type type, int... targets) {
		this(type, targets, "");
	}

	/** 该关系是否牵涉到指定 ID。 */
	public boolean involves(int id) {
		for (int t : targets) {
			if (t == id) return true;
		}
		return false;
	}
}
