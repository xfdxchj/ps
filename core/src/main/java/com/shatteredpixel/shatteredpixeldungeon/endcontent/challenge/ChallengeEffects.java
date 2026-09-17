/*
 * 破碎的地牢 (End fork) — 挑战规则的运行时效果
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeBerserkMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

/**
 * END(挑战框架): 挑战规则的**运行时数值修饰**。
 *
 * <h3>伤害管线（7 步，权威定义）</h3>
 * <pre>
 * 第1步 基础伤害        武器基础伤害 × 力量加成 × 武器等级加成
 * 第2步 攻击方增益      **加法叠加**：玻璃大炮+20% 破釜沉舟+30%(条件)
 *                       极致攻哈+20% 黄金蜂蜜酒+50%
 * 第3步 低血倍率        **乘法叠加**：亡者之怒×2(<10%) 怨恨之剑×1~3
 *                       极端状态×2
 * 第4步 特殊替换        等我启动 → 直接替换为 20%/50%/110%（优先级最高）
 * 第5步 目标方减伤      目标护甲减免 → 脆弱+13%
 * 第6步 最终拦截        物极必反(>150%最大生命→免疫) 九九归一(%9==0→1)
 * 第7步 应用伤害
 * </pre>
 *
 * <p><b>易档规则只涉及第 2、3、5 步</b>。第 4、6 步是中档，
 * 本类预留了钩子（{@link #specialOverride} / {@link #finalIntercept}），
 * 目前为恒等实现。
 *
 * <h3>为什么第 2 步必须加法</h3>
 * 若把 +20%、+30%、+20% 连乘会得到 ×1.872，而正确结果是 ×1.70(+70%)。
 * 偏差会随规则数量迅速放大，所以**加法叠加是硬性要求**，见 {@link #additiveBonus}。
 */
public final class ChallengeEffects {

	private ChallengeEffects() {}

	//==== 规则 ID 常量（表 ID，与原表一致）====

	/** 9 狂暴：怪物受击后获得 20% 攻击提升。 */
	public static final int BERSERK          = 9;
	/** 11 脆弱：玩家与怪物受到的伤害提高 13%。 */
	public static final int FRAGILE          = 11;
	/** 12 玻璃大炮：玩家攻击提高 20%，生命降低 13%。 */
	public static final int GLASS_CANNON     = 12;
	/** 14 精英强化：精英怪获得 20% 属性。 */
	public static final int ELITE_BOOST      = 14;
	/** 25 越战越勇：怪物每损失 20% 生命，攻击力 +10%。 */
	public static final int GROWING_FURY     = 25;
	/** 26 破釜沉舟：玩家生命低于 30% 时，攻击 +30%、攻速 +20%。 */
	public static final int LAST_STAND       = 26;
	/** 27 极致攻哈：玩家攻击 +20%，怪物攻击 +20%。 */
	public static final int ALL_OUT          = 27;
	/** 135 亡者之怒：玩家生命低于 10% 时伤害翻倍。 */
	public static final int WRATH_OF_DEAD    = 135;

	//==== 第 2 步：加法增益的加数 ====

	/** 12 玻璃大炮：+20%。 */
	private static final float BONUS_GLASS        = 0.20f;
	/** 26 破釜沉舟：+30%（条件：HP<30%）。 */
	private static final float BONUS_LAST_STAND   = 0.30f;
	/** 27 极致攻哈：+20%。 */
	private static final float BONUS_ALL_OUT      = 0.20f;

	//==== 第 3 步：乘法倍率 ====

	/** 135 亡者之怒：×2（条件：HP<10%）。 */
	private static final float MULT_WRATH         = 2.00f;
	/** 25 越战越勇：每损失 20% 生命 ×(1+10%)，封顶 4 档。 */
	private static final float FURY_PER_STEP      = 0.10f;
	private static final int   FURY_STEP_PERCENT  = 20;
	private static final int   FURY_MAX_STEPS     = 4;

	//==== 第 2 步（怪物侧）====

	/** 9 狂暴：怪物受击后 +20%（不叠加）。 */
	private static final float BONUS_BERSERK      = 0.20f;

	//==== 第 5 步：承伤 ====

	/** 11 脆弱：承伤 +13%。 */
	private static final float TAKEN_FRAGILE      = 1.13f;

	//==== 其他 ====

	/** 12 玻璃大炮：生命上限 ×0.87。 */
	private static final float GLASS_HT           = 0.87f;
	/** 14 精英强化：属性 ×1.2。 */
	private static final float ELITE_MULT         = 1.20f;
	/** 26 破釜沉舟：攻速 ×1.2（条件：HP<30%）。 */
	private static final float LAST_STAND_SPEED   = 1.20f;
	/** 26 破釜沉舟 / 135 亡者之怒 的血量阈值。 */
	private static final float HP_FRAC_LAST_STAND = 0.30f;
	private static final float HP_FRAC_WRATH      = 0.10f;

	//==== 便捷判断 ====

	private static boolean on(int id) {
		return Dungeon.challengeMask != null && Dungeon.challengeMask.has(id);
	}

	//==== 第 2 步：加法增益 ====

	/**
	 * END(第2步·攻击方增益): 计算**加法叠加**的增益总和。
	 *
	 * <p>返回值是"加数"，例如 0.70f 表示 +70%。调用方应做
	 * {@code dmg *= (1f + bonus)}，**不要**把多条规则各自连乘。
	 *
	 * <p>涉及：12 玻璃大炮 +20%、26 破釜沉舟 +30%(条件)、
	 * 27 极致攻哈 +20%。
	 *
	 * <p>（134 黄金蜂蜜酒 +50%、68 极端状态 属中档，暂未实装。）
	 *
	 * @return 增益总和（无任何增益时为 0f）
	 */
	public static float additiveBonus(Char attacker) {
		if (attacker == null) return 0f;

		float bonus = 0f;

		if (attacker instanceof Hero) {

			//12 玻璃大炮：玩家攻击 +20%
			if (on(GLASS_CANNON)) {
				bonus += BONUS_GLASS;
			}

			//27 极致攻哈：玩家攻击 +20%
			if (on(ALL_OUT)) {
				bonus += BONUS_ALL_OUT;
			}

			//26 破釜沉舟：HP < 30% 时攻击 +30%
			if (on(LAST_STAND) && isBelowHpFraction(attacker, HP_FRAC_LAST_STAND)) {
				bonus += BONUS_LAST_STAND;
			}

		} else if (attacker instanceof Mob) {

			//27 极致攻哈：怪物攻击 +20%
			if (on(ALL_OUT)) {
				bonus += BONUS_ALL_OUT;
			}

			//9 狂暴：怪物受击后 +20%（不叠加）
			if (on(BERSERK) && isBerserk(attacker)) {
				bonus += BONUS_BERSERK;
			}
		}

		return bonus;
	}

	//==== 第 3 步：低血乘法倍率 ====

	/**
	 * END(第3步·低血倍率): 计算**乘法叠加**的倍率。
	 *
	 * <p>返回值是乘数，例如 2f 表示翻倍。调用方应做 {@code dmg *= mult}。
	 *
	 * <p>涉及：135 亡者之怒 ×2(HP&lt;10%)、25 越战越勇(怪物按已损生命)。
	 *
	 * <p>（怨恨之剑 125/136、极端状态 68 属难档/中档，暂未实装。）
	 */
	public static float lowHpMultiplier(Char attacker) {
		if (attacker == null) return 1f;

		float mult = 1f;

		if (attacker instanceof Hero) {

			//135 亡者之怒：HP < 10% 时伤害翻倍
			if (on(WRATH_OF_DEAD) && isBelowHpFraction(attacker, HP_FRAC_WRATH)) {
				mult *= MULT_WRATH;
			}

		} else if (attacker instanceof Mob) {

			//25 越战越勇：每损失 20% 生命，攻击 ×(1+10%)
			if (on(GROWING_FURY)) {
				mult *= growingFuryMultiplier(attacker);
			}
		}

		return mult;
	}

	//==== 第 4 步：特殊替换（中档，预留钩子）====

	/**
	 * END(第4步·特殊替换): 高优先级伤害替换。
	 *
	 * <p>「20 等我启动」会把伤害**直接替换**为固定比例（第一次20%/第二次50%/
	 * 第三次及以后110%），而不是乘系数。属**中档**，尚未实装。
	 *
	 * @param attacker 攻击方
	 * @param enemy    目标
	 * @param dmg      当前伤害（第 3 步之后）
	 * @return 替换后的伤害；未实装时**原样返回**
	 */
	public static float specialOverride(Char attacker, Char enemy, float dmg) {
		//TODO(中档): 20 等我启动 —— 需要记录"对同一目标的攻击次数"
		return dmg;
	}

	//==== 第 5 步：目标方减伤 ====

	/**
	 * END(11 脆弱): 承伤修饰 —— **玩家与怪物都**受到 +13% 伤害。
	 *
	 * <p>调用点：{@code Char.damage()} 里 {@code dmg = Math.round(damage)} 之后。
	 * 那里护甲减免**已经**完成，正符合"第 5 步：护甲减免 → 脆弱"的顺序。
	 *
	 * @param ch  受伤者
	 * @param dmg 已算完护甲减免的伤害
	 */
	public static int damageTaken(Char ch, int dmg) {
		if (dmg <= 0) return dmg;
		if (!on(FRAGILE)) return dmg;

		return Math.max(1, Math.round(dmg * TAKEN_FRAGILE));
	}

	//==== 第 6 步：最终拦截（中档，预留钩子）====

	/**
	 * END(第6步·最终拦截): 伤害生效前的最后判定。
	 *
	 * <p>「22 物极必反」（伤害 &gt; 目标最大生命 150% → 完全免疫）与
	 * 「69 九九归一」（伤害 % 9 == 0 → 变为 1）都属**中档**，尚未实装。
	 *
	 * @return 拦截后的伤害；返回值 &lt;= 0 表示完全免疫
	 */
	public static int finalIntercept(Char target, int dmg) {
		//TODO(中档): 22 物极必反 / 69 九九归一
		return dmg;
	}

	//==== 速度侧 ====

	/**
	 * END(速度类规则): 速度倍率（1f = 不变）。
	 *
	 * <p>涉及：26 破釜沉舟（玩家 HP&lt;30% 时攻速 ×1.2）。
	 * （16 大力水手、19 风驰电掣、13 狂热 属后续批次。）
	 */
	public static float speedModifier(Char ch) {
		if (ch == null) return 1f;

		if (ch instanceof Hero) {
			if (on(LAST_STAND) && isBelowHpFraction(ch, HP_FRAC_LAST_STAND)) {
				return LAST_STAND_SPEED;
			}
		}

		return 1f;
	}

	//==== 生命上限侧 ====

	/** END(12 玻璃大炮): 玩家生命上限倍率（HT 降低后不低于 1）。 */
	public static float heroHtMultiplier() {
		return on(GLASS_CANNON) ? GLASS_HT : 1f;
	}

	/** END(14 精英强化): 精英怪属性倍率（"谁是精英"由 4/75 决定）。 */
	public static float eliteStatMultiplier() {
		return on(ELITE_BOOST) ? ELITE_MULT : 1f;
	}

	//==== 内部辅助 ====

	/**
	 * 当前 HP 是否严格低于 HT 的给定比例。
	 *
	 * <p><b>为什么用整数比较而不是 {@code HP < HT * frac}：</b>
	 * {@code 0.30f} 的实际值是 0.30000001…，于是 {@code HT=100, HP=30} 时
	 * {@code 30 < 100*0.30000001f = 30.000001} 为 <b>true</b> ——
	 * 本该"严格小于 30% 才触发"的规则会在恰好 30% 时错误触发。
	 * 改成整数交叉相乘 {@code HP*100 < HT*30} 后判定精确。
	 */
	private static boolean isBelowHpFraction(Char ch, float frac) {
		if (ch.HT <= 0) return false;
		int pct = Math.round(frac * 100f);
		return ch.HP * 100 < ch.HT * pct;
	}

	/**
	 * END(25 越战越勇): 按已损失生命计算攻击倍率。
	 * <p>每损失 20% 给 +10%，最多 4 档（损失 80%+）→ 上限 ×1.4。
	 * 实时按当前 HP 计算，回血后加成自然降低。
	 *
	 * <p>同样用**整数**算档数：{@code (int)(lostFrac/0.2f)} 在
	 * HP=80/HT=100 时会因 0.19999999 得到 0 档，而正确答案是 1 档。
	 * 改成 {@code (HT-HP)*100/(HT*20)} 后精确。
	 */
	private static float growingFuryMultiplier(Char ch) {
		if (ch.HT <= 0) return 1f;

		int lost = ch.HT - ch.HP;
		if (lost <= 0) return 1f;

		//每损失 HT 的 20% 算一档：档数 = 已损% / 20
		int steps = (lost * 100) / (ch.HT * FURY_STEP_PERCENT);
		if (steps > FURY_MAX_STEPS) steps = FURY_MAX_STEPS;
		if (steps <= 0) return 1f;

		return 1f + steps * FURY_PER_STEP;
	}

	/** END(9 狂暴): 该怪物是否处于"受击后狂暴"状态（见 {@link ChallengeBerserkMark}）。 */
	public static boolean isBerserk(Char ch) {
		return ch != null && ch.buff(ChallengeBerserkMark.class) != null;
	}

	/**
	 * END(9 狂暴): 给受击的怪物挂上狂暴标记。
	 * <p>由 {@code Mob.damage()} 调用；持续 2 回合（见功能大全 ID 9）。
	 */
	public static void markBerserk(Char ch) {
		if (ch == null) return;
		if (!on(BERSERK)) return;
		if (!(ch instanceof Mob)) return;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
				ch, ChallengeBerserkMark.class, ChallengeBerserkMark.DURATION);
	}
}
