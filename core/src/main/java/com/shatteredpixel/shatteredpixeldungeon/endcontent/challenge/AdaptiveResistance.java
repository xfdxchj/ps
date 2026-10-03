package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * END(231 魔虚罗): 古神 / 古神之拳的适应。
 *
 * <p>统计玩家用**近战 / 投掷 / 法术**三种方式对它造成的累计伤害。
 * 累计受伤每达到最大生命的 20% 就适应一次，最多 4 次；
 * 每次适应到当前累计伤害最高的方式，依次获得
 * 10% / 25% / 43% / 70% 的**该方式**减伤。
 */
public class AdaptiveResistance extends FlavourBuff {

	public static final int MELEE = 0;
	public static final int MISSILE = 1;
	public static final int SPELL = 2;
	public static final int TYPES = 3;

	/** 4 次适应的减伤档位（同一方式被连续适应时逐档提升）。 */
	public static final float[] TIERS = { 0.10f, 0.25f, 0.43f, 0.70f };
	public static final int MAX_STACKS = 4;
	/** 每个「区域」（每 20% 最大生命）适应一次。 */
	public static final float THRESHOLD_FRAC = 0.20f;

	private final float[] dmg = new float[TYPES];
	private final float[] reduction = new float[TYPES];
	private int stacks = 0;
	private float nextThreshold = 0f;
	private boolean started = false;

	{
		type = buffType.NEGATIVE;
		announced = false;
	}

	/** 把伤害来源分类成三种方式之一；与玩家无关的来源返回 -1。 */
	public static int classify(Object src){
		if (src instanceof Wand){
			return SPELL;
		}
		if (src instanceof Hero){
			KindOfWeapon w = ((Hero) src).belongings.attackingWeapon();
			return (w instanceof MissileWeapon) ? MISSILE : MELEE;
		}
		return -1;
	}

	/** 在 Char.damage 里调用：累加伤害并返回减伤后的伤害。 */
	public float apply(int type, float damage){
		if (type < 0 || type >= TYPES) return damage;
		if (!started){
			started = true;
			nextThreshold = Math.max(1f, target.HT * THRESHOLD_FRAC);
		}

		//先按当前减伤结算，再记录原始伤害（避免减伤让自己永远适应不了）
		float mult = Math.max(0f, 1f - reduction[type]);
		dmg[type] += damage;

		adaptIfNeeded();

		return damage * mult;
	}

	private void adaptIfNeeded(){
		while (stacks < MAX_STACKS){
			float total = dmg[MELEE] + dmg[MISSILE] + dmg[SPELL];
			if (total < nextThreshold) break;

			int dominant = MELEE;
			for (int i = 1; i < TYPES; i++){
				if (dmg[i] > dmg[dominant]) dominant = i;
			}
			reduction[dominant] = TIERS[stacks];
			stacks++;

			//每个「区域」只统计本区域内的伤害，适应后清零
			for (int i = 0; i < TYPES; i++) dmg[i] = 0f;
			nextThreshold = Math.max(1f, target.HT * THRESHOLD_FRAC);

			//END: 适应时给一个"咔哒"音效
			try {
				com.watabou.noosa.audio.Sample.INSTANCE.play(
						com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.UNLOCK);
			} catch (Throwable ignored) { }
			if (target.sprite != null){
				target.sprite.showStatus(CharSprite.WARNING, "适应·" + typeName(dominant));
			}
		}
	}

	public static String typeName(int type){
		switch (type){
			case MELEE:   return "近战";
			case MISSILE: return "投掷";
			case SPELL:   return "法术";
			default:      return "?";
		}
	}

	@Override
	public String desc(){
		return "适应层数 " + stacks + "/" + MAX_STACKS
				+ "（近战 " + Math.round(reduction[MELEE] * 100f) + "% / "
				+ "投掷 " + Math.round(reduction[MISSILE] * 100f) + "% / "
				+ "法术 " + Math.round(reduction[SPELL] * 100f) + "%）";
	}

	/** 当前对某种方式的减伤（0~1）。 */
	public float reductionFor(int type){
		if (type < 0 || type >= TYPES) return 0f;
		return reduction[type];
	}

	public int stacks(){
		return stacks;
	}

	/** 该 buff 是否应该挂在目标身上（古神 / 古神之拳）。 */
	public static boolean isAdaptiveTarget(Char ch){
		return ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu.Sukuna;
	}

	@Override public int icon(){ return BuffIndicator.NONE; }

	//——持久化 ——//
	private static final String DMG = "dmg";
	private static final String RED = "red";
	private static final String STACKS = "stacks";
	private static final String NEXT = "next";
	private static final String STARTED = "started";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(DMG, dmg);
		bundle.put(RED, reduction);
		bundle.put(STACKS, stacks);
		bundle.put(NEXT, nextThreshold);
		bundle.put(STARTED, started);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		float[] d = bundle.getFloatArray(DMG);
		if (d != null) for (int i = 0; i < TYPES && i < d.length; i++) dmg[i] = d[i];
		float[] r = bundle.getFloatArray(RED);
		if (r != null) for (int i = 0; i < TYPES && i < r.length; i++) reduction[i] = r[i];
		stacks = bundle.getInt(STACKS);
		nextThreshold = bundle.getFloat(NEXT);
		started = bundle.getBoolean(STARTED);
	}
}
