package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * END(二.7 凝霜重做): 冰爆。
 *
 * <p>由冰雪风暴气体**每回合挂上**（像酸蚀气体挂 Corrosion 一样）。
 * 它自己负责：施加寒冷、概率冻结、冻结结束后引爆，
 * 以及离开气体后到期消失（冻住期间不倒数）。
 */
public class FrostBurst extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
		announced = false;
	}

	/** 引爆伤害（面板 400%，由法杖写入）。 */
	public int damage = 0;
	/** 气体写入的寒冷持续时间。 */
	private float chillDuration = 2f;
	private boolean wasFrozen = false;
	/** 由气体每回合刷新的存续时间；离开气体后若未冻住则到期消失。 */
	private float left = 0f;
	/** 离开气体后 buff 还能保留几回合（与酸蚀气体的 2 回合一致）。 */
	public static final float DURATION = 2f;
	/** 已在寒冷中的敌人每回合被冻住的概率(%)。 */
	private static final int FREEZE_PCT = 40;
	/** 冻住持续回合数。 */
	private static final float FREEZE_TURNS = 2f;

	/** 气体每回合调用：写入伤害/寒冷并刷新存续时间。 */
	public void set(int dmg, float chillDur){
		if (dmg > damage) damage = dmg;
		if (chillDur > chillDuration) chillDuration = chillDur;
		left = Math.max(left, DURATION);
	}

	@Override
	public boolean act(){
		if (!target.isAlive()){
			detach();
			return true;
		}

		//冻结刚消失 → 先引爆，避免又被概率重新冻住导致永远不爆
		if (wasFrozen && target.buff(Frost.class) == null){
			if (damage > 0 && target.isAlive()){
				target.damage(damage, this);
			}
			detach();
			return true;
		}

		//像气体一样，每回合给目标施加寒冷
		if (!target.isImmune(Chill.class)){
			Buff.prolong(target, Chill.class, chillDuration);
		}

		//已在寒冷中的敌人有概率被冻住 → 冻结消失时触发冰爆
		if (!target.isImmune(Frost.class)
				&& target.buff(Frost.class) == null
				&& target.buff(Chill.class) != null
				&& Random.Int(100) < FREEZE_PCT){
			Buff.affect(target, Frost.class, FREEZE_TURNS);
		}

		if (target.buff(Frost.class) != null){
			//冻住期间不倒数，保证冻结结束时一定引爆
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
	private static final String CHILL = "chill_duration";
	private static final String FROZEN = "was_frozen";
	private static final String LEFT = "left";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(DMG, damage);
		bundle.put(CHILL, chillDuration);
		bundle.put(FROZEN, wasFrozen);
		bundle.put(LEFT, left);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		damage = bundle.getInt(DMG);
		chillDuration = bundle.getFloat(CHILL);
		wasFrozen = bundle.getBoolean(FROZEN);
		left = bundle.getFloat(LEFT);
	}
}
