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
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public final class JujutsuFx {

	private JujutsuFx() {}

	/** 领域展开的粒子/斩击数量倍率（用户反馈太少，×50）。 */
	public static final int FX_MULT = 50;

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

	/** 苍：蓝球飞向指定位置，到达后回调（拉怪+伤害）。 */
	public static void cang(Hero hero, int cell, Callback onArrive){
		if (hero == null) return;
		if (hero.sprite == null || hero.sprite.parent == null){
			cellBurst(cell, Speck.BLUE_LIGHT, 10);
			if (onArrive != null) onArrive.call();
			return;
		}
		JujutsuBolt bolt = new JujutsuBolt();
		bolt.reset(hero.pos, cell, JujutsuBolt.CANG, true, () -> {
			burst(hero, cell, JujutsuBolt.CANG);
			if (onArrive != null) onArrive.call();
		});
		GameScene.effect(bolt);
	}

	/** 赫：红球飞向指定位置，到达后回调（推怪+伤害）。 */
	public static void he(Hero hero, int cell, Callback onArrive){
		if (hero == null) return;
		if (hero.sprite == null || hero.sprite.parent == null){
			cellBurst(cell, Speck.RED_LIGHT, 10);
			if (onArrive != null) onArrive.call();
			return;
		}
		JujutsuBolt bolt = new JujutsuBolt();
		bolt.reset(hero.pos, cell, JujutsuBolt.HE, true, () -> {
			burst(hero, cell, JujutsuBolt.HE);
			GameScene.flash(0x40FF2200);
			if (com.watabou.noosa.Camera.main != null) com.watabou.noosa.Camera.main.shake(0.15f, 0.6f);
			if (onArrive != null) onArrive.call();
		});
		GameScene.effect(bolt);
	}

	/** 茈：苍赫落点重叠时的紫色大爆炸。 */
	public static void ziAt(int cell){
		burst(Dungeon.hero, cell, 2);
		GameScene.flash(0xC080D0FF);
		if (com.watabou.noosa.Camera.main != null) com.watabou.noosa.Camera.main.shake(0.5f, 3.0f);
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
		cellBurst(hero.pos, Speck.STAR, 28 * FX_MULT);
		cellBurst(hero.pos, Speck.LIGHT, 18 * FX_MULT);
		//从玩家向外拉几道白光
		for (Mob m : Jujutsu.visibleEnemies(hero)){
			beam(hero, m, 0.85f, 0.9f, 1f);
			cellBurst(m.pos, Speck.STAR, 6 * FX_MULT);
		}
		GameScene.flash(0x80FFFFFF);
	}

	/** 宿傩斩击：从宿傩射向目标的红色斩线。 */
	public static void slash(Char from, Char to, boolean big){
		if (from == null || to == null) return;
		//END(修复·红色激光): 不再画红色光束，改用刺客斩击特效
		//END(×50): 斩击数量放大
		for (int i = 0; i < FX_MULT; i++){
			try {
				com.shatteredpixel.shatteredpixeldungeon.effects.Wound.hit(to, Random.Float()*360f);
			} catch (Throwable ignored) {}
		}
		cellBurst(to.pos, big ? Speck.RED_LIGHT : Speck.LIGHT, (big ? 16 : 8) * FX_MULT);
		if (big) GameScene.flash(0x40FF0000);
	}

	/** 空间斩：一道刀光从宿傩飞向目标。 */
	public static void spaceSlash(Char from, Char to){
		if (from == null || to == null) return;
		if (from.sprite != null && from.sprite.parent != null){
			JujutsuSlash sl = (JujutsuSlash) from.sprite.parent.recycle(JujutsuSlash.class);
			sl.reset(from.pos, to.pos, () -> {
				cellBurst(to.pos, Speck.LIGHT, 16 * FX_MULT);
				GameScene.flash(0x80FFFFFF);
				if (com.watabou.noosa.Camera.main != null) com.watabou.noosa.Camera.main.shake(0.25f, 1.5f);
			});
		} else {
			cellBurst(to.pos, Speck.LIGHT, 12 * FX_MULT);
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
				cellBurst(to.pos, Speck.BLUE_LIGHT, 26 * FX_MULT);
				cellBurst(to.pos, Speck.STAR, 14 * FX_MULT);
				GameScene.flash(0x80AAD4FF);
			});
		} else {
			cellBurst(to.pos, Speck.BLUE_LIGHT, 20 * FX_MULT);
		}
	}
}
