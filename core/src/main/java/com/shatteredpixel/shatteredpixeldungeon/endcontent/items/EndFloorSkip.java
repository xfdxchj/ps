package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;

import java.util.ArrayList;

/**
 * END(挑战区测试用): 深渊传送符。
 *
 * 用途：便利挑战(CONVENIENCE)开局赠送。使用后直接把英雄送到 **25 层**
 * （主线终点层），方便跳过 1-24 层、直接测试 26F 起的挑战区内容。
 *
 * 说明：
 * - 只在便利挑战下发放（见 Dungeon.init 的 CONVENIENCE 分支），普通对局不会出现。
 * - 使用后不消耗（可反复用），因为它本就是测试工具。
 * - 目标层用 25；若所选挑战区需要 26F 入口，走到 25F 的楼梯下去即可。
 */
public class EndFloorSkip extends Item {

	public static final String AC_USE = "USE";

	{
		stackable = false;
		unique = true;
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

		if (action.equals(AC_USE)) {

			//END: 解除限时类 buff（与卷轴/楼梯一致的收尾）
			TimekeepersHourglass.timeFreeze timeFreeze = hero.buff(TimekeepersHourglass.timeFreeze.class);
			if (timeFreeze != null) timeFreeze.disarmPresses();
			Swiftthistle.TimeBubble timeBubble = hero.buff(Swiftthistle.TimeBubble.class);
			if (timeBubble != null) timeBubble.disarmPresses();

			//END: 传送到下一个「关键层」。每使用一次前进一站：
			//  1) 主线终点 25F
			//  2) 挑战区入口 26F
			//  3) 各 Boss 层（冥犬31 / 剧院32 / 四柱33 / 火龙38）
			//这样可以直接跳到想测的地方，不必一层层走。
			int target = nextTarget();

			InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
			InterlevelScene.curTransition = new LevelTransition();
			InterlevelScene.curTransition.destDepth = target;
			InterlevelScene.curTransition.destBranch = 0;
			InterlevelScene.curTransition.destType = LevelTransition.Type.REGULAR_ENTRANCE;
			InterlevelScene.curTransition.type = LevelTransition.Type.REGULAR_ENTRANCE;
			InterlevelScene.curTransition.centerCell = -1;

			GLog.p(Messages.get(this, "teleport") + " (" + target + "F)");
			Game.switchScene(InterlevelScene.class);
		}
	}

	/** 站点表：按层号顺序排列的「关键层」。 */
	private static final int[] STOPS = { 25, 26, 31, 32, 33, 38, 45 };

	/** 下一个站点（当前层之后最近的一个）。 */
	private static int nextTarget() {
		int cur = Dungeon.depth;
		for (int s : STOPS) {
			if (s > cur) return s;
		}
		return STOPS[STOPS.length - 1];   //已到末尾则停在最后一站
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
		//END: 用本 fork 顶层的空闲图标槽（DOCUMENTS 块，见 ItemSpriteSheet）
		return ItemSpriteSheet.CITY_HOOD;
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
