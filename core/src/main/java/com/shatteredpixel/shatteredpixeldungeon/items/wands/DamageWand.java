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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

//for wands that directly damage a target
//wands with AOE or circumstantial direct damage count here (e.g. fireblast, transfusion), but wands with indirect damage do not (e.g. corrosion)
public abstract class DamageWand extends Wand{

	//END(二.5): 进阶法杖伤害成长下限+1、上限+2（按当前等级）
	protected boolean evolvedWand(){
		return getClass().getName().contains(".endcontent.evolved.EvolvedWandOf");
	}

	public int min(){
		int v = min(buffedLvl());
		return evolvedWand() ? v + buffedLvl() : v;
	}

	public abstract int min(int lvl);

	public int max(){
		int v = max(buffedLvl());
		return evolvedWand() ? v + 2*buffedLvl() : v;
	}

	public abstract int max(int lvl);

	public int damageRoll(){
		return damageRoll(buffedLvl());
	}

	public int damageRoll(int lvl){
		int dmg = Hero.heroDamageIntRange(min(lvl) + (evolvedWand()?lvl:0), max(lvl) + (evolvedWand()?2*lvl:0));
		WandEmpower emp = Dungeon.hero.buff(WandEmpower.class);
		if (emp != null){
			dmg += emp.dmgBoost;
			emp.left--;
			if (emp.left <= 0) {
				emp.detach();
			}
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
		}
		//==== END(修复·126 格林之心): 法杖伤害 +2 +1% ×等级 ====
		dmg += com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
			.BlackSoul.grimmMagicBonus(Dungeon.hero, dmg);

		//END(二.12 原神副词条): 法术伤害加成
		if (Dungeon.hero != null){
			dmg = Math.round(dmg * com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.RingAffix.spellDamageMultiplier(Dungeon.hero));
		}

		//END(二.12 修订): 法杖也可以暴击，倍率与近战一致（2 倍）
		lastRollCrit = false;
		if (Dungeon.hero != null){
			float crit = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.RingAffix.critChance(Dungeon.hero);
			if (crit > 0f && com.watabou.utils.Random.Float() < crit){
				dmg = Math.round(dmg * 2f);
				lastRollCrit = true;
			}
		}

		//==== END(修复·124 野生狗奶): 法杖伤害也吃"全属性 -75%" ====
		if (Dungeon.hero != null && com.shatteredpixel.shatteredpixeldungeon.endcontent
				.grimm.WildDogMilk.isActive(Dungeon.hero)){
			dmg = Math.round(dmg * com.shatteredpixel.shatteredpixeldungeon.endcontent
					.grimm.WildDogMilk.STAT_MULT);
		}
		return dmg;
	}

	//END(二.12 修订): 最近一次 damageRoll 是否触发暴击，供飘字使用
	private static boolean lastRollCrit = false;
	public static boolean consumeLastRollCrit(){
		boolean b = lastRollCrit;
		lastRollCrit = false;
		return b;
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max());
		else
			return Messages.get(this, "stats_desc", min(0), max(0));
	}

	@Override
	public String upgradeStat1(int level) {
		return min(level) + "-" + max(level);
	}
}
