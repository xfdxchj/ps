/*
 * 破碎的地牢 (End fork) — 挑战 39「All or Nothing」的赌博道具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * END(挑战 39 All or Nothing): 赌徒之骰。
 *
 * <h3>原表效果</h3>
 * "选择物品赌博，成功数量翻倍，失败清零"
 *
 * <h3>按文档所有者定稿：做成道具</h3>
 * 使用后进入"选择物品"模式 —— 选中的**可堆叠物品**会：
 * <ul>
 *   <li>**50% 成功**：数量翻倍</li>
 *   <li>**50% 失败**：数量清零（物品消失）</li>
 * </ul>
 *
 * <h3>限制</h3>
 * <ul>
 *   <li>只对**可堆叠且数量 ≥ 2** 的物品生效 —— 数量 1 的"翻倍"没有意义，
 *       而"清零"会直接毁掉唯一一件装备，太粗暴</li>
 *   <li>不可对**任务物品**使用</li>
 *   <li>不可对**已装备**的物品使用</li>
 * </ul>
 */
public class GamblersDice extends Item {

	/** 成功概率（50%）。 */
	public static final float WIN_CHANCE = 0.5f;

	{
		image = ItemSpriteSheet.GRIMM_RABBIT_RING;   //暂时复用（骰子图标以后补）
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "赌徒之骰"; }

	@Override
	public String info(){
		return "一颗灌过铅的骰子 —— 但你不知道铅灌在哪一面。\n\n" +
				"- 使用后选择一件**可堆叠**的物品赌博\n" +
				"- **" + (int)(WIN_CHANCE * 100) + "% 成功**：数量**翻倍**\n" +
				"- **" + (int)((1 - WIN_CHANCE) * 100) + "% 失败**：数量**清零**\n\n" +
				"只能赌数量 ≥ 2 的可堆叠物品 —— 单件装备不参与。";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }

	public static final String AC_GAMBLE = "GAMBLE";

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_GAMBLE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_GAMBLE) || hero == null) return;

		//打开背包选择要赌的物品
		//
		//用 WndBag.adapt(Listener) 而不是直接实现 ItemSelector：
		//ItemSelector 要求额外实现 preferredBag() / usesTargeting() 等方法
		//（那是"选择目标"用的），而我们只需要一个 onSelect。
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.getBag(
						com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.adapt(
								new com.shatteredpixel.shatteredpixeldungeon.windows.WndBag
										.Listener() {
									@Override
									public void onSelect(Item item) {
										if (item == null) return;
										gamble(hero, item);
									}
								})));
	}

	/**
	 * END(39): 执行一次赌博。
	 *
	 * @param hero 玩家
	 * @param item 要赌的物品
	 * @return true 表示赢了
	 */
	public static boolean gamble(Hero hero, Item item) {
		if (hero == null || item == null) return false;

		//---- 资格检查 ----
		if (!item.isUpgradable() && !item.stackable) {
			GLog.w("这件东西赌不了。");
			return false;
		}
		if (!item.stackable || item.quantity() < 2) {
			GLog.w("只能赌**可堆叠且数量 ≥ 2** 的物品。");
			return false;
		}
		if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.Gold) {
			GLog.w("金币不接受赌博。");
			return false;
		}

		boolean win = Random.Float() < WIN_CHANCE;

		if (win) {
			int before = item.quantity();
			item.quantity(before * 2);
			GLog.p("骰子停在了正面 —— " + item.name() + " 由 " + before
					+ " 变成 " + item.quantity() + "！");
		} else {
			GLog.n("骰子停在了反面 —— " + item.name() + " 全部消失了。");
			item.quantity(0);
		}

		//消耗一颗骰子
		GamblersDice dice = hero.belongings.getItem(GamblersDice.class);
		if (dice != null) {
			dice.quantity(dice.quantity() - 1);
		}

		return win;
	}
}
