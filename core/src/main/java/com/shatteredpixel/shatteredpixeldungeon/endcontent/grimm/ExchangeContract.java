/*
 * 破碎的地牢 (End fork) — 挑战 42「等价交换」的道具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * END(挑战 42 等价交换): 交换契约。
 *
 * <h3>原表效果</h3>
 * "用一物品交换**同类别**随机物品"
 *
 * <h3>同类别怎么定义</h3>
 * 按**大分类**判断：武器换武器、护甲换护甲、法杖换法杖、
 * 戒指换戒指、药水换药水、卷轴换卷轴、符石换符石。
 *
 * <p>找不到同类别的（比如种子、食物）就拒绝交换 ——
 * 与其乱给一件，不如明确告诉玩家"这个换不了"。
 *
 * <h3>等级保留</h3>
 * 换来的物品**保留原物品的等级与升级次数**。否则拿 +5 武器去换
 * 会变成纯粹的亏损，这条规则就没有"交换"的意义了。
 */
public class ExchangeContract extends Item {

	{
		image = ItemSpriteSheet.GRIMM_FAIRY_FRAGMENT;   //暂时复用
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "交换契约"; }

	@Override
	public String info(){
		return "一张写满小字的羊皮纸。签名处空着。\n\n" +
				"- 使用后选择一件物品\n" +
				"- 随机换成**同类别**的另一件物品\n" +
				"- **等级保留**（+3 的武器换来的也是 +3）\n" +
				"- 种子、食物等没有同类别的物品**无法交换**";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }

	public static final String AC_EXCHANGE = "EXCHANGE";

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_EXCHANGE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_EXCHANGE) || hero == null) return;

		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.getBag(
						com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.adapt(
								new com.shatteredpixel.shatteredpixeldungeon.windows.WndBag
										.Listener() {
									@Override
									public void onSelect(Item item) {
										if (item == null) return;
										exchange(hero, item);
									}
								})));
	}

	/**
	 * END(42): 执行一次交换。
	 *
	 * @return 换来的物品；无法交换时返回 null
	 */
	public static Item exchange(Hero hero, Item item) {
		if (hero == null || item == null) return null;

		Item replacement = rollSameCategory(item);
		if (replacement == null) {
			GLog.w("找不到与「" + item.name() + "」同类别的东西。");
			return null;
		}

		//保留等级（只对可升级物品有意义）
		if (item.isUpgradable() && replacement.isUpgradable()) {
			replacement.level(item.level());
		}
		replacement.identify();

		//换掉
		item.detach(hero.belongings.backpack);
		replacement.collect();

		GLog.i("「" + item.name() + "」变成了「" + replacement.name() + "」。");

		//消耗一张契约
		ExchangeContract c = hero.belongings.getItem(ExchangeContract.class);
		if (c != null) com.shatteredpixel.shatteredpixeldungeon.endcontent.ItemConsume.consumeOne(c);

		return replacement;
	}

	/**
	 * END(42): 按"大类别"随机换一件。
	 *
	 * @return 同类别的随机物品；该类别没有对应池时返回 null
	 */
	public static Item rollSameCategory(Item item) {
		try {
			Class<?> pool = null;
			com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category cat = null;

			if (item instanceof Weapon) {
				cat = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.Category.WEAPON;
			} else if (item instanceof Armor) {
				cat = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.Category.ARMOR;
			} else if (item instanceof Wand) {
				cat = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.Category.WAND;
			} else if (item instanceof Ring) {
				cat = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.Category.RING;
			} else if (item instanceof Potion) {
				cat = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.Category.POTION;
			} else if (item instanceof Scroll) {
				cat = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.Category.SCROLL;
			} else if (item instanceof Runestone) {
				cat = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.Category.STONE;
			}

			if (cat == null) return null;

			//最多试 20 次，避免随机到"同一件"或拿不到实例
			for (int i = 0; i < 20; i++) {
				Item out = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.randomUsingDefaults(cat);
				if (out == null) continue;
				if (out.getClass() == item.getClass()) continue;   //同一件不算交换
				return out;
			}
			return null;
		} catch (Throwable t) {
			return null;
		}
	}
}
