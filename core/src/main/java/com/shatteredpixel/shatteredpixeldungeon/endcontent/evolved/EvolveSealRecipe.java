/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 炼金配方（战士侧）：把 破印(BrokenSeal) + 邪能碎片(MetalShard) + 其三方向料之一 → 锻成对应的进阶破印成品。
 *
 * “方向料”采用现有物品来区分三支（与灵弓三向配方同构，用户指定三件现成料两族共用）：
 *    ① 血盾 BladeShieldSeal → 速度药水 PotionOfHaste（Survival / 快速护体）
 *    ② 狂暴 BloodRageSeal   → 浮空药水 PotionOfLevitation（mobility? 归于狂态，按需求保留）
 *    ③ 飞掷 FlyWeaponSeal   → 复仇卷轴 ScrollOfRetribution（Toss / 远程回敬）
 * （映射为“易改注释”：若你想换任意方向料，只改本类 pickClass 一个 switch。）
 *
 * 输入 3 样：1 原版破印(干净/已鉴定/未诅咒)、1 颗 MetalShard、上述三方向料之一。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.BladeShieldSeal;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.BloodRageSeal;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.FlyWeaponSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class EvolveSealRecipe extends Recipe {

	/** 方向料（仓库现成物）→ 对应破印成品类型。易改之处就在这。 */
	private Class<? extends BrokenSeal> pickClass( Item special ){
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste)   return BladeShieldSeal.class; //速度→血盾
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation) return BloodRageSeal.class; //浮空→狂暴
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution) return FlyWeaponSeal.class; //复仇→飞掷
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
				if (pickClass(it) != null) want = true;
				else return false;
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
			if (it instanceof MetalShard){
				it.quantity(it.quantity() - 1);
			} else if (it instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste
					|| it instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation
					|| it instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution){
				it.quantity(it.quantity() - 1);
			} else if (it instanceof BrokenSeal){
				it.quantity(0); //原版破印归零 → 炉内当空气移走，避免同包混两把
			}
		}
		GLog.i("炼成破印·进阶: " + out.title() + " 已记入图鉴/日志。");
		return out;
	}
}
