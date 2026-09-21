/*
 * 破碎的地牢 (End fork) — 统一诊断输出
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent;

/**
 * END(诊断): 统一的调试日志出口。
 *
 * <h3>为什么需要它</h3>
 * 之前每个模块各自有一个 {@code DEBUG} 常量（{@code UI_DEBUG} /
 * {@code DEBUG_MUSIC} / {@code ChallengeSfx.DEBUG} / {@code ABSURD_SPRITE_DEBUG}…），
 * 排查问题时要在四五个文件之间来回改。文档所有者要的是"加一下日志来检测
 * 之前的 bug" —— 那就应该有一个**总开关**。
 *
 * <h3>怎么用</h3>
 * <pre>
 *   Dbg.log("挑战", "119 怪物浪潮：要生成 " + n + " 只");
 *   Dbg.log("战斗", "反弹伤害 " + dmg);
 * </pre>
 *
 * <p>输出形如：
 * <pre>
 *   [END·挑战] 119 怪物浪潮：要生成 40 只
 * </pre>
 *
 * <h3>分类开关</h3>
 * 除了总开关，还能按分类单独开关 —— 排查某一类问题时不会被别类的刷屏淹没。
 * 见 {@link #enable(String, boolean)}。
 */
public final class Dbg {

	private Dbg() {}

	/**
	 * 总开关。
	 *
	 * <p><b>排查完记得改回 false</b> —— 开着会让日志非常长。
	 */
	public static final boolean ON = true;

	/** 单独关闭的分类（总开关开着时生效）。 */
	private static final java.util.HashSet<String> muted =
			new java.util.HashSet<>();

	/** 单独强制打开的分类（总开关关着时也输出）。 */
	private static final java.util.HashSet<String> forced =
			new java.util.HashSet<>();

	//---- 分类名（避免拼写错误）----

	public static final String CHALLENGE = "挑战";
	public static final String COMBAT    = "战斗";
	public static final String ITEM      = "物品";
	public static final String UI        = "界面";
	public static final String LEVEL     = "关卡";
	public static final String MUSIC     = "音乐";
	public static final String MOB       = "怪物";
	public static final String SAVE      = "存档";

	/** END: 当前分类是否应该输出。 */
	public static boolean on(String tag){
		if (forced.contains(tag)) return true;
		if (muted.contains(tag)) return false;
		return ON;
	}

	/** END: 静音某个分类。 */
	public static void mute(String tag, boolean value){
		if (value) muted.add(tag); else muted.remove(tag);
	}

	/** END: 强制输出某个分类（即使总开关关着）。 */
	public static void force(String tag, boolean value){
		if (value) forced.add(tag); else forced.remove(tag);
	}

	/** END: 打一行诊断。 */
	public static void log(String tag, String msg){
		if (!on(tag)) return;
		System.out.println("[END·" + tag + "] " + msg);
	}

	/**
	 * END: 打一行"带数值"的诊断。
	 *
	 * <p>专为"查数值不对"的场景 —— 把名字与值并排打印，一眼能看出差异。
	 */
	public static void val(String tag, String name, Object value){
		if (!on(tag)) return;
		System.out.println("[END·" + tag + "] " + name + " = " + value);
	}

	/**
	 * END: 打一行"预期 vs 实际"。
	 *
	 * <p>专为"UI 偏移 / 数量不对"这类**对比型**排查 ——
	 * 直接标出 [OK] 或 [差异]，不用拿计算器算。
	 */
	public static void expect(String tag, String name, Object want, Object actual){
		if (!on(tag)) return;
		boolean ok = (want == null) ? (actual == null) : want.equals(actual);
		System.out.println("[END·" + tag + "] " + name
				+ "  预期=" + want + "  实际=" + actual
				+ (ok ? "  [OK]" : "  [差异!]"));
	}

	/** END: 打一行警告（永远输出，不受开关影响）。 */
	public static void warn(String tag, String msg){
		System.out.println("[END·" + tag + "·警告] " + msg);
	}

	/** END: 打一行错误（永远输出）。 */
	public static void err(String tag, String msg, Throwable t){
		System.out.println("[END·" + tag + "·错误] " + msg
				+ (t == null ? "" : ("  → " + t)));
		if (t != null) t.printStackTrace();
	}
}
