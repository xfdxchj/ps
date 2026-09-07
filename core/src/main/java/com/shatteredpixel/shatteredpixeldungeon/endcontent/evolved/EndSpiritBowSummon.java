package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * END 灵能弓· 成品 ③「唤魔」。
 *
 * 当这一发真正击杀敌人时，掷 10%：从目标身旁召出一名随机元素盟友(火/霜/雷)，
 * 并把其生命压到「该元素在盟友缩放后的自身最大生命」的约 30%。
 *
 * 引擎要点：
 *  - Weapon.proc 先于 Char.attack 期末把物理伤害真正扣到 HP；proc 内不能同步判定“已死”。
 *    故仿 Grim / Hero 神弓的自然之怒：排队一个 VFX_PRIO 的 Actor，延迟一帧判 !isAlive()。
 *  - Elemental.setSummonedALly() 会让子类以盟友身份重算 HT；生命须在它之后设置，
 *    否则会被覆盖（此仅约缩现能力正确的一档）。
 */
public class EndSpiritBowSummon extends SpiritBow {

	private static final Class[] summonKinds = new Class[]{
			Elemental.FireElemental.class,
			Elemental.FrostElemental.class,
			Elemental.ShockElemental.class
	};

	@Override public String name() { return "唤魔灵弓"; }

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);
		if (defender == null || !defender.isAlive() || defender == attacker) return damage;

		final Char victim = defender;

		//延迟一帧判亡后掷 10% 召唤（对齐手感的自然延迟；VFX_PRIO 优先结算）
		Actor.add(new Actor() {
			{
				actPriority = VFX_PRIO;
			}
			@Override
			protected boolean act() {
				if (!victim.isAlive() && Random.Float() < 0.10f){
					spawnAllyAt( victim.pos );
				}
				Actor.remove(this);
				return true;
			}
		});

		return damage;
	}

	private void spawnAllyAt( int atPos ){
		Elemental element = (Elemental) Reflection.newInstance( Random.element( summonKinds ) );
		if (element == null) return;

		int spawn = nearFreeCell( atPos );
		GameScene.add( element );
		element.setSummonedALly();
		//先让它以“盟友缩放后”的满血实体在场,再把存活压到其自身最大生命 ~30%
		element.HP = element.HT;
		ScrollOfTeleportation.appear( element, spawn );
		element.HP = Math.max( 1, Math.round( element.HT * 0.30f ) );
	}

	/** 在 atPos 附近挑一个可站且无人占用的格子放元素。 */
	private int nearFreeCell( int atPos ){
		ArrayList<Integer> cands = new ArrayList<>();
		if (freeToDrop( atPos )) cands.add( atPos );
		for (int n : PathFinder.NEIGHBOURS8){
			int c = atPos + n;
			if (freeToDrop( c )) cands.add( c );
		}
		return cands.isEmpty() ? atPos : Random.element( cands );
	}

	private boolean freeToDrop( int c ){
		return Dungeon.level.insideMap( c )
				&& !Dungeon.level.solid[c]
				&& Dungeon.level.passable[c]
				&& Actor.findChar( c ) == null;
	}
}
