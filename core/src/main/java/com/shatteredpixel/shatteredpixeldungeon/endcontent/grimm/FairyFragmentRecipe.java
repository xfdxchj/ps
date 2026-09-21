/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」的炼金配方
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * END(挑战 129 心爱的少女): 童话残片 → 心爱的少女。
 *
 * <h3>3 合 1 机制（三轮两层）</h3>
 * 原表说"通过炼金合成（3 合 1 机制）获得 1 个唯一物品"，
 * 同时又说"收集 9 个童话残片"。9 = 3 × 3，所以是**两级合成**：
 * <pre>
 *   3 枚童话残片 → 1 张童话残页        （第一级）
 *   3 张童话残页 → 1 个「心爱的少女」   （第二级）
 * </pre>
 * 合计消耗 9 枚残片，正好对上原表的两个数字。
 *
 * <h3>为什么要求"不同的残片"</h3>
 * 残片有 9 种（对应 9 个角色）。若允许同种堆叠，
 * 玩家刷 9 张同一个人就能通关，收集玩法就失去意义了。
 * 所以{@link #testIngredients}会检查**种类不重复**。
 *
 * <h3>为什么不用 SimpleRecipe</h3>
 * {@code SimpleRecipe} 只按"类 + 数量"匹配，无法表达"必须不同种类"。
 * 所以用完整的 {@code Recipe} 子类自己写判定。
 */
public class FairyFragmentRecipe extends Recipe {

	/** 合成一级产物需要几张残片。 */
	public static final int FRAGMENTS_PER_NOTE = 3;
	/** 合成最终产物需要几张残页。 */
	public static final int NOTES_PER_GIRL = 3;

	//==================================================================
	//第一级：3 枚残片 -> 1 张残页
	//==================================================================

	/**
	 * END(129): 判定是否为"3 枚不同的残片"。
	 *
	 * <p>残片是可堆叠物品，所以一次投入可能只占一个 {@code Item} 槽
	 * 但 {@code quantity() == 3}。这里两种情况都要认。
	 */
	@Override
	public boolean testIngredients(ArrayList<Item> ingredients){
		if (ingredients == null) return false;

		ArrayList<FairyFragment> frags = new ArrayList<>();
		int total = 0;
		for (Item it : ingredients){
			if (it == null) return false;
			if (it instanceof FairyFragment){
				frags.add((FairyFragment) it);
				total += it.quantity();
			} else {
				return false;                     //混入别的东西就不成立
			}
		}

		if (total != FRAGMENTS_PER_NOTE) return false;

		//必须**不同种类**：把每种残片的数量算出来，任何一种超过 1 就拒绝
		HashSet<Integer> kinds = new HashSet<>();
		for (FairyFragment f : frags){
			//一张 Item 可能代表多枚（quantity>1），那种情况必然重复
			if (f.quantity() > 1) return false;
			if (!kinds.add(f.kind)) return false;
		}
		return kinds.size() == FRAGMENTS_PER_NOTE;
	}

	@Override
	public int cost(ArrayList<Item> ingredients){
		return 0;                                  //不额外收能量
	}

	@Override
	public Item sampleOutput(ArrayList<Item> ingredients){
		return new FairyNote();
	}

	@Override
	public Item brew(ArrayList<Item> ingredients){
		if (!testIngredients(ingredients)) return null;

		//消耗掉投入的残片：每个槽减 1 枚
		for (Item it : ingredients){
			if (it instanceof FairyFragment){
				com.shatteredpixel.shatteredpixeldungeon.endcontent.ItemConsume.consumeOne(it);
			}
		}

		FairyNote note = new FairyNote();
		note.identify();
		return note;
	}

	//==================================================================
	//第二级：3 张残页 -> 心爱的少女
	//==================================================================

	/**
	 * END(129): 第二级配方 —— 3 张残页合成「心爱的少女」。
	 */
	public static class GirlRecipe extends Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients){
			if (ingredients == null) return false;

			int total = 0;
			for (Item it : ingredients){
				if (!(it instanceof FairyNote)) return false;
				total += it.quantity();
			}
			return total == NOTES_PER_GIRL;
		}

		@Override
		public int cost(ArrayList<Item> ingredients){ return 0; }

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients){
			return new BelovedGirl();
		}

		@Override
		public Item brew(ArrayList<Item> ingredients){
			if (!testIngredients(ingredients)) return null;

			for (Item it : ingredients){
				if (it instanceof FairyNote){
					com.shatteredpixel.shatteredpixeldungeon.endcontent.ItemConsume.consumeOne(it);
				}
			}

			BelovedGirl g = new BelovedGirl();
			g.identify();
			return g;
		}
	}
}
