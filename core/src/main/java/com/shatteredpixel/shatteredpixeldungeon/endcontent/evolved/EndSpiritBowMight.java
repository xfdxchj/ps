package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment;

/**
 * END 灵能弓· 成品 ①「附魔灵弓」。
 *
 * 需求（最终要求）：“每次攻击必定触发一个【随机】附魔（可含稀有附魔），
 * 而不是触发弓身上固定的那一个附魔。”
 *
 * 本弓是“随机附魔工匠”：每一下命中的附魔，
 * 是在常见 / 稀见 / 稀有 全池里当场随机掷一把（而非身上那个静态 enchantment），
 * 并按该把原本的效果就地执行一次（可含稀有 Grim/Vampiric/腐化…），且必触发、不猜概率。
 *
 * 实现取舍：
 *  - 命中时临时把“身上静态附魔”摘下来，令父级(SpiritBow→Weapon)的常规处理不去触发那固定的一把，
 *    以免出现“固定 + 随机”双重触发、违背“不是弓上那个附魔”的原意；
 *  - 每击经 Weapon.Enchantment.random() 从【正面向全池 random (仅 common/uncommon/rare, 不含诅咒)】
 *    掷一把，再用本弓本体执行其 `proc` —— 效果强度随本弓 buffedLvl/level 自然缩放。
 */
public class EndSpiritBowMight extends SpiritBow {

	@Override public String name() { return "附魔灵弓"; }

	@Override
	public int proc(Char attacker, Char defender, int damage) {

		//1) 让父级先做自然之怒 / ID 等常规处理，但先摘掉身上的静态附魔，
		//   以免父级 Weapon.proc 顺手触发“弓上固定那一个”。
		Enchantment carried = enchantment;
		if (carried != null) {
			enchantment = null;                 // 直接字段存取，避开 enchant() 的附加副作用
		}
		damage = super.proc(attacker, defender, damage);
		if (carried != null) {
			enchantment = carried;              // 命中后还原，让它仍显示在物品面板上
		}

		//2) 每击都掷一把正面向【随机】附魔并执行（必触发；常见/稀见/稀有全池；若掷出即放）
		if (defender != null && defender.isAlive()){
			Enchantment roll = null;
			try {
				roll = Enchantment.random();    // 只取正面向，落诅咒的池外
			} catch (Exception ignore) { /* keep null */ }
			if (roll != null){
				try {
					damage = roll.proc(this, attacker, defender, damage);
				} catch (Exception ignore) {
					//个别附魔在不搭的处境抛异常就跳过本次该发，不进 crash
				}
			}
		}

		return damage;
	}
}
