/*
 * 破碎的地牢 (End fork) — 顶级装备「虚空不灭之甲」
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/**
 * END(顶级装备): 虚空不灭之甲。
 *
 * <h3>数值（文档所有者定稿）</h3>
 * <ul>
 *   <li>护甲值 **5-10**，成长 **5-10**</li>
 *   <li>免伤 {@code 15 + 升级等级} %，**最多 50%**</li>
 *   <li>每 **50 回合**回复 **50%** 生命</li>
 *   <li>受到致命伤时触发**祝福十字架**效果，CD **50 回合**</li>
 *   <li>{@code 15 + 升级等级} % 概率**完全免疫**一次伤害（上限 50%）</li>
 * </ul>
 */
public class VoidArmor extends Armor {

	{
		image = ItemSpriteSheet.GRIMM_INFINITY_ARMOR;
	}

	/** "顶级装备"：tier 走构造函数（与原版一致）。 */
	public VoidArmor() {
		super( 5 );
	}

	@Override public String name(){ return "虚空不灭之甲"; }

	//==== 数值：5-10，成长 5-10 ====

	@Override public int DRMin(int lvl){ return 5 + 5 * lvl; }
	@Override public int DRMax(int lvl){ return 10 + 10 * lvl; }

	@Override public int STRReq(int lvl){ return Math.max(16, 16 + lvl); }

	//==== 特效参数 ====

	/** 免伤基数（%）。 */
	public static final int REDUCTION_BASE = 15;
	/** 免伤上限（%）。 */
	public static final int REDUCTION_CAP = 50;
	/** 回血间隔（回合）。 */
	public static final int REGEN_INTERVAL = 50;
	/** 回血比例（最大生命的百分比）。 */
	public static final float REGEN_PCT = 0.50f;
	/** 免疫概率上限（%）—— 与免伤共用同一套基数公式。 */
	public static final int IMMUNE_CAP = 50;
	/** 致命伤保命的冷却（回合）。 */
	public static final int SECOND_LIFE_CD = 50;

	/**
	 * END: 当前免伤比例（0.0~0.5）。
	 *
	 * <p>公式 {@code 15 + 升级等级}%，上限 50%。
	 */
	public static float damageReduction(int lvl){
		int pct = Math.min(REDUCTION_CAP, REDUCTION_BASE + lvl);
		return pct / 100f;
	}

	/** END: 当前免疫概率（0.0~0.5）。 */
	public static float immuneChance(int lvl){
		int pct = Math.min(IMMUNE_CAP, REDUCTION_BASE + lvl);
		return pct / 100f;
	}

	@Override
	public String info(){
		return "甲面上什么也映不出来 —— 包括你自己。\n\n" +
				"护甲值 5-10，每级 +5~+10。\n\n" +
				"-免伤 **15 + 升级等级** %（最多 **50%**）\n" +
				"-**15 + 升级等级** % 概率完全免疫一次伤害（最多 50%）\n" +
				"-每 **50 回合**回复 **50%** 生命\n" +
				"-受到致命伤时触发**祝福十字架**，冷却 **50 回合**";
	}

	@Override
	public String desc(){
		return info();
	}


	/**
	 * END: 伤害拦截。
	 *
	 * <p>原版 {@code Armor} 没有统一的"受伤前回调"，所以这里由
	 * {@code Char.damage()} 主动查询（见那里的接线）。
	 *
	 * @return 处理后的伤害；返回 -1 表示"完全免疫"（调用方应直接跳过扣血）
	 */
	public static int absorb(Char target, int dmg){
		if (target == null) return dmg;

		VoidArmor a = equippedOn(target);
		if (a == null) return dmg;
		int lvl = a.buffedLvl();

		//① 概率完全免疫
		if (com.watabou.utils.Random.Float() < immuneChance(lvl)){
			return -1;
		}

		//② 免伤
		int reduced = Math.max(1, Math.round(dmg * (1f - damageReduction(lvl))));
		return reduced;
	}

	/** END: 取某个角色身上穿的虚空甲；没穿返回 null。 */
	public static VoidArmor equippedOn(Char ch){
		if (!(ch instanceof Hero)) return null;
		Armor a = ((Hero) ch).belongings.armor();
		return (a instanceof VoidArmor) ? (VoidArmor) a : null;
	}

	/**
	 * END: 致命伤保命（祝福十字架效果）。
	 *
	 * @return true 表示已保命（调用方不应再扣血）
	 */
	public static boolean secondLife(Char target, int dmg){
		if (target == null) return false;
		VoidArmor a = equippedOn(target);
		if (a == null) return false;
		if (dmg < target.HP) return false;              //不是致命伤

		VoidArmorCooldown cd = target.buff(VoidArmorCooldown.class);
		if (cd != null) return false;                   //还在冷却

		//保命：留 1 点血 + 进入 50 回合冷却
		target.HP = 1;
		Buff.affect(target, VoidArmorCooldown.class, SECOND_LIFE_CD);

		if (target.sprite != null){
			target.sprite.showStatus(
					com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE,
					"不灭");
		}
		return true;
	}

	/** END: 每 50 回合回复 50% 生命（由 VoidArmorRegen 计时器驱动）。 */
	public static void tickRegen(Char target, float time){
		if (target == null) return;
		if (equippedOn(target) == null) return;

		VoidArmorRegen r = target.buff(VoidArmorRegen.class);
		if (r == null){
			r = Buff.affect(target, VoidArmorRegen.class);
		}
		r.accumulate(time);
	}

	/** END: 致命伤保命的冷却计时。 */
	public static class VoidArmorCooldown extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}
		@Override public int icon(){ return BuffIndicator.NONE; }
	}

	/** END: 回血计时器（每 50 回合回 50%）。 */
	public static class VoidArmorRegen extends FlavourBuff {
		{
			type = buffType.POSITIVE;
			announced = false;
		}

		private float left = REGEN_INTERVAL;

		public void accumulate(float time){
			left -= time;
			while (left <= 0){
				left += REGEN_INTERVAL;
				if (target != null && target.isAlive() && target.HP < target.HT){
					int heal = Math.max(1, Math.round(target.HT * REGEN_PCT));
					target.HP = Math.min(target.HT, target.HP + heal);
					if (target.sprite != null){
						target.sprite.showStatus(
								com.shatteredpixel.shatteredpixeldungeon.sprites
										.CharSprite.POSITIVE,
								"+" + heal);
					}
				}
			}
		}

		@Override
		public boolean act(){
			accumulate(1f);
			spend(1f);
			return true;
		}

		@Override public int icon(){ return BuffIndicator.BLESS; }   //没有专门的回血图标，借用祝福
		@Override public void tintIcon(Image icon){ icon.hardlight(0.6f, 0.3f, 0.9f); }
	}
}
