/*
 * 破碎的地牢 (End fork) — 挑战 133「格林之器2」的专属武器
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(挑战 133 格林之器2): 神天使双剑。
 *
 * <h3>原表效果</h3>
 * "每攻击一次，攻击次数加一，最大 7 次攻击，每次造成 60% 伤害"
 *
 * <h3>曲线</h3>
 * <pre>
 *   第 1 次攻击 -> 1 下
 *   第 2 次攻击 -> 2 下
 *   ...
 *   第 7 次攻击 -> 7 下
 *   第 8 次攻击 -> 7 下（封顶）
 * </pre>
 * 每下都是 **60% 伤害**。
 *
 * <p>所以它的期望输出是**累进**的：打得越久越猛，
 * 但每下只有 60%，前期明显弱于同档武器 —— 这是个"熬"的武器。
 *
 * <h3>计数何时清空</h3>
 * 按原表字面"每攻击一次加一"，**不做战斗结束重置**（那会削弱它）。
 * 只在**切换楼层**时清零，避免跨层累积到看不懂的地步。
 */
public class AngelSword extends MeleeWeapon
		implements com.shatteredpixel.shatteredpixeldungeon.actors.Char.MultiHitWeapon {

	/** 每次攻击的伤害倍率。 */
	public static final float HIT_MULT = 0.60f;
	/** 攻击次数的上限。 */
	public static final int MAX_HITS = 7;

	/**
	 * END(修订·必定命中): 神天使双剑**必定命中**。
	 *
	 * <p>文档所有者要求："神天使之剑加上必定命中"。
	 *
	 * <p>做成整个连击链都必中（而不是只有第一下）——
	 * 否则这个武器会变成"第一下必中、后面全靠运气"，
	 * 与描述里"挥动时会自行加速"的爽感不符。
	 *
	 * <p>实现：给命中倍率一个极大的值，{@code hit()} 的判定必然通过。
	 */
	public static final int PERFECT_ACCURACY = 10000;

	/**
	 * END(修复·多段攻击没生效): 让 attack() 真正打出 N 段。
	 *
	 * <h3>原先的问题</h3>
	 * {@code currentHits()} / {@code advance()} 都写了，
	 * 但**没有任何地方调用它们** —— 全是死代码。
	 * 文档所有者实测："多段武器好像没有多段"。
	 *
	 * @return {段数, 每段倍率×100, 命中倍率×100}
	 */
	@Override
	public int[] multiHitProfile(Char attacker, Char enemy) {
		int hits = currentHits(attacker);
		if (hits <= 1) hits = 1;      //至少 1 段（第一次攻击也要走这个路径）

		return new int[]{ hits, Math.round(HIT_MULT * 100), PERFECT_ACCURACY };
	}

	/**
	 * END(133): 一整轮打完后推进连击层数。
	 *
	 * <p>注意：{@code advance()} 的语义是"**每回合**只加一层"，
	 * 所以这里在"一次攻击（可能含多段）结束"后调一次，
	 * 由它内部按回合去重。
	 */
	@Override
	public void onMultiHitFinished(Char attacker, Char enemy) {
		advance(attacker);
	}

	{
		image = ItemSpriteSheet.GRIMM_ANGELSWORD;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.15f;
		tier = 5;
		DLY = 1f;
	}

	@Override public String name(){ return "神天使双剑"; }

	@Override public int min(int lvl){ return 4 + lvl; }
	@Override public int max(int lvl){ return 18 + 5*lvl; }

		/**
	 * END(修复): 力量需求随等级递减。
	 *
	 * <p>原先硬编码返回 18 —— 于是"每升 3 级减力量需求"完全没有，
	 * 玩家实测就是这个现象。
	 *
	 * <p>改用原版 Weapon 的通用公式（三角数递减 +1/+3/+6/+10…），
	 * 与其它 T5 武器一致。
	 */
	@Override public int STRReq(int lvl){ return STRReq(5, lvl); }

	@Override
	public String info(){
		//END(修复): 先取父类文本 —— 那里面才有伤害面板/力量需求/等级。
		//原先直接 return 自定义文本，于是物品描述里完全看不到数值。
		return super.info() + "\n\n" +
				"一对由神使遗落的双剑，挥动时会自行加速。\n\n" +
				"- 每**回合**攻击时，攻击次数 +1（同回合多次命中只算一次）\n" +
				"- 最多 " + MAX_HITS + " 次\n" +
				"- 每一次造成 **60% 伤害**\n" +
				"- **停手一回合，计数直接归零**\n\n" +
				"所以它逼你不能停下 —— 一旦犹豫，就得从头再来。" + com.shatteredpixel.shatteredpixeldungeon.endcontent.EndItemStats.block(this);
	}

	/**
	 * END(133): 下一次攻击会打出几下。
	 *
	 * <p>返回 0 表示**连击链已断**（上一回合没攻击），
	 * 下一次命中会从 1 重新开始。
	 */
	public static int currentHits(Char owner) {
		if (owner == null) return 0;
		AngelSwordCounter c = owner.buff(AngelSwordCounter.class);
		if (c == null) return 0;
		return Math.max(0, Math.min(MAX_HITS, c.hits));
	}

	/**
	 * END(133): 攻击后尝试推进计数。
	 *
	 * <h3>叠加规则（按文档所有者定稿）</h3>
	 * <ul>
	 *   <li><b>每回合最多叠加一次</b> —— 同一回合内多次命中只 +1</li>
	 *   <li><b>中断一回合就重新叠加</b> —— 有一回合没攻击，计数直接清零</li>
	 * </ul>
	 *
	 * <p>实现方式：buff 每回合在 {@code act()} 里检查"上一回合有没有攻击过"，
	 * 没有就把 {@code hits} 归零。所以"零"是一个合法状态 ——
	 * 它表示**连击链已断**，下一次攻击从 1 重新开始。
	 *
	 * <p>只在**命中**后推进：挥空不该攒层数，否则玩家空挥就能叠满。
	 *
	 * @return true 表示本次确实叠加了（用于判断是不是"当回合第一次命中"）
	 */
	public static boolean advance(Char owner) {
		if (owner == null) return false;
		AngelSwordCounter c = owner.buff(AngelSwordCounter.class);
		if (c == null) {
			c = Buff.affect(owner, AngelSwordCounter.class, 9999f);
			c.hits = 0;
			c.attackedThisTurn = false;
		}

		//本回合已经叠过 → 不再叠加
		if (c.attackedThisTurn) return false;

		c.attackedThisTurn = true;

		//链已断（0）时从 1 重新开始，否则 +1
		if (c.hits <= 0) {
			c.hits = 1;
		} else if (c.hits < MAX_HITS) {
			c.hits++;
		}
		return true;
	}

	/** END(133): 清零（切层时调用）。 */
	public static void reset(Char owner) {
		if (owner == null) return;
		AngelSwordCounter c = owner.buff(AngelSwordCounter.class);
		if (c != null) c.detach();
	}

	/**
	 * END(133): 神天使双剑的攻击次数计数（不显示图标）。
	 *
	 * <p>{@code hits == 0} 表示"连击链已断" —— 下一次命中从 1 重新开始。
	 */
	public static class AngelSwordCounter extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		/** 下一次攻击会打出几下；0 表示链已断。 */
		public int hits = 0;

		/** 本回合是否已经叠加过（每回合只叠一次的实现）。 */
		public boolean attackedThisTurn = false;

		/**
		 * END(133): 每回合检查是否"中断"。
		 *
		 * <p>{@code act()} 在该 buff 轮到时执行一次（约等于英雄的一个回合）。
		 * 若上一回合**没有攻击过**，说明连击链断了 → 计数清零。
		 * 然后把标记复位，供下一回合使用。
		 */
		@Override
		public boolean act() {
			if (!attackedThisTurn) {
				//上一回合没打 → 链断了
				hits = 0;
			}
			attackedThisTurn = false;
			spend(TICK);
			return true;
		}

		@Override public int icon(){ return BuffIndicator.NONE; }

		private static final String HITS = "hits";
		private static final String ATTACKED = "attacked_this_turn";

		@Override
		public void storeInBundle(com.watabou.utils.Bundle bundle){
			super.storeInBundle(bundle);
			bundle.put(HITS, hits);
			bundle.put(ATTACKED, attackedThisTurn);
		}

		@Override
		public void restoreFromBundle(com.watabou.utils.Bundle bundle){
			super.restoreFromBundle(bundle);
			hits = bundle.getInt(HITS);
			attackedThisTurn = bundle.getBoolean(ATTACKED);
		}
	}

	@Override
	public int value(){ return 0; }
}
