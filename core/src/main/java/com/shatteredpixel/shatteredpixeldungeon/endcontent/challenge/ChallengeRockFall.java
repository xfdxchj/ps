/*
 * 破碎的地牢 (End fork) — 挑战 76「原始状态」的投石
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DelayedRockFall;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * END(挑战 76 原始状态): 非远程怪物扔出的石头。
 *
 * <h3>原表效果</h3>
 * "非远程怪物可扔石头进行远程攻击"
 *
 * <h3>实现</h3>
 * 复用本 fork 已有的 {@link DelayedRockFall}（豺狼投石用的那套）——
 * 它是"延迟一回合落石"的效果，正好表现"怪物举起石头砸过来"。
 *
 * <p>与 {@code GnollRockFall} 的区别：
 * <ul>
 *   <li>伤害按**投掷者**的属性算，而不是写死 6-12</li>
 *   <li>不附带麻痹（原表只说"远程攻击"，没说控制）</li>
 *   <li>不破坏地形（{@code affectCell} 留空）</li>
 * </ul>
 */
public class ChallengeRockFall extends DelayedRockFall {

	/** 投掷者的伤害下限（由调用方设置）。 */
	private int dmgMin = 1;
	/** 投掷者的伤害上限。 */
	private int dmgMax = 3;

	/** END(76): 设置伤害范围（由发起投石的怪物决定）。 */
	public void setDamage(int min, int max) {
		this.dmgMin = Math.max(1, min);
		this.dmgMax = Math.max(this.dmgMin, max);
	}

	@Override
	public void affectChar(Char ch) {
		ch.damage(Random.NormalIntRange(dmgMin, dmgMax), this);
	}

	/** 不破坏地形 —— 原表只说"远程攻击"，砸出坑是额外的。 */
	@Override
	public void affectCell(int cell) {
		//留空
	}

	//==================================================================
	//发起投石
	//==================================================================

	/**
	 * END(76): 让某只怪物朝目标扔一块石头。
	 *
	 * <p>调用点：{@code Mob.act()} —— 怪物无法近身时改用投石。
	 *
	 * @param attacker 投掷者
	 * @param target   目标
	 * @return true 表示确实发起了投石
	 */
	public static boolean throwRockAt(Char attacker, Char target) {
		if (attacker == null || target == null) return false;
		if (Dungeon.level == null) return false;
		if (!attacker.isAlive() || !target.isAlive()) return false;

		//落点取目标格；如果目标不在，就取它旁边一格
		List<Integer> cells = new ArrayList<>();
		cells.add(target.pos);
		if (Random.Int(3) == 0 && !Dungeon.level.solid[target.pos]) {
			//偶尔偏一格（不是必中），给玩家闪避的余地
			int[] n = com.watabou.utils.PathFinder.NEIGHBOURS8;
			int alt = target.pos + n[Random.Int(n.length)];
			if (alt >= 0 && alt < Dungeon.level.length() && !Dungeon.level.solid[alt]) {
				cells.clear();
				cells.add(alt);
			}
		}

		try {
			ChallengeRockFall fall = new ChallengeRockFall();
			fall.setRockPositions(cells);
			fall.setDamage(attacker.damageRoll() / 2, attacker.damageRoll());

			//直接 attach 我们自己的实例 ——
			//**不能用 Buff.affect()**：那会新建一个实例，我们设好的落点与伤害就丢了。
			fall.attachTo(attacker);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}
}
