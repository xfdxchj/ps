/*
 * 破碎的地牢 (End fork) — 挑战 24「以牙还牙」的反击冷却标记
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 24 以牙还牙): 限制"每回合每怪最多反击一次"的标记。
 *
 * <p>为什么需要它：玩家一回合可能对同一只怪攻击多次（连击、额外回合）。
 * 若每次都有 13% 反击，实际反击率会远高于设计值。
 * 挂上本标记后，同一回合内该怪不会再反击。
 *
 * <p>持续 1 回合即可 —— 下一回合标记自动消失，允许再次反击。
 * 不显示图标（内部标记）。
 */
public class ChallengeRetaliateMark extends FlavourBuff {

	/** 持续 1 回合。 */
	public static final float DURATION = 1f;

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}
}
