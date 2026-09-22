/*
 * 破碎的地牢 (End fork) — 「爆裂魔法」的进化版法杖
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

/**
 * END(挑战 217 爆裂魔法·进化): 进化的爆裂魔法。
 *
 * <h3>文档所有者定稿</h3>
 * "进阶版范围 **5×5**，**3×3 内额外 25% 伤害**。"
 *
 * <h3>遵循本 fork 既有的「进化法杖」规范</h3>
 * 与 13 种 {@code EvolvedWandOfXxx} 一致（见 {@code EvolvedWandOfBlastWave}）：
 * <ul>
 *   <li>继承源法杖 —— 在这里是 {@link WandOfMeteor}</li>
 *   <li>起一个**中文独有名字**</li>
 *   <li>{@code desc()} 以"进化·名（源：原法杖）：…"开头</li>
 *   <li>{@code glowing()} 给出辨识度高的光泽</li>
 *   <li>**真实等级 +8**、**充能上限 20**</li>
 *   <li>注册进 {@code EndWandEvolution.REGISTRY}</li>
 * </ul>
 *
 * <h3>与基础版的区别</h3>
 * <pre>
 *   爆裂魔法        3×3 范围，全额伤害
 *   焚天法杖        5×5 范围，其中 **3×3 内再 +25%**，外圈全额
 * </pre>
 */
public class EvolvedWandOfMeteor extends WandOfMeteor {

	/** 充能上限（与本 fork 其它 13 把进化法杖一致）。 */
	public static final int CHARGE_CAP = 20;

	@Override
	public String name() {
		return "爆裂法杖";
	}

	@Override
	public String desc() {
		return "进化·爆裂法杖（源：爆炸法杖）：爆炸范围扩至 5×5，"
				+ "爆炸正中 3×3 范围内的敌人承受额外 25% 伤害。\n\n"
				+ "**继承源法杖的等级**（不再有 +8 地板），充能上限提升到 20。";
	}

	@Override
	public String info() {
		return desc();
	}

	//==================================================================
	//进化法杖的标配
	//==================================================================

	/** END: 进化法杖的辨识光泽（橙红，与爆裂主题一致）。 */
	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 0xFF6A00, 1.3f );
	}

	/**
	 * END(终焉·进化基础): 充能上限 20（10 起步，每级 +1）。
	 *
	 * <p>照本 fork 那 13 把进化法杖的写法。
	 *
	 * <p>**等级直接来自源法杖** —— 见 {@code EndWandEvolution.evolve()} 里那句
	 * {@code evolved.level(source.level())}：不再有 +8 地板。
	 * 所以"真实等级 +8"是旧版说法，现在的进化法杖就是**继承原等级**。
	 */
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), CHARGE_CAP);
		curCharges = Math.min(curCharges, maxCharges);
	}

	/** END: 5×5 的偏移表（半径 2）。 */
	@Override
	protected int[] blastArea(){
		int w = (Dungeon.level == null) ? 1 : Dungeon.level.width();
		return new int[]{
				-2*w - 2, -2*w - 1, -2*w, -2*w + 1, -2*w + 2,
				-1*w - 2, -1*w - 1, -1*w, -1*w + 1, -1*w + 2,
				-2,       -1,        0,     1,        2,
				 1*w - 2,  1*w - 1,  1*w,   1*w + 1,  1*w + 2,
				 2*w - 2,  2*w - 1,  2*w,   2*w + 1,  2*w + 2,
		};
	}

	/**
	 * END: 内圈（3×3）伤害 = 基础上限再 ×1.25。
	 *
	 * <p>文档所有者定稿："3×3 内**额外 25%** 伤害" ——
	 * 所以是"内圈多打 25%"，而不是"外圈只有 25%"。
	 */
	@Override
	protected int innerDamage(){
		return Math.max(1, Math.round(damageRoll() * 1.25f));
	}

	/** END: 外圈（5×5 去掉 3×3）保持基础伤害。 */
	@Override
	protected int outerDamage(){
		return damageRoll();
	}

	//==================================================================
	//存档
	//==================================================================

	private static final String THRUST = "evolved_meteor";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(THRUST, true);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		//标记用，无状态需要恢复
	}
}
