/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」999 层的爱丽丝
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

/**
 * END(挑战 129): 999 层「爱丽丝领域」的 NPC 精灵。
 *
 * <h3>贴图</h3>
 * `sprites/alice.png` 是 96×136，布局 **3 列 × 4 行，每帧 32×34**
 * （内容 32×32 + 2px 间隔）。
 *
 * <p>按文档所有者指定：**只用第一行的 3 帧做循环**。
 *
 * <h3>为什么帧高取 34 而不是 32</h3>
 * 源图每格实际占 34 像素高（32 内容 + 2 间隔）。
 * 用 34 切能让三帧正好落在格心；用 32 切会逐行累积偏移，
 * 到第 4 行时已经偏了 8 像素。
 */
public class AliceSprite extends MobSprite {

	/** 帧尺寸（与源图的格子一致）。 */
	private static final int FRAME_W = 32;
	private static final int FRAME_H = 34;

	public AliceSprite() {
		super();

		//视角抬高：她比普通怪高一些，不然会陷进地面
		perspectiveRaise = 6 / 16f;

		texture( Assets.Sprites.ALICE );

		TextureFilm frames = new TextureFilm( texture, FRAME_W, FRAME_H );

		//3 帧缓慢循环（0 -> 1 -> 2）
		idle = new Animation( 6, true );
		idle.frames( frames, 0, 1, 2 );

		//NPC 不移动、不攻击 —— 复用 idle，避免出现"未播放动画"的空白状态
		run = idle.clone();
		attack = idle.clone();
		die = idle.clone();

		play( idle );
	}

	@Override
	public void link(com.shatteredpixel.shatteredpixeldungeon.actors.Char ch) {
		super.link(ch);
		//NPC 不投影子：她站的地方是虚空
		renderShadow = false;
	}

	/** NPC 不参与"受击闪白"。 */
	@Override
	public void flash() {
	}
}
