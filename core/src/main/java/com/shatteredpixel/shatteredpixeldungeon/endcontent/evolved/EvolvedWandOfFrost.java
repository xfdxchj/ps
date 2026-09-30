/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 凝霜法杖(冰霜进化)：只有一种形态「冰天雪地」——
 * 在选定落点放出冰雪气体，气体每回合给范围内敌人挂冰爆 buff，
 * buff 负责挂寒冷、寒冷满时冻结，冻结结束引爆。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class EvolvedWandOfFrost extends WandOfFrost {

	@Override
	public String name() {
		return "凝霜法杖";
	}

	@Override
	public String desc() {
		return "进化·凝霜法杖：在你指定的落点放出一股冰蓝色冰雪气体。"
				+ "气体像原版冰霜一样给范围内的敌人挂上寒冷，寒冷堆满后将其冻结；"
				+ "敌人被冻结后，冻结消失时冰爆造成 150% 面板伤害，并对周围 3×3 范围造成 50% 伤害。\n\n"
				+ "充能上限提升到 20；**继承源法杖的等级**。";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 11923711, 1.5f );
	}

	/** 每次施法消耗 1 充能。 */
	@Override
	protected int chargesPerCast() {
		return 1;
	}

	/** 可以像酸蚀气一样直接指定任意空地。 */
	@Override
	public int collisionProperties(int target){
		return Ballistica.STOP_TARGET | Ballistica.STOP_SOLID;
	}

	@Override
	public void onZap(Ballistica bolt) {
		onZapField( bolt );
	}

	/** 在落点放出冰雪气体。 */
	private void onZapField(Ballistica bolt) {

		int center = bolt.collisionPos;
		if (!Dungeon.level.insideMap( center )){
			return;
		}

		int dmgBase = damageRoll();
		int burstDmg = Math.round( dmgBase * FrostBalance.BURST_PCT );
		int splashDmg = Math.round( dmgBase * FrostBalance.SPLASH_PCT );
		float chillDur = FrostBalance.CHILL_BASE + buffedLvl();
		int volume = FrostBalance.GAS_VOLUME_BASE + FrostBalance.GAS_VOLUME_PER_LVL * buffedLvl();

		EndFrostField field = Blob.seed( center, volume, EndFrostField.class );
		if (field != null){
			field.set( burstDmg, splashDmg, chillDur, this );
			GameScene.add( field );
		}

		Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.1f * Random.Float(0.87f, 1.15f) );
	}

	// ---- 终焉·进化基础(统一13把)：继承源法杖等级、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}
