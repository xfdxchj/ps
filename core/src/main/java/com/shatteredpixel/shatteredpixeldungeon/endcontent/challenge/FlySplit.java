/*
 * 破碎的地牢 (End fork) — 挑战「嗡嗡嗡嗡」：所有怪物获得苍蝇的分裂能力。
 *
 * 逻辑照原版 Swarm（苍蝇群）：受到伤害时，若当前生命足够（HP >= 伤害 + 2），
 * 且旁边有空格，就分裂出一只同类，两只生命对半。Boss / 小 Boss 不分裂，
 * 苍蝇本身不重复处理。分裂体不掉经验。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public final class FlySplit {

	private FlySplit() {}

	/** 挑战表位号（= 表 ID）。 */
	public static final int BUZZ = 239;

	public static boolean enabled(){
		return Dungeon.challengeMask != null && Dungeon.challengeMask.has(BUZZ);
	}

	/** 受伤时按苍蝇逻辑分裂。返回 true 表示发生了分裂。 */
	public static boolean trySplit(Mob mob, int dmg){
		if (!enabled() || mob == null || !mob.isAlive()) return false;
		if (dmg <= 0 || Dungeon.level == null) return false;
		//苍蝇本身已经会分裂，别叠
		if (mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm) return false;
		//Boss / 小 Boss 不分裂
		if (mob.properties().contains(Char.Property.BOSS)
				|| mob.properties().contains(Char.Property.MINIBOSS)) return false;

		if (mob.HP < dmg + 2) return false;

		ArrayList<Integer> candidates = new ArrayList<>();
		int w = Dungeon.level.width();
		int[] neighbours = {mob.pos + 1, mob.pos - 1, mob.pos + w, mob.pos - w};
		for (int n : neighbours){
			if (!Dungeon.level.insideMap(n)) continue;
			if (!Dungeon.level.solid[n]
					&& Actor.findChar(n) == null
					&& (Dungeon.level.passable[n] || Dungeon.level.avoid[n])
					&& (!mob.properties().contains(Char.Property.LARGE) || Dungeon.level.openSpace[n])){
				candidates.add(n);
			}
		}
		if (candidates.isEmpty()) return false;

		Mob clone;
		try {
			clone = Reflection.newInstance(mob.getClass());
		} catch (Throwable t){
			return false;
		}
		if (clone == null) return false;

		clone.pos = Random.element(candidates);
		clone.HP = (mob.HP - dmg) / 2;
		clone.HT = clone.HP;
		clone.EXP = 0; //分裂体不给经验，避免刷经验
		GameScene.add(clone, 1f);
		Actor.add(new Pushing(clone, mob.pos, clone.pos));
		Dungeon.level.occupyCell(clone);

		mob.HP -= clone.HP;
		return true;
	}
}
