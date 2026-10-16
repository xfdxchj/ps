/*
 * 破碎的地牢 (End fork) — 启动自检。
 *
 * 目的：把"只有玩到才会发现"的低级错误，在开发期就写进日志（pd_log.txt）。
 * 当前检查：
 *   1. 挑战 ID 是否重复
 *   2. 挑战关系（互斥/联动/限制/前置）是否指向不存在的 ID
 *   3. 挑战总数与分组数（基线，方便对比改动前后）
 *
 * 纯只读，不改任何游戏状态；出错也只写日志，不影响启动。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent;

import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeDef;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRegistry;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRelation;

import java.util.HashSet;

public final class SelfCheck {

	private SelfCheck() {}

	public static void run(){
		int problems = 0;
		try {
			problems += checkChallenges();
		} catch (Throwable t){
			log("[自检] 挑战自检异常: " + t);
			problems++;
		}

		log("[自检] 完成，发现 " + problems + " 处问题");
	}

	private static int checkChallenges(){
		int p = 0;
		HashSet<Integer> seen = new HashSet<>();

		for (ChallengeDef d : ChallengeRegistry.ALL){
			if (!seen.add(d.id)){
				report("挑战 ID 重复: " + d.id + " (" + d.name + ")");
				p++;
			}
			if (d.relations == null) continue;
			for (ChallengeRelation r : d.relations){
				if (r == null || r.targets == null) continue;
				for (int t : r.targets){
					if (ChallengeRegistry.byId(t) == null){
						report("挑战 " + d.id + "(" + d.name + ") 的 "
								+ r.type + " 指向不存在的 ID " + t);
						p++;
					}
					if (t == d.id){
						report("挑战 " + d.id + "(" + d.name + ") 的关系指向自身");
						p++;
					}
				}
			}
		}

		log("[自检] 挑战 " + ChallengeRegistry.ALL.size()
				+ " 条 / 分组 " + ChallengeRegistry.groups().size() + " 个");
		return p;
	}

	private static void report(String msg){
		log("[自检] " + msg);
	}

	private static void log(String msg){
		try { CrashLog.record(msg); } catch (Throwable ignored) {}
		try {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg
					.log(com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.LEVEL, msg);
		} catch (Throwable ignored) {}
	}
}
