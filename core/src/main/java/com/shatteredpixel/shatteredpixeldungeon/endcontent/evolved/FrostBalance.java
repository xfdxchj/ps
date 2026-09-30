package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

/**
 * END(规范化): 凝霜法杖「冰天雪地」的全部数值，只在这里定义一次。
 *
 * <p>代码和物品说明都引用这里的常量，避免"改了代码忘了改文本"。
 */
public final class FrostBalance {

	private FrostBalance() {}

	/** 冰爆主伤害倍率：150% 面板。 */
	public static final float BURST_PCT = 1.5f;
	/** 周围 3×3 溅射倍率：50% 面板。 */
	public static final float SPLASH_PCT = 0.5f;

	/** 寒冷持续时间 = 基础 + 法杖等级；水中再加水域加成。 */
	public static final float CHILL_BASE = 2f;
	public static final float CHILL_WATER_BONUS = 2f;

	/** 气体量 = 基础 + 每级；越大扩散越广、持续越久。 */
	public static final int GAS_VOLUME_BASE = 60;
	public static final int GAS_VOLUME_PER_LVL = 20;

	/** 粒子发射间隔，与原版毒气/酸蚀气一致。 */
	public static final float PARTICLE_INTERVAL = 0.4f;

	/** 面板伤害占比的整数百分比，供文本使用。 */
	public static int burstPctInt(){ return Math.round(BURST_PCT * 100f); }
	public static int splashPctInt(){ return Math.round(SPLASH_PCT * 100f); }
}
