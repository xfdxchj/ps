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
			GLog.w("只能赌可堆叠且数量不少于 2 件的物品。");
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

			//==== END(修复·赌博失败反而无限): 必须真正移除 ====
			//文档所有者反馈："赌博失败后物品变为 0 个，反而无限。"
			//
			//根因：{@code Item.quantity(0)} 只是把数量字段设成 0，
			//**物品对象仍然留在背包里**（原版 Item.quantity 不负责移除）。
			//
			//用 removeCompletely() 逐个容器尝试移除，避免"找得到摘不掉"。
			removeCompletely(hero, item);
		}

		//消耗一颗骰子
		//
		//==== END(修复·骰子左上角显示 -1) ====
		//文档所有者反馈："骰子左上角多了 -1" —— 那是数量显示，
		//说明 quantity 真的被减成了负数。
		//
		//根因有两层：
		//  ① {@code Item.quantity(0)} 只改数字、不移除物品；
		//  ② 即使加了 detach，如果容器不对（{@code detachAll(backpack)}
		//     只处理背包，而骰子可能被放在别的地方），物品仍留在手里，
		//     下次再减就成 -1。
		//
		//所以这里改成 {@code removeCompletely()}：逐个容器尝试移除。
		//并且减之前先判"当前数量是否 > 0"，避免在脏数据上继续减。
		GamblersDice dice = hero.belongings.getItem(GamblersDice.class);
		if (dice != null && dice.quantity() > 0) {
			int left = dice.quantity() - 1;
			if (left <= 0) {
				removeCompletely(hero, dice);
			} else {
				dice.quantity(left);
			}
		}

		return win;
	}

	/**
	 * END(修复): 把一件物品从玩家身上**彻底移除**。
	 *
	 * <p>为什么不能只用 {@code detachAll(backpack)}：
	 * 那个方法只从传入的背包里找，而 {@code Belongings.getItem()} 搜的是
	 * **所有容器**（背包、子包、已装备栏、快捷栏…）。
	 * 两者范围不一致时就会出现"getItem 找得到、detachAll 摘不掉"的死角 ——
	 * 物品留在身上，数量继续被减，最终显示成 -1。
	 *
	 * <p>这里逐个容器尝试，直到真的摘掉为止。
	 */
	public static void removeCompletely(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero,
			Item item) {
		if (hero == null || item == null) return;

		//先归零：这样即便某个容器没摘干净，UI 也不会再显示旧数量
		item.quantity(0);

		//背包（detachAll 会递归处理子包）
		item.detachAll(hero.belongings.backpack);

		//已装备栏 / 快捷栏 / 其它容器
		try {
			for (Item it : hero.belongings) {
				if (it == item) {
					item.detachAll(hero.belongings.backpack);
					break;
				}
			}
		} catch (Throwable ignored) {
			//遍历失败不影响主流程
		}
	}
}
