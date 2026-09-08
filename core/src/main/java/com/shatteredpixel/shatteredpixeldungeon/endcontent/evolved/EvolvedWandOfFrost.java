/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 凝霜法杖(冰霜进化) 双形态：
 *   形态 0·冰霜直击(default)：耗 1 充能。命中点单目标冰冻/寒冷，并对命中点 3×3 内其它敌人附加寒冷。
 *   形态 1·冰雪区域：耗 3 充能。选中一个位置铺开 3×3 持续冰雪区域，持续数回合；
 *       每回合对区域内敌人造成 50% 面板伤害 + 全额寒冷；对已冻结(冰封)的敌人直接破除冻结
 *       并造成 150% 面板伤害。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class EvolvedWandOfFrost extends WandOfFrost implements EndModeWand {

	private static final int MODE_FROST_BOLT = 0;
	private static final int MODE_FROST_FIELD = 1;

	/** 冰雪区域持续回合数(铺地后每格 cur 值；可调)。 */
	private static final int FIELD_TURNS = 4;

	/** 当前形态(0=冰霜直击,1=冰雪区域)。 */
	private int mode = MODE_FROST_BOLT;

	@Override
	public String name() {
		return "凝霜法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 11923711, 1.5f );
	}

	/* ---------------- EndModeWand ---------------- */
	@Override
	public int modeCount() {
		return 2;
	}
	@Override
	public int modeIndex() {
		return mode;
	}
	@Override
	public void setModeIndex( int index ) {
		if (index < 0 || index >= modeCount()) index = 0;
		this.mode = index;
	}
	@Override
	public String modeName( int index ) {
		switch (index){
			case MODE_FROST_BOLT:  return "冰霜直击";
			case MODE_FROST_FIELD: return "冰雪区域";
			default:               return "";
		}
	}

	/** 冰雪区域耗 2 充能(较原 3 减 1)；冰霜直击耗 1。 */
	@Override
	protected int chargesPerCast() {
		return mode == MODE_FROST_FIELD ? 2 : 1;
	}

	/** 铺地形态也需要能选中地面空格，瞄准沿用父类(法杖默认弹道)。 */
	@Override
	public int collisionProperties( int target ){
		if (mode == MODE_FROST_FIELD){
			return Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID;
		}
		return super.collisionProperties( target );
	}

	//END M2：命中点直击(形态0) + 冰雪区域(形态1)
	@Override
	public void onZap(Ballistica bolt) {
		if (mode == MODE_FROST_FIELD){
			onZapField( bolt );
		} else {
			onZapBolt( bolt );
		}
	}

	/** 形态 0：命中点冰冻/寒冷，并对命中点 3×3 内其它敌人施加寒冷。 */
	private void onZapBolt(Ballistica bolt) {

		Heap heap = Dungeon.level.heaps.get(bolt.collisionPos);
		if (heap != null) {
			heap.freeze();
		}

		Fire fire = (Fire) Dungeon.level.blobs.get(Fire.class);
		if (fire != null && fire.volume > 0) {
			fire.clear( bolt.collisionPos );
		}

		MagicalFireRoom.EternalFire eternalFire = (MagicalFireRoom.EternalFire)Dungeon.level.blobs.get(MagicalFireRoom.EternalFire.class);
		if (eternalFire != null && eternalFire.volume > 0) {
			eternalFire.clear( bolt.collisionPos );
			if (bolt.path.size() > bolt.dist+1){
				eternalFire.clear( bolt.path.get(bolt.dist+1) );
			}
		}

		Char ch = Actor.findChar(bolt.collisionPos);
		if (ch != null){

			int damage = damageRoll();

			if (ch.buff(Frost.class) != null){
				return; //do nothing, can't affect a frozen target
			}
			if (ch.buff(Chill.class) != null){
				//6.67% less damage per turn of chill remaining, to a max of 10 turns (50% dmg)
				float chillturns = Math.min(10, ch.buff(Chill.class).cooldown());
				damage = (int)Math.round(damage * Math.pow(0.9333f, chillturns));
			} else {
				ch.sprite.burst( 0xFF99CCFF, buffedLvl() / 2 + 2 );
			}

			wandProc(ch, chargesPerCast());
			ch.damage(damage, this);
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.1f * Random.Float(0.87f, 1.15f) );

			if (ch.isAlive()){
				if (Dungeon.level.water[ch.pos])
					Buff.affect(ch, Chill.class, 4+buffedLvl());
				else
					Buff.affect(ch, Chill.class, 2+buffedLvl());
			}
		} else {
			Dungeon.level.pressCell(bolt.collisionPos);
		}

		//命中点周围 3×3 内的其它敌人施加较低等级寒冷(Chill)
		for (int i : PathFinder.NEIGHBOURS8) {
			Char around = Actor.findChar(bolt.collisionPos + i);
			if (around == null || around == ch) continue;
			if (around.buff(Frost.class) != null) continue;
			Buff.affect(around, Chill.class, 1f + buffedLvl()/2f);
		}
	}

	/** 形态 1：以命中点为中心铺开 3×3 冰雪区域(持续 FIELD_TURNS 回合)。 */
	private void onZapField(Ballistica bolt) {

		//需求⑦：冰雪区域不再可任意指定落点——按法术直线向前推进，到撞墙(首个无法通行处)停下，
		//区域铺在“撞墙前一格”的可行地面上，不会飘到墙里或远处空地的任意处。
		int center = lastPassableBeforeSolid( bolt );
		if (center < 0 || !Dungeon.level.insideMap( center )){
			return;
		}

		int dmgBase = damageRoll();
		int halfDmg = Math.round( dmgBase * 0.5f );   //每回合常规伤害(面板 50%)
		int fullDmg = Math.round( dmgBase * 1.5f );   //破冰伤害(面板 150%)
		float chillDur = 2f + buffedLvl();            //"全额"寒冷时长(与直击命中一致)

		//3×3 铺开(含中心)，经 Blob.seed 登记到 level.blobs 以便持久化/查询
		int seeded = 0;
		EndFrostField field = null;
		for (int i : PathFinder.NEIGHBOURS9) {
			int cell = center + i;
			if (!Dungeon.level.insideMap( cell ) || Dungeon.level.solid[cell]) {
				continue;
			}
			field = Blob.seed( cell, FIELD_TURNS, EndFrostField.class );
			seeded++;
		}

		if (field != null && seeded > 0){
			field.set( halfDmg, fullDmg, chillDur, this );
			GameScene.add( field );
		}

		Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.1f * Random.Float(0.87f, 1.15f) );
	}

	/** 沿弹道取“最远可行地面格”：从起点顺 path 前进，遇首个 solid 即停，返回前一可行格。
	 *  无实体会被拦时其终点即最后一格可行地——冰雪区域只会在法术正前方铺到撞墙前,不再任意远放。 */
	private int lastPassableBeforeSolid( Ballistica bolt ){
		//先看是否真的撞到墙(终点 solid)：若终点自身可行(空地/出口)则原地即为最远可达。
		if (bolt.collisionPos >= 0 && Dungeon.level.insideMap( bolt.collisionPos )
				&& !Dungeon.level.solid[ bolt.collisionPos ] && Dungeon.level.passable[ bolt.collisionPos ]){
			return bolt.collisionPos;
		}
		int hi = Math.min( bolt.path.size() - 1, bolt.dist );
		for (int i = hi; i > 0; i--){
			int c = bolt.path.get( i );
			if (Dungeon.level.insideMap( c ) && !Dungeon.level.solid[ c ] && Dungeon.level.passable[ c ]){
				return c;
			}
		}
		return bolt.collisionPos;
	}

	// ---- 形态持久化 ----
	private static final String MODE = "end_mode";
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( MODE, mode );
	}
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		mode = bundle.getInt( MODE );
		if (mode < 0 || mode >= modeCount()) mode = 0;
	}

	// ---- 终焉·进化基础(统一13把)：真实等级+8、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}