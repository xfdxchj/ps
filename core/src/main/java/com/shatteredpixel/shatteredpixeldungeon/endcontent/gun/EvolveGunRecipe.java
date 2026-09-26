/*
 * Shattered Pixel Dungeon: End
 * 炼金配方：枪械进阶。
 * 输入（同一炼金锅里选三样）：1 把干净且已鉴定的 T5 枪 + 1 颗强化符石 + 1 颗星花种子。
 * 输出：该枪对应的 T6 战术型（保留强化等级与改造部件）。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.gun;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class EvolveGunRecipe extends Recipe {

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		if (ingredients.size() != 3) return false;

		boolean hasGun = false;
		boolean hasStone = false;
		boolean hasSeed = false;
		for (Item it : ingredients) {
			if (it instanceof Gun) {
				if (it.isIdentified() && !it.cursed && evolvedClass((Gun) it) != null) {
					hasGun = true;
				} else {
					return false;
				}
			} else if (it instanceof StoneOfAugmentation) {
				hasStone = true;
			} else if (it instanceof Starflower.Seed) {
				hasSeed = true;
			} else {
				return false;
			}
		}
		return hasGun && hasStone && hasSeed;
	}

	@Override
	public int cost(ArrayList<Item> ingredients) {
		return 0;
	}

	@Override
	public Item brew(ArrayList<Item> ingredients) {
		if (!testIngredients(ingredients)) return null;

		Gun source = null;
		for (Item it : ingredients) {
			if (it instanceof Gun) {
				source = (Gun) it;
				break;
			}
		}
		if (source == null) return null;

		Gun evolved = evolve(source);
		if (evolved == null) return null;

		evolved.identify();
		Catalog.setSeen(evolved.getClass());
		GLog.i("炼成：" + evolved.title());

		for (Item it : ingredients) {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.ItemConsume.consumeOne(it);
		}
		return evolved;
	}

	@Override
	public Item sampleOutput(ArrayList<Item> ingredients) {
		if (!testIngredients(ingredients)) return null;
		for (Item it : ingredients) {
			if (it instanceof Gun) {
				return evolve((Gun) it);
			}
		}
		return null;
	}

	private static Class<? extends Gun> evolvedClass(Gun gun) {
		String n = gun.getClass().getSimpleName();
		if ("AR_T5".equals(n)) return com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.AR_T6.class;
		if ("SR_T5".equals(n)) return com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.SR_T6.class;
		if ("HG_T5".equals(n)) return com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.HG_T6.class;
		if ("GL_T5".equals(n)) return com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.GL_T6.class;
		if ("RL_T5".equals(n)) return com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.RL_T6.class;
		return null;
	}

	private static Gun evolve(Gun source) {
		Class<? extends Gun> cls = evolvedClass(source);
		if (cls == null) return null;
		Gun evolved = com.watabou.utils.Reflection.newInstance(cls);
		evolved.level(source.level());
		evolved.barrelMod = source.barrelMod;
		evolved.magazineMod = source.magazineMod;
		evolved.bulletMod = source.bulletMod;
		evolved.weightMod = source.weightMod;
		evolved.attachMod = source.attachMod;
		evolved.enchantMod = source.enchantMod;
		evolved.inscribeMod = source.inscribeMod;
		return evolved;
	}
}
