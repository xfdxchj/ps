/*
 * 破碎的地牢 (End fork) — 轮回的九种诅咒
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent;

/**
 * END(轮回九咒): 九种轮回诅咒。
 *
 * <h3>文档所有者定稿</h3>
 * "最大 9 次轮回，一共 9 种 buff" —— 都是**诅咒**，
 * **主语是怪物**：每轮回一次，地牢里的怪物就多一种特性，
 * 玩家则越来越难。这正是"无尽"的压迫感来源。
 *
 * <h3>九种诅咒</h3>
 * <pre>
 *   ① 不灭       受到致命伤时触发一次无敌
 *   ② 疾行       怪物移速 +100%
 *   ③ 狂乱       怪物攻速 +20%
 *   ④ 呼号       怪物会呼喊同伴（原版行为）
 *   ⑤ resilience 减伤 10%
 *   ⑥ 侵蚀       攻击附带随机 debuff
 *   ⑦ 爆裂       死亡后爆炸
 *   ⑧ 厚躯       生命上限 +20%
 *   ⑨ 铁鳞       护甲 +50%
 * </pre>
 *
 * <h3>解锁顺序</h3>
 * 按 {@link #ordinal()} 依次解锁（第 1 次轮回得第一个，以此类推）——
 * 玩家能预期"下一次地牢会多什么麻烦"。
 */
public enum ReincarnationCurse {

	/** ① 不灭 —— 致命伤时无敌一次。 */
	UNDYING("不灭", "它们学会了怎么不死。",
			"怪物受到致命伤时触发一次**无敌**"),

	/** ② 疾行 —— 移速。 */
	SWIFT("疾行", "它们比你更熟悉这条路。",
			"怪物**移动速度 +100%**", 1.00f),

	/** ③ 狂乱 —— 攻速。 */
	FRENZY("狂乱", "它们的手快得不像是活物。",
			"怪物**攻击速度 +20%**", 0.20f),

	/** ④ 呼号 —— 呼喊同伴。 */
	HOWL("呼号", "一声叫喊，整层都听见了。",
			"怪物会**呼喊附近的同伴**（原版行为）"),

	/** ⑤ 顽抗 —— 减伤。 */
	RESILIENCE("顽抗", "刀砍进去的感觉不太对。",
			"怪物**受到的伤害 -10%**", 0.10f),

	/** ⑥ 侵蚀 —— 附带随机 debuff。 */
	EROSION("侵蚀", "被碰到的地方开始不对劲。",
			"怪物命中时**附带一个随机负面效果**"),

	/** ⑦ 爆裂 —— 死亡爆炸。 */
	DETONATE("爆裂", "它们连死都不安分。",
			"怪物**死亡时爆炸**"),

	/** ⑧ 厚躯 —— 生命上限。 */
	THICK_HIDE("厚躯", "这一世的皮比上一世厚。",
			"怪物**生命上限 +20%**", 0.20f),

	/** ⑨ 铁鳞 —— 护甲。 */
	IRON_SCALE("铁鳞", "鳞片下面已经没有肉了。",
			"怪物**护甲 +50%**", 0.50f);

	//==================================================================

	/** 中文名。 */
	public final String title;

	/** 一句表现（UI 里显示）。 */
	public final String flavor;

	/** 效果说明（UI 里显示）。 */
	public final String effect;

	/** 数值（含义随种类而定；纯行为型的为 0）。 */
	public final float value;

	ReincarnationCurse(String title, String flavor, String effect, float value){
		this.title = title;
		this.flavor = flavor;
		this.effect = effect;
		this.value = value;
	}

	ReincarnationCurse(String title, String flavor, String effect){
		this(title, flavor, effect, 0f);
	}

	/** END: 总共有几种。 */
	public static int count(){
		return values().length;
	}

	/** END: 第 n 次轮回（0 基）解锁哪种诅咒；超出范围返回 null。 */
	public static ReincarnationCurse forCycle(int cycle){
		if (cycle < 0 || cycle >= values().length) return null;
		return values()[cycle];
	}

	/** END: 这条诅咒是否已生效。 */
	public static boolean active(ReincarnationCurse c){
		if (c == null) return false;
		return Reincarnation.cycles() > c.ordinal();
	}

	/** END: 按名字取（存档 / 调试用）。 */
	public static ReincarnationCurse byName(String name){
		try { return valueOf(name); }
		catch (Exception e){ return null; }
	}
}
