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
import com.watabou.utils.Random;

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

	//==== 第二批：生成数量 / 掉落类规则 ID ====

	/** 30 人口密集：普通怪物生成数量 +20%。 */
	public static final int CROWDED          = 30;
	/** 35 丰饶：资源及物品掉落增加。 */
	public static final int ABUNDANCE        = 35;
	/** 36 贫瘠：资源及物品掉落减少。 */
	public static final int BARREN           = 36;
	/** 47 稀缺补给：消耗品生成数量减少。 */
	public static final int SCARCE_SUPPLIES  = 47;
	/** 48 过量补给：消耗品生成数量增加。 */
	public static final int EXCESS_SUPPLIES  = 48;
	/** 61 炸弹狂魔：每层额外增加 1 个炸弹。 */
	public static final int BOMB_FANATIC     = 61;
	/** 62 芙莉莲：宝箱生成概率与数量 +20%。 */
	public static final int FRIEREN          = 62;
	/** 64 宝物猎人：普通怪物掉落降低，宝箱内容增加。 */
	public static final int TREASURE_HUNTER  = 64;
	/** 119 怪物浪潮：生成率 +300%，怪物数值变为 20%。 */
	public static final int MONSTER_WAVE     = 119;

	//==== 第三批：商店 / 经济类规则 ID ====

	/** 34 赏金制度：击杀精英怪、Boss 获得额外金币或资源。 */
	public static final int BOUNTY           = 34;
	/** 37 高价回收：商店回收物品价格更高。 */
	public static final int HIGH_BUYBACK     = 37;
	/** 43 一贫如洗：进入下一区域时金币减少 20%。 */
	public static final int DESTITUTE        = 43;
	/** 44 慷慨商人：商店商品数量增加，价格提高。 */
	public static final int GENEROUS_MERCHANT= 44;

	//==== 经济类系数 ====

	/** 37 高价回收：回收价 ×1.5。 */
	private static final float BUYBACK_MULT        = 1.50f;
	/** 43 一贫如洗：进新区域金币 ×0.8（即 -20%）。 */
	private static final float DESTITUTE_MULT      = 0.80f;
	/** 44 慷慨商人：商品数 +30%。 */
	private static final float MERCHANT_COUNT_MULT = 1.30f;
	/** 44 慷慨商人：价格 ×1.25。 */
	private static final float MERCHANT_PRICE_MULT = 1.25f;
	/** 34 赏金制度：击杀精英/Boss 的额外金币。 */
	private static final int   BOUNTY_GOLD_ELITE   = 30;
	private static final int   BOUNTY_GOLD_BOSS    = 120;

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

			//16 大力水手：物理攻击 +25%（攻速惩罚在 speedModifier 里）
			//"物理"判据：玩家手持近战武器。法杖/投掷物不计入。
			if (on(POPEYE) && isPhysicalAttacker(attacker)) {
				bonus += (POPEYE_DMG_MULT - 1f);   // 1.25 → +0.25
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

		float mult = 1f;

		if (ch instanceof Hero) {
			if (on(LAST_STAND) && isBelowHpFraction(ch, HP_FRAC_LAST_STAND)) {
				mult *= LAST_STAND_SPEED;
			}
		}

		//==== 第五批速度类 ====
		//16 大力水手：玩家攻速 −20%
		mult *= popeyeSpeedMultiplier(ch);
		//19 风驰电掣：玩家 +20%、怪物 +20%
		mult *= swiftSpeedMultiplier(ch);
		//13 狂热：怪物攻击命中后叠加攻速（封顶 3 层）
		mult *= frenzySpeedMultiplier(ch);

		return mult;
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
	 * END(16 大力水手): 攻击方是否在"物理攻击"。
	 *
	 * <p>判据：玩家且手持**近战武器**。法杖、投掷物、徒手都算非物理
	 * （徒手虽然造成物理伤害，但原表说的是"物理攻击 +25%"，
	 * 为与"武器"语义一致，这里只认近战武器）。
	 */
	private static boolean isPhysicalAttacker(Char attacker) {
		if (!(attacker instanceof Hero)) return false;

		Hero hero = (Hero) attacker;
		if (hero.belongings == null) return false;

		return hero.belongings.attackingWeapon()
				instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
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

	//==================================================================
	//第二批：生成数量 / 掉落类（30 / 119 / 47 / 48 / 35 / 36 / 61 / 62 / 64）
	//==================================================================

	//==== 系数常量 ====

	/** 30 人口密集：刷怪数量 +20%。 */
	private static final float CROWDED_MOBS_MULT   = 1.20f;
	/** 119 怪物浪潮：刷怪数量 ×4（原文"生成率提升 300%"）。 */
	private static final float WAVE_MOBS_MULT      = 4.00f;
	/** 119 怪物浪潮：怪物数值 ×0.2。 */
	private static final float WAVE_STAT_MULT      = 0.20f;
	/** 47 稀缺补给：消耗品 ×0.6。 */
	private static final float SCARCE_ITEM_MULT    = 0.60f;
	/** 48 过量补给：消耗品 ×1.5。 */
	private static final float EXCESS_ITEM_MULT    = 1.50f;
	/** 35 丰饶：掉落 +25%。 */
	private static final float ABUNDANCE_MULT      = 1.25f;
	/** 36 贫瘠：掉落 −25%。 */
	private static final float BARREN_MULT         = 0.75f;
	/** 62 芙莉莲：宝箱概率/数量 ×1.2。 */
	private static final float FRIEREN_MULT        = 1.20f;
	/** 64 宝物猎人：普通怪掉落 ×0.7，宝箱物品 +1。 */
	private static final float HUNTER_DROP_MULT    = 0.70f;
	private static final int   HUNTER_CHEST_BONUS  = 1;

	/**
	 * END(30 人口密集 + 119 怪物浪潮): 刷怪数量倍率。
	 *
	 * <p>调用点：{@code RegularLevel.mobLimit()}。
	 * 两条可叠加（原文明确 119 与 30 联动）：1.2 × 4 = 4.8。
	 *
	 * <p><b>Boss 层不受影响</b> —— {@code RegularLevel} 只管常规层，
	 * Boss 层是其它关卡类，天然不经过这里。
	 */
	public static float mobCountMultiplier() {
		float mult = 1f;
		if (on(CROWDED))      mult *= CROWDED_MOBS_MULT;
		if (on(MONSTER_WAVE)) mult *= WAVE_MOBS_MULT;
		return mult;
	}

	/**
	 * END(119 怪物浪潮): 怪物**数值**倍率（HP / 伤害 / 命中 / 闪避）。
	 *
	 * <p>调用点：{@code Level.createMob()} —— 全游戏怪物实例化的唯一出口。
	 *
	 * <p><b>Boss 不削弱</b>（已定稿）：Boss 保持原数值，否则会软得离谱。
	 * 判据用 {@code Char.Property.BOSS}/{@code MINIBOSS}。
	 *
	 * @param m 刚创建的怪物
	 */
	public static float mobStatMultiplier(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m) {
		if (!on(MONSTER_WAVE)) return 1f;
		if (m == null) return 1f;

		//Boss / 小 Boss 不削弱
		//用 Char.hasProp 而不是 m.properties —— 后者是 protected，包外访问不到。
		if (Char.hasProp(m, Char.Property.BOSS)
				|| Char.hasProp(m, Char.Property.MINIBOSS)) {
			return 1f;
		}
		return WAVE_STAT_MULT;
	}

	/**
	 * END(47 稀缺补给 + 48 过量补给): 消耗品生成数量倍率。
	 *
	 * <p>两条**互斥**（框架已强制置灰），所以实际不会同时生效；
	 * 这里仍写成累乘以防数据出错时行为可预期。
	 *
	 * @return 倍数；无相关挑战时为 1f
	 */
	public static float consumableCountMultiplier() {
		float mult = 1f;
		if (on(SCARCE_SUPPLIES)) mult *= SCARCE_ITEM_MULT;
		if (on(EXCESS_SUPPLIES)) mult *= EXCESS_ITEM_MULT;
		return mult;
	}

	/**
	 * END(35 丰饶 + 36 贫瘠): 掉落数量倍率。
	 *
	 * <p>两条**互斥**（框架已强制置灰）。
	 * <p>贫瘠的下限保障：即使叠加也不会降到 0（见 {@link #scaleCount}）。
	 */
	public static float dropCountMultiplier() {
		float mult = 1f;
		if (on(ABUNDANCE)) mult *= ABUNDANCE_MULT;
		if (on(BARREN))    mult *= BARREN_MULT;
		return mult;
	}

	/**
	 * END(62 芙莉莲): 宝箱生成概率 / 数量倍率。
	 */
	public static float chestCountMultiplier() {
		return on(FRIEREN) ? FRIEREN_MULT : 1f;
	}

	/**
	 * END(64 宝物猎人): 普通怪物掉落倍率（降低）。
	 */
	public static float monsterDropMultiplier() {
		return on(TREASURE_HUNTER) ? HUNTER_DROP_MULT : 1f;
	}

	/**
	 * END(64 宝物猎人): 宝箱内容额外物品数。
	 */
	public static int chestContentBonus() {
		return on(TREASURE_HUNTER) ? HUNTER_CHEST_BONUS : 0;
	}

	/**
	 * END(61 炸弹狂魔): 每层额外掉落几个炸弹。
	 */
	public static int bonusBombsPerLevel() {
		return on(BOMB_FANATIC) ? 1 : 0;
	}

	/**
	 * END(通用): 把一个"数量"按倍率缩放。
	 *
	 * <p>三条保护：
	 * <ol>
	 *   <li>结果**至少为 1**（除非原值为 0）—— 避免"贫瘠 + 稀缺"把资源压到 0
	 *       导致无法通关。原表对 36 贫瘠的要求就是"不能降到 0"。</li>
	 *   <li>原值为 0 时保持 0 —— 不该把"本来就没有"变成 1。</li>
	 *   <li>向上取整用 {@code ceil}，保证加成方向对玩家可见。</li>
	 * </ol>
	 *
	 * @param original 原数量
	 * @param mult     倍率
	 * @param roundUp  true = 向上取整（增益类），false = 向下取整（惩罚类）
	 */
	public static int scaleCount(int original, float mult, boolean roundUp) {
		if (original <= 0) return 0;
		if (mult == 1f) return original;

		float scaled = original * mult;
		int result = roundUp ? (int) Math.ceil(scaled) : (int) Math.floor(scaled);

		//永远不能把原本存在的资源压到 0
		return Math.max(1, result);
	}

	//==== 掉落类：按"物品类型"决定倍率 ====

	/** 该物品是否属于"消耗品"（药水 / 卷轴 / 食物等一次性资源）。 */
	public static boolean isConsumable(com.shatteredpixel.shatteredpixeldungeon.items.Item item) {
		return item != null && isConsumableClass(item.getClass());
	}

	/**
	 * END(挑战): 按**类型**判断是否消耗品（不依赖实例）。
	 *
	 * <p>与 {@link #isConsumable} 逻辑相同，但可用于不需要构造实例的场合
	 * （构造 Item 会触发 ItemSpriteSheet 的纹理加载，需要图形环境）。
	 */
	public static boolean isConsumableClass(Class<?> cls) {
		if (cls == null) return false;
		return com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.class.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll.class.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb.class.isAssignableFrom(cls);
	}

	/**
	 * END(挑战 35/36/47/48): **进度关键物品**——不受掉落增减影响。
	 *
	 * <p>为什么必须有这份豁免：{@code RegularLevel} 里力量药水、升级卷轴、
	 * 各种强化道具都是走 {@code drop() } 落地的（见 RegularLevel:460-477），
	 * 而 35/36 的全局过滤正好拦在 {@code drop()} 入口。
	 * 若不豁免，「贫瘠」会**概率性丢掉升级卷轴与力量药水** ——
	 * 这会直接破坏通关可行性，远超"资源压力"的设计意图。
	 *
	 * <p>豁免清单（都是每局数量受限、决定成长曲线的物品）：
	 * <ul>
	 *   <li>力量药水 —— 直接决定能否穿装</li>
	 *   <li>升级卷轴 —— 核心成长资源</li>
	 *   <li>金钥匙 —— 宝箱钥匙，丢了箱子就开不了</li>
	 *   <li>奥术刻笔 / 附魔石 / 直觉石 / 饰品位面催化剂 —— 都被
	 *       {@code Dungeon.LimitedDrops} 限数</li>
	 * </ul>
	 *
	 * <p>注意：**食物不在豁免内**（食物本就是消耗品，受补给规则影响是合理的），
	 * 但 47/48 已单独覆盖它。
	 */
	public static boolean isProgressCritical(Class<?> cls) {
		if (cls == null) return false;

		return com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class
					.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class
					.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey.class
					.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.Stylus.class
					.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment.class
					.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition.class
					.isAssignableFrom(cls)
				|| com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst.class
					.isAssignableFrom(cls);
	}

	/**
	 * END(挑战 47/48/35/36): 该物品的**掉落倍率**（消耗品与全局掉落叠加）。
	 *
	 * <p>关键两点：
	 * <ol>
	 *   <li><b>35/36 是全局掉落规则，必须叠加到消耗品上</b>，
	 *       而不是"消耗品只看 47/48"。否则勾了 36 贫瘠时，
	 *       药水卷轴完全不受影响（曾经就是这个问题）。</li>
	 *   <li><b>进度关键物品直接返回 1f</b> —— 见 {@link #isProgressCritical}。</li>
	 * </ol>
	 *
	 * <p>例：36 贫瘠 + 47 稀缺补给 → 消耗品倍率 0.75 × 0.6 = 0.45。
	 */
	private static float dropMultiplierFor(Class<?> cls) {
		if (isProgressCritical(cls)) return 1f;

		float mult = dropCountMultiplier();
		if (isConsumableClass(cls)) {
			mult *= consumableCountMultiplier();
		}
		return mult;
	}

	/**
	 * END(挑战 47/48/35/36): 该物品的**掉落保留概率**。
	 *
	 * <p>为什么用概率而不是数量：{@code Level.drop()} 每次只处理**一个**物品，
	 * 没有"数量"可供相乘。把倍率 &gt; 1 实现为"再掉一份"会递归调用 drop、
	 * 有重复触发与死循环风险。所以统一用概率表达：
	 * <ul>
	 *   <li>倍率 &lt; 1 → 按该概率**保留**（其余丢弃），等效于减少掉落</li>
	 *   <li>倍率 &gt; 1 → 保留概率 100%，额外的量由 {@link #extraDropCopy} 处理</li>
	 * </ul>
	 *
	 * @param item 待掉落物品
	 * @return 0~1 的保留概率
	 */
	public static float dropKeepChance(com.shatteredpixel.shatteredpixeldungeon.items.Item item) {
		if (item == null) return 1f;
		return dropKeepChanceFor(item.getClass());
	}

	/** END(挑战): 按类型计算保留概率（不依赖实例，供测试与无实例场合使用）。 */
	public static float dropKeepChanceFor(Class<?> cls) {
		float mult = dropMultiplierFor(cls);

		//只处理"减少"方向
		if (mult >= 1f) return 1f;
		return mult;
	}

	/**
	 * END(挑战 48/35): 该物品是否应**额外多掉一份**。
	 *
	 * <p>倍率 &gt; 1 的部分用"按小数部分追加一次"表达：
	 * 1.5 → 总是保留 + 50% 概率追加一份；1.25 → 25% 概率追加。
	 * 这样长期期望掉落量正好等于倍率，且不会递归调用 drop。
	 */
	public static boolean extraDropCopy(com.shatteredpixel.shatteredpixeldungeon.items.Item item) {
		if (item == null) return false;
		return extraDropCopyFor(item.getClass());
	}

	/** END(挑战): 按类型判断是否追加掉落（不依赖实例）。 */
	public static boolean extraDropCopyFor(Class<?> cls) {
		float mult = dropMultiplierFor(cls);
		if (mult <= 1f) return false;

		float frac = mult - (float) Math.floor(mult);
		if (frac <= 0f) return true;    //整数倍：每次都追加
		return com.watabou.utils.Random.Float() < frac;
	}

	//==================================================================
	//第三批：商店 / 经济（34 / 37 / 43 / 44）
	//==================================================================

	/**
	 * END(37 高价回收): 商店**回收**玩家物品时的价格倍率。
	 *
	 * <p>调用点：{@code WndTradeItem.shopPaysFor()}。
	 *
	 * <p>注意区分两个方向（原版命名容易混）：
	 * <ul>
	 *   <li>{@code Shopkeeper.sellPrice()} = 商店**卖给玩家**的售价（很贵）</li>
	 *   <li>{@code WndTradeItem.shopPaysFor()} = 商店**回收**玩家物品给的价</li>
	 * </ul>
	 * 37 高价回收改的是**后者**。与 32 通货膨胀互斥（框架已置灰），
	 * 所以不会出现 1.5 × 0.5 同时生效的情况。
	 */
	public static float buybackMultiplier() {
		return on(HIGH_BUYBACK) ? BUYBACK_MULT : 1f;
	}

	/**
	 * END(44 慷慨商人): 商店**售价**倍率（玩家买东西更贵）。
	 *
	 * <p>调用点：{@code Shopkeeper.sellPrice()}。
	 * 与 32 通货膨胀**可共存**（原表注明"效果重叠，可共存"）：
	 * 两者叠加时价格为 1.5 × 1.25 = 1.875 倍 —— 这是**预期行为**。
	 */
	public static float merchantPriceMultiplier() {
		return on(GENEROUS_MERCHANT) ? MERCHANT_PRICE_MULT : 1f;
	}

	/**
	 * END(44 慷慨商人): 商店商品数量倍率。
	 *
	 * <p>调用点：{@code ShopRoom.generateItems()}。
	 */
	public static float merchantItemCountMultiplier() {
		return on(GENEROUS_MERCHANT) ? MERCHANT_COUNT_MULT : 1f;
	}

	/**
	 * END(43 一贫如洗): 进入新区域时的金币倍率（0.8 = 扣 20%）。
	 *
	 * <p>调用点：{@code Dungeon.newLevel()}，仅在"跨区域"时调用一次。
	 * 区域判据沿用原版的 {@code depth % 5}（每 5 层一组）；
	 * 开了「完整地牢」（每区 9-10 层）时仍是每 5 层触发，
	 * 这一点在原表备注里已注明。
	 *
	 * @return 金币应乘的倍率；无该挑战时为 1f
	 */
	public static float goldOnNewRegionMultiplier() {
		return on(DESTITUTE) ? DESTITUTE_MULT : 1f;
	}

	/**
	 * END(34 赏金制度): 击杀该怪物应额外获得多少金币。
	 *
	 * <p>只对**精英怪**与**Boss / 小 Boss**生效，普通怪返回 0。
	 * <p>金币由调用方（{@code Mob.die()}）通过 {@code Dungeon.level.drop(new Gold(n), pos)} 落地，
	 * 这样会正常走掉落的堆叠与拾取流程。
	 *
	 * @param m 被击杀的怪物
	 * @return 额外金币数（未勾选或非精英/Boss 时为 0）
	 */
	public static int bountyGoldFor(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m) {
		if (!on(BOUNTY) || m == null) return 0;

		if (Char.hasProp(m, Char.Property.BOSS) || Char.hasProp(m, Char.Property.MINIBOSS)) {
			return BOUNTY_GOLD_BOSS;
		}
		//必须用 buffs()（复数）而不是 buff()：
		//Char.buff(Class) 内部是 b.getClass() == c 的**精确匹配**，
		//而精英是 Blazing/Projecting 等子类，用 buff(ChampionEnemy.class) 永远查不到。
		if (!m.buffs(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy.class)
				.isEmpty()) {
			return BOUNTY_GOLD_ELITE;
		}
		return 0;
	}

	//==================================================================
	//精英体系的重新设计（本 fork 对「116 精英强敌」的改造）
	//==================================================================
	//
	//原版：勾选 116 → 精英怪按 1/8~1/6 概率生成
	//本 fork：116 的效果改为「13% 生成**稀有怪**」；
	//        精英怪改由「精英类规则」自动开启（不需要 116）。
	//
	//理由：精英怪有三种来源，只有一种是 116 控制的
	//  (a) rollForChampion()        —— 原受 116 控制
	//  (b) ShubNiggurath 硬编码      —— 从不看 116（Hollow Boss 召唤的分身）
	//  (c) Albino 等稀有怪替代       —— 与精英无关，是另一套机制
	//把 116 与精英解绑后，14/34/64 这类规则在任何精英来源下都能正确工作。

	/** END(75 精英地牢·改): 稀有怪出现概率（13%）。 */
	public static final float RARE_ALT_CHANCE = 0.13f;

	/**
	 * END(75 精英地牢·改): 是否启用"13% 稀有怪"。
	 *
	 * <p>读取**表 ID 75**（精英地牢）。原表 75 写的是"13% 替换为精英怪"，
	 * 本 fork 改为"13% 替换为稀有怪"——精英已由精英类规则负责。
	 */
	public static boolean rareAltChanceEnabled() {
		return on(ELITE_DUNGEON_ID);
	}

	//==== 精英类规则 ====

	/** 14 精英强化。 */
	public static final int ELITE_BOOST_ID   = 14;
	/** 75 精英地牢（未实装，已预留）。 */
	public static final int ELITE_DUNGEON_ID = 75;
	/** 4 精英迁徙（未实装，已预留）。 */
	public static final int ELITE_MIGRATION_ID = 4;

	/**
	 * END(精英体系): 是否有任一「精英类规则」被勾选。
	 *
	 * <p>精英怪**不需要** 116 就会生成 —— 只要勾了以下任意一条：
	 * <ul>
	 *   <li>14 精英强化 —— 决定精英多强</li>
	 *   <li>34 赏金制度 —— 击杀精英给赏金</li>
	 *   <li>64 宝物猎人 —— 精英不受掉落削减</li>
	 *   <li>75 精英地牢 / 4 精英迁徙 —— 精英的来源（未实装，先纳入）</li>
	 * </ul>
	 *
	 * <p>为什么这样做：精英怪有多种来源（刷怪、Boss 召唤、硬编码），
	 * 只有刷怪那一种原本受 116 控制。把"要不要精英"交给精英类规则本身，
	 * 可以避免"勾了 14 却因为没勾 116 而完全无效"的死组合。
	 */
	public static boolean eliteChallengesEnabled() {
		return on(ELITE_BOOST_ID)
				|| on(BOUNTY)
				|| on(TREASURE_HUNTER)
				|| on(ELITE_DUNGEON_ID)
				|| on(ELITE_MIGRATION_ID);
	}

	/**
	 * END(精英体系): 精英怪生成时，应使用的"距离下一只精英"的基准间隔。
	 *
	 * <p>原版用 {@code 8 - depth/10}（约 1/8 ~ 1/6）。
	 * 本 fork 的设计下，精英由精英类规则开启，间隔沿用原版数值，
	 * 保证手感与"精英强敌"时代一致。
	 *
	 * @return 是否需要生成精英；false 表示完全关闭
	 */
	public static boolean shouldRollChampion() {
		//116 已改为稀有怪，不再参与精英生成。
		//精英是否出现完全由精英类规则决定。
		return eliteChallengesEnabled();
	}

	//==================================================================
	//第五批：战斗触发类（13 / 16 / 19 / 23 / 24 / 28 / 78 / 80 / 90 / 123）
	//==================================================================

	/** 13 狂热：怪物攻击后攻速 +13%，封顶 3 层。 */
	public static final int FRENZY          = 13;
	/** 16 大力水手：物理攻击 +25%，攻速 −20%。 */
	public static final int POPEYE          = 16;
	/** 19 风驰电掣：玩家攻速 +20%，怪物移速 +20%。 */
	public static final int SWIFT           = 19;
	/** 23 血流成河：攻击 13% 概率使目标流血 1 回合。 */
	public static final int BLOODBATH       = 23;
	/** 24 以牙还牙：玩家攻击后怪物 13% 概率立即反击。 */
	public static final int RETALIATION     = 24;
	/** 28 不动如山：怪物受击 13% 概率完全免疫。 */
	public static final int IMMOVABLE       = 28;
	/** 78 烈火焚身：玩家受击 13% 概率燃烧。 */
	public static final int IMMOLATION      = 78;
	/** 80 冰天雪地：玩家每回合 13% 寒冷、2% 冰冻。 */
	public static final int FROZEN_WORLD    = 80;
	/** 90 雷暴：每回合 5% 闪电随机劈中角色。 */
	public static final int THUNDERSTORM    = 90;
	/** 123 大学生：玩家每回合 3% 受 1 点伤害（不致死）。 */
	public static final int COLLEGE_STUDENT = 123;

	//==== 概率与数值 ====

	/** 23 血流成河 / 24 以牙还牙 / 28 不动如山 / 78 烈火焚身：13%。 */
	private static final int PCT_13 = 13;
	/** 13 狂热：每次 +13%，封顶 3 层。 */
	private static final float FRENZY_STEP      = 0.13f;
	private static final int   FRENZY_MAX_STACK = 3;
	/** 16 大力水手：物理 +25%、攻速 −20%。 */
	private static final float POPEYE_DMG_MULT   = 1.25f;
	private static final float POPEYE_SPEED_MULT = 0.80f;
	/** 19 风驰电掣：玩家攻速 +20%、怪物移速 +20%。 */
	private static final float SWIFT_SPEED_MULT  = 1.20f;
	/** 80 冰天雪地：13% 寒冷、2% 冰冻。 */
	private static final int   FROZEN_CHILL_PCT  = 13;
	private static final int   FROZEN_FREEZE_PCT = 2;
	/** 90 雷暴：每回合 5% 概率触发。 */
	private static final int   THUNDER_PCT       = 5;
	/** 123 大学生：每回合 3% 受 1 点伤害。 */
	private static final int   STUDENT_PCT       = 3;

	/**
	 * END(13 狂热): 怪物攻击命中后的攻速加成倍率。
	 *
	 * <p>每次成功攻击 +13%，最多 3 层（×1.44），持续 5 回合。
	 * 用 {@code ChallengeFrenzyMark} 记录层数（纯标记 buff，不显示图标）。
	 */
	public static float frenzySpeedMultiplier(Char ch) {
		if (!on(FRENZY) || ch == null) return 1f;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeFrenzyMark mark =
				ch.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChallengeFrenzyMark.class);
		if (mark == null) return 1f;

		int stacks = Math.min(FRENZY_MAX_STACK, mark.stacks);
		if (stacks <= 0) return 1f;

		return 1f + stacks * FRENZY_STEP;
	}

	/** END(13 狂热): 怪物攻击命中后累加一层（由 {@code Char.attack()} 调用）。 */
	public static void onMobAttackHit(Char attacker) {
		if (!on(FRENZY) || attacker == null) return;
		if (attacker instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) return;

		//ChallengeFrenzyMark 自己管倒计时（见该类注释），用 addStack 刷新+累加。
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeFrenzyMark mark =
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						attacker,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.ChallengeFrenzyMark.class);
		if (mark != null) {
			mark.addStack(FRENZY_MAX_STACK);
		}
	}

	/**
	 * END(23 血流成河): 攻击命中后 13% 概率给目标挂流血 1 回合。
	 * <p>**任何攻击**（玩家与怪物）都触发。已有流血则不重复挂。
	 */
	public static void onAttackHitBleed(Char enemy) {
		if (!on(BLOODBATH) || enemy == null) return;
		if (enemy.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding.class) != null) {
			return;
		}
		if (Random.Int(100) >= PCT_13) return;

		//Bleeding 不是 FlavourBuff，没有带 duration 的 affect 重载：
		//它是"每次结算扣固定血"的持续 debuff，用 .set(每跳伤害) 设置强度，
		//持续回合由 Bleeding 自身逻辑决定（原表说"持续 1 回合"，
		//这里给一个很小的强度值，让它在一回合内结算完即消失）。
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
				.affect(enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding.class)
				.set(1f);
	}

	/**
	 * END(24 以牙还牙): 玩家攻击结算后，怪物 13% 概率立即反击。
	 *
	 * <p>反击**不消耗**怪物回合；每回合每怪最多一次（由 {@code ChallengeRetaliateMark} 限制）。
	 */
	public static void onHeroAttack(Char attacker, Char enemy) {
		if (!on(RETALIATION)) return;
		if (attacker == null || enemy == null) return;
		if (!(attacker instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)) return;
		if (!(enemy instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob)) return;
		if (!enemy.isAlive() || !attacker.isAlive()) return;

		//每回合每怪最多反击一次
		if (enemy.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
				.ChallengeRetaliateMark.class) != null) {
			return;
		}
		if (Random.Int(100) >= PCT_13) return;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
				enemy,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChallengeRetaliateMark.class,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChallengeRetaliateMark.DURATION);

		//立即反击（不消耗回合）
		enemy.attack(attacker);
	}

	/**
	 * END(28 不动如山): 怪物受击 13% 概率完全免疫。
	 *
	 * <p>调用点：{@code Mob.damage()} 入口。与 22 物极必反独立判定。
	 *
	 * @return true 表示本次伤害应被完全免疫
	 */
	public static boolean immovableBlocks(Char ch) {
		if (!on(IMMOVABLE) || ch == null) return false;
		if (ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) return false;
		return Random.Int(100) < PCT_13;
	}

	/**
	 * END(78 烈火焚身): 玩家受击 13% 概率燃烧。
	 * <p>已有燃烧则不重复挂。
	 */
	public static void onHeroDamagedBurn(Char ch) {
		if (!on(IMMOLATION) || ch == null) return;
		if (!(ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)) return;
		if (ch.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class) != null) {
			return;
		}
		if (Random.Int(100) >= PCT_13) return;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
				ch, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class);
	}

	/**
	 * END(80 冰天雪地 + 90 雷暴 + 123 大学生): 玩家每回合的环境类判定。
	 *
	 * <p>调用点：{@code Hero.act()}（与音频类同处）。
	 * 三条独立判定，互不影响。
	 */
	public static void onHeroTurnEnvironment(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
		if (hero == null || !hero.isAlive()) return;

		//---- 80 冰天雪地：13% 寒冷、2% 冰冻（只对玩家生效，已定稿）----
		if (on(FROZEN_WORLD)) {
			if (Random.Int(100) < FROZEN_FREEZE_PCT) {
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
						hero,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost.class,
						2f);
			} else if (Random.Int(100) < FROZEN_CHILL_PCT) {
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
						hero,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill.class,
						3f);
			}
		}

		//---- 90 雷暴：5% 闪电随机劈中一个角色（玩家或怪物）----
		if (on(THUNDERSTORM) && Random.Int(100) < THUNDER_PCT) {
			strikeLightning(hero);
		}

		//---- 123 大学生：3% 受 1 点伤害（**不致死**，已定稿）----
		if (on(COLLEGE_STUDENT) && Random.Int(100) < STUDENT_PCT) {
			//HP <= 1 时本次伤害不生效，避免 3% 概率暴毙
			if (hero.HP > 1) {
				hero.damage(1, hero);
			}
		}
	}

	/** END(90 雷暴): 闪电劈中随机角色，伤害 = 3 × 层数 ÷ 5（已定稿）。 */
	private static void strikeLightning(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {

		int dmg = 3 * Dungeon.depth / 5;    //第1层=0、第5层=3、第25层=15

		//收集场上所有角色（玩家 + 存活的怪物）
		java.util.ArrayList<Char> targets = new java.util.ArrayList<>();
		targets.add(hero);
		if (Dungeon.level != null) {
			for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m
					: Dungeon.level.mobs.toArray(
							new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])) {
				if (m.isAlive()) targets.add(m);
			}
		}
		if (targets.isEmpty()) return;

		Char victim = targets.get(Random.Int(targets.size()));

		//视觉与音效（沿用雷击类效果的既有资源）
		if (victim.sprite != null) {
			victim.sprite.flash();
		}

		//伤害（第 1~4 层可能为 0，这是整数截断的预期结果，见功能大全 ID 90）
		victim.damage(dmg, hero);

		//可能点燃
		if (victim.isAlive() && Random.Int(100) < 50) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					victim, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class);
		}
	}

	/**
	 * END(16 大力水手): 玩家物理伤害倍率。
	 *
	 * <p>只在**物理**攻击时生效（法杖/投掷物不算）。
	 * 由 {@code Char.attack()} 判断当前武器类型后调用。
	 */
	public static float popeyeDamageMultiplier(boolean isPhysical) {
		if (!on(POPEYE) || !isPhysical) return 1f;
		return POPEYE_DMG_MULT;
	}

	/** END(16 大力水手): 玩家攻速倍率（−20%）。 */
	public static float popeyeSpeedMultiplier(Char ch) {
		if (!on(POPEYE) || ch == null) return 1f;
		if (!(ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)) return 1f;
		return POPEYE_SPEED_MULT;
	}

	/**
	 * END(19 风驰电掣): 速度倍率 —— 玩家 +20%、怪物 +20%。
	 *
	 * <p>与 16 大力水手可叠加（若同时勾选，玩家为 1.2 × 0.8 = 0.96）。
	 */
	public static float swiftSpeedMultiplier(Char ch) {
		if (!on(SWIFT) || ch == null) return 1f;
		return SWIFT_SPEED_MULT;
	}
}
