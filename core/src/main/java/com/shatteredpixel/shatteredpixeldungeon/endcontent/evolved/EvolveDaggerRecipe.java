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
	 * <h3>END(修订·恢复三方向)</h3>
	 * 文档所有者定稿："刺杀匕首的进阶全部成了三叉戟，还是一个刺杀匕首变 3 个进阶"
	 * —— 要的是**三个方向各自产出各自的成品**：
	 * <pre>
	 *   加速药水   → 刺杀·三叉戟（DaggerTrident）
	 *   漂浮药水   → 刺杀·传送（DaggerTeleport）
	 *   报应卷轴   → 刺杀·处决（DaggerExecution）
	 * </pre>
	 *
	 * <p>先前那版把它们统一成三叉戟，是早期的一次误改，现已恢复。
	 */
	private Class<? extends MissileWeapon> pickClass( Item special ){
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions
				.PotionOfHaste) {
			return DaggerTrident.class;
		}
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions
				.PotionOfLevitation) {
			return DaggerTeleport.class;
		}
		if (special instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls
				.ScrollOfRetribution) {
			return DaggerExecution.class;
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
				//END(修复): 减到 0 时要真的移除 ——
				//quantity(0) 只改数字，物品还留在背包里。
				int left = it.quantity() - 1;
				if (left <= 0) {
					it.detachAll(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero
							.belongings.backpack);
				} else {
					it.quantity(left);
				}
			} else if (it instanceof AssassinDagger){
				//END(修复): 基础匕首必须**真正移除** ——
				//原来是 quantity(0)，只把数量设成 0，物品对象还留在背包里，
				//于是"基础匕首 + 成品"两把共存。
				it.detachAll(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero
						.belongings.backpack);
			}
		}
		GLog.i("炼成：" + out.title());
		return out;
	}
}
