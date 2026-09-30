/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 凝霜法杖(冰霜进化)"冰雪风暴"形态的持续区域。
 *
 * END(二.7 重做): 区域本身不造成伤害，只施加「寒冷」与「冰爆」；
 * 敌人被冻结后，冻结消失时由冰爆造成面板 400% 伤害。
 * 区域像气体一样**扩散**（调用 Blob.super.evolve），颜色为蓝色。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class EndFrostField extends Blob {

	/** 冰爆伤害（面板 400%）。 */
	private int burstDamage = 0;
	private float chillDuration = 0;
	private Wand cause;

	/** END: 已在寒冷中的敌人每回合被冻住的概率(%)。 */
	private static final int FREEZE_PCT = 40;
	/** END: 冻住持续回合数。 */
	private static final float FREEZE_TURNS = 2f;

	public EndFrostField set(int burstDamage, float chillDuration, Wand cause){
		this.burstDamage = burstDamage;
		this.chillDuration = chillDuration;
		this.cause = cause;
		return this;
	}

	@Override
	protected void evolve() {

		//END(二.7 修订): 先走 Blob 的扩散/衰减 —— 冰霜领域像气体一样向外蔓延
		super.evolve();

		int cell;

		//再用**旧体积 cur** 对格子上的角色生效（与 ToxicGas 的写法一致）
		for (int i = area.top-1; i <= area.bottom; i++) {
			for (int j = area.left-1; j <= area.right; j++) {
				cell = j + i * Dungeon.level.width();
				if (!Dungeon.level.insideMap( cell )) {
					continue;
				}
				if (cur[cell] > 0){

					Char ch = Actor.findChar( cell );
					if (ch != null
							&& ch != Dungeon.hero
							&& ch.alignment != Char.Alignment.ALLY
							&& ch.isAlive()){

						//施加寒冷
						if (!ch.isImmune( Chill.class )){
							Buff.prolong( ch, Chill.class, chillDuration );
						}

						//施加/刷新冰爆（面板 400% 由法杖写入）
						if (!ch.isImmune( FrostBurst.class )){
							Buff.affect( ch, FrostBurst.class ).damage = burstDamage;
						}

						//已在寒冷中的敌人有概率被冻住 → 冻结消失时触发冰爆
						if (!ch.isImmune( Frost.class )
								&& ch.buff( Frost.class ) == null
								&& ch.buff( Chill.class ) != null
								&& Random.Int(100) < FREEZE_PCT){
							Buff.affect( ch, Frost.class, FREEZE_TURNS );
						}
					}
				}
			}
		}
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		//蓝色暴风雪粒子
		emitter.pour( Speck.factory( Speck.BLIZZARD, true ), 0.3f );
	}

	//——持久化 ——//
	private static final String BURST_DMG = "burst_damage";
	private static final String CHILL_DUR = "chill_duration";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( BURST_DMG, burstDamage );
		bundle.put( CHILL_DUR, chillDuration );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		burstDamage = bundle.getInt( BURST_DMG );
		chillDuration = bundle.getFloat( CHILL_DUR );
	}
}
