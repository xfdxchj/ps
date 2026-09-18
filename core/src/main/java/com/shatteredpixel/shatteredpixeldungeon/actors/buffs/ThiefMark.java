/*
 * 破碎的地牢 (End fork) — 挑战 87「盗贼鼠群」的赃款记录
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * END(挑战 87 盗贼鼠群): 记录某只怪物从玩家身上偷了多少金币。
 *
 * <h3>为什么需要它</h3>
 * 原表："怪物攻击 5% 概率偷金币，击杀后**返还双倍**"。
 * "击杀后返还"要求把"偷了多少"记在**具体的某只怪**身上 ——
 * 否则只能全局记账，就无法体现"追着打那只怪把钱拿回来"的设计。
 *
 * <p>这是一个纯标记 buff：不显示图标、不影响任何数值计算，
 * 只承载 {@link #stolenGold} 一个字段。
 */
public class ThiefMark extends Buff {

	/** 这只怪从玩家身上偷走的金币总额。 */
	public int stolenGold = 0;

	{
		//不显示图标：纯内部标记，玩家不需要看到"这只怪身上有钱"的状态条
		type = buffType.NEUTRAL;
		announced = false;
	}

	@Override
	public boolean act() {
		//纯标记，不做任何事，但必须周期性 spend 否则会被 Actor 移除
		spend(TICK);
		return true;
	}

	@Override
	public int icon() {
		//BuffIndicator.NONE 表示不显示
		return BuffIndicator.NONE;
	}

	private static final String STOLEN = "stolen_gold";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STOLEN, stolenGold);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		stolenGold = bundle.getInt(STOLEN);
	}
}
