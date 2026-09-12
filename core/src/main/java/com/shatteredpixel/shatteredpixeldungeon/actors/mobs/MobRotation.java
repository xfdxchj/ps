package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * END(port from Arknights): 方舟的刷怪表。
 *
 * <p>方舟原版有 317 行，按 26-45F 逐层硬编码，并引用 60+ 个怪。
 * 本移植版**只覆盖挑战区层段**（26F 及以后），且只使用已搬运的 22 个怪：
 * <ul>
 *   <li>{@code extrastage_Sea} → 伊比利亚·海嗣怪</li>
 *   <li>{@code extrastage_Gavial} → 嘉维尔·雨林怪</li>
 *   <li>两者都不选 → 汐斯塔·海滨怪（方舟原逻辑）</li>
 * </ul>
 *
 * <p>层内强度按 floorIn 递增（前段少而弱、后段多而强），与原版节奏一致。
 */
public final class MobRotation {

	private MobRotation() {}

	/** 是否应接管该层的刷怪（挑战区层段且选中了方舟区）。 */
	public static boolean handles(int depth) {
		int[] info = ChallengeArea.areaAtDepth(depth);
		if (info == null) return false;
		int id = info[0];
		return id == ChallengeArea.IBERIA.id
				|| id == ChallengeArea.GAVIAL.id
				|| id == ChallengeArea.SIESTA.id;
	}

	/** 主入口：返回该层要刷的怪物列表（未打乱）。 */
	public static ArrayList<Class<? extends Mob>> getMobRotation(int depth) {
		int[] info = ChallengeArea.areaAtDepth(depth);
		int floorIn = (info != null) ? info[1] : 0;
		int id = (info != null) ? info[0] : -1;

		if (id == ChallengeArea.IBERIA.id)   return seaRotation(floorIn);
		if (id == ChallengeArea.GAVIAL.id)   return gavialRotation(floorIn);
		if (id == ChallengeArea.SIESTA.id)   return siestaRotation(floorIn);

		//不在挑战区内 → 交回本 fork 原有逻辑
		return MobSpawner.getMobRotation(depth);
	}

	//==== 伊比利亚·海嗣 ====
	private static ArrayList<Class<? extends Mob>> seaRotation(int f) {
		if (f <= 1) return list(SeaRunner.class, SeaRunner.class, SeaRunner.class);
		if (f <= 3) return list(SeaRunner.class, SeaRunner.class, FloatingSeaDrifter.class, FloatingSeaDrifter.class);
		if (f <= 5) return list(SeaRunner.class, FloatingSeaDrifter.class, FloatingSeaDrifter.class, SeaCapsule.class);
		if (f <= 7) return list(FloatingSeaDrifter.class, SeaReaper.class, SeaReaper.class, SeaCapsule.class, SeaCapsule.class);
		return list(SeaReaper.class, SeaReaper.class, SeaCapsule.class, Sea_Octo.class, SeaLeef.class, NetherseaBrandguider.class);
	}

	//==== 嘉维尔·雨林 ====
	private static ArrayList<Class<? extends Mob>> gavialRotation(int f) {
		if (f <= 1) return list(TiacauhWarrior.class, TiacauhWarrior.class, TiacauhWarrior.class);
		if (f <= 3) return list(TiacauhWarrior.class, TiacauhWarrior.class, TiacauhFanatic.class, TiacauhFanatic.class);
		if (f <= 5) return list(TiacauhWarrior.class, TiacauhFanatic.class, TiacauhLancer.class, TiacauhLancer.class, TiacauhRipper.class);
		if (f <= 7) return list(TiacauhFanatic.class, TiacauhLancer.class, TiacauhAddict.class, TiacauhBrave.class, TiacauhShredder.class);
		return list(TiacauhLancer.class, TiacauhAddict.class, TiacauhBrave.class, TiacauhRitualist.class,
				TiacauhShaman.class, TiacauhSniper.class, TiacauhShredder.class);
	}

	//==== 汐斯塔·海滨 ====
	private static ArrayList<Class<? extends Mob>> siestaRotation(int f) {
		if (f <= 1) return list(Infantry.class, Infantry.class, Infantry.class);
		if (f <= 3) return list(Infantry.class, Infantry.class, Ergate.class, Ergate.class);
		if (f <= 5) return list(Infantry.class, Ergate.class, Piersailor.class, Piersailor.class, Sniper.class);
		if (f <= 7) return list(Ergate.class, Sniper.class, Sniper.class, Piersailor.class, Piersailor.class, Agent.class);
		return list(Sniper.class, Sniper.class, Piersailor.class, Agent.class, Agent.class, Ergate.class, Infantry.class);
	}

	@SafeVarargs
	private static ArrayList<Class<? extends Mob>> list(Class<? extends Mob>... cs) {
		return new ArrayList<>(Arrays.asList(cs));
	}
}
