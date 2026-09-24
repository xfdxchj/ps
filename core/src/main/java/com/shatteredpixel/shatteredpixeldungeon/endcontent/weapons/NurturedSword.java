/*
 * 破碎的地牢 (End fork) — 蕴生之剑
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(蕴生之剑): 随升级数**提升阶数**的剑。
 *
 * <h3>文档所有者定稿</h3>
 * "增加武器，可以随着升级数提升阶数（基础伤害与成长），开局可获得。"
 *
 * <h3>与普通武器有什么不同</h3>
 * 原版武器的 {@code tier} 是**写死的**（1~5 阶），升级只是把
 * {@code min/max} 各加 {@code lvl}。蕴生之剑不是：
 *
 * <pre>
 *   原版 T3 剑：min = 3 + lvl      max = 20 + 5×lvl
 *   蕴生之剑：  tier 会随强化等级**一起涨** ——
 *              每 +3 级提升一阶（上限 5 阶），
 *              而阶数又反过来抬高**基础值与成长率**
 * </pre>
 *
 * <p>所以它是一把"越养越强"的剑：早期平淡，后期远超前期的自己。
 * 这正是"蕴生"这个名字的意思 —— 力量是养出来的，不是捡来的。
 *
 * <h3>"开局可获得"</h3>
 * 与家传法杖/神射戒指一样，走 {@code ChallengeEffects.startingGear()} 发放。
 * 但它**不是挑战奖励**，而是一个独立的开局装备选项（见 {@code OWNER} 标记）。
 */
public class NurturedSword extends MeleeWeapon {

	{
		image = ItemSpriteSheet.GRIMM_SILVER_BROADSWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;
		tier = 1;          //起手是 1 阶 —— 弱，但能长
	}

	@Override public String name(){ return "蕴生之剑"; }

	//==================================================================
	//阶数随强化等级成长
	//==================================================================

	/** 每多少级提升一阶。 */
	//==== END(修订): 文档所有者定稿"3 级升一阶"（原为 5）====
	public static final int LEVELS_PER_TIER = 3;
	/** 阶数上限（与原版最高阶一致）。 */
	public static final int MAX_TIER = 5;

	/**
	 * END: 当前的**有效阶数**。
	 *
	 * <p>{@code 1 + 强化等级 / 3}，上限 5。
	 * 例：+0 → 1 阶、+3 → 2 阶、+6 → 3 阶、+9 → 4 阶、+12 → 5 阶。
	 */
	public int effectiveTier(){
		return Math.min(MAX_TIER, 1 + Math.max(0, buffedLvl()) / LEVELS_PER_TIER);
	}

	//==================================================================
	//基础伤害与成长 —— 都由有效阶数决定
	//==================================================================

	/**
	 * END: 伤害下限。
	 *
	 * <p>用原版那条公式但把 {@code tier} 换成有效阶数：
	 * {@code 有效阶数 + 等级}。
	 */
	@Override
	public int min(int lvl){
		int t = effectiveTier();
		return t + lvl;
	}

	/**
	 * END: 伤害上限。
	 *
	 * <p>原版公式 {@code 5×(tier+1) + lvl×(tier+1)}。
	 *
	 * <p>蕴生之剑额外给一份"成长奖励"：**阶数越高，每级加得越多** ——
	 * 这就是"基础伤害与成长都跟着提阶上涨"的含义。
	 */
	@Override
	public int max(int lvl){
		int t = effectiveTier();
		return 5 * (t + 1) + lvl * (t + 1) + (t - 1) * 3;
	}

	/**
	 * END: 力量需求也跟着阶数走。
	 *
	 * <p>否则会出现"5 阶的面板、1 阶的力量需求"这种不合理的性价比。
	 */
	@Override
	public int STRReq(int lvl){
		return STRReq(effectiveTier(), lvl);
	}

	//==================================================================
	//说明
	//==================================================================

	@Override
	public String info(){
		int t = effectiveTier();
		StringBuilder sb = new StringBuilder();
		sb.append("剑身上刻着一圈年轮。它记得自己每一次被磨利的样子。\n\n");

		sb.append("**伤害：1 阶 +0 为 1-10，5 阶 +12 为 17-114**（都随等级成长）。\n");
		sb.append("-每 **+").append(LEVELS_PER_TIER)
				.append(" 级**提升一阶（最高 ").append(MAX_TIER).append(" 阶）\n");
		sb.append("-阶数越高，**基础伤害与每级成长**都越高\n");
		sb.append("-开局即可获得\n\n");

		sb.append("当前：**").append(t).append(" 阶**");
		int next = (LEVELS_PER_TIER - (Math.max(0, buffedLvl()) % LEVELS_PER_TIER));
		if (t < MAX_TIER){
			sb.append("（再 +").append(next).append(" 级可提阶）");
		} else {
			sb.append("（已满阶）");
		}
		return sb.toString() + com.shatteredpixel.shatteredpixeldungeon.endcontent.EndItemStats.block(this);
	}

	@Override
	public String desc(){
		return info();
	}
}
