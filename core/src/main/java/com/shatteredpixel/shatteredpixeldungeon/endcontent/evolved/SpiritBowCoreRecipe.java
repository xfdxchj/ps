package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.endcontent.items.SpiritBowCore;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;

import java.util.ArrayList;

/**
 * 炼金： 1× 强化符石(StoneOfAugmentation) + 1× 驱邪卷轴(ScrollOfRemoveCurse) → 灵能核心。
 * 动态配方：不限槽数，逐个 Sum 判足即可（variableRecipes 段）。
 */
public class SpiritBowCoreRecipe extends Recipe {

	private static final int STONE_NEEDED = 1;
	private static final int SCROLL_NEEDED = 1;

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		int stones = 0, scrolls = 0;
		for (Item it : ingredients){
			if (it == null) continue;
			if (it.getClass() == StoneOfAugmentation.class){
				stones += it.quantity();
			} else if (it.getClass() == ScrollOfRemoveCurse.class){
				scrolls += it.quantity();
			}
		}
		return stones >= STONE_NEEDED && scrolls >= SCROLL_NEEDED;
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
		int stones = STONE_NEEDED, scrolls = SCROLL_NEEDED;
		for (Item it : ingredients){
			if (it == null) continue;
			if (it.getClass() == StoneOfAugmentation.class && stones > 0){
				int take = Math.min(stones, it.quantity());
				stones -= take;
				it.quantity(it.quantity() - take);
			} else if (it.getClass() == ScrollOfRemoveCurse.class && scrolls > 0){
				int take = Math.min(scrolls, it.quantity());
				scrolls -= take;
				it.quantity(it.quantity() - take);
			}
		}
		return sampleOutput(ingredients);
	}
}
