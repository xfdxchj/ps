package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

/**
 * END(二.7 凝霜重做): 冰爆。
 *
 * <p>由冰天雪地气体每回合挂上。它自己负责：
 * 像原版冰霜一样按法杖等级挂寒冷，寒冷满时冻结；
 * 冻结结束后冰爆造成 150% 面板伤害，并对周围 3×3 造成 50% 伤害。
 */
public class FrostBurst extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
		announced = false;
		//END: 比 Frost 晚一步行动，冻结在同一回合解除时能立刻引爆
		actPriority = BUFF_PRIO - 1;
	}

	/** 冰爆主伤害（面板 150%，由法杖写入）。 */
	public int damage = 0;
	/** 周围 3×3 溅射伤害（面板 50%，由法杖写入）。 */
	public int splashDamage = 0;
	/** 寒冷持续时间 = 2 + 法杖等级；水中再 +2。 */
	private float chillDuration = 2f;
	private boolean wasFrozen = false;
	/** 由气体每回合刷新的存续时间；离开气体后若未冻住则到期消失。 */
	private float left = 0f;
	public static final float DURATION = 2f;

	/** 气体每回合调用：写入伤害、寒冷并刷新存续时间。 */
	public void set(int dmg, int splash, float chillDur){
		if (dmg > damage) damage = dmg;
		if (splash > splashDamage) splashDamage = splash;
		if (chillDur > chillDuration) chillDuration = chillDur;
		left = Math.max(left, DURATION);
	}

	@Override
	public boolean act(){
		if (!target.isAlive()){
			detach();
			return true;
		}

		//冻结刚消失 -> 引爆：主目标 150%，周围 3×3 各 50%
		if (wasFrozen && target.buff(Frost.class) == null){
			if (damage > 0 && target.isAlive()){
				target.damage(damage, this);
			}
			if (splashDamage > 0){
				for (int i : PathFinder.NEIGHBOURS9){
					if (i == 0) continue;
					int cell = target.pos + i;
					if (!Dungeon.level.insideMap(cell)) continue;
					Char ch = Actor.findChar(cell);
					if (ch != null && ch != target && ch.isAlive()
							&& ch.alignment == target.alignment){
						ch.damage(splashDamage, this);
					}
				}
			}
			detach();
			return true;
		}

		//已经冻住：不再重复挂寒冷/冻结，避免无限刷新
		if (target.buff(Frost.class) != null){
			wasFrozen = true;
			spend(TICK);
			return true;
		}

		//像原版冰霜一样挂寒冷，时长随法杖等级提升
		if (!target.isImmune(Chill.class)){
			float dur = chillDuration + (Dungeon.level.water[target.pos] ? FrostBalance.CHILL_WATER_BONUS : 0f);
			Chill chill = target.buff(Chill.class);
			if (chill == null){
				Buff.affect(target, Chill.class, dur);
			} else {
				float cap = Chill.DURATION - chill.cooldown();
				cap /= target.resist(Chill.class);
				if (cap > 0f){
					Buff.affect(target, Chill.class, Math.min(dur, cap));
				}
			}
		}

		//寒冷堆满 -> 冻结
		Chill chill = target.buff(Chill.class);
		if (chill != null && chill.cooldown() >= Chill.DURATION
				&& !target.isImmune(Frost.class)){
			Buff.affect(target, Frost.class, Frost.DURATION);
		}

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
	private static final String SPLASH = "splash_damage";
	private static final String CHILL = "chill_duration";
	private static final String FROZEN = "was_frozen";
	private static final String LEFT = "left";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(DMG, damage);
		bundle.put(SPLASH, splashDamage);
		bundle.put(CHILL, chillDuration);
		bundle.put(FROZEN, wasFrozen);
		bundle.put(LEFT, left);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		damage = bundle.getInt(DMG);
		splashDamage = bundle.getInt(SPLASH);
		chillDuration = bundle.getFloat(CHILL);
		wasFrozen = bundle.getBoolean(FROZEN);
		left = bundle.getFloat(LEFT);
	}
}
