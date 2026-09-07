/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 凝霜法杖(冰霜进化)"冰雪区域"形态的持续 3×3 区域。
 *
 * 区域铺开后持续若干回合(每格 cur 值=剩余回合数)，每回合 tick 对站于格上的敌人：
 *   - 若目标已冻结(Frost/冰封)：破除冻结并造成 150% 面板伤害；
 *   - 否则造成 50% 面板伤害并施加全额寒冷(Chill)。
 * 不扩散、不蔓延，只按初始 3×3 铺开范围随时间消散。
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

public class EndFrostField extends Blob {

	private int halfDamage = 0;   //每回合常规伤害(面板 50%)
	private int fullDamage = 0;   //破除冰封伤害(面板 150%)
	private float chillDuration = 0;
	private Wand cause;

	public EndFrostField set(int halfDamage, int fullDamage, float chillDuration, Wand cause){
		this.halfDamage = halfDamage;
		this.fullDamage = fullDamage;
		this.chillDuration = chillDuration;
		this.cause = cause;
		return this;
	}

	@Override
	protected void evolve() {

		int cell;
		int remaining;

		//逐格衰减，不做扩散
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j * Dungeon.level.width();
				if (!Dungeon.level.insideMap( cell )) {
					continue;
				}
				remaining = cur[cell];
				if (remaining > 0){

					//对站在这格上的敌人结算(不影响英雄/友方)
					Char ch = Actor.findChar( cell );
					if (ch != null
							&& ch != Dungeon.hero
							&& ch.alignment != Char.Alignment.ALLY
							&& !ch.isImmune( Frost.class )
							&& !ch.isImmune( Chill.class )){

						if (ch.buff( Frost.class ) != null){
							//破冰：解除冻结并造成 150% 面板伤害
							Buff.detach( ch, Frost.class );
							ch.damage( fullDamage, (cause != null) ? cause : this );
							ch.sprite.burst( 0xFF99CCFF, buffedLvlBase()/2 + 2 );
						} else {
							ch.damage( halfDamage, (cause != null) ? cause : this );
						}

						//全额寒冷
						if (ch.isAlive()){
							Buff.affect( ch, Chill.class, chillDuration );
						}
					}

					//剩余回合-1
					off[cell] = remaining - 1;
					volume += off[cell];

				} else {
					off[cell] = 0;
				}
			}
		}
	}

	private int buffedLvlBase(){
		return (cause != null) ? cause.buffedLvl() : 0;
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( Speck.factory( Speck.BLIZZARD, true ), 0.3f );
	}

	//——持久化(伤害/寒冷/剩余回合随区域保存)——//
	private static final String HALF_DMG  = "half_damage";
	private static final String FULL_DMG  = "full_damage";
	private static final String CHILL_DUR = "chill_duration";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( HALF_DMG, halfDamage );
		bundle.put( FULL_DMG, fullDamage );
		bundle.put( CHILL_DUR, chillDuration );
		//cause 不持久化(法杖引用仅用于伤害归因),读档后默认按区域自身结算
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		halfDamage = bundle.getInt( HALF_DMG );
		fullDamage = bundle.getInt( FULL_DMG );
		chillDuration = bundle.getFloat( CHILL_DUR );
		cause = null;
	}
}
