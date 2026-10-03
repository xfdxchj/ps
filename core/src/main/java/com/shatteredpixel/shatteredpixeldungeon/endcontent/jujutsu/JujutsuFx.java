/*
 * 破碎的地牢 (End fork) — 咒术回战系列技能特效。
 *
 * 全部复用现有粒子/闪屏，不新增贴图；先保证"看得见"，后续可换成专属素材。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

public final class JujutsuFx {

	private JujutsuFx() {}

	public static void cellBurst(int cell, int speckType, int n){
		if (Dungeon.level == null || !Dungeon.level.insideMap(cell)) return;
		CellEmitter.get(cell).burst(Speck.factory(speckType), n);
	}

	/** 苍：蓝色引力球 + 内向光爆。 */
	public static void cang(int heroCell, int targetCell){
		cellBurst(targetCell, Speck.BLUE_LIGHT, 10);
		cellBurst(targetCell, Speck.STAR, 4);
	}

	/** 赫：红色冲击波。 */
	public static void he(int targetCell){
		cellBurst(targetCell, Speck.RED_LIGHT, 12);
		cellBurst(targetCell, Speck.WOOL, 6);
	}

	/** 茈：紫黑空间撕裂。 */
	public static void zi(int targetCell){
		cellBurst(targetCell, Speck.STAR, 14);
		cellBurst(targetCell, Speck.SMOKE, 8);
		GameScene.flash(0x6030A060);
	}

	/** 无量空处：星空白领域展开。 */
	public static void domain(int heroCell){
		cellBurst(heroCell, Speck.STAR, 24);
		cellBurst(heroCell, Speck.LIGHT, 16);
		GameScene.flash(0x80FFFFFF);
	}

	/** 宿傩斩击：红色斩线。 */
	public static void slash(int targetCell, boolean big){
		cellBurst(targetCell, big ? Speck.RED_LIGHT : Speck.LIGHT, big ? 16 : 8);
		if (big) GameScene.flash(0x40FF0000);
	}

	/** 领域/灶开：血色或火焰全屏。 */
	public static void shrine(){
		GameScene.flash(0x60FF0000);
	}

	public static void fuga(int targetCell){
		cellBurst(targetCell, Speck.INFERNO, 20);
		GameScene.flash(0x60FF4400);
	}
}
