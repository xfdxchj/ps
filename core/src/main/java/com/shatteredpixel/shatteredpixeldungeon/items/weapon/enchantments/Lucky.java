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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.noosa.Visual;
import com.watabou.utils.Random;

public class Lucky extends Weapon.Enchantment {

	private static ItemSprite.Glowing GREEN = new ItemSprite.Glowing( 0x00FF00 );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.buffedLvl() );

		// lvl 0 - 10%
		// lvl 1 ~ 12%
		// lvl 2 ~ 14%
		float procChance = Math.min(1f, (level+4f)/(level+40f) * procChanceMultiplier(attacker) + activeChanceAdd());

		//==== END(挑战 198 幸运药水): 幸运附魔触发率 +50% ====
		//文档所有者定稿："提升财富戒/幸运附魔 50% 效果，50 回合。"
		//
		//与财富戒那边同样的处理：只放大**概率本身**，不碰其它乘数。
		//封顶仍是 1（100%），所以不会出现"必定触发"。
		if (attacker != null && attacker.buff(com.shatteredpixel.shatteredpixeldungeon
				.actors.buffs.LuckyPotionBuff.class) != null) {
			procChance = Math.min(1f, procChance * com.shatteredpixel.shatteredpixeldungeon
					.actors.buffs.LuckyPotionBuff.MULT);
		}

		if (Random.Float() < procChance){

			float powerMulti = Math.max(1f, procChance);

			//default is -5: 80% common, 20% uncommon, 0% rare
			//ring level increases by 1 for each 20% above 100% proc rate
			Buff.affect(defender, LuckProc.class).ringLevel = -10 + Math.round(5*powerMulti);
		} else {
			//in rare cases where we attack many times at once (e.g. gladiator fury)
			// make sure that failed luck procs override prior succeeded ones
			if (defender.buff(LuckProc.class) != null){
				defender.buff(LuckProc.class).detach();
			}
		}
		
		return damage;

	}
	
	public static Item genLoot(){
		//80% common, 20% uncommon, 0% rare
		return RingOfWealth.genConsumableDrop(-5);
	}

	public static void showFlare( Visual vis ){
		RingOfWealth.showFlareForBonusDrop(vis);
	}

	@Override
	public Glowing glowing() {
		return GREEN;
	}
	
	//used to keep track of whether a luck proc is incoming. see Mob.die()
	public static class LuckProc extends Buff {

		private int ringLevel = -5;
		
		@Override
		public boolean act() {
			detach();
			return true;
		}

		public Item genLoot(){
			detach();
			return RingOfWealth.genConsumableDrop(ringLevel);
		}
	}
	
}
