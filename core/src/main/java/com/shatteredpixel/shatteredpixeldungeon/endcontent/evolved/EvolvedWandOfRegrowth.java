/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfRegrowth → 繁生法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：再生:取消次数限制、可长草。
 * 实现：完全重写 onZap，将"超限贫瘠(furrowedChance)"判定恒设为永不触发，
 *       并移除 totChrgUsed / chargesOverLimit 的累积，使法杖永远正常生成草与植物。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class EvolvedWandOfRegrowth extends WandOfRegrowth {

	//父类的 cone/target 为包私有，跨包不可见，这里自行维护一份
	private ConeAOE cone;
	private int target;

	@Override
	public String name() {
		return "繁生法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 10020979, 1.8f );
	}

	@Override
	public boolean tryToZap(Hero owner, int target) {
		if (super.tryToZap(owner, target)){
			this.target = target;
			return true;
		} else {
			return false;
		}
	}

	//END M2 真机制：取消次数限制（furrowedChance 恒为 0），永远正常长草 + 长植物
	@Override
	public void onZap(Ballistica bolt) {

		ArrayList<Integer> cells = new ArrayList<>(cone.cells);

		int chrgUsed = chargesPerCast();
		int grassToPlace = Math.round((3.67f+buffedLvl()/3f)*chrgUsed);

		//ignore cells which can't have anything grow in them.
		for (Iterator<Integer> i = cells.iterator(); i.hasNext();) {
			int cell = i.next();
			int terr = Dungeon.level.map[cell];
			if (!(terr == Terrain.EMPTY || terr == Terrain.EMBERS || terr == Terrain.EMPTY_DECO ||
					terr == Terrain.GRASS || terr == Terrain.HIGH_GRASS || terr == Terrain.FURROWED_GRASS)) {
				i.remove();
			} else if (Char.hasProp(Actor.findChar(cell), Char.Property.IMMOVABLE)) {
				i.remove();
			} else if (Dungeon.level.plants.get(cell) != null){
				i.remove();
			} else {
				if (terr != Terrain.HIGH_GRASS && terr != Terrain.FURROWED_GRASS) {
					Level.set(cell, Terrain.GRASS);
					GameScene.updateMap( cell );
				}
				Char ch = Actor.findChar(cell);
				if (ch != null){
					if (ch instanceof DwarfKing){
						Statistics.qualifiedForBossChallengeBadge = false;
					}
					wandProc(ch, chargesPerCast());
					Buff.prolong( ch, Roots.class, 4f * chrgUsed );
				}
			}
		}

		Random.shuffle(cells);

		//END fix: 父类(Lotus)的 setLevel 是 private,进化子类不同顶层类无法调用;Lotus 生成交给父类路径
		//(此处不直接调父私有成员)，因此这里不额外生成 Lotus——进化体现为“不限次长草/铺面积更大”。
		//(注: 若需保留 Lotus,应在父类内部开 setter 供子类调用,这是后续增强点。)

		//places grass along center of cone
		for (int cell : bolt.path){
			if (grassToPlace > 0 && cells.contains(cell)){
				Level.set(cell, Terrain.HIGH_GRASS);
				GameScene.updateMap( cell );
				grassToPlace--;
				//moves cell to the back
				cells.remove((Integer)cell);
				cells.add(cell);
			}
		}

		if (!cells.isEmpty() && (Random.Int(6) < chrgUsed)){ // 16%/33%/50% chance to spawn a seed pod or dewcatcher
			int cell = cells.remove(0);
			Dungeon.level.plant( Random.Int(2) == 0 ? new Seedpod.Seed() : new Dewcatcher.Seed(), cell);
		}

		if (!cells.isEmpty() && (Random.Int(3) < chrgUsed)){ // 33%/66%/100% chance to spawn a plant
			int cell = cells.remove(0);
			Dungeon.level.plant((Plant.Seed) Generator.randomUsingDefaults(Generator.Category.SEED), cell);
		}

		for (int cell : cells){
			if (grassToPlace <= 0 || bolt.path.contains(cell)) break;

			if (Dungeon.level.map[cell] == Terrain.HIGH_GRASS) continue;

			Level.set(cell, Terrain.HIGH_GRASS);
			GameScene.updateMap( cell );
			grassToPlace--;
		}

	}

	//原版 Lotus.setLevel 为 private，跨类无法调用，这里用反射等价实现其初始化
	private void setLotusLevel(Lotus l, int level){
		try {
			java.lang.reflect.Field wandLvl = Lotus.class.getDeclaredField("wandLvl");
			wandLvl.setAccessible(true);
			wandLvl.setInt(l, level);
		} catch (Exception e) {
			// 忽略：仅影响描述文字与 seedPreservation 系数
		}
		l.HP = l.HT = 25 + 3*level;
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {

		// 4/6/8 distance
		int maxDist = 2 + 2*chargesPerCast();

		cone = new ConeAOE( bolt,
				maxDist,
				20 + 10*chargesPerCast(),
				Ballistica.STOP_SOLID | Ballistica.STOP_TARGET);

		//cast to cells at the tip, rather than all cells, better performance.
		Ballistica longestRay = null;
		for (Ballistica ray : cone.outerRays){
			if (longestRay == null || ray.dist > longestRay.dist){
				longestRay = ray;
			}
			((MagicMissile)curUser.sprite.parent.recycle( MagicMissile.class )).reset(
					MagicMissile.FOLIAGE_CONE,
					curUser.sprite,
					ray.path.get(ray.dist),
					null
			);
		}

		//final zap at half distance of the longest ray, for timing of the actual wand effect
		MagicMissile.boltFromChar( curUser.sprite.parent,
				MagicMissile.FOLIAGE_CONE,
				curUser.sprite,
				longestRay.path.get(longestRay.dist/2),
				callback );
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}

	// ---- 终焉·进化基础(统一13把)：等级归零后强度锚定+8并成长+20%、最大充能20 ----
	@Override
	public int buffedLvl() {
		return 8 + Math.round(super.buffedLvl() * 1.2f);
	}

	@Override
	public void updateLevel() {
		maxCharges = 20;
		curCharges = Math.min(curCharges, maxCharges);
	}
}