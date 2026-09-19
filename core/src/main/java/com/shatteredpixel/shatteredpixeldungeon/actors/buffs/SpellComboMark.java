/*
 * 破碎的地牢 (End fork) — 挑战 21「法术连击」的连锁防护标记
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 21 法术连击): 「本次施法已经追加过一次」的标记。
 *
 * <h3>为什么需要它</h3>
 * 原表："施法后 13% 概率再次施法，不消耗新资源，**单次最多追加一次**"。
 *
 * <p>没有这个标记的话，追加的那次施法又会走一遍 13% 判定 ——
 * 理论上可以无限连锁（虽然概率越来越低，但期望上有 1/0.87 ≈ 1.15 次额外施法，
 * 且极少数情况下会连出十几次）。
 *
 * <p>所以追加施法前先检查本标记：存在就不再追加。
 * 标记只活很短时间（见调用处的 2 回合），够挡住一次连锁即可。
 */
public class SpellComboMark extends FlavourBuff {

	{
		type = buffType.NEUTRAL;
		announced = false;
	}

	@Override public int icon(){ return BuffIndicator.NONE; }
}
