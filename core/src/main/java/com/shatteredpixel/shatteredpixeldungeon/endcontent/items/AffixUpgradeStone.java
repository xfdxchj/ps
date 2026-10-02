package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.RingAffix;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * END(二.13 原神地牢·词条升级): 词条强化石。
 *
 * <p>对一枚已有词条的戒指使用，从它的词条中**选择 1 条**强化：
 * 随机获得 +20% / +35% / +40% 的**乘算**提升，每条词条最多强化 3 次。
 *
 * <p>炼金：升级卷轴 + 强化符石 -> 3 个。
 */
public class AffixUpgradeStone extends Item {

	public static final String AC_USE = "USE";


	{
		image = ItemSpriteSheet.STONE_AUGMENTATION;
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
				return "选择要强化的戒指";
			}

			@Override
			public boolean itemSelectable(Item item){
				return item instanceof Ring;
			}

			@Override
			public void onSelect(Item item){
				if (!(item instanceof Ring)) return;
				final Ring ring = (Ring) item;
				if (ring.affixes == null || ring.affixes.length == 0){
					GLog.w("这枚戒指还没有词条（每强化 3 级获得 1 条，最多 3 条）。");
					return;
				}

				final String[] opts = new String[ring.affixes.length];
				for (int i = 0; i < ring.affixes.length; i++){
					opts[i] = (i + 1) + ". " + ring.affixes[i].label
							+ "  " + currentText(ring, i)
							+ "  （已强化 " + ring.affixUpgrades(i) + "/3）";
				}

				GameScene.show(new WndOptions("选择要强化的词条", "强化随机获得 +20% / +35% / +40%（乘算），每条词条最多 3 次。", opts){
					@Override
					protected void onSelect(int index){
						if (ring.affixUpgrades(index) >= RingAffix.MAX_UPGRADES){
							GLog.w("这条词条已经强化 3 次，不能再强化了。");
							return;
						}
						float bonus = RingAffix.UPGRADE_BONUSES[Random.Int(RingAffix.UPGRADE_BONUSES.length)];
						if (!ring.upgradeAffix(index, bonus)){
							GLog.w("强化失败。");
							return;
						}
						int pct = Math.round(bonus * 100f);
						GLog.p("词条强化成功：" + ring.affixes[index].label
								+ " +" + pct + "%（已强化 " + ring.affixUpgrades(index) + "/3）");
						AffixUpgradeStone.this.detach(hero.belongings.backpack);
						updateQuickslot();
					}
				});
			}
		});
	}

	/** 词条当前数值的展示文本。 */
	private static String currentText(Ring ring, int i){
		RingAffix a = ring.affixes[i];
		float v = ring.affixValue(i);
		if (a == RingAffix.REGEN){
			return "每回合 " + trim(v);
		}
		return "+" + Math.round(v * 100f) + "%";
	}

	private static String trim(float v){
		return (v == Math.round(v)) ? Integer.toString(Math.round(v)) : String.format("%.1f", v);
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	@Override
	public String desc(){
		return "一块能强化戒指词条的符石。\n\n"
				+ "对一枚已有词条的戒指使用，从它的词条中**选择 1 条**强化："
				+ "随机获得 **" + RingAffix.upgradeBonusText() + "** 的**乘算**提升，"
				+ "每条词条最多强化 **" + RingAffix.MAX_UPGRADES + " 次**。\n\n"
				+ "炼金配方：**升级卷轴 ×1 + 强化符石 ×1 -> 3 个**。";
	}

	/** 炼金：升级卷轴 + 强化符石 -> 3 个词条强化石（仅 230 挑战开启时可用）。 */
	public static class UpgradeRecipe extends Recipe.SimpleRecipe {
		public UpgradeRecipe(){
			inputs = new Class[]{ScrollOfUpgrade.class, StoneOfAugmentation.class};
			inQuantity = new int[]{1, 1};
			cost = 0;
			output = AffixUpgradeStone.class;
			outQuantity = 3;
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients){
			return ChallengeEffects.genshinEnabled() && super.testIngredients(ingredients);
		}
	}
}
