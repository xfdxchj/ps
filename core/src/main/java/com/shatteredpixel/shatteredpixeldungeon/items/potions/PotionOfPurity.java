/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class PotionOfPurity extends Potion {
	
	private static final int DISTANCE	= 3;
	
	private static ArrayList<Class> affectedBlobs;

	{
		icon = ItemSpriteSheet.Icons.POTION_PURITY;
		
		affectedBlobs = new ArrayList<>(new BlobImmunity().immunities());
	}

	@Override
	public void shatter( int cell ) {
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), DISTANCE );
		
		ArrayList<Blob> blobs = new ArrayList<>();
		for (Class c : affectedBlobs){
			Blob b = Dungeon.level.blobs.get(c);
			if (b != null && b.volume > 0){
				blobs.add(b);
			}
		}
		
		for (int i=0; i < Dungeon.level.length(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				
				for (Blob blob : blobs) {
					blob.clear(i);
				}
				
				if (Dungeon.level.heroFOV[i]) {
					CellEmitter.get( i ).burst( Speck.factory( Speck.DISCOVER ), 2 );
				}
				
			}
		}


		splash( cell );
		if (Dungeon.level.heroFOV[cell]) {
			Sample.INSTANCE.play(Assets.Sounds.SHATTER);

			identify();
			GLog.i(Messages.get(this, "freshness"));
		}
		
	}
	
	@Override
	public void apply( Hero hero ) {

		//==== END(挑战 49 切尔诺贝利): 净化药水改为"只有解毒功能" ====
		//原表："持续时间增加至 300 回合…净化药水改为只有解毒功能"。
		//
		//含义：原本给的是 BlobImmunity（对所有气体免疫），
		//勾选 49 后**只清除中毒**，不再提供"站在毒气里也不受伤"的全免疫 ——
		//否则"全图毒气"这条规则就形同虚设（喝一瓶就能横穿全图）。
		//
		//但持续回合仍按 300 计：这是"解毒后的一段时间内不再中毒"，
		//而不是永久免疫毒气伤害。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.purityOnlyCuresDebuffs()) {

			//清除已有的中毒
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison poison =
					hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.Poison.class);
			if (poison != null) poison.detach();

			//300 回合内不再中毒（而不是对所有气体免疫）
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
					hero,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.PoisonImmunity.class,
					com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.purityDuration(
									com.shatteredpixel.shatteredpixeldungeon.actors.buffs
											.BlobImmunity.DURATION));

			GLog.w( Messages.get(this, "protected") );
			SpellSprite.show(hero, SpellSprite.PURITY);
			identify();
			return;
		}

		//---- 未勾选 49：原版行为 ----
		GLog.w( Messages.get(this, "protected") );
		Buff.prolong( hero, BlobImmunity.class, BlobImmunity.DURATION );
		SpellSprite.show(hero, SpellSprite.PURITY);
		identify();
	}
	
	@Override
	public int value() {
		return isKnown() ? 40 * quantity : super.value();
	}
}
