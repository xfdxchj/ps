/*
 * 破碎的地牢 (End fork) — 挑战 126「格林之心」的黑之魂系统
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;

/**
 * END(挑战 126 格林之心): 黑之魂系统。
 *
 * <h3>原表效果</h3>
 * "无法升级，改为死亡后获得黑之魂，通过杀怪获得魂来增加属性，每次死亡后回到上层"
 *
 * <h3>设计</h3>
 * 这套系统**完全替代**原版的等级系统：
 *
 * <table border="1">
 *   <tr><th>原版</th><th>格林之心</th></tr>
 *   <tr><td>杀怪得经验 → 自动升级</td><td>杀怪得**魂** → 手动花魂换属性</td></tr>
 *   <tr><td>死亡 = 游戏结束</td><td>死亡 → 得到**黑之魂**并回到上层</td></tr>
 * </dl>
 *
 * <h3>魂从哪来</h3>
 * <ul>
 *   <li>**杀怪**：每只怪给 {@code max(1, EXP)} 点魂</li>
 *   <li>**死亡**：每次死亡额外给一笔"黑之魂"（原表："死亡后获得黑之魂"）</li>
 * </ul>
 *
 * <h3>魂怎么花</h3>
 * 用 {@link WndOptions} 做一个简单的"献祭"界面 ——
 * 每次花固定数量的魂，随机提升一项属性（生命上限 / 力量 / 闪避 / 命中）。
 * 之所以随机而不是自选：原表只说"增加属性"，
 * 随机化能避免玩家无脑堆某一项，也更贴合"魂"这种不可控的东西。
 */
public class BlackSoul {

	/** 126 格林之心。 */
	public static final int GRIMM_HEART = 126;

	/** 每次"献祭"消耗的魂。 */
	public static final int COST_PER_OFFERING = 10;

	/** 每次死亡获得的黑之魂。 */
	public static final int DEATH_SOUL_REWARD = 25;

	/**
	 * 静默模式（**仅供自动化测试**）。
	 *
	 * <p>测试环境没有 libGDX 的 {@code Gdx.app}，
	 * 而 {@code GLog.i()} 内部会调 {@code DeviceCompat.log()} → NPE。
	 * 打开本开关后所有日志调用被跳过，纯逻辑可在无图形环境下验证。
	 *
	 * <p>**绝不要在游戏运行时打开它。**
	 */
	public static boolean silentForTests = false;

	/** 安全地写一条信息日志。 */
	private static void logI(String text) {
		if (silentForTests || text == null) return;
		try { GLog.i(text); } catch (Throwable ignored) { }
	}

	/** 安全地写一条警告日志。 */
	private static void logW(String text) {
		if (silentForTests || text == null) return;
		try { GLog.w(text); } catch (Throwable ignored) { }
	}

	/** 安全地写一条危险日志。 */
	private static void logN(String text) {
		if (silentForTests || text == null) return;
		try { GLog.n(text); } catch (Throwable ignored) { }
	}

	/** END(126): 是否启用黑之魂系统。 */
	public static boolean enabled() {
		try {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask m =
					com.shatteredpixel.shatteredpixeldungeon.Dungeon.challengeMask;
			return m != null && m.has(GRIMM_HEART);
		} catch (Throwable t) {
			return false;
		}
	}

	//==================================================================
	//魂的存取
	//==================================================================

	/** END(126): 当前持有的魂。 */
	public static int souls(Hero hero) {
		if (hero == null) return 0;
		BlackSoulCounter c = hero.buff(BlackSoulCounter.class);
		return c == null ? 0 : c.souls;
	}

	/** END(126): 增加魂。 */
	public static void gainSouls(Hero hero, int amount, String reason) {
		if (hero == null || amount <= 0) return;
		if (!enabled()) return;

		BlackSoulCounter c = hero.buff(BlackSoulCounter.class);
		if (c == null) {
			c = Buff.affect(hero, BlackSoulCounter.class, 99999f);
			c.souls = 0;
		}
		c.souls += amount;

		logI("获得 " + amount + " 点黑之魂（" + reason + "）。当前：" + c.souls);
	}

	/** END(126): 消耗魂。不足时返回 false。 */
	public static boolean spendSouls(Hero hero, int amount) {
		if (hero == null || amount <= 0) return false;
		BlackSoulCounter c = hero.buff(BlackSoulCounter.class);
		if (c == null || c.souls < amount) return false;
		c.souls -= amount;
		return true;
	}

	//==================================================================
	//杀怪得魂
	//==================================================================

	/**
	 * END(126): 击杀怪物时给魂。
	 *
	 * <p>调用点：{@code Mob.die()}。
	 *
	 * <p>数量取 {@code max(1, EXP)} —— 直接复用原版的 EXP 数值，
	 * 这样"强怪给得多"的直觉被保留下来，不必另配一张表。
	 */
	public static void onMobKilled(Mob mob) {
		if (!enabled()) return;
		if (mob == null) return;
		if (Dungeon.hero == null) return;

		int amount = Math.max(1, mob.EXP);
		gainSouls(Dungeon.hero, amount, mob.name());
	}

	//==================================================================
	//死亡
	//==================================================================

	/**
	 * END(126): 玩家死亡时的处理 —— 给黑之魂并回到上层。
	 *
	 * <p>调用点：{@code Hero.die()} 之前（由 {@code Char.damage()} 判断致命后调用）。
	 *
	 * <p>**这是本规则最关键的部分**：死亡不再是游戏结束，而是
	 * "损失一层进度 + 获得一笔魂"。
	 *
	 * @return true 表示已接管死亡（调用方不应再走原版的 game over 流程）
	 */
	public static boolean handleDeath(Hero hero) {
		if (!enabled() || hero == null) return false;

		gainSouls(hero, DEATH_SOUL_REWARD, "死亡");

		//复活：半血，不结束游戏
		hero.HP = Math.max(1, hero.HT / 2);

		logN("你的心脏停了一瞬，然后又开始跳动。");
		logI("黑之魂在你体内沉积 —— 但你必须退回去一层。");

		//回到上一层
		retreatOneFloor(hero);
		return true;
	}

	/** END(126): 退回上一层（不是重开）。 */
	private static void retreatOneFloor(Hero hero) {
		if (hero == null) return;

		int target = Math.max(1, Dungeon.depth - 1);

		//用 InterlevelScene 的 ASCEND 模式 —— 那是原版"上楼"的标准路径
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.mode =
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.Mode.ASCEND;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition =
				new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition();
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.destDepth =
				target;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.destBranch =
				Dungeon.branch;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.destType =
				com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type
						.REGULAR_ENTRANCE;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.type =
				com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type
						.REGULAR_EXIT;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.centerCell = -1;

		com.watabou.noosa.Game.switchScene(
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.class);
	}

	//==================================================================
	//献祭：花魂换属性
	//==================================================================

	/** END(126): 打开"献祭"界面。 */
	public static void openOfferingWindow(Hero hero) {
		if (hero == null || !enabled()) return;

		final int have = souls(hero);
		String body = "你体内沉积着 " + have + " 点黑之魂。\n\n" +
				"献祭 " + COST_PER_OFFERING + " 点，换取一项属性的提升。\n" +
				"具体是哪一项，**由魂自己决定**。";

		GameScene.show(new WndOptions("黑之魂", body,
				"献祭 " + COST_PER_OFFERING + " 点",
				"算了") {
			@Override
			protected void onSelect(int index) {
				if (index != 0) return;
				offer(hero);
			}
		});
	}

	/** END(126): 执行一次献祭。 */
	public static void offer(Hero hero) {
		if (hero == null) return;
		if (!spendSouls(hero, COST_PER_OFFERING)) {
			logW("魂不够。");
			return;
		}

		//四选一：生命上限 / 力量 / 闪避 / 命中
		int roll = com.watabou.utils.Random.Int(4);
		switch (roll) {
			case 0:
				hero.grimmBoostMaxHP(5);
				logI("魂渗进了血肉。最大生命 +5。");
				break;
			case 1:
				hero.STR++;
				logI("魂锻进了骨骼。力量 +1。");
				break;
			case 2:
			default:
				//命中与闪避是 private 字段，只能通过 Hero 提供的方法加
				//（原版只在 levelUp() 里各 +1，而本规则关闭了升级）
				hero.grimmBoostAccuracyAndEvasion();
				logI(roll == 2 ? "魂浸进了步伐。闪避与命中 +1。"
						: "魂凝进了目光。命中与闪避 +1。");
				break;
		}
	}

	//==================================================================
	//buff：魂的容器
	//==================================================================

	/**
	 * END(126): 黑之魂的计数器。
	 *
	 * <p>必须继承 {@code FlavourBuff}（{@code Buff.affect} 三参重载的限制）。
	 */
	public static class BlackSoulCounter extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		public int souls = 0;

		@Override public int icon(){ return BuffIndicator.NONE; }

		/** 显示成"魂：N"（供状态栏使用，可选）。 */
		public String label(){
			return "魂 " + souls;
		}

		private static final String SOULS = "souls";

		@Override
		public void storeInBundle(Bundle bundle){
			super.storeInBundle(bundle);
			bundle.put(SOULS, souls);
		}

		@Override
		public void restoreFromBundle(Bundle bundle){
			super.restoreFromBundle(bundle);
			souls = bundle.getInt(SOULS);
		}
	}
}
