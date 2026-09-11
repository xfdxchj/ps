package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeadEndLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HollowExitLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HollowLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/**
 * END(移植自魔绫·挑战区): 挑战区域注册表 + 层号调度。
 *
 * <h3>机制（多选串联）</h3>
 * 主线 1-25F 走完后，从 26F 起把「选中的区域」按 id 顺序**串联**成一条区间：
 * <pre>
 *   只选 Hollow          → Hollow 占 26..33F（8层）
 *   只选 Galaxy          → 26F 直接进火龙的场地（1层）
 *   Hollow + Galaxy      → Hollow 26..33F，Galaxy 34F（火龙）
 * </pre>
 * 每层关卡由 {@link #createAreaLevel(int, int, int)} 按「区内偏移」决定，
 * 因此**单选任意一个区都能独立走通**（层号是动态算的，不是硬编码的）。
 *
 * <h3>关于 7 个「塔·堕落」等 Boss</h3>
 * ShubNiggurath / Nyarlathotep / YogSoul / MyCoreHeart /
 * TowerGodsBad / TowerTimeBad / TowerMachineBad / TowerMindBad
 * **不是独立 Boss** —— 它们由 Morphs 在 Hollow 33F 的 MorpheusBossLevel 里
 * 按三阶段依次登场（见 Morphs.act()）。因此本注册表**不单列 BossRush 区**，
 * 这些 Boss 在 33F 正常打即可。
 */
public final class ChallengeArea {

	/** 区域 id（顺序即进入顺序）。 */
	public final int id;
	/** 区域中文名。 */
	public final String name;
	/** 该区占用的层数（主线 25F 之后的连续层）。 */
	public final int floors;
	/** 是否已实装。 */
	public final boolean implemented;

	private ChallengeArea(int id, String name, int floors, boolean implemented) {
		this.id = id;
		this.name = name;
		this.floors = floors;
		this.implemented = implemented;
	}

	//==== 注册表（id 顺序 = 进入顺序）====
	//魔绫 2 区（已实装）
	public static final ChallengeArea HOLLOW      = new ChallengeArea(1, "空洞遗迹", 8, true);
	public static final ChallengeArea GALAXY      = new ChallengeArea(3, "银河深渊·火龙", 1, true);
	//方舟 3 区（待实装；方舟内容与原版体系自包含，与魔绫无耦合）
	public static final ChallengeArea IBERIA      = new ChallengeArea(4, "伊比利亚·海嗣", 4, false);
	public static final ChallengeArea GAVIAL      = new ChallengeArea(5, "嘉维尔·雨林", 4, false);
	public static final ChallengeArea SIESTA      = new ChallengeArea(6, "汐斯塔·海滨", 4, false);

	public static final ChallengeArea[] ALL = {
			HOLLOW, GALAXY, IBERIA, GAVIAL, SIESTA
	};

	/** 挑战区起始层（主线 25F 之后）。 */
	public static final int FIRST_CHALLENGE_DEPTH = 26;

	/** 该区域是否已被选中（位掩码第 id 位）。 */
	public static boolean isSelected(int mask, ChallengeArea area) {
		return (mask & (1 << area.id)) != 0;
	}

	/** 勾选/取消勾选。 */
	public static int toggle(int mask, ChallengeArea area) {
		return mask ^ (1 << area.id);
	}

	/** 把选中的区域写入 Statistics。应在开局（英雄创建完成）时调用一次。 */
	public static void applySelection(int mask) {
		Statistics.Hollow_Holiday = isSelected(mask, HOLLOW);
		Statistics.Galaxy_Rules   = isSelected(mask, GALAXY);
		Statistics.challengeMask  = mask;
	}

	/**
	 * 查询某层属于哪个区的哪个区内偏移。
	 *
	 * @return {@code {areaId, floorInArea}}；该层不属于任何已选区则返回 {@code null}
	 */
	public static int[] areaAtDepth(int depth) {
		if (depth < FIRST_CHALLENGE_DEPTH) return null;

		int cur = FIRST_CHALLENGE_DEPTH;
		for (ChallengeArea a : ALL) {
			if (!a.implemented) continue;
			if (!isSelected(Statistics.challengeMask, a)) continue;

			if (depth >= cur && depth < cur + a.floors) {
				return new int[]{ a.id, depth - cur };   //区内偏移从 0 开始
			}
			cur += a.floors;
		}
		return null;   //超出所有已选区 → 终局
	}

	/**
	 * 按「区 id + 区内偏移」创建关卡。
	 *
	 * @param areaId  区 id
	 * @param floorIn 区内偏移（0 起）
	 * @param depth   绝对层号
	 */
	public static Level createAreaLevel(int areaId, int floorIn, int depth) {

		if (areaId == HOLLOW.id) {
			//Hollow：0=入口(26F) 1-4=常规(27-30F) 5=冥犬(31F) 6=剧院(32F) 7=四柱(33F)
			switch (floorIn) {
				case 0:  return new HollowExitLevel();
				case 5:  return new com.shatteredpixel.shatteredpixeldungeon.levels.hollow.CerDogBossLevel();
				case 6:  return new com.shatteredpixel.shatteredpixeldungeon.levels.hollow.TheatreLevel();
				case 7:  return new com.shatteredpixel.shatteredpixeldungeon.levels.hollow.MorpheusBossLevel();
				default: return new HollowLevel();      //1-4
			}
		}

		if (areaId == GALAXY.id) {
			//END: Galaxy 简化为「直接打火龙」—— 不含常规层，进入即在火龙的场地。
			return new com.shatteredpixel.shatteredpixeldungeon.levels.LaveCavesBossLevel();
		}

		//未实装区域（方舟 3 区）：占位（正常流程走不到，areaAtDepth 只返回 implemented 的区）
		return new DeadEndLevel();
	}
}
