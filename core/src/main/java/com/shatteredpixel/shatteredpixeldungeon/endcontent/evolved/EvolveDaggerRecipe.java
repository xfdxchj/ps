/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 炼金配方（盗贼侧）：把 基础刺杀匕首(AssassinDagger) + 邪能碎片(MetalShard) + 其三方向料之一
 * → 锻成对应的“进阶匕首成品”（三叉/传送/处决），一次只产一支；三支成品不可并存。
 *
 * 方向料沿用与破印族同一套“两族共用三料”（同构 EvolveSpiritBowRecipe），仅换基底与目标映射：
 *    ① 三叉 DaggerTrident    → 速度药水  PotionOfHaste        （手感快速并自动回旋）
 *    ② 传送 DaggerTeleport   → 浮空药水  PotionOfLevitation   （传送/位移）
 *    ③ 处决 DaggerExecution  → 复仇卷轴  ScrollOfRetribution  （处决裁决）
 * （映射易改：只动本类 pickClass。）
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.AssassinDagger;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.DaggerExecution;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.DaggerTeleport;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.DaggerTrident;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class EvolveDaggerRecipe extends Recipe {

	/** 方向料 → 对应进阶匕首成品。易改之处就在这。 */
	private Class<? extends MissileWeapon> pickClass( Item special ){
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste)   return DaggerTrident.class;   //速度→三叉(高数值,不回旋)
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation) return DaggerTeleport.class; //浮空→传送
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution) return DaggerExecution.class; //复仇→处决
		return null;
	}

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		boolean metal = false;
		boolean hasBase = false;
		boolean want = false;
		for (Item it : ingredients){
			if (it == null || it.cursed) return false;
			if (it instanceof AssassinDagger){
				//防止拿已经锻好的成品匕首当基底再互锻
				if (it instanceof DaggerTrident
						|| it instanceof DaggerTeleport
						|| it instanceof DaggerExecution){
					return false;
				}
				hasBase = true;
			} else if (it instanceof MetalShard){
				metal = true;
			} else {
				if (pickClass(it) != null) want = true;
				else return false;
			}
		}
		return hasBase && metal && want;
	}

	@Override public int cost(ArrayList<Item> ingredients){ return 0; }

	private Class<? extends MissileWeapon> direction(ArrayList<Item> ingredients){
		if (ingredients == null) return null;
		for (Item it : ingredients){
			if (it instanceof AssassinDagger || it instanceof MetalShard) continue;
			Class<? extends MissileWeapon> p = pickClass(it);
			if (p != null) return p;
		}
		return null;
	}

	@Override public Item sampleOutput(ArrayList<Item> ingredients){
		Class<? extends MissileWeapon> out = direction(ingredients);
		if (out == null) return null;
		try {
			return out.newInstance();
		} catch (Exception e){ return null; }
	}

	@Override public Item brew(ArrayList<Item> ingredients){
		if (!testIngredients(ingredients)) return null;
		Class<? extends MissileWeapon> outCls = direction(ingredients);
		if (outCls == null) return null;

		MissileWeapon out;
		try {
			out = outCls.newInstance();
		} catch (Exception e){ return null; }

		out.identify();
		Catalog.setSeen(out.getClass());

		for (Item it : ingredients){
			if (it instanceof MetalShard
					|| it instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste
					|| it instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation
					|| it instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution){
				it.quantity(it.quantity() - 1);
			} else if (it instanceof AssassinDagger){
				it.quantity(0); //基础匕首归零 → 移走，避免与成品共存
			}
		}
		GLog.i("炼成刺杀匕首·成品: " + out.title() + " 已记入图鉴/日志。");
		return out;
	}
}
