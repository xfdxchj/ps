/*
 * 破碎的地牢 (End fork) — 挑战 152「和平地牢」的违约标记
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 152 和平地牢): 「玩家违反了和平合约」的强化标记。
 *
 * <h3>作用</h3>
 * 挂在被强化的怪物身上，表示"它现在属性 +50%"。
 *
 * <p>"生命 +50%" 在挂载时直接加到 {@code HT}（一次性）；
 * 而"伤害 +50%" 需要每次算伤害时读这个标记 —— 因为伤害是每回合重算的。
 *
 * <p>换层时由 {@code ChallengeEffects.resetPeaceful()} 统一 detach，
 * 符合原表"每下一层重置"。
 */
public class PeaceBrokenMark extends FlavourBuff {

	{
		type = buffType.NEUTRAL;
		announced = false;
	}

	/** 伤害倍率（原表：属性 +50%）。 */
	public static final float DMG_MULT = 1.5f;

	@Override public int icon(){ return BuffIndicator.NONE; }
}
