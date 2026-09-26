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
			Item result = sampleOutput(null);

			//==== END(挑战 200 炼金术士): 13% 概率获得两份 ====
			//文档所有者定稿："制作秘药 13% 获得两份。"
			//
			//接在**所有**配方共用的 brew() 上 —— 不必逐个配方去改。
			//只对秘药类（Elixir）生效：原表说的是"制作秘药"，
			//不该把卷轴/炸弹的合成也算进去。
			if (result instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions
					.exotic.ExoticPotion
					|| result instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions
							.elixirs.Elixir
					|| (result != null && result.getClass().getSimpleName().contains("Elixir"))) {
				if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.rollAlchemist()) {
					result.quantity(result.quantity() * 2);
					com.shatteredpixel.shatteredpixeldungeon.utils.GLog
							.p("配方多出了一份。");
				}
			}

			return result;
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
			new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolveDaggerRecipe(),
			//END(顶级装备体系): 2 无尽锭 + 对应核心 + 样品装备 → 顶级装备
			new com.shatteredpixel.shatteredpixeldungeon.endcontent.items
					.InfinityRecipes.ToEquipment()
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
		//END(顶级装备体系): 分解装备得碎片 / 15级以上装备得核心 / 100碎片得锭
		new com.shatteredpixel.shatteredpixeldungeon.endcontent.items
				.InfinityRecipes.Decompose(),
		new com.shatteredpixel.shatteredpixeldungeon.endcontent.items
				.InfinityRecipes.ToCore(),
		new com.shatteredpixel.shatteredpixeldungeon.endcontent.items
				.InfinityRecipes.ShardToIngot(),
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
		//==== END(ReReARPD gun port): 强化符石 + 20 液态金属 -> 枪械改造工具 ====
		new GunSmithingTool.ToolRecipe(),
	};
	
	private static Recipe[] threeIngredientRecipes = new Recipe[]{
		new Potion.SeedToPotion(),
		new StewedMeat.threeMeat(),
		new MeatPie.Recipe()
	};
	
	//END 法杖蜕变：+8 法杖 ＋ 强化符石 → 对应进化法杖（由一条动态配方覆盖全部 13 把）
	private static Recipe[] endVariantRecipes = new Recipe[]{
		new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolveWandRecipe(),
		//==== END(ReReARPD gun port): T5 枪 + 强化符石 + 星花种子 -> T6 战术型 ====
		new com.shatteredpixel.shatteredpixeldungeon.endcontent.gun.EvolveGunRecipe(),
		//==== END(删除·童话残片的炼金路径) ====
		//文档所有者定稿："（残片 → 3 残页 → 炼金 → 少女 → 去 999 层）
		//这个方法去掉，因为如果已有碎片 1 捡到 2 回变 2 个 1，
		//即使修复，占 9 个格子不好。"
		//
		//原因：9 种残片各占一格（还得再加残页、少女），
		//背包里光这一套就吃掉十几个格子 —— 而玩家真正需要的
		//只是"我集齐了没有"这一个信息。
		//
		//现在改走《未知的童话书》：开局给一本书，捡到残片时
		//书自动记一页（残片本身不留背包），集齐 9 页后直接用书去 999 层。
		//见 UnknownFairyTale。
		//
		//两条配方（FairyFragmentRecipe / GirlRecipe）保留在代码里不删 ——
		//万一以后要恢复，取消下面两行的注释即可。
		//new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm.FairyFragmentRecipe(),
		//new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
		//		.FairyFragmentRecipe.GirlRecipe()
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
			//==== END(修复·法杖炼金进阶失效) ====
			//文档所有者反馈："爆裂法杖的进阶[应该]和原版法杖的进阶一样，是指靠炼金进阶。"
			//根因：EvolveWandRecipe 需要 **3 样**（法杖 + 强化符石 + 星露花种子），
			//但这里只在 size==2 分支里遍历 endVariantRecipes —— 3 材料时根本没人查它，
			//于是 14 把法杖（含爆裂法杖）的炼金进阶**全部不可用**。
			for (Recipe recipe : endVariantRecipes){
				if (recipe.testIngredients(ingredients)){
					result.add(recipe);
				}
			}
		}
		
		return result;
	}
	
	public static boolean usableInRecipe(Item item){
		//==== END(修复·214 无尽碎片：装备进不了炼金炉) ====
		//文档所有者反馈："无尽炼金不能使用，无法获得无尽碎片。"
		//根因：炼金界面（AlchemyScene）用本方法过滤可投入的材料，而下面那段
		//只放行"可升级的投掷物 + 灵能弓"，近战武器/护甲/戒指全被挡在炉外 ——
		//无尽碎片的三条配方（分解装备 / 15 级装备出核心 / 样品装备合顶级装备）永远不成立。
		//修法：勾选 214 时把无尽体系要吃的三类装备按同样规则放行（已鉴定且未诅咒）。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
			.ChallengeEffects.infinityShardEnabled()
			&& (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon
				|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor
				|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)) {
		return item.cursedKnown && !item.cursed;
		}
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


