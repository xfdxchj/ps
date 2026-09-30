package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * END(二.7 凝霜重做): 冰爆。
 *
 * <p>由冰雪风暴施加。目标被冻结(Frost)后，冻结一旦消失就引爆，
 * 造成施法者预先写入的伤害（面板 400%）。
 */
public class FrostBurst extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
		announced = false;
	}

	/** 引爆伤害（面板 400%，由法杖写入）。 */
	public int damage = 0;
	private boolean wasFrozen = false;

	@Override
	public boolean act(){
		if (target.buff(Frost.class) != null){
			wasFrozen = true;
		} else if (wasFrozen){
			if (damage > 0 && target.isAlive()){
				target.damage(damage, this);
			}
			detach();
			return true;
		}
		spend(TICK);
		return true;
	}

	@Override public int icon(){ return BuffIndicator.NONE; }

	private static final String DMG = "damage";
	private static final String FROZEN = "was_frozen";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(DMG, damage);
		bundle.put(FROZEN, wasFrozen);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		damage = bundle.getInt(DMG);
		wasFrozen = bundle.getBoolean(FROZEN);
	}
}
