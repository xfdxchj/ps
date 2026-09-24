/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 凝霜法杖(冰霜进化)"冰雪区域"形态的持续 3×3 区域。
 *
 * 区域铺开后持续若干回合(每格 cur 值=剩余回合数)，每回合 tick 对站于格上的敌人：
 *   - 若目标已冻结(Frost/冰封)：破除冻结并造成 200% 面板伤害；
 *   - 否则造成 100% 面板伤害并施加全额寒冷(Chill)；
 *   - 已在寒冷中的敌人每回合有概率被冻住，从而触发上面的破冰分支。
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
import com.watabou.utils.Random;

public class EndFrostField extends Blob {

	//注：这两个值由 EvolvedWandOfFrost 按"面板 100% / 200%"传入，
	//字段名沿用旧的以免读档不兼容。
	private int halfDamage = 0;   //每回合常规伤害(现在=面板 100%)
	private int fullDamage = 0;   //破除冰封伤害(现在=面板 200%)

	/** END: 已在寒冷中的敌人每回合被冻住的概率(%)。 */
	private static final int FREEZE_PCT = 40;
	/** END: 冻住持续回合数。 */
	private static final float FREEZE_TURNS = 2f;
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
							&& ch.alignment != Char.Alignment.ALLY){

						//==== END(修复·冰霜区域数值问题) ====
						//① 原来"免疫 Frost/Chill"把整段都挡掉了：免疫寒冷的怪连伤害也不吃。
						//   现在只挡附带的寒冷效果，伤害照常结算。
						//② 原来对已中寒冷的怪用 Buff.affect(..., duration) 继续叠加时长
						//   （affect 的 duration 重载是累加），站在区域里会无限叠冷。
						//   改成 Buff.prolong（取更长的那个）——与各冷却 buff 的修法一致。
						if (ch.buff( Frost.class ) != null){
							//破冰：解除冻结并造成 150% 面板伤害
							Buff.detach( ch, Frost.class );
							ch.damage( fullDamage, (cause != null) ? cause : this );
							if (ch.sprite != null){
								ch.sprite.burst( 0xFF99CCFF, buffedLvlBase()/2 + 2 );
							}
						} else {
							ch.damage( halfDamage, (cause != null) ? cause : this );
						}

						//全额寒冷（免疫寒冷的怪只是不吃这个效果）
						if (ch.isAlive() && !ch.isImmune( Chill.class )){
							Buff.prolong( ch, Chill.class, chillDuration );
						}

						//==== END(修复·冰霜区域没有冰冻手段) ====
						//文档所有者反馈："也没有方法造成冰冻。"
						//规则：已经在寒冷中的敌人，每回合有 FREEZE_PCT% 被冻住(Frost)，
						//于是下一次 tick 会走上面的"破冰 200%"分支，形成 寒冷↔破冰 的循环。
						if (ch.isAlive()
							&& !ch.isImmune( Frost.class )
							&& ch.buff( Frost.class ) == null
							&& ch.buff( Chill.class ) != null
							&& Random.Int(100) < FREEZE_PCT){
							Buff.affect( ch, Frost.class, FREEZE_TURNS );
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
