package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

/**
 * END(移植自魔绫·挑战区): 挑战区域注册表。
 * <p>每个区域有唯一 id（按 id 顺序即为挑战时的进入顺序）；开局可多选，选中的区域会在
 * 主线 25F 之后、26F 起按 id 顺序依次进入。
 * <p>目前只有 Hollow(空洞遗迹) 已移植；其余区域占位，待搬运后逐个启用。
 */
public final class ChallengeArea {

	/** 区域 id（顺序即进入顺序）。 */
	public final int id;
	/** 区域中文名。 */
	public final String name;
	/** 对应的开局置位开关（目前只有 Hollow 有真实内容）。 */
	public final boolean implemented;

	private ChallengeArea(int id, String name, boolean implemented) {
		this.id = id;
		this.name = name;
		this.implemented = implemented;
	}

	//==== 注册表 ====
	public static final ChallengeArea HOLLOW      = new ChallengeArea(1, "空洞遗迹", true);
	public static final ChallengeArea BOSS_RUSH   = new ChallengeArea(2, "Boss Rush", false);
	public static final ChallengeArea GALAXY      = new ChallengeArea(3, "银河深渊", false);
	public static final ChallengeArea PEACH       = new ChallengeArea(4, "桃神试炼", false);
	public static final ChallengeArea DEEP_SHADOW = new ChallengeArea(5, "深影领域", false);
	public static final ChallengeArea FOREST_HARD = new ChallengeArea(6, "森林灾厄", false);

	public static final ChallengeArea[] ALL = {
			HOLLOW, BOSS_RUSH, GALAXY, PEACH, DEEP_SHADOW, FOREST_HARD
	};

	/** 该区域是否已被选中（位掩码第 id 位）。 */
	public static boolean isSelected(int mask, ChallengeArea area) {
		return (mask & (1 << area.id)) != 0;
	}

	/** 勾选/取消勾选。 */
	public static int toggle(int mask, ChallengeArea area) {
		return mask ^ (1 << area.id);
	}

	/**
	 * 把选中的区域写入 Statistics（目前仅 Hollow 有真实开关）。
	 * 应在开局（英雄创建完成）时调用一次。
	 */
	public static void applySelection(int mask) {
		com.shatteredpixel.shatteredpixeldungeon.Statistics.Hollow_Holiday = isSelected(mask, HOLLOW);
	}
}
