/*
 * 破碎的地牢 (End fork) — 挑战 131「格林之敌」的文本替换层
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects;

/**
 * END(挑战 131 格林之敌): 把古神（Yog-Dzewa）的所有文本换成「莉耶芙」。
 *
 * <h3>原表效果</h3>
 * "将第 5 区域最终 Boss'古神'的贴图替换为《Black Souls》角色'莉耶芙'，
 *   同时替换其 Boss 介绍、图鉴文本、战斗内对话与相关描述文本。
 *   纯外观与文本替换，不影响战斗数值与机制。"
 *
 * <h3>为什么需要这一层</h3>
 * 游戏取文本一律走 {@code Messages.get(mob, "key")}，
 * 而 {@code Messages} 是按**类名**拼 key 的 —— 没有"按条件换文本"的能力。
 *
 * <p>所以这里的做法是：在 {@code YogDzewa} 里把每一处
 * {@code Messages.get(this, "xxx")} 换成
 * {@code GrimmText.yog("xxx", 原文)}。
 * 未勾选 131 时原样返回 {@code fallback}，一行都不变。
 *
 * <h3>莉耶芙是谁</h3>
 * 按文档所有者给的设定：**她没有对应的童话原型** ——
 * 她的真实身份是**玛丽·苏**，是篡改童话的主凶，并非童话的"剧中人"。
 * 所以她的台词刻意带着"作者"的口吻：她知道自己在一个被写出来的世界里。
 */
public class GrimmText {

	/** 131 格林之敌。 */
	public static final int GRIMM_ENEMY = 131;

	/** END(131): 是否启用莉耶芙文本替换。 */
	public static boolean grimmEnemyEnabled() {
		//ChallengeEffects.on() 是 private，所以这里直接查 mask ——
		//与 ChallengeEffects 内部用的是同一个判据（bit 索引 = 表 ID）。
		try {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask m =
					com.shatteredpixel.shatteredpixeldungeon.Dungeon.challengeMask;
			return m != null && m.has(GRIMM_ENEMY);
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * END(131): 取古神某条文本 —— 勾选 131 时返回莉耶芙的版本。
	 *
	 * @param key      原本的文本 key（如 "notice" / "hope" / "desc"）
	 * @param fallback 未勾选 131 时使用的原文
	 * @return 应当显示的文本
	 */
	public static String yog(String key, String fallback) {
		if (!grimmEnemyEnabled()) return fallback;
		return yogOverride(key, fallback);
	}

	/**
	 * END(131): 莉耶芙版本的文本表。
	 *
	 * <h3>台词来源</h3>
	 * notice / hope / darkness / defeated / desc 这五处用的是
	 * **文档所有者提供的正式台词**（逐字，未改写）：
	 * <pre>
	 *   格林……你来了呢呢。
	 *   嘻嘻，你以为这是童话吗？人家可是作者哦。是登场人物，是黑幕，又或者是公主大人。
	 *   书页正在合拢……这场戏，可得好好演下去才行。
	 *   原来如此，那就换一个童话吧。
	 *   ——人家是玛丽·苏，是把那些童话，全都染上绝望的，坏心眼的主凶啦♪
	 * </pre>
	 */
	private static String yogOverride(String key, String fallback) {
		switch (key) {
			case "name":
				return "莉耶芙";

			case "notice":
				//战斗开场：她认得你（"格林"是玩家/主角的代称）
				return "格林……你来了呢呢。";

			case "hope":
				//原版是"汝之希望皆为虚妄" —— 她的版本自曝作者身份
				return "嘻嘻，你以为这是童话吗？人家可是作者哦。" +
						"是登场人物，是黑幕，又或者是公主大人。";

			case "darkness":
				return "书页正在合拢……这场戏，可得好好演下去才行。";

			case "defeated":
				//原版是 "..." —— 她输得毫不在意，因为"换个童话"就行
				return "原来如此，那就换一个童话吧。";

			case "rankings_desc":
				return "被莉耶芙改写了结局";

			case "desc":
				//图鉴文本：正式台词的最后一句 + 一句设定说明
				return "——人家是玛丽·苏，是把那些童话，全都染上绝望的，" +
						"坏心眼的主凶啦♪\n\n" +
						"莉耶芙没有对应的童话。她不是任何故事的「剧中人」，\n" +
						"而是握着笔的那只手 —— 她篡改童话，也篡改读童话的人。\n\n" +
						"这只巨眼是她落在这个世界上的墨点：她透过它看你们\n" +
						"在她的稿纸上挣扎、相爱、死去，然后翻到下一页。";

			case "desc_spawners":
				return "你能感觉到恶魔能量正从上层涌来。_但这次不一样 —— " +
						"那不是古神在召唤仆从，而是**作者在补写角色**。_" +
						"莉耶芙会用这股能量召唤更强大的手下！_";

			case "larva_name":
				return "未完成的草稿";

			case "larva_desc":
				return "这些是还没写完的角色。作者只描了个轮廓就丢在了一边，\n" +
						"于是它们只剩下「想要存在」这一个本能。\n\n" +
						"单个很脆弱，但作者随时可以再写几个出来。";

			case "fist_invuln_warn":
				return "拳头邻接莉耶芙时无法受到伤害！";

			case "fist_desc":
				return "这些拳头是莉耶芙力量的延伸 —— 更准确地说，\n" +
						"是她写在稿纸边角的批注。它们靠近主文时会免疫一切伤害。";

			default:
				return fallback;
		}
	}

	//==================================================================
	//132 黑暗之魂：古神护符 → 爱丽丝
	//==================================================================

	/** 132 黑暗之魂。 */
	public static final int GRIM_DARK_SOUL = 132;

	/**
	 * END(132): 黑暗之魂是否启用 —— 需要**全部格林系列规则**都勾上。
	 *
	 * <p>原表限制："需全部勾选格林系列规则（125~131）后方可解锁并启用"。
	 *
	 * <p>这里**自己再判一次**，而不是依赖注册表的互斥/前置声明：
	 * 注册表的 `p:` 关系只在 UI 上置灰，如果存档里已经带了 132
	 * （例如升级前后勾选状态不同），UI 层是拦不住的。
	 * 所以在**实际生效的地方**也要判，两道保险。
	 */
	public static boolean darkSoulEnabled() {
		try {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask m =
					com.shatteredpixel.shatteredpixeldungeon.Dungeon.challengeMask;
			if (m == null || !m.has(GRIM_DARK_SOUL)) return false;

			//必须同时勾选 125~131 这 7 条
			for (int id = 125; id <= 131; id++) {
				if (!m.has(id)) return false;
			}
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * END(132): 古神护符的文本替换（爱丽丝版本）。
	 *
	 * <h3>为什么护符会变成爱丽丝</h3>
	 * 按原表："古神护符改为爱丽丝贴图与文本"。
	 * 结合 129 的设定（爱丽丝发觉整个世界是伪造的、是别人写出来的妄想），
	 * 这里的逻辑是自洽的：**护符就是那本书本身** ——
	 * 你一路捡到的东西，其实就是让你来到这个世界的那个东西。
	 *
	 * <p>所以文本不写"护符的力量"，而是写"这是一本书"。
	 */
	public static String amulet(String key, String fallback) {
		if (!darkSoulEnabled()) return fallback;

		switch (key) {
			case "name":
				return "爱丽丝";

			case "desc":
				return "这不是护符。\n\n" +
						"它是一本书 —— 封面上印着一个低垂着眼的少女。\n" +
						"你翻过它，然后又翻了回来，反复许多次，\n" +
						"但每次合上时，书里的字都少了一点。";

			case "desc_origins":
				return "没人知道这本书是从哪来的。\n" +
						"矮人国王说他在地牢最深处「捡到」了它 —— 但更可能的是，\n" +
						"**是它捡到了他**。\n\n" +
						"莉耶芙从书页间撕下过几页，于是有了那些童话残片。\n" +
						"而你现在手里拿着的，是被撕剩下的部分。";

			case "desc_ascent":
				return "书页正在你一页一页地往回翻。\n" +
						"你走过的每一寸地牢都开始褪色，像是被人从稿纸上擦掉。\n" +
						"前面的敌人变得又多又强 —— 因为它们是被临时添上去的。\n" +
						"你既不能使用它，也不能丢下它。";

			case "ascent_title":
				return "逆读";

			case "ascent_desc":
				return "你开始听见翻页的声音。\n\n" +
						"如果你想带着这本书爬回地面，路会比你以为的难走得多。\n" +
						"地牢会变得更凶险，跨层传送会被抑制，\n" +
						"**杀出一条路**会是你回到地面的唯一方式。\n\n" +
						"如果你只是想在不开护符挑战的情况下上去，\n" +
						"可以把书留在这里，或者干脆在这里合上它、结束这一局。";

			case "ascent_yes":
				return "继续往上翻";

			case "ascent_no":
				return "再等等";

			case "discover_hint":
				return "你可以在最深处找到那本书……";

			default:
				return fallback;
		}
	}

	/** END(132): 取护符的贴图索引（护符 → 爱丽丝）。 */
	public static int amuletImage(int original) {
		if (!darkSoulEnabled()) return original;
		return com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet
				.GRIMM_BELOVED_GIRL;
	}
}
