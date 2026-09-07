/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 炼金配方：法杖蜕变。
 * 输入（同一炼金锅里选两样）：1 把干净且 ≥+8 的（已鉴定）法杖  +  1 颗“强化符石”StoneOfAugmentation。
 * 输出：该法杖对应的进化法杖（EvolvedXxx），尽量保留强化等级与当前充能。
 * 一条动态配方即可覆盖全部 13 把（来源→其进化）的映射。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class EvolveWandRecipe extends Recipe {

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		if (ingredients.size() != 2) return false;

		boolean hasWand = false;
		boolean hasStone = false;
		for (Item it : ingredients){
			if (it instanceof Wand){
				//只能用于干净、已鉴定且 ≥+8 的可进化法杖
				hasWand = it.isIdentified() && !it.cursed && EndWandEvolution.isEligible((Wand) it);
			} else if (it instanceof StoneOfAugmentation){
				hasStone = true;
			} else {
				return false;
			}
		}
		return hasWand && hasStone;
	}

	@Override
	public int cost(ArrayList<Item> ingredients) {
		return 0; //真正消耗的是材料本身，不需额外炼金能量
	}

	@Override
	public Item brew(ArrayList<Item> ingredients) {
		if (!testIngredients(ingredients)) return null;

		Wand source = null;
		for (Item it : ingredients){
			if (it instanceof Wand) { source = (Wand) it; break; }
		}
		if (source == null) return null;

		Wand evolved = EndWandEvolution.evolve(source);
		if (evolved == null) return null;

		//END: 产物标记已鉴定并登记进图鉴(法杖组)，方便日志记录本次配方
		evolved.identify();
		Catalog.setSeen(evolved.getClass());
		GLog.i("炼成进化法杖: " + evolved.title() + " 已记入图鉴/日志。");

		//消耗材料：源法杖与一颗强化符石清零（AlchemyScene 会按 quantity 移除）
		for (Item it : ingredients){
			it.quantity( it.quantity() - 1 );
		}
		return evolved;
	}

	@Override
	public Item sampleOutput(ArrayList<Item> ingredients) {
		if (!testIngredients(ingredients)) return null;
		for (Item it : ingredients){
			if (it instanceof Wand && EndWandEvolution.isEligible((Wand) it)){
				return EndWandEvolution.evolve((Wand) it);
			}
		}
		return null;
	}
}
