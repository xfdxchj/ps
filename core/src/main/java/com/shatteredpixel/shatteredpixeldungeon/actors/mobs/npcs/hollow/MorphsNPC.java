package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.hollow;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GodNPC;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.plot.Plot;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.plot.hollow.MorphsEndTheaterPlot;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.plot.hollow.MorphsGodEndTheaterPlot;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.plot.hollow.MorphsNPCPlot;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.hollow.StarCrystal;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MorpheusSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDialog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

/**
 * END(移植自魔绫·挑战区): 剧院层(32F) 的剧情 NPC —— 莫菲斯。
 *
 * <h3>移植裁剪说明（重要）</h3>
 * 魔绫原版的 MorphsNPC 依赖一整套**小游戏系统**：
 * <ul>
 *   <li>{@code MorphsAllEndPlot}(712行) —— 按小游戏总分播放不同结局对话</li>
 *   <li>{@code LingBag} —— 小游戏奖励背包</li>
 *   <li>{@code Statistics.miniGamesTotalLevel} —— 小游戏成绩</li>
 *   <li>{@code WndPacManReadyGo} —— 小游戏开始窗口</li>
 * </ul>
 * 本项目**不做小游戏**，因此这里只保留**剧情对话**部分：
 * <ul>
 *   <li>持有「星晶」时 → 传送到 33F（四柱 Boss 层）</li>
 *   <li>首次对话 → {@code MorphsNPCPlot}</li>
 *   <li>其他情况 → {@code MorphsEndTheaterPlot}（原版按成绩二选一，此处固定用后者）</li>
 * </ul>
 * 被裁掉的仅是"小游戏成绩相关分支"，剧情主线完整。
 */
public class MorphsNPC extends GodNPC {

	{
		spriteClass = MorpheusSprite.class;
		properties.add(Property.IMMOVABLE);
		maxLvl = -1;
	}

	private boolean first = true;

	private static final String FIRST = "first";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(FIRST, first);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		first = bundle.getBoolean(FIRST);
	}

	@Override
	public boolean act() {

		//END: 拿到「星晶」后 → 立刻传送去 33F（四柱 Boss 层）
		StarCrystal starCrystal = Dungeon.hero.belongings.getItem(StarCrystal.class);
		if (starCrystal != null) {
			die(true);

			InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
			TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
			if (timeFreeze != null) timeFreeze.disarmPresses();
			Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
			if (timeBubble != null) timeBubble.disarmPresses();

			InterlevelScene.curTransition = new LevelTransition();
			InterlevelScene.curTransition.destDepth = 33;
			InterlevelScene.curTransition.destType = LevelTransition.Type.REGULAR_ENTRANCE;
			InterlevelScene.curTransition.destBranch = 0;
			InterlevelScene.curTransition.type = LevelTransition.Type.REGULAR_EXIT;
			InterlevelScene.curTransition.centerCell = -1;
			Game.switchScene(InterlevelScene.class);

			Buff.detach(hero, LostInventory.class);
			starCrystal.detach(hero.belongings.backpack);
		}

		return super.act();
	}

	@Override
	public boolean interact(Char c) {

		sprite.turnTo(pos, Dungeon.hero.pos);

		//END(裁剪): 原版此处要求先拿到 LingBag（小游戏奖励）才给对话，现改为直接给剧情
		if (first) {
			MorphsNPCPlot plot = new MorphsNPCPlot();
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndDialog(plot, false));
				}
			});
			first = false;
			return true;
		}

		//END(裁剪): 原版按小游戏成绩在 MorphsGodEndTheaterPlot / MorphsEndTheaterPlot 间二选一，
		//本项目无小游戏 → 统一用 MorphsEndTheaterPlot（剧情主线的收尾对话）
		MorphsEndTheaterPlot plot = new MorphsEndTheaterPlot();
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndDialog(plot, false));
			}
		});
		return true;
	}
}
