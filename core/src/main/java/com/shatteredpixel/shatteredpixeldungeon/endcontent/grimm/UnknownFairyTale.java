/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」的关键道具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

/**
 * END(129 心爱的少女): 未知的童话书。
 *
 * <h3>文档所有者定稿</h3>
 * "心爱的少女增加物品，未知的童话书，**每获得童话就补齐一部分**，
 *  **完整后去往 999 层**。"
 *
 * <h3>玩法</h3>
 * <ol>
 *   <li>勾选 129 时**开局发放**一本空书</li>
 *   <li>每捡到一枚**童话残片**，书里就自动补上一页 ——
 *       与"是否拿在手里"无关，只要进过背包就记上</li>
 *   <li>集齐 9 页后书变"完整"，此时**使用它**就能去 999 层见爱丽丝</li>
 * </ol>
 *
 * <h3>与《心爱的少女》的分工</h3>
 * <pre>
 *   童话书  —— 收集品 & 传送钥匙（这本）
 *   心爱的少女 —— 由残片炼金合成的最终产物（原有的那条路）
 * </pre>
 * 所以童话书是**另一条通往 999 层的路**：不必炼金，靠捡齐 9 枚残片即可。
 * 两者不冲突（炼金那条依然可用）。
 *
 * <h3>为什么书要"记页码"而不是消耗残片</h3>
 * 残片本身还能拿去炼金（3 枚 → 1 张残页）。如果书把残片吃掉，
 * 玩家就得在"两条路"之间二选一 —— 那是惩罚，不是设计。
 * 所以书只是**记下你见过哪些**，残片仍在手里。
 */
public class UnknownFairyTale extends Item {

	{
		image = ItemSpriteSheet.GRIMM_FAIRY_FRAGMENT;   //暂时共用图标
		stackable = false;
		bones = false;
		unique = true;
	}

	/** 需要集齐的残片种类数。 */
	public static final int PAGES = 9;

	/**
	 * END: 已经补齐了哪几页。
	 *
	 * <p>下标 = 残片的 kind（0..8）。只记"见过没有"，
	 * 所以重复捡到同一枚不会让它变成 2/9。
	 */
	private boolean[] pages = new boolean[PAGES];

	@Override public String name(){
		return isComplete() ? "未知的童话书（完整）" : "未知的童话书";
	}

	@Override
	public String info(){
		int have = pageCount();
		StringBuilder sb = new StringBuilder();
		sb.append("一本没有标题的书。翻开第一页，是空白的。\n\n");
		sb.append("**进度：").append(have).append(" / ").append(PAGES).append("**\n\n");

		for (int i = 0; i < PAGES; i++){
			sb.append(pages[i] ? "· " : "· （空白） ");
			sb.append(pages[i] ? FairyFragment.characterName(i) : "？？？");
			sb.append("\n");
		}
		sb.append("\n");

		if (isComplete()){
			sb.append("书页已经写满。**使用它可以翻到最后一页** —— 那里有人在等你。");
		} else {
			sb.append("每捡到一枚**童话残片**，这里就会自动补上一页。");
		}
		return sb.toString();
	}

	@Override public String desc(){ return info(); }

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	//==================================================================
	//使用：完整后去 999 层
	//==================================================================

	/** END: 动作名（Item 基类没有 AC_USE，子类自己声明）。 */
	public static final String AC_USE = "USE";

	@Override
	public java.util.ArrayList<String> actions(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero){
		java.util.ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		//只有写满的书才能用 —— 否则玩家会反复点它看提示，很烦
		if (isComplete()) actions.add(AC_USE);
		return actions;
	}

	/**
	 * END: 使用完整的童话书 → 去 999 层见爱丽丝。
	 *
	 * <p>照 {@code BelovedGirl.execute()} 的既有写法 ——
	 * 那是本 fork 已验证的"切到 999 层"路径。
	 *
	 * <h3>与《心爱的少女》的区别</h3>
	 * 那条路要炼金（9 残片 → 3 残页 → 1 少女），
	 * 这条只要捡齐 9 枚残片即可 —— 是**更慢但更省事**的另一条路。
	 * 两者都能到 999 层，不冲突。
	 */
	@Override
	public void execute(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero,
			String action){
		super.execute(hero, action);
		if (!action.equals(AC_USE) || hero == null) return;

		if (!isComplete()){
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog
					.w("书页还是空的 —— 那个地方尚未成形。");
			return;
		}

		//记下当前位置，供对话结束后返回
		BelovedGirl.AliceReturn.setReturnPoint(hero);

		//书**不消耗** —— 它与《心爱的少女》不同：
		//那条路是"一次性道具"，这条是"通关凭证"，用掉就没了反而奇怪。
		//而且它已经写满了，留着也不会破坏什么。

		com.shatteredpixel.shatteredpixeldungeon.utils.GLog
				.i("书页翻动的声音。你被带往了某个不该存在的地方。");

		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.mode =
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.Mode.DESCEND;
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition =
				new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition();
		com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.destDepth =
				BelovedGirl.ALICE_DEPTH;
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

	//==================================================================
	//补齐
	//==================================================================

	/** END: 已补齐的页数。 */
	public int pageCount(){
		int n = 0;
		for (boolean b : pages) if (b) n++;
		return n;
	}

	/** END: 书是否完整（九页齐全）。 */
	public boolean isComplete(){
		return pageCount() >= PAGES;
	}

	/**
	 * END: 记下一页。
	 *
	 * @param kind 残片种类（0..8）
	 * @return true 表示**这一页是新的**（之前没见过）
	 */
	public boolean learn(int kind){
		if (kind < 0 || kind >= PAGES) return false;
		if (pages[kind]) return false;
		pages[kind] = true;
		return true;
	}

	//==================================================================
	//发放 / 查找
	//==================================================================

	/**
	 * END: 给玩家发一本（已有就不重复发）。
	 *
	 * <p>调用点：{@code ChallengeEffects.startingGear()}（勾选 129 时）。
	 */
	public static void grant() {
		if (Dungeon.hero == null) return;
		try {
			if (Dungeon.hero.belongings.getItem(UnknownFairyTale.class) != null) return;
		} catch (Throwable ignored) { }

		UnknownFairyTale book = new UnknownFairyTale();
		book.identify();
		if (!book.collect()) {
			//背包满了就丢在脚下
			try {
				Dungeon.level.drop(book, Dungeon.hero.pos).sprite.drop(Dungeon.hero.pos);
			} catch (Throwable ignored) { }
		}
	}

	/**
	 * END: 取玩家身上的那本书；没有则返回 null。
	 *
	 * <p>用 {@code getItem} 而不是查背包 —— 后者搜不到已装备/已放进容器的。
	 */
	public static UnknownFairyTale of(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
		if (hero == null) return null;
		try {
			return hero.belongings.getItem(UnknownFairyTale.class);
		} catch (Throwable t) {
			return null;
		}
	}

	/**
	 * END: 记下一枚残片（由 {@link FairyFragment} 拾取时调用）。
	 *
	 * <p>顺便给玩家一句反馈 —— 让他知道"捡到了新的一页"。
	 */
	public static void onFragmentCollected(int kind) {
		UnknownFairyTale book = of(Dungeon.hero);
		if (book == null) return;

		if (book.learn(kind)) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.i(
					"《未知的童话书》补上了一页 —— "
							+ FairyFragment.characterName(kind)
							+ "（" + book.pageCount() + " / " + PAGES + "）。");

			if (book.isComplete()) {
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p(
						"书页已经写满了。最后一页上，写着一个你不认识的名字。");
			}
		}
	}

	//==================================================================
	//存档
	//==================================================================

	private static final String PAGES_KEY = "pages";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(PAGES_KEY, pages);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		boolean[] saved = bundle.getBooleanArray(PAGES_KEY);
		if (saved != null){
			int n = Math.min(saved.length, pages.length);
			System.arraycopy(saved, 0, pages, 0, n);
		}
	}
}
