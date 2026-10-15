/*
 * 破碎的地牢 (End fork) — 后缀卷轴。
 *
 * 炼金：升级卷轴 + 任意符石 + 邪能碎片 -> 后缀卷轴。
 * 对当前武器使用后，随机附加 1 个后缀技能（每把武器最多 1 个）。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Identification;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.WeaponSuffix;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class SuffixScroll extends Scroll {

	{
		image = ItemSpriteSheet.SCROLL_KAUNAN; //临时图标
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "后缀卷轴"; }
	@Override public String info(){
		return "把卷轴按在**当前武器**上，随机附加 1 个后缀技能。"
				+ "每把武器最多 1 个后缀。\n\n炼金：**升级卷轴 + 任意符石 + 邪能碎片**。";
	}
	@Override public String desc(){ return info(); }
	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	@Override
	public void doRead(){
		com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon k = curUser.belongings.attackingWeapon();
		Weapon w = (k instanceof Weapon) ? (Weapon) k : null;
		if (w == null){
			k = curUser.belongings.weapon();
			w = (k instanceof Weapon) ? (Weapon) k : null;
		}
		if (w == null){
			GLog.w("没有可附加后缀的武器。");
			return;
		}
		if (w.suffix != null){
			GLog.w("这把武器已经有后缀了。");
			return;
		}
		detach(curUser.belongings.backpack);
		curUser.sprite.parent.add(new Identification(curUser.sprite.center().offset(0, -16)));
		Sample.INSTANCE.play(Assets.Sounds.READ);

		WeaponSuffix s = WeaponSuffix.random();
		w.suffix = s.name();
		GLog.i("武器获得后缀：" + s.label);

		readAnimation();
		identify();
		Item.updateQuickslot();
	}

	/** 炼金：升级卷轴 + 任意符石 + 邪能碎片。 */
	public static class Recipe3 extends Recipe {
		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.suffixEnabled()) return false;
			if (ingredients.size() != 3) return false;
			boolean scroll = false, stone = false, shard = false;
			for (Item it : ingredients){
				if (it instanceof ScrollOfUpgrade) scroll = true;
				else if (it instanceof Runestone) stone = true;
				else if (it instanceof MetalShard) shard = true;
				else return false;
			}
			return scroll && stone && shard;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) { return 0; }

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			for (Item i : ingredients) i.quantity(i.quantity() - 1);
			return new SuffixScroll();
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new SuffixScroll();
		}
	}
}
