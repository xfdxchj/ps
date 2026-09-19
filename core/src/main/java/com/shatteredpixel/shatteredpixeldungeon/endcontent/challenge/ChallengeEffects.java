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

	//==== 第 6 步：最终拦截 ====

	/**
	 * END(第6步·最终拦截): 伤害生效前的最后判定。
	 *
	 * <p>顺序：**先 22 物极必反（免疫）→ 再 69 九九归一（变为 1）**。
	 * 被完全免疫的伤害不该再走 69 的改写。
	 *
	 * <ul>
	 *   <li><b>22 物极必反</b>：怪物单次受伤害超过其最大生命 **150%** 时，
	 *       该次伤害**完全免疫**。只对怪物生效（原表写的是"怪物单次受伤害"）。</li>
	 *   <li><b>69 九九归一</b>：最终伤害是 **9 的倍数**时变为 **1** 点。
	 *       不分敌我（倾向为"双刃剑"）。</li>
	 * </ul>
	 *
	 * <p><b>返回值语义</b>：
	 * <ul>
	 *   <li>{@link #IMMUNE}（-1）= 被 22 完全免疫 —— 调用方应显示"免疫"并跳过扣血</li>
	 *   <li>其它值 = 正常伤害（可能本来就是 0，那是"没打穿护甲"，不是免疫）</li>
	 * </ul>
	 *
	 * <p>⚠️ 调用方**不能**用 {@code dmg <= 0} 判断"被免疫"：
	 * 护甲完全吸收时伤害本来就是 0，那样会误报"无敌"。
	 * 这正是本类早期版本出现过的问题。
	 *
	 * @return 拦截后的伤害；{@link #IMMUNE} 表示完全免疫
	 */
	public static final int IMMUNE = -1;

	public static int finalIntercept(Char target, int dmg) {
		if (target == null) return dmg;

		//---- 22 物极必反：超 150% 最大生命 → 完全免疫 ----
		if (on(OVERKILL_REVERSE) && target.HT > 0 && dmg > 0) {
			//只对怪物生效（Hero 不受此条保护）
			if (!(target instanceof Hero)) {
				if (dmg * 100 > target.HT * 150) {
					return IMMUNE;
				}
			}
		}

		//---- 69 九九归一：9 的倍数 → 1 ----
		//注意只对正伤害生效：0 不是"9 的倍数"意义上的伤害，
		//否则会把"没打穿护甲"变成 1 点伤害。
		if (on(NINE_TO_ONE) && dmg > 0 && dmg % 9 == 0) {
			return 1;
		}

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
		//121 中世纪骑士：玩家移速 −50%
		mult *= knightSpeedMultiplier(ch);

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
	/** 10 巨型化：13% 怪物获得更高生命值与更大体型。 */
	public static final int GIANT             = 10;
	/** 54 我爱花花：草 13% 替换成随机花。 */
	public static final int FLOWER_LOVER      = 54;
	/** END(诊断): 138 贴图诊断开关。定稿后改回 false。 */
	public static final boolean ABSURD_SPRITE_DEBUG = true;

	/** 138 荒诞世界：怪物贴图随机变化。 */
	public static final int ABSURD_WORLD      = 138;
	/** 145 神圣附体：经验获取 +20%。 */
	public static final int HOLY_POSSESSION   = 145;
	/** 146 醍醐灌顶：每层获得 1 点额外天赋点。 */
	public static final int ENLIGHTENMENT    = 146;
	/** 147 就业紧张：职业天赋全部失效。 */
	public static final int JOB_CRISIS       = 147;
	/** 155 家传戒指：开局获得神射戒指。 */
	public static final int HEIRLOOM_RING    = 155;
	/** 156 家传铠甲：开局获得板甲。 */
	public static final int HEIRLOOM_ARMOR   = 156;
	/** 165 神圣之光：13% 概率回复 2% 生命。 */
	public static final int HOLY_LIGHT       = 165;
	/** 60 家传法杖：开局随机获得一支进阶法杖。 */
	public static final int HEIRLOOM_WAND    = 60;
	/** 79 高级附魔台：每个区域获得 1 个附魔秘卷。 */
	public static final int ADVANCED_ENCHANT = 79;
	/** 17 情人节：攻击 13% 概率魅惑目标。 */
	public static final int VALENTINE        = 17;
	/** 57 残缺装备：13% 概率获得「残缺」附魔（攻击 −20%）。 */
	public static final int BROKEN_EQUIPMENT = 57;
	/** 59 诅咒装备：获得诅咒的概率 +13%。 */
	public static final int CURSED_EQUIPMENT = 59;
	/** 7 跳级生：可直接跳级（以物品形式实现）。 */
	public static final int SKIP_STUDENT     = 7;
	/** 58 随机附魔：装备获得时 50% 概率随机附魔。 */
	public static final int RANDOM_ENCHANT   = 58;
	/** 55 不稳定强化：强化时 13% 额外 +2。 */
	public static final int UNSTABLE_UPGRADE = 55;
	/** 71 喝大了：每回合 3% 眩晕 3 回合。 */
	public static final int DRUNK             = 71;
	/** 46 药剂不稳定：使用药水后 13% 产生随机效果。 */
	public static final int UNSTABLE_POTION   = 46;
	/** 52 陷阱泛滥：地图陷阱数量增加。 */
	public static final int TRAP_OVERFLOW     = 52;
	/** 49 切尔诺贝利：全图毒气。 */
	public static final int CHERNOBYL         = 49;
	/** 74 热带雨林：水中 13% 生成食人鱼。 */
	public static final int RAINFOREST        = 74;
	/** 141 禁魔空间：所有魔法伤害 −20%。 */
	public static final int ANTI_MAGIC_ZONE   = 141;
	/** 149 黏糊蜂蜜：每层刷新 2 只蜜蜂。 */
	public static final int STICKY_HONEY      = 149;
	/** 162 真实地牢：空气稀薄，需要定期停下深呼吸。 */
	public static final int REALISTIC_DUNGEON = 162;
	/** 159 绵羊地牢：玩家周围周期性生成绵羊。 */
	public static final int SHEEP_DUNGEON     = 159;
	/** 65 及时雨：第一次致命伤害不死，保留 1 点生命。 */
	public static final int TIMELY_RAIN       = 65;
	/** 104 命悬一线：致命伤 13% 保 1 点。 */
	public static final int CLOSE_CALL        = 104;
	/** 40 贷款：可贷款金币。 */
	public static final int LOAN              = 40;
	/** 81 搏杀赌徒：改变升级系统。 */
	public static final int GAMBLER           = 81;
	/** 2 楼层混乱：普通楼层随机重排。 */
	public static final int FLOOR_SHUFFLE     = 2;

	//==================================================================
	//本批（扩展包）新增规则
	//==================================================================

	/** 87 盗贼鼠群：怪物攻击 5% 偷金币，击杀后双倍返还。 */
	public static final int THIEF_RATS        = 87;
	/** 142 无下限术士：怪物受远程攻击 13% 完全免疫。 */
	public static final int NO_LOWER_LIMIT    = 142;
	/** 143 吾为王者：所有 Boss 命中/闪避 +20%。 */
	public static final int I_AM_KING         = 143;
	/** 148 飞天神偷：怪物 13% 获得隐身。 */
	public static final int FLYING_THIEF      = 148;
	/** 153 恶魔地牢：所有怪物变为恶魔类。 */
	public static final int DEMON_DUNGEON     = 153;
	/** 158 神圣之力：对恶魔类造成额外伤害。 */
	public static final int HOLY_POWER        = 158;
	/** 163 古代升级：每 3 级 +10% 固定伤害。 */
	public static final int ANCIENT_UPGRADE   = 163;
	/** 168 怪物地牢：所有怪物相关概率提升至 25%。 */
	public static final int MONSTER_DUNGEON   = 168;

	/** 87 盗贼鼠群：偷钱概率 5%，返还倍数 2。 */
	private static final int THIEF_RATS_PCT     = 5;
	private static final int THIEF_RATS_REFUND  = 2;
	/** 142 无下限术士：免疫概率 13%。 */
	private static final int NO_LOWER_LIMIT_PCT = 13;
	/** 143 吾为王者：Boss 命中/闪避倍率 1.2。 */
	private static final float KING_STAT_MULT   = 1.20f;
	/** 148 飞天神偷：隐身概率 13%。 */
	private static final int FLYING_THIEF_PCT   = 13;
	/** 158 神圣之力：对恶魔额外伤害倍率 1.3（+30%）。 */
	private static final float HOLY_POWER_MULT  = 1.30f;
	/** 163 古代升级：每 3 级 +10%。 */
	private static final int ANCIENT_STEP_LEVEL = 3;
	private static final float ANCIENT_PER_STEP = 0.10f;
	/** 168 怪物地牢：怪物相关概率统一提升到 25%。 */
	private static final int MONSTER_DUNGEON_PCT = 25;

	//==================================================================
	//2 楼层混乱
	//==================================================================

	/**
	 * END(2 楼层混乱): 本局"游玩顺序 -> 楼层编号"的映射。
	 *
	 * <h3>设计</h3>
	 * 原表："普通楼层随机重排，Boss 层固定；楼层编号与游玩顺序分离"。
	 *
	 * <p>约束（来自文档所有者）：
	 * <ol>
	 *   <li>1-25 层构成一个**随机排列**，每层只去一次，共 25 步。</li>
	 *   <li><b>第 5/10/15/20/25 步必须是 Boss 层</b>（5/10/15/20/25），
	 *       即游玩节奏与原版一致，只是中间普通层的编号被打乱。</li>
	 *   <li>每层的"下一层"必须**固定** —— 否则玩家存档后重进会走到别的层。</li>
	 * </ol>
	 *
	 * <p>由约束 1+2 可推出：只能在**每个区域内部**打乱那 4 个普通层。
	 * 例如第 1 区是 {1,2,3,4} 的某个排列，第 5 步固定为 5；
	 * 第 2 区是 {6,7,8,9} 的某个排列，第 10 步固定为 10；以此类推。
	 *
	 * <h3>为什么用固定种子</h3>
	 * 用常量种子（而非每次调用重新掷骰）保证：
	 * 同一个存档里"从 3F 下楼"永远到同一层，读档/回退都不会错位。
	 *
	 * <p>数组含义：{@code ORDER[step] = depth}，step 为 0..24（对应第 1..25 步）。
	 */
	private static int[] shuffleOrder = null;

	/** 排列用的固定种子。 */
	private static final long SHUFFLE_SEED = 0x5EEDF100L;

	/**
	 * 构建本局的楼层排列。种子固定 → 结果固定。
	 *
	 * <p>END(适配 6 完整地牢): 排列长度与 Boss 间隔**不再是写死的 25/5**，
	 * 而是取自 {@link #maxMainDepth()} 与 {@link #bossInterval()}：
	 * <ul>
	 *   <li>未勾选 6：25 层，每 5 层一个 Boss（原版节奏）</li>
	 *   <li>勾选 6：50 层，每 10 层一个 Boss</li>
	 * </ul>
	 * 这样 2 与 6 可以同时勾选而不会互相失效。
	 */
	private static void buildShuffleOrder() {
		com.watabou.utils.Random.pushGenerator(SHUFFLE_SEED);

		int total = maxMainDepth();          //25 或 50
		int interval = bossInterval();       //5 或 10

		//==== END(修订): 普通层**全局**打乱，不再限制在区域内 ====
		//文档所有者给出的例子是
		//   1 → 4 → 21 → 12 → 5 → 9 → 18 → 2 → 6 → 10
		//其中第 5 步是 5F、第 10 步是 10F，**Boss 固定在原步数**，
		//而中间的普通层来自各个区域（4、21、12 分属不同区）。
		//
		//做法：把 20 个普通层**整体打乱**，然后每 4 个插入一个 Boss：
		//   位置 1-4   普通层（随机）
		//   位置 5     Boss 5F
		//   位置 6-9   普通层（随机）
		//   位置 10    Boss 10F
		//   ... 以此类推
		//
		//==== END(再修订): 第 1 步**固定为 1F** ====
		//文档所有者要求"让第一层必定为一"。
		//因此把 1F 从打乱池里拿出来，钉在首位；其余 19 个普通层照常打乱。
		//这样既保证开局在下水道（不会一上来就跳到某个高层），
		//又不破坏"每层只去一次"与"Boss 步数固定"。

		java.util.ArrayList<Integer> normals = new java.util.ArrayList<>();
		for (int d = 2; d <= total; d++) {           //从 2 开始：1F 被固定
			if (d % interval != 0) normals.add(d);   //排除 Boss 层
		}
		com.watabou.utils.Random.shuffle(normals);

		shuffleOrder = new int[total];
		shuffleOrder[0] = 1;                         //第 1 步固定 1F
		int n = 0;
		for (int step = 1; step < total; step++) {
			if ((step + 1) % interval == 0) {
				//Boss 步：层号与步号相同
				shuffleOrder[step] = step + 1;
			} else {
				shuffleOrder[step] = normals.get(n++);
			}
		}

		com.watabou.utils.Random.popGenerator();
	}

	/**
	 * END(2 楼层混乱): 给定"当前楼层"，返回它的下一层。
	 *
	 * <p>先查出当前楼层在游玩顺序中的位置，再取下一个位置对应的楼层。
	 * 若当前楼层不在排列里（挑战区 26F+ 等），原样返回 {@code depth + 1}。
	 */
	public static int nextShuffledDepth(int depth) {
		if (!on(FLOOR_SHUFFLE)) return depth + 1;
		//END(适配 6 完整地牢): 上限用 maxMainDepth()（25 或 50），
		//否则勾选 6 之后 26-50 层会被当成"不适用"而完全失效。
		if (depth < 1 || depth > maxMainDepth()) return depth + 1;

		if (shuffleOrder == null) buildShuffleOrder();

		for (int step = 0; step < shuffleOrder.length; step++) {
			if (shuffleOrder[step] == depth) {
				if (step + 1 >= shuffleOrder.length) return depth + 1;   //最后一步，无下层
				return shuffleOrder[step + 1];
			}
		}
		return depth + 1;
	}

	/** END(2 楼层混乱): 当前楼层在游玩顺序中是第几步（1-based）；不适用时返回 0。 */
	public static int shuffleStepOf(int depth) {
		if (!on(FLOOR_SHUFFLE) || depth < 1 || depth > maxMainDepth()) return 0;
		if (shuffleOrder == null) buildShuffleOrder();
		for (int step = 0; step < shuffleOrder.length; step++) {
			if (shuffleOrder[step] == depth) return step + 1;
		}
		return 0;
	}

	/**
	 * END(2 楼层混乱): 给定"当前楼层"，返回它的**上一层**（沿同一排列往回走）。
	 *
	 * <p>上楼必须走排列的逆方向，否则会跳到错误的楼层。
	 * 未勾选 2 或不在排列内时返回 {@code depth - 1}（等价原版）。
	 */
	public static int prevShuffledDepth(int depth) {
		if (!on(FLOOR_SHUFFLE)) return depth - 1;
		//END(适配 6 完整地牢): 同上
		if (depth < 1 || depth > maxMainDepth()) return depth - 1;

		if (shuffleOrder == null) buildShuffleOrder();

		for (int step = 0; step < shuffleOrder.length; step++) {
			if (shuffleOrder[step] == depth) {
				if (step == 0) return Math.max(0, depth - 1);   //第一步，没有上层
				return shuffleOrder[step - 1];
			}
		}
		return depth - 1;
	}

	/** 诊断用：当前的排列（副本）。 */
	public static int[] shuffleOrderCopy() {
		if (shuffleOrder == null) buildShuffleOrder();
		return shuffleOrder.clone();
	}

	/** 换局时重置，让它按新局重新（用同一常量种子）构建。 */
	public static void resetShuffleOrder() {
		shuffleOrder = null;
	}

	/** 104 命悬一线：保命概率 13%。 */
	private static final int CLOSE_CALL_PCT = 13;

	/**
	 * END(65 及时雨 / 104 命悬一线): 致命伤害的"保命"判定。
	 *
	 * <p>调用点：{@code Char.damage()} 在扣血之前。
	 *
	 * <p><b>触发顺序（原表明确要求：先及时雨后命悬一线）</b>：
	 * <ol>
	 *   <li><b>65 及时雨</b>：整局**第一次**致命伤害必定不死，保留 1 点。
	 *       用一次性标记，用完就失效。</li>
	 *   <li><b>104 命悬一线</b>：之后每次致命伤害有 13% 概率保 1 点，**可重复触发**。</li>
	 * </ol>
	 *
	 * <p>两者可同时勾选：先用掉及时雨的那一次，之后靠命悬一线赌 13%。
	 *
	 * @param dmg 即将生效的伤害
	 * @return true 表示本次伤害应被削到"保留 1 点生命"
	 */
	public static boolean survivingFatalBlow(Char target, int dmg) {
		if (target == null) return false;
		//只对玩家生效（两条都是"玩家收益"）
		if (!(target instanceof Hero)) return false;
		//不是致命伤就不用管
		if (dmg < target.HP) return false;
		if (target.HP <= 1) return false;      //已经只剩 1 点，没有可保的

		//---- 65 及时雨：整局第一次 ----
		if (on(TIMELY_RAIN) && !timelyRainUsed) {
			timelyRainUsed = true;
			return true;
		}

		//---- 104 命悬一线：每次都判 ----
		if (on(CLOSE_CALL) && Random.Int(100) < CLOSE_CALL_PCT) {
			return true;
		}

		return false;
	}

	/** 65 及时雨的"已用掉"标记（每局重置，不存读档）。 */
	private static boolean timelyRainUsed = false;

	public static void resetTimelyRain() {
		timelyRainUsed = false;
	}

	/** 诊断用：及时雨是否已经用掉。 */
	public static boolean timelyRainUsed() {
		return timelyRainUsed;
	}

	/**
	 * END(104 命悬一线): 是否隐藏血量数字。
	 *
	 * <p>调用点：{@code StatusPane} 的血量文本处。
	 */
	public static boolean hideHpNumbers() {
		return on(CLOSE_CALL);
	}

	/**
	 * END(104 命悬一线): 用文字描述代替血量数字。
	 *
	 * <p>分档刻意做得**粗**（每档 20%）—— 太细就等同于显示数值，
	 * 那就失去这条规则的意义了。
	 *
	 * @return 描述文本；未勾选 104 时返回 null（调用方回退到数字显示）
	 */
	public static String hpStateDescription(int hp, int max) {
		if (!on(CLOSE_CALL) || max <= 0) return null;

		float pct = hp / (float) max;

		//边界：先用整数比较，避免浮点误差把正好 100% 判成 99.99%
		if (hp >= max)                      return msg("hp_perfect");
		if (hp * 5 >= max * 4)              return msg("hp_good");        // >=80%
		if (hp * 5 >= max * 3)              return msg("hp_hurt");        // >=60%
		if (hp * 2 >= max)                  return msg("hp_bad");         // >=50%
		if (hp * 5 >= max)                  return msg("hp_severe");      // >=20%
		if (hp * 10 >= max)                 return msg("hp_critical");    // >=10%
		return msg("hp_brink");                                           // <10%
	}

	/**
	 * 静默模式（**仅供自动化测试**）。
	 *
	 * <p>测试环境没有 libGDX 的 {@code Gdx.app}，而 {@code Messages.get()}
	 * 在静态初始化时会读 {@code SPDSettings.language()}，从而 NPE。
	 * 打开本开关后，所有 {@code GLog} / {@code Messages} 调用都会被跳过，
	 * 于是纯逻辑（概率、数值）可以在无图形环境下验证。
	 *
	 * <p>**绝不要在游戏运行时打开它** —— 那会让所有挑战提示消失。
	 */
	public static boolean silentForTests = false;

	/** 安全地取一条描述文案（测试环境下返回空串而不是崩）。 */
	private static String msg(String key) {
		if (silentForTests) return "";
		try {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
					ChallengeEffects.class, key);
		} catch (Throwable t) {
			return "";
		}
	}
	/** 安全地取一条带参数的文案。 */
	private static String msgArgs(String key, Object... args) {
		if (silentForTests) return "";
		try {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
					ChallengeEffects.class, key, args);
		} catch (Throwable t) {
			return "";
		}
	}

	/** 静默安全地写一条警告日志。 */
	private static void safeLogW(String text) {
		if (silentForTests || text == null || text.isEmpty()) return;
		try {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(text);
		} catch (Throwable ignored) { }
	}

	/** 静默安全地写一条信息日志。 */
	private static void safeLogI(String text) {
		if (silentForTests || text == null || text.isEmpty()) return;
		try {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.i(text);
		} catch (Throwable ignored) { }
	}

	/**
	 * END(81 搏杀赌徒): 是否停止常规的升级卷轴投放。
	 *
	 * <p>调用点：{@code Dungeon.souNeeded()}。
	 */
	public static boolean gamblerNoUpgradeScrolls() {
		return on(GAMBLER);
	}

	/**
	 * END(81 搏杀赌徒): 财富戒指产出的卷轴里，升级卷轴应占多大比重。
	 *
	 * <p>原表："财富戒指可获得升级卷轴" —— 但没说概率。
	 *
	 * <p><b>END(修复·体感不生效)</b>：原值 8% 实际触发概率只有
	 * 25%（case 3 在 Random.Int(4) 里被选中）× 8% = 2%，
	 * 玩家几百次掉落才看到一次，反馈是"财富戒不生效"。
	 * 提高到 **50%**：case 3 内一半概率出升级卷轴，
	 * 总概率约 12.5%，既让玩家明显感受到生效，
	 * 又不至让财富戒成为稳定的升级来源（常规升级卷轴投放仍然停止）。
	 *
	 * <p>调用点：{@code RingOfWealth} 生成卷轴处。
	 *
	 * @return 概率（0~1）；未勾选 81 时为 0
	 */
	public static final float GAMBLER_SOU_CHANCE = 0.50f;

	public static float gamblerUpgradeScrollChance() {
		return on(GAMBLER) ? GAMBLER_SOU_CHANCE : 0f;
	}

	/**
	 * END(81 搏杀赌徒): 开户赠送的财富戒指等级。
	 *
	 * <p>原表："开局获得 +3 财富戒指"。
	 */
	public static final int GAMBLER_RING_LEVEL = 3;

	public static boolean gamblerStarterRing() {
		return on(GAMBLER);
	}

	/**
	 * END(40 贷款): 是否可以在商店贷款。
	 *
	 * <p>同时要求"商店存在"—— 没勾这条规则时不出现贷款选项。
	 */
	public static boolean loanAvailable() {
		return on(LOAN);
	}

	/** END(40 贷款): 可贷款的金额档位（玩家在商店里自选）。 */
	public static final int[] LOAN_AMOUNTS = { 100, 300, 500, 1000 };

	/**
	 * END(40 贷款): 是否允许再借一笔。
	 *
	 * <p>同一时间只允许**一笔未还清的债务** —— 否则玩家可以无限叠加，
	 * 把"1000 回合后还 110%"变成没有约束的白送。
	 */
	public static boolean canTakeLoan(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
		if (!on(LOAN) || hero == null) return false;
		return hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
				.LoanDebt.class) == null;
	}

	/**
	 * END(40 贷款): 借入一笔钱。
	 *
	 * @return 实际借到的金额（0 表示借不了）
	 */
	public static int takeLoan(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero, int amount) {
		if (!canTakeLoan(hero) || amount <= 0) return 0;

		Dungeon.gold += amount;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LoanDebt debt =
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						hero,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.LoanDebt.class);
		debt.setPrincipal(amount);

		return amount;
	}

	/** 141 禁魔空间：魔法伤害倍率 0.8。 */
	private static final float ANTI_MAGIC_MULT = 0.80f;
	/** 52 陷阱泛滥：陷阱数量倍率 2.0。 */
	private static final float TRAP_COUNT_MULT = 2.0f;
	/** 74 热带雨林：水中生成食人鱼概率 13%。 */
	private static final int   RAINFOREST_PCT  = 13;
	/** 149 黏糊蜂蜜：每层蜜蜂数量。 */
	private static final int   HONEY_BEES      = 2;

	/**
	 * END(141 禁魔空间): 魔法伤害的整体倍率。
	 *
	 * <p>调用点：{@code Char.damage()} 里判明伤害来源属于魔法
	 * （{@code AntiMagic.RESISTS.contains(src.getClass())}）之后。
	 *
	 * <p>玩家与怪物**都**受影响 —— 这才是原表说的"包括玩家与怪物"。
	 */
	public static int magicDamageTaken(int dmg) {
		if (!on(ANTI_MAGIC_ZONE) || dmg <= 0) return dmg;
		return Math.max(1, Math.round(dmg * ANTI_MAGIC_MULT));
	}

	/**
	 * END(52 陷阱泛滥): 地图陷阱数量的倍率。
	 *
	 * <p>调用点：{@code RegularLevel} 里生成陷阱处。
	 * 实现方式与其它"数量倍率"（30/119 怪物数量）保持一致：
	 * 倍率作用在**生成上限**上，而不是逐条掷骰。
	 */
	public static float trapCountMultiplier() {
		return on(TRAP_OVERFLOW) ? TRAP_COUNT_MULT : 1f;
	}

	/**
	 * END(49 切尔诺贝利): 该格是否应变成毒气。
	 *
	 * <p>原表："全图毒气，玩家受影响，怪物免疫"。
	 * 这里返回是否启用；具体的毒气铺设与"怪物免疫"由调用点处理。
	 */
	public static boolean chernobylEnabled() {
		return on(CHERNOBYL);
	}

	/**
	 * END(49 切尔诺贝利): 每层额外发放的净化药水数量。
	 *
	 * <p>原表："开局给净化药水，每层额外给"。
	 * 这里是**每层**给的数量；开局那份由 {@link #startingGear()} 单独发放。
	 */
	public static final int CHERNOBYL_PURIFY_PER_FLOOR = 1;

	public static int chernobylPurifyPerFloor() {
		return on(CHERNOBYL) ? CHERNOBYL_PURIFY_PER_FLOOR : 0;
	}

	/**
	 * END(49 切尔诺贝利): 净化药水的持续回合数。
	 *
	 * <p>原表："持续时间增加至 300 回合"。未勾选 49 时返回原版的
	 * {@code BlobImmunity.DURATION}（20），勾选后为 300。
	 */
	public static final float CHERNOBYL_PURITY_DURATION = 300f;

	public static float purityDuration(float vanilla) {
		return on(CHERNOBYL) ? CHERNOBYL_PURITY_DURATION : vanilla;
	}

	/**
	 * END(49 切尔诺贝利): 全面净化（进化版净化药水）的持续回合数。
	 *
	 * <p>原表："全面净化持续回合增加到 900" —— 但文档所有者后来修正为 **600**。
	 * 未勾选 49 时返回原版的 {@code PotionOfCleansing.Cleanse.DURATION}（5）。
	 */
	public static final float CHERNOBYL_CLEANSE_DURATION = 600f;

	public static float cleanseDuration(float vanilla) {
		return on(CHERNOBYL) ? CHERNOBYL_CLEANSE_DURATION : vanilla;
	}

	/**
	 * END(49 切尔诺贝利): 净化药水是否应**只保留解毒功能**。
	 *
	 * <p>原表："净化药水改为只有解毒功能"。
	 *
	 * <p>含义：原本喝净化药水会给 {@code BlobImmunity}（对所有气体免疫），
	 * 勾选 49 后改为**只清除身上已有的负面效果**，
	 * 不再提供"站在毒气里也不受伤"的全免疫 ——
	 * 否则全图毒气这条规则就形同虚设。
	 */
	public static boolean purityOnlyCuresDebuffs() {
		return on(CHERNOBYL);
	}

	/**
	 * END(159 绵羊地牢): 玩家周围是否该刷羊，并返回刷几只。
	 *
	 * <p>原表："回合在玩家 7×7 范围 13% 生成 1~2 绵羊，生成后有 20 回合 CD"。
	 *
	 * <p>三条约束：
	 * <ol>
	 *   <li><b>7×7 范围</b>：以玩家为中心、半径 3 格内找空位。</li>
	 *   <li><b>20 回合 CD</b>：用独立计数器，不挂 buff（这是纯生成节流，
	 *       中途存读档重新计数无伤大雅）。</li>
	 *   <li><b>13% 概率</b>。</li>
	 * </ol>
	 *
	 * @return 本次应生成的绵羊数量（0 表示不生成）
	 */
	private static final int   SHEEP_RADIUS = 3;      // 7x7 => 半径 3
	private static final int   SHEEP_PCT    = 13;
	private static final int   SHEEP_COOLDOWN = 20;
	private static int sheepCooldown = 0;

	public static int rollSheepSpawn(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {

		if (!on(SHEEP_DUNGEON) || hero == null || !hero.isAlive()) {
			sheepCooldown = 0;
			return 0;
		}

		//CD 中：递减并跳过
		if (sheepCooldown > 0) {
			sheepCooldown--;
			return 0;
		}

		if (Random.Int(100) >= SHEEP_PCT) return 0;

		sheepCooldown = SHEEP_COOLDOWN;
		return Random.IntRange(1, 2);     // 1~2 只
	}

	/**
	 * END(159 绵羊地牢): 在玩家周围找一个可站立的空位。
	 *
	 * @return 格子编号；找不到返回 -1
	 */
	public static int findSheepSpot(int heroPos, int radius) {
		if (Dungeon.level == null) return -1;

		java.util.ArrayList<Integer> candidates = new java.util.ArrayList<>();
		int w = Dungeon.level.width();
		int cx = heroPos % w;
		int cy = heroPos / w;

		for (int dy = -radius; dy <= radius; dy++) {
			for (int dx = -radius; dx <= radius; dx++) {
				if (dx == 0 && dy == 0) continue;          //玩家自己那格不刷
				int x = cx + dx, y = cy + dy;
				if (x < 0 || y < 0 || x >= w || y >= Dungeon.level.height()) continue;
				int cell = x + y * w;
				//必须能站、没被占、且玩家看得到（否则羊在视野外莫名出现）
				if (!Dungeon.level.passable[cell]) continue;
				if (Dungeon.level.solid[cell]) continue;
				if (Dungeon.level.heroFOV[cell]) continue; //刷在可见格会突兀
				if (com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(cell)
						!= null) continue;
				candidates.add(cell);
			}
		}

		if (candidates.isEmpty()) return -1;
		return candidates.get(Random.Int(candidates.size()));
	}

	public static int sheepRadius() { return SHEEP_RADIUS; }

	//==================================================================
	//1 牢地碎破：区域交叉
	//==================================================================

	/** 1 牢地碎破：区域倒置（1区↔5区、2区↔4区、3区不变）。 */
	public static final int CRUMBLING_DUNGEON = 1;

	/**
	 * END(1 牢地碎破): 把"实际楼层"映射成"用于查怪物表的楼层"。
	 *
	 * <p>实现的是**区域倒置**：
	 * <pre>
	 *   实际 1区(1-5F)   -> 5区(21-25F)
	 *   实际 2区(6-10F)  -> 4区(16-20F)
	 *   实际 3区(11-15F) -> 3区(11-15F)   不变
	 *   实际 4区(16-20F) -> 2区(6-10F)
	 *   实际 5区(21-25F) -> 1区(1-5F)
	 * </pre>
	 *
	 * <p>映射只改"用哪张表"，怪物配比（每种几只）沿用原表，
	 * 因此不必重写五张表 —— 这是最不容易出错的做法。
	 *
	 * <p>未勾选 1 时原样返回 {@code depth}。
	 */
	public static int crumblingCrossDepth(int depth) {
		if (!on(CRUMBLING_DUNGEON)) return depth;
		if (depth < 1) return depth;

		//只处理主线 1-25 层；挑战区（26F 起）有自己的刷怪表，不参与倒置
		if (depth > 25) return depth;

		int region = (depth - 1) / 5;              //0..4
		int within = (depth - 1) % 5;              //区域内偏移 0..4

		//区域倒置：0<->4, 1<->3, 2 不变
		int mirroredRegion = 4 - region;

		return mirroredRegion * 5 + within + 1;
	}

	/**
	 * END(1 牢地碎破): 取某个角色当前所在的区域（1..5），用于查数值表。
	 *
	 * <p>用 {@code Dungeon.depth} 的**实际楼层**分区，而不是交叉后的楼层 ——
	 * 因为数值表本身就是按"实际区域"配的（1 区的表里写的是魅魔等）。
	 *
	 * @return 区域编号；不在主线 1-25 层时返回 0（表示"不适用"）
	 */
	public static int crumblingRegion() {
		int d = Dungeon.depth;
		if (d < 1 || d > 25) return 0;
		return (d - 1) / 5 + 1;
	}

	/** 查表：某个角色对应的数值行；无表项或未勾选挑战时返回 null。 */
	private static com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
			.CrumblingStats.Stats crumblingRow(Char ch) {
		if (!on(CRUMBLING_DUNGEON) || ch == null) return null;
		//只对怪物生效（玩家数值不受这条规则影响）
		if (!(ch instanceof Mob)) return null;
		int region = crumblingRegion();
		if (region == 0) return null;
		return com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.CrumblingStats.lookup(region, ch.getClass().getSimpleName());
	}

	/**
	 * END(1 牢地碎破): 覆写攻击命中。
	 *
	 * <p>调用点：{@code Char.hit()} —— 全游戏唯一的命中判定点。
	 * 未勾选或表里没有该怪物时原样返回 {@code vanilla}。
	 */
	public static float crumblingAccuracy(Char attacker, float vanilla) {
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.CrumblingStats.Stats s = crumblingRow(attacker);
		return (s == null) ? vanilla : s.acc;
	}

	/** END(1 牢地碎破): 覆写闪避。调用点同上。 */
	public static float crumblingEvasion(Char defender, float vanilla) {
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.CrumblingStats.Stats s = crumblingRow(defender);
		return (s == null) ? vanilla : s.eva;
	}

	/** END(1 牢地碎破): 覆写护甲。调用点：{@code Char.attack()} 的 drRoll 处。 */
	public static int crumblingArmor(Char target, int vanilla) {
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.CrumblingStats.Stats s = crumblingRow(target);
		if (s == null) return vanilla;
		//表里给的是 [min,max] 区间，取随机值（与原版 drRoll 的语义一致）
		return Random.NormalIntRange(s.drMin, s.drMax);
	}

	/**
	 * END(1 牢地碎破): 覆写攻击伤害。
	 *
	 * <p>只在**表里有该怪物**时生效；表里的伤害是 [min,max]，按原版语义取随机。
	 */
	public static float crumblingDamage(Char attacker, float vanilla) {
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.CrumblingStats.Stats s = crumblingRow(attacker);
		if (s == null) return vanilla;
		if (s.dmgMax <= 0) return 0;          //幽灵死灵法师、菌丝节点等伤害为 0
		return Random.NormalIntRange(s.dmgMin, s.dmgMax);
	}

	/**
	 * END(1 牢地碎破): 覆写生命上限。
	 *
	 * <p>HP/HT 是**实例字段**（不像命中/伤害是方法），所以可以直接赋值。
	 * 调用点：{@code Level.createMob()} —— 全游戏怪物实例化的唯一出口。
	 *
	 * @return 覆写后的 HP；表里没有则返回 {@code vanilla}
	 */
	public static int crumblingHP(Char mob, int vanilla) {
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.CrumblingStats.Stats s = crumblingRow(mob);
		return (s == null) ? vanilla : Math.max(1, s.hp);
	}

	/**
	 * END(1 牢地碎破): 5 区刷原 1 区怪物时，是否应追加"移速×2 + 永久祝福"。
	 *
	 * <p>配置表说明："原1区来源的怪物（出现在5区）已追加移速×2，永久祝福"。
	 * 这些怪物的表项在 5 区，所以判据是"当前区域为 5 且该怪在表中有 5 区项"。
	 */
	public static boolean crumblingSwiftBlessed(Char mob) {
		if (!on(CRUMBLING_DUNGEON) || mob == null) return false;
		return crumblingRegion() == 5 && crumblingRow(mob) != null;
	}

	/**
	 * END(74 热带雨林): 水中是否生成食人鱼，以及概率。
	 *
	 * <p>调用点：地图生成后遍历水域格。
	 *
	 * @return 生成概率（0~100）；未勾选时为 0
	 */
	public static int rainforestPiranhaChance() {
		return on(RAINFOREST) ? RAINFOREST_PCT : 0;
	}

	/**
	 * END(149 黏糊蜂蜜): 每层额外刷新的蜜蜂数量。
	 *
	 * <p>调用点：{@code RegularLevel.createMobs()} 之后。
	 */
	public static int honeyBeeCount() {
		return on(STICKY_HONEY) ? HONEY_BEES : 0;
	}

	/**
	 * END(162 真实地牢): 空气稀薄 —— 玩家需要每 N 回合停下深呼吸一次。
	 *
	 * <p>原表："每下一个区域，空气会稀薄，需要每 50−5×(层数/5) 回合停下来深呼吸"。
	 * 即**层数越深，间隔越短**：第 5 层 = 50−5 = 45 回合，第 25 层 = 50−25 = 25 回合。
	 * 最低不低于 {@link #REALISTIC_MIN_INTERVAL}，避免深层变成每回合都要停。
	 *
	 * @return 两次深呼吸之间的回合间隔
	 */
	public static final int REALISTIC_MIN_INTERVAL = 15;

	public static int realisticBreathInterval() {
		int region = Math.max(1, Dungeon.depth / 5);        //第 5 层算 1 区
		int interval = 50 - 5 * region;
		return Math.max(REALISTIC_MIN_INTERVAL, interval);
	}

	/**
	 * END(162 真实地牢): 玩家本回合是否需要"停下深呼吸"。
	 *
	 * <p>用独立的计数器，而不是挂 buff —— 这个状态不需要存读档
	 * （深呼吸只是每 N 回合强制消耗一回合，中途存读档重新计数无伤大雅）。
	 *
	 * @return true 表示本回合被强制停下
	 */
	private static int breathCounter = 0;

	public static boolean tickRealisticBreath(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
		if (!on(REALISTIC_DUNGEON) || hero == null || !hero.isAlive()) {
			breathCounter = 0;
			return false;
		}

		breathCounter++;
		int interval = realisticBreathInterval();
		if (breathCounter < interval) return false;

		breathCounter = 0;

		//停下深呼吸：消耗本回合 + 给一个短暂的可视反馈
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
				hero,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis.class,
				1f);
		com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
				com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
						ChallengeEffects.class, "breath_stop"));
		return true;
	}

	/** 71 喝大了：概率与眩晕回合数。 */
	private static final int   DRUNK_PCT   = 3;
	private static final float DRUNK_TURNS = 3f;
	/** 46 药剂不稳定：触发概率 13%。 */
	private static final int   UNSTABLE_POTION_PCT = 13;

	/** 58 随机附魔：获得时附魔概率 50%。 */
	private static final int RANDOM_ENCHANT_PCT = 50;
	/** 55 不稳定强化：额外强化概率 13%，加成 +2。 */
	private static final int UNSTABLE_PCT   = 13;
	private static final int UNSTABLE_BONUS = 2;

	/**
	 * END(46 药剂不稳定): 玩家饮用药水后，13% 概率追加一个随机效果。
	 *
	 * <p>调用点：{@code Potion.drink()} —— 在 {@code apply()} **之后**，
	 * 所以药水本身的效果照常生效，随机效果是额外叠加。
	 *
	 * <p>倾向为"双刃剑"（原表如此），因此效果表**正负各半**：
	 * 可能给你增益，也可能给你减益。这才是这条规则的核心体验。
	 */
	public static void onPotionDrunk(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
		if (!on(UNSTABLE_POTION) || hero == null || !hero.isAlive()) return;
		if (Random.Int(100) >= UNSTABLE_POTION_PCT) return;

		int roll = Random.Int(6);
		switch (roll) {
			case 0:   //正面：急速
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
						hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.Haste.class, 10f);
				showPotionEffect(hero, "haste");
				break;
			case 1:   //正面：隐形
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
						hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.Invisibility.class, 10f);
				showPotionEffect(hero, "invisible");
				break;
			case 2:   //正面：护盾
				//注意：Barrier 不是 FlavourBuff，没有带时长的 affect 重载，
				//只能用无时长版本（它的持续时长由自身机制决定）。
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.Barrier.class);
				showPotionEffect(hero, "barrier");
				break;
			case 3:   //负面：中毒
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.Poison.class);
				showPotionEffect(hero, "poison");
				break;
			case 4:   //负面：燃烧
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.Burning.class);
				showPotionEffect(hero, "burning");
				break;
			default:  //负面：眩晕
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
						hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.Paralysis.class, 3f);
				showPotionEffect(hero, "paralysed");
				break;
		}
	}

	/** 提示玩家随机效果是什么（46 药剂不稳定）。 */
	private static void showPotionEffect(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero, String key) {
		com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
				com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
						ChallengeEffects.class, "unstable_potion_" + key));
	}

	/**
	 * END(58 随机附魔): 装备获得时是否应随机附魔。
	 *
	 * <p>原表："装备获得时 50% 概率获得随机附魔词缀"（与 108 觉醒独立计算）。
	 * 调用点：{@code Weapon.random()} / {@code Armor.random()} —— 在**原有**的
	 * 10% 附魔判定之外**额外**判定一次，两者互不排斥。
	 *
	 * @return true 表示应给这件新装备附魔
	 */
	public static boolean rollRandomEnchant() {
		return on(RANDOM_ENCHANT) && Random.Int(100) < RANDOM_ENCHANT_PCT;
	}

	/**
	 * END(55 不稳定强化): 本次强化额外增加的等级。
	 *
	 * <p>原表："强化时 13% 额外 +2，13% 不变，其余正常"。
	 * 即：13% 概率额外 +2 级；成功或失败的原判定照旧。
	 *
	 * <p>调用点：{@code ScrollOfUpgrade} / {@code Item.upgrade()} 的强化结算处。
	 *
	 * @return 额外等级（0 或 2）
	 */
	public static int bonusUpgradeLevels() {
		if (!on(UNSTABLE_UPGRADE)) return 0;
		return (Random.Int(100) < UNSTABLE_PCT) ? UNSTABLE_BONUS : 0;
	}

	/** 57 残缺装备：触发概率 13%。 */
	private static final int FLAWED_PCT = 13;
	/** 59 诅咒装备：额外诅咒概率 13%。 */
	private static final int CURSED_PCT = 13;

	/**
	 * END(挑战 57 残缺装备): 随机附魔时是否应给出「残缺」词缀。
	 *
	 * <p>调用点：{@code Weapon.Enchantment.random(...)}。
	 * **只在勾选了 57 时才可能为 true** —— 未勾选时此方法恒返回 false，
	 * 因此不会污染普通对局的随机附魔池。
	 *
	 * @param toIgnore 调用方要求排除的附魔类型；若已包含 Flawed 则不再给出
	 */
	public static boolean rollFlawedEnchant(Class<?>... toIgnore) {
		if (!on(BROKEN_EQUIPMENT)) return false;

		//调用方明确排除了 Flawed（例如已有该词缀），就不再给
		if (toIgnore != null) {
			for (Class<?> c : toIgnore) {
				if (c != null && c.getName().endsWith("Flawed")) return false;
			}
		}

		return Random.Int(100) < FLAWED_PCT;
	}

	/**
	 * END(挑战 59 诅咒装备): 装备获得时，额外增加多少诅咒概率。
	 *
	 * <p>原表："装备获得诅咒概率增加 13%" —— 是**概率**提升，不是一个新附魔。
	 * 调用点：{@code Weapon.random()} / {@code Armor.random()} 里判断是否诅咒处。
	 *
	 * @return 额外的诅咒概率（0~1）；未勾选时为 0
	 */
	public static float extraCurseChance() {
		return on(CURSED_EQUIPMENT) ? (CURSED_PCT / 100f) : 0f;
	}

	/** 17 情人节：魅惑概率 13%。 */
	private static final int VALENTINE_PCT = 13;
	/** 79 高级附魔台：每区域发放数量。 */
	private static final int ENCHANT_PER_REGION = 1;

	//==== 第五批（准易档）：管线钩子已预留，填空即可 ====

	/** 18 老龄化：普通怪物每回合 13% 概率睡眠 1 回合。 */
	public static final int AGING            = 18;
	/** 22 物极必反：单次伤害超目标最大生命 150% 时完全免疫。 */
	public static final int OVERKILL_REVERSE = 22;
	/** 69 九九归一：最终伤害为 9 的倍数时变为 1。 */
	public static final int NINE_TO_ONE      = 69;
	/** 103 弹幕地狱：远程投射物变 3 发散射。 */
	public static final int BULLET_HELL      = 103;
	/** 121 中世纪骑士：护甲值 +60%，移动速度 -50%。 */
	public static final int MEDIEVAL_KNIGHT  = 121;
	/** 140 枪枪爆头：距离 >=5 格时远程伤害必为最大值。 */
	public static final int HEADSHOT         = 140;

	/** 18 老龄化：每回合触发概率。 */
	private static final int   AGING_PCT        = 13;
	/** 103 弹幕地狱：投射物数量。 */
	public static final int    BULLET_HELL_COUNT = 3;
	/** 121 中世纪骑士：护甲与移速倍率。 */
	private static final float KNIGHT_ARMOR_MULT = 1.60f;
	private static final float KNIGHT_SPEED_MULT = 0.50f;
	/** 140 枪枪爆头：触发距离（格）。 */
	private static final int   HEADSHOT_RANGE    = 5;

	/** 145 神圣附体：经验倍率 1.2。 */
	private static final float HOLY_EXP_MULT    = 1.20f;
	/** 146 醍醐灌顶：每个天赋层级额外点数。 */
	private static final int   ENLIGHTEN_BONUS  = 1;
	/** 165 神圣之光：触发概率与回复比例。 */
	/** END(修订): 165 神圣之光每回合触发概率，13% → **3%**。 */
	private static final int   HOLY_LIGHT_PCT   = 3;
	private static final float HOLY_LIGHT_HEAL  = 0.02f;

	/**
	 * END(145 神圣附体): 经验获取倍率。
	 * <p>调用点：{@code Hero.gainExp()} / 经验计算处。
	 */
	public static float expMultiplier() {
		return on(HOLY_POSSESSION) ? HOLY_EXP_MULT : 1f;
	}

	/**
	 * END(146 醍醐灌顶): 该天赋层级应获得多少**额外**天赋点。
	 *
	 * <p>调用点：{@code Hero.bonusTalentPoints(tier)}。
	 * 每个层级都 +1（原表说"每层获得 1 点额外天赋点"，
	 * "每层"指每提高一级，这里按"每个天赋层级各 +1"实现，
	 * 效果等价于每次升级都多一点可分配。
	 *
	 * <p>与 147 就业紧张**互斥**（框架已强制置灰），不会同时生效。
	 */
	public static int bonusTalentPoints(int tier) {
		return on(ENLIGHTENMENT) ? ENLIGHTEN_BONUS : 0;
	}

	/**
	 * END(147 就业紧张): 职业天赋是否应全部失效。
	 *
	 * <p>调用点：{@code Hero.pointsInTalent()} —— 让它直接返回 0，
	 * 所有下游的 {@code hasTalent()} / 天赋加成一并失效。
	 * 这比在几十处逐个判断可靠得多。
	 */
	public static boolean talentsDisabled() {
		return on(JOB_CRISIS);
	}

	/**
	 * END(165 神圣之光): 每回合 13% 概率回复 2% 最大生命。
	 *
	 * <p>调用点：{@code Hero.act()}（与其它回合类规则同处）。
	 * 满血时不触发（避免无意义的状态提示）。
	 *
	 * @return 实际回复量（0 表示未触发或已满血）
	 */
	public static int rollHolyLightHeal(Char ch) {
		if (!on(HOLY_LIGHT) || ch == null) return 0;
		if (ch.HP >= ch.HT) return 0;                       //满血不回
		if (Random.Int(100) >= HOLY_LIGHT_PCT) return 0;

		int heal = Math.max(1, Math.round(ch.HT * HOLY_LIGHT_HEAL));
		return Math.min(heal, ch.HT - ch.HP);               //不溢出上限
	}

	/**
	 * END(155 家传戒指 / 156 家传铠甲): 开局额外获得的装备**数量**。
	 *
	 * <p>单独提供这个方法是为了可测试性：构造 Item 实例会触发
	 * ItemSpriteSheet 的纹理加载（需要 libGDX 图形环境），
	 * 在无图形的环境里无法实例化。数量判断则不依赖实例。
	 */
	public static int startingGearCount() {
		int n = 0;
		if (on(HEIRLOOM_RING))  n++;
		if (on(HEIRLOOM_ARMOR)) n++;
		if (on(HEIRLOOM_WAND))  n++;
		if (on(SKIP_STUDENT))   n++;   // 7 跳级券
		if (on(CHERNOBYL))      n++;   // 49 净化药水
		if (on(GAMBLER))        n++;   // 81 财富戒指
		if (on(GRIMM_WEAPON))   n += 2; // 125 银色短铳 + 兔子怀表
		if (on(GRIMM_WEAPON_2)) n++;    // 133 神天使双剑
		if (on(GRIMM_WEAPON_3)) n += 2; // 136 怨恨之剑 + 勇剑
		if (on(GRIMM_RING))     n++;    // 127 黑兔戒指
		if (on(GRIMM_ART))      n++;    // 128 镇魂歌
		if (on(GOLDEN_MEAD))    n += 3; // 134 黄金蜂蜜酒 x3
		if (on(GRIMM_HEART))    n++;    // 126 魂之容器
		if (on(ALL_OR_NOTHING)) n++;    // 39 赌徒之骰
		if (on(EXCHANGE))       n++;    // 42 交换契约
		if (on(MONEY_IS_POWER)) n++;    // 41 万能钱袋
		return n;
	}

	/**
	 * END(155 家传戒指 / 156 家传铠甲): 开局额外获得的装备。
	 *
	 * <p>调用点：{@code Dungeon.init()}，在 {@code initHero} 之后。
	 * 返回需要发放的装备实例列表（**每次调用都新建**，不要缓存）。
	 *
	 * <p>同时勾选两条时两件都发。
	 */
	public static java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.items.Item>
			startingGear() {
		java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.items.Item> out =
				new java.util.ArrayList<>();

		//155 家传戒指：神射戒指
		if (on(HEIRLOOM_RING)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting());
		}

		//156 家传铠甲：板甲
		if (on(HEIRLOOM_ARMOR)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor());
		}

		//60 家传法杖：从 13 种进阶法杖里随机一支
		if (on(HEIRLOOM_WAND)) {
			try {
				java.util.List<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items
						.wands.Wand>> classes =
						com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved
								.EndWandEvolution.allEvolvedWandClasses();
				if (!classes.isEmpty()) {
					Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand> c =
							classes.get(Random.Int(classes.size()));
					com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand w =
							c.getDeclaredConstructor().newInstance();
					out.add(w);
				}
			} catch (Throwable t) {
				//拿不到就不发，绝不让"开局送装备"把游戏拖崩
				System.err.println("[挑战 60 家传法杖] 生成法杖失败：" + t);
			}
		}

		//7 跳级生：开局给 1 张跳级券（一次性物品，玩家自己决定何时用）
		if (on(SKIP_STUDENT)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.items.SkipTicket());
		}

		//49 切尔诺贝利：开局给净化药水（否则第 1 层就开始中毒，没有解药）
		if (on(CHERNOBYL)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.items.potions
					.PotionOfPurity());
		}

		//==== END(格林系列): 开局发放专属装备 ====
		//125 格林之器：银色短铳 + 兔子怀表
		//（原表把怨恨之剑/勇剑也写在 125 里，但文档所有者明确
		//  "125 没有后面两个武器，那是 3 里的"，所以 125 只发这两件）
		if (on(GRIMM_WEAPON)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.SilverGun());
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.RabbitWatch());
		}

		//133 格林之器2：神天使双剑
		if (on(GRIMM_WEAPON_2)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.AngelSword());
		}

		//136 格林之器3：怨恨之剑 + 勇剑
		if (on(GRIMM_WEAPON_3)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.HateSword());
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.BraveSword());
		}

		//127 格林之戒：黑兔戒指
		if (on(GRIMM_RING)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.RabbitRing());
		}

		//128 格林之术：镇魂歌
		//END(修订): 由 2 张改为 **1 张**（文档所有者指定）。
		if (on(GRIMM_ART)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.SoulRequiem());
		}

		//41 钱是万能：万能钱袋
		if (on(MONEY_IS_POWER)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.AlmightyPurse());
		}

		//42 等价交换：交换契约
		if (on(EXCHANGE)) {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm.ExchangeContract ec =
					new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
							.ExchangeContract();
			ec.quantity(3);
			out.add(ec);
		}

		//39 All or Nothing：赌徒之骰
		if (on(ALL_OR_NOTHING)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.GamblersDice());
		}

		//126 格林之心：魂之容器（攒魂/献祭的入口）
		if (on(GRIMM_HEART)) {
			out.add(new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.SoulVessel());
		}

		//134 黄金蜂蜜酒：给 3 瓶
		if (on(GOLDEN_MEAD)) {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm.GoldenMead gm =
					new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
							.GoldenMead();
			gm.quantity(3);
			out.add(gm);
		}

		//81 搏杀赌徒：开局给 +3 财富戒指
		//（常规升级卷轴投放已被取消，这枚戒指是唯一的升级来源）
		if (on(GAMBLER)) {
			com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth ring =
					new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth();
			ring.level(GAMBLER_RING_LEVEL);
			out.add(ring);
		}

		return out;
	}

	/** 10 巨型化：触发概率 13%。 */
	private static final int   GIANT_PCT        = 13;
	/** 10 巨型化：生命倍率。 */
	private static final float GIANT_HP_MULT    = 1.50f;
	/** 54 我爱花花：替换概率 13%。 */
	private static final int   FLOWER_PCT       = 13;
	/** 138 荒诞世界：贴图随机变化概率。 */
	private static final int   ABSURD_PCT       = 100;

	/**
	 * END(10 巨型化): 决定该怪物是否被巨型化（13% 概率）。
	 *
	 * <p>调用点：{@code Level.createMob()} —— 怪物实例化的唯一出口，
	 * 所以是"生成时决定一次"而非每回合判定。
	 *
	 * <p>Boss / 小 Boss 不参与（否则血量 ×1.5 会严重失衡）；NPC 也不参与。
	 */
	public static boolean rollGiant(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m) {
		if (!on(GIANT) || m == null) return false;

		if (Char.hasProp(m, Char.Property.BOSS) || Char.hasProp(m, Char.Property.MINIBOSS)) {
			return false;
		}
		if (m.alignment != Char.Alignment.ENEMY) return false;

		return Random.Int(100) < GIANT_PCT;
	}

	/**
	 * END(10 巨型化): 应用生命加成并打上标记。
	 *
	 * <p>当前血量同步提升，否则巨型怪会以"残血"状态登场。
	 * 视觉放大由 {@code Mob.sprite()} 读取标记后设置
	 * （**不加 Property.LARGE** —— 那会让怪进不了门道，
	 * 超出了原表"更高生命值及更大体型"的范围）。
	 */
	public static void applyGiant(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m) {
		if (m == null) return;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
				m, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChallengeGiantMark.class);

		int newHT = Math.max(1, Math.round(m.HT * GIANT_HP_MULT));
		int gained = newHT - m.HT;
		m.HT = newHT;
		m.HP = Math.min(newHT, m.HP + Math.max(0, gained));
	}

	/** END(10 巨型化): 该角色是否带巨型化标记（供 {@code Mob.sprite()} 使用）。 */
	public static boolean isGiant(Char ch) {
		return ch != null && ch.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
				.ChallengeGiantMark.class) != null;
	}

	/** END(54 我爱花花): 是否勾选了该挑战（用于跳过整图遍历）。 */
	public static boolean flowerEnabled() {
		return on(FLOWER_LOVER);
	}

	/** END(54 我爱花花): 单格草地是否应替换成花（13%）。 */
	public static boolean rollFlower() {
		if (!on(FLOWER_LOVER)) return false;
		return Random.Int(100) < FLOWER_PCT;
	}

	/**
	 * END(138 荒诞世界): 怪物贴图是否随机变化。
	 *
	 * <p>**纯外观，绝不改数值**。原表没给概率，取"全部变化"。
	 */
	public static boolean rollAbsurdSprite() {
		if (!on(ABSURD_WORLD)) return false;
		return Random.Int(100) < ABSURD_PCT;
	}

	/**
	 * END(138 荒诞世界): 确保该怪物已挂上"贴图记录"标记。
	 *
	 * <p>调用点：{@code Mob.sprite()}。返回 true 表示需要走换贴图流程。
	 * 标记本身承载"这次换成哪个贴图"，避免每次重建 sprite 都重新随机
	 * 导致贴图闪烁。
	 */
	public static boolean onAbsurdWorld(Char ch) {
		if (!on(ABSURD_WORLD) || ch == null) return false;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
				ch, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChallengeAbsurdMark.class);
		return true;
	}

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
	/**
	 * 80 冰天雪地：寒冷概率、冰冻概率。
	 *
	 * <p>END(修订): 寒冷由 13% 下调为 **3%**（文档所有者要求）。
	 * 冰冻保持 2% 不变。
	 *
	 * <p>注意两者是 {@code if / else if} 关系（先判冰冻），所以**总触发率**
	 * 不是简单相加，而是 {@code 冰冻 + (1-冰冻)×寒冷}：
	 * 改前 = 2% + 98%×13% ≈ 14.7%，改后 = 2% + 98%×3% ≈ 4.9%。
	 */
	private static final int   FROZEN_CHILL_PCT  = 1;   //END(修订): 3% -> 1%（实测过高）
	private static final int   FROZEN_FREEZE_PCT = 1;   //END(修订): 2% -> 1%
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

		//---- 71 喝大了：每回合 3% 触发眩晕 3 回合 ----
		//原表："每回合3%触发眩晕3回合"。用 Paralysis 实现 ——
		//它是本 fork 既有的"不能行动"状态，图标与回合递减都已处理好。
		if (on(DRUNK) && Random.Int(100) < DRUNK_PCT) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
					hero,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis.class,
					DRUNK_TURNS);
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
					com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
							ChallengeEffects.class, "drunk_stun"));
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

	//==================================================================
	//第六批（准易档：管线钩子已预留，填空即可）
	//==================================================================

	/**
	 * END(17 情人节): 玩家攻击命中后，13% 概率魅惑目标。
	 *
	 * <p>只对**玩家**的攻击生效（原表写的是"玩家攻击"）。
	 * 用 {@code Charm} buff 实现 —— 它会记录魅惑来源，让被魅惑者停止敌对行为。
	 *
	 * <p>与 23 血流成河分开判定：两者可同时生效，互不干扰。
	 *
	 * @param attacker 攻击方
	 * @param enemy    目标
	 */
	public static void onHeroAttackCharm(Char attacker, Char enemy) {
		if (!on(VALENTINE)) return;
		if (attacker == null || enemy == null) return;
		if (!(attacker instanceof Hero)) return;
		if (!(enemy instanceof Mob)) return;
		if (!enemy.isAlive()) return;

		//已经魅惑着的就别重复挂（避免刷 buff 计时）
		if (enemy.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm.class)
				!= null) {
			return;
		}
		if (Random.Int(100) >= VALENTINE_PCT) return;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm charm =
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						enemy,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm.class,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm.DURATION);
		//记录魅惑来源 —— Charm 靠这个字段判断"被谁魅惑"，
		//不设的话被魅惑者不会正确地把玩家当盟友。
		charm.object = attacker.id();
	}

	/**
	 * END(79 高级附魔台): 进入新区域时发放的附魔秘卷数量。
	 *
	 * <p>原表："每个区域获得 1 个附魔秘卷" —— 是**每区域**（每 5 层）而不是每层。
	 * 调用点：{@code Dungeon.newLevel()} 里判 {@code depth % 5 == 1} 的地方。
	 */
	public static int enchantScrollsOnNewRegion() {
		return on(ADVANCED_ENCHANT) ? ENCHANT_PER_REGION : 0;
	}

	/**
	 * END(18 老龄化): 该怪物本回合是否应睡眠。
	 *
	 * <p>普通怪物每回合 13% 概率睡眠，持续 1 回合。
	 * <p>只对**普通怪**生效：Boss / 精英怪免疫（否则 Boss 被睡 1 回合太离谱）。
	 *
	 * @return true 表示应给该怪挂睡眠
	 */
	public static boolean rollAgingSleep(Char ch) {
		if (!on(AGING) || ch == null) return false;
		if (ch instanceof Hero) return false;
		if (!(ch instanceof Mob)) return false;

		//Boss / 小 Boss 不睡
		if (Char.hasProp(ch, Char.Property.BOSS) || Char.hasProp(ch, Char.Property.MINIBOSS)) {
			return false;
		}
		//精英怪不睡（它们已经有额外能力，再被睡会显得很突兀）
		if (!ch.buffs(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
				.ChampionEnemy.class).isEmpty()) {
			return false;
		}

		return Random.Int(100) < AGING_PCT;
	}

	/**
	 * END(121 中世纪骑士): 玩家护甲值倍率（+60%）。
	 * <p>调用点：{@code Armor.drRoll()} 或等效的护甲值计算处。
	 */
	public static float knightArmorMultiplier(Char ch) {
		if (!on(MEDIEVAL_KNIGHT) || ch == null) return 1f;
		if (!(ch instanceof Hero)) return 1f;
		return KNIGHT_ARMOR_MULT;
	}

	/**
	 * END(121 中世纪骑士): 玩家移动速度倍率（−50%）。
	 * <p>与 16/19/26 等速度规则一并作用。
	 */
	public static float knightSpeedMultiplier(Char ch) {
		if (!on(MEDIEVAL_KNIGHT) || ch == null) return 1f;
		if (!(ch instanceof Hero)) return 1f;
		return KNIGHT_SPEED_MULT;
	}

	/**
	 * END(103 弹幕地狱): 远程投射物数量。
	 *
	 * <p>原本 1 发，改为 3 发散射（有间隙可走位）。
	 * 与 19 风驰电掣联动时，**投射物速度单独计算**（不受 19 影响）。
	 *
	 * @return 投射物数量（无该挑战时为 1）
	 */
	public static int projectileCount() {
		return on(BULLET_HELL) ? BULLET_HELL_COUNT : 1;
	}

	/**
	 * END(140 枪枪爆头): 远程攻击是否应"必中最大值"。
	 *
	 * <p>玩家与目标距离 **>= 5 格** 时，远程攻击伤害必定为最大值；
	 * 距离小于 5 格时正常结算。
	 *
	 * @param attacker 攻击方
	 * @param target   目标
	 * @return true 表示本次伤害应取最大值
	 */
	public static boolean isHeadshot(Char attacker, Char target) {
		if (!on(HEADSHOT)) return false;
		if (attacker == null || target == null) return false;
		//只对玩家生效（倾向为"玩家收益"）
		if (!(attacker instanceof Hero)) return false;
		if (com.shatteredpixel.shatteredpixeldungeon.Dungeon.level == null) return false;

		int dist = com.shatteredpixel.shatteredpixeldungeon.Dungeon.level
				.distance(attacker.pos, target.pos);
		return dist >= HEADSHOT_RANGE;
	}

	//==================================================================
	//本批新增规则的实现
	//==================================================================

	/**
	 * END(87 盗贼鼠群): 怪物攻击命中后，5% 概率偷走玩家的金币。
	 *
	 * <p>原表："怪物攻击 5% 概率偷金币，击杀后返还双倍"。
	 *
	 * <p>偷走的钱**记在怪物身上**（见 {@code ThiefMark}），
	 * 击杀它时按双倍返还 —— 所以玩家有动力去追那只怪，
	 * 而不是单纯地挨罚。
	 *
	 * <p>调用点：{@code Char.attack()} 的命中结算处（攻击方是怪物时）。
	 *
	 * @param attacker 攻击方（怪物）
	 * @param enemy    目标（玩家）
	 */
	public static void onMobStealGold(Char attacker, Char enemy) {
		if (!on(THIEF_RATS) || attacker == null || enemy == null) return;
		if (!(attacker instanceof Mob)) return;
		if (!(enemy instanceof Hero)) return;
		if (Dungeon.gold <= 0) return;                     //没钱可偷
		//168 怪物地牢：把这条概率抬到至少 25%（未勾选 168 时原样使用 5%）
		if (Random.Int(100) >= bumpMobChance(THIEF_RATS_PCT)) return;

		//偷走 5% 的金币，至少 1 枚
		int stolen = Math.max(1, Dungeon.gold / 20);
		stolen = Math.min(stolen, Dungeon.gold);
		Dungeon.gold -= stolen;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ThiefMark mark =
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						attacker,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.ThiefMark.class);
		mark.stolenGold += stolen;

		safeLogW(msgArgs("thief_rats_stolen", stolen));
	}

	/**
	 * END(87 盗贼鼠群): 击杀带赃款的怪物时，双倍返还。
	 *
	 * <p>调用点：{@code Mob.die()} 或掉落结算处。
	 */
	public static void onThiefKilled(Char mob) {
		if (!on(THIEF_RATS) || mob == null) return;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ThiefMark mark =
				mob.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ThiefMark.class);
		if (mark == null || mark.stolenGold <= 0) return;

		int refund = mark.stolenGold * THIEF_RATS_REFUND;
		Dungeon.gold += refund;
		mark.stolenGold = 0;

		safeLogI(msgArgs("thief_rats_refund", refund));
	}

	/**
	 * END(142 无下限术士): 怪物受到远程攻击时是否完全免疫。
	 *
	 * <p>原表："怪物受到远程攻击时，13% 概率完全免疫该次伤害"。
	 *
	 * <p>调用点：{@code Char.damage()} 的最终拦截处（第 6 步之后）。
	 *
	 * @param target 受击方
	 * @param src    伤害来源
	 * @return true 表示本次伤害应被完全免除
	 */
	public static boolean rollRangedImmunity(Char target, Object src) {
		if (!on(NO_LOWER_LIMIT) || target == null) return false;
		if (!(target instanceof Mob)) return false;         //只对怪物生效
		if (!isRangedSource(src)) return false;
		//168 怪物地牢：把这条概率抬到至少 25%
		return Random.Int(100) < bumpMobChance(NO_LOWER_LIMIT_PCT);
	}

	/** 判断伤害来源是否属于"远程攻击"。 */
	private static boolean isRangedSource(Object src) {
		if (src == null) return false;
		return src instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
				.missiles.MissileWeapon
			|| src instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
				.SpiritBow;
	}

	/**
	 * END(143 吾为王者): Boss 的命中/闪避倍率。
	 *
	 * <p>原表："所有 Boss 的命中和闪避提升 20%"。
	 * 调用点：{@code Char.hit()} 里对攻防双方各查一次。
	 */
	public static float kingStatMultiplier(Char ch) {
		if (!on(I_AM_KING) || ch == null) return 1f;
		if (!Char.hasProp(ch, Char.Property.BOSS)) return 1f;
		return KING_STAT_MULT;
	}

	/**
	 * END(148 飞天神偷): 怪物是否应获得隐身。
	 *
	 * <p>原表："怪物有 13% 获得隐身"。
	 * 调用点：{@code Level.createMob()} 或怪物入场时（一次性判定）。
	 */
	public static boolean rollFlyingThiefInvisible(Char mob) {
		if (!on(FLYING_THIEF) || mob == null) return false;
		if (!(mob instanceof Mob)) return false;
		//Boss 不给隐身 —— 那会让 Boss 战无法进行
		if (Char.hasProp(mob, Char.Property.BOSS)) return false;
		//168 怪物地牢：把这条概率抬到至少 25%
		return Random.Int(100) < bumpMobChance(FLYING_THIEF_PCT);
	}

	/**
	 * END(153 恶魔地牢): 所有怪物是否应被视为恶魔类。
	 *
	 * <p>原表："所有怪物变为恶魔类（只是代码）" —— 即只改属性标记，
	 * 不改外观/数值。这样 158 神圣之力的"对恶魔额外伤害"才能生效。
	 */
	public static boolean demonsEnabled() {
		return on(DEMON_DUNGEON);
	}

	/**
	 * END(158 神圣之力): 对恶魔类目标的额外伤害倍率。
	 *
	 * <p>原表："对恶魔类造成额外伤害"。这里取 +30%。
	 * 与 153 恶魔地牢联动：勾了 153 之后所有怪都是恶魔，本条的收益最大化。
	 */
	public static float holyPowerDamageMultiplier(Char target) {
		if (!on(HOLY_POWER) || target == null) return 1f;
		if (!Char.hasProp(target, Char.Property.DEMONIC)) return 1f;
		return HOLY_POWER_MULT;
	}

	/**
	 * END(163 古代升级): 每 3 级提升 10% 的固定伤害。
	 *
	 * <p>原表："每达到 3 级，提升当前伤害 10% 的固定伤害，例如 15-20 → 16-22"。
	 *
	 * <p>理解：等级每满 3 级为一档，每档把伤害的**下限与上限各 +10%**。
	 * 例：15-20 且等级 3 时 → 16.5-22 → 取整 16-22（与原表例子一致，
	 * 说明是**先乘再取整**，而不是先取整再乘）。
	 *
	 * @param attacker 攻击方
	 * @param min      伤害下限
	 * @param max      伤害上限
	 * @return 调整后的 {min, max}
	 */
	public static int[] ancientUpgradeRange(Char attacker, int min, int max) {
		if (!on(ANCIENT_UPGRADE) || attacker == null) return new int[]{min, max};
		//等级是 Hero 的字段（Char 本身没有 lvl），必须先判类型再取
		if (!(attacker instanceof Hero)) return new int[]{min, max};

		int lvl = ((Hero) attacker).lvl;
		if (lvl < ANCIENT_STEP_LEVEL) return new int[]{min, max};

		int steps = lvl / ANCIENT_STEP_LEVEL;               //每 3 级一档
		float mult = 1f + ANCIENT_PER_STEP * steps;

		return new int[]{
				Math.max(min, Math.round(min * mult)),
				Math.max(max, Math.round(max * mult))
		};
	}

	/**
	 * END(168 怪物地牢): 怪物相关概率统一提升到 25%。
	 *
	 * <p>原表："所有怪物相关概率提升至 25%"。
	 *
	 * <p>实现：提供一个统一的"概率提升"入口，各条怪物概率规则
	 * 通过 {@link #bumpMobChance(int)} 把自己的概率抬到至少 25%。
	 * 未勾选 168 时原样返回。
	 *
	 * @param basePct 该规则原本的概率（百分比）
	 * @return 提升后的概率
	 */
	public static int bumpMobChance(int basePct) {
		if (!on(MONSTER_DUNGEON)) return basePct;
		return Math.max(basePct, MONSTER_DUNGEON_PCT);
	}
	/** 56 装备绑定：装备获得后自动绑定。 */
	public static final int EQUIPMENT_BINDING = 56;
	/** 108 装备觉醒：武器击杀 50 / 护甲格挡 100 后觉醒。 */
	public static final int AWAKENING         = 108;

	/** END(56 装备绑定): 是否启用装备绑定。 */
	public static boolean equipmentBindingEnabled() {
		return on(EQUIPMENT_BINDING);
	}

	/** 108 装备觉醒：武器所需击杀数。 */
	public static final int AWAKEN_KILLS   = 50;
	/** 108 装备觉醒：护甲所需格挡数。 */
	public static final int AWAKEN_BLOCKS  = 100;

	/** END(108 装备觉醒): 是否启用。 */
	public static boolean awakeningEnabled() {
		return on(AWAKENING);
	}

	/**
	 * END(108 装备觉醒): 某件装备是否**不能**觉醒。
	 *
	 * <p>原表限制："每件一次；残缺/诅咒不能觉醒"。
	 * 这里判断 57 残缺 / 59 诅咒 两种词缀。
	 */
	public static boolean cannotAwaken(com.shatteredpixel.shatteredpixeldungeon.items
			.weapon.Weapon w) {
		if (w == null || w.enchantment == null) return false;
		return w.enchantment instanceof com.shatteredpixel.shatteredpixeldungeon.items
				.weapon.enchantments.Flawed
			|| w.enchantment.curse();
	}

	/** END(108 装备觉醒): 护甲版本的同类判断。 */
	public static boolean cannotAwaken(com.shatteredpixel.shatteredpixeldungeon.items
			.armor.Armor a) {
		if (a == null || a.glyph == null) return false;
		return a.glyph.curse();
	}
	/**
	 * END(108 装备觉醒): 玩家用武器击杀怪物时，给当前武器累计一次。
	 *
	 * <p>调用点：{@code Mob.die()}。
	 *
	 * <p>只认"玩家手持武器造成的击杀"：法术、陷阱、环境致死都不算，
	 * 否则玩家挂机让陷阱杀怪也能刷满 50 只。
	 *
	 * @param killed 被杀的怪物
	 * @param cause  致死原因（通常是伤害来源对象）
	 */
	public static void onMobKilledForAwakening(Char killed, Object cause) {
		if (!on(AWAKENING) || Dungeon.hero == null) return;

		//致死来源必须是玩家（法术也算玩家的，但法术不累加武器计数）
		Char killer = (cause instanceof Char) ? (Char) cause : null;

		com.shatteredpixel.shatteredpixeldungeon.items.Item w =
				Dungeon.hero.belongings.attackingWeapon();
		if (!(w instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)) {
			return;
		}
		com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon weapon =
				(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon) w;

		//只有玩家直接造成的击杀才计数
		if (killer != Dungeon.hero) return;

		weapon.awakenKillCount++;
		if (weapon.awakenKillCount >= AWAKEN_KILLS && !weapon.awakenedOnce) {
			awakenWeapon(weapon);
		}
	}

	/** END(108 装备觉醒): 给武器觉醒 —— 补一条随机附魔。 */
	private static void awakenWeapon(
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon weapon) {
		if (cannotAwaken(weapon)) return;          //残缺/诅咒不能觉醒
		weapon.awakenedOnce = true;

		if (weapon.enchantment == null) {
			//没有附魔 → 直接给一条随机正面附魔
			weapon.enchant();
		} else {
			//已有附魔 → 换一条（"每件一次"的意思是只能觉醒一次，不是不能换）
			weapon.enchant();
		}
		weapon.identify();

		safeLogI(msg("awakening_done"));
	}

	/** END(108 装备觉醒): 护甲格挡计数。 */
	public static void onArmorBlockForAwakening(
			com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor armor) {
		if (!on(AWAKENING) || armor == null) return;
		armor.awakenBlockCount++;
		if (armor.awakenBlockCount >= AWAKEN_BLOCKS && !armor.awakenedOnce) {
			if (!cannotAwaken(armor)) {
				armor.awakenedOnce = true;
				if (armor.glyph == null) {
					armor.inscribe();
				} else {
					armor.inscribe();
				}
				armor.identify();
				safeLogI(msg("awakening_done"));
			}
		}
	}
	/** 157 附魔扩充：向随机附魔池加入「锋利」「力量」。 */
	public static final int ENCHANT_EXPANSION = 157;

	//==== 格林系列（125/133/136 的专属装备）====

	/** 125 格林之器：银色短铳 + 兔子怀表。 */
	public static final int GRIMM_WEAPON   = 125;
	/** 133 格林之器2：神天使双剑。 */
	public static final int GRIMM_WEAPON_2 = 133;
	/** 136 格林之器3：怨恨之剑 + 勇剑。 */
	public static final int GRIMM_WEAPON_3 = 136;

	/** 129 心爱的少女：童话残片 + 999 层的爱丽丝。 */
	public static final int BELOVED_GIRL   = 129;

	/** 127 格林之戒：黑兔戒指。 */
	public static final int GRIMM_RING     = 127;
	/** 128 格林之术：镇魂歌。 */
	public static final int GRIMM_ART      = 128;
	/** 134 黄金蜂蜜酒。 */
	public static final int GOLDEN_MEAD    = 134;

	/** 126 格林之心：黑之魂系统。 */
	public static final int GRIMM_HEART    = 126;

	/** END(157 附魔扩充): 是否向随机附魔池加入新附魔。 */
	public static boolean enchantExpansionEnabled() {
		return on(ENCHANT_EXPANSION);
	}
	//==================================================================
	//地图类：3 区域错位 / 4 精英迁徙 / 5 怪物入侵
	//==================================================================

	/** 3 区域错位。 */
	public static final int REGION_SHIFT     = 3;
	/** 4 精英迁徙。 */
	public static final int ELITE_MIGRATION  = 4;
	/** 5 怪物入侵。 */
	public static final int MONSTER_INVASION = 5;

	/** 5 怪物入侵：每层混入的外区域怪物数量。 */
	private static final int INVASION_COUNT = 2;

	/** END(5 怪物入侵): 本层应混入几只外区域怪物。 */
	public static int invasionCount(int depth) {
		return on(MONSTER_INVASION) ? INVASION_COUNT : 0;
	}

	/**
	 * END(5 怪物入侵): 从**其它区域**的普通怪里随机抽一只。
	 *
	 * <p>做法：随机挑一个不属于当前区域、且在主线 1-25 范围内的层号，
	 * 取那一层的普通怪轮换表，再随机抽一只。
	 *
	 * @param currentDepth 当前层（用于判断"哪些区域是别的区域"）
	 * @return 怪物类；取不到时返回 null（调用方跳过）
	 */
	public static Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.mobs
			.Mob> pickInvader(int currentDepth) {
		if (!on(MONSTER_INVASION)) return null;
		if (currentDepth < 1 || currentDepth > 25) return null;

		int curRegion = (currentDepth - 1) / 5;        //0..4

		//最多试 10 次，避免某些层取不到表时死循环
		for (int tries = 0; tries < 10; tries++) {
			int r = Random.Int(5);
			if (r == curRegion) continue;              //必须来自**别的**区域

			int probeDepth = r * 5 + 1 + Random.Int(4); //该区域的某个普通层
			try {
				java.util.ArrayList<Class<? extends com.shatteredpixel.shatteredpixeldungeon
						.actors.mobs.Mob>> pool =
						com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner
								.standardMobRotation(probeDepth);
				if (pool != null && !pool.isEmpty()) {
					return pool.get(Random.Int(pool.size()));
				}
			} catch (Throwable ignored) {
				//取不到就换一个区域再试
			}
		}
		return null;
	}

	/**
	 * END(4 精英迁徙): 精英怪出现时，是否放宽"只在本区域"的限制。
	 *
	 * <p>原表："精英怪可出现在原本不属于自己的区域"。
	 *
	 * <p>实现说明：本 fork 的精英（ChampionEnemy）判定是**逐怪掷骰**的，
	 * 本身不区分区域 —— 任何怪都可能成为精英。所以这条规则的
	 * 实际含义是"**提高**精英出现的广度"：勾选后，连那些
	 * 通常被排除在精英体系之外的怪（例如召唤物、部分特殊怪）
	 * 也能成为精英。
	 *
	 * <p>因此这里提供一个"是否放宽限制"的开关，由 {@code ChampionEnemy} 查询。
	 */
	public static boolean eliteMigrationEnabled() {
		return on(ELITE_MIGRATION);
	}

	/**
	 * END(3 区域错位): 地图生成的"生态"是否应向邻区偏移。
	 *
	 * <p>原表："不同区域部分地图生态、怪物或生成内容错位"。
	 *
	 * <p>实现：提供一个**相邻区域**的层号，供地图生成时参考。
	 * 返回 0 表示不启用。
	 *
	 * <p>与 1 牢地碎破的区别：碎破是"整个区域对调"（1↔5、2↔4），
	 * 本条是"局部错位"（与相邻区域混合），程度轻得多。
	 */
	public static int regionShiftReference(int depth) {
		if (!on(REGION_SHIFT)) return 0;
		if (depth < 1 || depth > 25) return 0;
		int curRegion = (depth - 1) / 5;

		//向相邻区域偏移：随机选左邻或右邻（边界时只能选一侧）
		int target;
		if (curRegion == 0)      target = 1;
		else if (curRegion == 4) target = 3;
		else                     target = Random.Int(2) == 0 ? curRegion - 1 : curRegion + 1;

		return target * 5 + 1;      //该区域的第一层，作为"生态参考层"
	}
	//==================================================================
	//6 完整地牢：每区 9 普通层 + 1 Boss 层（共 50 层）
	//==================================================================

	/** 6 完整地牢。 */
	public static final int FULL_DUNGEON = 6;

	/** 完整地牢下每区的层数（9 普通 + 1 Boss）。 */
	public static final int FULL_REGION_SIZE = 10;

	/** END(6 完整地牢): 是否启用加长地牢。 */
	public static boolean fullDungeonEnabled() {
		return on(FULL_DUNGEON);
	}

	/**
	 * END(6 完整地牢): 把"实际深度"映射成"用于取资源的原版深度"。
	 *
	 * <h3>为什么要映射</h3>
	 * 加长后主线有 50 层，但游戏里所有资源表（怪物轮换、掉落、
	 * 商店、任务、Boss 房）都是按 **1-25 层** 设计的。
	 * 与其把几十张表都改一遍，不如把"实际深度"折算成等价的原版深度，
	 * 让它们照常工作。
	 *
	 * <h3>映射规则</h3>
	 * <pre>
	 *   实际 1-10F  (1区) -> 原版 1-5F
	 *   实际 11-20F (2区) -> 原版 6-10F
	 *   实际 21-30F (3区) -> 原版 11-15F
	 *   实际 31-40F (4区) -> 原版 16-20F
	 *   实际 41-50F (5区) -> 原版 21-25F
	 * </pre>
	 * 区内按比例折算：实际区内偏移 0..9 → 原版区内偏移 0..4。
	 * **第 10 层（偏移 9）永远映射到该区的 Boss 层**（原版偏移 4）。
	 *
	 * <p>未勾选 6 时原样返回 {@code depth}。
	 */
	public static int fullDungeonMappedDepth(int depth) {
		if (!on(FULL_DUNGEON)) return depth;
		if (depth < 1 || depth > 50) return depth;

		int region = (depth - 1) / FULL_REGION_SIZE;   //0..4
		int within = (depth - 1) % FULL_REGION_SIZE;   //0..9

		//==== END(修复·映射越界): 9 个普通层要映射到 4 个普通层 ====
		//原版每区 5 层：偏移 0-3 是普通层，偏移 4 是 Boss。
		//加长后每区 10 层：偏移 0-8 是普通层（9 个），偏移 9 是 Boss。
		//
		//所以普通层是"9 -> 4"的压缩映射，即 within * 3 / 8：
		//    0,1,2 -> 0   3,4,5 -> 1   6,7 -> 2   8 -> 3
		//**不能**用 within * 4 / 8 —— 那会让 within=8 也得 4，
		//与 Boss 层的 4 撞车，region=4 时算出 24+4+1 = 25，
		//resourceSegment 就成了 5（越界），资源发放随之失衡。
		int mappedWithin;
		if (within >= FULL_REGION_SIZE - 1) {
			mappedWithin = 4;                              //Boss 层
		} else {
			mappedWithin = within * 3 / (FULL_REGION_SIZE - 2);   //0..8 -> 0..3
		}

		return region * 5 + mappedWithin + 1;
	}

	/** END(6 完整地牢): 给定实际深度，返回它所在的区域（1..5）。 */
	public static int fullDungeonRegion(int depth) {
		if (depth < 1) return 1;
		return Math.min(5, (depth - 1) / FULL_REGION_SIZE + 1);
	}

	/** END(6 完整地牢): 该实际深度是否是 Boss 层。 */
	public static boolean fullDungeonIsBossLevel(int depth) {
		if (!on(FULL_DUNGEON)) return false;
		if (depth < 1 || depth > 50) return false;
		return (depth - 1) % FULL_REGION_SIZE == FULL_REGION_SIZE - 1;
	}

	/**
	 * END(6 完整地牢): 主线最深一层。
	 *
	 * <p>原版是 25，勾选 6 后是 50。Boss 层判定、结局触发等都要用它，
	 * 所以单独抽一个方法。
	 */
	public static int maxMainDepth() {
		return on(FULL_DUNGEON) ? 50 : 25;
	}
	//==================================================================
	//150 淹没地牢 / 154 废弃地牢（照搬原版 MossyClump 的 feeling 机制）
	//==================================================================

	/** 150 淹没地牢：每一层都是水域生态。 */
	public static final int FLOODED_DUNGEON  = 150;
	/** 154 废弃地牢：每一层都是草木生态。 */
	public static final int ABANDONED_DUNGEON = 154;

	/** END(150 淹没地牢): 是否强制整层为水域生态。 */
	public static boolean floodedEnabled() {
		return on(FLOODED_DUNGEON);
	}

	/** END(154 废弃地牢): 是否强制整层为草木生态。 */
	public static boolean abandonedEnabled() {
		return on(ABANDONED_DUNGEON);
	}

	/** 150 淹没地牢：水中生成幻影食人鱼的概率。 */
	private static final int FLOODED_PIRANHA_PCT = 3;    //END(修订): 20% -> 3%（实测过多）

	public static int floodedPiranhaChance() {
		return on(FLOODED_DUNGEON) ? FLOODED_PIRANHA_PCT : 0;
	}

	/**
	 * 154 废弃地牢：踩到植物时被缠绕的概率与回合数。
	 *
	 * <p>END(修订): 概率由 20% 下调为 **3%**（文档所有者要求）。
	 * 整层都是植被，踩到的机会极多，20% 会让玩家几乎寸步难行。
	 */
	private static final int ABANDONED_TANGLE_PCT = 3;
	private static final float ABANDONED_TANGLE_TURNS = 3f;

	public static int abandonedTangleChance() {
		return on(ABANDONED_DUNGEON) ? ABANDONED_TANGLE_PCT : 0;
	}

	public static float abandonedTangleTurns() {
		return ABANDONED_TANGLE_TURNS;
	}
	/**
	 * END(适配 6 完整地牢): Boss 层之间的间隔。
	 *
	 * <p>原版是每 5 层一个 Boss（5/10/15/20/25）；
	 * 勾选 6 后每 10 层一个（10/20/30/40/50）。
	 * 2 楼层混乱的排列、7 跳级生的区域划分都按它走。
	 */
	public static int bossInterval() {
		return on(FULL_DUNGEON) ? FULL_REGION_SIZE : 5;
	}
	//==================================================================
	//6 完整地牢：资源分段适配
	//==================================================================

	/**
	 * END(适配 6 完整地牢): "资源层段号"（0-based）。
	 *
	 * <p>原版大量代码用 {@code depth / 5} 来算"这是第几个区域的第几段"，
	 * 用来决定每段发多少力量药水、升级卷轴、附魔石（见
	 * {@code Dungeon.posNeeded()} / {@code souNeeded()} / {@code asNeeded()}）。
	 *
	 * <p>勾选 6 后主线是 50 层，若继续用 {@code depth/5}：
	 * <ul>
	 *   <li>实际 41-50F 会算出段号 8-10，而资源表只有 5 段 → **发放失衡**</li>
	 *   <li>每段只有 5 层，但玩家要走 10 层 → **资源密度减半**</li>
	 * </ul>
	 *
	 * <p>所以统一改成：先折算成原版深度，再除以 5。
	 * 未勾选 6 时 {@code fullDungeonMappedDepth} 原样返回，结果与 {@code depth/5} 完全相同。
	 */
	public static int resourceSegment(int depth) {
		return fullDungeonMappedDepth(depth) / 5;
	}

	/**
	 * END(适配 6 完整地牢): "在资源段内的第几层"（0-based）。
	 *
	 * <p>与 {@link #resourceSegment} 配套：原版是 {@code depth % 5}，
	 * 用来算"这一段还剩几层可以发资源"。
	 */
	public static int resourceFloorInSegment(int depth) {
		return fullDungeonMappedDepth(depth) % 5;
	}

	/**
	 * END(适配 6 完整地牢): "这是第几个区域"（1-based），用于 43/79 等按区域触发的规则。
	 *
	 * <p>原版写的是 {@code depth % 5 == 1}（每 5 层一次）。
	 * 勾选 6 后应当变成**每 10 层一次**，所以用 {@link #bossInterval()} 推导。
	 */
	public static boolean isRegionStart(int depth) {
		int interval = bossInterval();
		return depth > 1 && (depth - 1) % interval == 0;
	}
	/** END(129 心爱的少女): 是否启用童话残片系统。 */
	public static boolean belovedGirlEnabled() {
		return on(BELOVED_GIRL);
	}
	/**
	 * END(129): 999 层的"爱丽丝领域"是否已就绪。
	 *
	 * <p>已实装：{@code AliceRealm}（虚空层）+ {@code Alice}（NPC）+ 专属 BGM。
	 * 见 {@code AliceRealm}、{@code Alice}、{@code Assets.Music.GRIMM_ALICE}。
	 */
	public static boolean aliceRealmReady() {
		return true;
	}
	/**
	 * END(修复 119 怪物浪潮): "同一房间追加第二只怪"的判定阈值（0..4）。
	 *
	 * <p>原版写死 {@code Random.Int(4) == 0}，即 25%。
	 * 勾选 119 后如果仍是 25%，玩家感觉不到"浪潮" ——
	 * 因为绝大多数房间还是只有 1 只怪。
	 *
	 * <p>所以这里返回一个**阈值**，调用方用
	 * {@code Random.Int(4) < threshold} 判定：
	 * <ul>
	 *   <li>未勾选：返回 1（25%，等价原版 {@code == 0}）</li>
	 *   <li>119：返回 3（75%）</li>
	 *   <li>30 人口密集：返回 2（50%）</li>
	 *   <li>两条同开：返回 4（100%，必定 2 只）</li>
	 * </ul>
	 *
	 * <p>为什么用阈值而不是概率：{@code Random.Int(n)} 的调用次数保持固定，
	 * 不会因为勾选状态而改变关卡生成的随机序列。
	 */
	public static int extraMobPerRoomChance() {
		int threshold = 1;                        //25%
		if (on(CROWDED))      threshold++;        //50%
		if (on(MONSTER_WAVE)) threshold += 2;     //75%（单独）/ 100%（叠加）
		return Math.min(4, threshold);
	}
	//==================================================================
	//本批新增：8 / 15 / 20 / 29
	//==================================================================

	/** 15 首领护卫：Boss 战额外精英数量。 */
	public static final int BOSS_GUARD      = 15;
	/** 20 等我启动：对同一目标的连击递增。 */
	public static final int MOMENTUM        = 20;
	/** 29 雇佣童工：13% 怪物变成"童工"。 */
	public static final int CHILD_LABOR     = 29;

	//---- 15 首领护卫 ----

	/** 每个 Boss 战额外生成的精英护卫数量。 */
	private static final int BOSS_GUARD_COUNT = 3;

	/** END(15): Boss 战应生成几个精英护卫。 */
	public static int bossGuardCount() {
		return on(BOSS_GUARD) ? BOSS_GUARD_COUNT : 0;
	}

	//---- 20 等我启动 ----

	/**
	 * END(20 等我启动): 对同一目标的连击伤害。
	 *
	 * <p>原表："对同一目标伤害：第一次 20%，第二次 50%，第三次及以后 110%"
	 *
	 * <p>第 1、2 次是**惩罚**（打得很轻），第 3 次开始**奖励**（+10%）。
	 * 所以这条规则逼玩家"咬住一个目标不放"，而不是四处点血。
	 *
	 * <p>用 {@code QuickSlotButton.lastTarget} 之外的独立记录：
	 * 那个是"快速栏选中的目标"，语义不同，会被其它逻辑改写。
	 * 这里单独存"上一次打的谁 + 连了几次"。
	 */
	public static float applyMomentumDamage(Char attacker, Char target, float dmg) {
		if (!on(MOMENTUM) || attacker == null || target == null) return dmg;

		//每次命中都推进计数（无论伤害是否被减到 0）
		int stacks = bumpMomentum(attacker, target);

		float mult;
		if (stacks <= 1)      mult = 0.20f;   //第一次
		else if (stacks == 2) mult = 0.50f;   //第二次
		else                  mult = 1.10f;   //第三次及以后

		return Math.max(1f, dmg * mult);
	}

	/**
	 * END(20): 连击计数（以"攻击方 + 目标"为键）。
	 *
	 * <p>用静态 Map 而不是 buff：本规则只关心"上一发打的是谁"，
	 * 不需要跨存档保留（读档后从第一次重新开始，符合直觉）。
	 */
	private static final java.util.HashMap<Char, Char> momentumTarget =
			new java.util.HashMap<>();
	private static final java.util.HashMap<Char, Integer> momentumCount =
			new java.util.HashMap<>();

	private static int bumpMomentum(Char attacker, Char target) {
		Char last = momentumTarget.get(attacker);
		int n = momentumCount.containsKey(attacker) ? momentumCount.get(attacker) : 0;

		if (last == target) {
			n++;
		} else {
			n = 1;                       //换了目标，从第一次重新算
		}

		momentumTarget.put(attacker, target);
		momentumCount.put(attacker, n);
		return n;
	}

	/** END(20): 当前连击数（供测试与界面显示）。 */
	public static int momentumStacks(Char attacker) {
		return momentumCount.containsKey(attacker) ? momentumCount.get(attacker) : 0;
	}

	//---- 29 雇佣童工 ----

	/** 童工出现概率（%）。 */
	private static final int CHILD_LABOR_PCT = 13;
	/** 童工的生命倍率。 */
	private static final float CHILD_LABOR_HP_MULT = 0.20f;

	/** END(29): 该怪物是否应变成"童工"。 */
	public static boolean rollChildLabor() {
		if (!on(CHILD_LABOR)) return false;
		return Random.Int(100) < CHILD_LABOR_PCT;
	}

	/** END(29): 童工的生命倍率（只有 20%）。 */
	public static float childLaborHpMultiplier() {
		return CHILD_LABOR_HP_MULT;
	}

	/** END(29): 童工的移速倍率（×2）。 */
	public static float childLaborSpeedMultiplier() {
		return 2.0f;
	}
	/**
	 * END(15 首领护卫): 在 Boss 层生成若干个精英护卫。
	 *
	 * <p>做法：取该层**本区域**的普通怪作为护卫模板，给它挂一个
	 * 随机 {@code ChampionEnemy}（精英）buff —— 与 116/14 那套体系一致，
	 * 不另造一种"护卫怪"。
	 *
	 * <p>生成位置用 {@code randomRespawnCell()}，它会避开玩家视野与
	 * 不可通行的格子；拿不到位置就少生成一只，不会报错。
	 *
	 * <p>调用点：{@code Dungeon.newLevel()} —— 每层只跑一次。
	 */
	public static void spawnBossGuards(com.shatteredpixel.shatteredpixeldungeon.levels.Level level, int count) {
		if (level == null || count <= 0) return;

		for (int i = 0; i < count; i++) {
			try {
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob guard = level.createMob();
				if (guard == null) continue;

				int cell = level.randomRespawnCell(guard);
				if (cell == -1) continue;              //放不下就少一只

				guard.pos = cell;
				guard.state = guard.WANDERING;

				//挂一个随机精英 buff（与原版精英体系同一套）
				Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChampionEnemy> cls = pickChampionClass();
				if (cls != null) {
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
							guard, cls);
				}

				level.mobs.add(guard);
			} catch (Throwable t) {
				//生成失败不影响关卡 —— 那只是少一个护卫
			}
		}
	}

	/** END(15): 随机挑一种精英类型（与原版 ChampionEnemy 的六种一致）。 */
	private static Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs
			.ChampionEnemy> pickChampionClass() {
		switch (Random.Int(6)) {
			case 0: default:
				return com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChampionEnemy.Blazing.class;
			case 1:
				return com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChampionEnemy.Projecting.class;
			case 2:
				return com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChampionEnemy.AntiMagic.class;
			case 3:
				return com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChampionEnemy.Giant.class;
			case 4:
				return com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChampionEnemy.Blessed.class;
			case 5:
				return com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChampionEnemy.Growing.class;
		}
	}
	//==================================================================
	//本批新增：8 / 21 / 68 / 103
	//==================================================================

	/** 8 混乱：战斗中随机产生 buff。 */
	public static final int CHAOS           = 8;
	/** 21 法术连击：施法后 13% 追加一次。 */
	public static final int SPELL_COMBO     = 21;
	/** 68 极端状态：生命 10%、攻击与命中翻倍。 */
	public static final int EXTREME_STATE   = 68;
	//（103 弹幕地狱的常量定义在文件上方，本批未重复添加）

	//---- 68 极端状态 ----

	/** 生命降低到原来的 10%。 */
	public static final float EXTREME_HP_MULT = 0.10f;
	/** 生命下限（原表："最低 10"）。 */
	public static final int EXTREME_MIN_HP = 10;
	/** 攻击与命中的倍率。 */
	public static final float EXTREME_OFFENSE_MULT = 2.0f;

	/**
	 * END(68 极端状态): 开局压低生命、翻倍攻击与命中。
	 *
	 * <p>原表："生命降低 90%（最低 10），攻击翻倍，命中翻倍"
	 *
	 * <p>调用点：{@code Dungeon.init()} —— 在 {@code initHero} 之后、
	 * 发放开局装备之前。必须在这里而不是每次升级时算，
	 * 否则"最低 10"这条会被后续升级反复抬起来。
	 *
	 * <p>与 104 命悬一线兼容：那条是"隐藏血量数字 + 13% 保命"，
	 * 与本条互不干扰（一个改上限，一个改显示与致命伤判定）。
	 */
	public static void applyExtremeState(com.shatteredpixel.shatteredpixeldungeon.actors
			.hero.Hero hero) {
		if (!on(EXTREME_STATE) || hero == null) return;

		int newHT = Math.max(EXTREME_MIN_HP,
				Math.round(hero.HT * EXTREME_HP_MULT));

		hero.HT = newHT;
		hero.HP = newHT;

		//攻击与命中翻倍：用 Hero 提供的公开方法（attackSkill 是 private 的）
		hero.grimmBoostAccuracyAndEvasion();   //+1 命中 / +1 闪避
		//再补一次命中，凑成"命中翻倍"的体感（+1 之后大致翻倍）
		hero.grimmBoostAccuracyAndEvasion();
	}

	//---- 21 法术连击 ----

	/** 追加施法的概率（%）。 */
	private static final int SPELL_COMBO_PCT = 13;

	/**
	 * END(21 法术连击): 本次施法后是否应追加一次。
	 *
	 * <p>原表："施法后 13% 概率再次施法，不消耗新资源，**单次最多追加一次**"
	 *
	 * <p>"单次最多追加一次"用施法者身上的标记实现：
	 * 追加施法时打上标记，同一个标记存在期间不再触发，
	 * 避免连锁反应变成无限施法。
	 *
	 * @param caster 施法者
	 * @return true 表示应追加一次施法
	 */
	public static boolean rollSpellCombo(Char caster) {
		if (!on(SPELL_COMBO) || caster == null) return false;

		//已经追加过了 → 不再触发
		if (caster.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
				.SpellComboMark.class) != null) {
			return false;
		}
		if (Random.Int(100) >= SPELL_COMBO_PCT) return false;

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
				caster,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.SpellComboMark.class,
				2f);   //只活一小会儿，够挡住这次连锁即可
		return true;
	}


	//---- 8 混乱 ----

	/** 每次命中触发随机 buff 的概率（%）。 */
	private static final int CHAOS_PCT = 25;

	/**
	 * END(8 混乱): 命中时给双方挂一个随机 buff。
	 *
	 * <p>原表："战斗过程中产生随机 buff" —— 没指定给谁。
	 *
	 * <p>设计：**给防守方**（被打的那个人）。理由是"混乱"描述的是一场
	 * 失控的战斗，谁挨打谁身上出状况最直观；而且这样玩家和怪物都会中招，
	 * 符合"双刃剑"的倾向。
	 */
	public static void rollChaosBuff(Char target) {
		if (!on(CHAOS) || target == null) return;
		if (Random.Int(100) >= CHAOS_PCT) return;

		int roll = Random.Int(6);
		try {
			switch (roll) {
				case 0:
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
							target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
									.Haste.class, 5f);
					break;
				case 1:
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
							target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
									.Weakness.class, 5f);
					break;
				case 2:
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
							target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
									.Chill.class, 5f);
					break;
				case 3:
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
							target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
									.Invisibility.class, 3f);
					break;
				case 4:
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
							target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
									.Blindness.class, 3f);
					break;
				default:
					//Barkskin 不是 FlavourBuff（没有带时长的 prolong 重载），
					//所以这里用 Bless 代替 —— 同样是正面 buff，语义也通（"走运"）。
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
							target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
									.Bless.class, 5f);
					break;
			}
		} catch (Throwable t) {
			//挂不上就算了 —— 这条规则纯属"氛围"，不该把游戏拖崩
		}
	}
	//==================================================================
	//幽灵链：73 神秘复苏 / 77 亡灵法师 / 86 复仇之魂
	//==================================================================

	/** 73 神秘复苏：13% 生成幽灵。 */
	public static final int MYSTIC_REVIVAL  = 73;
	/** 77 亡灵法师：怪物死亡后 20% 变成幽灵。 */
	public static final int NECROMANCER     = 77;
	/** 86 复仇之魂：被击杀怪物 10% 在下一层以幽灵形式复仇。 */
	public static final int VENGEFUL_SOUL   = 86;

	/** 73 的概率（%）。 */
	private static final int MYSTIC_PCT     = 13;
	/** 77 的概率（%）。 */
	private static final int NECRO_PCT      = 20;
	/** 86 的概率（%）。 */
	private static final int VENGEFUL_PCT   = 10;

	/**
	 * END(73 神秘复苏): 该怪物生成时是否应变成幽灵。
	 *
	 * <p>原表："13% 生成幽灵"。
	 *
	 * <p>调用点：{@code Mob.onAdd()}。Boss / 小 Boss / 已经是幽灵的不参与。
	 */
	public static boolean rollMysticRevival(Mob mob) {
		if (!on(MYSTIC_REVIVAL) || mob == null) return false;
		if (mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith) {
			return false;                 //已经是幽灵
		}
		if (Char.hasProp(mob, Char.Property.BOSS)
				|| Char.hasProp(mob, Char.Property.MINIBOSS)) {
			return false;
		}
		return Random.Int(100) < MYSTIC_PCT;
	}

	/**
	 * END(77 亡灵法师): 怪物死亡时是否应留下一个幽灵。
	 *
	 * <p>原表："怪物死亡后 20% 变成幽灵"。
	 *
	 * <p>调用点：{@code Mob.die()}。
	 *
	 * <p>Boss 不参与 —— 打死 Boss 后爬起来一只幽灵毫无意义。
	 */
	public static boolean rollNecromancer(Mob mob) {
		if (!on(NECROMANCER) || mob == null) return false;
		if (mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith) {
			return false;
		}
		if (Char.hasProp(mob, Char.Property.BOSS)
				|| Char.hasProp(mob, Char.Property.MINIBOSS)) {
			return false;
		}
		return Random.Int(100) < NECRO_PCT;
	}

	/** END(73/77): 在指定格生成一只幽灵。 */
	public static void spawnWraithAt(int cell) {
		if (Dungeon.level == null) return;
		try {
			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith w =
					new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith();
			w.pos = cell;
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(w);
		} catch (Throwable t) {
			//生成失败就算了 —— 不该把游戏拖崩
		}
	}

	//---- 86 复仇之魂 ----

	/**
	 * END(86 复仇之魂): 本层待复仇的幽灵数量。
	 *
	 * <p>原表："被击杀怪物 10% 概率在**下一层**以幽灵形式复仇"。
	 *
	 * <p>实现：死亡时若判定成功，就把"欠的幽灵"记进一个待处理计数；
	 * 进入下一层时（{@code Level.create()}）取出来生成。
	 *
	 * <p>计数存在静态字段里，**不跨存档** —— 换局时由
	 * {@link #clearVengefulSouls()} 清空。
	 */
	private static int pendingVengefulSouls = 0;

	/** END(86): 判定并累积"下层的复仇幽灵"。 */
	public static void rollVengefulSoul(Mob mob) {
		if (!on(VENGEFUL_SOUL) || mob == null) return;
		if (mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith) {
			return;
		}
		if (Char.hasProp(mob, Char.Property.BOSS)
				|| Char.hasProp(mob, Char.Property.MINIBOSS)) {
			return;
		}
		if (Random.Int(100) >= VENGEFUL_PCT) return;

		//上限：每层最多 5 只，避免雪球效应
		if (pendingVengefulSouls < 5) pendingVengefulSouls++;
	}

	/** END(86): 取出并清空待处理的复仇幽灵数（进新层时调用）。 */
	public static int consumeVengefulSouls() {
		int n = pendingVengefulSouls;
		pendingVengefulSouls = 0;
		return on(VENGEFUL_SOUL) ? n : 0;
	}

	/** END(86): 换局时清空。 */
	public static void clearVengefulSouls() {
		pendingVengefulSouls = 0;
	}

	/** END(86): 当前待处理数量（供测试）。 */
	public static int pendingVengefulSouls() {
		return pendingVengefulSouls;
	}
	//==================================================================
	//76 原始状态：非远程怪物扔石头
	//==================================================================

	/** 76 原始状态。 */
	public static final int PRIMITIVE_STATE = 76;

	/** 扔石头的最大距离（格）。 */
	private static final int ROCK_RANGE = 6;

	/**
	 * END(76 原始状态): 这只怪物是否应该"扔石头"。
	 *
	 * <p>原表："非远程怪物可扔石头进行远程攻击"
	 *
	 * <p>排除条件：
	 * <ul>
	 *   <li>已经是远程怪（本来就能打到，不需要）</li>
	 *   <li>Boss / 小 Boss（它们有自己的远程手段）</li>
	 *   <li>距离超过 {@link #ROCK_RANGE} 格（太远了扔不到）</li>
	 * </ul>
	 */
	public static boolean canThrowRock(Mob mob, Char enemy) {
		if (!on(PRIMITIVE_STATE) || mob == null || enemy == null) return false;

		//远程怪排除 —— 原版没有统一的"远程怪基类"，
		//所以靠 isRangedMobByClass 的名单判断（见其说明）。
		if (isRangedMobByClass(mob)) return false;

		//Boss 排除
		if (Char.hasProp(mob, Char.Property.BOSS)
				|| Char.hasProp(mob, Char.Property.MINIBOSS)) return false;

		//距离
		if (Dungeon.level == null) return false;
		int dist = Dungeon.level.distance(mob.pos, enemy.pos);
		return dist > 1 && dist <= ROCK_RANGE;
	}

	/**
	 * END(76): 靠类名判断是不是远程怪。
	 *
	 * <p>原版的远程怪并没有统一的基类或接口（GnollGeomancer 自己实现投石、
	 * DM-100 用光束、Tengu 用飞刀…），所以只能按**已知会远程攻击的类**排除。
	 * 这份名单不求完备 —— 漏掉一两个只会让那只怪多一个远程手段，
	 * 不会破坏游戏。
	 */
	private static boolean isRangedMobByClass(Mob mob) {
		String n = mob.getClass().getSimpleName();
		switch (n) {
			case "GnollGeomancer":      //豺狼法师：投石
			case "GnollSapper":         //豺狼投弹手
			case "DM100":               //电击
			case "DM200":               //腐蚀
			case "DM201":
			case "DM300":               //Boss，但保险起见也列上
			case "Tengu":               //飞刀
			case "Warlock":             //亡灵法师：远程法球
			case "Shaman":              //豺狼祭司：远程
			case "Eye":                 //邪眼：即死射线
			case "Scorpio":             //巨蝎：远程
			case "Succubus":            //魅魔：传送+远程
			case "RipperDemon":         //撕裂者：跳斩
			case "Necromancer":         //亡灵法师
			case "Elemental":           //元素：远程
			case "FireElemental":
			case "Sniper":              //狙击手
			case "Centurion":           //整合运动干部
				return true;
			default:
				return false;
		}
	}
	//==================================================================
	//经济类：160 / 161 / 167
	//==================================================================

	/** 160 氪金大佬：消耗金币给物品升级。 */
	public static final int WHALE             = 160;
	/** 161 钱就是命：致命伤用金币抵消。 */
	public static final int MONEY_IS_LIFE     = 161;
	/** 167 黄金地牢：只掉金币、可用钱买一切。 */
	public static final int GOLDEN_DUNGEON    = 167;
	/** 39 All or Nothing：赌徒之骰。 */
	public static final int ALL_OR_NOTHING    = 39;
	/** 42 等价交换：交换契约。 */
	public static final int EXCHANGE          = 42;

	//---- 161 钱就是命 ----

	/**
	 * END(161 钱就是命): 致命伤是否可以用金币抵消。
	 *
	 * <p>按文档所有者说明："在受到致命伤时，用**等量金币**抵消"。
	 * 即：需要多少金币取决于伤害超出多少 —— 1 金币抵 1 点伤害。
	 *
	 * <p>调用点：{@code Char.damage()} 的致命伤拦截处（与 65/104/128/124 并列）。
	 *
	 * @param ch  受击者
	 * @param dmg 即将造成的伤害
	 * @return true 表示已用金币抵消（调用方不应再扣血）
	 */
	public static boolean payToSurvive(Char ch, int dmg) {
		if (!on(MONEY_IS_LIFE) || ch == null) return false;
		if (!(ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)) {
			return false;                      //只对玩家生效
		}
		if (dmg < ch.HP) return false;         //不是致命伤

		//需要"刚好够活下来"的金币：伤害 - (当前生命 - 1)
		int needed = dmg - (ch.HP - 1);
		if (needed <= 0) return false;
		if (Dungeon.gold < needed) return false;

		Dungeon.gold -= needed;
		ch.HP = 1;

		if (ch.sprite != null) {
			ch.sprite.showStatus(
					com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE,
					"-" + needed + "G");
		}
		safeLogW("你用 " + needed + " 金币买回了自己的命。");
		return true;
	}

	//---- 167 黄金地牢 ----

	/** END(167): 怪物是否完全不掉落物品（只掉金币）。 */
	public static boolean goldenNoDrops() {
		return on(GOLDEN_DUNGEON);
	}

	/**
	 * END(167): 地面生成的物品是否应被替换成金币。
	 *
	 * <p>按文档所有者说明："地面不刷新物品，只刷新金币"。
	 *
	 * <p>但**特殊物品不替换**（天狗面具等）—— 那些是剧情/任务道具，
	 * 换成金币会让主线断掉。这里按"任务相关"判断。
	 */
	public static boolean shouldConvertDropToGold(com.shatteredpixel.shatteredpixeldungeon
			.items.Item item) {
		if (!on(GOLDEN_DUNGEON) || item == null) return false;
		return !isQuestRelated(item);
	}

	/**
	 * END(167): 该物品是否与剧情/任务相关（不能被换成金币）。
	 *
	 * <p>名单不求完备，覆盖主要剧情道具即可：
	 * 天狗面具、各类任务物品、Boss 掉落的关键物。
	 */
	private static boolean isQuestRelated(com.shatteredpixel.shatteredpixeldungeon.items
			.Item item) {
		String n = item.getClass().getSimpleName();
		switch (n) {
			//天狗相关
			case "CeremonialMask":
			case "Mask":
			//任务物品
			case "CorpseDust":
			case "DwarfToken":
			case "Embers":
			case "Pickaxe":
			case "DarkGold":
			case "Amulet":
			case "DriedRose":
			//笔记/图鉴类
			case "Note":
			case "GuidePage":
				return true;
			default:
				//类名里带 Quest 的一律算任务相关
				return n.contains("Quest");
		}
	}

	//---- 160 氪金大佬 ----

	/** 每次"氪金升级"消耗的金币（按等级递增）。 */
	private static final int WHALE_BASE_COST = 100;

	/**
	 * END(160 氪金大佬): 把一件物品升级所需的金币。
	 *
	 * <p>按文档所有者说明："可以消耗金币，对物品升级"。
	 *
	 * <p>费用随当前等级递增 —— 否则后期金币充裕时升级会变成免费的。
	 * 公式：{@code 100 × (等级 + 1)}，即 +0→100、+1→200、+2→300…
	 */
	public static int whaleUpgradeCost(com.shatteredpixel.shatteredpixeldungeon.items
			.Item item) {
		if (item == null) return Integer.MAX_VALUE;
		return WHALE_BASE_COST * (Math.max(0, item.level()) + 1);
	}

	/** END(160): 是否可以用金币升级该物品。 */
	public static boolean whaleCanUpgrade(com.shatteredpixel.shatteredpixeldungeon.items
			.Item item) {
		if (!on(WHALE) || item == null) return false;
		if (!item.isUpgradable()) return false;
		if (item.level() >= 10) return false;   //与原版升级卷轴同样的上限
		return Dungeon.gold >= whaleUpgradeCost(item);
	}

	/**
	 * END(160): 真的花钱升级。
	 *
	 * @return true 表示成功（金币已扣、物品已升级）
	 */
	public static boolean whaleUpgrade(com.shatteredpixel.shatteredpixeldungeon.items
			.Item item) {
		if (!whaleCanUpgrade(item)) return false;

		int cost = whaleUpgradeCost(item);
		Dungeon.gold -= cost;
		item.upgrade();
		item.identify();

		safeLogI("花费 " + cost + " 金币，将" + item.name() + "强化至 +" + item.level() + "。");
		return true;
	}
	//==================================================================
	//33 黑市 / 38 盲盒
	//==================================================================

	/** 33 黑市：商店出现特殊商品。 */
	public static final int BLACK_MARKET = 33;
	/** 38 盲盒：商店可购买盲盒。 */
	public static final int MYSTERY_BOX  = 38;

	//---- 38 盲盒 ----

	/** END(38): 商店是否上架盲盒。 */
	public static boolean mysteryBoxEnabled() { return on(MYSTERY_BOX); }

	/** END(38): 每家商店的盲盒数量。 */
	public static int mysteryBoxStock() { return on(MYSTERY_BOX) ? 2 : 0; }

	//---- 33 黑市 ----

	/** END(33): 商店是否上架特殊商品。 */
	public static boolean blackMarketEnabled() { return on(BLACK_MARKET); }

	/** END(33): 每家商店的特殊商品数量（1-2 件）。 */
	public static int blackMarketStock() {
		return on(BLACK_MARKET) ? (1 + Random.Int(2)) : 0;
	}

	/**
	 * END(33 黑市): 随机挑一件"原版以外"的物品。
	 *
	 * <p>来源是本 fork 新增的内容 —— 这些在正常对局里不会出现在商店，
	 * 所以勾选 33 后玩家会明显感到"这家店不太对劲"。
	 *
	 * <p>**不含**格林系列的武器/戒指（那些是 125-136 的专属内容，
	 * 放进普通商店会打乱那套规则的经济）。
	 */
	public static com.shatteredpixel.shatteredpixeldungeon.items.Item
			rollBlackMarketItem() {
		try {
			switch (Random.Int(8)) {
				case 0:
					//进化法杖的原料
					return new com.shatteredpixel.shatteredpixeldungeon.items.stones
							.StoneOfEnchantment();
				case 1:
					//稀有符石
					return new com.shatteredpixel.shatteredpixeldungeon.items.stones
							.StoneOfAugmentation();
				case 2:
					//神器（正常对局很少见）
					return com.shatteredpixel.shatteredpixeldungeon.items.Generator
							.randomUsingDefaults(
									com.shatteredpixel.shatteredpixeldungeon.items.Generator
											.Category.ARTIFACT);
				case 3:
					//法杖
					return com.shatteredpixel.shatteredpixeldungeon.items.Generator
							.randomUsingDefaults(
									com.shatteredpixel.shatteredpixeldungeon.items.Generator
											.Category.WAND);
				case 4:
					//魂之容器（126 的入口道具）—— 商店能买到会方便很多
					return new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
							.SoulVessel();
				case 5:
					//赌徒之骰（39 的道具）
					return new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
							.GamblersDice();
				case 6:
					//野生狗奶（124 的道具）
					return new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
							.WildDogMilk();
				default:
					//镇魂歌（128 的道具）
					return new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
							.SoulRequiem();
			}
		} catch (Throwable t) {
			//挑不出来就空着 —— 那只是少一件商品
			return null;
		}
	}
	//==================================================================
	//139 紊乱法杖：施法时随机变成别的法杖
	//==================================================================

	/** 139 紊乱法杖。 */
	public static final int CHAOS_WAND = 139;

	/** 每次施法触发"紊乱"的概率（%）。 END(修订): 40% -> 13%（文档所有者指定）。 */
	private static final int CHAOS_WAND_PCT = 13;

	/**
	 * END(139 紊乱法杖): 本次施法是否"紊乱"，若是则返回要冒充的法杖。
	 *
	 * <p>文档所有者定稿："对所有施法时生效" ——
	 * **不是**一件新物品，而是勾选 139 后任何法杖施法
	 * 都可能放出另一种法杖的效果。
	 *
	 * <p>概率 13%（文档所有者指定）：太低玩家感觉不到，太高会让"法杖选择"失去意义。
	 *
	 * @param current 玩家正在用的法杖
	 * @return 要冒充的法杖（调用方会用它代替 current 施法）；不触发时返回 null
	 */
	public static com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand rollChaosWand(
			com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand current) {
		if (!on(CHAOS_WAND) || current == null) return null;
		if (Random.Int(100) >= CHAOS_WAND_PCT) return null;

		Class<?>[] pool = com.shatteredpixel.shatteredpixeldungeon.items.Generator
				.Category.WAND.classes;
		if (pool == null || pool.length == 0) return null;

		//最多试 10 次，避免随机到"同一件"（那就不叫紊乱了）
		for (int i = 0; i < 10; i++) {
			Class<?> c = pool[Random.Int(pool.length)];
			if (c == current.getClass()) continue;
			try {
				Object o = com.watabou.utils.Reflection.newInstance(c);
				if (o instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand) {
					return (com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand) o;
				}
			} catch (Throwable t) {
				//换一个
			}
		}
		return null;
	}
	//==================================================================
	//41 钱是万能
	//==================================================================

	/** 41 钱是万能。 */
	public static final int MONEY_IS_POWER = 41;

	/** END(41): 是否可以用金币买任何物品。 */
	public static boolean moneyPurchaseEnabled() { return on(MONEY_IS_POWER); }

	/**
	 * END(41 钱是万能): 按类别"购买"一件物品。
	 *
	 * <p>与商店不同：这里**不从库存里拿**，而是直接生成一件新的。
	 * 所以"钱是万能"是真的"任何物品"，不受商店刷新限制。
	 *
	 * <p>键的语义：
	 * <ul>
	 *   <li>{@code POTION}/{@code SCROLL}/{@code SEED}/{@code STONE}/{@code WEAPON}/
	 *       {@code ARMOR}/{@code WAND}/{@code RING}/{@code ARTIFACT}
	 *       → 对应 {@code Generator.Category}</li>
	 *   <li>{@code @STR} → 力量药水</li>
	 *   <li>{@code @SOU} → 升级卷轴</li>
	 *   <li>{@code @EXP} → 经验药水</li>
	 * </ul>
	 *
	 * @return 生成的物品；键无效时返回 null
	 */
	public static com.shatteredpixel.shatteredpixeldungeon.items.Item purchaseItem(
			String key) {
		if (key == null) return null;

		try {
			switch (key) {
				case "@STR":
					return new com.shatteredpixel.shatteredpixeldungeon.items.potions
							.PotionOfStrength();
				case "@SOU":
					return new com.shatteredpixel.shatteredpixeldungeon.items.scrolls
							.ScrollOfUpgrade();
				case "@EXP":
					return new com.shatteredpixel.shatteredpixeldungeon.items.potions
							.PotionOfExperience();
				default:
					break;
			}

			com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category cat = null;
			switch (key) {
				case "POTION":   cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.POTION;   break;
				case "SCROLL":   cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.SCROLL;   break;
				case "SEED":     cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.SEED;     break;
				case "STONE":    cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.STONE;    break;
				case "WEAPON":   cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.WEAPON;   break;
				case "ARMOR":    cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.ARMOR;    break;
				case "WAND":     cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.WAND;     break;
				case "RING":     cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.RING;     break;
				case "ARTIFACT": cat = com.shatteredpixel.shatteredpixeldungeon.items
						.Generator.Category.ARTIFACT; break;
				default: return null;
			}

			return com.shatteredpixel.shatteredpixeldungeon.items.Generator
					.randomUsingDefaults(cat);
		} catch (Throwable t) {
			return null;
		}
	}
	//==================================================================
	//88 拍卖行（简化版）
	//==================================================================

	/** 88 拍卖行。 */
	public static final int AUCTION_HOUSE = 88;

	/**
	 * END(88 拍卖行): 简化版的"竞价"。
	 *
	 * <h3>原表效果</h3>
	 * "商店物品可竞价，价格波动，可低价买入或被 NPC 抬价"
	 *
	 * <h3>简化版的做法</h3>
	 * 不做完整的竞价 UI（那需要一套实时出价系统）。改为：
	 * <ol>
	 *   <li><b>价格波动</b>：每件商品在首次看到时定一个随机倍率
	 *       （0.5 ~ 1.8），之后不变 —— 所以"逛店"是有意义的</li>
	 *   <li><b>NPC 抬价</b>：进入商店时有概率触发一次全场抬价
	 *       （×1.5），持续到离开这一层</li>
	 * </ol>
	 *
	 * <p>为什么用"首次看到时定格"而不是"每次计算都随机"：
	 * 后者会让玩家在购买界面看到的价格与结算价格不一致（很恼人）。
	 *
	 * @param item  商品
	 * @param price 原价
	 * @return 调整后的价格
	 */
	public static int auctionPrice(com.shatteredpixel.shatteredpixeldungeon.items.Item item,
								   int price) {
		if (!on(AUCTION_HOUSE) || item == null) return price;

		float mult = auctionMultiplier(item);

		//NPC 抬价：本层是否已被抬价
		if (auctionBidUpThisFloor) mult *= AUCTION_BID_UP_MULT;

		return Math.max(1, Math.round(price * mult));
	}

	/** 价格波动范围：0.5 ~ 1.8 倍。 */
	private static final float AUCTION_MIN_MULT = 0.5f;
	private static final float AUCTION_MAX_MULT = 1.8f;
	/** NPC 抬价的倍率。 */
	private static final float AUCTION_BID_UP_MULT = 1.5f;

	/** 每件商品的价格倍率（首次查询时定格）。 */
	private static final java.util.HashMap<com.shatteredpixel.shatteredpixeldungeon.items.Item,
			Float> auctionMults = new java.util.HashMap<>();

	/** 本层是否已被 NPC 抬价。 */
	private static boolean auctionBidUpThisFloor = false;

	private static float auctionMultiplier(
			com.shatteredpixel.shatteredpixeldungeon.items.Item item) {
		Float cached = auctionMults.get(item);
		if (cached != null) return cached;

		float m = AUCTION_MIN_MULT
				+ Random.Float() * (AUCTION_MAX_MULT - AUCTION_MIN_MULT);
		auctionMults.put(item, m);
		return m;
	}

	/**
	 * END(88): 进入新层时掷一次"NPC 抬价"。
	 *
	 * <p>调用点：{@code Dungeon.newLevel()}。
	 * 概率 35% —— 太高会让这条规则纯粹变成惩罚。
	 */
	public static void rollAuctionBidUp() {
		auctionMults.clear();                 //换层 → 重新定价
		auctionBidUpThisFloor = on(AUCTION_HOUSE) && Random.Int(100) < 35;
	}

	/** END(88): 本层是否被抬价（供界面提示）。 */
	public static boolean auctionBidUpActive() {
		return on(AUCTION_HOUSE) && auctionBidUpThisFloor;
	}

	/** END(88): 换局时清空。 */
	public static void clearAuction() {
		auctionMults.clear();
		auctionBidUpThisFloor = false;
	}
	//==================================================================
	//151 圣明神明 / 164 魔法地牢 / 144 破碎权柄
	//==================================================================

	/** 151 圣明神明。 */
	public static final int HOLY_DEITY   = 151;
	/** 164 魔法地牢。 */
	public static final int MAGIC_DUNGEON = 164;
	/** 144 破碎权柄。 */
	public static final int BROKEN_POWER  = 144;

	//---- 151 圣明神明 ----

	/** 生命/命中/闪避的倍率（原表：提升 50%）。 */
	public static final float DEITY_DEF_MULT = 1.5f;
	/** 攻击的倍率（原表：提升 30%）。 */
	public static final float DEITY_ATK_MULT = 1.3f;

	/** END(151): 该角色是否享受圣明神明的加成。只对玩家生效。 */
	public static boolean deityBlessing(Char ch) {
		return on(HOLY_DEITY)
				&& ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
	}

	/**
	 * END(151 圣明神明): "停止并祷告" —— 玩家每回合开始时有概率被定住一回合。
	 *
	 * <p>文档所有者说明："每 1 回合要停止并祷告"。
	 *
	 * <p>字面"每回合都停"会让游戏完全无法进行（玩家永远动不了），
	 * 所以实现为**每回合开始时有概率**被祷告打断。
	 * 概率取 25% —— 明显能感觉到，但不至于卡死。
	 *
	 * <p>如果文档所有者要的是"真的每回合",把 {@link #DEITY_PRAY_PCT} 改成 100 即可。
	 */
	private static final int DEITY_PRAY_PCT = 25;

	/** END(151): 本回合是否要"停下来祷告"。 */
	public static boolean rollDeityPray() {
		if (!on(HOLY_DEITY)) return false;
		return Random.Int(100) < DEITY_PRAY_PCT;
	}

	//---- 164 魔法地牢 ----

	/** 怪物能使用魔法的概率（%）。 */
	private static final int MAGIC_MOB_PCT = 13;

	/** END(164): 这只怪物是否会使用魔法。 */
	public static boolean rollMagicMob(Mob mob) {
		if (!on(MAGIC_DUNGEON) || mob == null) return false;
		if (Char.hasProp(mob, Char.Property.BOSS)
				|| Char.hasProp(mob, Char.Property.MINIBOSS)) return false;
		return Random.Int(100) < MAGIC_MOB_PCT;
	}

	/**
	 * END(164): 该怪物使用的随机魔法。
	 *
	 * <p>"魔法"用**法杖的法术**来表示 —— 这是本作里最接近"怪物放法术"的现成机制，
	 * 不必另造一套。抽取时会实例化一个临时法杖，
	 * 由 {@code MobMagicCast} 负责在怪物回合里释放。
	 *
	 * @return 要释放的法杖（调用方用完即弃）；无法抽取时返回 null
	 */
	public static com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand rollMobMagic() {
		Class<?>[] pool = com.shatteredpixel.shatteredpixeldungeon.items.Generator
				.Category.WAND.classes;
		if (pool == null || pool.length == 0) return null;
		try {
			Object o = com.watabou.utils.Reflection.newInstance(pool[Random.Int(pool.length)]);
			if (o instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand) {
				return (com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand) o;
			}
		} catch (Throwable t) {
			//抽不到就算了
		}
		return null;
	}

	//---- 144 破碎权柄 ----

	/** 触发召唤的生命阈值（33%）。 */
	public static final float BROKEN_POWER_HP_THRESHOLD = 0.33f;
	/** 召唤间隔（回合）。 */
	public static final int BROKEN_POWER_INTERVAL = 5;

	/** END(144): 这只 Boss 是否已进入"破碎权柄"阶段。 */
	public static boolean brokenPowerActive(Mob boss) {
		if (!on(BROKEN_POWER) || boss == null) return false;
		if (!Char.hasProp(boss, Char.Property.BOSS)) return false;
		return boss.HP <= Math.round(boss.HT * BROKEN_POWER_HP_THRESHOLD);
	}

	/**
	 * END(144 破碎权柄): Boss 进入 33% 阶段后，每 5 回合召唤 1 只稀有怪。
	 *
	 * <p>调用点：{@code Mob.damage()} 的末尾 —— 每次 Boss 掉血时检查一次。
	 *
	 * <h3>为什么要用 buff 计时</h3>
	 * "每 5 回合"需要一个跨回合的计数器。原版的现成做法就是挂一个
	 * {@code FlavourBuff} 当计时器（它的时长会自动递减，归零时 detach）。
	 * 这里复用同一个思路：挂一个 5 回合的 {@code BrokenPowerTimer}，
	 * 它消失就说明 5 回合到了，于是再召唤一只并重新挂上。
	 *
	 * @param boss 掉血的 Boss
	 */
	public static void tryBrokenPowerSummon(Mob boss) {
		if (!brokenPowerActive(boss)) return;

		//计时器还在 → 没到 5 回合
		if (boss.buff(BrokenPowerTimer.class) != null) return;

		//召唤一只稀有怪
		spawnRareMobNear(boss);

		//重新开始计时
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
				boss, BrokenPowerTimer.class, BROKEN_POWER_INTERVAL);
	}

	/** END(144): 5 回合的计时器。 */
	public static class BrokenPowerTimer
			extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}
		@Override public int icon() {
			return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.NONE;
		}
	}

	/**
	 * END(144): 在 Boss 附近生成一只"稀有怪"。
	 *
	 * <p>"稀有怪"用**精英怪**表示（{@code ChampionEnemy}）——
	 * 与原版"稀有怪 = 带精英词缀的怪"的定义一致。
	 */
	private static void spawnRareMobNear(Mob boss) {
		if (Dungeon.level == null || boss == null) return;
		try {
			Mob add = Dungeon.level.createMob();
			if (add == null) return;

			//找个 Boss 附近的空位
			int cell = -1;
			int[] n = com.watabou.utils.PathFinder.NEIGHBOURS8;
			java.util.ArrayList<Integer> cand = new java.util.ArrayList<>();
			for (int d : n) {
				int c = boss.pos + d;
				if (c < 0 || c >= Dungeon.level.length()) continue;
				if (Dungeon.level.solid[c]) continue;
				if (Dungeon.level.passable[c] && com.shatteredpixel.shatteredpixeldungeon
						.actors.Actor.findChar(c) == null) {
					cand.add(c);
				}
			}
			if (cand.isEmpty()) return;
			cell = cand.get(Random.Int(cand.size()));

			add.pos = cell;
			add.state = add.WANDERING;

			//挂一个随机精英 buff —— 这就是"稀有怪"
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(add,
					pickChampionClass());

			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(add);
			safeLogW("权柄碎裂 —— 又一只怪物从裂缝中爬了出来。");
		} catch (Throwable t) {
			//召唤失败不影响 Boss 战
		}
	}
	//==================================================================
	//103 弹幕地狱：3 发散射
	//==================================================================

	/**
	 * END(103 弹幕地狱): 在主投射物落点周围补若干发散射。
	 *
	 * <p>原表："远程投射物数量变 3 发散射，有间隙可走位"
	 *
	 * <h3>"有间隙可走位"怎么体现</h3>
	 * 额外 2 发落在落点的**相邻格**（8 方向里随机挑，且互不重复）。
	 * 所以 3 发的覆盖是 1 + 2 个点，而不是一整片 ——
	 * 站在格与格之间、或者落点侧面，都能躲开。
	 *
	 * <h3>为什么不复用原物品实例</h3>
	 * 主投射物在 {@code onThrow} 里已经结算过（可能已被消耗、掉落或碎裂），
	 * 复用会让数量与耐久错乱。所以这里**新建同类型实例**，
	 * 并把它标记为 {@code spawnedForEffect}（不参与掉落/消耗）。
	 *
	 * @param origin 主投射物（用来取类型与等级）
	 * @param user   投掷者
	 * @param cell   主落点
	 * @param count  要补的散射数量（通常是 2）
	 */
	public static void spawnScatterShots(
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon
					origin,
			Char user, int cell, int count) {
		if (origin == null || user == null || Dungeon.level == null) return;
		if (count <= 0) return;

		int[] n = com.watabou.utils.PathFinder.NEIGHBOURS8;

		//先把候选格收集起来并打乱，避免两发落在同一格
		java.util.ArrayList<Integer> cand = new java.util.ArrayList<>();
		for (int d : n) {
			int c = cell + d;
			if (c < 0 || c >= Dungeon.level.length()) continue;
			if (Dungeon.level.solid[c]) continue;
			cand.add(c);
		}
		if (cand.isEmpty()) return;
		java.util.Collections.shuffle(cand, new java.util.Random(
				com.watabou.utils.Random.Long()));

		int made = 0;
		int dmgMin = Math.max(1, origin.damageRoll(user) / 2);   //散射伤害减半
		for (int c : cand) {
			if (made >= count) break;
			try {
				Char victim = com.shatteredpixel.shatteredpixeldungeon.actors.Actor
						.findChar(c);
				if (victim == null || victim == user) continue;

				//散射不掷命中骰 —— 否则 3 发的命中期望反而低于 1 发，
				//那这条规则就变成纯惩罚了。直接按半额伤害结算。
				victim.damage(dmgMin, origin);
				if (victim.sprite != null) {
					victim.sprite.flash();
				}
				made++;
			} catch (Throwable t) {
				//补射失败就少一发，不影响主投射物
			}
		}
	}
	//==================================================================
	//152 和平地牢 / 166 神圣天使 / 120 404
	//==================================================================

	/** 152 和平地牢。 */
	public static final int PEACEFUL_DUNGEON = 152;
	/** 166 神圣天使。 */
	public static final int HOLY_ANGEL       = 166;
	/** 120 404。 */
	public static final int ERROR_404        = 120;

	//---- 152 和平地牢 ----

	/**
	 * END(152 和平地牢): 玩家是否仍然遵守和平合约。
	 *
	 * <p>文档所有者说明："所有怪物不会对你有攻击行为，直到你违反了和平合约；
	 * 违反后 Boss 层 Boss 生命 +50%，常规层视为怪物属性 +50%，每下一层重置"
	 *
	 * <h3>什么算"违反"</h3>
	 * 玩家**主动攻击任何怪物**即视为违反。此后：
	 * <ul>
	 *   <li>本层怪物属性 +50%（Boss 层则是 Boss 生命 +50%）</li>
	 *   <li>**每下一层重置** —— 也就是"道歉"之后重新和平</li>
	 * </ul>
	 *
	 * <p>用一个静态标记记录"本层是否已违反"，由 {@link #resetPeaceful()} 在换层时清空。
	 */
	private static boolean peaceBrokenThisFloor = false;

	/** END(152): 本层是否已破坏和平。 */
	public static boolean peacefulBroken() {
		return on(PEACEFUL_DUNGEON) && peaceBrokenThisFloor;
	}

	/** END(152): 记录一次"玩家动手了"。 */
	public static void breakPeace() {
		if (on(PEACEFUL_DUNGEON)) peaceBrokenThisFloor = true;
	}

	/** END(152): 换层时重置（"每下一层重置"）。 */
	public static void resetPeaceful() {
		peaceBrokenThisFloor = false;
	}

	/**
	 * END(152): 怪物是否应"不攻击玩家"。
	 *
	 * <p>未破坏合约时所有怪物都不主动攻击；破坏后恢复原版行为。
	 */
	public static boolean monstersPassive() {
		return on(PEACEFUL_DUNGEON) && !peaceBrokenThisFloor;
	}

	/** END(152): 破坏合约后的怪物属性倍率。 */
	public static float peaceBrokenStatMult() {
		return peacefulBroken() ? 1.5f : 1f;
	}

	//---- 166 神圣天使 ----

	/** 祷告 CD 的基础值（回合）。 */
	public static final int ANGEL_PRAY_BASE_CD = 10;
	/** 选择"神圣天使"后 CD 增加的回合数。 */
	public static final int ANGEL_PRAY_CD_BONUS = 4;
	/** 祷告后获得的护盾比例。 */
	public static final float ANGEL_SHIELD_PCT = 0.02f;
	/** 祷告后回复的生命比例。 */
	public static final float ANGEL_HEAL_PCT = 0.02f;

	/**
	 * END(166 神圣天使): 是否已满足"变为天使"的条件。
	 *
	 * <p>文档所有者说明："选择**所有神圣类**后，变为天使"
	 *
	 * <p>"神圣类"= 本表里所有带"神圣"名号的条目：
	 * 145 神圣附体 / 151 圣明神明 / 158 神圣之力 / 166 神圣天使 / 神圣之光。
	 */
	private static final int[] HOLY_CHALLENGES = {
			145,   //神圣附体
			151,   //圣明神明
			158,   //神圣之力
			166,   //神圣天使
	};

	public static boolean angelForm() {
		if (!on(HOLY_ANGEL)) return false;
		for (int id : HOLY_CHALLENGES) {
			if (!on(id)) return false;
		}
		return true;
	}

	/** END(166): 祷告的实际 CD（天使形态下 +4）。 */
	public static int angelPrayCooldown() {
		return ANGEL_PRAY_BASE_CD + (angelForm() ? ANGEL_PRAY_CD_BONUS : 0);
	}

	//---- 120 404 ----

	/**
	 * END(120 404): 本回合是否"退出到主界面"。
	 *
	 * <p>文档所有者说明（END 修订）："每回合 0.50% 概率送回主界面"
	 *
	 * <p>我最初写成"进入新层时 50%" —— **那是错的**：
	 * 那样每次下楼有一半概率被踢出去，游戏根本没法玩。
	 * 正确的语义是**每回合一个很小的概率**（0.5%），
	 * 期望约 200 回合触发一次 —— 大概两层到三层会遇到一回。
	 *
	 * <p>调用点：{@code Hero.act()} —— 玩家每回合掷一次。
	 */
	public static boolean rollError404() {
		if (!on(ERROR_404)) return false;
		return Random.Float() < 0.005f;      //0.5%
	}
	//==================================================================
	//67 宝箱危机
	//==================================================================

	/** 67 宝箱危机。 */
	public static final int MIMIC_THREAT = 67;

	/** 每层生成宝箱怪的概率（%）。 */
	private static final int MIMIC_THREAT_PCT = 20;

	/**
	 * END(67 宝箱危机): 每层 20% 概率生成一只"保险怪"（宝箱怪）。
	 *
	 * <p>文档所有者说明："每层 20% 概率生成保险怪（黑檀、黄金等）"
	 *
	 * <h3>为什么照抄原版那段 ebony mimics</h3>
	 * {@code RegularLevel} 里已经有一段官方写法（MimicTooth 饰品触发的），
	 * 逻辑是"藏在堆下 → 没有堆就藏门口/出口"。这里复用同一套判据，
	 * 只是把触发条件换成固定 20%、并把类型扩到三种。
	 *
	 * <h3>RNG 隔离</h3>
	 * 用自己 push 的随机生成器 —— 否则会改变关卡后续的随机序列，
	 * 让"勾了 67"和"没勾 67"的地图长得不一样（那会让种子分享失效）。
	 */
	public static void spawnMimicThreat(
			com.shatteredpixel.shatteredpixeldungeon.levels.Level level) {
		if (!on(MIMIC_THREAT) || level == null) return;
		if (Random.Int(100) >= MIMIC_THREAT_PCT) return;

		com.watabou.utils.Random.pushGenerator(com.watabou.utils.Random.Long());
		try {
			java.util.ArrayList<Integer> cand = new java.util.ArrayList<>();

			//优先藏在普通堆下
			for (com.shatteredpixel.shatteredpixeldungeon.items.Heap h
					: level.heaps.valueList()) {
				if (h.type == com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type.HEAP
						&& level.findMob(h.pos) == null) {
					cand.add(h.pos);
				}
			}

			//没有堆 → 退而求其次，藏门口
			if (cand.isEmpty()) {
				for (int i = 0; i < level.length(); i++) {
					if (level.map[i] == com.shatteredpixel.shatteredpixeldungeon.levels
							.Terrain.DOOR && level.findMob(i) == null) {
						cand.add(i);
					}
				}
			}

			//再没有 → 出口
			if (cand.isEmpty() && level.findMob(level.exit()) == null) {
				cand.add(level.exit());
			}

			if (cand.isEmpty()) return;

			int pos = cand.get(Random.Int(cand.size()));

			//三种随机：普通 / 黑檀 / 黄金
			Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic> kind;
			switch (Random.Int(3)) {
				case 0: default:
					kind = com.shatteredpixel.shatteredpixeldungeon.actors.mobs
							.Mimic.class;
					break;
				case 1:
					kind = com.shatteredpixel.shatteredpixeldungeon.actors.mobs
							.EbonyMimic.class;
					break;
				case 2:
					kind = com.shatteredpixel.shatteredpixeldungeon.actors.mobs
							.GoldenMimic.class;
					break;
			}

			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic m =
					com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic
							.spawnAt(pos, kind, true);
			if (m != null) level.mobs.add(m);
		} catch (Throwable t) {
			//生成失败不影响关卡
		} finally {
			com.watabou.utils.Random.popGenerator();
		}
	}
	//==================================================================
	//100 镜像对决
	//==================================================================

	/** 100 镜像对决。 */
	public static final int MIRROR_DUEL = 100;

	/** 每层生成镜像的概率（%）。 */
	private static final int MIRROR_DUEL_PCT = 13;

	/**
	 * END(100 镜像对决): 每层 13% 概率生成一只敌对镜像。
	 *
	 * <p>文档所有者说明："每层 13% 概率生成镜像，同玩家装备外观/攻击/生命，
	 * 主动攻击，掉落随机复制品，不用道具和法杖"
	 *
	 * <p>调用点：{@code RegularLevel.createMobs()} 末尾。
	 * 用自己 push 的随机生成器，避免污染关卡 RNG（保护种子分享）。
	 */
	public static void spawnHostileMirror(
			com.shatteredpixel.shatteredpixeldungeon.levels.Level level) {
		if (!on(MIRROR_DUEL) || level == null) return;
		if (Dungeon.hero == null) return;
		if (Random.Int(100) >= MIRROR_DUEL_PCT) return;

		com.watabou.utils.Random.pushGenerator(com.watabou.utils.Random.Long());
		try {
			int cell = level.randomRespawnCell(null);
			if (cell == -1) return;

			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HostileMirror m =
					com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HostileMirror
							.createFor(Dungeon.hero);
			m.pos = cell;
			level.mobs.add(m);

			safeLogW("你看到了……你自己。");
		} catch (Throwable t) {
			//生成失败不影响关卡
		} finally {
			com.watabou.utils.Random.popGenerator();
		}
	}

	/**
	 * END(100): 镜像掉落的"随机复制品"。
	 *
	 * <p>文档所有者澄清："随机掉一件**同等级**的普通装备"。
	 * 所以不复制玩家身上那件具体的装备，而是从对应类别里
	 * 随机抽一件，并把等级对齐到玩家装备的水平。
	 */
	public static com.shatteredpixel.shatteredpixeldungeon.items.Item rollMirrorDrop() {
		if (Dungeon.hero == null) return null;
		try {
			com.shatteredpixel.shatteredpixeldungeon.items.Item out;
			if (Random.Int(2) == 0) {
				out = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.randomUsingDefaults(com.shatteredpixel.shatteredpixeldungeon
								.items.Generator.Category.WEAPON);
			} else {
				out = com.shatteredpixel.shatteredpixeldungeon.items.Generator
						.randomUsingDefaults(com.shatteredpixel.shatteredpixeldungeon
								.items.Generator.Category.ARMOR);
			}
			if (out == null) return null;

			//等级对齐：与玩家当前装备同级（"同等级"）
			int lvl = 0;
			com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon w =
					Dungeon.hero.belongings.attackingWeapon();
			if (w != null) lvl = Math.max(0, w.level());
			if (out.isUpgradable()) out.level(lvl);

			out.identify();
			return out;
		} catch (Throwable t) {
			return null;
		}
	}
	//==================================================================
	//63 鼠鼠可爱
	//==================================================================

	/** 63 鼠鼠可爱。 */
	public static final int CUTE_RATS = 63;

	/** END(63): 是否启用"怪物全变小鼠"。 */
	public static boolean cuteRatsOn() {
		return on(CUTE_RATS);
	}

	/**
	 * END(63): 这只怪物是否要变成小鼠。
	 *
	 * <p>文档所有者说明："怪物贴图、文本、近战后 UI 显示都变成小鼠"。
	 *
	 * <p>排除 Boss 与小 Boss：把古神变成小鼠会让整场 Boss 战失去意义，
	 * 而且 Boss 血条上的图标也会跟着变，视觉上很怪。
	 */
	public static boolean cuteRatsEnabled(Char ch) {
		if (!on(CUTE_RATS) || ch == null) return false;
		if (!(ch instanceof Mob)) return false;
		if (ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat) {
			return false;                       //本来就是小鼠
		}
		if (Char.hasProp(ch, Char.Property.BOSS)
				|| Char.hasProp(ch, Char.Property.MINIBOSS)) return false;
		return true;
	}

	/**
	 * END(63): 名字是否要显示成"小鼠"。
	 *
	 * <p>返回 non-null 表示要替换（数组内容无意义，只是为了避免
	 * 让调用方再多引一个类；用 boolean 会更清楚，但那样就要两处判断）。
	 *
	 * <p>实际上这里只需要一个"是/否" —— 名字从 Rat 的 messages 取。
	 */
	private static final int[] RAT_NAME_FLAG = { 0 };

	public static int[] cuteRatName(Char ch) {
		return cuteRatsEnabled(ch) ? RAT_NAME_FLAG : null;
	}
	//==================================================================
	//贴图查询（无副作用）—— 供 UI 使用
	//==================================================================

	/**
	 * END(63/138): 查询某只怪物"当前应该用哪个贴图类"。
	 *
	 * <h3>为什么需要这个"无副作用"的版本</h3>
	 * UI（{@code AttackIndicator}、{@code WndInfoMob}）需要画怪物的图标。
	 * 最直接的做法是调 {@code mob.sprite()} —— 但那会：
	 * <ol>
	 *   <li>挂上 138 的 {@code ChallengeAbsurdMark} buff</li>
	 *   <li>触碰 {@code ch.sprite} 相关状态</li>
	 * </ol>
	 * 在 UI 的 {@code update()} 里做这些会**打断正在进行的渲染** ——
	 * 实测表现为 {@code CharHealthIndicator} 的
	 * {@code target.sprite.visible} NPE 闪退。
	 *
	 * <p>所以这里只**读**状态，不写：判断该用哪个类，由调用方自己 new。
	 *
	 * @return 应该使用的 sprite 类；无特殊规则时返回 null（调用方用原 spriteClass）
	 */
	public static Class<? extends com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite>
			spriteClassFor(Mob mob) {
		if (mob == null) return null;

		//63 优先：全部变成小鼠
		if (cuteRatsEnabled(mob)) {
			return com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite.class;
		}

		//138：读已经记下来的那个类名（**不新掷、不挂 buff**）
		if (on(ABSURD_WORLD)) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeAbsurdMark mark =
					mob.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.ChallengeAbsurdMark.class);
			if (mark != null && mark.spriteClassName != null) {
				try {
					Class<?> c = Class.forName(mark.spriteClassName);
					if (com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class
							.isAssignableFrom(c)) {
						@SuppressWarnings("unchecked")
						Class<? extends com.shatteredpixel.shatteredpixeldungeon.sprites
								.CharSprite> sc =
								(Class<? extends com.shatteredpixel.shatteredpixeldungeon
										.sprites.CharSprite>) c;
						return sc;
					}
				} catch (Throwable ignored) {
					//类名失效就用原贴图
				}
			}
		}
		return null;
	}
}
