/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfFrost → 凝霜法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：冰霜:冰冻变为 3×3 范围的低温(Chill)区域。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class EvolvedWandOfFrost extends WandOfFrost {

	@Override
	public String name() {
		return "凝霜法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 11923711, 1.5f );
	}

	//END M2 真机制：除命中单体外，还对命中点 3×3 内其它敌人施加较低等级的寒冷
	@Override
	public void onZap(Ballistica bolt) {

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

		//END M2：命中点周围 3×3 内的其它敌人施加较低等级寒冷(Chill)
		for (int i : PathFinder.NEIGHBOURS8) {
			Char around = Actor.findChar(bolt.collisionPos + i);
			if (around == null || around == ch) continue;
			if (around.buff(Frost.class) != null) continue;
			Buff.affect(around, Chill.class, 1f + buffedLvl()/2f);
		}
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