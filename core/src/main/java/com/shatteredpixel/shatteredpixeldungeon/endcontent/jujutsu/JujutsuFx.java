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

	/** 苍：从每个敌人射向玩家的蓝色引力弹 + 目标处内旋蓝光。 */
	public static void cang(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (m.sprite != null && m.sprite.parent != null){
			MagicMissile.boltFromChar(m.sprite.parent, MagicMissile.SHAMAN_BLUE,
					m.sprite, hero.pos, null);
		}
		beam(m, hero, 0.45f, 0.7f, 1f);
		cellBurst(m.pos, Speck.BLUE_LIGHT, 8);
	}

	/** 赫：从玩家射向每个敌人的红色斥力弹 + 冲击波。 */
	public static void he(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (hero.sprite != null && hero.sprite.parent != null){
			MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.SHAMAN_RED,
					hero.sprite, m.pos, null);
		}
		beam(hero, m, 1f, 0.35f, 0.25f);
		cellBurst(m.pos, Speck.RED_LIGHT, 10);
		cellBurst(m.pos, Speck.WOOL, 6);
	}

	/** 茈：紫黑吞噬 + 空间撕裂。 */
	public static void zi(Hero hero, Mob m){
		if (hero == null || m == null) return;
		if (m.sprite != null && m.sprite.parent != null){
			MagicMissile.boltFromChar(m.sprite.parent, MagicMissile.SHADOW,
					m.sprite, hero.pos, null);
		}
		beam(hero, m, 0.75f, 0.35f, 1f);
		cellBurst(m.pos, Speck.STAR, 12);
		cellBurst(m.pos, Speck.SMOKE, 8);
		GameScene.flash(0x6030A060);
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
