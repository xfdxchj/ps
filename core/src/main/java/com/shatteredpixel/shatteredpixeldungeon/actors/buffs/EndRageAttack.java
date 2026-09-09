package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

/**
 * 狂暴攻击增幅(由破印Rage开启)：命中伤害 ×2；不影响攻速。
 * 行为在 Char.attack 中按此类判定。
 */
public class EndRageAttack extends FlavourBuff {
	public static final float DURATION = 10f;

	@Override public String toString(){ return "狂暴·痛击(+100%攻击)"; }
}
