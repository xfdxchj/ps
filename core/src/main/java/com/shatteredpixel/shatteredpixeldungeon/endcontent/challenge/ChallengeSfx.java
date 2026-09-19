/*
 * 破碎的地牢 (End fork) — 音频类挑战规则
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.HashSet;

/**
 * END(挑战·音频类): 7 条音效/BGM 挑战规则的统一实现。
 *
 * <h3>覆盖规则</h3>
 * <ul>
 *   <li><b>118 天意侵蚀</b> — 玩家每回合 13% 概率随机播一段新三国音效</li>
 *   <li><b>137 奶龙大笑</b> — 每回合 3% 播奶龙音效 + 冒一句台词</li>
 *   <li><b>70 生活部长</b> — 每回合 3% 停止行动 + 台词「首先，我是生活部部长」</li>
 *   <li><b>72 前程似锦</b> — 每回合 3% 停止行动 + 台词「王同学，我祝你前～程～似锦」</li>
 *   <li><b>96 奥利给</b> — 每回合 1% 停止行动 + 喊「奥利给」+ 1 回合狂暴</li>
 *   <li><b>95 耗子尾汁</b> — 闪避成功时 3% 概率反击 + 显示「耗子尾汁」</li>
 *   <li><b>130 格林之音</b> — 把所有 BGM 替换为格林（黑魂）主题</li>
 * </ul>
 *
 * <h3>为什么音效按需加载</h3>
 * 这些规则加起来有 31 个音频文件。全量预加载（塞进 {@code Assets.Sounds.all}）
 * 会拖慢每次启动，而绝大多数对局根本没勾选对应挑战。所以：
 * <ul>
 *   <li>开局（{@link #init}）只加载**已勾选**规则用到的文件</li>
 *   <li>{@code loaded} 集合防止重复 load（{@code Sample.load} 内部虽有去重，
 *       但每次调用都会进加载队列，重复调用会白白占队列）</li>
 * </ul>
 *
 * <h3>130 的拦截点</h3>
 * {@code Music.play(assetName, looping)} 是所有 BGM 的唯一入口，
 * 在那里把曲目名换成格林主题即可，不需要改任何关卡的播放代码。
 * 见 {@link #grimmTrackFor(String)}。
 */
public final class ChallengeSfx {

	private ChallengeSfx() {}

	//==== 规则 ID ====

	/** 70 生活部长。 */
	public static final int LIFE_MINISTER   = 70;
	/** 72 前程似锦。 */
	public static final int BRIGHT_FUTURE   = 72;
	/** 95 耗子尾汁。 */
	public static final int RAT_TAIL_SOUP   = 95;
	/** 96 奥利给。 */
	public static final int OLIGEI          = 96;
	/** 118 天意侵蚀。 */
	public static final int PROVIDENCE      = 118;
	/** 130 格林之音。 */
	public static final int GRIMM_MUSIC     = 130;
	/** 137 奶龙大笑。 */
	public static final int MILK_DRAGON     = 137;

	//==== 概率（百分比）====

	/**
	 * 118 天意侵蚀：每回合触发概率。
	 *
	 * <p>END(修订): 由 13% 下调为 **3%**（文档所有者要求）。
	 */
	private static final int CHANCE_PROVIDENCE  = 3;
	/** 137 奶龙大笑：每回合触发概率。 */
	private static final int CHANCE_MILK_DRAGON = 3;
	/** 70 生活部长：每回合触发概率。 */
	private static final int CHANCE_MINISTER    = 3;
	/** 72 前程似锦：每回合触发概率。 */
	private static final int CHANCE_FUTURE      = 3;
	/** 96 奥利给：每回合触发概率。 END(修订): 3% -> 1%（文档所有者实测过高）。 */
	private static final int CHANCE_OLIGEI      = 1;
	/** 95 耗子尾汁：每回合触发概率（原表原为"闪避成功时3%"，已按文档所有者要求改为每回合判定）。 */
	private static final int CHANCE_RAT_TAIL    = 3;

	/** 70/72 触发时的停止行动回合数。 */
	private static final float STOP_TURNS = 1f;
	/** 96 奥利给给的狂暴持续回合数。 */
	private static final float OLIGEI_RAGE_TURNS = 1f;

	//==== 已加载记录 ====

	private static final HashSet<String> loaded = new HashSet<>();

	//==== 便捷判断 ====

	private static boolean on(int id) {
		return Dungeon.challengeMask != null && Dungeon.challengeMask.has(id);
	}

	/**
	 * END(挑战·音频): 开局初始化 —— 只加载**已勾选**规则用到的音频。
	 *
	 * <p>调用点：{@code Dungeon.init()} —— 那时掩码已经从设置/存档读好了。
	 */
	public static void init() {

		loaded.clear();

		if (on(PROVIDENCE)) {
			loadAll(Assets.Sounds.CH_PROVIDENCE);
		}
		if (on(RAT_TAIL_SOUP)) {
			loadAll(Assets.Sounds.CH_HAOZIHAO);
		}
		if (on(MILK_DRAGON)) {
			loadOne(Assets.Sounds.CH_Nailong);
		}
		if (on(OLIGEI)) {
			loadOne(Assets.Sounds.CH_Oligei);
		}
		if (on(LIFE_MINISTER)) {
			loadOne(Assets.Sounds.CH_Minister);
		}
		if (on(BRIGHT_FUTURE)) {
			loadOne(Assets.Sounds.CH_Future);
		}
		//130 格林之音的 BGM 由 Music 播放时按需加载，这里不做预加载
		//（11 个文件、总计约 32MB，开局全读会明显卡顿）。

		//==== END(诊断·加载清单): 开局打印到底加载了哪些音效 ====
		if (DEBUG) {
			System.out.println("=== [挑战音效] init() 加载清单 ===");
			System.out.println("  掩码位：70=" + on(LIFE_MINISTER)
					+ " 72=" + on(BRIGHT_FUTURE)
					+ " 95=" + on(RAT_TAIL_SOUP)
					+ " 96=" + on(OLIGEI)
					+ " 118=" + on(PROVIDENCE)
					+ " 137=" + on(MILK_DRAGON));
			System.out.println("  已加载 " + loaded.size() + " 个音效文件：");
			for (String a : loaded) {
				System.out.println("    " + a);
			}
			if (loaded.isEmpty()) {
				System.out.println("    (空) —— 说明没有勾选任何音频类挑战，");
				System.out.println("          或 Dungeon.init() 时掩码还没准备好");
			}
		}
	}

	/**
	 * END(挑战·音频): 加载单个音效，**失败不抛异常**。
	 *
	 * <h3>为什么必须兜住</h3>
	 * {@code Sample.INSTANCE.load()} 在解码失败时会抛 {@code GdxRuntimeException}。
	 * 实测：若素材是 **Ogg FLAC**（而不是 libGDX 只支持的 Ogg Vorbis），
	 * 每个文件都会抛一次 —— 20 个文件刷屏 20 条堆栈，而且异常会从
	 * {@code Dungeon.init()} 一路冒到 {@code InterlevelScene.descend()}。
	 *
	 * <p>游戏本身还能继续（libGDX 在别处也 reportException），但：
	 * <ol>
	 *   <li>日志被刷爆，掩盖其它真正的问题</li>
	 *   <li>玩家看到"崩溃"式输出，以为是致命错误</li>
	 * </ol>
	 *
	 * <h3>loaded 的语义</h3>
	 * 只有**确实加载成功**才记入 {@code loaded}。
	 * 早期版本先 add 再 load，导致诊断日志把"我调用过 load"误报成"加载成功" ——
	 * 那次排查因此多绕了一圈。失败的另记入 {@code failed}，避免每次触发都重试并刷屏。
	 */
	private static final HashSet<String> failed = new HashSet<>();

	private static void loadOne(String asset) {
		if (asset == null) return;
		if (loaded.contains(asset) || failed.contains(asset)) return;

		try {
			Sample.INSTANCE.load(asset);
			loaded.add(asset);
		} catch (Throwable t) {
			failed.add(asset);
			//只报一行摘要，不打印完整堆栈 —— 格式错是素材问题，
			//堆栈对定位没有额外帮助，只会刷屏。
			System.err.println("[挑战音效] 加载失败（跳过）：" + asset
					+ " —— " + t.getClass().getSimpleName() + ": " + t.getMessage()
					+ "  [提示] libGDX 只支持 Ogg Vorbis，"
					+ "若素材是 Ogg FLAC / Opus / mp3 改后缀则无法解码");
		}
	}

	private static void loadAll(String[] assets) {
		for (String a : assets) loadOne(a);
	}

	//==== 每回合结算 ====

	/**
	 * END(挑战·音频): 玩家每回合结算时调用。
	 *
	 * <p>调用点：{@code Hero.act()} —— 返回 true 表示本回合**停止行动**
	 * （对应 70/72/96 的"停止行动"效果）。
	 *
	 * <p>触发顺序：按 ID 从小到大，先触发的先返回。
	 * 同一回合多条同时命中时只生效一条（避免一回合叠好几个音效）。
	 *
	 * @return 是否应停止本回合行动
	 */
	public static boolean onHeroTurn(Hero hero) {
		if (hero == null) return false;

		//==== END(诊断·每回合状态): 每 N 回合打印一次，避免刷屏 ====
		//关键：这里能直接看出**掩码里到底有没有勾这些规则**。
		//若全是 false，说明勾选没保存（UI→存档 的链路问题），
		//而不是音效或概率的问题。
		turnCounter++;
		if (DEBUG && turnCounter % DEBUG_TURN_INTERVAL == 0) {
			System.out.println("=== [挑战音效] 第 " + turnCounter + " 回合判定 ==="
					+ " 掩码位：70=" + on(LIFE_MINISTER)
					+ " 72=" + on(BRIGHT_FUTURE)
					+ " 95=" + on(RAT_TAIL_SOUP)
					+ " 96=" + on(OLIGEI)
					+ " 118=" + on(PROVIDENCE)
					+ " 137=" + on(MILK_DRAGON));
			System.out.println("    SPDSettings.soundFx() = "
					+ com.shatteredpixel.shatteredpixeldungeon.SPDSettings.soundFx()
					+ "   (false 则所有音效都不会响)");
			System.out.println("    challengeMask.isEmpty() = "
					+ (com.shatteredpixel.shatteredpixeldungeon.Dungeon.challengeMask == null
						? "null"
						: com.shatteredpixel.shatteredpixeldungeon.Dungeon.challengeMask.isEmpty()));
		}

		//---- 70 生活部长：3% 播音效 + 台词（**不**停止行动）----
		//END(修订): 按文档所有者要求，去掉"麻痹/停止回合"。
		//原来用 Paralysis 实现"停止行动"，但那条规则的本意只是**搞笑音效 + 台词**，
		//不该真的让玩家损失一个回合。
		if (on(LIFE_MINISTER) && Random.Int(100) < CHANCE_MINISTER) {
			play(Assets.Sounds.CH_Minister, "70 生活部长");
			say(hero, "minister_line");
			//不 return true —— 继续往下判断其它规则，本回合照常行动
		}

		//---- 72 前程似锦：3% 停止行动 + 台词 ----
		if (on(BRIGHT_FUTURE) && Random.Int(100) < CHANCE_FUTURE) {
			play(Assets.Sounds.CH_Future, "72 前程似锦");
			say(hero, "future_line");
			stopHero(hero, STOP_TURNS);
			return true;
		}

		//---- 96 奥利给：3% 停止行动 + 喊话 + 1 回合狂暴 ----
		if (on(OLIGEI) && Random.Int(100) < CHANCE_OLIGEI) {
			play(Assets.Sounds.CH_Oligei, "96 奥利给");
			say(hero, "oligei_line");
			//用 EndRageAttack（FlavourBuff，命中伤害 ×2）而不是 Fury：
			//Fury 是**条件** buff —— HP 高于 50% 就自动消失，不是计时 buff，
			//拿它做"1 回合狂暴"会得到"一直持续到回血"的错误语义。
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					hero,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.EndRageAttack.class,
					OLIGEI_RAGE_TURNS);
			stopHero(hero, STOP_TURNS);
			return true;
		}

		//---- 95 耗子尾汁：3% 播音效 + 显示"耗子尾汁"（**不**停止行动）----
		//END(修订): 原表写的是"闪避成功时3%概率反击"，按文档所有者要求
		//改为**每回合无条件判定**，与 70/72/96/137 一致的回合制触发。
		if (on(RAT_TAIL_SOUP) && Random.Int(100) < CHANCE_RAT_TAIL) {
			play(Assets.Sounds.CH_HAOZIHAO[Random.Int(Assets.Sounds.CH_HAOZIHAO.length)], "95 耗子尾汁");
			say(hero, "haozihao_line");
		}

		//---- 137 奶龙大笑：3% 播音效 + 台词（**不**停止行动）----
		if (on(MILK_DRAGON) && Random.Int(100) < CHANCE_MILK_DRAGON) {
			play(Assets.Sounds.CH_Nailong, "137 奶龙大笑");
			say(hero, "nailong_line");
			//不停止行动，继续往下判断 118
		}

		//---- 118 天意侵蚀：13% 随机播一段音效（**不**停止行动）----
		if (on(PROVIDENCE) && Random.Int(100) < CHANCE_PROVIDENCE) {
			play(Assets.Sounds.CH_PROVIDENCE[Random.Int(Assets.Sounds.CH_PROVIDENCE.length)], "118 天意侵蚀");
		}

		return false;
	}

	//==== 95 耗子尾汁：已改为回合制，见 onHeroTurn ====
	//（原设计是"闪避成功时触发"，由 Char.attack 未命中分支调用；
	//  按文档所有者要求改为每回合无条件判定后，此处不再需要独立入口。）

	//==== 130 格林之音：BGM 替换 ====

	/**
	 * END(130 格林之音): 把原版曲目映射到格林主题。
	 *
	 * <p>调用点：{@code Music.play()} 开头。未勾选 130 时**原样返回**，
	 * 因此对正常游戏零影响。
	 *
	 * <p>映射思路：按"曲目属于哪个区域"替换，而不是逐个曲目写死 ——
	 * 这样新增的区域音乐也会走同一套规则。
	 *
	 * @param original 原本要播放的资源路径
	 * @return 实际应播放的路径
	 */
	public static String grimmTrackFor(String original) {
		if (original == null) return null;

		//==== END(诊断 130): 把每一次曲目请求都打出来 ====
		//用来区分"没勾选 130"、"勾了但没匹配上"、"匹配了但没播"三种情况。
		//实测反馈"格林之音没有正常生效"，但代码路径看起来是通的，
		//所以先拿到真实数据再改。
		if (GRIMM_MUSIC_DEBUG) {
			System.out.println("[格林之音] 请求曲目: " + original
					+ "  130已勾选=" + on(GRIMM_MUSIC));
		}

		if (!on(GRIMM_MUSIC)) return original;

		//已经是格林曲目就不要再映射（幂等，防止二次替换）
		if (original.startsWith("music/grimm/")) return original;

		String mapped = mapToGrimm(original);

		if (GRIMM_MUSIC_DEBUG) {
			System.out.println("[格林之音] " + (mapped.equals(original) ? "未替换" : "替换为")
					+ ": " + mapped);
		}
		return mapped;
	}

	/** 诊断开关：排查 130 时置 true，定稿后改回 false。 */
	public static final boolean GRIMM_MUSIC_DEBUG = true;

	/** END(130): 实际的映射表。 */
	private static String mapToGrimm(String original) {
		String name = original;

		//---- 最终 Boss（古神 Yog-Dzewa）----
		if (name.equals(Assets.Music.HALLS_BOSS)
				|| name.equals(Assets.Music.HALLS_BOSS_FINALE)
				|| name.equals(Assets.Music.CITY_BOSS_FINALE)) {
			return Assets.Music.GRIMM_YOG_1;
		}

		//---- 各区域 Boss ----
		if (name.equals(Assets.Music.SEWERS_BOSS)) return Assets.Music.GRIMM_AREA1_BOSS;
		if (name.equals(Assets.Music.PRISON_BOSS)) return Assets.Music.GRIMM_AREA2_BOSS;
		if (name.equals(Assets.Music.CAVES_BOSS)
				|| name.equals(Assets.Music.CAVES_BOSS_FINALE)) return Assets.Music.GRIMM_AREA3_BOSS;
		if (name.equals(Assets.Music.CITY_BOSS)) return Assets.Music.GRIMM_AREA4_BOSS;

		//---- 通用 boss.ogg（挑战区 Boss 层在用，没有对应的 Assets 常量）----
		//直接按字面路径匹配：本 fork 的挑战区音乐是硬编码字符串。
		if (name.equals("music/boss.ogg"))    return Assets.Music.GRIMM_AREA1_BOSS;
		if (name.equals("music/boss2.ogg"))   return Assets.Music.GRIMM_AREA2_BOSS;
		if (name.equals("music/boss3.ogg"))   return Assets.Music.GRIMM_AREA3_BOSS;
		if (name.equals("music/boss4.ogg"))   return Assets.Music.GRIMM_AREA4_BOSS;
		if (name.equals("music/boss5.ogg"))   return Assets.Music.GRIMM_AREA5;

		//---- 各区域常规层（按路径前缀，覆盖 _1/_2/_3/_tense）----
		if (name.startsWith("music/sewers")) return Assets.Music.GRIMM_AREA1;
		if (name.startsWith("music/prison")) return Assets.Music.GRIMM_AREA2;
		if (name.startsWith("music/caves"))  return Assets.Music.GRIMM_AREA3;
		if (name.startsWith("music/city"))   return Assets.Music.GRIMM_AREA4;
		if (name.startsWith("music/halls"))  return Assets.Music.GRIMM_AREA5;

		//---- 通用 game.ogg / boss.ogg（挑战区与部分主线层在用）----
		if (name.equals("music/game.ogg"))   return Assets.Music.GRIMM_AREA5;

		//---- 标题 / 结局等非区域音乐：用 1 区主题兜底 ----
		if (name.equals(Assets.Music.THEME_1)
				|| name.equals(Assets.Music.THEME_2)
				|| name.equals(Assets.Music.THEME_FINALE)) {
			return Assets.Music.GRIMM_AREA1;
		}

		//其它（含各类 MOD 区域音乐）：不替换，避免把挑战区 BGM 也冲掉
		return original;
	}

	//==== 内部辅助 ====

	/**
	 * END(诊断·音效): 临时日志开关。
	 *
	 * <p>用来区分三种「没声音」：
	 * <ol>
	 *   <li>规则没触发（掩码里没勾 / 概率没中）</li>
	 *   <li>触发了但资源没加载成功（Sample.play 返回 -1）</li>
	 *   <li>加载播放都成功，但设备静音 / 音量 0 / 资源本身无声</li>
	 * </ol>
	 *
	 * <p>定稿后应把 {@link #DEBUG} 改为 false（或删掉相关打印）。
	 */
	public static final boolean DEBUG = true;

	/** 统计：本局各规则触发次数（诊断用）。 */
	public static final java.util.HashMap<String, Integer> triggerCount = new java.util.HashMap<>();

	/** 玩家回合计数（诊断用）。 */
	private static int turnCounter = 0;

	/** 每多少回合打印一次状态（诊断用，避免刷屏）。 */
	private static final int DEBUG_TURN_INTERVAL = 5;

	private static void play(String asset) {
		play(asset, "unknown");
	}

	private static void play(String asset, String ruleTag) {
		if (asset == null) {
			if (DEBUG) System.out.println("[挑战音效] " + ruleTag + " 资源路径为 null，跳过");
			return;
		}

		//兜底：init 之后再勾选也能正常播放
		loadOne(asset);

		long handle = Sample.INSTANCE.play(asset);

		if (DEBUG) {
			boolean ok = handle > 0;
			int n = triggerCount.containsKey(ruleTag) ? triggerCount.get(ruleTag) : 0;
			triggerCount.put(ruleTag, n + 1);

			//注意：loaded 现在只在"确实加载成功"时才包含该资源，
			//所以这里的"加载标记"是可信的。
			System.out.println("[挑战音效] " + ruleTag
					+ " 触发#" + (n + 1)
					+ " 资源=" + asset
					+ " 加载成功=" + loaded.contains(asset)
					+ " play返回值=" + handle
					+ (ok ? "  => 已播放"
						  : "  => **未播放**"));
			if (!ok && failed.contains(asset)) {
				System.out.println("          ↑ 原因：该文件**解码失败**（多半是编码格式不被支持）");
			} else if (!ok) {
				System.out.println("          ↑ 原因：Sample 未启用（设置里关掉了音效）");
			}
		}
	}

	/** 在角色头顶显示一句台词（走 GLog，与仓内其它提示一致）。 */
	private static void say(com.shatteredpixel.shatteredpixeldungeon.actors.Char ch, String key) {
		String text = com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
				ChallengeSfx.class, key);
		com.shatteredpixel.shatteredpixeldungeon.utils.GLog.i(text);
	}

	/**
	 * 让玩家"停止行动"指定回合数。
	 *
	 * <p>用 {@code Paralysis} 实现 —— 它是本 fork 既有的"不能行动"状态，
	 * 存读档、图标、回合递减都已处理好，不必新造一套。
	 */
	private static void stopHero(Hero hero, float turns) {
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
				hero,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis.class,
				turns);
	}
}
