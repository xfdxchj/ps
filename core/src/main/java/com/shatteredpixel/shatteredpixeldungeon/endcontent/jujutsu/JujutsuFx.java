/*
 * 破碎的地牢 (End fork) — 咒术回战系列技能特效。
 *
 * 用原版已有的投射物（MagicMissile）、光束（Beam）、粒子与闪屏组合，
 * 比纯粒子爆发更接近"术式"的观感；不新增贴图也能用。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.utils.PointF;

public final class JujutsuFx {

	private JujutsuFx() {}

	public static void cellBurst(int cell, int speckType, int n){
		if (Dungeon.level == null || !Dungeon.level.insideMap(cell)) return;
		CellEmitter.get(cell).burst(Speck.factory(speckType), n);
	}

	private static Group groupOf(Char ch){
		if (ch == null || ch.sprite == null) return null;
		return ch.sprite.parent;
	}

	/** 蓝色光束（苍）。 */
	private static void beam(Char from, Char to, float r, float g, float b){
		Group parent = groupOf(from);
		if (parent == null || from == null || to == null) return;
		Beam.LightRay ray = new Beam.LightRay(world(from.pos), world(to.pos));
		ray.tint(r, g, b, 1f);
		parent.add(ray);
	}

	private static PointF world(int cell){
		return DungeonTilemap.tileToWorld(cell);
	}

	/** 苍：蓝球从敌人飞向玩家，到达后释放。 */
	public static void cang(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (m.sprite == null || m.sprite.parent == null){
			cellBurst(m.pos, Speck.BLUE_LIGHT, 8);
			return;
		}
		JujutsuBolt bolt = (JujutsuBolt) m.sprite.parent.recycle(JujutsuBolt.class);
		bolt.reset(m.pos, hero.pos, JujutsuBolt.CANG, () -> {
			if (hero.sprite != null && hero.sprite.parent != null){
				JujutsuRing ring = (JujutsuRing) hero.sprite.parent.recycle(JujutsuRing.class);
				ring.reset(hero.pos, JujutsuBolt.CANG);
			}
			cellBurst(hero.pos, Speck.BLUE_LIGHT, 10);
		});
	}

	/** 赫：红球从玩家飞向敌人，到达后冲击释放。 */
	public static void he(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (hero.sprite == null || hero.sprite.parent == null){
			cellBurst(m.pos, Speck.RED_LIGHT, 10);
			return;
		}
		JujutsuBolt bolt = (JujutsuBolt) hero.sprite.parent.recycle(JujutsuBolt.class);
		bolt.reset(hero.pos, m.pos, JujutsuBolt.HE, () -> {
			if (m.sprite != null && m.sprite.parent != null){
				JujutsuRing ring = (JujutsuRing) m.sprite.parent.recycle(JujutsuRing.class);
				ring.reset(m.pos, JujutsuBolt.HE);
			}
			cellBurst(m.pos, Speck.RED_LIGHT, 14);
			cellBurst(m.pos, Speck.WOOL, 8);
			GameScene.flash(0x40FF2200);
			if (com.watabou.noosa.Camera.main != null) com.watabou.noosa.Camera.main.shake(0.15f, 0.6f);
		});
	}

	/** 茈：小紫球飞向敌人，到达后空间撕裂。 */
	public static void zi(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (hero.sprite == null || hero.sprite.parent == null){
			cellBurst(m.pos, Speck.STAR, 12);
			return;
		}
		JujutsuBolt bolt = (JujutsuBolt) hero.sprite.parent.recycle(JujutsuBolt.class);
		bolt.reset(hero.pos, m.pos, JujutsuBolt.ZI, () -> {
			if (m.sprite != null && m.sprite.parent != null){
				JujutsuRing ring = (JujutsuRing) m.sprite.parent.recycle(JujutsuRing.class);
				ring.reset(m.pos, JujutsuBolt.ZI);
			}
			cellBurst(m.pos, Speck.STAR, 16);
			cellBurst(m.pos, Speck.SMOKE, 10);
			GameScene.flash(0x6030A060);
			if (com.watabou.noosa.Camera.main != null) com.watabou.noosa.Camera.main.shake(0.25f, 1.2f);
		});
	}

	/** 无量空处：星空白领域展开。 */
	public static void domain(Hero hero){
		if (hero == null) return;
		cellBurst(hero.pos, Speck.STAR, 28);
		cellBurst(hero.pos, Speck.LIGHT, 18);
		//从玩家向外拉几道白光
		for (Mob m : Jujutsu.visibleEnemies(hero)){
			beam(hero, m, 0.85f, 0.9f, 1f);
			cellBurst(m.pos, Speck.STAR, 6);
		}
		GameScene.flash(0x80FFFFFF);
	}

	/** 宿傩斩击：从宿傩射向目标的红色斩线。 */
	public static void slash(Char from, Char to, boolean big){
		if (from == null || to == null) return;
		beam(from, to, 1f, 0.2f, 0.2f);
		cellBurst(to.pos, big ? Speck.RED_LIGHT : Speck.LIGHT, big ? 16 : 8);
		if (big) GameScene.flash(0x40FF0000);
	}

	/** 伏魔御厨子：全屏血色。 */
	public static void shrine(){
		GameScene.flash(0x60FF0000);
	}

	/** 灶开：火焰喷射。 */
	public static void fuga(Char from, Char to){
		if (from != null && to != null){
			beam(from, to, 1f, 0.5f, 0.1f);
			cellBurst(to.pos, Speck.INFERNO, 20);
		}
		GameScene.flash(0x60FF4400);
	}
}
