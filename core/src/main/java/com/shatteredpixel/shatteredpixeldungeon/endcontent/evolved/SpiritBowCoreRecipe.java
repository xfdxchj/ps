package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.items.SpiritBowCore;

import java.util.ArrayList;

/**
 * 炼金： 2× 升级卷轴 + 50× 液金 → 灵能核心。
 * 动态配方：原料不限槽数，逐个 Sum 判足即可，任意 count 都可用(variableRecipes 段)。
 */
public class SpiritBowCoreRecipe extends Recipe {

	private static final int LIQUID_NEEDED = 50;
	private static final int SCROLL_NEEDED = 2;

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		int scrolls = 0, liquid = 0;
		for (Item it : ingredients){
			if (it == null || !it.isIdentified()) continue;
			if (it.getClass() == ScrollOfUpgrade.class){
				scrolls += it.quantity();
			} else if (it.getClass() == LiquidMetal.class){
				liquid += it.quantity();
			}
		}
		return scrolls >= SCROLL_NEEDED && liquid >= LIQUID_NEEDED;
	}

	@Override
	public int cost(ArrayList<Item> ingredients) {
		return 0;
	}

	@Override
	public Item sampleOutput(ArrayList<Item> ingredients) {
		return new SpiritBowCore().quantity(1);
	}

	@Override
	public Item brew(ArrayList<Item> ingredients) {
		if (!testIngredients(ingredients)) return null;
		int scrolls = SCROLL_NEEDED, liquid = LIQUID_NEEDED;
		for (Item it : ingredients){
			if (it == null) continue;
			if (it.getClass() == ScrollOfUpgrade.class && scrolls > 0){
				int take = Math.min(scrolls, it.quantity());
				scrolls -= take;
				it.quantity(it.quantity() - take);
			} else if (it.getClass() == LiquidMetal.class && liquid > 0){
				int take = Math.min(liquid, it.quantity());
				liquid -= take;
				it.quantity(it.quantity() - take);
			}
		}
		return sampleOutput(ingredients);
	}
}
