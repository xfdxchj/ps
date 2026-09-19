/*
 * 破碎的地牢 (End fork) — 挑战 127「格林之戒」的专属戒指
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 127 格林之戒): 黑兔戒指。
 *
 * <h3>原表效果</h3>
 * "攻击时回合数加一（表现为可以一回合释放两次攻击，类似手里剑，
 *   每回合最多触发一次）。该戒指无法升级。"
 *
 * <h3>实现</h3>
 * 戒指装备后，**每回合第一次命中**时返还一个回合 ——
 * 于是玩家能在同一回合内再出手一次（或移动、喝药）。
 *
 * <ul>
 *   <li>**每回合最多触发一次**：用 {@link RabbitRingTracker} 记录本回合是否已触发</li>
 *   <li>**无法升级**：{@code isUpgradable()} 返回 false</li>
 *   <li>不依赖戒指等级 —— 原表没给等级曲线，所以是"有就生效"的开关型戒指</li>
 * </ul>
 *
 * <h3>与银色短铳的区别</h3>
 * 短铳限制的是"武器自身的出手"，本戒指限制的是"每回合一次"。
 * 两者可以叠加：短铳的免费出手不会消耗戒指的次数（各自独立计数）。
 */
public class RabbitRing extends Ring {

	{
		image = ItemSpriteSheet.GRIMM_RABBIT_RING;
	}

	/**
	 * END(修复·闪退): 必须提供一个 RingBuff 实例。
	 *
	 * <h3>原来为什么会闪退</h3>
	 * {@code Ring.activate()} 里是这么写的：
	 * <pre>
	 *   buff = buff();
	 *   buff.attachTo( ch );      // ← buff() 返回 null 就在这里 NPE
	 * </pre>
	 * 而基类的 {@code buff()} 默认返回 {@code null}，
	 * 子类要么设 {@code buffClass = XXX.class}，要么覆写 {@code buff()}。
	 *
	 * <p>我两样都没做，所以一装备黑兔戒指就崩。
	 *
	 * <h3>为什么用覆写而不是 buffClass</h3>
	 * 本戒指是**开关型**的（不依赖等级），用不上 {@code buffClass} 那套
	 * "按等级查询加成"的机制。直接给一个空的 RingBuff 占位即可 ——
	 * 它的作用只是让 {@code activate()} 不炸。
	 */
	@Override
	protected RingBuff buff() {
		return new RabbitRingBuff();
	}

	/** END(修复): 空的效果载体 —— 实际效果由静态方法 {@link #shouldRefundTurn} 提供。 */
	public class RabbitRingBuff extends RingBuff {
	}

	@Override public String name(){ return "黑兔戒指"; }

	@Override
	public String info(){
		return "一圈几乎看不见的黑铁。它让时间对佩戴者格外宽容。\n\n" +
				"- **每回合第一次命中**后，返还一个回合\n" +
				"- 表现为一回合可以攻击两次\n" +
				"- 每回合最多触发一次\n" +
				"- **无法升级**";
	}

	/** 无法升级 —— 原表明确要求。 */
	@Override
	public boolean isUpgradable(){ return false; }

	/** 也无法被强化卷轴之类的效果提升。 */
	@Override
	public boolean isIdentified(){ return true; }

	@Override
	public String desc(){
		return info();
	}

	//==================================================================
	//触发
	//==================================================================

	/**
	 * END(127): 玩家是否装备着黑兔戒指。
	 *
	 * <p>用 {@code Ring} 的现成查询方式：{@code hero.belongings.getItem}。
	 */
	public static boolean equipped(Hero hero){
		if (hero == null || hero.belongings == null) return false;
		try {
			RabbitRing r = hero.belongings.getItem(RabbitRing.class);
			return r != null && r.isEquipped(hero);
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * END(127): 本次命中是否应该返还一个回合。
	 *
	 * <p>调用点：{@code Char.attack()} 的命中结算处。
	 *
	 * <p>返回 true 时**顺便**记下本回合已触发，所以调用方不需要再处理。
	 *
	 * @param hero 攻击者
	 * @return true 表示应把回合还给玩家
	 */
	public static boolean shouldRefundTurn(Hero hero){
		if (!equipped(hero)) return false;

		RabbitRingTracker t = hero.buff(RabbitRingTracker.class);
		if (t == null) {
			t = Buff.affect(hero, RabbitRingTracker.class, 9999f);
			t.usedThisTurn = false;
		}
		if (t.usedThisTurn) return false;          //每回合最多一次

		t.usedThisTurn = true;
		return true;
	}

	/** END(127): 本回合的触发计数（不显示图标）。 */
	public static class RabbitRingTracker extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		public boolean usedThisTurn = false;

		@Override
		public boolean act(){
			//回合结束，允许下一回合再次触发
			usedThisTurn = false;
			spend(TICK);
			return true;
		}

		@Override public int icon(){ return BuffIndicator.NONE; }

		private static final String USED = "used_this_turn";

		@Override
		public void storeInBundle(com.watabou.utils.Bundle bundle){
			super.storeInBundle(bundle);
			bundle.put(USED, usedThisTurn);
		}

		@Override
		public void restoreFromBundle(com.watabou.utils.Bundle bundle){
			super.restoreFromBundle(bundle);
			usedThisTurn = bundle.getBoolean(USED);
		}
	}
}
