/*
 * 破碎的地牢 (End fork) — 「轮回」无尽循环
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent;

/**
 * END(轮回): 无尽循环 —— 九次轮回，九种诅咒。
 *
 * <h3>文档所有者定稿</h3>
 * <ul>
 *   <li>"没有原版的 26 层，26 层直接变成原版一层的，计入一次轮回"</li>
 *   <li>"最大 9 次轮回，一共 9 种 buff" —— 都是**诅咒**，**主语是怪物**</li>
 *   <li>"9 次后到达原版 26 层，结束"</li>
 * </ul>
 *
 * <h3>完整流程</h3>
 * <pre>
 *   第 1 轮：打 1-25 层   → 进 26 层  → 怪物吃【诅咒① 不灭】
 *   第 2 轮：打 26-50 层  → 进 51 层  → 怪物吃【诅咒② 疾行】
 *   第 3 轮：打 51-75 层  → 进 76 层  → 怪物吃【诅咒③ 狂乱】
 *   ...
 *   第 9 轮：打 201-225 层 → 进 226 层 → 怪物吃【诅咒⑨ 铁鳞】
 *   第 10 轮：226-250 层  → 进 251 层 → **原版结局（不再循环）**
 * </pre>
 *
 * <p>也就是：**轮回 9 次之后，玩家抵达"原版第 26 层"对应的位置 —— 游戏结束。**
 * 越往后走，地牢越凶；撑过九轮才算通关。
 *
 * <h3>关卡怎么生成</h3>
 * 实际楼层 {@code depth} 映射回原版 1..25（见 {@link #mappedDepth}），
 * 用映射值去建关卡 —— 所以"第 26 层"就是原版第 1 层的地图与怪物，
 * 但 **HUD 与存档仍然显示 26**。
 *
 * <p>九种诅咒的定义见 {@link ReincarnationCurse}。
 */
public final class Reincarnation {

	private Reincarnation() {}

	//==================================================================
	//开关：整个轮回系统由挑战 210「永无止境」开启
	//==================================================================

	/** 挑战 210 的 ID。 */
	public static final int ENDLESS_CHALLENGE = 210;

	/**
	 * END: 轮回系统是否启用。
	 *
	 * <p>文档所有者定稿："**开启靠永无止境挑战**"。
	 *
	 * <p>所以轮回不是自动的 —— 只有勾选了挑战 210，26 层之后才会
	 * 进入循环、怪物才会吃到诅咒与倍率。没勾选时整个系统等于不存在，
	 * 26 层照原版的结局流程走。
	 *
	 * <p>所有查询出口（{@code mobXxxMultiplier} / {@code mobCanXxx}）
	 * 都会先过这一关 —— 这样"没开挑战"与"开了但还没轮回"两种情况
	 * 都不会有任何副作用。
	 */
	public static boolean enabled(){
		return com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.endlessEnabled();
	}

	//==================================================================
	//循环长度与轮次
	//==================================================================

	/**
	 * END: 一个循环有几层。
	 *
	 * <p>原版主线 25 层（5 区域 × 5 层），所以第 26 层 = 新一轮第 1 层。
	 * 勾选「完整地牢」时是 50 层，循环长度跟着变。
	 */
	public static int loopLength(){
		return com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.maxMainDepth();
	}

	/** END: 最多轮回几次（= 九种诅咒的数量）。 */
	public static int maxCycles(){
		return ReincarnationCurse.count();
	}

	/**
	 * END: 这个"实际楼层"对应原版的第几层。
	 *
	 * <p>例（len = 25）：26 → 1、27 → 2、51 → 1。
	 */
	public static int mappedDepth(int depth){
		int len = loopLength();
		if (len <= 0) return depth;
		if (depth <= len) return depth;
		return ((depth - 1) % len) + 1;
	}

	/** END: 当前是第几轮（0 = 第一轮，尚未循环）。 */
	public static int cycleOf(int depth){
		int len = loopLength();
		if (len <= 0) return 0;
		if (depth <= len) return 0;
		return (depth - 1) / len;
	}

	/**
	 * END: 这一层是否应该**结束游戏**（抵达"原版 26 层"的位置）。
	 *
	 * <p>文档所有者定稿："9 次后到达原版 26 层，结束"。
	 *
	 * <p>也就是走完第 9 轮（打满 9 × len 层）之后，
	 * 再往前就是原版的"拿护符之后"那一段 —— 该走正常结局了。
	 *
	 * @return true 表示不再循环，应交给原版结局流程
	 */
	public static boolean shouldEnd(int depth){
		if (!enabled()) return false;      //没开挑战 → 不走循环，交给原版流程
		//==== END(真·无尽): 在护符处选择"陷入无尽轮回"后，永远不再走结局 ====
		if (trueEndless) return false;
		int len = loopLength();
		if (len <= 0) return false;
		return depth > (maxCycles() + 1) * len;
	}

	//==================================================================
	//状态
	//==================================================================

	/** 已完成的轮回次数（0 ~ 9）。 */
	private static int cycles = 0;

	public static int cycles(){ return cycles; }

	/** END: 九种诅咒是否已全部生效。 */
	public static boolean isMaxed(){
		return cycles >= maxCycles();
	}

	/** END: 叠一层（进入新循环时调用），最多 9 层。 */
	public static void addCycle(){
		if (isMaxed()){
			Dbg.log(Dbg.CHALLENGE, "轮回已达上限（" + cycles + "），不再增加");
			return;
		}
		int before = cycles;
		cycles++;

		ReincarnationCurse c = ReincarnationCurse.forCycle(cycles - 1);
		if (c != null){
			Dbg.log(Dbg.CHALLENGE, "轮回 " + cycles + " → 怪物获得【" + c.title + "】" + c.effect);
		}
	}

	/** END: 直接设置（读档用），按 0~9 夹取。 */
	public static void setCycles(int n){
		cycles = Math.max(0, Math.min(maxCycles(), n));
	}

	/** END: 新的一局清零。 */
	public static void reset(){ cycles = 0; }

	//==================================================================
	//永无止境：按区域的怪物数值倍率
	//==================================================================

	/**
	 * END(永无止境): 基础倍率（第 1 轮）。
	 *
	 * <p>文档所有者定稿："提升比例，1区，2.3.4.5，1200%，600%，200%，
	 * 200%，200%，后续每次 200% 提升" —— 且"后续每次"是**乘法递增**。
	 *
	 * <pre>
	 *   1 区  1200%  = ×12
	 *   2 区   600%  = ×6
	 *   3 区   200%  = ×2
	 *   4 区   200%  = ×2
	 *   5 区   200%  = ×2
	 * </pre>
	 *
	 * <p>为什么 1 区最高：轮回之后玩家从第 1 层重新开始，
	 * 而那已经是"第二轮"了 —— 起点就该比原版凶得多，
	 * 否则前三区会变成纯粹的走路。
	 */
	private static final float[] BASE_RATIO = { 12f, 6f, 2f, 2f, 2f };

	/** END(永无止境): 每多一轮，倍率**翻倍**。 */
	private static final float RATIO_PER_CYCLE = 2f;

	/**
	 * END(永无止境): 某个楼层当前的怪物数值倍率。
	 *
	 * <p>公式：{@code BASE_RATIO[区域] × (1 + 已轮回次数)} —— **加算**（文档所有者定稿：
	 * "无尽 2 次轮回的为加算，不是乘算"）。
	 *
	 * <pre>
	 *   第 1 轮（cycles=0）：1区 ×12、2区 ×6、3-5区 ×2
	 *   第 2 轮（cycles=1）：1区 ×24、2区 ×12、3-5区 ×4
	 *   第 3 轮（cycles=2）：1区 ×36、2区 ×18、3-5区 ×6
	 *   第 4 轮（cycles=3）：1区 ×48、2区 ×24、3-5区 ×8
	 * </pre>
	 *
	 * @param depth 实际楼层（会用映射后的层号算区域）
	 */
	public static float mobStatRatio(int depth){
		if (!enabled() || cycles <= 0) return 1f;

		int mapped = mappedDepth(depth);
		int region = Math.max(0, Math.min(4, (mapped - 1) / 5));

		float base = BASE_RATIO[region];
		//==== END(修订·无尽轮回数值改为加算) ====
		//文档所有者定稿："无尽 2 次轮回的为加算，不是乘算。"
		//原来 base × 2^cycles（1→2→4→8…）；现改为 base × (1 + cycles)（2→3→4→5…）。
		float mult = 1f + cycles;

		float result = base * mult;

		//安全上限：再高就是纯数字游戏了（1e6 倍已经远超一切）
		return Math.min(1_000_000f, result);
	}

	/**
	 * END(永无止境): 把倍率应用到一个怪物的生命上。
	 *
	 * <p>只改生命 —— 伤害已经由 {@code AscensionChallenge} 那套
	 * （按怪种类的 modifiers 表）管，再叠一层乘法会让深层变成"一击必杀"。
	 *
	 * <p>返回 {@code -1} 表示不需要改（调用方保持原值）。
	 */
	public static int applyStatRatio(int baseHT, int depth){
		float r = mobStatRatio(depth);
		if (r == 1f) return -1;
		return Math.max(1, Math.round(baseHT * r));
	}

	//==================================================================
	//查询接口 —— **是否对某个怪物生效**
	//==================================================================

	/** END: 这条诅咒现在是否生效。 */
	public static boolean active(ReincarnationCurse c){
		return ReincarnationCurse.active(c);
	}

	/** END: 当前生效的诅咒列表（UI / 日志用）。 */
	public static String summary(){
		if (cycles <= 0) return "尚未轮回";
		StringBuilder sb = new StringBuilder("轮回 " + cycles + " / " + maxCycles() + "：");
		for (ReincarnationCurse c : ReincarnationCurse.values()){
			if (active(c)) sb.append(c.title).append("、");
		}
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '、'){
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	//==================================================================
	//九种诅咒的数值出口（全部只作用于怪物）
	//==================================================================

	/** END(② 疾行): 怪物移速倍率。 */
	public static float mobSpeedMultiplier(){
		if (!enabled()) return 1f;
		return active(ReincarnationCurse.SWIFT)
				? 1f + ReincarnationCurse.SWIFT.value : 1f;
	}

	/** END(③ 狂乱): 怪物攻速倍率。 */
	public static float mobAttackSpeedMultiplier(){
		if (!enabled()) return 1f;
		return active(ReincarnationCurse.FRENZY)
				? 1f + ReincarnationCurse.FRENZY.value : 1f;
	}

	/** END(⑤ 顽抗): 怪物减伤比例。 */
	public static float mobDamageReduction(){
		if (!enabled()) return 0f;
		return active(ReincarnationCurse.RESILIENCE)
				? ReincarnationCurse.RESILIENCE.value : 0f;
	}

	/** END(⑧ 厚躯): 怪物生命上限倍率。 */
	public static float mobMaxHpMultiplier(){
		if (!enabled()) return 1f;
		return active(ReincarnationCurse.THICK_HIDE)
				? 1f + ReincarnationCurse.THICK_HIDE.value : 1f;
	}

	/** END(⑨ 铁鳞): 怪物护甲倍率。 */
	public static float mobArmorMultiplier(){
		if (!enabled()) return 1f;
		return active(ReincarnationCurse.IRON_SCALE)
				? 1f + ReincarnationCurse.IRON_SCALE.value : 1f;
	}

	/** END(① 不灭): 致命伤时是否触发一次无敌。 */
	public static boolean mobCanSurviveFatal(){
		return enabled() && active(ReincarnationCurse.UNDYING);
	}

	/** END(④ 呼号): 是否呼喊同伴。 */
	public static boolean mobCanHowl(){
		return enabled() && active(ReincarnationCurse.HOWL);
	}

	/** END(⑥ 侵蚀): 是否附带随机负面。 */
	public static boolean mobCanErode(){
		return enabled() && active(ReincarnationCurse.EROSION);
	}

	/** END(⑦ 爆裂): 死亡是否爆炸。 */
	public static boolean mobCanDetonate(){
		return enabled() && active(ReincarnationCurse.DETONATE);
	}

	//==================================================================
	//存档
	//==================================================================

	private static final String CYCLES = "end_reincarnation_cycles";
	private static final String TRUE_ENDLESS = "end_reincarnation_true_endless";

	/** END(真·无尽): 玩家是否已在护符处选择"陷入无尽轮回"。 */
	private static boolean trueEndless = false;

	public static boolean isTrueEndless(){ return trueEndless; }

	/**
	 * END(真·无尽): 在古神护符处选择"陷入无尽轮回"后调用。
	 *
	 * <p>文档所有者定稿："可以在第九次后的古神护符加一个，陷入无尽轮回，
	 * 开始真正的无尽。" —— 开启后 {@link #shouldEnd} 永远返回 false，
	 * 楼层一直按 {@link #mappedDepth} 循环生成下去。
	 */
	public static void startTrueEndless(){ trueEndless = true; }

	public static void storeInBundle(com.watabou.utils.Bundle bundle){
		bundle.put(CYCLES, cycles);
		bundle.put(TRUE_ENDLESS, trueEndless);
	}

	public static void restoreFromBundle(com.watabou.utils.Bundle bundle){
		int n = bundle.contains(CYCLES) ? bundle.getInt(CYCLES) : 0;
		setCycles(n);
		trueEndless = bundle.getBoolean(TRUE_ENDLESS);
	}
}
