package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;

import java.util.ArrayList;

/**
 * END 灵能弓· 成品 ②「雷鸣」。
 * 命中主目标后，以主目标为中心向外跳 1 格的闪电链，命中怪各吃 20% 原始伤害。
 * 电弧收集/动效复用原生 Shocking.arc 助手；只对存活敌对额外结算，避免劈到射击者自己。
 *
 * 额外：雷鸣灵弓自身攻击速度 +50%（它射得比普通灵能弓快一半）；
 * 通过 speedMultiplier(owner) ×1.5 → Weapon.delayFactor = baseDelay / speedMultiplier 自动变快。
 */
public class EndSpiritBowStorm extends SpiritBow {

	@Override public String name() { return "雷鸣灵弓"; }

	/** 雷鸣灵弓自身 +50% 攻击速度。 */
	@Override
	protected float speedMultiplier( Char owner ){
		return super.speedMultiplier( owner ) * 1.5f;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);

		if (defender == null || !defender.isAlive() || defender == attacker) return damage;

		int bolt = Math.round(damage * 0.2f);
		if (bolt < 1) bolt = 1;

		ArrayList<Char> chained = new ArrayList<>();
		ArrayList<Lightning.Arc> arcs = new ArrayList<>();
		Shocking.arc( attacker, defender, 1, chained, arcs );

		if (!chained.isEmpty()){
			attacker.sprite.parent.addToFront( new Lightning( arcs, null ) );
			for (Char hit : chained){
				//只影响与主目标敌对的存活单位，绝不劈射击者自身
				if (hit != attacker && hit != defender
						&& hit.isAlive()
						&& hit.alignment != Char.Alignment.ALLY
						&& hit.alignment != attacker.alignment){
					hit.damage( bolt, this );
				}
			}
		}
		return damage;
	}
}
