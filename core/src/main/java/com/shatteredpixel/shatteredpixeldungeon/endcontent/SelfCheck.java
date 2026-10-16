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

		try {
			problems += checkChallengeTexts();
		} catch (Throwable t){
			log("[自检] 挑战文案自检异常: " + t);
			problems++;
		}

		try {
			problems += checkFrames();
		} catch (Throwable t){
			log("[自检] 贴图自检异常: " + t);
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

	/** 检查 ItemSpriteSheet 里 END 新增的帧号 / 帧序列是否越界。 */
	private static int checkFrames(){
		int p = 0;
		Class<?> c = com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.class;
		for (java.lang.reflect.Field f : c.getFields()){
			String n = f.getName();
			boolean interesting = n.startsWith("GRIMM_") || n.startsWith("END_")
					|| n.startsWith("INFINITY_") || n.startsWith("HOLLOW_")
					|| n.endsWith("_FRAMES");
			if (!interesting) continue;
			try {
				if (f.getType() == int.class){
					int v = f.getInt(null);
					if (!com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.hasFrame(v)){
						report("贴图帧越界: " + n + " = " + v);
						p++;
					}
				} else if (f.getType() == int[].class){
					int[] arr = (int[]) f.get(null);
					if (arr == null) continue;
					for (int i = 0; i < arr.length; i++){
						if (!com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.hasFrame(arr[i])){
							report("贴图帧越界: " + n + "[" + i + "] = " + arr[i]);
							p++;
						}
					}
				}
			} catch (Throwable ignored) {}
		}
		return p;
	}

	/** 检查挑战 key / 名字重复，以及"既无 effect 又无 _desc 文案"。 */
	private static int checkChallengeTexts(){
		int p = 0;
		HashSet<String> keys = new HashSet<>();
		HashSet<String> names = new HashSet<>();
		boolean messagesOk = true;

		for (ChallengeDef d : ChallengeRegistry.ALL){
			if (d.key != null && !keys.add(d.key)){
				report("挑战 key 重复: " + d.key + " (id " + d.id + ")");
				p++;
			}
			if (d.name != null && !names.add(d.name)){
				report("挑战名重复: " + d.name + " (id " + d.id + ")");
				p++;
			}

			boolean hasEffect = d.effect != null && !d.effect.isEmpty();
			if (hasEffect || !messagesOk) continue;

			try {
				if (!com.shatteredpixel.shatteredpixeldungeon.messages.Messages
						.exists(null, "challenges." + d.key + "_desc")){
					report("挑战 " + d.id + "(" + d.name
							+ ") 既无 effect 也无 challenges." + d.key + "_desc 文案");
					p++;
				}
			} catch (Throwable t){
				//离线运行（无 Gdx）时 Messages 不可用，直接跳过文案检查
				messagesOk = false;
				log("[自检] 文案检查跳过：Messages 不可用（仅离线运行时会出现）");
			}
		}
		return p;
	}
}
