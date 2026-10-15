/*
 * 破碎的地牢 (End fork) — 破碎之刃重铸配方。
 * 破碎碎片 + 液金 [+ 升级卷轴 / 强化符石 / 诅咒菱晶] -> 原武器。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.BrittleBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.CurseInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class BladeReforge extends Recipe {

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		if (ingredients.size() < 2 || ingredients.size() > 3) return false;
		boolean shard = false, metal = false, extra = false;
		for (Item it : ingredients){
			if (it instanceof BladeShard) shard = true;
			else if (it instanceof LiquidMetal) metal = true;
			else if (it instanceof ScrollOfUpgrade || it instanceof StoneOfAugmentation
					|| it instanceof CurseInfusion){
				if (extra) return false;
				extra = true;
			} else {
				return false;
			}
		}
		return shard && metal;
	}

	@Override
	public int cost(ArrayList<Item> ingredients) { return 0; }

	@Override
	public Item brew(ArrayList<Item> ingredients) {
		BladeShard shard = null;
		boolean up = false, aug = false, curse = false;
		for (Item it : ingredients){
			if (it instanceof BladeShard) shard = (BladeShard) it;
			else if (it instanceof ScrollOfUpgrade) up = true;
			else if (it instanceof StoneOfAugmentation) aug = true;
			else if (it instanceof CurseInfusion) curse = true;
			it.quantity(it.quantity() - 1);
		}
		if (shard == null || shard.original == null) return null;

		Weapon w = shard.original;
		w.durability = BrittleBlade.MAX_DURABILITY;
		w.infiniteDurability = true;
		if (up){
			w.upgrade();
		}
		if (aug){
			w.brittleMinBonus = true;
		}
		if (curse){
			w.upgrade();
			w.upgrade();
			w.enchant(Weapon.Enchantment.randomCurse());
		}
		GLog.i("重铸：" + w.title());
		return w;
	}

	@Override
	public Item sampleOutput(ArrayList<Item> ingredients) {
		for (Item it : ingredients){
			if (it instanceof BladeShard && ((BladeShard) it).original != null){
				return ((BladeShard) it).original;
			}
		}
		return null;
	}
}
