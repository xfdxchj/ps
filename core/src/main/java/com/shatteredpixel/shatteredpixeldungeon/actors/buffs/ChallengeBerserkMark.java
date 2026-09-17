/*
 * 破碎的地牢 (End fork) — 挑战规则 9「狂暴」的标记
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 9 狂暴): 怪物受击后获得 20% 攻击提升 —— 的**纯标记**。
 *
 * <h3>为什么用 buff 而不是加字段</h3>
 * 如果直接在 {@code Mob} 上加一个 {@code boolean berserk} 字段，
 * 就必须同步改 {@code storeInBundle}/{@code restoreFromBundle}，
 * 否则旧存档缺这个键会读出默认值、新存档多了键会让旧版本崩。
 * 用 buff 载体：{@code Char} 的 buff 存读档是既有的、自动的，
 * 而且回合数由 {@link Buff} 自己维护，不用手写倒计时。
 *
 * <h3>为什么不出现在 buff 栏</h3>
 * {@link #icon()} 返回 {@code -1} 表示不显示图标。
 * 这是给怪物挂的内部标记，玩家不该在状态栏看到它；
 * 而且本 fork 的 {@code buffs.png} 帧数有限，新增图标会 nofound
 * （移植文档「避坑清单」第 6 条踩过这个坑）。
 */
public class ChallengeBerserkMark extends FlavourBuff {

	/** 持续回合数（见功能大全 ID 9：持续 2 回合）。 */
	public static final float DURATION = 2f;

	/**
	 * 不显示图标 —— 这是内部标记，不是给玩家看的状态。
	 * 返回 -1 时 {@code BuffIndicator} 会跳过它。
	 */
	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	/** 该标记是否让攻击者获得加成（供 {@link ChallengeEffects} 查询）。 */
	public boolean active() {
		return true;
	}
}
