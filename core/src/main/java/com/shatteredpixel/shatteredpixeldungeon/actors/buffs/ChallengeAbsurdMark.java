/*
 * 破碎的地牢 (End fork) — 挑战 138「荒诞世界」的贴图记录
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * END(挑战 138 荒诞世界): 记录"这只怪被换成哪个贴图"。
 *
 * <h3>为什么必须记录，而不是每次随机</h3>
 * {@code Mob.sprite()} 是**每次调用都新建 CharSprite 实例**的
 * （见其实现：{@code return Reflection.newInstance(spriteClass)}）。
 * 如果在那里直接随机，同一只怪每次重建贴图都会换一个外观 ——
 * 表现为**贴图疯狂闪烁**。所以随机结果必须固定在 buff 上。
 *
 * <p>存的是 sprite 类名（字符串）而不是 Class 对象：
 * 存档里存类名更稳（Class 需要序列化支持），且反查失败时可以安全退化。
 *
 * <p>**纯外观**：只影响贴图，绝不改属性、AI 或行为。
 */
public class ChallengeAbsurdMark extends Buff {

	/** 被替换成的 sprite 类名；null 表示尚未决定（或替换失败）。 */
	public String spriteClassName;

	private static final String SPRITE = "sprite";

	@Override
	public boolean act() {
		//永久标记
		spend(TICK);
		return true;
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (spriteClassName != null) {
			bundle.put(SPRITE, spriteClassName);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(SPRITE)) {
			spriteClassName = bundle.getString(SPRITE);
		}
	}
}
