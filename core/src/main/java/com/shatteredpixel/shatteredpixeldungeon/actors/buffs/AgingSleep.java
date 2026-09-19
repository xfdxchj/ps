/*
 * 破碎的地牢 (End fork) — 挑战 18「老龄化」的困倦状态
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/**
 * END(挑战 18 老龄化): 困倦 —— 睡觉，但只有几回合。
 *
 * <h3>为什么不用原版的 Drowsy</h3>
 * {@code Drowsy.act()} 里有一句 {@code Buff.affect(target, MagicalSleep.class)} ——
 * 它是"入睡前奏"，到时间会**真正进入魔法睡眠**，然后一直睡到被打醒。
 *
 * <p>而原表 18 要的是"睡眠 2 回合"。用 Drowsy 的后果是：
 * 怪物挂上 Drowsy → 1 回合后变成 MagicalSleep → **永久睡着**，
 * 玩家实测就是"睡眠变成了魔法睡眠"。
 *
 * <h3>做法</h3>
 * 自己做一个"真睡眠但会自然醒"的状态：
 * <ul>
 *   <li>{@code attachTo} 里把怪物设成 {@code SLEEPING}（真睡眠，外观是 Zzz）</li>
 *   <li>{@code act()} 计数，到时间**主动唤醒**并 detach</li>
 *   <li>**绝不挂 MagicalSleep** —— 醒来就是醒来</li>
 * </ul>
 *
 * <p>被攻击时靠 {@code Mob.damage()} 的原有逻辑叫醒（那条路径不需要本类处理）。
 */
public class AgingSleep extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
		announced = false;      //不弹提示，怪物睡着了自己看得出来
	}

	/** 睡眠回合数。 */
	public static final float DURATION = 2f;

	@Override
	public boolean attachTo(Char target) {
		if (!(target instanceof Mob)) return false;
		if (target.isImmune(Sleep.class)) return false;
		if (!super.attachTo(target)) return false;

		Mob m = (Mob) target;

		//已经是睡眠状态就不重复设置（避免打断已有睡眠的计时）
		if (m.state != m.SLEEPING) {
			m.state = m.SLEEPING;
			//让它的精灵播放"睡着"的表现（原版 Mob 里有这个钩子）
			if (Dungeon.level != null && Dungeon.level.heroFOV[m.pos]) {
				if (m.sprite != null) {
					m.sprite.idle();
				}
			}
		}
		return true;
	}

	/**
	 * END(18): 计时并自然醒。
	 *
	 * <p>{@code FlavourBuff} 的时长会自动递减，归零时自动 detach；
	 * 这里额外负责"把怪物从 SLEEPING 状态里放出来"。
	 */
	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	/** 醒来时把状态还原 —— 这是与 Drowsy 的关键区别。 */
	@Override
	public void detach() {
		Char t = target;
		super.detach();

		if (t instanceof Mob) {
			Mob m = (Mob) t;
			//只在自己确实把它弄睡的情况下才唤醒，
			//避免覆盖其它睡眠来源（比如玩家用睡眠卷轴）
			if (m.state == m.SLEEPING && m.buff(MagicalSleep.class) == null
					&& m.buff(AgingSleep.class) == null) {
				m.state = m.WANDERING;
			}
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;   //不给怪物显示 buff 图标
	}
}
