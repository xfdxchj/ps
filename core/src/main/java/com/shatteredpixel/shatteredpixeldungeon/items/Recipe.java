/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MeatPie;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StewedMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.BlizzardBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.CausticBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.InfernalBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfIcyTouch;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfToxicEssence;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.BeaconOfReturning;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.CurseInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.PhaseShift;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ReclaimTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Recycle;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TelekineticGrab;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.WildEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public abstract class Recipe {
	
	public abstract boolean testIngredients(ArrayList<Item> ingredients);
	
	public abstract int cost(ArrayList<Item> ingredients);
	
	public abstract Item brew(ArrayList<Item> ingredients);
	
	public abstract Item sampleOutput(ArrayList<Item> ingredients);
	
	//subclass for the common situation of a recipe with static inputs and outputs
	public static abstract class SimpleRecipe extends Recipe {
		
		//*** These elements must be filled in by subclasses
		protected Class<?extends Item>[] inputs; //each class should be unique
		protected int[] inQuantity;
		
		protected int cost;
		
		protected Class<?extends Item> output;
		protected int outQuantity;
		//***
		
		//gets a simple list of items based on inputs
		public ArrayList<Item> getIngredients() {
			ArrayList<Item> result = new ArrayList<>();
			for (int i = 0; i < inputs.length; i++) {
				Item ingredient = Reflection.newInstance(inputs[i]);
				ingredient.quantity(inQuantity[i]);
				result.add(ingredient);
			}
			return result;
		}
		
		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			
			int[] needed = inQuantity.clone();
			
			for (Item ingredient : ingredients){
				if (!ingredient.isIdentified()) return false;
				for (int i = 0; i < inputs.length; i++){
					if (ingredient.getClass() == inputs[i]){
						needed[i] -= ingredient.quantity();
						break;
					}
				}
			}
			
			for (int i : needed){
				if (i > 0){
					return false;
				}
			}
			
			return true;
		}
		
		public int cost(ArrayList<Item> ingredients){
			return cost;
		}
		
		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			int[] needed = inQuantity.clone();
			
			for (Item ingredient : ingredients){
				for (int i = 0; i < inputs.length; i++) {
					if (ingredient.getClass() == inputs[i] && needed[i] > 0) {
						if (needed[i] <= ingredient.quantity()) {
							ingredient.quantity(ingredient.quantity() - needed[i]);
							needed[i] = 0;
						} else {
							needed[i] -= ingredient.quantity();
							ingredient.quantity(0);
						}
					}
				}
			}
			
			//sample output and real output are identical in this case.
			return sampleOutput(null);
		}
		
		//ingredients are ignored, as output doesn't vary
		public Item sampleOutput(ArrayList<Item> ingredients){
			try {
				Item result = Reflection.newInstance(output);
				result.quantity(outQuantity);
				return result;
			} catch (Exception e) {
				ShatteredPixelDungeon.reportException( e );
				return null;
			}
		}
	}
	
	
	//*******
	// Static members
	//*******

	private static Recipe[] variableRecipes = new Recipe[]{
			//END 灵能弓改造：任意槽数均可判定的两条动态配方(核心 + 四选一锻造)
			new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.SpiritBowCoreRecipe(),
			new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolveSpiritBowRecipe(),
			//END 装备进化族：破印进阶与刺杀匕首进阶(3 料,任意槽数判定)
			new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolveSealRecipe(),
			new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolveDaggerRecipe()
	};
	
	private static Recipe[] oneIngredientRecipes = new Recipe[]{
		new Scroll.ScrollToStone(),
		new ExoticPotion.PotionToExotic(),
		new ExoticScroll.ScrollToExotic(),
		new ArcaneResin.Recipe(),
		new LiquidMetal.Recipe(),
		new BlizzardBrew.Recipe(),
		new InfernalBrew.Recipe(),
		new AquaBrew.Recipe(),
		new ShockingBrew.Recipe(),
		new ElixirOfDragonsBlood.Recipe(),
		new ElixirOfIcyTouch.Recipe(),
		new ElixirOfToxicEssence.Recipe(),
		new ElixirOfMight.Recipe(),
		new ElixirOfFeatherFall.Recipe(),
		new MagicalInfusion.Recipe(),
		new BeaconOfReturning.Recipe(),
		new PhaseShift.Recipe(),
		new Recycle.Recipe(),
		new TelekineticGrab.Recipe(),
		new SummonElemental.Recipe(),
		new StewedMeat.oneMeat(),
		new TrinketCatalyst.Recipe(),
		new Trinket.UpgradeTrinket()
	};
	
	private static Recipe[] twoIngredientRecipes = new Recipe[]{
		new Blandfruit.CookFruit(),
		new Bomb.EnhanceBomb(),
		new UnstableBrew.Recipe(),
		new CausticBrew.Recipe(),
		new ElixirOfArcaneArmor.Recipe(),
		new ElixirOfAquaticRejuvenation.Recipe(),
		new ElixirOfHoneyedHealing.Recipe(),
		new UnstableSpell.Recipe(),
		new Alchemize.Recipe(),
		new CurseInfusion.Recipe(),
		new ReclaimTrap.Recipe(),
		new WildEnergy.Recipe(),
		new StewedMeat.twoMeat(),
		//END(port from Arknights): 枪械弹药 —— 玩家反馈"子弹没有获得方式"。
		//方舟原版写好了配方类，却从未注册进炼金配方表，所以炼金釜里根本找不到它们。
		//   普通弹匣 = 任意符文石 + 任意种子
		//   高级弹匣 = 普通弹匣 + 金属碎片
		new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.NormalMagazine.Recipe(),
		new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.UpMagazine.UpMagazineRecipe()
	};
	
	private static Recipe[] threeIngredientRecipes = new Recipe[]{
		new Potion.SeedToPotion(),
		new StewedMeat.threeMeat(),
		new MeatPie.Recipe()
	};
	
	//END 法杖蜕变：+8 法杖 ＋ 强化符石 → 对应进化法杖（由一条动态配方覆盖全部 13 把）
	private static Recipe[] endVariantRecipes = new Recipe[]{
		new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolveWandRecipe()
	};
	
	public static ArrayList<Recipe> endVariantRecipeList(){
		ArrayList<Recipe> result = new ArrayList<>();
		for (Recipe r : endVariantRecipes) result.add(r);
		return result;
	}
	
	public static ArrayList<Recipe> findRecipes(ArrayList<Item> ingredients){

		ArrayList<Recipe> result = new ArrayList<>();

		for (Recipe recipe : variableRecipes){
			if (recipe.testIngredients(ingredients)){
				result.add(recipe);
			}
		}

		if (ingredients.size() == 1){
			for (Recipe recipe : oneIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					result.add(recipe);
				}
			}
			
		} else if (ingredients.size() == 2){
			for (Recipe recipe : twoIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					result.add(recipe);
				}
			}
			//END: 法杖蜕变配方（源法杖+强化符石）也在 2 材料情形参与
			for (Recipe recipe : endVariantRecipes){
				if (recipe.testIngredients(ingredients)){
					result.add(recipe);
				}
			}
			
		} else if (ingredients.size() == 3){
			for (Recipe recipe : threeIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					result.add(recipe);
				}
			}
		}
		
		return result;
	}
	
	public static boolean usableInRecipe(Item item){
		//only upgradeable thrown weapons and wands allowed among equipment items
		if (item instanceof EquipableItem){
			return item.cursedKnown && !item.cursed &&
					//END 灵能弓改造：未受诅咒的神弓(及共子类)可作为炼金原料
					((item instanceof MissileWeapon && item.isUpgradable())
							|| item instanceof SpiritBow);
		} else if (item instanceof Wand) {
			return item.cursedKnown && !item.cursed;
		} else {
			//other items can be unidentified, but not cursed
			return !item.cursed;
		}
	}
}


