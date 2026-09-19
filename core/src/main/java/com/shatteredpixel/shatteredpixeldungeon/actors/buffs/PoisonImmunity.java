/*
 * 破碎的地牢 (End fork) — 挑战 49「切尔诺贝利」配套的中毒免疫
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/**
 * END(挑战 49 切尔诺贝利): 「解毒后的一段时间内不再中毒」。
 *
 * <h3>为什么不直接用 BlobImmunity</h3>
 * 原版净化药水给的是 {@link BlobImmunity} —— 那是**对所有气体免疫**
 * （毒气、麻痹气、腐蚀气…）。勾选 49 后全图都是毒气，
 * 一瓶全免疫的净化药水就能让玩家横穿整层，这条规则会形同虚设。
 *
 * <p>所以按原表"净化药水改为只有解毒功能"，
 * 换成一个**只挡中毒**的专属 buff：
 * <ul>
 *   <li>{@link Poison#attachTo} 会检查本 buff，存在则拒绝挂上中毒</li>
 *   <li>其它气体仍会正常生效</li>
 * </ul>
 *
 * <p>时长由 {@code ChallengeEffects.purityDuration()} 决定
 * （勾选 49 时为 300 回合）。
 */
public class PoisonImmunity extends FlavourBuff {

	{
		type = buffType.POSITIVE;
	}

	public static final float DURATION = 300f;

	@Override
	public int icon() {
		return BuffIndicator.IMMUNITY;
	}

	@Override
	public void tintIcon(Image icon) {
		//偏绿的色调 —— 与"解毒"语义呼应
		icon.hardlight(0.4f, 1f, 0.4f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	/**
	 * END(修复 49): 免疫**毒气地块的伤害**。
	 *
	 * <h3>原先漏了什么</h3>
	 * 这个 buff 原本只靠 {@code Poison.attachTo()} 拒绝中毒，
	 * 但那挡不住**站在毒气格子上每回合直接掉血** ——
	 * {@code ToxicGas.evolve()} 里是直接 {@code ch.damage(...)}，
	 * 根本不经过 {@code Poison} 这个 buff。
	 *
	 * <p>表现就是玩家用了净化药水，还是一格一格掉血。
	 *
	 * <h3>为什么返回 ToxicGas.class 而不是 BlobImmunity</h3>
	 * {@code Char.isImmune()} 会收集所有 buff 的 {@code immunities()}，
	 * 所以只要在这里列出 {@code ToxicGas}，那一处判定就会放过玩家。
	 * 而**不用**原版的 {@code BlobImmunity}（那是全气体免疫）——
	 * 勾选 49 后全图毒气，一瓶全免疫的药水会让规则形同虚设。
	 */
	@Override
	public java.util.HashSet<Class> immunities() {
		java.util.HashSet<Class> set = new java.util.HashSet<>();
		set.add(com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas.class);
		return set;
	}
}
