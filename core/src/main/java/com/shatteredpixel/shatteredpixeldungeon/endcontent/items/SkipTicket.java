/*
 * 破碎的地牢 (End fork) — 挑战 7「跳级生」的物品实现
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;

import java.util.ArrayList;

/**
 * END(挑战 7 跳级生): 跳级券 —— 使用后**直接进入下一区域开头**。
 *
 * <h3>为什么做成物品而不是全局规则</h3>
 * 原表写的是"可直接进入下一区域"，若做成"永久可跳"，玩家会一路跳完全程，
 * 整局的关卡与成长曲线都会被破坏。做成**一次性物品**后：
 * 玩家自己决定何时用、用几次，跳级变成一个需要权衡的资源。
 *
 * <h3>效果</h3>
 * <ul>
 *   <li>跳到**下一区域的第一层**（按 depth % 5 的区域划分）</li>
 *   <li>同时发放原表的跳级奖励：<b>+2 力量、+3 升级卷轴、+4 力量药水</b>
 *       （原表写的是"+4 升级药水"，本 fork 无此物品，按同类奖励发放力量药水）</li>
 * </ul>
 *
 * <h3>与 EndFloorSkip 的区别</h3>
 * {@code EndFloorSkip} 是**测试工具**（便利挑战发放、不消耗、可反复用、跳到固定站点表）；
 * 本物品是**正式玩法**（挑战 7 发放、用一次少一张、目标是下一区域）。
 */
public class SkipTicket extends Item {

	public static final String AC_USE = "USE";

	/** 原表奖励：+2 力量。 */
	private static final int REWARD_STRENGTH = 2;
	/** 原表奖励：+3 升级卷轴。 */
	private static final int REWARD_UPGRADE_SCROLLS = 3;
	/**
	 * 原表奖励：+4 **升级药水**。
	 *
	 * <p>END(修订): 原表正文写的是"升级药水"，但按文档所有者说明，
	 * 这里实际指的就是**经验药水**（{@code PotionOfExperience}，喝一瓶升一级）。
	 * 早期版本我误以为本 fork 没有"升级药水"而换成了力量药水 —— 那是错的，
	 * {@code PotionOfExperience} 一直存在。
	 */
	private static final int REWARD_EXP_POTIONS = 4;

	{
		stackable = true;
		unique = false;
		levelKnown = true;
		defaultAction = AC_USE;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_USE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (!action.equals(AC_USE)) return;

		//已经在最后一层就没有下一区域可跳
		int target = nextRegionFirstFloor(Dungeon.depth);
		if (target <= Dungeon.depth) {
			GLog.w(Messages.get(this, "no_target"));
			return;
		}

		//END: 解除限时类 buff（与卷轴/楼梯一致的收尾）
		TimekeepersHourglass.timeFreeze timeFreeze =
				hero.buff(TimekeepersHourglass.timeFreeze.class);
		if (timeFreeze != null) timeFreeze.disarmPresses();
		Swiftthistle.TimeBubble timeBubble = hero.buff(Swiftthistle.TimeBubble.class);
		if (timeBubble != null) timeBubble.disarmPresses();

		//---- 发放跳级奖励 ----
		//用 collect() 进背包，而不是丢地上 —— 跳层后地上那堆就找不到了。
		for (int i = 0; i < REWARD_UPGRADE_SCROLLS; i++) {
			new ScrollOfUpgrade().collect();
		}
		for (int i = 0; i < REWARD_EXP_POTIONS; i++) {
			new com.shatteredpixel.shatteredpixeldungeon.items.potions
					.PotionOfExperience().collect();
		}
		//力量：直接加在英雄身上（原表"+2 力量"是永久属性）
		hero.STR += REWARD_STRENGTH;

		GLog.p(Messages.get(this, "skip", target));

		//---- 跳层 ----
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		InterlevelScene.curTransition = new LevelTransition();
		InterlevelScene.curTransition.destDepth = target;
		InterlevelScene.curTransition.destBranch = 0;
		InterlevelScene.curTransition.destType = LevelTransition.Type.REGULAR_ENTRANCE;
		InterlevelScene.curTransition.type = LevelTransition.Type.REGULAR_ENTRANCE;
		InterlevelScene.curTransition.centerCell = -1;

		Game.switchScene(InterlevelScene.class);

		//消耗一张（在 switchScene 之前 detach 会更安全，但 Item 的消耗
		//按仓内惯例在 execute 内完成即可）
		detach(hero.belongings.backpack);
	}

	/**
	 * END(7 跳级生): 下一区域的第一层。
	 *
	 * <p>区域划分按 {@code bossInterval()} 走：
	 * <ul>
	 *   <li>未勾选 6：每 5 层一区（1-5 / 6-10 / … / 21-25）</li>
	 *   <li>勾选 6：每 10 层一区（1-10 / 11-20 / … / 41-50）</li>
	 * </ul>
	 * 当前在第 k 层时，返回下一个区间的起点；已在最后一区间则返回当前层（表示无处可跳）。
	 *
	 * <p>END(适配 6 完整地牢): 原先写死 5 层一区，勾选 6 后会跳到错误的区域
	 * （例如在 8F 用券会跳到 11F，而 8F 本来就还在第 1 区）。
	 */
	public static int nextRegionFirstFloor(int currentDepth) {
		if (currentDepth <= 0) return currentDepth;

		int interval = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.bossInterval();
		int maxDepth = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.maxMainDepth();

		int regionIndex = (currentDepth - 1) / interval;      // 0,1,2,...
		int nextRegionStart = (regionIndex + 1) * interval + 1;

		//跳级券**不**把玩家送进挑战区（那是通关后才该做的事），
		//所以上限取 maxMainDepth()（25 或 50）。
		if (nextRegionStart > maxDepth) return currentDepth;

		return nextRegionStart;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public int image() {
		//复用图集中确实存在的传送石图标（与 EndFloorSkip 一致，避免空白贴图）
		return ItemSpriteSheet.STONE_BLINK;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}

	@Override
	public String info() {
		return desc();
	}
}
