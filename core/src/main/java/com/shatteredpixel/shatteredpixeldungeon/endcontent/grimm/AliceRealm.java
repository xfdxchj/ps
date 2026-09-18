/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」的 999 层
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.audio.Music;

/**
 * END(挑战 129 心爱的少女): 999 层「爱丽丝领域」。
 *
 * <h3>这是什么地方</h3>
 * 按文档所有者给的设定，爱丽丝"发觉了这个不可思议的世界是伪造出来的"。
 * 所以 999 层不是地牢的一部分 —— 它是**书页之外**：
 * 一片虚空，中间一小块可以站立的地面，爱丽丝站在那里等你。
 *
 * <h3>实现参考</h3>
 * 结构照搬本 fork 的 {@code MorpheusBossLevel}（挑战区的一个虚空 Boss 层）——
 * 那已经验证过"用 CHASM + 星空背景做虚空"是可行且稳定的：
 * <ul>
 *   <li>{@code tilesTex()} 用 {@code TILES_MORGALAXY}（星云贴图）</li>
 *   <li>{@code waterTex()} 用 {@code BLACK_RECT}（水面画成纯黑）</li>
 *   <li>自定义 {@link StarfieldBackground} 铺整层背景</li>
 * </ul>
 *
 * <h3>为什么是一块小平台</h3>
 * 玩家在这里没有事可做（只有对话），大面积地图纯属浪费。
 * 9×9 的可走区域足够站下两个人和一点走动空间。
 */
public class AliceRealm extends Level {

	/** 999 层的深度号。 */
	public static final int DEPTH = 999;

	private static final int W = 15;
	private static final int H = 15;

	//地图字符：S=深渊  G=墙  E=可走地面
	private static final int S = Terrain.CHASM;
	private static final int G = Terrain.WALL;
	private static final int E = Terrain.EMPTY_SP;

	{
		color1 = 0x1a0b1e;      //暗紫
		color2 = 0x4a2a55;
		viewDistance = 12;
		extraGlass = false;
	}

	/**
	 * END(129): 15×15 的虚空 —— 四角是深渊，中间是菱形平台。
	 *
	 * <p>菱形而非方形：视觉上更像"浮在空中的一块地"。
	 */
	private static final int[] CODE_MAP = {
			S,S,S,S,S,S,S,G,G,G,S,S,S,S,S,
			S,S,S,S,S,S,G,G,E,G,G,S,S,S,S,
			S,S,S,S,S,G,G,E,E,E,G,G,S,S,S,
			S,S,S,S,G,G,E,E,E,E,E,G,G,S,S,
			S,S,S,G,G,E,E,E,E,E,E,E,G,G,S,
			S,S,G,G,E,E,E,E,E,E,E,E,E,G,G,
			S,G,G,E,E,E,E,E,E,E,E,E,E,E,G,
			G,G,E,E,E,E,E,E,E,E,E,E,E,E,G,
			S,G,G,E,E,E,E,E,E,E,E,E,E,E,G,
			S,S,G,G,E,E,E,E,E,E,E,E,E,G,G,
			S,S,S,G,G,E,E,E,E,E,E,E,G,G,S,
			S,S,S,S,G,G,E,E,E,E,E,G,G,S,S,
			S,S,S,S,S,G,G,E,E,E,G,G,S,S,S,
			S,S,S,S,S,S,G,G,E,G,G,S,S,S,S,
			S,S,S,S,S,S,S,G,G,G,S,S,S,S,S,
	};

	@Override
	protected boolean build() {
		//清理上一层的锁（跨层传送过来时可能残留）
		unseal();
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach(
				com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor.class);

		setSize(W, H);
		map = CODE_MAP.clone();

		//入口与出口：都放在平台中心偏下（玩家和爱丽丝都在这一带）
		int entrance = 7 * W + 7;
		map[entrance] = Terrain.ENTRANCE;
		transitions.add(new LevelTransition(this, entrance,
				LevelTransition.Type.REGULAR_ENTRANCE));

		//星空背景
		CustomTilemap vis = new StarfieldBackground();
		vis.pos(0, 0);
		customTiles.add(vis);

		return true;
	}

	@Override
	protected void createMobs() {
		//爱丽丝站在平台中心
		Alice alice = new Alice();
		alice.pos = 7 * W + 7;
		mobs.add(alice);
	}

	@Override
	protected void createItems() {
		//这里什么都不放 —— 奖励由对话给出
	}

	/** END(129): 一进来就播爱丽丝的主题曲。 */
	@Override
	public void playLevelMusic() {
		Music.playModeBGM(Assets.Music.GRIMM_ALICE, true);
	}

	/** END(129): 玩家不能从这里走楼梯离开 —— 只能通过对话返回。 */
	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		return false;
	}

	@Override
	public void occupyCell(Char ch) {
		super.occupyCell(ch);
		//不做任何封锁：这里没有战斗
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_MORGALAXY;
	}

	@Override
	public String waterTex() {
		return Assets.Interfaces.BLACK_RECT;
	}

	/** END(129): 铺满整层的星空背景。 */
	public static class StarfieldBackground extends CustomTilemap {

		{
			texture = Assets.Environment.GALAXY_BACKGROUND;
			tileW = W;
			tileH = H;
		}

		final int TEX_WIDTH = W * 16;

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			int[] data = mapSimpleImage(0, 0, TEX_WIDTH);
			v.map(data, tileW);
			return v;
		}
	}
}
