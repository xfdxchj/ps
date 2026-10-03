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

	/** 领域核心：不可移动、不主动追击，只等玩家来拆。 */
	public void makeCore(){
		properties.add(Char.Property.IMMOVABLE);
		state = PASSIVE;
		HP = HT = 80;
	}

	@Override
	public int attackProc(Char enemy, int damage){
		//式神：命中有 20% 概率点燃
		if (!isCore && Random.Int(5) == 0){
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
					.affect(enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.Burning.class).reignite(enemy, 2f);
		}
		return super.attackProc(enemy, damage);
	}

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
