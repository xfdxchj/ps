/*
 * 破碎的地牢 (End fork) — 顶级装备「寰宇支配之剑」
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.CosmicParticles;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.noosa.particles.Emitter;

import java.util.ArrayList;

/**
 * END(顶级装备): 寰宇支配之剑。
 *
 * <h3>数值（文档所有者定稿）</h3>
 * <ul>
 *   <li>伤害 **60-80**，成长 **5-10**</li>
 *   <li>附带 **自身最大生命 10%** 的额外伤害</li>
 *   <li>不再有「目标最大生命 10%」的额外伤害（已删除）</li>
 * </ul>
 *
 * <h3>两种模式（免费切换）</h3>
 * <ul>
 *   <li>**支配模式**（默认）：单体 100% 伤害；每通过此剑击杀 5 个敌人，
 *       伤害 +1%（本局累计，跨局重置）。</li>
 *   <li>**寰宇模式**：攻击 **9x9** 范围内的所有敌人，伤害 **200%**，
 *       但攻击速度减半。</li>
 * </ul>
 */
public class UniverseSword extends MeleeWeapon {

	public static final String AC_MODE = "MODE";

	{
		image = ItemSpriteSheet.GRIMM_INFINITY_SWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.85f;
		//"顶级装备"不属于原版 1-5 阶体系，但给 5 让 STRReq 等逻辑有个基准
		tier = 5;
	}

	/** END(四): true = 寰宇模式（9x9 / 200% / 半攻速）；false = 支配模式（单体 / 击杀成长）。 */
	public boolean universeMode = false;
	/** END(四): 支配模式下，本局通过此剑击杀的敌人数；跨局重置。 */
	public int kills = 0;

	private static final String MODE = "universe_mode";
	private static final String KILLS = "universe_kills";
	/** 防止 9x9 溅射递归。 */
	private static boolean splashing = false;

	@Override public String name(){ return "寰宇支配之剑"; }

	@Override
	public Emitter emitter() {
		return CosmicParticles.equipmentEmitter();
	}

	//==== 数值：60-80，成长 5-10 ====
	//注意：**不**沿用原版的 tier 公式（那是 5-30），这里显式写死。

	@Override public int min(int lvl){ return 60 + 5 * lvl; }
	@Override public int max(int lvl){ return 80 + 10 * lvl; }

	/** 重量级武器：力量需求比同阶更高。 */
	@Override public int STRReq(int lvl){ return Math.max(18, 18 + lvl); }

	//==== 特效常量 ====

	/** 额外伤害 = 自身最大生命的 10%。 */
	public static final float MAX_HP_BONUS = 0.10f;
	/** 支配模式：每击杀 5 个敌人 +1% 伤害。 */
	public static final int KILLS_PER_STACK = 5;
	public static final float KILL_STACK_BONUS = 0.01f;
	/** 寰宇模式：9x9 = 半径 4（Chebyshev 距离）。 */
	public static final int AOE_RADIUS = 4;
	/** 寰宇模式伤害倍率。 */
	public static final float UNIVERSE_DAMAGE_MULT = 2f;
	/** 寰宇模式攻速倍率（0.5 = 半攻速）。 */
	public static final float UNIVERSE_SPEED_MULT = 0.5f;

	//==== 模式切换 ====

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) actions.add(AC_MODE);
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (AC_MODE.equals(action)) return "切换模式";
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (AC_MODE.equals(action) && isEquipped(hero)) {
			universeMode = !universeMode;
			GLog.p("寰宇支配之剑切换为：" + (universeMode
					? "寰宇模式（9x9 / 200% / 半攻速）"
					: "支配模式（单体 / 每 5 杀 +1%）"));
		}
	}

	@Override
	protected float speedMultiplier(Char owner) {
		float s = super.speedMultiplier(owner);
		if (universeMode) s *= UNIVERSE_SPEED_MULT;
		return s;
	}

	//==== 伤害 ====

	@Override
	public int damageRoll(Char owner) {
		int dmg = super.damageRoll(owner);

		//① 自身最大生命的 10%（保留）
		if (owner != null) {
			dmg += Math.round(owner.HT * MAX_HP_BONUS);
		}

		//② 模式
		if (universeMode) {
			dmg = Math.round(dmg * UNIVERSE_DAMAGE_MULT);
		} else if (kills >= KILLS_PER_STACK) {
			dmg = Math.round(dmg * (1f + KILL_STACK_BONUS * (kills / KILLS_PER_STACK)));
		}

		return dmg;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);

		//① 已删除「目标最大生命 10%」的额外伤害。

		//② 寰宇模式：9x9 范围内其它敌人同样吃这次伤害。
		if (universeMode && attacker != null && defender != null && !splashing) {
			splashing = true;
			try {
				for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
					if (m != defender && m.isAlive() && m.alignment != attacker.alignment
							&& Dungeon.level.distance(defender.pos, m.pos) <= AOE_RADIUS) {
						m.damage(Math.max(1, damage), attacker);
					}
				}
			} finally {
				splashing = false;
			}
		}

		//③ 支配模式：统计击杀（用于 +1%/5 杀）。
		if (!universeMode && attacker != null && defender != null
				&& defender.isAlive() && defender.HP <= damage) {
			kills++;
		}

		return damage;
	}

	//==== 存档 ====

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(MODE, universeMode);
		bundle.put(KILLS, kills);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		universeMode = bundle.getBoolean(MODE);
		kills = bundle.getInt(KILLS);
	}

	@Override
	public String info() {
		int stacks = kills / KILLS_PER_STACK;
		return "剑身上浮着整片星图，每一次挥动都像在挪动某个世界。\n\n"
				+ "基础伤害 60-80，每级 +5~+10；额外附带自身最大生命 10% 的伤害。\n\n"
				+ "-**支配模式**（默认）：单体 100% 伤害；每通过此剑击杀 5 个敌人伤害 +1%（跨局重置）。\n"
				+ "　　当前击杀 " + kills + "，加成 +" + stacks + "%。\n"
				+ "-**寰宇模式**：攻击 9x9 范围内所有敌人，伤害 200%，攻击速度减半。\n"
				+ "-切换模式不消耗任何资源。"
				+ com.shatteredpixel.shatteredpixeldungeon.endcontent.EndItemStats.block(this);
	}

	@Override
	public String desc() {
		return info();
	}
}
