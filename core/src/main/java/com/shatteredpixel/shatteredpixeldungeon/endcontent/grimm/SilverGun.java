/*
 * 破碎的地牢 (End fork) — 挑战 125「格林之器」的专属武器
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 125 格林之器): 银色短铳。
 *
 * <h3>原表效果</h3>
 * "可以不消耗回合发出远程攻击"。
 *
 * <h3>实现（按文档所有者定稿）</h3>
 * <ul>
 *   <li>投掷时若**不在冷却中** → 本次投掷不消耗回合，并进入
 *       {@link #FREE_CD} 回合的冷却</li>
 *   <li>冷却期间投掷照常消耗回合（等于普通投掷武器）</li>
 *   <li>{@link RabbitWatch 兔子怀表} 可以把这条冷却**直接归零**</li>
 * </ul>
 *
 * <p>所以它不是"每回合白嫖"，而是**每 10 回合攒一次免回合出手**，
 * 并且怀表能刷新这个资源 —— 两件装备形成配合。
 */
public class SilverGun extends MissileWeapon {

	/** 免费出手后的冷却回合数。 */
	public static final int FREE_CD = 10;

	{
		image = ItemSpriteSheet.GRIMM_SILVERGUN;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1.1f;
		stackable = false;
		bones = false;
		tier = 4;
		baseUses = 5;
	}

	@Override public String name(){ return "银色短铳"; }

	/**
	 * 面板：**固定伤害** 2 + 0.5×lv。
	 *
	 * <p>原表（文档所有者定稿）："银色短从的子弹伤害为 2+0.5lv"，
	 * 且明确是**固定值**（min == max），不是随机区间。
	 *
	 * <p>这样设计的意图很明显：铳本身的杀伤力很弱，
	 * 它的价值全在"不消耗回合"上 —— 是一件**节奏型**装备，
	 * 而不是输出装。所以下限与上限取同一个数。
	 *
	 * <p>用 {@code Math.round} 而不是截断：0.5 级时应当进位（lv1 → 2.5 → 3），
	 * 截断会让低等级时成长感受不到。
	 */
	@Override public int min(int lvl){ return Math.round(2 + 0.5f * lvl); }
	@Override public int max(int lvl){ return Math.round(2 + 0.5f * lvl); }

	@Override public int STRReq(int lvl){ return 13; }

	/** 无限耐久（限制来自冷却，而不是耐久）。 */
	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override
	public String info(){
		return "一把镀银的短铳，扣下扳机几乎不占时间。\n\n" +
				"- 子弹伤害固定为 **2 + 0.5×等级**（不掷骰）\n" +
				"- 出手时**不消耗回合**，随后进入 " + FREE_CD + " 回合冷却\n" +
				"- 冷却期间攻击照常消耗回合\n" +
				"- 无限耐久\n\n" +
				"它几乎打不死人 —— 价值全在节奏上。与**兔子怀表**配合时，\n" +
				"怀表可以立刻重置这条冷却。";
	}

	//==================================================================
	//冷却
	//==================================================================

	/**
	 * END(125): 银色短铳的冷却。
	 *
	 * <p>用 {@code FlavourBuff} 的时长字段当冷却计时器 ——
	 * 它每回合自动递减，减到 0 就自动 detach，不需要自己写计时逻辑。
	 *
	 * <p>必须继承 {@code FlavourBuff}：{@code Buff.affect(Char, Class, float)}
	 * 这个三参重载只接受它的子类（原版限制，本 fork 已踩过多次）。
	 */
	public static class SilverGunCooldown extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		@Override public int icon(){ return BuffIndicator.NONE; }

		/** 剩余回合数（用于界面提示）。 */
		public int turnsLeft(){
			return Math.max(0, (int) Math.ceil(visualcooldown()));
		}
	}

	/**
	 * END(125): 本次投掷是否应该"不消耗回合"。
	 *
	 * <p>调用点：{@code Item.cast()} 的命中回调里（与 LethalMomentumTracker 并列）。
	 *
	 * <p>返回 true 时会**顺便**挂上冷却，所以调用方不需要再做别的。
	 *
	 * @param hero 投掷者
	 * @return true 表示本次出手不应消耗回合
	 */
	public static boolean shouldBeFree(Hero hero) {
		if (hero == null) return false;

		//冷却中 → 照常消耗回合
		if (hero.buff(SilverGunCooldown.class) != null) return false;

		//不在冷却 → 免费出手，并进入冷却
		Buff.affect(hero, SilverGunCooldown.class, (float) FREE_CD);
		return true;
	}

	/**
	 * END(125): 把冷却直接归零（兔子怀表调用）。
	 *
	 * @param hero 目标
	 * @return true 表示确实清掉了一层冷却（用于决定要不要提示玩家）
	 */
	public static boolean resetCooldown(Hero hero) {
		if (hero == null) return false;
		SilverGunCooldown cd = hero.buff(SilverGunCooldown.class);
		if (cd == null) return false;
		cd.detach();
		return true;
	}

	/** END(125): 当前冷却剩余回合；不在冷却时返回 0。 */
	public static int cooldownTurns(Hero hero) {
		if (hero == null) return 0;
		SilverGunCooldown cd = hero.buff(SilverGunCooldown.class);
		return cd == null ? 0 : cd.turnsLeft();
	}
}
