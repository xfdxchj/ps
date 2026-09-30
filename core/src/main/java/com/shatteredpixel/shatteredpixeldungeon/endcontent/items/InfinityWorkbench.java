package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Game;

import java.util.ArrayList;

/**
 * END(无尽炼金): 无尽工作台。
 * 由炼金工具箱 + 1 个无尽锭合成；使用后打开 3×3（9 格）炼金界面。
 */
public class InfinityWorkbench extends Item {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.INFINITY_WORKBENCH;
		defaultAction = AC_USE;
		unique = true;
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
		AlchemyScene.assignWorkbench();
		Game.switchScene(AlchemyScene.class);
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	/** 炼金：炼金工具箱 + 无尽锭 → 无尽工作台。 */
	public static class BenchRecipe extends Recipe.SimpleRecipe {
		public BenchRecipe(){
			inputs = new Class[]{AlchemistsToolkit.class, InfinityMaterials.InfinityIngot.class};
			inQuantity = new int[]{1, 1};
			cost = 0;
			output = InfinityWorkbench.class;
			outQuantity = 1;
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients){
			return com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.infinityShardEnabled() && super.testIngredients(ingredients);
		}
	}
}
