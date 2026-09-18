/*
 * 破碎的地牢 (End fork) — 挑战 10「巨型化」的标记
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 10 巨型化): 标记"这只怪是巨型化过的"。
 *
 * <h3>为什么用 buff 而不是在 Mob 上加字段</h3>
 * 加字段就必须同步改 {@code storeInBundle}/{@code restoreFromBundle}，
 * 否则旧存档缺这个键会读出默认值、新存档多了键会让旧版本崩。
 * 用 buff 载体：{@code Char} 的 buff 存读档是既有的、自动的。
 *
 * <h3>为什么不用 Property.LARGE</h3>
 * 原表 10 只说"更高生命值及更大体型"，**没有**说会进不了狭窄空间。
 * 而 {@code Property.LARGE} 会让怪物无法通过门道（那是精英 Giant 的行为）。
 * 所以这里只用它做标记，视觉放大在 {@code Mob.sprite()} 里处理。
 *
 * <p>永久性标记（不按回合消失），跟随怪物一生。
 */
public class ChallengeGiantMark extends Buff {

	/** 视觉放大倍率。 */
	public static final float SCALE = 1.2f;

	@Override
	public boolean act() {
		//永久标记：不递减、不解绑，只是跟着宿主
		spend(TICK);
		return true;
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}
}
