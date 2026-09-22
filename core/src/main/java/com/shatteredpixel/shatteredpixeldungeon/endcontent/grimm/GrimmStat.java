/*
 * 破碎的地牢 (End fork) — 挑战 126「格林之心」的属性定义
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

/**
 * END(126 格林之心): 六项可用魂强化的属性。
 *
 * <h3>文档所有者定稿</h3>
 * <pre>
 * 属性      效果              初始消耗   上限
 * 生命      最大生命 +5+1%    10 魂      99 级
 * 物理伤害  近战伤害 +2+1%    15 魂      99 级
 * 命中      命中 +2           10 魂      99 级
 * 闪避      闪避 +2           10 魂      99 级
 * 法术伤害  法杖伤害 +2+1%    15 魂      99 级
 * 力量      力量 +1           次数×20魂  无上限（最大 200 魂/次）
 * </pre>
 *
 * <p>除力量外每项上限 **99 级**；力量**无上限**，但消耗随次数线性上涨
 * （第 n 次要 {@code n × 20} 魂，封顶 200）——
 * 所以"力量无限"是理论上的，实际受魂的总量限制。
 */
public enum GrimmStat {

	/** 最大生命 +5 + 1%。 */
	HP("生命", 10, 99),

	/** 近战伤害 +2 + 1%。 */
	PHYS("物理伤害", 15, 99),

	/** 命中 +2。 */
	ACC("命中", 10, 99),

	/** 闪避 +2。 */
	EVA("闪避", 10, 99),

	/** 法杖伤害 +2 + 1%。 */
	MAGIC("法术伤害", 15, 99),

	/** 力量 +1；消耗为 次数 × 20，封顶 200；无上限。 */
	STR("力量", 0, Integer.MAX_VALUE);

	//==================================================================

	/** 中文名（UI 显示）。 */
	public final String title;

	/** **初始**消耗（力量为 0 —— 它另走"次数×20"的公式）。 */
	public final int baseCost;

	/** 等级上限。 */
	public final int maxLevel;

	GrimmStat(String title, int baseCost, int maxLevel){
		this.title = title;
		this.baseCost = baseCost;
		this.maxLevel = maxLevel;
	}

	/** END: 力量每级的消耗上限。 */
	public static final int STR_COST_PER_LEVEL = 20;
	/** END: 力量单次消耗的上限。 */
	public static final int STR_COST_CAP = 200;

	/**
	 * END: 把某项从 {@code currentLevel} 提到下一级要多少魂。
	 *
	 * <p>力量的公式与其它五项不同（文档所有者定稿）：
	 * <pre>
	 *   其它：固定值（生命/命中/闪避 10，物理/法术 15）
	 *   力量：min(200, (当前等级 + 1) × 20)
	 * </pre>
	 * 也就是第 1 级 20 魂、第 2 级 40、…、第 10 级起封顶 200。
	 */
	public int costAt(int currentLevel){
		if (this == STR){
			return Math.min(STR_COST_CAP, (Math.max(0, currentLevel) + 1) * STR_COST_PER_LEVEL);
		}
		return baseCost;
	}

	/** END: 该项是否还能继续升。 */
	public boolean canLevelUp(int currentLevel){
		return currentLevel < maxLevel;
	}

	/** END: 效果说明（UI 显示）。 */
	public String effectText(){
		switch (this){
			case HP:    return "最大生命 +5，再 +1%";
			case PHYS:  return "近战伤害 +2，再 +1%";
			case ACC:   return "命中 +2";
			case EVA:   return "闪避 +2";
			case MAGIC: return "法杖伤害 +2，再 +1%";
			case STR:   return "力量 +1";
			default:    return "";
		}
	}
}
