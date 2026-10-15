/*
 * 破碎的地牢 (End fork) — 无尽体系：鉴定卷轴。
 *
 * 由「预知秘卷 + 8 能量」在炼金炉合成，使用后鉴定背包里
 * 所有武器、护甲与投掷物的种类与诅咒状态。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Identification;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfDivination;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class ScrollOfAppraisal extends Scroll {

	{
		image = ItemSpriteSheet.SCROLL_KAUNAN; //临时图标，之后换专属贴图
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "鉴定卷轴"; }
	@Override public String info(){
		return "无尽炼金产物。展开后照亮背包里所有**武器、护甲与投掷物**，"
				+ "同时揭示它们是否带有诅咒。\n\n炼金：**预知秘卷 ×1 + 8 能量**。";
	}
	@Override public String desc(){ return info(); }
	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	@Override
	public void doRead(){
		detach(curUser.belongings.backpack);
		curUser.sprite.parent.add(new Identification(curUser.sprite.center().offset(0, -16)));
		Sample.INSTANCE.play(Assets.Sounds.READ);

		int n = 0;
		ArrayList<Item> items = new ArrayList<>(curUser.belongings.backpack.items);
		for (Item it : items){
			if (it instanceof Weapon || it instanceof Armor){
				it.identify();
				it.cursedKnown = true;
				n++;
			}
		}
		GLog.i("鉴定卷轴照亮了 " + n + " 件装备。");

		readAnimation();
		identify();
	}

	/** 炼金：预知秘卷 + 8 能量。 */
	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			return ingredients.size() == 1 && ingredients.get(0) instanceof ScrollOfDivination;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 8;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			for (Item i : ingredients) i.quantity(i.quantity() - 1);
			return new ScrollOfAppraisal();
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new ScrollOfAppraisal();
		}
	}
}
