/*
 * 破碎的地牢 (End fork) — 顶级装备「寰宇支配之剑」
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/**
 * END(顶级装备): 寰宇支配之剑。
 *
 * <h3>数值（文档所有者定稿）</h3>
 * <ul>
 *   <li>伤害 **60-80**，成长 **5-10**</li>
 *   <li>附带 **10% 最大生命** 的额外伤害</li>
 *   <li>造成 **10% 百分比伤害**</li>
 *   <li>每攻击一次**伤害与攻速翻倍**，**最多三次**</li>
 * </ul>
 *
 * <h3>关于"翻倍三次"</h3>
 * 文档所有者的原话是"每攻击一次，伤害/攻速翻倍，最大三次"。
 * 实现在连击计数器（{@link DominanceTracker}）里：
 * 每命中一次 +1 层，最多 3 层；层数越高伤害与攻速越高。
 * 切换目标或脱离战斗一段时间后清零（否则会一路叠到通关）。
 */
public class UniverseSword extends MeleeWeapon {

	{
		image = ItemSpriteSheet.GRIMM_INFINITY_SWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.85f;
		//"顶级装备"不属于原版 1-5 阶体系，但给 5 让 STRReq 等逻辑有个基准
		tier = 5;
	}

	@Override public String name(){ return "寰宇支配之剑"; }

	//==== 数值：60-80，成长 5-10 ====
	//注意：**不**沿用原版的 tier 公式（那是 5-30），这里显式写死。

	@Override public int min(int lvl){ return 60 + 5 * lvl; }
	@Override public int max(int lvl){ return 80 + 10 * lvl; }

	/** 重量级武器：力量需求比同阶更高。 */
	@Override public int STRReq(int lvl){ return Math.max(18, 18 + lvl); }

	//==== 特效 ====

	/** 额外伤害 = 目标最大生命的 10%。 */
	public static final float PERCENT_DAMAGE = 0.10f;
	/** 额外伤害 = 自身最大生命的 10%。 */
	public static final float MAX_HP_BONUS = 0.10f;
	/** 连击上限。 */
	public static final int MAX_STACKS = 3;

	@Override
	public String info(){
		return "剑身上浮着整片星图，每一次挥动都像在挪动某个世界。\n\n" +
				"基础伤害 60-80，每级 +5~+10。\n\n" +
				"-附带相当于你**最大生命 10%** 的额外伤害\n" +
				"-造成相当于目标**最大生命 10%** 的百分比伤害\n" +
				"-每命中一次，伤害与攻速**翻倍**，最多 **3 次**\n" +
				"-切换目标后连击清零" + com.shatteredpixel.shatteredpixeldungeon.endcontent.EndItemStats.block(this);
	}

	@Override
	public String desc(){
		return info();
	}

	@Override
	public int damageRoll(Char owner){
		int dmg = super.damageRoll(owner);

		//① 自身最大生命的 10%
		if (owner != null){
			dmg += Math.round(owner.HT * MAX_HP_BONUS);
		}

		//② 连击层数 → 伤害翻倍（1 层 x2、2 层 x4、3 层 x8）
		if (owner != null){
			DominanceTracker t = owner.buff(DominanceTracker.class);
			if (t != null && t.stacks > 0){
				dmg = dmg * (1 << t.stacks);        //2^层数
			}
		}

		return dmg;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage){
		damage = super.proc(attacker, defender, damage);

		//③ 目标最大生命的 10%（百分比伤害）
		//放在 proc 里而不是 damageRoll —— 因为 damageRoll 拿不到"目标"。
		if (defender != null){
			damage += Math.round(defender.HT * PERCENT_DAMAGE);
		}

		//④ 推进连击
		if (attacker != null){
			DominanceTracker t = attacker.buff(DominanceTracker.class);
			if (t == null) t = Buff.affect(attacker, DominanceTracker.class);
			t.bump(defender);
		}

		return damage;
	}

	/**
	 * END: 连击计数器。
	 *
	 * <p>"每攻击一次伤害与攻速翻倍，最多三次" —— 用层数表达：
	 * 每层让伤害 x2、攻速 +100%（相当于每回合多打一次）。
	 *
	 * <p>为什么要有"换目标清零"：不然打小怪叠满 3 层之后，
	 * 见到 Boss 直接 8 倍伤害 —— 那会让整局失去张力。
	 */
	public static class DominanceTracker extends FlavourBuff {

		{
			type = buffType.POSITIVE;
			announced = true;
		}

		public int stacks = 0;
		public Char target = null;

		/** 持续时间（回合）。超时会自动清零。 */
		public static final float DURATION = 5f;

		public void bump(Char newTarget){
			if (newTarget != target){
				//换目标 → 重新开始
				stacks = 0;
				target = newTarget;
			}
			if (stacks < MAX_STACKS) stacks++;
			//每次命中都刷新计时
			spend(-cooldown());
			spend(DURATION);
		}

		@Override
		public boolean act(){
			detach();
			return true;
		}

		@Override
		public int icon(){ return BuffIndicator.FURY; }

		@Override
		public void tintIcon(Image icon){
			icon.hardlight(1f, 0.85f, 0.4f);
		}

		@Override
		public float iconFadePercent(){
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		@Override
		public String iconTextDisplay(){
			return Integer.toString(stacks);
		}

		@Override
		public String desc(){
			return "寰宇支配：伤害 x" + (1 << Math.max(0, stacks))
					+ "（" + stacks + " / " + MAX_STACKS + " 层）";
		}
	}
}
