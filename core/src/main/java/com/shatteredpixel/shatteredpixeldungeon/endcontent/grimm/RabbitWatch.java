/*
 * 破碎的地牢 (End fork) — 挑战 125「格林之器」的专属物品
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * END(挑战 125 格林之器): 兔子的怀表。
 *
 * <h3>唯一效果：把银色短铳的冷却归零</h3>
 * 原表最初写作"暂停世界一回合，CD 10 回合"，
 * 但**文档所有者最终定稿为**：
 * <ul>
 *   <li>效果 = 把 {@link SilverGun} 的免费出手冷却**直接归零**</li>
 *   <li>怀表自身 CD = {@link #WATCH_CD} 回合</li>
 * </ul>
 *
 * <p>注意：**不做"暂停世界"**。那是早期设计，已废弃。
 *
 * <h3>用法</h3>
 * 它是**背包里点击使用**的物品（不是投掷武器）。
 * 使用后：银色短铳的冷却立即清空，之后就能再一次免回合出手。
 */
public class RabbitWatch extends Item {

	/** 怀表自身的冷却回合数。 */
	public static final int WATCH_CD = 10;

	/**
	 * "使用怀表"这个操作的动作名。
	 *
	 * <p>{@code Item} 基类只定义了 {@code AC_DROP} / {@code AC_THROW}，
	 * 子类要自己声明动作常量（原版 {@code Potion.AC_DRINK} 就是这么做的）。
	 */
	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.GRIMM_WATCH;
		stackable = false;
		bones = false;
		unique = true;
	}

	@Override public String name(){ return "兔子的怀表"; }

	@Override public String info(){
		return "一只不再走动的怀表，指针停在某个不会到来的时刻。\n\n" +
				"- 使用后**立即重置银色短铳的冷却**\n" +
				"- 怀表自身冷却 " + WATCH_CD + " 回合";
	}

	@Override
	public boolean isUpgradable(){ return false; }

	@Override
	public boolean isIdentified(){ return true; }

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		//可以被绑定挑战锁住，这时 actions() 会返回空列表
		if (actions.isEmpty()) return actions;
		actions.add(AC_USE);
		return actions;
	}

	/**
	 * END(125): 使用怀表 —— 归零银色短铳的冷却。
	 *
	 * <p>即使银色短铳**当前不在冷却**也允许使用（那样只是白用一次，
	 * 怀表照样进 CD）—— 这样行为可预测，玩家不用去猜"现在能不能用"。
	 */
	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);

		if (!action.equals(AC_USE)) return;
		if (hero == null) return;

		//怀表自身还在冷却
		if (hero.buff(WatchCooldown.class) != null) {
			GLog.w("怀表还没走完一圈。");
			return;
		}

		boolean reset = SilverGun.resetCooldown(hero);

		//怀表进入冷却
		Buff.affect(hero, WatchCooldown.class, (float) WATCH_CD);

		if (reset) {
			GLog.i("银色短铳的冷却被归零了。");
		} else {
			GLog.i("怀表的指针轻轻一跳 —— 铳本来就是冷的。");
		}

		hero.spendAndNext(1f);
	}

	/** END(125): 怀表自身的冷却。 */
	public static class WatchCooldown extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		@Override public int icon(){ return BuffIndicator.NONE; }
	}

	@Override
	public int value(){ return 0; }        //不可出售
}
