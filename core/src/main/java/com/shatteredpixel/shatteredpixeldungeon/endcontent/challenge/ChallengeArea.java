package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
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
	//方舟 3 区（阶段A：已接入，每区 10 层；关卡内容待 B1 搬运）
	//方舟原版节奏：0-3=第1章 4=Boss1 5-8=第2章 9=Boss2
	public static final ChallengeArea IBERIA      = new ChallengeArea(4, "伊比利亚·海嗣", 10, true);
	public static final ChallengeArea GAVIAL      = new ChallengeArea(5, "嘉维尔·雨林", 10, true);
	public static final ChallengeArea SIESTA      = new ChallengeArea(6, "汐斯塔·海滨", 10, true);

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

	/**
	 * END(修复): 只保留第一个被选中的区域，其余全部清除。
	 *
	 * <p>本 MOD 的挑战区**只允许单选**：多选时各区按 id 顺序串接层号，
	 * 且方舟三区在原作里本就是互斥分支（extrastage_Sea / extrastage_Gavial），
	 * 同时勾选会互相干扰、产生难以定位的 bug。
	 *
	 * <p>UI 层（WndChallengeAreas）已改成单选行为；这里是**数据层兜底**，
	 * 防止旧存档或其它入口写入多选掩码。
	 */
	public static int firstSelectedOnly(int mask) {
		for (ChallengeArea a : ALL) {
			if (isSelected(mask, a)) {
				return 1 << a.id;
			}
		}
		return 0;
	}

	/** 返回当前被选中的那个区域；没选则返回 {@code null}。 */
	public static ChallengeArea selectedArea(int mask) {
		for (ChallengeArea a : ALL) {
			if (isSelected(mask, a)) return a;
		}
		return null;
	}

	/** 把选中的区域写入 Statistics。应在开局（英雄创建完成）时调用一次。 */
	public static void applySelection(int mask) {
		//END(修复): 数据层兜底 —— 强制单选
		mask = firstSelectedOnly(mask);

		Statistics.Hollow_Holiday = isSelected(mask, HOLLOW);
		Statistics.Galaxy_Rules   = isSelected(mask, GALAXY);
		Statistics.challengeMask  = mask;

		//END(方舟兼容桩): 方舟原版用 extrastage_Sea / extrastage_Gavial 两个布尔选择 31-40F 的剧情线。
		//这里由挑战区勾选结果推导；两区都不选 = Siesta（方舟原逻辑）。
		Dungeon.extrastage_Sea    = isSelected(mask, IBERIA);
		Dungeon.extrastage_Gavial = isSelected(mask, GAVIAL);
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

		//==== 方舟 3 区（地形阶段 T5：真实关卡已接入；怪物/Boss 仍为桩）====
		//布局（每区 10 层）：0-3 第1章 / 4 Boss1 / 5-8 第2章 / 9 Boss2

		if (areaId == IBERIA.id) {
			//伊比利亚·海嗣
			switch (floorIn) {
				case 4:  return new com.shatteredpixel.shatteredpixeldungeon.levels.SeaBossLevel1();
				case 9:  return new com.shatteredpixel.shatteredpixeldungeon.levels.SeaBossLevel2();
				case 5: case 6: case 7: case 8:
					return new com.shatteredpixel.shatteredpixeldungeon.levels.SeaLevel_part2();
				default: return new com.shatteredpixel.shatteredpixeldungeon.levels.SeaLevel_part1();  //0-3
			}
		}

		if (areaId == GAVIAL.id) {
			//嘉维尔·雨林
			switch (floorIn) {
				case 4:  return new com.shatteredpixel.shatteredpixeldungeon.levels.GavialBossLevel1();
				case 9:  return new com.shatteredpixel.shatteredpixeldungeon.levels.GavialBossLevel2();
				case 5: case 6: case 7: case 8:
					return new com.shatteredpixel.shatteredpixeldungeon.levels.GavialLevel2();
				default: return new com.shatteredpixel.shatteredpixeldungeon.levels.GavialLevel();     //0-3
			}
		}

		if (areaId == SIESTA.id) {
			//汐斯塔·海滨
			switch (floorIn) {
				case 4:  return new com.shatteredpixel.shatteredpixeldungeon.levels.SiestaBossLevel_part1();
				case 9:  return new com.shatteredpixel.shatteredpixeldungeon.levels.SiestaBossLevel_part2();
				case 5: case 6: case 7: case 8:
					return new com.shatteredpixel.shatteredpixeldungeon.levels.SiestaLevel_part2();
				default: return new com.shatteredpixel.shatteredpixeldungeon.levels.SiestaLevel_part1(); //0-3
			}
		}

				//未知区：占位
		return new DeadEndLevel();
	}
}
