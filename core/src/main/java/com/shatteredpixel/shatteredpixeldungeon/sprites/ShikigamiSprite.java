/*
 * 破碎的地牢 (End fork) — 挑战 237 宿傩的式神/领域核心精灵。
 *
 * 素材来自 grok-workspace-1.zip：4 帧 128x128，水平拼成 512x128。
 */
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class ShikigamiSprite extends MobSprite {

	private static final int FRAME = 128;

	public ShikigamiSprite() {
		super();
		perspectiveRaise = 8 / 16f;
		texture( Assets.Sprites.SHIKIGAMI );

		TextureFilm frames = new TextureFilm( texture, FRAME, FRAME );

		idle = new Animation( 8, true );
		idle.frames( frames, 0, 1, 2, 3 );

		run = idle.clone();

		attack = new Animation( 8, false );
		attack.frames( frames, 0, 1, 2, 3, 0 );

		die = new Animation( 10, false );
		die.frames( frames, 3, 2, 1, 0 );

		play( idle );
	}
}
