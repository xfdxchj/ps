/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 炼金配方：把 灵能弓 + 灵能核心 + 任一“特殊材料” → 锻造成对应的 三选一 成品弓。
 *
 * “特殊材料”用本 fork 原装物品表示(Huntress 起步弓进化)，放入哪个、便锻成哪一把：
 *  ① 附魔灵弓 → 升级卷轴 ScrollOfUpgrade
 *  ② 雷鸣灵弓 → 雷鸣魔药 ShockingBrew
 *  ③ 唤魔灵弓 → 唤魔晶柱 SummonElemental
 * (④ 奥术弓已按需求移除，本配方不再含奥术分支)
 *
 * 输入 3 样：1 把 干净/已鉴定/未诅咒 的 灵能弓( SpiritBow 及其已成品子类 除外，
 *   只允许原版起始 SpiritBow),1 颗 灵能核心(SpiritBowCore)，1 份上述三种特殊物品之一。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.items.SpiritBowCore;

import java.util.ArrayList;

public class EvolveSpiritBowRecipe extends Recipe {

	private SpiritBow source = null;

	/** 当前投入的特殊物品类 → 对应输出成品弓类(无④奥术)。 */
	private Class<? extends SpiritBow> pickClass( Item special ){
		if (special instanceof ScrollOfUpgrade)    return EndSpiritBowMight.class;
		if (special instanceof ShockingBrew)       return EndSpiritBowStorm.class;
		if (special instanceof SummonElemental)    return EndSpiritBowSummon.class;
		return null;
	}

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		boolean core = false;
		Class<? extends SpiritBow> want = null;
		SpiritBow bow = null;
		for (Item it : ingredients){
			if (it == null || !it.isIdentified() || it.cursed) continue;
			if (it instanceof SpiritBow) {
				//只允许“原版起始灵能弓”作为基底；已锻造过的三把成品弓不可再当原料进场
				if (it instanceof EndSpiritBowMight
						|| it instanceof EndSpiritBowStorm
						|| it instanceof EndSpiritBowSummon){
					return false;
				}
				bow = (SpiritBow) it;
			}
			else if (it instanceof SpiritBowCore) core = true;
			else {
				Class<? extends SpiritBow> p = pickClass(it);
				if (p != null) want = p;
				//其它无关物品 → 视为不匹配,立即失败
				else return false;
			}
		}
		if (bow == null || !core || want == null) return false;
		source = bow;
		return true;
	}

	@Override
	public int cost(ArrayList<Item> ingredients) {
		return 0;
	}

	@Override
	public Item sampleOutput(ArrayList<Item> ingredients) {
		Class<? extends SpiritBow> outCls = pickDirection(ingredients);
		if (outCls == null) return null;
		try {
			return outCls.newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	/** 从已投入原料里找出被当成“特殊料”的那个 → 它对应哪把成品弓。 */
	private Class<? extends SpiritBow> pickDirection(ArrayList<Item> ingredients){
		if (ingredients == null) return null;
		for (Item it : ingredients){
			if (it == null) continue;
			if (it instanceof SpiritBow || it instanceof SpiritBowCore) continue;
			Class<? extends SpiritBow> p = pickClass(it);
			if (p != null) return p;
		}
		return null;
	}

	@Override
	public Item brew(ArrayList<Item> ingredients) {
		if (!testIngredients(ingredients) || source == null) return null;

		//根据“特殊料”→ 选要锻造的成品弓方向
		Class<? extends SpiritBow> outCls = pickDirection(ingredients);
		if (outCls == null) return null;

		SpiritBow out;
		try {
			out = outCls.newInstance();
		} catch (Exception e) {
			return null;
		}
		out.identify();
		//① 附魔灵弓自身是“随机附魔工匠”：每击掷随机附魔，不依赖身上静态附魔。
		//因此不再把源弓附魔强拷到①上(拷了也只当摆设)，故仅在非 Might 时结转源附魔：
		if (!(out instanceof EndSpiritBowMight) && source.enchantment != null){
			out.enchant( source.enchantment );
		}

		for (Item it : ingredients){
			if (it instanceof SpiritBowCore){
				it.quantity( it.quantity() - 1 );
			} else if (it instanceof ScrollOfUpgrade
					|| it instanceof ShockingBrew
					|| it instanceof SummonElemental){
				it.quantity( it.quantity() - 1 );
			} else if (it instanceof SpiritBow){
				it.quantity( 0 ); //源弓归零 → 炼金炉自动当空气并移除，避免同背包混出两把成品
			}
		}
		return out;
	}
}
