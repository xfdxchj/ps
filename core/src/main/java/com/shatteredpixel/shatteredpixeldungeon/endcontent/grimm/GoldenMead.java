/*
 * 破碎的地牢 (End fork) — 挑战 134「黄金蜂蜜酒」的专属道具
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
 * END(挑战 134 黄金蜂蜜酒): 黄金蜂蜜酒。
 *
 * <h3>原表效果</h3>
 * "使用后获得'发狂'：攻击提升 50%，每回合扣除最大生命值的一半，最低为 1"
 *
 * <h3>这是典型的"双刃剑"道具</h3>
 * <ul>
 *   <li>攻击 +50%（原版 buff 里没有正好 50% 的，所以自己做）</li>
 *   <li>每回合扣 **最大生命的一半** —— 这是极重的代价：
 *       满血时撑不过 2 个回合就会见底</li>
 *   <li>但扣血**最低保留 1 点**，所以不会直接扣死 ——
 *       真正杀死玩家的是"扣到 1 血之后的下一发敌人攻击"</li>
 * </ul>
 *
 * <p>原表还写"与发狂类 buff 兼容"，所以它**不叫 Fury**（原版有 Fury，
 * 但那个是"HP 高于 50% 就自动消失"的条件 buff，语义完全不同），
 * 而是自己做一个 {@link Madness}。
 */
public class GoldenMead extends Item {

	{
		image = ItemSpriteSheet.GRIMM_MEAD;
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "黄金蜂蜜酒"; }

	@Override public String info(){
		return "琥珀色的酒液里浮着细小的金屑。喝下去会听见自己的心跳。\n\n" +
				"- 获得**发狂**状态（永久，直到你死）\n" +
				"- 攻击力 **+50%**\n" +
				"- **每回合扣除最大生命的一半**（最低保留 1 点）\n\n" +
				"它是给不想活了的人准备的 —— 但死之前，你能砍得很痛快。";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }

	public static final String AC_DRINK = "DRINK";

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_DRINK);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_DRINK) || hero == null) return;

		//永久：给一个很长的时长，实际上会一直持续到玩家死亡
		Buff.affect(hero, Madness.class, 99999f);
		GLog.w("蜂蜜酒烧过喉咙。你开始发狂。");

		detach(hero.belongings.backpack);
		hero.spendAndNext(1f);
	}

	@Override
	public int value(){ return 0; }

	//==================================================================
	//发狂
	//==================================================================

	/**
	 * END(134): 发狂状态。
	 *
	 * <p>攻击 +50%，每回合扣最大生命的一半（最低 1）。
	 */
	public static class Madness extends FlavourBuff {
		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		/** 攻击倍率：+50%。 */
		public static final float ATTACK_MULT = 1.50f;

		@Override public int icon(){ return BuffIndicator.RAGE; }

		/**
		 * END(134): 每回合扣血。
		 *
		 * <p>扣 **最大生命的一半**，但最低保留 1 点 ——
		 * 原表明确写"最低为 1"。
		 */
		@Override
		public boolean act(){
			if (target != null && target.isAlive()){
				int drain = Math.max(1, target.HT / 2);
				int newHp = Math.max(1, target.HP - drain);

				if (newHp < target.HP){
					target.HP = newHp;
					if (target.sprite != null){
						target.sprite.showStatus(
								com.shatteredpixel.shatteredpixeldungeon.sprites
										.CharSprite.NEGATIVE,
								"-" + (target.HP - newHp + drain - drain));  //显示扣血量
					}
				}
			}
			spend(TICK);
			return true;
		}
	}

	/**
	 * END(134): 攻击倍率。
	 *
	 * <p>调用点：{@code Char.attack()} 的伤害计算里（与 158 神圣之力并列）。
	 * 未处于发狂状态时返回 1.0，等价于原版。
	 */
	public static float madnessAttackMultiplier(com.shatteredpixel.shatteredpixeldungeon
			.actors.Char ch){
		if (ch == null) return 1f;
		if (ch.buff(Madness.class) == null) return 1f;
		return Madness.ATTACK_MULT;
	}
}
