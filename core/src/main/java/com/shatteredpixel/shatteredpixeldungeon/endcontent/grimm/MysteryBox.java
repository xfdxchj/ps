/*
 * 破碎的地牢 (End fork) — 挑战 38「盲盒」的道具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * END(挑战 38 盲盒): 盲盒。
 *
 * <h3>原表效果</h3>
 * "商店可购买盲盒，随机获得物品"
 *
 * <h3>设计</h3>
 * 打开时**加权随机**给一件东西：
 * <ul>
 *   <li>60% 普通消耗品（药水/卷轴/种子/符石）</li>
 *   <li>30% 装备（武器/护甲/法杖/戒指）</li>
 *   <li>10% 好东西（升级卷轴 / 经验药水 / 点金石）</li>
 * </ul>
 *
 * <p>所以它**期望上是赚的**（商店价 100，普通消耗品也就值 30-60），
 * 但方差很大 —— 这就是"双刃剑"的倾向。
 */
public class MysteryBox extends Item {

	{
		//暂时复用"童话残页"的图标（盲盒图标以后再补）
		image = ItemSpriteSheet.GRIMM_FAIRY_FRAGMENT;
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "盲盒"; }

	@Override
	public String info(){
		return "一个封得严严实实的盒子。摇一摇，里面有东西在响。\n\n" +
				"打开后随机获得一件物品：\n" +
				"- **60%** 普通消耗品（药水 / 卷轴 / 种子 / 符石）\n" +
				"- **30%** 装备（武器 / 护甲 / 法杖 / 戒指）\n" +
				"- **10%** 稀罕物（升级卷轴 / 经验药水 / 点金石）\n\n" +
				"期望上是赚的，但你得先接受方差。";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }

	public static final String AC_OPEN = "OPEN";

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_OPEN);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_OPEN) || hero == null) return;

		Item reward = rollReward();
		if (reward == null) {
			GLog.w("盒子是空的。");
		} else {
			GLog.p("盒子里是：" + reward.name() + "！");
			reward.identify();
			reward.collect();
		}

		//消耗一个
		detach(hero.belongings.backpack);
		hero.spendAndNext(1f);
	}

	/**
	 * END(38): 掷一次奖励。
	 *
	 * <p>独立成 public static 是为了让测试能直接跑概率分布。
	 */
	public static Item rollReward() {
		int roll = Random.Int(100);

		if (roll < 60) {
			//---- 普通消耗品 ----
			switch (Random.Int(4)) {
				case 0: default:
					return Generator.randomUsingDefaults(Generator.Category.POTION);
				case 1:
					return Generator.randomUsingDefaults(Generator.Category.SCROLL);
				case 2:
					return Generator.randomUsingDefaults(Generator.Category.SEED);
				case 3:
					return Generator.randomUsingDefaults(Generator.Category.STONE);
			}
		} else if (roll < 90) {
			//---- 装备 ----
			switch (Random.Int(4)) {
				case 0: default:
					return Generator.randomUsingDefaults(Generator.Category.WEAPON);
				case 1:
					return Generator.randomUsingDefaults(Generator.Category.ARMOR);
				case 2:
					return Generator.randomUsingDefaults(Generator.Category.WAND);
				case 3:
					return Generator.randomUsingDefaults(Generator.Category.RING);
			}
		} else {
			//---- 稀罕物 ----
			switch (Random.Int(3)) {
				case 0: default:
					return new com.shatteredpixel.shatteredpixeldungeon.items.scrolls
							.ScrollOfUpgrade();
				case 1:
					return new com.shatteredpixel.shatteredpixeldungeon.items.potions
							.PotionOfExperience();
				case 2:
					return new com.shatteredpixel.shatteredpixeldungeon.items.stones
							.StoneOfEnchantment();
			}
		}
	}
}
