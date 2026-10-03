/*
 * 破碎的地牢 (End fork) — 宿傩的式神 / 伏魔御厨子领域核心。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.watabou.utils.Random;

public class SukunaMinion extends Mob {

	public boolean isCore = false;

	{
		spriteClass = SkeletonSprite.class;
		HP = HT = 60;
		EXP = 0;
		defenseSkill = 18;
		state = HUNTING;
	}

	@Override public int damageRoll(){ return Random.NormalIntRange(10, 16); }
	@Override public int attackSkill(Char target){ return 28; }
	@Override public int drRoll(){ return 0; }

	@Override
	public void die(Object cause){
		if (isCore){
			Sukuna boss = null;
			for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
				if (m instanceof Sukuna){ boss = (Sukuna) m; break; }
			}
			if (boss != null) boss.onCoreKilled();
		}
		super.die(cause);
	}
}
