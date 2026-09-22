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
	 * <h3>文档所有者定稿的公式</h3>
	 * <pre>
	 *   普通怪：1 + 区域数 × 2
	 *   精英怪：3 + 区域数 × 5
	 *   Boss  ：10 + 区域数 × 20
	 * </pre>
	 *
	 * <p>"区域数"是 1 基的（1 区 = 1）。用 {@code Dungeon.depth} 换算 ——
	 * 每 5 层一个区域。
	 *
	 * <p>为什么不用 {@code mob.EXP}（原实现）：文档所有者给了明确的表，
	 * 而 EXP 在各区域之间是同一量级（不像这里的公式那样按区域拉开差距）。
	 */
	public static void onMobKilled(Mob mob) {
		if (!enabled()) return;
		if (mob == null) return;
		if (Dungeon.hero == null) return;

		int region = regionOf(Dungeon.effectiveDepth());
		int amount;

		if (mob.properties().contains(Char.Property.BOSS)){
			amount = 10 + region * 20;
		} else if (mob.properties().contains(Char.Property.MINIBOSS)
				|| !mob.buffs(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ChampionEnemy.class).isEmpty()){
			amount = 3 + region * 5;
		} else {
			amount = 1 + region * 2;
		}

		gainSouls(Dungeon.hero, Math.max(1, amount), mob.name());
	}

	/**
	 * END(126): 楼层 → 区域数（1 基）。
	 *
	 * <p>每 5 层一个区域：1-5 → 1 区、6-10 → 2 区 … 21+ → 5 区。
	 * 用**映射后的楼层**（轮回之后 26 层 = 新一轮 1 层），
	 * 这样"无尽轮回"里区域数也跟着循环，魂量不会无限膨胀。
	 */
	public static int regionOf(int depth) {
		int mapped = com.shatteredpixel.shatteredpixeldungeon.endcontent
				.Reincarnation.mappedDepth(depth);
		return Math.max(1, Math.min(5, (mapped - 1) / 5 + 1));
	}

	//==================================================================
	//死亡
	//==================================================================

	/**
	 * END(126): 玩家死亡时的处理 —— 保留一半魂并回到上层。
	 *
	 * <p>调用点：{@code Hero.die()} 之前（由 {@code Char.damage()} 判断致命后调用）。
	 *
	 * <p>**这是本规则最关键的部分**：死亡不再是游戏结束，而是
	 * "损失一层进度 + 保留一半魂"。
	 *
	 * <h3>文档所有者定稿</h3>
	 * "死亡后回到上一层，已分配属性保留，**未使用魂保留 50%**。"
	 *
	 * <p>注意"已分配属性保留" —— 六项属性的**等级不会因为死亡而回退**，
	 * 只有**手里还没花掉的魂**减半。这是很关键的一条：
	 * 否则玩家会不敢花魂，玩法就变成了"攒着不动"。
	 *
	 * @return true 表示已接管死亡（调用方不应再走原版的 game over 流程）
	 */
	public static boolean handleDeath(Hero hero) {
		if (!enabled() || hero == null) return false;

		//未使用的魂保留 50%（向下取整，且至少留 0）
		int before = souls(hero);
		int kept = before / 2;
		setSouls(hero, kept);

		logN("你的心脏停了一瞬，然后又开始跳动。");
		if (before > 0) {
			logI("黑之魂散去了一半 —— " + before + " → " + kept + "。");
		} else {
			logI("黑之魂在你体内沉积 —— 但你必须退回去一层。");
		}

		//已分配属性**保留** —— 六项等级不动（存于 buff 里，不随死亡清空）

		//复活：半血，不结束游戏
		hero.HP = Math.max(1, hero.HT / 2);

		//回到上一层
		retreatOneFloor(hero);
		return true;
	}

	/** END(126): 直接设置魂的数量（死亡减半用）。 */
	public static void setSouls(Hero hero, int value) {
		if (hero == null) return;
		BlackSoulCounter c = hero.buff(BlackSoulCounter.class);
		if (c == null) {
			c = Buff.affect(hero, BlackSoulCounter.class, 99999f);
		}
		c.souls = Math.max(0, value);
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
	//加点：六项自选
	//==================================================================

	/**
	 * END(126): 打开「加点」界面。
	 *
	 * <h3>文档所有者定稿</h3>
	 * "可在加点界面**自由分配**魂至六项属性。"
	 *
	 * <p>与原来的"随机提升一项"完全不同 —— 现在是**玩家自选**哪一项，
	 * 且每项有各自的消耗与上限（见 {@link GrimmStat}）。
	 */
	public static void openOfferingWindow(final Hero hero) {
		if (hero == null || !enabled()) return;

		final int have = souls(hero);
		StringBuilder body = new StringBuilder();
		body.append("你体内沉积着 **").append(have).append("** 点黑之魂。\n\n");
		body.append("选择要提升的属性：\n\n");
		for (GrimmStat s : GrimmStat.values()){
			int lv = levelOf(hero, s);
			int cost = s.costAt(lv);
			body.append("· ").append(s.title)
					.append("  Lv").append(lv);
			if (s.canLevelUp(lv)){
				body.append("  （下一级 ").append(cost).append(" 魂）");
			} else {
				body.append("  （已满级）");
			}
			body.append("\n");
		}

		//六项 + 取消 = 七个按钮
		String[] options = new String[GrimmStat.values().length + 1];
		for (int i = 0; i < GrimmStat.values().length; i++){
			GrimmStat s = GrimmStat.values()[i];
			int lv = levelOf(hero, s);
			int cost = s.costAt(lv);
			if (!s.canLevelUp(lv)){
				options[i] = s.title + "（已满级）";
			} else if (have < cost){
				options[i] = s.title + "（魂不足，需 " + cost + "）";
			} else {
				options[i] = s.title + "  ← " + cost + " 魂";
			}
		}
		options[GrimmStat.values().length] = "算了";

		GameScene.show(new WndOptions("黑之魂 · 加点", body.toString(), options) {
			@Override
			protected void onSelect(int index) {
				if (index < 0 || index >= GrimmStat.values().length) return;
				GrimmStat s = GrimmStat.values()[index];
				if (applyStat(hero, s)) {
					//成功 → 再开一次，方便连续加点
					openOfferingWindow(hero);
				}
			}
		});
	}

	/**
	 * END(126): 把魂投入到某一项。
	 *
	 * @return true 表示成功（魂够、未满级）
	 */
	public static boolean applyStat(Hero hero, GrimmStat stat) {
		if (hero == null || stat == null) return false;
		if (!enabled()) return false;

		int lv = levelOf(hero, stat);
		if (!stat.canLevelUp(lv)) {
			logW(stat.title + " 已经到顶了。");
			return false;
		}
		int cost = stat.costAt(lv);
		if (!spendSouls(hero, cost)) {
			logW("魂不够（需要 " + cost + "）。");
			return false;
		}

		//记下等级
		setLevel(hero, stat, lv + 1);

		//立刻把效果加到角色身上
		applyEffect(hero, stat);

		logI(stat.title + " 提升到 Lv" + (lv + 1) + "（花费 " + cost + " 魂）。");
		return true;
	}

	/** END(126): 把某一项的**即时效果**施加到角色身上。 */
	private static void applyEffect(Hero hero, GrimmStat stat) {
		switch (stat){
			case HP:
				hero.grimmBoostMaxHP(5);
				break;
			case PHYS:
			case MAGIC:
				//伤害类不需要改字段 —— 由 HostileController 在结算时读等级
				//（见 GrimmCombat 的查询），这里什么都不做。
				break;
			case ACC:
			case EVA:
				hero.grimmBoostAccuracyAndEvasion();
				break;
			case STR:
				hero.STR++;
				break;
		}
	}

	//==================================================================
	//六项等级的存取
	//==================================================================

	/** END(126): 某项当前的等级。 */
	public static int levelOf(Hero hero, GrimmStat stat) {
		if (hero == null || stat == null) return 0;
		BlackSoulCounter c = hero.buff(BlackSoulCounter.class);
		return c == null ? 0 : c.levels[stat.ordinal()];
	}

	/** END(126): 设置某项的等级。 */
	public static void setLevel(Hero hero, GrimmStat stat, int value) {
		if (hero == null || stat == null) return;
		BlackSoulCounter c = hero.buff(BlackSoulCounter.class);
		if (c == null) {
			c = Buff.affect(hero, BlackSoulCounter.class, 99999f);
		}
		c.levels[stat.ordinal()] = Math.max(0, Math.min(stat.maxLevel, value));
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

		/**
		 * END(126): 六项属性各自的等级。
		 *
		 * <p>下标 = {@link GrimmStat#ordinal()}。
		 * 存在 buff 里而不是 Hero 上 —— 这样**死亡后自然保留**
		 * （文档所有者定稿："已分配属性保留"）。
		 */
		public int[] levels = new int[GrimmStat.values().length];

		@Override public int icon(){ return BuffIndicator.NONE; }

		/** 显示成"魂：N"（供状态栏使用，可选）。 */
		public String label(){
			return "魂 " + souls;
		}

		private static final String SOULS = "souls";
		private static final String LEVELS = "stat_levels";

		@Override
		public void storeInBundle(Bundle bundle){
			super.storeInBundle(bundle);
			bundle.put(SOULS, souls);
			bundle.put(LEVELS, levels);
		}

		@Override
		public void restoreFromBundle(Bundle bundle){
			super.restoreFromBundle(bundle);
			souls = bundle.getInt(SOULS);
			int[] saved = bundle.getIntArray(LEVELS);
			if (saved != null) {
				//版本兼容：旧存档的数组可能比现在短（新增了属性），逐项拷贝
				int n = Math.min(saved.length, levels.length);
				System.arraycopy(saved, 0, levels, 0, n);
			}
		}
	}
}
