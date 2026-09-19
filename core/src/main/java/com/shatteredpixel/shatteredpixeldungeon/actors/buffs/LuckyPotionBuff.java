/*
 * 破碎的地牢 (End fork) — 挑战 198「幸运药水」的幸运状态
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/**
 * END(挑战 198 幸运药水): 额外的幸运。
 *
 * <h3>效果（文档所有者定稿）</h3>
 * "增加物品，提升财富戒/幸运附魔 50% 效果，50 回合。"
 *
 * <p>所以它是个**乘在现有公式上的倍率**：
 * <ul>
 *   <li>{@code RingOfWealth.dropChanceMultiplier()} —— 财富戒的掉落倍率 ×1.5</li>
 *   <li>{@code Lucky} 附魔的触发概率 ×1.5</li>
 * </ul>
 *
 * <p>与"再喝一瓶"是叠加的（同时喝两瓶 → 效果仍然只算一次，
 * 因为倍率是固定 1.5 而不是按层数累加）。
 */
public class LuckyPotionBuff extends FlavourBuff {

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	/** 持续时间（原表：50 回合）。 */
	public static final float DURATION = 50f;

	/** 效果倍率（原表：+50%）。 */
	public static final float MULT = 1.50f;

	@Override
	public int icon() {
		return BuffIndicator.BLESS;
	}

	@Override
	public void tintIcon(Image icon) {
		//金色调 —— 与"幸运"呼应
		icon.hardlight(1f, 0.9f, 0.4f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}
}
