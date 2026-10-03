/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 炼金配方（战士侧）：把 破印(BrokenSeal) + 邪能碎片(MetalShard) + 分支特殊物品 -> 对应的进阶破印成品。
 *
 * END(修订): 文档所有者定稿的三支特殊物品：
 *    1. 血盾 BladeShieldSeal -> 治疗药水 PotionOfHealing
 *    2. 狂暴 BloodRageSeal   -> 复仇卷轴 ScrollOfRetribution
 *    3. 飞掷 FlyWeaponSeal   -> 50 液金 LiquidMetal
 * 基底固定为：破碎纹章(BrokenSeal) + 邪能碎片(MetalShard)。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.BladeShieldSeal;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.BloodRageSeal;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.FlyWeaponSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class EvolveSealRecipe extends Recipe {

	/** 飞掷分支需要的液金数量。 */
	public static final int LIQUID_METAL_COST = 50;

	/** 分支特殊物品 -> 对应破印成品类型。 */
	private Class<? extends BrokenSeal> pickClass( Item special ){
		if (special instanceof PotionOfHealing)     return BladeShieldSeal.class;
		if (special instanceof ScrollOfRetribution) return BloodRageSeal.class;
		if (special instanceof LiquidMetal)         return FlyWeaponSeal.class;
		return null;
	}

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		boolean metal = false;
		BrokenSeal source = null;
		boolean want = false;
		for (Item it : ingredients){
			if (it == null || it.cursed) return false;
			if (it instanceof BrokenSeal){
				if (source != null) return false;       //只允许一次一块原版破印
				source = (BrokenSeal) it;
			} else if (it instanceof MetalShard){
				metal = true;
			} else {
				if (pickClass(it) == null) return false;
				//液金要求数量足够
				if (it instanceof LiquidMetal && it.quantity() < LIQUID_METAL_COST) return false;
				want = true;
			}
		}
		return source != null && metal && want;
	}

	@Override public int cost(ArrayList<Item> ingredients){ return 0; }

	private Class<? extends BrokenSeal> direction(ArrayList<Item> ingredients){
		if (ingredients == null) return null;
		for (Item it : ingredients){
			if (it instanceof BrokenSeal || it instanceof MetalShard) continue;
			Class<? extends BrokenSeal> p = pickClass(it);
			if (p != null) return p;
		}
		return null;
	}

	@Override public Item sampleOutput(ArrayList<Item> ingredients){
		Class<? extends BrokenSeal> out = direction(ingredients);
		if (out == null) return null;
		try {
			return out.newInstance();
		} catch (Exception e){ return null; }
	}

	@Override public Item brew(ArrayList<Item> ingredients){
		if (!testIngredients(ingredients)) return null;
		Class<? extends BrokenSeal> outCls = direction(ingredients);
		if (outCls == null) return null;

		BrokenSeal out;
		try {
			out = outCls.newInstance();
		} catch (Exception e){ return null; }

		out.identify();
		Catalog.setSeen(out.getClass());

		for (Item it : ingredients){
			if (it instanceof LiquidMetal){
				//液金按 50 滴消耗
				int left = it.quantity() - LIQUID_METAL_COST;
				if (left <= 0){
					it.detachAll(Dungeon.hero.belongings.backpack);
				} else {
					it.quantity(left);
				}
			} else if (it instanceof MetalShard
					|| it instanceof PotionOfHealing
					|| it instanceof ScrollOfRetribution){
				consumeOne(it);
			} else if (it instanceof BrokenSeal){
				it.detachAll(Dungeon.hero.belongings.backpack);
			}
		}
		GLog.i("炼成：" + out.title());
		return out;
	}

	/** 消耗一件材料；数量减到 0 时显式移除。 */
	private static void consumeOne(Item it) {
		int left = it.quantity() - 1;
		if (left <= 0) {
			it.detachAll(Dungeon.hero.belongings.backpack);
		} else {
			it.quantity(left);
		}
	}
}
