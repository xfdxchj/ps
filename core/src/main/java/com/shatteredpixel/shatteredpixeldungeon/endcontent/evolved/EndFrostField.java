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
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.watabou.utils.Bundle;

public class EndFrostField extends Blob {

	/** 冰爆主伤害（面板 150%）。 */
	private int burstDamage = 0;
	/** 周围 3×3 溅射伤害（面板 50%）。 */
	private int splashDamage = 0;
	/** 寒冷持续时间（随法杖等级提升）。 */
	private float chillDuration = 2f;
	private Wand cause;

	public EndFrostField set(int burstDamage, int splashDamage, float chillDuration, Wand cause){
		this.burstDamage = burstDamage;
		this.splashDamage = splashDamage;
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

						//像酸蚀气体挂 Corrosion 一样：气体只负责每回合挂上
						//FrostBurst，具体的寒冷/冻结/引爆都在 buff 里处理。
						if (!ch.isImmune( FrostBurst.class )){
							Buff.affect( ch, FrostBurst.class ).set( burstDamage, splashDamage, chillDuration );
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
		//END(冰天雪地): 用纯白雪花粒子铺出雪场
		emitter.pour( SnowflakeParticle.FACTORY, 0.4f ); //与原版毒气/酸蚀气一致
	}

	//——持久化 ——//
	private static final String BURST_DMG = "burst_damage";
	private static final String SPLASH_DMG = "splash_damage";
	private static final String CHILL_DUR = "chill_duration";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( BURST_DMG, burstDamage );
		bundle.put( SPLASH_DMG, splashDamage );
		bundle.put( CHILL_DUR, chillDuration );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		burstDamage = bundle.getInt( BURST_DMG );
		splashDamage = bundle.getInt( SPLASH_DMG );
		chillDuration = bundle.getFloat( CHILL_DUR );
	}
}
