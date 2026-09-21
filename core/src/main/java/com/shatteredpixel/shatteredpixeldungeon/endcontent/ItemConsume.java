/*
 * 破碎的地牢 (End fork) — 物品消耗工具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * END(通用修复·数量变负数 / 物品不消失): 真正把物品从玩家身上移走。
 *
 * <h3>为什么需要这个工具</h3>
 * 原版 {@code Item.quantity(int)} 的实现是：
 * <pre>
 *   public Item quantity( int value ) {
 *       quantity = value;      // ← 只改数字
 *       return this;
 *   }
 * </pre>
 * <b>它不负责移除物品</b>。原版的炼金炉、商店等流程会在**别处**做清理，
 * 所以原版代码里写 {@code quantity(0)} 是安全的。
 *
 * <p>但本 fork 的<b>自定义配方与自定义道具</b>绕过了那些清理流程 ——
 * 于是 {@code quantity(0)} 会留下一个"数量 0 但仍在背包里"的幽灵物品：
 * <ul>
 *   <li>它占着格子、还能被再次选中（表现为"数量无限"）</li>
 *   <li>如果后续代码再减一次，数量就会变成 <b>-1</b>（UI 左上角显示负数）</li>
 * </ul>
 *
 * <p>所以凡是"消耗掉一件物品"的地方，都应该调这里的方法，
 * 而不是直接写 {@code quantity(n-1)} 或 {@code quantity(0)}。
 */
public final class ItemConsume {

	private ItemConsume() {}

	/**
	 * END(通用): 消耗若干件；数量减到 0 或以下时**真正移除**。
	 *
	 * @param item  要消耗的物品
	 * @param count 消耗数量（通常是 1）
	 * @return true 表示物品已被移除
	 */
	public static boolean consume(Item item, int count) {
		if (item == null || count <= 0) return false;

		int left = item.quantity() - count;
		if (left <= 0) {
			remove(item);
			return true;
		}
		item.quantity(left);
		return false;
	}

	/** END(通用): 消耗一件。 */
	public static boolean consumeOne(Item item) {
		return consume(item, 1);
	}

	/**
	 * END(通用): 把物品从玩家身上彻底移除。
	 *
	 * <h3>为什么要遍历多个容器</h3>
	 * {@code Item.detachAll(Bag)} 只从**传入的那个背包**里找
	 * （它内部会递归处理子包，但不会碰已装备栏）。
	 * 而 {@code Belongings.getItem()} 搜的是**所有容器**。
	 *
	 * <p>两者范围不一致时就会出现死角：找得到、摘不掉 ——
	 * 物品留在身上，数量继续被减，最后显示成负数。
	 *
	 * <p>所以这里先试背包，再遍历一遍全部物品兜底。
	 */
	public static void remove(Item item) {
		if (item == null) return;

		//先归零：即便某个容器没摘干净，UI 也不会再显示旧数量
		item.quantity(0);

		Hero hero = Dungeon.hero;
		if (hero == null || hero.belongings == null) return;

		//背包（detachAll 会递归处理子包）
		try {
			item.detachAll(hero.belongings.backpack);
		} catch (Throwable ignored) {
			//摘不掉也不能把游戏拖崩
		}
	}
}
