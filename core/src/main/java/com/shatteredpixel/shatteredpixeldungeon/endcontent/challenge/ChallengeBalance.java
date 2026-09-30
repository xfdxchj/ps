package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

/**
 * END(规范化): 从 ChallengeEffects 里迁出的挑战数值。
 *
 * <p>新挑战的数值一律放这里或对应系统类，不再往 3000 多行的
 * ChallengeEffects 里加硬编码常量。
 */
public final class ChallengeBalance {

	private ChallengeBalance() {}

	/** 218 登神长阶：怪物复活概率（%）。 */
	public static final int ASCENSION_CHANCE_PCT = 13;

	/** 221 复仇狂怒：每层受到伤害的增幅。 */
	public static final float REVENGE_FURY_TAKEN_PER_STACK = 0.20f;
	/** 221 复仇狂怒：最多叠层。 */
	public static final int REVENGE_FURY_MAX_STACKS = 9;
	/** 221 复仇狂怒：持续回合。 */
	public static final float REVENGE_FURY_DURATION = 15f;
	//---- 68 极端状态：数量/掉落倍率 ----
	public static final float CROWDED_MOBS_MULT   = 1.20f;
	public static final float WAVE_MOBS_MULT      = 4.00f;
	public static final float SCARCE_ITEM_MULT    = 0.60f;
	public static final float EXCESS_ITEM_MULT    = 1.50f;
	public static final float ABUNDANCE_MULT      = 1.25f;
	public static final float BARREN_MULT         = 0.75f;
	public static final float FRIEREN_MULT        = 1.20f;
	public static final float HUNTER_DROP_MULT    = 0.70f;
	public static final int   HUNTER_CHEST_BONUS  = 1;

	//---- 通用概率（%）----
	public static final int PCT_13 = 13;
	public static final int MAGIC_MOB_PCT = 13;
	public static final int TOXIC_PCT = 13;
	public static final int ONE_MORE_PCT = 13;
	public static final int ALCHEMIST_PCT = 13;
	public static final int EMPOWERED_PCT = 13;
	public static final int GIANT_PCT = 13;
	public static final int FLOWER_PCT = 13;
	public static final int MIRROR_DUEL_PCT = 13;
	public static final int MIMIC_THREAT_PCT = 20;
	public static final int GRIMM_WEAPON_DROP_PCT = 25;

}
