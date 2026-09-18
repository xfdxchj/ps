/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」的最终产物
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * END(挑战 129 心爱的少女):「心爱的少女」。
 *
 * <h3>原表效果</h3>
 * "使用该物品后，立即传送至第 999 层遇见 NPC 爱丽丝。
 *   对话结束后，物品消失，获得黑兔戒指（若已持有则转为等价魂），
 *   随后传送回使用物品时的原始位置。"
 *
 * <h3>本阶段的实现范围</h3>
 * 本类目前只做**物品本身 + 传送骨架**：
 * <ul>
 *   <li>使用后传送到 999 层（爱丽丝所在的特殊层）</li>
 *   <li>记录原始位置，供对话结束后返回</li>
 * </ul>
 *
 * <p>**999 层的实际关卡与爱丽丝 NPC 在下一阶段实现** ——
 * 那一块需要一个自定义 Level 与 NPC 类，工作量独立。
 * 在此之前，若 999 层尚未建立，使用物品会给出提示而不是报错。
 */
public class BelovedGirl extends Item {

	/** 爱丽丝所在的特殊层号。 */
	public static final int ALICE_DEPTH = 999;

	{
		//END: 用**爱丽丝**的贴图（文档所有者指定）
		image = ItemSpriteSheet.GRIMM_BELOVED_GIRL;
		stackable = false;
		bones = false;
		unique = true;
	}

	@Override public String name(){ return "心爱的少女"; }

	@Override public String info(){
		return "九枚残片拼出的完整故事。\n\n" +
				"封面上的少女低垂着眼，你看不清她的表情。\n\n" +
				"使用后会被带到某个**不该存在的地方**。";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_USE);
		return actions;
	}

	/** 动作名（Item 基类没有 AC_USE，子类自己声明）。 */
	public static final String AC_USE = "USE";

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_USE) || hero == null) return;

		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.aliceRealmReady()) {
			GLog.w("故事还缺最后几页 —— 那个地方尚未成形。");
			return;
		}

		//记下当前位置，供对话结束后返回
		AliceReturn.setReturnPoint(hero);

		//物品消失（原表："对话结束后，物品消失" —— 这里在使用时就消耗，
		//避免玩家带着它反复进出 999 层刷戒指）
		detach(hero.belongings.backpack);

		GLog.i("书页翻动的声音。你被带往了某个不该存在的地方。");

		//切场景到 999 层
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.mode =
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.Mode.DESCEND;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition =
				new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition();
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.destDepth =
				ALICE_DEPTH;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.destBranch = 0;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.destType =
				com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type
						.REGULAR_ENTRANCE;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.type =
				com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type
						.REGULAR_ENTRANCE;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.centerCell = -1;

		com.watabou.noosa.Game.switchScene(
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.class);
	}

	/**
	 * END(129): 记录"从哪来"，用于从 999 层传送回去。
	 *
	 * <p>用静态字段而不是 buff：这段数据只需要活过一次场景切换，
	 * 而玩家在 999 层不存档（那边是特殊层）。
	 * 即便如此仍然提供 store/restore，避免意外存档导致丢位置。
	 */
	public static class AliceReturn {
		private static int depth = 1;
		private static int branch = 0;
		private static int pos = -1;

		public static void setReturnPoint(Hero hero){
			if (hero == null) return;
			depth = Dungeon.depth;
			branch = Dungeon.branch;
			pos = hero.pos;          //落点也记下来，回去时站回原格
		}

		public static int returnDepth(){ return depth; }
		public static int returnBranch(){ return branch; }
		public static int returnPos(){ return pos; }
	}
}
