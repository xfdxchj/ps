/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 凝霜法杖(冰霜进化) 双形态：
 *   形态 0·冰霜直击(default)：耗 1 充能。命中点单目标冰冻/寒冷，并对命中点 3×3 内其它敌人附加寒冷。
 *   形态 1·冰天雪地：耗 1 充能。选中一个位置放出冰雪气体，像酸蚀气一样向外扩散；
 *       气体每回合给区域内敌人挂上冰爆 buff；buff 负责施加寒冷、概率冻结，
 *       并在冻结消失时造成 400% 面板伤害。
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

	/** END: 形态0 —— 已寒冷的敌人被直接冻住的概率(%)。 */
	private static final int DIRECT_FREEZE_PCT = 25;

	/** 当前形态(0=冰霜直击,1=冰天雪地)。 */
	private int mode = MODE_FROST_BOLT;

	@Override
	public String name() {
		return "凝霜法杖";
	}

	@Override
	public String desc() {
		return "进化·凝霜法杖：拥有两种发射形态，可在背包-法杖窗口切换。\n\n▍形态 0·冰霜直击（默认，耗 1 充）：命中点单目标冰冻/寒冷，并把落点周围 3×3 内其它敌人附上寒冷。\n▍形态 1·冰天雪地（耗 1 充）：在你指定的落点放出一股冰蓝色冰雪气体。气体本身不造成伤害，只会像原版冰霜一样给范围内的敌人挂上寒冷，寒冷堆满后将其冻结；敌人被冻结后，冻结消失时冰爆造成 400% 面板伤害。\n\n充能上限提升到 20；**继承源法杖的等级**。";
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
			case MODE_FROST_FIELD: return "冰天雪地";
			default:               return "";
		}
	}

	/** END(二.7): 两个形态都只耗 1 充能。 */
	@Override
	protected int chargesPerCast() {
		return 1;  //END(二.7): 两个形态都只耗 1
	}

	/** 瞄准按父类默认：点到的可达最远落点即铺放处（撤掉此前试验性的特殊瞄准）。 */
	//END M2：命中点直击(形态0) + 冰天雪地(形态1)
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

				//==== END(修复·冰霜法杖没有冰冻手段) ====
				//文档所有者反馈："也没有方法造成冰冻。"
				//原版冰霜法杖只会挂寒冷(Chill)，从不产生冰冻(Frost)，
				//于是区域形态里"对已冻结敌人破冰 200%"这段永远触发不了。
				//规则：目标已经在寒冷中时，有概率直接把它冻住(2 回合)。
				if (!ch.isImmune( Frost.class )
					&& ch.buff( Frost.class ) == null
					&& ch.buff( Chill.class ) != null
					&& Random.Int(100) < DIRECT_FREEZE_PCT){
					Buff.affect( ch, Frost.class, 2f );
					if (ch.sprite != null){
						ch.sprite.burst( 0xFF99CCFF, 4 );
					}
				}
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

	/** 形态 1：以“玩家选定的命中点”为中心放出冰雪气体，
	 *  允许在视野内任意指定落点位置。 */
	private void onZapField(Ballistica bolt) {

		int center = bolt.collisionPos;
		if (!Dungeon.level.insideMap( center )){
			return;
		}

		int dmgBase = damageRoll();
		//END(二.7 重做): 区域不造成伤害；冰爆在冻结消失时造成面板 400%
		int burstDmg = Math.round( dmgBase * 4f );
		//END(改·真气体): 像腐蚀法杖一样只在一个点放出气体，
		//由 Blob.super.evolve() 自然向外扩散，不再固定铺 3×3。
		int volume = 50 + 10 * buffedLvl();
		EndFrostField field = Blob.seed( center, volume, EndFrostField.class );
		if (field != null){
			field.set( burstDmg, this );
			GameScene.add( field );
		}

		Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.1f * Random.Float(0.87f, 1.15f) );
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

	// ---- 终焉·进化基础(统一13把)：继承源法杖等级、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}