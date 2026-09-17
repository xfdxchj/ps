/*
 * 破碎的地牢 (End fork) — 挑战规则随机生成
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * END(挑战框架): 按**目标通过等级**随机生成一组挑战规则。
 *
 * <h3>要解决的问题</h3>
 * 玩家想"随机来一局 X 分的挑战"，但组合不能乱来：
 * <ul>
 *   <li>抽出的规则之间**不能互斥**（如 32 通货膨胀 + 37 高价回收）</li>
 *   <li>**前置必须满足**（132 黑暗之魂需 125~131 全选）</li>
 *   <li>**等级总和必须恰好等于**目标分</li>
 * </ul>
 *
 * <h3>算法</h3>
 * 这是一个带约束的子集和问题。用「随机贪心 + 约束回溯」：
 * <ol>
 *   <li>把候选规则打乱，逐条尝试加入</li>
 *   <li>加之前检查：互斥冲突？前置满足？加了会不会超出目标分？</li>
 *   <li>超出就跳过，直到凑满目标分</li>
 *   <li>凑不满则整体重试（多轮随机），仍失败就返回当前最好结果</li>
 * </ol>
 *
 * <p>**为什么不用精确算法**：规则只有 118 条、等级只有 1/2/3 三档，
 * 随机重试几十轮几乎总能命中；而精确子集和要枚举 2^118 种组合，不现实。
 * 这里用有限轮数换取实现简单与结果多样（每次点结果都不一样）。
 */
public final class ChallengeRandomizer {

	/** 单轮最多尝试的规则数（防止极端情况下空转）。 */
	private static final int MAX_ATTEMPTS_PER_ROUND = 400;
	/** 最多重试轮数。 */
	private static final int MAX_ROUNDS = 120;

	/** 可选的最低目标分。 */
	public static final int MIN_TARGET = 1;

	/**
	 * 某个候选池能达到的**最高通过等级**。
	 *
	 * <p>这不是可有可无的装饰：只算已实装规则时全部加起来也只有 22 分，
	 * 玩家若填 30 分会永远凑不满，且界面上看不出原因。UI 必须用它限制输入上限。
	 *
	 * @param includePending 是否把未实装规则也算进候选
	 */
	public static int maxTarget(boolean includePending) {
		int sum = 0;
		for (ChallengeDef def : ChallengeRegistry.ALL) {
			if (!def.countsForLevel || def.level <= 0) continue;
			if (!includePending && !def.isImplemented()) continue;
			sum += def.level;
		}
		return sum;
	}

	private ChallengeRandomizer() {}

	/**
	 * 随机生成一组挑战，使 {@code passLevel()} 恰好等于 {@code targetLevel}。
	 *
	 * @param targetLevel 目标通过等级
	 * @param includePending 是否把未实装规则也纳入候选（默认建议 false，
	 *                       因为未实装规则勾了没有效果）
	 * @return 生成的掩码；无解时返回**最接近目标且不超过**的结果
	 */
	public static ChallengeMask roll(int targetLevel, boolean includePending) {
		if (targetLevel <= 0) return ChallengeMask.empty();

		//候选池：只保留"可计数"的正式规则（排除便利测试包这类 countsForLevel=false）
		List<ChallengeDef> pool = new ArrayList<>();
		for (ChallengeDef def : ChallengeRegistry.ALL) {
			if (!def.countsForLevel) continue;
			if (def.level <= 0) continue;
			if (!includePending && !def.isImplemented()) continue;
			pool.add(def);
		}
		if (pool.isEmpty()) return ChallengeMask.empty();

		ChallengeMask best = ChallengeMask.empty();

		for (int round = 0; round < MAX_ROUNDS; round++) {

			List<ChallengeDef> shuffled = new ArrayList<>(pool);
			//每轮换一个种子打乱顺序，保证"再点一次"会有不同结果。
			Collections.shuffle(shuffled, new java.util.Random());

			ChallengeMask cur = ChallengeMask.empty();
			int sum = 0;
			int attempts = 0;

			for (ChallengeDef def : shuffled) {
				if (sum == targetLevel) break;
				if (attempts++ > MAX_ATTEMPTS_PER_ROUND) break;

				int lv = def.level;
				if (lv <= 0) continue;
				if (sum + lv > targetLevel) continue;      // 会超出目标分

				//约束 1：与已选规则互斥
				if (!def.conflictingWith(cur).isEmpty()) continue;

				//约束 2：前置必须已满足（在本轮已选范围内判断）
				if (!def.prerequisitesMet(cur)) continue;

				//约束 3：反向检查 —— 已选规则里不能有"要求本条"的前置
				//（例如先抽到 132，后面才抽到 125；132 要求 125~131 全选，
				//  此时加 125 是安全的，但反过来若 132 已在集合里而 126 缺，就不该收尾）
				if (breaksOthersPrerequisite(def, cur, targetLevel)) continue;

				cur = cur.with(def.id);
				sum += lv;
			}

			if (sum == targetLevel) {
				return cur;      // 恰好命中，直接返回
			}

			//记录"最接近但不超过"的结果作为兜底
			if (sum > best.passLevel() && sum <= targetLevel) {
				best = cur;
			}
		}

		return best;
	}

	/**
	 * 加入 {@code candidate} 后，是否会让**已选规则的前置条件**变得不可能满足。
	 *
	 * <p>场景：已选里有 132（要求 125~131 全选），此时集合里还缺 126。
	 * 如果候选是别的规则、把分数占满导致 126 再也塞不进去，那这局就是坏的。
	 * 这里做一个保守判断：若存在"前置未满足的已选规则"，
	 * 且剩余分数不足以补齐它的前置，则拒绝该候选。
	 */
	private static boolean breaksOthersPrerequisite(ChallengeDef candidate,
													ChallengeMask cur,
													int targetLevel) {
		int remainingAfter = targetLevel - cur.passLevel() - candidate.level;

		for (ChallengeDef d : ChallengeRegistry.ALL) {
			if (!cur.has(d.id)) continue;
			if (d.prerequisitesMet(cur)) continue;

			//这条已选规则的前置没满足 —— 算它缺多少分才能补齐
			int need = 0;
			for (ChallengeRelation r : d.relationsOf(ChallengeRelation.Type.PREREQUISITE)) {
				for (int id : r.targets) {
					if (cur.has(id)) continue;
					ChallengeDef missing = ChallengeRegistry.byId(id);
					if (missing != null && missing.countsForLevel) need += missing.level;
				}
			}
			if (need > remainingAfter) return true;   // 补不齐了，别占分
		}
		return false;
	}

	/**
	 * 给出几个常见的目标分档位，供 UI 做快捷按钮。
	 *
	 * <p>档位会根据**当前候选池的可达上限**裁剪 —— 只算已实装时上限只有 22，
	 * 就不该给出 30 这个永远凑不满的选项。
	 */
	public static int[] suggestedTargets(boolean includePending) {
		int max = maxTarget(includePending);
		int[] wanted = { 3, 6, 10, 15, 20, 30, 45, 60 };

		java.util.List<Integer> out = new java.util.ArrayList<>();
		for (int w : wanted) {
			if (w <= max) out.add(w);
		}
		if (out.isEmpty()) out.add(Math.max(1, max));

		int[] arr = new int[out.size()];
		for (int i = 0; i < arr.length; i++) arr[i] = out.get(i);
		return arr;
	}
}
