/*
 * 破碎的地牢 (End fork) — 挑战 13「狂热」的层数标记
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * END(挑战 13 狂热): 怪物每次成功攻击后攻速提升的**层数标记**。
 *
 * <p>每层 +13%，最多 3 层（×1.44），持续 5 回合。
 *
 * <h3>为什么自己管倒计时</h3>
 * {@code Buff.affect(Char, Class, float)} 只接受 {@link FlavourBuff} 子类，
 * 而 {@link FlavourBuff} 的 {@code act()} 是"立刻 detach"（不按回合递减）。
 * 本 buff 需要"持续 5 回合、期间可刷新"，所以继承 {@link Buff} 并自己
 * 用 {@code spend(TICK)} 递减 —— 这与 {@code Bleeding} 等持续型 buff 的写法一致。
 *
 * <p>层数需要存读档：若不写 {@code storeInBundle}，存档再读会丢掉层数，
 * 导致"读档后精英突然恢复原速"。
 */
public class ChallengeFrenzyMark extends Buff {

	/** 持续回合数。 */
	public static final float DURATION = 5f;

	/** 当前层数（0..3）。 */
	public int stacks = 0;

	private static final String STACKS = "stacks";

	@Override
	public boolean act() {
		//每回合递减；倒计时结束即消失，层数随之清空
		spend(TICK);
		if (cooldown() <= 0) {
			detach();
		}
		return true;
	}

	/**
	 * END(13 狂热): 刷新持续时间并累加一层。
	 *
	 * @param maxStacks 层数上限
	 */
	public void addStack(int maxStacks) {
		//把剩余时间重置为完整时长
		spend(-cooldown() + DURATION);
		if (stacks < maxStacks) {
			stacks++;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STACKS, stacks);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		stacks = bundle.getInt(STACKS);
	}
}
