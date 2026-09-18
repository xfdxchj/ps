/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 炼金配方（盗贼侧）：把 基础刺杀匕首(AssassinDagger) + 邪能碎片(MetalShard) + 方向料
 * → 锻成「刺杀·三叉戟」，一次只产一支。
 *
 * END(修订): 原设计有**三个方向**（三叉/传送/处决），由三种不同材料决定产出。
 * 按文档所有者要求改为**只有一种成品**：任意一种方向料都产出三叉戟。
 * DaggerTeleport / DaggerExecution 两个类保留（图鉴与旧存档兼容），但不再能锻出。
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

	/**
	 * 方向料 → 对应进阶匕首成品。
	 *
	 * <p>END(修订): 按文档所有者要求，**只保留一种成品**（刺杀·三叉戟），
	 * 去掉原来的"传送"与"处决"两个方向。
	 *
	 * <p>原因：三方向料的设计让一件基础匕首能锻成三种不同成品，
	 * 实际玩起来是"三选一"，而需求是**只有一种**。
	 *
	 * <p>注意：{@code DaggerTeleport} / {@code DaggerExecution} 两个**类仍然保留**
	 * （图鉴、存档里的旧物品还要能反序列化），只是不再能通过配方产出。
	 *
	 * <p>现在任意一种"方向料"都可以触发锻造，统一产出三叉戟。
	 */
	private Class<? extends MissileWeapon> pickClass( Item special ){
		//只要是可用的方向料，一律产出三叉戟
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste
				|| special instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation
				|| special instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution) {
			return DaggerTrident.class;
		}
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
