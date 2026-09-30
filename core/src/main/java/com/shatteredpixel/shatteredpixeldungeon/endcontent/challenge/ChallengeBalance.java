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
}
