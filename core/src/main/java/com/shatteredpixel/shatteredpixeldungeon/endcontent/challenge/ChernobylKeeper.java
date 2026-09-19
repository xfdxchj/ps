/*
 * 破碎的地牢 (End fork) — 挑战 49「切尔诺贝利」的毒气维持器
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

/**
 * END(挑战 49 切尔诺贝利): 每回合把毒气铺回来。
 *
 * <h3>为什么需要它</h3>
 * {@code Blob} 的衰减公式是（见 {@code Blob.evolve()}）：
 * <pre>
 *   value = sum >= count ? (sum / count) - 1 : 0;
 * </pre>
 * **每回合每格 -1**。所以 `Blob.seed(cell, 30, ToxicGas.class)` 铺下去的毒气
 * **30 回合后就彻底散光** —— 一层打完一半就没了，规则等于失效。
 *
 * <h3>做法（照位面日晷的模式）</h3>
 * {@code DimensionalSundial} 的思路是"不一次性算死，而是每次需要时查一次"。
 * 这里同理：**不把毒气当成一次性铺设的地块，而是每回合补一次**。
 *
 * <p>用一个常驻 {@code Actor}（类似 {@code MobSpawner} 的存在方式），
 * 每回合往还空着的格子里续一点毒气。
 * 未勾选 49 时这个 Actor 会立刻把自己移除，不产生任何开销。
 */
public class ChernobylKeeper extends Actor {

	/** 每回合给每格补的量。必须 > 1，否则补不过衰减。 */
	private static final int REFILL_AMOUNT = 40;

	/** 检查间隔（回合）。每回合全图遍历太浪费，隔几回合补一次即可。 */
	private static final float INTERVAL = 3f;

	{
		//和 MobSpawner 一样，当作 buff 优先级 —— 保证在怪物行动之前补好
		actPriority = BUFF_PRIO;
	}

	@Override
	protected boolean act() {

		//规则已关闭 → 停止运行（返回 false 让 Actor 自动移除自己）
		if (!ChallengeEffects.chernobylEnabled()) {
			instance = null;
			return false;
		}

		if (Dungeon.level != null) {
			refill();
		}

		spend(INTERVAL);
		return true;
	}

	/** END(49): 把毒气铺回所有空的普通地面格。 */
	private void refill() {
		try {
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.map[i] != Terrain.EMPTY) continue;

				//该格已经有毒气且够浓 → 跳过（省掉 Blob.seed 的开销）
				int have = Blob.volumeAt(i, ToxicGas.class);
				if (have >= REFILL_AMOUNT) continue;

				Blob.seed(i, REFILL_AMOUNT - have, ToxicGas.class, Dungeon.level);
			}
		} catch (Throwable t) {
			//补气失败绝不能把游戏拖崩 —— 那只是一层氛围效果
		}
	}

	//==================================================================
	//挂载
	//==================================================================

	private static ChernobylKeeper instance = null;

	/** END(49): 确保维持器正在运行（进入新层时调用）。 */
	public static void ensureRunning() {
		if (!ChallengeEffects.chernobylEnabled()) return;
		if (Dungeon.level == null) return;

		//Actor 是静态注册的，换层后旧实例可能还挂着 —— 先清掉
		if (instance != null) {
			instance = null;
		}
		instance = new ChernobylKeeper();
		Actor.add(instance);
	}
}
