/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 样板 1：魔弹法杖·进化「魔弹 ×2」：单发命中时连续造成两段魔弹伤害，
 * 在“新物品+进阶附魔光泽”之上把机制做成真实可验证。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class EvolvedWandOfMagicMissile extends WandOfMagicMissile {

	@Override
	public String name() {
		return "灵陨·魔弹法杖";
	}

	@Override
	public String desc() {
		return "进化·灵陨法杖：你掷出的魔弹命中敌人时会立即连作两段独立的魔弹伤害（魔弹 ×2），每一段都会各自结算一次命中加成。\n\n**继承源法杖的等级**；充能上限提升到 20。";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 0x88FFFF, 2f );
	}

	//END M2 真实机制：魔弹命中目标造成两次独立的魔弹伤害（=魔弹 ×2）
	@Override
	public void onZap(Ballistica bolt) {
		Char ch = Actor.findChar( bolt.collisionPos );
		if (ch != null) {
			for (int i = 0; i < 2; i++) {
				wandProc(ch, chargesPerCast());
				ch.damage(damageRoll(), this);
				Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f) );
				ch.sprite.burst( 0xFFFFFFFF, buffedLvl()/2 + 2 );
			}

			//END(一.10): 保留原版魔弹「强化其它法杖」的效果（MagicCharge）
			for (Wand.Charger wandCharger : curUser.buffs(Wand.Charger.class)){
				if (wandCharger.wand().buffedLvl() < buffedLvl() || curUser.buff(MagicCharge.class) != null){
					Buff.prolong(curUser, MagicCharge.class, MagicCharge.DURATION).setup(this);
					break;
				}
			}
		} else {
			com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.pressCell( bolt.collisionPos );
		}
	}
	// ---- 终焉·进化基础(统一13把)：继承源法杖等级、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}
