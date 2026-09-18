/*
 * 破碎的地牢 (End fork) — 挑战 57「残缺装备」的附魔词缀
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * END(挑战 57 残缺装备): 一个**负面**附魔词缀。
 *
 * <h3>为什么做成附魔而不是全局掉率</h3>
 * 原表写的是"装备 13% 概率残缺，攻击力降低 20%"。
 * 若做成"获得装备时掷骰"，就要在武器/护甲/戒指等**每一条获得路径**上插桩，
 * 既容易漏，也会让"残缺"变成不可见的状态（玩家看不出这件为什么弱）。
 * 做成附魔后：状态可见（有词缀名与颜色）、存读档自动跟随、可被驱邪正常移除。
 *
 * <h3>效果</h3>
 * <ul>
 *   <li>攻击时伤害 <b>−20%</b>（在 {@link #proc} 里削减）</li>
 *   <li>标记为负面词缀（{@link #curse()} 返回 true），因此不能被当作正面外观炫耀</li>
 * </ul>
 *
 * <p>注意：本条**不**阻止 108 装备觉醒 —— "残缺不能觉醒"由 108 自己去查
 * {@code hasEnchant(Flawed.class)}，职责分开，避免两处逻辑打架。
 */
public class Flawed extends Weapon.Enchantment {

	/** 暗红偏灰 —— 与其它正面附魔的亮色区分。 */
	private static ItemSprite.Glowing RUST = new ItemSprite.Glowing( 0x8B4A3A );

	/** 攻击力降低比例：20%。 */
	public static final float DAMAGE_MULT = 0.80f;

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
		//减伤在 proc 里做：这是攻击伤害的收口点之一，
		//而且 proc 拿得到"这把武器"的上下文。
		return Math.max(1, Math.round(damage * DAMAGE_MULT));
	}

	@Override
	public boolean curse() {
		//标记为负面词缀：UI 上会以"被诅咒"的语气显示
		return true;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return RUST;
	}
}
