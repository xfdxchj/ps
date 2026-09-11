package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * END(移植自魔绫·挑战区): Boss 基类。
 *
 * 说明：魔绫原版 actors/Boss.java 为 237 行，且依赖一批魔绫主线物品
 * (IceCyanBlueSquareCoin/KingGold/KingsCrown/TengusMask/Prop/SmallLeafHardDungeon/Conducts 等)。
 * 本 fork 是"挑战区移植"，不需要那些主线物品，故此处只保留 **挑战区 Boss 真正用到** 的骨架：
 *   - 属性表驱动：initProperty() / initBaseStatus(...) / initStatus(exp)
 *   - 由属性表派生的 damageRoll() / attackSkill() / drRoll()
 *   - noDropIceCoin 字段（存档兼容）
 * 被省略（均为魔绫主线逻辑，与挑战区无关）：
 *   - die() 里的蓝币结算 / 困难模式小叶子 / 掉落道具
 *   - SprintableModeBoolean（依赖 Conducts/bossRushMode 组合）
 *   - act() 里的 VenomGas 相关逻辑
 *   - attackDelay() 的 gameNight 加成、RollCS()/RollEX()/notice()
 */
abstract public class Boss extends Mob {

	{
		//与魔绫一致：Boss 默认免疫恐惧
		immunities.add( Terror.class );
	}

	protected static float baseMin;    //最小伤害
	protected static float baseMax;    //最大伤害
	protected static float baseAcc;    //命中率
	protected static float baseEva;    //闪避率
	protected static float baseHT;     //生命值
	protected static float baseMinDef; //最小防御
	protected static float baseMaxDef; //最大防御

	/** 添加 Boss 通用属性与免疫 */
	protected void initProperty() {
		properties.add( Property.BOSS );
		immunities.add( Grim.class );
		immunities.add( ScrollOfPsionicBlast.class );
		immunities.add( ScrollOfRetribution.class );
		immunities.add( Corruption.class );
	}

	/**
	 * @param min 最小伤害
	 * @param max 最大伤害
	 * @param acc 命中率
	 * @param eva 闪避率
	 * @param ht  生命值
	 * @param mid 最小防御
	 * @param mad 最大防御
	 */
	protected void initBaseStatus(float min, float max, float acc, float eva, float ht, float mid, float mad) {
		baseMin = min;
		baseMax = max;
		baseAcc = acc;
		baseEva = eva;
		baseHT = ht;
		baseMinDef = mid;
		baseMaxDef = mad;
	}

	/** 由属性表写入闪避/经验/生命 */
	protected void initStatus(int exp) {
		defenseSkill = Math.round( baseEva );
		EXP = exp;
		HP = HT = Math.round( baseHT );
	}

	@Override
	public int damageRoll() {
		return Math.round( Random.NormalFloat( baseMin, baseMax ) );
	}

	@Override
	public int attackSkill( Char target ) {
		return Math.round( baseAcc );
	}

	@Override
	public int drRoll() {
		return Math.round( Random.NormalFloat( baseMinDef, baseMaxDef ) );
	}

	public boolean noDropIceCoin = false;

	private static final String NO_DROP_ICE_COIN_KEY = "noDropIceCoin";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( NO_DROP_ICE_COIN_KEY, noDropIceCoin );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains( NO_DROP_ICE_COIN_KEY )) {
			noDropIceCoin = bundle.getBoolean( NO_DROP_ICE_COIN_KEY );
		}
	}
}
