package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Random;

/**
 * END(移植自魔绫·挑战区): 银河深渊(Galaxy) 常规楼层。
 *
 * <p>设计：Galaxy 简化为 **5 层区域**：
 * <ul>
 *   <li>G1-G4：本层 {@code GalaxyLevel}（熔岩洞贴图，刷挑战区怪物）</li>
 *   <li>G5：火龙 Boss 层 {@code LaveCavesBossLevel}</li>
 * </ul>
 *
 * <p>为什么不照搬魔绫的 MiningLevel/DragonCaveLevel：那两个关卡依赖魔绫的房型系统
 * （JunglePainter + 多个专用 room 类），牵连过大；此处用标准 RegularLevel 生成，
 * 足以承载「打 4 层 → 见火龙」的流程。结构与已完成的 HollowLevel 一致。
 */
public class GalaxyLevel extends RegularLevel {

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
		return Assets.Environment.TILES_FIRE;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HALLS;
	}

	@Override
	protected Painter painter() {
		//END: 熔岩洞 → 用 CavesPainter（与主线洞穴同风格，含岩浆/水池）
		return new com.shatteredpixel.shatteredpixeldungeon.levels.painters.CavesPainter()
				.setWater(feeling == Feeling.WATER ? 0.85f : 0.30f, 6)
				.setGrass(feeling == Feeling.GRASS ? 0.65f : 0.15f, 3)
				.setTraps(nTraps(), trapClasses(), trapChances());
	}

	@Override
	protected int standardRooms(boolean forceMax) {
		if (forceMax) return 6;
		//7 to 9, average 8
		return 7 + Random.chances(new float[]{2, 1, 1});
	}

	@Override
	protected int specialRooms(boolean forceMax) {
		if (forceMax) return 3;
		//2 to 3, average 2.5
		return 2 + Random.chances(new float[]{1, 1});
	}

	@Override
	protected Class<?>[] trapClasses() {
		return new Class<?>[]{
				com.shatteredpixel.shatteredpixeldungeon.levels.traps.BlazingTrap.class,
				com.shatteredpixel.shatteredpixeldungeon.levels.traps.FrostTrap.class,
				com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap.class,
				com.shatteredpixel.shatteredpixeldungeon.levels.traps.WeakeningTrap.class,
				com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap.class};
	}

	@Override
	protected float[] trapChances() {
		return new float[]{4, 4, 4, 4, 2};
	}

	@Override
	protected void createItems() {
		//END: 少量补给——挑战区，给点治疗/净化
		com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing heal =
				new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing();
		heal.quantity(Random.NormalIntRange(1, 2));
		addItemToSpawn(heal);

		com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity pur =
				new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity();
		pur.quantity(1);
		addItemToSpawn(pur);

		switch (Random.Int(3)) {
			case 0:
				addItemToSpawn(new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade());
				break;
			case 1:
				addItemToSpawn(new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping());
				break;
			case 2:
				addItemToSpawn(new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse());
				break;
		}

		addItemToSpawn(com.shatteredpixel.shatteredpixeldungeon.items.Generator.random());
		addItemToSpawn(com.shatteredpixel.shatteredpixeldungeon.items.Generator.random());
	}
}
