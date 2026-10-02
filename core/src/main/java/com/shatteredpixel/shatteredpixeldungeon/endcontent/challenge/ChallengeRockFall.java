/*
 * 破碎的地牢 (End fork) — 挑战 76「原始状态」的投石
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * END(挑战 76 原始状态): 非远程怪物扔出的石头。
 *
 * <p>按用户要求改成**投掷物**表现：用战士的 {@link ThrowingStone}
 * 走 {@link MissileSprite} 飞出去，而不是落石陷阱那种从天而降。
 */
public final class ChallengeRockFall {

	private ChallengeRockFall() {}

	/**
	 * 让某只怪物朝目标扔一块石头。
	 *
	 * @return true 表示确实发起了投石
	 */
	public static boolean throwRockAt(Char attacker, Char target) {
		if (attacker == null || target == null) return false;
		if (Dungeon.level == null) return false;
		if (!attacker.isAlive() || !target.isAlive()) return false;
		if (attacker.sprite == null || attacker.sprite.parent == null) return false;

		final int dmgMin = Math.max(1, attacker.damageRoll() / 2);
		final int dmgMax = Math.max(dmgMin, attacker.damageRoll());

		try {
			ThrowingStone stone = new ThrowingStone();
			stone.quantity(1);
			MissileSprite missile = (MissileSprite) attacker.sprite.parent
					.recycle(MissileSprite.class);
			final Char src = attacker;
			missile.reset(attacker.sprite, target.pos, stone, new Callback() {
				@Override public void call(){
					if (!target.isAlive()) return;
					if (Random.Float() < 0.75f){
						target.damage(Random.NormalIntRange(dmgMin, dmgMax), src);
						Sample.INSTANCE.play(Assets.Sounds.HIT, 1f, 1.1f);
					} else {
						Sample.INSTANCE.play(Assets.Sounds.MISS);
					}
				}
			});
			return true;
		} catch (Throwable t) {
			return false;
		}
	}
}
