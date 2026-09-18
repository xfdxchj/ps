/*
 * 破碎的地牢 (End fork) — 挑战 157「附魔扩充」的两个新附魔
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * END(挑战 157 附魔扩充): 「力量」—— 提升**远程**伤害。
 *
 * <p>与「锋利」互补：锋利管近战，力量管远程。
 * 两者可同时存在于不同武器上，但同一把武器只会有一条附魔，
 * 所以实际上玩家需要**分别**准备近战武器与远程武器才能吃满。
 *
 * <h3>什么算"远程"</h3>
 * <ul>
 *   <li>投掷武器（{@code MissileWeapon}）</li>
 *   <li>灵能弓（{@code SpiritBow}）</li>
 * </ul>
 */
public class Mighty extends Weapon.Enchantment {

	private static ItemSprite.Glowing AMBER = new ItemSprite.Glowing( 0xFFB347 );

	/** 远程伤害倍率：+20%。 */
	public static final float RANGED_MULT = 1.20f;

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
		if (isRanged(weapon)) {
			return Math.max(1, Math.round(damage * RANGED_MULT));
		}
		return damage;
	}

	/** 该武器是否属于远程。 */
	private static boolean isRanged(Weapon weapon) {
		return weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
					.missiles.MissileWeapon
			|| weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
					.SpiritBow;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return AMBER;
	}
}
