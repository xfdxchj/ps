/*
 * 破碎的地牢 (End fork) — 英烈地牢精英词条移植（常规 3 种 + 高阶 9 种）
 *
 * 源自 _extract/英烈地牢_反编译源码 的 ChampionEnemy 内部类，
 * 按本 fork 的 ChampionEnemy 钩子（meleeDamageFactor / damageTakenFactor /
 * evasionAndAccuracyFactor / act）重写为等价机制。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.champion;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public final class EndChampions {

	private EndChampions() {}

	/** 常规精英词条池（英烈新增 3 种 + 本体 6 种）。 */
	public static final Class<? extends ChampionEnemy>[] NORMAL_POOL = new Class[]{
			ChampionEnemy.Blazing.class, ChampionEnemy.Projecting.class,
			ChampionEnemy.AntiMagic.class, ChampionEnemy.Giant.class,
			ChampionEnemy.Blessed.class, ChampionEnemy.Growing.class,
			Assassin.class, Flowing.class, Stone.class,
	};

	/** 高阶精英词条池（英烈 9 种）。 */
	public static final Class<? extends ChampionEnemy>[] ELITE_POOL = new Class[]{
			Citadel.class, Infectious.class, Restoring.class, Sacrificial.class,
			Seeking.class, Summoning.class, Swarming.class, Timebending.class, Toxic.class,
	};

	public static Class<? extends ChampionEnemy> randomNormal(){
		return NORMAL_POOL[Random.Int(NORMAL_POOL.length)];
	}
	public static Class<? extends ChampionEnemy> randomElite(){
		return ELITE_POOL[Random.Int(ELITE_POOL.length)];
	}

	//====================== 常规：暗杀 / 涌流 / 磐岩 ======================

	/** 暗杀：第一次出手伤害极高。 */
	public static class Assassin extends ChampionEnemy {
		{ color = 0x2A2A2A; rays = 0; }
		private boolean first = true;
		@Override public float meleeDamageFactor(){ return first ? 2.5f : 1f; }
		@Override public void onAttackProc(Char enemy){ first = false; }
	}

	/** 涌流：命中与闪避更高。 */
	public static class Flowing extends ChampionEnemy {
		{ color = 0xB7F0FF; rays = 4; }
		@Override public float evasionAndAccuracyFactor(){ return 1.25f; }
	}

	/** 磐岩：血越少越硬。 */
	public static class Stone extends ChampionEnemy {
		{ color = 0x727272; rays = 0; }
		@Override public float damageTakenFactor(){
			if (target == null || target.HT <= 0) return 0.5f;
			float byHp = target.HP / (float) target.HT;
			float byDepth = 0.5f - (Dungeon.depth / 5) / 5f;
			return Math.max(0.1f, Math.max(byHp, Math.min(0.5f, byDepth)));
		}
	}

	//====================== 高阶：9 种 ======================

	/** 要塞：自身减伤。 */
	public static class Citadel extends ChampionEnemy {
		{ color = 0xFFF2AA; rays = 6; }
		@Override public float damageTakenFactor(){ return 0.6f; }
	}

	/** 瘟疫：把自身词条传染给视线内的同伴。 */
	public static class Infectious extends ChampionEnemy {
		{ color = 0x663300; rays = 3; }
		@Override public boolean act(){
			if (target == null || target.fieldOfView == null){ spend(TICK); return true; }
			for (Char ch : Actor.chars()){
				if (ch == target || !(ch instanceof Mob) || !ch.isAlive()) continue;
				if (ch.pos < 0 || ch.pos >= target.fieldOfView.length) continue;
				if (!target.fieldOfView[ch.pos]) continue;
				if (ch.buff(Infectious.class) == null) Buff.affect(ch, Infectious.class);
			}
			spend(TICK * 5f);
			return true;
		}
	}

	/** 回复：持续治疗视线内的同伴。 */
	public static class Restoring extends ChampionEnemy {
		{ color = 0xFFFFFF; rays = 5; }
		@Override public boolean act(){
			if (target == null || target.fieldOfView == null){ spend(TICK); return true; }
			for (Char ch : Actor.chars()){
				if (!(ch instanceof Mob) || !ch.isAlive()) continue;
				if (ch.pos < 0 || ch.pos >= target.fieldOfView.length) continue;
				if (!target.fieldOfView[ch.pos]) continue;
				ch.HP = Math.min(ch.HT, ch.HP + Math.max(1, ch.HT / 20));
			}
			spend(TICK * 5f);
			return true;
		}
	}

	/** 献祭：高减伤。 */
	public static class Sacrificial extends ChampionEnemy {
		{ color = 0xFCC000; rays = 7; }
		@Override public float damageTakenFactor(){ return 0.5f; }
	}

	/** 侦察：把玩家的位置喊给视野内的同伴。 */
	public static class Seeking extends ChampionEnemy {
		{ color = 0x28FFFF; rays = 4; }
		@Override public boolean act(){
			if (target instanceof Mob && Dungeon.hero != null && Dungeon.hero.isAlive()){
				for (Char ch : Actor.chars()){
					if (!(ch instanceof Mob) || !ch.isAlive()) continue;
					if (ch.fieldOfView != null && Dungeon.hero.pos < ch.fieldOfView.length
							&& ch.fieldOfView[Dungeon.hero.pos]){
						((Mob) ch).beckon(Dungeon.hero.pos);
					}
				}
			}
			spend(TICK * 20f);
			return true;
		}
	}

	/** 召唤：定期召唤随从。 */
	public static class Summoning extends ChampionEnemy {
		{ color = 0x4B0082; rays = 6; }
		@Override public boolean act(){
			spend(TICK * 20f);
			if (Dungeon.level != null && target != null){
				try {
					int cell = target.pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
					if (cell >= 0 && cell < Dungeon.level.length()
							&& !Dungeon.level.solid[cell]
							&& Actor.findChar(cell) == null){
						Mob m = com.watabou.utils.Reflection.newInstance(
								com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat.class);
						m.pos = cell;
						GameScene.add(m);
					}
				} catch (Throwable ignored) { }
			}
			return true;
		}
	}

	/** 分裂：入场时分裂出 2 只同类（只分裂一次）。 */
	public static class Swarming extends ChampionEnemy {
		{ color = 0xC0FFEE; rays = 5; }
		private boolean split = false;
		@Override public boolean act(){
			spend(TICK);
			if (!split && target instanceof Mob && Dungeon.level != null){
				split = true;
				try {
					for (int i = 0; i < 2; i++){
						int cell = target.pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
						if (cell < 0 || cell >= Dungeon.level.length()) continue;
						if (Dungeon.level.solid[cell] || Actor.findChar(cell) != null) continue;
						Mob m = (Mob) com.watabou.utils.Reflection.newInstance(target.getClass());
						m.pos = cell;
						GameScene.add(m);
					}
				} catch (Throwable ignored) { }
			}
			return true;
		}
	}

	/** 缓时：让视线内的玩家变慢。 */
	public static class Timebending extends ChampionEnemy {
		{ color = 0x0087FF; rays = 6; }
		@Override public boolean act(){
			if (Dungeon.hero != null && Dungeon.hero.isAlive()
					&& target != null && target.fieldOfView != null
					&& Dungeon.hero.pos < target.fieldOfView.length
					&& target.fieldOfView[Dungeon.hero.pos]){
				Buff.prolong(Dungeon.hero, Slow.class, 2f);
			}
			spend(TICK * 10f);
			return true;
		}
	}

	/** 剧毒：在场时持续吐出毒气。 */
	public static class Toxic extends ChampionEnemy {
		{ color = 0x808000; rays = 4; }
		@Override public boolean act(){
			if (Dungeon.level != null && target != null){
				try {
					for (int i : PathFinder.NEIGHBOURS9){
						int cell = target.pos + i;
						if (cell < 0 || cell >= Dungeon.level.length()) continue;
						GameScene.add(Blob.seed(cell, 3, ToxicGas.class));
					}
				} catch (Throwable ignored) { }
			}
			spend(TICK * 10f);
			return true;
		}
	}
}
