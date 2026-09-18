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
 * END(挑战 157 附魔扩充): 「锋利」—— 提升**近战**伤害。
 *
 * <p>与「锐利」的区别：锐利（{@code Sharp}）是原版匕首自带的穿透效果；
 * 本附魔是纯粹的近战伤害倍率，任何武器都能带。
 *
 * <p>只在**近战**攻击时生效：远程武器（投掷物/弓）不吃这个加成 ——
 * 远程走的是「力量」那一条，两者分工明确。
 */
public class Keen extends Weapon.Enchantment {

	private static ItemSprite.Glowing STEEL = new ItemSprite.Glowing( 0xC0C0D0 );

	/** 近战伤害倍率：+20%。 */
	public static final float MELEE_MULT = 1.20f;

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
		//近战判定：武器不是投掷物且攻击者与目标相邻
		if (isMelee(weapon, attacker, defender)) {
			return Math.max(1, Math.round(damage * MELEE_MULT));
		}
		return damage;
	}

	/** 是否属于近战攻击。 */
	private static boolean isMelee(Weapon weapon, Char attacker, Char defender) {
		if (weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
				.missiles.MissileWeapon) {
			return false;
		}
		if (weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
				.SpiritBow) {
			return false;
		}
		if (attacker == null || defender == null) return false;
		if (com.shatteredpixel.shatteredpixeldungeon.Dungeon.level == null) return false;
		//相邻 = 近战
		return com.shatteredpixel.shatteredpixeldungeon.Dungeon.level
				.adjacent(attacker.pos, defender.pos);
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return STEEL;
	}
}
