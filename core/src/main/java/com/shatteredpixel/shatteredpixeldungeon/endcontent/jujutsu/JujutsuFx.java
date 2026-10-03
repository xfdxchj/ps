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
import com.watabou.utils.Random;

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

	/** 苍：蓝球从敌人飞向玩家，到达后蓝爆。 */
	public static void cang(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (m.sprite == null || m.sprite.parent == null){
			cellBurst(m.pos, Speck.BLUE_LIGHT, 8);
			return;
		}
		JujutsuBolt bolt = (JujutsuBolt) m.sprite.parent.recycle(JujutsuBolt.class);
		bolt.reset(m.pos, hero.pos, JujutsuBolt.CANG, () -> {
			burst(hero, hero.pos, JujutsuBolt.CANG);
		});
	}

	/** 赫：红球从玩家飞向敌人，到达后红爆 + 震屏。 */
	public static void he(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (hero.sprite == null || hero.sprite.parent == null){
			cellBurst(m.pos, Speck.RED_LIGHT, 10);
			return;
		}
		JujutsuBolt bolt = (JujutsuBolt) hero.sprite.parent.recycle(JujutsuBolt.class);
		bolt.reset(hero.pos, m.pos, JujutsuBolt.HE, () -> {
			burst(m, m.pos, JujutsuBolt.HE);
			GameScene.flash(0x40FF2200);
			if (com.watabou.noosa.Camera.main != null) com.watabou.noosa.Camera.main.shake(0.15f, 0.6f);
		});
	}

	/** 茈：苍球与赫球在目标处重叠 -> 直接大爆炸，没有小紫球。 */
	public static void zi(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (hero.sprite == null || hero.sprite.parent == null){
			burst(m, m.pos, 2);
			return;
		}
		final int[] arrived = {0};
		final Runnable explosion = () -> {
			if (++arrived[0] < 2) return;
			//END(范围): 单团紫色大爆炸，靠尺寸覆盖范围，而不是多团叠加
			burst(m, m.pos, 2);
			GameScene.flash(0xC080D0FF);
			if (com.watabou.noosa.Camera.main != null) com.watabou.noosa.Camera.main.shake(0.5f, 3.0f);
		};

		// 苍球：从玩家飞向目标
		JujutsuBolt blue = (JujutsuBolt) hero.sprite.parent.recycle(JujutsuBolt.class);
		blue.reset(hero.pos, m.pos, JujutsuBolt.CANG, () -> explosion.run());

		// 赫球：从目标另一侧飞来，与苍球在目标点重叠
		int w = Dungeon.level.width();
		int dx = Integer.signum((m.pos % w) - (hero.pos % w));
		int dy = Integer.signum((m.pos / w) - (hero.pos / w));
		int from = m.pos;
		for (int i = 0; i < 3; i++){
			int c = from + dx + dy * w;
			if (!Dungeon.level.insideMap(c)) break;
			from = c;
		}
		if (from == m.pos) from = hero.pos;
		JujutsuBolt red = (JujutsuBolt) hero.sprite.parent.recycle(JujutsuBolt.class);
		red.reset(from, m.pos, JujutsuBolt.HE, () -> explosion.run());
	}

	/** 在角色所在格生成对应颜色的爆炸。 */
	private static void burst(Char at, int cell, int type){
		if (at != null && at.sprite != null && at.sprite.parent != null){
			JujutsuBurst b = (JujutsuBurst) at.sprite.parent.recycle(JujutsuBurst.class);
			b.reset(cell, type);
		} else if (Dungeon.hero != null && Dungeon.hero.sprite != null
				&& Dungeon.hero.sprite.parent != null){
			JujutsuBurst b = (JujutsuBurst) Dungeon.hero.sprite.parent.recycle(JujutsuBurst.class);
			b.reset(cell, type);
		}
		//END(按早期预览的粒子爆发风格): 大量星点/光点
		if (type == JujutsuBolt.CANG){
			cellBurst(cell, Speck.BLUE_LIGHT, 24);
			cellBurst(cell, Speck.STAR, 16);
			cellBurst(cell, Speck.LIGHT, 12);
		} else if (type == JujutsuBolt.HE){
			cellBurst(cell, Speck.RED_LIGHT, 24);
			cellBurst(cell, Speck.WOOL, 14);
			cellBurst(cell, Speck.STAR, 12);
		} else {
			cellBurst(cell, Speck.STAR, 28);
			cellBurst(cell, Speck.SMOKE, 18);
			cellBurst(cell, Speck.LIGHT, 14);
		}
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

	/** 空间斩：一道刀光从宿傩飞向目标。 */
	public static void spaceSlash(Char from, Char to){
		if (from == null || to == null) return;
		if (from.sprite != null && from.sprite.parent != null){
			JujutsuSlash sl = (JujutsuSlash) from.sprite.parent.recycle(JujutsuSlash.class);
			sl.reset(from.pos, to.pos, () -> {
				cellBurst(to.pos, Speck.LIGHT, 16);
				GameScene.flash(0x80FFFFFF);
				if (com.watabou.noosa.Camera.main != null) com.watabou.noosa.Camera.main.shake(0.25f, 1.5f);
			});
		} else {
			cellBurst(to.pos, Speck.LIGHT, 12);
		}
	}

	/** 伏魔御厨子：全屏血色。 */
	public static void shrine(){
		GameScene.flash(0x60FF0000);
	}

	/** 灶开：一支火焰箭飞向目标，命中后蓝白爆开。 */
	public static void fuga(Char from, Char to){
		if (from == null || to == null) return;
		if (from.sprite != null && from.sprite.parent != null){
			JujutsuArrow arrow = (JujutsuArrow) from.sprite.parent.recycle(JujutsuArrow.class);
			arrow.reset(from.pos, to.pos, () -> {
				cellBurst(to.pos, Speck.BLUE_LIGHT, 26);
				cellBurst(to.pos, Speck.STAR, 14);
				GameScene.flash(0x80AAD4FF);
			});
		} else {
			cellBurst(to.pos, Speck.BLUE_LIGHT, 20);
		}
	}
}
