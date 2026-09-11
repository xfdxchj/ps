package com.shatteredpixel.shatteredpixeldungeon.levels;

import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CHASM;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY_SP;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.noosa.audio.Music;

/**
 * END(移植自魔绫·挑战区): Boss Rush 层。
 *
 * <p>设计：BossRush 区共 7 层，**每层一个 Boss 竞技场**，用的是之前已搬入、
 * 原本"无关卡可打"的 7 个 Boss：
 * <pre>
 *   0. ShubNiggurath   (莎布·尼古拉丝)
 *   1. MyCoreHeart     (核心之心)
 *   2. Nyarlathotep    (奈亚拉托提普)
 *   3. YogSoul         (尤格之魂)
 *   4. TowerGodsBad    (塔·神·堕落)
 *   5. TowerMachineBad (塔·机·堕落)
 *   6. TowerMindBad    (塔·心·堕落) —— 与 7 合并为同一层双 Boss 时也在此处理
 * </pre>
 * 每层打完自动开放向下的楼梯，最后一层打完回到主线终局。
 *
 * <p>竞技场形制沿用 {@code MorpheusBossLevel} 的圆形斗兽场（圆形空地 + 外圈墙 +
 * 外圈深渊），保证 Boss 有足够活动空间。
 */
public class BossRushLevel extends Level {

	/** 本层要生成的 Boss（由 ChallengeArea 传入区内偏移）。 */
	private int bossIdx = 0;

	public BossRushLevel() {
		this(0);
	}

	public BossRushLevel(int bossIdx) {
		this.bossIdx = Math.max(0, Math.min(6, bossIdx));
	}

	{
		color1 = 0x801500;
		color2 = 0xa68521;
		extraGlass = false;
	}

	@Override
	public void playLevelMusic() {
		Music.playModeBGM(Assets.Music.HOLLOW_CITY_HARD, true);
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_HOLLOW_CS;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HOLLOW;
	}

	//==== 竞技场地形（25x25，与 MorpheusBossLevel 同形制）====
	private static final int WIDTH = 25;
	private static final int HEIGHT = 25;

	private static final int S = CHASM;
	private static final int G = WALL;
	private static final int E = EMPTY_SP;

	private static final int[] code_map = {
			S,S,S,S,S,S,S,S,G,G,G,G,G,G,G,G,S,S,S,S,S,S,S,S,S,
			S,S,S,S,S,G,G,G,G,E,E,E,E,E,E,G,G,G,G,S,S,S,S,S,S,
			S,S,S,S,G,G,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,S,S,S,S,
			S,S,S,S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,S,S,S,
			S,S,G,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,G,S,S,
			S,S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,
			S,S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,
			S,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,
			S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,
			G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
			G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
			G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
			G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
			G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
			G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
			G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
			S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,
			S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,
			S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,
			S,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,
			S,S,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,S,
			S,S,S,G,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,S,S,
			S,S,S,S,S,G,G,G,E,E,E,E,E,E,E,E,E,E,G,G,G,G,S,S,S,
			S,S,S,S,S,S,S,G,G,G,E,E,E,E,E,G,G,G,G,S,S,S,S,S,S,
			S,S,S,S,S,S,S,S,S,G,G,G,G,G,G,G,S,S,S,S,S,S,S,S,S,
	};

	/** 入口/出口格（与 MorpheusBossLevel 相同，均为场地内空地位）。 */
	private static final int ENTRANCE_CELL = 412;
	private static final int EXIT_CELL     = 300;

	@Override
	protected boolean build() {
		setSize(WIDTH, HEIGHT);
		map = code_map.clone();

		LevelTransition enter = new LevelTransition(this, ENTRANCE_CELL, LevelTransition.Type.REGULAR_ENTRANCE);
		transitions.add(enter);

		LevelTransition exits = new LevelTransition(this, EXIT_CELL, LevelTransition.Type.REGULAR_EXIT);
		transitions.add(exits);

		return true;
	}

	@Override
	protected void createMobs() {
		//END: 本层主角 —— 按 bossIdx 生成对应 Boss
		Mob boss;
		switch (bossIdx) {
			case 0:  boss = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.ShubNiggurath(); break;
			case 1:  boss = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.MyCoreHeart();    break;
			case 2:  boss = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.Nyarlathotep();   break;
			case 3:  boss = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.YogSoul();        break;
			case 4:  boss = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.bad.TowerGodsBad();    break;
			case 5:  boss = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.bad.TowerMachineBad(); break;
			default: boss = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.bad.TowerMindBad();    break;
		}
		boss.pos = 312;   //场地中心附近
		mobs.add(boss);
	}

	@Override
	protected void createItems() {
		//END: 每层给一点补给（Boss Rush 连续作战）
		com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing heal =
				new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing();
		heal.quantity(2);
		addItemToSpawn(heal);

		addItemToSpawn(new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade());
	}
}
