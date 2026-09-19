/*
 * 破碎的地牢 (End fork) — 挑战 49「切尔诺贝利」的毒气维持器
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

/**
 * END(挑战 49 切尔诺贝利): 在玩家周围持续刷新毒气。
 *
 * <h3>为什么需要它</h3>
 * {@code Blob} 的衰减公式是（见 {@code Blob.evolve()}）：
 * <pre>
 *   value = sum >= count ? (sum / count) - 1 : 0;
 * </pre>
 * **每回合每格 -1**。所以一次性铺下去的毒气**很快就会散光**，
 * 规则等于失效 —— 这就是文档所有者反馈"毒气几回合就消失"的原因。
 *
 * <h3>文档所有者定稿的做法</h3>
 * "13% 概率在 9×9 随机刷新毒气" ——
 * 每回合以玩家为中心取 9×9 范围，**每格 13% 概率**补一股毒气。
 * 这样毒气会像"辐射云"一样跟着玩家走，而不是一次性铺满然后消失。
 *
 * <h3>挂载时机（第一版没生效的原因）</h3>
 * 原来在 {@code Level.create()} 末尾调用 {@code ensureRunning()}，
 * 但那时：
 * <ol>
 *   <li>{@code Dungeon.level} 还没赋值（在第 998 行才赋），
 *       所以 {@code if (Dungeon.level == null) return;} 直接返回了</li>
 *   <li>即使挂上，后面的 {@code Actor.init()} 也会清空全部 Actor</li>
 * </ol>
 * 现在改为在 {@code Dungeon.newLevel()} 的 {@code Actor.init()} **之后**挂载。
 */
public class ChernobylKeeper extends Actor {

	/** 每格补气的概率（%）。文档所有者定稿：13%。 */
	private static final int REFILL_PCT = 13;

	/** 以玩家为中心的半径（格）。9×9 → 半径 4。 */
	private static final int RADIUS = 4;

	/** 每格补的量。够撑到下次刷新即可（衰减每回合 -1）。 */
	private static final int REFILL_AMOUNT = 12;

	/** 检查间隔（回合）。1 = 每回合都刷。 */
	private static final float INTERVAL = 1f;

	{
		//和 MobSpawner 一样，当作 buff 优先级 —— 在怪物行动之前补好
		actPriority = BUFF_PRIO;
	}

	@Override
	protected boolean act() {

		//规则已关闭 → 停止运行（返回 false 让 Actor 自动移除自己）
		if (!ChallengeEffects.chernobylEnabled()) {
			instance = null;
			return false;
		}

		if (Dungeon.level != null && Dungeon.hero != null) {
			refillAroundHero();
		}

		spend(INTERVAL);
		return true;
	}

	/**
	 * END(49): 在玩家周围 9×9 范围内随机刷毒气。
	 *
	 * <p>每格独立掷 13% —— 所以是"稀疏的辐射云"，
	 * 而不是"整片毒气墙"。玩家仍然能走动，只是处处危险。
	 */
	private void refillAroundHero() {
		try {
			int w = Dungeon.level.width();
			int h = Dungeon.level.height();
			int center = Dungeon.hero.pos;

			int cx = center % w;
			int cy = center / w;

			for (int dy = -RADIUS; dy <= RADIUS; dy++) {
				for (int dx = -RADIUS; dx <= RADIUS; dx++) {
					int x = cx + dx;
					int y = cy + dy;
					if (x < 0 || y < 0 || x >= w || y >= h) continue;

					int cell = x + y * w;

					//只铺可站立的普通地面
					if (Dungeon.level.solid[cell]) continue;
					if (!Dungeon.level.passable[cell]) continue;

					//每格独立判定
					if (Random.Int(100) >= REFILL_PCT) continue;

					int have = Blob.volumeAt(cell, ToxicGas.class);
					if (have >= REFILL_AMOUNT) continue;

					Blob.seed(cell, REFILL_AMOUNT - have, ToxicGas.class, Dungeon.level);
				}
			}
		} catch (Throwable t) {
			//补气失败绝不能把游戏拖崩 —— 那只是一层氛围效果
		}
	}

	//==================================================================
	//挂载
	//==================================================================

	private static ChernobylKeeper instance = null;

	/**
	 * END(49): 确保维持器正在运行。
	 *
	 * <p><b>必须在 {@code Actor.init()} 之后调用</b> ——
	 * 那个方法会清空所有 Actor，早挂的会被抹掉。
	 *
	 * <p>为什么用"层的引用"判断是否需要重挂：
	 * {@code Actor.all} 是 private，外部查不到；
	 * 而 {@code Actor.init()} 清空后 {@code actor.id} 会变，
	 * 所以这里用**静态引用 + 层哈希**来判断"是不是同一层的同一个实例"。
	 */
	public static void ensureRunning() {
		if (!ChallengeEffects.chernobylEnabled()) return;

		//同一层重复调用 → 不重复挂
		if (instance != null && instance.levelRef == System.identityHashCode(Dungeon.level)) {
			//还要确认它确实还在 Actor 列表里（可能被 Actor.init 清掉了）
			//判据：重新 add 一次是无害的（Actor.add 会去重）
			Actor.add(instance);
			return;
		}

		instance = new ChernobylKeeper();
		instance.levelRef = System.identityHashCode(Dungeon.level);
		Actor.add(instance);
	}

	/** END(49): 记录挂载时所在的层（用来判断是否需要重挂）。 */
	private int levelRef = 0;
}
