package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * END(二.7 凝霜重做): 冰爆。
 *
 * <p>由冰天雪地气体每回合挂上。它自己负责：
 * 像原版冻气一样给目标挂寒冷、寒冷满时冻结，
 * 冻结结束后引爆，以及离开气体后到期消失。
 */
public class FrostBurst extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
		announced = false;
	}

	/** 引爆伤害（面板 400%，由法杖写入）。 */
	public int damage = 0;
	private boolean wasFrozen = false;
	/** 由气体每回合刷新的存续时间；离开气体后若未冻住则到期消失。 */
	private float left = 0f;
	public static final float DURATION = 2f;

	/** 气体每回合调用：写入伤害并刷新存续时间。 */
	public void set(int dmg){
		if (dmg > damage) damage = dmg;
		left = Math.max(left, DURATION);
	}

	@Override
	public boolean act(){
		if (!target.isAlive()){
			detach();
			return true;
		}

		//冻结刚消失 → 先引爆
		if (wasFrozen && target.buff(Frost.class) == null){
			if (damage > 0 && target.isAlive()){
				target.damage(damage, this);
			}
			detach();
			return true;
		}

		//像原版冻气一样：挂寒冷，寒冷堆满时冻结
		Freezing.freeze( target.pos );

		if (target.buff(Frost.class) != null){
			wasFrozen = true;
			spend(TICK);
			return true;
		}
		spend(TICK);
		left -= TICK;
		if (left <= 0f){
			detach();
		}
		return true;
	}

	@Override public int icon(){ return BuffIndicator.NONE; }

	private static final String DMG = "damage";
	private static final String FROZEN = "was_frozen";
	private static final String LEFT = "left";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(DMG, damage);
		bundle.put(FROZEN, wasFrozen);
		bundle.put(LEFT, left);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		damage = bundle.getInt(DMG);
		wasFrozen = bundle.getBoolean(FROZEN);
		left = bundle.getFloat(LEFT);
	}
}
