/*
 * 破碎的地牢 (End fork) — 挑战 237 宿傩的精灵。
 *
 * 素材来自 grok-workspace-1.zip：9 帧 128x128，水平拼成 1152x128。
 */
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class SukunaSprite extends MobSprite {

	private static final int FRAME = 128;

	public SukunaSprite() {
		super();
		perspectiveRaise = 8 / 16f;
		texture( Assets.Sprites.SUKUNA );

		TextureFilm frames = new TextureFilm( texture, FRAME, FRAME );

		idle = new Animation( 8, true );
		idle.frames( frames, 0, 1, 2, 3, 4, 5, 6, 7, 8 );

		run = idle.clone();

		attack = new Animation( 8, false );
		attack.frames( frames, 0, 2, 4, 6, 8, 0 );

		die = new Animation( 10, false );
		die.frames( frames, 8, 7, 6, 5, 4, 3, 2, 1, 0 );

		play( idle );
	}
}
