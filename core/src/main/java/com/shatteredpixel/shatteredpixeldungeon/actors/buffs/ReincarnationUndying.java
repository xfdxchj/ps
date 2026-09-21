/*
 * 破碎的地牢 (End fork) — 轮回诅咒「不灭」的用过标记
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(轮回诅咒① 不灭): "这次无敌已经用掉了"的标记。
 *
 * <h3>为什么需要它</h3>
 * 文档所有者定稿："受到致命伤触发**一次**无敌" ——
 * 关键在"一次"：一只怪只能挡一次，之后再受到致命伤就该正常死。
 *
 * <p>用一个**永久 buff** 当标记是最省事的做法：
 * <ul>
 *   <li>挂在怪物身上，天然是"每只怪一份"</li>
 *   <li>不必额外维护一张"哪些怪用过了"的表</li>
 *   <li>怪物死了标记自然消失（不会有内存泄漏）</li>
 * </ul>
 *
 * <p>它不显示图标（{@code BuffIndicator.NONE}）—— 玩家不该看见
 * 一个内部标记，只需要在触发时看到"不灭"两个字。
 */
public class ReincarnationUndying extends Buff {

	{
		type = buffType.NEUTRAL;
		announced = false;
	}

	/** END: 永久 —— 只花时间，不倒计时。 */
	@Override
	public boolean act(){
		spend(TICK);
		return true;
	}

	@Override
	public int icon(){ return BuffIndicator.NONE; }
}
