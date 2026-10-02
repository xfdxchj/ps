package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.watabou.utils.Random;

/**
 * END(230 原神地牢): 戒指词条。
 *
 * <p>每强化 3 级获得 1 条，最多 3 条；获得时随机，可用洗练石重掷。
 * 词条数值：生命 +20% 最大生命、回复 0.2/回合、暴击率 +20%、
 * 闪避 +20%、命中 +20%、防御 +20%、充能效率 +20%、近战伤害 +20%、投掷伤害 +20%、法术伤害 +20%。
 */
public enum RingAffix {

	HP("生命", 0.20f, "最大生命"),
	REGEN("回复", 0.20f, "每回合回复生命"),
	CRIT("暴击率", 0.20f, "暴击率"),
	EVASION("闪避", 0.20f, "闪避"),
	ACCURACY("命中", 0.20f, "命中"),
	DEFENSE("防御", 0.20f, "防御"),
	CHARGE("充能效率", 0.20f, "充能效率"),
	//END(二.12): 副词条
	MELEE_DMG("近战伤害", 0.20f, "近战伤害"),
	MISSILE_DMG("投掷伤害", 0.20f, "投掷伤害"),
	SPELL_DMG("法术伤害", 0.20f, "法术伤害");

	public final String label;
	public final float value;
	public final String effectText;

	RingAffix(String label, float value, String effectText){
		this.label = label;
		this.value = value;
		this.effectText = effectText;
	}

	/** 每多少级获得 1 条词条。 */
	public static final int LEVELS_PER_AFFIX = 3;
	/** 最多几条词条。 */
	public static final int MAX_AFFIXES = 3;
	/** 每条词条最多强化几次。 */
	public static final int MAX_UPGRADES = 3;
	/** 每次强化的乘算加成档位。 */
	public static final float[] UPGRADE_BONUSES = { 0.20f, 0.35f, 0.40f };

	/** 供文本使用的 "+20% / +35% / +40%" 串，避免说明里再手写数字。 */
	public static String upgradeBonusText(){
		StringBuilder sb = new StringBuilder();
		for (float b : UPGRADE_BONUSES){
			if (sb.length() > 0) sb.append(" / ");
			sb.append("+").append(Math.round(b * 100f)).append("%");
		}
		return sb.toString();
	}

	/** 每 3 级 1 条，最多 3 条。 */
	public static int countFor(int level){
		return Math.min(MAX_AFFIXES, Math.max(0, level) / LEVELS_PER_AFFIX);
	}

	/** 随机一条词条（允许重复）。 */
	public static RingAffix randomAffix(){
		return values()[Random.Int(values().length)];
	}

	/** 生成 count 条随机词条。 */
	public static RingAffix[] roll(int count){
		if (count <= 0) return new RingAffix[0];
		RingAffix[] out = new RingAffix[count];
		for (int i = 0; i < count; i++) out[i] = randomAffix();
		return out;
	}

	/** 某角色身上所有已装备戒指的某类词条值合计。 */
	public static float total(Char ch, RingAffix type){
		if (ch == null || !ChallengeEffects.genshinEnabled()) return 0f;
		if (!(ch instanceof Hero)) return 0f;
		Hero hero = (Hero) ch;
		float sum = 0f;
		try {
			for (Item it : hero.belongings){
				if (it instanceof Ring && it.isEquipped(hero)){
					Ring r = (Ring) it;
					if (r.affixes != null){
						//END(二.13): 按"基础值 × 升级倍率"计入
						for (int i = 0; i < r.affixes.length; i++){
							if (r.affixes[i] == type) sum += r.affixValue(i);
						}
					}
				}
			}
		} catch (Throwable ignored){ }
		return sum;
	}

	public static float htMultiplier(Hero hero){ return 1f + total(hero, HP); }
	public static float regenPerTurn(Hero hero){ return total(hero, REGEN); }
	public static float critChance(Hero hero){ return Math.min(1f, total(hero, CRIT)); }
	public static float evasionMultiplier(Hero hero){ return 1f + total(hero, EVASION); }
	public static float accuracyMultiplier(Hero hero){ return 1f + total(hero, ACCURACY); }
	public static float defenseMultiplier(Hero hero){ return Math.max(0f, 1f - total(hero, DEFENSE)); }
	public static float chargeMultiplier(Char ch){ return 1f + total(ch, CHARGE); }
	public static float meleeDamageMultiplier(Hero hero){ return 1f + total(hero, MELEE_DMG); }
	public static float missileDamageMultiplier(Hero hero){ return 1f + total(hero, MISSILE_DMG); }
	public static float spellDamageMultiplier(Hero hero){ return 1f + total(hero, SPELL_DMG); }
}
