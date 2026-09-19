/*
 * 破碎的地牢 (End fork) — 挑战 29「雇佣童工」的标记
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 29 雇佣童工): 「这只怪物是童工」的标记。
 *
 * <h3>为什么需要一个标记</h3>
 * 原表："怪物 13% 概率被替换：生命=原 20%，移速×2"。
 *
 * <p>生命可以直接在 {@code Mob.onAdd()} 里改（一次性），
 * 但**移速**不能 —— 它是每次查询时算的（{@code Char.speed()}），
 * 所以需要一个"这只怪是童工"的持久标记，让移速查询时能识别出来。
 *
 * <p>原来的做法是直接挂一个 99999 回合的 {@code Haste}，
 * 但那样**与玩家自己上的加速分不开**，而且 Haste 的语义是"暂时的"。
 * 这里用独立标记 + 在 {@code Char.speed()} 里乘倍率，更准确。
 */
public class ChildLaborMark extends FlavourBuff {

	{
		type = buffType.NEUTRAL;
		announced = false;
	}

	@Override public int icon(){ return BuffIndicator.NONE; }
}
