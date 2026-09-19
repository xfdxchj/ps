/*
 * 破碎的地牢 (End fork) — 挑战 125/136「格林之器」的专属武器
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
 * END(挑战 136 格林之器3): 勇剑。
 *
 * <h3>原表效果</h3>
 * "第一次造成 60% 三连击，第二次 100% 必中，第三次 60% 四连击"
 *
 * <h3>三段循环</h3>
 * 每次**成功命中**推进一段，三段一循环：
 * <pre>
 *   第 1 段：3 连击，每击 60% 伤害
 *   第 2 段：1 次攻击，但**必定命中**
 *   第 3 段：4 连击，每击 60% 伤害
 *   第 4 段：回到第 1 段 …
 * </pre>
 *
 * <h3>为什么用"命中后推进"而不是"每次挥空也推进"</h3>
 * 若挥空也推进，玩家会靠空挥来"刷"到必中那一段，
 * 违背"第 2 段是奖励"的设计意图。
 */
public class BraveSword extends MeleeWeapon {

	/** 多段攻击时每击的伤害倍率。 */
	public static final float MULTI_HIT_MULT = 0.60f;
	/** 第 1 段连击数。 */
	public static final int HITS_A = 3;
	/** 第 3 段连击数。 */
	public static final int HITS_C = 4;

	{
		image = ItemSpriteSheet.GRIMM_BRAVESWORD;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.0f;
		tier = 5;
		DLY = 1f;
	}

	@Override public String name(){ return "勇剑"; }

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
				"剑锋上流转着某种祝福 —— 它奖励敢于挥剑的人。\n\n" +
				"攻击会按**三段循环**推进：\n" +
				"- 第 1 次：**3 连击**，每击 60% 伤害\n" +
				"- 第 2 次：单次攻击，但**必定命中**\n" +
				"- 第 3 次：**4 连击**，每击 60% 伤害\n" +
				"- 然后回到第 1 次\n\n" +
				"只有**命中**才会推进循环 —— 空挥不会。";
	}

	/**
	 * END(136): 当前处于第几段（0/1/2）。
	 *
	 * <p>状态挂在英雄身上的 buff 里，而不是武器实例字段 ——
	 * 武器可能在 `detach`/复制时换实例，buff 更稳。
	 */
	public static int currentStage(Char owner) {
		if (owner == null) return 0;
		BraveSwordStage s = owner.buff(BraveSwordStage.class);
		return s == null ? 0 : s.stage;
	}

	/** END(136): 推进到下一段。 */
	public static void advanceStage(Char owner) {
		if (owner == null) return;
		BraveSwordStage s = owner.buff(BraveSwordStage.class);
		if (s == null) {
			s = Buff.affect(owner, BraveSwordStage.class, 9999f);
			s.stage = 0;
		}
		s.stage = (s.stage + 1) % 3;
	}

	/**
	 * END(136): 本次攻击的连击数与是否必中。
	 *
	 * @param stage 当前段（0/1/2）
	 * @return {连击数, 是否必中}
	 */
	public static int[] attackProfile(int stage) {
		switch (stage) {
			case 1:  return new int[]{ 1, 1 };          //第 2 段：单次必中
			case 2:  return new int[]{ HITS_C, 0 };     //第 3 段：4 连击
			default: return new int[]{ HITS_A, 0 };     //第 1 段：3 连击
		}
	}

	/** END(136): 勇剑的三段循环状态（不显示图标）。 */
	public static class BraveSwordStage extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		public int stage = 0;

		@Override public int icon(){ return BuffIndicator.NONE; }

		private static final String STAGE = "stage";

		@Override
		public void storeInBundle(com.watabou.utils.Bundle bundle){
			super.storeInBundle(bundle);
			bundle.put(STAGE, stage);
		}

		@Override
		public void restoreFromBundle(com.watabou.utils.Bundle bundle){
			super.restoreFromBundle(bundle);
			stage = bundle.getInt(STAGE);
		}
	}

	@Override
	public int value(){ return 0; }
}
