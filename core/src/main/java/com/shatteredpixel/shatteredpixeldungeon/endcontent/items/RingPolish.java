package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;

import java.util.ArrayList;

/**
 * END(230 原神地牢): 洗练石。
 * 对一枚戒指使用可重掷它的全部词条。炼金：升级卷轴 + 强化符石 → 50 个。
 */
public class RingPolish extends Item {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.ARCANE_RESIN;
		defaultAction = AC_USE;
		stackable = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_USE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!AC_USE.equals(action)) return;
		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt(){
				return "选择要洗练的戒指";
			}

			@Override
			public boolean itemSelectable(Item item){
				return item instanceof Ring;
			}

			@Override
			public void onSelect(Item item){
				if (!(curItem instanceof RingPolish)) return;
				if (!(item instanceof Ring)) return;
				Ring ring = (Ring) item;
				if (ring.affixes == null || ring.affixes.length == 0){
					GLog.w("这枚戒指还没有词条（每强化 3 级获得 1 条，最多 3 条）。");
					return;
				}
				ring.rerollAffixes();
				GLog.p("洗练完成，戒指词条已重掷。");
				RingPolish.this.detach(hero.belongings.backpack);
				updateQuickslot();
			}
		});
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	/** 炼金：升级卷轴 + 强化符石 → 50 个洗练石（仅 230 挑战开启时可用）。 */
	public static class PolishRecipe extends Recipe.SimpleRecipe {
		public PolishRecipe(){
			inputs = new Class[]{ScrollOfUpgrade.class, StoneOfAugmentation.class};
			inQuantity = new int[]{1, 1};
			cost = 0;
			output = RingPolish.class;
			outQuantity = 50;
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients){
			return ChallengeEffects.genshinEnabled() && super.testIngredients(ingredients);
		}
	}
}
